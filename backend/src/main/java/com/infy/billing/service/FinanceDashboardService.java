package com.infy.billing.service;

import com.infy.billing.dto.customer.InvoiceDTO;
import com.infy.billing.dto.finance.*;
import java.util.List;

public interface FinanceDashboardService {
    FinanceStatsResponse getFinanceStats();
    List<SubscriptionFinanceDTO> getMrrSubscriptions();
    List<SubscriptionFinanceDTO> getArrSubscriptions();
    List<CustomerFinanceDTO> getArpuCustomers();
    List<ChurnFinanceDTO> getChurnedSubscriptions();
    List<InvoiceDTO> getAllInvoices(String status);
    void recordDailyRevenueSnapshot();
}
