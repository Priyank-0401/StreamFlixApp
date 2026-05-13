package com.infy.billing.service;

import com.infy.billing.dto.customer.NotificationDTO;
import java.util.List;

public interface NotificationService {
    List<NotificationDTO> getCustomerNotifications(String email);
    List<NotificationDTO> getUnreadNotifications(String email);
    void markAsRead(String email, Long notificationId);
    void markAllAsRead(String email);
}
