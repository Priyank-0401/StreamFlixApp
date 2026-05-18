package com.infy.billing.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.infy.billing.entity.Invoice;
import com.infy.billing.entity.InvoiceLineItem;
import com.infy.billing.entity.Customer;
import com.infy.billing.entity.User;
import com.infy.billing.entity.Subscription;
import com.infy.billing.entity.Plan;
import com.infy.billing.enums.Status;
import com.infy.billing.enums.BillingReason;
import com.infy.billing.enums.BillingPeriod;
import com.infy.billing.repository.InvoiceLineItemRepository;
import com.infy.billing.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
public class InvoicePdfServiceImplTest {

    @Mock private InvoiceLineItemRepository lineItemRepository;
    @Mock private PaymentRepository paymentRepository;

    @InjectMocks
    private InvoicePdfServiceImpl invoicePdfService;

    private Invoice invoice;
    private Customer customer;
    private User user;
    private Subscription subscription;
    private Plan plan;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).fullName("Test User").email("test@test.com").build();
        customer = Customer.builder().id(1L).user(user).build();
        plan = Plan.builder().id(1L).name("Basic").billingPeriod(BillingPeriod.MONTHLY).build();
        subscription = Subscription.builder().id(1L).plan(plan).currentPeriodStart(LocalDate.now()).currentPeriodEnd(LocalDate.now().plusMonths(1)).build();
        invoice = Invoice.builder()
                .id(1L)
                .invoiceNumber("INV-1")
                .customer(customer)
                .subscription(subscription)
                .status(Status.PAID)
                .billingReason(BillingReason.SUBSCRIPTION_CYCLE)
                .issueDate(LocalDate.now())
                .currency("USD")
                .subtotalMinor(1000L)
                .totalMinor(1000L)
                .build();
    }

    @Test
    void testGeneratePdf() {
        InvoiceLineItem item = new InvoiceLineItem();
        item.setId(1L);
        item.setDescription("Item 1");
        item.setQuantity(1);
        item.setUnitPriceMinor(1000L);
        item.setAmountMinor(1000L);
        item.setLineType(InvoiceLineItem.LineType.PLAN);

        when(lineItemRepository.findByInvoice_Id(1L)).thenReturn(Arrays.asList(item));
        when(paymentRepository.findByInvoice_Id(1L)).thenReturn(Arrays.asList());

        byte[] pdf = invoicePdfService.generatePdf(invoice);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void testGeneratePdf_OpenStatus() {
        invoice.setStatus(Status.OPEN);
        when(lineItemRepository.findByInvoice_Id(1L)).thenReturn(Arrays.asList());

        byte[] pdf = invoicePdfService.generatePdf(invoice);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }
}
