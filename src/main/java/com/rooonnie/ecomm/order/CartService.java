package com.rooonnie.ecomm.order;

import com.rooonnie.ecomm.catalog.Sku;
import com.rooonnie.ecomm.catalog.SkuService;
import com.rooonnie.ecomm.catalog.SkuStatus;
import com.rooonnie.ecomm.common.BadRequestException;
import com.rooonnie.ecomm.common.ResourceNotFoundException;
import com.rooonnie.ecomm.customer.Customer;
import com.rooonnie.ecomm.customer.CustomerService;
import com.rooonnie.ecomm.inventory.InventoryService;
import com.rooonnie.ecomm.inventory.PriceBreakService;
import com.rooonnie.ecomm.inventory.PriceQuoteResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CustomerService customerService;
    private final SkuService skuService;
    private final PriceBreakService priceBreakService;
    private final InventoryService inventoryService;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            CustomerService customerService,
            SkuService skuService,
            PriceBreakService priceBreakService,
            InventoryService inventoryService
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.customerService = customerService;
        this.skuService = skuService;
        this.priceBreakService = priceBreakService;
        this.inventoryService = inventoryService;
    }

    @Transactional
    public CartResponse getOrCreate(Long userId) {
        return toResponse(requireCart(userId));
    }

    @Transactional
    public CartResponse addItem(Long userId, CartItemRequest request) {
        Cart cart = requireCart(userId);
        Sku sku = skuService.getById(request.skuId());
        if (sku.getStatus() != SkuStatus.ACTIVE) {
            throw new BadRequestException("SKU is not active: " + sku.getSkuCode());
        }
        CartItem item = cartItemRepository.findByCartIdAndSkuId(cart.getId(), sku.getId())
                .orElseGet(() -> {
                    CartItem created = new CartItem();
                    created.setCart(cart);
                    created.setSku(sku);
                    created.setQty(0);
                    cart.getItems().add(created);
                    return created;
                });
        int newQty = item.getQty() + request.qty();
        validateLine(sku, newQty);
        item.setQty(newQty);
        cartItemRepository.save(item);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(Long userId, Long itemId) {
        Cart cart = requireCart(userId);
        CartItem item = cart.getItems().stream()
                .filter(row -> row.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + itemId));
        cart.getItems().remove(item);
        return toResponse(cart);
    }

    Cart requireCart(Long userId) {
        Customer customer = customerService.getById(userId);
        return cartRepository.findByCustomerId(userId).orElseGet(() -> cartRepository.save(new Cart(customer)));
    }

    void clear(Cart cart) {
        cart.getItems().clear();
    }

    void validateLine(Sku sku, int qty) {
        if (qty < sku.getMoq()) {
            throw new BadRequestException("Qty " + qty + " is below MOQ " + sku.getMoq() + " for " + sku.getSkuCode());
        }
        inventoryService.assertAvailable(sku.getId(), qty);
        priceBreakService.quote(sku.getId(), qty);
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            Sku sku = item.getSku();
            PriceQuoteResponse quote = priceBreakService.quote(sku.getId(), item.getQty());
            items.add(new CartItemResponse(
                    item.getId(),
                    sku.getId(),
                    sku.getSkuCode(),
                    sku.getPackagingType().getCode(),
                    item.getQty(),
                    sku.getMoq(),
                    quote.unitPrice(),
                    quote.lineTotal()
            ));
            subtotal = subtotal.add(quote.lineTotal());
        }
        return new CartResponse(cart.getId(), cart.getCustomer().getId(), items, subtotal);
    }
}
