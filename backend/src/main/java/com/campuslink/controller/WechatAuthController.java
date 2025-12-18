package com.campuslink.controller;

import com.campuslink.common.Result;
import com.campuslink.dto.AuthResponse;
import com.campuslink.dto.UserVO;
import com.campuslink.dto.wechat.BindAccountRequest;
import com.campuslink.dto.wechat.WechatLoginRequest;
import com.campuslink.dto.wechat.WechatSessionInfo;
import com.campuslink.entity.User;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.UserMapper;
import com.campuslink.service.UserService;
import com.campuslink.service.WechatService;
import com.campuslink.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 微信小程序登录控制器
 */
@Slf4j
@RestController
@RequestMapping("/auth/wechat")
@RequiredArgsConstructor
@Tag(name = "微信登录", description = "微信小程序登录相关接口")
public class WechatAuthController {

    private final WechatService wechatService;
    private final UserService userService;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    /**
     * 微信小程序登录（自动注册）
     */
    @PostMapping("/login")
    @Operation(summary = "微信小程序登录", description = "通过微信 code 登录，首次登录自动注册")
    public Result<AuthResponse> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        log.info("微信小程序登录请求: code={}", request.getCode());

        try {
            // 1. 调用微信 API 获取 openid 和 session_key
            WechatSessionInfo sessionInfo = wechatService.code2Session(request.getCode());
            String openid = sessionInfo.getOpenid();
            String unionid = sessionInfo.getUnionid();

            log.info("微信 API 返回: openid={}, unionid={}", openid, unionid);

            // 2. 查询用户是否存在
            User user = userService.findByWechatOpenid(openid);

            // 3. 如果用户不存在，自动注册
            if (user == null) {
                log.info("用户不存在，开始自动注册: openid={}", openid);

                user = userService.createWechatUser(
                    openid,
                    unionid,
                    request.getNickname(),
                    request.getAvatarUrl()
                );

                log.info("用户自动注册成功: userId={}, username={}", user.getUId(), user.getUsername());
            } else {
                log.info("用户已存在: userId={}, username={}", user.getUId(), user.getUsername());

                // 更新最后登录时间
                user.setLastLoginTime(LocalDateTime.now());
                user.setUpdatedAt(LocalDateTime.now());
                userMapper.updateById(user);
            }

            // 4. 生成 JWT Token
            String token = jwtUtil.generateToken(user.getUId(), user.getUsername(), user.getRole());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUId());

            // 5. 构建用户信息 VO
            UserVO userVO = new UserVO();
            userVO.setUId(user.getUId());
            userVO.setUsername(user.getUsername());
            userVO.setNickname(user.getNickname());
            userVO.setAvatarUrl(user.getAvatarUrl());
            userVO.setRole(user.getRole());
            userVO.setPoints(user.getPoints());
            userVO.setLevel(user.getLevel());

            // 6. 构建响应
            AuthResponse response = new AuthResponse(token, refreshToken, userVO);

            log.info("微信登录成功: userId={}, token已生成", user.getUId());

            return Result.success(response);

        } catch (BusinessException e) {
            log.error("微信登录失败（业务异常）: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("微信登录失败（系统异常）", e);
            throw new BusinessException("登录失败，请稍后重试");
        }
    }

    /**
     * 绑定已有账号
     */
    @PostMapping("/bind-account")
    @Operation(summary = "绑定已有账号", description = "将微信账号绑定到已有的用户名密码账号")
    public Result<String> bindAccount(@Valid @RequestBody BindAccountRequest request,
                                    @RequestHeader("Authorization") String token) {
        log.info("绑定账号请求: username={}", request.getUsername());

        try {
            // 1. 验证 Token 获取当前微信用户
            Long currentUserId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
            User currentUser = userService.getUserById(currentUserId) != null ? userMapper.selectById(currentUserId) : null;

            if (currentUser == null) {
                throw new BusinessException("用户不存在");
            }

            if (currentUser.getWechatOpenid() == null) {
                throw new BusinessException("当前用户不是微信登录用户");
            }

            // 2. 查找要绑定的用户
            User existingUser = userService.findByWechatOpenid(null); // 先定义，下面实现查找逻辑
            // 使用 mapper 直接查询用户名
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, request.getUsername());
            existingUser = userMapper.selectOne(wrapper);

            if (existingUser == null) {
                throw new BusinessException("用户名不存在");
            }

            // 验证密码
            String md5Password = DigestUtils.md5DigestAsHex(request.getPassword().getBytes());
            if (!md5Password.equals(existingUser.getPasswordHash())) {
                throw new BusinessException("密码错误");
            }

            // 3. 检查目标账号是否已绑定微信
            if (existingUser.getWechatOpenid() != null) {
                throw new BusinessException("该账号已绑定其他微信");
            }

            // 4. 绑定 openid 到已有用户
            existingUser.setWechatOpenid(currentUser.getWechatOpenid());
            existingUser.setWechatUnionid(currentUser.getWechatUnionid());

            // 5. 合并数据（积分、等级取较大值）
            existingUser.setPoints(Math.max(existingUser.getPoints(), currentUser.getPoints()));
            existingUser.setLevel(Math.max(existingUser.getLevel(), currentUser.getLevel()));
            existingUser.setUpdatedAt(LocalDateTime.now());

            userMapper.updateById(existingUser);

            // 6. 删除临时微信用户记录
            userMapper.deleteById(currentUser.getUId());

            log.info("账号绑定成功: username={}, openid={}", request.getUsername(), currentUser.getWechatOpenid());

            return Result.success("账号绑定成功");

        } catch (BusinessException e) {
            log.error("账号绑定失败（业务异常）: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("账号绑定失败（系统异常）", e);
            throw new BusinessException("绑定失败，请稍后重试");
        }
    }
}
