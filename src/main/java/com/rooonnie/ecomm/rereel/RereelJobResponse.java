package com.rooonnie.ecomm.rereel;

import java.time.Instant;

public record RereelJobResponse(
        Long id,
        String jobNo,
        Long sourceSkuId,
        String sourceSkuCode,
        String sourcePackagingCode,
        String mpn,
        Long targetPackagingTypeId,
        String targetPackagingCode,
        Long targetSkuId,
        String targetSkuCode,
        Integer qty,
        String reelSize,
        RereelJobStatus status,
        Instant createdAt,
        Instant completedAt
) {
}
