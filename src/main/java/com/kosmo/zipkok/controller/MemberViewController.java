package com.kosmo.zipkok.controller;

import com.kosmo.zipkok.dto.HelperDTO;
import com.kosmo.zipkok.service.TokenService;
import com.kosmo.zipkok.util.CookieUtil;
import com.kosmo.zipkok.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class MemberViewController {

	@Autowired
	JwtUtil jwtUtil;

	@Autowired
	TokenService tokenService;


	// 회원가입
	@RequestMapping("/member/join")
	public ModelAndView join() {
		ModelAndView mv = new ModelAndView();
		mv.addObject("isModify", false);      // 수정 모드 플래그
		mv.setViewName("member/join");
		return mv;
	}

	//로그인
	@RequestMapping("/member/login")
	public String login(HttpServletRequest request) {
		String token = CookieUtil.getCookieValue(request, "accessToken");

		if (token != null && jwtUtil.validateToken(token) && jwtUtil.isAccessToken(token)) {
			return "index";
		} else {
			return "member/login";
		}
	}

	//마이페이지
	@RequestMapping("/member/mypage")
	public String mypage() {
		return "member/mypage";
	}

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

	//로그아웃
	@RequestMapping("/member/logout")
	public String memberLogout() {
		return "member/logout/action";
	}

	//아이디/비밀번호 찾기
	@RequestMapping("/member/find")
	public String find() {
		return "member/find";
	}

}
