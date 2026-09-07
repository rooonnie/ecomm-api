package com.rooonnie.ecomm.catalog;

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
        ProductLifecycle lifecycle
) {

    public static ProductResponse from(Product product) {
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
                product.getLifecycle()
        );
    }
}
