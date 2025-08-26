package com.kosmo.zipkok.service;

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
		MemberDTO member = memberService.selectMemberById(username);
		if (member == null) {
			throw new UsernameNotFoundException(username + " -> 데이터베이스에서 찾을 수 없습니다.");
		}
		return createUserDetails(member);
	}

	// DB에 User 값이 존재한다면 UserDetails 객체로 만들어서 리턴
	private UserDetails createUserDetails(MemberDTO member) {
		GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(Integer.toString(member.getMemberStatus()));

		return new User(
				member.getMemberId(),
				member.getMemberPass(),
				Collections.singleton(grantedAuthority)
		);
	}
}
