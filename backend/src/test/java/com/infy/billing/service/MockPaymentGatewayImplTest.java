package com.infy.billing.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.infy.billing.exception.CustomException;

class MockPaymentGatewayImplTest {

    private MockPaymentGatewayImpl mockPaymentGateway;

    @BeforeEach
    void setUp() {
        mockPaymentGateway = new MockPaymentGatewayImpl();
    }

    @Test
    void testCharge_Success() {
        String ref = mockPaymentGateway.charge("valid_token", 1000L, "USD");
        assertNotNull(ref);
        assertTrue(ref.startsWith("mock_charge_"));
    }

    @Test
    void testCharge_DeclineCard() {
        assertThrows(CustomException.class, () -> 
            mockPaymentGateway.charge("decline_token", 1000L, "USD")
        );
    }

    @Test
    void testCharge_DeclineUpi() {
        assertThrows(CustomException.class, () -> 
            mockPaymentGateway.charge("decline@upi", 1000L, "USD")
        );
    }

    @Test
    void testCharge_NullToken() {
        assertThrows(CustomException.class, () -> 
            mockPaymentGateway.charge(null, 1000L, "USD")
        );
    }

    @Test
    void testRefund_Success() {
        String ref = mockPaymentGateway.refund("mock_charge_123", 500L, "USD");
        assertNotNull(ref);
        assertTrue(ref.startsWith("mock_refund_"));
    }

    @Test
    void testCharge_SuccessTokenBypass() {
        // "success" in token should bypass random failure
        String ref = mockPaymentGateway.charge("success_token_123", 1000L, "INR");
        assertNotNull(ref);
        assertTrue(ref.startsWith("mock_charge_"));
    }

    @Test
    void testCharge_ValidTokenBypass() {
        // "valid" in token should bypass random failure
        String ref = mockPaymentGateway.charge("valid_card_456", 2000L, "EUR");
        assertNotNull(ref);
        assertTrue(ref.startsWith("mock_charge_"));
    }

    @Test
    void testCharge_StripeDeclineCard() {
        // Stripe-style test card 4000000000000002
        assertThrows(CustomException.class, () ->
            mockPaymentGateway.charge("4000000000000002", 1000L, "USD")
        );
    }

    @Test
    void testCharge_StripeDeclineCard2() {
        assertThrows(CustomException.class, () ->
            mockPaymentGateway.charge("4000000000009995", 1000L, "USD")
        );
    }

    @Test
    void testCharge_FailUpi() {
        assertThrows(CustomException.class, () ->
            mockPaymentGateway.charge("fail@upi", 1000L, "INR")
        );
    }

    @Test
    void testCharge_ZeroUpi() {
        assertThrows(CustomException.class, () ->
            mockPaymentGateway.charge("0000000000@upi", 1000L, "INR")
        );
    }
}
