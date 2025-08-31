package com.kosmo.zipkok.controller;

import com.kosmo.zipkok.dto.MemberDTO;
import com.kosmo.zipkok.service.MemberService;
import com.kosmo.zipkok.service.TokenService;
import com.kosmo.zipkok.util.CookieUtil;
import com.kosmo.zipkok.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LoginController {

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

	//로그아웃
	@RequestMapping("/member/logout")
	public String memberLogout() {
		return "member/logout/action";
	}

	//마이페이지
	@RequestMapping("/member/mypage")
	public String mypage(HttpServletRequest request, Model model) {
		// 계속 변하는 정보이므로 토큰에 담지 않고 조회
		// MemberDTO dto = tokenService.getMemberFromAccessToken(request);
		// model.addAttribute("missionCnt", dto.getMemberMissionN());
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
