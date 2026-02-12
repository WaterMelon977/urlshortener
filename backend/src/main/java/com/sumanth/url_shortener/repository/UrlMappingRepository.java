package com.sumanth.url_shortener.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.sumanth.url_shortener.model.UrlMapping;

@Repository
public interface UrlMappingRepository extends MongoRepository<UrlMapping, String> {

    Optional<UrlMapping> findByUrlHash(String urlHash);

    Optional<UrlMapping> findByShortCode(String shortCode);

}
