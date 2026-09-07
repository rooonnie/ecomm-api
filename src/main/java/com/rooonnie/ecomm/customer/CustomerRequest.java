package com.rooonnie.ecomm.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank @Email @Size(max = 160) String email,
        @NotBlank @Size(max = 120) String name
) {
}
