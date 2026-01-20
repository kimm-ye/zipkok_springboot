package com.kosmo.zipkok.dto;

import lombok.Data;

@Data
public class MissionRatingDTO {
    private int ratingSeq;
    private int missionSeq;
    private int raterSeq;        // 평가자
    private int rateeSeq;        // 피평가자
    private String ratingType;   // CLIENT_TO_HELPER, HELPER_TO_CLIENT
    private Double rating;
    private String ratingComment;
    private String ratingCreateDt;



}
