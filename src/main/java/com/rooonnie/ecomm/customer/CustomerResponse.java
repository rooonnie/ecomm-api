package com.rooonnie.ecomm.customer;

public record CustomerResponse(Long id, String email, String name) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getEmail(), customer.getName());
    }
}
