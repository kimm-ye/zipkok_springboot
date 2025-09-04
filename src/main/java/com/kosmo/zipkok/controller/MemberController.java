package com.kosmo.zipkok.controller;


import java.io.File;
import java.io.IOException;
import java.util.*;

import com.kosmo.zipkok.dto.HelperDTO;
import com.kosmo.zipkok.dto.TokenDTO;
import com.kosmo.zipkok.service.RedisService;
import com.kosmo.zipkok.dto.MemberDTO;
import com.kosmo.zipkok.service.MemberService;
<<<<<<< HEAD
import com.kosmo.zipkok.service.TokenService;
import com.kosmo.zipkok.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;


@Slf4j
@RestController
public class MemberController {

	@Autowired
	MemberService memberService;

	@Autowired
	RedisService redisService;

	@Autowired
	TokenService tokenService;

	// 회원가입
	@PostMapping(value="/member/join/action")
	public Map<String, Object> member(HelperDTO dto) throws Exception {
		Map<String, Object> result = new HashMap<>();

		try{
			System.out.println("memberDTO : " + dto);

			// 1. 해당하는 이메일이 존재하는지 체크
			boolean hasEmail = memberService.selectEmail(dto.getMemberEmail());

			// 2. 있으면 return 없으면 비밀번호 암호화해서 insert
			if(hasEmail) {
				result.put("success", false);
				result.put("message", "해당 이메일이 이미 존재합니다.");
			} else {
				memberService.insertMember(dto);
				result.put("success", true);
				result.put("message", "가입완료! 집콕에 오신것을 환영합니다^^");
				result.put("redirectUrl", "/zipkok");
			}

		} catch (Exception e) {
			e.printStackTrace();
			result.put("success", false);
			result.put("message", "회원가입 중 오류가 발생하였습니다.\n관리자에게 문의 바랍니다.");
		}

		return result;
	}

	// 아이디 중복체크
	@PostMapping("/member/join/check")
	public Map<String, Object> idCheck(@RequestParam("memberId") String memberId) {
	    String mId = memberService.idCheck(memberId);
	    boolean exists = (mId != null);

	    Map<String, Object> result = new HashMap<>();
	    result.put("exists", exists);
	    result.put("id", memberId);

	    return result;
	}


	// 로그인
	@PostMapping("/member/login/action")
	public Map<String, Object> login (@RequestBody Map<String, String> param, HttpServletResponse res) throws IOException {

		Map<String, Object> result = new HashMap<>();

		// 입력한 id, pass값을 비교해서 사용자자 정보 조회
		MemberDTO dto = memberService.authenticate(param.get("memberId"), param.get("memberPass"));

		// 성공시 Redis 세션 생성

		if (dto == null && "".equals(param.get("kakaoemail"))) {
			// 로그인 실패
			result.put("success", false);
			result.put("message", "아이디/비밀번호가 틀렸습니다.");
		}
		else if(dto != null && "".equals(param.get("kakaoemail"))) {

			// 로그인 성공
			TokenDTO tokens = redisService.saveTokenRedis(dto);  // JWT 토큰 생성 및 redis 저장

			CookieUtil.createCookie("accessToken", 15 * 60, "/", tokens.getAccessToken(), res); // 15분
			CookieUtil.createCookie("refreshToken", 7 * 24 * 60 * 60, "/", tokens.getRefreshToken(), res); // 7일

			result.put("success", true);
			result.put("memberId", dto.getMemberId());
			result.put("memberName", dto.getMemberName());
			result.put("message", "로그인 성공!");
		}

		// ==================== 카카오 로그인 ========================
		/*else if (!"".equals(param.get("kakaoemail"))) {

			// kakaoemail을 kakaoid에 저장
			String kakaoid = param.get("kakaoemail");
			//System.out.println("kakaoid : "+kakaoid);

			// 카카오계정으로 로그인한 적이 있는지 없는지
//			MemberDTO result = sqlSession.getMapper(MemberImpl.class).kakaoLogin(req.getParameter("kakaoemail"));
//			MemberDTO result = new MemberDTO();
			//System.out.println(result);

			if (result == null) { // 회원이 아닌경우 (카카오 계정으로 처음 방문한 경우) 카카오 회원정보 설정 창으로 이동
				//System.out.println("카카오 회원 정보 설정한다");
				resp.setContentType("text/html; charset=UTF-8");
				PrintWriter out = resp.getWriter();
				String alertText = "등록된 정보가 없어 회원가입페이지로 이동합니다.";
				out.println("<script>alert('" + alertText + "');</script> ");
				out.flush();

				req.setAttribute("kakaoemail", req.getParameter("kakaoemail"));
				req.setAttribute("kakaoname", req.getParameter("kakaoname"));

				//mv.setViewName("member/join_Kakao");
				//return mv;

			} else { // 이미 카카오로 로그인한 적이 있을 때 (최초 1회 로그인때 회원가입된 상태)
				String accessToken = sessionService.createSession(dto);  // JWT 토큰 반환
				result.put("token", accessToken);                       // 응답에 JWT 토큰 포함
				result.put("message", "로그인 성공");
			}
		}*/

		return result;
	}

	// 아이디 찾기
	@PostMapping("/member/find/id")
	public String findId(@RequestParam Map<String, String> param) {

		String name = param.get("name");
		String email = param.get("email_1")+"@"+param.get("email_2");

		Map<String, String> info = new HashMap<>();
		info.put("name", name);
		info.put("email", email);

		return memberService.findId(info);
	}

	// 패스워드 찾기
	@PostMapping("/member/find/pw")
	public String findPwd( @RequestBody Map<String, String> param) {

		String email = param.get("email_1")+"@"+param.get("email_2");

		Map<String, String> info = new HashMap<>();
		info.put("mid", param.get("id"));
		info.put("name", param.get("name"));
		info.put("email", email);

		return memberService.findPwd(info);
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


	//회원정보수정
	@PatchMapping("/member/mypage/modify/action")
	public Map<String, Object> modify(HelperDTO dto) throws Exception {
		Map<String, Object> result = new HashMap<>();

		try{
			// 정보 업데이트
			memberService.updateMember(dto);

			result.put("success", true);
			result.put("message", "수정완료!");
			result.put("redirectUrl", "./");

		} catch (Exception e) {
			e.printStackTrace();
			result.put("success", false);
			result.put("message", "정보수정 중 오류가 발생하였습니다.\n관리자에게 문의 바랍니다.");
		}
		return result;
	}


	@RequestMapping("/myUserPageAction.do")
	public String myUserPageAction(HttpSession session, HttpServletRequest req, MemberDTO memberDTO, Model model) {

		String id = (String)session.getAttribute("Id");
		int status = (Integer)session.getAttribute("UserStatus");

		memberDTO.setMemberId(id);
		memberDTO.setMemberStatus(status);
		memberDTO.setMemberEmail(req.getParameter("email_1") + "@" + req.getParameter("email_2"));


//		sqlSession.getMapper(MemberImpl.class).userMyPage(memberDTO);

		model.addAttribute("msg", "회원정보 변경완료");


		return "member/changeAlert";
	}


    //회원탈퇴
    @RequestMapping("/memberDelete.do")
    public String delete(HttpServletRequest req, HttpSession session) {
        //로그인 확인
        if(session.getAttribute("siteUserInfo")==null){
            return "redirect:login.do";
        }

//        sqlSession.getMapper(MemberImpl.class).memberDelete(
//            ((MemberDTO)session.getAttribute("siteUserInfo")).getMember_id()
//        );
        return "member/memberDelete";
    }

	// 로그아웃시 토큰 초기화
	@PostMapping("/member/logout/action")
	public ModelAndView logout(HttpServletResponse res) {

		CookieUtil.deleteCookie("accessToken", "/", res);
		CookieUtil.deleteCookie("refreshToken", "/", res);

	    return new ModelAndView("redirect:/");
	}


  //앱 채팅
//  	@RequestMapping("/android/chatList.do")
//  	@ResponseBody
//  	public ArrayList<MissionDTO> chatList(HttpServletRequest req, MissionDTO missionDTO) {
//  		System.out.println("안드로이드 채팅 리스트 요청");
//
//  		ArrayList<MissionDTO> lists =
//  				sqlSession.getMapper(IAndroidDAO.class).chatList(missionDTO);
//
//  		return lists;
//  	}


}
