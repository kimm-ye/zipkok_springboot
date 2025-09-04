package com.kosmo.zipkok.service;

import com.kosmo.zipkok.dto.HelperDTO;
import com.kosmo.zipkok.dto.MemberDTO;

import java.io.IOException;
import java.util.Map;

public interface MemberService {

    // 이메일 체크 확인
    boolean selectEmail(String email);
    //비밀번호 검증
    MemberDTO authenticate(String inputId, String inputPwd);
    //아이디 중복체크
    String idCheck(String id);
    HelperDTO selectMemberById(String memberId);

    //아이디찾기
    String findId (Map<String, String> param);
    String findPwd (Map<String, String> param);
    // 회원가입
    void insertMember(HelperDTO dto) throws IOException;
    void updateMember(HelperDTO dto) throws IOException;

}