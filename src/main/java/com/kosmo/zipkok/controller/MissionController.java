package com.kosmo.zipkok.controller;

import com.kosmo.zipkok.dto.CustomUserDetail;
import com.kosmo.zipkok.dto.MissionDTO;
import com.kosmo.zipkok.dto.PagingDTO;
import com.kosmo.zipkok.service.MissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class MissionController {

	@Autowired
	MissionService missionService;

	// 심부름 등록
	@PostMapping("/mission/request/register")
	public Map<String, Object> register(@AuthenticationPrincipal CustomUserDetail me,
										MissionDTO missionDTO) throws IOException {
		Map<String, Object> result = new HashMap<>();

		try {
			missionDTO.setMemberSeq(me.getMemberSeq());
			missionService.insertMission(missionDTO);

			result.put("success", true);
			result.put("message", "심부름 등록 완료!");
			result.put("redirectUrl", "/zipkok/mission/request/history");

		}catch (Exception e) {
			e.printStackTrace();
			result.put("success", false);
			result.put("message", "정보수정 중 오류가 발생하였습니다.\n관리자에게 문의 바랍니다.");
		}
		return result;
	}

	// 내가 요청한 심부름 내역 조회
	@GetMapping("/mission/request/history")
	public ModelAndView request(@AuthenticationPrincipal CustomUserDetail me,
								@RequestParam(value = "page", defaultValue = "1") int page,
								@RequestParam(value = "size", defaultValue = "10") int size) {

		ModelAndView mv = new ModelAndView("mission/performance");

		int totalCount = missionService.getRequestHistoryCount(me.getMemberSeq());
		PagingDTO paging = PagingDTO.of(page, size, totalCount);
		List<MissionDTO> missionHistory = missionService.getRequestHistory(me.getMemberSeq(), paging);

		mv.addObject("history", "request");
		mv.addObject("lists", missionHistory);
		mv.addObject("paging", paging);

		return mv;
	}

	// 상세페이지 이동 (이동시 본인이 작성한 글이 아니면 view, 맞으면 edit)
	@GetMapping("/mission/request/detail")
	public ModelAndView detail(@AuthenticationPrincipal CustomUserDetail me,
							   @RequestParam("missionSeq") String missionSeq) {

		ModelAndView mv = new ModelAndView("mission/register");
		MissionDTO detail = missionService.getMissionDetail(missionSeq);

		// 본인 심부름인지 확인
		boolean isOwner = String.valueOf(detail.getMemberSeq()).equals(me.getMemberSeq());

		mv.addObject("memberSeq", me.getMemberSeq());   // 수정 모드
		mv.addObject("mode", isOwner ? "edit" : "view");  // 본인이면 edit, 아니면 view
		mv.addObject("mission", detail);

		return mv;
	}

	// 심부름 내용 수정
	@PostMapping("/mission/request/update")
	public Map<String, Object> update(@AuthenticationPrincipal CustomUserDetail me,
									  MissionDTO missionDTO) throws IOException {

		Map<String, Object> result = new HashMap<>();

		try {
			// 작성한 사람만 수정할 수 있도록 비교
			if(me.getMemberSeq().equals(missionDTO.getMemberSeq())) {
				missionService.updateMission(missionDTO);

				result.put("success", true);
				result.put("message", "심부름 수정 완료!");
				result.put("redirectUrl", "/zipkok/mission/request/history");
			} else {
				result.put("success", true);
				result.put("message", "다시 로그인해주세요!");
				result.put("redirectUrl", "/zipkok/mission/request/history");
			}

		}catch (Exception e) {
			e.printStackTrace();
			result.put("success", false);
			result.put("message", "수정 중 오류가 발생하였습니다.\n관리자에게 문의 바랍니다.");
		}
		return result;
	}

	// 심부름 삭제 (심부름이 신청 상태인 경우에만 상태를 9 로 만든다.)
	@DeleteMapping("/mission/delete/{missionSeq}")
	public Map<String, Object> delete(@AuthenticationPrincipal CustomUserDetail me,
									  @PathVariable("missionSeq") String missionSeq) throws IOException {

		Map<String, Object> result = new HashMap<>();

		try {
			// 본인이 작성한 심부름인지 한번더체크
			String memberSeq = missionService.selectMemberSeq(missionSeq);

			if(me.getMemberSeq().equals(memberSeq)) {
				// 심부름 신청 상태인지 체크
				int status = missionService.selectMissionStatus(missionSeq);

				if(status == 0) {
					// 심부름 상태가 신청상태인 경우에만 삭제 가능하도록
					missionService.deleteMission(missionSeq);
					result.put("success", true);
					result.put("message", "심부름 삭제 완료!");
					result.put("redirectUrl", "/zipkok/mission/request/history");
				} else {
					result.put("success", false);
					result.put("message", "신청 상태인 심부름만 삭제 가능 합니다!");
					result.put("redirectUrl", "/zipkok/mission/request/history");
				}
			} else {
				result.put("success", false);
				result.put("message", "본인이 작성한 심부름만 삭제 가능합니다.");
				result.put("redirectUrl", "/zipkok/mission/request/history");
			}

		} catch (Exception e) {
			e.printStackTrace();
			result.put("success", false);
			result.put("message", "삭제 중 오류가 발생하였습니다.\n관리자에게 문의 바랍니다.");
		}
		return result;
	}









	// 헬퍼 - 응모 가능한 수행내역 리스트 조회
	@GetMapping("/mission/performance/history")
	public ModelAndView performance(@RequestParam(value = "page", defaultValue = "1") int page,
									@RequestParam(value = "size", defaultValue = "10") int size) {

		ModelAndView mv = new ModelAndView("mission/performance");

		int totalCount = missionService.getPerformanceHistoryCount();
		PagingDTO paging = PagingDTO.of(page, size, totalCount);
		List<MissionDTO> missionHistory = missionService.getPerformanceHistory(paging);

		mv.addObject("history", "performance");
		mv.addObject("lists", missionHistory);
		mv.addObject("paging", paging);

		return mv;
	}


	// 헬퍼 - 나의 수행내역 조회
	@GetMapping("/mission/performance/history/my")
	public ModelAndView myPerformance(@AuthenticationPrincipal CustomUserDetail me,
									  @RequestParam(value = "page", defaultValue = "1") int page,
									  @RequestParam(value = "size", defaultValue = "10") int size) {

		ModelAndView mv = new ModelAndView("mission/performance");

		int totalCount = missionService.getMyPerformanceHistoryCount(me.getMemberSeq());
		PagingDTO paging = PagingDTO.of(page, size, totalCount);
		List<MissionDTO> missionHistory = missionService.getMyPerformanceHistory(me.getMemberSeq(), paging);

		mv.addObject("history", "performance");
		mv.addObject("lists", missionHistory);
		mv.addObject("paging", paging);

		return mv;
	}

	// 헬퍼 - 심부름 수행 버튼 클릭하여 상태 변경
	// TODO @AuthenticationPrincipal 사용하면 JWT 토큰을 쿠키에 저장하지 않고도 member 정보를 확인할 수 있따.
	@PatchMapping("/mission/perform/{missionSeq}")
	public Map<String, Object> perform(@AuthenticationPrincipal CustomUserDetail me,
									   @PathVariable("missionSeq") String missionSeq) throws Exception  {
		Map<String, Object> result = new HashMap<>();

		try {
			Map<String, Object> param = new HashMap<>();
			param.put("missionSeq", missionSeq);
			param.put("helperSeq", me.getMemberSeq());
			param.put("missionStatus", 1); // 0=신청(대기), 1=진행, 2=완료, 3=취소

			missionService.updateMissionStatus(param);

			result.put("success", true);
			result.put("message", "심부름 수행이 등록되었습니다!");
			result.put("redirectUrl", "/zipkok/member/mypage");

		} catch (Exception e) {
			result.put("success", false);
			result.put("message", "등록에 실패했습니다.\n관리자에게 문의 바랍니다.");
		}

		return result;
	}

}
