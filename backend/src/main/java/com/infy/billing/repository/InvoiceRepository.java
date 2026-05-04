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
}
