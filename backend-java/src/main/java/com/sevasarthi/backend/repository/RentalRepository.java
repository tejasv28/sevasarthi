package com.sevasarthi.backend.repository;

import com.sevasarthi.backend.models.Rental;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalRepository extends MongoRepository<Rental, String> {
    List<Rental> findByUserId(String userId);
    List<Rental> findByToolId(String toolId);
    List<Rental> findByUserIdOrderByCreatedAtDesc(String userId);
    
    long countByUserIdAndStatusNotIn(String userId, List<String> statuses);
}
