package com.rooonnie.ecomm.catalog;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SkuRequest(
        @NotNull Long productId,
        @NotNull Long packagingTypeId,
        @NotBlank @Size(max = 80) String skuCode,
        @NotNull @Min(1) Integer qtyPerPack,
        @Size(max = 20) String reelSize,
        @NotNull @Min(1) Integer moq,
        SkuStatus status
) {
}
