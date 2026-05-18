package com.infy.billing.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.infy.billing.dto.support.CustomerSearchResponse;
import com.infy.billing.dto.support.CustomerDetailResponse;
import com.infy.billing.dto.customer.SubscriptionDTO;
import com.infy.billing.entity.Customer;
import com.infy.billing.entity.User;
import com.infy.billing.entity.AuditLog;
import com.infy.billing.entity.BillingJob;
import com.infy.billing.entity.DunningRetryLog;
import com.infy.billing.entity.Subscription;
import com.infy.billing.entity.Plan;
import com.infy.billing.entity.Invoice;
import com.infy.billing.entity.UsageRecord;
import com.infy.billing.entity.MeteredComponent;
import com.infy.billing.enums.Status;
import com.infy.billing.repository.CustomerRepository;
import com.infy.billing.repository.SubscriptionRepository;
import com.infy.billing.repository.InvoiceRepository;
import com.infy.billing.repository.UsageRecordRepository;
import com.infy.billing.repository.CreditNoteRepository;
import com.infy.billing.repository.AuditLogRepository;
import com.infy.billing.repository.BillingJobRepository;
import com.infy.billing.repository.DunningRetryLogRepository;

@ExtendWith(MockitoExtension.class)
public class SupportServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private UsageRecordRepository usageRecordRepository;
    @Mock
    private CreditNoteRepository creditNoteRepository;
    @Mock
    private AuditLogRepository auditLogRepository;
    @Mock
    private BillingJobRepository billingJobRepository;
    @Mock
    private DunningRetryLogRepository dunningRetryLogRepository;

    @InjectMocks
    private SupportServiceImpl supportService;

    private Customer customer;
    private User user;
    private Plan plan;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).fullName("John Doe").email("john@test.com").build();
        customer = Customer.builder().id(1L).user(user).status(Status.ACTIVE).build();
        plan = Plan.builder().id(1L).name("Premium").build();
        subscription = Subscription.builder()
                .id(1L)
                .customer(customer)
                .plan(plan)
                .status(Status.ACTIVE)
                .build();
    }

    @Test
    void testSearchCustomers() {
        when(customerRepository.findByUser_FullNameContainingIgnoreCaseOrUser_EmailContainingIgnoreCase("John", "John"))
                .thenReturn(Arrays.asList(customer));

        List<CustomerSearchResponse> responses = supportService.searchCustomers("John");

        assertEquals(1, responses.size());
        assertEquals("John Doe", responses.get(0).getFullName());
    }

    @Test
    void testGetCustomerDetails() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(subscriptionRepository.findByCustomer_Id(1L)).thenReturn(Arrays.asList(subscription));
        
        Invoice invoice = Invoice.builder()
                .id(1L)
                .invoiceNumber("INV-001")
                .subscription(subscription)
                .status(Status.ACTIVE)
                .billingReason(com.infy.billing.enums.BillingReason.SUBSCRIPTION_CYCLE)
                .build();
        
        when(invoiceRepository.findByCustomer_IdOrderByIssueDateDesc(1L)).thenReturn(Arrays.asList(invoice));

        MeteredComponent component = MeteredComponent.builder().id(1L).name("Storage").unitName("GB").build();
        UsageRecord usageRecord = new UsageRecord();
        usageRecord.setId(1L);
        usageRecord.setComponent(component);
        usageRecord.setQuantity(10L);
        
        when(usageRecordRepository.findBySubscription_Customer_Id(1L)).thenReturn(Arrays.asList(usageRecord));

        com.infy.billing.entity.CreditNote creditNote = new com.infy.billing.entity.CreditNote();
        creditNote.setId(1L);
        creditNote.setCreditNoteNumber("CN-001");
        creditNote.setInvoice(invoice);
        creditNote.setReason("Test refund");
        creditNote.setAmountMinor(1000L);
        creditNote.setStatus(Status.ACTIVE);
        creditNote.setCreatedAt(java.time.LocalDateTime.now());

        when(creditNoteRepository.findByInvoice_Customer_Id(1L)).thenReturn(Arrays.asList(creditNote));
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        CustomerDetailResponse response = supportService.getCustomerDetails(1L);

        assertNotNull(response);
        assertEquals("John Doe", response.getCustomerProfile().getFullName());
        assertEquals(1, response.getSubscriptions().size());
        assertEquals("Premium", response.getSubscriptions().get(0).getPlanName());
        assertEquals(1, response.getInvoices().size());
        assertEquals(1, response.getUsageRecords().size());
        assertEquals(1, response.getCreditNotes().size());
        assertEquals("CN-001", response.getCreditNotes().get(0).getCreditNoteNumber());
    }

    @Test
    void testGetCustomerDetails_NotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> supportService.getCustomerDetails(1L));
    }

    @Test
    void testGetRecentAuditLogs() {
        AuditLog log = new AuditLog();
        when(auditLogRepository.findTop100ByOrderByCreatedAtDesc()).thenReturn(Arrays.asList(log));

        List<AuditLog> logs = supportService.getRecentAuditLogs();

        assertEquals(1, logs.size());
    }

    @Test
    void testGetBillingJobs() {
        BillingJob job = new BillingJob();
        when(billingJobRepository.findAll()).thenReturn(Arrays.asList(job));

        List<BillingJob> jobs = supportService.getBillingJobs();

        assertEquals(1, jobs.size());
    }

    @Test
    void testGetDunningLogs() {
        DunningRetryLog log = new DunningRetryLog();
        when(dunningRetryLogRepository.findAll()).thenReturn(Arrays.asList(log));

        List<DunningRetryLog> logs = supportService.getDunningLogs();

        assertEquals(1, logs.size());
    }

    @Test
    void testGetPastDueSubscriptions() {
        subscription.setStatus(Status.PAST_DUE);
        when(subscriptionRepository.findByStatusIn(List.of(Status.PAST_DUE, Status.ON_HOLD)))
                .thenReturn(Arrays.asList(subscription));

        List<SubscriptionDTO> dtos = supportService.getPastDueSubscriptions();

        assertEquals(1, dtos.size());
        assertEquals("Premium", dtos.get(0).getPlanName());
    }
}
