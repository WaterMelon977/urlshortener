package com.sumanth.url_shortener.exception;

import org.springframework.http.HttpStatus;

public class UrlNotFoundException extends UrlShortenerException {
    public UrlNotFoundException(String shortCode) {
        super("URL not found for short code: " + shortCode, "URL_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}
