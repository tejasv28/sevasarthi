package com.sevasarthi.backend.controllers;

import com.sevasarthi.backend.dto.ApiResponse;
import com.sevasarthi.backend.models.Booking;
import com.sevasarthi.backend.models.Provider;
import com.sevasarthi.backend.models.Review;
import com.sevasarthi.backend.repository.BookingRepository;
import com.sevasarthi.backend.repository.ProviderRepository;
import com.sevasarthi.backend.repository.ReviewRepository;
import com.sevasarthi.backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    ProviderRepository providerRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    private UserDetailsImpl getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            return (UserDetailsImpl) principal;
        }
        return null;
    }

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody Review request) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (request.getProviderId() == null || request.getBookingId() == null || request.getRating() < 1 || request.getRating() > 5) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(400, null, "ProviderId, BookingId, and valid rating are required."));
        }

        Optional<Provider> providerOpt = providerRepository.findById(request.getProviderId());
        if (providerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, null, "Provider not found."));
        }

        Optional<Booking> bookingOpt = bookingRepository.findById(request.getBookingId());
        if (bookingOpt.isEmpty() || !bookingOpt.get().getUserId().equals(currentUser.getId()) || !"completed".equals(bookingOpt.get().getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(403, null, "You can only review a job after it is completed."));
        }

        Optional<Review> existingReview = reviewRepository.findByBookingId(request.getBookingId());
        if (existingReview.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(409, null, "You have already reviewed this job."));
        }

        Review review = Review.builder()
                .userId(currentUser.getId())
                .providerId(request.getProviderId())
                .bookingId(request.getBookingId())
                .rating(request.getRating())
                .comment(request.getComment() != null ? request.getComment() : "")
                .createdAt(new Date())
                .build();

        Booking booking = bookingOpt.get();
        // Just mark logically that it is reviewed in booking (Optional: Add isReviewed to Booking schema)

        Provider provider = providerOpt.get();
        if (provider.getRatingBreakdown() == null) provider.setRatingBreakdown(new HashMap<>());
        
        String ratingKey = String.valueOf(request.getRating());
        provider.getRatingBreakdown().put(ratingKey, provider.getRatingBreakdown().getOrDefault(ratingKey, 0) + 1);
        
        provider.setReviewCount(provider.getReviewCount() + 1);

        double totalStars = 0;
        for (int i = 1; i <= 5; i++) {
            totalStars += i * provider.getRatingBreakdown().getOrDefault(String.valueOf(i), 0);
        }
        
        double newRating = Math.round((totalStars / provider.getReviewCount()) * 10.0) / 10.0;
        provider.setRating(newRating);
        provider.setTopRated(provider.getRating() >= 4.8 && provider.getReviewCount() >= 10);
        
        providerRepository.save(provider);
        Review savedReview = reviewRepository.save(review);

        Map<String, Object> data = new HashMap<>();
        data.put("review", savedReview);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, data, "Review submitted."));
    }

    @GetMapping("/provider/{id}")
    public ResponseEntity<?> getProviderReviews(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        
        Query query = new Query(Criteria.where("providerId").is(id));
        long total = mongoTemplate.count(query, Review.class);
        
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        query.with(PageRequest.of(page - 1, limit));
        
        List<Review> reviews = mongoTemplate.find(query, Review.class);

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("total", total);
        pagination.put("page", page);
        pagination.put("pages", (int) Math.ceil((double) total / limit));

        Map<String, Object> data = new HashMap<>();
        data.put("reviews", reviews);
        data.put("pagination", pagination);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Reviews retrieved."));
    }
}
