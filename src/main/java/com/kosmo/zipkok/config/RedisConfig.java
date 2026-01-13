package com.kosmo.zipkok.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${REDIS_HOST}")
    private String redisHost;

    @Value("${REDIS_PORT}")
    private int redisPort;

    @Value("${REDIS_PASSWORD}")
    private String redisPassword;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {

        RedisStandaloneConfiguration config =
                new RedisStandaloneConfiguration(redisHost, redisPort);
        if (redisPassword != null && !redisPassword.isBlank()) {
            config.setPassword(RedisPassword.of(redisPassword.trim()));
        }

        return new LettuceConnectionFactory(config);
    }

    /**
     * @Cacheable 을 사용하지 않고 RedisTemplate 사용하는 이유
     * 로그인시 Refresh Token(RT)을 발급하고 관리하는 시나리오라면, **RedisTemplate을 직접 사용하는 방식(작성하신 코드)**이 훨씬 적합
     *  > 만료시간 정밀제어, 로그아웃 처리 때문
    * */

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());

        // Key는 문자열로
        template.setKeySerializer(new StringRedisSerializer());

        /* GenericJackson2JsonRedisSerializer는  Value를 JSON 직렬화하는 것임
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer()); */

        //Value도 문자열 (이걸로 변경!) > 어차피 토큰에 json 형식이 아닌 문자열 하나만 들어가므로 좀 더 가볍고 빠르게 다루기 위해
        template.setValueSerializer(new StringRedisSerializer());

        return template;
    }
}