package com.infy.billing.controller;

import com.infy.billing.dto.customer.*;
import com.infy.billing.entity.User;
import com.infy.billing.request.*;
import com.infy.billing.service.CustomerSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerSubscriptionController {

   private final CustomerSubscriptionService subscriptionService;

   @GetMapping("/subscription")
   public ResponseEntity<SubscriptionDTO> getCurrentSubscription(@AuthenticationPrincipal User user) {
       SubscriptionDTO subscription = subscriptionService.getCurrentSubscription(user.getEmail());
       return subscription != null ? ResponseEntity.ok(subscription) : ResponseEntity.notFound().build();
   }

   @PostMapping("/subscription")
   public ResponseEntity<SubscriptionDTO> createSubscription(
           @AuthenticationPrincipal User user,
           @RequestBody CreateSubscriptionRequest request) {
       return ResponseEntity.ok(subscriptionService.createSubscription(user.getEmail(), request));
   }

   @PutMapping("/subscription/upgrade")
   public ResponseEntity<SubscriptionDTO> upgradeSubscription(
           @AuthenticationPrincipal User user,
           @RequestBody UpgradeSubscriptionRequest request) {
       return ResponseEntity.ok(subscriptionService.upgradeSubscription(user.getEmail(), request));
   }

   @DeleteMapping("/subscription")
   public ResponseEntity<Void> cancelSubscription(
           @AuthenticationPrincipal User user,
           @RequestParam(defaultValue = "true") boolean atPeriodEnd) {
       subscriptionService.cancelSubscription(user.getEmail(), atPeriodEnd);
       return ResponseEntity.ok().build();
   }

   @PutMapping("/subscription/pause")
   public ResponseEntity<SubscriptionDTO> pauseSubscription(
           @AuthenticationPrincipal User user,
           @RequestBody PauseSubscriptionRequest request) {
       return ResponseEntity.ok(subscriptionService.pauseSubscription(user.getEmail(), request));
   }

   @PutMapping("/subscription/resume")
   public ResponseEntity<SubscriptionDTO> resumeSubscription(@AuthenticationPrincipal User user) {
       return ResponseEntity.ok(subscriptionService.resumeSubscription(user.getEmail()));
   }

   @PostMapping("/addons/{addonId}")
   public ResponseEntity<Void> addAddOn(
           @AuthenticationPrincipal User user,
           @PathVariable Long addonId) {
       subscriptionService.addAddOn(user.getEmail(), addonId);
       return ResponseEntity.ok().build();
   }

   @DeleteMapping("/addons/{addonId}")
   public ResponseEntity<Void> removeAddOn(
           @AuthenticationPrincipal User user,
           @PathVariable Long addonId) {
       subscriptionService.removeAddOn(user.getEmail(), addonId);
       return ResponseEntity.ok().build();
   }

   @GetMapping("/usage")
   public ResponseEntity<List<UsageRecordDTO>> getMeteredUsage(
           @AuthenticationPrincipal User user,
           @RequestParam(required = false) String startDate,
           @RequestParam(required = false) String endDate) {
       return ResponseEntity.ok(subscriptionService.getMeteredUsage(user.getEmail(), startDate, endDate));
   }
}
