package com.sumanth.url_shortener.security;

import com.sumanth.url_shortener.model.OAuthUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final String frontendUrl;

    public OAuth2LoginSuccessHandler(
            JwtService jwtService,
            @Value("${FRONTEND_URL:http://localhost:3000}") String frontendUrl) {
        this.jwtService = jwtService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        OAuthUser oAuthUser = (OAuthUser) oAuth2User.getAttributes().get("oAuthUser");

        if (oAuthUser == null) {
            // Fallback for OIDC providers (like Google) or standard OAuth providers
            // where CustomOAuth2UserService was not engaged.
            oAuthUser = extractUserFromPrincipal(authentication);
        }

        String token = jwtService.generateToken(
                oAuthUser.getUsername(),
                oAuthUser.getProvider(),
                oAuthUser.getAvatar(),
                oAuthUser.getEmail());

        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // 1 day
        // SameSite=None must be set via header since Cookie API doesn't support it
        response.addHeader("Set-Cookie",
                String.format("jwt=%s; Path=/; Max-Age=%d; HttpOnly; Secure; SameSite=None",
                        token, 24 * 60 * 60));

        response.sendRedirect(frontendUrl);
    }

    private OAuthUser extractUserFromPrincipal(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            String registrationId = oauthToken.getAuthorizedClientRegistrationId();
            OAuth2User user = oauthToken.getPrincipal();
            Map<String, Object> attributes = user.getAttributes();

            OAuthUser extracted = new OAuthUser();
            extracted.setProvider(registrationId);

            if ("google".equals(registrationId)) {
                extracted.setUsername((String) attributes.getOrDefault("name", "Google User"));
                extracted.setEmail((String) attributes.getOrDefault("email", ""));
                extracted.setAvatar((String) attributes.getOrDefault("picture", ""));
            } else if ("github".equals(registrationId)) {
                extracted.setUsername((String) attributes.getOrDefault("login", "GitHub User"));
                extracted.setEmail((String) attributes.getOrDefault("email", ""));
                extracted.setAvatar((String) attributes.getOrDefault("avatar_url", ""));
            } else {
                // Generic fallback
                extracted.setUsername(user.getName());
                extracted.setEmail("");
                extracted.setAvatar("");
            }
            return extracted;
        }
        return new OAuthUser("Unknown", "", "", "unknown");
    }
}
