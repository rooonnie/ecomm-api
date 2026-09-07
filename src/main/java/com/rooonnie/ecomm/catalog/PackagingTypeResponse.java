package com.rooonnie.ecomm.catalog;

public record PackagingTypeResponse(Long id, String code, String name) {

    public static PackagingTypeResponse from(PackagingType entity) {
        return new PackagingTypeResponse(entity.getId(), entity.getCode(), entity.getName());
    }
}
