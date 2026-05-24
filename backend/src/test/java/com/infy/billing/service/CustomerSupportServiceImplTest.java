package com.infy.billing.service;

import com.infy.billing.dto.customer.SupportMessageDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CustomerSupportServiceImplTest {

    private CustomerSupportServiceImpl customerSupportService;

    @BeforeEach
    void setUp() {
        customerSupportService = new CustomerSupportServiceImpl();
    }

    @Test
    void testGetFAQs_ReturnsNonEmptyList() {
        List<Map<String, String>> faqs = customerSupportService.getFAQs();
        assertNotNull(faqs);
        assertFalse(faqs.isEmpty());
    }

    @Test
    void testGetFAQs_ReturnsCorrectCount() {
        List<Map<String, String>> faqs = customerSupportService.getFAQs();
        assertEquals(8, faqs.size());
    }

    @Test
    void testGetFAQs_FirstFAQ_IsAccountCategory() {
        List<Map<String, String>> faqs = customerSupportService.getFAQs();
        assertEquals("ACCOUNT", faqs.get(0).get("category"));
    }

    @Test
    void testGetFAQs_ContainsBillingCategory() {
        List<Map<String, String>> faqs = customerSupportService.getFAQs();
        boolean hasBilling = faqs.stream()
                .anyMatch(faq -> "BILLING".equals(faq.get("category")));
        assertTrue(hasBilling);
    }

    @Test
    void testGetFAQs_ContainsPaymentCategory() {
        List<Map<String, String>> faqs = customerSupportService.getFAQs();
        boolean hasPayment = faqs.stream()
                .anyMatch(faq -> "PAYMENT".equals(faq.get("category")));
        assertTrue(hasPayment);
    }

    @Test
    void testGetFAQs_ContainsFeaturesCategory() {
        List<Map<String, String>> faqs = customerSupportService.getFAQs();
        boolean hasFeatures = faqs.stream()
                .anyMatch(faq -> "FEATURES".equals(faq.get("category")));
        assertTrue(hasFeatures);
    }

    @Test
    void testGetFAQs_AllEntriesHaveRequiredKeys() {
        List<Map<String, String>> faqs = customerSupportService.getFAQs();
        for (Map<String, String> faq : faqs) {
            assertTrue(faq.containsKey("question"), "FAQ missing 'question' key");
            assertTrue(faq.containsKey("answer"), "FAQ missing 'answer' key");
            assertTrue(faq.containsKey("category"), "FAQ missing 'category' key");
        }
    }

    @Test
    void testGetFAQs_NoEmptyValues() {
        List<Map<String, String>> faqs = customerSupportService.getFAQs();
        for (Map<String, String> faq : faqs) {
            assertFalse(faq.get("question").isBlank(), "FAQ has blank question");
            assertFalse(faq.get("answer").isBlank(), "FAQ has blank answer");
            assertFalse(faq.get("category").isBlank(), "FAQ has blank category");
        }
    }

    @Test
    void testGetFAQs_BillingCategoryCount() {
        List<Map<String, String>> faqs = customerSupportService.getFAQs();
        long billingCount = faqs.stream()
                .filter(faq -> "BILLING".equals(faq.get("category")))
                .count();
        assertEquals(3, billingCount);
    }

    @Test
    void testGetFAQs_AccountCategoryCount() {
        List<Map<String, String>> faqs = customerSupportService.getFAQs();
        long accountCount = faqs.stream()
                .filter(faq -> "ACCOUNT".equals(faq.get("category")))
                .count();
        assertEquals(2, accountCount);
    }

    @Test
    void testGetFAQs_FeaturesCategoryCount() {
        List<Map<String, String>> faqs = customerSupportService.getFAQs();
        long featuresCount = faqs.stream()
                .filter(faq -> "FEATURES".equals(faq.get("category")))
                .count();
        assertEquals(2, featuresCount);
    }

    @Test
    void testGetFAQs_PaymentCategoryCount() {
        List<Map<String, String>> faqs = customerSupportService.getFAQs();
        long paymentCount = faqs.stream()
                .filter(faq -> "PAYMENT".equals(faq.get("category")))
                .count();
        assertEquals(1, paymentCount);
    }

    @Test
    void testGetFAQs_CancelQuestionExists() {
        List<Map<String, String>> faqs = customerSupportService.getFAQs();
        boolean hasCancelQuestion = faqs.stream()
                .anyMatch(faq -> faq.get("question").toLowerCase().contains("cancel"));
        assertTrue(hasCancelQuestion, "Expected a FAQ about cancellation");
    }

    @Test
    void testGetFAQs_PauseQuestionExists() {
        List<Map<String, String>> faqs = customerSupportService.getFAQs();
        boolean hasPauseQuestion = faqs.stream()
                .anyMatch(faq -> faq.get("question").toLowerCase().contains("pause"));
        assertTrue(hasPauseQuestion, "Expected a FAQ about pausing");
    }

    @Test
    void testSendSupportMessage_DoesNotThrow() {
        SupportMessageDTO dto = new SupportMessageDTO();
        dto.setSubject("Test Subject");
        dto.setMessage("Test message body");
        dto.setCategory("BILLING");
        assertDoesNotThrow(() -> customerSupportService.sendSupportMessage("user@example.com", dto));
    }

    @Test
    void testSendSupportMessage_NullEmail_DoesNotThrow() {
        SupportMessageDTO dto = new SupportMessageDTO();
        dto.setSubject("Test Subject");
        dto.setMessage("Test message body");
        dto.setCategory("BILLING");
        assertDoesNotThrow(() -> customerSupportService.sendSupportMessage(null, dto));
    }

    @Test
    void testSendSupportMessage_NullMessage_DoesNotThrow() {
        assertDoesNotThrow(() -> customerSupportService.sendSupportMessage("user@example.com", null));
    }

    @Test
    void testSendSupportMessage_WithPopulatedDTO() {
        SupportMessageDTO dto = new SupportMessageDTO();
        dto.setSubject("Billing Issue");
        dto.setMessage("I was charged twice for my subscription this month.");
        dto.setCategory("BILLING");
        assertDoesNotThrow(() -> customerSupportService.sendSupportMessage("customer@test.com", dto));
    }

    @Test
    void testGetFAQs_ImmutableList() {
        List<Map<String, String>> faqs = customerSupportService.getFAQs();
        assertThrows(UnsupportedOperationException.class, () -> faqs.add(Map.of("question", "test", "answer", "test", "category", "test")));
    }

    @Test
    void testGetFAQs_ConsistentResults() {
        List<Map<String, String>> firstCall = customerSupportService.getFAQs();
        List<Map<String, String>> secondCall = customerSupportService.getFAQs();
        assertEquals(firstCall.size(), secondCall.size());
        for (int i = 0; i < firstCall.size(); i++) {
            assertEquals(firstCall.get(i).get("question"), secondCall.get(i).get("question"));
            assertEquals(firstCall.get(i).get("answer"), secondCall.get(i).get("answer"));
            assertEquals(firstCall.get(i).get("category"), secondCall.get(i).get("category"));
        }
    }
}
