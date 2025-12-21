package com.kosmo.zipkok.controller;

import com.kosmo.zipkok.dto.*;
import com.kosmo.zipkok.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    // 공지사항 리스트 조회(비로그인 대상자도 조회가능)
    @GetMapping("/notice/history")
    public ResponseEntity<?> noticeHistory(@RequestParam(value = "page", defaultValue = "1") int page,
                                           @RequestParam(value = "size", defaultValue = "10") int size) {

        int totalCount = noticeService.selectNoticeCount();
        PagingDTO paging = PagingDTO.of(page, size, totalCount);
        List<NoticeDTO> noticeList = noticeService.selectNoticeList(paging);

        BoardResponse<List<NoticeDTO>> response = new BoardResponse<>(
                true,
                "조회 성공",
                noticeList,
                paging
        );

        return ResponseEntity.ok(response);
    }

    // 공지사항 등록 (관리자만 가능)
    @PostMapping("/notice/write/action")
    public ResponseEntity<ApiResponse<Void>> writeAction(NoticeDTO noticeDTO,
                                                        @AuthenticationPrincipal CustomUserDetail me) throws IOException {

        // 관리자인지 한번 더 체크
        adminCheck(me);

        // 예외는 잡지 않고 전역 예외 처리기로 위임
        noticeDTO.setMemberSeq(me.getMemberSeq());
        noticeService.insertNotice(noticeDTO);

        ApiResponse<Void> response = new ApiResponse<>(true, "공지사항 등록 완료!", "/zipkok/notice");
        return ResponseEntity.ok(response);
    }

    // 공지사항 상세
    @GetMapping("/api/notice/detail")
    public ResponseEntity<?> detail(@RequestParam("noticeId") String noticeId) {

        NoticeDTO detail = noticeService.selectNoticeDetail(noticeId);

        BoardResponse<NoticeDTO> response = new BoardResponse<>(
                true,
                "조회 성공",
                detail,
                null
        );

        return ResponseEntity.ok(response);
    }

    // 공지사항 수정 (관리자만 가능)
    @PatchMapping("/api/notice/update")
    public ResponseEntity<ApiResponse<Void>> updateAction(NoticeDTO noticeDTO,
                                                          @AuthenticationPrincipal CustomUserDetail me) throws IOException {

        // 관리자인지 한번 더 체크
        adminCheck(me);

        // 해당 id에 값이 있는지 먼저 확인
        int cnt = noticeService.isExistNotice(noticeDTO.getNoticeSeq());
        if(cnt < 0) {
            ApiResponse<Void> response =
                    new ApiResponse<>(false, "해당 공지사항이 존재하지 않습니다.", null);

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(response);
        }

        // 예외는 잡지 않고 전역 예외 처리기로 위임
        noticeDTO.setMemberSeq(me.getMemberSeq());
        noticeService.updateNotice(noticeDTO);

        ApiResponse<Void> response = new ApiResponse<>(true, "공지사항 수정 완료!", "/zipkok/notice");
        return ResponseEntity.ok(response);
    }

        @DeleteMapping("/api/notice/delete")
        public ResponseEntity<ApiResponse<Void>> deleteAction(String noticeId,
                                                              @AuthenticationPrincipal CustomUserDetail me) throws Exception {

            // 관리자인지 한번 더 체크
            adminCheck(me);

            ApiResponse<Void> response = new ApiResponse<>(true, "공지사항 삭제 완료!", "/zipkok/notice");
            return ResponseEntity.ok(response);
        }

        public void adminCheck(@AuthenticationPrincipal CustomUserDetail me) {
            // 관리자인지 한번 더 체크
            if (me.getMemberStatus() != 0) {
                ApiResponse<Void> response =
                        new ApiResponse<>(false, "공지사항에 대한 권한이 없습니다.", null);

                ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(response);
            }
        }
    }
