package com.infy.billing.service;

import com.infy.billing.dto.customer.*;
import com.infy.billing.entity.*;
import com.infy.billing.enums.ItemType;
import com.infy.billing.enums.Status;
import com.infy.billing.repository.*;
import com.infy.billing.request.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerSubscriptionServiceImpl implements CustomerSubscriptionService {

   private final SubscriptionRepository subscriptionRepository;
   private final SubscriptionItemRepository subscriptionItemRepository;
   private final CustomerRepository customerRepository;
   private final UserRepository userRepository;
   private final PlanRepository planRepository;
   private final AddOnRepository addOnRepository;
   private final MeteredComponentRepository meteredComponentRepository;
   private final UsageRecordRepository usageRecordRepository;
   private final PaymentMethodRepository paymentMethodRepository;

   public SubscriptionDTO getCurrentSubscription(String email) {
       Customer customer = getCustomerByEmail(email);
       Subscription subscription = subscriptionRepository.findByCustomer_IdAndStatusIn(
               customer.getId(),
               List.of(Status.ACTIVE, Status.TRIALING, Status.PAST_DUE, Status.PAUSED, Status.ON_HOLD)
       ).stream().findFirst().orElse(null);
       
       if (subscription == null) return null;
       return mapToSubscriptionDTO(subscription);
   }

   @Transactional
   public SubscriptionDTO createSubscription(String email, CreateSubscriptionRequest request) {
       Customer customer = getCustomerByEmail(email);
       
       if (subscriptionRepository.findByCustomer_IdAndStatusIn(customer.getId(),
               List.of(Status.ACTIVE, Status.TRIALING, Status.PAST_DUE)).size() > 0) {
           throw new RuntimeException("Customer already has an active subscription");
       }

       Plan plan = planRepository.findById(request.getPlanId())
               .orElseThrow(() -> new RuntimeException("Plan not found"));
       
       PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId())
               .orElseThrow(() -> new RuntimeException("Payment method not found"));

       LocalDate now = LocalDate.now();
       LocalDate trialEnd = now.plusDays(plan.getTrialDays());
       LocalDate periodEnd = plan.getBillingPeriod().equals("MONTHLY") ? now.plusMonths(1) : now.plusYears(1);

       Subscription subscription = new Subscription();
       subscription.setCustomer(customer);
       subscription.setPlan(plan);
       subscription.setStatus(plan.getTrialDays() > 0 ? Status.TRIALING : Status.ACTIVE);
       subscription.setStartDate(now);
       subscription.setTrialEndDate(trialEnd);
       subscription.setCurrentPeriodStart(now);
       subscription.setCurrentPeriodEnd(periodEnd);
       subscription.setCancelAtPeriodEnd(false);
       subscription.setPaymentMethodId(paymentMethod.getId());
       subscription.setCurrency(customer.getCurrency());
       subscription.setCreatedAt(LocalDateTime.now());

       subscription = subscriptionRepository.save(subscription);

       SubscriptionItem planItem = new SubscriptionItem();
       planItem.setSubscription(subscription);
       planItem.setItemType(ItemType.PLAN);
       planItem.setPlan(plan);
       planItem.setUnitPriceMinor(plan.getDefaultPriceMinor());
       planItem.setQuantity(1);
       planItem.setTaxMode(plan.getTaxMode());
       subscriptionItemRepository.save(planItem);

       return mapToSubscriptionDTO(subscription);
   }

   @Transactional
   public SubscriptionDTO upgradeSubscription(String email, UpgradeSubscriptionRequest request) {
       Customer customer = getCustomerByEmail(email);
       Subscription subscription = getActiveSubscription(customer.getId());
       
       Plan newPlan = planRepository.findById(request.getPlanId())
               .orElseThrow(() -> new RuntimeException("Plan not found"));

       subscription.setId(newPlan.getId());
       subscription.setUpdatedAt(LocalDateTime.now());
       subscriptionRepository.save(subscription);

       SubscriptionItem planItem = subscriptionItemRepository.findBySubscription_IdAndItemType(
               subscription.getId(), ItemType.PLAN);
       planItem.setPlan(newPlan);
       planItem.setUnitPriceMinor(newPlan.getDefaultPriceMinor());
       subscriptionItemRepository.save(planItem);

       return mapToSubscriptionDTO(subscription);
   }

   @Transactional
   public void cancelSubscription(String email, boolean atPeriodEnd) {
       Customer customer = getCustomerByEmail(email);
       Subscription subscription = getActiveSubscription(customer.getId());
       
       if (atPeriodEnd) {
           subscription.setCancelAtPeriodEnd(true);
       } else {
           subscription.setStatus(Status.CANCELED);
           subscription.setCanceledAt(LocalDateTime.now());
       }
       subscriptionRepository.save(subscription);
   }

   @Transactional
   public SubscriptionDTO pauseSubscription(String email, PauseSubscriptionRequest request) {
       Customer customer = getCustomerByEmail(email);
       Subscription subscription = getActiveSubscription(customer.getId());
       
       subscription.setStatus(Status.PAUSED);
       subscription.setPausedFrom(LocalDate.now());
       subscription.setPausedTo(LocalDate.parse(request.getPausedTo()));
       subscriptionRepository.save(subscription);
       
       return mapToSubscriptionDTO(subscription);
   }

   @Transactional
   public SubscriptionDTO resumeSubscription(String email) {
       Customer customer = getCustomerByEmail(email);
       Subscription subscription = subscriptionRepository.findByCustomer_IdAndStatus(
               customer.getId(), Status.PAUSED)
               .orElseThrow(() -> new RuntimeException("No paused subscription found"));
       
       subscription.setStatus(Status.ACTIVE);
       subscription.setPausedFrom(null);
       subscription.setPausedTo(null);
       subscriptionRepository.save(subscription);
       
       return mapToSubscriptionDTO(subscription);
   }

   @Transactional
   public void addAddOn(String email, Long addonId) {
       Customer customer = getCustomerByEmail(email);
       Subscription subscription = getActiveSubscription(customer.getId());
       AddOn addOn = addOnRepository.findById(addonId)
               .orElseThrow(() -> new RuntimeException("Add-on not found"));

       SubscriptionItem item = new SubscriptionItem();
       item.setSubscription(subscription);
       item.setItemType(ItemType.ADDON);
       item.setAddOn(addOn);
       item.setUnitPriceMinor(addOn.getPriceMinor());
       item.setQuantity(1);
       item.setTaxMode(addOn.getTaxMode());
       subscriptionItemRepository.save(item);
   }

   @Transactional
   public void removeAddOn(String email, Long addonId) {
       Customer customer = getCustomerByEmail(email);
       Subscription subscription = getActiveSubscription(customer.getId());
       
       SubscriptionItem item = subscriptionItemRepository.findBySubscription_IdAndAddOn_Id(
               subscription.getId(), addonId);
       if (item != null) {
           subscriptionItemRepository.delete(item);
       }
   }

   public List<UsageRecordDTO> getMeteredUsage(String email, String startDate, String endDate) {
       Customer customer = getCustomerByEmail(email);
       Subscription subscription = getActiveSubscription(customer.getId());
       
       LocalDate start = startDate != null ? LocalDate.parse(startDate) : subscription.getCurrentPeriodStart();
       LocalDate end = endDate != null ? LocalDate.parse(endDate) : subscription.getCurrentPeriodEnd();
       
       List<UsageRecord> usage = usageRecordRepository.findBySubscription_IdAndBillingPeriodStartGreaterThanEqualAndBillingPeriodEndLessThanEqual(
               subscription.getId(), start, end);
       
       return usage.stream().map(this::mapToUsageRecordDTO).collect(Collectors.toList());
   }

   private Customer getCustomerByEmail(String email) {
       User user = userRepository.findByEmail(email)
               .orElseThrow(() -> new RuntimeException("User not found"));
       return customerRepository.findByUser_Id(user.getId())
               .orElseThrow(() -> new RuntimeException("Customer not found"));
   }

   private Subscription getActiveSubscription(Long customerId) {
       return subscriptionRepository.findByCustomer_IdAndStatusIn(customerId,
               List.of(Status.ACTIVE, Status.TRIALING, Status.PAST_DUE, Status.PAUSED, Status.ON_HOLD)).stream()
               .findFirst()
               .orElseThrow(() -> new RuntimeException("No active subscription found"));
   }

   private SubscriptionDTO mapToSubscriptionDTO(Subscription subscription) {
       SubscriptionDTO dto = new SubscriptionDTO();
       dto.setSubscriptionId(subscription.getId());
       dto.setCustomerId(subscription.getCustomer().getId());
       dto.setPlanId(subscription.getPlan().getId());
       dto.setPlanName(planRepository.findById(subscription.getPlan().getId()).map(Plan::getName).orElse("Unknown"));
       dto.setStatus(subscription.getStatus());
       dto.setStartDate(subscription.getStartDate().toString());
       dto.setTrialEndDate(subscription.getTrialEndDate() != null ? subscription.getTrialEndDate().toString() : null);
       dto.setCurrentPeriodStart(subscription.getCurrentPeriodStart().toString());
       dto.setCurrentPeriodEnd(subscription.getCurrentPeriodEnd().toString());
       dto.setCancelAtPeriodEnd(subscription.getCancelAtPeriodEnd());
       dto.setCanceledAt(subscription.getCanceledAt() != null ? subscription.getCanceledAt().toString() : null);
       dto.setPausedFrom(subscription.getPausedFrom() != null ? subscription.getPausedFrom().toString() : null);
       dto.setPausedTo(subscription.getPausedTo() != null ? subscription.getPausedTo().toString() : null);
       dto.setCurrency(subscription.getCurrency());

       List<SubscriptionItem> items = subscriptionItemRepository.findBySubscription_Id(subscription.getId());
       dto.setAddOns(items.stream()
               .filter(i -> "ADDON".equals(i.getItemType()))
               .map(this::mapToSubscriptionAddOnDTO)
               .collect(Collectors.toList()));

       dto.setMeteredUsage(items.stream()
               .filter(i -> "METERED".equals(i.getItemType()))
               .map(this::mapToMeteredUsageDTO)
               .collect(Collectors.toList()));

       return dto;
   }

   private SubscriptionItemDTO mapToSubscriptionAddOnDTO(SubscriptionItem item) {
       SubscriptionItemDTO dto = new SubscriptionItemDTO();
       dto.setItemId(item.getId());
       dto.setAddonId(item.getAddOn().getId());
       AddOn addOn = addOnRepository.findById(item.getAddOn().getId()).orElse(null);
       dto.setAddonName(addOn != null ? addOn.getName() : "Unknown");
       dto.setUnitPriceMinor(item.getUnitPriceMinor());
       dto.setQuantity(item.getQuantity());
       return dto;
   }

   private MeteredUsageDTO mapToMeteredUsageDTO(SubscriptionItem item) {
       MeteredUsageDTO dto = new MeteredUsageDTO();
       dto.setComponentId(item.getComponent().getId());
       MeteredComponent comp = meteredComponentRepository.findById(item.getComponent().getId()).orElse(null);
       dto.setComponentName(comp != null ? comp.getName() : "Unknown");
       dto.setUnitName(comp != null ? comp.getUnitName() : "unit");
       dto.setPricePerUnitMinor(comp != null ? comp.getPricePerUnitMinor() : 0);
       dto.setFreeTierQuantity(comp != null ? comp.getFreeTierQuantity() : 0);
       Long usage = usageRecordRepository.sumQuantityBySubscription_IdAndComponent_Id(
               item.getSubscription().getId(), item.getComponent().getId());
       dto.setQuantityUsed(usage != null ? usage : 0);
       long billable = Math.max(0, dto.getQuantityUsed() - dto.getFreeTierQuantity());
       dto.setCostMinor(billable * dto.getPricePerUnitMinor());
       return dto;
   }

   private UsageRecordDTO mapToUsageRecordDTO(UsageRecord usage) {
       UsageRecordDTO dto = new UsageRecordDTO();
       dto.setUsageId(usage.getId());
       dto.setComponentId(usage.getComponent().getId());
       MeteredComponent comp = meteredComponentRepository.findById(usage.getComponent().getId()).orElse(null);
       dto.setComponentName(comp != null ? comp.getName() : "Unknown");
       dto.setQuantity(usage.getQuantity());
       dto.setUnitName(comp != null ? comp.getUnitName() : "unit");
       dto.setRecordedAt(usage.getRecordedAt().toString());
       dto.setBillingPeriodStart(usage.getBillingPeriodStart().toString());
       dto.setBillingPeriodEnd(usage.getBillingPeriodEnd().toString());
       return dto;
   }
}
