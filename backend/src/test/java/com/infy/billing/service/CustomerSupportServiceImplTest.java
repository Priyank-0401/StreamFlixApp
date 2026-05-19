package com.infy.billing.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomerSupportServiceImplTest {

    private CustomerSupportServiceImpl customerSupportService;

    @BeforeEach
    void setUp() {
        customerSupportService = new CustomerSupportServiceImpl();
    }

    @Test
    void testGetFAQs() {
        List<Map<String, String>> faqs = customerSupportService.getFAQs();

        assertNotNull(faqs);
        assertTrue(faqs.size() > 0);
        assertEquals("ACCOUNT", faqs.get(0).get("category"));
    }

    @Test
    void testSendSupportMessage() {
        // Just verify it doesn't throw exception
        customerSupportService.sendSupportMessage("test@test.com", new com.infy.billing.dto.customer.SupportMessageDTO());
    }
}
