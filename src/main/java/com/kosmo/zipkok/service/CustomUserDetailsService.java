package com.kosmo.zipkok.service;

import com.kosmo.zipkok.dto.HelperDTO;
import com.kosmo.zipkok.dto.MemberDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final MemberService memberService;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		HelperDTO member = memberService.selectMemberById(username);
		if (member == null) {
			throw new UsernameNotFoundException(username + " -> 데이터베이스에서 찾을 수 없습니다.");
		}
		return createUserDetails(member);
	}

	// DB에 User 값이 존재한다면 UserDetails 객체로 만들어서 리턴
	private UserDetails createUserDetails(MemberDTO member) {
		String role;
		switch (member.getMemberStatus()) {
			case 0:
				role = "ROLE_ADMIN";
				break;
			case 1:
				role = "ROLE_USER";
				break;
			case 2:
				role = "ROLE_HELPER";
				break;
			case 3:
				role = "ROLE_BLACK";
				break;
			default:
				role = "ROLE_GUEST";
				break;
		}

		GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(role);

		return new User(
				member.getMemberId(),
				member.getMemberPass(), // 패스워드가 들어가야 함
				Collections.singleton(grantedAuthority)
		);
	}
}
