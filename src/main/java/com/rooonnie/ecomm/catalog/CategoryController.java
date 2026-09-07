package com.rooonnie.ecomm.catalog;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(summary = "List categories")
    public List<NamedSlugResponse> list() {
        return categoryService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by id")
    public NamedSlugResponse get(@PathVariable Long id) {
        return categoryService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create category")
    public ResponseEntity<NamedSlugResponse> create(@Valid @RequestBody NamedSlugRequest request) {
        NamedSlugResponse created = categoryService.create(request);
        return ResponseEntity.created(URI.create("/api/categories/" + created.id())).body(created);
    }
}
