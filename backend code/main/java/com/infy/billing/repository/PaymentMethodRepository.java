package com.infy.billing.repository;

import com.infy.billing.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
	List<PaymentMethod> findByCustomer_Id(Long customerId);
}