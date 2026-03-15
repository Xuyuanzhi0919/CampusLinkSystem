package com.campuslink.controller;

import com.campuslink.common.Result;
import com.campuslink.dto.AuthResponse;
import com.campuslink.dto.LoginRequest;
import com.campuslink.dto.RegisterRequest;
import com.campuslink.service.EmailCodeService;
import com.campuslink.service.EmailService;
import com.campuslink.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "用户注册、登录、Token刷新等接口")
@Validated
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final EmailCodeService emailCodeService;
    private final EmailService emailService;

    @Value("${campuslink.email.expire-minutes:5}")
    private int expireMinutes;

    /** 发送邮箱验证码请求体 */
    @Data
    static class SendEmailCodeRequest {
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;

        @NotBlank(message = "用途不能为空")
        private String type; // register / login / reset
    }

    @Operation(summary = "发送邮箱验证码")
    @PostMapping("/email/send")
    public Result<Void> sendEmailCode(@Valid @RequestBody SendEmailCodeRequest req) {
        String code = emailCodeService.generateAndStore(req.getEmail(), req.getType());
        emailService.sendVerificationCode(req.getEmail(), code, expireMinutes);
        return Result.success("验证码已发送，请查收邮件", null);
    }

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

    @Operation(summary = "检查用户名是否可用", description = "注册时实时校验用户名唯一性")
    @GetMapping("/check-username")
    public Result<Boolean> checkUsername(
            @Parameter(description = "用户名") @RequestParam @NotBlank String username) {
        boolean available = userService.isUsernameAvailable(username);
        return Result.success(available ? "用户名可用" : "用户名已被占用", available);
    }
}
