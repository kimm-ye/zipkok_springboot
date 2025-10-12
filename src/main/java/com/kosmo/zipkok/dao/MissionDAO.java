package com.kosmo.zipkok.dao;

import com.kosmo.zipkok.dto.MissionDTO;
import com.kosmo.zipkok.dto.MissionFileDTO;
import com.kosmo.zipkok.dto.PagingDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;


@Mapper
public interface MissionDAO {

	List<MissionDTO> getPerformanceHistory(Map<String, Object> params);
	int getPerformanceHistoryCount();
	List<MissionDTO> getMyPerformanceHistory(Map<String, Object> params);
	int getMyPerformanceHistoryCount(String helperSeq);
	List<MissionDTO> getRequestHistory(Map<String, Object> params);
	int getRequestHistoryCount(String memberSeq);
	MissionDTO getMissionDetail(String missionSeq);
	MissionFileDTO getMissionImage(String missionSeq);
	String selectMemberSeq(String missionSeq);
	int selectMissionStatus(String missionSeq);
	void insertMission(MissionDTO missionDTO);
	void insertMissionLocation(MissionDTO missionDTO);
	void insertMissionImage(MissionDTO missionDTO);
	void updateMission(MissionDTO missionDTO);
	int updateMissionWayLocation(MissionDTO missionDTO);
	int updateMissionEndLocation(MissionDTO missionDTO);
	void updateMissionImage(MissionDTO missionDTO);
	void updateMissionStatus(Map<String, Object> param);
	void deleteMission(String missionSeq);
}