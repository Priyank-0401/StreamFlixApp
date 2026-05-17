package com.infy.billing.service;

import java.util.List;

import com.infy.billing.dto.customer.NotificationResponse;

public interface NotificationService {

	List<NotificationResponse> getCustomerNotifications(Long customerId);

	void generateRenewalReminders();

	void processPendingNotifications();

	void generatePendingPaymentReminders();

}
