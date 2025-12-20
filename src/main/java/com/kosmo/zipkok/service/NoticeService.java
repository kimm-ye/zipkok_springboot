package com.kosmo.zipkok.service;


import com.kosmo.zipkok.dto.NoticeDTO;
import com.kosmo.zipkok.dto.PagingDTO;

import java.io.IOException;
import java.util.List;

public interface NoticeService {

    int selectNoticeCount();
    List<NoticeDTO> selectNoticeList(PagingDTO paging);
    NoticeDTO selectNoticeDetail(String noticeId);
    int isExistNotice(String noticeId);
    void insertNotice(NoticeDTO noticeDTO) throws IOException;
    void updateNotice(NoticeDTO noticeDTO) throws IOException;


}