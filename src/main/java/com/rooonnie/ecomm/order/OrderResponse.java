package com.rooonnie.ecomm.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNo,
        Long userId,
        OrderStatus status,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal total,
        String shippingLine1,
        String shippingCity,
        String shippingCountry,
        String shippingPostal,
        Instant createdAt,
        List<OrderItemResponse> items,
        PaymentResponse payment
) {
}
