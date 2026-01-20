package com.kosmo.zipkok.service.impl;

import com.kosmo.zipkok.dao.MissionDAO;
import com.kosmo.zipkok.dto.*;
import com.kosmo.zipkok.service.MissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

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
    public int getMyPerformanceHistoryCount(MissionSearchDTO searchDTO) {
        return missionDao.getMyPerformanceHistoryCount(searchDTO);
    }

    @Override
    public List<MissionDTO> getMyPerformanceHistory(MissionSearchDTO searchDTO, PagingDTO paging) {

        Map<String, Object> params = new HashMap<>();
        params.put("offset", paging.getOffset());
        params.put("limit", paging.getLimit());
        params.put("searchDTO", searchDTO);

        return missionDao.getMyPerformanceHistory(params);
    }

    @Override
    public int getRequestHistoryCount(MissionSearchDTO searchDTO) {
        return missionDao.getRequestHistoryCount(searchDTO);
    }

    // 요청내역 조회
    @Override
    public List<MissionDTO> getRequestHistory(MissionSearchDTO searchDTO, PagingDTO paging) {

        Map<String, Object> params = new HashMap<>();
        params.put("offset", paging.getOffset());
        params.put("limit", paging.getLimit());
        params.put("searchDTO", searchDTO);

        return missionDao.getRequestHistory(params);
    }

    // 미션 상세페이지
    @Override
    public MissionResponse getMissionDetail(String missionSeq) {
        return missionDao.getMissionDetail(missionSeq);
    }

    @Override
    public ImageDTO getMissionImage(String imageSeq) {
        return missionDao.getMissionImage(imageSeq);
    }

    @Override
    public MissionDTO selectMissionBySeq(String missionSeq) {
        return missionDao.selectMissionBySeq(missionSeq);
    }

    @Override
    public boolean hasRating(int missionSeq, int raterSeq, String ratingType) {
        return missionDao.hasRating(missionSeq, raterSeq, ratingType);
    }

    @Override
    public void insertMission(MissionDTO missionDTO) throws IOException {

        try{
            // 1. Mission 메인 정보 저장
            missionDao.insertMission(missionDTO);

            // 2. 위치 정보 저장
            // mission_location에 insert
            //missionDao.insertMissionLocation(missionDTO);
            // way 위치가 있으면
            if(missionDTO.getWayAddress1() != null &&
                    missionDTO.getWayLatitude() != null &&
                    missionDTO.getWayLongitude() != null) {
                missionDao.insertMissionWayLocation(missionDTO);
            }

            // end 위치가 있으면
            if(missionDTO.getEndAddress1() != null &&
                    missionDTO.getEndLatitude() != null &&
                    missionDTO.getEndLongitude() != null) {
                missionDao.insertMissionEndLocation(missionDTO);
            }

            // 3. 이미지 저장 (공통 함수 호출)
            saveMissionImages(missionDTO.getMissionSeq(), missionDTO.getAttachFiles());

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void updateMission(MissionDTO missionDTO) throws IOException {
        try {
            // 1. Mission 메인 정보 수정
            missionDao.updateMission(missionDTO);

            // 2. 위치 정보 Upsert update/insert (merge문이 없어서)
            if(missionDTO.getWayLatitude() != null && missionDTO.getWayLongitude() != null) {
                int wayUpdated = missionDao.updateMissionWayLocation(missionDTO);
                if(wayUpdated == 0) {
                    missionDao.insertMissionWayLocation(missionDTO);
                }
            }

            if(missionDTO.getEndLatitude() != null && missionDTO.getEndLongitude() != null) {
                int endUpdated = missionDao.updateMissionEndLocation(missionDTO);
                if(endUpdated == 0) {
                    missionDao.insertMissionEndLocation(missionDTO);
                }
            }

            // 3. 다중 첨부파일이므로 삭제한 파일 지우기
            List<Integer> deleteSeqs = missionDTO.getDeleteImageSeqs();
            if (deleteSeqs != null && !deleteSeqs.isEmpty()) {
                for (Integer imageSeq : deleteSeqs) {
                    // image_seq(PK)로 하나씩 삭제
                    missionDao.deleteMissionImages(imageSeq);
                }
            }

            // 4. 신규 이미지 처리
            List<MultipartFile> newFiles = missionDTO.getAttachFiles();

            // 새 파일이 들어왔을 때만 기존 것 삭제 후 저장
            if (newFiles != null && !newFiles.isEmpty()) {
                // 새 이미지 저장 (공통 함수 호출)
                saveMissionImages(missionDTO.getMissionSeq(), newFiles);
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /* 이미지 처리 */
    private void saveMissionImages(String missionSeq, List<MultipartFile> images) throws IOException {
        // 파일이 없으면 바로 종료
        if (images == null || images.isEmpty()) {
            return;
        }

        for (MultipartFile rawFile : images) {
            // 빈 파일(용량 0)은 무시
            if (rawFile.getSize() <= 0) {
                continue;
            }

            // DTO 생성 및 값 세팅
            ImageDTO imageDTO = new ImageDTO();
            imageDTO.setMissionSeq(missionSeq); // ✨ FK 세팅 필수!

            String fileName = rawFile.getOriginalFilename();
            String ext = StringUtils.getFilenameExtension(fileName);
            String originalName = StringUtils.stripFilenameExtension(fileName);

            imageDTO.setImageFile(rawFile.getBytes());
            imageDTO.setImageFileName(originalName);
            imageDTO.setImageFileEtx(ext); // DTO 필드명이 etx라면 유지 (ext 권장)

            // ✨ 반복문 안에서 각각 insert 수행 (파라미터는 ImageDTO)
            missionDao.insertMissionImage(imageDTO);
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
    public void insertMissionRating(MissionRatingDTO dto) throws Exception{
        try {
            missionDao.insertMissionRating(dto);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
