package com.sevasarthi.backend.repository;

import com.sevasarthi.backend.models.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByUserIdOrderByCreatedAtDesc(String userId);
    Optional<Review> findByBookingId(String bookingId);
}
