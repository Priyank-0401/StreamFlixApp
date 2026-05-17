package com.infy.billing.repository;

import com.infy.billing.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
	List<Payment> findByInvoice_Id(Long invoiceId);

	List<Payment> findByStatus(com.infy.billing.enums.Status status);

	List<Payment> findAllByOrderByCreatedAtDesc();

	Optional<Payment> findFirstByInvoiceIdOrderByCreatedAtDesc(Long id);
}