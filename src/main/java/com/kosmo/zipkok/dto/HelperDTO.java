package com.kosmo.zipkok.dto;

import lombok.Data;
import lombok.ToString;

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

	private ImageDTO imageDTO;

}
