package com.kosmo.zipkok.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class BoardFileDTO {

    private String fileId;
    private String boardType; // 공지사항(N), 질문게시판(Q), 이벤트게시판(E)
    private MultipartFile boardAttachFile;
    private byte[] boardFile; // 실제 파일 내용을 바이트 배열로 읽어옴
    private String boardFileName;
    private String boardFileEtx;

}
