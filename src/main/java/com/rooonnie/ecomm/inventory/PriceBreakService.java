package com.rooonnie.ecomm.inventory;

import com.rooonnie.ecomm.catalog.Sku;
import com.rooonnie.ecomm.catalog.SkuService;
import com.rooonnie.ecomm.common.BadRequestException;
import com.rooonnie.ecomm.common.ConflictException;
import com.rooonnie.ecomm.common.ResourceNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PriceBreakService {

    private final PriceBreakRepository priceBreakRepository;
    private final SkuService skuService;

    public PriceBreakService(PriceBreakRepository priceBreakRepository, SkuService skuService) {
        this.priceBreakRepository = priceBreakRepository;
        this.skuService = skuService;
    }

    @Transactional(readOnly = true)
    public List<PriceBreakResponse> findBySkuId(Long skuId) {
        skuService.getById(skuId);
        return priceBreakRepository.findBySkuIdOrderByMinQtyAsc(skuId).stream()
                .map(PriceBreakResponse::from)
                .toList();
    }

    @Transactional
    public PriceBreakResponse create(Long skuId, PriceBreakRequest request) {
        Sku sku = skuService.getById(skuId);
        if (priceBreakRepository.existsBySkuIdAndMinQty(skuId, request.minQty())) {
            throw new ConflictException("Price break already exists for minQty " + request.minQty());
        }
        PriceBreak priceBreak = new PriceBreak();
        priceBreak.setSku(sku);
        priceBreak.setMinQty(request.minQty());
        priceBreak.setUnitPrice(request.unitPrice().setScale(4, RoundingMode.HALF_UP));
        return PriceBreakResponse.from(priceBreakRepository.save(priceBreak));
    }

    @Transactional
    public List<PriceBreakResponse> replaceAll(Long skuId, List<PriceBreakRequest> requests) {
        Sku sku = skuService.getById(skuId);
        long distinctQty = requests.stream().map(PriceBreakRequest::minQty).distinct().count();
        if (distinctQty != requests.size()) {
            throw new BadRequestException("Duplicate minQty in price breaks");
        }
        priceBreakRepository.deleteBySkuId(skuId);
        return requests.stream()
                .sorted(Comparator.comparing(PriceBreakRequest::minQty))
                .map(request -> {
                    PriceBreak priceBreak = new PriceBreak();
                    priceBreak.setSku(sku);
                    priceBreak.setMinQty(request.minQty());
                    priceBreak.setUnitPrice(request.unitPrice().setScale(4, RoundingMode.HALF_UP));
                    return PriceBreakResponse.from(priceBreakRepository.save(priceBreak));
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public PriceQuoteResponse quote(Long skuId, int qty) {
        if (qty < 1) {
            throw new BadRequestException("qty must be at least 1");
        }
        Sku sku = skuService.getById(skuId);
        PriceBreak applied = priceBreakRepository.findBySkuIdOrderByMinQtyAsc(skuId).stream()
                .filter(breakRow -> breakRow.getMinQty() <= qty)
                .max(Comparator.comparing(PriceBreak::getMinQty))
                .orElseThrow(() -> new ResourceNotFoundException("No price break for qty " + qty));
        BigDecimal unitPrice = applied.getUnitPrice();
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty)).setScale(4, RoundingMode.HALF_UP);
        return new PriceQuoteResponse(
                sku.getId(),
                sku.getSkuCode(),
                qty,
                sku.getMoq(),
                applied.getMinQty(),
                unitPrice,
                lineTotal
        );
    }
}
