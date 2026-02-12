package com.sumanth.url_shortener.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "url_mappings")
public class UrlMapping {
    @Id
    private String id;

    private String longUrl;

    @Indexed(unique = true)
    private String urlHash;

    @Indexed(unique = true)
    private String shortCode;
    private Instant createdAt;
    private Instant updatedAt;
    private long clickCount;

    public String getShortCode() {
        return shortCode;
    }
}
