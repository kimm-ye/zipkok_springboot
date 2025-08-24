package com.kosmo.zipkok.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LoginController {

	//사용자로 가입할지 헬퍼로 가입할지
	@RequestMapping("/member/join")
	public String join() {
		return "member/join";
	}

	//회원정보 변경시 알림창
	@RequestMapping("/changeAlert.do")
	public String changeAlter() {
		
		return "member/changeAlert";
	}
	
	//로그인
	@RequestMapping("/member/login")
	public String login() {
		return "member/login";
	}

	//로그아웃
	@RequestMapping("/logout.do")
	public String memberLogout() {
		
		return "member/logout";
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
