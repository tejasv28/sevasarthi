package com.sevasarthi.backend.dto;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class CreateBookingRequest {
    private String serviceId;
    private String providerId;
    private Date scheduledDate;
    private String scheduledTime;
    private String address;
    private String instructions;
    private List<String> photos;
    private String paymentMethod;
    private String couponCode;
    private String serviceName;
    private Double platformFee;
    private Double baseRate;
    private Double discount;
    private Double tax;
    private Double totalAmount;
}
