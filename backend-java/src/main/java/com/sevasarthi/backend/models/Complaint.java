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
    
    private String userId; // Maps to User ObjectId
    private String type;
    private String bookingId; // Maps to Booking ObjectId
    private String rentalId; // Maps to Rental ObjectId
    private String providerId; // Maps to Provider ObjectId
    
    private String category;
    private String description;
    
    @Builder.Default
    private String proofImage = "";
    
    @Builder.Default
    private String status = "pending"; // pending, in_review, resolved, rejected, reopened, escalated
    
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
        
        private String changedBy; // Maps to User ObjectId
    }
}
