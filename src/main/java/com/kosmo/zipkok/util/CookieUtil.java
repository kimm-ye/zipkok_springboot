package com.kosmo.zipkok.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    public static void createCookie(String tokenName, long expMillis, String path, String token, HttpServletResponse response) {

        // 여기서 계산! (밀리초 -> 초 변환)
        // 0보다 클 때만 변환하고, 0이면(세션 쿠키 등) 그대로 0 처리
        long exp = (expMillis > 0) ? (expMillis / 1000) : 0;

        ResponseCookie cookie = ResponseCookie.from(tokenName, token)
                .httpOnly(true)
                .path(path)
                .maxAge(exp)
                .sameSite("Lax")   // ← 이게 중요
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }



    /**
     * 쿠키를 삭제합니다 (만료시간을 0으로 설정)
     *
     * @param cookieName 삭제할 쿠키 이름
     * @param path 쿠키 경로
     * @param response HTTP 응답 객체
     */
    public static void deleteCookie(String cookieName, String path, HttpServletResponse response) {
        createCookie(cookieName, 0, path, "", response);
    }

    /**
     * 요청에서 특정 쿠키 값을 추출합니다.
     *
     * @param request HTTP 요청 객체
     * @param cookieName 찾을 쿠키 이름
     * @return 쿠키 값 (없으면 null)
     */
    public static String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
