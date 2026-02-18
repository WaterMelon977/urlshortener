package com.sumanth.url_shortener.security;

import com.sumanth.url_shortener.model.OAuthUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);
    private final OAuth2UserProcessor processor;

    public CustomOAuth2UserService(OAuth2UserProcessor processor) {
        this.processor = processor;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();

        logger.info("Loading user from provider: {}", provider);

        OAuthUser oAuthUser = processor.processUser(provider, oAuth2User.getAttributes());

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("oAuthUser", oAuthUser);

        String nameAttributeKey = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        return new DefaultOAuth2User(oAuth2User.getAuthorities(), attributes, nameAttributeKey);
    }
}
