package com.infy.billing.repository;

import com.infy.billing.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByCustomer_IdOrderByCreatedAtDesc(Long customerId);
    List<Notification> findByCustomer_IdAndStatusOrderByCreatedAtDesc(Long customerId, Notification.Status status);
}
