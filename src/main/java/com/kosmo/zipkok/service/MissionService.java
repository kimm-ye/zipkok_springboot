package com.kosmo.zipkok.service;

import com.kosmo.zipkok.dto.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface MissionService {

    List<MissionDTO> getPerformanceHistory(PagingDTO paging);
    int getPerformanceHistoryCount();

    int getMyPerformanceHistoryCount(MissionSearchDTO searchDTO);
    List<MissionDTO> getMyPerformanceHistory(MissionSearchDTO searchDTO, PagingDTO paging);

    int getRequestHistoryCount(MissionSearchDTO searchDTO);
    List<MissionDTO> getRequestHistory(MissionSearchDTO searchDTO, PagingDTO paging);

    MissionResponse getMissionDetail(String missionSeq);
    ImageDTO getMissionImage(String imageSeq);

    MissionDTO selectMissionBySeq(String missionSeq);

    boolean hasRating(int missionSeq, int raterSeq, String ratingType);

    void insertMission(MissionDTO missionDTO) throws IOException;
    void updateMission(MissionDTO missionDTO) throws IOException;
    void updateMissionStatus(Map<String, Object> param) throws Exception;

    void insertMissionRating(MissionRatingDTO param) throws Exception;
}