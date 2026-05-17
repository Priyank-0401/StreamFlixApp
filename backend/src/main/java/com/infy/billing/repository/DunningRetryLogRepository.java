package com.infy.billing.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.infy.billing.entity.DunningRetryLog;

import java.time.LocalDateTime;
import java.util.List;
import com.infy.billing.enums.DunningStatus;


public interface DunningRetryLogRepository extends JpaRepository<DunningRetryLog,Long>{
	
	List<DunningRetryLog> findByStatusAndScheduledAtLessThanEqual(DunningStatus status,LocalDateTime time);
}
