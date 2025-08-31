package com.kosmo.zipkok.service.impl;

import com.kosmo.zipkok.dao.MemberDAO;
import com.kosmo.zipkok.dto.HelperDTO;
import com.kosmo.zipkok.dto.MemberDTO;
import com.kosmo.zipkok.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class MemberServiceImpl implements MemberService {

	@Autowired
	private MemberDAO memberDao;  // final 필드

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public boolean selectEmail(String email) {
		return memberDao.selectEmail(email);
	}


	@Override
	public MemberDTO authenticate(String inputId, String inputPwd) {
	    try {

	        MemberDTO member = memberDao.selectMemberById(inputId);

			System.out.println("member : " + member);

	        if (member != null) {

	            // 비밀번호 비교: 원본 vs 암호화된 비밀번호
	            boolean isMatch = passwordEncoder.matches(inputPwd, member.getMemberPass());

	            if (isMatch) {
	                System.out.println("로그인 성공: " + inputId);
	                return member;
	            } else {
	                System.out.println("비밀번호 불일치: " + inputId);
	            }
	        } else {
	            System.out.println("사용자 없음: " + inputId);
	        }

	        return null;

	    } catch (Exception e) {
	        e.printStackTrace();
	        throw e;
	    }
	}


	@Override
	public String idCheck(String id) {
		return memberDao.idCheck(id);
	}

	@Override
	public String findId(Map<String, String> param) {
		return memberDao.findId(param);
	}

	@Override
	public String findPwd(Map<String, String> param) {
		return memberDao.findPwd(param);
	}

	@Override
	public HelperDTO selectMemberById(String memberId) {
		return  memberDao.selectMemberById(memberId);
	}

	@Override
	public void insertMember(HelperDTO dto) throws IOException {

		try {
			// 패스워드 security 사용해서 BCrypt 암호화 (고정 60자)
			String encryptPwd = passwordEncoder.encode(dto.getMemberPass());
			dto.setMemberPass(encryptPwd);

			memberDao.insertMember(dto);

			// 헬퍼인 경우 helper 테이블 저장
			if(dto.getMemberStatus() == 2) {
				memberDao.insertHelper(dto);

				if(dto.getAttachFile().getSize() > 0) {
					String fileName = dto.getAttachFile().getOriginalFilename();
					String fileEtx = StringUtils.getFilenameExtension(fileName); // 파일 확장자
					String originalName = StringUtils.stripFilenameExtension(fileName); // 확장자 제외한 파일 이름만

					dto.setImageFile(dto.getAttachFile().getBytes());
					dto.setImageFileName(originalName);
					dto.setImageFileEtx(fileEtx);

					memberDao.insertHelperImage(dto);
				}
			}
		} catch (Exception e){
			e.printStackTrace();
			throw e; // 컨트롤러에서 예외처리 하기 위함
		}
	}


}
