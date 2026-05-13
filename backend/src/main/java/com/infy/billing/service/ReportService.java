package com.infy.billing.service;

/**
 * Service for generating various business reports.
 */
public interface ReportService {

    /**
     * Generates a high-level revenue snapshot PDF.
     * @return PDF as byte array
     */
    byte[] generateRevenueSnapshotPdf();

    /**
     * Generates a tax compliance report as CSV.
     * @return CSV as byte array
     */
    byte[] generateTaxReportCsv();
}
