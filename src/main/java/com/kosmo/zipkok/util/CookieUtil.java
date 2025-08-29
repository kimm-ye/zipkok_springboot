package com.kosmo.zipkok.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    public static void createCookie(String tokenName, int exp, String path, String token, HttpServletResponse response) {

        Cookie tokenCookie = new Cookie(tokenName, token);
        tokenCookie.setHttpOnly(true);
        tokenCookie.setSecure(false); // HTTPS 환경에서는 true로 설정
        tokenCookie.setMaxAge(exp); // 15분
        tokenCookie.setPath(path);
        response.addCookie(tokenCookie);
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
