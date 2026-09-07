package com.rooonnie.ecomm.catalog;

import com.rooonnie.ecomm.common.ConflictException;
import com.rooonnie.ecomm.common.ResourceNotFoundException;
import com.rooonnie.ecomm.inventory.Inventory;
import com.rooonnie.ecomm.inventory.InventoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SkuService {

    private final SkuRepository skuRepository;
    private final ProductService productService;
    private final PackagingTypeService packagingTypeService;
    private final InventoryRepository inventoryRepository;

    public SkuService(
            SkuRepository skuRepository,
            ProductService productService,
            PackagingTypeService packagingTypeService,
            InventoryRepository inventoryRepository
    ) {
        this.skuRepository = skuRepository;
        this.productService = productService;
        this.packagingTypeService = packagingTypeService;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional(readOnly = true)
    public List<SkuResponse> findAll() {
        return skuRepository.findAll().stream().map(SkuResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<SkuResponse> findByProductId(Long productId) {
        productService.getById(productId);
        return skuRepository.findByProductId(productId).stream().map(SkuResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public SkuResponse findById(Long id) {
        return SkuResponse.from(getById(id));
    }

    @Transactional
    public SkuResponse create(SkuRequest request) {
        if (skuRepository.existsBySkuCode(request.skuCode().trim())) {
            throw new ConflictException("SKU code already exists: " + request.skuCode());
        }

        Sku sku = new Sku();
        sku.setProduct(productService.getById(request.productId()));
        sku.setPackagingType(packagingTypeService.getById(request.packagingTypeId()));
        sku.setSkuCode(request.skuCode().trim().toUpperCase());
        sku.setQtyPerPack(request.qtyPerPack());
        sku.setReelSize(request.reelSize());
        sku.setMoq(request.moq());
        sku.setStatus(request.status() == null ? SkuStatus.ACTIVE : request.status());
        Sku saved = skuRepository.save(sku);
        inventoryRepository.save(new Inventory(saved));
        return SkuResponse.from(saved);
    }

    public Sku getById(Long id) {
        return skuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SKU not found: " + id));
    }

    @Transactional
    public Sku findOrCreateForPackaging(Sku source, PackagingType targetPackaging, String reelSize) {
        return skuRepository
                .findFirstByProductIdAndPackagingTypeId(source.getProduct().getId(), targetPackaging.getId())
                .orElseGet(() -> createCompanionSku(source, targetPackaging, reelSize));
    }

    private Sku createCompanionSku(Sku source, PackagingType targetPackaging, String reelSize) {
        String base = (source.getSkuCode() + "-" + packagingSuffix(targetPackaging.getCode())).toUpperCase();
        String skuCode = base;
        int suffix = 2;
        while (skuRepository.existsBySkuCode(skuCode)) {
            skuCode = base + "-" + suffix;
            suffix++;
        }

        Sku sku = new Sku();
        sku.setProduct(source.getProduct());
        sku.setPackagingType(targetPackaging);
        sku.setSkuCode(skuCode);
        sku.setQtyPerPack(1);
        sku.setReelSize(reelSize);
        sku.setMoq(1);
        sku.setStatus(SkuStatus.ACTIVE);
        Sku saved = skuRepository.save(sku);
        inventoryRepository.save(new Inventory(saved));
        return saved;
    }

    private static String packagingSuffix(String packagingCode) {
        return switch (packagingCode) {
            case "MINI_REEL" -> "MR";
            case "REREEL" -> "RR";
            default -> packagingCode;
        };
    }
}
