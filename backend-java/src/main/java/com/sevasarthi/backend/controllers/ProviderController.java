package com.sevasarthi.backend.controllers;

import com.sevasarthi.backend.dto.ApiResponse;
import com.sevasarthi.backend.models.Booking;
import com.sevasarthi.backend.models.Provider;
import com.sevasarthi.backend.models.Rental;
import com.sevasarthi.backend.models.Service;
import com.sevasarthi.backend.models.Tool;
import com.sevasarthi.backend.models.User;
import com.sevasarthi.backend.repository.BookingRepository;
import com.sevasarthi.backend.repository.ProviderRepository;
import com.sevasarthi.backend.repository.RentalRepository;
import com.sevasarthi.backend.repository.ServiceRepository;
import com.sevasarthi.backend.repository.ToolRepository;
import com.sevasarthi.backend.repository.UserRepository;
import com.sevasarthi.backend.security.UserDetailsImpl;
import com.sevasarthi.backend.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/providers")
public class ProviderController {

    @Autowired
    ProviderRepository providerRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    RentalRepository rentalRepository;

    @Autowired
    ServiceRepository serviceRepository;

    @Autowired
    ToolRepository toolRepository;

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

    @GetMapping
    public ResponseEntity<?> getAllProviders(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "relevance") String sortBy,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {

        Query query = new Query();
        query.addCriteria(Criteria.where("isAvailable").is(true).and("verificationStatus").is("approved"));

        if (category != null && !category.isEmpty()) {
            query.addCriteria(Criteria.where("category").is(category));
        }

        if (city != null && !city.isEmpty()) {
            List<User> usersInCity = mongoTemplate.find(
                    new Query(Criteria.where("role").is("provider").and("address.city").regex("^" + city + "$", "i")),
                    User.class
            );
            List<String> userIdsInCity = usersInCity.stream().map(User::getId).toList();

            Criteria cityCriteria = new Criteria().orOperator(
                    Criteria.where("city").regex("^" + city + "$", "i"),
                    Criteria.where("userId").in(userIdsInCity)
            );
            query.addCriteria(cityCriteria);
        }

        if (search != null && !search.isEmpty()) {
            List<User> matchingUsers = mongoTemplate.find(
                    new Query(Criteria.where("role").is("provider").and("name").regex(search, "i")),
                    User.class
            );
            List<String> userIdsSearch = matchingUsers.stream().map(User::getId).toList();

            Criteria searchCriteria = new Criteria().orOperator(
                    Criteria.where("title").regex(search, "i"),
                    Criteria.where("category").regex(search, "i"),
                    Criteria.where("userId").in(userIdsSearch)
            );
            query.addCriteria(searchCriteria);
        }

        long total = mongoTemplate.count(query, Provider.class);

        Sort sort = Sort.by(Sort.Direction.DESC, "rating");
        if (!"highestRated".equals(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "isTopRated", "rating");
        }

        query.with(PageRequest.of(page - 1, limit, sort));

        List<Provider> providers = mongoTemplate.find(query, Provider.class);

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("total", total);
        pagination.put("page", page);
        pagination.put("pages", (int) Math.ceil((double) total / limit));

        Map<String, Object> data = new HashMap<>();
        data.put("providers", providers);
        data.put("pagination", pagination);

        return ResponseEntity.ok(new ApiResponse<>(200, data, "Providers retrieved."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProvider(@PathVariable String id) {
        Optional<Provider> providerOpt = providerRepository.findById(id);
        
        if (providerOpt.isEmpty()) {
            providerOpt = providerRepository.findByUserId(id);
        }

        if (providerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, null, "Provider not found."));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("provider", providerOpt.get());
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Provider retrieved."));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProviderProfile(@RequestBody Provider updatedProvider) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Provider> providerOpt = providerRepository.findByUserId(currentUser.getId());
        if (providerOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, null, "Provider profile not found."));
        }

        Provider provider = providerOpt.get();

        if (updatedProvider.getCategory() != null) provider.setCategory(updatedProvider.getCategory());
        if (updatedProvider.getTitle() != null) provider.setTitle(updatedProvider.getTitle());
        if (updatedProvider.getBio() != null) provider.setBio(updatedProvider.getBio());
        if (updatedProvider.getSkills() != null) provider.setSkills(updatedProvider.getSkills());
        if (updatedProvider.getCertifications() != null) provider.setCertifications(updatedProvider.getCertifications());
        if (updatedProvider.getPortfolio() != null) provider.setPortfolio(updatedProvider.getPortfolio());
        if (updatedProvider.getExperience() != null) provider.setExperience(updatedProvider.getExperience());
        if (updatedProvider.getPricePerHour() != null) provider.setPricePerHour(updatedProvider.getPricePerHour());
        if (updatedProvider.isAvailable() != provider.isAvailable()) provider.setAvailable(updatedProvider.isAvailable());

        provider.setUpdatedAt(new Date());

        Provider savedProvider = providerRepository.save(provider);
        
        Map<String, Object> data = new HashMap<>();
        data.put("provider", savedProvider);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Profile updated."));
    }

    @GetMapping("/requests")
    public ResponseEntity<?> getPendingRequests() {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Provider> providerOpt = providerRepository.findByUserId(currentUser.getId());
        if (providerOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        Query query = new Query(Criteria.where("providerId").is(providerOpt.get().getId()).and("status").is("pending"));
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Booking> requests = mongoTemplate.find(query, Booking.class);

        Map<String, Object> data = new HashMap<>();
        data.put("requests", requests);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Pending requests retrieved."));
    }

    @PutMapping("/availability")
    public ResponseEntity<?> toggleAvailability() {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Provider> providerOpt = providerRepository.findByUserId(currentUser.getId());
        if (providerOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        Provider provider = providerOpt.get();
        boolean newStatus = !provider.isAvailable();
        provider.setAvailable(newStatus);
        providerRepository.save(provider);

        
        List<Service> services = serviceRepository.findByProviderId(currentUser.getId());
        services.forEach(s -> s.setActive(newStatus));
        serviceRepository.saveAll(services);

        
        List<Tool> tools = toolRepository.findByOwnerId(currentUser.getId());
        tools.forEach(t -> {
            if (Constants.ToolStatus.AVAILABLE.equals(t.getStatus()) || Constants.ToolStatus.MAINTENANCE.equals(t.getStatus())) {
                t.setStatus(newStatus ? Constants.ToolStatus.AVAILABLE : Constants.ToolStatus.MAINTENANCE);
            }
        });
        toolRepository.saveAll(tools);

        Map<String, Object> data = new HashMap<>();
        data.put("isAvailable", newStatus);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Now " + (newStatus ? "accepting" : "not accepting") + " jobs. All services and tools updated."));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats() {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Provider> providerOpt = providerRepository.findByUserId(currentUser.getId());
        if (providerOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        Provider provider = providerOpt.get();
        String providerId = provider.getId();
        String userId = currentUser.getId();

        LocalDate todayLocal = LocalDate.now(ZoneId.systemDefault());
        Date today = Date.from(todayLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date tomorrow = Date.from(todayLocal.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        
        
        long todayBookings = bookingRepository.countByProviderId(providerId); 
        long pendingRequests = bookingRepository.countByProviderId(providerId); 
        double weeklyEarnings = 0.0;
        double totalEarnings = 0.0;
        
        
        Aggregation totalEarningsAgg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("providerId").is(providerId).and("status").is("completed")),
                Aggregation.group().sum("totalAmount").as("total")
        );
        AggregationResults<Map> results = mongoTemplate.aggregate(totalEarningsAgg, Booking.class, Map.class);
        if (results.getUniqueMappedResult() != null && results.getUniqueMappedResult().get("total") != null) {
            totalEarnings = ((Number) results.getUniqueMappedResult().get("total")).doubleValue();
        }

        long totalCompleted = mongoTemplate.count(new Query(Criteria.where("providerId").is(providerId).and("status").is("completed")), Booking.class);
        long totalBookings = mongoTemplate.count(new Query(Criteria.where("providerId").is(providerId).and("status").ne("cancelled")), Booking.class);

        Map<String, Object> data = new HashMap<>();
        data.put("isAvailable", provider.isAvailable());
        data.put("todayJobs", todayBookings); 
        data.put("pendingRequests", pendingRequests); 
        data.put("weeklyEarnings", weeklyEarnings); 
        data.put("totalEarnings", totalEarnings);
        data.put("rating", provider.getRating());
        data.put("jobsCompleted", totalCompleted);
        data.put("completionRate", totalBookings > 0 ? Math.round(((double)totalCompleted / totalBookings) * 100) + "%" : "0%");

        return ResponseEntity.ok(new ApiResponse<>(200, data, "Dashboard stats retrieved."));
    }

    @GetMapping("/schedule")
    public ResponseEntity<?> getTodaySchedule() {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Provider> providerOpt = providerRepository.findByUserId(currentUser.getId());
        if (providerOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        Query query = new Query(Criteria.where("providerId").is(providerOpt.get().getId())
                .and("status").in("accepted", "en_route", "working"));
        query.with(Sort.by(Sort.Direction.ASC, "scheduledDate", "scheduledTime"));
        
        List<Booking> schedule = mongoTemplate.find(query, Booking.class);
        
        Map<String, Object> data = new HashMap<>();
        data.put("schedule", schedule);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Schedule retrieved."));
    }

    @GetMapping("/earnings")
    public ResponseEntity<?> getEarnings(@RequestParam(defaultValue = "week") String period) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Provider> providerOpt = providerRepository.findByUserId(currentUser.getId());
        if (providerOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        
        
        List<Map<String, Object>> earnings = new ArrayList<>();
        double totalEarnings = 0.0;
        
        Map<String, Object> data = new HashMap<>();
        data.put("earnings", earnings);
        data.put("totalEarnings", totalEarnings);
        data.put("period", period);

        return ResponseEntity.ok(new ApiResponse<>(200, data, "Earnings retrieved."));
    }

    @GetMapping("/onboarding-status")
    public ResponseEntity<?> getOnboardingStatus() {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Provider> providerOpt = providerRepository.findByUserId(currentUser.getId());
        if (providerOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        Provider provider = providerOpt.get();

        Map<String, Object> data = new HashMap<>();
        data.put("verificationStatus", provider.getVerificationStatus());
        data.put("rejectionReason", provider.getRejectionReason() != null ? provider.getRejectionReason() : "");
        data.put("businessType", provider.getBusinessType());
        data.put("businessName", provider.getBusinessName());
        data.put("primaryCategory", provider.getPrimaryCategory() != null ? provider.getPrimaryCategory() : provider.getCategory());
        data.put("appliedAt", provider.getCreatedAt());
        data.put("approvedAt", provider.getApprovedAt());

        return ResponseEntity.ok(new ApiResponse<>(200, data, "Onboarding status retrieved."));
    }
}
