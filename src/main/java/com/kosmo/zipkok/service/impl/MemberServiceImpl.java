package com.kosmo.zipkok.service.impl;

import com.kosmo.zipkok.dao.MemberDAO;
import com.kosmo.zipkok.dto.HelperDTO;
import com.kosmo.zipkok.service.MemberService;
import com.kosmo.zipkok.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;

@Service
@Transactional
public class MemberServiceImpl implements MemberService {

	@Autowired
	private MemberDAO memberDao;  // final 필드

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtUtil jwtUtil;

	@Override
	public boolean selectEmail(String email) {
		return memberDao.selectEmail(email);
	}


	@Override
	public HelperDTO authenticate(String inputId, String inputPwd) {
		try {
			HelperDTO member = memberDao.selectMemberById(inputId);
			if (member == null) {
				throw new RuntimeException("사용자 없음: " + inputId);
			}

			boolean isMatch = passwordEncoder.matches(inputPwd, member.getMemberPass());
			if (!isMatch) {
				throw new RuntimeException("비밀번호 불일치: " + inputId);
			}

			return member;

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public HelperDTO selectSnsLogin(Map<String, String> param) {
		return memberDao.selectSnsLogin(param);
	}


	@Override
	public String idCheck(String id) {
		return memberDao.idCheck(id);
	}

	// helper 이미지까지조회
	@Override
	public HelperDTO selectMemberWithImageBySeq(String memberSeq) {
		return memberDao.selectMemberWithImageBySeq(memberSeq);
	}

	// member 정보만 죄회
	@Override
	public HelperDTO selectMemberBySeq(String memberSeq) {
		return memberDao.selectMemberBySeq(memberSeq);
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
	public String findPwdBySeq(String memberSeq) {
		return memberDao.findPwdBySeq(memberSeq);
	}

	@Override
	public void insertMember(HelperDTO dto) throws IOException {

		try {
			// 패스워드 security 사용해서 BCrypt 암호화 (고정 60자)
			String encryptPwd = passwordEncoder.encode(dto.getMemberPass());
			dto.setMemberPass(encryptPwd);

			memberDao.insertMember(dto);

			if(dto.getAttachFile().getSize() > 0) {
				String fileName = dto.getAttachFile().getOriginalFilename();
				String fileEtx = StringUtils.getFilenameExtension(fileName); // 파일 확장자
				String originalName = StringUtils.stripFilenameExtension(fileName); // 확장자 제외한 파일 이름만

				dto.setImageFile(dto.getAttachFile().getBytes());
				dto.setImageFileName(originalName);
				dto.setImageFileEtx(fileEtx);

				memberDao.insertImageImage(dto);
			}

			// 헬퍼인 경우 helper 테이블 저장
			if(dto.getMemberStatus() == 2) {
				memberDao.insertHelper(dto);
			}

		} catch (Exception e){
			e.printStackTrace();
			throw e; // 컨트롤러에서 예외처리 하기 위함
		}
	}

	@Override
	public void insertSnsMember(HelperDTO dto, Map<String, String> snsInfo) throws IOException {
		try {
			// SNS 로그인은 비밀번호 없음
			String encryptPwd = passwordEncoder.encode(dto.getMemberPass());
			dto.setMemberPass(encryptPwd);

			// member 테이블 저장
			memberDao.insertMember(dto);

			snsInfo.put("memberSeq", dto.getMemberSeq());
			memberDao.insertSnsLogin(snsInfo);

			if(dto.getAttachFile().getSize() > 0) {
				String fileName = dto.getAttachFile().getOriginalFilename();
				String fileEtx = StringUtils.getFilenameExtension(fileName); // 파일 확장자
				String originalName = StringUtils.stripFilenameExtension(fileName); // 확장자 제외한 파일 이름만

				dto.setImageFile(dto.getAttachFile().getBytes());
				dto.setImageFileName(originalName);
				dto.setImageFileEtx(fileEtx);

				memberDao.insertImageImage(dto);
			}

			// 헬퍼인 경우 추가 정보 저장
			if(dto.getMemberStatus() == 2) {
				memberDao.insertHelper(dto);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public void updateMember(HelperDTO dto) throws IOException {
		try{
			if(!"".equals(dto.getMemberPass()) && dto.getMemberPass() != null) {
				// 패스워드 security 사용해서 BCrypt 암호화 (고정 60자)
				String encryptPwd = passwordEncoder.encode(dto.getMemberPass());
				dto.setMemberPass(encryptPwd);
			}
			memberDao.updateMember(dto);

			if(dto.getAttachFile().getSize() > 0) {
				String fileName = dto.getAttachFile().getOriginalFilename();
				String fileEtx = StringUtils.getFilenameExtension(fileName); // 파일 확장자
				String originalName = StringUtils.stripFilenameExtension(fileName); // 확장자 제외한 파일 이름만

				dto.setImageFile(dto.getAttachFile().getBytes());
				dto.setImageFileName(originalName);
				dto.setImageFileEtx(fileEtx);

				int updateCnt = memberDao.updateHelperImage(dto);
				if (updateCnt < 1) {
					memberDao.insertImageImage(dto);
				}
			}

			// 헬퍼인 경우 helper 테이블 수정
			if(dto.getMemberStatus() == 2) {
				memberDao.updateHelper(dto);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw e; // 컨트롤러에서 예외처리 하기 위함
		}
	}

	@Override
	public void deleteMember(String memberSeq) throws Exception {

		try {
			memberDao.deleteMember(memberSeq);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
