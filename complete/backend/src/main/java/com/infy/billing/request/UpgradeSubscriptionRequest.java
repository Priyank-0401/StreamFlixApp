package com.infy.billing.request;

import lombok.Data;

@Data
public class UpgradeSubscriptionRequest {
   private Long planId;
   private Boolean proration;
}