package com.healthcare.platform.controller;

import com.healthcare.platform.dto.ApiResponse;
import com.healthcare.platform.dto.AuthRequest;
import com.healthcare.platform.dto.AuthResponse;
import com.healthcare.platform.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Authentication controller.
 * Provides login endpoint for obtaining JWT tokens.
 * 
 * NOTE: This is a simplified authentication for demo purposes.
 * In production, integrate with a proper user store and authentication mechanism.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final JwtTokenProvider tokenProvider;

    // Demo users - In production, use a proper user store
    private static final Map<String, DemoUser> DEMO_USERS = Map.of(
            "admin", new DemoUser("admin", "admin123", List.of("ADMIN", "USER")),
            "user", new DemoUser("user", "user123", List.of("USER")),
            "doctor", new DemoUser("doctor", "doctor123", List.of("DOCTOR", "USER")),
            "nurse", new DemoUser("nurse", "nurse123", List.of("NURSE", "USER"))
    );

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Authenticates a user and returns a JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        // Validate credentials (demo implementation)
        DemoUser user = DEMO_USERS.get(request.getUsername());
        if (user == null || !user.password.equals(request.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        // Generate token
        String token = tokenProvider.generateToken(user.username, user.roles);

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationMs() / 1000)
                .username(user.username)
                .roles(user.roles)
                .build();

        log.info("Login successful for user: {}", request.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Authentication successful", authResponse));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Issues a new JWT token (placeholder endpoint)")
    public ResponseEntity<ApiResponse<String>> refresh() {
        // Placeholder - In production, implement proper token refresh logic
        return ResponseEntity.ok(ApiResponse.success("Token refresh not implemented in demo", null));
    }

    /**
     * Demo user record for simplified authentication.
     */
    private record DemoUser(String username, String password, List<String> roles) {}
}
