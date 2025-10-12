package com.kosmo.zipkok.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class MissionFileDTO {

    private MultipartFile missionAttachFile;
    private byte[] missionImageFile; // 실제 파일 내용을 바이트 배열로 읽어옴
    private String missionImageFileName;
    private String missionImageFileEtx;


    public String getFullMissionImageName() {
        if (missionImageFileName != null && missionImageFileEtx != null) {
            return missionImageFileName + "." + missionImageFileEtx;
        } else {
            return null;
        }
    }
}
