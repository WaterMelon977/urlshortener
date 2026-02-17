package com.sumanth.url_shortener.security;

import com.sumanth.url_shortener.model.OAuthUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        logger.info("Loading user from provider: {}", registrationId);
        logger.debug("User attributes: {}", oAuth2User.getAttributes());

        OAuthUser oAuthUser = extractUser(registrationId, oAuth2User.getAttributes());

        // Store the OAuthUser in the attributes so the success handler can access it
        Map<String, Object> attributes = new java.util.HashMap<>(oAuth2User.getAttributes());
        attributes.put("oAuthUser", oAuthUser);

        String nameAttributeKey = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                attributes,
                nameAttributeKey);
    }

    private OAuthUser extractUser(String registrationId, Map<String, Object> attributes) {
        OAuthUser user = new OAuthUser();
        user.setProvider(registrationId);

        try {
            switch (registrationId) {
                case "github" -> {
                    user.setUsername(getAttributeAsString(attributes, "login", "GitHub User"));
                    user.setEmail(getAttributeAsString(attributes, "email", ""));
                    user.setAvatar(getAttributeAsString(attributes, "avatar_url", ""));
                }
                case "google" -> {
                    user.setUsername(getAttributeAsString(attributes, "name", "Google User"));
                    user.setEmail(getAttributeAsString(attributes, "email", ""));
                    user.setAvatar(getAttributeAsString(attributes, "picture", ""));
                }
                default -> {
                    logger.error("Unsupported OAuth provider: {}", registrationId);
                    throw new OAuth2AuthenticationException(new OAuth2Error("invalid_provider"),
                            "Unsupported provider: " + registrationId);
                }
            }
        } catch (Exception e) {
            logger.error("Error extracting user from {}: {}", registrationId, e.getMessage(), e);
            throw new OAuth2AuthenticationException(new OAuth2Error("extraction_error"), "Failed to extract user info",
                    e);
        }

        logger.info("Extracted user: username={}, email={}, provider={}",
                user.getUsername(), user.getEmail(), user.getProvider());

        return user;
    }

    private String getAttributeAsString(Map<String, Object> attributes, String key, String defaultValue) {
        Object value = attributes.get(key);
        if (value == null) {
            logger.warn("Attribute '{}' is null, using default: '{}'", key, defaultValue);
            return defaultValue;
        }
        return value.toString();
    }
}
