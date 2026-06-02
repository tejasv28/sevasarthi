package com.sevasarthi.backend.repository;

import com.sevasarthi.backend.models.Provider;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProviderRepository extends MongoRepository<Provider, String> {
    Optional<Provider> findByUserId(String userId);
}
