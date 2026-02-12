package com.sumanth.url_shortener.service;

import com.sumanth.url_shortener.model.Counter;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CounterService {

    private final MongoTemplate mongoTemplate;

    public CounterService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public long getNextSequence() {
        Query query = new Query(Criteria.where("_id").is("url_sequence"));
        Update update = new Update().inc("seq", 1);
        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true).upsert(true);

        Counter counter = mongoTemplate.findAndModify(query, update, options, Counter.class);

        return !Objects.isNull(counter) ? counter.getSeq() : 1;
    }
}
