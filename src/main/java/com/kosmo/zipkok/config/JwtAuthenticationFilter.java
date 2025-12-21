package com.kosmo.zipkok.config;

import com.kosmo.zipkok.service.CustomUserDetailsService;
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
 *
 * 요청 → JwtAuthenticationFilter (토큰 검증 + 갱신 + 인증 설정)
 *      → SecurityConfig (권한 체크)
 *      → Controller
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;           

    /**
     * Redis 토큰 관리 서비스
     * 토큰 블랙리스트 확인과 Refresh Token 관리를 담당합니다.
     */
    @Autowired
    private RedisService redisService;

    @Autowired
    @Lazy //  @Lazy 어노테이션을 사용하여 순환 참조 임시 방지
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

        // 1단계: 요청 헤더에서 JWT access, refresh Token 추출
        // Authorization: Bearer {token} 형식 또는 쿠키에서 추출
        String accessToken = extractToken(request);
        String refreshToken = extractRefreshToken(request);

        log.info("accessToken : {}",  accessToken);
        log.info("refreshToken : {}",  refreshToken);

        // 2단계: 토큰이 존재하고, 유효한 Access Token인 경우에만 인증 처리
        if (accessToken != null && jwtUtil.validateToken(accessToken) && jwtUtil.isAccessToken(accessToken)) {

            // TODO 이 부분 블랙리스트 되는지 한번 더 확인해야함
            /*
            3단계: Redis 블랙리스트에서 토큰 확인 (로그아웃된 토큰인지)
            Access Token은 JWT만으로 검증하지만, 로그아웃된 토큰은 무효화 => 이걸 블랙리스트라고 한다.

            ### 실제 문제 상황
                ```
                1. 사용자가 로그인 → Access Token 발급 (15분 유효)
                2. 5분 후 사용자가 로그아웃
                3. ❌ 문제: 남은 10분 동안 Access Token은 여전히 유효!
                4. 누군가 그 토큰을 탈취했다면? → 10분 동안 계속 사용 가능!
             */
            if (!redisService.isAccessTokenBlacklisted(accessToken)) {

                // 4단계: JWT Access Token에서 사용자명과 권한 추출
                // JWT 자체에 포함된 정보를 사용하므로 Redis 조회 불필요
                String memberSeq = jwtUtil.getMemberSeqFromToken(accessToken);

                // 5단계: 사용자 상세 정보 로드 (권한 정보 포함)
                // 데이터베이스에서 최신 사용자 정보를 가져와 권한을 확인
                UserDetails userDetails = userDetailsService.loadUserByUsername(memberSeq);

                // 6단계: Spring Security 인증 객체 생성
                // UsernamePasswordAuthenticationToken은 Spring Security가 인증된 사용자로 인식하는 객체
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,           // 사용자 정보 (UserDetails 객체)
                                null,                  // 비밀번호 (JWT에서는 불필요하므로 null)
                                userDetails.getAuthorities()  // 사용자 권한 목록 (ROLE_ADMIN, ROLE_USER 등)
                        );

                // 7단계: Spring Security 컨텍스트에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        // Access Token이 만료되었지만 Refresh Token이 유효한 경우
        } else if (accessToken == null && !jwtUtil.validateToken(accessToken)
                    && refreshToken != null && jwtUtil.validateToken(refreshToken)) {

            String memberSeq = jwtUtil.getMemberSeqFromToken(refreshToken);

            // Redis에서 Refresh Token 확인
            boolean isValidRefresh = redisService.isValidRefreshToken(memberSeq, refreshToken);
            System.out.println("isValidRefresh================" + isValidRefresh);

            if (isValidRefresh) {
                // 유효한 refreshToken에서 role 추출
                String role = jwtUtil.getRoleFromToken(refreshToken);

                // 새로운 Access Token 생성
                String newAccessToken = jwtUtil.generateAccessToken(memberSeq, role);

                // 쿠키에 새 Access Token 저장 (15분짜리)
                CookieUtil.createCookie("accessToken", 15 * 60, "/", newAccessToken, response); // 15분

                // 인증 처리
                UserDetails userDetails = userDetailsService.loadUserByUsername(memberSeq);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);

                System.out.println("Access Token 자동 갱신 완료: " + memberSeq);
            } else {
                //redis에서 삭제되어 refreshToken도 쿠키에서 삭제한다.
                CookieUtil.deleteCookie("refreshToken", "/", response);
            }
        }

        // 8단계: 다음 필터로 요청 전달 (인증 성공/실패와 관계없이)
        // 인증에 실패해도 요청은 계속 진행되며, 이후 필터나 컨트롤러에서 처리
        filterChain.doFilter(request, response);
    }

    // JWT Access Token을 추출
    private String extractToken(HttpServletRequest request) {
        // Authorization 헤더
        // TODO 아직 미사용 추후 모바일 붙이면 헤더에 담아서 넘길 예정
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // accessToken 쿠키 (로그아웃과 일치)
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    // Refresh Token 추출 메서드 추가 (refreshToken은 헤더로 전송하지 않음)
    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}