package com.kosmo.zipkok.config.interceptor;

import com.kosmo.zipkok.service.TokenService;
import com.kosmo.zipkok.util.CookieUtil;
import com.kosmo.zipkok.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * Redis 토큰 관리 서비스
     * 토큰 블랙리스트 확인과 Refresh Token 관리를 담당합니다.
     */
    private final TokenService tokenService;
    
    /**
     * JWT 토큰 생성 및 검증을 위한 유틸리티
     * Access Token과 Refresh Token을 생성하고 검증합니다.
     */
    private final JwtUtil jwtUtil;

    /**
     * LoginInterceptor 생성자
     * 
     * @param tokenService Redis 토큰 관리 서비스
     * @param jwtUtil JWT 토큰 유틸리티
     */
    public LoginInterceptor(TokenService tokenService, JwtUtil jwtUtil) {
        this.tokenService = tokenService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 컨트롤러 메서드 실행 전에 호출되는 메서드
     * 
     * 이 메서드에서 JWT Access Token의 유효성을 검사하고:
     * 1. 토큰이 없으면 로그인 페이지로 리다이렉트
     * 2. 토큰이 유효하지 않으면 쿠키 삭제 후 로그인 페이지로 리다이렉트
     * 3. 토큰이 블랙리스트에 등록되어 있으면 로그인 페이지로 리다이렉트
     * 4. 모든 검증을 통과하면 true 반환하여 컨트롤러 실행 허용
     * 
     * WebConfig의 excludePathPatterns에 정의된 경로는 이 인터셉터를 거치지 않습니다.
     * 
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param handler 실행될 컨트롤러 메서드
     * @return true이면 컨트롤러 실행 허용, false이면 실행 차단
     * @throws Exception 예외 발생 시
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 쿠키에서 JWT Access Token 추출
        // 로그인 시 쿠키에 저장된 토큰을 사용
        String token = CookieUtil.getCookieValue(request, "accessToken");

        System.out.println("인터셉터 token : " + token);
        System.out.println("요청 URL: " + request.getRequestURL());

        // 1단계: 토큰 존재 여부 확인
        if (token == null) {
            // 토큰이 없으면 로그인 페이지로 리다이렉트
            response.sendRedirect("/login");
            return false;
        }

        // 2단계: JWT Access Token 유효성 검사
        // 다음 사항들을 검사합니다:
        // - JWT 서명이 유효한지
        // - 토큰이 만료되지 않았는지
        // - 토큰 타입이 "access"인지
        if (!jwtUtil.validateToken(token) || !jwtUtil.isAccessToken(token)) {
            // 토큰이 유효하지 않으면 쿠키 삭제 후 로그인 페이지로 리다이렉트
            CookieUtil.deleteCookie("accessToken", "/", response);
            response.sendRedirect("/login");
            return false;
        }

        // 3단계: Redis 블랙리스트에서 토큰 확인 (로그아웃된 토큰인지)
        // Access Token은 JWT만으로 검증하지만, 로그아웃된 토큰은 무효화
        if (tokenService.isAccessTokenBlacklisted(token)) {
            // 블랙리스트에 등록된 토큰이면 쿠키 삭제 후 로그인 페이지로 리다이렉트
            CookieUtil.deleteCookie("accessToken", "/", response);
            response.sendRedirect("/login");
            return false;
        }

        // 모든 검증을 통과하면 컨트롤러 실행 허용
        return true;
    }


}