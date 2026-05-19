package com.infy.billing.controller;

import com.infy.billing.dto.support.CustomerSearchResponse;
import com.infy.billing.dto.support.CustomerDetailResponse;
import com.infy.billing.entity.AuditLog;
import com.infy.billing.entity.BillingJob;
import com.infy.billing.entity.DunningRetryLog;
import com.infy.billing.service.SupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.infy.billing.dto.customer.CancellationRequestDTO;
import com.infy.billing.dto.customer.CancellationResponse;
import com.infy.billing.request.ProcessCancellationRequestInput;
import com.infy.billing.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPPORT')")
public class SupportController {

    private final SupportService supportService;

    @GetMapping("/customers")
    public ResponseEntity<List<CustomerSearchResponse>> searchCustomers(@RequestParam String query) {
        return ResponseEntity.ok(supportService.searchCustomers(query));
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<CustomerDetailResponse> getCustomerDetails(@PathVariable Long id) {
        return ResponseEntity.ok(supportService.getCustomerDetails(id));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getRecentAuditLogs() {
        return ResponseEntity.ok(supportService.getRecentAuditLogs());
    }

    @GetMapping("/billing-jobs")
    public ResponseEntity<List<BillingJob>> getBillingJobs() {
        return ResponseEntity.ok(supportService.getBillingJobs());
    }

    @GetMapping("/dunning-logs")
    public ResponseEntity<List<DunningRetryLog>> getDunningLogs() {
        return ResponseEntity.ok(supportService.getDunningLogs());
    }

    @GetMapping("/subscriptions/past-due")
    public ResponseEntity<List<com.infy.billing.dto.customer.SubscriptionDTO>> getPastDueSubscriptions() {
        return ResponseEntity.ok(supportService.getPastDueSubscriptions());
    }

    @GetMapping("/cancellation-requests")
    public ResponseEntity<List<CancellationRequestDTO>> getPendingCancellationRequests() {
        return ResponseEntity.ok(supportService.getPendingCancellationRequests());
    }

    @PostMapping("/cancellation-requests/{id}/approve")
    public ResponseEntity<CancellationResponse> approveCancellationRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal User agent,
            @RequestBody ProcessCancellationRequestInput input) {
        return ResponseEntity.ok(supportService.approveCancellationRequest(id, agent.getEmail(), input));
    }

    @PostMapping("/cancellation-requests/{id}/reject")
    public ResponseEntity<Void> rejectCancellationRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal User agent,
            @RequestBody ProcessCancellationRequestInput input) {
        supportService.rejectCancellationRequest(id, agent.getEmail(), input);
        return ResponseEntity.noContent().build();
    }
}
