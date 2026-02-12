package com.sumanth.url_shortener.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sumanth.url_shortener.controller.dto.ShortUrlDto;
import com.sumanth.url_shortener.model.UrlMapping;
import com.sumanth.url_shortener.service.UrlShortenService;

@RestController
@RequestMapping("/url")
public class UrlController {

    private UrlShortenService urlShortenService;

    public UrlController(UrlShortenService urlShortenService) {
        this.urlShortenService = urlShortenService;

    }

    @PostMapping
    public ShortUrlDto shortenUrl(@RequestBody String longUrl) {
        UrlMapping urlMapping = urlShortenService.shortenUrl(longUrl);
        return new ShortUrlDto(urlMapping.getShortCode());
    }

    @GetMapping("/{shortCode}")
    public String expandUrl(@PathVariable String shortCode) {
        UrlMapping urlMapping = urlShortenService.expandUrl(shortCode);
        return urlMapping.getLongUrl();
    }

}
