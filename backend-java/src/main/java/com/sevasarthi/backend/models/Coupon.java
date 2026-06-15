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
@Document(collection = "coupons")
public class Coupon {
    @Id
    private String id;
    
    @Builder.Default
    private boolean isBannerOnly = false;
    
    private String title;
    private String subtitle;
    private String imageUrl;
    private String targetUrl;
    
    @Builder.Default
    private boolean showOnHome = false;
    
    @Builder.Default
    private String userType = "all"; 
    
    private String code;
    private String discountType; 
    private Double discountValue;
    
    @Builder.Default
    private Double minOrderAmount = 0.0;
    
    private Double maxDiscount; 
    private Integer maxUses; 
    
    @Builder.Default
    private Integer usedCount = 0;
    
    @Builder.Default
    private boolean isActive = true;
    
    private Date expiresAt;
    
    @Builder.Default
    private String description = "";

    private Date createdAt;
    private Date updatedAt;
}
