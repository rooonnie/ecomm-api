package com.rooonnie.ecomm.inventory;

import com.rooonnie.ecomm.catalog.Sku;
import com.rooonnie.ecomm.catalog.SkuService;
import com.rooonnie.ecomm.common.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final SkuService skuService;

    public InventoryService(InventoryRepository inventoryRepository, SkuService skuService) {
        this.inventoryRepository = inventoryRepository;
        this.skuService = skuService;
    }

    @Transactional(readOnly = true)
    public InventoryResponse getBySkuId(Long skuId) {
        return InventoryResponse.from(requireBySkuId(skuId));
    }

    @Transactional
    public InventoryResponse updateOnHand(Long skuId, InventoryUpdateRequest request) {
        Inventory inventory = requireBySkuId(skuId);
        if (request.qtyOnHand() < inventory.getQtyReserved()) {
            throw new BadRequestException(
                    "qtyOnHand cannot be below reserved qty " + inventory.getQtyReserved()
            );
        }
        inventory.setQtyOnHand(request.qtyOnHand());
        return InventoryResponse.from(inventoryRepository.save(inventory));
    }

    @Transactional
    public void reserve(Long skuId, int qty) {
        Inventory inventory = requireBySkuId(skuId);
        if (inventory.available() < qty) {
            throw new BadRequestException(
                    "Insufficient stock for SKU " + inventory.getSku().getSkuCode()
                            + ": available " + inventory.available() + ", requested " + qty
            );
        }
        inventory.setQtyReserved(inventory.getQtyReserved() + qty);
        inventoryRepository.save(inventory);
    }

    @Transactional
    public void capture(Long skuId, int qty) {
        Inventory inventory = requireBySkuId(skuId);
        if (inventory.getQtyReserved() < qty) {
            throw new BadRequestException("Cannot capture more than reserved qty for SKU " + skuId);
        }
        if (inventory.getQtyOnHand() < qty) {
            throw new BadRequestException("Cannot capture more than on-hand qty for SKU " + skuId);
        }
        inventory.setQtyOnHand(inventory.getQtyOnHand() - qty);
        inventory.setQtyReserved(inventory.getQtyReserved() - qty);
        inventoryRepository.save(inventory);
    }

    public void assertAvailable(Long skuId, int qty) {
        Inventory inventory = requireBySkuId(skuId);
        if (inventory.available() < qty) {
            throw new BadRequestException(
                    "Insufficient stock for SKU " + inventory.getSku().getSkuCode()
                            + ": available " + inventory.available() + ", requested " + qty
            );
        }
    }

    private Inventory requireBySkuId(Long skuId) {
        Sku sku = skuService.getById(skuId);
        return inventoryRepository.findBySkuId(sku.getId()).orElseGet(() -> inventoryRepository.save(new Inventory(sku)));
    }
}
