package com.infy.billing.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.infy.billing.entity.CancellationRequest;
import com.infy.billing.enums.CancellationRequestStatus;

public interface CancellationRequestRepository extends JpaRepository<CancellationRequest, Long> {
    Optional<CancellationRequest> findBySubscription_Customer_IdAndStatus(Long customerId, CancellationRequestStatus status);
    List<CancellationRequest> findByStatus(CancellationRequestStatus status);
}
