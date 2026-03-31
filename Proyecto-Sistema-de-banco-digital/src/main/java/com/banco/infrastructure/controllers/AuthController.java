package com.banco.infrastructure.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banco.application.dto.auth.AuthResponse;
import com.banco.application.dto.auth.LoginRequest;
import com.banco.application.dto.auth.RegisterRequest;
import com.banco.application.services.AuthService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;






@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        
        AuthResponse response = authService.login(request);
        
        return ResponseEntity.ok(response);
    }


    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        
        String mensaje = authService.register(request);
        
        return ResponseEntity.ok(mensaje);
    }
    
    

}
