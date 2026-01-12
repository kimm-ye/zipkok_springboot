package com.kosmo.zipkok.controller;

import com.kosmo.zipkok.dto.CustomUserDetail;
import com.kosmo.zipkok.dto.MissionDTO;
import com.kosmo.zipkok.service.MissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Controller
public class MissionViewController {

	@Autowired
	MissionService missionService;

    // 유저 - 심부름 신청
    @GetMapping("/mission/select")
    public String selectMission(@RequestParam(value = "flag", required = false) String flag,
								Model model) {
		model.addAttribute("flag", flag); // 모델에 추가 (심부름 카테고리)
		model.addAttribute("mode", "create"); // 등록 모드
		return "mission/register";
    }

	// 상세페이지 이동 (이동시 본인이 작성한 글이 아니면 view, 맞으면 edit)
	@GetMapping("/mission/request/detail")
	public ModelAndView detail(@AuthenticationPrincipal CustomUserDetail me,
							   @RequestParam("missionSeq") String missionSeq) {

		ModelAndView mv = new ModelAndView("mission/register");
		MissionDTO detail = missionService.getMissionDetail(missionSeq);

		// 본인 심부름인지 확인 (모드 분기를 위해)
		boolean isOwner = String.valueOf(detail.getMemberSeq()).equals(me.getMemberSeq());

		mv.addObject("memberSeq", me.getMemberSeq());   // 수정 모드
		mv.addObject("mode", isOwner ? "edit" : "view");  // 본인이면 edit, 아니면 view
		mv.addObject("mission", detail);

		return mv;
	}
}
