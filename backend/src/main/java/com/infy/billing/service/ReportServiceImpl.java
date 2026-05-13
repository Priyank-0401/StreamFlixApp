package com.infy.billing.service;

import com.infy.billing.dto.finance.FinanceStatsResponse;
import com.infy.billing.entity.Invoice;
import com.infy.billing.enums.Status;
import com.infy.billing.repository.InvoiceRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final FinanceDashboardService financeService;
    private final InvoiceRepository invoiceRepository;

    // ── Color Palette (Matching Invoice Style) ──
    private static final Color BRAND_COLOR = new Color(91, 79, 255); // #5b4fff
    private static final Color HEADER_BG = new Color(249, 250, 251); // #f9fafb
    private static final Color BORDER_COLOR = new Color(229, 231, 235); // #e5e7eb
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55); // #1f2937
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128); // #6b7280

    // ── Fonts ──
    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 22, Font.BOLD, BRAND_COLOR);
    private static final Font SECTION_HEADER_FONT = new Font(Font.HELVETICA, 14, Font.BOLD, TEXT_PRIMARY);
    private static final Font LABEL_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, TEXT_SECONDARY);
    private static final Font VALUE_FONT = new Font(Font.HELVETICA, 16, Font.BOLD, BRAND_COLOR);
    private static final Font TABLE_HEADER_FONT = new Font(Font.HELVETICA, 10, Font.BOLD, TEXT_SECONDARY);
    private static final Font TABLE_CELL_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, TEXT_PRIMARY);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @Override
    public byte[] generateRevenueSnapshotPdf() {
        FinanceStatsResponse stats = financeService.getFinanceStats();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // 1. Header
            addReportHeader(document, "REVENUE SNAPSHOT");
            document.add(Chunk.NEWLINE);

            // 2. Summary Cards (MRR, ARR, LTV, Churn)
            addMetricsSummary(document, stats);
            document.add(Chunk.NEWLINE);

            // 3. Collection Stats
            addCollectionStats(document, stats);
            document.add(Chunk.NEWLINE);

            // 4. Footer
            addFooter(document);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Revenue Snapshot PDF", e);
        }

        return out.toByteArray();
    }

    @Override
    public byte[] generateTaxReportCsv() {
        List<Invoice> paidInvoices = invoiceRepository.findAll().stream()
                .filter(i -> i.getStatus() == Status.PAID && i.getTaxMinor() > 0)
                .toList();

        StringBuilder csv = new StringBuilder();
        csv.append("Invoice Number,Country,Issue Date,Subtotal,Tax,Total,Currency\n");

        for (Invoice inv : paidInvoices) {
            csv.append(String.format("%s,%s,%s,%.2f,%.2f,%.2f,%s\n",
                    inv.getInvoiceNumber(),
                    inv.getCustomer().getCountry(),
                    inv.getIssueDate(),
                    inv.getSubtotalMinor() / 100.0,
                    inv.getTaxMinor() / 100.0,
                    inv.getTotalMinor() / 100.0,
                    inv.getCurrency()));
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void addReportHeader(Document document, String titleText) throws DocumentException {
        Paragraph title = new Paragraph(titleText, TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph date = new Paragraph("Generated on: " + LocalDate.now().format(DATE_FMT), TABLE_HEADER_FONT);
        date.setAlignment(Element.ALIGN_CENTER);
        document.add(date);

        // Divider
        document.add(new Paragraph(" "));
        PdfPTable divider = new PdfPTable(1);
        divider.setWidthPercentage(100);
        PdfPCell line = new PdfPCell();
        line.setBorder(PdfPCell.BOTTOM);
        line.setBorderColor(BRAND_COLOR);
        line.setBorderWidth(2);
        divider.addCell(line);
        document.add(divider);
    }

    private void addMetricsSummary(Document document, FinanceStatsResponse stats) throws DocumentException {
        document.add(new Paragraph("KEY PERFORMANCE INDICATORS", SECTION_HEADER_FONT));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);

        addMetricCell(table, "Monthly Recurring Revenue (MRR)", formatCurrency(stats.getMrrMinor(), "INR"));
        addMetricCell(table, "Annual Recurring Revenue (ARR)", formatCurrency(stats.getArrMinor(), "INR"));
        addMetricCell(table, "Average Revenue Per User (ARPU)", formatCurrency(stats.getArpuMinor(), "INR"));
        addMetricCell(table, "Lifetime Value (LTV)", formatCurrency(stats.getLtvMinor(), "INR"));
        addMetricCell(table, "Churn Rate", String.format("%.2f%%", stats.getChurnRate()));
        addMetricCell(table, "Active Customers", String.valueOf(stats.getPaidInvoices())); // Approximation

        document.add(table);
    }

    private void addCollectionStats(Document document, FinanceStatsResponse stats) throws DocumentException {
        document.add(new Paragraph("INVOICE & COLLECTION SUMMARY", SECTION_HEADER_FONT));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);

        addTableHeader(table, "Metric");
        addTableHeader(table, "Count");
        addTableHeader(table, "Amount (INR equivalent)");

        addTableRow(table, "Total Collected", String.valueOf(stats.getPaidInvoices()),
                formatCurrency(stats.getTotalCollectedMinor(), "INR"));
        addTableRow(table, "Pending Collection", String.valueOf(stats.getPendingInvoices()),
                formatCurrency(stats.getPendingCollectionMinor(), "INR"));
        addTableRow(table, "Failed/Void Invoices", String.valueOf(stats.getFailedInvoices()), "—");

        document.add(table);
    }

    private void addMetricCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(PdfPCell.BOX);
        cell.setBorderColor(BORDER_COLOR);
        cell.setPadding(15);
        cell.setBackgroundColor(HEADER_BG);

        Paragraph l = new Paragraph(label.toUpperCase(), LABEL_FONT);
        cell.addElement(l);
        Paragraph v = new Paragraph(value, VALUE_FONT);
        cell.addElement(v);

        table.addCell(cell);
    }

    private void addTableHeader(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, TABLE_HEADER_FONT));
        cell.setBackgroundColor(HEADER_BG);
        cell.setBorder(PdfPCell.BOTTOM);
        cell.setBorderColor(BORDER_COLOR);
        cell.setPadding(8);
        table.addCell(cell);
    }

    private void addTableRow(PdfPTable table, String metric, String count, String amount) {
        table.addCell(new PdfPCell(new Phrase(metric, TABLE_CELL_FONT)));
        table.addCell(new PdfPCell(new Phrase(count, TABLE_CELL_FONT)));
        table.addCell(new PdfPCell(new Phrase(amount, TABLE_CELL_FONT)));
    }

    private void addFooter(Document document) throws DocumentException {
        document.add(new Paragraph(" "));
        document.add(new Paragraph(" "));
        Paragraph footer = new Paragraph("StreamFlix Confidential - For Internal Use Only", TABLE_HEADER_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }

    private String formatCurrency(Long amountMinor, String currencyCode) {
        if (amountMinor == null)
            return "—";
        double amount = amountMinor / 100.0;
        try {
            NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
            fmt.setCurrency(Currency.getInstance(currencyCode));
            return fmt.format(amount);
        } catch (Exception e) {
            return currencyCode + " " + String.format("%.2f", amount);
        }
    }
}
