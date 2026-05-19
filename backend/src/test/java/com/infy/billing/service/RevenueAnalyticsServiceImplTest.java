package com.infy.billing.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.infy.billing.dto.finance.*;
import com.infy.billing.entity.*;
import com.infy.billing.enums.Status;
import com.infy.billing.enums.BillingPeriod;
import com.infy.billing.enums.PaymentType;
import com.infy.billing.repository.*;

@ExtendWith(MockitoExtension.class)
class RevenueAnalyticsServiceImplTest {

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
    private RevenueSnapshot snapshot2;

    @BeforeEach
    void setUp() {
        customer = Customer.builder().id(1L).country("IN").build();
        plan = Plan.builder().id(1L).name("Basic").defaultPriceMinor(1000L).billingPeriod(BillingPeriod.MONTHLY).build();
        subscription = Subscription.builder()
                .id(1L).customer(customer).plan(plan).status(Status.ACTIVE).currency("INR").build();
        snapshot = RevenueSnapshot.builder()
                .id(1L).snapshotDate(LocalDate.of(2026, 4, 30)).mrrMinor(10000L).arrMinor(120000L)
                .arpuMinor(5000L).netChurnPercent(new BigDecimal("2.5"))
                .activeCustomers(2).newCustomers(1).totalRevenueMinor(50000L).build();
        snapshot2 = RevenueSnapshot.builder()
                .id(2L).snapshotDate(LocalDate.of(2026, 5, 31)).mrrMinor(9000L).arrMinor(108000L)
                .arpuMinor(4500L).netChurnPercent(new BigDecimal("3.0"))
                .activeCustomers(2).newCustomers(0).totalRevenueMinor(45000L).build();
    }

    // ==================== FINANCE DASHBOARD ====================
    @Test
    void testGetFinanceDashboard() {
        when(subscriptionRepository.findByStatus(Status.ACTIVE)).thenReturn(Arrays.asList(subscription));
        when(subscriptionRepository.findByStatus(Status.CANCELED)).thenReturn(Collections.emptyList());
        when(snapshotRepository.findAllByOrderBySnapshotDateAsc()).thenReturn(Arrays.asList(snapshot));
        when(paymentRepository.findByStatus(Status.FAILED)).thenReturn(Collections.emptyList());
        when(creditNoteRepository.findAll()).thenReturn(Collections.emptyList());

        FinanceDashboardDTO dto = revenueAnalyticsService.getFinanceDashboard();
        assertNotNull(dto);
        assertEquals(1000L, dto.getMrrMinor());
        assertEquals(12000L, dto.getArrMinor());
    }

    @Test
    void testGetFinanceDashboard_SnapshotsEmpty() {
        when(subscriptionRepository.findByStatus(Status.ACTIVE)).thenReturn(Arrays.asList(subscription));
        when(subscriptionRepository.findByStatus(Status.CANCELED)).thenReturn(Collections.emptyList());
        when(snapshotRepository.findAllByOrderBySnapshotDateAsc()).thenReturn(Collections.emptyList());
        when(paymentRepository.findByStatus(Status.FAILED)).thenReturn(Collections.emptyList());
        when(creditNoteRepository.findAll()).thenReturn(Collections.emptyList());

        FinanceDashboardDTO dto = revenueAnalyticsService.getFinanceDashboard();
        assertNotNull(dto);
        assertEquals(0.0, dto.getNetChurnPercent().doubleValue());
    }

    @Test
    void testGetFinanceDashboard_UsdCurrency() {
        subscription.setCurrency("USD");
        when(subscriptionRepository.findByStatus(Status.ACTIVE)).thenReturn(Arrays.asList(subscription));
        when(subscriptionRepository.findByStatus(Status.CANCELED)).thenReturn(Collections.emptyList());
        when(snapshotRepository.findAllByOrderBySnapshotDateAsc()).thenReturn(Arrays.asList(snapshot));
        when(paymentRepository.findByStatus(Status.FAILED)).thenReturn(Collections.emptyList());
        when(creditNoteRepository.findAll()).thenReturn(Collections.emptyList());

        FinanceDashboardDTO dto = revenueAnalyticsService.getFinanceDashboard();
        assertEquals(1000L * 95, dto.getMrrMinor());
    }

    @Test
    void testGetFinanceDashboard_GbpCurrency() {
        subscription.setCurrency("GBP");
        when(subscriptionRepository.findByStatus(Status.ACTIVE)).thenReturn(Arrays.asList(subscription));
        when(subscriptionRepository.findByStatus(Status.CANCELED)).thenReturn(Collections.emptyList());
        when(snapshotRepository.findAllByOrderBySnapshotDateAsc()).thenReturn(Arrays.asList(snapshot));
        when(paymentRepository.findByStatus(Status.FAILED)).thenReturn(Collections.emptyList());
        when(creditNoteRepository.findAll()).thenReturn(Collections.emptyList());

        FinanceDashboardDTO dto = revenueAnalyticsService.getFinanceDashboard();
        assertEquals(1000L * 129, dto.getMrrMinor());
    }

    @Test
    void testGetFinanceDashboard_AnnualPlan() {
        Plan yearlyPlan = Plan.builder().id(2L).name("Annual").defaultPriceMinor(12000L)
                .billingPeriod(BillingPeriod.YEARLY).build();
        Subscription yearlySub = Subscription.builder().id(2L).customer(customer)
                .plan(yearlyPlan).status(Status.ACTIVE).currency("INR").build();

        when(subscriptionRepository.findByStatus(Status.ACTIVE)).thenReturn(Arrays.asList(yearlySub));
        when(subscriptionRepository.findByStatus(Status.CANCELED)).thenReturn(Collections.emptyList());
        when(snapshotRepository.findAllByOrderBySnapshotDateAsc()).thenReturn(Arrays.asList(snapshot));
        when(paymentRepository.findByStatus(Status.FAILED)).thenReturn(Collections.emptyList());
        when(creditNoteRepository.findAll()).thenReturn(Collections.emptyList());

        FinanceDashboardDTO dto = revenueAnalyticsService.getFinanceDashboard();
        assertEquals(1000L, dto.getMrrMinor()); // 12000/12 = 1000
    }

    @Test
    void testGetFinanceDashboard_WithCanceledSubs() {
        Subscription canceledSub = Subscription.builder().id(2L).customer(customer)
                .plan(plan).status(Status.CANCELED).currency("INR").build();
        Payment successPayment = new Payment();
        successPayment.setStatus(Status.SUCCESS);
        successPayment.setAmountMinor(5000L);
        successPayment.setCurrency("INR");
        Invoice paidInvoice = Invoice.builder().status(Status.PAID).customer(customer).build();
        successPayment.setInvoice(paidInvoice);

        when(subscriptionRepository.findByStatus(Status.ACTIVE)).thenReturn(Arrays.asList(subscription));
        when(subscriptionRepository.findByStatus(Status.CANCELED)).thenReturn(Arrays.asList(canceledSub));
        when(snapshotRepository.findAllByOrderBySnapshotDateAsc()).thenReturn(Arrays.asList(snapshot));
        when(paymentRepository.findByStatus(Status.FAILED)).thenReturn(Collections.emptyList());
        when(paymentRepository.findByStatus(Status.SUCCESS)).thenReturn(Arrays.asList(successPayment));
        when(creditNoteRepository.findAll()).thenReturn(Collections.emptyList());

        FinanceDashboardDTO dto = revenueAnalyticsService.getFinanceDashboard();
        assertEquals(5000L, dto.getLtvMinor());
    }

    @Test
    void testGetFinanceDashboard_LtvFallback_ZeroChurn() {
        RevenueSnapshot zeroChurnSnap = RevenueSnapshot.builder().id(1L)
                .snapshotDate(LocalDate.now()).mrrMinor(10000L).arrMinor(120000L)
                .netChurnPercent(BigDecimal.ZERO).build();

        when(subscriptionRepository.findByStatus(Status.ACTIVE)).thenReturn(Arrays.asList(subscription));
        when(subscriptionRepository.findByStatus(Status.CANCELED)).thenReturn(Collections.emptyList());
        when(snapshotRepository.findAllByOrderBySnapshotDateAsc()).thenReturn(Arrays.asList(zeroChurnSnap));
        when(paymentRepository.findByStatus(Status.FAILED)).thenReturn(Collections.emptyList());
        when(creditNoteRepository.findAll()).thenReturn(Collections.emptyList());

        FinanceDashboardDTO dto = revenueAnalyticsService.getFinanceDashboard();
        // LTV = arpu * 20 when churn is 0
        assertEquals(1000L * 20, dto.getLtvMinor());
    }

    // ==================== MRR REPORT ====================
    @Test
    void testGetMrrReport() {
        when(subscriptionRepository.findByStatus(Status.ACTIVE)).thenReturn(Arrays.asList(subscription));
        when(snapshotRepository.findAllByOrderBySnapshotDateAsc()).thenReturn(Arrays.asList(snapshot));
        MrrReportDTO dto = revenueAnalyticsService.getMrrReport();
        assertNotNull(dto);
        assertTrue(dto.getMrrMinor() > 0);
        assertNotNull(dto.getRevenueTrend());
    }

    // ==================== ARR REPORT ====================
    @Test
    void testGetArrReport() {
        when(subscriptionRepository.findByStatus(Status.ACTIVE)).thenReturn(Arrays.asList(subscription));
        when(snapshotRepository.findAllByOrderBySnapshotDateAsc()).thenReturn(Arrays.asList(snapshot));
        ArrReportDTO dto = revenueAnalyticsService.getArrReport();
        assertNotNull(dto);
        assertNotNull(dto.getArrTrend());
        assertNotNull(dto.getRevenueByPlan());
        assertNotNull(dto.getRevenueByRegion());
    }

    // ==================== CHURN REPORT ====================
    @Test
    void testGetChurnReport_WithTwoSnapshots() {
        when(subscriptionRepository.findByStatus(Status.ACTIVE)).thenReturn(Arrays.asList(subscription));
        when(subscriptionRepository.findByStatus(Status.CANCELED)).thenReturn(Collections.emptyList());
        when(snapshotRepository.findAllByOrderBySnapshotDateAsc()).thenReturn(Arrays.asList(snapshot, snapshot2));

        ChurnReportDTO dto = revenueAnalyticsService.getChurnReport();
        assertNotNull(dto);
        assertTrue(dto.getRevenueChurnPercent().doubleValue() > 0);
        assertNotNull(dto.getChurnTrend());
        assertNotNull(dto.getReasons());
    }

    @Test
    void testGetChurnReport_WithOneSnapshot() {
        when(subscriptionRepository.findByStatus(Status.ACTIVE)).thenReturn(Arrays.asList(subscription));
        when(subscriptionRepository.findByStatus(Status.CANCELED)).thenReturn(Collections.emptyList());
        when(snapshotRepository.findAllByOrderBySnapshotDateAsc()).thenReturn(Arrays.asList(snapshot));

        ChurnReportDTO dto = revenueAnalyticsService.getChurnReport();
        assertNotNull(dto);
        assertNotNull(dto.getChurnTrend());
    }

    @Test
    void testGetChurnReport_NoSnapshots() {
        when(subscriptionRepository.findByStatus(Status.ACTIVE)).thenReturn(Arrays.asList(subscription));
        Subscription canceledSub = Subscription.builder().id(2L).customer(customer)
                .plan(plan).status(Status.CANCELED).currency("INR").build();
        when(subscriptionRepository.findByStatus(Status.CANCELED)).thenReturn(Arrays.asList(canceledSub));
        when(snapshotRepository.findAllByOrderBySnapshotDateAsc()).thenReturn(Collections.emptyList());

        ChurnReportDTO dto = revenueAnalyticsService.getChurnReport();
        assertNotNull(dto);
        assertTrue(dto.getNetChurnPercent().doubleValue() > 0);
    }

    // ==================== ARPU/LTV REPORT ====================
    @Test
    void testGetArpuLtvReport() {
        when(subscriptionRepository.findByStatus(Status.ACTIVE)).thenReturn(Arrays.asList(subscription));
        when(subscriptionRepository.findByStatus(Status.CANCELED)).thenReturn(Collections.emptyList());
        when(snapshotRepository.findAllByOrderBySnapshotDateAsc()).thenReturn(Arrays.asList(snapshot));

        ArpuLtvReportDTO dto = revenueAnalyticsService.getArpuLtvReport();
        assertNotNull(dto);
        assertTrue(dto.getArpuMinor() > 0);
        assertTrue(dto.getLtvMinor() > 0);
        assertNotNull(dto.getCacLtvRatio());
        assertNotNull(dto.getArpuTrend());
    }

    @Test
    void testGetArpuLtvReport_NoActiveCustomers() {
        when(subscriptionRepository.findByStatus(Status.ACTIVE)).thenReturn(Collections.emptyList());
        when(subscriptionRepository.findByStatus(Status.CANCELED)).thenReturn(Collections.emptyList());
        when(snapshotRepository.findAllByOrderBySnapshotDateAsc()).thenReturn(Collections.emptyList());

        ArpuLtvReportDTO dto = revenueAnalyticsService.getArpuLtvReport();
        assertNotNull(dto);
        assertEquals(0L, dto.getArpuMinor());
    }

    // ==================== INVOICE RECORDS ====================
    @Test
    void testGetAllInvoiceRecords() {
        Invoice invoice = Invoice.builder().id(1L).customer(customer).totalMinor(5000L)
                .currency("INR").issueDate(LocalDate.now()).dueDate(LocalDate.now().plusDays(7))
                .status(Status.PAID).createdAt(LocalDateTime.now()).build();
        Pageable pageable = PageRequest.of(0, 10);
        when(invoiceRepository.findAllByOrderByIdDesc(pageable))
                .thenReturn(new PageImpl<>(Arrays.asList(invoice)));

        Page<InvoiceRecordDTO> result = revenueAnalyticsService.getAllInvoiceRecords(pageable);
        assertEquals(1, result.getTotalElements());
    }

    // ==================== PAYMENT RECORDS ====================
    @Test
    void testGetAllPaymentRecords() {
        PaymentMethod pm = new PaymentMethod();
        pm.setPaymentType(PaymentType.CARD);
        pm.setCardLast4("1234");
        Invoice invoice = Invoice.builder().id(1L).customer(customer).currency("INR")
                .issueDate(LocalDate.now()).build();
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setInvoice(invoice);
        payment.setPaymentMethod(pm);
        payment.setAmountMinor(5000L);
        payment.setCurrency("INR");
        payment.setStatus(Status.SUCCESS);
        payment.setCreatedAt(LocalDateTime.now());

        Pageable pageable = PageRequest.of(0, 10);
        when(paymentRepository.findAllByOrderByIdDesc(pageable))
                .thenReturn(new PageImpl<>(Arrays.asList(payment)));

        Page<PaymentRecordDTO> result = revenueAnalyticsService.getAllPaymentRecords(pageable);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetAllPaymentRecords_NullInvoice() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setInvoice(null);
        payment.setPaymentMethod(null);
        payment.setAmountMinor(5000L);
        payment.setCurrency("INR");
        payment.setStatus(Status.FAILED);
        payment.setCreatedAt(LocalDateTime.now());

        Pageable pageable = PageRequest.of(0, 10);
        when(paymentRepository.findAllByOrderByIdDesc(pageable))
                .thenReturn(new PageImpl<>(Arrays.asList(payment)));

        Page<PaymentRecordDTO> result = revenueAnalyticsService.getAllPaymentRecords(pageable);
        assertEquals(1, result.getTotalElements());
        assertEquals("N/A", result.getContent().get(0).getInvoiceNumber());
    }

    @Test
    void testGetAllPaymentRecords_UpiPaymentMethod() {
        PaymentMethod pm = new PaymentMethod();
        pm.setPaymentType(PaymentType.UPI);
        pm.setUpiId("user@okbank");
        Invoice invoice = Invoice.builder().id(1L).customer(customer).currency("INR")
                .issueDate(LocalDate.now()).build();
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setInvoice(invoice);
        payment.setPaymentMethod(pm);
        payment.setAmountMinor(3000L);
        payment.setCurrency("INR");
        payment.setStatus(Status.SUCCESS);
        payment.setCreatedAt(LocalDateTime.now());

        Pageable pageable = PageRequest.of(0, 10);
        when(paymentRepository.findAllByOrderByIdDesc(pageable))
                .thenReturn(new PageImpl<>(Arrays.asList(payment)));

        Page<PaymentRecordDTO> result = revenueAnalyticsService.getAllPaymentRecords(pageable);
        assertTrue(result.getContent().get(0).getPaymentMethod().contains("UPI"));
    }

    // ==================== REFUND CREDITS ====================
    @Test
    void testGetAllRefundCredits() {
        Invoice invoice = Invoice.builder().id(1L).customer(customer).currency("INR")
                .issueDate(LocalDate.now()).build();
        CreditNote cn = new CreditNote();
        cn.setId(1L);
        cn.setInvoice(invoice);
        cn.setAmountMinor(2000L);
        cn.setReason("Proration credit");
        cn.setStatus(Status.ISSUED);
        cn.setCreatedAt(LocalDateTime.now());

        Payment relatedPayment = new Payment();
        relatedPayment.setId(10L);

        Pageable pageable = PageRequest.of(0, 10);
        when(creditNoteRepository.findAllByOrderByIdDesc(pageable))
                .thenReturn(new PageImpl<>(Arrays.asList(cn)));
        when(paymentRepository.findFirstByInvoiceIdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(relatedPayment));

        Page<RefundCreditDTO> result = revenueAnalyticsService.getAllRefundCredits(pageable);
        assertEquals(1, result.getTotalElements());
        assertEquals(10L, result.getContent().get(0).getPaymentId());
    }

    @Test
    void testGetAllRefundCredits_NullInvoice() {
        CreditNote cn = new CreditNote();
        cn.setId(1L);
        cn.setInvoice(null);
        cn.setAmountMinor(1000L);
        cn.setReason("Manual credit");
        cn.setStatus(Status.ISSUED);
        cn.setCreatedAt(LocalDateTime.now());

        Pageable pageable = PageRequest.of(0, 10);
        when(creditNoteRepository.findAllByOrderByIdDesc(pageable))
                .thenReturn(new PageImpl<>(Arrays.asList(cn)));

        Page<RefundCreditDTO> result = revenueAnalyticsService.getAllRefundCredits(pageable);
        assertEquals(1, result.getTotalElements());
        assertNull(result.getContent().get(0).getPaymentId());
    }

    // ==================== REVENUE SNAPSHOTS ====================
    @Test
    void testGetAllRevenueSnapshots() {
        Pageable pageable = PageRequest.of(0, 10);
        when(snapshotRepository.findAllByOrderByIdDesc(pageable))
                .thenReturn(new PageImpl<>(Arrays.asList(snapshot)));

        Page<RevenueSnapshotDTO> result = revenueAnalyticsService.getAllRevenueSnapshots(pageable);
        assertEquals(1, result.getTotalElements());
    }
}
