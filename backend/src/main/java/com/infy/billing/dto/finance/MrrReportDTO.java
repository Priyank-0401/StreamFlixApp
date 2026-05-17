package com.infy.billing.dto.finance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MrrReportDTO {
    private long mrrMinor;                       // current MRR in INR paise
    private long expansionMinor;                 // MRR gained from upgrades
    private long contractionMinor;               // MRR lost from downgrades
    private long newMrrMinor;                    // MRR from new customers
    private long reactivationMinor;              // MRR from reactivated customers
    private List<MonthlyTrendDTO> revenueTrend;  // all months from snapshots, valueMinor = MRR
}
