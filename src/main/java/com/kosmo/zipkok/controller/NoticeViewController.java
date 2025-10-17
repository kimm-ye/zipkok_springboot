package com.kosmo.zipkok.controller;

import com.kosmo.zipkok.service.MissionService;
import com.kosmo.zipkok.service.NoticeService;
import com.kosmo.zipkok.util.CookieUtil;
import com.kosmo.zipkok.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
	public ModelAndView notice(HttpServletRequest request) {

		ModelAndView mv = new ModelAndView("board/notice");

		String token = CookieUtil.getCookieValue(request, "accessToken");

		if (token != null && !token.isBlank()) {
			try {
				String role = jwtUtil.getRoleFromToken(token);
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

}
