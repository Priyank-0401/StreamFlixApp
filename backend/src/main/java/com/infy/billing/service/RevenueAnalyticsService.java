package com.infy.billing.service;

import com.infy.billing.dto.finance.*;

import java.util.List;

public interface RevenueAnalyticsService {
    FinanceDashboardDTO getFinanceDashboard();
    MrrReportDTO getMrrReport();
    ArrReportDTO getArrReport();
    ChurnReportDTO getChurnReport();
    ArpuLtvReportDTO getArpuLtvReport();
    List<InvoiceRecordDTO> getAllInvoiceRecords();
    List<PaymentRecordDTO> getAllPaymentRecords();
    List<RefundCreditDTO> getAllRefundCredits();
    List<RevenueSnapshotDTO> getAllRevenueSnapshots();
}
