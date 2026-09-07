package com.rooonnie.ecomm.inventory;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PriceBreakRequest(
        @NotNull @Min(1) Integer minQty,
        @NotNull @DecimalMin("0.0001") BigDecimal unitPrice
) {
}
