package com.kosmo.zipkok.controller;

import com.kosmo.zipkok.service.MissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
public class MissionViewController {

	@Autowired
	MissionService missionService;
/*

	// 우저 - 요청내역 조회 페이지로 이동
	@RequestMapping("/mission/request")
	public String request() {
		return "request.html_사용안함";
	}

	// 헬퍼 - 수헹내역 조회 페이지로 이동
	@RequestMapping("/mission/performance")
	public String performance() {
		return "mission/performance";
	}
*/

    // 유저 - 심부름 신청
    @GetMapping("/mission/select")
    public String selectMission(@RequestParam("flag") String flag, Model model) {
		model.addAttribute("flag", flag); // 모델에 추가 (심부름 카테고리)
		model.addAttribute("mode", "create"); // 등록 모드
		return "mission/register";
    }
}
