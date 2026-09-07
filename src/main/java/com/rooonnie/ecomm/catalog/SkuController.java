package com.rooonnie.ecomm.catalog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/skus")
@Tag(name = "SKUs", description = "Sellable unit = product + packaging (cut tape, reel, mini-reel)")
public class SkuController {

    private final SkuService skuService;

    public SkuController(SkuService skuService) {
        this.skuService = skuService;
    }

    @GetMapping
    @Operation(summary = "List SKUs")
    public List<SkuResponse> list() {
        return skuService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get SKU by id")
    public SkuResponse get(@PathVariable Long id) {
        return skuService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create SKU")
    public ResponseEntity<SkuResponse> create(@Valid @RequestBody SkuRequest request) {
        SkuResponse created = skuService.create(request);
        return ResponseEntity.created(URI.create("/api/skus/" + created.id())).body(created);
    }
}
