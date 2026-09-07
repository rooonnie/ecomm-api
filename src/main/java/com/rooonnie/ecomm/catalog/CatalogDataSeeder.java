package com.rooonnie.ecomm.catalog;

import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CatalogDataSeeder implements ApplicationRunner {

    private final PackagingTypeRepository packagingTypeRepository;
    private final CategoryRepository categoryRepository;
    private final ManufacturerRepository manufacturerRepository;

    public CatalogDataSeeder(
            PackagingTypeRepository packagingTypeRepository,
            CategoryRepository categoryRepository,
            ManufacturerRepository manufacturerRepository
    ) {
        this.packagingTypeRepository = packagingTypeRepository;
        this.categoryRepository = categoryRepository;
        this.manufacturerRepository = manufacturerRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedPackagingTypes();
        seedCategory("Chip Resistors", "chip-resistors");
        seedManufacturer("Yageo", "yageo");
    }

    private void seedPackagingTypes() {
        List<PackagingType> defaults = List.of(
                new PackagingType("CUT_TAPE", "Cut tape"),
                new PackagingType("TAPE_AND_REEL", "Tape and reel"),
                new PackagingType("MINI_REEL", "Mini-reel"),
                new PackagingType("REREEL", "Rereel"),
                new PackagingType("TUBE", "Tube"),
                new PackagingType("TRAY", "Tray"),
                new PackagingType("BULK", "Bulk")
        );
        for (PackagingType type : defaults) {
            if (!packagingTypeRepository.existsByCode(type.getCode())) {
                packagingTypeRepository.save(type);
            }
        }
    }

    private void seedCategory(String name, String slug) {
        if (!categoryRepository.existsBySlug(slug)) {
            categoryRepository.save(new Category(name, slug));
        }
    }

    private void seedManufacturer(String name, String slug) {
        if (!manufacturerRepository.existsBySlug(slug)) {
            manufacturerRepository.save(new Manufacturer(name, slug));
        }
    }
}
