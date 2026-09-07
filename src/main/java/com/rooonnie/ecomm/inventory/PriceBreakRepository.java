package com.rooonnie.ecomm.inventory;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceBreakRepository extends JpaRepository<PriceBreak, Long> {

    List<PriceBreak> findBySkuIdOrderByMinQtyAsc(Long skuId);

    Optional<PriceBreak> findBySkuIdAndMinQty(Long skuId, Integer minQty);

    boolean existsBySkuIdAndMinQty(Long skuId, Integer minQty);

    void deleteBySkuId(Long skuId);
}
