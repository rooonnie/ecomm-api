package com.rooonnie.ecomm.customer;

public record AddressResponse(Long id, Long userId, String type, String line1, String city, String country, String postal) {

    public static AddressResponse from(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getCustomer().getId(),
                address.getType(),
                address.getLine1(),
                address.getCity(),
                address.getCountry(),
                address.getPostal()
        );
    }
}
