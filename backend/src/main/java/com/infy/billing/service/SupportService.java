package com.infy.billing.service;

import com.infy.billing.dto.support.CustomerSearchResponse;
import com.infy.billing.dto.customer.SubscriptionDTO;
import com.infy.billing.dto.support.CustomerDetailResponse;
import com.infy.billing.entity.AuditLog;
import com.infy.billing.entity.BillingJob;
import com.infy.billing.entity.DunningRetryLog;

import com.infy.billing.dto.customer.CancellationRequestDTO;
import com.infy.billing.dto.customer.CancellationResponse;
import com.infy.billing.request.ProcessCancellationRequestInput;

import java.util.List;

public interface SupportService {
    List<CustomerSearchResponse> searchCustomers(String query);

    CustomerDetailResponse getCustomerDetails(Long customerId);

    List<AuditLog> getRecentAuditLogs();

    List<BillingJob> getBillingJobs();

    List<DunningRetryLog> getDunningLogs();

    List<SubscriptionDTO> getPastDueSubscriptions();

    List<CancellationRequestDTO> getPendingCancellationRequests();

    CancellationResponse approveCancellationRequest(Long requestId, String agentEmail, ProcessCancellationRequestInput input);

    void rejectCancellationRequest(Long requestId, String agentEmail, ProcessCancellationRequestInput input);
}
