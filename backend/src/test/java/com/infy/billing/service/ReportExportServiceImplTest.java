package com.infy.billing.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.infy.billing.dto.finance.FinanceDashboardDTO;

public class ReportExportServiceImplTest {

    private ReportExportServiceImpl reportExportService;
    private FinanceDashboardDTO data;

    @BeforeEach
    void setUp() {
        reportExportService = new ReportExportServiceImpl();
        data = FinanceDashboardDTO.builder()
                .mrrMinor(1000L)
                .arrMinor(12000L)
                .arpuMinor(100L)
                .netChurnPercent(new java.math.BigDecimal("2.5"))
                .ltvMinor(5000L)
                .activeCustomers(10)
                .revenueByPlan(new ArrayList<>())
                .revenueByRegion(new ArrayList<>())
                .revenueTrend(new ArrayList<>())
                .build();
    }

    @Test
    void testExportDashboardAsCsv() {
        byte[] csv = reportExportService.exportDashboardAsCsv(data);
        assertNotNull(csv);
        assertTrue(csv.length > 0);
        String csvStr = new String(csv);
        assertTrue(csvStr.contains("MRR"));
    }

    @Test
    void testExportDashboardAsPdf() {
        byte[] pdf = reportExportService.exportDashboardAsPdf(data);
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }
}
