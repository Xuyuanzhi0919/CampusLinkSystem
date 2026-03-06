package com.campuslink.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 自定义昵称校验注解 - 禁止使用系统保留词
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NicknameValidator.class)
@Documented
public @interface ValidNickname {
    String message() default "昵称包含系统保留词，请重新输入";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
