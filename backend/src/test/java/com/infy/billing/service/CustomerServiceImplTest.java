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

import com.infy.billing.dto.customer.CustomerProfileDTO;
import com.infy.billing.dto.customer.PlanDTO;
import com.infy.billing.dto.customer.AddOnDTO;
import com.infy.billing.entity.Customer;
import com.infy.billing.entity.User;
import com.infy.billing.entity.Plan;
import com.infy.billing.entity.AddOn;
import com.infy.billing.entity.Subscription;
import com.infy.billing.enums.BillingPeriod;
import com.infy.billing.enums.Status;
import com.infy.billing.repository.CustomerRepository;
import com.infy.billing.repository.UserRepository;
import com.infy.billing.repository.PlanRepository;
import com.infy.billing.repository.AddOnRepository;
import com.infy.billing.repository.SubscriptionRepository;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private UserRepository userRepository;
    @Mock private PlanRepository planRepository;
    @Mock private AddOnRepository addOnRepository;
    @Mock private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer;
    private User user;
    private Plan plan;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@test.com").fullName("Test User").build();
        customer = Customer.builder().id(1L).user(user).status(Status.ACTIVE).build();
        plan = Plan.builder()
                .id(1L)
                .name("Basic")
                .status(Status.ACTIVE)
                .effectiveFrom(LocalDate.now())
                .billingPeriod(BillingPeriod.MONTHLY)
                .build();
    }

    @Test
    void testGetProfile() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));

        CustomerProfileDTO dto = customerService.getProfile("test@test.com");

        assertNotNull(dto);
        assertEquals("Test User", dto.getFullName());
    }

    @Test
    void testUpdateProfile() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));

        CustomerProfileDTO updateDto = new CustomerProfileDTO();
        updateDto.setPhone("1234567890");

        CustomerProfileDTO result = customerService.updateProfile("test@test.com", updateDto);

        assertNotNull(result);
        assertEquals("1234567890", customer.getPhone());
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    void testGetAvailablePlans() {
        when(planRepository.findByStatus(Status.ACTIVE)).thenReturn(Arrays.asList(plan));

        List<PlanDTO> plans = customerService.getAvailablePlans();

        assertEquals(1, plans.size());
        assertEquals("Basic", plans.get(0).getName());
    }

    @Test
    void testGetFeaturedPlans() {
        when(planRepository.findAllById(anyList())).thenReturn(Arrays.asList(plan));

        List<PlanDTO> plans = customerService.getFeaturedPlans();

        assertEquals(1, plans.size());
    }

    @Test
    void testGetAvailableAddOns() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
        
        Subscription sub = Subscription.builder().id(1L).plan(plan).build();
        when(subscriptionRepository.findByCustomer_IdAndStatusIn(anyLong(), anyList()))
                .thenReturn(Arrays.asList(sub));

        AddOn addOn = AddOn.builder()
                .id(1L)
                .name("Storage")
                .billingPeriod(BillingPeriod.MONTHLY)
                .status(Status.ACTIVE)
                .build();
        
        when(addOnRepository.findByStatus(Status.ACTIVE)).thenReturn(Arrays.asList(addOn));

        List<AddOnDTO> addOns = customerService.getAvailableAddOns("test@test.com");

        assertEquals(1, addOns.size());
        assertEquals("Storage", addOns.get(0).getName());
    }

    @Test
    void testGetFeaturedPlans_InactivePlan() {
        Plan inactivePlan = Plan.builder()
                .id(2L)
                .name("Old")
                .status(Status.INACTIVE)
                .build();
        when(planRepository.findAllById(anyList())).thenReturn(Arrays.asList(plan, inactivePlan));

        List<PlanDTO> plans = customerService.getFeaturedPlans();

        assertEquals(1, plans.size());
        assertEquals("Basic", plans.get(0).getName());
    }

    @Test
    void testGetAvailableAddOns_NoSubscription() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));
        when(subscriptionRepository.findByCustomer_IdAndStatusIn(anyLong(), anyList()))
                .thenReturn(Arrays.asList());

        AddOn addOn = AddOn.builder()
                .id(1L)
                .name("Storage")
                .billingPeriod(BillingPeriod.MONTHLY)
                .status(Status.ACTIVE)
                .build();
        
        when(addOnRepository.findByStatus(Status.ACTIVE)).thenReturn(Arrays.asList(addOn));

        List<AddOnDTO> addOns = customerService.getAvailableAddOns("test@test.com");

        assertEquals(1, addOns.size());
        assertEquals("Storage", addOns.get(0).getName());
    }

    @Test
    void testGetProfile_UserNotFound() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> customerService.getProfile("test@test.com"));
    }

    @Test
    void testGetProfile_CustomerNotFound() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> customerService.getProfile("test@test.com"));
    }

    @Test
    void testGetAvailablePlans_EmptyList() {
        when(planRepository.findByStatus(Status.ACTIVE)).thenReturn(Arrays.asList());

        List<PlanDTO> plans = customerService.getAvailablePlans();

        assertNotNull(plans);
        assertTrue(plans.isEmpty());
    }

    @Test
    void testGetAllActivePlans() {
        when(planRepository.findByStatus(Status.ACTIVE)).thenReturn(Arrays.asList(plan));

        List<PlanDTO> plans = customerService.getAllActivePlans();

        assertEquals(1, plans.size());
        assertEquals("Basic", plans.get(0).getName());
    }

    @Test
    void testGetAvailablePlans_WithEffectiveTo() {
        plan.setEffectiveTo(LocalDate.now().plusDays(30));
        when(planRepository.findByStatus(Status.ACTIVE)).thenReturn(Arrays.asList(plan));

        List<PlanDTO> plans = customerService.getAvailablePlans();

        assertEquals(1, plans.size());
        assertNotNull(plans.get(0).getEffectiveTo());
    }

    @Test
    void testGetAvailableAddOns_MismatchedBillingPeriod() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(customerRepository.findByUser_Id(1L)).thenReturn(Optional.of(customer));

        Subscription sub = Subscription.builder().id(1L).plan(plan).build(); // MONTHLY plan
        when(subscriptionRepository.findByCustomer_IdAndStatusIn(anyLong(), anyList()))
                .thenReturn(Arrays.asList(sub));

        AddOn yearlyAddOn = AddOn.builder()
                .id(1L)
                .name("Yearly Storage")
                .billingPeriod(BillingPeriod.YEARLY)
                .status(Status.ACTIVE)
                .build();
        
        when(addOnRepository.findByStatus(Status.ACTIVE)).thenReturn(Arrays.asList(yearlyAddOn));

        List<AddOnDTO> addOns = customerService.getAvailableAddOns("test@test.com");

        // YEARLY addon should be filtered out for MONTHLY subscription
        assertEquals(0, addOns.size());
    }
}
