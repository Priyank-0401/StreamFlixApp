package com.infy.billing.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "revenue_snapshot")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RevenueSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "snapshot_id")
    private Long id;

    @Column(name = "snapshot_date", nullable = false, unique = true)
    private LocalDate snapshotDate;

    @Column(name = "mrr_minor", nullable = false)
    private Long mrrMinor;

    @Column(name = "arr_minor", nullable = false)
    private Long arrMinor;

    @Column(name = "arpu_minor", nullable = false)
    private Long arpuMinor;

    @Column(name = "active_customers", nullable = false)
    private Integer activeCustomers;

    @Column(name = "new_customers", nullable = false)
    private Integer newCustomers;

    @Column(name = "churned_customers", nullable = false)
    private Integer churnedCustomers;

    @Column(name = "gross_churn_percent", nullable = false)
    private Double grossChurnPercent;

    @Column(name = "net_churn_percent", nullable = false)
    private Double netChurnPercent;

    @Column(name = "ltv_minor", nullable = false)
    private Long ltvMinor;

    @Column(name = "total_revenue_minor", nullable = false)
    private Long totalRevenueMinor;

    @Column(name = "total_refunds_minor", nullable = false)
    private Long totalRefundsMinor;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
