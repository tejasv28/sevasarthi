package com.sevasarthi.backend.controllers;

import com.sevasarthi.backend.dto.ApiResponse;
import com.sevasarthi.backend.models.Tool;
import com.sevasarthi.backend.models.User;
import com.sevasarthi.backend.repository.ToolRepository;
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
@RequestMapping("/api/tools")
public class ToolController {

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

    @GetMapping
    public ResponseEntity<?> getAllTools(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {

        Query query = new Query();

        if (category != null && !category.isEmpty() && !"All".equals(category)) {
            query.addCriteria(Criteria.where("category").is(category));
        }

        if (status != null && !status.isEmpty()) {
            query.addCriteria(Criteria.where("status").is(status));
        }

        if (search != null && !search.isEmpty()) {
            String escaped = search.replaceAll("[.*+?^${}()|\\[\\]\\\\]", "\\\\$0");
            Criteria searchCriteria = new Criteria().orOperator(
                    Criteria.where("name").regex("(?:^|[\\s\\-_/,&()])" + escaped, "i"),
                    Criteria.where("description").regex("(?:^|[\\s\\-_/,&()])" + escaped, "i")
            );
            query.addCriteria(searchCriteria);
        }

        if (city != null && !city.isEmpty()) {
            List<User> usersInCity = mongoTemplate.find(
                    new Query(Criteria.where("address.city").regex("^" + city + "$", "i")),
                    User.class
            );
            List<String> userIds = usersInCity.stream().map(User::getId).toList();

            if (!userIds.isEmpty()) {
                query.addCriteria(Criteria.where("ownerId").in(userIds));
            } else {
                query.addCriteria(Criteria.where("ownerId").isNull()); 
            }
        }

        long total = mongoTemplate.count(query, Tool.class);

        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        query.with(PageRequest.of(page - 1, limit));

        List<Tool> tools = mongoTemplate.find(query, Tool.class);

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("total", total);
        pagination.put("page", page);
        pagination.put("pages", (int) Math.ceil((double) total / limit));

        Map<String, Object> data = new HashMap<>();
        data.put("tools", tools);
        data.put("pagination", pagination);

        return ResponseEntity.ok(new ApiResponse<>(200, data, "Tools retrieved."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTool(@PathVariable String id) {
        Optional<Tool> toolOpt = toolRepository.findById(id);
        if (toolOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, null, "Tool not found."));
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("tool", toolOpt.get());
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Tool retrieved."));
    }

    @PostMapping
    public ResponseEntity<?> createTool(@RequestBody Tool requestTool) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Tool tool = Tool.builder()
                .name(requestTool.getName())
                .description(requestTool.getDescription() != null ? requestTool.getDescription() : "")
                .category(requestTool.getCategory())
                .condition(requestTool.getCondition() != null ? requestTool.getCondition() : "Good")
                .dailyRate(requestTool.getDailyRate())
                .image(requestTool.getImage() != null ? requestTool.getImage() : "")
                .ownerId(currentUser.getId())
                .status("available")
                .createdAt(new Date())
                .build();
                
        Tool savedTool = toolRepository.save(tool);
        
        Map<String, Object> data = new HashMap<>();
        data.put("tool", savedTool);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, data, "Tool listed successfully."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTool(@PathVariable String id, @RequestBody Tool updatedFields) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Tool> toolOpt = toolRepository.findById(id);
        if (toolOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, null, "Tool not found."));
        }

        Tool tool = toolOpt.get();
        if (!tool.getOwnerId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(403, null, "Not authorized."));
        }

        if (updatedFields.getName() != null) tool.setName(updatedFields.getName());
        if (updatedFields.getDescription() != null) tool.setDescription(updatedFields.getDescription());
        if (updatedFields.getCategory() != null) tool.setCategory(updatedFields.getCategory());
        if (updatedFields.getCondition() != null) tool.setCondition(updatedFields.getCondition());
        if (updatedFields.getDailyRate() != null) tool.setDailyRate(updatedFields.getDailyRate());
        if (updatedFields.getImage() != null) tool.setImage(updatedFields.getImage());
        if (updatedFields.getStatus() != null) tool.setStatus(updatedFields.getStatus());
        
        tool.setUpdatedAt(new Date());

        Tool savedTool = toolRepository.save(tool);
        
        Map<String, Object> data = new HashMap<>();
        data.put("tool", savedTool);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Tool updated."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTool(@PathVariable String id) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Tool> toolOpt = toolRepository.findById(id);
        if (toolOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, null, "Tool not found."));
        }

        Tool tool = toolOpt.get();
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!tool.getOwnerId().equals(currentUser.getId()) && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(403, null, "Not authorized."));
        }

        toolRepository.delete(tool);
        return ResponseEntity.ok(new ApiResponse<>(200, null, "Tool deleted."));
    }

    @GetMapping("/my-tools")
    public ResponseEntity<?> getMyTools() {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Query query = new Query(Criteria.where("ownerId").is(currentUser.getId()));
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        
        List<Tool> tools = mongoTemplate.find(query, Tool.class);
        
        Map<String, Object> data = new HashMap<>();
        data.put("tools", tools);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Your tools retrieved."));
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleToolStatus(@PathVariable String id) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Tool> toolOpt = toolRepository.findById(id);
        if (toolOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, null, "Tool not found."));
        }

        Tool tool = toolOpt.get();
        if (!tool.getOwnerId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(403, null, "Not authorized."));
        }

        tool.setStatus("available".equals(tool.getStatus()) ? "maintenance" : "available");
        tool.setUpdatedAt(new Date());

        Tool savedTool = toolRepository.save(tool);
        
        Map<String, Object> data = new HashMap<>();
        data.put("tool", savedTool);
        return ResponseEntity.ok(new ApiResponse<>(200, data, "Tool " + ("available".equals(savedTool.getStatus()) ? "activated" : "deactivated") + "."));
    }
}
