package com.example.movies_recommendation_engine.config;

import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    // @BeforeEach — runs BEFORE every @Test method.
    // This ensures each test starts with a fresh JwtUtils instance.
    // Without this, state from one test could leak into another.
    @BeforeEach
    void setUp() {
        // JwtUtils expects a Base64-encoded secret (same format as your .env JWT_SECRET).
        // We generate a proper 256-bit key here for testing.
        byte[] keyBytes = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256).getEncoded();
        String base64Secret = Base64.getEncoder().encodeToString(keyBytes);
        jwtUtils = new JwtUtils(base64Secret);
    }

    @Test
    void generateToken_shouldReturnNonNullToken() {
        String token = jwtUtils.generateToken("test@example.com", "USER");

        // assertNotNull — fails the test if the value is null
        assertNotNull(token);
        // A JWT has 3 parts separated by dots: header.payload.signature
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void getEmail_shouldReturnCorrectEmail() {
        String token = jwtUtils.generateToken("test@example.com", "USER");

        String email = jwtUtils.getEmail(token);

        assertEquals("test@example.com", email);
    }

    @Test
    void getRole_shouldReturnCorrectRole() {
        String token = jwtUtils.generateToken("test@example.com", "ADMIN");

        String role = jwtUtils.getRole(token);

        assertEquals("ADMIN", role);
    }

    @Test
    void getEmail_withGarbageToken_shouldThrowException() {
        // assertThrows — verifies that calling getEmail with a bad token
        // throws RuntimeException (your code catches JWT exceptions and re-throws)
        assertThrows(RuntimeException.class, () -> jwtUtils.getEmail("not.a.valid-token"));
    }

    @Test
    void differentEmails_shouldProduceDifferentTokens() {
        String token1 = jwtUtils.generateToken("user1@example.com", "USER");
        String token2 = jwtUtils.generateToken("user2@example.com", "USER");

        // assertNotEquals — tokens for different users must be different
        assertNotEquals(token1, token2);
    }
}
