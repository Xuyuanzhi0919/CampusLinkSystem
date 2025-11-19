package com.campuslink.middleware;

import com.campuslink.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 可选的 JWT 认证拦截器
 * 用于游客可访问但需要识别登录用户的接口（如活动列表）
 *
 * 行为：
 * - 如果请求携带有效 Token → 解析用户信息并设置到 request 属性
 * - 如果没有 Token 或 Token 无效 → 放行请求，userId 为 null（游客模式）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OptionalJwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 获取 Token
        String token = getTokenFromRequest(request);

        // 🎯 如果没有 Token，直接放行（游客模式）
        if (token == null || token.isEmpty()) {
            log.debug("游客访问 - URI: {}", request.getRequestURI());
            return true;
        }

        try {
            // 🎯 验证 Token，如果有效则设置用户信息
            if (jwtUtil.validateToken(token)) {
                Long userId = jwtUtil.getUserIdFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);

                log.debug("已登录用户访问 - userId: {}, username: {}, role: {}, URI: {}",
                    userId, username, role, request.getRequestURI());

                request.setAttribute("userId", userId);
                request.setAttribute("username", username);
                request.setAttribute("role", role);
            } else {
                // Token 无效，按游客处理
                log.debug("Token无效，按游客处理 - URI: {}", request.getRequestURI());
            }
        } catch (Exception e) {
            // Token 解析失败，按游客处理
            log.debug("Token解析失败，按游客处理 - URI: {}, 错误: {}", request.getRequestURI(), e.getMessage());
        }

        // 🎯 总是放行（不像强制认证拦截器会返回 401）
        return true;
    }

    /**
     * 从请求中获取 Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 从 Header 中获取
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
