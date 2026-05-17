package com.infy.billing.repository;

import com.infy.billing.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
	List<Invoice> findByCustomer_IdOrderByIssueDateDesc(Long customerId);
	List<Invoice> findByCustomer_IdAndStatus(Long customerId, String status);
	List<Invoice> findByCustomer_Id(Long customerId);
    java.util.Optional<Invoice> findBySubscription_IdAndStatus(Long subscriptionId, com.infy.billing.enums.Status status);
    
    boolean existsBySubscriptionAndBillingReasonAndIssueDate(com.infy.billing.entity.Subscription subscription, com.infy.billing.enums.BillingReason billingReason, java.time.LocalDate issueDate);
    org.springframework.data.domain.Page<Invoice> findAllByOrderByIdDesc(org.springframework.data.domain.Pageable pageable);
}
