package com.infy.billing.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.infy.billing.dto.admin.ProductResponse;
import com.infy.billing.dto.admin.PlanResponse;
import com.infy.billing.entity.Product;
import com.infy.billing.entity.Plan;
import com.infy.billing.enums.Status;
import com.infy.billing.repository.ProductRepository;
import com.infy.billing.repository.PlanRepository;
import com.infy.billing.repository.PriceBookEntryRepository;
import com.infy.billing.repository.AddOnRepository;
import com.infy.billing.repository.MeteredComponentRepository;
import com.infy.billing.repository.TaxRateRepository;
import com.infy.billing.repository.CouponRepository;
import com.infy.billing.repository.CustomerRepository;
import com.infy.billing.repository.SubscriptionRepository;
import com.infy.billing.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AdminDashboardServiceImplTest {

    @Mock private ProductRepository productRepository;
    @Mock private PlanRepository planRepository;
    @Mock private PriceBookEntryRepository priceBookEntryRepository;
    @Mock private AddOnRepository addOnRepository;
    @Mock private MeteredComponentRepository meteredComponentRepository;
    @Mock private TaxRateRepository taxRateRepository;
    @Mock private CouponRepository couponRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminDashboardServiceImpl adminDashboardService;

    private Product product;
    private Plan plan;

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
    }

    @Test
    void testGetDashboardStats() {
        when(productRepository.count()).thenReturn(5L);
        when(planRepository.count()).thenReturn(10L);
        when(couponRepository.countByStatus(Status.ACTIVE)).thenReturn(3L);

        Map<String, Object> stats = adminDashboardService.getDashboardStats();

        assertEquals(5L, stats.get("totalProducts"));
        assertEquals(10L, stats.get("totalPlans"));
        assertEquals(3L, stats.get("activeCoupons"));
    }

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
    void testCreateProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse result = adminDashboardService.createProduct(product);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
    }

    @Test
    void testToggleProductStatus() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        adminDashboardService.toggleProductStatus(1L);

        assertEquals(Status.INACTIVE, product.getStatus());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void testUpdateProduct_Success() {
        Product updatedProduct = new Product();
        updatedProduct.setName("Updated Product");
        updatedProduct.setStatus(Status.ACTIVE);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        ProductResponse result = adminDashboardService.updateProduct(1L, updatedProduct);

        assertNotNull(result);
        assertEquals("Updated Product", result.getName());
    }

    @Test
    void testUpdateProduct_NotFound() {
        Product updatedProduct = new Product();
        updatedProduct.setName("Updated Product");

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            adminDashboardService.updateProduct(1L, updatedProduct)
        );
    }

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
        Plan updatedPlan = new Plan();
        updatedPlan.setName("Updated Plan");
        updatedPlan.setStatus(Status.ACTIVE);
        updatedPlan.setProduct(product);

        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(planRepository.save(any(Plan.class))).thenReturn(updatedPlan);

        PlanResponse result = adminDashboardService.updatePlan(1L, updatedPlan);

        assertNotNull(result);
        assertEquals("Updated Plan", result.getName());
    }

    @Test
    void testTogglePlanStatus() {
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));

        adminDashboardService.togglePlanStatus(1L);

        assertEquals(Status.INACTIVE, plan.getStatus());
        verify(planRepository, times(1)).save(plan);
    }

    @Test
    void testUpdatePlan_NotFound() {
        Plan updatedPlan = new Plan();
        when(planRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            adminDashboardService.updatePlan(1L, updatedPlan)
        );
    }

    @Test
    void testTogglePlanStatus_NotFound() {
        when(planRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            adminDashboardService.togglePlanStatus(1L)
        );
    }

    @Test
    void testGetProductById_NotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            adminDashboardService.getProductById(1L)
        );
    }

    @Test
    void testGetAllPriceBookEntries() {
        com.infy.billing.entity.PriceBookEntry entry = new com.infy.billing.entity.PriceBookEntry();
        entry.setId(1L);
        entry.setRegion("US");
        entry.setPlan(plan);
        when(priceBookEntryRepository.findAll()).thenReturn(Arrays.asList(entry));

        List<com.infy.billing.dto.admin.PriceBookResponse> result = adminDashboardService.getAllPriceBookEntries();

        assertEquals(1, result.size());
        assertEquals("US", result.get(0).getRegion());
    }

    @Test
    void testCreatePriceBookEntry() {
        com.infy.billing.entity.PriceBookEntry entry = new com.infy.billing.entity.PriceBookEntry();
        entry.setId(1L);
        entry.setPlan(plan);
        when(priceBookEntryRepository.save(any(com.infy.billing.entity.PriceBookEntry.class))).thenReturn(entry);

        com.infy.billing.dto.admin.PriceBookResponse result = adminDashboardService.createPriceBookEntry(entry);

        assertNotNull(result);
    }

    @Test
    void testUpdatePriceBookEntry_Success() {
        com.infy.billing.entity.PriceBookEntry entry = new com.infy.billing.entity.PriceBookEntry();
        entry.setId(1L);
        entry.setRegion("US");
        entry.setPlan(plan);

        com.infy.billing.entity.PriceBookEntry updated = new com.infy.billing.entity.PriceBookEntry();
        updated.setRegion("EU");

        when(priceBookEntryRepository.findById(1L)).thenReturn(Optional.of(entry));
        when(priceBookEntryRepository.save(any(com.infy.billing.entity.PriceBookEntry.class))).thenReturn(entry);

        com.infy.billing.dto.admin.PriceBookResponse result = adminDashboardService.updatePriceBookEntry(1L, updated);

        assertNotNull(result);
        assertEquals("EU", entry.getRegion());
    }

    @Test
    void testArchivePriceBookEntry_Success() {
        com.infy.billing.entity.PriceBookEntry entry = new com.infy.billing.entity.PriceBookEntry();
        entry.setId(1L);
        entry.setPlan(plan);

        when(priceBookEntryRepository.findById(1L)).thenReturn(Optional.of(entry));
        when(subscriptionRepository.countByPlan_IdAndStatusIn(anyLong(), any())).thenReturn(0L);

        adminDashboardService.archivePriceBookEntry(1L);

        verify(priceBookEntryRepository, times(1)).deleteById(1L);
    }

    @Test
    void testArchivePriceBookEntry_Conflict() {
        com.infy.billing.entity.PriceBookEntry entry = new com.infy.billing.entity.PriceBookEntry();
        entry.setId(1L);
        entry.setPlan(plan);

        when(priceBookEntryRepository.findById(1L)).thenReturn(Optional.of(entry));
        when(subscriptionRepository.countByPlan_IdAndStatusIn(anyLong(), any())).thenReturn(5L);

        assertThrows(RuntimeException.class, () -> 
            adminDashboardService.archivePriceBookEntry(1L)
        );
    }
}
