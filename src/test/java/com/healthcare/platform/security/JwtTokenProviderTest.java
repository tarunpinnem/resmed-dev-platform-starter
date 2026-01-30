package com.healthcare.platform.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JWT Token Provider Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_SECRET = "your-256-bit-secret-key-for-jwt-signing-which-should-be-at-least-256-bits-long";
    private static final long EXPIRATION_MS = 86400000L;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", EXPIRATION_MS);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtIssuer", "healthcare-platform");
        jwtTokenProvider.init();
    }

    @Test
    @DisplayName("should generate valid token")
    void shouldGenerateValidToken() {
        String token = jwtTokenProvider.generateToken("testuser", List.of("USER", "ADMIN"));

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    @DisplayName("should extract username from token")
    void shouldExtractUsernameFromToken() {
        String token = jwtTokenProvider.generateToken("testuser", List.of("USER"));

        String username = jwtTokenProvider.getUsernameFromToken(token);

        assertThat(username).isEqualTo("testuser");
    }

    @Test
    @DisplayName("should validate valid token")
    void shouldValidateValidToken() {
        String token = jwtTokenProvider.generateToken("testuser", List.of("USER"));

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("should reject invalid token")
    void shouldRejectInvalidToken() {
        String invalidToken = "invalid.token.here";

        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("should reject empty token")
    void shouldRejectEmptyToken() {
        boolean isValid = jwtTokenProvider.validateToken("");

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("should get authentication from token")
    void shouldGetAuthenticationFromToken() {
        String token = jwtTokenProvider.generateToken("testuser", List.of("USER", "ADMIN"));

        Authentication authentication = jwtTokenProvider.getAuthentication(token);

        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo("testuser");
        assertThat(authentication.getAuthorities()).hasSize(2);
    }

    @Test
    @DisplayName("should return correct expiration time")
    void shouldReturnCorrectExpirationTime() {
        long expiration = jwtTokenProvider.getExpirationMs();

        assertThat(expiration).isEqualTo(EXPIRATION_MS);
    }
}
