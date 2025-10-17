package com.kosmo.zipkok.dao;

import com.kosmo.zipkok.dto.HelperDTO;
import com.kosmo.zipkok.dto.MemberDTO;
import org.apache.ibatis.annotations.Mapper;

import java.io.IOException;
import java.util.Map;

@Mapper
public interface MemberDAO{

	boolean selectEmail(String email);
	HelperDTO selectMemberBySeq(String memberSeq);
	HelperDTO selectMemberById(String memberId);
	HelperDTO selectSnsLogin(Map<String, String> param);
	String findId (Map<String, String> param);
	String findPwd (Map<String, String> param);
	String idCheck(String id);
	void insertMember(HelperDTO dto);
	void insertHelper(HelperDTO dto);
	void insertSnsLogin(Map<String, String> snsInfo);
	void insertHelperImage(HelperDTO dto);
	void updateMember(HelperDTO dto);
	void updateHelper(HelperDTO dto);
	int updateHelperImage(HelperDTO dto);
	void deleteMember(String memberSeq);
}