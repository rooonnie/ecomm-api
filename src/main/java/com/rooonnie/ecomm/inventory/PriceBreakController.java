package com.rooonnie.ecomm.inventory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/skus/{skuId}/price-breaks")
@Tag(name = "Price Breaks", description = "Volume pricing per SKU")
public class PriceBreakController {

    private final PriceBreakService priceBreakService;

    public PriceBreakController(PriceBreakService priceBreakService) {
        this.priceBreakService = priceBreakService;
    }

    @GetMapping
    @Operation(summary = "List price breaks for a SKU")
    public List<PriceBreakResponse> list(@PathVariable Long skuId) {
        return priceBreakService.findBySkuId(skuId);
    }

    @PostMapping
    @Operation(summary = "Add one price break")
    public ResponseEntity<PriceBreakResponse> create(
            @PathVariable Long skuId,
            @Valid @RequestBody PriceBreakRequest request
    ) {
        PriceBreakResponse created = priceBreakService.create(skuId, request);
        return ResponseEntity.created(URI.create("/api/skus/" + skuId + "/price-breaks")).body(created);
    }

    @PutMapping
    @Operation(summary = "Replace all price breaks for a SKU")
    public List<PriceBreakResponse> replace(
            @PathVariable Long skuId,
            @Valid @RequestBody ReplacePriceBreaksRequest request
    ) {
        return priceBreakService.replaceAll(skuId, request.breaks());
    }

    @GetMapping("/quote")
    @Operation(summary = "Quote unit price for a qty")
    public PriceQuoteResponse quote(@PathVariable Long skuId, @RequestParam int qty) {
        return priceBreakService.quote(skuId, qty);
    }
}
