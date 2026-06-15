package com.sevasarthi.backend.controllers;

import com.sevasarthi.backend.dto.ApiResponse;
import com.sevasarthi.backend.models.Coupon;
import com.sevasarthi.backend.repository.BookingRepository;
import com.sevasarthi.backend.repository.CouponRepository;
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
@RequestMapping("/api/coupons")
public class CouponController {

    @Autowired
    CouponRepository couponRepository;

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

    private boolean isAdmin(UserDetailsImpl user) {
        return user != null && user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    

    @PostMapping
    public ResponseEntity<?> createCoupon(@RequestBody Coupon request) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        request.setCreatedAt(new Date());
        request.setUsedCount(0);
        Coupon savedCoupon = couponRepository.save(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, savedCoupon, "Coupon created successfully"));
    }

    @GetMapping("/admin")
    public ResponseEntity<?> getAllCoupons() {
        UserDetailsImpl currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Coupon> coupons = mongoTemplate.find(query, Coupon.class);

        return ResponseEntity.ok(new ApiResponse<>(200, coupons, "Coupons fetched successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCoupon(@PathVariable String id, @RequestBody Coupon updatedFields) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        Optional<Coupon> couponOpt = couponRepository.findById(id);
        if (couponOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, null, "Coupon not found"));
        }

        Coupon coupon = couponOpt.get();
        if (updatedFields.getCode() != null) coupon.setCode(updatedFields.getCode());
        if (updatedFields.getTitle() != null) coupon.setTitle(updatedFields.getTitle());
        if (updatedFields.getSubtitle() != null) coupon.setSubtitle(updatedFields.getSubtitle());
        if (updatedFields.getDescription() != null) coupon.setDescription(updatedFields.getDescription());
        if (updatedFields.getDiscountType() != null) coupon.setDiscountType(updatedFields.getDiscountType());
        if (updatedFields.getDiscountValue() != null) coupon.setDiscountValue(updatedFields.getDiscountValue());
        if (updatedFields.getMaxDiscount() != null) coupon.setMaxDiscount(updatedFields.getMaxDiscount());
        if (updatedFields.getMinOrderAmount() != null) coupon.setMinOrderAmount(updatedFields.getMinOrderAmount());
        if (updatedFields.getExpiresAt() != null) coupon.setExpiresAt(updatedFields.getExpiresAt());
        if (updatedFields.getMaxUses() != null) coupon.setMaxUses(updatedFields.getMaxUses());
        if (updatedFields.getUserType() != null) coupon.setUserType(updatedFields.getUserType());
        if (updatedFields.getImageUrl() != null) coupon.setImageUrl(updatedFields.getImageUrl());
        if (updatedFields.getTargetUrl() != null) coupon.setTargetUrl(updatedFields.getTargetUrl());
        coupon.setBannerOnly(updatedFields.isBannerOnly());
        coupon.setShowOnHome(updatedFields.isShowOnHome());
        coupon.setActive(updatedFields.isActive());

        coupon.setUpdatedAt(new Date());

        Coupon savedCoupon = couponRepository.save(coupon);
        return ResponseEntity.ok(new ApiResponse<>(200, savedCoupon, "Coupon updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCoupon(@PathVariable String id) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        Optional<Coupon> couponOpt = couponRepository.findById(id);
        if (couponOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, null, "Coupon not found"));
        }

        couponRepository.delete(couponOpt.get());
        return ResponseEntity.ok(new ApiResponse<>(200, null, "Coupon deleted successfully"));
    }

    

    @GetMapping("/home")
    public ResponseEntity<?> getHomeOffers() {
        Query query = new Query(Criteria.where("showOnHome").is(true).and("isActive").is(true));
        query.fields().include("title", "subtitle", "imageUrl", "targetUrl", "isBannerOnly", "code", "userType");
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        
        List<Coupon> offers = mongoTemplate.find(query, Coupon.class);
        return ResponseEntity.ok(new ApiResponse<>(200, offers, "Home offers fetched"));
    }

    @GetMapping("/eligible")
    public ResponseEntity<?> getEligibleCoupons() {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        long previousBookings = bookingRepository.countByUserId(currentUser.getId());
        boolean isNewUser = previousBookings == 0;

        Criteria expiryCriteria = new Criteria().orOperator(
                Criteria.where("expiresAt").isNull(),
                Criteria.where("expiresAt").gt(new Date())
        );

        Criteria queryCriteria = Criteria.where("isActive").is(true)
                .and("isBannerOnly").is(false)
                .andOperator(expiryCriteria);

        if (!isNewUser) {
            queryCriteria.and("userType").is("all");
        }

        Query query = new Query(queryCriteria);
        List<Coupon> coupons = mongoTemplate.find(query, Coupon.class);
        
        return ResponseEntity.ok(new ApiResponse<>(200, coupons, "Eligible coupons fetched"));
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateCoupon(@RequestBody Map<String, Object> body) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        String code = (String) body.get("code");
        Number orderAmountNum = (Number) body.get("orderAmount");
        double orderAmount = orderAmountNum != null ? orderAmountNum.doubleValue() : 0.0;

        if (code == null || code.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(400, null, "Coupon code is required."));
        }

        Query query = new Query(Criteria.where("code").is(code.toUpperCase())
                .and("isActive").is(true)
                .and("isBannerOnly").is(false));
        
        Coupon coupon = mongoTemplate.findOne(query, Coupon.class);

        if (coupon == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, null, "Invalid or expired coupon code."));
        }

        if (coupon.getExpiresAt() != null && new Date().after(coupon.getExpiresAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(400, null, "This coupon has expired."));
        }

        if (coupon.getMaxUses() != null && coupon.getUsedCount() != null && coupon.getUsedCount() >= coupon.getMaxUses()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(400, null, "This coupon has reached its usage limit."));
        }

        if (coupon.getMinOrderAmount() != null && orderAmount < coupon.getMinOrderAmount()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(400, null, "Minimum order amount of ₹" + coupon.getMinOrderAmount() + " required."));
        }

        if ("new".equals(coupon.getUserType())) {
            long previousBookings = bookingRepository.countByUserId(currentUser.getId());
            if (previousBookings > 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(400, null, "This coupon is only valid for first-time users."));
            }
        }

        double discount = 0.0;
        if ("flat".equals(coupon.getDiscountType())) {
            discount = coupon.getDiscountValue();
        } else if ("percent".equals(coupon.getDiscountType())) {
            discount = (orderAmount * coupon.getDiscountValue()) / 100;
            if (coupon.getMaxDiscount() != null) {
                discount = Math.min(discount, coupon.getMaxDiscount());
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("_id", coupon.getId());
        data.put("code", coupon.getCode());
        data.put("discountType", coupon.getDiscountType());
        data.put("discountValue", coupon.getDiscountValue());
        data.put("discount", Math.round(discount));
        data.put("description", coupon.getDescription());

        return ResponseEntity.ok(new ApiResponse<>(200, data, "Coupon is valid."));
    }
}
