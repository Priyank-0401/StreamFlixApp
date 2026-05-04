package com.infy.billing.dto.customer;

import com.infy.billing.entity.InvoiceLineItem.LineType;

import lombok.Data;

@Data
public class InvoiceLineItemDTO {
   private Long lineItemId;
   private String description;
   private LineType lineType;
   private Integer quantity;
   private Long unitPriceMinor;
   private Long amountMinor;
   private String periodStart;
   private String periodEnd;
}