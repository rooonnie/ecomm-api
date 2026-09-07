package com.rooonnie.ecomm.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PackagingTypeRequest(
        @NotBlank @Size(max = 40) String code,
        @NotBlank @Size(max = 80) String name
) {
}
