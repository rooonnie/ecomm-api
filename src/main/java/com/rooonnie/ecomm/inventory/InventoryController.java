package com.rooonnie.ecomm.inventory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/skus/{skuId}/inventory")
@Tag(name = "Inventory", description = "Stock per SKU, not per product")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    @Operation(summary = "Get stock for a SKU")
    public InventoryResponse get(@PathVariable Long skuId) {
        return inventoryService.getBySkuId(skuId);
    }

    @PutMapping
    @Operation(summary = "Set on-hand qty for a SKU")
    public InventoryResponse update(
            @PathVariable Long skuId,
            @Valid @RequestBody InventoryUpdateRequest request
    ) {
        return inventoryService.updateOnHand(skuId, request);
    }
}
