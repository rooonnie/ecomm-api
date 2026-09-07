package com.rooonnie.ecomm.rereel;

import com.rooonnie.ecomm.catalog.PackagingType;
import com.rooonnie.ecomm.catalog.PackagingTypeService;
import com.rooonnie.ecomm.catalog.Sku;
import com.rooonnie.ecomm.catalog.SkuService;
import com.rooonnie.ecomm.catalog.SkuStatus;
import com.rooonnie.ecomm.common.BadRequestException;
import com.rooonnie.ecomm.common.ConflictException;
import com.rooonnie.ecomm.common.ResourceNotFoundException;
import com.rooonnie.ecomm.inventory.InventoryService;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RereelJobService {

    private static final Set<String> SOURCE_PACKAGING = Set.of("CUT_TAPE");
    private static final Set<String> TARGET_PACKAGING = Set.of("MINI_REEL", "REREEL");

    private final RereelJobRepository rereelJobRepository;
    private final SkuService skuService;
    private final PackagingTypeService packagingTypeService;
    private final InventoryService inventoryService;

    public RereelJobService(
            RereelJobRepository rereelJobRepository,
            SkuService skuService,
            PackagingTypeService packagingTypeService,
            InventoryService inventoryService
    ) {
        this.rereelJobRepository = rereelJobRepository;
        this.skuService = skuService;
        this.packagingTypeService = packagingTypeService;
        this.inventoryService = inventoryService;
    }

    @Transactional
    public RereelJobResponse create(RereelJobRequest request) {
        Sku source = skuService.getById(request.sourceSkuId());
        PackagingType targetPackaging = packagingTypeService.getById(request.targetPackagingTypeId());
        validate(source, targetPackaging);

        inventoryService.reserve(source.getId(), request.qty());

        RereelJob job = new RereelJob();
        job.setJobNo("RRL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        job.setSourceSku(source);
        job.setTargetPackagingType(targetPackaging);
        job.setQty(request.qty());
        job.setReelSize(blankToNull(request.reelSize()));
        job.setStatus(RereelJobStatus.REQUESTED);
        return toResponse(rereelJobRepository.save(job));
    }

    @Transactional(readOnly = true)
    public List<RereelJobResponse> findAll() {
        return rereelJobRepository.findAllByOrderByIdDesc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public RereelJobResponse findById(Long id) {
        return toResponse(getById(id));
    }

    @Transactional
    public RereelJobResponse complete(Long id) {
        RereelJob job = requireRequested(id);
        Sku source = job.getSourceSku();
        inventoryService.capture(source.getId(), job.getQty());
        Sku target = skuService.findOrCreateForPackaging(source, job.getTargetPackagingType(), job.getReelSize());
        inventoryService.addOnHand(target.getId(), job.getQty());
        job.setTargetSku(target);
        job.setStatus(RereelJobStatus.COMPLETED);
        job.setCompletedAt(Instant.now());
        return toResponse(rereelJobRepository.save(job));
    }

    @Transactional
    public RereelJobResponse cancel(Long id) {
        RereelJob job = requireRequested(id);
        inventoryService.release(job.getSourceSku().getId(), job.getQty());
        job.setStatus(RereelJobStatus.CANCELLED);
        return toResponse(rereelJobRepository.save(job));
    }

    private void validate(Sku source, PackagingType targetPackaging) {
        if (source.getStatus() != SkuStatus.ACTIVE) {
            throw new BadRequestException("Source SKU is not active: " + source.getSkuCode());
        }
        String sourceCode = source.getPackagingType().getCode();
        if (!SOURCE_PACKAGING.contains(sourceCode)) {
            throw new BadRequestException("Rereel source must be CUT_TAPE, got " + sourceCode);
        }
        String targetCode = targetPackaging.getCode();
        if (!TARGET_PACKAGING.contains(targetCode)) {
            throw new BadRequestException("Rereel target must be MINI_REEL or REREEL, got " + targetCode);
        }
    }

    private RereelJob requireRequested(Long id) {
        RereelJob job = getById(id);
        if (job.getStatus() != RereelJobStatus.REQUESTED) {
            throw new ConflictException(
                    "Rereel job " + job.getJobNo() + " cannot change; status is " + job.getStatus()
            );
        }
        return job;
    }

    private RereelJob getById(Long id) {
        return rereelJobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rereel job not found: " + id));
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private RereelJobResponse toResponse(RereelJob job) {
        Sku source = job.getSourceSku();
        Sku target = job.getTargetSku();
        PackagingType targetPackaging = job.getTargetPackagingType();
        return new RereelJobResponse(
                job.getId(),
                job.getJobNo(),
                source.getId(),
                source.getSkuCode(),
                source.getPackagingType().getCode(),
                source.getProduct().getMpn(),
                targetPackaging.getId(),
                targetPackaging.getCode(),
                target == null ? null : target.getId(),
                target == null ? null : target.getSkuCode(),
                job.getQty(),
                job.getReelSize(),
                job.getStatus(),
                job.getCreatedAt(),
                job.getCompletedAt()
        );
    }
}
