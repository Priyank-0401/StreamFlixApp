package com.infy.billing.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.infy.billing.dto.auth.CustomerRegisterRequest;
import com.infy.billing.dto.auth.LoginRequest;
import com.infy.billing.dto.auth.UserResponse;
import com.infy.billing.entity.User;
import com.infy.billing.enums.Status;
import com.infy.billing.enums.UserRole;
import com.infy.billing.exception.CustomException;
import com.infy.billing.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@test.com")
                .fullName("Test User")
                .role(UserRole.CUSTOMER)
                .status(Status.ACTIVE)
                .build();
        
        authentication = mock(Authentication.class);
    }

    @Test
    void testRegisterCustomer_Success() {
        CustomerRegisterRequest request = new CustomerRegisterRequest();
        request.setEmail("new@test.com");
        request.setFullName("New User");
        request.setPassword("password");

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded_password");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.of(user));

        UserResponse response = authService.registerCustomer(request);

        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterCustomer_EmailConflict() {
        CustomerRegisterRequest request = new CustomerRegisterRequest();
        request.setEmail("test@test.com");

        when(userRepository.existsByEmail("test@test.com")).thenReturn(true);

        assertThrows(CustomException.class, () -> authService.registerCustomer(request));
    }

    @Test
    void testLoginCustomer_Success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        UserResponse response = authService.loginCustomer(request);

        assertNotNull(response);
    }

    @Test
    void testLoginCustomer_Forbidden_StaffPortal() {
        user.setRole(UserRole.ADMIN);
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        assertThrows(CustomException.class, () -> authService.loginCustomer(request));
    }

    @Test
    void testLoginManager_Success() {
        user.setRole(UserRole.ADMIN);
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@test.com");
        request.setPassword("password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));

        UserResponse response = authService.loginManager(request);

        assertNotNull(response);
    }

    @Test
    void testLoginManager_Forbidden_Customer() {
        user.setRole(UserRole.CUSTOMER);
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        assertThrows(CustomException.class, () -> authService.loginManager(request));
    }

    @Test
    void testGetAuthenticatedUserResponse() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        UserResponse response = authService.getAuthenticatedUserResponse("test@test.com");

        assertNotNull(response);
        assertEquals("test@test.com", response.getEmail());
    }
}
