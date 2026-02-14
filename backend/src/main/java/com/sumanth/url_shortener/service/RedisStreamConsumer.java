package com.sumanth.url_shortener.service;

import com.sumanth.url_shortener.model.UrlMapping;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RedisStreamConsumer {

    private static final String STREAM_KEY = "url:click:stream";
    private final StringRedisTemplate redisTemplate;
    private final MongoTemplate mongoTemplate;

    public RedisStreamConsumer(StringRedisTemplate redisTemplate, MongoTemplate mongoTemplate) {
        this.redisTemplate = redisTemplate;
        this.mongoTemplate = mongoTemplate;
    }

    @Scheduled(fixedDelay = 30000) // 30 seconds
    public void consumeClickEvents() {
        try {
            // Read messages from the beginning of the stream
            List<MapRecord<String, Object, Object>> messages = redisTemplate.opsForStream()
                    .read(StreamOffset.fromStart(STREAM_KEY));

            if (messages == null || messages.isEmpty()) {
                return;
            }

            Map<String, Long> shortCodeCounts = new HashMap<>();
            List<String> messageIds = new ArrayList<>();

            for (MapRecord<String, Object, Object> message : messages) {
                Map<Object, Object> value = message.getValue();
                String shortCode = (String) value.get("shortCode");

                if (shortCode != null) {
                    shortCodeCounts.put(shortCode, shortCodeCounts.getOrDefault(shortCode, 0L) + 1);
                }
                messageIds.add(message.getId().toString());
            }

            if (!shortCodeCounts.isEmpty()) {
                BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, UrlMapping.class);

                for (Map.Entry<String, Long> entry : shortCodeCounts.entrySet()) {
                    Query query = new Query(Criteria.where("shortCode").is(entry.getKey()));
                    Update update = new Update().inc("clickCount", entry.getValue());
                    bulkOps.updateOne(query, update);
                }

                bulkOps.execute();
            }

            // Acknowledge/Delete processed messages
            if (!messageIds.isEmpty()) {
                redisTemplate.opsForStream().delete(STREAM_KEY, messageIds.toArray(new String[0]));
            }

        } catch (Exception e) {
            System.err.println("Error consuming click events: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
