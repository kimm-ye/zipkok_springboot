package com.kosmo.zipkok.controller;

import com.kosmo.zipkok.dto.CustomUserDetail;
import com.kosmo.zipkok.service.MissionService;
import com.kosmo.zipkok.service.NoticeService;
import com.kosmo.zipkok.util.CookieUtil;
import com.kosmo.zipkok.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Controller
public class NoticeViewController {

	@Autowired
	JwtUtil jwtUtil;

	@Autowired
	NoticeService noticeService;


	// 공지사항 페이지 이동
	@RequestMapping("/notice")
	public ModelAndView notice(@CookieValue(value = "accessToken", required = false) String accessToken) {

		ModelAndView mv = new ModelAndView("board/notice");

		if (accessToken != null && !accessToken.isBlank()) {
			try {
				String role = jwtUtil.getRoleFromToken(accessToken);
				mv.addObject("role", role);
			} catch (Exception e) {
				// 토큰 파싱 실패 시 안전하게 무시하거나 로그 남기기
				log.info(e.getMessage());
			}
		}

		return mv;
	}

	// 공지사항 작성 페이지 이동
	@RequestMapping("/notice/write")
	public String write(){
		return "board/noticeWrite";
	}

	// 상세 페이지로 이동
	@RequestMapping("/notice/detail")
	public String detail(@RequestParam("noticeId") String noticeId,
						 @AuthenticationPrincipal CustomUserDetail me, Model model){

		boolean isAdmin = false;

		if(me != null) {
			if("ROLE_ADMIN".equals(me.getRole())) {
				isAdmin = true;
			}
		}


		System.out.println("isAdmin ===== " + isAdmin);

        model.addAttribute("mode", "view");
		model.addAttribute("noticeId", noticeId);
		model.addAttribute("isAdmin", isAdmin);
		return "board/noticeWrite";
	}

}
