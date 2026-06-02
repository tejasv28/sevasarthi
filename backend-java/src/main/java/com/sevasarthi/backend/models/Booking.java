package com.sevasarthi.backend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "bookings")
public class Booking {
    @Id
    private String id;
    
    private String userId; // Maps to User ObjectId
    private String providerId; // Maps to Provider ObjectId
    private String serviceId; // Maps to Service ObjectId
    
    @Builder.Default
    private String serviceName = "";
    
    @Builder.Default
    private String status = "pending";
    
    private Date scheduledDate;
    private String scheduledTime;
    private String address;
    
    @Builder.Default
    private String instructions = "";
    
    private List<String> photos;
    
    @Builder.Default
    private String paymentMethod = "online";
    
    @Builder.Default
    private String couponCode = "";
    
    @Builder.Default
    private double baseRate = 0;
    
    @Builder.Default
    private double platformFee = 49;
    
    @Builder.Default
    private double discount = 0;
    
    @Builder.Default
    private double tax = 0;
    
    private double totalAmount;
    
    private List<TrackingStep> trackingSteps;
    
    @Builder.Default
    private boolean isProviderInactive = false;
    
    private String reassignedFrom;
    
    @Builder.Default
    private String otp = "";
    
    @Builder.Default
    private String completionOtp = "";
    
    @Builder.Default
    private boolean isReviewed = false;
    
    @Builder.Default
    private String paymentStatus = "pending";
    
    @Builder.Default
    private String razorpayOrderId = "";
    
    @Builder.Default
    private String razorpayPaymentId = "";

    private Date createdAt;
    private Date updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TrackingStep {
        private String status;
        
        @Builder.Default
        private Date timestamp = new Date();
        
        @Builder.Default
        private String note = "";
    }
}
