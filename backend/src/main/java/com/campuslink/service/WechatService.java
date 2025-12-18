package com.campuslink.service;

import com.campuslink.config.WechatConfig;
import com.campuslink.dto.wechat.WechatSessionInfo;
import com.campuslink.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 微信小程序服务
 */
@Slf4j
@Service
public class WechatService {

    @Autowired
    private WechatConfig wechatConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 调用微信 code2session 接口，获取用户 openid 和 session_key
     *
     * @param code 微信登录临时凭证
     * @return 微信会话信息
     */
    public WechatSessionInfo code2Session(String code) {
        try {
            // 构建请求 URL
            String url = String.format(
                "%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                wechatConfig.getAuthUrl(),
                wechatConfig.getAppid(),
                wechatConfig.getSecret(),
                code
            );

            log.info("调用微信 code2session API: appid={}", wechatConfig.getAppid());

            // 调用微信 API
            String response = restTemplate.getForObject(url, String.class);
            log.debug("微信 API 响应: {}", response);

            // 解析响应
            WechatSessionInfo sessionInfo = objectMapper.readValue(response, WechatSessionInfo.class);

            // 检查错误
            if (sessionInfo.getErrcode() != null && sessionInfo.getErrcode() != 0) {
                log.error("微信登录失败: errcode={}, errmsg={}",
                         sessionInfo.getErrcode(), sessionInfo.getErrmsg());

                // 常见错误码处理
                String errorMessage = switch (sessionInfo.getErrcode()) {
                    case 40029 -> "微信登录凭证（code）无效";
                    case 45011 -> "微信登录频率限制，请稍后再试";
                    case 40163 -> "微信登录凭证（code）已被使用";
                    case -1 -> "微信系统繁忙，请稍后再试";
                    default -> "微信登录失败: " + sessionInfo.getErrmsg();
                };

                throw new BusinessException(errorMessage);
            }

            // 验证必需字段
            if (sessionInfo.getOpenid() == null || sessionInfo.getOpenid().isEmpty()) {
                throw new BusinessException("获取微信用户 openid 失败");
            }

            log.info("微信登录成功: openid={}, unionid={}",
                    sessionInfo.getOpenid(), sessionInfo.getUnionid());

            return sessionInfo;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用微信 API 失败", e);
            throw new BusinessException("微信登录服务异常，请稍后重试");
        }
    }
}
