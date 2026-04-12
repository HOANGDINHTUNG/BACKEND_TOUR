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

/*
    Class JwtService này là bộ xử lý token JWT của hệ thống bạn. Nó có 4 nhiệm vụ chính:
        Tạo token khi user đăng nhập thành công
        Đọc token để lấy thông tin bên trong
        Kiểm tra chữ ký token có bị sửa hay giả mạo không
        Kiểm tra token còn hạn hay hết hạn
 */

// Nghĩa là Spring coi đây là một bean thuộc tầng service.
@Service
@RequiredArgsConstructor
public class JwtService {

    /*
        Dùng để mã hoá Base64 URL-safe.

        JWT có cấu trúc:
            header.payload.signature

        Trong đó header và payload không để dạng JSON thô, mà phải encode thành Base64 URL-safe.

        Ví dụ:
            dấu +, /, = trong Base64 thường dễ gây rắc rối trong URL/header
            JWT dùng bản URL-safe để an toàn hơn
     */
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    // Ngược lại với encoder, cái này dùng để giải mã phần header/payload đã encode.
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    /*
        Đây là tên thuật toán ký token:HmacSHA256

        JWT của bạn đang dùng thuật toán HS256, tức là:
            HMAC
            SHA-256
            ký bằng secret key chung

        Nói dễ hiểu:
            Server dùng một secret bí mật để đóng dấu token.
            Sau này kiểm tra lại con dấu đó xem token có bị sửa không.
     */
    private static final String HMAC_SHA256 = "HmacSHA256";
    // Class này không hard-code secret trong code, mà lấy từ config
    private final JwtProperties jwtProperties;
    // JWT của bạn không dùng thư viện JWT chuyên dụng, mà tự build token bằng tay. Vì vậy cần ObjectMapper để chuyển đổi JSON.
    private final ObjectMapper objectMapper;

    // Đây là method tạo JWT access token cho user sau khi đăng nhập thành công.
    public String generateAccessToken(CustomUserDetails userDetails) {
        long issuedAt = Instant.now().toEpochMilli();
        long expiredAt = issuedAt + jwtProperties.getExpiration();

        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", userDetails.getUsername());
        payload.put("userId", userDetails.getUserId());
        payload.put("role", userDetails.getRole().name());
        payload.put("type", "access");
        payload.put("iat", issuedAt);
        payload.put("exp", expiredAt);

        String encodedHeader = encodeJson(header);
        String encodedPayload = encodeJson(payload);
        String signature = sign(encodedHeader + "." + encodedPayload);
        return encodedHeader + "." + encodedPayload + "." + signature;
    }

    /**
     * Tạo refresh token với TTL dài hơn access token.
     * Refresh token chứa claim "type":"refresh" để phân biệt với access token.
     */
    public String generateRefreshToken(CustomUserDetails userDetails) {
        long issuedAt = Instant.now().toEpochMilli();
        long expiredAt = issuedAt + jwtProperties.getRefreshExpiration();

        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", userDetails.getUsername());
        payload.put("userId", userDetails.getUserId());
        payload.put("type", "refresh");
        payload.put("iat", issuedAt);
        payload.put("exp", expiredAt);

        String encodedHeader = encodeJson(header);
        String encodedPayload = encodeJson(payload);
        String signature = sign(encodedHeader + "." + encodedPayload);
        return encodedHeader + "." + encodedPayload + "." + signature;
    }

    // Method này dùng để lấy sub(email) từ token.
    public String extractSubject(String token) {
        Object subject = extractAllClaims(token).get("sub");
        return subject == null ? null : subject.toString();
    }

    /** Trả về giá trị claim "type" (access | refresh). */
    public String extractTokenType(String token) {
        Object type = extractAllClaims(token).get("type");
        return type == null ? "access" : type.toString();
    }

    // Kiểm tra access token hợp lệ — refresh token bị loại bỏ.
    public boolean isTokenValid(String token, UserDetails userDetails) {
        Map<String, Object> claims = extractAllClaims(token);
        String subject = claims.get("sub") == null ? null : claims.get("sub").toString();
        long expirationTime = toLong(claims.get("exp"));
        String tokenType = claims.get("type") == null ? "access" : claims.get("type").toString();

        return subject != null
                && subject.equalsIgnoreCase(userDetails.getUsername())
                && expirationTime > Instant.now().toEpochMilli()
                && "access".equals(tokenType); // refresh token không được dùng như access token
    }

    /** Kiểm tra refresh token hợp lệ (chưa hết hạn và đúng type). */
    public boolean isRefreshTokenValid(String token) {
        try {
            Map<String, Object> claims = extractAllClaims(token);
            long expirationTime = toLong(claims.get("exp"));
            String tokenType = claims.get("type") == null ? "" : claims.get("type").toString();
            return "refresh".equals(tokenType) && expirationTime > Instant.now().toEpochMilli();
        } catch (Exception e) {
            return false;
        }
    }

    // Method này chỉ trả lại thời gian sống của access token từ config
    public long getExpiration() {
        return jwtProperties.getExpiration();
    }

    // Trả lại thời gian sống của refresh token từ config
    public long getRefreshExpiration() {
        return jwtProperties.getRefreshExpiration();
    }

    // Đây là method rất trung tâm. Nó làm việc đọc toàn bộ payload từ token.
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

    // Đây là chỗ kiểm tra token có bị sửa hay không.
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

    // Đây là method tạo chữ ký số cho token.
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

    // Methor dùng để tạo token
    private String encodeJson(Map<String, Object> value) {
        try {
            byte[] jsonBytes = objectMapper.writeValueAsBytes(value);
            return BASE64_URL_ENCODER.encodeToString(jsonBytes);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot serialize JWT content", ex);
        }
    }

    // Tách token để check
    private String[] splitToken(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT token format");
        }
        return parts;
    }

    // Method này dùng để ép dữ liệu claim sang long.
    private long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }
}
