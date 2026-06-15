package com.sevasarthi.backend.controllers;

import com.sevasarthi.backend.dto.ApiResponse;
import com.sevasarthi.backend.models.Rental;
import com.sevasarthi.backend.models.Tool;
import com.sevasarthi.backend.repository.RentalRepository;
import com.sevasarthi.backend.repository.ToolRepository;
import com.sevasarthi.backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/api/rentals")
public class RentalController {

    @Autowired
    RentalRepository rentalRepository;

    @Autowired
    ToolRepository toolRepository;

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

    private String generateOtp() {
        return String.valueOf((int)(Math.random() * 900000) + 100000); 
    }

    @PostMapping
    public ResponseEntity<?> createRental(@RequestBody Rental requestRental) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Tool> toolOpt = toolRepository.findById(requestRental.getToolId());
        if (toolOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, null, "Tool not found."));
        }

        Tool tool = toolOpt.get();
        if (!"available".equals(tool.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(400, null, "Tool is not available for rent."));
        }

        int days = requestRental.getDays() != null ? requestRental.getDays() : 1;
        double subtotal = tool.getDailyRate() * days;
        double deliveryFee = 99.0;
        double tax = Math.round(subtotal * 0.05);
        double refundableDeposit = Math.max(500, Math.round(subtotal * 0.4));
        double total = subtotal + deliveryFee + tax;

        Rental rental = Rental.builder()
                .userId(currentUser.getId())
                .toolId(tool.getId())
                .toolName(tool.getName())
                .days(days)
                .subtotal(subtotal)
                .deliveryFee(deliveryFee)
                .tax(tax)
                .refundableDeposit(refundableDeposit)
                .total(total)
                .deliveryDetails(requestRental.getDeliveryDetails())
                .status("confirmed")
                .deliveryOtp(generateOtp())
                .returnOtp(generateOtp())
                .createdAt(new Date())
                .build();

        tool.setStatus("rented");
        toolRepository.save(tool);

        Rental savedRental = rentalRepository.save(rental);

        Map<String, Object> data = new HashMap<>();
        data.put("rental", savedRental);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, data, "Rental created successfully."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRental(@PathVariable String id) {
        Optional<Rental> rentalOpt = rentalRepository.findById(id);
        if (rentalOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, null, "Rental not found."));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("rental", rentalOpt.get());
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Rental retrieved."));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateRentalStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        String otp = body.get("otp");

        Optional<Rental> rentalOpt = rentalRepository.findById(id);
        if (rentalOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, null, "Rental not found."));
        }

        Rental rental = rentalOpt.get();

        if ("delivered".equals(status)) {
            if (otp == null || !otp.equals(rental.getDeliveryOtp())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(400, null, "Invalid Delivery OTP"));
            }
        } else if ("returned".equals(status)) {
            if (otp == null || !otp.equals(rental.getReturnOtp())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(400, null, "Invalid Return OTP"));
            }
            Optional<Tool> toolOpt = toolRepository.findById(rental.getToolId());
            if (toolOpt.isPresent()) {
                Tool tool = toolOpt.get();
                tool.setStatus("available");
                toolRepository.save(tool);
            }
        }

        rental.setStatus(status);
        rental.setUpdatedAt(new Date());

        Rental savedRental = rentalRepository.save(rental);
        
        Map<String, Object> data = new HashMap<>();
        data.put("rental", savedRental);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Rental status updated to " + status + "."));
    }

    @GetMapping("/provider")
    public ResponseEntity<?> getProviderRentals() {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<Tool> providerTools = toolRepository.findByOwnerId(currentUser.getId());
        List<String> toolIds = providerTools.stream().map(Tool::getId).toList();

        Query query = new Query(Criteria.where("toolId").in(toolIds));
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        
        List<Rental> rentals = mongoTemplate.find(query, Rental.class);

        Map<String, Object> data = new HashMap<>();
        data.put("rentals", rentals);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Provider rentals retrieved."));
    }
}
