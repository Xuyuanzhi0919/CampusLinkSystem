package com.campuslink.dto.resource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OSS 上传签名响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OssSignatureResponse {
    /**
     * OSS 访问地址
     */
    private String host;

    /**
     * Policy (Base64编码)
     */
    private String policy;

    /**
     * 签名
     */
    private String signature;

    /**
     * AccessKeyId
     */
    private String accessId;

    /**
     * 过期时间戳 (秒)
     */
    private Long expire;

    /**
     * 文件存储路径 (key)
     */
    private String key;

    /**
     * 上传目录
     */
    private String dir;
}
