package com.kosmo.zipkok.dao;

import com.kosmo.zipkok.dto.ImageDTO;
import com.kosmo.zipkok.dto.MissionDTO;
import com.kosmo.zipkok.dto.MissionResponse;
import com.kosmo.zipkok.dto.MissionRatingDTO;
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

	MissionResponse getMissionDetail(String missionSeq);
	ImageDTO getMissionImage(String imageSeq);
	MissionDTO selectMissionBySeq(String missionSeq);

	boolean hasRating(int missionSeq, int raterSeq, String ratingType);

	void insertMission(MissionDTO missionDTO);
	void insertMissionWayLocation(MissionDTO missionDTO);
	void insertMissionEndLocation(MissionDTO missionDTO);

	void insertMissionImage(ImageDTO imageDTO);

	void updateMission(MissionDTO missionDTO);
	int updateMissionWayLocation(MissionDTO missionDTO);
	int updateMissionEndLocation(MissionDTO missionDTO);

	void deleteMissionImages(int imageSeq);
	void updateMissionStatus(Map<String, Object> param);

	void insertMissionRating(MissionRatingDTO dto);
}