package com.rooonnie.ecomm.order;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/cart")
@Tag(name = "Cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @Operation(summary = "Get or create cart")
    public CartResponse get(@PathVariable Long userId) {
        return cartService.getOrCreate(userId);
    }

    @PostMapping("/items")
    @Operation(summary = "Set SKU qty in cart")
    public CartResponse addItem(@PathVariable Long userId, @Valid @RequestBody CartItemRequest request) {
        return cartService.addItem(userId, request);
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Replace cart item qty (0 removes it)")
    public CartResponse setItemQty(
            @PathVariable Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody CartItemQtyRequest request
    ) {
        return cartService.setItemQty(userId, itemId, request);
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove cart item")
    public CartResponse removeItem(@PathVariable Long userId, @PathVariable Long itemId) {
        return cartService.removeItem(userId, itemId);
    }
}
