package com.rooonnie.ecomm.order;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(Long cartId, Long userId, List<CartItemResponse> items, BigDecimal subtotal) {
}
