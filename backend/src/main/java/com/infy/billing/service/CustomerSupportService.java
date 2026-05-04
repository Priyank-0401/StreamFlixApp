package com.infy.billing.service;

import com.infy.billing.dto.customer.SupportMessageDTO;

import java.util.List;
import java.util.Map;

public interface CustomerSupportService {

   List<Map<String, String>> getFAQs();

   void sendSupportMessage(String email, SupportMessageDTO message);
}
