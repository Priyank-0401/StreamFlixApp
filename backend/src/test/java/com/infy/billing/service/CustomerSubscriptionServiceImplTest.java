package com.infy.billing.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.List;
import com.infy.billing.dto.customer.UsageRecordDTO;
import com.infy.billing.entity.AddOn;
import com.infy.billing.enums.TaxMode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.infy.billing.dto.customer.SubscriptionDTO;
import com.infy.billing.dto.customer.SubscriptionResponse;
import com.infy.billing.entity.Subscription;
import com.infy.billing.entity.SubscriptionCoupon;
import com.infy.billing.entity.SubscriptionItem;
import com.infy.billing.entity.Customer;
import com.infy.billing.entity.PaymentMethod;
import com.infy.billing.entity.User;
import com.infy.billing.entity.Plan;
import com.infy.billing.enums.Status;
import com.infy.billing.enums.BillingPeriod;
import com.infy.billing.enums.ItemType;
import com.infy.billing.repository.*;
import com.infy.billing.request.*;

@ExtendWith(MockitoExtension.class)
public class CustomerSubscriptionServiceImplTest {

        @Mock
        private SubscriptionRepository subscriptionRepository;
        @Mock
        private SubscriptionItemRepository subscriptionItemRepository;
        @Mock
        private CustomerRepository customerRepository;
        @Mock
        private UserRepository userRepository;
        @Mock
        private PlanRepository planRepository;
        @Mock
        private AddOnRepository addOnRepository;
        @Mock
        private MeteredComponentRepository meteredComponentRepository;
        @Mock
        private UsageRecordRepository usageRecordRepository;
        @Mock
        private PaymentMethodRepository paymentMethodRepository;
        @Mock
        private InvoiceRepository invoiceRepository;
        @Mock
        private InvoiceLineItemRepository invoiceLineItemRepository;
        @Mock
        private PaymentRepository paymentRepository;
        @Mock
        private SubscriptionCouponRepository subscriptionCouponRepository;
        @Mock
        private MockPaymentGateway mockPaymentGateway;
        @Mock
        private TaxRateRepository taxRateRepository;
        @Mock
        private CreditNoteRepository creditNoteRepository;
        @Mock
        private SubscriptionFlowService subscriptionFlowService;

        @InjectMocks
        private CustomerSubscriptionServiceImpl customerSubscriptionService;

        private Customer customer;
        private User user;
        private Subscription subscription;
        private Plan plan;

        @BeforeEach
        void setUp() {
                user = User.builder().id(1L).email("test@test.com").build();
                customer = Customer.builder().id(1L).user(user).country("IN").currency("USD").creditBalanceMinor(0L)
                                .build();
                plan = Plan.builder().id(1L).name("Basic").defaultPriceMinor(1000L).billingPeriod(BillingPeriod.MONTHLY)
                                .build();
                subscription = Subscription.builder()
                                .id(1L)
                                .customer(customer)
                                .plan(plan)
                                .status(Status.ACTIVE)
                                .startDate(LocalDate.now())
                                .currentPeriodStart(LocalDate.now())
                                .currentPeriodEnd(LocalDate.now().plusMonths(1))
                                .build();
        }

        @Test
        void testGetCurrentSubscription() {
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));

                SubscriptionDTO dto = customerSubscriptionService.getCurrentSubscription("test@test.com");

                assertNotNull(dto);
                assertEquals(1L, dto.getSubscriptionId());
        }

        @Test
        void testCreateSubscription() {
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList()); // No active sub

                when(planRepository.findById(1L)).thenReturn(Optional.of(plan));

                SubscriptionResponse resp = new SubscriptionResponse();
                resp.setSubscriptionId(1L);
                when(subscriptionFlowService.completeSubscription(eq(1L), any())).thenReturn(resp);
                when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));

                CreateSubscriptionRequest req = new CreateSubscriptionRequest();
                req.setPlanId(1L);
                req.setPaymentMethodId(1L);

                SubscriptionDTO dto = customerSubscriptionService.createSubscription("test@test.com", req);

                assertNotNull(dto);
                assertEquals(1L, dto.getSubscriptionId());
        }

        @Test
        void testCancelSubscription_AtPeriodEnd() {
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));

                customerSubscriptionService.cancelSubscription("test@test.com", true);

                assertTrue(subscription.getCancelAtPeriodEnd());
                verify(subscriptionRepository, times(1)).save(subscription);
        }

        @Test
        void testCancelSubscription_Immediately() {
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));

                customerSubscriptionService.cancelSubscription("test@test.com", false);

                assertEquals(Status.CANCELED, subscription.getStatus());
                assertNotNull(subscription.getCanceledAt());
                verify(subscriptionRepository, times(1)).save(subscription);
        }

        @Test
        void testPauseSubscription() {
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));

                PauseSubscriptionRequest req = new PauseSubscriptionRequest();
                req.setPausedTo("2026-06-18");

                SubscriptionDTO dto = customerSubscriptionService.pauseSubscription("test@test.com", req);

                assertNotNull(dto);
                assertEquals(Status.PAUSED, subscription.getStatus());
                assertEquals(LocalDate.parse("2026-06-18"), subscription.getPausedTo());
        }

        @Test
        void testResumeSubscription() {
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                subscription.setStatus(Status.PAUSED);
                when(subscriptionRepository.findByCustomer_IdAndStatus(1L, Status.PAUSED))
                                .thenReturn(Optional.of(subscription));

                SubscriptionDTO dto = customerSubscriptionService.resumeSubscription("test@test.com");

                assertNotNull(dto);
                assertEquals(Status.ACTIVE, subscription.getStatus());
                assertNull(subscription.getPausedFrom());
                assertNull(subscription.getPausedTo());
        }

        @Test
        void testGetMeteredUsage() {
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));

                when(usageRecordRepository
                                .findBySubscription_IdAndBillingPeriodStartGreaterThanEqualAndBillingPeriodEndLessThanEqual(
                                                eq(1L), any(), any()))
                                .thenReturn(new java.util.ArrayList<>());

                List<UsageRecordDTO> usage = customerSubscriptionService.getMeteredUsage("test@test.com", "2026-05-18",
                                "2026-06-18");

                assertNotNull(usage);
                assertTrue(usage.isEmpty());
        }

        @Test
        void testAddAddOn() {
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));

                AddOn addOn = AddOn.builder().id(1L).name("HD").priceMinor(500L).billingPeriod(BillingPeriod.MONTHLY)
                                .taxMode(TaxMode.EXCLUSIVE).build();
                when(addOnRepository.findById(1L)).thenReturn(Optional.of(addOn));
                when(subscriptionItemRepository.findBySubscription_IdAndAddOn_Id(1L, 1L)).thenReturn(null);

                subscription.setPaymentMethodId(1L);
                com.infy.billing.entity.PaymentMethod pm = new com.infy.billing.entity.PaymentMethod();
                pm.setId(1L);
                pm.setGatewayToken("token123");
                pm.setStatus(Status.ACTIVE);
                when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(pm));

                SubscriptionDTO dto = customerSubscriptionService.addAddOn("test@test.com", 1L);

                assertNotNull(dto);
                verify(subscriptionItemRepository, times(1)).save(any());
        }

        @Test
        void testUpgradeSubscription_Success() {
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));

                Plan newPlan = Plan.builder().id(2L).name("Premium").defaultPriceMinor(2000L)
                                .billingPeriod(BillingPeriod.MONTHLY).taxMode(TaxMode.EXCLUSIVE).build();
                when(planRepository.findById(2L)).thenReturn(Optional.of(newPlan));
                when(subscriptionCouponRepository.findBySubscription_IdAndStatus(anyLong(), any()))
                                .thenReturn(Optional.empty());
                when(subscriptionItemRepository.findBySubscription_Id(anyLong()))
                                .thenReturn(new java.util.ArrayList<>());

                subscription.setPaymentMethodId(1L);
                com.infy.billing.entity.PaymentMethod pm = new com.infy.billing.entity.PaymentMethod();
                pm.setId(1L);
                pm.setGatewayToken("token123");
                pm.setStatus(Status.ACTIVE);
                when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(pm));
                when(mockPaymentGateway.charge(anyString(), anyLong(), anyString())).thenReturn("mock_charge_ref");

                UpgradeSubscriptionRequest req = new UpgradeSubscriptionRequest();
                req.setPlanId(2L);

                SubscriptionDTO dto = customerSubscriptionService.upgradeSubscription("test@test.com", req);

                assertNotNull(dto);
                assertEquals(2L, subscription.getPlan().getId());
                verify(subscriptionRepository, times(1)).save(subscription);
        }

        @Test
        void testUpgradeSubscription_AlreadyOnPlan() {
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));

                when(planRepository.findById(1L)).thenReturn(Optional.of(plan)); // Same plan ID

                UpgradeSubscriptionRequest req = new UpgradeSubscriptionRequest();
                req.setPlanId(1L);

                assertThrows(RuntimeException.class,
                                () -> customerSubscriptionService.upgradeSubscription("test@test.com", req));
        }

        @Test
        void testUpgradeSubscription_PlanNotFound() {
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));

                when(planRepository.findById(2L)).thenReturn(Optional.empty());

                UpgradeSubscriptionRequest req = new UpgradeSubscriptionRequest();
                req.setPlanId(2L);

                assertThrows(RuntimeException.class,
                                () -> customerSubscriptionService.upgradeSubscription("test@test.com", req));
        }

        @Test
        void testUpgradeSubscription_Downgrade() {
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));

                Plan newPlan = Plan.builder().id(2L).name("Cheaper").defaultPriceMinor(500L)
                                .billingPeriod(BillingPeriod.MONTHLY).taxMode(TaxMode.EXCLUSIVE).build();
                when(planRepository.findById(2L)).thenReturn(Optional.of(newPlan));
                when(subscriptionCouponRepository.findBySubscription_IdAndStatus(anyLong(), any()))
                                .thenReturn(Optional.empty());
                when(subscriptionItemRepository.findBySubscription_Id(anyLong()))
                                .thenReturn(new java.util.ArrayList<>());

                UpgradeSubscriptionRequest req = new UpgradeSubscriptionRequest();
                req.setPlanId(2L);

                SubscriptionDTO dto = customerSubscriptionService.upgradeSubscription("test@test.com", req);

                assertNotNull(dto);
                assertEquals(2L, subscription.getPlan().getId());
                verify(customerRepository, times(1)).save(customer);
        }

        @Test
        void testCreateSubscription_AlreadyHasActiveSub() {
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription)); // Has active sub!

                CreateSubscriptionRequest req = new CreateSubscriptionRequest();
                req.setPlanId(1L);

                assertThrows(RuntimeException.class,
                                () -> customerSubscriptionService.createSubscription("test@test.com", req));
        }

        @Test
        void testAddAddOn_IncompatibleBillingPeriod() {
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));

                AddOn addOn = AddOn.builder().id(1L).name("HD").priceMinor(500L).billingPeriod(BillingPeriod.YEARLY)
                                .taxMode(TaxMode.EXCLUSIVE).build(); // Yearly addon!
                // Plan is monthly in setUp!
                when(addOnRepository.findById(1L)).thenReturn(Optional.of(addOn));

                assertThrows(RuntimeException.class, () -> customerSubscriptionService.addAddOn("test@test.com", 1L));
        }

        @Test
        void testAddAddOn_AlreadyActive() {
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));

                AddOn addOn = AddOn.builder().id(1L).name("HD").priceMinor(500L).billingPeriod(BillingPeriod.MONTHLY)
                                .taxMode(TaxMode.EXCLUSIVE).build();
                when(addOnRepository.findById(1L)).thenReturn(Optional.of(addOn));

                com.infy.billing.entity.SubscriptionItem existingItem = new com.infy.billing.entity.SubscriptionItem(); // Already
                                                                                                                        // active!
                when(subscriptionItemRepository.findBySubscription_IdAndAddOn_Id(1L, 1L)).thenReturn(existingItem);

                assertThrows(RuntimeException.class, () -> customerSubscriptionService.addAddOn("test@test.com", 1L));
        }

        @Test
        void testGetCurrentSubscription_NoSubscription() {
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList()); // No active sub

                SubscriptionDTO dto = customerSubscriptionService.getCurrentSubscription("test@test.com");

                assertNull(dto);
        }

        @Test
        void testUpgradeSubscription_Trialing() {
                subscription.setStatus(Status.TRIALING);
                subscription.setTrialEndDate(LocalDate.now().plusDays(7));
                Plan newPlan = Plan.builder().id(2L).name("Premium").defaultPriceMinor(2000L)
                                .billingPeriod(BillingPeriod.MONTHLY).taxMode(TaxMode.EXCLUSIVE).trialDays(14).build();

                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));
                when(planRepository.findById(2L)).thenReturn(Optional.of(newPlan));
                when(subscriptionCouponRepository.findBySubscription_IdAndStatus(anyLong(), any()))
                                .thenReturn(Optional.empty());
                when(subscriptionItemRepository.findBySubscription_Id(anyLong()))
                                .thenReturn(new java.util.ArrayList<>());
                when(subscriptionItemRepository.findBySubscription_IdAndItemType(anyLong(), any())).thenReturn(null);
                when(invoiceRepository.findBySubscription_IdAndStatus(anyLong(), eq(Status.OPEN)))
                                .thenReturn(Optional.empty());
                when(taxRateRepository.findByRegionAndEffectiveToIsNullOrFuture(any(), any()))
                                .thenReturn(Optional.empty());

                UpgradeSubscriptionRequest req = new UpgradeSubscriptionRequest();
                req.setPlanId(2L);

                SubscriptionDTO dto = customerSubscriptionService.upgradeSubscription("test@test.com", req);

                assertNotNull(dto);
                assertEquals(2L, subscription.getPlan().getId());
        }

        @Test
        void testUpgradeSubscription_WithPercentCoupon() {
                Plan newPlan = Plan.builder().id(2L).name("Premium").defaultPriceMinor(2000L)
                                .billingPeriod(BillingPeriod.MONTHLY).taxMode(TaxMode.EXCLUSIVE).build();
                com.infy.billing.entity.Coupon coupon = com.infy.billing.entity.Coupon.builder()
                                .id(1L).code("SAVE10").name("10% Off").type(com.infy.billing.enums.CouponType.PERCENT)
                                .amount(10L).status(Status.ACTIVE).redeemedCount(0).build();
                SubscriptionCoupon sc = new SubscriptionCoupon();
                sc.setCoupon(coupon);

                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));
                when(planRepository.findById(2L)).thenReturn(Optional.of(newPlan));
                when(subscriptionCouponRepository.findBySubscription_IdAndStatus(anyLong(), any()))
                                .thenReturn(Optional.of(sc));
                when(subscriptionItemRepository.findBySubscription_Id(anyLong()))
                                .thenReturn(new java.util.ArrayList<>());

                subscription.setPaymentMethodId(1L);
                com.infy.billing.entity.PaymentMethod pm = new com.infy.billing.entity.PaymentMethod();
                pm.setId(1L);
                pm.setGatewayToken("token123");
                pm.setStatus(Status.ACTIVE);
                when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(pm));
                when(mockPaymentGateway.charge(anyString(), anyLong(), anyString())).thenReturn("mock_ref");
                when(subscriptionItemRepository.findBySubscription_IdAndItemType(anyLong(), any())).thenReturn(null);
                when(taxRateRepository.findByRegionAndEffectiveToIsNullOrFuture(any(), any()))
                                .thenReturn(Optional.empty());

                UpgradeSubscriptionRequest req = new UpgradeSubscriptionRequest();
                req.setPlanId(2L);

                SubscriptionDTO dto = customerSubscriptionService.upgradeSubscription("test@test.com", req);

                assertNotNull(dto);
                verify(invoiceLineItemRepository, atLeast(1)).save(any()); // Discount line saved
        }

        @Test
        void testRemoveAddOn_Success() {
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));

                com.infy.billing.entity.SubscriptionItem item = new com.infy.billing.entity.SubscriptionItem();
                item.setId(1L);
                when(subscriptionItemRepository.findBySubscription_IdAndAddOn_Id(1L, 1L)).thenReturn(item);

                customerSubscriptionService.removeAddOn("test@test.com", 1L);

                verify(subscriptionItemRepository, times(1)).delete(item);
        }

        @Test
        void testRemoveAddOn_NotFound() {
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));

                when(subscriptionItemRepository.findBySubscription_IdAndAddOn_Id(1L, 999L)).thenReturn(null);

                // Should not throw, just no-op
                customerSubscriptionService.removeAddOn("test@test.com", 999L);

                verify(subscriptionItemRepository, never()).delete(any());
        }

        @Test
        void testGetMeteredUsage_WithData() {
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));

                com.infy.billing.entity.MeteredComponent mc = com.infy.billing.entity.MeteredComponent.builder()
                        .id(1L).name("Storage").unitName("GB").pricePerUnitMinor(50L).build();
                com.infy.billing.entity.UsageRecord ur = new com.infy.billing.entity.UsageRecord();
                ur.setId(1L);
                ur.setComponent(mc);
                ur.setQuantity(10L);
                ur.setRecordedAt(java.time.LocalDateTime.now());
                ur.setBillingPeriodStart(LocalDate.now().minusDays(10));
                ur.setBillingPeriodEnd(LocalDate.now());

                when(usageRecordRepository
                                .findBySubscription_IdAndBillingPeriodStartGreaterThanEqualAndBillingPeriodEndLessThanEqual(
                                                eq(1L), any(), any()))
                                .thenReturn(Arrays.asList(ur));
                when(meteredComponentRepository.findById(1L)).thenReturn(Optional.of(mc));

                List<UsageRecordDTO> usage = customerSubscriptionService.getMeteredUsage("test@test.com", "2023-01-01", "2023-01-10");

                assertNotNull(usage);
                assertEquals(1, usage.size());
                assertEquals("Storage", usage.get(0).getComponentName());
                assertEquals(10L, usage.get(0).getQuantity());
        }

        @Test
        void testGetCurrentSubscription_WithAddOn() {
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));

                AddOn addon = AddOn.builder()
                        .id(1L).name("Support").priceMinor(500L).build();
                SubscriptionItem item = new SubscriptionItem();
                item.setId(1L);
                item.setItemType(ItemType.ADDON);
                item.setAddOn(addon);
                item.setSubscription(subscription);
                item.setQuantity(1);
                item.setUnitPriceMinor(500L);

                when(subscriptionItemRepository.findBySubscription_Id(1L)).thenReturn(Arrays.asList(item));
                when(subscriptionCouponRepository.findBySubscription_IdAndStatus(anyLong(), any()))
                        .thenReturn(Optional.empty());
                when(addOnRepository.findById(1L)).thenReturn(Optional.of(addon));

                SubscriptionDTO dto = customerSubscriptionService.getCurrentSubscription("test@test.com");

                assertNotNull(dto);
                assertEquals(1, dto.getAddOns().size());
                assertEquals("Support", dto.getAddOns().get(0).getAddonName());
        }

        @Test
        void testAddAddOn_WithCustomerCredit() {
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));
                
                AddOn addon = AddOn.builder().id(2L).name("Support").priceMinor(1000L)
                                .billingPeriod(BillingPeriod.MONTHLY).build();
                when(addOnRepository.findById(2L)).thenReturn(Optional.of(addon));
                when(subscriptionItemRepository.findBySubscription_IdAndAddOn_Id(anyLong(), anyLong()))
                                .thenReturn(null);
                
                subscription.setPaymentMethodId(1L);
                customer.setCreditBalanceMinor(200L); // Customer has partial credit
                
                // (No OPEN invoice mock needed, as it generates a new prorated invoice directly)
                
                PaymentMethod pm = new PaymentMethod();
                pm.setId(1L);
                pm.setGatewayToken("token123");
                pm.setStatus(Status.ACTIVE);
                when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(pm));
                when(mockPaymentGateway.charge(anyString(), anyLong(), any())).thenReturn("mock_ref");
                
                customerSubscriptionService.addAddOn("test@test.com", 2L);
                
                verify(mockPaymentGateway, times(1)).charge(anyString(), eq(800L), any()); // 1000 - 200 credit
                assertEquals(0L, customer.getCreditBalanceMinor()); // Credit used up
        }

        @Test
        void testUpgradeSubscription_YearlyPlan() {
                Plan yearlyPlan = Plan.builder().id(3L).name("Pro Yearly").defaultPriceMinor(12000L)
                                .billingPeriod(BillingPeriod.YEARLY).taxMode(com.infy.billing.enums.TaxMode.EXCLUSIVE).build();
                subscription.getPlan().setBillingPeriod(BillingPeriod.YEARLY);
                subscription.setCurrentPeriodStart(LocalDate.now().minusDays(100));
                subscription.setCurrentPeriodEnd(LocalDate.now().plusDays(265));
                
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));
                when(planRepository.findById(3L)).thenReturn(Optional.of(yearlyPlan));
                
                subscription.setPaymentMethodId(1L);
                PaymentMethod pm = new PaymentMethod();
                pm.setId(1L);
                pm.setGatewayToken("token123");
                pm.setStatus(Status.ACTIVE);
                when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(pm));
                when(mockPaymentGateway.charge(anyString(), anyLong(), any())).thenReturn("mock_ref");
                when(subscriptionItemRepository.findBySubscription_Id(anyLong()))
                                .thenReturn(new java.util.ArrayList<>());
                                
                UpgradeSubscriptionRequest req = new UpgradeSubscriptionRequest();
                req.setPlanId(3L);
                
                customerSubscriptionService.upgradeSubscription("test@test.com", req);
                
                verify(mockPaymentGateway, times(1)).charge(anyString(), anyLong(), any());
        }

        @Test
        void testUpgradeSubscription_ZeroDifference() {
                Plan samePricePlan = Plan.builder().id(4L).name("Alt Plan").defaultPriceMinor(0L) // Set diff to 0
                                .billingPeriod(BillingPeriod.MONTHLY).taxMode(com.infy.billing.enums.TaxMode.EXCLUSIVE).build();
                subscription.getPlan().setDefaultPriceMinor(0L); // Proration will be 0
                
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));
                when(planRepository.findById(4L)).thenReturn(Optional.of(samePricePlan));
                
                UpgradeSubscriptionRequest req = new UpgradeSubscriptionRequest();
                req.setPlanId(4L);
                
                customerSubscriptionService.upgradeSubscription("test@test.com", req);
                
                verify(mockPaymentGateway, never()).charge(anyString(), anyLong(), anyString());
        }

        @Test
        void testUpgradeSubscription_TrialingWithOpenInvoice() {
                subscription.setStatus(Status.TRIALING);
                subscription.setTrialEndDate(LocalDate.now().plusDays(7));
                Plan newPlan = Plan.builder().id(2L).name("Premium").defaultPriceMinor(2000L)
                                .billingPeriod(BillingPeriod.MONTHLY).taxMode(TaxMode.EXCLUSIVE).trialDays(14).build();

                com.infy.billing.entity.Invoice openInvoice = com.infy.billing.entity.Invoice.builder()
                                .id(1L).subtotalMinor(1000L).taxMinor(0L).totalMinor(1000L).balanceMinor(1000L)
                                .discountMinor(0L).build();

                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));
                when(planRepository.findById(2L)).thenReturn(Optional.of(newPlan));
                when(subscriptionCouponRepository.findBySubscription_IdAndStatus(anyLong(), any()))
                                .thenReturn(Optional.empty());
                when(subscriptionItemRepository.findBySubscription_Id(anyLong()))
                                .thenReturn(new java.util.ArrayList<>());
                when(subscriptionItemRepository.findBySubscription_IdAndItemType(anyLong(), any())).thenReturn(null);
                when(invoiceRepository.findBySubscription_IdAndStatus(anyLong(), eq(Status.OPEN)))
                                .thenReturn(Optional.of(openInvoice));
                when(invoiceLineItemRepository.findByInvoice_Id(anyLong()))
                                .thenReturn(new java.util.ArrayList<>());
                when(taxRateRepository.findByRegionAndEffectiveToIsNullOrFuture(any(), any()))
                                .thenReturn(Optional.empty());

                UpgradeSubscriptionRequest req = new UpgradeSubscriptionRequest();
                req.setPlanId(2L);

                SubscriptionDTO dto = customerSubscriptionService.upgradeSubscription("test@test.com", req);
                assertNotNull(dto);
                verify(invoiceLineItemRepository).deleteAll(anyList());
                verify(invoiceRepository, atLeast(1)).save(any());
        }

        @Test
        void testUpgradeSubscription_TrialingWithFixedCouponAndAddons() {
                subscription.setStatus(Status.TRIALING);
                subscription.setTrialEndDate(LocalDate.now().plusDays(7));
                Plan newPlan = Plan.builder().id(2L).name("Premium").defaultPriceMinor(2000L)
                                .billingPeriod(BillingPeriod.MONTHLY).taxMode(TaxMode.EXCLUSIVE).trialDays(14).build();

                com.infy.billing.entity.Coupon coupon = com.infy.billing.entity.Coupon.builder()
                                .id(1L).code("FLAT200").name("Flat 200 Off")
                                .type(com.infy.billing.enums.CouponType.FIXED).amount(200L)
                                .status(Status.ACTIVE).redeemedCount(0).build();
                SubscriptionCoupon sc = new SubscriptionCoupon();
                sc.setCoupon(coupon);

                AddOn addon = AddOn.builder().id(1L).name("Support").priceMinor(500L).build();
                SubscriptionItem addonItem = new SubscriptionItem();
                addonItem.setItemType(ItemType.ADDON);
                addonItem.setAddOn(addon);
                addonItem.setUnitPriceMinor(500L);
                addonItem.setQuantity(1);

                com.infy.billing.entity.Invoice openInvoice = com.infy.billing.entity.Invoice.builder()
                                .id(1L).subtotalMinor(1000L).taxMinor(0L).totalMinor(1000L).balanceMinor(1000L)
                                .discountMinor(0L).build();

                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));
                when(planRepository.findById(2L)).thenReturn(Optional.of(newPlan));
                when(subscriptionCouponRepository.findBySubscription_IdAndStatus(anyLong(), any()))
                                .thenReturn(Optional.of(sc));
                when(subscriptionItemRepository.findBySubscription_Id(anyLong()))
                                .thenReturn(Arrays.asList(addonItem));
                when(subscriptionItemRepository.findBySubscription_IdAndItemType(anyLong(), any())).thenReturn(null);
                when(invoiceRepository.findBySubscription_IdAndStatus(anyLong(), eq(Status.OPEN)))
                                .thenReturn(Optional.of(openInvoice));
                when(invoiceLineItemRepository.findByInvoice_Id(anyLong()))
                                .thenReturn(new java.util.ArrayList<>());
                when(taxRateRepository.findByRegionAndEffectiveToIsNullOrFuture(any(), any()))
                                .thenReturn(Optional.empty());

                UpgradeSubscriptionRequest req = new UpgradeSubscriptionRequest();
                req.setPlanId(2L);

                SubscriptionDTO dto = customerSubscriptionService.upgradeSubscription("test@test.com", req);
                assertNotNull(dto);
                // Discount line + addon line + plan line should be saved
                verify(invoiceLineItemRepository, atLeast(3)).save(any());
        }

        @Test
        void testUpgradeSubscription_TrialingNewTrialDaysZero() {
                subscription.setStatus(Status.TRIALING);
                subscription.setStartDate(LocalDate.now().minusDays(20));
                subscription.setTrialEndDate(LocalDate.now().plusDays(1));
                // New plan has only 5 trial days, but 20 days already used -> newTrialDays = max(0, 5-20) = 0
                Plan newPlan = Plan.builder().id(2L).name("Premium").defaultPriceMinor(2000L)
                                .billingPeriod(BillingPeriod.MONTHLY).taxMode(TaxMode.EXCLUSIVE).trialDays(5).build();

                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));
                when(planRepository.findById(2L)).thenReturn(Optional.of(newPlan));
                when(subscriptionCouponRepository.findBySubscription_IdAndStatus(anyLong(), any()))
                                .thenReturn(Optional.empty());
                when(subscriptionItemRepository.findBySubscription_Id(anyLong()))
                                .thenReturn(new java.util.ArrayList<>());
                when(subscriptionItemRepository.findBySubscription_IdAndItemType(anyLong(), any())).thenReturn(null);
                when(invoiceRepository.findBySubscription_IdAndStatus(anyLong(), eq(Status.OPEN)))
                                .thenReturn(Optional.empty());
                when(taxRateRepository.findByRegionAndEffectiveToIsNullOrFuture(any(), any()))
                                .thenReturn(Optional.empty());

                UpgradeSubscriptionRequest req = new UpgradeSubscriptionRequest();
                req.setPlanId(2L);

                customerSubscriptionService.upgradeSubscription("test@test.com", req);
                assertEquals(Status.ACTIVE, subscription.getStatus()); // newTrialDays==0 -> ACTIVE
        }

        @Test
        void testUpgradeSubscription_ActiveWithInclusiveTax() {
                com.infy.billing.entity.TaxRate taxRate = new com.infy.billing.entity.TaxRate();
                taxRate.setRatePercent(java.math.BigDecimal.TEN); // 10%

                Plan newPlan = Plan.builder().id(2L).name("Premium").defaultPriceMinor(2200L)
                                .billingPeriod(BillingPeriod.MONTHLY).taxMode(TaxMode.INCLUSIVE).build();
                plan.setTaxMode(TaxMode.INCLUSIVE);

                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));
                when(planRepository.findById(2L)).thenReturn(Optional.of(newPlan));
                when(subscriptionCouponRepository.findBySubscription_IdAndStatus(anyLong(), any()))
                                .thenReturn(Optional.empty());
                when(subscriptionItemRepository.findBySubscription_Id(anyLong()))
                                .thenReturn(new java.util.ArrayList<>());
                when(subscriptionItemRepository.findBySubscription_IdAndItemType(anyLong(), any())).thenReturn(null);
                when(taxRateRepository.findByRegionAndEffectiveToIsNullOrFuture(any(), any()))
                                .thenReturn(Optional.of(taxRate));

                subscription.setPaymentMethodId(1L);
                PaymentMethod pm = new PaymentMethod();
                pm.setId(1L); pm.setGatewayToken("tok"); pm.setStatus(Status.ACTIVE);
                when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(pm));
                when(mockPaymentGateway.charge(anyString(), anyLong(), any())).thenReturn("ref");

                UpgradeSubscriptionRequest req = new UpgradeSubscriptionRequest();
                req.setPlanId(2L);

                SubscriptionDTO dto = customerSubscriptionService.upgradeSubscription("test@test.com", req);
                assertNotNull(dto);
        }

        @Test
        void testUpgradeSubscription_ActiveWithAddonsAndTax() {
                com.infy.billing.entity.TaxRate taxRate = new com.infy.billing.entity.TaxRate();
                taxRate.setRatePercent(new java.math.BigDecimal("18")); // 18%

                Plan newPlan = Plan.builder().id(2L).name("Premium").defaultPriceMinor(2000L)
                                .billingPeriod(BillingPeriod.MONTHLY).taxMode(TaxMode.EXCLUSIVE).build();

                AddOn addon = AddOn.builder().id(1L).name("Support").priceMinor(500L).build();
                SubscriptionItem addonItem = new SubscriptionItem();
                addonItem.setItemType(ItemType.ADDON);
                addonItem.setAddOn(addon);
                addonItem.setUnitPriceMinor(500L);
                addonItem.setQuantity(1);

                SubscriptionItem planSI = new SubscriptionItem();
                planSI.setId(10L);
                planSI.setItemType(ItemType.PLAN);
                planSI.setPlan(plan);
                planSI.setSubscription(subscription);

                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));
                when(planRepository.findById(2L)).thenReturn(Optional.of(newPlan));
                when(subscriptionCouponRepository.findBySubscription_IdAndStatus(anyLong(), any()))
                                .thenReturn(Optional.empty());
                when(subscriptionItemRepository.findBySubscription_Id(anyLong()))
                                .thenReturn(Arrays.asList(addonItem));
                when(subscriptionItemRepository.findBySubscription_IdAndItemType(anyLong(), eq(ItemType.PLAN)))
                                .thenReturn(planSI); // existing plan item
                when(taxRateRepository.findByRegionAndEffectiveToIsNullOrFuture(any(), any()))
                                .thenReturn(Optional.of(taxRate));

                subscription.setPaymentMethodId(1L);
                PaymentMethod pm = new PaymentMethod();
                pm.setId(1L); pm.setGatewayToken("tok"); pm.setStatus(Status.ACTIVE);
                when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(pm));
                when(mockPaymentGateway.charge(anyString(), anyLong(), any())).thenReturn("ref");

                UpgradeSubscriptionRequest req = new UpgradeSubscriptionRequest();
                req.setPlanId(2L);

                SubscriptionDTO dto = customerSubscriptionService.upgradeSubscription("test@test.com", req);
                assertNotNull(dto);
                // Tax line + addon line + plan line + credit line saved
                verify(invoiceLineItemRepository, atLeast(4)).save(any());
        }

        @Test
        void testAddAddOn_TrialingWithOpenInvoice() {
                subscription.setStatus(Status.TRIALING);
                AddOn addOn = AddOn.builder().id(1L).name("HD").priceMinor(500L)
                                .billingPeriod(BillingPeriod.MONTHLY).taxMode(TaxMode.EXCLUSIVE).build();

                com.infy.billing.entity.Invoice openInvoice = com.infy.billing.entity.Invoice.builder()
                                .id(1L).subtotalMinor(1000L).taxMinor(100L).totalMinor(1100L).balanceMinor(1100L)
                                .discountMinor(0L).build();

                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));
                when(addOnRepository.findById(1L)).thenReturn(Optional.of(addOn));
                when(subscriptionItemRepository.findBySubscription_IdAndAddOn_Id(1L, 1L)).thenReturn(null);
                when(invoiceRepository.findBySubscription_IdAndStatus(anyLong(), eq(Status.OPEN)))
                                .thenReturn(Optional.of(openInvoice));
                when(taxRateRepository.findByRegionAndEffectiveToIsNullOrFuture(any(), any()))
                                .thenReturn(Optional.empty());

                SubscriptionDTO dto = customerSubscriptionService.addAddOn("test@test.com", 1L);
                assertNotNull(dto);
                // Addon line + tax line saved
                verify(invoiceLineItemRepository, atLeast(2)).save(any());
                verify(invoiceRepository, atLeast(1)).save(any());
        }

        @Test
        void testAddAddOn_ActiveCreditCoversAll() {
                customer.setCreditBalanceMinor(10000L); // Large credit

                AddOn addOn = AddOn.builder().id(1L).name("HD").priceMinor(500L)
                                .billingPeriod(BillingPeriod.MONTHLY).taxMode(TaxMode.EXCLUSIVE).build();

                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));
                when(addOnRepository.findById(1L)).thenReturn(Optional.of(addOn));
                when(subscriptionItemRepository.findBySubscription_IdAndAddOn_Id(1L, 1L)).thenReturn(null);
                when(taxRateRepository.findByRegionAndEffectiveToIsNullOrFuture(any(), any()))
                                .thenReturn(Optional.empty());

                customerSubscriptionService.addAddOn("test@test.com", 1L);

                // Credit covers everything — no gateway charge
                verify(mockPaymentGateway, never()).charge(anyString(), anyLong(), any());
        }

        @Test
        void testGetCurrentSubscription_WithMeteredComponent() {
                com.infy.billing.entity.MeteredComponent comp = com.infy.billing.entity.MeteredComponent.builder()
                                .id(1L).name("API Calls").unitName("call").pricePerUnitMinor(10L).freeTierQuantity(100L).build();

                SubscriptionItem meteredItem = new SubscriptionItem();
                meteredItem.setId(2L);
                meteredItem.setItemType(ItemType.METERED);
                meteredItem.setComponent(comp);
                meteredItem.setSubscription(subscription);
                meteredItem.setQuantity(1);
                meteredItem.setUnitPriceMinor(10L);

                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));
                when(subscriptionItemRepository.findBySubscription_Id(1L)).thenReturn(Arrays.asList(meteredItem));
                when(subscriptionCouponRepository.findBySubscription_IdAndStatus(anyLong(), any()))
                                .thenReturn(Optional.empty());
                when(meteredComponentRepository.findById(1L)).thenReturn(Optional.of(comp));
                when(usageRecordRepository.sumQuantityBySubscription_IdAndComponent_Id(1L, 1L)).thenReturn(150L);
                when(taxRateRepository.findByRegionAndEffectiveToIsNullOrFuture(any(), any()))
                                .thenReturn(Optional.empty());

                SubscriptionDTO dto = customerSubscriptionService.getCurrentSubscription("test@test.com");

                assertNotNull(dto);
                assertEquals(1, dto.getMeteredUsage().size());
                assertEquals("API Calls", dto.getMeteredUsage().get(0).getComponentName());
                assertEquals(150L, dto.getMeteredUsage().get(0).getQuantityUsed());
                // billable = max(0, 150-100)=50, cost = 50*10=500
                assertEquals(500L, dto.getMeteredUsage().get(0).getCostMinor());
        }

        @Test
        void testGetCurrentSubscription_NullBillingPeriod() {
                plan.setBillingPeriod(null); // null billing period -> default "MONTHLY"

                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));
                when(subscriptionItemRepository.findBySubscription_Id(1L))
                                .thenReturn(new java.util.ArrayList<>());
                when(subscriptionCouponRepository.findBySubscription_IdAndStatus(anyLong(), any()))
                                .thenReturn(Optional.empty());
                when(taxRateRepository.findByRegionAndEffectiveToIsNullOrFuture(any(), any()))
                                .thenReturn(Optional.empty());

                SubscriptionDTO dto = customerSubscriptionService.getCurrentSubscription("test@test.com");

                assertNotNull(dto);
                assertEquals("MONTHLY", dto.getBillingPeriod());
        }

        @Test
        void testGetCurrentSubscription_WithFixedCoupon() {
                com.infy.billing.entity.Coupon coupon = com.infy.billing.entity.Coupon.builder()
                                .id(1L).code("FLAT500").name("Flat 500 Off")
                                .type(com.infy.billing.enums.CouponType.FIXED).amount(500L)
                                .status(Status.ACTIVE).redeemedCount(0).build();
                SubscriptionCoupon sc = new SubscriptionCoupon();
                sc.setCoupon(coupon);

                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));
                when(subscriptionItemRepository.findBySubscription_Id(1L))
                                .thenReturn(new java.util.ArrayList<>());
                when(subscriptionCouponRepository.findBySubscription_IdAndStatus(anyLong(), any()))
                                .thenReturn(Optional.of(sc));
                when(taxRateRepository.findByRegionAndEffectiveToIsNullOrFuture(any(), any()))
                                .thenReturn(Optional.empty());

                SubscriptionDTO dto = customerSubscriptionService.getCurrentSubscription("test@test.com");

                assertNotNull(dto);
                assertEquals(500L, dto.getDiscountMinor());
        }

        @Test
        void testGetCurrentSubscription_NegativeSubtotalClampedToZero() {
                // Discount (2000) > plan price (1000) -> subtotal clamped to 0
                com.infy.billing.entity.Coupon coupon = com.infy.billing.entity.Coupon.builder()
                                .id(1L).code("BIG").name("Big Discount")
                                .type(com.infy.billing.enums.CouponType.FIXED).amount(2000L)
                                .status(Status.ACTIVE).redeemedCount(0).build();
                SubscriptionCoupon sc = new SubscriptionCoupon();
                sc.setCoupon(coupon);

                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));
                when(subscriptionItemRepository.findBySubscription_Id(1L))
                                .thenReturn(new java.util.ArrayList<>());
                when(subscriptionCouponRepository.findBySubscription_IdAndStatus(anyLong(), any()))
                                .thenReturn(Optional.of(sc));
                when(taxRateRepository.findByRegionAndEffectiveToIsNullOrFuture(any(), any()))
                                .thenReturn(Optional.empty());

                SubscriptionDTO dto = customerSubscriptionService.getCurrentSubscription("test@test.com");

                assertNotNull(dto);
                assertEquals(0L, dto.getTotalDueMinor()); // subtotal clamped to 0
        }

        @Test
        void testGetCurrentSubscription_InclusiveTaxWithRate() {
                plan.setTaxMode(TaxMode.INCLUSIVE);
                com.infy.billing.entity.TaxRate taxRate = new com.infy.billing.entity.TaxRate();
                taxRate.setRatePercent(java.math.BigDecimal.TEN); // 10%

                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));
                when(subscriptionItemRepository.findBySubscription_Id(1L))
                                .thenReturn(new java.util.ArrayList<>());
                when(subscriptionCouponRepository.findBySubscription_IdAndStatus(anyLong(), any()))
                                .thenReturn(Optional.empty());
                when(taxRateRepository.findByRegionAndEffectiveToIsNullOrFuture(any(), any()))
                                .thenReturn(Optional.of(taxRate));

                SubscriptionDTO dto = customerSubscriptionService.getCurrentSubscription("test@test.com");

                assertNotNull(dto);
                // INCLUSIVE: totalDue = subtotal (1000), not subtotal + tax
                assertEquals(1000L, dto.getTotalDueMinor());
        }

        @Test
        void testAddAddOn_InclusiveTaxMode() {
                AddOn addOn = AddOn.builder().id(1L).name("HD").priceMinor(500L)
                                .billingPeriod(BillingPeriod.MONTHLY).taxMode(TaxMode.INCLUSIVE).build();

                com.infy.billing.entity.TaxRate taxRate = new com.infy.billing.entity.TaxRate();
                taxRate.setRatePercent(java.math.BigDecimal.TEN);

                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));
                when(addOnRepository.findById(1L)).thenReturn(Optional.of(addOn));
                when(subscriptionItemRepository.findBySubscription_IdAndAddOn_Id(1L, 1L)).thenReturn(null);
                when(taxRateRepository.findByRegionAndEffectiveToIsNullOrFuture(any(), any()))
                                .thenReturn(Optional.of(taxRate));

                subscription.setPaymentMethodId(1L);
                PaymentMethod pm = new PaymentMethod();
                pm.setId(1L); pm.setGatewayToken("tok"); pm.setStatus(Status.ACTIVE);
                when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(pm));
                when(mockPaymentGateway.charge(anyString(), anyLong(), any())).thenReturn("ref");

                SubscriptionDTO dto = customerSubscriptionService.addAddOn("test@test.com", 1L);
                assertNotNull(dto);
        }

        @Test
        void testGetMeteredUsage_WithDefaultDates() {
                // No startDate/endDate provided — uses subscription period dates
                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));
                when(usageRecordRepository
                                .findBySubscription_IdAndBillingPeriodStartGreaterThanEqualAndBillingPeriodEndLessThanEqual(
                                                eq(1L), any(), any()))
                                .thenReturn(new java.util.ArrayList<>());

                List<UsageRecordDTO> usage = customerSubscriptionService.getMeteredUsage("test@test.com", null, null);

                assertNotNull(usage);
                assertTrue(usage.isEmpty());
        }

        @Test
        void testGetCurrentSubscription_WithMeteredComponent_ComponentNotFound() {
                SubscriptionItem meteredItem = new SubscriptionItem();
                meteredItem.setId(2L);
                meteredItem.setItemType(ItemType.METERED);
                com.infy.billing.entity.MeteredComponent comp = new com.infy.billing.entity.MeteredComponent();
                comp.setId(1L);
                meteredItem.setComponent(comp);
                meteredItem.setSubscription(subscription);

                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));
                when(subscriptionItemRepository.findBySubscription_Id(1L)).thenReturn(Arrays.asList(meteredItem));
                when(subscriptionCouponRepository.findBySubscription_IdAndStatus(anyLong(), any()))
                                .thenReturn(Optional.empty());
                // Return empty to trigger the "Unknown" and default branches
                when(meteredComponentRepository.findById(1L)).thenReturn(Optional.empty());
                when(taxRateRepository.findByRegionAndEffectiveToIsNullOrFuture(any(), any()))
                                .thenReturn(Optional.empty());

                SubscriptionDTO dto = customerSubscriptionService.getCurrentSubscription("test@test.com");

                assertNotNull(dto);
                assertEquals(1, dto.getMeteredUsage().size());
                assertEquals("Unknown", dto.getMeteredUsage().get(0).getComponentName());
                assertEquals("unit", dto.getMeteredUsage().get(0).getUnitName());
        }

        @Test
        void testUpgradeSubscription_ActiveEdgeDates() {
                // Set end date to today to trigger remainingDays <= 0
                subscription.setCurrentPeriodEnd(LocalDate.now());
                subscription.setCurrentPeriodStart(LocalDate.now()); // totalDays = 0

                Plan newPlan = Plan.builder().id(2L).name("Premium").defaultPriceMinor(2000L)
                                .billingPeriod(BillingPeriod.MONTHLY).taxMode(TaxMode.EXCLUSIVE).build();

                when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
                when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
                when(subscriptionRepository.findByCustomer_IdAndStatusIn(eq(1L), anyList()))
                                .thenReturn(Arrays.asList(subscription));
                when(planRepository.findById(2L)).thenReturn(Optional.of(newPlan));
                when(subscriptionCouponRepository.findBySubscription_IdAndStatus(anyLong(), any()))
                                .thenReturn(Optional.empty());
                when(subscriptionItemRepository.findBySubscription_Id(anyLong()))
                                .thenReturn(new java.util.ArrayList<>());
                when(subscriptionItemRepository.findBySubscription_IdAndItemType(anyLong(), any())).thenReturn(null);

                subscription.setPaymentMethodId(1L);
                PaymentMethod pm = new PaymentMethod();
                pm.setId(1L); pm.setGatewayToken("tok"); pm.setStatus(Status.ACTIVE);
                when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(pm));
                when(mockPaymentGateway.charge(anyString(), anyLong(), any())).thenReturn("ref");

                UpgradeSubscriptionRequest req = new UpgradeSubscriptionRequest();
                req.setPlanId(2L);

                SubscriptionDTO dto = customerSubscriptionService.upgradeSubscription("test@test.com", req);
                assertNotNull(dto);
        }
}
