package com.kosmo.zipkok.service.impl;

import com.kosmo.zipkok.dao.MemberDAO;
import com.kosmo.zipkok.dto.HelperDTO;
import com.kosmo.zipkok.dto.ImageDTO;
import com.kosmo.zipkok.service.MemberService;
import com.kosmo.zipkok.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
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
	public ImageDTO selectMemberImage(String memberSeq) {

		try {
			// fix : seq로 이미지 조회 resultType을 byte로 하니 오류가 나서 dto로 변경함
			ImageDTO result = memberDao.selectMemberImage(memberSeq);

			if (result != null) {
				Object imageObj = result.getImageFile();

				// 이미지가 byte[] 타입인지 확인
				if (imageObj instanceof byte[] imageFile) {
                    // log.debug("✅ 이미지 발견: size={}KB", imageFile.length / 1024);
					return result;
				}
			}

			//log.debug("⚠️ 이미지 없음: memberSeq={}", memberSeq);
			return null;

		} catch (Exception e) {
			log.error("❌ 이미지 조회 실패: memberSeq={}, error={}", memberSeq, e.getMessage(), e);
			return null;
		}
	}

	@Override
	public Map<String, String> selectMemberBasicInfo(String memberSeq) {
		return memberDao.selectMemberBasicInfo(memberSeq);
	}

	@Override
	public void insertMember(HelperDTO dto, MultipartFile profileImage) throws IOException {

		try {
			// 패스워드 security 사용해서 BCrypt 암호화 (고정 60자)
			String encryptPwd = passwordEncoder.encode(dto.getMemberPass());
			dto.setMemberPass(encryptPwd);

			memberDao.insertMember(dto);

			// 3. 프로필 이미지 저장 (있으면)
			if(profileImage != null && profileImage.getSize() > 0) {
				saveProfileImage(dto.getMemberSeq(), profileImage);
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
	public void insertSnsMember(HelperDTO dto, Map<String, String> snsInfo, MultipartFile profileImage) throws IOException {
		try {
			// SNS 로그인은 비밀번호 없음
			String encryptPwd = passwordEncoder.encode(dto.getMemberPass());
			dto.setMemberPass(encryptPwd);

			// member 테이블 저장
			memberDao.insertMember(dto);

			snsInfo.put("memberSeq", dto.getMemberSeq());
			memberDao.insertSnsLogin(snsInfo);

			if(profileImage != null && profileImage.getSize() > 0) {
				saveProfileImage(dto.getMemberSeq(), profileImage);
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


	// 프로필 이미지 저장
	private void saveProfileImage(String memberSeq, MultipartFile profileImage) {
		try {
			ImageDTO imageDTO = createImageDTO(profileImage);

			// DB 저장
			memberDao.insertMemberImage(memberSeq, imageDTO);

			// 이미지 버전 초기화
			memberDao.initImageVersion(memberSeq);

		} catch (IOException e) {
			log.error("프로필 이미지 저장 실패: memberSeq={}", memberSeq, e);
			throw new RuntimeException("프로필 이미지 저장 실패", e);
		}
	}


	/* 회원정보 수정 */
	@Override
	public void updateMember(HelperDTO dto, MultipartFile profileImage) throws IOException {
		try{
			if(!"".equals(dto.getMemberPass()) && dto.getMemberPass() != null) {
				// 패스워드 security 사용해서 BCrypt 암호화 (고정 60자)
				String encryptPwd = passwordEncoder.encode(dto.getMemberPass());
				dto.setMemberPass(encryptPwd);
			}
			// 이메일, 핸드폰, 비밀번호만 변경가능
			memberDao.updateMember(dto);

			if(profileImage.getSize() > 0) {

				ImageDTO imageDTO = createImageDTO(profileImage);

				int updateCnt = memberDao.updateMemberImage(dto.getMemberSeq(), imageDTO);
				if (updateCnt < 1) {
					memberDao.insertMemberImage(dto.getMemberSeq(), imageDTO);
				}

				memberDao.incrementImageVersion(dto.getMemberSeq());
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

	private ImageDTO createImageDTO(MultipartFile profileImage) throws IOException {
		ImageDTO imageDTO = new ImageDTO();
		imageDTO.setAttachFile(profileImage);
		imageDTO.setImageFile(profileImage.getBytes());

		String fileName = profileImage.getOriginalFilename();
		String fileEtx = StringUtils.getFilenameExtension(fileName);
		String originalName = StringUtils.stripFilenameExtension(fileName);

		imageDTO.setImageFileName(originalName);
		imageDTO.setImageFileEtx(fileEtx);
		return imageDTO;
	}

	/* 회원탈퇴 -  상태값만 변경 */
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
