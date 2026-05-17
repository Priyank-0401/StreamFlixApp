package com.infy.billing.service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.infy.billing.entity.DunningRetryLog;
import com.infy.billing.entity.Invoice;
import com.infy.billing.entity.Payment;
import com.infy.billing.entity.PaymentMethod;
import com.infy.billing.entity.Subscription;
import com.infy.billing.enums.BillingPeriod;
import com.infy.billing.enums.DunningStatus;
import com.infy.billing.enums.Status;
import com.infy.billing.exception.CustomException;
import com.infy.billing.repository.DunningRetryLogRepository;
import com.infy.billing.repository.InvoiceRepository;
import com.infy.billing.repository.PaymentMethodRepository;
import com.infy.billing.repository.PaymentRepository;
import com.infy.billing.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class BillingRetryServiceImpl
		implements BillingRetryService {

	private final DunningRetryLogRepository dunningRetryLogRepository;

	private final PaymentRepository paymentRepository;

	private final PaymentMethodRepository paymentMethodRepository;

	private final InvoiceRepository invoiceRepository;

	private final SubscriptionRepository subscriptionRepository;

	private final MockPaymentGateway mockPaymentGateway;

	@Override

	@Transactional

	public void retryFailedPayments() {
		List<DunningRetryLog> retryLogs = dunningRetryLogRepository
				.findByStatusAndScheduledAtLessThanEqual(DunningStatus.SCHEDULED, LocalDateTime.now());
		for (DunningRetryLog retryLog : retryLogs) {
			try {
				Invoice invoice = retryLog.getInvoice();
				if (invoice == null || invoice.getStatus() == Status.PAID) {
					continue;
				}
				Subscription subscription = invoice.getSubscription();
				if (subscription == null || subscription.getStatus() == Status.CANCELED) {
					continue;
				}

				// FULLY CREDIT-COVERED INVOICE
				if (invoice.getTotalMinor() == 0L) {
					invoice.setStatus(Status.PAID);
					invoice.setBalanceMinor(0L);
					invoiceRepository.save(invoice);
					retryLog.setStatus(DunningStatus.SUCCESS);
					retryLog.setAttemptedAt(LocalDateTime.now());
					dunningRetryLogRepository.save(retryLog);
					advanceSubscription(subscription);
					continue;
				}
				PaymentMethod paymentMethod = paymentMethodRepository.findById(subscription.getPaymentMethodId())
						.orElse(null);
				if (paymentMethod == null || paymentMethod.getGatewayToken() == null
						|| paymentMethod.getGatewayToken().isBlank()) {
					retryLog.setStatus(DunningStatus.FAILED);
					retryLog.setFailureReason("Invalid payment method");
					retryLog.setAttemptedAt(LocalDateTime.now());
					dunningRetryLogRepository.save(retryLog);
					continue;
				}

//                  MARK RETRY ATTEMPTED
				retryLog.setStatus(DunningStatus.ATTEMPTED);
				retryLog.setAttemptedAt(LocalDateTime.now());
				dunningRetryLogRepository.save(retryLog);

//                 CREATE RETRY PAYMENT
				Payment payment = new Payment();
				payment.setInvoice(invoice);
				payment.setPaymentMethod(paymentMethod);
				payment.setAttemptNo(retryLog.getAttemptNo() + 1);
				payment.setAmountMinor(invoice.getTotalMinor());
				payment.setCurrency(invoice.getCurrency());
				payment.setStatus(Status.PENDING);
				payment.setResponseCode("PENDING");
				payment.setIdempotencyKey(
						"retry-" + invoice.getId() + "-" + retryLog.getAttemptNo() + "-" + System.currentTimeMillis());
				paymentRepository.save(payment);

//                 CHARGE PAYMENT
				try {
					String gatewayRef =
							mockPaymentGateway.charge(
									paymentMethod.getGatewayToken(),
									invoice.getTotalMinor(),
									invoice.getCurrency()
							);
					payment.setGatewayRef(gatewayRef);
					payment.setStatus(Status.SUCCESS);
					payment.setResponseCode("SUCCESS");
					paymentRepository.save(payment);

//                     MARK RETRY SUCCESS
					retryLog.setStatus(DunningStatus.SUCCESS);
					dunningRetryLogRepository.save(retryLog);

//  MARK INVOICE PAID
					invoice.setStatus(Status.PAID);
					invoice.setBalanceMinor(0L);
					invoiceRepository.save(invoice);

//                     * ADVANCE SUBSCRIPTION
					advanceSubscription(subscription);
				} catch (CustomException ex) {

//                    PAYMENT FAILED
					payment.setStatus(Status.FAILED);
					payment.setResponseCode("FAILED");
					payment.setFailureReason(
							ex.getMessage()
					);
					paymentRepository.save(payment);

//                      MARK RETRY FAILED
					retryLog.setStatus(DunningStatus.FAILED);
					retryLog.setFailureReason(ex.getMessage());
					dunningRetryLogRepository.save(retryLog);

//                     CANCEL AFTER FINAL ATTEMPT
					if (retryLog.getAttemptNo() >= 3) {
						subscription.setStatus(Status.CANCELED);
						subscriptionRepository.save(subscription);
						continue;
					}

//                    CREATE NEXT RETRY
					createNextRetryLog(
							invoice, payment, retryLog.getAttemptNo() + 1);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void createNextRetryLog(
			Invoice invoice,
			Payment payment,
			int attemptNo
	) {
		DunningRetryLog log = new DunningRetryLog();
		log.setInvoice(invoice);
		log.setPayment(payment);
		log.setAttemptNo(attemptNo);
		switch (attemptNo) {
		case 2:
			log.setScheduledAt(LocalDateTime.now().plusDays(2));
			break;
		case 3:
			log.setScheduledAt(LocalDateTime.now().plusDays(4));
			break;
		default:
			log.setScheduledAt(LocalDateTime.now().plusDays(4));
		}
		log.setStatus(DunningStatus.SCHEDULED);
		dunningRetryLogRepository.save(log);
	}

	private void advanceSubscription(
			Subscription subscription
	) {
		LocalDate start = subscription.getCurrentPeriodEnd().plusDays(1);
		LocalDate end;
		BillingPeriod billingPeriod = subscription.getPlan().getBillingPeriod();
		switch (billingPeriod) {
		case MONTHLY:
			end = start.plusMonths(1).minusDays(1);
			break;
		case YEARLY:
			end = start.plusYears(1).minusDays(1);
			break;
		default:
			throw new RuntimeException(
					"Unsupported billing period");
		}
		subscription.setCurrentPeriodStart(start);
		subscription.setCurrentPeriodEnd(end);
		subscription.setStatus(Status.ACTIVE);
		subscriptionRepository.save(subscription);
	}
}
