package com.infy.billing.service;

import com.infy.billing.repository.CustomerRepository;
import com.infy.billing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerDataService {
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    public CustomerRepository getCustomerRepository() {
        return customerRepository;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }
}
