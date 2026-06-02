package com.sevasarthi.backend.controllers;

import com.sevasarthi.backend.dto.ApiResponse;
import com.sevasarthi.backend.models.Provider;
import com.sevasarthi.backend.models.Service;
import com.sevasarthi.backend.models.User;
import com.sevasarthi.backend.repository.ProviderRepository;
import com.sevasarthi.backend.repository.ServiceRepository;
import com.sevasarthi.backend.repository.UserRepository;
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
@RequestMapping("/api/services")
public class ServiceController {

    @Autowired
    ServiceRepository serviceRepository;

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

    @GetMapping
    public ResponseEntity<?> getAllServices(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String search) {

        Query query = new Query();
        query.addCriteria(Criteria.where("isActive").is(true).and("approvalStatus").is("approved"));

        if (category != null && !category.isEmpty()) {
            query.addCriteria(Criteria.where("category").is(category));
        }

        if (search != null && !search.isEmpty()) {
            String escaped = search.replaceAll("[.*+?^${}()|\\[\\]\\\\]", "\\\\$0");
            Criteria searchCriteria = new Criteria().orOperator(
                    Criteria.where("name").regex("(?:^|[\\s\\-_/,&()])" + escaped, "i"),
                    Criteria.where("description").regex("(?:^|[\\s\\-_/,&()])" + escaped, "i"),
                    Criteria.where("category").regex("(?:^|[\\s\\-_/,&()])" + escaped, "i")
            );
            query.addCriteria(searchCriteria);
        }

        query.with(Sort.by(Sort.Direction.ASC, "category", "name"));
        List<Service> services = mongoTemplate.find(query, Service.class);

        if (city != null && !city.isEmpty()) {
            List<Provider> providersInCity = mongoTemplate.find(
                    new Query(Criteria.where("city").regex("^" + city + "$", "i")), Provider.class);
            List<User> usersInCity = mongoTemplate.find(
                    new Query(Criteria.where("role").is("provider").and("address.city").regex("^" + city + "$", "i")), User.class);

            List<String> userIds = new java.util.ArrayList<>(providersInCity.stream().map(Provider::getUserId).toList());
            userIds.addAll(usersInCity.stream().map(User::getId).toList());

            if (!userIds.isEmpty()) {
                services = services.stream().filter(s -> s.getProviderId() != null && userIds.contains(s.getProviderId())).toList();
            } else {
                services = List.of();
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("services", services);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Services retrieved."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getService(@PathVariable String id) {
        Optional<Service> serviceOpt = serviceRepository.findById(id);
        if (serviceOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, null, "Service not found."));
        }
        Map<String, Object> data = new HashMap<>();
        data.put("service", serviceOpt.get());
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Service retrieved."));
    }

    @GetMapping("/my-services")
    public ResponseEntity<?> getMyServices() {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Query query = new Query(Criteria.where("providerId").is(currentUser.getId()));
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Service> services = mongoTemplate.find(query, Service.class);

        Map<String, Object> data = new HashMap<>();
        data.put("services", services);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Your services retrieved."));
    }

    @PostMapping
    public ResponseEntity<?> createService(@RequestBody Service serviceRequest) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (serviceRequest.getName() == null || serviceRequest.getCategory() == null || serviceRequest.getBasePrice() == null) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(new ApiResponse<>(422, null, "Name, category, and base price are required."));
        }

        Service service = Service.builder()
                .name(serviceRequest.getName())
                .category(serviceRequest.getCategory())
                .description(serviceRequest.getDescription() != null ? serviceRequest.getDescription() : "")
                .icon(serviceRequest.getIcon() != null ? serviceRequest.getIcon() : "home_repair_service")
                .basePrice(serviceRequest.getBasePrice())
                .image(serviceRequest.getImage() != null ? serviceRequest.getImage() : "")
                .providerId(currentUser.getId())
                .isActive(true)
                .approvalStatus("pending")
                .workingHours(serviceRequest.getWorkingHours() != null ? serviceRequest.getWorkingHours() : 
                              Service.WorkingHours.builder().start("09:00").end("18:00").build())
                .createdAt(new Date())
                .build();

        Service savedService = serviceRepository.save(service);

        Map<String, Object> data = new HashMap<>();
        data.put("service", savedService);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, data, "Service created. It will be visible after admin approval."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateService(@PathVariable String id, @RequestBody Service updatedFields) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Service> serviceOpt = serviceRepository.findById(id);
        if (serviceOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, null, "Service not found."));
        }

        Service service = serviceOpt.get();
        if (service.getProviderId() != null && !service.getProviderId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(403, null, "Not authorized to update this service."));
        }

        if (updatedFields.getName() != null) service.setName(updatedFields.getName());
        if (updatedFields.getCategory() != null) service.setCategory(updatedFields.getCategory());
        if (updatedFields.getDescription() != null) service.setDescription(updatedFields.getDescription());
        if (updatedFields.getIcon() != null) service.setIcon(updatedFields.getIcon());
        if (updatedFields.getBasePrice() != null) service.setBasePrice(updatedFields.getBasePrice());
        if (updatedFields.getImage() != null) service.setImage(updatedFields.getImage());
        service.setActive(updatedFields.isActive());
        if (updatedFields.getWorkingHours() != null) service.setWorkingHours(updatedFields.getWorkingHours());

        service.setUpdatedAt(new Date());

        Service savedService = serviceRepository.save(service);
        Map<String, Object> data = new HashMap<>();
        data.put("service", savedService);

        return ResponseEntity.ok(new ApiResponse<>(200, data, "Service updated."));
    }

    @PutMapping("/{id}/toggle-active")
    public ResponseEntity<?> toggleServiceActive(@PathVariable String id) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Service> serviceOpt = serviceRepository.findById(id);
        if (serviceOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, null, "Service not found."));
        }

        Service service = serviceOpt.get();
        if (service.getProviderId() != null && !service.getProviderId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(403, null, "Not authorized."));
        }

        service.setActive(!service.isActive());
        service.setUpdatedAt(new Date());
        Service savedService = serviceRepository.save(service);

        Map<String, Object> data = new HashMap<>();
        data.put("service", savedService);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Service " + (savedService.isActive() ? "activated" : "deactivated") + "."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteService(@PathVariable String id) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Service> serviceOpt = serviceRepository.findById(id);
        if (serviceOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, null, "Service not found."));
        }

        Service service = serviceOpt.get();
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (service.getProviderId() != null && !service.getProviderId().equals(currentUser.getId()) && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(403, null, "Not authorized."));
        }

        serviceRepository.delete(service);
        return ResponseEntity.ok(new ApiResponse<>(200, null, "Service deleted."));
    }
}
