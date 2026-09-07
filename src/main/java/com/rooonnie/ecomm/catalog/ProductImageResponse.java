package com.rooonnie.ecomm.catalog;

public record ProductImageResponse(
        Long id,
        Long productId,
        String url,
        String altText,
        Integer sortOrder
) {

    public static ProductImageResponse from(ProductImage image) {
        return new ProductImageResponse(
                image.getId(),
                image.getProduct().getId(),
                image.getUrl(),
                image.getAltText(),
                image.getSortOrder()
        );
    }
}
