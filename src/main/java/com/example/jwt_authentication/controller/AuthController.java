package com.example.jwt_authentication.controller;

import com.example.jwt_authentication.dto.LoginRequest;
import com.example.jwt_authentication.dto.LoginResponse;
import com.example.jwt_authentication.dto.RegisterRequest;
import com.example.jwt_authentication.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.jwt_authentication.dto.MessageResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        authService.register(request);

        return ResponseEntity.ok(
                new MessageResponse("User registered successfully")
        );
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        String token = authService.login(
                request.getUsername(),
                request.getPassword()
        );

        return ResponseEntity.ok(
                new LoginResponse(token)
        );
    }
}