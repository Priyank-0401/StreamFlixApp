package com.infy.billing.service;

import com.infy.billing.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Facade service that aggregates subscription-related repositories.
 * Reduces constructor parameter count for classes that depend on multiple subscription repos.
 */
@Service
@RequiredArgsConstructor
public class SubscriptionDataService {
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionItemRepository subscriptionItemRepository;
    private final SubscriptionCouponRepository subscriptionCouponRepository;
    private final AddOnRepository addOnRepository;
    private final MeteredComponentRepository meteredComponentRepository;
    private final UsageRecordRepository usageRecordRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineItemRepository invoiceLineItemRepository;
    private final PaymentRepository paymentRepository;

    public SubscriptionRepository getSubscriptionRepository() {
        return subscriptionRepository;
    }

    public SubscriptionItemRepository getSubscriptionItemRepository() {
        return subscriptionItemRepository;
    }

    public SubscriptionCouponRepository getSubscriptionCouponRepository() {
        return subscriptionCouponRepository;
    }

    public AddOnRepository getAddOnRepository() {
        return addOnRepository;
    }

    public MeteredComponentRepository getMeteredComponentRepository() {
        return meteredComponentRepository;
    }

    public UsageRecordRepository getUsageRecordRepository() {
        return usageRecordRepository;
    }

    public InvoiceRepository getInvoiceRepository() {
        return invoiceRepository;
    }

    public InvoiceLineItemRepository getInvoiceLineItemRepository() {
        return invoiceLineItemRepository;
    }

    public PaymentRepository getPaymentRepository() {
        return paymentRepository;
    }
}
