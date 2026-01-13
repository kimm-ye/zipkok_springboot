package com.kosmo.zipkok.config;

import com.kosmo.zipkok.dto.CustomUserDetail;
import com.kosmo.zipkok.service.RedisService;
import com.kosmo.zipkok.util.CookieUtil;
import com.kosmo.zipkok.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * 성능 최적화된 JWT 인증 필터 (DB 조회 제거)
 *
 * 핵심 개선: userDetailsService.loadUserByUsername() 호출 제거
 * - 기존: 매 요청마다 DB 조회 (50-100ms)
 * - 개선: JWT에서 정보 추출 (5-10ms)
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisService redisService;

    @Value("${jwt.access.expiration}") // 15분 (밀리초)
    private long accessExpiration;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.contains("/css/") ||
                path.contains("/js/") ||
                path.contains("/img/") ||
                path.contains("/favicon.ico") ||
                path.contains("/resources/") ||
                path.contains("/webjars/") ||
                path.endsWith(".css") ||
                path.endsWith(".js") ||
                path.endsWith(".png") ||
                path.endsWith(".jpg");
    }

    // TODO 권한을 복수권한으로 할지 좀 더 생각해봐야함
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1️⃣ 이미 인증된 경우 스킵
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = extractToken(request, "accessToken");
        String refreshToken = extractToken(request, "refreshToken");

        // 토큰이 없으면 바로 통과
        if (accessToken == null && refreshToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2️⃣ Access Token 처리 (유효한 경우)
            if (accessToken != null
                    && jwtUtil.validateToken(accessToken)
                    && jwtUtil.isAccessToken(accessToken)) {

                // 블랙리스트 체크 (// TODO 해당 내용 테스트 필요
                if (!redisService.isAccessTokenBlacklisted(accessToken)) {
                    // DB 조회 없이 JWT에서 직접 정보 추출
                    String memberSeq = jwtUtil.getMemberSeqFromToken(accessToken);
                    String role = jwtUtil.getRoleFromToken(accessToken);

                    setAuthenticationFromJwt(memberSeq, role);
                    log.debug("✅ Access Token 인증 성공 (DB 조회 X) memberSeq : {}", memberSeq);
                }

            // 3️⃣ Access Token 만료 → Refresh Token으로 갱신
            } else if ((accessToken == null || jwtUtil.isExpired(accessToken))
                    && refreshToken != null
                    && jwtUtil.validateToken(refreshToken)) {

                String memberSeq = jwtUtil.getMemberSeqFromToken(refreshToken);

                // Redis에서 Refresh Token 검증
                Boolean isValid = redisService.isValidRefreshToken(memberSeq, refreshToken);

                if(isValid == null) {
                    log.warn("❌ Redis 서버 오류 - Redis 서버와 비교 인증 스킵");
                } else if (isValid) {
                    String role = jwtUtil.getRoleFromToken(refreshToken);

                    // 새 Access Token 발급
                    String newAccessToken = jwtUtil.generateAccessToken(memberSeq, role);
                    CookieUtil.createCookie("accessToken", accessExpiration, "/", newAccessToken, response);

                    // ⚡ 핵심: DB 조회 없이 JWT에서 직접 인증
                    setAuthenticationFromJwt(memberSeq, role);
                    log.info("✅ Access Token 자동 갱신 완료 (DB 조회 X) memberSeq: {}", memberSeq);
                } else {
                    log.warn("❌ 유효하지 않은 Refresh Token - 쿠키 삭제");
                    CookieUtil.deleteCookie("refreshToken", "/", response); // TODO 이부분을 어떻게 해야할지 해결 필요 << 일단 redis 서버 오류부터 고치고 수정하자
                }
            }

        } catch (Exception e) {
            log.error("인증 처리 중 오류: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * DB 조회 없이 JWT 정보만으로 인증으로 수정 - 속도개선
     *
     * 기존 방식 (느림):
     * UserDetails userDetails = userDetailsService.loadUserByUsername(memberSeq);
     * → DB 쿼리 실행 (50-100ms)
     *
     * 개선 방식 (빠름):
     * JWT에서 직접 추출 (5-10ms)
     * → memberName, memberStatus는 null (필요한 컨트롤러에서만 추가 조회)
     */
    private void setAuthenticationFromJwt(String memberSeq, String role) {
        // 경량 CustomUserDetail 생성 (DB 조회 X)
        CustomUserDetail userDetails = new CustomUserDetail(
                memberSeq,              // JWT에서는 memberSeq 사용
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * 쿠키 또는 헤더에서 토큰 추출
     */
    private String extractToken(HttpServletRequest request, String tokenName) {
        // Authorization 헤더 (모바일용 - accessToken만)
        if ("accessToken".equals(tokenName)) {
            String bearerToken = request.getHeader("Authorization");
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }
        }

        // 쿠키에서 추출
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (tokenName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}