package com.sevasarthi.backend.repository;

import com.sevasarthi.backend.models.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findByUserId(String userId);
    long countByUserId(String userId);
    List<Booking> findByProviderId(String providerId);
    long countByProviderId(String providerId);
    List<Booking> findByStatus(String status);
    
    org.springframework.data.domain.Page<Booking> findByUserId(String userId, org.springframework.data.domain.Pageable pageable);
    org.springframework.data.domain.Page<Booking> findByUserIdAndStatus(String userId, String status, org.springframework.data.domain.Pageable pageable);
    
    long countByUserIdAndStatusNotIn(String userId, List<String> statuses);
    long countByUserIdAndStatus(String userId, String status);
}
