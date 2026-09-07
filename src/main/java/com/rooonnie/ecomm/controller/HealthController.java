package com.rooonnie.ecomm.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Backend status")
public class HealthController {

    @GetMapping
    @Operation(summary = "Check API status")
    public HealthResponse health() {
        return new HealthResponse("UP", "ecomm-api");
    }

    public record HealthResponse(String status, String service) {
    }
}
