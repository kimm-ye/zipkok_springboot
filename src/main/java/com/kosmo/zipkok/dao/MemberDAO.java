package com.kosmo.zipkok.dao;

import com.kosmo.zipkok.dto.HelperDTO;
import com.kosmo.zipkok.dto.MemberDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface MemberDAO{

	boolean selectEmail(String email);
	HelperDTO selectMemberById(String memberId);
	String findId (Map<String, String> param);
	String findPwd (Map<String, String> param);
	String idCheck(String id);
	void insertMember(HelperDTO dto);
	void insertHelper(HelperDTO dto);
	void insertHelperImage(HelperDTO dto);

}