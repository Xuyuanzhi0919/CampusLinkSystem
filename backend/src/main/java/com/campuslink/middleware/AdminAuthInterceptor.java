package com.campuslink.middleware;

import com.campuslink.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理员权限拦截器
 * 拦截 /admin/** 路径，要求 role == "admin"
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String token = getTokenFromRequest(request);
        if (token == null || token.isEmpty()) {
            writeError(response, 401, "未授权，请先登录");
            return false;
        }

        try {
            if (!jwtUtil.validateToken(token)) {
                writeError(response, 401, "Token无效或已过期");
                return false;
            }

            String role = jwtUtil.getRoleFromToken(token);
            if (!"admin".equals(role)) {
                log.warn("非管理员访问 admin 接口 - role: {}, URI: {}", role, request.getRequestURI());
                writeError(response, 403, "无权限，仅管理员可访问");
                return false;
            }

            request.setAttribute("userId", jwtUtil.getUserIdFromToken(token));
            request.setAttribute("username", jwtUtil.getUsernameFromToken(token));
            request.setAttribute("role", role);
            return true;

        } catch (Exception e) {
            log.error("AdminAuth Token验证失败 - URI: {}, 错误: {}", request.getRequestURI(), e.getMessage());
            writeError(response, 401, "Token验证失败");
            return false;
        }
    }

    private void writeError(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format("{\"code\":%d,\"message\":\"%s\"}", status, message));
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
