package com.rooonnie.ecomm.rereel;

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
@RequestMapping("/api/rereel-jobs")
@Tag(name = "Rereel Jobs", description = "Convert cut tape leftover into mini-reel or rereel stock")
public class RereelJobController {

    private final RereelJobService rereelJobService;

    public RereelJobController(RereelJobService rereelJobService) {
        this.rereelJobService = rereelJobService;
    }

    @GetMapping
    @Operation(summary = "List rereel jobs")
    public List<RereelJobResponse> list() {
        return rereelJobService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get rereel job")
    public RereelJobResponse get(@PathVariable Long id) {
        return rereelJobService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Request a rereel job and reserve source stock")
    public ResponseEntity<RereelJobResponse> create(@Valid @RequestBody RereelJobRequest request) {
        RereelJobResponse created = rereelJobService.create(request);
        return ResponseEntity.created(URI.create("/api/rereel-jobs/" + created.id())).body(created);
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete job: consume cut tape and add mini-reel stock")
    public RereelJobResponse complete(@PathVariable Long id) {
        return rereelJobService.complete(id);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel job and release reserved cut tape")
    public RereelJobResponse cancel(@PathVariable Long id) {
        return rereelJobService.cancel(id);
    }
}
