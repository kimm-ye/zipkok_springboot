package com.kosmo.zipkok.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. filterChain이 interceptor보다 먼저 동작해 사용자가 인증되었는지, 권한이 있는지 확인
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/member/login/**", "/member/join/**", "/member/find/**",
                                "/resources/**", "/css/**", "/js/**", "/img/**", "/webjars/**", "/favicon.ico", "/error").permitAll() // 특정 경로 접근 허용
                        //.anyRequest().permitAll() // 나머지 요청 모두 허용 (.anyRequest().permitAll()로 설정했으니 모든 요청이 인증 없이 통과하지만, 실제로는 .authenticated()로 바꾸면 로그인한 사용자만 통과.)
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/member/login") // 로그인 페이지 지정
                        .permitAll() // 로그인 페이지 접근 허용
                )
                .logout(logout -> logout
                        .logoutUrl("/member/logout") // 로그아웃 URL
                        .logoutSuccessUrl("/") // 로그아웃 후 리다이렉트 URL
                );

        return http.build(); // SecurityFilterChain 객체 반환
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}