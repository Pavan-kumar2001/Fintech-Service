package com.fintech.fintech_service.service;

import com.fintech.fintech_service.dto.auth.AuthRequest;
import com.fintech.fintech_service.dto.user.UserRequest;
import com.fintech.fintech_service.security.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final UserService userService;

    public Map<String, Object> login(AuthRequest authRequest) {
        log.info("Login attempt for mobile: {}", authRequest.getMobile());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getMobile(),
                        authRequest.getPassword()
                )
        );

        String token = jwtUtil.generateToken(authRequest.getMobile());
        log.info("Login successful — token generated for mobile: {}",
                authRequest.getMobile());
        return Map.of(
                "message", "Login successful",
                "token", token
        );
    }

    public Map<String, Object> register(UserRequest userRequest) {
        log.info("Registration attempt for mobile: {}", userRequest.getMobile());
        userService.create(userRequest);
        log.info("Registration successful for mobile: {}", userRequest.getMobile());
        return Map.of(
                "message", "Registration successful. Please login."
        );
    }
}
