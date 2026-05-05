package com.infy.billing.service;

import com.infy.billing.dto.customer.*;
import com.infy.billing.entity.*;
import com.infy.billing.enums.Status;
import com.infy.billing.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

   private final CustomerRepository customerRepository;
   private final UserRepository userRepository;
   private final PlanRepository planRepository;
   private final AddOnRepository addOnRepository;
   private final SubscriptionRepository subscriptionRepository;

   public CustomerProfileDTO getProfile(String email) {
       Customer customer = getCustomerByEmail(email);
       return mapToProfileDTO(customer);
   }

   @Transactional
   public CustomerProfileDTO updateProfile(String email, CustomerProfileDTO dto) {
       Customer customer = getCustomerByEmail(email);
       customer.setPhone(dto.getPhone());
       customer.setState(dto.getState());
       customer.setCity(dto.getCity());
       customer.setAddressLine1(dto.getAddressLine1());
       customer.setPostalCode(dto.getPostalCode());
       customerRepository.save(customer);
       return mapToProfileDTO(customer);
   }

   public List<PlanDTO> getAvailablePlans() {
       // Fetch active plans
       List<Plan> plans = planRepository.findByStatus(Status.ACTIVE);
       System.out.println("DEBUG: Found " + plans.size() + " available plans");
       return plans.stream()
               .map(this::mapToPlanDTO)
               .collect(Collectors.toList());
   }

   public List<PlanDTO> getFeaturedPlans() {
	   // Return Basic Monthly (id=1) and Premium Monthly (id=3) as featured plans
	   List<Long> featuredPlanIds = java.util.Arrays.asList(1L, 3L);
	   List<Plan> featuredPlans = planRepository.findAllById(featuredPlanIds);
	   System.out.println("DEBUG: Found " + featuredPlans.size() + " featured plans");
	   return featuredPlans.stream()
			   .filter(plan -> plan.getStatus() == Status.ACTIVE)
               .map(this::mapToPlanDTO)
               .collect(Collectors.toList());
   }

   public List<PlanDTO> getAllActivePlans() {
	   // Return all active plans without filtering by effectiveTo
	   List<Plan> plans = planRepository.findByStatus(Status.ACTIVE);
	   System.out.println("DEBUG: Found " + plans.size() + " total active plans");
	   return plans.stream()
               .map(this::mapToPlanDTO)
               .collect(Collectors.toList());
   }

    public List<AddOnDTO> getAvailableAddOns(String email) {
        Customer customer = getCustomerByEmail(email);
        
        // Find current subscription period (include DRAFT for checkout flow)
        final com.infy.billing.enums.BillingPeriod currentPeriod = subscriptionRepository.findByCustomer_IdAndStatusIn(
                customer.getId(),
                List.of(com.infy.billing.enums.Status.ACTIVE, com.infy.billing.enums.Status.TRIALING, 
                        com.infy.billing.enums.Status.DRAFT, com.infy.billing.enums.Status.PAST_DUE,
                        com.infy.billing.enums.Status.PAUSED)
        ).stream()
        .sorted((s1, s2) -> s2.getId().compareTo(s1.getId()))
        .findFirst()
        .map(s -> s.getPlan().getBillingPeriod())
        .orElse(com.infy.billing.enums.BillingPeriod.MONTHLY);

        return addOnRepository.findByStatus(com.infy.billing.enums.Status.ACTIVE).stream()
                .filter(a -> a.getBillingPeriod() == currentPeriod)
                .map(this::mapToAddOnDTO)
                .collect(Collectors.toList());
    }

   private Customer getCustomerByEmail(String email) {
       User user = userRepository.findByEmail(email)
               .orElseThrow(() -> new RuntimeException("User not found"));
       return customerRepository.findByUser_Id(user.getId())
               .orElseThrow(() -> new RuntimeException("Customer not found"));
   }

   private CustomerProfileDTO mapToProfileDTO(Customer customer) {
       User user = customer.getUser();
       CustomerProfileDTO dto = new CustomerProfileDTO();
       dto.setCustomerId(customer.getId());
       dto.setUserId(user.getId());
       dto.setFullName(user.getFullName());
       dto.setEmail(user.getEmail());
       dto.setPhone(customer.getPhone());
       dto.setCurrency(customer.getCurrency());
       dto.setCountry(customer.getCountry());
       dto.setState(customer.getState());
       dto.setCity(customer.getCity());
       dto.setAddressLine1(customer.getAddressLine1());
       dto.setPostalCode(customer.getPostalCode());
       dto.setStatus(customer.getStatus());
       dto.setCreatedAt(customer.getCreatedAt());
       return dto;
   }

   private PlanDTO mapToPlanDTO(Plan plan) {
       PlanDTO dto = new PlanDTO();
       dto.setPlanId(plan.getId());
       dto.setName(plan.getName());
       dto.setBillingPeriod(plan.getBillingPeriod());
       dto.setDefaultPriceMinor(plan.getDefaultPriceMinor());
       dto.setDefaultCurrency(plan.getDefaultCurrency());
       dto.setTrialDays(plan.getTrialDays());
       dto.setSetupFeeMinor(plan.getSetupFeeMinor());
       dto.setTaxMode(plan.getTaxMode());
       dto.setEffectiveFrom(plan.getEffectiveFrom().toString());
       dto.setEffectiveTo(plan.getEffectiveTo() != null ? plan.getEffectiveTo().toString() : null);
       dto.setStatus(plan.getStatus());
       dto.setProductName("StreamFlix");
       return dto;
   }

   private AddOnDTO mapToAddOnDTO(AddOn addOn) {
       AddOnDTO dto = new AddOnDTO();
       dto.setAddOnId(addOn.getId());
       dto.setName(addOn.getName());
       dto.setPriceMinor(addOn.getPriceMinor());
       dto.setCurrency(addOn.getCurrency());
       dto.setBillingPeriod(addOn.getBillingPeriod());
       dto.setStatus(addOn.getStatus());
       return dto;
   }

}
