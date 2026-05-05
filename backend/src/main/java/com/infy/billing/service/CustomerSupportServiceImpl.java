package com.infy.billing.service;

import com.infy.billing.dto.customer.SupportMessageDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CustomerSupportServiceImpl implements CustomerSupportService {

   public List<Map<String, String>> getFAQs() {
       return List.of(
               Map.of("question", "How do I change my plan?",
                      "answer", "Go to the Subscription page and click 'Change Plan' to upgrade or downgrade.",
                      "category", "ACCOUNT"),
               Map.of("question", "Can I cancel anytime?",
                      "answer", "Yes! You can cancel at any time. Access continues until the end of your billing period.",
                      "category", "BILLING"),
               Map.of("question", "How do I update my payment method?",
                      "answer", "Visit Payment Methods page to add, edit, or remove payment options.",
                      "category", "PAYMENT"),
               Map.of("question", "What happens when my trial ends?",
                      "answer", "Your subscription will automatically start billing based on your selected plan.",
                      "category", "BILLING"),
               Map.of("question", "How do I download my invoices?",
                      "answer", "Go to the Billing page, find your invoice, and click the download button.",
                      "category", "BILLING"),
               Map.of("question", "What is the Ad-Free add-on?",
                      "answer", "The Ad-Free add-on removes all advertisements for ₹99/month.",
                      "category", "FEATURES"),
               Map.of("question", "How is download storage calculated?",
                      "answer", "You get 5-10GB free depending on your plan. Additional storage is ₹20/GB.",
                      "category", "FEATURES"),
               Map.of("question", "Can I pause my subscription?",
                      "answer", "Yes, you can pause for up to 1 month. You won't be charged during the pause period.",
                      "category", "ACCOUNT")
       );
   }

   public void sendSupportMessage(String email, SupportMessageDTO message) {
       // Support message logic removed
   }
}
