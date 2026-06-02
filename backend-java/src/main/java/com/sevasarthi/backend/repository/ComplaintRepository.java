package com.sevasarthi.backend.repository;

import com.sevasarthi.backend.models.Complaint;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends MongoRepository<Complaint, String> {
    List<Complaint> findByUserIdOrderByCreatedAtDesc(String userId);
}
