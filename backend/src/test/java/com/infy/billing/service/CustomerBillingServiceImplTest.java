package com.infy.billing.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.infy.billing.dto.customer.*;
import com.infy.billing.entity.*;
import com.infy.billing.enums.Status;
import com.infy.billing.enums.BillingReason;
import com.infy.billing.enums.CouponType;
import com.infy.billing.repository.*;

@ExtendWith(MockitoExtension.class)
class CustomerBillingServiceImplTest {

    @Mock private InvoiceRepository invoiceRepository;
    @Mock private InvoiceLineItemRepository invoiceLineItemRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private CreditNoteRepository creditNoteRepository;
    @Mock private CouponRepository couponRepository;
    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private UserRepository userRepository;
    @Mock private InvoicePdfService invoicePdfService;
    @Mock private SubscriptionCouponRepository subscriptionCouponRepository;

    @InjectMocks
    private CustomerBillingServiceImpl customerBillingService;

    private Customer customer;
    private User user;
    private Invoice invoice;
    private Subscription subscription;
    private Coupon coupon;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@test.com").fullName("Test User").build();
        customer = Customer.builder().id(1L).user(user).status(Status.ACTIVE).build();
        subscription = Subscription.builder().id(1L).customer(customer).status(Status.ACTIVE).build();
        invoice = Invoice.builder()
                .id(1L).invoiceNumber("INV-1").customer(customer).subscription(subscription)
                .status(Status.OPEN).billingReason(BillingReason.SUBSCRIPTION_CYCLE)
                .issueDate(LocalDate.now()).totalMinor(1000L).balanceMinor(1000L).build();
        coupon = Coupon.builder()
                .id(1L).code("DISCOUNT10").name("10% Off").type(CouponType.PERCENT)
                .amount(10L).status(Status.ACTIVE).validFrom(LocalDate.now().minusDays(1))
                .validTo(LocalDate.now().plusDays(10)).redeemedCount(0).maxRedemptions(100).build();
    }

    // ==================== GET INVOICES ====================
    @Test
    void testGetInvoices_NoStatusFilter() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
        when(invoiceRepository.findByCustomer_IdOrderByIssueDateDesc(1L)).thenReturn(Arrays.asList(invoice));

        List<InvoiceDTO> result = customerBillingService.getInvoices("test@test.com", null, null, null);
        assertEquals(1, result.size());
        assertEquals("INV-1", result.get(0).getInvoiceNumber());
    }

    @Test
    void testGetInvoices_WithStatusFilter() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
        when(invoiceRepository.findByCustomer_IdAndStatus(1L, "OPEN")).thenReturn(Arrays.asList(invoice));

        List<InvoiceDTO> result = customerBillingService.getInvoices("test@test.com", "OPEN", null, null);
        assertEquals(1, result.size());
    }

    // ==================== GET INVOICE DETAIL ====================
    @Test
    void testGetInvoiceDetail_Success() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        InvoiceDTO dto = customerBillingService.getInvoiceDetail("test@test.com", 1L);
        assertEquals("INV-1", dto.getInvoiceNumber());
    }

    @Test
    void testGetInvoiceDetail_NotFound() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
        when(invoiceRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> customerBillingService.getInvoiceDetail("test@test.com", 1L));
    }

    @Test
    void testGetInvoiceDetail_Unauthorized() {
        Customer otherCustomer = Customer.builder().id(2L).build();
        invoice.setCustomer(otherCustomer);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        assertThrows(RuntimeException.class, () -> customerBillingService.getInvoiceDetail("test@test.com", 1L));
    }

    // ==================== GENERATE INVOICE PDF ====================
    @Test
    void testGenerateInvoicePdf_Success() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoicePdfService.generatePdf(invoice)).thenReturn(new byte[]{1, 2, 3});
        byte[] pdf = customerBillingService.generateInvoicePdf("test@test.com", 1L);
        assertEquals(3, pdf.length);
    }

    @Test
    void testGenerateInvoicePdf_NotFound() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
        when(invoiceRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> customerBillingService.generateInvoicePdf("test@test.com", 1L));
    }

    @Test
    void testGenerateInvoicePdf_Unauthorized() {
        Customer otherCustomer = Customer.builder().id(2L).build();
        invoice.setCustomer(otherCustomer);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        assertThrows(RuntimeException.class, () -> customerBillingService.generateInvoicePdf("test@test.com", 1L));
    }

    // ==================== GET PAYMENTS ====================
    @Test
    void testGetPayments() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setInvoice(invoice);
        payment.setAmountMinor(1000L);
        payment.setCurrency("INR");
        payment.setStatus(Status.SUCCESS);
        payment.setAttemptNo(1);
        payment.setCreatedAt(LocalDateTime.now());
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        List<PaymentDTO> result = customerBillingService.getPayments("test@test.com");
        assertNotNull(result);
    }

    // ==================== GET CREDIT NOTES ====================
    @Test
    void testGetCreditNotes() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));

        CreditNote cn = new CreditNote();
        cn.setId(1L);
        cn.setInvoice(invoice);
        cn.setCreditNoteNumber("CN-1");
        cn.setReason("Refund");
        cn.setAmountMinor(500L);
        cn.setStatus(Status.ISSUED);
        cn.setCreatedAt(LocalDateTime.now());
        when(creditNoteRepository.findById(1L)).thenReturn(Optional.of(cn));

        List<CreditNoteDTO> result = customerBillingService.getCreditNotes("test@test.com");
        assertNotNull(result);
    }

    // ==================== APPLY COUPON ====================
    @Test
    void testApplyCoupon_Success() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
        when(couponRepository.findByCodeAndStatus("DISCOUNT10", Status.ACTIVE)).thenReturn(Optional.of(coupon));
        when(subscriptionRepository.findByCustomer_IdAndStatusIn(anyLong(), anyList()))
                .thenReturn(Arrays.asList(subscription));
        when(subscriptionCouponRepository.findBySubscription_IdAndStatus(1L, Status.ACTIVE))
                .thenReturn(Optional.empty());

        CouponDTO dto = customerBillingService.applyCoupon("test@test.com", "DISCOUNT10");
        assertEquals("DISCOUNT10", dto.getCode());
        verify(subscriptionCouponRepository).save(any(SubscriptionCoupon.class));
        verify(couponRepository).save(coupon);
    }

    @Test
    void testApplyCoupon_InvalidCoupon() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
        when(couponRepository.findByCodeAndStatus("INVALID", Status.ACTIVE)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> customerBillingService.applyCoupon("test@test.com", "INVALID"));
    }

    @Test
    void testApplyCoupon_NoActiveSubscription() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
        when(couponRepository.findByCodeAndStatus("DISCOUNT10", Status.ACTIVE)).thenReturn(Optional.of(coupon));
        when(subscriptionRepository.findByCustomer_IdAndStatusIn(anyLong(), anyList()))
                .thenReturn(Collections.emptyList());
        assertThrows(RuntimeException.class, () -> customerBillingService.applyCoupon("test@test.com", "DISCOUNT10"));
    }

    @Test
    void testApplyCoupon_SameCouponAlreadyActive() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
        when(couponRepository.findByCodeAndStatus("DISCOUNT10", Status.ACTIVE)).thenReturn(Optional.of(coupon));
        when(subscriptionRepository.findByCustomer_IdAndStatusIn(anyLong(), anyList()))
                .thenReturn(Arrays.asList(subscription));

        SubscriptionCoupon sc = new SubscriptionCoupon();
        sc.setCoupon(coupon); // Same coupon!
        when(subscriptionCouponRepository.findBySubscription_IdAndStatus(1L, Status.ACTIVE))
                .thenReturn(Optional.of(sc));

        assertThrows(RuntimeException.class, () -> customerBillingService.applyCoupon("test@test.com", "DISCOUNT10"));
    }

    @Test
    void testApplyCoupon_ReplacesExistingCoupon() {
        Coupon otherCoupon = Coupon.builder().id(2L).code("OTHER").name("Other").type(CouponType.FIXED)
                .amount(500L).status(Status.ACTIVE).validFrom(LocalDate.now().minusDays(1))
                .redeemedCount(0).build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
        when(couponRepository.findByCodeAndStatus("DISCOUNT10", Status.ACTIVE)).thenReturn(Optional.of(coupon));
        when(subscriptionRepository.findByCustomer_IdAndStatusIn(anyLong(), anyList()))
                .thenReturn(Arrays.asList(subscription));

        SubscriptionCoupon oldSc = new SubscriptionCoupon();
        oldSc.setCoupon(otherCoupon); // Different coupon
        oldSc.setStatus(Status.ACTIVE);
        when(subscriptionCouponRepository.findBySubscription_IdAndStatus(1L, Status.ACTIVE))
                .thenReturn(Optional.of(oldSc));

        CouponDTO dto = customerBillingService.applyCoupon("test@test.com", "DISCOUNT10");
        assertEquals("DISCOUNT10", dto.getCode());
        assertEquals(Status.CANCELED, oldSc.getStatus()); // Old coupon deactivated
        verify(subscriptionCouponRepository, times(2)).save(any(SubscriptionCoupon.class));
    }

    // ==================== VALIDATE COUPON ====================
    @Test
    void testValidateCoupon_Success() {
        when(couponRepository.findByCodeAndStatus("DISCOUNT10", Status.ACTIVE)).thenReturn(Optional.of(coupon));
        CouponDTO dto = customerBillingService.validateCoupon("DISCOUNT10");
        assertEquals("DISCOUNT10", dto.getCode());
    }

    @Test
    void testValidateCoupon_InvalidCode() {
        when(couponRepository.findByCodeAndStatus("INVALID", Status.ACTIVE)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> customerBillingService.validateCoupon("INVALID"));
    }

    @Test
    void testValidateCoupon_NotYetValid() {
        coupon.setValidFrom(LocalDate.now().plusDays(5));
        when(couponRepository.findByCodeAndStatus("DISCOUNT10", Status.ACTIVE)).thenReturn(Optional.of(coupon));
        assertThrows(RuntimeException.class, () -> customerBillingService.validateCoupon("DISCOUNT10"));
    }

    @Test
    void testValidateCoupon_Expired() {
        coupon.setValidTo(LocalDate.now().minusDays(1));
        when(couponRepository.findByCodeAndStatus("DISCOUNT10", Status.ACTIVE)).thenReturn(Optional.of(coupon));
        assertThrows(RuntimeException.class, () -> customerBillingService.validateCoupon("DISCOUNT10"));
    }

    @Test
    void testValidateCoupon_MaxRedemptions() {
        coupon.setRedeemedCount(100);
        coupon.setMaxRedemptions(100);
        when(couponRepository.findByCodeAndStatus("DISCOUNT10", Status.ACTIVE)).thenReturn(Optional.of(coupon));
        assertThrows(RuntimeException.class, () -> customerBillingService.validateCoupon("DISCOUNT10"));
    }

    // ==================== GET AVAILABLE COUPONS ====================
    @Test
    void testGetAvailableCoupons() {
        when(couponRepository.findAll()).thenReturn(Arrays.asList(coupon));
        List<CouponDTO> result = customerBillingService.getAvailableCoupons();
        assertEquals(1, result.size());
        assertEquals("DISCOUNT10", result.get(0).getCode());
    }

    @Test
    void testGetAvailableCoupons_FiltersExpired() {
        Coupon expired = Coupon.builder().id(2L).code("EXPIRED").name("Expired").type(CouponType.FIXED)
                .amount(100L).status(Status.ACTIVE).validFrom(LocalDate.now().minusDays(30))
                .validTo(LocalDate.now().minusDays(1)).redeemedCount(0).build();
        when(couponRepository.findAll()).thenReturn(Arrays.asList(coupon, expired));
        List<CouponDTO> result = customerBillingService.getAvailableCoupons();
        assertEquals(1, result.size()); // Only the valid coupon
    }

    @Test
    void testGetAvailableCoupons_FiltersInactive() {
        Coupon inactive = Coupon.builder().id(3L).code("INACTIVE").name("Inactive").type(CouponType.FIXED)
                .amount(100L).status(Status.DISABLED).validFrom(LocalDate.now().minusDays(1))
                .redeemedCount(0).build();
        when(couponRepository.findAll()).thenReturn(Arrays.asList(coupon, inactive));
        List<CouponDTO> result = customerBillingService.getAvailableCoupons();
        assertEquals(1, result.size());
    }

    // ==================== HELPER: USER NOT FOUND ====================
    @Test
    void testGetInvoices_UserNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> customerBillingService.getInvoices("unknown@test.com", null, null, null));
    }
}
