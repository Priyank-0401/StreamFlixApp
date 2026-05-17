package com.infy.billing.service;

import com.infy.billing.dto.finance.FinanceDashboardDTO;

public interface ReportExportService {
    byte[] exportDashboardAsCsv(FinanceDashboardDTO dashboardData);
    byte[] exportDashboardAsPdf(FinanceDashboardDTO dashboardData);
}
