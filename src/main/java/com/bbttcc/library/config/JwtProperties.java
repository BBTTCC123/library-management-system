package com.bbttcc.library.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 相关配置，对应 {@code app.jwt.*}。
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        long expirationMs,
        String issuer
) {
}
