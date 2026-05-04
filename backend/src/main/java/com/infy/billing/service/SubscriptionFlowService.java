package com.infy.billing.service;

import com.infy.billing.dto.customer.*;
import com.infy.billing.entity.Customer;
import com.infy.billing.entity.PaymentMethod;

public interface SubscriptionFlowService {

   Customer registerCustomerDetails(String email, CustomerRegistrationRequest request);

   PaymentMethod createPaymentMethod(Long customerId, PaymentMethodRequest request);

   SubscriptionResponse completeSubscription(Long customerId, SubscriptionCompletionRequest request);

   CustomerStatusResponse checkCustomerStatus(String email);
}
