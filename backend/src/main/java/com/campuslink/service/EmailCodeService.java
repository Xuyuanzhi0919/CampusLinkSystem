package com.campuslink.service;

import com.campuslink.common.ResultCode;
import com.campuslink.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

/**
 * 邮箱验证码服务
 * 使用 Redis 存储验证码，支持发送频率限制
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailCodeService {

    private final StringRedisTemplate redisTemplate;

    @Value("${campuslink.email.expire-minutes:5}")
    private int expireMinutes;

    @Value("${campuslink.email.max-send-per-hour:5}")
    private int maxSendPerHour;

    // Redis Key 前缀
    private static final String CODE_KEY_PREFIX   = "email:code:";    // email:code:{type}:{email}
    private static final String LIMIT_KEY_PREFIX  = "email:limit:";   // email:limit:{email}

    /**
     * 生成并存储验证码
     * @return 6位数字验证码
     */
    public String generateAndStore(String email, String type) {
        // 频率限制：每小时最多发送 maxSendPerHour 次
        String limitKey = LIMIT_KEY_PREFIX + email;
        Long count = redisTemplate.opsForValue().increment(limitKey);
        if (count == 1) {
            redisTemplate.expire(limitKey, Duration.ofHours(1));
        }
        if (count > maxSendPerHour) {
            throw new BusinessException(429, "发送过于频繁，请 1 小时后再试");
        }

        String code = generateCode();
        String codeKey = CODE_KEY_PREFIX + type + ":" + email;
        redisTemplate.opsForValue().set(codeKey, code, Duration.ofMinutes(expireMinutes));
        log.info("验证码已生成并存储: email={}, type={}, expireMinutes={}", email, type, expireMinutes);
        return code;
    }

    /**
     * 验证验证码（验证成功后立即删除）
     */
    public void verify(String email, String type, String code) {
        String codeKey = CODE_KEY_PREFIX + type + ":" + email;
        String stored = redisTemplate.opsForValue().get(codeKey);
        if (stored == null) {
            throw new BusinessException(400, "验证码已过期，请重新获取");
        }
        if (!stored.equals(code)) {
            throw new BusinessException(400, "验证码错误");
        }
        // 验证成功，删除验证码防止重复使用
        redisTemplate.delete(codeKey);
    }

    /**
     * 生成 6 位随机数字验证码
     */
    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int num = 100000 + random.nextInt(900000);
        return String.valueOf(num);
    }
}
