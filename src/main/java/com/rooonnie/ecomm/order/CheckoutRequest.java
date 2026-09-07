package com.rooonnie.ecomm.order;

import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(@NotNull Long addressId) {
}
