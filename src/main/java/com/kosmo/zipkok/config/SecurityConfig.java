package com.kosmo.zipkok.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // 2. filterChain이 interceptor보다 먼저 동작해 사용자가 인증되었는지, 권한이 있는지 확인
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/member/login/**", "/member/logout/**", "/member/join/**", "/member/find/**",
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
