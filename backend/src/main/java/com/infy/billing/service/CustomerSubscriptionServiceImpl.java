package com.infy.billing.service;

import com.infy.billing.dto.customer.*;
import com.infy.billing.entity.*;
import com.infy.billing.enums.*;

import com.infy.billing.repository.*;
import com.infy.billing.request.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
   private final InvoiceRepository invoiceRepository;
   private final InvoiceLineItemRepository invoiceLineItemRepository;
   private final PaymentRepository paymentRepository;
   private final SubscriptionCouponRepository subscriptionCouponRepository;

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
       LocalDate periodEnd = plan.getBillingPeriod() == BillingPeriod.MONTHLY ? now.plusMonths(1) : now.plusYears(1);

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
       Plan oldPlan = subscription.getPlan();
       
       Plan newPlan = planRepository.findById(request.getPlanId())
               .orElseThrow(() -> new RuntimeException("Plan not found"));

       if (oldPlan.getId().equals(newPlan.getId())) {
           throw new RuntimeException("Already on this plan");
       }

       boolean isTrialing = subscription.getStatus() == Status.TRIALING;

       // During trial: just switch the plan, no proration needed
       if (isTrialing) {
           subscription.setPlan(newPlan);
           // Recalculate period after trial based on new plan's billing period
           LocalDate trialEnd = subscription.getTrialEndDate();
           if (newPlan.getBillingPeriod() == BillingPeriod.MONTHLY) {
               subscription.setCurrentPeriodStart(trialEnd);
               subscription.setCurrentPeriodEnd(trialEnd.plusMonths(1));
           } else {
               subscription.setCurrentPeriodStart(trialEnd);
               subscription.setCurrentPeriodEnd(trialEnd.plusYears(1));
           }
           subscription.setUpdatedAt(LocalDateTime.now());
           subscriptionRepository.save(subscription);

           // Update the plan subscription item
           SubscriptionItem planItem = subscriptionItemRepository.findBySubscription_IdAndItemType(
                   subscription.getId(), ItemType.PLAN);
           
           if (planItem == null) {
               planItem = new SubscriptionItem();
               planItem.setSubscription(subscription);
               planItem.setItemType(ItemType.PLAN);
               planItem.setQuantity(1);
               planItem.setTaxMode(newPlan.getTaxMode());
           }
           
           planItem.setPlan(newPlan);
           planItem.setUnitPriceMinor(newPlan.getDefaultPriceMinor());
           subscriptionItemRepository.save(planItem);

           return mapToSubscriptionDTO(subscription);
       }

       // Active subscription: calculate proration
       LocalDate today = LocalDate.now();
       LocalDate periodStart = subscription.getCurrentPeriodStart();
       LocalDate periodEnd = subscription.getCurrentPeriodEnd();
       
       long totalDaysInPeriod = ChronoUnit.DAYS.between(periodStart, periodEnd);
       long remainingDays = ChronoUnit.DAYS.between(today, periodEnd);
       
       if (totalDaysInPeriod <= 0) totalDaysInPeriod = 1;
       if (remainingDays <= 0) remainingDays = 0;

       long oldDailyRate = oldPlan.getDefaultPriceMinor() / totalDaysInPeriod;
       long newDailyRate = newPlan.getDefaultPriceMinor() / totalDaysInPeriod;

       boolean isUpgrade = newPlan.getDefaultPriceMinor() > oldPlan.getDefaultPriceMinor();

       if (isUpgrade) {
           // UPGRADE: charge the difference for remaining days
           long credit = oldDailyRate * remainingDays;
           long debit = newDailyRate * remainingDays;
           long prorationAmount = debit - credit;

           // Generate proration invoice
           generateProrationInvoice(customer, subscription, oldPlan, newPlan, 
                   prorationAmount, today, periodEnd, "UPGRADE");

           // Update subscription to new plan, keep same period
           subscription.setPlan(newPlan);
           subscription.setUpdatedAt(LocalDateTime.now());
           subscriptionRepository.save(subscription);

       } else {
           // DOWNGRADE: extend the period with remaining credit
           long creditAmount = oldDailyRate * remainingDays;
           
           // Calculate new daily rate for the downgraded plan's own period
           long newPlanTotalDays = newPlan.getBillingPeriod() == BillingPeriod.MONTHLY ? 30 : 365;
           long newPlanDailyRate = newPlan.getDefaultPriceMinor() / newPlanTotalDays;
           if (newPlanDailyRate <= 0) newPlanDailyRate = 1;
           
           long extendedDays = creditAmount / newPlanDailyRate;

           // Generate a $0 proration invoice documenting the change
           generateProrationInvoice(customer, subscription, oldPlan, newPlan, 
                   0L, today, today.plusDays(extendedDays), "DOWNGRADE");

           // Update subscription: new plan with extended period
           subscription.setPlan(newPlan);
           subscription.setCurrentPeriodStart(today);
           subscription.setCurrentPeriodEnd(today.plusDays(extendedDays));
           subscription.setUpdatedAt(LocalDateTime.now());
           subscriptionRepository.save(subscription);
       }

       // Update the plan subscription item
       SubscriptionItem planItem = subscriptionItemRepository.findBySubscription_IdAndItemType(
               subscription.getId(), ItemType.PLAN);
       
       if (planItem == null) {
           planItem = new SubscriptionItem();
           planItem.setSubscription(subscription);
           planItem.setItemType(ItemType.PLAN);
           planItem.setQuantity(1);
           planItem.setTaxMode(newPlan.getTaxMode());
       }
       
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
    public SubscriptionDTO addAddOn(String email, Long addonId) {
        Customer customer = getCustomerByEmail(email);
        Subscription subscription = getActiveSubscription(customer.getId());
        AddOn addOn = addOnRepository.findById(addonId)
                .orElseThrow(() -> new RuntimeException("Add-on not found"));

        // Compatibility check: Monthly add-ons only for monthly plans, etc.
        if (addOn.getBillingPeriod() != subscription.getPlan().getBillingPeriod()) {
            throw new RuntimeException("Add-on billing period (" + addOn.getBillingPeriod() + 
                    ") must match your plan's billing period (" + subscription.getPlan().getBillingPeriod() + ")");
        }

        // Check if already added
        SubscriptionItem existing = subscriptionItemRepository.findBySubscription_IdAndAddOn_Id(
                subscription.getId(), addonId);
        if (existing != null) {
            throw new RuntimeException("Add-on already active on this subscription");
        }

        SubscriptionItem item = new SubscriptionItem();
        item.setSubscription(subscription);
        item.setItemType(ItemType.ADDON);
        item.setAddOn(addOn);
        item.setUnitPriceMinor(addOn.getPriceMinor());
        item.setQuantity(1);
        item.setTaxMode(addOn.getTaxMode());
        subscriptionItemRepository.save(item);

        LocalDate today = LocalDate.now();
        boolean isTrialing = subscription.getStatus() == Status.TRIALING;

        if (isTrialing) {
            // Update the existing OPEN trial invoice
            Optional<Invoice> openInvoiceOpt = invoiceRepository.findBySubscription_IdAndStatus(subscription.getId(), Status.OPEN);
            if (openInvoiceOpt.isPresent()) {
                Invoice invoice = openInvoiceOpt.get();
                
                // Add Add-on line
                InvoiceLineItem addonLine = new InvoiceLineItem();
                addonLine.setInvoice(invoice);
                addonLine.setDescription("Add-on: " + addOn.getName() + " (Starts after trial)");
                addonLine.setLineType(InvoiceLineItem.LineType.ADDON);
                addonLine.setQuantity(1);
                addonLine.setUnitPriceMinor(addOn.getPriceMinor());
                addonLine.setAmountMinor(addOn.getPriceMinor());
                invoiceLineItemRepository.save(addonLine);

                // Add Tax line for this addon
                Long addonTax = Math.round(addOn.getPriceMinor() * 0.18);
                InvoiceLineItem taxLine = new InvoiceLineItem();
                taxLine.setInvoice(invoice);
                taxLine.setDescription("GST (18%) - " + addOn.getName());
                taxLine.setLineType(InvoiceLineItem.LineType.TAX);
                taxLine.setQuantity(1);
                taxLine.setUnitPriceMinor(addonTax);
                taxLine.setAmountMinor(addonTax);
                invoiceLineItemRepository.save(taxLine);

                // Update invoice totals
                invoice.setSubtotalMinor(invoice.getSubtotalMinor() + addOn.getPriceMinor());
                invoice.setTaxMinor(invoice.getTaxMinor() + addonTax);
                invoice.setTotalMinor(invoice.getTotalMinor() + addOn.getPriceMinor() + addonTax);
                invoice.setBalanceMinor(invoice.getTotalMinor());
                invoiceRepository.save(invoice);
            }
        } else {
            // Generate a prorated invoice for the add-on immediately
            LocalDate periodEnd = subscription.getCurrentPeriodEnd();
            long totalDays = ChronoUnit.DAYS.between(subscription.getCurrentPeriodStart(), periodEnd);
            long remainingDays = ChronoUnit.DAYS.between(today, periodEnd);
            if (totalDays <= 0) totalDays = 1;
            if (remainingDays <= 0) remainingDays = 1;

            long proratedAmount = (addOn.getPriceMinor() * remainingDays) / totalDays;
            Long taxMinor = Math.round(proratedAmount * 0.18);
            Long totalMinor = proratedAmount + taxMinor;
            
            Invoice invoice = new Invoice();
            invoice.setCustomer(customer);
            invoice.setSubscription(subscription);
            invoice.setInvoiceNumber("INV-ADDON-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")));
            invoice.setStatus(Status.PAID);
            invoice.setBillingReason(BillingReason.SUBSCRIPTION_UPDATE);
            invoice.setIssueDate(today);
            invoice.setDueDate(today);
            invoice.setSubtotalMinor(proratedAmount);
            invoice.setTaxMinor(taxMinor);
            invoice.setDiscountMinor(0L);
            invoice.setTotalMinor(totalMinor);
            invoice.setBalanceMinor(0L);
            invoice.setCurrency(subscription.getCurrency());
            invoice.setIdempotencyKey(UUID.randomUUID().toString());
            invoiceRepository.save(invoice);

            // Line items
            InvoiceLineItem addonLine = new InvoiceLineItem();
            addonLine.setInvoice(invoice);
            addonLine.setDescription("Add-on: " + addOn.getName() + " (prorated)");
            addonLine.setLineType(InvoiceLineItem.LineType.ADDON);
            addonLine.setQuantity(1);
            addonLine.setUnitPriceMinor(proratedAmount);
            addonLine.setAmountMinor(proratedAmount);
            addonLine.setPeriodStart(today);
            addonLine.setPeriodEnd(periodEnd);
            invoiceLineItemRepository.save(addonLine);

            InvoiceLineItem taxLine = new InvoiceLineItem();
            taxLine.setInvoice(invoice);
            taxLine.setDescription("GST (18%)");
            taxLine.setLineType(InvoiceLineItem.LineType.TAX);
            taxLine.setQuantity(1);
            taxLine.setUnitPriceMinor(taxMinor);
            taxLine.setAmountMinor(taxMinor);
            invoiceLineItemRepository.save(taxLine);

            // Create payment
            PaymentMethod pm = paymentMethodRepository.findById(subscription.getPaymentMethodId()).orElse(null);
            Payment payment = new Payment();
            payment.setInvoice(invoice);
            payment.setPaymentMethod(pm);
            payment.setAmountMinor(totalMinor);
            payment.setCurrency(invoice.getCurrency());
            payment.setStatus(Status.SUCCESS);
            payment.setIdempotencyKey(UUID.randomUUID().toString());
            payment.setAttemptNo(1);
            paymentRepository.save(payment);
            paymentRepository.flush();
        }
        
        subscriptionItemRepository.flush();
        invoiceRepository.flush();

        return mapToSubscriptionDTO(subscription);
    }

   @Transactional
   public SubscriptionDTO removeAddOn(String email, Long addonId) {
       Customer customer = getCustomerByEmail(email);
       Subscription subscription = getActiveSubscription(customer.getId());
       
       SubscriptionItem item = subscriptionItemRepository.findBySubscription_IdAndAddOn_Id(
               subscription.getId(), addonId);
       if (item != null) {
           subscriptionItemRepository.delete(item);
       }

       return mapToSubscriptionDTO(subscription);
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

   // ==================== HELPER METHODS ====================

   private void generateProrationInvoice(Customer customer, Subscription subscription,
           Plan oldPlan, Plan newPlan, long amount, LocalDate periodStart, LocalDate periodEnd, String direction) {
       
       Invoice invoice = new Invoice();
       invoice.setCustomer(customer);
       invoice.setSubscription(subscription);
       invoice.setInvoiceNumber("INV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")));
       invoice.setStatus(amount > 0 ? Status.PAID : Status.PAID);
       invoice.setBillingReason(BillingReason.SUBSCRIPTION_UPDATE);
       invoice.setIssueDate(LocalDate.now());
       invoice.setDueDate(LocalDate.now());
       invoice.setSubtotalMinor(amount);
       invoice.setTaxMinor(0L);
       invoice.setDiscountMinor(0L);
       invoice.setTotalMinor(amount);
       invoice.setBalanceMinor(0L);
       invoice.setCurrency(customer.getCurrency());
       invoice.setIdempotencyKey(UUID.randomUUID().toString());
       invoiceRepository.save(invoice);

       // Credit line for old plan (remaining unused days)
       InvoiceLineItem creditLine = new InvoiceLineItem();
       creditLine.setInvoice(invoice);
       creditLine.setDescription("Plan change: " + oldPlan.getName() + " → " + newPlan.getName() + " (" + direction.toLowerCase() + ")");
       creditLine.setLineType(InvoiceLineItem.LineType.PRORATION);
       creditLine.setQuantity(1);
       creditLine.setUnitPriceMinor(amount);
       creditLine.setAmountMinor(amount);
       creditLine.setPeriodStart(periodStart);
       creditLine.setPeriodEnd(periodEnd);
       invoiceLineItemRepository.save(creditLine);

       // Create payment record
       if (amount > 0) {
           Payment payment = new Payment();
           payment.setInvoice(invoice);
           // Use customer's default payment method
           PaymentMethod pm = paymentMethodRepository.findById(subscription.getPaymentMethodId()).orElse(null);
           payment.setPaymentMethod(pm);
           payment.setIdempotencyKey(UUID.randomUUID().toString());
           payment.setGatewayRef("mock_proration_" + UUID.randomUUID().toString().substring(0, 8));
           payment.setAmountMinor(amount);
           payment.setCurrency(customer.getCurrency());
           payment.setStatus(Status.SUCCESS);
           payment.setAttemptNo(1);
           paymentRepository.save(payment);
       }
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

        Plan plan = subscription.getPlan();
        dto.setPlanName(plan.getName());
        dto.setPlanPriceMinor(plan.getDefaultPriceMinor());
        dto.setBillingPeriod(plan.getBillingPeriod() != null ? plan.getBillingPeriod().name() : "MONTHLY");

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
                .filter(i -> i.getItemType() == ItemType.ADDON)
                .map(this::mapToSubscriptionAddOnDTO)
                .collect(Collectors.toList()));

        dto.setMeteredUsage(items.stream()
                .filter(i -> i.getItemType() == ItemType.METERED)
                .map(this::mapToMeteredUsageDTO)
                .collect(Collectors.toList()));

        // Calculate discount if any
        Long discountMinor = 0L;
        Optional<SubscriptionCoupon> scOpt = subscriptionCouponRepository.findBySubscription_IdAndStatus(subscription.getId(), Status.ACTIVE);
        if (scOpt.isPresent()) {
            Coupon coupon = scOpt.get().getCoupon();
            if (coupon.getType() == com.infy.billing.enums.CouponType.PERCENT) {
                discountMinor = plan.getDefaultPriceMinor() * coupon.getAmount() / 100;
            } else {
                discountMinor = coupon.getAmount();
            }
        }
        dto.setDiscountMinor(discountMinor);

        // Calculate total due (Plan + Add-ons - Discount + 18% Tax)
        Long basePrice = plan.getDefaultPriceMinor() != null ? plan.getDefaultPriceMinor() : 0L;
        Long addOnTotal = items.stream()
                .filter(i -> i.getItemType() == ItemType.ADDON)
                .mapToLong(i -> i.getUnitPriceMinor() != null ? i.getUnitPriceMinor() * i.getQuantity() : 0L)
                .sum();
        
        Long subtotal = basePrice + addOnTotal - discountMinor;
        if (subtotal < 0) subtotal = 0L;
        
        Long taxMinor = Math.round(subtotal * 0.18);
        dto.setTotalDueMinor(subtotal + taxMinor);

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
