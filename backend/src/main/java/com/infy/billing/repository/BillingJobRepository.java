package com.infy.billing.repository;

import com.infy.billing.entity.BillingJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillingJobRepository extends JpaRepository<BillingJob, Long> {
}
