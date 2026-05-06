package com.infy.billing.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.infy.billing.dto.admin.AddOnResponse;
import com.infy.billing.dto.admin.CustomerResponse;
import com.infy.billing.dto.admin.MeteredComponentResponse;
import com.infy.billing.dto.admin.PlanResponse;
import com.infy.billing.dto.admin.PriceBookResponse;
import com.infy.billing.dto.admin.ProductResponse;
import com.infy.billing.dto.admin.StaffResponse;
import com.infy.billing.dto.admin.SubscriptionResponse;
import com.infy.billing.entity.AddOn;
import com.infy.billing.entity.Coupon;
import com.infy.billing.entity.Customer;
import com.infy.billing.entity.MeteredComponent;
import com.infy.billing.entity.Plan;
import com.infy.billing.entity.PriceBookEntry;
import com.infy.billing.entity.Product;
import com.infy.billing.entity.Subscription;
import com.infy.billing.entity.TaxRate;
import com.infy.billing.entity.User;
import com.infy.billing.enums.Status;
import com.infy.billing.enums.UserRole;
import com.infy.billing.exception.CustomException;
import com.infy.billing.repository.AddOnRepository;
import com.infy.billing.repository.CouponRepository;
import com.infy.billing.repository.CustomerRepository;
import com.infy.billing.repository.MeteredComponentRepository;
import com.infy.billing.repository.PlanRepository;
import com.infy.billing.repository.PriceBookEntryRepository;
import com.infy.billing.repository.ProductRepository;
import com.infy.billing.repository.SubscriptionRepository;
import com.infy.billing.repository.TaxRateRepository;
import com.infy.billing.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final ProductRepository productRepository;
    private final PlanRepository planRepository;
    private final PriceBookEntryRepository priceBookEntryRepository;
    private final AddOnRepository addOnRepository;
    private final MeteredComponentRepository meteredComponentRepository;
    private final TaxRateRepository taxRateRepository;
    private final CouponRepository couponRepository;
    private final CustomerRepository customerRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ==================== DASHBOARD ====================
    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        // Catalog Management Stats
        stats.put("totalProducts", productRepository.count());
        stats.put("totalPlans", planRepository.count());
        stats.put("activeCoupons", couponRepository.countByStatus(Status.ACTIVE));
        stats.put("totalAddOns", addOnRepository.count());
        stats.put("activeTaxRates", taxRateRepository.countActiveTaxRates());
        stats.put("totalPriceBooks", priceBookEntryRepository.count());
        stats.put("totalCustomers", customerRepository.count());
        stats.put("totalStaff",
                userRepository.findAll().stream().filter(u -> u.getRole() != UserRole.CUSTOMER).count());
        return stats;
    }

    // ==================== PRODUCT ====================
    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(p -> ProductResponse.from(p, planRepository.countByProductId(p.getId())))
                .toList();
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("Product not found with id: " + id));
        return ProductResponse.from(p, planRepository.countByProductId(p.getId()));
    }

    @Override
    public ProductResponse createProduct(Product product) {
        Product saved = productRepository.save(product);
        return ProductResponse.from(saved, 0);
    }

    @Override
    public ProductResponse updateProduct(Long id, Product updated) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("Product not found with id: " + id));
        product.setName(updated.getName());
        product.setDescription(updated.getDescription());
        Product saved = productRepository.save(product);
        return ProductResponse.from(saved, planRepository.countByProductId(saved.getId()));
    }

    @Override
    public void toggleProductStatus(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("Product not found with id: " + id));
        product.setStatus(product.getStatus().equals(Status.ACTIVE) ? Status.INACTIVE : Status.ACTIVE);
        productRepository.save(product);
    }

    // ==================== PLAN ====================
    @Override
    public List<PlanResponse> getAllPlans() {
        return planRepository.findAll().stream().map(PlanResponse::from).toList();
    }

    @Override
    public PlanResponse createPlan(Plan plan) {
        return PlanResponse.from(planRepository.save(plan));
    }

    @Override
    public PlanResponse updatePlan(Long id, Plan updated) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("Plan not found"));
        plan.setName(updated.getName());
        plan.setBillingPeriod(updated.getBillingPeriod());
        plan.setDefaultPriceMinor(updated.getDefaultPriceMinor());
        plan.setDefaultCurrency(updated.getDefaultCurrency());
        plan.setTrialDays(updated.getTrialDays());
        plan.setSetupFeeMinor(updated.getSetupFeeMinor());
        plan.setTaxMode(updated.getTaxMode());
        plan.setEffectiveFrom(updated.getEffectiveFrom());
        plan.setEffectiveTo(updated.getEffectiveTo());
        return PlanResponse.from(planRepository.save(plan));
    }

    @Override
    public void togglePlanStatus(Long id) {
        Plan plan = planRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("Plan not found"));
        plan.setStatus(plan.getStatus().equals(Status.ACTIVE) ? Status.INACTIVE : Status.ACTIVE);
        planRepository.save(plan);
    }

    // ==================== PRICE BOOK ====================
    @Override
    public List<PriceBookResponse> getAllPriceBookEntries() {
        return priceBookEntryRepository.findAll().stream().map(PriceBookResponse::from).toList();
    }

    @Override
    public PriceBookResponse createPriceBookEntry(PriceBookEntry entry) {
        return PriceBookResponse.from(priceBookEntryRepository.save(entry));
    }

    @Override
    public PriceBookResponse updatePriceBookEntry(Long id, PriceBookEntry updated) {
        PriceBookEntry entry = priceBookEntryRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("Price book entry not found"));
        entry.setRegion(updated.getRegion());
        entry.setCurrency(updated.getCurrency());
        entry.setPriceMinor(updated.getPriceMinor());
        entry.setEffectiveFrom(updated.getEffectiveFrom());
        entry.setEffectiveTo(updated.getEffectiveTo());
        return PriceBookResponse.from(priceBookEntryRepository.save(entry));
    }

    @Override
    public void deletePriceBookEntry(Long id) {
        priceBookEntryRepository.deleteById(id);
    }

    // ==================== ADD-ON ====================
    @Override
    public List<AddOnResponse> getAllAddOns() {
        return addOnRepository.findAll().stream().map(AddOnResponse::from).toList();
    }

    @Override
    public AddOnResponse createAddOn(AddOn addOn) {
        return AddOnResponse.from(addOnRepository.save(addOn));
    }

    @Override
    public AddOnResponse updateAddOn(Long id, AddOn updated) {
        AddOn addOn = addOnRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("Add-on not found"));
        addOn.setName(updated.getName());
        addOn.setPriceMinor(updated.getPriceMinor());
        addOn.setCurrency(updated.getCurrency());
        addOn.setBillingPeriod(updated.getBillingPeriod());
        addOn.setTaxMode(updated.getTaxMode());
        return AddOnResponse.from(addOnRepository.save(addOn));
    }

    @Override
    public void toggleAddOnStatus(Long id) {
        AddOn addOn = addOnRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("Add-on not found"));
        addOn.setStatus(addOn.getStatus().equals(Status.ACTIVE) ? Status.INACTIVE : Status.ACTIVE);
        addOnRepository.save(addOn);
    }

    // ==================== METERED COMPONENT ====================
    @Override
    public List<MeteredComponentResponse> getAllMeteredComponents() {
        return meteredComponentRepository.findAll().stream().map(MeteredComponentResponse::from).toList();
    }

    @Override
    public MeteredComponentResponse createMeteredComponent(MeteredComponent component) {
        return MeteredComponentResponse.from(meteredComponentRepository.save(component));
    }

    @Override
    public MeteredComponentResponse updateMeteredComponent(Long id, MeteredComponent updated) {
        MeteredComponent component = meteredComponentRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("Metered component not found"));
        component.setName(updated.getName());
        component.setUnitName(updated.getUnitName());
        component.setPricePerUnitMinor(updated.getPricePerUnitMinor());
        component.setFreeTierQuantity(updated.getFreeTierQuantity());
        return MeteredComponentResponse.from(meteredComponentRepository.save(component));
    }

    @Override
    public void toggleMeteredComponentStatus(Long id) {
        MeteredComponent component = meteredComponentRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("Metered component not found"));
        component.setStatus(component.getStatus().equals(Status.ACTIVE) ? Status.INACTIVE : Status.ACTIVE);
        meteredComponentRepository.save(component);
    }

    // ==================== TAX RATE ====================
    @Override
    public List<TaxRate> getAllTaxRates() {
        return taxRateRepository.findAll();
    }

    @Override
    public TaxRate createTaxRate(TaxRate taxRate) {
        return taxRateRepository.save(taxRate);
    }

    @Override
    public TaxRate updateTaxRate(Long id, TaxRate updated) {
        TaxRate taxRate = taxRateRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("Tax rate not found"));
        taxRate.setName(updated.getName());
        taxRate.setRegion(updated.getRegion());
        taxRate.setRatePercent(updated.getRatePercent());
        taxRate.setInclusive(updated.getInclusive());
        taxRate.setEffectiveFrom(updated.getEffectiveFrom());
        taxRate.setEffectiveTo(updated.getEffectiveTo());
        return taxRateRepository.save(taxRate);
    }

    @Override
    public void deleteTaxRate(Long id) {
        taxRateRepository.deleteById(id);
    }

    // ==================== COUPON ====================
    @Override
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    @Override
    public Coupon createCoupon(Coupon coupon) {
        if (couponRepository.existsByCode(coupon.getCode())) {
            throw CustomException.conflict("Coupon code already exists: " + coupon.getCode());
        }
        return couponRepository.save(coupon);
    }

    @Override
    public Coupon updateCoupon(Long id, Coupon updated) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("Coupon not found"));
        coupon.setName(updated.getName());
        coupon.setType(updated.getType());
        coupon.setAmount(updated.getAmount());
        coupon.setCurrency(updated.getCurrency());
        coupon.setDuration(updated.getDuration());
        coupon.setDurationInMonths(updated.getDurationInMonths());
        coupon.setMaxRedemptions(updated.getMaxRedemptions());
        coupon.setValidFrom(updated.getValidFrom());
        coupon.setValidTo(updated.getValidTo());
        return couponRepository.save(coupon);
    }

    @Override
    public void toggleCouponStatus(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("Coupon not found"));
        coupon.setStatus(coupon.getStatus().equals(Status.ACTIVE) ? Status.DISABLED : Status.ACTIVE);
        couponRepository.save(coupon);
    }

    // ==================== CUSTOMERS ====================
    @Override
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream().map(CustomerResponse::from).toList();
    }

    @Override
    public void toggleCustomerStatus(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("Customer not found"));
        customer.setStatus(customer.getStatus().equals(Status.ACTIVE) ? Status.INACTIVE : Status.ACTIVE);
        customerRepository.save(customer);
    }

    // ==================== STAFF ====================
    @Override
    public List<StaffResponse> getAllStaff() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() != UserRole.CUSTOMER)
                .map(StaffResponse::from)
                .toList();
    }

    @Override
    public StaffResponse createStaff(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw CustomException.conflict("Email already exists: " + user.getEmail());
        }
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setStatus(Status.ACTIVE);
        return StaffResponse.from(userRepository.save(user));
    }

    @Override
    public void deleteStaff(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("Staff not found"));

        // Prevent self-deletion
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (user.getEmail().equals(currentUserEmail)) {
            throw CustomException.forbidden("You cannot delete your own account.");
        }

        if (user.getRole() == UserRole.CUSTOMER) {
            throw CustomException.forbidden("Cannot delete a customer from staff management.");
        }
        userRepository.deleteById(id);
    }

    // ==================== SUBSCRIPTIONS ====================
    @Override
    public List<SubscriptionResponse> getAllSubscriptions() {
        return subscriptionRepository.findAll().stream().map(SubscriptionResponse::from).toList();
    }

    @Override
    public SubscriptionResponse getSubscriptionById(Long id) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("Subscription not found"));
        return SubscriptionResponse.from(sub);
    }
}