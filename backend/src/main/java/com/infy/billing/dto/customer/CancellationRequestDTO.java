package com.infy.billing.dto.customer;

import lombok.Data;
import com.infy.billing.enums.CancellationRequestStatus;

@Data
public class CancellationRequestDTO {
    private Long requestId;
    private Long subscriptionId;
    private String planName;
    private String customerEmail;
    private String customerName;
    private String reason;
    private CancellationRequestStatus status;
    private Boolean atPeriodEnd;
    private String createdAt;
    private String processedByEmail;
    private String processedAt;
    private String agentNotes;
}
