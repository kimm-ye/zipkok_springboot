package com.kosmo.zipkok.service;

import com.kosmo.zipkok.dto.MemberDTO;
import com.kosmo.zipkok.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Service
public class SessionService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    // === 세션 생성 ===
    public String createSession(MemberDTO memberDTO) {

        // JWT 토큰 생성
        String token = jwtUtil.generateToken(memberDTO.getMemberId());

        // Redis에 30분간 저장
        redisTemplate.opsForValue().set(
                "token:" + token,
                memberDTO,
                30,
                TimeUnit.MINUTES
        );

        System.out.println("토큰 및 생성: " + token);
        return token;
    }

    // === 세션 조회 ===
    public MemberDTO getSession(String token) {

        if (!jwtUtil.validateToken(token)) {
            return null;
        }

        Object data = redisTemplate.opsForValue().get("token:" + token);

        // Redis에서 가져온 데이터가 MemberDTO 타입인지 확인
        // 맞으면 캐스팅해서 반환
        if (data instanceof MemberDTO) {
            // 토큰 유효기간 연장
            extendSession(token);
            return (MemberDTO) data;
        }

        return null;
    }

    // === 세션 삭제 ===
    public void deleteSession(String token) {
        MemberDTO member = getSession(token);

        if (member != null) {
            redisTemplate.delete("token:" + token);
            redisTemplate.opsForSet().remove("user_tokens:" + member.getMemberId(), token);
        }

        System.out.println("세션 삭제: " + token);
    }

    // === 세션 연장 ===
    public void extendSession(String token) {
        redisTemplate.expire("token:" + token, 30, TimeUnit.MINUTES);
    }

    // === 사용자별 모든 토큰 삭제 (로그아웃 시) ===
    public void deleteAllUserSessions(String memberId) {
        Set<Object> tokens = redisTemplate.opsForSet().members("user_tokens:" + memberId);
        if (tokens != null) {
            for (Object token : tokens) {
                redisTemplate.delete("token:" + token);
            }
            redisTemplate.delete("user_tokens:" + memberId);
        }
    }
    
}