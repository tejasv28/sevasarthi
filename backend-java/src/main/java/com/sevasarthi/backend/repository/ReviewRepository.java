package com.sevasarthi.backend.repository;

import com.sevasarthi.backend.models.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByProviderId(String providerId);
}
