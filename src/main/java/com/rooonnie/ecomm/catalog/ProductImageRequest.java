package com.rooonnie.ecomm.catalog;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductImageRequest(
        @NotBlank @Size(max = 500) String url,
        @Size(max = 200) String altText,
        @Min(0) Integer sortOrder
) {
}
