package com.infy.billing.request;

import lombok.Data;

@Data
public class CancellationRequestInput {
    private String reason;
    private boolean atPeriodEnd;
}
