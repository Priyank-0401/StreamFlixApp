package com.infy.billing.service;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import com.infy.billing.exception.CustomException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.infy.billing.entity.Customer;
import com.infy.billing.entity.Subscription;
import com.infy.billing.entity.Plan;
import com.infy.billing.entity.Invoice;
import com.infy.billing.entity.PaymentMethod;
import com.infy.billing.enums.Status;
import com.infy.billing.enums.BillingPeriod;
import com.infy.billing.repository.SubscriptionRepository;
import com.infy.billing.repository.InvoiceRepository;
import com.infy.billing.repository.PaymentMethodRepository;
import com.infy.billing.repository.PaymentRepository;
import com.infy.billing.repository.InvoiceLineItemRepository;
import com.infy.billing.repository.SubscriptionItemRepository;
import com.infy.billing.repository.DunningRetryLogRepository;
import com.infy.billing.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BillingEngineServiceImplTest {

        @Mock
        private SubscriptionRepository subscriptionRepository;
        @Mock
        private InvoiceRepository invoiceRepository;
        @Mock
        private PaymentMethodRepository paymentMethodRepository;
        @Mock
        private PaymentRepository paymentRepository;
        @Mock
        private InvoiceLineItemRepository invoiceLineItemRepository;
        @Mock
        private SubscriptionItemRepository subscriptionItemRepository;
        @Mock
        private DunningRetryLogRepository dunningRetryLogRepository;
        @Mock
        private CustomerRepository customerRepository;
        @Mock
        private MockPaymentGateway mockPaymentGateway;

        @InjectMocks
        private BillingEngineServiceImpl billingEngineService;

        private Subscription subscription;
        private Plan plan;
        private Customer customer;
        private PaymentMethod paymentMethod;

        @BeforeEach
        void setUp() {
                customer = Customer.builder().id(1L).creditBalanceMinor(0L).build();
                plan = Plan.builder()
                                .id(1L)
                                .name("Basic")
                                .defaultPriceMinor(1000L)
                                .billingPeriod(BillingPeriod.MONTHLY)
                                .build();
                subscription = Subscription.builder()
                                .id(1L)
                                .customer(customer)
                                .plan(plan)
                                .status(Status.ACTIVE)
                                .paymentMethodId(1L)
                                .currency("USD")
                                .currentPeriodEnd(LocalDate.now())
                                .build();
                paymentMethod = new PaymentMethod();
                paymentMethod.setId(1L);
                paymentMethod.setStatus(Status.ACTIVE);
                paymentMethod.setGatewayToken("token123");
        }

        @Test
        void testProcessRenewals_Success() throws Exception {
                when(subscriptionRepository.findByCurrentPeriodEndAndStatusAndCancelAtPeriodEndFalse(any(), any()))
                                .thenReturn(new ArrayList<>(Arrays.asList(subscription)));
                when(subscriptionRepository.findByTrialEndDateAndStatus(any(), any()))
                                .thenReturn(new ArrayList<>());
                when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(paymentMethod));
                when(invoiceRepository.existsBySubscriptionAndBillingReasonAndIssueDate(any(), any(), any()))
                                .thenReturn(false);

                Invoice invoice = Invoice.builder()
                                .id(1L)
                                .totalMinor(1000L)
                                .build();
                
                doReturn(invoice).when(invoiceRepository).save(any(Invoice.class));
                lenient().when(invoiceLineItemRepository.findByInvoice_Id(anyLong())).thenReturn(new ArrayList<>());
                when(mockPaymentGateway.charge(anyString(), anyLong(), anyString())).thenReturn("gateway_ref");

                billingEngineService.processRenewals();

                verify(invoiceRepository, times(3)).save(any(Invoice.class)); // Create, Recalculate, Pay
                verify(subscriptionRepository, times(1)).save(subscription); // Advance
        }

        @Test
        void testProcessRenewals_MissingPaymentMethod() {
                subscription.setPaymentMethodId(null);
                when(subscriptionRepository.findByCurrentPeriodEndAndStatusAndCancelAtPeriodEndFalse(any(), any()))
                                .thenReturn(new ArrayList<>(Arrays.asList(subscription)));
                when(subscriptionRepository.findByTrialEndDateAndStatus(any(), any()))
                                .thenReturn(new ArrayList<>());

                billingEngineService.processRenewals();

                assertEquals(Status.PAST_DUE, subscription.getStatus());
                verify(subscriptionRepository, times(1)).save(subscription);
        }

        @Test
        void testProcessRenewals_PaymentFailed() throws Exception {
                when(subscriptionRepository.findByCurrentPeriodEndAndStatusAndCancelAtPeriodEndFalse(any(), any()))
                                .thenReturn(new ArrayList<>(Arrays.asList(subscription)));
                when(subscriptionRepository.findByTrialEndDateAndStatus(any(), any()))
                                .thenReturn(new java.util.ArrayList<>());
                when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(paymentMethod));
                when(invoiceRepository.existsBySubscriptionAndBillingReasonAndIssueDate(any(), any(), any()))
                                .thenReturn(false);

                Invoice invoice = Invoice.builder().id(1L).totalMinor(1000L).currency("USD").build();
                when(invoiceRepository.save(any())).thenReturn(invoice);
                
                when(paymentRepository.save(any(com.infy.billing.entity.Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
                
                com.infy.billing.entity.InvoiceLineItem line = new com.infy.billing.entity.InvoiceLineItem();
                line.setAmountMinor(1000L);
                line.setLineType(com.infy.billing.entity.InvoiceLineItem.LineType.PLAN);
                when(invoiceLineItemRepository.findByInvoice_Id(anyLong())).thenReturn(java.util.Arrays.asList(line));
                
                when(mockPaymentGateway.charge(eq("token123"), eq(1000L), eq("USD"))).thenThrow(CustomException.paymentFailed("Card declined"));

                billingEngineService.processRenewals();

                assertEquals(Status.PAST_DUE, subscription.getStatus());
                verify(dunningRetryLogRepository, times(1)).save(any());
        }

        @Test
        void testProcessRenewals_TrialExpired() throws Exception {
                when(subscriptionRepository.findByCurrentPeriodEndAndStatusAndCancelAtPeriodEndFalse(any(), any()))
                                .thenReturn(new java.util.ArrayList<>());
                
                subscription.setStatus(Status.TRIALING);
                when(subscriptionRepository.findByTrialEndDateAndStatus(any(), any()))
                                .thenReturn(new java.util.ArrayList<>(Arrays.asList(subscription)));
                                
                when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(paymentMethod));
                when(invoiceRepository.existsBySubscriptionAndBillingReasonAndIssueDate(any(), any(), any()))
                                .thenReturn(false);

                Invoice invoice = Invoice.builder().id(1L).totalMinor(1000L).build();
                doReturn(invoice).when(invoiceRepository).save(any(Invoice.class));
                lenient().when(invoiceLineItemRepository.findByInvoice_Id(anyLong())).thenReturn(new java.util.ArrayList<>());
                when(mockPaymentGateway.charge(anyString(), anyLong(), anyString())).thenReturn("gateway_ref");

                billingEngineService.processRenewals();

                assertEquals(Status.ACTIVE, subscription.getStatus()); // Upgraded to active!
                verify(subscriptionRepository, times(1)).save(subscription);
        }
}
