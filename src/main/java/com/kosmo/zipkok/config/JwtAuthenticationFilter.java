package com.kosmo.zipkok.config;

import com.kosmo.zipkok.service.CustomUserDetailsService;
import com.kosmo.zipkok.service.TokenService;
import com.kosmo.zipkok.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Access Token을 검증하고 인증 정보를 설정하는 필터
 *
 * 이 필터는 모든 HTTP 요청에 대해 실행되며 다음과 같은 역할을 담당합니다:
 * 1. 요청 헤더에서 JWT Access Token을 추출
 * 2. Access Token의 유효성을 검증 (서명, 만료시간, 토큰 타입)
 * 3. Redis 블랙리스트에서 토큰 확인 (로그아웃된 토큰인지)
 * 4. Spring Security 컨텍스트에 인증 정보를 설정
 *
 * 인증 플로우:
 * 요청 → 토큰 추출 → JWT 검증 → 블랙리스트 확인 → 사용자 정보 로드 → 인증 객체 생성 → SecurityContext 설정
 *
 * OncePerRequestFilter를 상속받아서 한 요청당 한 번만 실행되도록 보장합니다.
 * 이는 성능 최적화와 중복 실행 방지를 위한 것입니다.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;           

    /**
     * Redis 토큰 관리 서비스
     * 토큰 블랙리스트 확인과 Refresh Token 관리를 담당합니다.
     */
    @Autowired
    private TokenService tokenService;

    /**
     * 사용자 상세 정보를 로드하는 서비스
     * 데이터베이스에서 사용자 정보와 권한을 가져옵니다.
     * 
     * @Lazy 어노테이션을 사용하여 순환 참조 문제를 방지합니다.
     */
    @Autowired
    @Lazy 
    private CustomUserDetailsService userDetailsService;  

    // 필터를 적용하지 않을 경로를 정의
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

    /*
     * 모든 HTTP 요청에 대해 실행되는 핵심 인증 메서드
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        System.out.println("요청 URL : " + requestUri);

        // 1단계: 요청 헤더에서 JWT Access Token 추출
        // Authorization: Bearer {token} 형식 또는 쿠키에서 추출
        String token = extractToken(request);
        System.out.println("token : " + token);

        // 2단계: 토큰이 존재하고, 유효한 Access Token인 경우에만 인증 처리
        if (token != null && jwtUtil.validateToken(token) && jwtUtil.isAccessToken(token)) {

            // 3단계: Redis 블랙리스트에서 토큰 확인 (로그아웃된 토큰인지)
            // Access Token은 JWT만으로 검증하지만, 로그아웃된 토큰은 무효화
            if (!tokenService.isAccessTokenBlacklisted(token)) {

                // 4단계: JWT Access Token에서 사용자명과 권한 추출
                // JWT 자체에 포함된 정보를 사용하므로 Redis 조회 불필요
                String memberId = jwtUtil.getMemberIdFromToken(token);

                // 5단계: 사용자 상세 정보 로드 (권한 정보 포함)
                // 데이터베이스에서 최신 사용자 정보를 가져와 권한을 확인
                UserDetails userDetails = userDetailsService.loadUserByUsername(memberId);

                // 6단계: Spring Security 인증 객체 생성
                // UsernamePasswordAuthenticationToken은 Spring Security가 인증된 사용자로 인식하는 객체
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,           // 사용자 정보 (UserDetails 객체)
                        null,                  // 비밀번호 (JWT에서는 불필요하므로 null)
                        userDetails.getAuthorities()  // 사용자 권한 목록 (ROLE_ADMIN, ROLE_USER 등)
                    );

                // 7단계: Spring Security 컨텍스트에 인증 정보 설정
                // 이렇게 설정하면 @PreAuthorize, @Secured 등의 보안 어노테이션이 작동
                SecurityContextHolder.getContext().setAuthentication(authentication);

                System.out.println("JWT Access Token 인증 성공: " + memberId);
            } else {
                System.out.println("블랙리스트에 등록된 Access Token: " + token);
                // 블랙리스트에 등록된 토큰은 인증 실패로 처리
            }
        } else if (token != null) {
            // 토큰이 존재하지만 유효하지 않은 경우
            if (!jwtUtil.isAccessToken(token)) {
                System.out.println("Access Token이 아님 (Refresh Token 또는 잘못된 토큰)");
                // Refresh Token이 전송된 경우 Access Token이 필요함을 안내
            } else {
                System.out.println("JWT Access Token이 유효하지 않음");
                // JWT 서명이 잘못되었거나 만료된 경우
            }
        }

        // 8단계: 다음 필터로 요청 전달 (인증 성공/실패와 관계없이)
        // 인증에 실패해도 요청은 계속 진행되며, 이후 필터나 컨트롤러에서 처리
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청에서 JWT Access Token을 추출하는 메서드
     *
     * 토큰 추출 우선순위:
     * 1. Authorization 헤더: "Bearer {token}" 형식
     * 2. 쿠키: "accessToken" 이름의 쿠키 값
     *
     * @param request HTTP 요청 객체
     * @return JWT Access Token 문자열 (토큰이 없으면 null)
     */
    private String extractToken(HttpServletRequest request) {
        // 1순위: Authorization 헤더
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 2순위: accessToken 쿠키 (로그아웃과 일치)
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}