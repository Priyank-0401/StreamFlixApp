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
        Customer customer = customerRepository.findById(customerId)
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
            paymentMethod.setGatewayToken("mock_token_card_" + UUID.randomUUID().toString().substring(0, 8));
            
        } else if (request.getPaymentType() == PaymentType.UPI) {
            paymentMethod.setGatewayToken("mock_token_upi_" + request.getUpiId());
            // UPI doesn't use card fields
        }

        paymentMethodRepository.save(paymentMethod);
        System.out.println("DEBUG: Payment method created for customer: " + customerId + ", methodId: " + paymentMethod.getId());
        
        return paymentMethod;
    }

    /**
     * Step 3: Complete subscription
     */
    @Transactional
    public SubscriptionResponse completeSubscription(Long customerId, SubscriptionCompletionRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> CustomException.notFound("Customer not found"));

        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> CustomException.notFound("Plan not found"));

        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> CustomException.notFound("Payment method not found"));

        // Calculate price based on region
        Long priceMinor = getPriceForRegion(plan, customer.getCountry(), customer.getCurrency());
        
        // Calculate tax
        BigDecimal taxRate = getTaxRateForRegion(customer.getCountry());
        Long taxMinor = calculateTax(priceMinor, taxRate, plan.getTaxMode());
        Long totalMinor = priceMinor + taxMinor;

        LocalDate today = LocalDate.now();
        LocalDate periodEnd = calculatePeriodEnd(today, request.getBillingPeriod());

        // Create subscription
        Subscription subscription = new Subscription();
        subscription.setCustomer(customer);
        subscription.setPlan(plan);
        subscription.setStatus(Status.ACTIVE);
        subscription.setStartDate(today);
        
        // Set trial if applicable
        if (plan.getTrialDays() > 0) {
            subscription.setStatus(Status.TRIALING);
            subscription.setTrialEndDate(today.plusDays(plan.getTrialDays()));
        }
        
        subscription.setCurrentPeriodStart(today);
        subscription.setCurrentPeriodEnd(periodEnd);
        subscription.setPaymentMethodId(paymentMethod.getId());
        subscription.setCurrency(customer.getCurrency());
        subscription.setCancelAtPeriodEnd(false);

        subscriptionRepository.save(subscription);
        System.out.println("DEBUG: Subscription created: " + subscription.getId());

        // Create invoice
        Invoice invoice = createInvoice(customer, subscription, priceMinor, taxMinor, totalMinor, today);
        System.out.println("DEBUG: Invoice created: " + invoice.getId());

        // Create payment
        createPayment(invoice, paymentMethod, totalMinor, customer.getCurrency());
        System.out.println("DEBUG: Payment created for invoice: " + invoice.getId());

        SubscriptionResponse response = new SubscriptionResponse();
        response.setSubscriptionId(subscription.getId());
        response.setInvoiceId(invoice.getId());
        response.setInvoiceNumber(invoice.getInvoiceNumber());
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
            return new CustomerStatusResponse(false, false, "User not found");
        }

        User user = userOpt.get();
        Optional<Customer> customerOpt = customerRepository.findByUser_Id(user.getId());
        
        if (customerOpt.isEmpty()) {
            return new CustomerStatusResponse(false, false, "Customer record not created");
        }

        Customer customer = customerOpt.get();
        boolean hasActiveSubscription = subscriptionRepository
                .findByCustomer_IdAndStatusIn(customer.getId(), 
                    java.util.List.of(Status.ACTIVE, Status.TRIALING))
                .stream()
                .findAny()
                .isPresent();

        return new CustomerStatusResponse(true, hasActiveSubscription, 
                hasActiveSubscription ? "Active subscription found" : "Customer exists but no active subscription");
    }

    // Helper methods

    private String detectCardBrand(String cardNumber) {
        if (cardNumber.startsWith("4")) return "VISA";
        if (cardNumber.startsWith("5")) return "MASTERCARD";
        if (cardNumber.startsWith("34") || cardNumber.startsWith("37")) return "AMEX";
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
        Optional<TaxRate> taxRate = taxRateRepository.findByRegionAndEffectiveToIsNullOrFuture(country, LocalDate.now());
        return taxRate.map(tr -> tr.getRatePercent()).orElse(BigDecimal.ZERO);
    }

    private Long calculateTax(Long amount, BigDecimal taxRate, TaxMode taxMode) {
        if (taxMode == TaxMode.INCLUSIVE) {
            // Tax is already included in the price
            return 0L;
        }
        // Exclusive: add tax on top
        return amount * taxRate.longValue() / 100;
    }

    private LocalDate calculatePeriodEnd(LocalDate start, BillingPeriod period) {
        if (period == BillingPeriod.MONTHLY) {
            return start.plusMonths(1);
        } else {
            return start.plusYears(1);
        }
    }

    private Invoice createInvoice(Customer customer, Subscription subscription, 
                                   Long subtotal, Long tax, Long total, LocalDate today) {
        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setSubscription(subscription);
        invoice.setInvoiceNumber("INV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")));
        invoice.setStatus(Status.PAID);
        invoice.setBillingReason(BillingReason.SUBSCRIPTION_CREATE);
        invoice.setIssueDate(today);
        invoice.setDueDate(today);
        invoice.setSubtotalMinor(subtotal);
        invoice.setTaxMinor(tax);
        invoice.setDiscountMinor(0L);
        invoice.setTotalMinor(total);
        invoice.setBalanceMinor(0L);
        invoice.setCurrency(customer.getCurrency());
        invoice.setIdempotencyKey(UUID.randomUUID().toString());

        invoiceRepository.save(invoice);
        return invoice;
    }

    private void createPayment(Invoice invoice, PaymentMethod paymentMethod, Long amount, String currency) {
        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setPaymentMethod(paymentMethod);
        payment.setIdempotencyKey(UUID.randomUUID().toString());
        payment.setGatewayRef("mock_payment_" + UUID.randomUUID().toString().substring(0, 8));
        payment.setAmountMinor(amount);
        payment.setCurrency(currency);
        payment.setStatus(Status.SUCCESS);
        payment.setAttemptNo(1);

        paymentRepository.save(payment);
    }
}
