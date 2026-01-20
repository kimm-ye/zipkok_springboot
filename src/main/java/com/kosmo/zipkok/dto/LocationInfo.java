package com.kosmo.zipkok.dto;

import lombok.Data;

import java.util.List;

@Data
public class LocationInfo {
    private String address1; // 기본주소
    private String address2; // 상세주소
    private String postcode; // 우편번호
    private Double latitude; // 위도
    private Double longitude; // 경도
}
