package com.rooonnie.ecomm.catalog;

import com.rooonnie.ecomm.common.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductService productService;

    public ProductImageService(ProductImageRepository productImageRepository, ProductService productService) {
        this.productImageRepository = productImageRepository;
        this.productService = productService;
    }

    @Transactional(readOnly = true)
    public List<ProductImageResponse> findByProductId(Long productId) {
        productService.getById(productId);
        return productImageRepository.findByProductIdOrderBySortOrderAscIdAsc(productId).stream()
                .map(ProductImageResponse::from)
                .toList();
    }

    @Transactional
    public ProductImageResponse create(Long productId, ProductImageRequest request) {
        Product product = productService.getById(productId);
        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setUrl(request.url().trim());
        image.setAltText(blankToNull(request.altText()));
        image.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        return ProductImageResponse.from(productImageRepository.save(image));
    }

    @Transactional
    public void delete(Long productId, Long imageId) {
        productService.getById(productId);
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Product image not found: " + imageId));
        if (!image.getProduct().getId().equals(productId)) {
            throw new ResourceNotFoundException("Product image not found: " + imageId);
        }
        productImageRepository.delete(image);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
