package com.sumanth.url_shortener.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sumanth.url_shortener.controller.dto.ShortUrlDto;
import com.sumanth.url_shortener.controller.dto.UrlRequestDto;
import com.sumanth.url_shortener.model.UrlMapping;
import com.sumanth.url_shortener.service.UrlShortenService;

@RestController
@RequestMapping
public class UrlController {

    private UrlShortenService urlShortenService;

    public UrlController(UrlShortenService urlShortenService) {
        this.urlShortenService = urlShortenService;

    }

    @PostMapping("/shorten")
    public ResponseEntity<?> shortenUrl(@RequestBody UrlRequestDto request) {
        UrlMapping urlMapping = urlShortenService.shortenUrl(request.getLongUrl());
        return ResponseEntity.ok(new ShortUrlDto(urlMapping.getShortCode()));
    }

    @GetMapping(value = "/{shortCode}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UrlMapping> getUrlJson(@PathVariable String shortCode) {
        UrlMapping urlMapping = urlShortenService.expandUrl(shortCode, true);
        return ResponseEntity.ok(urlMapping);
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> expandUrl(@PathVariable String shortCode) {
        UrlMapping urlMapping = urlShortenService.expandUrl(shortCode, true);
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .header(HttpHeaders.LOCATION, urlMapping.getLongUrl())
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.LOCATION)
                .build();
    }

    @GetMapping("/{shortCode}/stats")
    public ResponseEntity<Long> getClickCount(@PathVariable String shortCode) {
        UrlMapping urlMapping = urlShortenService.expandUrl(shortCode, false);
        if (urlMapping == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(urlMapping.getClickCount());
    }

}
