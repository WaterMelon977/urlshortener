package com.sumanth.url_shortener.repository;

import com.sumanth.url_shortener.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
}
