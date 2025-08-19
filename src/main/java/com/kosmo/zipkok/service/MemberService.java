package com.kosmo.zipkok.service;

import com.kosmo.zipkok.dto.MemberDTO;

import java.util.Map;

public interface MemberService {

    // 이메일 체크 확인
    boolean selectEmail(String email);

    //비밀번호 검증
    MemberDTO authenticate(String inputId, String inputPwd);

    //아이디 중복체크
    String idCheck(String id);

    //로그인
    MemberDTO login (String id, String pass);

    //아이디찾기
    String findId (Map<String, String> param);

    String findPwd (Map<String, String> param);

    // 일반 유저 회원가입
    void insertMember(MemberDTO dto) throws Exception;

}