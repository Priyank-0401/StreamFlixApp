package com.infy.billing.service;

import com.infy.billing.exception.CustomException;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.Set;

/**
 * Implementation of {@link MockPaymentGateway}.
 *
 * <p>Simulates a payment gateway for testing and development.</p>
 *
 * <p>Deterministic failures:</p>
 * <ul>
 *   <li>Card numbers containing "decline" always fail</li>
 *   <li>Card number 4000000000000002 always fails (Stripe-style test card)</li>
 *   <li>UPI IDs "decline@upi", "fail@upi", "0000000000@upi" always fail</li>
 * </ul>
 *
 * <p>For non-test payment methods, a 20% random failure rate is applied
 * to simulate real-world payment unreliability.</p>
 */
@Component
public class MockPaymentGatewayImpl implements MockPaymentGateway {

    private static final Set<String> DECLINE_TOKENS = Set.of(
            "decline",
            "4000000000000002",
            "4000000000009995",
            "4000000000000127"
    );

    private static final Set<String> DECLINE_UPI_IDS = Set.of(
            "decline@upi",
            "fail@upi",
            "0000000000@upi"
    );

    private static final double DEFAULT_RANDOM_FAILURE_RATE = 0.20; // 20% random failure for realism

    private final Random random = new Random();

    @Override
    public String charge(String gatewayToken, long amountMinor, String currency) {
        if (shouldDecline(gatewayToken)) {
            throw CustomException.paymentFailed(
                    "Payment declined. Use a different payment method or contact your bank."
            );
        }
        return "mock_charge_" + System.currentTimeMillis();
    }

    private boolean shouldDecline(String gatewayToken) {
        if (gatewayToken == null) {
            return true;
        }
        String lower = gatewayToken.toLowerCase();

        // Card decline patterns
        for (String pattern : DECLINE_TOKENS) {
            if (lower.contains(pattern.toLowerCase())) {
                return true;
            }
        }

        // UPI decline patterns
        for (String pattern : DECLINE_UPI_IDS) {
            if (lower.equals(pattern.toLowerCase())) {
                return true;
            }
        }

        // Random failure for non-test cards/UPIs
        if (DEFAULT_RANDOM_FAILURE_RATE > 0.0 && random.nextDouble() < DEFAULT_RANDOM_FAILURE_RATE) {
            return true;
        }
        return false;
    }
}
