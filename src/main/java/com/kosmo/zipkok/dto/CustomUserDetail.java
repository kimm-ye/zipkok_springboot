package com.kosmo.zipkok.dto;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetail extends User {

    private final String memberSeq;      // 회원 시퀀스 (PK)
    private final String memberId;       // 아이디
    private final String memberName;     // 이름
    private final int memberStatus;      // 상태 (0:관리자, 1:일반, 2:헬퍼, 3:블랙)

    public CustomUserDetail(String memberId,
                            String password,
                            Collection<? extends GrantedAuthority> authorities,
                            String memberSeq,
                            String memberName,
                            int memberStatus) {
        super(memberId, password, authorities);
        this.memberSeq = memberSeq;
        this.memberId = memberId;
        this.memberName = memberName;
        this.memberStatus = memberStatus;
    }

    /**
     * 역할 문자열 반환 (ROLE_ADMIN, ROLE_USER 등)
     */
    public String getRole() {
        return getAuthorities().iterator().next().getAuthority();
    }
}
