package com.sumanth.url_shortener.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OAuthUser {
    private String username;
    private String email;
    private String avatar;
    private String provider;
}
