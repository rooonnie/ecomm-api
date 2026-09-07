package com.rooonnie.ecomm.auth;

import com.rooonnie.ecomm.customer.CustomerResponse;

public record AuthResponse(String token, String tokenType, CustomerResponse user) {
}
