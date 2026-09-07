package com.rooonnie.ecomm.catalog;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkuRepository extends JpaRepository<Sku, Long> {

    List<Sku> findByProductId(Long productId);

    boolean existsBySkuCode(String skuCode);
}
