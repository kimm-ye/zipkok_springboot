package com.kosmo.zipkok.security;

import com.kosmo.zipkok.dto.MemberDTO;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetail extends User {

    private final String memberSeq;      // 회원 시퀀스 (PK)
    private final MemberDTO memberDTO;  // nullable

    // authorities : 권한. 확장성을 위해 단일이 아닌 복수 권한 가능하도록 세팅
    // 경량 생성자 (JWT 필터용)
    public CustomUserDetail(String memberSeq,
                            Collection<? extends GrantedAuthority> authorities) {
        super(memberSeq, "", authorities);
        this.memberSeq = memberSeq;
        this.memberDTO = null;
    }

    // Full 생성자 (필요시)
    public CustomUserDetail(MemberDTO memberDTO,
                            Collection<? extends GrantedAuthority> authorities) {
        super(memberDTO.getMemberSeq(), "", authorities);
        this.memberSeq = memberDTO.getMemberSeq();
        this.memberDTO = memberDTO;
    }

    /**
     * 역할 문자열 반환 (ROLE_ADMIN, ROLE_USER 등)
     */
    public String getRole() {
        return getAuthorities().iterator().next().getAuthority();
    }

    // 편의 메서드
    public boolean hasFullInfo() {
        return memberDTO != null;
    }
}
