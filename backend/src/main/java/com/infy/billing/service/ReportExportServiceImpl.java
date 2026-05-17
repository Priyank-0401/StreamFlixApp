package com.infy.billing.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;

import com.infy.billing.dto.finance.FinanceDashboardDTO;
import com.infy.billing.dto.finance.PlanRevenueDTO;
import com.infy.billing.dto.finance.RegionRevenueDTO;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class ReportExportServiceImpl implements ReportExportService {

    private static final java.awt.Color BRAND_COLOR     = new java.awt.Color(91, 79, 255);
    private static final java.awt.Color HEADER_BG_COLOR = new java.awt.Color(240, 240, 240);

    private static final com.lowagie.text.Font TITLE_FONT =
            new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18,
                    com.lowagie.text.Font.BOLD, BRAND_COLOR);
    private static final com.lowagie.text.Font HEADER_FONT =
            new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12,
                    com.lowagie.text.Font.BOLD);
    private static final com.lowagie.text.Font NORMAL_FONT =
            new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10,
                    com.lowagie.text.Font.NORMAL);

    // CSV Export

    @Override
    public byte[] exportDashboardAsCsv(FinanceDashboardDTO data) {
        StringBuilder csv = new StringBuilder();

        csv.append("Metric,Value\n");
        csv.append("MRR,").append(formatAmount(data.getMrrMinor())).append("\n");
        csv.append("ARR,").append(formatAmount(data.getArrMinor())).append("\n");
        csv.append("ARPU,").append(formatAmount(data.getArpuMinor())).append("\n");
        csv.append("Net Churn %,").append(data.getNetChurnPercent()).append("\n");
        csv.append("LTV,").append(formatAmount(data.getLtvMinor())).append("\n");
        csv.append("Active Customers,").append(data.getActiveCustomers()).append("\n");

        csv.append("\nRevenue by Plan (MRR)\n");
        csv.append("Plan ID,Plan Name,MRR Contribution (INR)\n");
        for (PlanRevenueDTO p : data.getRevenueByPlan()) {
            csv.append(p.getPlanId()).append(",")
               .append(escapeCsv(p.getPlanName())).append(",")
               .append(formatAmount(p.getRevenueMinor())).append("\n");
        }

        csv.append("\nRevenue by Region (MRR)\n");
        csv.append("Region,MRR Contribution (INR)\n");
        for (RegionRevenueDTO r : data.getRevenueByRegion()) {
            csv.append(escapeCsv(r.getRegion())).append(",")
               .append(formatAmount(r.getRevenueMinor())).append("\n");
        }

        csv.append("\nMonthly Revenue Trend\n");
        csv.append("Month,Year,MRR (INR)\n");
        data.getRevenueTrend().forEach(t ->
            csv.append(t.getMonth()).append(",")
               .append(t.getYear()).append(",")
               .append(formatAmount(t.getValueMinor())).append("\n")
        );

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    // PDF Export

    @Override
    public byte[] exportDashboardAsPdf(FinanceDashboardDTO data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph title = new Paragraph("Revenue Analytics Report", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Key Performance Indicators", HEADER_FONT));
            document.add(Chunk.NEWLINE);
            PdfPTable kpiTable = new PdfPTable(2);
            kpiTable.setWidthPercentage(100);
            addTableHeader(kpiTable, "Metric", "Value");
            addTableRow(kpiTable, "MRR (INR)",        formatAmount(data.getMrrMinor()));
            addTableRow(kpiTable, "ARR (INR)",        formatAmount(data.getArrMinor()));
            addTableRow(kpiTable, "ARPU (INR)",       formatAmount(data.getArpuMinor()));
            addTableRow(kpiTable, "Net Churn",        data.getNetChurnPercent() + "%");
            addTableRow(kpiTable, "LTV (INR)",        formatAmount(data.getLtvMinor()));
            addTableRow(kpiTable, "Active Customers", String.valueOf(data.getActiveCustomers()));
            document.add(kpiTable);
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Revenue by Plan (MRR)", HEADER_FONT));
            document.add(Chunk.NEWLINE);
            PdfPTable planTable = new PdfPTable(3);
            planTable.setWidthPercentage(100);
            addTableHeader(planTable, "Plan ID", "Plan Name", "MRR Contribution (INR)");
            for (PlanRevenueDTO p : data.getRevenueByPlan()) {
                addTableRow(planTable,
                        String.valueOf(p.getPlanId()),
                        p.getPlanName(),
                        formatAmount(p.getRevenueMinor()));
            }
            document.add(planTable);
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Revenue by Region (MRR)", HEADER_FONT));
            document.add(Chunk.NEWLINE);
            PdfPTable regionTable = new PdfPTable(2);
            regionTable.setWidthPercentage(100);
            addTableHeader(regionTable, "Region", "MRR Contribution (INR)");
            for (RegionRevenueDTO r : data.getRevenueByRegion()) {
                addTableRow(regionTable, r.getRegion(), formatAmount(r.getRevenueMinor()));
            }
            document.add(regionTable);
            document.add(Chunk.NEWLINE);

//            document.add(new Paragraph("Monthly Revenue Trend", HEADER_FONT));
//            document.add(Chunk.NEWLINE);
//            PdfPTable trendTable = new PdfPTable(3);
//            trendTable.setWidthPercentage(100);
//            addTableHeader(trendTable, "Month", "Year", "MRR (INR)");
//            data.getRevenueTrend().forEach(t ->
//                addTableRow(trendTable,
//                        t.getMonth(),
//                        String.valueOf(t.getYear()),
//                        formatAmount(t.getValueMinor()))
//            );
            //document.add(trendTable);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF report", e);
        }

        return out.toByteArray();
    }

    // Helpers

    private void addTableHeader(PdfPTable table, String... headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
            cell.setBackgroundColor(HEADER_BG_COLOR);
            cell.setPadding(6);
            table.addCell(cell);
        }
    }

    private void addTableRow(PdfPTable table, String... cells) {
        for (String cell : cells) {
            PdfPCell pdfCell = new PdfPCell(new Phrase(cell, NORMAL_FONT));
            pdfCell.setPadding(5);
            table.addCell(pdfCell);
        }
    }

    private String formatAmount(long minor) {
        return String.format("%.2f", minor / 100.0);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
