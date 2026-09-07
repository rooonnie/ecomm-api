package com.rooonnie.ecomm.catalog;

import java.util.List;

public record ProductResponse(
        Long id,
        String mpn,
        Long manufacturerId,
        String manufacturerName,
        Long categoryId,
        String categoryName,
        String name,
        String description,
        String packageCase,
        ProductLifecycle lifecycle,
        List<ProductImageResponse> images
) {

    public static ProductResponse from(Product product, List<ProductImage> images) {
        return new ProductResponse(
                product.getId(),
                product.getMpn(),
                product.getManufacturer().getId(),
                product.getManufacturer().getName(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getName(),
                product.getDescription(),
                product.getPackageCase(),
                product.getLifecycle(),
                images.stream().map(ProductImageResponse::from).toList()
        );
    }
}
