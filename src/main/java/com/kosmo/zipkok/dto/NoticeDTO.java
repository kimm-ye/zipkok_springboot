package com.kosmo.zipkok.dto;

import lombok.Data;

import java.util.Date;

@Data
public class NoticeDTO extends BoardFileDTO{

    private String noticeSeq;
    private String noticeType;
    private String noticeTitle;
    private String noticeContent;
    private String memberSeq;
    private String createDt;
    private String noticeView;
    
    private String writerName; // 공지사항 작성자 이름 보여주기 위함

}