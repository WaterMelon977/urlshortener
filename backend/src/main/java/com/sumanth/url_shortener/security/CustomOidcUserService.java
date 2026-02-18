package com.sumanth.url_shortener.security;

import com.sumanth.url_shortener.model.OAuthUser;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CustomOidcUserService extends OidcUserService {

    private final OAuth2UserProcessor processor;

    public CustomOidcUserService(OAuth2UserProcessor processor) {
        this.processor = processor;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();

        OAuthUser oAuthUser = processor.processUser(provider, oidcUser.getAttributes());

        // Inject oAuthUser into attributes so the success handler can read it
        Map<String, Object> attributes = new HashMap<>(oidcUser.getAttributes());
        attributes.put("oAuthUser", oAuthUser);

        String nameAttributeKey = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        return new DefaultOidcUser(oidcUser.getAuthorities(), oidcUser.getIdToken(), oidcUser.getUserInfo(),
                nameAttributeKey);
    }
}
