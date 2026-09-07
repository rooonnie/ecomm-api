package com.rooonnie.ecomm.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank @Size(max = 200) String line1,
        @NotBlank @Size(max = 80) String city,
        @NotBlank @Size(max = 80) String country,
        @NotBlank @Size(max = 20) String postal
) {
}
