package com.rooonnie.ecomm.order;

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
@Tag(name = "Orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/api/users/{userId}/checkout")
    @Operation(summary = "Checkout cart into an order")
    public ResponseEntity<OrderResponse> checkout(
            @PathVariable Long userId,
            @Valid @RequestBody CheckoutRequest request
    ) {
        OrderResponse created = orderService.checkout(userId, request);
        return ResponseEntity.created(URI.create("/api/orders/" + created.id())).body(created);
    }

    @GetMapping("/api/users/{userId}/orders")
    @Operation(summary = "List user orders")
    public List<OrderResponse> list(@PathVariable Long userId) {
        return orderService.findByUser(userId);
    }

    @GetMapping("/api/orders/{id}")
    @Operation(summary = "Get order")
    public OrderResponse get(@PathVariable Long id) {
        return orderService.findById(id);
    }

    @PostMapping("/api/orders/{id}/pay")
    @Operation(summary = "Mark order as paid (manual payment)")
    public OrderResponse pay(@PathVariable Long id) {
        return orderService.pay(id);
    }
}
