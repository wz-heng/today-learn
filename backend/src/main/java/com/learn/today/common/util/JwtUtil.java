package com.learn.today.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类
 * 使用 jjwt 0.12.x API，HS256 签名
 */
@Slf4j
@Component
public class JwtUtil {

    /** 至少 32 个字符，生产环境从环境变量注入 */
    @Value("${app.jwt.secret}")
    private String secret;

    /** 过期时间，单位秒，默认 7 天 */
    @Value("${app.jwt.expiration}")
    private long expiration;

    // -------------------------------------------------------
    // 生成 token
    // -------------------------------------------------------

    /**
     * 生成 JWT
     *
     * @param userId 用户 ID（存入 subject）
     * @param email  用户邮箱（存入 claim）
     */
    public String generateToken(String userId, String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration * 1000);

        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSecretKey())
                .compact();
    }

    // -------------------------------------------------------
    // 解析 token
    // -------------------------------------------------------

    /**
     * 从 token 中提取用户 ID
     *
     * @throws JwtException token 无效或已过期
     */
    public String getUserIdFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 从 token 中提取邮箱
     */
    public String getEmailFromToken(String token) {
        return parseClaims(token).get("email", String.class);
    }

    /**
     * 验证 token 是否有效（签名正确且未过期）
     *
     * @return true = 有效
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT 校验失败: {}", e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------
    // 私有方法
    // -------------------------------------------------------

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
