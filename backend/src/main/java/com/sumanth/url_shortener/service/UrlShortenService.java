package com.sumanth.url_shortener.service;

import com.sumanth.url_shortener.model.UrlMapping;
import com.sumanth.url_shortener.repository.UrlMappingRepository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sumanth.url_shortener.exception.InvalidUrlException;
import com.sumanth.url_shortener.exception.UrlNotFoundException;
import com.sumanth.url_shortener.util.Base62Encoder;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;

@Service
public class UrlShortenService {

    private final UrlMappingRepository repo;
    private final CounterService counterService;
    private final StringRedisTemplate redisTemplate;
    private final MongoTemplate mongoTemplate;

    public UrlShortenService(UrlMappingRepository repo, CounterService counterService,
            StringRedisTemplate redisTemplate, MongoTemplate mongoTemplate) {
        this.repo = repo;
        this.counterService = counterService;
        this.redisTemplate = redisTemplate;
        this.mongoTemplate = mongoTemplate;
    }

    public UrlMapping shortenUrl(String longUrl) {
        if (longUrl == null || longUrl.trim().isEmpty()) {
            throw new InvalidUrlException("URL cannot be empty");
        }

        String normalisedLongUrl = normalizeAndValidateUrl(longUrl);
        String hash = hash(normalisedLongUrl);
        Optional<UrlMapping> existing = repo.findByUrlHash(hash);

        if (existing.isPresent()) {
            return existing.get();
        } else {
            long seq = counterService.getNextSequence();
            String shortCode = Base62Encoder.encode(seq);
            UrlMapping mapping = new UrlMapping(null, longUrl, hash, shortCode, Instant.now(), Instant.now(), 0);

            return repo.save(mapping);
        }

    }

    private String normalizeAndValidateUrl(String urlString) {
        try {
            // Basic format check
            String lowerCaseUrl = urlString.toLowerCase();
            if (!lowerCaseUrl.startsWith("http://") && !lowerCaseUrl.startsWith("https://")) {
                throw new InvalidUrlException("URL must start with http:// or https://");
            }

            URI uri = new URI(urlString);

            // Validate host
            if (uri.getHost() == null || uri.getHost().isEmpty()) {
                throw new InvalidUrlException("Invalid URL: Host cannot be empty");
            }

            // Normalize: Scheme and Host to lowercase, remove default ports
            String scheme = uri.getScheme().toLowerCase();
            String host = uri.getHost().toLowerCase();
            int port = uri.getPort();

            // Remove default ports
            if ((scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443)) {
                port = -1;
            }

            // Reconstruct URI with normalized components
            URI normalizedUri = new URI(scheme, uri.getUserInfo(), host, port, uri.getPath(), uri.getQuery(),
                    uri.getFragment());

            // Resolve dot segments (./ and ../)
            return normalizedUri.normalize().toString();

        } catch (URISyntaxException e) {
            throw new InvalidUrlException("Invalid URL format: " + e.getMessage());
        }
    }

    private String hash(String normalisedLongUrl) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalisedLongUrl.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public UrlMapping expandUrl(String shortCode, boolean incrementClickCount) {
        if (!incrementClickCount) {
            // For stats, bypass cache to get the latest click count
            return repo.findByShortCode(shortCode)
                    .orElseThrow(() -> new UrlNotFoundException(shortCode));
        }

        // 1. Check Redis
        String longUrl = redisTemplate.opsForValue().get(shortCode);

        if (longUrl == null) {
            // 2. Cache Miss: Fetch from DB
            UrlMapping mapping = repo.findByShortCode(shortCode)
                    .orElseThrow(() -> new UrlNotFoundException(shortCode));
            longUrl = mapping.getLongUrl();

            // Populate Cache (e.g., set TTL to 24 hours)
            redisTemplate.opsForValue().set(shortCode, longUrl, Duration.ofHours(24));
        }

        // 3. Increment Click Count in Mongo (Atomic)
        Query query = new Query(Criteria.where("shortCode").is(shortCode));
        Update update = new Update().inc("clickCount", 1);
        mongoTemplate.updateFirst(query, update, UrlMapping.class);

        // Required for redirection: Return object with longUrl.
        // Note: The returned object might not have the latest clickCount or other
        // fields if it came from cache.
        UrlMapping result = new UrlMapping();
        result.setLongUrl(longUrl);
        return result;
    }

}
