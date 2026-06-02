package com.sevasarthi.backend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "notifications")
public class Notification {
    @Id
    private String id;
    
    private String userId; // Maps to User ObjectId
    
    private String title;
    private String message;
    
    @Builder.Default
    private String type = "system";
    
    @Builder.Default
    private boolean read = false;
    
    private Map<String, Object> metadata;
    
    private Date createdAt;
    private Date updatedAt;
}
