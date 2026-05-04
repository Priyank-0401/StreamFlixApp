package com.infy.billing.service;

import com.infy.billing.dto.customer.*;
import com.infy.billing.entity.*;
import com.infy.billing.enums.Status;
import com.infy.billing.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerBillingServiceImpl implements CustomerBillingService {

   private final InvoiceRepository invoiceRepository;
   private final InvoiceLineItemRepository invoiceLineItemRepository;
   private final PaymentRepository paymentRepository;
   private final CreditNoteRepository creditNoteRepository;
   private final CouponRepository couponRepository;
   private final SubscriptionRepository subscriptionRepository;
   private final CustomerRepository customerRepository;
   private final UserRepository userRepository;

   public List<InvoiceDTO> getInvoices(String email, String status, String from, String to) {
       Customer customer = getCustomerByEmail(email);
       List<Invoice> invoices;
       
       if (status != null) {
           invoices = invoiceRepository.findByCustomer_IdAndStatus(customer.getId(), status);
       } else {
           invoices = invoiceRepository.findByCustomer_IdOrderByIssueDateDesc(customer.getId());
       }
       
       return invoices.stream().map(this::mapToInvoiceDTO).collect(Collectors.toList());
   }

   public InvoiceDTO getInvoiceDetail(String email, Long invoiceId) {
       Customer customer = getCustomerByEmail(email);
       Invoice invoice = invoiceRepository.findById(invoiceId)
               .orElseThrow(() -> new RuntimeException("Invoice not found"));
       
       if (!invoice.getCustomer().getId().equals(customer.getId())) {
           throw new RuntimeException("Unauthorized");
       }
       
       return mapToInvoiceDTO(invoice);
   }

   public byte[] generateInvoicePdf(String email, Long invoiceId) {
       return new byte[0];
   }

   public List<PaymentDTO> getPayments(String email) {
       Customer customer = getCustomerByEmail(email);
       return paymentRepository.findById(customer.getId()).stream()
               .map(this::mapToPaymentDTO)
               .collect(Collectors.toList());
   }

   public List<CreditNoteDTO> getCreditNotes(String email) {
       Customer customer = getCustomerByEmail(email);
       return creditNoteRepository.findById(customer.getId()).stream()
               .map(this::mapToCreditNoteDTO)
               .collect(Collectors.toList());
   }

   @Transactional
   public CouponDTO applyCoupon(String email, String code) {
       Customer customer = getCustomerByEmail(email);
       Coupon coupon = couponRepository.findByCodeAndStatus(code, Status.ACTIVE)
               .orElseThrow(() -> new RuntimeException("Invalid or expired coupon"));
       
       subscriptionRepository.findByCustomer_IdAndStatusIn(
               customer.getId(), List.of(Status.ACTIVE, Status.TRIALING)).stream()
               .findFirst()
               .orElseThrow(() -> new RuntimeException("No active subscription"));
       
       return mapToCouponDTO(coupon);
   }

   private Customer getCustomerByEmail(String email) {
       User user = userRepository.findByEmail(email)
               .orElseThrow(() -> new RuntimeException("User not found"));
       return customerRepository.findByUser_Id(user.getId())
               .orElseThrow(() -> new RuntimeException("Customer not found"));
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
       dto.setLineItems(items.stream().map(this::mapToLineItemDTO).collect(Collectors.toList()));
       
       return dto;
   }

   private InvoiceLineItemDTO mapToLineItemDTO(InvoiceLineItem item) {
       InvoiceLineItemDTO dto = new InvoiceLineItemDTO();
       dto.setLineItemId(item.getId());
       dto.setDescription(item.getDescription());
       dto.setLineType(item.getLineType());
       dto.setQuantity(item.getQuantity());
       dto.setUnitPriceMinor(item.getUnitPriceMinor());
       dto.setAmountMinor(item.getAmountMinor());
       dto.setPeriodStart(item.getPeriodStart() != null ? item.getPeriodStart().toString() : null);
       dto.setPeriodEnd(item.getPeriodEnd() != null ? item.getPeriodEnd().toString() : null);
       return dto;
   }

   private PaymentDTO mapToPaymentDTO(Payment payment) {
       PaymentDTO dto = new PaymentDTO();
       dto.setPaymentId(payment.getId());
       dto.setInvoiceId(payment.getInvoice().getId());
       Invoice inv = invoiceRepository.findById(payment.getInvoice().getId()).orElse(null);
       dto.setInvoiceNumber(inv != null ? inv.getInvoiceNumber() : "N/A");
       dto.setAmountMinor(payment.getAmountMinor());
       dto.setCurrency(payment.getCurrency());
       dto.setStatus(payment.getStatus());
       dto.setAttemptNo(payment.getAttemptNo());
       dto.setFailureReason(payment.getFailureReason());
       dto.setCreatedAt(payment.getCreatedAt().toString());
       return dto;
   }

   private CreditNoteDTO mapToCreditNoteDTO(CreditNote creditNote) {
       CreditNoteDTO dto = new CreditNoteDTO();
       dto.setCreditNoteId(creditNote.getId());
       dto.setCreditNoteNumber(creditNote.getCreditNoteNumber());
       dto.setInvoiceId(creditNote.getInvoice().getId());
       Invoice inv = invoiceRepository.findById(creditNote.getInvoice().getId()).orElse(null);
       dto.setInvoiceNumber(inv != null ? inv.getInvoiceNumber() : "N/A");
       dto.setReason(creditNote.getReason());
       dto.setAmountMinor(creditNote.getAmountMinor());
       dto.setStatus(creditNote.getStatus());
       dto.setCreatedAt(creditNote.getCreatedAt().toString());
       return dto;
   }

   private CouponDTO mapToCouponDTO(Coupon coupon) {
       CouponDTO dto = new CouponDTO();
       dto.setCouponId(coupon.getId());
       dto.setCode(coupon.getCode());
       dto.setName(coupon.getName());
       dto.setType(coupon.getType());
       dto.setAmount(coupon.getAmount());
       dto.setCurrency(coupon.getCurrency());
       dto.setDuration(coupon.getDuration());
       dto.setDurationInMonths(coupon.getDurationInMonths());
       dto.setValidFrom(coupon.getValidFrom().toString());
       dto.setValidTo(coupon.getValidTo() != null ? coupon.getValidTo().toString() : null);
       dto.setStatus(coupon.getStatus());
       return dto;
   }
}
