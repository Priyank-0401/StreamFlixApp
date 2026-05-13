package com.infy.billing.controller;

import com.infy.billing.dto.customer.InvoiceDTO;
import com.infy.billing.dto.finance.*;
import com.infy.billing.service.FinanceDashboardService;
import com.infy.billing.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('FINANCE')")
public class FinanceController {

    private final FinanceDashboardService financeService;
    private final ReportService reportService;

    @GetMapping("/stats")
    public ResponseEntity<FinanceStatsResponse> getStats() {
        return ResponseEntity.ok(financeService.getFinanceStats());
    }

    @GetMapping("/mrr-subscriptions")
    public ResponseEntity<List<SubscriptionFinanceDTO>> getMrrSubscriptions() {
        return ResponseEntity.ok(financeService.getMrrSubscriptions());
    }

    @GetMapping("/arr-subscriptions")
    public ResponseEntity<List<SubscriptionFinanceDTO>> getArrSubscriptions() {
        return ResponseEntity.ok(financeService.getArrSubscriptions());
    }

    @GetMapping("/arpu-customers")
    public ResponseEntity<List<CustomerFinanceDTO>> getArpuCustomers() {
        return ResponseEntity.ok(financeService.getArpuCustomers());
    }

    @GetMapping("/churned-subscriptions")
    public ResponseEntity<List<ChurnFinanceDTO>> getChurnedSubscriptions() {
        return ResponseEntity.ok(financeService.getChurnedSubscriptions());
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceDTO>> getInvoices(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(financeService.getAllInvoices(status));
    }

    @PostMapping("/snapshots/record")
    public ResponseEntity<Void> recordSnapshot() {
        financeService.recordDailyRevenueSnapshot();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reports/revenue-snapshot")
    public ResponseEntity<byte[]> downloadRevenueSnapshot() {
        byte[] pdf = reportService.generateRevenueSnapshotPdf();
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=revenue_snapshot.pdf")
                .body(pdf);
    }

    @GetMapping("/reports/tax-compliance")
    public ResponseEntity<byte[]> downloadTaxReport() {
        byte[] csv = reportService.generateTaxReportCsv();
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=tax_compliance_report.csv")
                .body(csv);
    }
}
