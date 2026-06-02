package com.sevasarthi.backend.repository;

import com.sevasarthi.backend.models.Tool;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToolRepository extends MongoRepository<Tool, String> {
    List<Tool> findByCategoryAndStatus(String category, String status);
    List<Tool> findByOwnerId(String ownerId);
}
