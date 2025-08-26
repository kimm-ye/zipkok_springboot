package com.kosmo.zipkok.dto;

import lombok.Data;
@Data
public class MemberDTO {

	private String memberId; //아이디
	private String memberPass; //패스워드
	private String memberName; //이름
	private String memberEmail; //이메일
	private String memberAge; //연령대
	private int memberSex; // 성별
	private String memberPhone;
	private int memberMissionN;
	private int memberStatus;  // 구분 : 관리자(0) / 일반사용자(1) / 헬퍼(2) / 블랙리스트(3)
}