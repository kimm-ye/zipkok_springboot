package com.kosmo.zipkok.service;

import com.kosmo.zipkok.dto.MemberDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Service
public class SessionService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // === 세션 생성 ===
    public String createSession(MemberDTO memberDTO) {
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        System.out.println(" sessionId : " + sessionId);

        // Redis에 30분간 저장
        redisTemplate.opsForValue().set(
                "session:" + sessionId,
                memberDTO,
                30,
                TimeUnit.MINUTES
        );

        System.out.println("세션 생성: " + sessionId);
        return sessionId;
    }

    // === 세션 조회 ===
    public MemberDTO getSession(String sessionId) {
        Object data = redisTemplate.opsForValue().get("session:" + sessionId);

        // Redis에서 가져온 데이터가 MemberDTO 타입인지 확인
        // 맞으면 캐스팅해서 반환
        if (data instanceof MemberDTO) {
            System.out.println("세션 조회 성공: " + sessionId);
            return (MemberDTO) data;
        }

        System.out.println("세션 없음: " + sessionId);
        return null;
    }

    // === 세션 삭제 ===
    public void deleteSession(String sessionId) {
        redisTemplate.delete("session:" + sessionId);
        System.out.println("세션 삭제: " + sessionId);
    }

    // === 세션 연장 ===
    public void extendSession(String sessionId) {
        redisTemplate.expire("session:" + sessionId, 30, TimeUnit.MINUTES);
    }
}