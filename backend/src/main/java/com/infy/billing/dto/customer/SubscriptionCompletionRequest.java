package com.infy.billing.dto.customer;

import com.infy.billing.enums.BillingPeriod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubscriptionCompletionRequest {
    
    @NotNull(message = "Plan ID is required")
    private Long planId;
    
    @NotNull(message = "Payment method ID is required")
    private Long paymentMethodId;
    
    @NotNull(message = "Billing period is required")
    private BillingPeriod billingPeriod;
}
