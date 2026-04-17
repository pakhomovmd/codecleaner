package ru.ssau.codecleaner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TokenService {

    private final String secret;
    private final ObjectMapper objectMapper;

    public TokenService() {
        this.secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET environment variable not set or too short (need 32+ chars)");
        }
        this.objectMapper = new ObjectMapper();
    }

    public String generateToken(Map<String, Object> payload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            String encodedPayload = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(jsonPayload.getBytes());

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            mac.init(keySpec);
            byte[] signatureBytes = mac.doFinal(encodedPayload.getBytes());

            String encodedSignature = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(signatureBytes);

            return encodedPayload + "." + encodedSignature;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate token", e);
        }
    }

    public Map<String, Object> verifyToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 2) {
                throw new RuntimeException("Invalid token format");
            }

            String encodedPayload = parts[0];
            String encodedSignature = parts[1];

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            mac.init(keySpec);
            byte[] expectedSignature = mac.doFinal(encodedPayload.getBytes());
            String expectedEncodedSignature = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(expectedSignature);

            if (!expectedEncodedSignature.equals(encodedSignature)) {
                throw new RuntimeException("Invalid signature");
            }

            byte[] decodedPayload = Base64.getUrlDecoder().decode(encodedPayload);
            String jsonPayload = new String(decodedPayload);

            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(jsonPayload, Map.class);

            Long exp = ((Number) payload.get("exp")).longValue();
            long now = System.currentTimeMillis() / 1000;

            if (exp < now) {
                throw new RuntimeException("Token expired");
            }

            return payload;

        } catch (Exception e) {
            throw new RuntimeException("Token verification failed: " + e.getMessage(), e);
        }
    }

    public String createAccessToken(Long userId, String email, List<String> roles) {
        long now = System.currentTimeMillis() / 1000;
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("email", email);
        payload.put("roles", roles);
        payload.put("iat", now);
        payload.put("exp", now + 15 * 60);  // 15 минут
        return generateToken(payload);
    }

    public String createRefreshToken(Long userId) {
        long now = System.currentTimeMillis() / 1000;
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("iat", now);
        payload.put("exp", now + 7 * 24 * 60 * 60);  // 7 дней
        return generateToken(payload);
    }
}
