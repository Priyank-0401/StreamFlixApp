package com.infy.billing.service;

import com.infy.billing.dto.customer.*;
import com.infy.billing.entity.*;
import com.infy.billing.enums.*;
import com.infy.billing.exception.CustomException;
import com.infy.billing.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionFlowServiceImpl implements SubscriptionFlowService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final PriceBookEntryRepository priceBookEntryRepository;
    private final TaxRateRepository taxRateRepository;
    private final SubscriptionItemRepository subscriptionItemRepository;
    private final CouponRepository couponRepository;
    private final SubscriptionCouponRepository subscriptionCouponRepository;
    private final InvoiceLineItemRepository invoiceLineItemRepository;
    private final MockPaymentGateway mockPaymentGateway;

    /**
     * Step 1: Register customer details
     */
    @Transactional
    public Customer registerCustomerDetails(String email, CustomerRegistrationRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> CustomException.notFound("User not found"));

        // Check if customer already exists
        Optional<Customer> existingCustomer = customerRepository.findByUser_Id(user.getId());
        if (existingCustomer.isPresent()) {
            return existingCustomer.get();
        }

        Customer customer = new Customer();
        customer.setUser(user);
        customer.setPhone(request.getPhone());
        customer.setCountry(request.getCountry());
        customer.setState(request.getState());
        customer.setCity(request.getCity());
        customer.setAddressLine1(request.getAddressLine1());
        customer.setPostalCode(request.getPostalCode());
        customer.setCurrency(request.getCurrency());
        customer.setStatus(Status.ACTIVE);

        customerRepository.save(customer);
        System.out.println("DEBUG: Customer registered for user: " + email + ", customerId: " + customer.getId());

        return customer;
    }

    /**
     * Step 2: Create payment method
     */
    @Transactional
    public PaymentMethod createPaymentMethod(Long customerId, PaymentMethodRequest request) {
        Long cid = customerId;
        if (cid == null) throw CustomException.badRequest("Customer ID is required");
        Customer customer = customerRepository.findById(cid)
                .orElseThrow(() -> CustomException.notFound("Customer not found"));

        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setCustomer(customer);
        paymentMethod.setPaymentType(request.getPaymentType());
        paymentMethod.setIsDefault(true);
        paymentMethod.setStatus(Status.ACTIVE);

        if (request.getPaymentType() == PaymentType.CARD) {
            // Validate and extract card details
            String cardNumber = request.getCardNumber();
            String last4 = cardNumber.substring(cardNumber.length() - 4);
            String brand = detectCardBrand(cardNumber);

            paymentMethod.setCardLast4(last4);
            paymentMethod.setCardBrand(brand);
            paymentMethod.setExpiryMonth(Integer.parseInt(request.getExpiryMonth()));
            paymentMethod.setExpiryYear(Integer.parseInt(request.getExpiryYear()));
            paymentMethod.setGatewayToken("mock_token_card_" + cardNumber);

        } else if (request.getPaymentType() == PaymentType.UPI) {
            paymentMethod.setUpiId(request.getUpiId());
            paymentMethod.setGatewayToken("mock_token_upi_" + request.getUpiId());
        }

        paymentMethodRepository.save(paymentMethod);
        System.out.println(
                "DEBUG: Payment method created for customer: " + customerId + ", methodId: " + paymentMethod.getId());

        return paymentMethod;
    }

    /**
     * Step 3: Complete subscription
     */
    @Transactional
    public SubscriptionResponse completeSubscription(Long customerId, SubscriptionCompletionRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> CustomException.notFound("Customer not found"));

        Long planId = request.getPlanId();
        if (planId == null) throw CustomException.badRequest("Plan ID is required");
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> CustomException.notFound("Plan not found"));

        if (plan.getStatus() == Status.INACTIVE) {
            throw CustomException.badRequest("This plan is no longer available for new subscriptions.");
        }

        Long paymentMethodId = request.getPaymentMethodId();
        if (paymentMethodId == null) throw CustomException.badRequest("Payment method ID is required");
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> CustomException.notFound("Payment method not found"));

        // Calculate price based on region
        Long priceMinor = getPriceForRegion(plan, customer.getCountry(), customer.getCurrency());

        // Apply coupon discount if provided
        LocalDate today = LocalDate.now();
        Long discountMinor = 0L;
        Coupon appliedCoupon = null;
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            appliedCoupon = couponRepository.findByCodeAndStatus(request.getCouponCode(), Status.ACTIVE)
                    .orElse(null);
            if (appliedCoupon != null) {
                boolean valid = true;
                if (appliedCoupon.getValidFrom() != null && today.isBefore(appliedCoupon.getValidFrom()))
                    valid = false;
                if (appliedCoupon.getValidTo() != null && today.isAfter(appliedCoupon.getValidTo()))
                    valid = false;
                if (appliedCoupon.getMaxRedemptions() != null
                        && appliedCoupon.getRedeemedCount() >= appliedCoupon.getMaxRedemptions())
                    valid = false;

                if (valid) {
                    if (appliedCoupon.getType() == CouponType.PERCENT) {
                        discountMinor = priceMinor * appliedCoupon.getAmount() / 100;
                    } else {
                        discountMinor = appliedCoupon.getAmount(); // FIXED type, amount is already in minor
                    }
                    // Don't let discount exceed price
                    if (discountMinor > priceMinor)
                        discountMinor = priceMinor;
                } else {
                    appliedCoupon = null; // Invalid, ignore
                }
            }
        }

        Long priceAfterDiscount = priceMinor - discountMinor;

        // Calculate tax on price after discount
        BigDecimal taxRate = getTaxRateForRegion(customer.getCountry());
        Long taxMinor = calculateTax(priceAfterDiscount, taxRate, plan.getTaxMode());
        
        Long subtotalMinor;
        Long totalMinor;
        if (plan.getTaxMode() == TaxMode.INCLUSIVE) {
            subtotalMinor = priceAfterDiscount - taxMinor;
            totalMinor = priceAfterDiscount;
        } else {
            subtotalMinor = priceAfterDiscount;
            totalMinor = priceAfterDiscount + taxMinor;
        }

        // Check if trial is applicable
        boolean isTrial = plan.getTrialDays() > 0;

        LocalDate periodEnd = calculatePeriodEnd(today, request.getBillingPeriod());

        // Create subscription
        Subscription subscription = new Subscription();
        subscription.setCustomer(customer);
        subscription.setPlan(plan);
        subscription.setStatus(Status.ACTIVE);
        subscription.setStartDate(today);

        // Set trial if applicable
        if (isTrial) {
            subscription.setStatus(Status.TRIALING);
            subscription.setTrialEndDate(today.plusDays(plan.getTrialDays()));
            // The first PAID period starts AFTER the trial
            subscription.setCurrentPeriodStart(subscription.getTrialEndDate());
            subscription.setCurrentPeriodEnd(
                    calculatePeriodEnd(subscription.getTrialEndDate(), request.getBillingPeriod()));
        } else {
            subscription.setCurrentPeriodStart(today);
            subscription.setCurrentPeriodEnd(periodEnd);
        }

        subscription.setPaymentMethodId(paymentMethod.getId());
        subscription.setCurrency(customer.getCurrency());
        subscription.setCancelAtPeriodEnd(false);

        subscriptionRepository.save(subscription);
        System.out.println("DEBUG: Subscription created: " + subscription.getId());

        // Create SubscriptionItem for the plan
        SubscriptionItem planItem = new SubscriptionItem();
        planItem.setSubscription(subscription);
        planItem.setItemType(ItemType.PLAN);
        planItem.setPlan(plan);
        planItem.setUnitPriceMinor(plan.getDefaultPriceMinor());
        planItem.setQuantity(1);
        planItem.setTaxMode(plan.getTaxMode());
        subscriptionItemRepository.save(planItem);
        // Persist coupon linkage
        if (appliedCoupon != null) {
            SubscriptionCoupon sc = new SubscriptionCoupon();
            sc.setSubscription(subscription);
            sc.setCoupon(appliedCoupon);
            sc.setAppliedAt(LocalDateTime.now());
            sc.setStatus(Status.ACTIVE);
            subscriptionCouponRepository.save(sc);

            // Increment redeemed count
            appliedCoupon.setRedeemedCount(appliedCoupon.getRedeemedCount() + 1);
            couponRepository.save(appliedCoupon);
        }

        SubscriptionResponse response = new SubscriptionResponse();

        // Create the invoice for the period
        // If it's a trial, the status is OPEN and due date is when trial ends
        // If not a trial, the status is PAID and due date is today
        Status invoiceStatus = isTrial ? Status.OPEN : Status.PAID;
        LocalDate dueDate = isTrial ? subscription.getTrialEndDate() : today;

        Invoice invoice = createInvoice(customer, subscription, subtotalMinor, taxMinor, totalMinor, today,
                dueDate, invoiceStatus);
        invoice.setDiscountMinor(discountMinor);
        invoiceRepository.save(invoice);

        // Add line items to the invoice
        InvoiceLineItem planLine = new InvoiceLineItem();
        planLine.setInvoice(invoice);
        planLine.setDescription(plan.getName() + " Subscription" + (isTrial ? " (Starts after trial)" : ""));
        planLine.setLineType(InvoiceLineItem.LineType.PLAN);
        planLine.setQuantity(1);
        planLine.setUnitPriceMinor(priceMinor);
        planLine.setAmountMinor(priceMinor);
        invoiceLineItemRepository.save(planLine);

        // Add discount line item if coupon was applied
        if (discountMinor > 0 && appliedCoupon != null) {
            InvoiceLineItem discountLine = new InvoiceLineItem();
            discountLine.setInvoice(invoice);
            discountLine.setDescription("Coupon: " + appliedCoupon.getCode() + " (" + appliedCoupon.getName() + ")");
            discountLine.setLineType(InvoiceLineItem.LineType.DISCOUNT);
            discountLine.setQuantity(1);
            discountLine.setUnitPriceMinor(-discountMinor);
            discountLine.setAmountMinor(-discountMinor);
            invoiceLineItemRepository.save(discountLine);
        }

        // Add tax line item
        if (taxMinor > 0) {
            InvoiceLineItem taxLine = new InvoiceLineItem();
            taxLine.setInvoice(invoice);
            BigDecimal rate = getTaxRateForRegion(customer.getCountry());
            taxLine.setDescription("Tax (" + rate.stripTrailingZeros().toPlainString() + "%)");
            taxLine.setLineType(InvoiceLineItem.LineType.TAX);
            taxLine.setQuantity(1);
            taxLine.setUnitPriceMinor(taxMinor);
            taxLine.setAmountMinor(taxMinor);
            invoiceLineItemRepository.save(taxLine);
        }

        if (!isTrial) {
            // Auto-apply customer account credit before charging
            long amountToCharge = totalMinor;
            long creditApplied = 0L;
            if (customer.getCreditBalanceMinor() > 0 && totalMinor > 0) {
                creditApplied = Math.min(customer.getCreditBalanceMinor(), totalMinor);
                amountToCharge = totalMinor - creditApplied;

                // Add credit line item to invoice
                InvoiceLineItem creditLine = new InvoiceLineItem();
                creditLine.setInvoice(invoice);
                creditLine.setDescription("Account Credit Applied");
                creditLine.setLineType(InvoiceLineItem.LineType.DISCOUNT);
                creditLine.setQuantity(1);
                creditLine.setUnitPriceMinor(-creditApplied);
                creditLine.setAmountMinor(-creditApplied);
                invoiceLineItemRepository.save(creditLine);

                // Update invoice totals
                invoice.setTotalMinor(amountToCharge);
                invoice.setBalanceMinor(amountToCharge);
                invoiceRepository.save(invoice);

                // Deduct from customer credit balance
                customer.setCreditBalanceMinor(customer.getCreditBalanceMinor() - creditApplied);
                customerRepository.save(customer);
            }

            // Create payment record for the remaining amount (skip if fully covered by credit)
            if (amountToCharge > 0) {
                createPayment(invoice, paymentMethod, amountToCharge, customer.getCurrency());
            }
            System.out.println("DEBUG: Payment created for invoice: " + invoice.getId());
        }

        response.setInvoiceId(invoice.getId());
        response.setInvoiceNumber(invoice.getInvoiceNumber());
        response.setTotalAmountMinor(totalMinor);

        response.setSubscriptionId(subscription.getId());
        response.setStatus(subscription.getStatus().name());
        response.setMessage("Subscription activated successfully");
        if (subscription.getTrialEndDate() != null) {
            response.setTrialEndDate(subscription.getTrialEndDate().toString());
        }

        return response;
    }

    /**
     * Check customer status
     */
    public CustomerStatusResponse checkCustomerStatus(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return new CustomerStatusResponse(false, false, false, "User not found");
        }

        User user = userOpt.get();
        Optional<Customer> customerOpt = customerRepository.findByUser_Id(user.getId());

        if (customerOpt.isEmpty()) {
            return new CustomerStatusResponse(false, false, false, "Customer record not created");
        }

        Customer customer = customerOpt.get();
        
        Optional<Subscription> dashboardEligibleSub = subscriptionRepository
                .findByCustomer_IdAndStatusIn(customer.getId(),
                        java.util.List.of(Status.ACTIVE, Status.TRIALING, Status.PAST_DUE, Status.PAUSED, Status.ON_HOLD))
                .stream().findAny();

        Optional<Subscription> draftSub = subscriptionRepository
                .findByCustomer_IdAndStatusIn(customer.getId(),
                        java.util.List.of(Status.DRAFT)).stream().findAny();

        boolean isDashboardEligible = dashboardEligibleSub.isPresent();
        boolean hasDraftSubscription = draftSub.isPresent();

        // Only consider them a "full customer" if they have an active or billing-relevant subscription
        return new CustomerStatusResponse(isDashboardEligible, isDashboardEligible, hasDraftSubscription,
                isDashboardEligible ? "Active or relevant subscription found" : 
                (hasDraftSubscription ? "Draft subscription found" : "Customer exists but no subscription"));
    }

    // Helper methods

    private String detectCardBrand(String cardNumber) {
        if (cardNumber.startsWith("4"))
            return "VISA";
        if (cardNumber.startsWith("5"))
            return "MASTERCARD";
        if (cardNumber.startsWith("34") || cardNumber.startsWith("37"))
            return "AMEX";
        return "UNKNOWN";
    }

    private Long getPriceForRegion(Plan plan, String country, String currency) {
        // Try to find region-specific price
        Optional<PriceBookEntry> entry = priceBookEntryRepository
                .findByPlan_IdAndRegionAndCurrency(plan.getId(), country, currency);

        if (entry.isPresent()) {
            return entry.get().getPriceMinor();
        }

        // Fallback to default price
        return plan.getDefaultPriceMinor();
    }

    private BigDecimal getTaxRateForRegion(String country) {
        Optional<TaxRate> taxRate = taxRateRepository.findByRegionAndEffectiveToIsNullOrFuture(country,
                LocalDate.now());
        return taxRate.map(tr -> tr.getRatePercent()).orElse(BigDecimal.ZERO);
    }

    private Long calculateTax(Long amount, BigDecimal taxRate, TaxMode taxMode) {
        if (taxMode == TaxMode.INCLUSIVE) {
            // Price already includes tax — extract the tax portion
            // e.g., ₹499 incl 18% GST → base = 499 / 1.18 = 423.73, tax = 75.27
            if (taxRate == null || taxRate.compareTo(BigDecimal.ZERO) == 0) {
                return 0L;
            }
            BigDecimal rateFactor = BigDecimal.ONE.add(taxRate.divide(BigDecimal.valueOf(100)));
            long baseMinor = BigDecimal.valueOf(amount).divide(rateFactor, 0, java.math.RoundingMode.HALF_UP).longValue();
            return amount - baseMinor;
        }
        // Exclusive: add tax on top
        if (taxRate == null || taxRate.compareTo(BigDecimal.ZERO) == 0) {
            return 0L;
        }
        return BigDecimal.valueOf(amount)
                .multiply(taxRate)
                .divide(BigDecimal.valueOf(100), 0, java.math.RoundingMode.HALF_UP)
                .longValue();
    }

    private LocalDate calculatePeriodEnd(LocalDate start, BillingPeriod period) {
        if (period == BillingPeriod.MONTHLY) {
            return start.plusMonths(1);
        } else {
            return start.plusYears(1);
        }
    }

    private Invoice createInvoice(Customer customer, Subscription subscription,
            Long subtotal, Long tax, Long total, LocalDate today, LocalDate dueDate, Status status) {
        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setSubscription(subscription);
        invoice.setInvoiceNumber("INV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")));
        invoice.setStatus(status);
        invoice.setBillingReason(BillingReason.SUBSCRIPTION_CREATE);
        invoice.setIssueDate(today);
        invoice.setDueDate(dueDate);
        invoice.setSubtotalMinor(subtotal);
        invoice.setTaxMinor(tax);
        invoice.setDiscountMinor(0L);
        invoice.setTotalMinor(total);
        invoice.setBalanceMinor(status == Status.OPEN ? total : 0L);
        invoice.setCurrency(customer.getCurrency());
        invoice.setIdempotencyKey(UUID.randomUUID().toString());

        invoiceRepository.save(invoice);
        return invoice;
    }

    private void createPayment(Invoice invoice, PaymentMethod paymentMethod, Long amount, String currency) {
        // Attempt charge via mock gateway — will throw CustomException on decline
        String gatewayRef = mockPaymentGateway.charge(paymentMethod.getGatewayToken(), amount, currency);

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setPaymentMethod(paymentMethod);
        payment.setIdempotencyKey(UUID.randomUUID().toString());
        payment.setGatewayRef(gatewayRef);
        payment.setAmountMinor(amount);
        payment.setCurrency(currency);
        payment.setStatus(Status.SUCCESS);
        payment.setAttemptNo(1);

        paymentRepository.save(payment);
    }
}
