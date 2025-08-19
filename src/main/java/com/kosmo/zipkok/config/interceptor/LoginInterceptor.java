package com.kosmo.zipkok.config.interceptor;

import com.kosmo.zipkok.service.SessionService;
import com.kosmo.zipkok.dto.MemberDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {

    private final SessionService sessionService;

    public LoginInterceptor(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String redisSessionId = (String) request.getSession().getAttribute("redisSession");

        System.out.println("인터셉테ㅓㅓㅓㅓㅓㅓㅓㅓ redisSessionId : " + redisSessionId);

        if (redisSessionId == null) {
            response.sendRedirect(request.getContextPath() + "/member/login");
            return false;
        }

        MemberDTO member = sessionService.getSession(redisSessionId);
        if (member == null) {
            response.sendRedirect(request.getContextPath() + "/member/login");
            return false;
        }

        // 사용자 정보를 request에 저장
        request.setAttribute("loginUser", member);
        return true;
    }
}