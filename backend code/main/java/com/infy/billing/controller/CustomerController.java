package com.infy.billing.controller;

import com.infy.billing.dto.customer.*;
import com.infy.billing.entity.Customer;
import com.infy.billing.entity.PaymentMethod;
import com.infy.billing.entity.User;
import com.infy.billing.service.CustomerService;
import com.infy.billing.service.SubscriptionFlowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerController {

   private final CustomerService customerService;
   private final SubscriptionFlowService subscriptionFlowService;

   @GetMapping("/me")
   public ResponseEntity<CustomerProfileDTO> getProfile(@AuthenticationPrincipal User user) {
       return ResponseEntity.ok(customerService.getProfile(user.getEmail()));
   }

   @PutMapping("/me")
   public ResponseEntity<CustomerProfileDTO> updateProfile(
           @AuthenticationPrincipal User user,
           @RequestBody CustomerProfileDTO dto) {
       return ResponseEntity.ok(customerService.updateProfile(user.getEmail(), dto));
   }

   @GetMapping("/plans")
   public ResponseEntity<List<PlanDTO>> getAvailablePlans() {
       return ResponseEntity.ok(customerService.getAvailablePlans());
   }

   @GetMapping("/plans/featured")
   @PreAuthorize("permitAll()")
   public ResponseEntity<List<PlanDTO>> getFeaturedPlans() {
       return ResponseEntity.ok(customerService.getFeaturedPlans());
   }

   @GetMapping("/plans/all")
   @PreAuthorize("permitAll()")
   public ResponseEntity<List<PlanDTO>> getAllPlans() {
       return ResponseEntity.ok(customerService.getAllActivePlans());
   }

   @GetMapping("/addons")
   public ResponseEntity<List<AddOnDTO>> getAvailableAddOns() {
       return ResponseEntity.ok(customerService.getAvailableAddOns());
   }

   @GetMapping("/notifications")
   public ResponseEntity<List<NotificationDTO>> getNotifications(@AuthenticationPrincipal User user) {
       return ResponseEntity.ok(customerService.getNotifications(user.getEmail()));
   }

   @PutMapping("/notifications/{id}/read")
   public ResponseEntity<Void> markNotificationAsRead(
           @AuthenticationPrincipal User user,
           @PathVariable Long id) {
       customerService.markNotificationAsRead(user.getEmail(), id);
       return ResponseEntity.ok().build();
   }

   // ========== SUBSCRIPTION FLOW ENDPOINTS ==========

   @GetMapping("/status")
   public ResponseEntity<CustomerStatusResponse> checkCustomerStatus(@AuthenticationPrincipal User user) {
       return ResponseEntity.ok(subscriptionFlowService.checkCustomerStatus(user.getEmail()));
   }

   @PostMapping("/register-details")
   public ResponseEntity<Map<String, Object>> registerCustomerDetails(
           @AuthenticationPrincipal User user,
           @Valid @RequestBody CustomerRegistrationRequest request) {
       Customer customer = subscriptionFlowService.registerCustomerDetails(user.getEmail(), request);
       Map<String, Object> response = new HashMap<>();
       response.put("customerId", customer.getId());
       response.put("message", "Customer details registered successfully");
       return ResponseEntity.ok(response);
   }

   @PostMapping("/payment-method")
   public ResponseEntity<Map<String, Object>> createPaymentMethod(
           @RequestParam Long customerId,
           @Valid @RequestBody PaymentMethodRequest request) {
       PaymentMethod paymentMethod = subscriptionFlowService.createPaymentMethod(customerId, request);
       Map<String, Object> response = new HashMap<>();
       response.put("paymentMethodId", paymentMethod.getId());
       response.put("last4", paymentMethod.getCardLast4());
       response.put("brand", paymentMethod.getCardBrand());
       response.put("message", "Payment method created successfully");
       return ResponseEntity.ok(response);
   }

   @PostMapping("/subscription/complete")
   public ResponseEntity<SubscriptionResponse> completeSubscription(
           @RequestParam Long customerId,
           @Valid @RequestBody SubscriptionCompletionRequest request) {
       SubscriptionResponse response = subscriptionFlowService.completeSubscription(customerId, request);
       return ResponseEntity.ok(response);
   }
}
