package com.kosmo.zipkok.controller;

import com.kosmo.zipkok.dto.MissionDTO;
import com.kosmo.zipkok.dto.PagingDTO;
import com.kosmo.zipkok.service.MissionService;
import com.kosmo.zipkok.util.CookieUtil;
import com.kosmo.zipkok.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
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

	@Autowired
	JwtUtil jwtUtil;

	// 사용자 심부름 등록
	@PostMapping("/mission/request/register")
	public Map<String, Object> register(HttpServletRequest request, MissionDTO missionDTO) throws IOException {
		Map<String, Object> result = new HashMap<>();

		try {
			String accessToken = CookieUtil.getCookieValue(request, "accessToken");
			String memberSeq = jwtUtil.getMemberSeqFromToken(accessToken);

			missionDTO.setMemberSeq(memberSeq);
			missionDTO.setMissionStatus(0); // 신청(대기)는 0

			missionService.insertMission(missionDTO);

			result.put("success", true);
			result.put("message", "심부름 등록 완료!");
			result.put("redirectUrl", "/zipkok/member/mypage");

		}catch (Exception e) {
			e.printStackTrace();
			result.put("success", false);
			result.put("message", "정보수정 중 오류가 발생하였습니다.\n관리자에게 문의 바랍니다.");
		}
		return result;
	}

	// 수행내역 리스트 조회
	@GetMapping("/mission/performance/history")
	public ModelAndView performance(HttpServletRequest request,
									@RequestParam(value = "page", defaultValue = "1") int page,
									@RequestParam(value = "size", defaultValue = "10") int size) {

		ModelAndView mv = new ModelAndView("mission/performance");
		String accessToken = CookieUtil.getCookieValue(request, "accessToken");

		int totalCount = missionService.getPerformanceHistoryCount(accessToken);
		PagingDTO paging = PagingDTO.of(page, size, totalCount);
		List<MissionDTO> missionHistory = missionService.getPerformanceHistory(accessToken, paging);

		mv.addObject("lists", missionHistory);
		mv.addObject("paging", paging);

		return mv;
	}


	// 요청내역 리스트 조회
	@GetMapping("/mission/request/history")
	public ModelAndView request(HttpServletRequest request,
									@RequestParam(value = "page", defaultValue = "1") int page,
									@RequestParam(value = "size", defaultValue = "10") int size) {

		ModelAndView mv = new ModelAndView("mission/performance");
		String accessToken = CookieUtil.getCookieValue(request, "accessToken");

		int totalCount = missionService.getRequestHistoryCount(accessToken);
		PagingDTO paging = PagingDTO.of(page, size, totalCount);
		List<MissionDTO> missionHistory = missionService.getRequestHistory(accessToken, paging);

		mv.addObject("lists", missionHistory);
		mv.addObject("paging", paging);

		return mv;
	}

	@GetMapping("/mission/request/detail")
	public ModelAndView detail(HttpServletRequest request,
							   @RequestParam("missionSeq") String missionSeq) {

		ModelAndView mv = new ModelAndView("mission/register");
		MissionDTO detail = missionService.getMissionDetail(missionSeq);

		mv.addObject("mode", "edit");   // 수정 모드
		mv.addObject("mission", detail);

		return mv;
	}

	@PostMapping("/mission/request/update")
	public void update(){

	}

}
