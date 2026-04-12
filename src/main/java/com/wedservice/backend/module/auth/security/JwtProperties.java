package com.wedservice.backend.module.auth.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
// Lấy tất cả config bắt đầu bằng app.security.jwt ở application.yaml và map vào class này
@ConfigurationProperties(prefix = "app.security.jwt")
@Getter
@Setter
public class JwtProperties {
    private String secret;        // mã bí mật
    private long expiration;       // thời gian hết hạn của access token (ms)
    private long refreshExpiration; // thời gian hết hạn của refresh token (ms)
}
