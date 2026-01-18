package com.kosmo.zipkok.controller;

import com.kosmo.zipkok.security.CustomUserDetail;
import com.kosmo.zipkok.dto.MissionDTO;
import com.kosmo.zipkok.dto.MissionSearchDTO;
import com.kosmo.zipkok.dto.PagingDTO;
import com.kosmo.zipkok.service.MissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class MissionController {

	@Autowired
	MissionService missionService;


	/**
	 * 유저 - 심부름 요청내역 더보기
	 * 헬퍼 - 심부름 요청/수행 내역 더보기
	 * @param flag - request(요청), perform(수행)
	 * @param page - 페이지 번호
	 * @param search - 검색어 (제목, 내용)
	 * @param missionStatus - 상태(""=전체 0=신청(대기), 1=진행, 2=완료, 9=취소)
	 * @param sort - 정렬 기준 (recent=최신순, old=오래된순)
	 *
	 */
	@GetMapping("/mission/history")
	public ModelAndView history(@AuthenticationPrincipal CustomUserDetail me,
								@RequestParam(defaultValue = "request") String flag,
								@RequestParam(defaultValue = "1") int page,
								@RequestParam(defaultValue = "10") int pageSize,
								@RequestParam(defaultValue = "") String search,
								@RequestParam(defaultValue = "") String missionStatus,
								@RequestParam(defaultValue = "recent") String sort,
								@RequestParam(defaultValue = "false") boolean isAjax) {

		ModelAndView mv = new ModelAndView();

		MissionSearchDTO searchDTO = new MissionSearchDTO();
		searchDTO.setMemberSeq(me.getMemberSeq());
		searchDTO.setSearch(search);
		searchDTO.setMissionStatus(missionStatus);
		searchDTO.setSort(sort);

		int totalCount = 0;
		PagingDTO paging = PagingDTO.of(page, pageSize, totalCount);
		List<MissionDTO> lists = new ArrayList<>();

		System.out.println(searchDTO);

		// 요청 내역 조회
		if("request".equals(flag) || "user".equals(flag)) {
			totalCount = missionService.getRequestHistoryCount(searchDTO);
			paging = PagingDTO.of(page, pageSize, totalCount);
			lists = missionService.getRequestHistory(searchDTO, paging);

			mv.addObject("history", "request");
		}
		// 수행 내역 조회 (헬퍼만)
		else if("perform".equals(flag) || "helper".equals(flag)) {
			totalCount = missionService.getMyPerformanceHistoryCount(searchDTO);
			paging = PagingDTO.of(page, 10, totalCount);
			lists = missionService.getMyPerformanceHistory(searchDTO, paging);

			mv.addObject("history", "performance");
		}

		mv.addObject("flag", flag);
		mv.addObject("search", search);
		mv.addObject("missionStatus", missionStatus);
		mv.addObject("sort", sort);
		mv.addObject("lists", lists);
		mv.addObject("paging", paging);

		// ✅ View 설정 (AJAX 여부에 따라)
		if(isAjax) {
			mv.setViewName("mission/history :: missionListFragment");
		} else {
			mv.setViewName("mission/history");
		}

		return mv;
	}

	// 심부름 등록
	@PostMapping("/mission/request/register")
	public Map<String, Object> register(@AuthenticationPrincipal CustomUserDetail me,
										MissionDTO missionDTO) throws IOException {
		Map<String, Object> result = new HashMap<>();

		try {
			missionDTO.setMemberSeq(me.getMemberSeq());

			System.out.println(missionDTO);
			missionService.insertMission(missionDTO);

			result.put("success", true);
			result.put("message", "심부름 등록 완료!");
			result.put("redirectUrl", "/zipkok/mission/history?flag=request");

		}catch (Exception e) {
			e.printStackTrace();
			result.put("success", false);
			result.put("message", "등록 중 오류가 발생하였습니다.\n관리자에게 문의 바랍니다.");
		}
		return result;
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
				result.put("message", "심부름 수정이 완료되었습니다.");
			} else {
				result.put("success", true);
				result.put("message", "작성한 사람만 수정이 가능합니다.");
			}

			result.put("redirectUrl", "/zipkok/mission/history?flag=request");

		}catch (Exception e) {
			e.printStackTrace();
			result.put("success", false);
			result.put("message", "수정 중 오류가 발생하였습니다.\n관리자에게 문의 바랍니다.");
		}
		return result;
	}

// ================================================================================================================//

	// 헬퍼 - 신청 가능한 수행내역 리스트 조회
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


	// 헬퍼 - 심부름 수행 버튼 클릭하여 상태 변경
	// TODO @AuthenticationPrincipal 사용하면 JWT 토큰을 쿠키에 저장하지 않고도 member 정보를 확인할 수 있따.
	@PatchMapping("/mission/status")
	public Map<String, Object> updateStatus(@AuthenticationPrincipal CustomUserDetail me,
									   		@RequestBody Map<String, String> reqData) throws Exception  {
		Map<String, Object> result = new HashMap<>();

		try {

			// 1. 심부름 조회
			MissionDTO mission = missionService.selectMissionBySeq(reqData.get("missionSeq"));

			if (mission == null) {
				result.put("success", false);
				result.put("message", "존재하지 않는 심부름입니다.");
				return result;
			}

			// 업데이트할 상태값을 int로 변환한다.
			int newStatus = Integer.parseInt(reqData.get("missionStatus"));

			// 2. 권한 및 상태 변경 검증
			String validationMsg = validateStatusChange(mission, newStatus, me.getMemberSeq());
			if (validationMsg != null) {
				result.put("success", false);
				result.put("message", validationMsg);
				return result;
			}

			// 3. 상태 변경
			Map<String, Object> param = new HashMap<>();
			param.put("missionSeq", reqData.get("missionSeq"));
			param.put("missionStatus", reqData.get("missionStatus")); // 0=신청(대기), 1=진행, 2=완료, 9=취소
			// 수행하기(0→1)인 경우 헬퍼 배정
			if (newStatus == 1) {
				param.put("helperSeq", me.getMemberSeq());
			}
			missionService.updateMissionStatus(param);

			result.put("success", true);
			result.put("message", getStatusMessage(newStatus));
			result.put("redirectUrl", getRedirectUrl(newStatus, mission.getMemberSeq(), me.getMemberSeq()));

		} catch (Exception e) {
			result.put("success", false);
			result.put("message", "등록에 실패했습니다.\n관리자에게 문의 바랍니다.");
		}

		return result;
	}

	// 해당 심부름에 대한 권한과 상태 변경 검증
	private String validateStatusChange(MissionDTO mission, Integer newStatus, String memberSeq) {
		// 현재 심부름 상태
		int currentStatus = mission.getMissionStatus();

		// 0→1 (수행하기): 본인 글 아니어야 함
		if (currentStatus == 0 && newStatus == 1) {
			if (mission.getMemberSeq().equals(memberSeq)) {
				return "본인이 작성한 심부름은 수행할 수 없습니다.";
			}
			return null;
		}

		// 1→2 (완료): 배정된 헬퍼만 가능
		if (currentStatus == 1 && newStatus == 2) {
			if (!mission.getHelperSeq().equals(memberSeq)) {
				return "배정된 헬퍼만 완료할 수 있습니다.";
			}
			return null;
		}

		// 0/1→9 (취소/삭제): 작성자만 가능
		if (newStatus == 9) {
			boolean isAuthor = mission.getMemberSeq().equals(memberSeq);

			if (!isAuthor) {
				return "작성자만 취소할 수 있습니다.";
			}

			// 작성자는 신청(0) 상태에서만 삭제 가능
			if (isAuthor && currentStatus != 0) {
				return "신청 상태인 심부름만 삭제 가능합니다.";
			}

			return null;
		}

		return "잘못된 상태 변경 요청입니다.";
	}

	// 상태별 메시지
	private String getStatusMessage(Integer status) {
		return switch (status) {
			case 1 -> "심부름 수행이 등록되었습니다!\n심부름을 완료해주세요";
			case 2 -> "심부름 수행이 완료되었습니다!";
			case 9 -> "심부름이 취소되었습니다!";
			default -> "상태가 변경되었습니다.";
		};
	}

	// 리다이렉트 URL (상황에 맞게)
	private String getRedirectUrl(Integer status, String missionAuthorSeq, String currentUserSeq) {
		// 본인이 작성한 글을 삭제(9)한 경우
		if (status == 9 && missionAuthorSeq.equals(currentUserSeq)) {
			return "/zipkok/mission/history?flag=request";
		}
		// 헬퍼가 수행/완료한 경우
		return "/zipkok/mission/history?flag=perform";
	}
}
