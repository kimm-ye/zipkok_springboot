package com.kosmo.zipkok.dto;

import lombok.Data;

@Data
public class TokenDTO {
    private String accessToken;        // 15분 만료
    private String refreshToken;       // 7일 만료
}