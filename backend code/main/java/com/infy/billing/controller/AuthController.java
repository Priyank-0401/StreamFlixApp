package com.infy.billing.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.infy.billing.dto.auth.CustomerRegisterRequest;
import com.infy.billing.dto.auth.LoginRequest;
import com.infy.billing.dto.auth.UserResponse;
import com.infy.billing.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
//@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequiredArgsConstructor
public class AuthController {

   private final AuthService authService;

   // --- CUSTOMER PORTAL ---
   @PostMapping("/customer/register")
   public ResponseEntity<UserResponse> registerCustomer(@Valid @RequestBody CustomerRegisterRequest request, HttpServletRequest httpRequest) {
       UserResponse response = authService.registerCustomer(request);
       // Explicitly create session
       HttpSession session = httpRequest.getSession(true);
       System.out.println("DEBUG: Customer register - Session created: " + session.getId());
       return ResponseEntity.ok(response);
   }

   @PostMapping("/customer/login")
   public ResponseEntity<UserResponse> loginCustomer(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
       UserResponse response = authService.loginCustomer(request);
       // Explicitly create session
       HttpSession session = httpRequest.getSession(true);
       System.out.println("DEBUG: Customer login - Session created: " + session.getId());
       return ResponseEntity.ok(response);
   }

   // --- MANAGEMENT PORTAL ---
   @PostMapping("/manager/login")
   public ResponseEntity<UserResponse> loginManager(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
       UserResponse response = authService.loginManager(request);
       // Explicitly create session
       HttpSession session = httpRequest.getSession(true);
       System.out.println("DEBUG: Manager login - Session created: " + session.getId());
       return ResponseEntity.ok(response);
   }

   // --- SESSION CHECK ENDPOINT ---
   // React calls this on page refresh to see if the JSESSIONID cookie is still valid
   @GetMapping("/auth/me")
   public ResponseEntity<?> getMe(Authentication authentication, HttpServletRequest request) {
       HttpSession session = request.getSession(false);
       System.out.println("DEBUG: /auth/me - Session: " + (session != null ? session.getId() : "null"));
       System.out.println("DEBUG: /auth/me - Authentication: " + (authentication != null ? authentication.getName() : "null"));
       
       if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
           System.out.println("DEBUG: /auth/me - Not authenticated, returning 401");
           return ResponseEntity.status(401).body("Not authenticated");
       }
       System.out.println("DEBUG: /auth/me - Returning user for: " + authentication.getName());
       return ResponseEntity.ok(authService.getAuthenticatedUserResponse(authentication.getName()));
   }
}

