package com.sumanth.url_shortener.service;

import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class RedisStreamPublisher {

    private static final String STREAM_KEY = "url:click:stream";
    private final StringRedisTemplate redisTemplate;

    public RedisStreamPublisher(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void publishClickEvent(String shortCode) {
        try {
            Map<String, String> fields = new HashMap<>();
            fields.put("shortCode", shortCode);
            fields.put("timestamp", Instant.now().toString());

            MapRecord<String, String, String> record = StreamRecords.newRecord()
                    .ofStrings(fields)
                    .withStreamKey(STREAM_KEY);

            redisTemplate.opsForStream().add(record);
        } catch (Exception e) {
            // Best-effort: Log error but don't block main flow
            System.err.println("Failed to publish click event for " + shortCode + ": " + e.getMessage());
        }
    }
}
