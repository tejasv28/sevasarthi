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
    private double baseRate;
    private double discount;
    private double tax;
    private double totalAmount;
}
