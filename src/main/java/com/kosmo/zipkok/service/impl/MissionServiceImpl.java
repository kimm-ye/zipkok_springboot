package com.kosmo.zipkok.service.impl;

import com.kosmo.zipkok.dao.MissionDAO;
import com.kosmo.zipkok.dto.*;
import com.kosmo.zipkok.service.MissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class MissionServiceImpl implements MissionService {

    @Autowired
    MissionDAO missionDao;

    //수행내역 조회
    @Override
    public List<MissionDTO> getPerformanceHistory(PagingDTO paging) {

        Map<String, Object> params = new HashMap<>();
        params.put("offset", paging.getOffset());
        params.put("limit", paging.getLimit());

        return missionDao.getPerformanceHistory(params);
    }

    @Override
    public int getPerformanceHistoryCount() {
        return missionDao.getPerformanceHistoryCount();
    }

    @Override
    public List<MissionDTO> getMyPerformanceHistory(String helperSeq, PagingDTO paging) {

        Map<String, Object> params = new HashMap<>();
        params.put("offset", paging.getOffset());
        params.put("limit", paging.getLimit());
        params.put("helperSeq", helperSeq);

        return missionDao.getMyPerformanceHistory(params);
    }

    @Override
    public int getMyPerformanceHistoryCount(String helperSeq) {
        return missionDao.getMyPerformanceHistoryCount(helperSeq);
    }

    // 요청내역 조회
    @Override
    public List<MissionDTO> getRequestHistory(String memberSeq, PagingDTO paging) {

        Map<String, Object> params = new HashMap<>();
        params.put("offset", paging.getOffset());
        params.put("limit", paging.getLimit());
        params.put("memberSeq", memberSeq);

        return missionDao.getRequestHistory(params);
    }

    @Override
    public int getRequestHistoryCount(String memberSeq) {
        return missionDao.getRequestHistoryCount(memberSeq);
    }

    // 미션 상세페이지
    @Override
    public MissionDTO getMissionDetail(String missionSeq) {
        return missionDao.getMissionDetail(missionSeq);
    }

    @Override
    public MissionFileDTO getMissionImage(String missionSeq) {
        return missionDao.getMissionImage(missionSeq);
    }

    @Override
    public String selectMemberSeq(String missionSeq) {
        return missionDao.selectMemberSeq(missionSeq);
    }

    @Override
    public int selectMissionStatus(String missionSeq) {
        return missionDao.selectMissionStatus(missionSeq);
    }

    @Override
    public void insertMission(MissionDTO missionDTO) throws IOException {
        try{
            //mission 테이블에 데이터 insert
            missionDao.insertMission(missionDTO);

            // mission_location에 insert
            missionDao.insertMissionLocation(missionDTO);

            // mission_file에 insert
            if(missionDTO.getMissionAttachFile().getSize() > 0) {
                String fileName = missionDTO.getMissionAttachFile().getOriginalFilename();
                String fileEtx = StringUtils.getFilenameExtension(fileName); // 파일 확장자
                String originalName = StringUtils.stripFilenameExtension(fileName); // 확장자 제외한 파일 이름만

                missionDTO.setMissionImageFile(missionDTO.getMissionAttachFile().getBytes());
                missionDTO.setMissionImageFileName(originalName);
                missionDTO.setMissionImageFileEtx(fileEtx);

                missionDao.insertMissionImage(missionDTO);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void updateMission(MissionDTO missionDTO) throws IOException {
        try {
            //mission 테이블에 데이터 update
            missionDao.updateMission(missionDTO);

            // mission_location에 update/insert (merge문이 없어서)
            if(missionDTO.getWayLatitude() != null && missionDTO.getWayLongitude() != null) {
                int wayUpdated = missionDao.updateMissionWayLocation(missionDTO);
                if(wayUpdated == 0) {
                    missionDao.insertMissionLocation(missionDTO);
                }
            }

            if(missionDTO.getEndLatitude() != null && missionDTO.getEndLongitude() != null) {
                int endUpdated = missionDao.updateMissionEndLocation(missionDTO);
                if(endUpdated == 0) {
                    missionDao.insertMissionLocation(missionDTO);
                }
            }

            // mission_file에 insert
            if(missionDTO.getMissionAttachFile().getSize() > 0) {
                String fileName = missionDTO.getMissionAttachFile().getOriginalFilename();
                String fileEtx = StringUtils.getFilenameExtension(fileName); // 파일 확장자
                String originalName = StringUtils.stripFilenameExtension(fileName); // 확장자 제외한 파일 이름만

                missionDTO.setMissionImageFile(missionDTO.getMissionAttachFile().getBytes());
                missionDTO.setMissionImageFileName(originalName);
                missionDTO.setMissionImageFileEtx(fileEtx);

                missionDao.updateMissionImage(missionDTO);
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void updateMissionStatus(Map<String, Object> param) throws Exception {
        try {
            missionDao.updateMissionStatus(param);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void deleteMission(String missionSeq) throws Exception {
        try {
            missionDao.deleteMission(missionSeq);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
