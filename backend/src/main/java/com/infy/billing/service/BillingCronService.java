package com.infy.billing.service;

import com.infy.billing.entity.BillingJob;
import com.infy.billing.entity.Notification;
import com.infy.billing.entity.Subscription;
import com.infy.billing.enums.Status;
import com.infy.billing.repository.BillingJobRepository;
import com.infy.billing.repository.NotificationRepository;
import com.infy.billing.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class BillingCronService {

    private final CycleBillingService cycleBillingService;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationRepository notificationRepository;
    private final BillingJobRepository billingJobRepository;

    @Scheduled(cron = "0 0 0 * * ?") // Runs every day at 12:00 AM
    public void runDailyBillingCycle() {
        log.info("Starting Daily Billing Cron Jobs...");

        // 1. Renewal Notifications
        try {
            BillingJob job = createJob(BillingJob.JobType.REMINDER);
            int count = processRenewalNotifications();
            finishJob(job, count, count, 0, null);
        } catch (Exception e) {
            log.error("Error processing renewal notifications: {}", e.getMessage());
        }

        // 2. Cycle Billing
        try {
            BillingJob job = createJob(BillingJob.JobType.CYCLE_BILLING);
            CycleBillingService.JobStats stats = cycleBillingService.processCycleBilling();
            finishJob(job, stats.totalRecords, stats.successCount, stats.failureCount, stats.errorSummary.toString());
        } catch (Exception e) {
            log.error("Error processing cycle billing: {}", e.getMessage());
        }

        // 3. Dunning Retries
        try {
            BillingJob job = createJob(BillingJob.JobType.DUNNING_RETRY);
            CycleBillingService.JobStats stats = cycleBillingService.processDunningRetries();
            finishJob(job, stats.totalRecords, stats.successCount, stats.failureCount, stats.errorSummary.toString());
        } catch (Exception e) {
            log.error("Error processing dunning retries: {}", e.getMessage());
        }

        log.info("Finished Daily Billing Cron Jobs.");
    }

    private BillingJob createJob(BillingJob.JobType type) {
        BillingJob job = new BillingJob();
        job.setJobType(type);
        job.setStatus(BillingJob.Status.RUNNING);
        job.setStartedAt(java.time.LocalDateTime.now());
        return billingJobRepository.save(job);
    }

    private void finishJob(BillingJob job, int total, int success, int failure, String errorSummary) {
        job.setTotalRecords(total);
        job.setSuccessCount(success);
        job.setFailureCount(failure);
        job.setErrorSummary(errorSummary);
        job.setCompletedAt(java.time.LocalDateTime.now());
        
        if (failure > 0 && success > 0) {
            job.setStatus(BillingJob.Status.PARTIAL);
        } else if (failure > 0 && success == 0) {
            job.setStatus(BillingJob.Status.FAILED);
        } else {
            job.setStatus(BillingJob.Status.COMPLETED);
        }
        
        billingJobRepository.save(job);
    }

    private int processRenewalNotifications() {
        log.info("Processing renewal notifications...");
        LocalDate today = LocalDate.now();

        List<Subscription> activeSubs = subscriptionRepository.findByStatusIn(List.of(Status.ACTIVE, Status.TRIALING));

        int notifCount = 0;

        for (Subscription sub : activeSubs) {
            if (sub.getCurrentPeriodEnd() == null) continue;

            long daysUntilEnd = ChronoUnit.DAYS.between(today, sub.getCurrentPeriodEnd());

            if (daysUntilEnd == 7 || daysUntilEnd == 3 || daysUntilEnd == 1) {
                Notification notification = new Notification();
                notification.setCustomer(sub.getCustomer());
                notification.setType("RENEWAL_REMINDER");
                notification.setSubject("Upcoming Renewal in " + daysUntilEnd + " day(s)");
                notification.setBody("Your " + sub.getPlan().getName() + " subscription will renew on " + sub.getCurrentPeriodEnd() + ".");
                notification.setChannel(Notification.Channel.IN_APP);
                notification.setStatus(Notification.Status.PENDING);
                notificationRepository.save(notification);
                notifCount++;
                log.info("Created renewal notification for subscription ID: {} (T-{} days)", sub.getId(), daysUntilEnd);
            }
        }
        return notifCount;
    }
}
