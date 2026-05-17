package com.infy.billing.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.infy.billing.service.BillingEngineService;
import com.infy.billing.service.BillingRetryService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor

public class BillingScheduler {

	private final BillingEngineService billingEngineService;

	private final BillingRetryService billingRetryService;

	// @Scheduled(cron="0 0 0 * * ?",zone = "Asia/Kolkata")

	// @Scheduled(fixedRate = 60000)
	@Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Kolkata")
	public void runRenewalProcessing() {
		System.out.println("Starting automated renewal processing ...");
		billingEngineService.processRenewals();
		System.out.println("Completed automated renewal processing");
	}

	// @Scheduled(fixedRate = 60000)
	@Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Kolkata")

	public void runRetries() {
		billingRetryService.retryFailedPayments();
	}
}
