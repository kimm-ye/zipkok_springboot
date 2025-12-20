package com.kosmo.zipkok.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@Data
@ToString(callSuper=true) // extends한 dto도 출력하기 위함
public class HelperDTO extends MemberDTO{

	//헬퍼추가정보
	private String memberBank;
	private String memberAccount;
	private int memberVehicle;
	private String memberIntroduce;

	private int memberReview;
	private int memberMissionC;
	private int memberPoint;

	private MultipartFile attachFile; // 업로드한 프로필 사진 - MultipartFile은 임시 메모리나 임시 파일에 저장된 상태
	private byte[] imageFile; // 실제 파일 내용을 바이트 배열로 읽어옴
	private String imageFileName;
	private String imageFileEtx;

	public String getFullImageName() {
		if (imageFileName != null && imageFileEtx != null) {
			return imageFileName + "." + imageFileEtx;
		}

		// 기본 이미지 처리
		switch (getMemberGender()) {
			case 0:
				return "default_male.png";
			case 1:
				return "default_female.png";
			default:
				return "default.png";
		}
	}

	// 헬퍼에 프로필 이미지가 있는 경우 가져오고 없으면 default 사진을 사용한다.
	public String getImageUrl() {

		System.out.println("123123 === " + (imageFile != null ? imageFile.length : 0));

		// imageFile이 null이면 빈 배열로 초기화
		byte[] fileData = (imageFile != null) ? imageFile : new byte[0];

		if (fileData.length > 0) {
			String base64 = java.util.Base64.getEncoder().encodeToString(fileData);
			String contentType = getContentType(imageFileEtx);
			return "data:" + contentType + ";base64," + base64;
		}

		// 기본 이미지 반환
		return "/img/profile/default.png";
	}


	private String getContentType(String extension) {
		if (extension == null) return "image/png";

		switch (extension.toLowerCase()) {
			case "jpg":
			case "jpeg": return "image/jpeg";
			case "png": return "image/png";
			case "gif": return "image/gif";
			case "webp": return "image/webp";
			default: return "image/png";
		}
	}

}
