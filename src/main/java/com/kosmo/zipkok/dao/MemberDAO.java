package com.kosmo.zipkok.dao;

import com.kosmo.zipkok.dto.HelperDTO;
import com.kosmo.zipkok.dto.ImageDTO;
import org.apache.ibatis.annotations.Mapper;

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
	void insertImageImage(HelperDTO dto);
	void updateMember(HelperDTO dto);
	void updateHelper(HelperDTO dto);
	int updateHelperImage(HelperDTO dto);
	void deleteMember(String memberSeq);
}