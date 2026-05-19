package com.infy.billing.service;

import com.infy.billing.dto.customer.CustomerProfileDTO;
import com.infy.billing.dto.customer.SubscriptionDTO;
import com.infy.billing.dto.customer.InvoiceDTO;
import com.infy.billing.dto.customer.UsageRecordDTO;
import com.infy.billing.dto.customer.CreditNoteDTO;
import com.infy.billing.dto.support.CustomerSearchResponse;
import com.infy.billing.dto.support.CustomerDetailResponse;
import com.infy.billing.repository.CreditNoteRepository;
import com.infy.billing.entity.Customer;
import com.infy.billing.entity.Subscription;
import com.infy.billing.entity.Invoice;
import com.infy.billing.entity.UsageRecord;
import com.infy.billing.entity.User;
import com.infy.billing.entity.AuditLog;
import com.infy.billing.entity.BillingJob;
import com.infy.billing.entity.DunningRetryLog;
import com.infy.billing.enums.Status;
import com.infy.billing.repository.CustomerRepository;
import com.infy.billing.repository.SubscriptionRepository;
import com.infy.billing.repository.InvoiceRepository;
import com.infy.billing.repository.UsageRecordRepository;
import com.infy.billing.repository.AuditLogRepository;
import com.infy.billing.repository.BillingJobRepository;
import com.infy.billing.repository.DunningRetryLogRepository;
import com.infy.billing.repository.CancellationRequestRepository;
import com.infy.billing.repository.UserRepository;
import com.infy.billing.entity.Notification;
import com.infy.billing.repository.NotificationRepository;
import com.infy.billing.dto.customer.CancellationRequestDTO;
import com.infy.billing.dto.customer.CancellationResponse;
import com.infy.billing.request.ProcessCancellationRequestInput;
import com.infy.billing.enums.CancellationRequestStatus;
import com.infy.billing.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupportServiceImpl implements SupportService {
 
     private final CustomerRepository customerRepository;
     private final SubscriptionRepository subscriptionRepository;
     private final InvoiceRepository invoiceRepository;
     private final UsageRecordRepository usageRecordRepository;
     private final AuditLogRepository auditLogRepository;
     private final BillingJobRepository billingJobRepository;
     private final DunningRetryLogRepository dunningRetryLogRepository;
     private final CreditNoteRepository creditNoteRepository;
     private final CancellationRequestRepository cancellationRequestRepository;
     private final UserRepository userRepository;
     private final CustomerSubscriptionService customerSubscriptionService;
     private final NotificationRepository notificationRepository;
 
     @Override
     public List<CustomerSearchResponse> searchCustomers(String query) {
         List<Customer> customers = customerRepository
                 .findByUser_FullNameContainingIgnoreCaseOrUser_EmailContainingIgnoreCase(query, query);
         return customers.stream()
                 .map(this::mapToSearchResponse)
                 .collect(Collectors.toList());
     }
 
     @Override
     public CustomerDetailResponse getCustomerDetails(Long customerId) {
         Customer customer = customerRepository.findById(customerId)
                 .orElseThrow(() -> CustomException.notFound("Customer not found"));
 
         CustomerProfileDTO profileDTO = mapToProfileDTO(customer);
 
         List<Subscription> subscriptions = subscriptionRepository.findByCustomer_Id(customerId);
         List<SubscriptionDTO> subscriptionDTOs = subscriptions.stream()
                 .map(this::mapToSubscriptionDTO)
                 .collect(Collectors.toList());
 
         List<Invoice> invoices = invoiceRepository.findByCustomer_IdOrderByIssueDateDesc(customerId);
         List<InvoiceDTO> invoiceDTOs = invoices.stream()
                 .map(this::mapToInvoiceDTO)
                 .collect(Collectors.toList());
 
         List<UsageRecord> usageRecords = usageRecordRepository.findBySubscription_Customer_Id(customerId);
         List<UsageRecordDTO> usageRecordDTOs = usageRecords.stream()
                 .map(this::mapToUsageRecordDTO)
                 .collect(Collectors.toList());
 
         List<com.infy.billing.entity.CreditNote> creditNotes = creditNoteRepository.findByInvoice_Customer_Id(customerId);
         List<CreditNoteDTO> creditNoteDTOs = creditNotes.stream()
                 .map(this::mapToCreditNoteDTO)
                 .collect(Collectors.toList());
 
         return new CustomerDetailResponse(profileDTO, subscriptionDTOs, invoiceDTOs, usageRecordDTOs, creditNoteDTOs);
     }

    private CustomerSearchResponse mapToSearchResponse(Customer customer) {
        User user = customer.getUser();
        return new CustomerSearchResponse(
                customer.getId(),
                user.getFullName(),
                user.getEmail(),
                customer.getStatus());
    }

    private CustomerProfileDTO mapToProfileDTO(Customer customer) {
        User user = customer.getUser();
        CustomerProfileDTO dto = new CustomerProfileDTO();
        dto.setCustomerId(customer.getId());
        dto.setUserId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setCurrency(customer.getCurrency());
        dto.setCountry(customer.getCountry());
        dto.setState(customer.getState());
        dto.setCity(customer.getCity());
        dto.setAddressLine1(customer.getAddressLine1());
        dto.setPostalCode(customer.getPostalCode());
        dto.setStatus(customer.getStatus());
        dto.setCreatedAt(customer.getCreatedAt());
        return dto;
    }

    private SubscriptionDTO mapToSubscriptionDTO(Subscription sub) {
        SubscriptionDTO dto = new SubscriptionDTO();
        dto.setSubscriptionId(sub.getId());
        dto.setCustomerId(sub.getCustomer().getId());
        dto.setPlanId(sub.getPlan().getId());
        dto.setPlanName(sub.getPlan().getName());
        dto.setStatus(sub.getStatus());
        dto.setStartDate(sub.getStartDate() != null ? sub.getStartDate().toString() : null);
        dto.setCurrentPeriodEnd(sub.getCurrentPeriodEnd() != null ? sub.getCurrentPeriodEnd().toString() : null);
        dto.setCurrency(sub.getCurrency());
        return dto;
    }

    private InvoiceDTO mapToInvoiceDTO(Invoice inv) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setInvoiceId(inv.getId());
        dto.setInvoiceNumber(inv.getInvoiceNumber());
        dto.setSubscriptionId(inv.getSubscription().getId());
        dto.setStatus(inv.getStatus());
        dto.setBillingReason(inv.getBillingReason());
        dto.setIssueDate(inv.getIssueDate() != null ? inv.getIssueDate().toString() : null);
        dto.setDueDate(inv.getDueDate() != null ? inv.getDueDate().toString() : null);
        dto.setTotalMinor(inv.getTotalMinor());
        dto.setCurrency(inv.getCurrency());
        return dto;
    }

    private UsageRecordDTO mapToUsageRecordDTO(UsageRecord ur) {
        UsageRecordDTO dto = new UsageRecordDTO();
        dto.setUsageId(ur.getId());
        dto.setComponentId(ur.getComponent().getId());
        dto.setComponentName(ur.getComponent().getName());
        dto.setQuantity(ur.getQuantity());
        dto.setUnitName(ur.getComponent().getUnitName());
        dto.setRecordedAt(ur.getRecordedAt() != null ? ur.getRecordedAt().toString() : null);
        return dto;
    }

    

    private CreditNoteDTO mapToCreditNoteDTO(com.infy.billing.entity.CreditNote creditNote) {
        CreditNoteDTO dto = new CreditNoteDTO();
        dto.setCreditNoteId(creditNote.getId());
        dto.setCreditNoteNumber(creditNote.getCreditNoteNumber());
        dto.setInvoiceId(creditNote.getInvoice().getId());
        Invoice inv = invoiceRepository.findById(creditNote.getInvoice().getId()).orElse(null);
        dto.setInvoiceNumber(inv != null ? inv.getInvoiceNumber() : "N/A");
        dto.setReason(creditNote.getReason());
        dto.setAmountMinor(creditNote.getAmountMinor());
        dto.setStatus(creditNote.getStatus());
        dto.setCreatedAt(creditNote.getCreatedAt().toString());
        return dto;
    }

    @Override
    public List<AuditLog> getRecentAuditLogs() {
        return auditLogRepository.findTop100ByOrderByCreatedAtDesc();
    }

    @Override
    public List<BillingJob> getBillingJobs() {
        return billingJobRepository.findAll();
    }

    @Override
    public List<DunningRetryLog> getDunningLogs() {
        return dunningRetryLogRepository.findAll();
    }

    @Override
    public List<com.infy.billing.dto.customer.SubscriptionDTO> getPastDueSubscriptions() {
        return subscriptionRepository.findByStatusIn(List.of(Status.PAST_DUE, Status.ON_HOLD))
                .stream()
                .map(this::mapToSubscriptionDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CancellationRequestDTO> getPendingCancellationRequests() {
        return cancellationRequestRepository.findByStatus(CancellationRequestStatus.PENDING)
                .stream()
                .map(this::mapToCancellationRequestDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CancellationResponse approveCancellationRequest(Long requestId, String agentEmail, ProcessCancellationRequestInput input) {
        com.infy.billing.entity.CancellationRequest request = cancellationRequestRepository.findById(requestId)
                .orElseThrow(() -> CustomException.notFound("Cancellation request not found"));

        if (request.getStatus() != CancellationRequestStatus.PENDING) {
            throw CustomException.badRequest("Cancellation request is not pending");
        }

        User agent = userRepository.findByEmail(agentEmail)
                .orElseThrow(() -> CustomException.notFound("Agent not found"));

        request.setStatus(CancellationRequestStatus.APPROVED);
        request.setProcessedBy(agent);
        request.setProcessedAt(LocalDateTime.now());
        request.setAgentNotes(input.getAgentNotes());
        cancellationRequestRepository.save(request);

        // Perform actual cancellation
        String customerEmail = request.getSubscription().getCustomer().getUser().getEmail();
        CancellationResponse response = customerSubscriptionService.cancelSubscription(customerEmail, request.getAtPeriodEnd());

        // Create notification
        String subject = "Subscription Cancellation Approved";
        String body;
        if (request.getAtPeriodEnd()) {
            body = "Your subscription cancellation request has been approved. Your subscription will remain active until the end of the current billing period on " + request.getSubscription().getCurrentPeriodEnd() + ".";
        } else {
            body = "Your subscription has been canceled immediately.";
            if (response.isRefundIssued()) {
                double refundAmt = response.getRefundAmountMinor() / 100.0;
                String symbol;
                try {
                    symbol = java.util.Currency.getInstance(response.getCurrency()).getSymbol();
                } catch (Exception e) {
                    symbol = response.getCurrency();
                }
                body += String.format(" A refund of %s%.2f has been processed to your payment method.", symbol, refundAmt);
            } else {
                body += " No refund was applicable.";
            }
        }

        Notification notification = Notification.builder()
                .customer(request.getSubscription().getCustomer())
                .type("CANCELLATION_APPROVED")
                .subject(subject)
                .body(body)
                .channel(com.infy.billing.enums.Channel.EMAIL)
                .status(com.infy.billing.enums.NotificationStatus.SENT)
                .scheduledAt(LocalDateTime.now())
                .sentAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);

        return response;
    }

    @Override
    public void rejectCancellationRequest(Long requestId, String agentEmail, ProcessCancellationRequestInput input) {
        com.infy.billing.entity.CancellationRequest request = cancellationRequestRepository.findById(requestId)
                .orElseThrow(() -> CustomException.notFound("Cancellation request not found"));

        if (request.getStatus() != CancellationRequestStatus.PENDING) {
            throw CustomException.badRequest("Cancellation request is not pending");
        }

        User agent = userRepository.findByEmail(agentEmail)
                .orElseThrow(() -> CustomException.notFound("Agent not found"));

        request.setStatus(CancellationRequestStatus.REJECTED);
        request.setProcessedBy(agent);
        request.setProcessedAt(LocalDateTime.now());
        request.setAgentNotes(input.getAgentNotes());
        cancellationRequestRepository.save(request);

        // Create notification
        String subject = "Cancellation Request Declined";
        String body = "Your subscription cancellation request has been declined by support. " + 
                      (input.getAgentNotes() != null && !input.getAgentNotes().isBlank() 
                       ? "Agent notes: " + input.getAgentNotes() 
                       : "Please contact support for more details.");

        Notification notification = Notification.builder()
                .customer(request.getSubscription().getCustomer())
                .type("CANCELLATION_REJECTED")
                .subject(subject)
                .body(body)
                .channel(com.infy.billing.enums.Channel.EMAIL)
                .status(com.infy.billing.enums.NotificationStatus.SENT)
                .scheduledAt(LocalDateTime.now())
                .sentAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    private CancellationRequestDTO mapToCancellationRequestDTO(com.infy.billing.entity.CancellationRequest request) {
        CancellationRequestDTO dto = new CancellationRequestDTO();
        dto.setRequestId(request.getId());
        dto.setSubscriptionId(request.getSubscription().getId());
        dto.setPlanName(request.getSubscription().getPlan().getName());
        dto.setCustomerEmail(request.getSubscription().getCustomer().getUser().getEmail());
        dto.setCustomerName(request.getSubscription().getCustomer().getUser().getFullName());
        dto.setReason(request.getReason());
        dto.setStatus(request.getStatus());
        dto.setAtPeriodEnd(request.getAtPeriodEnd());
        dto.setCreatedAt(request.getCreatedAt() != null ? request.getCreatedAt().toString() : null);
        dto.setProcessedByEmail(request.getProcessedBy() != null ? request.getProcessedBy().getEmail() : null);
        dto.setProcessedAt(request.getProcessedAt() != null ? request.getProcessedAt().toString() : null);
        dto.setAgentNotes(request.getAgentNotes());
        return dto;
    }
}
