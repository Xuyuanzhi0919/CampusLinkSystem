package com.campuslink.config;

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
     * 注意：不要在这里创建 ObjectMapper Bean
     * Spring Boot 会自动配置一个支持 Java 8 日期时间的 ObjectMapper
     * 如果需要自定义配置，应该使用 Jackson2ObjectMapperBuilderCustomizer
     */
}
