package com.kosmo.zipkok.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT(JSON Web Token) 토큰을 생성하고 검증하는 유틸리티 클래스
 *
 * 이 클래스는 다음과 같은 기능을 제공합니다:
 * 1. Access Token 생성 (15분 만료) - API 요청 시 인증용
 * 2. Refresh Token 생성 (7일 만료) - Access Token 갱신용
 * 3. 토큰 유효성 검증
 * 4. 토큰에서 사용자 정보 추출
 * 5. 토큰 타입 구분 (access/refresh)
 *
 * JWT 구조: Header.Payload.Signature
 * - Header: 알고리즘 정보
 * - Payload: 사용자 정보, 만료시간, 토큰 타입 등
 * - Signature: 서명 (무결성 보장)
 */
@Component
public class JwtUtil {

    /*
     * JWT 서명에 사용할 비밀키
     */
    @Value("${jwt.secret:your-secret-key}")
    private String secret;

    /*
     * Access Token의 만료 시간 (밀리초)
     * 환경변수 jwt.access.expiration에서 가져오며, 기본값은 15분
     */
    @Value("${jwt.access.expiration:900000}") // 15분 (밀리초)
    private long accessExpiration;

    /*
     * Refresh Token의 만료 시간 (밀리초)
     * 환경변수 jwt.refresh.expiration에서 가져오며, 기본값은 7일
     */
    @Value("${jwt.refresh.expiration:604800000}") // 7일 (밀리초)
    private long refreshExpiration;

    /**
     * 문자열 형태의 비밀키를 HMAC-SHA256 알고리즘용 SecretKey 객체로 변환
     *
     * @return HMAC-SHA256 알고리즘용 SecretKey 객체
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * JWT 토큰을 파싱하고 Claims를 반환
     *
     * ⚠️ 이 메서드 하나만 JWT 파싱 책임을 가짐
     * - 서명 검증
     * - 만료 검증
     * - 위조 검증
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Access Token을 생성
    public String generateAccessToken(String memberSeq, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessExpiration);

        return Jwts.builder()
                .subject(memberSeq)          // 토큰 주인 (사용자 ID)
                .claim("type", "access")     // 토큰 타입 (access token임을 명시)
                .claim("role", role)         // 사용자 권한 (Spring Security에서 사용)
                .issuedAt(now)               // 토큰 발행 시간
                .expiration(expiryDate)      // 토큰 만료 시간 (15분 후)
                .signWith(getSigningKey())   // HMAC-SHA256으로 서명
                .compact();                  // 최종 JWT 문자열 생성
    }

    // Refresh Token을 생성합니다.
    public String generateRefreshToken(String memberSeq, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .subject(memberSeq)          // 토큰 주인 (사용자 ID)
                .claim("type", "refresh")    // 토큰 타입 (refresh token임을 명시)
                .claim("role", role)         // 사용자 권한 (Spring Security에서 사용)
                .issuedAt(now)               // 토큰 발행 시간
                .expiration(expiryDate)      // 토큰 만료 시간 (7일 후)
                .signWith(getSigningKey())   // HMAC-SHA256으로 서명
                .compact();                  // 최종 JWT 문자열 생성
    }

    public String getMemberSeqFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    // JWT 토큰에서 사용자 권한을 추출합니다.
    public String getRoleFromToken(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /**
     * JWT 토큰의 타입을 확인합니다.
     *
     * 토큰의 "type" 클레임에서 토큰 타입을 가져옵니다.
     * "access" 또는 "refresh" 중 하나를 반환합니다.
     *
     * @param token JWT 토큰 문자열
     * @return 토큰 타입 ("access" 또는 "refresh")
     * @throws JwtException 토큰 파싱 실패 시
     */
    public String getTokenType(String token) {
        return parseClaims(token).get("type", String.class);
    }

    // JWT 토큰의 만료 시간을 추출
    public Date getExpirationFromToken(String token) {
        return parseClaims(token).getExpiration();
    }

    /**
     * JWT 토큰의 유효성을 검사합니다.
     *
     * 다음 사항들을 검사합니다:
     * 1. 토큰 형식이 올바른지
     * 2. 서명이 유효한지
     * 3. 만료 시간이 지나지 않았는지
     *
     * @param token 검사할 JWT 토큰 문자열
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 토큰이 Access Token인지 확인
    public boolean isAccessToken(String token) {
        try {
            return "access".equals(getTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }

    // 토큰이 Refresh Token인지 확인
    public boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(getTokenType(token));
        } catch (Exception e) {
            return false;
        }
    }

    // SNS 로그인시 SNS 정보를 담을 임시 JWT를 5분 동안 발급한다
    public String tempSnsToken(String type, String snsId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 5 * 60 * 1000); // 5분 (300000ms)

        return Jwts.builder()
                .subject(snsId)              // 토큰 주인 (sns ID)
                .claim("tokenType", "temp")  // ⭐ 추가: 임시 토큰 타입 명시
                .claim("snsType", type)      // SNS 타입 (kakao, naver 등)
                .claim("snsId", snsId)       // SNS 고유 ID
                .issuedAt(now)               // 토큰 발행 시간
                .expiration(expiryDate)      // 토큰 만료 시간 (5분 후)
                .signWith(getSigningKey())   // HMAC-SHA256으로 서명
                .compact();                  // 최종 JWT 문자열 생성
    }

    public Map<String, String> validateTempToken(String token) {
        try {
            Claims claims = parseClaims(token);

            // tokenType 확인
            String tokenType = claims.get("tokenType", String.class);
            if (!"temp".equals(tokenType)) {
                throw new IllegalArgumentException("유효하지 않은 토큰 타입입니다.");
            }

            // SNS 정보 추출
            Map<String, String> snsInfo = new HashMap<>();
            snsInfo.put("snsId", claims.get("snsId", String.class));
            snsInfo.put("snsType", claims.get("snsType", String.class));

            return snsInfo;

        } catch (ExpiredJwtException e) {
            throw new RuntimeException("임시 토큰이 만료되었습니다. 다시 로그인해주세요.", e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("토큰이 유효하지 않습니다.", e);
        }
    }

    // token 만료여부 체크
    // (나중에 오류 여부를 좀 더 쉽게 파악할 수 있기 때문에
    // !validationToken을 사용하지 않고 isExpired를 사용)
    public boolean isExpired(String token) {
        try {
            parseClaims(token);
            return false; // 만료 안 됨
        } catch (ExpiredJwtException e) {
            return true;  // 만료됨
        } catch (JwtException | IllegalArgumentException e) {
            return false; // 위조/깨짐 → 재발급 대상 아님
        }
    }
}
