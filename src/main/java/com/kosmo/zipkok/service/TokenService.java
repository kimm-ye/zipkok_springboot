package com.kosmo.zipkok.service;

import com.kosmo.zipkok.dto.HelperDTO;
import com.kosmo.zipkok.dto.MemberDTO;
import com.kosmo.zipkok.util.CookieUtil;
import com.kosmo.zipkok.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MemberService memberService;

    // accessToken으로 member정보 조회
    public HelperDTO getMemberFromAccessToken(HttpServletRequest request) {

        String token = CookieUtil.getCookieValue(request, "accessToken");
        HelperDTO dto = new HelperDTO();
        if (token != null && jwtUtil.validateToken(token) && jwtUtil.isAccessToken(token)) {
            String memberId = jwtUtil.getMemberIdFromToken(token);
            dto = memberService.selectMemberById(memberId);
        }
        return dto;
    }
}