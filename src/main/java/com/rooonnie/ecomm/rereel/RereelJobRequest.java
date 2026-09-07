package com.rooonnie.ecomm.rereel;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RereelJobRequest(
        @NotNull Long sourceSkuId,
        @NotNull Long targetPackagingTypeId,
        @NotNull @Min(1) Integer qty,
        @Size(max = 20) String reelSize
) {
}
