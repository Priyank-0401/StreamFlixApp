package com.infy.billing.dto.customer;

import com.infy.billing.enums.Status;

import lombok.Data;

@Data
public class PaymentDTO {
   private Long paymentId;
   private Long invoiceId;
   private String invoiceNumber;
   private Long amountMinor;
   private String currency;
   private Status status;
   private Integer attemptNo;
   private String failureReason;
   private String createdAt;
}
