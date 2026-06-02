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
@Document(collection = "users")
public class User {
    @Id
    private String id;

    private String name;
    private String email;
    private String password;
    private String phone;
    
    @Builder.Default
    private String role = "user"; // Using string for now, could be an Enum
    
    private String avatar;
    private Address address;
    
    @Builder.Default
    private boolean isVerified = false;
    
    @Builder.Default
    private boolean isActive = true;
    
    private String refreshToken;
    private List<Object> pushSubscriptions;
    
    @Builder.Default
    private String dashboard = "/user/dashboard";
    
    private String resetPasswordOtp;
    private Date resetPasswordOtpExpires;
    
    @Builder.Default
    private int otpAttempts = 0;
    
    private String signupOtp;
    private Date signupOtpExpires;
    
    @Builder.Default
    private int signupOtpAttempts = 0;
    
    @Builder.Default
    private boolean isEmailVerified = false;
    
    @Builder.Default
    private boolean isPhoneVerified = false;
    
    private Date createdAt;
    private Date updatedAt;
}
