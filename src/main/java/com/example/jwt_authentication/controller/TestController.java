package com.example.jwt_authentication.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public String test() {
        return "You are authenticated!";
    }

    @GetMapping("/api/admin")
    public String admin() {
        return "You are an admin!";
    }
}