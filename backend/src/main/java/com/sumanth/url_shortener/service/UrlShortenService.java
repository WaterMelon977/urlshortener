package com.sumanth.url_shortener.service;

import com.sumanth.url_shortener.model.UrlMapping;
import com.sumanth.url_shortener.repository.UrlMappingRepository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.sumanth.url_shortener.util.Base62Encoder;

@Service
public class UrlShortenService {

    private final UrlMappingRepository repo;
    private final CounterService counterService;

    public UrlShortenService(UrlMappingRepository repo, CounterService counterService) {
        this.repo = repo;
        this.counterService = counterService;

    }

    public UrlMapping shortenUrl(String longUrl) {
        String normalisedLongUrl = longUrl.trim().toLowerCase();
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

    public UrlMapping expandUrl(String shortCode) {
        Optional<UrlMapping> existing = repo.findByShortCode(shortCode);
        if (existing.isPresent()) {
            return existing.get();
        } else {
            throw new RuntimeException("Short code not found");
        }
    }

}
