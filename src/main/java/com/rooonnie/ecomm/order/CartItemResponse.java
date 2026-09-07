package com.rooonnie.ecomm.order;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long skuId,
        String skuCode,
        String packagingCode,
        Integer qty,
        Integer moq,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
