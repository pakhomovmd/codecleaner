package ru.ssau.codecleaner.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        // Устанавливаем JWT_SECRET для тестов
        System.setProperty("JWT_SECRET", "test-secret-key-with-minimum-32-characters-required");
        tokenService = new TokenService();
    }

    @Test
    void testCreateAccessToken() {
        // Arrange
        Long userId = 1L;
        String email = "test@example.com";
        List<String> roles = List.of("ROLE_VIEWER");

        // Act
        String token = tokenService.createAccessToken(userId, email, roles);

        // Assert
        assertNotNull(token);
        assertTrue(token.contains("."));
        String[] parts = token.split("\\.");
        assertEquals(2, parts.length);
    }

    @Test
    void testVerifyValidToken() {
        // Arrange
        Long userId = 1L;
        String email = "test@example.com";
        List<String> roles = List.of("ROLE_ADMIN");
        String token = tokenService.createAccessToken(userId, email, roles);

        // Act
        Map<String, Object> payload = tokenService.verifyToken(token);

        // Assert
        assertNotNull(payload);
        assertEquals(userId.intValue(), ((Number) payload.get("userId")).intValue());
        assertEquals(email, payload.get("email"));
        assertNotNull(payload.get("roles"));
        assertNotNull(payload.get("exp"));
    }

    @Test
    void testVerifyInvalidTokenFormat() {
        // Arrange
        String invalidToken = "invalid.token.format";

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            tokenService.verifyToken(invalidToken);
        });
    }

    @Test
    void testCreateRefreshToken() {
        // Arrange
        Long userId = 2L;

        // Act
        String refreshToken = tokenService.createRefreshToken(userId);

        // Assert
        assertNotNull(refreshToken);
        assertTrue(refreshToken.contains("."));
        
        Map<String, Object> payload = tokenService.verifyToken(refreshToken);
        assertEquals(userId.intValue(), ((Number) payload.get("userId")).intValue());
    }

    @Test
    void testTokenExpiration() {
        // Arrange - создаём токен с истёкшим сроком
        long pastTime = System.currentTimeMillis() / 1000 - 1000; // 1000 секунд назад
        Map<String, Object> payload = Map.of(
            "userId", 1,
            "email", "test@example.com",
            "exp", pastTime
        );
        String expiredToken = tokenService.generateToken(payload);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tokenService.verifyToken(expiredToken);
        });
        assertTrue(exception.getMessage().contains("Token expired") || 
                   exception.getMessage().contains("Token verification failed"));
    }
}
