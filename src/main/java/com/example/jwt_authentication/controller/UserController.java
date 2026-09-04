package com.example.jwt_authentication.controller;

import com.example.jwt_authentication.dto.UserResponse;
import com.example.jwt_authentication.entity.User;
import com.example.jwt_authentication.service.AuthService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(
            Authentication authentication) {

        String username = authentication.getName();

        User user =
                authService.getUserByUsername(username);

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}