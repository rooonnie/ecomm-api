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
@RequestMapping("/api/manufacturers")
@Tag(name = "Manufacturers")
public class ManufacturerController {

    private final ManufacturerService manufacturerService;

    public ManufacturerController(ManufacturerService manufacturerService) {
        this.manufacturerService = manufacturerService;
    }

    @GetMapping
    @Operation(summary = "List manufacturers")
    public List<NamedSlugResponse> list() {
        return manufacturerService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get manufacturer by id")
    public NamedSlugResponse get(@PathVariable Long id) {
        return manufacturerService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create manufacturer")
    public ResponseEntity<NamedSlugResponse> create(@Valid @RequestBody NamedSlugRequest request) {
        NamedSlugResponse created = manufacturerService.create(request);
        return ResponseEntity.created(URI.create("/api/manufacturers/" + created.id())).body(created);
    }
}
