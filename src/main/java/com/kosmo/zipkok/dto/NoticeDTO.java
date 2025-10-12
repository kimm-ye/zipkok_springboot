package com.kosmo.zipkok.dto;

import lombok.Data;

@Data
public class NoticeDTO extends BoardFileDTO{

    private String noticeSeq;
    private String noticeTitle;
    private String noticeContent;
    private String memberSeq;
    private String noticeView;

}