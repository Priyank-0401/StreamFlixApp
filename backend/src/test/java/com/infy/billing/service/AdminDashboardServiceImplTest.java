package com.infy.billing.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.infy.billing.dto.admin.*;
import com.infy.billing.entity.*;
import com.infy.billing.enums.Status;
import com.infy.billing.enums.UserRole;
import com.infy.billing.enums.BillingPeriod;
import com.infy.billing.enums.TaxMode;
import com.infy.billing.enums.CouponType;
import com.infy.billing.repository.*;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private PlanRepository planRepository;
    @Mock
    private PriceBookEntryRepository priceBookEntryRepository;
    @Mock
    private AddOnRepository addOnRepository;
    @Mock
    private MeteredComponentRepository meteredComponentRepository;
    @Mock
    private TaxRateRepository taxRateRepository;
    @Mock
    private CouponRepository couponRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminDashboardServiceImpl adminDashboardService;

    private Product product;
    private Plan plan;
    private AddOn addOn;
    private MeteredComponent meteredComponent;
    private TaxRate taxRate;
    private Coupon coupon;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setStatus(Status.ACTIVE);

        plan = new Plan();
        plan.setId(1L);
        plan.setName("Test Plan");
        plan.setStatus(Status.ACTIVE);
        plan.setProduct(product);

        addOn = AddOn.builder().id(1L).name("HD Streaming").priceMinor(500L)
                .currency("INR").billingPeriod(BillingPeriod.MONTHLY)
                .taxMode(TaxMode.EXCLUSIVE).status(Status.ACTIVE).build();

        meteredComponent = new MeteredComponent();
        meteredComponent.setId(1L);
        meteredComponent.setName("API Calls");
        meteredComponent.setUnitName("call");
        meteredComponent.setPricePerUnitMinor(10L);
        meteredComponent.setPlan(plan);
        meteredComponent.setFreeTierQuantity(100L);
        meteredComponent.setStatus(Status.ACTIVE);

        taxRate = new TaxRate();
        taxRate.setId(1L);
        taxRate.setName("GST");
        taxRate.setRegion("IN");
        taxRate.setRatePercent(new BigDecimal("18"));
        taxRate.setInclusive(false);

        coupon = new Coupon();
        coupon.setId(1L);
        coupon.setCode("SAVE20");
        coupon.setName("Save 20%");
        coupon.setType(CouponType.PERCENT);
        coupon.setAmount(20L);
        coupon.setStatus(Status.ACTIVE);
    }

    // ==================== DASHBOARD ====================
    @Test
    void testGetDashboardStats() {
        when(productRepository.count()).thenReturn(5L);
        when(planRepository.count()).thenReturn(10L);
        when(couponRepository.countByStatus(Status.ACTIVE)).thenReturn(3L);
        when(addOnRepository.count()).thenReturn(4L);
        when(taxRateRepository.countActiveTaxRates()).thenReturn(2L);
        when(priceBookEntryRepository.count()).thenReturn(6L);
        when(customerRepository.count()).thenReturn(8L);
        User staffUser = User.builder().id(1L).role(UserRole.ADMIN).build();
        when(userRepository.findAll()).thenReturn(Arrays.asList(staffUser));

        Map<String, Object> stats = adminDashboardService.getDashboardStats();

        assertEquals(5L, stats.get("totalProducts"));
        assertEquals(10L, stats.get("totalPlans"));
        assertEquals(3L, stats.get("activeCoupons"));
        assertEquals(4L, stats.get("totalAddOns"));
        assertEquals(2L, stats.get("activeTaxRates"));
        assertEquals(6L, stats.get("totalPriceBooks"));
        assertEquals(8L, stats.get("totalCustomers"));
        assertEquals(1L, stats.get("totalStaff"));
    }

    // ==================== PRODUCT ====================
    @Test
    void testGetAllProducts() {
        when(productRepository.findAll()).thenReturn(Arrays.asList(product));
        when(planRepository.countByProductId(1L)).thenReturn(2L);
        List<ProductResponse> result = adminDashboardService.getAllProducts();
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
    }

    @Test
    void testGetProductById() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(planRepository.countByProductId(1L)).thenReturn(2L);
        ProductResponse result = adminDashboardService.getProductById(1L);
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
    }

    @Test
    void testGetProductById_NotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> adminDashboardService.getProductById(1L));
    }

    @Test
    void testCreateProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ProductResponse result = adminDashboardService.createProduct(product);
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
    }

    @Test
    void testUpdateProduct_Success() {
        Product updated = new Product();
        updated.setName("Updated Product");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(updated);
        ProductResponse result = adminDashboardService.updateProduct(1L, updated);
        assertNotNull(result);
    }

    @Test
    void testUpdateProduct_NotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> adminDashboardService.updateProduct(1L, new Product()));
    }

    @Test
    void testToggleProductStatus_ActiveToInactive() {
        product.setStatus(Status.ACTIVE);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        adminDashboardService.toggleProductStatus(1L);
        assertEquals(Status.INACTIVE, product.getStatus());
        verify(productRepository).save(product);
    }

    @Test
    void testToggleProductStatus_InactiveToActive() {
        product.setStatus(Status.INACTIVE);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        adminDashboardService.toggleProductStatus(1L);
        assertEquals(Status.ACTIVE, product.getStatus());
        verify(productRepository).save(product);
    }

    @Test
    void testToggleProductStatus_NotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> adminDashboardService.toggleProductStatus(1L));
    }

    // ==================== PLAN ====================
    @Test
    void testGetAllPlans() {
        when(planRepository.findAll()).thenReturn(Arrays.asList(plan));
        List<PlanResponse> result = adminDashboardService.getAllPlans();
        assertEquals(1, result.size());
        assertEquals("Test Plan", result.get(0).getName());
    }

    @Test
    void testCreatePlan() {
        when(planRepository.save(any(Plan.class))).thenReturn(plan);
        PlanResponse result = adminDashboardService.createPlan(plan);
        assertNotNull(result);
        assertEquals("Test Plan", result.getName());
    }

    @Test
    void testUpdatePlan_Success() {
        Plan updated = new Plan();
        updated.setName("Updated Plan");
        updated.setProduct(product);
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(planRepository.save(any(Plan.class))).thenReturn(updated);
        PlanResponse result = adminDashboardService.updatePlan(1L, updated);
        assertNotNull(result);
    }

    @Test
    void testUpdatePlan_NotFound() {
        when(planRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> adminDashboardService.updatePlan(1L, new Plan()));
    }

    @Test
    void testTogglePlanStatus_ActiveToInactive() {
        plan.setStatus(Status.ACTIVE);
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        adminDashboardService.togglePlanStatus(1L);
        assertEquals(Status.INACTIVE, plan.getStatus());
        verify(planRepository).save(plan);
    }

    @Test
    void testTogglePlanStatus_InactiveToActive() {
        plan.setStatus(Status.INACTIVE);
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        adminDashboardService.togglePlanStatus(1L);
        assertEquals(Status.ACTIVE, plan.getStatus());
        verify(planRepository).save(plan);
    }

    @Test
    void testTogglePlanStatus_NotFound() {
        when(planRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> adminDashboardService.togglePlanStatus(1L));
    }

    // ==================== PRICE BOOK ====================
    @Test
    void testGetAllPriceBookEntries() {
        PriceBookEntry entry = new PriceBookEntry();
        entry.setId(1L);
        entry.setRegion("US");
        entry.setPlan(plan);
        when(priceBookEntryRepository.findAll()).thenReturn(Arrays.asList(entry));
        List<PriceBookResponse> result = adminDashboardService.getAllPriceBookEntries();
        assertEquals(1, result.size());
        assertEquals("US", result.get(0).getRegion());
    }

    @Test
    void testCreatePriceBookEntry() {
        PriceBookEntry entry = new PriceBookEntry();
        entry.setId(1L);
        entry.setPlan(plan);
        when(priceBookEntryRepository.save(any(PriceBookEntry.class))).thenReturn(entry);
        PriceBookResponse result = adminDashboardService.createPriceBookEntry(entry);
        assertNotNull(result);
    }

    @Test
    void testUpdatePriceBookEntry_Success() {
        PriceBookEntry entry = new PriceBookEntry();
        entry.setId(1L);
        entry.setRegion("US");
        entry.setPlan(plan);
        PriceBookEntry updated = new PriceBookEntry();
        updated.setRegion("EU");
        when(priceBookEntryRepository.findById(1L)).thenReturn(Optional.of(entry));
        when(priceBookEntryRepository.save(any(PriceBookEntry.class))).thenReturn(entry);
        PriceBookResponse result = adminDashboardService.updatePriceBookEntry(1L, updated);
        assertNotNull(result);
        assertEquals("EU", entry.getRegion());
    }

    @Test
    void testUpdatePriceBookEntry_NotFound() {
        when(priceBookEntryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> adminDashboardService.updatePriceBookEntry(1L, new PriceBookEntry()));
    }

    @Test
    void testArchivePriceBookEntry_Success() {
        PriceBookEntry entry = new PriceBookEntry();
        entry.setId(1L);
        entry.setPlan(plan);
        when(priceBookEntryRepository.findById(1L)).thenReturn(Optional.of(entry));
        when(subscriptionRepository.countByPlan_IdAndStatusIn(anyLong(), any())).thenReturn(0L);
        adminDashboardService.archivePriceBookEntry(1L);
        verify(priceBookEntryRepository).deleteById(1L);
    }

    @Test
    void testArchivePriceBookEntry_Conflict() {
        PriceBookEntry entry = new PriceBookEntry();
        entry.setId(1L);
        entry.setPlan(plan);
        when(priceBookEntryRepository.findById(1L)).thenReturn(Optional.of(entry));
        when(subscriptionRepository.countByPlan_IdAndStatusIn(anyLong(), any())).thenReturn(5L);
        assertThrows(RuntimeException.class, () -> adminDashboardService.archivePriceBookEntry(1L));
    }

    @Test
    void testArchivePriceBookEntry_NotFound() {
        when(priceBookEntryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> adminDashboardService.archivePriceBookEntry(1L));
    }

    // ==================== ADD-ON ====================
    @Test
    void testGetAllAddOns() {
        when(addOnRepository.findAll()).thenReturn(Arrays.asList(addOn));
        List<AddOnResponse> result = adminDashboardService.getAllAddOns();
        assertEquals(1, result.size());
        assertEquals("HD Streaming", result.get(0).getName());
    }

    @Test
    void testCreateAddOn() {
        when(addOnRepository.save(any(AddOn.class))).thenReturn(addOn);
        AddOnResponse result = adminDashboardService.createAddOn(addOn);
        assertNotNull(result);
        assertEquals("HD Streaming", result.getName());
    }

    @Test
    void testUpdateAddOn_Success() {
        AddOn updated = AddOn.builder().name("4K Streaming").priceMinor(800L)
                .currency("INR").billingPeriod(BillingPeriod.MONTHLY).taxMode(TaxMode.EXCLUSIVE).build();
        when(addOnRepository.findById(1L)).thenReturn(Optional.of(addOn));
        when(addOnRepository.save(any(AddOn.class))).thenReturn(addOn);
        AddOnResponse result = adminDashboardService.updateAddOn(1L, updated);
        assertNotNull(result);
        assertEquals("4K Streaming", addOn.getName());
    }

    @Test
    void testUpdateAddOn_NotFound() {
        when(addOnRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> adminDashboardService.updateAddOn(1L, new AddOn()));
    }

    @Test
    void testToggleAddOnStatus_ActiveToInactive() {
        addOn.setStatus(Status.ACTIVE);
        when(addOnRepository.findById(1L)).thenReturn(Optional.of(addOn));
        adminDashboardService.toggleAddOnStatus(1L);
        assertEquals(Status.INACTIVE, addOn.getStatus());
        verify(addOnRepository).save(addOn);
    }

    @Test
    void testToggleAddOnStatus_InactiveToActive() {
        addOn.setStatus(Status.INACTIVE);
        when(addOnRepository.findById(1L)).thenReturn(Optional.of(addOn));
        adminDashboardService.toggleAddOnStatus(1L);
        assertEquals(Status.ACTIVE, addOn.getStatus());
        verify(addOnRepository).save(addOn);
    }

    @Test
    void testToggleAddOnStatus_NotFound() {
        when(addOnRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> adminDashboardService.toggleAddOnStatus(1L));
    }

    // ==================== METERED COMPONENT ====================
    @Test
    void testGetAllMeteredComponents() {
        when(meteredComponentRepository.findAll()).thenReturn(Arrays.asList(meteredComponent));
        List<MeteredComponentResponse> result = adminDashboardService.getAllMeteredComponents();
        assertEquals(1, result.size());
        assertEquals("API Calls", result.get(0).getName());
    }

    @Test
    void testCreateMeteredComponent() {
        when(meteredComponentRepository.save(any(MeteredComponent.class))).thenReturn(meteredComponent);
        MeteredComponentResponse result = adminDashboardService.createMeteredComponent(meteredComponent);
        assertNotNull(result);
        assertEquals("API Calls", result.getName());
    }

    @Test
    void testUpdateMeteredComponent_Success() {
        MeteredComponent updated = new MeteredComponent();
        updated.setName("Bandwidth");
        updated.setUnitName("GB");
        updated.setPricePerUnitMinor(50L);
        updated.setFreeTierQuantity(5L);
        when(meteredComponentRepository.findById(1L)).thenReturn(Optional.of(meteredComponent));
        when(meteredComponentRepository.save(any(MeteredComponent.class))).thenReturn(meteredComponent);
        MeteredComponentResponse result = adminDashboardService.updateMeteredComponent(1L, updated);
        assertNotNull(result);
        assertEquals("Bandwidth", meteredComponent.getName());
    }

    @Test
    void testUpdateMeteredComponent_NotFound() {
        when(meteredComponentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> adminDashboardService.updateMeteredComponent(1L, new MeteredComponent()));
    }

    @Test
    void testToggleMeteredComponentStatus_ActiveToInactive() {
        meteredComponent.setStatus(Status.ACTIVE);
        when(meteredComponentRepository.findById(1L)).thenReturn(Optional.of(meteredComponent));
        adminDashboardService.toggleMeteredComponentStatus(1L);
        assertEquals(Status.INACTIVE, meteredComponent.getStatus());
        verify(meteredComponentRepository).save(meteredComponent);
    }

    @Test
    void testToggleMeteredComponentStatus_NotFound() {
        when(meteredComponentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> adminDashboardService.toggleMeteredComponentStatus(1L));
    }

    // ==================== TAX RATE ====================
    @Test
    void testGetAllTaxRates() {
        when(taxRateRepository.findAll()).thenReturn(Arrays.asList(taxRate));
        List<TaxRate> result = adminDashboardService.getAllTaxRates();
        assertEquals(1, result.size());
        assertEquals("GST", result.get(0).getName());
    }

    @Test
    void testCreateTaxRate() {
        when(taxRateRepository.save(any(TaxRate.class))).thenReturn(taxRate);
        TaxRate result = adminDashboardService.createTaxRate(taxRate);
        assertNotNull(result);
        assertEquals("GST", result.getName());
    }

    @Test
    void testUpdateTaxRate_Success() {
        TaxRate updated = new TaxRate();
        updated.setName("VAT");
        updated.setRegion("US");
        updated.setRatePercent(new BigDecimal("10"));
        updated.setInclusive(true);
        when(taxRateRepository.findById(1L)).thenReturn(Optional.of(taxRate));
        when(taxRateRepository.save(any(TaxRate.class))).thenReturn(taxRate);
        TaxRate result = adminDashboardService.updateTaxRate(1L, updated);
        assertNotNull(result);
        assertEquals("VAT", taxRate.getName());
    }

    @Test
    void testUpdateTaxRate_NotFound() {
        when(taxRateRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> adminDashboardService.updateTaxRate(1L, new TaxRate()));
    }

    @Test
    void testDeleteTaxRate() {
        adminDashboardService.deleteTaxRate(1L);
        verify(taxRateRepository).deleteById(1L);
    }

    // ==================== COUPON ====================
    @Test
    void testGetAllCoupons() {
        when(couponRepository.findAll()).thenReturn(Arrays.asList(coupon));
        List<Coupon> result = adminDashboardService.getAllCoupons();
        assertEquals(1, result.size());
        assertEquals("SAVE20", result.get(0).getCode());
    }

    @Test
    void testCreateCoupon_Success() {
        when(couponRepository.existsByCode("SAVE20")).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);
        Coupon result = adminDashboardService.createCoupon(coupon);
        assertNotNull(result);
        assertEquals("SAVE20", result.getCode());
    }

    @Test
    void testCreateCoupon_DuplicateCode() {
        when(couponRepository.existsByCode("SAVE20")).thenReturn(true);
        assertThrows(RuntimeException.class, () -> adminDashboardService.createCoupon(coupon));
    }

    @Test
    void testUpdateCoupon_Success() {
        Coupon updated = new Coupon();
        updated.setName("Save 30%");
        updated.setType(CouponType.PERCENT);
        updated.setAmount(30L);
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);
        Coupon result = adminDashboardService.updateCoupon(1L, updated);
        assertNotNull(result);
        assertEquals("Save 30%", coupon.getName());
    }

    @Test
    void testUpdateCoupon_NotFound() {
        when(couponRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> adminDashboardService.updateCoupon(1L, new Coupon()));
    }

    @Test
    void testToggleCouponStatus_ActiveToDisabled() {
        coupon.setStatus(Status.ACTIVE);
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        adminDashboardService.toggleCouponStatus(1L);
        assertEquals(Status.DISABLED, coupon.getStatus());
        verify(couponRepository).save(coupon);
    }

    @Test
    void testToggleCouponStatus_DisabledToActive() {
        coupon.setStatus(Status.DISABLED);
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        adminDashboardService.toggleCouponStatus(1L);
        assertEquals(Status.ACTIVE, coupon.getStatus());
        verify(couponRepository).save(coupon);
    }

    @Test
    void testToggleCouponStatus_NotFound() {
        when(couponRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> adminDashboardService.toggleCouponStatus(1L));
    }

    // ==================== CUSTOMERS ====================
    @Test
    void testGetAllCustomers() {
        User custUser = User.builder().id(10L).email("cust@test.com").fullName("Cust").build();
        Customer customer = Customer.builder().id(1L).user(custUser).build();
        when(customerRepository.findAll()).thenReturn(Arrays.asList(customer));
        List<CustomerResponse> result = adminDashboardService.getAllCustomers();
        assertEquals(1, result.size());
    }

    // ==================== STAFF ====================
    @Test
    void testGetAllStaff() {
        User admin = User.builder().id(1L).role(UserRole.ADMIN).build();
        User customer = User.builder().id(2L).role(UserRole.CUSTOMER).build();
        when(userRepository.findAll()).thenReturn(Arrays.asList(admin, customer));
        List<StaffResponse> result = adminDashboardService.getAllStaff();
        assertEquals(1, result.size()); // Only ADMIN, not CUSTOMER
    }

    @Test
    void testCreateStaff_Success() {
        User newStaff = User.builder().email("staff@test.com").passwordHash("plain123").role(UserRole.SUPPORT).build();
        when(userRepository.existsByEmail("staff@test.com")).thenReturn(false);
        when(passwordEncoder.encode("plain123")).thenReturn("encoded123");
        when(userRepository.save(any(User.class))).thenReturn(newStaff);
        StaffResponse result = adminDashboardService.createStaff(newStaff);
        assertNotNull(result);
        verify(passwordEncoder).encode("plain123");
    }

    @Test
    void testCreateStaff_DuplicateEmail() {
        User newStaff = User.builder().email("staff@test.com").build();
        when(userRepository.existsByEmail("staff@test.com")).thenReturn(true);
        assertThrows(RuntimeException.class, () -> adminDashboardService.createStaff(newStaff));
    }

    @Test
    void testDeleteStaff_Success() {
        User staffUser = User.builder().id(2L).email("other@test.com").role(UserRole.SUPPORT).build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(staffUser));

        // Mock SecurityContext
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin@test.com");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        adminDashboardService.deleteStaff(2L);
        verify(userRepository).deleteById(2L);
    }

    @Test
    void testDeleteStaff_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> adminDashboardService.deleteStaff(1L));
    }

    @Test
    void testDeleteStaff_SelfDeletion() {
        User staffUser = User.builder().id(1L).email("admin@test.com").role(UserRole.ADMIN).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(staffUser));

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin@test.com");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        assertThrows(RuntimeException.class, () -> adminDashboardService.deleteStaff(1L));
    }

    @Test
    void testDeleteStaff_CustomerRole() {
        User customerUser = User.builder().id(3L).email("cust@test.com").role(UserRole.CUSTOMER).build();
        when(userRepository.findById(3L)).thenReturn(Optional.of(customerUser));

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin@test.com");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        assertThrows(RuntimeException.class, () -> adminDashboardService.deleteStaff(3L));
    }

    // ==================== SUBSCRIPTIONS ====================
    @Test
    void testGetAllSubscriptions() {
        User subUser = User.builder().id(10L).fullName("Sub User").build();
        Customer subCustomer = Customer.builder().id(1L).user(subUser).build();
        Subscription sub = Subscription.builder().id(1L).customer(subCustomer)
                .plan(plan).status(Status.ACTIVE).build();
        when(subscriptionRepository.findAll()).thenReturn(Arrays.asList(sub));
        List<SubscriptionResponse> result = adminDashboardService.getAllSubscriptions();
        assertEquals(1, result.size());
    }

    @Test
    void testGetSubscriptionById_Success() {
        User subUser = User.builder().id(10L).fullName("Sub User").build();
        Customer subCustomer = Customer.builder().id(1L).user(subUser).build();
        Subscription sub = Subscription.builder().id(1L).customer(subCustomer)
                .plan(plan).status(Status.ACTIVE).build();
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(sub));
        SubscriptionResponse result = adminDashboardService.getSubscriptionById(1L);
        assertNotNull(result);
    }

    @Test
    void testGetSubscriptionById_NotFound() {
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> adminDashboardService.getSubscriptionById(1L));
    }
}
