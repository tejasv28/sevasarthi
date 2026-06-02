package com.sevasarthi.backend.dto;

import com.sevasarthi.backend.models.Address;
import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String phone;
    private String role;
    private Address address;
    private String signupMethod;
    private String otpToken;

    // Provider fields
    private String category;
    private String title;
    private String bio;
    private Object skills; // Could be List<String>
    private String experience;
    private String businessType;
    private String businessName;
    private String fullAddress;
    private String city;
    private String pincode;
    private String primaryCategory;
    private Object documents;
}
