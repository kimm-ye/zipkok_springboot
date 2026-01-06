package com.kosmo.zipkok.service;

import com.kosmo.zipkok.dto.HelperDTO;

import java.io.IOException;
import java.util.Map;

public interface MemberService {

    // 이메일 체크 확인
    boolean selectEmail(String email);
    //비밀번호 검증
    HelperDTO authenticate(String inputId, String inputPwd);
    // sns로그인
    HelperDTO selectSnsLogin(Map<String, String> param);
    //아이디 중복체크
    String idCheck(String id);
    HelperDTO selectMemberWithImageBySeq(String memberSeq);
    HelperDTO selectMemberBySeq(String memberSeq);
    //아이디찾기
    String findId (Map<String, String> param);
    String findPwd (Map<String, String> param);
    String findPwdBySeq (String memberSeq);
    // 회원가입
    void insertMember(HelperDTO dto) throws IOException;
    void insertSnsMember(HelperDTO dto, Map<String, String> snsInfo) throws IOException;
    void updateMember(HelperDTO dto) throws IOException;
    void deleteMember(String memberSeq) throws Exception;

}