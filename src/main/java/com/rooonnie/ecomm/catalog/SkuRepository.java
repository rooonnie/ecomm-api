package com.rooonnie.ecomm.catalog;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkuRepository extends JpaRepository<Sku, Long> {

    List<Sku> findByProductId(Long productId);

    Optional<Sku> findFirstByProductIdAndPackagingTypeId(Long productId, Long packagingTypeId);

    boolean existsBySkuCode(String skuCode);
}
