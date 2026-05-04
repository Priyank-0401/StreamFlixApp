package com.infy.billing.request;

import lombok.Data;

@Data
public class CreateSubscriptionRequest {
   private Long planId;
   private Long paymentMethodId;
   private String couponCode;
}