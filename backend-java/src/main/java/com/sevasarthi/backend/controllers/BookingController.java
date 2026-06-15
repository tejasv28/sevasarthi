package com.sevasarthi.backend.controllers;

import com.sevasarthi.backend.dto.ApiResponse;
import com.sevasarthi.backend.dto.CreateBookingRequest;
import com.sevasarthi.backend.models.Booking;
import com.sevasarthi.backend.models.Provider;
import com.sevasarthi.backend.models.Service;
import com.sevasarthi.backend.repository.BookingRepository;
import com.sevasarthi.backend.repository.ProviderRepository;
import com.sevasarthi.backend.repository.ServiceRepository;
import com.sevasarthi.backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    ProviderRepository providerRepository;

    @Autowired
    ServiceRepository serviceRepository;

    private UserDetailsImpl getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            return (UserDetailsImpl) principal;
        }
        return null;
    }

    private String generateOtp() {
        return String.valueOf((int)(Math.random() * 9000) + 1000); 
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody CreateBookingRequest request) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (request.getPhotos() != null && request.getPhotos().size() > 3) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(400, null, "You can upload a maximum of 3 photos."));
        }

        Optional<Provider> providerOpt = providerRepository.findById(request.getProviderId());
        if (providerOpt.isEmpty()) {
            providerOpt = providerRepository.findByUserId(request.getProviderId());
        }

        if (providerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, null, "Provider not found."));
        }

        String serviceName = request.getServiceName() != null ? request.getServiceName() : "";
        if (request.getServiceId() != null && serviceName.isEmpty()) {
            Optional<Service> serviceOpt = serviceRepository.findById(request.getServiceId());
            if (serviceOpt.isPresent()) {
                serviceName = serviceOpt.get().getName();
            }
        }

        Booking booking = Booking.builder()
                .userId(currentUser.getId())
                .providerId(providerOpt.get().getId())
                .serviceId(request.getServiceId())
                .serviceName(serviceName)
                .scheduledDate(request.getScheduledDate())
                .scheduledTime(request.getScheduledTime())
                .address(request.getAddress())
                .instructions(request.getInstructions() != null ? request.getInstructions() : "")
                .photos(request.getPhotos() != null ? request.getPhotos() : new ArrayList<>())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "online")
                .couponCode(request.getCouponCode() != null ? request.getCouponCode() : "")
                .baseRate(request.getBaseRate())
                .platformFee(request.getPlatformFee() != null ? request.getPlatformFee() : 49.0)
                .discount(request.getDiscount() != null ? request.getDiscount() : 0.0)
                .tax(request.getTax() != null ? request.getTax() : 0.0)
                .totalAmount(request.getTotalAmount())
                .status("pending")
                .trackingSteps(new ArrayList<>())
                .createdAt(new Date())
                .build();

        booking.getTrackingSteps().add(Booking.TrackingStep.builder()
                .status("pending")
                .note("Booking created")
                .timestamp(new Date())
                .build());

        Booking savedBooking = bookingRepository.save(booking);

        Map<String, Object> data = new HashMap<>();
        data.put("booking", savedBooking);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, data, "Booking created successfully."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBooking(@PathVariable String id) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, null, "Booking not found."));
        }

        Booking booking = bookingOpt.get();
        boolean isOwner = currentUser.getId().equals(booking.getUserId());
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        Optional<Provider> providerOpt = providerRepository.findByUserId(currentUser.getId());
        boolean isProvider = providerOpt.isPresent() && providerOpt.get().getId().equals(booking.getProviderId());

        if (!isOwner && !isProvider && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(403, null, "Not authorized to view this booking."));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("booking", booking);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Booking retrieved."));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateBookingStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        String note = body.get("note");
        String otp = body.get("otp");

        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, null, "Booking not found."));
        }

        Booking booking = bookingOpt.get();

        if ("working".equals(status)) {
            if (otp == null || !otp.equals(booking.getOtp())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(400, null, "Invalid Start Code. Please ask the customer for the correct 4-digit code."));
            }
            booking.setCompletionOtp(generateOtp());
        }

        if ("completed".equals(status)) {
            if (otp == null || !otp.equals(booking.getCompletionOtp())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(400, null, "Invalid Completion Code. Please ask the customer for the correct 4-digit code."));
            }
        }

        booking.setStatus(status);
        booking.getTrackingSteps().add(Booking.TrackingStep.builder()
                .status(status)
                .timestamp(new Date())
                .note(note != null ? note : "")
                .build());

        if ("completed".equals(status)) {
            providerRepository.findById(booking.getProviderId()).ifPresent(p -> {
                p.setJobsCompleted(p.getJobsCompleted() + 1);
                providerRepository.save(p);
            });
        }

        booking.setUpdatedAt(new Date());
        Booking savedBooking = bookingRepository.save(booking);

        Map<String, Object> data = new HashMap<>();
        data.put("booking", savedBooking);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Booking status updated to " + status + "."));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<?> acceptBooking(@PathVariable String id) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, null, "Booking not found."));
        }

        Booking booking = bookingOpt.get();
        if (!"pending".equals(booking.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(400, null, "This booking is no longer pending."));
        }

        booking.setStatus("accepted");
        booking.setOtp(generateOtp());
        booking.getTrackingSteps().add(Booking.TrackingStep.builder()
                .status("accepted")
                .timestamp(new Date())
                .note("Job accepted by provider")
                .build());

        booking.setUpdatedAt(new Date());
        Booking savedBooking = bookingRepository.save(booking);

        Map<String, Object> data = new HashMap<>();
        data.put("booking", savedBooking);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Booking accepted."));
    }

    @PostMapping("/{id}/decline")
    public ResponseEntity<?> declineBooking(@PathVariable String id) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, null, "Booking not found."));
        }

        Booking booking = bookingOpt.get();
        if (!"pending".equals(booking.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(400, null, "This booking is no longer pending."));
        }

        booking.setStatus("cancelled");
        booking.getTrackingSteps().add(Booking.TrackingStep.builder()
                .status("cancelled")
                .timestamp(new Date())
                .note("Job declined by provider")
                .build());

        booking.setUpdatedAt(new Date());
        Booking savedBooking = bookingRepository.save(booking);

        Map<String, Object> data = new HashMap<>();
        data.put("booking", savedBooking);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Booking declined."));
    }

    @PutMapping("/{id}/reassign")
    public ResponseEntity<?> reassignProvider(@PathVariable String id) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, null, "Booking not found."));
        }

        Booking booking = bookingOpt.get();
        Optional<Provider> currentProviderOpt = providerRepository.findById(booking.getProviderId());
        
        
        String category = currentProviderOpt.map(Provider::getCategory).orElse("Home Maintenance");
        
        
        Optional<Provider> newProviderOpt = providerRepository.findAll().stream()
                .filter(p -> p.isAvailable() && category.equals(p.getCategory()) && !p.getId().equals(booking.getProviderId()))
                .findFirst(); 

        if (newProviderOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, null, "No other available providers found."));
        }

        Provider newProvider = newProviderOpt.get();
        booking.setReassignedFrom(booking.getProviderId());
        booking.setProviderId(newProvider.getId());
        booking.setProviderInactive(false);
        booking.setStatus("accepted");
        
        booking.getTrackingSteps().add(Booking.TrackingStep.builder()
                .status("accepted")
                .timestamp(new Date())
                .note("Reassigned to new provider")
                .build());
                
        booking.setUpdatedAt(new Date());
        Booking savedBooking = bookingRepository.save(booking);

        Map<String, Object> data = new HashMap<>();
        data.put("booking", savedBooking);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Provider reassigned successfully."));
    }

    @GetMapping("/{id}/tracking")
    public ResponseEntity<?> getBookingTracking(@PathVariable String id) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, null, "Booking not found."));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("status", bookingOpt.get().getStatus());
        data.put("trackingSteps", bookingOpt.get().getTrackingSteps());
        
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Tracking info retrieved."));
    }
}
