package com.infy.billing.dto.admin;

import com.infy.billing.entity.Plan;
import com.infy.billing.enums.BillingPeriod;
import com.infy.billing.enums.Status;
import com.infy.billing.enums.TaxMode;

import lombok.Data;

@Data
public class PlanResponse {
   private Long id;
   private String name;
   private BillingPeriod billingPeriod;
   private Long defaultPriceMinor;
   private String defaultCurrency;
   private Integer trialDays;
   private Long setupFeeMinor;
   private TaxMode taxMode;
   private String effectiveFrom;
   private String effectiveTo;
   private Status status;
   private Long productId;
   private String productName;

   public static PlanResponse from(Plan plan) {
       PlanResponse r = new PlanResponse();
       r.setId(plan.getId());
       r.setName(plan.getName());
       r.setBillingPeriod(plan.getBillingPeriod());
       r.setDefaultPriceMinor(plan.getDefaultPriceMinor());
       r.setDefaultCurrency(plan.getDefaultCurrency());
       r.setTrialDays(plan.getTrialDays());
       r.setSetupFeeMinor(plan.getSetupFeeMinor());
       r.setTaxMode(plan.getTaxMode());
       r.setEffectiveFrom(plan.getEffectiveFrom() != null ? plan.getEffectiveFrom().toString() : null);
       r.setEffectiveTo(plan.getEffectiveTo() != null ? plan.getEffectiveTo().toString() : null);
       r.setStatus(plan.getStatus());
       r.setProductId(plan.getProduct().getId());
       r.setProductName(plan.getProduct().getName());
       return r;
   }
}
