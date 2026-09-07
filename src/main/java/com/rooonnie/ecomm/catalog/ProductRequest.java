package com.rooonnie.ecomm.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductRequest(
        @NotBlank @Size(max = 80) String mpn,
        @NotNull Long manufacturerId,
        @NotNull Long categoryId,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 1000) String description,
        @Size(max = 40) String packageCase,
        ProductLifecycle lifecycle
) {
}
