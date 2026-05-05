package com.infy.billing.service;

import com.infy.billing.dto.customer.*;
import com.infy.billing.entity.*;
import com.infy.billing.enums.PaymentType;
import com.infy.billing.enums.Status;
import com.infy.billing.repository.*;
import com.infy.billing.request.AddPaymentMethodRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerPaymentServiceImpl implements CustomerPaymentService {

   private final PaymentMethodRepository paymentMethodRepository;
   private final CustomerRepository customerRepository;
   private final UserRepository userRepository;

   public List<PaymentMethodDTO> getPaymentMethods(String email) {
       Customer customer = getCustomerByEmail(email);
       return paymentMethodRepository.findByCustomer_Id(customer.getId()).stream()
               .map(this::mapToPaymentMethodDTO)
               .collect(Collectors.toList());
   }

   @Transactional
   public PaymentMethodDTO addPaymentMethod(String email, AddPaymentMethodRequest request) {
       Customer customer = getCustomerByEmail(email);

       PaymentMethod method = new PaymentMethod();
       method.setCustomer(customer);
       method.setPaymentType(request.getPaymentType());
       method.setIsDefault(request.getIsDefault());
       method.setStatus(Status.ACTIVE);
       method.setCreatedAt(LocalDateTime.now());

       if (PaymentType.CARD.equals(request.getPaymentType())) {
           method.setCardLast4(request.getCardNumber().substring(request.getCardNumber().length() - 4));
           method.setCardBrand(detectCardBrand(request.getCardNumber()));
           method.setExpiryMonth(request.getExpiryMonth());
           method.setExpiryYear(request.getExpiryYear());
           method.setGatewayToken("tok_" + System.currentTimeMillis());
       } else if (PaymentType.UPI.equals(request.getPaymentType())) {
           method.setUpiId(request.getUpiId());
           method.setGatewayToken("tok_upi_" + System.currentTimeMillis());
       }

       List<PaymentMethod> existing = paymentMethodRepository.findByCustomer_Id(customer.getId());
       if (existing.isEmpty() || request.getIsDefault()) {
           existing.stream().filter(PaymentMethod::getIsDefault).forEach(m -> {
               m.setIsDefault(false);
               paymentMethodRepository.save(m);
           });
           method.setIsDefault(true);
       }

       PaymentMethod saved = paymentMethodRepository.save(method);
       return mapToPaymentMethodDTO(saved);
   }

   @Transactional
   public void setDefaultPaymentMethod(String email, Long paymentMethodId) {
       Customer customer = getCustomerByEmail(email);
       
       List<PaymentMethod> methods = paymentMethodRepository.findByCustomer_Id(customer.getId());
       methods.forEach(m -> {
           m.setIsDefault(false);
           paymentMethodRepository.save(m);
       });

       PaymentMethod method = paymentMethodRepository.findById(paymentMethodId)
               .orElseThrow(() -> new RuntimeException("Payment method not found"));
       
       if (!method.getCustomer().getId().equals(customer.getId())) {
           throw new RuntimeException("Unauthorized");
       }
       
       method.setIsDefault(true);
       paymentMethodRepository.save(method);
   }

   @Transactional
   public void deletePaymentMethod(String email, Long paymentMethodId) {
       Customer customer = getCustomerByEmail(email);
       PaymentMethod method = paymentMethodRepository.findById(paymentMethodId)
               .orElseThrow(() -> new RuntimeException("Payment method not found"));
       
       if (!method.getCustomer().getId().equals(customer.getId())) {
           throw new RuntimeException("Unauthorized");
       }

       List<PaymentMethod> remaining = paymentMethodRepository.findByCustomer_Id(customer.getId());
       if (method.getIsDefault() && remaining.size() == 1) {
           throw new RuntimeException("Cannot delete your only payment method");
       }

       if (method.getIsDefault() && remaining.size() > 1) {
           PaymentMethod newDefault = remaining.stream()
                   .filter(m -> !m.getId().equals(paymentMethodId))
                   .findFirst()
                   .orElse(null);
           if (newDefault != null) {
               newDefault.setIsDefault(true);
               paymentMethodRepository.save(newDefault);
           }
       }

       paymentMethodRepository.delete(method);
   }

   private Customer getCustomerByEmail(String email) {
       User user = userRepository.findByEmail(email)
               .orElseThrow(() -> new RuntimeException("User not found"));
       return customerRepository.findByUser_Id(user.getId())
               .orElseThrow(() -> new RuntimeException("Customer not found"));
   }

   private String detectCardBrand(String cardNumber) {
       if (cardNumber.startsWith("4")) return "VISA";
       if (cardNumber.startsWith("5")) return "MASTERCARD";
       if (cardNumber.startsWith("34") || cardNumber.startsWith("37")) return "AMEX";
       return "UNKNOWN";
   }

   private PaymentMethodDTO mapToPaymentMethodDTO(PaymentMethod method) {
       PaymentMethodDTO dto = new PaymentMethodDTO();
       dto.setPaymentMethodId(method.getId());
       dto.setPaymentType(method.getPaymentType());
       dto.setCardLast4(method.getCardLast4());
       dto.setCardBrand(method.getCardBrand());
       dto.setUpiId(method.getUpiId());
       dto.setIsDefault(method.getIsDefault());
       dto.setExpiryMonth(method.getExpiryMonth());
       dto.setExpiryYear(method.getExpiryYear());
       dto.setStatus(method.getStatus());
       return dto;
   }
}
