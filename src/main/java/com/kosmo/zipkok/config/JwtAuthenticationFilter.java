package com.kosmo.zipkok.config;

import com.kosmo.zipkok.service.CustomUserDetailsService;
import com.kosmo.zipkok.service.SessionService;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 토큰을 검증하고 인증 정보를 설정하는 필터
 *
 * 이 필터는 모든 HTTP 요청에 대해 실행되며:
 * 1. 요청 헤더에서 JWT 토큰을 추출
 * 2. 토큰의 유효성을 검증
 * 3. Redis에서 세션 정보를 확인
 * 4. Spring Security에 인증 정보를 설정
 *
 * OncePerRequestFilter를 상속받아서 한 요청당 한 번만 실행되도록 보장
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;           // JWT 토큰 생성/검증 유틸리티

    @Autowired
    private SessionService sessionService;  // Redis 세션 관리 서비스

    @Autowired
    @Lazy // 지연 주입 (사용 시 생성)
    private CustomUserDetailsService userDetailsService;  // 사용자 정보 로드 서비스

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

    /**
     * 모든 HTTP 요청에 대해 실행되는 메서드
     *
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param filterChain 다음 필터로 요청을 전달하는 체인
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        String method = request.getMethod();
        System.out.println("요청 URL : " + method + " " + requestUri);

        // 1단계: 요청 헤더에서 JWT 토큰 추출
        String token = extractToken(request);
        System.out.println("token : " + token);

        // 2단계: 토큰이 존재하고 유효한 경우에만 인증 처리
        if (token != null && jwtUtil.validateToken(token)) {

            // 3단계: JWT 토큰에서 사용자명 추출
            String memberId = jwtUtil.getMemberIdFromToken(token);

            // 4단계: Redis에서 실제 세션 정보 확인 (이중 검증)
            // JWT는 만료되지 않았지만, Redis에서 삭제된 경우를 대비
            if (sessionService.getSession(token) != null) {

                // 5단계: 사용자 상세 정보 로드 (권한 정보 포함)
                UserDetails userDetails = userDetailsService.loadUserByUsername(memberId);

                // 6단계: Spring Security 인증 객체 생성
                // UsernamePasswordAuthenticationToken은 Spring Security가 인증된 사용자로 인식하는 객체
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,           // 사용자 정보
                        null,                  // 비밀번호 (JWT에서는 불필요)
                        userDetails.getAuthorities()  // 사용자 권한 목록
                    );

                // 7단계: Spring Security 컨텍스트에 인증 정보 설정
                // 이렇게 설정하면 @PreAuthorize, @Secured 등의 보안 어노테이션이 작동
                SecurityContextHolder.getContext().setAuthentication(authentication);

                System.out.println("JWT 인증 성공: " + memberId);
            } else {
                System.out.println("Redis에서 세션 정보를 찾을 수 없음: " + memberId);
            }
        } else if (token != null) {
            System.out.println("JWT 토큰이 유효하지 않음");

        }

        // 8단계: 다음 필터로 요청 전달 (인증 성공/실패와 관계없이)
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청 헤더에서 JWT 토큰을 추출하는 메서드
     *
     * 클라이언트는 보통 다음과 같이 토큰을 보냄:
     * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     *
     * @param request HTTP 요청 객체
     * @return JWT 토큰 문자열 (토큰이 없으면 null)
     */
    private String extractToken(HttpServletRequest request) {
        // Authorization 헤더에서 토큰 추출
        String bearerToken = request.getHeader("Authorization");

        // "Bearer " 접두사가 있고, 실제 토큰이 있는 경우
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            // "Bearer " 부분을 제거하고 실제 토큰만 반환
            return bearerToken.substring(7);
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("loginCookie".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}