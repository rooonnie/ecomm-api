package com.rooonnie.ecomm.customer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @Operation(summary = "Create user")
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse created = customerService.create(request);
        return ResponseEntity.created(URI.create("/api/users/" + created.id())).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user")
    public CustomerResponse get(@PathVariable Long id) {
        return customerService.findById(id);
    }

    @GetMapping("/{id}/addresses")
    @Operation(summary = "List user addresses")
    public List<AddressResponse> listAddresses(@PathVariable Long id) {
        return customerService.listAddresses(id);
    }

    @PostMapping("/{id}/addresses")
    @Operation(summary = "Add shipping address")
    public ResponseEntity<AddressResponse> addAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request
    ) {
        AddressResponse created = customerService.addAddress(id, request);
        return ResponseEntity.created(URI.create("/api/users/" + id + "/addresses/" + created.id())).body(created);
    }
}
