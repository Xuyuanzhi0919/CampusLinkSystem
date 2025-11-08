package com.campuslink.controller;

import com.campuslink.common.Result;
import com.campuslink.dto.AuthResponse;
import com.campuslink.dto.LoginRequest;
import com.campuslink.dto.RegisterRequest;
import com.campuslink.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "用户注册、登录、Token刷新等接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "用户注册", description = "注册新用户账号")
    @PostMapping("/register")
    public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userService.register(request);
        return Result.success("注册成功", response);
    }

    @Operation(summary = "用户登录", description = "使用用户名/邮箱/手机号和密码登录")
    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        return Result.success("登录成功", response);
    }

    @Operation(summary = "刷新Token", description = "使用RefreshToken获取新的访问Token")
    @PostMapping("/refresh")
    public Result<AuthResponse> refreshToken(
            @Parameter(description = "刷新Token") @RequestHeader("Refresh-Token") String refreshToken) {
        AuthResponse response = userService.refreshToken(refreshToken);
        return Result.success("Token刷新成功", response);
    }

    @Operation(summary = "退出登录", description = "清除客户端Token即可")
    @PostMapping("/logout")
    public Result<Void> logout() {
        // 客户端清除 Token 即可，服务端无需处理（除非使用 Token 黑名单）
        return Result.success("退出成功", null);
    }
}
