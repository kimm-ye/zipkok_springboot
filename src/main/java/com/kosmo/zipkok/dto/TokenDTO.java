package com.kosmo.zipkok.dto;

import lombok.Data;

@Data
public class TokenDTO {
    private String accessToken;        // JWT 토큰
    private String tokenType;          // "Bearer"
    private long expiresIn;            // 만료 시간
    private String memberId;           // 사용자 ID
    private String memberName;         // 사용자 이름
    private String memberStatus;       // 사용자 상태
}