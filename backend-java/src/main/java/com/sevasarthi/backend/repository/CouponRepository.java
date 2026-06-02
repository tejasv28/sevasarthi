package com.sevasarthi.backend.repository;

import com.sevasarthi.backend.models.Coupon;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponRepository extends MongoRepository<Coupon, String> {
    List<Coupon> findByIsActiveTrue();
}
