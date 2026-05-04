package com.infy.billing.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.infy.billing.entity.Coupon;
import com.infy.billing.enums.Status;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
   Optional<Coupon> findByCode(String code);
   boolean existsByCode(String code);
   Optional<Coupon> findByCodeAndStatus(String code, Status active);
   long countByStatus(Status status);
}