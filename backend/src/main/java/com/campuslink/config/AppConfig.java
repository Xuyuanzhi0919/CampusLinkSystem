package com.campuslink.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 应用全局配置
 */
@Configuration
public class AppConfig {

    /**
     * RestTemplate Bean（用于调用外部 API）
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * ObjectMapper Bean（用于 JSON 序列化/反序列化）
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
