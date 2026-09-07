package com.rooonnie.ecomm.order;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long skuId,
        String skuCode,
        String mpn,
        String packagingCode,
        String packagingSnapshot,
        Integer qty,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
