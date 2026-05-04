package com.infy.billing.dto.auth;

import com.infy.billing.entity.User;

import lombok.Data;

@Data
public class UserResponse {
   private String email;
   private String fullName;
   private String role;

   public UserResponse(User user) {
       this.email = user.getEmail();
       this.fullName = user.getFullName();
       this.role = user.getRole().name();
   }
}