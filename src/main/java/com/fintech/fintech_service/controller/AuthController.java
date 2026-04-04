package com.fintech.fintech_service.controller;

import com.fintech.fintech_service.dto.auth.AuthRequest;
import com.fintech.fintech_service.dto.user.UserRequest;
import com.fintech.fintech_service.security.util.JWTUtil;
import com.fintech.fintech_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> generateToken(@RequestBody AuthRequest authReq) {

        // Authenticate (handles both user not found + wrong password)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authReq.getMobile(),
                        authReq.getPassword()
                )
        );

        String token = jwtUtil.generateToken(authReq.getMobile());

        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "token", token
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerNewUser(@RequestBody @Valid UserRequest registerReq) {

        userService.create(registerReq);

        return ResponseEntity.ok(Map.of(
                "message", "Registration successful. Please login."
        ));
    }
}