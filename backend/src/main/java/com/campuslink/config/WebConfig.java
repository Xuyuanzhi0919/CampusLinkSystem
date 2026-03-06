package com.campuslink.config;

import com.campuslink.middleware.JwtAuthInterceptor;
import com.campuslink.middleware.OptionalJwtAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;
    private final OptionalJwtAuthInterceptor optionalJwtAuthInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 🎯 可选认证拦截器 - 用于游客可访问但需要识别登录用户的接口
        registry.addInterceptor(optionalJwtAuthInterceptor)
                .addPathPatterns(
                        "/activity/list",      // 活动列表（游客可浏览，已登录用户看到报名状态）
                        "/activity/*",         // 活动详情（游客可浏览，已登录用户看到报名/签到状态）
                        "/club/list",          // 社团列表（游客可浏览，已登录用户看到加入状态）
                        "/club/*",             // 社团详情（游客可浏览，已登录用户看到加入状态）
                        "/notification/list",  // 校园公告列表
                        "/resource/list",      // 资源列表
                        "/resource/search",    // 搜索资源
                        "/question/list",      // 问题列表
                        "/question/*/answers", // 问题答案列表
                        "/task/list"           // 任务列表
                );

        // 🎯 强制认证拦截器 - 必须登录才能访问的接口
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/register",      // 注册
                        "/auth/login",         // 登录
                        "/auth/refresh",       // 刷新Token
                        "/auth/wechat/login",  // 微信小程序登录
                        "/health",             // 健康检查
                        "/api/pdf/**",         // PDF代理服务（公开访问）
                        "/resource/list",      // 资源列表（使用可选认证）
                        "/resource/search",    // 搜索资源（使用可选认证）
                        "/question/list",       // 问题列表（使用可选认证）
                        "/question/*/answers",  // 问题答案列表（使用可选认证）
                        "/question/hot-tags",   // 热门标签（游客可浏览）
                        "/question/active-users", // 活跃答主（游客可浏览）
                        // 注意：GET /question/{id} 详情由 JwtAuthInterceptor 内部按 Method 放行，此处不 exclude 以保护写接口安全
                        "/task/list",           // 任务列表（使用可选认证）
                        "/tag/hot",            // 热门标签（游客可浏览）
                        "/tag/hot/category",   // 分类热门标签（游客可浏览）
                        "/tag/search",         // 搜索标签（游客可浏览）
                        "/stats/today",        // 今日活跃统计（游客可浏览）
                        "/reward/items",       // 积分商品列表（游客可浏览）
                        "/activity/list",      // 活动列表（使用可选认证）
                        "/club/list",          // 社团列表（使用可选认证）
                        "/notification/list",  // 校园公告列表（使用可选认证）
                        "/doc.html",           // Swagger UI
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/v3/api-docs",
                        "/webjars/**",
                        "/favicon.ico",
                        "/ws/**"               // WebSocket 端点
                );
    }
}
