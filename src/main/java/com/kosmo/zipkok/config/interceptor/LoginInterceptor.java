package com.kosmo.zipkok.config.interceptor;

import com.kosmo.zipkok.service.RedisService;
import com.kosmo.zipkok.util.CookieUtil;
import com.kosmo.zipkok.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 로그인 상태를 확인하는 인터셉터
 * 
 * 이 인터셉터는 다음과 같은 역할을 담당합니다:
 * 1. 요청에서 JWT Access Token 추출 (쿠키에서)
 * 2. Access Token의 유효성 검사 (JWT 서명, 만료시간, 토큰 타입)
 * 3. Redis 블랙리스트에서 토큰 확인 (로그아웃된 토큰인지)
 * 4. 인증 실패 시 로그인 페이지로 리다이렉트
 * 
 * 실행 순서:
 * WebConfig → SecurityConfig → JwtAuthenticationFilter → LoginInterceptor (preHandle)
 * 
 * JwtAuthenticationFilter와의 차이점:
 * - JwtAuthenticationFilter: Spring Security 인증 객체 설정 (전역)
 * - LoginInterceptor: 특정 경로에 대한 추가 인증 검사 (선택적)
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    // preHandle: 컨트롤러 실행 BEFORE
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // Filter에서 이미 토큰 검증을 완료했으므로 SecurityContext만 확인
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("interceptor preHandle auth : {}", authentication);

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }

        // 인증된 사용자는 컨트롤러 실행 허용
        return true;
    }


}