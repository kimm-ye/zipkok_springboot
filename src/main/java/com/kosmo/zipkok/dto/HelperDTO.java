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


	public String getImageUrl() {
		return "/img/profile/" + getFullImageName();
	}

}
