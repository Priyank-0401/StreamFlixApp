package com.infy.billing.dto.admin;

import com.infy.billing.entity.AddOn;
import com.infy.billing.enums.BillingPeriod;
import com.infy.billing.enums.Status;
import com.infy.billing.enums.TaxMode;

import lombok.Data;

@Data
public class AddOnResponse {
   private Long id;
   private String name;
   private Long priceMinor;
   private String currency;
   private BillingPeriod billingPeriod;
   private TaxMode taxMode;
   private Status status;

   public static AddOnResponse from(AddOn addOn) {
       AddOnResponse r = new AddOnResponse();
       r.setId(addOn.getId());
       r.setName(addOn.getName());
       r.setPriceMinor(addOn.getPriceMinor());
       r.setCurrency(addOn.getCurrency());
       r.setBillingPeriod(addOn.getBillingPeriod());
       r.setTaxMode(addOn.getTaxMode());
       r.setStatus(addOn.getStatus());
       return r;
   }
}
