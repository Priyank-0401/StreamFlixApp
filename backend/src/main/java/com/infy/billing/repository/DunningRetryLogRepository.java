package com.infy.billing.repository;

import com.infy.billing.entity.DunningRetryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DunningRetryLogRepository extends JpaRepository<DunningRetryLog, Long> {
    List<DunningRetryLog> findByStatusAndScheduledAtLessThanEqual(DunningRetryLog.Status status, LocalDateTime scheduledAt);
    List<DunningRetryLog> findByInvoice_IdOrderByAttemptNoDesc(Long invoiceId);
}
