package com.rooonnie.ecomm.catalog;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManufacturerRepository extends JpaRepository<Manufacturer, Long> {

    Optional<Manufacturer> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
