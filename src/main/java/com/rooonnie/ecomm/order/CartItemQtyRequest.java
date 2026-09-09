package com.rooonnie.ecomm.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemQtyRequest(@NotNull @Min(0) Integer qty) {
}
