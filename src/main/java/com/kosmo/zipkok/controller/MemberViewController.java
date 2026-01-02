package com.kosmo.zipkok.controller;

import com.kosmo.zipkok.dto.CustomUserDetail;
import com.kosmo.zipkok.dto.HelperDTO;
import com.kosmo.zipkok.dto.MissionDTO;
import com.kosmo.zipkok.dto.PagingDTO;
import com.kosmo.zipkok.service.MemberService;
import com.kosmo.zipkok.service.MissionService;
import com.kosmo.zipkok.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class MemberViewController {

	@Autowired
	JwtUtil jwtUtil;

	@Autowired
	MemberService memberService;
	@Autowired
	MissionService missionService;


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
	public String login(@CookieValue(value = "accessToken", required = false) String accessToken) {

		if (accessToken != null && jwtUtil.validateToken(accessToken) && jwtUtil.isAccessToken(accessToken)) {
			return "index";
		} else {
			return "member/login";
		}
	}

	//마이페이지
	@RequestMapping("/member/mypage")
	public ModelAndView mypage(@AuthenticationPrincipal CustomUserDetail me) {
		ModelAndView mv = new ModelAndView("member/mypage");

		// 공통: 요청한 심부름 내역
		int missionCount = missionService.getRequestHistoryCount(me.getMemberSeq());
		PagingDTO requestPaging = PagingDTO.of(1, 5, missionCount);
		List<MissionDTO> missionHistory = missionService.getRequestHistory(me.getMemberSeq(), requestPaging);

		mv.addObject("missionCount", missionCount);
		mv.addObject("mission", missionHistory);  // 철자 수정
		mv.addObject("history", "request");

		// 헬퍼인 경우: 수행한 심부름 내역 추가
		if(me.getRole().equals("ROLE_HELPER")) {
			int performanceCount = missionService.getMyPerformanceHistoryCount(me.getMemberSeq());
			PagingDTO performancePaging = PagingDTO.of(1, 5, performanceCount);
			List<MissionDTO> performanceHistory = missionService.getMyPerformanceHistory(me.getMemberSeq(), performancePaging);

			mv.addObject("performanceCount", performanceCount);
			mv.addObject("performance", performanceHistory);

		}

		return mv;
	}

	// 프로필 수정하기 전 비밀번호 확인 페이지로 이동
	@GetMapping("/member/mypage/verify")
	public String showVerifyPage() {
		return "member/pwdVerify";
	}


	//회원정보 수정 페이지 이동
	@GetMapping("/member/mypage/modify")
	public ModelAndView modify(@AuthenticationPrincipal CustomUserDetail me) {
		ModelAndView mv = new ModelAndView();

		HelperDTO member;
		if("ROLE_HELPER".equals(me.getRole())) {
			// helper 이미지까지 조회해온다.
			member = memberService.selectMemberWithImageBySeq(me.getMemberSeq());
		} else {
			member = memberService.selectMemberBySeq(me.getMemberSeq());
		}

		if(member != null) {
			mv.addObject("info", member);
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
