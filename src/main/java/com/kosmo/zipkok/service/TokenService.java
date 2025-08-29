package com.kosmo.zipkok.service;

import com.kosmo.zipkok.dto.MemberDTO;
import com.kosmo.zipkok.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * JWT 토큰과 Redis를 연동하여 세션을 관리하는 서비스 클래스
 * 
 * 이 클래스는 다음과 같은 역할을 담당합니다:
 * 1. 로그인 시 Access Token과 Refresh Token 생성
 * 2. Refresh Token을 Redis에 저장 (장기 보관)
 * 3. Refresh Token 유효성 검사
 * 4. 로그아웃된 토큰을 블랙리스트에 추가
 * 5. 사용자별 토큰 관리
 * 
 * Redis 활용 전략:
 * - Access Token: JWT만으로 검증 (stateless)
 * - Refresh Token: Redis에 저장하여 관리
 * - 블랙리스트: 로그아웃된 토큰 관리
 */
@Service
public class TokenService {

    /**
     * Redis 데이터베이스와 상호작용하기 위한 템플릿
     * RedisTemplate을 사용하여 다양한 데이터 타입을 Redis에 저장/조회할 수 있습니다.
     */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 로그인 시 Access Token과 Refresh Token을 생성하고 Redis에 저장합니다.
     * 
     * 생성 과정:
     * 1. JwtUtil을 사용하여 Access Token (15분)과 Refresh Token (7일) 생성
     * 2. Refresh Token만 Redis에 저장 (Access Token은 stateless)
     * 3. 사용자별 활성 Refresh Token 목록 관리
     * 
     * Redis 키 구조:
     * - "refresh:{memberId}": 사용자의 Refresh Token
     * - "user_refresh:{memberId}": 사용자별 활성 Refresh Token 목록
     * 
     * @param memberDTO 로그인한 사용자 정보
     * @return 생성된 Access Token과 Refresh Token을 포함한 TokenResponse 객체
     */
    public TokenResponse createTokens(MemberDTO memberDTO) {
        // Access Token과 Refresh Token 생성
        String accessToken = jwtUtil.generateAccessToken(memberDTO.getMemberId(), memberDTO.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(memberDTO.getMemberId());

        // Refresh Token만 Redis에 저장 (장기 보관, 7일)
        redisTemplate.opsForValue().set(
                "refresh:" + memberDTO.getMemberId(),
                refreshToken,
                7,  // 7일
                TimeUnit.DAYS
        );

        // 사용자별 활성 Refresh Token 목록 관리 (동시 로그인 제한 등에 활용)
        //redisTemplate.opsForSet().add("user_refresh:" + memberDTO.getMemberId(), refreshToken);
        //redisTemplate.expire("user_refresh:" + memberDTO.getMemberId(), 7, TimeUnit.DAYS);

        System.out.println("Access Token 및 Refresh Token 생성 완료");
        return new TokenResponse(accessToken, refreshToken);
    }

     //Refresh Token의 유효성을 검사합니다.
    public boolean isValidRefreshToken(String memberId, String refreshToken) {
        // 1단계: JWT 유효성 검사 (서명, 만료시간 등)
        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            return false;
        }

        // 2단계: Redis에서 저장된 Refresh Token과 비교
        // 이는 사용자가 로그아웃했거나 다른 기기에서 로그인했을 때를 대비한 검증
        String storedToken = (String) redisTemplate.opsForValue().get("refresh:" + memberId);
        return refreshToken.equals(storedToken);
    }

    /**
     * Access Token이 블랙리스트에 등록되어 있는지 확인합니다.
     *
     * Access Token은 JWT만으로 검증하지만, 로그아웃된 토큰은
     * 만료 시간까지 블랙리스트에 보관하여 무효화합니다.
     *
     * @param token 확인할 Access Token
     * @return 블랙리스트에 등록되어 있으면 true, 그렇지 않으면 false
     */
    public boolean isAccessTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:access:" + token));
    }

    /**
     * Refresh Token이 블랙리스트에 등록되어 있는지 확인합니다.
     * 
     * Refresh Token도 로그아웃 시 블랙리스트에 추가되어
     * 만료 시간까지 무효화됩니다.
     * 
     * @param token 확인할 Refresh Token
     * @return 블랙리스트에 등록되어 있으면 true, 그렇지 않으면 false
     */
    public boolean isRefreshTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:refresh:" + token));
    }

    /**
     * 토큰을 블랙리스트에 추가합니다.
     * 
     * 로그아웃 시 호출되며, 토큰의 만료 시간까지 블랙리스트에 보관합니다.
     * 이는 토큰이 만료되기 전까지는 무효화된 상태를 유지하기 위함입니다.
     * 
     * 블랙리스트 키 구조:
     * - "blacklist:access:{token}": Access Token 블랙리스트
     * - "blacklist:refresh:{token}": Refresh Token 블랙리스트
     * 
     * @param token 블랙리스트에 추가할 토큰
     */
    public void blacklistToken(String token) {
        if (jwtUtil.isAccessToken(token)) {
            // Access Token을 블랙리스트에 추가 (만료 시간까지)
            Date expiration = jwtUtil.getExpirationFromToken(token);
            long ttl = expiration.getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                redisTemplate.opsForValue().set(
                    "blacklist:access:" + token,
                    "revoked",
                    ttl,
                    TimeUnit.MILLISECONDS
                );
            }
        } else if (jwtUtil.isRefreshToken(token)) {
            // Refresh Token을 블랙리스트에 추가 (만료 시간까지)
            Date expiration = jwtUtil.getExpirationFromToken(token);
            long ttl = expiration.getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                redisTemplate.opsForValue().set(
                    "blacklist:refresh:" + token,
                    "revoked",
                    ttl,
                    TimeUnit.MILLISECONDS
                );
            }
        }
    }

    /**
     * 사용자의 모든 토큰을 삭제합니다.
     * 
     * 로그아웃 시 호출되며, 다음 작업을 수행합니다:
     * 1. Redis에 저장된 Refresh Token 삭제
     * 2. 사용자별 활성 Refresh Token 목록 삭제
     * 3. 관련된 블랙리스트 항목들 삭제
     * 
     * 이는 사용자가 모든 기기에서 로그아웃되도록 보장합니다.
     * 
     * @param memberId 로그아웃할 사용자 ID
     */
    public void deleteAllUserTokens(String memberId) {
        // Refresh Token 삭제
        redisTemplate.delete("refresh:" + memberId);
        
        // 사용자별 활성 Refresh Token 목록 삭제
        Set<Object> refreshTokens = redisTemplate.opsForSet().members("user_refresh:" + memberId);
        if (refreshTokens != null) {
            for (Object token : refreshTokens) {
                redisTemplate.delete("blacklist:refresh:" + token);
            }
            redisTemplate.delete("user_refresh:" + memberId);
        }
    }

    /**
     * 토큰 응답을 위한 내부 DTO 클래스
     * 
     * 로그인 성공 시 클라이언트에게 반환할 Access Token과 Refresh Token을
     * 하나의 객체로 묶어서 전달합니다.
     */
    public static class TokenResponse {
        private String accessToken;    // API 요청 시 사용할 Access Token (15분 만료)
        private String refreshToken;   // 토큰 갱신 시 사용할 Refresh Token (7일 만료)

        /**
         * TokenResponse 생성자
         * 
         * @param accessToken 생성된 Access Token
         * @param refreshToken 생성된 Refresh Token
         */
        public TokenResponse(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        // Getter 메서드들
        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
    }
}