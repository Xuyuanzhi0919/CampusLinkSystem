package com.campuslink.aspect;

import com.campuslink.entity.AdminOperationLog;
import com.campuslink.mapper.AdminOperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;

/**
 * Admin 操作日志 AOP 切面
 * 自动拦截 admin 包下所有 Controller 的写操作（POST / PUT / DELETE），
 * 写入 admin_operation_log 表。GET 接口不记录。
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminLogAspect {

    private final AdminOperationLogMapper auditLogMapper;

    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "DELETE", "PATCH");

    /** 切点：admin 包下所有 Controller 的公共方法 */
    @Around("execution(public * com.campuslink.controller.admin..*(..))")
    public Object logAdminOp(ProceedingJoinPoint pjp) throws Throwable {
        // 只记录写请求，GET 直接放行
        HttpServletRequest request = currentRequest();
        if (request == null || !WRITE_METHODS.contains(request.getMethod())) {
            return pjp.proceed();
        }

        Object result = pjp.proceed();

        // 异步写日志，不影响主流程
        try {
            Long operatorId = (Long) request.getAttribute("userId");
            String action   = resolveAction(pjp, request);
            String target   = resolveTarget(request);

            AdminOperationLog logEntry = new AdminOperationLog();
            logEntry.setOperatorId(operatorId != null ? operatorId : 0L);
            logEntry.setAction(action);
            logEntry.setTarget(target);
            logEntry.setDetail(request.getMethod() + " " + request.getRequestURI());
            logEntry.setCreatedAt(LocalDateTime.now());
            auditLogMapper.insert(logEntry);
        } catch (Exception e) {
            log.warn("AdminLogAspect: 写入操作日志失败 - {}", e.getMessage());
        }

        return result;
    }

    // ── 辅助方法 ──────────────────────────────────────────────

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    /** 从方法注解或方法名推导操作名称 */
    private String resolveAction(ProceedingJoinPoint pjp, HttpServletRequest request) {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method method = sig.getMethod();

        // 优先取 Swagger @Operation(summary)
        io.swagger.v3.oas.annotations.Operation opAnn =
                method.getAnnotation(io.swagger.v3.oas.annotations.Operation.class);
        if (opAnn != null && !opAnn.summary().isBlank()) {
            return opAnn.summary();
        }

        // 否则用 HTTP 方法 + 简化的 URI
        return request.getMethod() + " " + simplifyUri(request.getRequestURI());
    }

    /** 从 URI 最后一段提取操作目标（跳过纯数字 ID 段） */
    private String resolveTarget(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String[] parts = uri.split("/");
        // 找最后一个非数字、非空的段作为 target
        for (int i = parts.length - 1; i >= 0; i--) {
            String seg = parts[i];
            if (!seg.isBlank() && !seg.matches("\\d+")) {
                return seg;
            }
        }
        return uri;
    }

    /** 把 URI 中的数字 ID 替换为 {id}，避免日志太碎 */
    private String simplifyUri(String uri) {
        return uri.replaceAll("/\\d+", "/{id}");
    }
}
