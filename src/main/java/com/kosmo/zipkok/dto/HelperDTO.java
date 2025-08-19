package com.kosmo.zipkok.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class HelperDTO extends MemberDTO{

	//헬퍼추가정보
	private String member_bank;
	private String member_account;
	private int member_vehicle;
	private String member_introduce;
	private String member_ofile;
	private String member_sfile;
	private int member_review;
	private int member_missionC;
	private int member_point;
}
