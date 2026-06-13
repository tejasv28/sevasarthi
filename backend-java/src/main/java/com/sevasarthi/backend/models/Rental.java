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
@Document(collection = "rentals")
public class Rental {
    @Id
    private String id;
    
    private String userId; // Maps to User ObjectId
    private String toolId; // Maps to Tool ObjectId
    
    private String toolName;
    private Integer days;
    
    private double subtotal;
    
    @Builder.Default
    private double deliveryFee = 99;
    
    @Builder.Default
    private double tax = 0;
    
    @Builder.Default
    private double refundableDeposit = 500;
    
    private double total;
    
    private DeliveryDetails deliveryDetails;
    
    @Builder.Default
    private String status = "pending";
    
    @Builder.Default
    private String deliveryOtp = "";
    
    @Builder.Default
    private String returnOtp = "";
    
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
    public static class DeliveryDetails {
        private String fullName;
        private String phone;
        private String addressLine1;
        @Builder.Default
        private String addressLine2 = "";
        private String city;
        private String pincode;
        @Builder.Default
        private String landmark = "";
        private Date deliveryDate;
        @Builder.Default
        private String deliveryWindow = "10:00 - 12:00";
        @Builder.Default
        private String idType = "Aadhaar";
        private String idNumber;
        @Builder.Default
        private String notes = "";
    }
}
