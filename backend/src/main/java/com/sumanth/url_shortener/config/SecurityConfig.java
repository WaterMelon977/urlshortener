package com.sumanth.url_shortener.config;

import com.sumanth.url_shortener.security.CustomOAuth2UserService;
import com.sumanth.url_shortener.security.JwtAuthenticationFilter;
import com.sumanth.url_shortener.security.OAuth2LoginSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final CustomOAuth2UserService customOAuth2UserService;
        private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final String frontendUrl;

        public SecurityConfig(
                        CustomOAuth2UserService customOAuth2UserService,
                        OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler,
                        JwtAuthenticationFilter jwtAuthenticationFilter,
                        @Value("${FRONTEND_URL}") String frontendUrl) {
                this.customOAuth2UserService = customOAuth2UserService;
                this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
                this.frontendUrl = frontendUrl;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .formLogin(form -> form.disable())
                                .httpBasic(basic -> basic.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/oauth2/**",
                                                                "/login/oauth2/**",
                                                                "/logout",
                                                                "/shorten",
                                                                "/{shortCode}",
                                                                "/{shortCode}/stats")
                                                .permitAll()
                                                .requestMatchers("/api/**").authenticated()
                                                .anyRequest().permitAll())
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        // For API endpoints, return 401 instead of redirecting to login
                                                        if (request.getRequestURI().startsWith("/api/")) {
                                                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                                                response.setContentType("application/json");
                                                                response.getWriter()
                                                                                .write("{\"error\":\"Unauthorized\"}");
                                                        } else {
                                                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                                                                                "Unauthorized");
                                                        }
                                                }))
                                .oauth2Login(oauth2 -> oauth2
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService))
                                                .successHandler(oAuth2LoginSuccessHandler))
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessHandler(customLogoutSuccessHandler())
                                                .deleteCookies("jwt")
                                                .invalidateHttpSession(true))
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public LogoutSuccessHandler customLogoutSuccessHandler() {
                return (request, response, authentication) -> {
                        // Clear the JWT cookie
                        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("jwt", null);
                        cookie.setPath("/");
                        cookie.setMaxAge(0);
                        cookie.setHttpOnly(true);
                        response.addCookie(cookie);

                        // Redirect to frontend
                        response.sendRedirect(frontendUrl);
                };
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(List.of(frontendUrl));
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);
                config.setExposedHeaders(List.of("Location"));

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return source;
        }
}
