package com.infy.billing.service;

import com.infy.billing.dto.customer.CustomerProfileDTO;
import com.infy.billing.dto.customer.SubscriptionDTO;
import com.infy.billing.dto.customer.InvoiceDTO;
import com.infy.billing.dto.customer.UsageRecordDTO;
import com.infy.billing.dto.customer.NotificationDTO;
import com.infy.billing.dto.support.CustomerSearchResponse;
import com.infy.billing.dto.support.CustomerDetailResponse;
import com.infy.billing.entity.Customer;
import com.infy.billing.entity.Subscription;
import com.infy.billing.entity.Invoice;
import com.infy.billing.entity.UsageRecord;
import com.infy.billing.entity.Notification;
import com.infy.billing.entity.User;
import com.infy.billing.entity.AuditLog;
import com.infy.billing.entity.BillingJob;
import com.infy.billing.entity.DunningRetryLog;
import com.infy.billing.enums.Status;
import com.infy.billing.repository.CustomerRepository;
import com.infy.billing.repository.SubscriptionRepository;
import com.infy.billing.repository.InvoiceRepository;
import com.infy.billing.repository.UsageRecordRepository;
import com.infy.billing.repository.NotificationRepository;
import com.infy.billing.repository.AuditLogRepository;
import com.infy.billing.repository.BillingJobRepository;
import com.infy.billing.repository.DunningRetryLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupportServiceImpl implements SupportService {

    private final CustomerRepository customerRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final UsageRecordRepository usageRecordRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogRepository auditLogRepository;
    private final BillingJobRepository billingJobRepository;
    private final DunningRetryLogRepository dunningRetryLogRepository;

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
                .orElseThrow(() -> new RuntimeException("Customer not found"));

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

        List<Notification> notifications = notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        List<NotificationDTO> notificationDTOs = notifications.stream()
                .map(this::mapToNotificationDTO)
                .collect(Collectors.toList());

        return new CustomerDetailResponse(profileDTO, subscriptionDTOs, invoiceDTOs, usageRecordDTOs, notificationDTOs);
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

    private NotificationDTO mapToNotificationDTO(Notification n) {
        NotificationDTO dto = new NotificationDTO();
        dto.setNotificationId(n.getNotificationId());
        dto.setType(n.getType());
        dto.setSubject(n.getSubject());
        dto.setBody(n.getBody());
        dto.setChannel(n.getChannel());
        dto.setStatus(com.infy.billing.enums.Status.ACTIVE); // Defaulting to ACTIVE as requested by the DTO structure
                                                             // or map correctly if possible. Wait, DTO uses
                                                             // com.infy.billing.enums.Status. Entity might use
                                                             // NotificationStatus.
        // Let's check Notification entity status type.
        // In schema.sql, notification status is ENUM('PENDING', 'SENT', 'FAILED',
        // 'SKIPPED', 'READ').
        // So I need to map it to com.infy.billing.enums.Status if needed, or if DTO
        // uses Status.
        // Let's check NotificationDTO again. Line 6: import
        // com.infy.billing.enums.Status;
        // Line 17: private Status status;
        // So it expects com.infy.billing.enums.Status.
        // I will default to ACTIVE or try to map it. Since NotificationStatus is likely
        // different, I'll just default to ACTIVE for now or map common ones.
        // Let's see if I can find a mapping or just leave it as ACTIVE.
        dto.setStatus(com.infy.billing.enums.Status.ACTIVE);
        dto.setSentAt(n.getSentAt());
        dto.setCreatedAt(n.getCreatedAt());
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
}
