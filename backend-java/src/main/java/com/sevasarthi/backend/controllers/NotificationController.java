package com.sevasarthi.backend.controllers;

import com.sevasarthi.backend.dto.ApiResponse;
import com.sevasarthi.backend.models.Notification;
import com.sevasarthi.backend.repository.NotificationRepository;
import com.sevasarthi.backend.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    NotificationRepository notificationRepository;

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
    public ResponseEntity<?> getNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Query query = new Query(Criteria.where("userId").is(currentUser.getId()));
        long total = mongoTemplate.count(query, Notification.class);

        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        query.with(PageRequest.of(page - 1, limit));
        List<Notification> notifications = mongoTemplate.find(query, Notification.class);

        Query unreadQuery = new Query(Criteria.where("userId").is(currentUser.getId()).and("isRead").is(false));
        long unreadCount = mongoTemplate.count(unreadQuery, Notification.class);

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("total", total);
        pagination.put("page", page);
        pagination.put("pages", (int) Math.ceil((double) total / limit));

        Map<String, Object> data = new HashMap<>();
        data.put("notifications", notifications);
        data.put("unreadCount", unreadCount);
        data.put("pagination", pagination);

        return ResponseEntity.ok(new ApiResponse<>(200, data, "Notifications retrieved."));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable String id) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Notification> notificationOpt = notificationRepository.findById(id);
        if (notificationOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, null, "Notification not found."));
        }

        Notification notification = notificationOpt.get();
        if (!notification.getUserId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(403, null, "Not authorized."));
        }

        notification.setRead(true);
        notificationRepository.save(notification);

        return ResponseEntity.ok(new ApiResponse<>(200, null, "Marked as read."));
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Query query = new Query(Criteria.where("userId").is(currentUser.getId()).and("isRead").is(false));
        Update update = new Update().set("isRead", true);
        mongoTemplate.updateMulti(query, update, Notification.class);

        return ResponseEntity.ok(new ApiResponse<>(200, null, "All notifications marked as read."));
    }

    @DeleteMapping("/clear-all")
    public ResponseEntity<?> clearAll() {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Query query = new Query(Criteria.where("userId").is(currentUser.getId()));
        mongoTemplate.remove(query, Notification.class);

        return ResponseEntity.ok(new ApiResponse<>(200, null, "All notifications cleared."));
    }
}
