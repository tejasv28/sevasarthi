package com.sevasarthi.backend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "complaints")
public class Complaint {
    @Id
    private String id;
    
    private String ticketId;
    
    private String userId; 
    private String type;
    private String bookingId; 
    private String rentalId; 
    private String providerId; 
    
    private String category;
    private String description;
    
    @Builder.Default
    private String proofImage = "";
    
    @Builder.Default
    private String status = "pending"; 
    
    @Builder.Default
    private String adminResponse = "";
    
    @Builder.Default
    private String adminAction = "";
    
    private Map<String, Object> actionDetails;
    
    @Builder.Default
    private List<StatusHistory> statusHistory = new ArrayList<>();
    
    @Builder.Default
    private int reopenCount = 0;
    
    @Builder.Default
    private String reopenReason = "";
    
    private Date resolvedAt;
    
    private Date createdAt;
    private Date updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusHistory {
        private String status;
        
        @Builder.Default
        private Date timestamp = new Date();
        
        @Builder.Default
        private String note = "";
        
        private String changedBy; 
    }
}
