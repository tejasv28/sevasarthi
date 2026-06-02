package com.sevasarthi.backend.repository;

import com.sevasarthi.backend.models.Service;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends MongoRepository<Service, String> {
    List<Service> findByCategoryAndIsActiveTrue(String category);
    List<Service> findByProviderId(String providerId);
}
