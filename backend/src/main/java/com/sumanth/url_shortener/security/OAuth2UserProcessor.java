package com.sumanth.url_shortener.security;

import com.sumanth.url_shortener.model.OAuthUser;
import com.sumanth.url_shortener.model.User;
import com.sumanth.url_shortener.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OAuth2UserProcessor {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2UserProcessor.class);
    private final UserRepository userRepository;

    public OAuth2UserProcessor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public OAuthUser processUser(String provider, Map<String, Object> attributes) {
        String providerId;
        String username;
        String email;
        String avatarUrl;

        if ("github".equals(provider)) {
            providerId = getAttr(attributes, "id", null);
            username = getAttr(attributes, "login", "GitHub User");
            email = getAttr(attributes, "email", "");
            avatarUrl = getAttr(attributes, "avatar_url", "");
        } else if ("google".equals(provider)) {
            providerId = getAttr(attributes, "sub", null);
            username = getAttr(attributes, "name", "Google User");
            email = getAttr(attributes, "email", "");
            avatarUrl = getAttr(attributes, "picture", "");
        } else {
            logger.error("Unsupported OAuth provider: {}", provider);
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_provider"), "Unsupported provider: " + provider);
        }

        if (providerId == null) {
            logger.error("Provider ID not found for provider: {}", provider);
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_provider_id"), "Provider ID not found");
        }

        final String finalProviderId = providerId;
        final String finalUsername = username;
        final String finalEmail = email;
        final String finalAvatarUrl = avatarUrl;

        User user = userRepository.findByProviderAndProviderId(provider, finalProviderId)
                .map(existing -> {
                    boolean changed = false;
                    if (!finalUsername.equals(existing.getUsername())) {
                        existing.setUsername(finalUsername);
                        changed = true;
                    }
                    if (!finalAvatarUrl.equals(existing.getAvatarUrl())) {
                        existing.setAvatarUrl(finalAvatarUrl);
                        changed = true;
                    }
                    if (!finalEmail.equals(existing.getEmail())) {
                        existing.setEmail(finalEmail);
                        changed = true;
                    }
                    return changed ? userRepository.save(existing) : existing;
                })
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .provider(provider)
                                .providerId(finalProviderId)
                                .username(finalUsername)
                                .email(finalEmail)
                                .avatarUrl(finalAvatarUrl)
                                .build()));

        logger.info("Upserted user: provider={}, providerId={}, username={}", provider, finalProviderId,
                user.getUsername());
        return new OAuthUser(user.getUsername(), user.getEmail(), user.getAvatarUrl(), user.getProvider());
    }

    private String getAttr(Map<String, Object> attrs, String key, String defaultValue) {
        Object v = attrs.get(key);
        return v == null ? defaultValue : v.toString();
    }
}
