package com.infy.billing.service;

import com.infy.billing.dto.customer.*;
import com.infy.billing.entity.*;
import com.infy.billing.enums.*;
import com.infy.billing.exception.CustomException;

import com.infy.billing.repository.*;
import com.infy.billing.request.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

   private static final String NO_GATEWAY_TOKEN_MSG = "Payment method has no gateway token";

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
   private final PriceBookEntryRepository priceBookEntryRepository;
   private final MockPaymentGateway mockPaymentGateway;
   private final TaxRateRepository taxRateRepository;
   private final CreditNoteRepository creditNoteRepository;
   private final SubscriptionFlowService subscriptionFlowService;
   private final CancellationRequestRepository cancellationRequestRepository;

   public SubscriptionDTO getCurrentSubscription(String email) {
      Customer customer = getCustomerByEmail(email);
      Subscription subscription = subscriptionRepository.findByCustomer_IdAndStatusIn(
            customer.getId(),
            List.of(Status.ACTIVE, Status.TRIALING, Status.PAST_DUE, Status.PAUSED, Status.ON_HOLD)).stream()
            .findFirst().orElse(null);

      if (subscription == null)
         return null;
      return mapToSubscriptionDTO(subscription);
   }

   @Transactional
   public SubscriptionDTO createSubscription(String email, CreateSubscriptionRequest request) {
      Customer customer = getCustomerByEmail(email);

      if (subscriptionRepository.findByCustomer_IdAndStatusIn(customer.getId(),
            List.of(Status.ACTIVE, Status.TRIALING, Status.PAST_DUE)).size() > 0) {
         throw CustomException.conflict("Customer already has an active subscription");
      }

      Plan plan = planRepository.findById(request.getPlanId())
            .orElseThrow(() -> CustomException.notFound("Plan not found"));

      SubscriptionCompletionRequest compReq = new SubscriptionCompletionRequest();
      compReq.setPlanId(request.getPlanId());
      compReq.setPaymentMethodId(request.getPaymentMethodId());
      compReq.setBillingPeriod(plan.getBillingPeriod());
      compReq.setCouponCode(request.getCouponCode());

      SubscriptionResponse resp = subscriptionFlowService.completeSubscription(customer.getId(), compReq);

      Subscription subscription = subscriptionRepository.findById(resp.getSubscriptionId())
            .orElseThrow(() -> CustomException.notFound("Subscription not found after creation"));

      return mapToSubscriptionDTO(subscription);
   }

   @Transactional
   public SubscriptionDTO upgradeSubscription(String email, UpgradeSubscriptionRequest request) {
      Customer customer = getCustomerByEmail(email);
      Subscription subscription = getActiveSubscription(customer.getId());
      Plan oldPlan = subscription.getPlan();

      Plan newPlan = planRepository.findById(request.getPlanId())
            .orElseThrow(() -> CustomException.notFound("Plan not found"));

      if (oldPlan.getId().equals(newPlan.getId())) {
         throw CustomException.badRequest("Already on this plan");
      }

      boolean isTrialing = subscription.getStatus() == Status.TRIALING;
      Optional<SubscriptionCoupon> scOpt = subscriptionCouponRepository
            .findBySubscription_IdAndStatus(subscription.getId(), Status.ACTIVE);
      List<SubscriptionItem> items = subscriptionItemRepository.findBySubscription_Id(subscription.getId());

      // During trial: adjust trial end date and update any open trial invoice
      if (isTrialing) {
         LocalDate today = LocalDate.now();
         long daysUsed = ChronoUnit.DAYS.between(subscription.getStartDate(), today);
         if (daysUsed < 0)
            daysUsed = 0;

         long newTrialDays = Math.max(0, newPlan.getTrialDays() - daysUsed);
         LocalDate newTrialEndDate = today.plusDays(newTrialDays);

         subscription.setPlan(newPlan);
         subscription.setTrialEndDate(newTrialEndDate);
         if (newTrialDays == 0) {
            subscription.setStatus(Status.ACTIVE);
         }

         if (newPlan.getBillingPeriod() == BillingPeriod.MONTHLY) {
            subscription.setCurrentPeriodStart(newTrialEndDate);
            subscription.setCurrentPeriodEnd(newTrialEndDate.plusMonths(1));
         } else {
            subscription.setCurrentPeriodStart(newTrialEndDate);
            subscription.setCurrentPeriodEnd(newTrialEndDate.plusYears(1));
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

         // Update the OPEN trial invoice to show the new plan + tax + coupon + addons
         Optional<Invoice> openInvoiceOpt = invoiceRepository.findBySubscription_IdAndStatus(subscription.getId(),
               Status.OPEN);
         if (openInvoiceOpt.isPresent()) {
            Invoice invoice = openInvoiceOpt.get();

            // Delete old line items
            List<InvoiceLineItem> oldLines = invoiceLineItemRepository.findByInvoice_Id(invoice.getId());
            invoiceLineItemRepository.deleteAll(oldLines);
            invoiceLineItemRepository.flush();

            // Compute fresh pricing
            long priceMinor = newPlan.getDefaultPriceMinor();
            long discountMinor = 0L;
            Coupon coupon = null;
            if (scOpt.isPresent()) {
               coupon = scOpt.get().getCoupon();
               if (coupon.getType() == com.infy.billing.enums.CouponType.PERCENT) {
                  discountMinor = priceMinor * coupon.getAmount() / 100;
               } else {
                  if (coupon.getCurrency() == null || coupon.getCurrency().equals(customer.getCurrency())) {
                     discountMinor = coupon.getAmount();
                  } else {
                     discountMinor = 0L;
                  }
               }
            }

            long addonTotal = items.stream()
                  .filter(i -> i.getItemType() == ItemType.ADDON)
                  .mapToLong(i -> i.getUnitPriceMinor() * i.getQuantity())
                  .sum();

            long subtotal = priceMinor + addonTotal - discountMinor;
            if (subtotal < 0)
               subtotal = 0;
            long taxMinor = calculateTaxMinor(subtotal, customer.getCountry(), newPlan.getTaxMode());
            long totalMinor = (newPlan.getTaxMode() == TaxMode.INCLUSIVE) ? subtotal : subtotal + taxMinor;
            long subtotalMinor = (newPlan.getTaxMode() == TaxMode.INCLUSIVE) ? subtotal - taxMinor : subtotal;

            // Update invoice headers
            invoice.setSubtotalMinor(subtotalMinor);
            invoice.setDiscountMinor(discountMinor);
            invoice.setTaxMinor(taxMinor);
            invoice.setTotalMinor(totalMinor);
            invoice.setBalanceMinor(totalMinor);
            invoice.setDueDate(newTrialEndDate);
            invoiceRepository.save(invoice);

            // Save fresh Plan Line item
            InvoiceLineItem planLine = new InvoiceLineItem();
            planLine.setInvoice(invoice);
            planLine.setDescription(
                  newPlan.getName() + " Subscription" + (newTrialDays > 0 ? " (Starts after trial)" : ""));
            planLine.setLineType(InvoiceLineItem.LineType.PLAN);
            planLine.setQuantity(1);
            planLine.setUnitPriceMinor(priceMinor);
            planLine.setAmountMinor(priceMinor);
            invoiceLineItemRepository.save(planLine);

            // Save Addon Lines
            for (SubscriptionItem item : items) {
               if (item.getItemType() == ItemType.ADDON) {
                  InvoiceLineItem addonLine = new InvoiceLineItem();
                  addonLine.setInvoice(invoice);
                  addonLine.setDescription("Add-on: " + item.getAddOn().getName() + " (Starts after trial)");
                  addonLine.setLineType(InvoiceLineItem.LineType.ADDON);
                  addonLine.setQuantity(item.getQuantity());
                  addonLine.setUnitPriceMinor(item.getUnitPriceMinor());
                  addonLine.setAmountMinor(item.getUnitPriceMinor() * item.getQuantity());
                  invoiceLineItemRepository.save(addonLine);
               }
            }

            // Save Discount Line
            if (discountMinor > 0 && coupon != null) {
               InvoiceLineItem discountLine = new InvoiceLineItem();
               discountLine.setInvoice(invoice);
               discountLine.setDescription("Coupon: " + coupon.getCode() + " (" + coupon.getName() + ")");
               discountLine.setLineType(InvoiceLineItem.LineType.DISCOUNT);
               discountLine.setQuantity(1);
               discountLine.setUnitPriceMinor(-discountMinor);
               discountLine.setAmountMinor(-discountMinor);
               invoiceLineItemRepository.save(discountLine);
            }

            // Save Tax Line
            if (taxMinor > 0) {
               InvoiceLineItem taxLine = new InvoiceLineItem();
               taxLine.setInvoice(invoice);
               taxLine.setDescription(getTaxDescription(customer.getCountry()));
               taxLine.setLineType(InvoiceLineItem.LineType.TAX);
               taxLine.setQuantity(1);
               taxLine.setUnitPriceMinor(taxMinor);
               taxLine.setAmountMinor(taxMinor);
               invoiceLineItemRepository.save(taxLine);
            }
         }

         return mapToSubscriptionDTO(subscription);
      }

      // Active subscription: calculate proration and reset billing period starting
      // today
      LocalDate today = LocalDate.now();
      LocalDate periodStart = subscription.getCurrentPeriodStart();
      LocalDate periodEnd = subscription.getCurrentPeriodEnd();

      long totalDaysInPeriod = ChronoUnit.DAYS.between(periodStart, periodEnd);
      long remainingDays = ChronoUnit.DAYS.between(today, periodEnd);

      if (totalDaysInPeriod <= 0)
         totalDaysInPeriod = 1;
      if (remainingDays <= 0)
         remainingDays = 0;

      // Old Plan calculations
      long oldPriceMinor = oldPlan.getDefaultPriceMinor();
      long oldDiscountMinor = 0L;
       if (scOpt.isPresent()) {
          Coupon coupon = scOpt.get().getCoupon();
          if (coupon.getType() == com.infy.billing.enums.CouponType.PERCENT) {
             oldDiscountMinor = oldPriceMinor * coupon.getAmount() / 100;
          } else {
             if (coupon.getCurrency() == null || coupon.getCurrency().equals(customer.getCurrency())) {
                oldDiscountMinor = coupon.getAmount();
             } else {
                oldDiscountMinor = 0L;
             }
          }
       }
      long oldAddonTotal = items.stream()
            .filter(i -> i.getItemType() == ItemType.ADDON)
            .mapToLong(i -> i.getUnitPriceMinor() * i.getQuantity())
            .sum();

      long oldSubtotal = oldPriceMinor + oldAddonTotal - oldDiscountMinor;
      if (oldSubtotal < 0)
         oldSubtotal = 0;
      long oldTaxMinor = calculateTaxMinor(oldSubtotal, customer.getCountry(), oldPlan.getTaxMode());
      long oldTotalPaid = (oldPlan.getTaxMode() == TaxMode.INCLUSIVE) ? oldSubtotal : oldSubtotal + oldTaxMinor;

      // Calculate unused refund credit we owe the user
      long unusedCredit = oldTotalPaid * remainingDays / totalDaysInPeriod;

      // New Plan calculations
      long newPriceMinor = newPlan.getDefaultPriceMinor();
      long newDiscountMinor = 0L;
      Coupon activeCoupon = null;
       if (scOpt.isPresent()) {
          activeCoupon = scOpt.get().getCoupon();
          if (activeCoupon.getType() == com.infy.billing.enums.CouponType.PERCENT) {
             newDiscountMinor = newPriceMinor * activeCoupon.getAmount() / 100;
          } else {
             if (activeCoupon.getCurrency() == null || activeCoupon.getCurrency().equals(customer.getCurrency())) {
                newDiscountMinor = activeCoupon.getAmount();
             } else {
                newDiscountMinor = 0L;
             }
          }
       }
      long newAddonTotal = items.stream()
            .filter(i -> i.getItemType() == ItemType.ADDON)
            .mapToLong(i -> i.getUnitPriceMinor() * i.getQuantity())
            .sum();

      long newSubtotal = newPriceMinor + newAddonTotal - newDiscountMinor;
      if (newSubtotal < 0)
         newSubtotal = 0;
      long newTaxMinor = calculateTaxMinor(newSubtotal, customer.getCountry(), newPlan.getTaxMode());
      long newTotalCost = (newPlan.getTaxMode() == TaxMode.INCLUSIVE) ? newSubtotal : newSubtotal + newTaxMinor;

      // Immediate prorated charge amount
      long prorationAmount = newTotalCost - unusedCredit;

      // Generate immediate proration invoice
      long headerSubtotal = (newPlan.getTaxMode() == TaxMode.INCLUSIVE) 
            ? (newPriceMinor + newAddonTotal - unusedCredit - newTaxMinor) 
            : (newPriceMinor + newAddonTotal - unusedCredit);

      Invoice invoice = Invoice.builder()
              .customer(customer)
              .subscription(subscription)
              .invoiceNumber("INV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")))
              .billingReason(BillingReason.SUBSCRIPTION_UPDATE)
              .issueDate(today)
              .subtotalMinor(headerSubtotal)
              .taxMinor(newTaxMinor)
              .currency(customer.getCurrency())
              .idempotencyKey(UUID.randomUUID().toString())
              .build();

      if (prorationAmount > 0) {
         // Upgrade: charge the difference
         invoice.setStatus(Status.PAID);
         invoice.setDiscountMinor(newDiscountMinor);
         invoice.setTotalMinor(prorationAmount);
         invoice.setBalanceMinor(0L);
      } else if (prorationAmount < 0) {
         // Downgrade: credit the difference to customer account
         long creditAmount = Math.abs(prorationAmount);
         customer.setCreditBalanceMinor(customer.getCreditBalanceMinor() + creditAmount);
         customerRepository.save(customer);

         invoice.setStatus(Status.PAID);
         invoice.setDiscountMinor(headerSubtotal + newTaxMinor); // Keep total at 0 by matching subtotal + tax
         invoice.setTotalMinor(0L);
         invoice.setBalanceMinor(0L);

         invoiceRepository.save(invoice);

         User currentUser = userRepository.findByEmail(email)
               .orElseThrow(() -> CustomException.notFound("User not found."));

         // Create credit note for audit trail
         CreditNote creditNote = new CreditNote();
         creditNote
               .setCreditNoteNumber("CN-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")));
         creditNote.setInvoice(invoice);
         creditNote.setReason("Proration credit from plan downgrade: " + oldPlan.getName() + " → " + newPlan.getName());
         creditNote.setAmountMinor(creditAmount);
         creditNote.setStatus(Status.ISSUED);
         creditNote.setCreatedBy(currentUser);
         creditNoteRepository.save(creditNote);
      } else {
         // Zero difference
         invoice.setStatus(Status.PAID);
         invoice.setDiscountMinor(newDiscountMinor);
         invoice.setTotalMinor(0L);
         invoice.setBalanceMinor(0L);
      }
      invoiceRepository.save(invoice);

      // Save New Plan Line
      InvoiceLineItem planLine = new InvoiceLineItem();
      planLine.setInvoice(invoice);
      planLine.setDescription("Plan change to " + newPlan.getName() + " (Cycle starting today)");
      planLine.setLineType(InvoiceLineItem.LineType.PLAN);
      planLine.setQuantity(1);
      planLine.setUnitPriceMinor(newPriceMinor);
      planLine.setAmountMinor(newPriceMinor);
      planLine.setPeriodStart(today);
      planLine.setPeriodEnd(
            newPlan.getBillingPeriod() == BillingPeriod.MONTHLY ? today.plusMonths(1) : today.plusYears(1));
      invoiceLineItemRepository.save(planLine);

      // Save Unused Credit line subtraction
      InvoiceLineItem creditLine = new InvoiceLineItem();
      creditLine.setInvoice(invoice);
      creditLine.setDescription("Unused credit from " + oldPlan.getName() + " (" + remainingDays + " days remaining)");
      creditLine.setLineType(InvoiceLineItem.LineType.PRORATION);
      creditLine.setQuantity(1);
      creditLine.setUnitPriceMinor(-unusedCredit);
      creditLine.setAmountMinor(-unusedCredit);
      creditLine.setPeriodStart(today);
      creditLine.setPeriodEnd(periodEnd);
      invoiceLineItemRepository.save(creditLine);

      // Save Tax Line
      if (newTaxMinor > 0) {
         InvoiceLineItem taxLine = new InvoiceLineItem();
         taxLine.setInvoice(invoice);
         taxLine.setDescription(getTaxDescription(customer.getCountry()));
         taxLine.setLineType(InvoiceLineItem.LineType.TAX);
         taxLine.setQuantity(1);
         taxLine.setUnitPriceMinor(newTaxMinor);
         taxLine.setAmountMinor(newTaxMinor);
         invoiceLineItemRepository.save(taxLine);
      }

      // Save Addon Lines
      for (SubscriptionItem item : items) {
         if (item.getItemType() == ItemType.ADDON) {
            InvoiceLineItem addonLine = new InvoiceLineItem();
            addonLine.setInvoice(invoice);
            addonLine.setDescription("Add-on: " + item.getAddOn().getName());
            addonLine.setLineType(InvoiceLineItem.LineType.ADDON);
            addonLine.setQuantity(item.getQuantity());
            addonLine.setUnitPriceMinor(item.getUnitPriceMinor());
            addonLine.setAmountMinor(item.getUnitPriceMinor() * item.getQuantity());
            invoiceLineItemRepository.save(addonLine);
         }
      }

      // Save Discount Line
      if (newDiscountMinor > 0 && activeCoupon != null) {
         InvoiceLineItem discountLine = new InvoiceLineItem();
         discountLine.setInvoice(invoice);
         discountLine.setDescription("Coupon: " + activeCoupon.getCode());
         discountLine.setLineType(InvoiceLineItem.LineType.DISCOUNT);
         discountLine.setQuantity(1);
         discountLine.setUnitPriceMinor(-newDiscountMinor);
         discountLine.setAmountMinor(-newDiscountMinor);
         invoiceLineItemRepository.save(discountLine);
      }

      // Create the immediate payment record if upgrade (positive amount)
      if (prorationAmount > 0) {
         PaymentMethod pm = paymentMethodRepository.findById(subscription.getPaymentMethodId())
               .orElseThrow(() -> CustomException.notFound("Payment method not found"));
         if (pm.getGatewayToken() == null || pm.getGatewayToken().isBlank()) {
            throw CustomException.badRequest(NO_GATEWAY_TOKEN_MSG);
         }
         String gatewayRef = mockPaymentGateway.charge(pm.getGatewayToken(), prorationAmount, customer.getCurrency());

         Payment payment = new Payment();
         payment.setInvoice(invoice);
         payment.setPaymentMethod(pm);
         payment.setIdempotencyKey(UUID.randomUUID().toString());
         payment.setGatewayRef(gatewayRef);
         payment.setAmountMinor(prorationAmount);
         payment.setCurrency(customer.getCurrency());
         payment.setStatus(Status.SUCCESS);
         payment.setAttemptNo(1);
         paymentRepository.save(payment);
      }

      // Reset subscription period and save
      subscription.setPlan(newPlan);
      subscription.setCurrentPeriodStart(today);
      subscription.setCurrentPeriodEnd(
            newPlan.getBillingPeriod() == BillingPeriod.MONTHLY ? today.plusMonths(1) : today.plusYears(1));
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

    @Transactional
    public CancellationResponse cancelSubscription(String email, boolean atPeriodEnd) {
       Customer customer = getCustomerByEmail(email);
       Subscription subscription = getActiveSubscription(customer.getId());

       if (atPeriodEnd) {
          subscription.setCancelAtPeriodEnd(true);
          subscriptionRepository.save(subscription);
          return new CancellationResponse(false, 0, customer.getCurrency(), null, null,
                "Subscription will be canceled at the end of the current billing period.");
       } else {
          subscription.setStatus(Status.CANCELED);
          subscription.setCanceledAt(LocalDateTime.now());
          subscriptionRepository.save(subscription);

          List<Invoice> invoices = invoiceRepository.findByCustomer_IdOrderByIssueDateDesc(customer.getId());
          voidOpenInvoices(subscription.getId(), invoices);
          Invoice lastPaidInvoice = findLastPaidInvoice(subscription.getId(), invoices);
          CancellationResponse response = processRefundIfApplicable(email, customer, subscription, lastPaidInvoice);
          updateCustomerStatusIfNoActiveSubscriptions(customer);

          return response;
       }
    }

    private void voidOpenInvoices(Long subscriptionId, List<Invoice> invoices) {
       invoices.stream()
               .filter(inv -> inv.getSubscription() != null
                       && inv.getSubscription().getId().equals(subscriptionId)
                       && inv.getStatus() == Status.OPEN)
               .forEach(openInv -> {
                   openInv.setStatus(Status.VOID);
                   invoiceRepository.save(openInv);
               });
    }

    private Invoice findLastPaidInvoice(Long subscriptionId, List<Invoice> invoices) {
       return invoices.stream()
               .filter(inv -> inv.getSubscription() != null
                       && inv.getSubscription().getId().equals(subscriptionId)
                       && inv.getStatus() == Status.PAID)
               .findFirst()
               .orElse(null);
    }

    private CancellationResponse processRefundIfApplicable(String email, Customer customer, Subscription subscription, Invoice lastPaidInvoice) {
       if (lastPaidInvoice == null) {
           return new CancellationResponse(false, 0, customer.getCurrency(), null, null,
                 "Subscription canceled. No refund applicable.");
       }

       LocalDate today = LocalDate.now();
       long totalDays = ChronoUnit.DAYS.between(subscription.getCurrentPeriodStart(), subscription.getCurrentPeriodEnd());
       long remainingDays = ChronoUnit.DAYS.between(today, subscription.getCurrentPeriodEnd());

       if (remainingDays <= 0 || totalDays <= 0) {
           return new CancellationResponse(false, 0, customer.getCurrency(), null, null,
                 "Subscription canceled. No refund applicable.");
       }

       long refundAmountMinor = (lastPaidInvoice.getTotalMinor() * remainingDays) / totalDays;

       User currentUser = userRepository.findByEmail(email)
               .orElseThrow(() -> CustomException.notFound("User not found."));

       // Issue mock refund via payment gateway
       Payment originalPayment = paymentRepository.findByInvoice_Id(lastPaidInvoice.getId())
               .stream().filter(p -> p.getStatus() == Status.SUCCESS).findFirst().orElse(null);
       String refundGatewayRef = mockPaymentGateway.refund(
               originalPayment != null ? originalPayment.getGatewayRef() : "unknown",
               refundAmountMinor, customer.getCurrency());

       // Create credit note for audit trail
       CreditNote creditNote = new CreditNote();
       String cnNumber = "CN-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
       creditNote.setCreditNoteNumber(cnNumber);
       creditNote.setInvoice(lastPaidInvoice);
       creditNote.setAmountMinor(refundAmountMinor);
       creditNote.setReason("Mid-cycle cancellation refund for " + subscription.getPlan().getName());
       creditNote.setStatus(Status.APPLIED);
       creditNote.setCreatedBy(currentUser);
       creditNoteRepository.save(creditNote);

       return new CancellationResponse(true, refundAmountMinor, customer.getCurrency(),
             refundGatewayRef, cnNumber,
             "Refund of " + refundAmountMinor + " minor units processed successfully.");
    }

    private void updateCustomerStatusIfNoActiveSubscriptions(Customer customer) {
       List<Subscription> otherSubs = subscriptionRepository.findByCustomer_Id(customer.getId());
       boolean hasActive = otherSubs.stream()
               .anyMatch(s -> s.getStatus() == Status.ACTIVE || s.getStatus() == Status.TRIALING || s.getStatus() == Status.PAST_DUE);

       if (!hasActive) {
           customer.setStatus(Status.INACTIVE);
           customerRepository.save(customer);
       }
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
            .orElseThrow(() -> CustomException.notFound("No paused subscription found"));

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
            .orElseThrow(() -> CustomException.notFound("Add-on not found"));

      // Compatibility check: Monthly add-ons only for monthly plans, etc.
      if (addOn.getBillingPeriod() != subscription.getPlan().getBillingPeriod()) {
         throw CustomException.badRequest("Add-on billing period (" + addOn.getBillingPeriod() +
               ") must match your plan's billing period (" + subscription.getPlan().getBillingPeriod() + ")");
      }

      // Check if already added
      SubscriptionItem existing = subscriptionItemRepository.findBySubscription_IdAndAddOn_Id(
            subscription.getId(), addonId);
      if (existing != null) {
         throw CustomException.conflict("Add-on already active on this subscription");
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
         Optional<Invoice> openInvoiceOpt = invoiceRepository.findBySubscription_IdAndStatus(subscription.getId(),
               Status.OPEN);
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
            Long addonTax = calculateTaxMinor(addOn.getPriceMinor(), customer.getCountry(), addOn.getTaxMode());
            Long addonTotal = (addOn.getTaxMode() == TaxMode.INCLUSIVE) ? addOn.getPriceMinor()
                  : addOn.getPriceMinor() + addonTax;
            Long addonSubtotal = (addOn.getTaxMode() == TaxMode.INCLUSIVE) ? addOn.getPriceMinor() - addonTax
                  : addOn.getPriceMinor();

            InvoiceLineItem taxLine = new InvoiceLineItem();
            taxLine.setInvoice(invoice);
            taxLine.setDescription(getTaxDescription(customer.getCountry()) + " - " + addOn.getName());
            taxLine.setLineType(InvoiceLineItem.LineType.TAX);
            taxLine.setQuantity(1);
            taxLine.setUnitPriceMinor(addonTax);
            taxLine.setAmountMinor(addonTax);
            invoiceLineItemRepository.save(taxLine);

            // Update invoice totals
            invoice.setSubtotalMinor(invoice.getSubtotalMinor() + addonSubtotal);
            invoice.setTaxMinor(invoice.getTaxMinor() + addonTax);
            invoice.setTotalMinor(invoice.getTotalMinor() + addonTotal);
            invoice.setBalanceMinor(invoice.getTotalMinor());
            invoiceRepository.save(invoice);
         }
      } else {
         // Generate a prorated invoice for the add-on immediately
         LocalDate periodEnd = subscription.getCurrentPeriodEnd();
         long totalDays = ChronoUnit.DAYS.between(subscription.getCurrentPeriodStart(), periodEnd);
         long remainingDays = ChronoUnit.DAYS.between(today, periodEnd);
         if (totalDays <= 0)
            totalDays = 1;
         if (remainingDays <= 0)
            remainingDays = 1;
         long proratedAmount = (addOn.getPriceMinor() * remainingDays) / totalDays;
         Long taxMinor = calculateTaxMinor(proratedAmount, customer.getCountry(), addOn.getTaxMode());
         Long totalMinor = (addOn.getTaxMode() == TaxMode.INCLUSIVE) ? proratedAmount : proratedAmount + taxMinor;
         Long subtotalMinor = (addOn.getTaxMode() == TaxMode.INCLUSIVE) ? proratedAmount - taxMinor : proratedAmount;

         Invoice invoice = Invoice.builder()
                 .customer(customer)
                 .subscription(subscription)
                 .invoiceNumber("INV-ADDON-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")))
                 .status(Status.PAID)
                 .billingReason(BillingReason.SUBSCRIPTION_UPDATE)
                 .issueDate(today)
                 .dueDate(today)
                 .subtotalMinor(subtotalMinor)
                 .taxMinor(taxMinor)
                 .discountMinor(0L)
                 .totalMinor(totalMinor)
                 .balanceMinor(0L)
                 .currency(subscription.getCurrency())
                 .idempotencyKey(UUID.randomUUID().toString())
                 .build();
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
         taxLine.setDescription(getTaxDescription(customer.getCountry()));
         taxLine.setLineType(InvoiceLineItem.LineType.TAX);
         taxLine.setQuantity(1);
         taxLine.setUnitPriceMinor(taxMinor);
         taxLine.setAmountMinor(taxMinor);
         invoiceLineItemRepository.save(taxLine);

         // Auto-apply customer account credit before charging
         long amountToCharge = totalMinor;
         if (customer.getCreditBalanceMinor() > 0 && totalMinor > 0) {
            long creditApplied = Math.min(customer.getCreditBalanceMinor(), totalMinor);
            amountToCharge = totalMinor - creditApplied;

            InvoiceLineItem creditLine = new InvoiceLineItem();
            creditLine.setInvoice(invoice);
            creditLine.setDescription("Account Credit Applied");
            creditLine.setLineType(InvoiceLineItem.LineType.DISCOUNT);
            creditLine.setQuantity(1);
            creditLine.setUnitPriceMinor(-creditApplied);
            creditLine.setAmountMinor(-creditApplied);
            invoiceLineItemRepository.save(creditLine);

            invoice.setTotalMinor(amountToCharge);
            invoice.setBalanceMinor(amountToCharge);
            invoiceRepository.save(invoice);

            customer.setCreditBalanceMinor(customer.getCreditBalanceMinor() - creditApplied);
            customerRepository.save(customer);
         }

         // Create payment via mock gateway for remaining amount
         if (amountToCharge > 0) {
            PaymentMethod pm = paymentMethodRepository.findById(subscription.getPaymentMethodId())
                  .orElseThrow(() -> CustomException.notFound("Payment method not found"));
            if (pm.getGatewayToken() == null || pm.getGatewayToken().isBlank()) {
               throw CustomException.badRequest(NO_GATEWAY_TOKEN_MSG);
            }
            String gatewayRef = mockPaymentGateway.charge(pm.getGatewayToken(), amountToCharge, invoice.getCurrency());

            Payment payment = new Payment();
            payment.setInvoice(invoice);
            payment.setPaymentMethod(pm);
            payment.setAmountMinor(amountToCharge);
            payment.setCurrency(invoice.getCurrency());
            payment.setStatus(Status.SUCCESS);
            payment.setIdempotencyKey(UUID.randomUUID().toString());
            payment.setGatewayRef(gatewayRef);
            payment.setAttemptNo(1);
            paymentRepository.save(payment);
         }
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

      List<UsageRecord> usage = usageRecordRepository
            .findBySubscription_IdAndBillingPeriodStartGreaterThanEqualAndBillingPeriodEndLessThanEqual(
                  subscription.getId(), start, end);

      return usage.stream().map(this::mapToUsageRecordDTO).collect(Collectors.toList());
   }

   // ==================== HELPER METHODS ====================

   private Customer getCustomerByEmail(String email) {
      User user = userRepository.findByEmail(email)
            .orElseThrow(() -> CustomException.notFound("User not found"));
      return customerRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> CustomException.notFound("Customer not found"));
   }

   private Subscription getActiveSubscription(Long customerId) {
      return subscriptionRepository.findByCustomer_IdAndStatusIn(customerId,
            List.of(Status.ACTIVE, Status.TRIALING, Status.PAST_DUE, Status.PAUSED, Status.ON_HOLD)).stream()
            .findFirst()
            .orElseThrow(() -> CustomException.notFound("No active subscription found"));
   }

   private SubscriptionDTO mapToSubscriptionDTO(Subscription subscription) {
      SubscriptionDTO dto = new SubscriptionDTO();
      dto.setSubscriptionId(subscription.getId());
      dto.setCustomerId(subscription.getCustomer().getId());
      dto.setPlanId(subscription.getPlan().getId());

      Plan plan = subscription.getPlan();
      dto.setPlanName(plan.getName());

      // Fetch region and currency specific plan price
      Long basePrice = priceBookEntryRepository.findByPlan_IdAndRegionAndCurrency(
            plan.getId(), subscription.getCustomer().getCountry(), subscription.getCurrency()
      ).map(PriceBookEntry::getPriceMinor).orElse(plan.getDefaultPriceMinor() != null ? plan.getDefaultPriceMinor() : 0L);

      dto.setPlanPriceMinor(basePrice);
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
      Optional<SubscriptionCoupon> scOpt = subscriptionCouponRepository
            .findBySubscription_IdAndStatus(subscription.getId(), Status.ACTIVE);
      if (scOpt.isPresent()) {
         Coupon coupon = scOpt.get().getCoupon();
         if (coupon.getType() == com.infy.billing.enums.CouponType.PERCENT) {
            discountMinor = basePrice * coupon.getAmount() / 100;
         } else {
            if (coupon.getCurrency() == null || coupon.getCurrency().equals(subscription.getCurrency())) {
               discountMinor = coupon.getAmount();
            } else {
               discountMinor = 0L;
            }
         }
      }
      dto.setDiscountMinor(discountMinor);

      // Calculate total due (Plan + Add-ons - Discount + dynamic tax)
      Long addOnTotal = items.stream()
            .filter(i -> i.getItemType() == ItemType.ADDON)
            .mapToLong(i -> i.getUnitPriceMinor() != null ? i.getUnitPriceMinor() * i.getQuantity() : 0L)
            .sum();

      Long subtotal = basePrice + addOnTotal - discountMinor;
      if (subtotal < 0)
         subtotal = 0L;

      String country = subscription.getCustomer().getCountry();
      Long taxMinor = calculateTaxMinor(subtotal, country, subscription.getPlan().getTaxMode());
      dto.setTotalDueMinor((subscription.getPlan().getTaxMode() == TaxMode.INCLUSIVE) ? subtotal : subtotal + taxMinor);
      dto.setCreditBalanceMinor(subscription.getCustomer().getCreditBalanceMinor());

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

   private long calculateTaxMinor(long amount, String country, TaxMode taxMode) {
      Optional<TaxRate> taxRateOpt = taxRateRepository.findByRegionAndEffectiveToIsNullOrFuture(country,
            LocalDate.now());
      if (taxRateOpt.isEmpty()) {
         return 0L;
      }
      BigDecimal rate = taxRateOpt.get().getRatePercent();

      if (taxMode == TaxMode.INCLUSIVE) {
         BigDecimal rateFactor = BigDecimal.ONE.add(rate.divide(BigDecimal.valueOf(100)));
         long baseMinor = BigDecimal.valueOf(amount).divide(rateFactor, 0, java.math.RoundingMode.HALF_UP).longValue();
         return amount - baseMinor;
      }

      return BigDecimal.valueOf(amount)
            .multiply(rate)
            .divide(BigDecimal.valueOf(100), 0, java.math.RoundingMode.HALF_UP)
            .longValue();
   }

   private String getTaxDescription(String country) {
      Optional<TaxRate> taxRateOpt = taxRateRepository.findByRegionAndEffectiveToIsNullOrFuture(country,
            LocalDate.now());
      if (taxRateOpt.isEmpty()) {
         return "Tax";
      }
      BigDecimal rate = taxRateOpt.get().getRatePercent();
      return "Tax (" + rate.stripTrailingZeros().toPlainString() + "%)";
   }

   @Override
   public CancellationRequestDTO createCancellationRequest(String email, CancellationRequestInput input) {
      Customer customer = getCustomerByEmail(email);
      Subscription subscription = getActiveSubscription(customer.getId());

      Optional<CancellationRequest> existing = cancellationRequestRepository
            .findBySubscription_Customer_IdAndStatus(customer.getId(), CancellationRequestStatus.PENDING);
      if (existing.isPresent()) {
         throw new RuntimeException("There is already a pending cancellation request for this customer");
      }

      CancellationRequest request = CancellationRequest.builder()
            .subscription(subscription)
            .reason(input.getReason())
            .status(CancellationRequestStatus.PENDING)
            .atPeriodEnd(input.isAtPeriodEnd())
            .build();

      CancellationRequest saved = cancellationRequestRepository.save(request);
      return mapToCancellationRequestDTO(saved);
   }

   @Override
   public CancellationRequestDTO withdrawCancellationRequest(String email) {
      Customer customer = getCustomerByEmail(email);
      CancellationRequest request = cancellationRequestRepository
            .findBySubscription_Customer_IdAndStatus(customer.getId(), CancellationRequestStatus.PENDING)
            .orElseThrow(() -> new RuntimeException("No pending cancellation request found"));

      request.setStatus(CancellationRequestStatus.WITHDRAWN);
      CancellationRequest saved = cancellationRequestRepository.save(request);
      return mapToCancellationRequestDTO(saved);
   }

   @Override
   public CancellationRequestDTO getPendingCancellationRequest(String email) {
      Customer customer = getCustomerByEmail(email);
      return cancellationRequestRepository
            .findBySubscription_Customer_IdAndStatus(customer.getId(), CancellationRequestStatus.PENDING)
            .map(this::mapToCancellationRequestDTO)
            .orElse(null);
   }

   private CancellationRequestDTO mapToCancellationRequestDTO(CancellationRequest request) {
      CancellationRequestDTO dto = new CancellationRequestDTO();
      dto.setRequestId(request.getId());
      dto.setSubscriptionId(request.getSubscription().getId());
      dto.setPlanName(request.getSubscription().getPlan().getName());
      dto.setCustomerEmail(request.getSubscription().getCustomer().getUser().getEmail());
      dto.setCustomerName(request.getSubscription().getCustomer().getUser().getFullName());
      dto.setReason(request.getReason());
      dto.setStatus(request.getStatus());
      dto.setAtPeriodEnd(request.getAtPeriodEnd());
      dto.setCreatedAt(request.getCreatedAt() != null ? request.getCreatedAt().toString() : null);
      dto.setProcessedByEmail(request.getProcessedBy() != null ? request.getProcessedBy().getEmail() : null);
      dto.setProcessedAt(request.getProcessedAt() != null ? request.getProcessedAt().toString() : null);
      dto.setAgentNotes(request.getAgentNotes());
      return dto;
   }
}
