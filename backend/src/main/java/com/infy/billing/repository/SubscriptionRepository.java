package com.infy.billing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.infy.billing.entity.Subscription;
import com.infy.billing.enums.Status;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
   long countByStatus(Status status);

   List<Subscription> findByCustomer_IdAndStatusIn(Long id, List<Status> statuses);
   Optional<Subscription> findByCustomer_IdAndStatus(Long customerId, Status status);

   long countByPlan_IdAndStatusIn(Long planId, List<Status> statuses);

   List<Subscription> findByStatusInAndCurrentPeriodEndLessThan(List<Status> statuses, java.time.LocalDate date);
   List<Subscription> findByStatusIn(List<Status> statuses);
}