package com.campuslink.dto.wechat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 微信小程序登录请求
 */
@Data
@Schema(description = "微信小程序登录请求")
public class WechatLoginRequest {

    @NotBlank(message = "微信登录 code 不能为空")
    @Schema(description = "微信登录临时凭证", required = true, example = "081aBcde2fGHij0w4qcd2YZKde0aBcda")
    private String code;

    @Schema(description = "用户昵称（可选）", example = "张三")
    private String nickname;

    @Schema(description = "用户头像 URL（可选）", example = "https://thirdwx.qlogo.cn/...")
    private String avatarUrl;
}
