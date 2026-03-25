package com.learn.today.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;

/**
 * 应用级公共 Bean 配置
 */
@Configuration
public class AppConfig {

    /**
     * 密码加密器，使用 BCrypt（强度默认 10）
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * HTTP 客户端，供 AiServiceImpl 调用 Claude API
     * Spring Boot 3.2+ 推荐使用 RestClient 替代 RestTemplate
     */
    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }
}
