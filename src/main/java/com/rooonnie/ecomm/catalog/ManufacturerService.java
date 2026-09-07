package com.rooonnie.ecomm.catalog;

import com.rooonnie.ecomm.common.ConflictException;
import com.rooonnie.ecomm.common.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManufacturerService {

    private final ManufacturerRepository manufacturerRepository;

    public ManufacturerService(ManufacturerRepository manufacturerRepository) {
        this.manufacturerRepository = manufacturerRepository;
    }

    @Transactional(readOnly = true)
    public List<NamedSlugResponse> findAll() {
        return manufacturerRepository.findAll().stream()
                .map(item -> new NamedSlugResponse(item.getId(), item.getName(), item.getSlug()))
                .toList();
    }

    @Transactional(readOnly = true)
    public NamedSlugResponse findById(Long id) {
        Manufacturer manufacturer = getById(id);
        return new NamedSlugResponse(manufacturer.getId(), manufacturer.getName(), manufacturer.getSlug());
    }

    @Transactional
    public NamedSlugResponse create(NamedSlugRequest request) {
        String slug = request.slug().trim().toLowerCase();
        if (manufacturerRepository.existsBySlug(slug)) {
            throw new ConflictException("Manufacturer slug already exists: " + slug);
        }
        Manufacturer saved = manufacturerRepository.save(new Manufacturer(request.name().trim(), slug));
        return new NamedSlugResponse(saved.getId(), saved.getName(), saved.getSlug());
    }

    Manufacturer getById(Long id) {
        return manufacturerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manufacturer not found: " + id));
    }
}
