package com.rooonnie.ecomm.catalog;

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

@Entity
@Table(name = "skus")
public class Sku {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "packaging_type_id", nullable = false)
    private PackagingType packagingType;

    @Column(name = "sku_code", nullable = false, unique = true, length = 80)
    private String skuCode;

    @Column(name = "qty_per_pack", nullable = false)
    private Integer qtyPerPack;

    @Column(name = "reel_size", length = 20)
    private String reelSize;

    @Column(nullable = false)
    private Integer moq;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SkuStatus status = SkuStatus.ACTIVE;

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public PackagingType getPackagingType() {
        return packagingType;
    }

    public void setPackagingType(PackagingType packagingType) {
        this.packagingType = packagingType;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public void setSkuCode(String skuCode) {
        this.skuCode = skuCode;
    }

    public Integer getQtyPerPack() {
        return qtyPerPack;
    }

    public void setQtyPerPack(Integer qtyPerPack) {
        this.qtyPerPack = qtyPerPack;
    }

    public String getReelSize() {
        return reelSize;
    }

    public void setReelSize(String reelSize) {
        this.reelSize = reelSize;
    }

    public Integer getMoq() {
        return moq;
    }

    public void setMoq(Integer moq) {
        this.moq = moq;
    }

    public SkuStatus getStatus() {
        return status;
    }

    public void setStatus(SkuStatus status) {
        this.status = status;
    }
}
