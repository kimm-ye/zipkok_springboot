package com.kosmo.zipkok.dao;

import com.kosmo.zipkok.dto.HelperDTO;
import com.kosmo.zipkok.dto.ImageDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface MemberDAO{

	boolean selectEmail(String email);
	HelperDTO selectMemberWithImageBySeq(String memberSeq);
	HelperDTO selectMemberBySeq(String memberSeq);
	HelperDTO selectMemberById(String memberId);
	HelperDTO selectSnsLogin(Map<String, String> param);
	String findId (Map<String, String> param);
	String findPwd (Map<String, String> param);
	String findPwdBySeq (String memberSeq);
	String idCheck(String id);
	ImageDTO selectMemberImage (String memberSeq);
	Map<String, String> selectMemberBasicInfo(String memberSeq);
	void insertMember(HelperDTO dto);
	void insertHelper(HelperDTO dto);
	void insertSnsLogin(Map<String, String> snsInfo);
	void insertMemberImage(@Param("memberSeq") String memberSeq,
						   @Param("image") ImageDTO imageDTO);
	void updateMember(HelperDTO dto);
	void updateHelper(HelperDTO dto);
	int updateMemberImage(@Param("memberSeq") String memberSeq,
						  @Param("image") ImageDTO imageDTO);
	// 이미지 버전 증가
	void incrementImageVersion(@Param("memberSeq") String memberSeq);

	// 이미지 버전 초기화
	void initImageVersion(@Param("memberSeq") String memberSeq);
	void deleteMember(String memberSeq);
}