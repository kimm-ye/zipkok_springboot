package com.kosmo.zipkok.config;

import com.kosmo.zipkok.config.interceptor.LoginInterceptor;
import com.kosmo.zipkok.service.TokenService;
import com.kosmo.zipkok.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 웹 설정 클래스
 * 
 * 이 클래스는 다음과 같은 웹 관련 설정을 담당합니다:
 * 1. CORS (Cross-Origin Resource Sharing) 설정
 * 2. 인터셉터 등록 및 경로 패턴 설정
 * 3. 정적 리소스 핸들러 설정
 * 
 * 전체 요청 처리 흐름:
 * 1. WebConfig (CORS) → 2. SecurityConfig (FilterChain) → 3. JwtAuthenticationFilter → 4. Interceptor (preHandle)
 * 
 * 각 단계별 역할:
 * - WebConfig: CORS 정책 적용, 인터셉터 등록
 * - SecurityConfig: Spring Security 필터 체인 구성
 * - JwtAuthenticationFilter: JWT 토큰 검증 및 인증 객체 설정
 * - LoginInterceptor: 추가적인 인증 검사 및 페이지 접근 제어
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Redis 토큰 관리 서비스
     * LoginInterceptor에서 토큰 블랙리스트 확인에 사용됩니다.
     */
    @Autowired
    private TokenService tokenService;
    
    /**
     * JWT 토큰 생성 및 검증을 위한 유틸리티
     * LoginInterceptor에서 JWT 토큰 유효성 검사에 사용됩니다.
     */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * CORS (Cross-Origin Resource Sharing) 설정
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowCredentials(true);        // 쿠키, Authorization 헤더 포함 허용
    }

    /**
     * 인터셉터를 등록하고 경로 패턴을 설정합니다.
     * 
     * 인터셉터 실행 순서:
     * 1. SecurityConfig의 필터 체인 통과
     * 2. JwtAuthenticationFilter에서 JWT 인증 처리
     * 3. LoginInterceptor에서 추가 인증 검사
     * 4. 컨트롤러 메서드 실행
     * 
     * @param registry 인터셉터를 등록하는 레지스트리
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // LoginInterceptor 등록
        // sessionService와 jwtUtil을 생성자로 주입하여 의존성 주입
        registry.addInterceptor(new LoginInterceptor(tokenService, jwtUtil))
                .addPathPatterns("/**")        // 모든 경로에 인터셉터 적용
                .excludePathPatterns(          // 인터셉터를 적용하지 않을 경로들
                    "/",                       // 메인 페이지
                    "/member/login/**",        // 로그인 관련 페이지
                    "/member/logout/**",       // 로그아웃 관련 페이지
                    "/member/join/**",         // 회원가입 관련 페이지
                    "/member/find/**",         // 아이디/비밀번호 찾기 페이지
                    "/resources/**",           // 정적 리소스
                    "/css/**",                 // CSS 파일
                    "/js/**",                  // JavaScript 파일
                    "/img/**",                 // 이미지 파일
                    "/webjars/**",             // WebJar 라이브러리
                    "/favicon.ico",            // 파비콘
                    "/error",                  // 에러 페이지
                    "/actuator/**"             // Spring Boot Actuator (모니터링)
                );
    }
}