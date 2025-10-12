package com.kosmo.zipkok.controller;

import com.kosmo.zipkok.dto.MissionDTO;
import com.kosmo.zipkok.dto.MissionFileDTO;
import com.kosmo.zipkok.dto.PagingDTO;
import com.kosmo.zipkok.service.MissionService;
import com.kosmo.zipkok.util.CookieUtil;
import com.kosmo.zipkok.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class ImageController {
	@Autowired
	MissionService missionService;

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
}
