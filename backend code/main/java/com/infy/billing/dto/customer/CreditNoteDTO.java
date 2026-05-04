package com.infy.billing.dto.customer;

import com.infy.billing.enums.Status;

import lombok.Data;

@Data
public class CreditNoteDTO {
   private Long creditNoteId;
   private String creditNoteNumber;
   private Long invoiceId;
   private String invoiceNumber;
   private String reason;
   private Long amountMinor;
   private Status status;
   private String createdAt;
}