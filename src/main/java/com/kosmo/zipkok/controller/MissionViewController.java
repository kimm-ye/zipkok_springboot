package com.kosmo.zipkok.controller;

import com.kosmo.zipkok.dto.CustomUserDetail;
import com.kosmo.zipkok.dto.MissionDTO;
import com.kosmo.zipkok.dto.PagingDTO;
import com.kosmo.zipkok.service.MissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Slf4j
@Controller
public class MissionViewController {

	@Autowired
	MissionService missionService;

    // 유저 - 심부름 신청
    @GetMapping("/mission/select")
    public String selectMission(@RequestParam(value = "flag", required = false) String flag, Model model) {
		model.addAttribute("flag", flag); // 모델에 추가 (심부름 카테고리)
		model.addAttribute("mode", "create"); // 등록 모드
		return "mission/register";
    }

	// 유저 - 심부름 요청내역 더보기
	// 헬퍼 - 심부름 요청/수행 내역 더보기
	@GetMapping("/mission/add")
	public ModelAndView addMission(@AuthenticationPrincipal CustomUserDetail me,
								   @RequestParam(defaultValue = "request") String flag,
								   @RequestParam(defaultValue = "1") int page,
								   @RequestParam(defaultValue = "") String search,
								   @RequestParam(defaultValue = "") String status) {

		ModelAndView mv = new ModelAndView("mission/list");

		// 요청 내역 조회
		if("request".equals(flag) || "user".equals(flag)) {
			int totalCount = missionService.getRequestHistoryCount(me.getMemberSeq());
			PagingDTO paging = PagingDTO.of(page, 10, totalCount);
			List<MissionDTO> lists = missionService.getRequestHistory(me.getMemberSeq(), paging);

			mv.addObject("lists", lists);
			mv.addObject("paging", paging);
			mv.addObject("history", "request");
		}
		// 수행 내역 조회 (헬퍼만)
		else if("perform".equals(flag) || "helper".equals(flag)) {
			int totalCount = missionService.getMyPerformanceHistoryCount(me.getMemberSeq());
			PagingDTO paging = PagingDTO.of(page, 10, totalCount);
			List<MissionDTO> lists = missionService.getMyPerformanceHistory(me.getMemberSeq(), paging);

			mv.addObject("lists", lists);
			mv.addObject("paging", paging);
			mv.addObject("history", "performance");
		}

		mv.addObject("flag", flag);
		mv.addObject("search", search);
		mv.addObject("status", status);

		return mv;
	}
}
