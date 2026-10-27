package com.ibms.auth_service.controller;

import com.ibms.auth_service.dto.AuthResponse;
import com.ibms.auth_service.dto.LoginRequest;
import com.ibms.auth_service.dto.RegisterRequest;
import com.ibms.auth_service.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/users/{id}")
    public AuthResponse getUser(@PathVariable Long id) {
        return authService.getUser(id);
    }
}
