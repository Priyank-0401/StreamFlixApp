package com.infy.billing.service;

import com.infy.billing.dto.customer.InvoiceDTO;
import com.infy.billing.dto.customer.InvoiceLineItemDTO;
import com.infy.billing.dto.finance.*;
import com.infy.billing.entity.*;
import com.infy.billing.enums.Status;
import com.infy.billing.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinanceDashboardServiceImpl implements FinanceDashboardService {

    private final SubscriptionRepository subscriptionRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceLineItemRepository invoiceLineItemRepository;
    private final RevenueSnapshotRepository snapshotRepository;
    private final PriceBookEntryRepository priceBookEntryRepository;

    private long getMinorPriceForSubscription(Subscription s) {
        long minorPrice = s.getPlan().getDefaultPriceMinor();
        Optional<PriceBookEntry> entry = priceBookEntryRepository
                .findByPlan_IdAndRegionAndCurrency(s.getPlan().getId(), s.getCustomer().getCountry(), s.getCurrency());
        if (entry.isPresent()) {
            minorPrice = entry.get().getPriceMinor();
        }
        return minorPrice;
    }

    private long convertToINRMinor(long minorAmount, String currency) {
        if ("USD".equalsIgnoreCase(currency)) {
            return minorAmount * 95;
        } else if ("GBP".equalsIgnoreCase(currency)) {
            return minorAmount * 129;
        }
        return minorAmount;
    }

    @Override
    @Transactional(readOnly = true)
    public FinanceStatsResponse getFinanceStats() {
        List<Subscription> allSubscriptions = subscriptionRepository.findAll();

        long mrrMinorINR = 0L;
        Set<Long> activeCustomerIds = new HashSet<>();
        int canceledSubCount = 0;

        for (Subscription s : allSubscriptions) {
            if (s.getStatus() == Status.ACTIVE) {
                activeCustomerIds.add(s.getCustomer().getId());
                long priceMinor = getMinorPriceForSubscription(s);
                long monthlyMinor = s.getPlan().getBillingPeriod() == com.infy.billing.enums.BillingPeriod.MONTHLY
                        ? priceMinor
                        : (priceMinor / 12);
                mrrMinorINR += convertToINRMinor(monthlyMinor, s.getCurrency());
            } else if (s.getStatus() == Status.CANCELED) {
                canceledSubCount++;
            }
        }

        long arrMinorINR = mrrMinorINR * 12;
        long arpuMinorINR = activeCustomerIds.isEmpty() ? 0L : mrrMinorINR / activeCustomerIds.size();

        double totalSubs = allSubscriptions.size();
        double churnRate = totalSubs == 0 ? 0.0 : (canceledSubCount / totalSubs) * 100.0;

        long ltvMinorINR;
        if (churnRate > 0.0) {
            ltvMinorINR = (long) (arpuMinorINR / (churnRate / 100.0));
        } else {
            ltvMinorINR = arpuMinorINR * 20; // Default floor assuming 5% monthly churn / 20 month lifespan
        }

        // Invoice stats
        List<Invoice> invoices = invoiceRepository.findAll();
        int totalInvoices = invoices.size();
        int paidInvoices = 0;
        int pendingInvoices = 0;
        int failedInvoices = 0;
        long totalCollectedMinorINR = 0L;
        long pendingCollectionMinorINR = 0L;

        for (Invoice inv : invoices) {
            if (inv.getStatus() == Status.PAID) {
                paidInvoices++;
                totalCollectedMinorINR += convertToINRMinor(inv.getTotalMinor(), inv.getCurrency());
            } else if (inv.getStatus() == Status.OPEN || inv.getStatus() == Status.DRAFT) {
                pendingInvoices++;
                pendingCollectionMinorINR += convertToINRMinor(inv.getBalanceMinor(), inv.getCurrency());
            } else if (inv.getStatus() == Status.VOID || inv.getStatus() == Status.UNCOLLECTIBLE) {
                failedInvoices++;
            }
        }

        // Fetch recent snapshots
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        List<RevenueSnapshot> snapshots = snapshotRepository
                .findBySnapshotDateBetweenOrderBySnapshotDateAsc(thirtyDaysAgo, LocalDate.now());

        return FinanceStatsResponse.builder()
                .mrrMinor(mrrMinorINR)
                .arrMinor(arrMinorINR)
                .arpuMinor(arpuMinorINR)
                .ltvMinor(ltvMinorINR)
                .churnRate(churnRate)
                .totalInvoices(totalInvoices)
                .paidInvoices(paidInvoices)
                .pendingInvoices(pendingInvoices)
                .failedInvoices(failedInvoices)
                .totalCollectedMinor(totalCollectedMinorINR)
                .pendingCollectionMinor(pendingCollectionMinorINR)
                .recentSnapshots(snapshots)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionFinanceDTO> getMrrSubscriptions() {
        return subscriptionRepository.findAll().stream()
                .filter(s -> s.getStatus() == Status.ACTIVE)
                .map(this::mapToSubscriptionFinanceDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionFinanceDTO> getArrSubscriptions() {
        return subscriptionRepository.findAll().stream()
                .filter(s -> s.getStatus() == Status.ACTIVE)
                .map(this::mapToSubscriptionFinanceDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerFinanceDTO> getArpuCustomers() {
        List<Subscription> activeSubs = subscriptionRepository.findAll().stream()
                .filter(s -> s.getStatus() == Status.ACTIVE)
                .toList();

        Map<Customer, List<Subscription>> subsByCust = activeSubs.stream()
                .collect(Collectors.groupingBy(Subscription::getCustomer));

        List<CustomerFinanceDTO> list = new ArrayList<>();
        for (Map.Entry<Customer, List<Subscription>> entry : subsByCust.entrySet()) {
            Customer cust = entry.getKey();
            List<Subscription> subs = entry.getValue();

            long monthlySumMinor = 0L;
            for (Subscription s : subs) {
                long priceMinor = getMinorPriceForSubscription(s);
                long monthlyMinor = s.getPlan().getBillingPeriod() == com.infy.billing.enums.BillingPeriod.MONTHLY
                        ? priceMinor
                        : (priceMinor / 12);
                monthlySumMinor += monthlyMinor;
            }

            list.add(CustomerFinanceDTO.builder()
                    .customerId(cust.getId())
                    .fullName(cust.getUser().getFullName())
                    .email(cust.getUser().getEmail())
                    .country(cust.getCountry())
                    .currency(cust.getCurrency())
                    .activeSubscriptionsCount(subs.size())
                    .monthlyContributionMinor(monthlySumMinor)
                    .build());
        }

        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChurnFinanceDTO> getChurnedSubscriptions() {
        return subscriptionRepository.findAll().stream()
                .filter(s -> s.getStatus() == Status.CANCELED)
                .map(s -> {
                    long priceMinor = getMinorPriceForSubscription(s);
                    long lostMonthly = s.getPlan().getBillingPeriod() == com.infy.billing.enums.BillingPeriod.MONTHLY
                            ? priceMinor
                            : (priceMinor / 12);
                    return ChurnFinanceDTO.builder()
                            .subscriptionId(s.getId())
                            .customerName(s.getCustomer().getUser().getFullName())
                            .customerEmail(s.getCustomer().getUser().getEmail())
                            .planName(s.getPlan().getName())
                            .lostMonthlyRevenueMinor(lostMonthly)
                            .currency(s.getCurrency())
                            .canceledAt(s.getCanceledAt() != null ? s.getCanceledAt().toString() : "N/A")
                            .reason("Customer Cancellation")
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceDTO> getAllInvoices(String status) {
        List<Invoice> invoices = invoiceRepository.findAll();
        if (status != null && !status.trim().isEmpty()) {
            try {
                Status s = Status.valueOf(status.toUpperCase());
                invoices = invoices.stream().filter(i -> i.getStatus() == s).toList();
            } catch (Exception e) {
                // Ignore invalid status filter
            }
        }

        return invoices.stream().map(this::mapToInvoiceDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void recordDailyRevenueSnapshot() {
        FinanceStatsResponse stats = getFinanceStats();
        LocalDate today = LocalDate.now();

        RevenueSnapshot snapshot = snapshotRepository.findBySnapshotDate(today)
                .orElse(RevenueSnapshot.builder().snapshotDate(today).build());

        snapshot.setMrrMinor(stats.getMrrMinor());
        snapshot.setArrMinor(stats.getArrMinor());
        snapshot.setArpuMinor(stats.getArpuMinor());
        snapshot.setLtvMinor(stats.getLtvMinor());

        long activeCustomers = customerRepository.count();
        long canceledSubs = subscriptionRepository.countByStatus(Status.CANCELED);

        snapshot.setActiveCustomers((int) activeCustomers);
        snapshot.setNewCustomers(0);
        snapshot.setChurnedCustomers((int) canceledSubs);
        snapshot.setGrossChurnPercent(stats.getChurnRate());
        snapshot.setNetChurnPercent(stats.getChurnRate());
        snapshot.setTotalRevenueMinor(stats.getTotalCollectedMinor());
        snapshot.setTotalRefundsMinor(0L);

        snapshotRepository.save(snapshot);
    }

    private SubscriptionFinanceDTO mapToSubscriptionFinanceDTO(Subscription s) {
        long priceMinor = getMinorPriceForSubscription(s);
        long monthly = s.getPlan().getBillingPeriod() == com.infy.billing.enums.BillingPeriod.MONTHLY ? priceMinor
                : (priceMinor / 12);
        long annual = monthly * 12;

        return SubscriptionFinanceDTO.builder()
                .subscriptionId(s.getId())
                .customerName(s.getCustomer().getUser().getFullName())
                .customerEmail(s.getCustomer().getUser().getEmail())
                .planName(s.getPlan().getName())
                .billingPeriod(s.getPlan().getBillingPeriod().name())
                .monthlyValueMinor(monthly)
                .annualValueMinor(annual)
                .currency(s.getCurrency())
                .status(s.getStatus().name())
                .startDate(s.getStartDate().toString())
                .build();
    }

    private InvoiceDTO mapToInvoiceDTO(Invoice invoice) {
        InvoiceDTO dto = new InvoiceDTO();
        dto.setInvoiceId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setSubscriptionId(invoice.getSubscription().getId());
        dto.setStatus(invoice.getStatus());
        dto.setBillingReason(invoice.getBillingReason());
        dto.setIssueDate(invoice.getIssueDate().toString());
        dto.setDueDate(invoice.getDueDate() != null ? invoice.getDueDate().toString() : null);
        dto.setSubtotalMinor(invoice.getSubtotalMinor());
        dto.setTaxMinor(invoice.getTaxMinor());
        dto.setDiscountMinor(invoice.getDiscountMinor());
        dto.setTotalMinor(invoice.getTotalMinor());
        dto.setBalanceMinor(invoice.getBalanceMinor());
        dto.setCurrency(invoice.getCurrency());

        List<InvoiceLineItem> items = invoiceLineItemRepository.findByInvoice_Id(invoice.getId());
        dto.setLineItems(items.stream().map(item -> {
            InvoiceLineItemDTO idto = new InvoiceLineItemDTO();
            idto.setLineItemId(item.getId());
            idto.setDescription(item.getDescription());
            idto.setLineType(item.getLineType());
            idto.setQuantity(item.getQuantity());
            idto.setUnitPriceMinor(item.getUnitPriceMinor());
            idto.setAmountMinor(item.getAmountMinor());
            idto.setPeriodStart(item.getPeriodStart() != null ? item.getPeriodStart().toString() : null);
            idto.setPeriodEnd(item.getPeriodEnd() != null ? item.getPeriodEnd().toString() : null);
            return idto;
        }).collect(Collectors.toList()));

        return dto;
    }
}
