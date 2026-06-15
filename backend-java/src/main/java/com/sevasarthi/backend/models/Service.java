package com.sevasarthi.backend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "services")
public class Service {
    @Id
    private String id;
    private String name;
    private String category;
    
    @Builder.Default
    private String description = "";
    
    @Builder.Default
    private String icon = "home_repair_service";
    
    private Double basePrice;
    
    @Builder.Default
    private String image = "";
    
    @Builder.Default
    private boolean isActive = true;
    
    private String providerId; 
    
    @Builder.Default
    private String approvalStatus = "approved"; 
    
    @Builder.Default
    private String rejectionReason = "";
    
    private WorkingHours workingHours;
    
    private Date createdAt;
    private Date updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WorkingHours {
        @Builder.Default
        private String start = "09:00";
        @Builder.Default
        private String end = "18:00";
    }
}
