package com.infy.billing.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.infy.billing.entity.AuditLog;
import com.infy.billing.repository.AuditLogRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class AuditLoggingServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    private AuditLoggingServiceImpl auditLoggingService;

    @BeforeEach
    void setUp() {
        auditLoggingService = new AuditLoggingServiceImpl(auditLogRepository);
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private void setAuthenticatedUser(String username, String role) {
        UserDetails userDetails = new User(username, "password",
                List.of(new SimpleGrantedAuthority(role)));
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void setAuthenticatedUserNoAuthorities(String username) {
        UserDetails userDetails = new User(username, "password", Collections.emptyList());
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void setRequestContext(String ip, String requestId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(ip);
        if (requestId != null) {
            request.addHeader("X-Request-ID", requestId);
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    // ─── Actor resolution ────────────────────────────────────────────────────

    @Test
    void testLogAction_WithAuthenticatedUserDetailsAndRole() {
        setAuthenticatedUser("alice@test.com", "ROLE_ADMIN");
        setRequestContext("127.0.0.1", "req-001");

        auditLoggingService.logAction("CREATE", "Subscription", 1L, null, "new-value");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals("alice@test.com", saved.getActor());
        assertEquals("ROLE_ADMIN", saved.getActorRole());
        assertEquals("CREATE", saved.getAction());
        assertEquals("Subscription", saved.getEntityType());
        assertEquals(1L, saved.getEntityId());
    }

    @Test
    void testLogAction_WithAuthenticatedUserDetailsNoAuthorities() {
        setAuthenticatedUserNoAuthorities("alice@test.com");
        setRequestContext("127.0.0.1", "req-002");

        auditLoggingService.logAction("UPDATE", "Invoice", 2L, null, null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals("alice@test.com", saved.getActor());
        // actorRole should be null — no authorities provided
        assertNull(saved.getActorRole());
    }

    @Test
    void testLogAction_WithNonUserDetailsPrincipal() {
        // Principal is a plain String, not a UserDetails instance
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("plain-principal", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
        setRequestContext("10.0.0.1", null);

        auditLoggingService.logAction("DELETE", "Plan", 5L, null, null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        assertEquals("plain-principal", captor.getValue().getActor());
    }

    @Test
    void testLogAction_WithNullAuthentication_FallsBackToSystem() {
        // No auth set — SecurityContext returns null authentication
        SecurityContextHolder.clearContext();
        setRequestContext("192.168.1.1", "req-003");

        auditLoggingService.logAction("PAUSE", "Subscription", 3L, null, null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        assertEquals("SYSTEM", captor.getValue().getActor());
        assertEquals("SYSTEM", captor.getValue().getActorRole());
    }

    @Test
    void testLogAction_WithAnonymousUser_FallsBackToSystem() {
        // Simulates Spring Security anonymous user
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("anonymousUser", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
        setRequestContext("10.10.10.10", null);

        auditLoggingService.logAction("VIEW", "Customer", 4L, null, null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        assertEquals("SYSTEM", captor.getValue().getActor());
        assertEquals("SYSTEM", captor.getValue().getActorRole());
    }

    // ─── Request context resolution ──────────────────────────────────────────

    @Test
    void testLogAction_WithRequestContextAndExplicitRequestId() {
        setAuthenticatedUser("bob@test.com", "ROLE_USER");
        setRequestContext("172.16.0.1", "explicit-req-id");

        auditLoggingService.logAction("CANCEL", "Subscription", 10L, null, null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        assertEquals("172.16.0.1", captor.getValue().getIp());
        assertEquals("explicit-req-id", captor.getValue().getRequestId());
    }

    @Test
    void testLogAction_WithRequestContextMissingXRequestIdHeader_GeneratesUUID() {
        setAuthenticatedUser("bob@test.com", "ROLE_USER");
        setRequestContext("172.16.0.2", null); // no X-Request-ID header

        auditLoggingService.logAction("RESUME", "Subscription", 11L, null, null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        assertNotNull(captor.getValue().getRequestId());
        assertFalse(captor.getValue().getRequestId().isBlank());
    }

    @Test
    void testLogAction_WithNoRequestContext_GeneratesUUID() {
        setAuthenticatedUser("carol@test.com", "ROLE_USER");
        // No RequestContextHolder attributes set

        auditLoggingService.logAction("UPGRADE", "Subscription", 12L, null, null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        assertNotNull(captor.getValue().getRequestId());
        // IP should be null since there is no servlet request
        assertNull(captor.getValue().getIp());
    }

    // ─── Serialization ───────────────────────────────────────────────────────

    @Test
    void testLogAction_NullOldAndNewValue_SerializesAsNull() {
        setAuthenticatedUser("dave@test.com", "ROLE_USER");

        auditLoggingService.logAction("ACTION", "Entity", 1L, null, null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        assertNull(captor.getValue().getOldValue());
        assertNull(captor.getValue().getNewValue());
    }

    @Test
    void testLogAction_StringValuesPassedThrough() {
        setAuthenticatedUser("dave@test.com", "ROLE_USER");

        auditLoggingService.logAction("UPDATE", "Plan", 2L, "old-json-string", "new-json-string");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        // String values should be stored as-is, no extra serialization
        assertEquals("old-json-string", captor.getValue().getOldValue());
        assertEquals("new-json-string", captor.getValue().getNewValue());
    }

    @Test
    void testLogAction_SerializableObjectIsJsonSerialized() {
        setAuthenticatedUser("eve@test.com", "ROLE_USER");

        // A simple serializable object
        var payload = new java.util.HashMap<String, Object>();
        payload.put("planId", 42);
        payload.put("name", "Gold");

        auditLoggingService.logAction("CREATE", "Plan", 3L, null, payload);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        String newValue = captor.getValue().getNewValue();
        assertNotNull(newValue);
        assertTrue(newValue.contains("planId") || newValue.contains("42"));
    }

    @Test
    void testLogAction_UnserializableObject_StoresErrorFallback() {
        setAuthenticatedUser("frank@test.com", "ROLE_USER");

        // Anonymous class with a field that causes Jackson to fail (no properties + no default constructor)
        Object unserializable = new Object() {
            // Jackson will fail with FAIL_ON_EMPTY_BEANS disabled but we can force it
            // by having a getter that throws
            public String getValue() {
                throw new RuntimeException("cannot serialize");
            }
        };

        auditLoggingService.logAction("UPDATE", "Entity", 7L, unserializable, null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        // Should fall back to the error JSON string
        String oldValue = captor.getValue().getOldValue();
        assertNotNull(oldValue);
        assertTrue(oldValue.contains("error") || oldValue.contains("Unserializable"));
    }

    // ─── Core field mapping ───────────────────────────────────────────────────

    @Test
    void testLogAction_CorrectEntityFieldsArePersisted() {
        setAuthenticatedUser("grace@test.com", "ROLE_AGENT");
        setRequestContext("1.2.3.4", "req-xyz");

        auditLoggingService.logAction("DOWNGRADE", "Subscription", 99L, "old", "new");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog log = captor.getValue();
        assertEquals("DOWNGRADE", log.getAction());
        assertEquals("Subscription", log.getEntityType());
        assertEquals(99L, log.getEntityId());
        assertEquals("old", log.getOldValue());
        assertEquals("new", log.getNewValue());
        assertEquals("grace@test.com", log.getActor());
        assertEquals("ROLE_AGENT", log.getActorRole());
        assertEquals("1.2.3.4", log.getIp());
        assertEquals("req-xyz", log.getRequestId());
    }

    // ─── Exception resilience ────────────────────────────────────────────────

    @Test
    void testLogAction_RepositorySaveThrows_DoesNotPropagateException() {
        setAuthenticatedUser("henry@test.com", "ROLE_USER");
        doThrow(new RuntimeException("DB down")).when(auditLogRepository).save(any());

        // Should swallow the exception and not blow up the caller
        assertDoesNotThrow(() ->
                auditLoggingService.logAction("SAVE_FAIL", "Subscription", 1L, null, null)
        );
    }
}