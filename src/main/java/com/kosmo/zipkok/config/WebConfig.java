package com.kosmo.zipkok.config;

import com.kosmo.zipkok.config.interceptor.LoginInterceptor;
import com.kosmo.zipkok.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private SessionService sessionService;

    // 1. WebConfig (CORS) → 2. SecurityConfig (FilterChain) → 3. JwtAuthenticationFilter → 4. Interceptor (preHandle)
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowCredentials(true)
                .allowedOrigins("http://localhost:3000");
    }


    // 3. SecurityFilterChain을 통과한 요청을 받아서 세션 확인이나 추가 로직 수행.
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor(sessionService))
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/",
                    "/member/login/**",
                    "/member/logout/**",
                    "/member/join/**",
                    "/member/find/**",
                    "/resources/**",
                    "/css/**",
                    "/js/**",
                    "/img/**",
                    "/webjars/**",
                    "/favicon.ico",
                    "/error",
                    "/actuator/**"
                );
        }

}