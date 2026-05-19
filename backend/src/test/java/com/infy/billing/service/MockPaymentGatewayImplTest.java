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
}
