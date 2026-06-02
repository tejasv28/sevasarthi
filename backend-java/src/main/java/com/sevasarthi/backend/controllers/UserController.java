package com.sevasarthi.backend.controllers;

import com.sevasarthi.backend.dto.ApiResponse;
import com.sevasarthi.backend.models.Booking;
import com.sevasarthi.backend.models.Rental;
import com.sevasarthi.backend.models.User;
import com.sevasarthi.backend.repository.BookingRepository;
import com.sevasarthi.backend.repository.RentalRepository;
import com.sevasarthi.backend.repository.UserRepository;
import com.sevasarthi.backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    RentalRepository rentalRepository;

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

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<User> userOpt = userRepository.findById(currentUser.getId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, null, "User not found"));
        }

        Map<String, Object> data = new HashMap<>();
        User user = userOpt.get();
        user.setPassword(null);
        user.setRefreshToken(null);
        data.put("user", user);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Profile retrieved."));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody User updatedUser) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<User> userOpt = userRepository.findById(currentUser.getId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, null, "User not found"));
        }

        User user = userOpt.get();
        if (updatedUser.getName() != null) user.setName(updatedUser.getName());
        if (updatedUser.getPhone() != null) user.setPhone(updatedUser.getPhone());
        if (updatedUser.getAvatar() != null) user.setAvatar(updatedUser.getAvatar());
        if (updatedUser.getAddress() != null) user.setAddress(updatedUser.getAddress());

        User savedUser = userRepository.save(user);
        savedUser.setPassword(null);
        savedUser.setRefreshToken(null);
        
        Map<String, Object> data = new HashMap<>();
        data.put("user", savedUser);
        
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Profile updated."));
    }

    @GetMapping("/bookings")
    public ResponseEntity<?> getMyBookings(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Booking> bookingPage;

        if (status != null && !status.isEmpty()) {
            bookingPage = bookingRepository.findByUserIdAndStatus(currentUser.getId(), status, pageable);
        } else {
            bookingPage = bookingRepository.findByUserId(currentUser.getId(), pageable);
        }

        // Ideally, we'd also populate Provider here using an Aggregation or manual lookups
        // For simplicity, we just return the bookings. If the frontend requires populated provider details,
        // it needs an aggregation pipeline or manual fetch.

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("total", bookingPage.getTotalElements());
        pagination.put("page", page);
        pagination.put("pages", bookingPage.getTotalPages());

        Map<String, Object> data = new HashMap<>();
        data.put("bookings", bookingPage.getContent());
        data.put("pagination", pagination);

        return ResponseEntity.ok(new ApiResponse<>(200, data, "Bookings retrieved."));
    }

    @GetMapping("/rentals")
    public ResponseEntity<?> getMyRentals() {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<Rental> rentals = rentalRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
        
        Map<String, Object> data = new HashMap<>();
        data.put("rentals", rentals);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Rentals retrieved."));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats() {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        String userId = currentUser.getId();

        long activeBookings = bookingRepository.countByUserIdAndStatusNotIn(userId, Arrays.asList("completed", "cancelled"));
        long completedBookings = bookingRepository.countByUserIdAndStatus(userId, "completed");
        long activeRentals = rentalRepository.countByUserIdAndStatusNotIn(userId, Arrays.asList("returned", "cancelled"));

        // Aggregate total spent
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("userId").is(userId).and("status").is("completed")),
                Aggregation.group().sum("totalAmount").as("totalSpent")
        );

        AggregationResults<Map> results = mongoTemplate.aggregate(agg, Booking.class, Map.class);
        Map result = results.getUniqueMappedResult();
        
        double totalSpent = 0.0;
        if (result != null && result.get("totalSpent") != null) {
            totalSpent = ((Number) result.get("totalSpent")).doubleValue();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("activeBookings", activeBookings);
        data.put("completedBookings", completedBookings);
        data.put("activeRentals", activeRentals);
        data.put("totalSpent", totalSpent);

        return ResponseEntity.ok(new ApiResponse<>(200, data, "Dashboard stats retrieved."));
    }
}
