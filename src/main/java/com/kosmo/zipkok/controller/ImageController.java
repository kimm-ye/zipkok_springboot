package com.kosmo.zipkok.controller;

import com.kosmo.zipkok.dto.*;
import com.kosmo.zipkok.service.MemberService;
import com.kosmo.zipkok.service.MissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
public class ImageController {
	@Autowired
	MissionService missionService;

	@Autowired
	MemberService memberService;

	// 심부름에 첨부한 파일 다운로드
	@GetMapping("/mission/image/{missionSeq}")
	public ResponseEntity<byte[]> downloadMissionImage(@PathVariable("missionSeq") String missionSeq) {
		try {
			// 해당하는 이미지 파일만 조회
			MissionFileDTO mission = missionService.getMissionImage(missionSeq);

			if (mission == null || mission.getMissionImageFile() == null) {
				return ResponseEntity.notFound().build();
			}

			HttpHeaders headers = new HttpHeaders();

			// Content-Type 설정
			String ext = mission.getMissionImageFileEtx();
			if (ext != null) {
				switch (ext.toLowerCase()) {
					case "jpg":
					case "jpeg":
						headers.setContentType(MediaType.IMAGE_JPEG);
						break;
					case "png":
						headers.setContentType(MediaType.IMAGE_PNG);
						break;
					case "gif":
						headers.setContentType(MediaType.IMAGE_GIF);
						break;
					default:
						headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
				}
			}

			// 파일명 설정 (다운로드용)
			String fileName = mission.getFullMissionImageName();
			String encodedFileName = UriUtils.encode(fileName, StandardCharsets.UTF_8);

			// Content-Disposition 헤더를 직접 설정
			headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encodedFileName);
			headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

			return new ResponseEntity<>(mission.getMissionImageFile(), headers, HttpStatus.OK);


		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * 프로필 이미지 제공
	 *
	 * URL: /image/profile/{memberSeq}
	 * 반환: 이미지 바이트 배열 (JPEG, PNG 등)
	 * 캐싱: 1시간 (브라우저 캐시)
	 */
	@GetMapping(
			value = "/member/image/profile",
			produces = MediaType.ALL_VALUE
	)
	public ResponseEntity<byte[]> getProfileImage(@AuthenticationPrincipal CustomUserDetail me) {
		try {

			if (me == null) {
				return getErrorImage();
			}

			// 1. 이미지 파일 조회
			byte[] imageFile = memberService.selectMemberImage(me.getMemberSeq());
			System.out.println(imageFile.length);

			// 2. DB에 이미지가 있는 경우
			if (imageFile != null && imageFile.length > 0) {

				/*HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.IMAGE_JPEG); // 확장자 없으면 고정이 안전
				headers.setCacheControl(
						CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic()
				);*/

				log.debug("✅ DB 프로필 이미지 제공: memberSeq={}, size={}KB",
						me.getMemberSeq(), imageFile.length / 1024);

				//return new ResponseEntity<>(imageFile, headers, HttpStatus.OK);
				return new ResponseEntity<>(imageFile, HttpStatus.OK);
			}

			// 3. 이미지 없으면 기본 이미지
			byte[] defaultImage = getDefaultImageBytes();

			/*HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.IMAGE_PNG);
			headers.setCacheControl(
					CacheControl.maxAge(24, TimeUnit.HOURS)
							.cachePublic()
			);*/

			//return new ResponseEntity<>(defaultImage, headers, HttpStatus.OK);
			return new ResponseEntity<>(defaultImage, HttpStatus.OK);

		} catch (Exception e) {
            assert me != null;
            log.error("❌ 프로필 이미지 조회 실패: memberSeq={}, error={}",
					me.getMemberSeq(), e.getMessage());

			// 에러 시 기본 이미지
			return getErrorImage();
		}
	}

	/**
	 * 파일 확장자 → MediaType 변환
	 */
	private MediaType getMediaType(String extension) {
		if (extension == null) return MediaType.IMAGE_PNG;

		switch (extension.toLowerCase()) {
			case "jpg":
			case "jpeg":
				return MediaType.IMAGE_JPEG;
			case "png":
				return MediaType.IMAGE_PNG;
			case "gif":
				return MediaType.IMAGE_GIF;
			case "webp":
				return MediaType.parseMediaType("image/webp");
			default:
				return MediaType.IMAGE_PNG;
		}
	}

	/**
	 * static 폴더의 이미지 읽기
	 */
	private byte[] getDefaultImageBytes() throws IOException {
		ClassPathResource resource = new ClassPathResource("static/img/profile/default.png");
		return resource.getInputStream().readAllBytes();
	}

	/**
	 * 에러 발생 시 기본 이미지 반환
	 */
	private ResponseEntity<byte[]> getErrorImage() {
		try {
			byte[] errorImage = getDefaultImageBytes();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.IMAGE_PNG);
			headers.setCacheControl(
					CacheControl.maxAge(1, TimeUnit.HOURS)
							.cachePublic()
			);
			return new ResponseEntity<>(errorImage, headers, HttpStatus.OK);
		} catch (IOException e) {
			log.error("❌ 기본 이미지도 로드 실패: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

}
