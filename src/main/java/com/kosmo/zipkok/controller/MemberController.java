package com.kosmo.zipkok.controller;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import com.kosmo.zipkok.service.SessionService;
import com.kosmo.zipkok.dto.MemberDTO;
import com.kosmo.zipkok.service.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;


@Slf4j
@RestController
public class MemberController {

	@Autowired
	MemberService memberService;

	@Autowired
    SessionService sessionService;

	// 회원가입
	@PostMapping(value="/member/join/action")
	public Map<String, Object> member(MemberDTO dto) throws Exception {
		Map<String, Object> result = new HashMap<>();

		try{
			System.out.println("memberDTO : " + dto);

			// 1. 해당하는 이메일이 존재하는지 체크
			boolean hasEmail = memberService.selectEmail(dto.getMemberEmail());
			System.out.println("hasEmail : " + hasEmail);

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
	public ModelAndView idCheck(@RequestParam("memberId") String memberId) {
		String mId = memberService.idCheck(memberId);
		boolean result = (mId != null);
		System.out.println(result);

		ModelAndView mv = new ModelAndView();
		mv.setViewName("member/idCheck");
		mv.addObject("idCheckResult", result);
		mv.addObject("id", memberId);

		return mv;
	}

	//헬퍼 회원가입(이미지업로드)
	@RequestMapping(value="/helper.do", method=RequestMethod.POST)
	public String helper(MemberDTO memberDTO, MultipartHttpServletRequest req, Model model) throws Exception {

		//물리적 경로 얻어오기
		String path = req.getSession().getServletContext().getRealPath("/resources/upload");
		MultipartFile mfile = null;
		// 파일정보를 저장한 Map컬렉션을 2개이상 저장하기 위한 용도의 List컬렉션
		List<Object> resultList = new ArrayList<Object>();

		try {

			//업로드폼의 file속성의 필드를 가져온다. (여기서는 2개임)
			Iterator itr = req.getFileNames();

			//갯수만큼 반복
			while(itr.hasNext()) {
				//전송된 파일명을 읽어온다.
				mfile = req.getFile(itr.next().toString());

				//한글깨짐방지 처리 후 전송된 파일명을 가져온다.
				String originalName = new String(mfile.getOriginalFilename().getBytes(), "UTF-8");

				//서버로 전송된 파일이 없다면 파일없이 서버에 저장

				if("".equals(originalName)) continue;

				String ext = originalName.substring(originalName.lastIndexOf('.'));
				//UUID를 통해 생성된 문자열과 확장자를 결합해서 파일명을 완성한다.
				String saveFileName = getUuid()	+ ext;

				//물리적 경로에 새롭게 생성된 파일명으로 파일 저장
				mfile.transferTo(new File(path + File.separator + saveFileName));

//				memberDTO.setMember_ofile(originalName);
//				memberDTO.setMember_sfile(saveFileName);

			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		memberDTO.setMemberEmail(req.getParameter("email_1") + "@" + req.getParameter("email_2"));
//		sqlSession.getMapper(MemberImpl.class).helper(memberDTO);

		return "redirect:welcomAlert.do";
	}

	//서버 업로드를 위한 메소드
	public static String getUuid() {
		String uuid = UUID.randomUUID().toString();
		System.out.println("생성된UUID-1:"+uuid);

		return uuid;
	}

	// 로그인
	@PostMapping("/member/login/action")
	public Map<String, Object> login (@RequestBody Map<String, String> param,
            HttpServletRequest req, HttpSession session, HttpServletResponse res) throws IOException {

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
			String accessToken = sessionService.createSession(dto);  // JWT 토큰 반환

			Cookie jwtCookie = new Cookie("loginCookie", accessToken);
			jwtCookie.setHttpOnly(true);
			jwtCookie.setSecure(false); // HTTPS 환경에서는 true로 설정
			jwtCookie.setMaxAge(60*60*24); //쿠키 유효 기간: 하루로 설정(60초 * 60분 * 24시간)
			jwtCookie.setPath("/"); //모든 경로에서 접근 가능하도록 설정
			res.addCookie(jwtCookie); //response에 Cookie 추가

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

	@PostMapping("/member/find/id")
	public String findId(@RequestParam Map<String, String> param) {

		String name = param.get("name");
		String email = param.get("email_1")+"@"+param.get("email_2");

		Map<String, String> info = new HashMap<>();
		info.put("name", name);
		info.put("email", email);

		return memberService.findId(info);
	}

	@PostMapping("/member/find/pw")
	public String findPwd( @RequestBody Map<String, String> param) {

		String email = param.get("email_1")+"@"+param.get("email_2");

		Map<String, String> info = new HashMap<>();
		info.put("mid", param.get("id"));
		info.put("name", param.get("name"));
		info.put("email", email);

		return memberService.findPwd(info);
	}

	//회원정보 수정(입장)
	@RequestMapping("/myPage.do")
	public String change(HttpServletRequest req, Model model, HttpSession session) {


		String id = (String)session.getAttribute("Id");

		// ArrayList<MemberDTO> memberList = sqlSession.getMapper(MemberImpl.class).getMemberInfo(id);
		ArrayList<MemberDTO> memberList = new ArrayList<>();
		for(MemberDTO dto : memberList) {

			String email = dto.getMemberEmail();

			int idx = email.indexOf("@");

			String email_1 = email.substring(0, idx);
			String email_2 = email.substring(idx+1);

			model.addAttribute("email_1", email_1);
			model.addAttribute("email_2", email_2);
		}

		model.addAttribute("dto", memberList);


		return "member/memberInfo";
	}


	//회원정보수정 (헬퍼)
	@RequestMapping("/myHelperPageAction.do")
	public String myHelperPageAction(HttpSession session, MultipartHttpServletRequest req, MemberDTO memberDTO, Model model) {

		String id = (String)session.getAttribute("Id");
		int status = (Integer)session.getAttribute("UserStatus");

		//물리적 경로 얻어오기
		String path = req.getSession().getServletContext().getRealPath("/resources/upload");
		MultipartFile mfile = null;
		// 파일정보를 저장한 Map컬렉션을 2개이상 저장하기 위한 용도의 List컬렉션
		List<Object> resultList = new ArrayList<Object>();

		try {

			//업로드폼의 file속성의 필드를 가져온다. (여기서는 2개임)
			Iterator itr = req.getFileNames();

			//갯수만큼 반복
			while(itr.hasNext()) {
				//전송된 파일명을 읽어온다.
				mfile = req.getFile(itr.next().toString());

				//한글깨짐방지 처리 후 전송된 파일명을 가져온다.
				String originalName = new String(mfile.getOriginalFilename().getBytes(), "UTF-8");

				//서버로 전송된 파일이 없다면 파일없이 서버에 저장
				if("".equals(originalName)) continue;

				String ext = originalName.substring(originalName.lastIndexOf('.'));
				//UUID를 통해 생성된 문자열과 확장자를 결합해서 파일명을 완성한다.
				String saveFileName = getUuid()	+ ext;

				//물리적 경로에 새롭게 생성된 파일명으로 파일 저장
				mfile.transferTo(new File(path + File.separator + saveFileName));

//				memberDTO.setMember_ofile(originalName);
//				memberDTO.setMember_sfile(saveFileName);
//
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}


		memberDTO.setMemberId(id);
		memberDTO.setMemberStatus(status);
		memberDTO.setMemberEmail(req.getParameter("email_1") + "@" + req.getParameter("email_2"));

//		sqlSession.getMapper(MemberImpl.class).helperMyPage(memberDTO);

		model.addAttribute("msg", "회원정보 변경완료");

		return "member/changeAlert";

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

	@PostMapping("/member/logout/action")
	public ModelAndView logout(HttpServletRequest req, HttpServletResponse res) {
	    Cookie loginCookie = WebUtils.getCookie(req, "loginCookie");

	    if (loginCookie != null) {
	        String jwtToken = loginCookie.getValue();
	        sessionService.deleteSession(jwtToken); // redis까지 잘 삭제됨

	        Cookie cookie = new Cookie("loginCookie", null);
	        cookie.setMaxAge(0);
	        cookie.setPath("/");
	        res.addCookie(cookie);
	    }

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
