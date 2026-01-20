package com.kosmo.zipkok.dao;

import com.kosmo.zipkok.dto.ImageDTO;
import com.kosmo.zipkok.dto.MissionDTO;
import com.kosmo.zipkok.dto.MissionSearchDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;


@Mapper
public interface MissionDAO {

	int getPerformanceHistoryCount();
	List<MissionDTO> getPerformanceHistory(Map<String, Object> params);

	int getMyPerformanceHistoryCount(MissionSearchDTO searchDTO);
	List<MissionDTO> getMyPerformanceHistory(Map<String, Object> params);

	int getRequestHistoryCount(MissionSearchDTO searchDTO);
	List<MissionDTO> getRequestHistory(Map<String, Object> params);

	MissionDTO getMissionDetail(String missionSeq);
	ImageDTO getMissionImage(String imageSeq);
	MissionDTO selectMissionBySeq(String missionSeq);

	void insertMission(MissionDTO missionDTO);
	void insertMissionLocation(MissionDTO missionDTO);
	void insertMissionImage(ImageDTO imageDTO);
	void updateMission(MissionDTO missionDTO);
	int updateMissionWayLocation(MissionDTO missionDTO);
	int updateMissionEndLocation(MissionDTO missionDTO);
	void updateMissionStatus(Map<String, Object> param);
	void deleteMissionImages(int imageSeq);
}