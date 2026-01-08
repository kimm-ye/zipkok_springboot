package com.kosmo.zipkok.service.impl;

import com.kosmo.zipkok.dao.NoticeDAO;
import com.kosmo.zipkok.dto.NoticeDTO;
import com.kosmo.zipkok.dto.PagingDTO;
import com.kosmo.zipkok.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

@Service
@Transactional
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    NoticeDAO noticeDao;

    /* 공지사항 총 개수  */
    @Override
    public int selectNoticeCount() {
        return noticeDao.selectNoticeCount();
    }

    /* 공지사항 리스트 조회 */
    @Override
    public List<NoticeDTO> selectNoticeList(PagingDTO paging) {
        return noticeDao.selectNoticeList(paging);
    }

    @Override
    public NoticeDTO selectNoticeDetail(String noticeId) {
        return noticeDao.selectNoticeDetail(noticeId);
    }

    // 수정하기 전 해당 id 공지사항이 있는지 먼저 조회
    @Override
    public int isExistNotice(String noticeId) {
        return noticeDao.isExistNotice(noticeId);
    }

    @Override
    public void insertNotice(NoticeDTO noticeDTO) throws IOException {
        // board_notice 테이블에 insert
        noticeDao.insertNotice(noticeDTO);

        if (noticeDTO.getAttachFile() != null && noticeDTO.getAttachFile().getSize() > 0) {
            String fileName = noticeDTO.getAttachFile().getOriginalFilename();
            String fileEtx = StringUtils.getFilenameExtension(fileName); // 파일 확장자
            String originalName = StringUtils.stripFilenameExtension(fileName); // 확장자 제외한 파일 이름만

            noticeDTO.setImageFile(noticeDTO.getAttachFile().getBytes());
            noticeDTO.setImageFileName(originalName);
            noticeDTO.setImageFileEtx(fileEtx);
            noticeDTO.setBoardType("notice");
            noticeDTO.setBoardSeq(noticeDTO.getNoticeSeq());

            noticeDao.insertBoardFile(noticeDTO);
        }
    }

    @Override
    public void updateNotice(NoticeDTO noticeDTO) throws IOException {
        noticeDao.updateNotice(noticeDTO);

        if (noticeDTO.getAttachFile() != null && noticeDTO.getAttachFile().getSize() > 0) {
            String fileName = noticeDTO.getAttachFile().getOriginalFilename();
            String fileEtx = StringUtils.getFilenameExtension(fileName); // 파일 확장자
            String originalName = StringUtils.stripFilenameExtension(fileName); // 확장자 제외한 파일 이름만

            noticeDTO.setImageFile(noticeDTO.getAttachFile().getBytes());
            noticeDTO.setImageFileName(originalName);
            noticeDTO.setImageFileEtx(fileEtx);
            noticeDTO.setBoardType("notice");
            noticeDTO.setBoardSeq(noticeDTO.getNoticeSeq());

            noticeDao.updateBoardFile(noticeDTO);
        }
    }

}
