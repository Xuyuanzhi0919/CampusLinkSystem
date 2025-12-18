package com.campuslink.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 微信小程序配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wechat.miniapp")
public class WechatConfig {

    /**
     * 小程序 AppID
     */
    private String appid;

    /**
     * 小程序 AppSecret
     */
    private String secret;

    /**
     * 微信登录 API 地址
     */
    private String authUrl;
}
