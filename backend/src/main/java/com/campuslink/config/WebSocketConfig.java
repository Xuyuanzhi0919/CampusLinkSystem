package com.campuslink.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket 配置
 */
@Configuration
@EnableWebSocket
@ConditionalOnWebApplication
public class WebSocketConfig implements WebSocketConfigurer {

    /**
     * ServerEndpointExporter 用于扫描和注册 @ServerEndpoint 注解的 WebSocket 端点
     *
     * <p>使用自定义条件 {@link ServerEndpointAvailableCondition} 确保只在
     * ServerContainer 可用的环境中创建该 Bean，避免在测试环境中因缺少
     * ServerContainer 而启动失败。</p>
     *
     * @see ServerEndpointAvailableCondition
     */
    @Bean
    @Conditional(ServerEndpointAvailableCondition.class)
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // WebSocket endpoint 由 @ServerEndpoint 注解定义
    }
}
