package com.rooonnie.ecomm.auth;

import com.rooonnie.ecomm.customer.CustomerRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register and return a JWT")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody CustomerRequest request) {
        AuthResponse created = authService.register(request);
        return ResponseEntity.created(URI.create("/api/users/" + created.user().id())).body(created);
    }

    @PostMapping("/login")
    @Operation(summary = "Login and return a JWT")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
