package com.infy.billing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infy.billing.dto.customer.SubscriptionDTO;
import com.infy.billing.dto.customer.UsageRecordDTO;
import com.infy.billing.entity.User;
import com.infy.billing.enums.UserRole;
import com.infy.billing.enums.Status;
import com.infy.billing.request.CreateSubscriptionRequest;
import com.infy.billing.request.PauseSubscriptionRequest;
import com.infy.billing.request.UpgradeSubscriptionRequest;
import com.infy.billing.service.CustomerSubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerSubscriptionController.class)
public class CustomerSubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerSubscriptionService subscriptionService;

    private User authUser;
    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setUp() {
        authUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .fullName("Test Customer")
                .role(UserRole.CUSTOMER)
                .status(Status.ACTIVE)
                .build();
        auth = new UsernamePasswordAuthenticationToken(authUser, null, List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
    }

    @Test
    void testGetSubscription() throws Exception {
        SubscriptionDTO dto = new SubscriptionDTO();
        dto.setSubscriptionId(100L);
        
        when(subscriptionService.getCurrentSubscription(anyString())).thenReturn(dto);

        mockMvc.perform(get("/api/customer/subscription").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptionId").value(100));

        verify(subscriptionService, times(1)).getCurrentSubscription("test@test.com");
    }

    @Test
    void testGetSubscription_NotFound() throws Exception {
        when(subscriptionService.getCurrentSubscription(anyString())).thenReturn(null);

        mockMvc.perform(get("/api/customer/subscription").with(authentication(auth)))
                .andExpect(status().isNotFound());

        verify(subscriptionService, times(1)).getCurrentSubscription("test@test.com");
    }

    @Test
    void testCreateSubscription() throws Exception {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setPlanId(1L);
        
        SubscriptionDTO dto = new SubscriptionDTO();
        dto.setSubscriptionId(100L);
        
        when(subscriptionService.createSubscription(anyString(), any())).thenReturn(dto);

        mockMvc.perform(post("/api/customer/subscription")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptionId").value(100));

        verify(subscriptionService, times(1)).createSubscription(eq("test@test.com"), any(CreateSubscriptionRequest.class));
    }

    @Test
    void testUpgradeSubscription() throws Exception {
        UpgradeSubscriptionRequest request = new UpgradeSubscriptionRequest();
        request.setPlanId(2L);
        
        SubscriptionDTO dto = new SubscriptionDTO();
        dto.setSubscriptionId(100L);
        
        when(subscriptionService.upgradeSubscription(anyString(), any())).thenReturn(dto);

        mockMvc.perform(put("/api/customer/subscription/upgrade")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptionId").value(100));

        verify(subscriptionService, times(1)).upgradeSubscription(eq("test@test.com"), any(UpgradeSubscriptionRequest.class));
    }

    @Test
    void testCancelSubscription() throws Exception {
        doNothing().when(subscriptionService).cancelSubscription(anyString(), anyBoolean());

        mockMvc.perform(delete("/api/customer/subscription")
                        .param("atPeriodEnd", "false")
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(subscriptionService, times(1)).cancelSubscription("test@test.com", false);
    }

    @Test
    void testPauseSubscription() throws Exception {
        PauseSubscriptionRequest request = new PauseSubscriptionRequest();
        request.setPausedTo("2025-01-01");
        
        SubscriptionDTO dto = new SubscriptionDTO();
        dto.setSubscriptionId(100L);
        
        when(subscriptionService.pauseSubscription(anyString(), any())).thenReturn(dto);

        mockMvc.perform(put("/api/customer/subscription/pause")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptionId").value(100));

        verify(subscriptionService, times(1)).pauseSubscription(eq("test@test.com"), any(PauseSubscriptionRequest.class));
    }

    @Test
    void testResumeSubscription() throws Exception {
        SubscriptionDTO dto = new SubscriptionDTO();
        dto.setSubscriptionId(100L);
        
        when(subscriptionService.resumeSubscription(anyString())).thenReturn(dto);

        mockMvc.perform(put("/api/customer/subscription/resume")
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptionId").value(100));

        verify(subscriptionService, times(1)).resumeSubscription("test@test.com");
    }

    @Test
    void testAddAddon() throws Exception {
        SubscriptionDTO dto = new SubscriptionDTO();
        dto.setSubscriptionId(100L);
        
        when(subscriptionService.addAddOn(anyString(), anyLong())).thenReturn(dto);

        mockMvc.perform(post("/api/customer/addons/1")
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptionId").value(100));

        verify(subscriptionService, times(1)).addAddOn("test@test.com", 1L);
    }

    @Test
    void testRemoveAddon() throws Exception {
        SubscriptionDTO dto = new SubscriptionDTO();
        dto.setSubscriptionId(100L);
        
        when(subscriptionService.removeAddOn(anyString(), anyLong())).thenReturn(dto);

        mockMvc.perform(delete("/api/customer/addons/1")
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptionId").value(100));

        verify(subscriptionService, times(1)).removeAddOn("test@test.com", 1L);
    }

    @Test
    void testGetMeteredUsage() throws Exception {
        UsageRecordDTO usage = new UsageRecordDTO();
        usage.setUsageId(10L);
        
        when(subscriptionService.getMeteredUsage(anyString(), any(), any())).thenReturn(List.of(usage));

        mockMvc.perform(get("/api/customer/usage")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usageId").value(10));

        verify(subscriptionService, times(1)).getMeteredUsage(eq("test@test.com"), any(), any());
    }
}
