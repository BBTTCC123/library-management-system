package com.bbttcc.library.security;

import com.bbttcc.library.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * JWT 生成与解析服务。
 */
@Slf4j
@Service
public class JwtService {

    private static final String CLAIM_USER_ID = "uid";
    private static final String CLAIM_ROLE = "role";

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        byte[] keyBytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret 长度必须至少为 32 字节（256 位）");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成访问令牌。
     */
    public String generateToken(SecurityUser user) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(properties.expirationMs());
        return Jwts.builder()
                .subject(user.getUsername())
                .issuer(properties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim(CLAIM_USER_ID, user.getId())
                .claim(CLAIM_ROLE, user.getRole().name())
                .signWith(signingKey)
                .compact();
    }

    public long expirationMs() {
        return properties.expirationMs();
    }

    /**
     * 解析并校验令牌，失败返回 {@code null}（由调用方决定如何处理）。
     */
    public Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(properties.issuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("JWT 校验失败: {}", ex.getMessage());
            return null;
        }
    }

    public String extractUsername(String token) {
        Claims claims = parse(token);
        return claims == null ? null : claims.getSubject();
    }
}
