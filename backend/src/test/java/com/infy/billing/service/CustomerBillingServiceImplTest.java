package com.infy.billing.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.infy.billing.dto.customer.InvoiceDTO;
import com.infy.billing.dto.customer.CouponDTO;
import com.infy.billing.entity.Customer;
import com.infy.billing.entity.User;
import com.infy.billing.entity.Invoice;
import com.infy.billing.entity.Coupon;
import com.infy.billing.entity.Subscription;
import com.infy.billing.entity.SubscriptionCoupon;
import com.infy.billing.enums.Status;
import com.infy.billing.enums.BillingReason;
import com.infy.billing.enums.CouponType;
import com.infy.billing.repository.InvoiceRepository;
import com.infy.billing.repository.InvoiceLineItemRepository;
import com.infy.billing.repository.PaymentRepository;
import com.infy.billing.repository.CreditNoteRepository;
import com.infy.billing.repository.CouponRepository;
import com.infy.billing.repository.SubscriptionRepository;
import com.infy.billing.repository.CustomerRepository;
import com.infy.billing.repository.UserRepository;
import com.infy.billing.repository.SubscriptionCouponRepository;

@ExtendWith(MockitoExtension.class)
public class CustomerBillingServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private InvoiceLineItemRepository invoiceLineItemRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private CreditNoteRepository creditNoteRepository;
    @Mock
    private CouponRepository couponRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private InvoicePdfService invoicePdfService;
    @Mock
    private SubscriptionCouponRepository subscriptionCouponRepository;

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
                .id(1L)
                .invoiceNumber("INV-1")
                .customer(customer)
                .subscription(subscription)
                .status(Status.OPEN)
                .billingReason(BillingReason.SUBSCRIPTION_CYCLE)
                .issueDate(LocalDate.now())
                .totalMinor(1000L)
                .balanceMinor(1000L)
                .build();
        coupon = Coupon.builder()
                .id(1L)
                .code("DISCOUNT10")
                .name("10% Off")
                .type(CouponType.PERCENT)
                .amount(10L)
                .status(Status.ACTIVE)
                .validFrom(LocalDate.now().minusDays(1))
                .validTo(LocalDate.now().plusDays(10))
                .redeemedCount(0)
                .maxRedemptions(100)
                .build();
    }

    @Test
    void testGetInvoices() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
        when(invoiceRepository.findByCustomer_IdOrderByIssueDateDesc(1L)).thenReturn(Arrays.asList(invoice));

        List<InvoiceDTO> result = customerBillingService.getInvoices("test@test.com", null, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("INV-1", result.get(0).getInvoiceNumber());
    }

    @Test
    void testGetInvoiceDetail() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        InvoiceDTO dto = customerBillingService.getInvoiceDetail("test@test.com", 1L);

        assertNotNull(dto);
        assertEquals("INV-1", dto.getInvoiceNumber());
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

    @Test
    void testGenerateInvoicePdf() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoicePdfService.generatePdf(invoice)).thenReturn(new byte[] { 1, 2, 3 });

        byte[] pdf = customerBillingService.generateInvoicePdf("test@test.com", 1L);

        assertNotNull(pdf);
        assertEquals(3, pdf.length);
    }

    @Test
    void testApplyCoupon() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
        when(couponRepository.findByCodeAndStatus("DISCOUNT10", Status.ACTIVE)).thenReturn(Optional.of(coupon));
        when(subscriptionRepository.findByCustomer_IdAndStatusIn(anyLong(), anyList()))
                .thenReturn(Arrays.asList(subscription));
        when(subscriptionCouponRepository.findBySubscription_IdAndStatus(1L, Status.ACTIVE))
                .thenReturn(Optional.empty());

        CouponDTO dto = customerBillingService.applyCoupon("test@test.com", "DISCOUNT10");

        assertNotNull(dto);
        assertEquals("DISCOUNT10", dto.getCode());
        verify(couponRepository, times(1)).save(coupon);
        verify(subscriptionCouponRepository, times(1)).save(any(SubscriptionCoupon.class));
    }

    @Test
    void testValidateCoupon_Expired() {
        coupon.setValidTo(LocalDate.now().minusDays(1));
        when(couponRepository.findByCodeAndStatus("DISCOUNT10", Status.ACTIVE)).thenReturn(Optional.of(coupon));

        assertThrows(RuntimeException.class, () -> customerBillingService.validateCoupon("DISCOUNT10"));
    }

    @Test
    void testApplyCoupon_NotFound() {
        lenient().when(couponRepository.findByCodeAndStatus("INVALID", Status.ACTIVE)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            customerBillingService.applyCoupon("test@test.com", "INVALID")
        );
    }

    @Test
    void testApplyCoupon_AlreadyUsed() {
        coupon.setRedeemedCount(100);
        coupon.setMaxRedemptions(100);
        lenient().when(couponRepository.findByCodeAndStatus("DISCOUNT10", Status.ACTIVE)).thenReturn(Optional.of(coupon));

        assertThrows(RuntimeException.class, () -> 
            customerBillingService.applyCoupon("test@test.com", "DISCOUNT10")
        );
    }

    @Test
    void testApplyCoupon_SubscriptionAlreadyHasCoupon() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
        when(couponRepository.findByCodeAndStatus("DISCOUNT10", Status.ACTIVE)).thenReturn(Optional.of(coupon));
        when(subscriptionRepository.findByCustomer_IdAndStatusIn(anyLong(), anyList()))
                .thenReturn(Arrays.asList(subscription));
                
        SubscriptionCoupon sc = new SubscriptionCoupon();
        when(subscriptionCouponRepository.findBySubscription_IdAndStatus(1L, Status.ACTIVE))
                .thenReturn(Optional.of(sc)); // Already has coupon!

        assertThrows(RuntimeException.class, () -> 
            customerBillingService.applyCoupon("test@test.com", "DISCOUNT10")
        );
    }
}
