package com.rooonnie.ecomm.inventory;

public record InventoryResponse(
        Long id,
        Long skuId,
        String skuCode,
        Integer qtyOnHand,
        Integer qtyReserved,
        Integer qtyAvailable
) {

    public static InventoryResponse from(Inventory inventory) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getSku().getId(),
                inventory.getSku().getSkuCode(),
                inventory.getQtyOnHand(),
                inventory.getQtyReserved(),
                inventory.available()
        );
    }
}
