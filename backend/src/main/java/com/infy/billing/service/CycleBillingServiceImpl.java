package com.infy.billing.service;

import com.infy.billing.entity.*;
import com.infy.billing.enums.BillingPeriod;
import com.infy.billing.enums.BillingReason;
import com.infy.billing.enums.ItemType;
import com.infy.billing.enums.Status;
import com.infy.billing.enums.TaxMode;
import com.infy.billing.exception.CustomException.PaymentPendingException;
import com.infy.billing.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CycleBillingServiceImpl implements CycleBillingService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionItemRepository subscriptionItemRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineItemRepository invoiceLineItemRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final TaxRateRepository taxRateRepository;
    private final MockPaymentGateway mockPaymentGateway;
    private final DunningRetryLogRepository dunningRetryLogRepository;
    private final org.springframework.transaction.support.TransactionTemplate transactionTemplate;

    @Override
    public JobStats processCycleBilling() {
        log.info("Starting cycle billing process...");
        JobStats stats = new JobStats();
        LocalDate today = LocalDate.now();

        // Subscriptions where the period ended yesterday or earlier (so today is the
        // renewal date or past due)
        List<Subscription> dueSubscriptions = subscriptionRepository.findByStatusInAndCurrentPeriodEndLessThan(
                List.of(Status.ACTIVE), today);

        stats.totalRecords = dueSubscriptions.size();

        for (Subscription sub : dueSubscriptions) {
            try {
                transactionTemplate.executeWithoutResult(status -> {
                    processSingleSubscription(sub, today);
                });
                stats.successCount++;
            } catch (Exception e) {
                stats.failureCount++;
                stats.errorSummary.append("Sub ").append(sub.getId()).append(": ").append(e.getMessage()).append("\n");
                log.error("Failed to process subscription {}: {}", sub.getId(), e.getMessage());
            }
        }
        return stats;
    }

    @Override
    public JobStats processDunningRetries() {
        log.info("Starting dunning retries...");
        JobStats stats = new JobStats();
        LocalDateTime now = LocalDateTime.now();
        List<DunningRetryLog> retries = dunningRetryLogRepository
                .findByStatusAndScheduledAtLessThanEqual(DunningRetryLog.Status.SCHEDULED, now);

        stats.totalRecords = retries.size();

        for (DunningRetryLog retry : retries) {
            try {
                transactionTemplate.executeWithoutResult(status -> {
                    processSingleRetry(retry);
                });
                stats.successCount++;
            } catch (Exception e) {
                stats.failureCount++;
                stats.errorSummary.append("Retry ").append(retry.getId()).append(": ").append(e.getMessage())
                        .append("\n");
                log.error("Failed to process retry {}: {}", retry.getId(), e.getMessage());
            }
        }
        return stats;
    }

    private void processSingleSubscription(Subscription subscription, LocalDate today) {
        Customer customer = subscription.getCustomer();
        Plan plan = subscription.getPlan();

        // 1. Calculate totals
        List<SubscriptionItem> items = subscriptionItemRepository.findBySubscription_Id(subscription.getId());
        long basePrice = plan.getDefaultPriceMinor();
        long addonTotal = items.stream()
                .filter(i -> i.getItemType() == ItemType.ADDON)
                .mapToLong(i -> i.getUnitPriceMinor() * i.getQuantity())
                .sum();

        long subtotal = basePrice + addonTotal; // Assuming no discounts for simplicity on renewal, or apply recurring
                                                // coupon
        long taxMinor = calculateTaxMinor(subtotal, customer.getCountry(), plan.getTaxMode());
        long totalMinor = (plan.getTaxMode() == TaxMode.INCLUSIVE) ? subtotal : subtotal + taxMinor;
        long subtotalMinor = (plan.getTaxMode() == TaxMode.INCLUSIVE) ? subtotal - taxMinor : subtotal;

        // 2. Create Invoice
        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setSubscription(subscription);
        invoice.setInvoiceNumber("INV-REN-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
                + "-" + subscription.getId());
        invoice.setStatus(Status.OPEN);
        invoice.setBillingReason(BillingReason.SUBSCRIPTION_CYCLE);
        invoice.setIssueDate(today);
        invoice.setDueDate(today);
        invoice.setSubtotalMinor(subtotalMinor);
        invoice.setTaxMinor(taxMinor);
        invoice.setDiscountMinor(0L);
        invoice.setTotalMinor(totalMinor);
        invoice.setBalanceMinor(totalMinor);
        invoice.setCurrency(subscription.getCurrency());
        invoice.setIdempotencyKey(UUID.randomUUID().toString());
        invoiceRepository.save(invoice);

        // 3. Line Items
        InvoiceLineItem planLine = new InvoiceLineItem();
        planLine.setInvoice(invoice);
        planLine.setDescription(plan.getName() + " Renewal");
        planLine.setLineType(InvoiceLineItem.LineType.PLAN);
        planLine.setQuantity(1);
        planLine.setUnitPriceMinor(basePrice);
        planLine.setAmountMinor(basePrice);
        planLine.setPeriodStart(today);
        planLine.setPeriodEnd(plan.getBillingPeriod() == com.infy.billing.enums.BillingPeriod.MONTHLY ? today.plusMonths(1) : today.plusYears(1));
        invoiceLineItemRepository.save(planLine);

        // Add Tax Line Item
        if (taxMinor > 0) {
            InvoiceLineItem taxLine = new InvoiceLineItem();
            taxLine.setInvoice(invoice);
            taxLine.setDescription("Tax");
            taxLine.setLineType(InvoiceLineItem.LineType.TAX);
            taxLine.setQuantity(1);
            taxLine.setUnitPriceMinor(taxMinor);
            taxLine.setAmountMinor(taxMinor);
            taxRateRepository.findByRegionAndEffectiveToIsNullOrFuture(customer.getCountry(), today)
                    .ifPresent(taxLine::setTaxRate);
            invoiceLineItemRepository.save(taxLine);
        }

        // 4. Charge
        PaymentMethod pm = paymentMethodRepository.findById(subscription.getPaymentMethodId()).orElse(null);
        if (pm == null || pm.getStatus() != Status.ACTIVE) {
            handleFailedPayment(invoice, subscription, null, 1, "No active payment method");
            return;
        }

        try {
            String gatewayRef = mockPaymentGateway.charge(pm.getGatewayToken(), totalMinor, invoice.getCurrency());

            // Success
            Payment payment = new Payment();
            payment.setInvoice(invoice);
            payment.setPaymentMethod(pm);
            payment.setAmountMinor(totalMinor);
            payment.setCurrency(invoice.getCurrency());
            payment.setStatus(Status.SUCCESS);
            payment.setIdempotencyKey(UUID.randomUUID().toString());
            payment.setGatewayRef(gatewayRef);
            payment.setAttemptNo(1);
            paymentRepository.save(payment);

            invoice.setStatus(Status.PAID);
            invoice.setBalanceMinor(0L);
            invoiceRepository.save(invoice);

            subscription.setCurrentPeriodStart(today);
            subscription.setCurrentPeriodEnd(
                    plan.getBillingPeriod() == BillingPeriod.MONTHLY ? today.plusMonths(1) : today.plusYears(1));
            subscriptionRepository.save(subscription);

        } catch (Exception e) {
            handleFailedPayment(invoice, subscription, pm, 1, e.getMessage());
        }
    }

    private void handleFailedPayment(Invoice invoice, Subscription subscription, PaymentMethod pm, int attemptNo,
            String reason) {
        subscription.setStatus(Status.PAST_DUE);
        subscriptionRepository.save(subscription);

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setPaymentMethod(pm);
        payment.setAmountMinor(invoice.getTotalMinor());
        payment.setCurrency(invoice.getCurrency());
        payment.setStatus(Status.PENDING); // As requested
        payment.setIdempotencyKey(UUID.randomUUID().toString());
        payment.setAttemptNo(attemptNo);
        payment.setFailureReason(reason);
        paymentRepository.save(payment);

        scheduleDunningRetry(invoice, payment, attemptNo);

        throw new PaymentPendingException("Payment failed and is scheduled for retry. Reason: " + reason);
    }

    private void scheduleDunningRetry(Invoice invoice, Payment payment, int previousAttemptNo) {
        int nextAttemptNo = previousAttemptNo + 1;
        if (nextAttemptNo > 3)
            return; // Max 3 attempts (initial + 2 retries at T+3, T+7)

        DunningRetryLog retry = new DunningRetryLog();
        retry.setInvoice(invoice);
        retry.setPayment(payment);
        retry.setAttemptNo(nextAttemptNo);

        int daysToAdd = (nextAttemptNo == 2) ? 3 : 4; // T+3, then T+7 (which is +4 from T+3)
        retry.setScheduledAt(LocalDateTime.now().plusDays(daysToAdd).withHour(0).withMinute(0).withSecond(0));
        retry.setStatus(DunningRetryLog.Status.SCHEDULED);
        retry.setFailureReason(payment.getFailureReason());
        dunningRetryLogRepository.save(retry);
    }

    private void processSingleRetry(DunningRetryLog retry) {
        retry.setAttemptedAt(LocalDateTime.now());

        Invoice invoice = retry.getInvoice();
        Subscription subscription = invoice.getSubscription();
        PaymentMethod pm = paymentMethodRepository.findById(subscription.getPaymentMethodId()).orElse(null);

        try {
            if (pm == null || pm.getStatus() != Status.ACTIVE) {
                throw new RuntimeException("No active payment method");
            }

            String gatewayRef = mockPaymentGateway.charge(pm.getGatewayToken(), invoice.getTotalMinor(),
                    invoice.getCurrency());

            // Success
            retry.setStatus(DunningRetryLog.Status.SUCCESS);
            dunningRetryLogRepository.save(retry);

            Payment payment = retry.getPayment();
            payment.setStatus(Status.SUCCESS);
            payment.setGatewayRef(gatewayRef);
            paymentRepository.save(payment);

            invoice.setStatus(Status.PAID);
            invoice.setBalanceMinor(0L);
            invoiceRepository.save(invoice);

            subscription.setStatus(Status.ACTIVE);
            subscription.setCurrentPeriodStart(LocalDate.now());
            subscription.setCurrentPeriodEnd(
                    subscription.getPlan().getBillingPeriod() == BillingPeriod.MONTHLY ? LocalDate.now().plusMonths(1)
                            : LocalDate.now().plusYears(1));
            subscriptionRepository.save(subscription);

        } catch (Exception e) {
            retry.setStatus(DunningRetryLog.Status.FAILED);
            retry.setFailureReason(e.getMessage());
            dunningRetryLogRepository.save(retry);

            Payment payment = retry.getPayment();
            payment.setFailureReason(e.getMessage());
            paymentRepository.save(payment);

            if (retry.getAttemptNo() >= 3) {
                // Final attempt failed
                payment.setStatus(Status.FAILED);
                paymentRepository.save(payment);

                subscription.setStatus(Status.CANCELED);
                subscription.setCanceledAt(LocalDateTime.now());
                subscriptionRepository.save(subscription);
            } else {
                scheduleDunningRetry(invoice, payment, retry.getAttemptNo());
            }
        }
    }

    private long calculateTaxMinor(long amount, String country, TaxMode taxMode) {
        Optional<TaxRate> taxRateOpt = taxRateRepository.findByRegionAndEffectiveToIsNullOrFuture(country,
                LocalDate.now());
        if (taxRateOpt.isEmpty())
            return 0L;

        BigDecimal rate = taxRateOpt.get().getRatePercent();
        if (taxMode == TaxMode.INCLUSIVE) {
            BigDecimal rateFactor = BigDecimal.ONE.add(rate.divide(BigDecimal.valueOf(100)));
            long baseMinor = BigDecimal.valueOf(amount).divide(rateFactor, 0, java.math.RoundingMode.HALF_UP)
                    .longValue();
            return amount - baseMinor;
        }
        return BigDecimal.valueOf(amount).multiply(rate)
                .divide(BigDecimal.valueOf(100), 0, java.math.RoundingMode.HALF_UP).longValue();
    }
}
