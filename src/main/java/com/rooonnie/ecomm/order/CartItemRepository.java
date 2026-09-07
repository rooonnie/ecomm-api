package com.rooonnie.ecomm.order;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartIdAndSkuId(Long cartId, Long skuId);
}
