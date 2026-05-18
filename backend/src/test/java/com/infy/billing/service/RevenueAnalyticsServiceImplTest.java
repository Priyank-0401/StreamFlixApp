package com.infy.billing.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.infy.billing.dto.finance.FinanceDashboardDTO;
import com.infy.billing.dto.finance.MrrReportDTO;
import com.infy.billing.dto.finance.ArrReportDTO;
import com.infy.billing.entity.Subscription;
import com.infy.billing.entity.Customer;
import com.infy.billing.entity.Plan;
import com.infy.billing.entity.RevenueSnapshot;
import com.infy.billing.enums.Status;
import com.infy.billing.enums.BillingPeriod;
import com.infy.billing.repository.SubscriptionRepository;
import com.infy.billing.repository.InvoiceRepository;
import com.infy.billing.repository.PaymentRepository;
import com.infy.billing.repository.CreditNoteRepository;
import com.infy.billing.repository.RevenueSnapshotRepository;
import com.infy.billing.repository.PriceBookEntryRepository;

@ExtendWith(MockitoExtension.class)
public class RevenueAnalyticsServiceImplTest {

    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private InvoiceRepository invoiceRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private CreditNoteRepository creditNoteRepository;
    @Mock private RevenueSnapshotRepository snapshotRepository;
    @Mock private PriceBookEntryRepository priceBookEntryRepository;

    @InjectMocks
    private RevenueAnalyticsServiceImpl revenueAnalyticsService;

    private Subscription subscription;
    private Customer customer;
    private Plan plan;
    private RevenueSnapshot snapshot;

    @BeforeEach
    void setUp() {
        customer = Customer.builder().id(1L).country("IN").build();
        plan = Plan.builder().id(1L).name("Basic").defaultPriceMinor(1000L).billingPeriod(BillingPeriod.MONTHLY).build();
        subscription = Subscription.builder()
                .id(1L)
                .customer(customer)
                .plan(plan)
                .status(Status.ACTIVE)
                .currency("INR")
                .build();
        snapshot = RevenueSnapshot.builder()
                .id(1L)
                .snapshotDate(LocalDate.now())
                .mrrMinor(10000L)
                .arrMinor(120000L)
                .netChurnPercent(new java.math.BigDecimal("2.5"))
                .build();
    }

    @Test
    void testGetFinanceDashboard() {
        when(subscriptionRepository.findByStatus(Status.ACTIVE)).thenReturn(Arrays.asList(subscription));
        when(subscriptionRepository.findByStatus(Status.CANCELED)).thenReturn(Arrays.asList());
        when(snapshotRepository.findAllByOrderBySnapshotDateAsc()).thenReturn(Arrays.asList(snapshot));
        when(paymentRepository.findByStatus(Status.FAILED)).thenReturn(Arrays.asList());
        when(creditNoteRepository.findAll()).thenReturn(Arrays.asList());

        FinanceDashboardDTO dto = revenueAnalyticsService.getFinanceDashboard();

        assertNotNull(dto);
        assertEquals(1000L, dto.getMrrMinor()); // 1000 INR
        assertEquals(12000L, dto.getArrMinor());
    }

    @Test
    void testGetMrrReport() {
        when(snapshotRepository.findAllByOrderBySnapshotDateAsc()).thenReturn(Arrays.asList(snapshot));

        MrrReportDTO dto = revenueAnalyticsService.getMrrReport();

        assertNotNull(dto);
        assertNotNull(dto.getRevenueTrend());
    }

    @Test
    void testGetArrReport() {
        when(snapshotRepository.findAllByOrderBySnapshotDateAsc()).thenReturn(Arrays.asList(snapshot));

        ArrReportDTO dto = revenueAnalyticsService.getArrReport();

        assertNotNull(dto);
        assertNotNull(dto.getArrTrend());
    }

    @Test
    void testGetFinanceDashboard_SnapshotsEmpty() {
        when(subscriptionRepository.findByStatus(Status.ACTIVE)).thenReturn(Arrays.asList(subscription));
        when(subscriptionRepository.findByStatus(Status.CANCELED)).thenReturn(Arrays.asList());
        when(snapshotRepository.findAllByOrderBySnapshotDateAsc()).thenReturn(Arrays.asList()); // Empty!
        when(paymentRepository.findByStatus(Status.FAILED)).thenReturn(Arrays.asList());
        when(creditNoteRepository.findAll()).thenReturn(Arrays.asList());

        FinanceDashboardDTO dto = revenueAnalyticsService.getFinanceDashboard();

        assertNotNull(dto);
        assertEquals(0.0, dto.getNetChurnPercent().doubleValue());
    }

    @Test
    void testGetFinanceDashboard_UsdCurrency() {
        subscription.setCurrency("USD");
        when(subscriptionRepository.findByStatus(Status.ACTIVE)).thenReturn(Arrays.asList(subscription));
        when(subscriptionRepository.findByStatus(Status.CANCELED)).thenReturn(Arrays.asList());
        when(snapshotRepository.findAllByOrderBySnapshotDateAsc()).thenReturn(Arrays.asList(snapshot));
        when(paymentRepository.findByStatus(Status.FAILED)).thenReturn(Arrays.asList());
        when(creditNoteRepository.findAll()).thenReturn(Arrays.asList());

        FinanceDashboardDTO dto = revenueAnalyticsService.getFinanceDashboard();

        assertNotNull(dto);
        assertEquals(1000L * 95, dto.getMrrMinor());
    }

    @Test
    void testGetFinanceDashboard_GbpCurrency() {
        subscription.setCurrency("GBP");
        when(subscriptionRepository.findByStatus(Status.ACTIVE)).thenReturn(Arrays.asList(subscription));
        when(subscriptionRepository.findByStatus(Status.CANCELED)).thenReturn(Arrays.asList());
        when(snapshotRepository.findAllByOrderBySnapshotDateAsc()).thenReturn(Arrays.asList(snapshot));
        when(paymentRepository.findByStatus(Status.FAILED)).thenReturn(Arrays.asList());
        when(creditNoteRepository.findAll()).thenReturn(Arrays.asList());

        FinanceDashboardDTO dto = revenueAnalyticsService.getFinanceDashboard();

        assertNotNull(dto);
        assertEquals(1000L * 129, dto.getMrrMinor());
    }

    @Test
    void testGetFinanceDashboard_WithCanceledSubs() {
        com.infy.billing.entity.Subscription canceledSub = com.infy.billing.entity.Subscription.builder()
                .id(2L)
                .customer(customer)
                .plan(plan)
                .status(Status.CANCELED)
                .currency("INR")
                .build();

        com.infy.billing.entity.Payment successPayment = new com.infy.billing.entity.Payment();
        successPayment.setStatus(Status.SUCCESS);
        successPayment.setAmountMinor(5000L);
        successPayment.setCurrency("INR");
        
        com.infy.billing.entity.Invoice paidInvoice = com.infy.billing.entity.Invoice.builder()
                .status(Status.PAID)
                .customer(customer)
                .build();
        
        successPayment.setInvoice(paidInvoice);

        when(subscriptionRepository.findByStatus(Status.ACTIVE)).thenReturn(Arrays.asList(subscription));
        when(subscriptionRepository.findByStatus(Status.CANCELED)).thenReturn(Arrays.asList(canceledSub));
        when(snapshotRepository.findAllByOrderBySnapshotDateAsc()).thenReturn(Arrays.asList(snapshot));
        when(paymentRepository.findByStatus(Status.FAILED)).thenReturn(Arrays.asList());
        when(paymentRepository.findByStatus(Status.SUCCESS)).thenReturn(Arrays.asList(successPayment));
        when(creditNoteRepository.findAll()).thenReturn(Arrays.asList());

        FinanceDashboardDTO dto = revenueAnalyticsService.getFinanceDashboard();

        assertNotNull(dto);
        assertEquals(5000L, dto.getLtvMinor());
    }
}
