package com.infy.billing.dto.customer;

import com.infy.billing.enums.PaymentType;
import com.infy.billing.enums.Status;

import lombok.Data;

@Data
public class PaymentMethodDTO {
   private Long paymentMethodId;
   private PaymentType paymentType;
   private String cardLast4;
   private String cardBrand;
   private Boolean isDefault;
   private Integer expiryMonth;
   private Integer expiryYear;
   private Status status;
}