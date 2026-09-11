package com.rooonnie.ecomm.order;

import com.rooonnie.ecomm.catalog.Sku;
import com.rooonnie.ecomm.auth.AuthGuard;
import com.rooonnie.ecomm.common.BadRequestException;
import com.rooonnie.ecomm.common.ConflictException;
import com.rooonnie.ecomm.common.ResourceNotFoundException;
import com.rooonnie.ecomm.customer.Address;
import com.rooonnie.ecomm.customer.Customer;
import com.rooonnie.ecomm.customer.CustomerService;
import com.rooonnie.ecomm.inventory.InventoryService;
import com.rooonnie.ecomm.inventory.PriceBreakService;
import com.rooonnie.ecomm.inventory.PriceQuoteResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final ShopOrderRepository shopOrderRepository;
    private final CartService cartService;
    private final CustomerService customerService;
    private final PriceBreakService priceBreakService;
    private final InventoryService inventoryService;
    private final ShippingService shippingService;
    private final AuthGuard authGuard;

    public OrderService(
            ShopOrderRepository shopOrderRepository,
            CartService cartService,
            CustomerService customerService,
            PriceBreakService priceBreakService,
            InventoryService inventoryService,
            ShippingService shippingService,
            AuthGuard authGuard
    ) {
        this.shopOrderRepository = shopOrderRepository;
        this.cartService = cartService;
        this.customerService = customerService;
        this.priceBreakService = priceBreakService;
        this.inventoryService = inventoryService;
        this.shippingService = shippingService;
        this.authGuard = authGuard;
    }

    @Transactional
    public OrderResponse checkout(Long userId, CheckoutRequest request) {
        authGuard.requireUser(userId);
        Customer customer = customerService.getById(userId);
        Address address = customerService.getAddress(userId, request.addressId());
        Cart cart = cartService.requireCart(userId);
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        ShopOrder order = new ShopOrder();
        order.setOrderNo("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setShippingFee(shippingService.quote(address.getCountry()));
        order.setShippingLine1(address.getLine1());
        order.setShippingCity(address.getCity());
        order.setShippingCountry(address.getCountry());
        order.setShippingPostal(address.getPostal());

        BigDecimal subtotal = BigDecimal.ZERO;
        List<CartItem> lines = List.copyOf(cart.getItems());
        for (CartItem cartItem : lines) {
            Sku sku = cartItem.getSku();
            cartService.validateLine(sku, cartItem.getQty());
            PriceQuoteResponse quote = priceBreakService.quote(sku.getId(), cartItem.getQty());
            inventoryService.reserve(sku.getId(), cartItem.getQty());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setSku(sku);
            orderItem.setSkuCode(sku.getSkuCode());
            orderItem.setMpn(sku.getProduct().getMpn());
            orderItem.setPackagingCode(sku.getPackagingType().getCode());
            orderItem.setPackagingSnapshot(snapshot(sku));
            orderItem.setQty(cartItem.getQty());
            orderItem.setUnitPrice(quote.unitPrice());
            orderItem.setLineTotal(quote.lineTotal());
            order.getItems().add(orderItem);
            subtotal = subtotal.add(quote.lineTotal());
        }

        order.setSubtotal(subtotal);
        order.setTotal(subtotal.add(order.getShippingFee()));

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setProvider("MANUAL");
        payment.setAmount(order.getTotal());
        payment.setStatus(PaymentStatus.PENDING);
        order.setPayment(payment);

        ShopOrder saved = shopOrderRepository.save(order);
        cartService.clear(cart);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        return toResponse(requireOwned(id));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findByUser(Long userId) {
        authGuard.requireUser(userId);
        customerService.getById(userId);
        return shopOrderRepository.findByCustomerIdOrderByIdDesc(userId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public OrderResponse pay(Long orderId) {
        ShopOrder order = requirePendingPayment(orderId);
        for (OrderItem item : order.getItems()) {
            inventoryService.capture(item.getSku().getId(), item.getQty());
        }
        order.setStatus(OrderStatus.PAID);
        Payment payment = order.getPayment();
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(Instant.now());
        return toResponse(shopOrderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancel(Long orderId) {
        ShopOrder order = requirePendingPayment(orderId);
        for (OrderItem item : order.getItems()) {
            inventoryService.release(item.getSku().getId(), item.getQty());
        }
        order.setStatus(OrderStatus.CANCELLED);
        Payment payment = order.getPayment();
        payment.setStatus(PaymentStatus.CANCELLED);
        return toResponse(shopOrderRepository.save(order));
    }

    private ShopOrder requirePendingPayment(Long orderId) {
        ShopOrder order = requireOwned(orderId);
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new ConflictException(
                    "Order " + order.getOrderNo() + " cannot change; status is " + order.getStatus()
            );
        }
        return order;
    }

    private ShopOrder requireOwned(Long orderId) {
        ShopOrder order = getById(orderId);
        authGuard.requireUser(order.getCustomer().getId());
        return order;
    }

    private ShopOrder getById(Long id) {
        return shopOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    private static String snapshot(Sku sku) {
        String reel = sku.getReelSize() == null ? "" : ", reel " + sku.getReelSize();
        return sku.getPackagingType().getName() + " x" + sku.getQtyPerPack() + reel;
    }

    private OrderResponse toResponse(ShopOrder order) {
        List<OrderItemResponse> items = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            items.add(new OrderItemResponse(
                    item.getId(),
                    item.getSku() == null ? null : item.getSku().getId(),
                    item.getSkuCode(),
                    item.getMpn(),
                    item.getPackagingCode(),
                    item.getPackagingSnapshot(),
                    item.getQty(),
                    item.getUnitPrice(),
                    item.getLineTotal()
            ));
        }
        Payment payment = order.getPayment();
        PaymentResponse paymentResponse = payment == null ? null : new PaymentResponse(
                payment.getId(),
                payment.getProvider(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaidAt()
        );
        return new OrderResponse(
                order.getId(),
                order.getOrderNo(),
                order.getCustomer().getId(),
                order.getStatus(),
                order.getSubtotal(),
                order.getShippingFee(),
                order.getTotal(),
                order.getShippingLine1(),
                order.getShippingCity(),
                order.getShippingCountry(),
                order.getShippingPostal(),
                order.getCreatedAt(),
                items,
                paymentResponse
        );
    }
}
