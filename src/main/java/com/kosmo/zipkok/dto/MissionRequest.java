package com.kosmo.zipkok.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class MissionRequest {

    private String memberSeq;
    private String missionSeq;
    private String missionCategory;
    private String missionTitle;
    private String missionContent;
    private int missionGender;
    private String helperSeq;
    private String missionReservation; // 0=즉시신청, 1=예약신청
    private String missionReservationDt; // 예약신청일자
    private String missionTime; // 1= 10분이내, 2=10 ~ 20분, 3=20 ~ 40분, 4=40 ~ 60분, 5=60분 이상
    private int missionCost;
    private int missionStatus; // 0=신청(대기), 1=진행, 2=완료, 9=취소

    private Double rating;           // 평점
    private String ratingComment;    // 평가 코멘트

    // -------------------------------
    // 위치 관련 필드 추가
    // -------------------------------
    private LocationInfo wayLocation; // 경유지
    private LocationInfo endLocation; // 도착지

    // 1. 새 파일 받을 리스트
    private List<MultipartFile> attachFiles;

    // 2. 삭제할 이미지 번호 받을 리스트 (수정 시 필요)
    private List<Integer> deleteImageSeqs;

}
