package com.infy.billing.repository;

import com.infy.billing.entity.RevenueSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RevenueSnapshotRepository extends JpaRepository<RevenueSnapshot, Long> {

    Optional<RevenueSnapshot> findBySnapshotDate(LocalDate snapshotDate);

    List<RevenueSnapshot> findBySnapshotDateBetweenOrderBySnapshotDateAsc(LocalDate startDate, LocalDate endDate);

    // Returns all 12 monthly snapshots ordered oldest → newest (Jan–Dec 2026 in seed data)
    List<RevenueSnapshot> findAllByOrderBySnapshotDateAsc();
}
