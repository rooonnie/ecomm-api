package com.rooonnie.ecomm.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NamedSlugRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 120) String slug
) {
}
