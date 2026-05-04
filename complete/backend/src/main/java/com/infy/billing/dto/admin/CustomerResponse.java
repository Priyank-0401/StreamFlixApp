package com.infy.billing.dto.admin;

import com.infy.billing.entity.Customer;
import com.infy.billing.enums.Status;

import lombok.Data;

@Data
public class CustomerResponse {
   private Long id;
   private Long userId;
   private String fullName;
   private String email;
   private String phone;
   private String currency;
   private String country;
   private Status status;
   private String createdAt;

   public static CustomerResponse from(Customer customer) {
       CustomerResponse r = new CustomerResponse();
       r.setId(customer.getId());
       r.setUserId(customer.getUser().getId());
       r.setFullName(customer.getUser().getFullName());
       r.setEmail(customer.getUser().getEmail());
       r.setPhone(customer.getPhone());
       r.setCurrency(customer.getCurrency());
       r.setCountry(customer.getCountry());
       r.setStatus(customer.getStatus());
       r.setCreatedAt(customer.getCreatedAt() != null ? customer.getCreatedAt().toString() : null);
       return r;
   }
}
