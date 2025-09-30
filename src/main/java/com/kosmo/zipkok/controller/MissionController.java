package com.kosmo.zipkok.controller;

import com.kosmo.zipkok.dto.HelperDTO;
import com.kosmo.zipkok.service.TokenService;
import com.kosmo.zipkok.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Controller
public class MyPageController {

	@Autowired
	JwtUtil jwtUtil;

	@Autowired
	TokenService tokenService;

	//회원정보 수정 페이지 이동
	@GetMapping("/member/mypage/modify")
	public ModelAndView modify(HttpServletRequest request) {
		ModelAndView mv = new ModelAndView();
		HelperDTO dto = tokenService.getMemberFromAccessToken(request);

		if(dto != null) {
			mv.addObject("info", dto);
			mv.addObject("isModify", true);      // 수정 모드 플래그
			mv.setViewName("member/join");
		} else {
			mv.setViewName("member/login");

		}
		return mv;
	}


	// 수행내역 조회 페이지로 이동
	@RequestMapping("/member/mission")
	public String mission() {
		return "mission/history";
	}

	// 수행내역 리스트 조회
	@GetMapping("/member/mission/history")
	public Map<String, String> history() {
		Map<String, String> result = new HashMap<>();



		return result;
	}


	//회원 탈퇴
	@RequestMapping("/mdelete.do")
	public String memberdelete() {
		return "member/mdelete";
	}

}
