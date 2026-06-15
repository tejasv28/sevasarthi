package com.sevasarthi.backend.controllers;

import com.sevasarthi.backend.dto.ApiResponse;
import com.sevasarthi.backend.models.Booking;
import com.sevasarthi.backend.models.Complaint;
import com.sevasarthi.backend.models.Provider;
import com.sevasarthi.backend.models.Rental;
import com.sevasarthi.backend.models.Tool;
import com.sevasarthi.backend.models.User;
import com.sevasarthi.backend.repository.BookingRepository;
import com.sevasarthi.backend.repository.ComplaintRepository;
import com.sevasarthi.backend.repository.ProviderRepository;
import com.sevasarthi.backend.repository.RentalRepository;
import com.sevasarthi.backend.repository.UserRepository;
import com.sevasarthi.backend.security.UserDetailsImpl;
import com.sevasarthi.backend.utils.Constants;
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

import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    @Autowired
    ComplaintRepository complaintRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    RentalRepository rentalRepository;

    @Autowired
    ProviderRepository providerRepository;

    @Autowired
    UserRepository userRepository;

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

    private boolean isAdmin(UserDetailsImpl user) {
        return user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    

    @PostMapping
    public ResponseEntity<?> createComplaint(@RequestBody Complaint request) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        String type = request.getType();
        String category = request.getCategory();

        if (!Constants.ComplaintTypes.SERVICE_BOOKING.equals(type) && !Constants.ComplaintTypes.TOOL_RENTAL.equals(type)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(400, null, "Invalid complaint type."));
        }

        String providerId = null;
        String referenceLabel = "";

        if (Constants.ComplaintTypes.SERVICE_BOOKING.equals(type)) {
            if (request.getBookingId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(400, null, "Booking ID is required."));
            }
            Optional<Booking> bookingOpt = bookingRepository.findById(request.getBookingId());
            if (bookingOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, null, "Booking not found."));
            Booking booking = bookingOpt.get();
            if (!booking.getUserId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(403, null, "This booking does not belong to you."));
            }
            providerId = booking.getProviderId();
            referenceLabel = booking.getServiceName() != null ? booking.getServiceName() : "Service Booking";
        } else if (Constants.ComplaintTypes.TOOL_RENTAL.equals(type)) {
            if (request.getRentalId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(400, null, "Rental ID is required."));
            }
            Optional<Rental> rentalOpt = rentalRepository.findById(request.getRentalId());
            if (rentalOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, null, "Rental not found."));
            Rental rental = rentalOpt.get();
            if (!rental.getUserId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(403, null, "This rental does not belong to you."));
            }
            
            referenceLabel = rental.getToolName() != null ? rental.getToolName() : "Tool Rental";
        }

        Complaint complaint = Complaint.builder()
                .userId(currentUser.getId())
                .type(type)
                .bookingId(Constants.ComplaintTypes.SERVICE_BOOKING.equals(type) ? request.getBookingId() : null)
                .rentalId(Constants.ComplaintTypes.TOOL_RENTAL.equals(type) ? request.getRentalId() : null)
                .providerId(providerId)
                .category(category)
                .description(request.getDescription())
                .proofImage(request.getProofImage() != null ? request.getProofImage() : "")
                .status(Constants.ComplaintStatus.PENDING)
                .statusHistory(new ArrayList<>())
                .createdAt(new Date())
                .build();

        complaint.getStatusHistory().add(Complaint.StatusHistory.builder()
                .status(Constants.ComplaintStatus.PENDING)
                .note("Complaint submitted by customer.")
                .timestamp(new Date())
                .build());

        Complaint savedComplaint = complaintRepository.save(complaint);
        Map<String, Object> data = new HashMap<>();
        data.put("complaint", savedComplaint);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(201, data, "Complaint submitted successfully."));
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyComplaints(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Query query = new Query(Criteria.where("userId").is(currentUser.getId()));
        if (status != null && !status.isEmpty()) query.addCriteria(Criteria.where("status").is(status));
        if (type != null && !type.isEmpty()) query.addCriteria(Criteria.where("type").is(type));

        long total = mongoTemplate.count(query, Complaint.class);
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        query.with(PageRequest.of(page - 1, limit));

        List<Complaint> complaints = mongoTemplate.find(query, Complaint.class);

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("total", total);
        pagination.put("page", page);
        pagination.put("pages", (int) Math.ceil((double) total / limit));

        Map<String, Object> data = new HashMap<>();
        data.put("complaints", complaints);
        data.put("pagination", pagination);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Complaints retrieved."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getComplaintById(@PathVariable String id) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Complaint> complaintOpt = complaintRepository.findById(id);
        if (complaintOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, null, "Complaint not found."));
        }

        Complaint complaint = complaintOpt.get();
        if (!isAdmin(currentUser) && !complaint.getUserId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(403, null, "Access denied."));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("complaint", complaint);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Complaint details retrieved."));
    }

    @PutMapping("/{id}/reopen")
    public ResponseEntity<?> reopenComplaint(@PathVariable String id, @RequestBody Map<String, String> body) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Complaint> complaintOpt = complaintRepository.findById(id);
        if (complaintOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, null, "Complaint not found."));
        }

        Complaint complaint = complaintOpt.get();
        if (!complaint.getUserId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(403, null, "Access denied."));
        }

        if (!Constants.ComplaintStatus.RESOLVED.equals(complaint.getStatus()) && !Constants.ComplaintStatus.REJECTED.equals(complaint.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(400, null, "Only resolved or rejected complaints can be reopened."));
        }

        if (complaint.getReopenCount() >= 3) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(400, null, "Maximum reopen limit (3) reached. Please contact support directly."));
        }

        complaint.setStatus(Constants.ComplaintStatus.REOPENED);
        complaint.setReopenCount(complaint.getReopenCount() + 1);
        complaint.setReopenReason(body.get("reason") != null ? body.get("reason") : "Customer reopened the complaint.");
        complaint.setResolvedAt(null);
        
        complaint.getStatusHistory().add(Complaint.StatusHistory.builder()
                .status(Constants.ComplaintStatus.REOPENED)
                .note(body.get("reason") != null ? body.get("reason") : "Reopened by customer.")
                .changedBy(currentUser.getId())
                .timestamp(new Date())
                .build());

        Complaint savedComplaint = complaintRepository.save(complaint);

        Map<String, Object> data = new HashMap<>();
        data.put("complaint", savedComplaint);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Complaint reopened."));
    }

    @GetMapping("/references")
    public ResponseEntity<?> getComplaintReferences() {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Query bookingQuery = new Query(Criteria.where("userId").is(currentUser.getId()));
        bookingQuery.with(Sort.by(Sort.Direction.DESC, "createdAt")).limit(50);
        List<Booking> bookings = mongoTemplate.find(bookingQuery, Booking.class);

        Query rentalQuery = new Query(Criteria.where("userId").is(currentUser.getId()));
        rentalQuery.with(Sort.by(Sort.Direction.DESC, "createdAt")).limit(50);
        List<Rental> rentals = mongoTemplate.find(rentalQuery, Rental.class);

        Map<String, Object> data = new HashMap<>();
        data.put("bookings", bookings);
        data.put("rentals", rentals);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "References retrieved."));
    }

    

    @GetMapping("/admin/complaints")
    public ResponseEntity<?> getAllComplaints(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null || !isAdmin(currentUser)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        Query query = new Query();
        if (status != null && !status.isEmpty()) query.addCriteria(Criteria.where("status").is(status));
        if (type != null && !type.isEmpty()) query.addCriteria(Criteria.where("type").is(type));
        if (search != null && !search.isEmpty()) {
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("ticketId").regex(search, "i"),
                    Criteria.where("category").regex(search, "i"),
                    Criteria.where("description").regex(search, "i")
            ));
        }

        long total = mongoTemplate.count(query, Complaint.class);
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        query.with(PageRequest.of(page - 1, limit));

        List<Complaint> complaints = mongoTemplate.find(query, Complaint.class);

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("total", total);
        pagination.put("page", page);
        pagination.put("pages", (int) Math.ceil((double) total / limit));

        Map<String, Object> stats = new HashMap<>();
        stats.put("pendingCount", mongoTemplate.count(new Query(Criteria.where("status").is(Constants.ComplaintStatus.PENDING)), Complaint.class));
        stats.put("inReviewCount", mongoTemplate.count(new Query(Criteria.where("status").is(Constants.ComplaintStatus.IN_REVIEW)), Complaint.class));
        stats.put("resolvedCount", mongoTemplate.count(new Query(Criteria.where("status").is(Constants.ComplaintStatus.RESOLVED)), Complaint.class));
        stats.put("escalatedCount", mongoTemplate.count(new Query(Criteria.where("status").is(Constants.ComplaintStatus.ESCALATED)), Complaint.class));
        stats.put("totalCount", total);

        Map<String, Object> data = new HashMap<>();
        data.put("complaints", complaints);
        data.put("stats", stats);
        data.put("pagination", pagination);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "All complaints retrieved."));
    }

    @GetMapping("/admin/complaints/{id}")
    public ResponseEntity<?> getAdminComplaintById(@PathVariable String id) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null || !isAdmin(currentUser)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        Optional<Complaint> complaintOpt = complaintRepository.findById(id);
        if (complaintOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, null, "Complaint not found."));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("complaint", complaintOpt.get());
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Complaint detail retrieved."));
    }

    @PutMapping("/admin/complaints/{id}/status")
    public ResponseEntity<?> updateComplaintStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null || !isAdmin(currentUser)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        String status = body.get("status");
        String adminResponse = body.get("adminResponse");

        Optional<Complaint> complaintOpt = complaintRepository.findById(id);
        if (complaintOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, null, "Complaint not found."));
        }

        Complaint complaint = complaintOpt.get();
        complaint.setStatus(status);
        if (adminResponse != null) complaint.setAdminResponse(adminResponse);
        if (Constants.ComplaintStatus.RESOLVED.equals(status)) complaint.setResolvedAt(new Date());

        complaint.getStatusHistory().add(Complaint.StatusHistory.builder()
                .status(status)
                .note(adminResponse != null ? adminResponse : "Status updated to " + status + " by admin.")
                .changedBy(currentUser.getId())
                .timestamp(new Date())
                .build());

        Complaint savedComplaint = complaintRepository.save(complaint);

        Map<String, Object> data = new HashMap<>();
        data.put("complaint", savedComplaint);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Complaint status updated."));
    }

    @PutMapping("/admin/complaints/{id}/action")
    public ResponseEntity<?> takeAdminAction(@PathVariable String id, @RequestBody Map<String, Object> body) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null || !isAdmin(currentUser)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        String action = (String) body.get("action");
        Map<String, Object> details = (Map<String, Object>) body.get("details");

        Optional<Complaint> complaintOpt = complaintRepository.findById(id);
        if (complaintOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, null, "Complaint not found."));
        }

        Complaint complaint = complaintOpt.get();
        if (complaint.getProviderId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(400, null, "No provider associated with this complaint."));
        }

        Optional<Provider> providerOpt = providerRepository.findById(complaint.getProviderId());
        if (providerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, null, "Provider not found."));
        }

        Provider provider = providerOpt.get();
        String actionNote = "Admin action taken: " + action;

        complaint.setAdminAction(action);
        complaint.setActionDetails(details);
        complaint.getStatusHistory().add(Complaint.StatusHistory.builder()
                .status(complaint.getStatus())
                .note(actionNote)
                .changedBy(currentUser.getId())
                .timestamp(new Date())
                .build());

        complaintRepository.save(complaint);

        
        

        Map<String, Object> data = new HashMap<>();
        data.put("complaint", complaint);
        data.put("provider", provider);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Action \"" + action + "\" applied successfully."));
    }
}
