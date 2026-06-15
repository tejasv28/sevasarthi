package com.sevasarthi.backend.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "providers")
public class Provider {
    @Id
    private String id;
    private String userId; 

    
    @Builder.Default
    private String businessType = "individual";
    private String businessName;
    private String ownerName;
    private String phone;
    private String city;
    private String fullAddress;
    private String pincode;

    
    @Builder.Default
    private String primaryCategory = "Home Maintenance";

    
    @Builder.Default
    private String verificationStatus = "pending";
    private String rejectionReason;
    private Date approvedAt;

    private Documents documents;

    
    @Builder.Default
    private String category = "Home Maintenance";
    @Builder.Default
    private String title = "Service Professional";
    private String bio;
    private List<String> skills;
    private List<Certification> certifications;
    private List<PortfolioItem> portfolio;
    
    @Builder.Default
    private String experience = "1 yr";
    
    @Builder.Default
    private Double pricePerHour = 0.0;

    
    @Builder.Default
    private double rating = 0;
    
    @Builder.Default
    private Map<String, Integer> ratingBreakdown = new HashMap<>(Map.of("1", 0, "2", 0, "3", 0, "4", 0, "5", 0));
    
    @Builder.Default
    private int reviewCount = 0;
    
    @Builder.Default
    private int jobsCompleted = 0;
    
    private List<String> badges;
    
    @Builder.Default
    private boolean isTopRated = false;

    
    @Builder.Default
    private boolean isAvailable = true;
    private WorkingHours workingHours;

    
    @Builder.Default
    private boolean isVerifiedProvider = false;
    private List<String> verificationDocs;

    
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint location;

    
    @Builder.Default
    private int trustScore = 100;
    
    private List<Warning> warnings = new ArrayList<>();
    
    @Builder.Default
    private boolean isSuspended = false;
    private Date suspendedUntil;
    
    private List<Penalty> penalties = new ArrayList<>();

    private Date createdAt;
    private Date updatedAt;

    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Documents {
        private String idProof;
        private String idProofType; 
        private String profilePhoto;
        private String businessLicense;
        private String gst;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Certification {
        private String title;
        private String issuer;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortfolioItem {
        private String image;
        private String label;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkingHours {
        private String start = "09:00";
        private String end = "18:00";
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Warning {
        private String reason;
        private Date issuedAt = new Date();
        private String complaintId; 
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Penalty {
        private double amount;
        private String reason;
        private Date appliedAt = new Date();
        private String complaintId; 
    }
}
