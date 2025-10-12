package com.kosmo.zipkok.service;

import com.kosmo.zipkok.dto.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface MissionService {

    List<MissionDTO> getPerformanceHistory(PagingDTO paging);
    int getPerformanceHistoryCount();
    List<MissionDTO> getMyPerformanceHistory(String helperSeq, PagingDTO paging);
    int getMyPerformanceHistoryCount(String helperSeq);
    List<MissionDTO> getRequestHistory(String memberSeq, PagingDTO paging);
    int getRequestHistoryCount(String memberSeq);
    MissionDTO getMissionDetail(String missionSeq);
    MissionFileDTO getMissionImage(String missionSeq);
    String selectMemberSeq(String missionSeq);
    int selectMissionStatus(String missionSeq);
    void insertMission(MissionDTO missionDTO) throws IOException;
    void updateMission(MissionDTO missionDTO) throws IOException;
    void updateMissionStatus(Map<String, Object> param) throws Exception;
    void deleteMission(String missionSeq) throws Exception;

}