package com.campuslink.middleware;

import com.campuslink.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 认证拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 放行详情 GET 请求（游客可浏览，但已登录用户需识别身份以返回个性化字段）
        String method = request.getMethod();
        String uri = request.getRequestURI();
        boolean isGuestAllowedGet =
            ("GET".equals(method) && uri.matches(".*/question/\\d+$")) ||
            ("GET".equals(method) && uri.matches(".*/resource/\\d+$")) ||
            ("GET".equals(method) && uri.matches(".*/resource/\\d+/comments$")) ||
            ("GET".equals(method) && uri.matches(".*/club/\\d+$")) ||
            ("GET".equals(method) && uri.matches(".*/club/\\d+/members$")) ||
            ("GET".equals(method) && uri.matches(".*/club/\\d+/posts$")) ||
            ("GET".equals(method) && uri.matches(".*/activity/\\d+$"));

        if (isGuestAllowedGet) {
            // 尝试解析 Token（可选：有 Token 就设置 userId，无 Token 以游客身份放行）
            String guestToken = getTokenFromRequest(request);
            if (guestToken != null && !guestToken.isEmpty()) {
                try {
                    if (jwtUtil.validateToken(guestToken)) {
                        request.setAttribute("userId", jwtUtil.getUserIdFromToken(guestToken));
                        request.setAttribute("username", jwtUtil.getUsernameFromToken(guestToken));
                        request.setAttribute("role", jwtUtil.getRoleFromToken(guestToken));
                    }
                } catch (Exception ignored) {
                    // Token 解析失败，按游客处理
                }
            }
            return true;
        }

        // 获取 Token
        String token = getTokenFromRequest(request);

        if (token == null || token.isEmpty()) {
            log.warn("请求未携带Token - URI: {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未授权，请先登录\"}");
            return false;
        }

        try {
            // 验证 Token
            if (!jwtUtil.validateToken(token)) {
                log.warn("Token无效 - URI: {}", request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"Token无效\"}");
                return false;
            }

            // 提取用户信息并存入请求属性
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);

            log.debug("用户认证成功 - userId: {}, username: {}, role: {}, URI: {}",
                userId, username, role, request.getRequestURI());

            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("role", role);

            return true;

        } catch (Exception e) {
            log.error("Token验证失败 - URI: {}, 错误: {}", request.getRequestURI(), e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token验证失败\"}");
            return false;
        }
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
