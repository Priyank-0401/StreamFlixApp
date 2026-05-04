package com.infy.billing.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.infy.billing.entity.AddOn;
import com.infy.billing.enums.Status;

public interface AddOnRepository extends JpaRepository<AddOn, Long> {

	   List<AddOn> findByStatus(Status status);
}