package com.infy.billing.dto.customer;

import com.infy.billing.enums.BillingPeriod;
import com.infy.billing.enums.Status;

import lombok.Data;

@Data
public class AddOnDTO {
   private Long addOnId;
   private String name;
   private Long priceMinor;
   private String currency;
   private BillingPeriod billingPeriod;
   private Status status;
}