package com.kosmo.zipkok.controller;

import com.kosmo.zipkok.service.MissionService;
import com.kosmo.zipkok.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class MissionController {

	@Autowired
	MissionService missionService;

	// 우저 - 요청내역 조회 페이지로 이동
	@RequestMapping("/mission/request")
	public ModelAndView request() {

		return "mission/request";
	}

	// 헬퍼 - 수헹내역 조회 페이지로 이동
	@RequestMapping("/mission/performance")
	public String performance() {
		return "mission/performance";
	}

	// 유저 - 요청내역 리스트 조회
	@GetMapping("/mission/request/history")
	public Map<String, String> request(HttpServletRequest request) {
		Map<String, String> result = new HashMap<>();

		String accessToken = CookieUtil.getCookieValue(request, "accessToken");



		return result;
	}



	// 헬퍼 - 수행내역 리스트 조회
	@GetMapping("/mission/performance/history")
	public Map<String, String> performance(HttpServletRequest request) {
		Map<String, String> result = new HashMap<>();

		String accessToken = CookieUtil.getCookieValue(request, "accessToken");

		List<String> missionHistory = missionService.getPerformanceHistory(accessToken);


		return result;
	}

    // 유저 - 심부름 신청
    @GetMapping("/mission/select")
    public String selectMission(@RequestParam("flag") String flag, Model model) {
        System.out.println("flag : " + flag);
		model.addAttribute("flag", flag); // 모델에 추가
        return "mission/register";
    }


}
