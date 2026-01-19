package com.kosmo.zipkok.controller;

import com.kosmo.zipkok.security.CustomUserDetail;
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

		MissionDTO detail = missionService.getMissionDetail(missionSeq);
		boolean isOwner = String.valueOf(detail.getMemberSeq()).equals(me.getMemberSeq());

		// 권한 없으면 리다이렉트
		if(!isOwner && !me.getRole().equals("ROLE_HELPER")) {
			return new ModelAndView("redirect:/mission/select"); // 신청페이지로
		}

		ModelAndView mv = new ModelAndView("mission/register");
		mv.addObject("memberSeq", me.getMemberSeq());

		// 심부름 상태가 신청이 아닌 경우 모드는 view로 바꾼다.
		// 모드 결정: 신청 상태(0)이고 작성자일 때만 edit, 나머지는 view
		String mode = (detail.getMissionStatus() == 0 && isOwner) ? "edit" : "view";
		mv.addObject("mode", mode);
		mv.addObject("mission", detail);

		return mv;
	}
}
