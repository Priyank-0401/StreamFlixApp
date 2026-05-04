package com.infy.billing.service;

import com.infy.billing.dto.auth.CustomerRegisterRequest;
import com.infy.billing.dto.auth.LoginRequest;
import com.infy.billing.dto.auth.UserResponse;

public interface AuthService {
	   UserResponse registerCustomer(CustomerRegisterRequest request);
	   UserResponse loginCustomer(LoginRequest request);
	   UserResponse loginManager(LoginRequest request);
	   UserResponse getAuthenticatedUserResponse(String email);
	}
