package com.rooonnie.ecomm.auth;

import com.rooonnie.ecomm.customer.Customer;
import com.rooonnie.ecomm.customer.CustomerRepository;
import com.rooonnie.ecomm.customer.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminUserSeeder implements ApplicationRunner {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;
    private final String name;

    public AdminUserSeeder(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            @Value("${ecomm.admin.email}") String email,
            @Value("${ecomm.admin.password}") String password,
            @Value("${ecomm.admin.name}") String name
    ) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
        this.name = name;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String normalized = email.trim().toLowerCase();
        if (customerRepository.existsByEmail(normalized)) {
            return;
        }
        customerRepository.save(new Customer(
                normalized,
                name.trim(),
                passwordEncoder.encode(password),
                UserRole.ADMIN
        ));
    }
}
