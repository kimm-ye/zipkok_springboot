package com.kosmo.zipkok.dto;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetail extends User {

    private final String memberSeq;      // 회원 시퀀스 (PK)

    public CustomUserDetail(String memberSeq,
                            Collection<? extends GrantedAuthority> authorities) {
        super(memberSeq,"", authorities);
        this.memberSeq = memberSeq;
    }

    /**
     * 역할 문자열 반환 (ROLE_ADMIN, ROLE_USER 등)
     */
    public String getRole() {
        return getAuthorities().iterator().next().getAuthority();
    }
}
