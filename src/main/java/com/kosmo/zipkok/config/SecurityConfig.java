package com.kosmo.zipkok.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * filterChain이 interceptor보다 먼저 동작해 사용자가 인증되었는지, 권한이 있는지 확인
     * 사용자 정보를 DB 조회하지 않고 JWT만 사용해서 인증할거니까 httpBasic, formLogin 을 disable한다.
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .httpBasic(httpBasic -> httpBasic.disable())  // 기본 HTTP Basic 인증 비활성화
            .formLogin(formLogin -> formLogin.disable())   // 기본 Form 로그인 비활성화
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/member/login/**", "/member/logout/**", "/member/join/**", "/member/find/**",
                            "/notice", "/notice/history/**", "/notice/detail", "/api/notice/detail",
                            "/resources/**", "/css/**", "/js/**", "/img/**", "/webjars/**",
                            "/favicon.ico", "/error", "/403", "/404").permitAll()
                    .anyRequest().authenticated() // 나머지는 인증 필요 (인증정보 없으면 403)
            )
            .sessionManagement(session -> session
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT 를 사용하기 위한 설정
            // exceptionHandling 설정 없음 → 자동으로 ErrorController로 이동!

            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); }
    }
