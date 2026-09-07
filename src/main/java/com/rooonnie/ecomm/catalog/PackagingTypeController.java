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
@RequestMapping("/api/packaging-types")
@Tag(name = "Packaging Types", description = "Cut tape, reel, tray, tube, bulk")
public class PackagingTypeController {

    private final PackagingTypeService packagingTypeService;

    public PackagingTypeController(PackagingTypeService packagingTypeService) {
        this.packagingTypeService = packagingTypeService;
    }

    @GetMapping
    @Operation(summary = "List packaging types")
    public List<PackagingTypeResponse> list() {
        return packagingTypeService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get packaging type by id")
    public PackagingTypeResponse get(@PathVariable Long id) {
        return packagingTypeService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create packaging type")
    public ResponseEntity<PackagingTypeResponse> create(@Valid @RequestBody PackagingTypeRequest request) {
        PackagingTypeResponse created = packagingTypeService.create(request);
        return ResponseEntity.created(URI.create("/api/packaging-types/" + created.id())).body(created);
    }
}
