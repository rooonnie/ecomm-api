package com.rooonnie.ecomm.catalog;

import com.rooonnie.ecomm.common.ConflictException;
import com.rooonnie.ecomm.common.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ManufacturerService manufacturerService;
    private final CategoryService categoryService;

    public ProductService(
            ProductRepository productRepository,
            ProductImageRepository productImageRepository,
            ManufacturerService manufacturerService,
            CategoryService categoryService
    ) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.manufacturerService = manufacturerService;
        this.categoryService = categoryService;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return toResponse(getById(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Manufacturer manufacturer = manufacturerService.getById(request.manufacturerId());
        Category category = categoryService.getById(request.categoryId());
        if (productRepository.existsByMpnAndManufacturerId(request.mpn().trim(), manufacturer.getId())) {
            throw new ConflictException("MPN already exists for this manufacturer: " + request.mpn());
        }

        Product product = new Product();
        product.setMpn(request.mpn().trim());
        product.setManufacturer(manufacturer);
        product.setCategory(category);
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setPackageCase(request.packageCase());
        product.setLifecycle(request.lifecycle() == null ? ProductLifecycle.ACTIVE : request.lifecycle());
        return toResponse(productRepository.save(product));
    }

    Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.from(
                product,
                productImageRepository.findByProductIdOrderBySortOrderAscIdAsc(product.getId())
        );
    }
}
