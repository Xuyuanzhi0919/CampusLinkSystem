package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.common.ResultCode;
import com.campuslink.dto.*;
import com.campuslink.entity.User;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.UserMapper;
import com.campuslink.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

/**
 * 用户服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    public AuthResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> usernameQuery = new LambdaQueryWrapper<>();
        usernameQuery.eq(User::getUsername, request.getUsername());
        if (userMapper.selectCount(usernameQuery) > 0) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }

        // 检查邮箱是否已存在
        LambdaQueryWrapper<User> emailQuery = new LambdaQueryWrapper<>();
        emailQuery.eq(User::getEmail, request.getEmail());
        if (userMapper.selectCount(emailQuery) > 0) {
            throw new BusinessException(400, "该邮箱已被注册");
        }

        // 检查手机号是否已存在（如果提供）
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            LambdaQueryWrapper<User> phoneQuery = new LambdaQueryWrapper<>();
            phoneQuery.eq(User::getPhone, request.getPhone());
            if (userMapper.selectCount(phoneQuery) > 0) {
                throw new BusinessException(400, "该手机号已被注册");
            }
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(encryptPassword(request.getPassword())); // 加密密码
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setSchoolId(request.getSchoolId());
        user.setRole(request.getRole() != null ? request.getRole() : "student");
        user.setPoints(100); // 注册奖励100积分
        user.setLevel(1);
        user.setStatus(1); // 1-正常
        user.setIsVerified(0); // 0-未认证

        userMapper.insert(user);

        log.info("用户注册成功: {}", user.getUsername());

        // 生成 Token
        String token = jwtUtil.generateToken(user.getUId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUId());

        // 返回用户信息
        UserVO userVO = convertToVO(user);
        return new AuthResponse(token, refreshToken, userVO);
    }

    /**
     * 用户登录
     */
    public AuthResponse login(LoginRequest request) {
        // 查询用户（支持用户名、邮箱、手机号登录）
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.and(wrapper -> wrapper
                .eq(User::getUsername, request.getAccount())
                .or()
                .eq(User::getEmail, request.getAccount())
                .or()
                .eq(User::getPhone, request.getAccount())
        );

        User user = userMapper.selectOne(query);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 验证密码
        if (!encryptPassword(request.getPassword()).equals(user.getPasswordHash())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 检查账号状态（1-正常，0-禁用）
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户登录成功: {}", user.getUsername());

        // 生成 Token
        String token = jwtUtil.generateToken(user.getUId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUId());

        // 返回用户信息
        UserVO userVO = convertToVO(user);
        return new AuthResponse(token, refreshToken, userVO);
    }

    /**
     * 根据 ID 获取用户信息
     */
    public UserVO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return convertToVO(user);
    }

    /**
     * 刷新 Token
     */
    public AuthResponse refreshToken(String refreshToken) {
        try {
            // 验证 RefreshToken
            if (!jwtUtil.validateToken(refreshToken)) {
                throw new BusinessException(ResultCode.TOKEN_INVALID);
            }

            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            User user = userMapper.selectById(userId);

            if (user == null) {
                throw new BusinessException(ResultCode.USER_NOT_FOUND);
            }

            // 生成新的 Token
            String newToken = jwtUtil.generateToken(user.getUId(), user.getUsername(), user.getRole());
            String newRefreshToken = jwtUtil.generateRefreshToken(user.getUId());

            UserVO userVO = convertToVO(user);
            return new AuthResponse(newToken, newRefreshToken, userVO);

        } catch (Exception e) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
    }

    /**
     * 密码加密（使用 MD5）
     */
    private String encryptPassword(String password) {
        return DigestUtils.md5DigestAsHex(password.getBytes());
    }

    /**
     * 转换为 VO 对象
     */
    private UserVO convertToVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        // 手机号脱敏
        if (user.getPhone() != null && user.getPhone().length() == 11) {
            userVO.setPhone(user.getPhone().substring(0, 3) + "****" + user.getPhone().substring(7));
        }
        return userVO;
    }
}
