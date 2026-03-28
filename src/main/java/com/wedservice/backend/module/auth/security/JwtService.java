package com.wedservice.backend.module.auth.security;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    private static final String HMAC_SHA256 = "HmacSHA256";

    private final JwtProperties jwtProperties;
    private final ObjectMapper objectMapper;

    public String generateAccessToken(CustomUserDetails userDetails) {
        long issuedAt = Instant.now().toEpochMilli();
        long expiredAt = issuedAt + jwtProperties.getExpiration();

        Map<String, Object> header = Map.of(
                "alg", "HS256",
                "typ", "JWT"
        );

        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", userDetails.getUsername());
        payload.put("userId", userDetails.getUserId());
        payload.put("role", userDetails.getRole().name());
        payload.put("iat", issuedAt);
        payload.put("exp", expiredAt);

        String encodedHeader = encodeJson(header);
        String encodedPayload = encodeJson(payload);
        String signature = sign(encodedHeader + "." + encodedPayload);

        return encodedHeader + "." + encodedPayload + "." + signature;
    }

    public String extractSubject(String token) {
        Object subject = extractAllClaims(token).get("sub");
        return subject == null ? null : subject.toString();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        Map<String, Object> claims = extractAllClaims(token);
        String subject = claims.get("sub") == null ? null : claims.get("sub").toString();
        long expirationTime = toLong(claims.get("exp"));

        return subject != null
                && subject.equalsIgnoreCase(userDetails.getUsername())
                && expirationTime > Instant.now().toEpochMilli();
    }

    public long getExpiration() {
        return jwtProperties.getExpiration();
    }

    public Map<String, Object> extractAllClaims(String token) {
        String[] parts = splitToken(token);
        validateSignature(parts);

        try {
            byte[] decodedPayload = BASE64_URL_DECODER.decode(parts[1]);
            return objectMapper.readValue(decodedPayload, new TypeReference<>() {
            });
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid JWT payload", ex);
        }
    }

    private void validateSignature(String[] parts) {
        String signingInput = parts[0] + "." + parts[1];
        String expectedSignature = sign(signingInput);

        boolean valid = MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                parts[2].getBytes(StandardCharsets.UTF_8)
        );

        if (!valid) {
            throw new IllegalArgumentException("Invalid JWT signature");
        }
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8),
                    HMAC_SHA256
            );
            mac.init(secretKeySpec);
            byte[] signatureBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return BASE64_URL_ENCODER.encodeToString(signatureBytes);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot sign JWT token", ex);
        }
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            byte[] jsonBytes = objectMapper.writeValueAsBytes(value);
            return BASE64_URL_ENCODER.encodeToString(jsonBytes);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot serialize JWT content", ex);
        }
    }

    private String[] splitToken(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT token format");
        }
        return parts;
    }

    private long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }
}
