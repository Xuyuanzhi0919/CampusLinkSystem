package com.campuslink.config;

import com.campuslink.middleware.JwtAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;

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
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/register",      // 注册
                        "/auth/login",         // 登录
                        "/auth/refresh",       // 刷新Token
                        "/health",             // 健康检查
                        "/resource/list",      // 资源列表（游客可浏览）
                        "/resource/search",    // 搜索资源（游客可浏览）
                        "/question/list",      // 问题列表（游客可浏览）
                        "/question/*/answers", // 问题答案列表（游客可浏览）
                        "/task/list",          // 任务列表（游客可浏览）
                        "/tag/hot",            // 热门标签（游客可浏览）
                        "/tag/hot/category",   // 分类热门标签（游客可浏览）
                        "/tag/search",         // 搜索标签（游客可浏览）
                        "/stats/today",        // 今日活跃统计（游客可浏览）
                        "/activity/list",      // 🎯 活动列表（游客可浏览）
                        "/notification/list",  // 🎯 校园公告列表（游客可浏览）
                        "/doc.html",           // Swagger UI
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        "/favicon.ico"
                );
    }
}
