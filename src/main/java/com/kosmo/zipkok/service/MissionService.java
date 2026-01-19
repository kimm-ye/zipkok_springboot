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

    MissionDTO getMissionDetail(String missionSeq);
    ImageDTO getMissionImage(String missionSeq);

    MissionDTO selectMissionBySeq(String missionSeq);

    void insertMission(MissionDTO missionDTO) throws IOException;
    void updateMission(MissionDTO missionDTO) throws IOException;
    void updateMissionStatus(Map<String, Object> param) throws Exception;

}