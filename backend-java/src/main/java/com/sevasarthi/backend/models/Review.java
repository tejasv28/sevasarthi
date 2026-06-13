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
@Document(collection = "reviews")
public class Review {
    @Id
    private String id;
    
    private String userId; // Maps to User ObjectId
    private String providerId; // Maps to Provider ObjectId
    private String bookingId; // Maps to Booking ObjectId
    
    private Integer rating;
    
    @Builder.Default
    private String comment = "";

    private Date createdAt;
    private Date updatedAt;
}
