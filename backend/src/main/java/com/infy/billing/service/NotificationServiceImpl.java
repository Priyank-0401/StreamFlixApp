package com.infy.billing.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.infy.billing.dto.customer.NotificationResponse;
import com.infy.billing.entity.Notification;
import com.infy.billing.entity.Subscription;
import com.infy.billing.enums.Channel;
import com.infy.billing.enums.NotificationStatus;
import com.infy.billing.enums.Status;
import com.infy.billing.repository.NotificationRepository;
import com.infy.billing.repository.SubscriptionRepository;

import lombok.RequiredArgsConstructor;

@Service

@RequiredArgsConstructor

@Transactional
public class NotificationServiceImpl implements NotificationService {

	private final NotificationRepository notificationRepository;

	private final SubscriptionRepository subscriptionRepository;

	@Override

	@Transactional(readOnly = true)

	public List<NotificationResponse> getCustomerNotifications(Long customerId) {

		List<Notification> notifications = notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);

		return notifications.stream().map(this::mapToResponse).collect(Collectors.toList());

	}

	@Override

	public void generateRenewalReminders() {

		LocalDate today = LocalDate.now();

		System.out.println(">>> TODAY=" + today); // ADD THIS

		LocalDate sevenDaysLater = today.plusDays(7);

		LocalDate oneDayLater = today.plusDays(1);

		List<Subscription> subscriptions = subscriptionRepository.findByStatus(Status.ACTIVE);

		System.out.println(">>> ACTIVE SUBS=" + subscriptions.size()); // ADD THIS

		for (Subscription subscription : subscriptions)

		{

			if (subscription.getCurrentPeriodEnd() == null)

			{

				continue;

			}

			if (subscription.getCustomer() == null) {

				continue;

			}

			LocalDate renewalDate = subscription.getCurrentPeriodEnd();

			if (renewalDate.equals(sevenDaysLater))

			{

				createNotification(subscription,

						"RENEWAL_REMINDER_7_DAYS",

						"Subscription Renewal Reminder",

						"Your subscription renews in 7 days."

				);

			}

			if (renewalDate.equals(oneDayLater))

			{

				createNotification(

						subscription,

						"RENEWAL_REMINDER_1_DAY",

						"Subscription Renewal Reminder",

						"Your subscription renews in 1 day."

				);

			}

		}

	}

	@Override

	public void generatePendingPaymentReminders() {

		LocalDate today = LocalDate.now();

		LocalDate sevenDaysLater = today.plusDays(7);

		LocalDate threeDaysLater = today.plusDays(3);

		LocalDate oneDayLater = today.plusDays(1);

		List<Subscription> subscriptions = subscriptionRepository.findByStatus(Status.ACTIVE);

		for (Subscription subscription : subscriptions) {

			if (subscription.getCurrentPeriodEnd() == null) {

				continue;

			}

			if (subscription.getCustomer() == null) {

				continue;

			}

			LocalDate periodEnd = subscription.getCurrentPeriodEnd();

			if (periodEnd.equals(sevenDaysLater)) {

				createNotification(subscription,

						"PENDING_PAYMENT_7_DAYS",

						"Payment Due Reminder",

						"Your subscription payment is due in 7 days."

				);

			}

			if (periodEnd.equals(threeDaysLater)) {

				createNotification(subscription,

						"PENDING_PAYMENT_3_DAYS",

						"Payment Due Reminder",

						"Your subscription payment is due in 3 days."

				);

			}

			if (periodEnd.equals(oneDayLater)) {

				createNotification(subscription,

						"PENDING_PAYMENT_1_DAY",

						"Payment Due Reminder",

						"Your subscription payment is due in 1 day."

				);

			}

		}

	}

	@Override

	public void processPendingNotifications()

	{

		LocalDateTime now = LocalDateTime.now();

		List<Notification> notifications = notificationRepository.findByStatus(NotificationStatus.PENDING);

		for (Notification notification : notifications) {

			try

			{

				notification.setStatus(NotificationStatus.SENT);

				notification.setSentAt(now);

				notificationRepository.save(notification);

			}

			catch (Exception ex) {

				notification.setStatus(NotificationStatus.FAILED);

				notificationRepository.save(notification);

			}

		}

	}

	private void createNotification(

			Subscription subscription,

			String type,

			String subject,

			String body

	)

	{

		LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

		LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay();

		boolean alreadyExists = notificationRepository.existsByCustomerIdAndTypeAndScheduledAtBetween(

				subscription.getCustomer().getId(),

				type,

				startOfDay,

				endOfDay);

		if (alreadyExists)

		{

			return;

		}

		Notification notification = Notification.builder()

				.customer(subscription.getCustomer())

				.type(type)

				.subject(subject)

				.body(body)

				.channel(Channel.EMAIL)

				.status(NotificationStatus.PENDING)

				.scheduledAt(LocalDateTime.now())

				.build();

		notificationRepository.save(notification);

	}

	private NotificationResponse mapToResponse(Notification notification)

	{

		return NotificationResponse.builder()

				.notificationId(notification.getNotificationId())

				.type(notification.getType())

				.subject(notification.getSubject())

				.body(notification.getBody())

				.channel(notification.getChannel())

				.status(notification.getStatus())

				.scheduledAt(notification.getScheduledAt())

				.sentAt(notification.getSentAt())

				.createdAt(notification.getCreatedAt())

				.build();

	}

}
