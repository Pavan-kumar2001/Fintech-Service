package com.fintech.fintech_service.controller;

import com.fintech.fintech_service.dto.auth.AuthRequest;
import com.fintech.fintech_service.dto.user.UserRequest;
import com.fintech.fintech_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authReq) {

        log.info("Login request received for mobile: {}", authReq.getMobile());
        return ResponseEntity.ok(authService.login(authReq));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerNewUser(@RequestBody @Valid UserRequest registerReq) {

        log.info("Register request received for Mobile: {}", registerReq.getMobile());
        return ResponseEntity.ok(authService.register(registerReq));
    }
}