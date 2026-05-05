package com.infy.billing.service;

import com.infy.billing.dto.customer.*;

import java.util.List;

public interface CustomerService {

   CustomerProfileDTO getProfile(String email);

   CustomerProfileDTO updateProfile(String email, CustomerProfileDTO dto);

   List<PlanDTO> getAvailablePlans();

   List<PlanDTO> getFeaturedPlans();

   List<PlanDTO> getAllActivePlans();

   List<AddOnDTO> getAvailableAddOns(String email);

   List<NotificationDTO> getNotifications(String email);

   void markNotificationAsRead(String email, Long notificationId);
}
