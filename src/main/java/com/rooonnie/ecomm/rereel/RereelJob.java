package com.rooonnie.ecomm.rereel;

import com.rooonnie.ecomm.catalog.PackagingType;
import com.rooonnie.ecomm.catalog.Sku;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "rereel_jobs")
public class RereelJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_no", nullable = false, unique = true, length = 40)
    private String jobNo;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "source_sku_id", nullable = false)
    private Sku sourceSku;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "target_packaging_type_id", nullable = false)
    private PackagingType targetPackagingType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_sku_id")
    private Sku targetSku;

    @Column(nullable = false)
    private Integer qty;

    @Column(name = "reel_size", length = 20)
    private String reelSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RereelJobStatus status = RereelJobStatus.REQUESTED;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    public Long getId() {
        return id;
    }

    public String getJobNo() {
        return jobNo;
    }

    public void setJobNo(String jobNo) {
        this.jobNo = jobNo;
    }

    public Sku getSourceSku() {
        return sourceSku;
    }

    public void setSourceSku(Sku sourceSku) {
        this.sourceSku = sourceSku;
    }

    public PackagingType getTargetPackagingType() {
        return targetPackagingType;
    }

    public void setTargetPackagingType(PackagingType targetPackagingType) {
        this.targetPackagingType = targetPackagingType;
    }

    public Sku getTargetSku() {
        return targetSku;
    }

    public void setTargetSku(Sku targetSku) {
        this.targetSku = targetSku;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public String getReelSize() {
        return reelSize;
    }

    public void setReelSize(String reelSize) {
        this.reelSize = reelSize;
    }

    public RereelJobStatus getStatus() {
        return status;
    }

    public void setStatus(RereelJobStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
