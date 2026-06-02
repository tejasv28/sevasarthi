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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "tools")
public class Tool {
    @Id
    private String id;
    
    private String name;
    
    @Builder.Default
    private String description = "";
    
    private String category;
    
    @Builder.Default
    private String condition = "Good";
    
    private double dailyRate;
    
    @Builder.Default
    private String image = "";
    
    private String ownerId; // Maps to User ObjectId
    
    @Builder.Default
    private String status = "available";
    
    @Builder.Default
    private boolean isVerified = false;
    
    @Builder.Default
    private double rating = 0;
    
    @Builder.Default
    private String distance = "0km";
    
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint location;

    private Date createdAt;
    private Date updatedAt;
}
