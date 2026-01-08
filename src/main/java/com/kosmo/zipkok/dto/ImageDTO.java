package com.kosmo.zipkok.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ImageDTO {

	// 업로드용
	private MultipartFile attachFile;

	// DB 저장/조회용
	private byte[] imageFile;
	private String imageFileName;
	private String imageFileEtx;

	// 편의 메서드
	public String getFullImageName() {
		if (imageFileName != null && imageFileEtx != null) {
			return imageFileName + "." + imageFileEtx;
		}
		return null;
	}
}
