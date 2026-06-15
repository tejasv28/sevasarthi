package com.sevasarthi.backend.controllers;

import com.sevasarthi.backend.dto.ApiResponse;
import com.sevasarthi.backend.models.Booking;
import com.sevasarthi.backend.models.Provider;
import com.sevasarthi.backend.models.Service;
import com.sevasarthi.backend.models.Tool;
import com.sevasarthi.backend.models.User;
import com.sevasarthi.backend.repository.BookingRepository;
import com.sevasarthi.backend.repository.ProviderRepository;
import com.sevasarthi.backend.repository.ServiceRepository;
import com.sevasarthi.backend.repository.ToolRepository;
import com.sevasarthi.backend.repository.UserRepository;
import com.sevasarthi.backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProviderRepository providerRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    ToolRepository toolRepository;

    @Autowired
    ServiceRepository serviceRepository;

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
        return user != null && user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        UserDetailsImpl currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        LocalDate now = LocalDate.now();
        LocalDate monthStartLocal = now.withDayOfMonth(1);
        Date monthStart = Date.from(monthStartLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());

        long totalUsers = mongoTemplate.count(new Query(Criteria.where("isActive").is(true).and("role").is("user")), User.class);
        long totalProviders = mongoTemplate.count(new Query(Criteria.where("isActive").is(true).and("role").is("provider")), User.class);
        long activeBookings = mongoTemplate.count(new Query(Criteria.where("status").nin("completed", "cancelled")), Booking.class);
        long completedBookings = mongoTemplate.count(new Query(Criteria.where("status").is("completed")), Booking.class);
        long newProviders = mongoTemplate.count(new Query(Criteria.where("verificationStatus").is("pending")), Provider.class);
        long totalTools = mongoTemplate.count(new Query(), Tool.class);
        long totalServices = mongoTemplate.count(new Query(Criteria.where("isActive").is(true)), Service.class);

        Aggregation revAgg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("status").is("completed")),
                Aggregation.group().sum("totalAmount").as("total")
        );
        AggregationResults<Map> revResults = mongoTemplate.aggregate(revAgg, Booking.class, Map.class);
        double revenue = 0.0;
        if (revResults.getUniqueMappedResult() != null && revResults.getUniqueMappedResult().get("total") != null) {
            revenue = ((Number) revResults.getUniqueMappedResult().get("total")).doubleValue();
        }

        Aggregation monthlyRevAgg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("status").is("completed").and("updatedAt").gte(monthStart)),
                Aggregation.group().sum("totalAmount").as("total")
        );
        AggregationResults<Map> monthlyRevResults = mongoTemplate.aggregate(monthlyRevAgg, Booking.class, Map.class);
        double monthlyRevenue = 0.0;
        if (monthlyRevResults.getUniqueMappedResult() != null && monthlyRevResults.getUniqueMappedResult().get("total") != null) {
            monthlyRevenue = ((Number) monthlyRevResults.getUniqueMappedResult().get("total")).doubleValue();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("totalUsers", totalUsers);
        data.put("totalProviders", totalProviders);
        data.put("activeBookings", activeBookings);
        data.put("completedBookings", completedBookings);
        data.put("newProviders", newProviders);
        data.put("revenue", revenue);
        data.put("monthlyRevenue", monthlyRevenue);
        data.put("totalTools", totalTools);
        data.put("totalServices", totalServices);

        return ResponseEntity.ok(new ApiResponse<>(200, data, "Stats retrieved."));
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        
        UserDetailsImpl currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        Query query = new Query();
        if (role != null && !role.isEmpty()) query.addCriteria(Criteria.where("role").is(role));
        if ("active".equals(status)) query.addCriteria(Criteria.where("isActive").is(true));
        if ("inactive".equals(status)) query.addCriteria(Criteria.where("isActive").is(false));
        if (search != null && !search.isEmpty()) {
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("name").regex(search, "i"),
                    Criteria.where("email").regex(search, "i")
            ));
        }

        long total = mongoTemplate.count(query, User.class);
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        query.with(PageRequest.of(page - 1, limit));

        List<User> users = mongoTemplate.find(query, User.class);

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("total", total);
        pagination.put("page", page);
        pagination.put("pages", (int) Math.ceil((double) total / limit));

        Map<String, Object> data = new HashMap<>();
        data.put("users", users);
        data.put("pagination", pagination);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Users retrieved."));
    }

    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable String id) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, null, "User not found."));

        User user = userOpt.get();
        if ("admin".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(400, null, "Cannot modify admin status."));
        }

        user.setActive(!user.isActive());
        userRepository.save(user);

        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "User " + (user.isActive() ? "activated" : "deactivated") + "."));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, null, "User not found."));

        User user = userOpt.get();
        if ("admin".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(400, null, "Cannot delete admin user."));
        }

        if ("provider".equals(user.getRole())) {
            Optional<Provider> providerOpt = providerRepository.findByUserId(user.getId());
            providerOpt.ifPresent(provider -> providerRepository.delete(provider));
        }
        
        userRepository.delete(user);
        return ResponseEntity.ok(new ApiResponse<>(200, null, "User deleted."));
    }

    @GetMapping("/verifications")
    public ResponseEntity<?> getVerifications(@RequestParam(defaultValue = "pending") String status) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        Query query = new Query();
        if (status != null && !"all".equals(status)) {
            query.addCriteria(Criteria.where("verificationStatus").is(status));
        } else if (status == null) {
            query.addCriteria(Criteria.where("verificationStatus").is("pending"));
        }

        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Provider> providers = mongoTemplate.find(query, Provider.class);

        Map<String, Object> data = new HashMap<>();
        data.put("verifications", providers);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Verifications retrieved."));
    }

    @PutMapping("/verifications/{id}")
    public ResponseEntity<?> handleVerification(@PathVariable String id, @RequestBody Map<String, String> body) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        String action = body.get("action");
        String reason = body.get("reason");

        Optional<Provider> providerOpt = providerRepository.findById(id);
        if (providerOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, null, "Provider not found."));

        Provider provider = providerOpt.get();

        if ("approve".equals(action)) {
            provider.setVerificationStatus("approved");
            provider.setVerifiedProvider(true);
            provider.setApprovedAt(new Date());
            provider.setRejectionReason("");
        } else if ("reject".equals(action)) {
            provider.setVerificationStatus("rejected");
            provider.setVerifiedProvider(false);
            provider.setRejectionReason(reason != null ? reason : "Application does not meet our requirements.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(400, null, "Action must be approve or reject."));
        }

        providerRepository.save(provider);
        return ResponseEntity.ok(new ApiResponse<>(200, null, "Provider " + action + "d."));
    }

    @GetMapping("/bookings")
    public ResponseEntity<?> getAllBookings(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        
        UserDetailsImpl currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        Query query = new Query();
        if (status != null && !status.isEmpty()) query.addCriteria(Criteria.where("status").is(status));

        long total = mongoTemplate.count(query, Booking.class);
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        query.with(PageRequest.of(page - 1, limit));

        List<Booking> bookings = mongoTemplate.find(query, Booking.class);

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("total", total);
        pagination.put("page", page);
        pagination.put("pages", (int) Math.ceil((double) total / limit));

        Map<String, Object> data = new HashMap<>();
        data.put("bookings", bookings);
        data.put("pagination", pagination);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Bookings retrieved."));
    }

    @GetMapping("/analytics")
    public ResponseEntity<?> getAnalytics() {
        UserDetailsImpl currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        List<Map<String, Object>> days = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate dateLocal = LocalDate.now().minusDays(i);
            Date date = Date.from(dateLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date nextDay = Date.from(dateLocal.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

            long bookingCount = mongoTemplate.count(new Query(Criteria.where("createdAt").gte(date).lt(nextDay)), Booking.class);

            Aggregation revAgg = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("status").is("completed").and("updatedAt").gte(date).lt(nextDay)),
                    Aggregation.group().sum("totalAmount").as("total")
            );
            AggregationResults<Map> revResults = mongoTemplate.aggregate(revAgg, Booking.class, Map.class);
            double revenue = 0.0;
            if (revResults.getUniqueMappedResult() != null && revResults.getUniqueMappedResult().get("total") != null) {
                revenue = ((Number) revResults.getUniqueMappedResult().get("total")).doubleValue();
            }

            Map<String, Object> dayStat = new HashMap<>();
            dayStat.put("day", dayNames[dateLocal.getDayOfWeek().getValue() % 7]); 
            dayStat.put("date", dateLocal.toString());
            dayStat.put("bookings", bookingCount);
            dayStat.put("revenue", revenue);
            days.add(dayStat);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("analytics", days);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Analytics retrieved."));
    }

    @GetMapping("/services")
    public ResponseEntity<?> getAllServices() {
        UserDetailsImpl currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        Query query = new Query();
        query.with(Sort.by(Sort.Direction.ASC, "category"));
        List<Service> services = mongoTemplate.find(query, Service.class);

        Map<String, Object> data = new HashMap<>();
        data.put("services", services);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Services retrieved."));
    }

    @PostMapping("/services")
    public ResponseEntity<?> createService(@RequestBody Service request) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        request.setApprovalStatus("approved");
        request.setCreatedAt(new Date());
        Service savedService = serviceRepository.save(request);

        Map<String, Object> data = new HashMap<>();
        data.put("service", savedService);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(201, data, "Service created."));
    }

    @DeleteMapping("/services/{id}")
    public ResponseEntity<?> deleteService(@PathVariable String id) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        Optional<Service> serviceOpt = serviceRepository.findById(id);
        if (serviceOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, null, "Service not found."));

        serviceRepository.delete(serviceOpt.get());
        return ResponseEntity.ok(new ApiResponse<>(200, null, "Service deleted."));
    }

    @GetMapping("/pending-services")
    public ResponseEntity<?> getPendingServices(@RequestParam(defaultValue = "pending") String status) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        Query query = new Query();
        if (status != null && !"all".equals(status)) {
            query.addCriteria(Criteria.where("approvalStatus").is(status));
        }

        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Service> services = mongoTemplate.find(query, Service.class);

        Map<String, Object> data = new HashMap<>();
        data.put("services", services);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Pending services retrieved."));
    }

    @PutMapping("/services/{id}/approve")
    public ResponseEntity<?> approveService(@PathVariable String id) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        Optional<Service> serviceOpt = serviceRepository.findById(id);
        if (serviceOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, null, "Service not found."));

        Service service = serviceOpt.get();
        service.setApprovalStatus("approved");
        service.setRejectionReason("");
        serviceRepository.save(service);

        Map<String, Object> data = new HashMap<>();
        data.put("service", service);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Service approved."));
    }

    @PutMapping("/services/{id}/reject")
    public ResponseEntity<?> rejectService(@PathVariable String id, @RequestBody Map<String, String> body) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        Optional<Service> serviceOpt = serviceRepository.findById(id);
        if (serviceOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(404, null, "Service not found."));

        Service service = serviceOpt.get();
        service.setApprovalStatus("rejected");
        service.setRejectionReason(body.get("reason") != null ? body.get("reason") : "Does not meet platform guidelines.");
        serviceRepository.save(service);

        Map<String, Object> data = new HashMap<>();
        data.put("service", service);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Service rejected."));
    }
}
