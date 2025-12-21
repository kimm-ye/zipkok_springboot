package com.kosmo.zipkok.dao;

import com.kosmo.zipkok.dto.MissionDTO;
import com.kosmo.zipkok.dto.MissionFileDTO;
import com.kosmo.zipkok.dto.NoticeDTO;
import com.kosmo.zipkok.dto.PagingDTO;
import org.apache.ibatis.annotations.Mapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;


@Mapper
public interface NoticeDAO {

    int selectNoticeCount();
    List<NoticeDTO> selectNoticeList(PagingDTO paging);
    NoticeDTO selectNoticeDetail(String noticeId);
    int isExistNotice(String noticeId);
    void insertNotice(NoticeDTO noticeDTO);
    void insertBoardFile(NoticeDTO noticeDTO);
    void updateNotice(NoticeDTO noticeDTO);
    void updateBoardFile(NoticeDTO noticeDTO);
}