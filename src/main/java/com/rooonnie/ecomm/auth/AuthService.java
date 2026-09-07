package com.rooonnie.ecomm.auth;

import com.rooonnie.ecomm.common.UnauthorizedException;
import com.rooonnie.ecomm.customer.Customer;
import com.rooonnie.ecomm.customer.CustomerRepository;
import com.rooonnie.ecomm.customer.CustomerRequest;
import com.rooonnie.ecomm.customer.CustomerResponse;
import com.rooonnie.ecomm.customer.CustomerService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final CustomerService customerService;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            CustomerService customerService,
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.customerService = customerService;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(CustomerRequest request) {
        CustomerResponse created = customerService.create(request);
        Customer customer = customerRepository.findById(created.id()).orElseThrow();
        return new AuthResponse(jwtService.issue(customer), "Bearer", created);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        if (customer.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), customer.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }
        return new AuthResponse(jwtService.issue(customer), "Bearer", CustomerResponse.from(customer));
    }
}
