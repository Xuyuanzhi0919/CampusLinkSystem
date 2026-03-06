package com.campuslink.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.WebApplicationContext;

/**
 * 判断 ServerContainer 是否可用的条件
 *
 * <p>ServerEndpointExporter 需要 jakarta.websocket.server.ServerContainer 才能正常工作。
 * 该容器只在真实的 Servlet 容器（如 Tomcat）完全启动后才可用。
 * 在测试场景中，如果 ServerContainer 不可用，则跳过该 Bean 的创建。</p>
 *
 * <p>该条件通过检查应用属性 {@code websocket.server-endpoint.enabled} 来控制。
 * 默认情况下启用，测试环境可以通过设置该属性为 false 来禁用 WebSocket 端点导出器。</p>
 *
 * <p>由于在 Condition 阶段 ServletContext 可能尚未初始化，此条件采用保守策略：
 * 仅在非测试环境且类路径存在 ServerContainer 类时返回 true。</p>
 */
public class ServerEndpointAvailableCondition implements Condition {

    private static final String SERVER_CONTAINER_CLASS = "jakarta.websocket.server.ServerContainer";
    private static final String ENABLED_PROPERTY = "websocket.server-endpoint.enabled";
    private static final String WEB_ENVIRONMENT_PROPERTY = "websocket.web-environment";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        // 检查是否通过属性显式禁用
        Boolean enabled = context.getEnvironment().getProperty(ENABLED_PROPERTY, Boolean.class, true);
        if (!enabled) {
            return false;
        }

        // 检查是否显式指定了非 Web 环境
        Boolean isWebEnvironment = context.getEnvironment().getProperty(WEB_ENVIRONMENT_PROPERTY, Boolean.class, true);
        if (!isWebEnvironment) {
            return false;
        }

        // 检查 ServerContainer 类是否在类路径上
        return ClassUtils.isPresent(SERVER_CONTAINER_CLASS, context.getClassLoader());
    }
}
