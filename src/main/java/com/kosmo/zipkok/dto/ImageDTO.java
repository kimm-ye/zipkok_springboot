package com.kosmo.zipkok.dto;

import lombok.Data;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ImageDTO {

	private byte[] imageFile;
	private String imageFileName;
	private String imageFileEtx;
}
