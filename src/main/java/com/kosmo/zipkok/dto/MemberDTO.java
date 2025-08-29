package com.kosmo.zipkok.dto;

import lombok.Data;
@Data
public class MemberDTO {

	private String memberSeq;
	private String memberId; //아이디
	private String memberPass; //패스워드
	private String memberName; //이름
	private String memberEmail; //이메일
	private String memberAge; //연령대
	private int memberSex; // 성별
	private String memberPhone;
	private int memberMissionN;
	private int memberStatus;  // 구분 : 관리자(0) / 일반사용자(1) / 헬퍼(2) / 블랙리스트(3)

	/**
     * JWT 토큰 생성 시 사용할 권한 정보를 반환합니다.
     * 
     * 이 메서드는 memberStatus 필드 값을 기반으로
     * Spring Security에서 사용할 권한 문자열을 생성합니다.
     * 
     * 권한 체계:
     * - ROLE_ADMIN: 관리자 권한 (모든 기능 접근 가능)
     * - ROLE_USER: 일반 사용자 권한 (기본 기능 접근 가능)
     * - ROLE_HELPER: 헬퍼 권한 (헬퍼 전용 기능 접근 가능)
     * - ROLE_BLACKLIST: 블랙리스트 (서비스 이용 제한)
     * 
     * JWT 토큰에 이 권한 정보가 포함되어 클라이언트의 접근 권한을 제어합니다.
     * 
     * @return Spring Security 형식의 권한 문자열 (ROLE_ 접두사 포함)
     */
    public String getRole() {
        switch (memberStatus) {
            case 0: return "ROLE_ADMIN";      // 관리자
            case 1: return "ROLE_USER";       // 일반사용자
            case 2: return "ROLE_HELPER";     // 헬퍼
            case 3: return "ROLE_BLACKLIST";  // 블랙리스트
            default: return "ROLE_USER";      // 기본값: 일반사용자
        }
    }
}