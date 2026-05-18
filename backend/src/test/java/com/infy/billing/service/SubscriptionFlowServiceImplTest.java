package com.infy.billing.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.infy.billing.dto.customer.CustomerRegistrationRequest;
import com.infy.billing.dto.customer.PaymentMethodRequest;
import com.infy.billing.dto.customer.SubscriptionCompletionRequest;
import com.infy.billing.dto.customer.SubscriptionResponse;
import com.infy.billing.entity.Customer;
import com.infy.billing.entity.User;
import com.infy.billing.entity.Plan;
import com.infy.billing.entity.PaymentMethod;
import com.infy.billing.entity.Subscription;
import com.infy.billing.enums.Status;
import com.infy.billing.enums.PaymentType;
import com.infy.billing.enums.BillingPeriod;
import com.infy.billing.enums.TaxMode;
import com.infy.billing.repository.CustomerRepository;
import com.infy.billing.repository.UserRepository;
import com.infy.billing.repository.PlanRepository;
import com.infy.billing.repository.PaymentMethodRepository;
import com.infy.billing.repository.SubscriptionRepository;
import com.infy.billing.repository.InvoiceRepository;
import com.infy.billing.repository.PaymentRepository;
import com.infy.billing.repository.PriceBookEntryRepository;
import com.infy.billing.repository.TaxRateRepository;
import com.infy.billing.repository.SubscriptionItemRepository;
import com.infy.billing.repository.CouponRepository;
import com.infy.billing.repository.SubscriptionCouponRepository;
import com.infy.billing.repository.InvoiceLineItemRepository;

@ExtendWith(MockitoExtension.class)
public class SubscriptionFlowServiceImplTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private UserRepository userRepository;
    @Mock private PlanRepository planRepository;
    @Mock private PaymentMethodRepository paymentMethodRepository;
    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private InvoiceRepository invoiceRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private PriceBookEntryRepository priceBookEntryRepository;
    @Mock private TaxRateRepository taxRateRepository;
    @Mock private SubscriptionItemRepository subscriptionItemRepository;
    @Mock private CouponRepository couponRepository;
    @Mock private SubscriptionCouponRepository subscriptionCouponRepository;
    @Mock private InvoiceLineItemRepository invoiceLineItemRepository;
    @Mock private MockPaymentGateway mockPaymentGateway;

    @InjectMocks
    private SubscriptionFlowServiceImpl subscriptionFlowService;

    private Customer customer;
    private User user;
    private Plan plan;
    private PaymentMethod paymentMethod;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@test.com").build();
        customer = Customer.builder().id(1L).user(user).status(Status.ACTIVE).creditBalanceMinor(0L).build();
        plan = Plan.builder()
                .id(1L)
                .name("Basic")
                .status(Status.ACTIVE)
                .defaultPriceMinor(1000L)
                .taxMode(TaxMode.EXCLUSIVE)
                .trialDays(0)
                .build();
        paymentMethod = new PaymentMethod();
        paymentMethod.setId(1L);
        paymentMethod.setCustomer(customer);
        paymentMethod.setPaymentType(PaymentType.CARD);
        paymentMethod.setIsDefault(true);
        paymentMethod.setGatewayToken("token123");
    }

    @Test
    void testRegisterCustomerDetails() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

        CustomerRegistrationRequest request = new CustomerRegistrationRequest();
        request.setPhone("1234567890");
        request.setCountry("US");

        Customer result = subscriptionFlowService.registerCustomerDetails("test@test.com", request);

        assertNotNull(result);
        assertEquals("1234567890", result.getPhone());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void testCreatePaymentMethod() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        PaymentMethodRequest request = new PaymentMethodRequest();
        request.setPaymentType(PaymentType.CARD);
        request.setCardNumber("1234567890124321");
        request.setExpiryMonth("12");
        request.setExpiryYear("2030");

        PaymentMethod result = subscriptionFlowService.createPaymentMethod(1L, request);

        assertNotNull(result);
        assertEquals("4321", result.getCardLast4());
        verify(paymentMethodRepository, times(1)).save(any(PaymentMethod.class));
    }

    @Test
    void testCompleteSubscription() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(paymentMethod));
        when(priceBookEntryRepository.findByPlan_IdAndRegionAndCurrency(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(taxRateRepository.findByRegionAndEffectiveToIsNullOrFuture(any(), any()))
                .thenReturn(Optional.empty());

        SubscriptionCompletionRequest request = new SubscriptionCompletionRequest();
        request.setPlanId(1L);
        request.setPaymentMethodId(1L);
        request.setBillingPeriod(BillingPeriod.MONTHLY);

        SubscriptionResponse response = subscriptionFlowService.completeSubscription(1L, request);

        assertNotNull(response);
        assertEquals("Subscription activated successfully", response.getMessage());
        verify(subscriptionRepository, times(1)).save(any(Subscription.class));
    }

    @Test
    void testRegisterCustomerDetails_UserNotFound() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        CustomerRegistrationRequest request = new CustomerRegistrationRequest();

        assertThrows(RuntimeException.class, () -> 
            subscriptionFlowService.registerCustomerDetails("test@test.com", request)
        );
    }

    @Test
    void testRegisterCustomerDetails_CustomerAlreadyExists() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer)); // Already exists!

        CustomerRegistrationRequest request = new CustomerRegistrationRequest();

        Customer result = subscriptionFlowService.registerCustomerDetails("test@test.com", request);

        assertNotNull(result);
        assertEquals(customer, result);
    }

    @Test
    void testCreatePaymentMethod_CustomerNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        PaymentMethodRequest request = new PaymentMethodRequest();

        assertThrows(RuntimeException.class, () -> 
            subscriptionFlowService.createPaymentMethod(1L, request)
        );
    }

    @Test
    void testCompleteSubscription_PlanNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(planRepository.findById(1L)).thenReturn(Optional.empty()); // Plan not found!

        SubscriptionCompletionRequest request = new SubscriptionCompletionRequest();
        request.setPlanId(1L);

        assertThrows(RuntimeException.class, () -> 
            subscriptionFlowService.completeSubscription(1L, request)
        );
    }
}
