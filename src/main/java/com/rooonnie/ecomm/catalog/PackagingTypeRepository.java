package com.rooonnie.ecomm.catalog;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackagingTypeRepository extends JpaRepository<PackagingType, Long> {

    Optional<PackagingType> findByCode(String code);

    boolean existsByCode(String code);
}
