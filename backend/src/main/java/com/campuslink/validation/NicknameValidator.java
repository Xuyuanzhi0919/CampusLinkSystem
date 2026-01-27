package com.campuslink.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

/**
 * 昵称校验器 - 禁止使用系统保留词
 */
public class NicknameValidator implements ConstraintValidator<ValidNickname, String> {

    /**
     * 系统保留词列表
     */
    private static final List<String> RESERVED_WORDS = Arrays.asList(
            "管理员", "admin", "administrator",
            "系统", "system",
            "匿名", "匿名用户", "anonymous",
            "用户已注销", "已注销",
            "未知用户", "未知",
            "官方", "official",
            "客服", "service"
    );

    @Override
    public void initialize(ValidNickname constraintAnnotation) {
        // 初始化方法，可选
    }

    @Override
    public boolean isValid(String nickname, ConstraintValidatorContext context) {
        // 空值由 @NotBlank 处理
        if (nickname == null || nickname.trim().isEmpty()) {
            return true;
        }

        String trimmedNickname = nickname.trim().toLowerCase();

        // 检查是否包含系统保留词
        for (String reservedWord : RESERVED_WORDS) {
            if (trimmedNickname.contains(reservedWord.toLowerCase())) {
                // 自定义错误消息
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "昵称不能包含系统保留词：\"" + reservedWord + "\""
                ).addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
