package com.rooonnie.ecomm.inventory;

import java.math.BigDecimal;

public record PriceBreakResponse(Long id, Long skuId, Integer minQty, BigDecimal unitPrice) {

    public static PriceBreakResponse from(PriceBreak priceBreak) {
        return new PriceBreakResponse(
                priceBreak.getId(),
                priceBreak.getSku().getId(),
                priceBreak.getMinQty(),
                priceBreak.getUnitPrice()
        );
    }
}
