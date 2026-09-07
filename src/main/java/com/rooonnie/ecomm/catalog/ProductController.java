package com.rooonnie.ecomm.catalog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "The electronic part itself (MPN), not the sellable packaging")
public class ProductController {

    private final ProductService productService;
    private final SkuService skuService;
    private final ProductImageService productImageService;

    public ProductController(
            ProductService productService,
            SkuService skuService,
            ProductImageService productImageService
    ) {
        this.productService = productService;
        this.skuService = skuService;
        this.productImageService = productImageService;
    }

    @GetMapping
    @Operation(summary = "List products")
    public List<ProductResponse> list() {
        return productService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by id")
    public ProductResponse get(@PathVariable Long id) {
        return productService.findById(id);
    }

    @GetMapping("/{id}/skus")
    @Operation(summary = "List SKUs for a product")
    public List<SkuResponse> listSkus(@PathVariable Long id) {
        return skuService.findByProductId(id);
    }

    @GetMapping("/{id}/images")
    @Operation(summary = "List product images")
    public List<ProductImageResponse> listImages(@PathVariable Long id) {
        return productImageService.findByProductId(id);
    }

    @PostMapping("/{id}/images")
    @Operation(summary = "Add a product image URL")
    public ResponseEntity<ProductImageResponse> addImage(
            @PathVariable Long id,
            @Valid @RequestBody ProductImageRequest request
    ) {
        ProductImageResponse created = productImageService.create(id, request);
        return ResponseEntity.created(URI.create("/api/products/" + id + "/images/" + created.id())).body(created);
    }

    @DeleteMapping("/{id}/images/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a product image")
    public void deleteImage(@PathVariable Long id, @PathVariable Long imageId) {
        productImageService.delete(id, imageId);
    }

    @PostMapping
    @Operation(summary = "Create product")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse created = productService.create(request);
        return ResponseEntity.created(URI.create("/api/products/" + created.id())).body(created);
    }
}
