package com.rooonnie.ecomm.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByMpnAndManufacturerId(String mpn, Long manufacturerId);
}
