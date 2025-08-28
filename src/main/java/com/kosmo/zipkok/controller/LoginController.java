package com.kosmo.zipkok.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LoginController {

	// 회원가입
	@RequestMapping("/member/join")
	public String join() {
		return "member/join";
	}

	//로그인
	@RequestMapping("/member/login")
	public String login() {
		return "member/login";
	}

	//로그아웃
	@RequestMapping("/member/logout")
	public String memberLogout() {
		return "member/logout/action";
	}

	//마이페이지
	@RequestMapping("/member/mypage")
	public String mypage() {
	        return "member/mypage";
}

	//회원 탈퇴
    @RequestMapping("/mdelete.do")
    public String memberdelete() {
        return "member/mdelete";
    }
	
	//아이디/비밀번호 찾기
	@RequestMapping("/member/find")
	public String find() {
		return "member/find";
	}
}
