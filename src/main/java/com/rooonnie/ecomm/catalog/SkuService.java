package com.rooonnie.ecomm.catalog;

import com.rooonnie.ecomm.common.ConflictException;
import com.rooonnie.ecomm.common.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SkuService {

    private final SkuRepository skuRepository;
    private final ProductService productService;
    private final PackagingTypeService packagingTypeService;

    public SkuService(
            SkuRepository skuRepository,
            ProductService productService,
            PackagingTypeService packagingTypeService
    ) {
        this.skuRepository = skuRepository;
        this.productService = productService;
        this.packagingTypeService = packagingTypeService;
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
        return SkuResponse.from(skuRepository.save(sku));
    }

    private Sku getById(Long id) {
        return skuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SKU not found: " + id));
    }
}
