package com.rooonnie.ecomm.catalog;

import com.rooonnie.ecomm.common.ConflictException;
import com.rooonnie.ecomm.common.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PackagingTypeService {

    private final PackagingTypeRepository packagingTypeRepository;

    public PackagingTypeService(PackagingTypeRepository packagingTypeRepository) {
        this.packagingTypeRepository = packagingTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<PackagingTypeResponse> findAll() {
        return packagingTypeRepository.findAll().stream().map(PackagingTypeResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PackagingTypeResponse findById(Long id) {
        return PackagingTypeResponse.from(getById(id));
    }

    @Transactional
    public PackagingTypeResponse create(PackagingTypeRequest request) {
        String code = request.code().trim().toUpperCase();
        if (packagingTypeRepository.existsByCode(code)) {
            throw new ConflictException("Packaging type already exists: " + code);
        }
        PackagingType saved = packagingTypeRepository.save(new PackagingType(code, request.name().trim()));
        return PackagingTypeResponse.from(saved);
    }

    public PackagingType getById(Long id) {
        return packagingTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Packaging type not found: " + id));
    }
}
