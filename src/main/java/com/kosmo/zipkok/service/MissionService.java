package com.kosmo.zipkok.service;

import com.kosmo.zipkok.dto.MissionDTO;
import com.kosmo.zipkok.dto.MissionFileDTO;
import com.kosmo.zipkok.dto.MissionSearchDTO;
import com.kosmo.zipkok.dto.PagingDTO;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface MissionService {

    List<MissionDTO> getPerformanceHistory(PagingDTO paging);
    int getPerformanceHistoryCount();

    List<MissionDTO> getMyPerformanceHistory(String helperSeq, PagingDTO paging);
    int getMyPerformanceHistoryCount(String helperSeq);

    int getRequestHistoryCount(MissionSearchDTO searchDTO);
    List<MissionDTO> getRequestHistory(MissionSearchDTO searchDTO, PagingDTO paging);

    MissionDTO getMissionDetail(String missionSeq);
    MissionFileDTO getMissionImage(String missionSeq);

    MissionDTO selectMissionBySeq(String missionSeq);

    void insertMission(MissionDTO missionDTO) throws IOException;
    void updateMission(MissionDTO missionDTO) throws IOException;
    void updateMissionStatus(Map<String, Object> param) throws Exception;

}