package com.rooonnie.ecomm.catalog;

import com.rooonnie.ecomm.common.ConflictException;
import com.rooonnie.ecomm.common.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<NamedSlugResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(category -> new NamedSlugResponse(category.getId(), category.getName(), category.getSlug()))
                .toList();
    }

    @Transactional(readOnly = true)
    public NamedSlugResponse findById(Long id) {
        Category category = getById(id);
        return new NamedSlugResponse(category.getId(), category.getName(), category.getSlug());
    }

    @Transactional
    public NamedSlugResponse create(NamedSlugRequest request) {
        String slug = request.slug().trim().toLowerCase();
        if (categoryRepository.existsBySlug(slug)) {
            throw new ConflictException("Category slug already exists: " + slug);
        }
        Category saved = categoryRepository.save(new Category(request.name().trim(), slug));
        return new NamedSlugResponse(saved.getId(), saved.getName(), saved.getSlug());
    }

    Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }
}
