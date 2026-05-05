package com.infy.billing.service;

import com.infy.billing.dto.customer.*;
import com.infy.billing.request.*;

import java.util.List;

public interface CustomerSubscriptionService {

   SubscriptionDTO getCurrentSubscription(String email);

   SubscriptionDTO createSubscription(String email, CreateSubscriptionRequest request);

   SubscriptionDTO upgradeSubscription(String email, UpgradeSubscriptionRequest request);

   void cancelSubscription(String email, boolean atPeriodEnd);

   SubscriptionDTO pauseSubscription(String email, PauseSubscriptionRequest request);

   SubscriptionDTO resumeSubscription(String email);

   SubscriptionDTO addAddOn(String email, Long addonId);

   SubscriptionDTO removeAddOn(String email, Long addonId);

   List<UsageRecordDTO> getMeteredUsage(String email, String startDate, String endDate);
}
