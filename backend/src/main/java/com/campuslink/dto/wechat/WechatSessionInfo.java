package com.campuslink.dto.wechat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 微信 code2session 响应
 */
@Data
public class WechatSessionInfo {

    /**
     * 用户唯一标识
     */
    private String openid;

    /**
     * 会话密钥
     */
    @JsonProperty("session_key")
    private String sessionKey;

    /**
     * 用户在开放平台的唯一标识符（需要在微信开放平台绑定）
     */
    private String unionid;

    /**
     * 错误码
     */
    private Integer errcode;

    /**
     * 错误信息
     */
    private String errmsg;
}
