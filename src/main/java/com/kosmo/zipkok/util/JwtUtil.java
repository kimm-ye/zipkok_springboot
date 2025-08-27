package com.kosmo.zipkok.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret:your-secret-key}")
    private String secret;
    
    @Value("${jwt.expiration:1800000}") // 30분 (밀리초)
    private long expiration;

    // 문자열 시크릿을 HMAC 알고리즘용 키 객체로 변환
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // 토큰 생성
    public String generateToken(String memberId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .subject(memberId)     // 토큰 주인 (보통 사용자 ID)
                .issuedAt(now)        // 발행시간
                .expiration(expiryDate) // 만료시간
                .signWith(getSigningKey()) // 서명
                .compact();           // 최종 토큰 문자열 생성
    }
    
    public String getMemberIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.getSubject();
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token); // 신규버전임
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
} 