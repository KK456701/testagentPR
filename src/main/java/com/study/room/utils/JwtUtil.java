package com.study.room.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {
    
    // 生成安全密钥 (至少 256 位)
    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    // Token 有效期 (比如 12 小时)
    private static final long EXPIRATION = 12 * 60 * 60 * 1000L;

    /**
     * 生成 JWT Token
     */
    public static String createToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 JWT Token，获取 Claims
     */
    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    //asdasd5466556

    /**
     * 解析 JWT Token，获取 Claims
     */
    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ========== 新增功能 1 ==========
    /**
     * 从 JWT Token 中直接提取用户 ID
     * @param token JWT 字符串
     * @return 用户 ID
     */
    public static Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    // ========== 新增功能 2 ==========
    /**
     * 验证 JWT Token 是否有效（能解析且未过期）
     * @param token JWT 字符串
     * @return true-有效，false-无效
     */
    public static boolean isTokenValid(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            return !expiration.before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

        // ========== 新增功能 3 ==========
    /**
     * 获取 JWT Token 剩余有效时间（毫秒）
     * @param token JWT 字符串
     * @return 剩余毫秒数，若 token 无效或已过期则返回 -1
     */
    public static long getRemainingTime(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            long remaining = expiration.getTime() - System.currentTimeMillis();
            return remaining > 0 ? remaining : -1;
        } catch (Exception e) {
            return -1;
        }
    }
}
