package com.rooonnie.ecomm.customer;

import com.rooonnie.ecomm.auth.AuthGuard;
import com.rooonnie.ecomm.common.ConflictException;
import com.rooonnie.ecomm.common.ResourceNotFoundException;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthGuard authGuard;

    public CustomerService(
            CustomerRepository customerRepository,
            AddressRepository addressRepository,
            PasswordEncoder passwordEncoder,
            AuthGuard authGuard
    ) {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
        this.passwordEncoder = passwordEncoder;
        this.authGuard = authGuard;
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        String email = request.email().trim().toLowerCase();
        if (customerRepository.existsByEmail(email)) {
            throw new ConflictException("Email already exists: " + email);
        }
        Customer saved = customerRepository.save(
                new Customer(email, request.name().trim(), passwordEncoder.encode(request.password()))
        );
        return CustomerResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(Long id) {
        authGuard.requireUser(id);
        return CustomerResponse.from(getById(id));
    }

    @Transactional
    public AddressResponse addAddress(Long userId, AddressRequest request) {
        authGuard.requireUser(userId);
        Customer customer = getById(userId);
        Address address = new Address();
        address.setCustomer(customer);
        address.setType("SHIPPING");
        address.setLine1(request.line1().trim());
        address.setCity(request.city().trim());
        address.setCountry(request.country().trim());
        address.setPostal(request.postal().trim());
        return AddressResponse.from(addressRepository.save(address));
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> listAddresses(Long userId) {
        authGuard.requireUser(userId);
        getById(userId);
        return addressRepository.findByCustomerId(userId).stream().map(AddressResponse::from).toList();
    }

    public Customer getById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    public Address getAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + addressId));
        if (!address.getCustomer().getId().equals(userId)) {
            throw new ResourceNotFoundException("Address not found: " + addressId);
        }
        return address;
    }
}
