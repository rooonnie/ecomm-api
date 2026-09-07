package com.rooonnie.ecomm.inventory;

import java.math.BigDecimal;

public record PriceQuoteResponse(
        Long skuId,
        String skuCode,
        Integer qty,
        Integer moq,
        Integer appliedMinQty,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
