package com.sumanth.url_shortener.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) ((UsernamePasswordAuthenticationToken) authentication)
                .getDetails();

        return ResponseEntity.ok(Map.of(
                "username", details.get("username"),
                "avatar", details.get("avatar"),
                "provider", details.get("provider")));
    }
}
