package com.campuslink.service;

import com.campuslink.dto.resource.OssSignatureResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.UUID;

/**
 * 阿里云 OSS 服务
 */
@Slf4j
@Service
public class OssService {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${aliyun.oss.upload-dir}")
    private String uploadDir;

    /**
     * 生成 OSS 上传签名
     *
     * @param fileName 文件名
     * @return OSS 签名响应
     */
    public OssSignatureResponse generateUploadSignature(String fileName) {
        try {
            // 过期时间：30分钟后
            long expireTime = System.currentTimeMillis() + 1800 * 1000;
            Date expiration = new Date(expireTime);

            // 生成唯一文件名
            String fileExtension = getFileExtension(fileName);
            String uniqueFileName = UUID.randomUUID().toString().replaceAll("-", "") + fileExtension;
            String key = uploadDir + uniqueFileName;

            // 构建 Policy
            String policy = buildPolicy(expiration, key);
            String encodedPolicy = Base64.getEncoder().encodeToString(policy.getBytes(StandardCharsets.UTF_8));

            // 生成签名
            String signature = computeSignature(encodedPolicy);

            // 构建 OSS Host
            String host = String.format("https://%s.%s", bucketName, endpoint);

            log.info("生成 OSS 签名成功: key={}, expire={}", key, expiration);

            return OssSignatureResponse.builder()
                    .host(host)
                    .policy(encodedPolicy)
                    .signature(signature)
                    .accessId(accessKeyId)
                    .expire(expireTime / 1000)
                    .key(key)
                    .dir(uploadDir)
                    .build();
        } catch (Exception e) {
            log.error("生成 OSS 签名失败", e);
            throw new RuntimeException("生成上传签名失败: " + e.getMessage());
        }
    }

    /**
     * 构建 Policy JSON
     */
    private String buildPolicy(Date expiration, String key) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        formatter.setTimeZone(new SimpleTimeZone(0, "GMT"));
        String expirationStr = formatter.format(expiration);

        // Policy JSON (限制文件大小最大 50MB)
        // 使用 starts-with 代替 eq,更宽松的匹配
        return String.format(
                "{\"expiration\":\"%s\",\"conditions\":[[\"content-length-range\",0,52428800],[\"starts-with\",\"$key\",\"%s\"]]}",
                expirationStr,
                uploadDir  // 只限制上传目录
        );
    }

    /**
     * 计算签名
     */
    private String computeSignature(String encodedPolicy) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(accessKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        byte[] signData = mac.doFinal(encodedPolicy.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signData);
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
