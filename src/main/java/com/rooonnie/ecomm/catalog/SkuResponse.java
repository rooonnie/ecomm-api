package com.rooonnie.ecomm.catalog;

public record SkuResponse(
        Long id,
        Long productId,
        String mpn,
        Long packagingTypeId,
        String packagingCode,
        String skuCode,
        Integer qtyPerPack,
        String reelSize,
        Integer moq,
        SkuStatus status
) {

    public static SkuResponse from(Sku sku) {
        return new SkuResponse(
                sku.getId(),
                sku.getProduct().getId(),
                sku.getProduct().getMpn(),
                sku.getPackagingType().getId(),
                sku.getPackagingType().getCode(),
                sku.getSkuCode(),
                sku.getQtyPerPack(),
                sku.getReelSize(),
                sku.getMoq(),
                sku.getStatus()
        );
    }
}
