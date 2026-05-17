package com.infy.billing.dto.support;

import com.infy.billing.dto.customer.CustomerProfileDTO;
import com.infy.billing.dto.customer.SubscriptionDTO;
import com.infy.billing.dto.customer.InvoiceDTO;
import com.infy.billing.dto.customer.UsageRecordDTO;
import com.infy.billing.dto.customer.NotificationDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDetailResponse {
    private CustomerProfileDTO customerProfile;
    private List<SubscriptionDTO> subscriptions;
    private List<InvoiceDTO> invoices;
    private List<UsageRecordDTO> usageRecords;
    private List<NotificationDTO> notifications;
}
