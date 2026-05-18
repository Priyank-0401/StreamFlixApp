package com.infy.billing.controller;

import com.infy.billing.dto.customer.SubscriptionDTO;
import com.infy.billing.dto.support.CustomerDetailResponse;
import com.infy.billing.dto.support.CustomerSearchResponse;
import com.infy.billing.dto.customer.CustomerProfileDTO;
import com.infy.billing.enums.Status;
import com.infy.billing.enums.DunningStatus;
import com.infy.billing.entity.AuditLog;
import com.infy.billing.entity.BillingJob;
import com.infy.billing.entity.DunningRetryLog;
import com.infy.billing.service.SupportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SupportController.class)
public class SupportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SupportService supportService;

    private UsernamePasswordAuthenticationToken supportAuth;

    @BeforeEach
    void setUp() {
        supportAuth = new UsernamePasswordAuthenticationToken(
                "support@test.com", null, List.of(new SimpleGrantedAuthority("ROLE_SUPPORT")));
    }

    @Test
    void testSearchCustomers() throws Exception {
        CustomerSearchResponse response = new CustomerSearchResponse();
        response.setEmail("test@test.com");
        when(supportService.searchCustomers(anyString())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/support/customers")
                        .param("query", "test")
                        .with(authentication(supportAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("test@test.com"));

        verify(supportService, times(1)).searchCustomers("test");
    }

    @Test
    void testGetCustomerDetails() throws Exception {
        CustomerDetailResponse response = new CustomerDetailResponse();
        CustomerProfileDTO profile = new CustomerProfileDTO();
        profile.setEmail("test@test.com");
        response.setCustomerProfile(profile);
        when(supportService.getCustomerDetails(anyLong())).thenReturn(response);

        mockMvc.perform(get("/api/support/customers/1")
                        .with(authentication(supportAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerProfile.email").value("test@test.com"));

        verify(supportService, times(1)).getCustomerDetails(1L);
    }

    @Test
    void testGetRecentAuditLogs() throws Exception {
        AuditLog log = new AuditLog();
        log.setAction("SUBSCRIPTION_CREATED");
        when(supportService.getRecentAuditLogs()).thenReturn(List.of(log));

        mockMvc.perform(get("/api/support/audit-logs")
                        .with(authentication(supportAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("SUBSCRIPTION_CREATED"));

        verify(supportService, times(1)).getRecentAuditLogs();
    }

    @Test
    void testGetBillingJobs() throws Exception {
        BillingJob job = new BillingJob();
        job.setStatus(BillingJob.Status.COMPLETED);
        when(supportService.getBillingJobs()).thenReturn(List.of(job));

        mockMvc.perform(get("/api/support/billing-jobs")
                        .with(authentication(supportAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));

        verify(supportService, times(1)).getBillingJobs();
    }

    @Test
    void testGetDunningLogs() throws Exception {
        DunningRetryLog log = new DunningRetryLog();
        log.setStatus(DunningStatus.SCHEDULED);
        when(supportService.getDunningLogs()).thenReturn(List.of(log));

        mockMvc.perform(get("/api/support/dunning-logs")
                        .with(authentication(supportAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"));

        verify(supportService, times(1)).getDunningLogs();
    }

    @Test
    void testGetPastDueSubscriptions() throws Exception {
        SubscriptionDTO dto = new SubscriptionDTO();
        dto.setStatus(Status.PAST_DUE);
        when(supportService.getPastDueSubscriptions()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/support/subscriptions/past-due")
                        .with(authentication(supportAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PAST_DUE"));

        verify(supportService, times(1)).getPastDueSubscriptions();
    }
}
