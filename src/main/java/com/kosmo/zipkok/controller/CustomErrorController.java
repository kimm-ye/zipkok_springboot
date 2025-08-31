package com.kosmo.zipkok.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 전역 에러 처리 컨트롤러
 *
 * Spring Boot의 기본 에러 처리를 커스터마이징
 */
@Controller
public class CustomErrorController implements ErrorController {

	@RequestMapping("/error")
	public String handleError(HttpServletRequest request, Model model) {
		// 에러 상태 코드 가져오기
		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

		if (status != null) {
			Integer statusCode = Integer.valueOf(status.toString());
			System.out.println("에러 상태 코드: " + statusCode);

			switch (statusCode) {
				case 404:
					System.out.println("404 에러 → 404 페이지로 이동");
					return "error/404";  // 404 에러 페이지
				case 403:
					System.out.println("403 에러 (JWT 인증 실패) → index로 리다이렉트");
					return "redirect:/"; // 403은 index로 리다이렉트
				case 401:
					System.out.println("401 에러 (인증 필요) → index로 리다이렉트");
					return "redirect:/"; // 401도 index로 리다이렉트
				case 500:
					return "error/500";  // 500 에러 페이지
				default:
					return "error/general"; // 기타 에러
			}
		}

		return "error/general";
	}
}