package com.rooonnie.ecomm.auth;

import com.rooonnie.ecomm.customer.UserRole;

public record AuthPrincipal(Long userId, String email, UserRole role) {
}
