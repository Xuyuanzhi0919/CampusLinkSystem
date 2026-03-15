package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.common.ResultCode;
import com.campuslink.dto.AuthResponse;
import com.campuslink.dto.ChangePasswordRequest;
import com.campuslink.dto.LoginRequest;
import com.campuslink.dto.UserVO;
import com.campuslink.entity.User;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.*;
import com.campuslink.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.DigestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserMapper userMapper;
    @Mock private PointsLogMapper pointsLogMapper;
    @Mock private SchoolMapper schoolMapper;
    @Mock private JwtUtil jwtUtil;
    @Mock private EmailCodeService emailCodeService;
    @Mock private ResourceMapper resourceMapper;
    @Mock private DownloadLogMapper downloadLogMapper;
    @Mock private QuestionMapper questionMapper;
    @Mock private AnswerMapper answerMapper;
    @Mock private TaskMapper taskMapper;
    @Mock private FavoriteMapper favoriteMapper;
    @Mock private SystemConfigMapper systemConfigMapper;
    @Mock private LevelService levelService;

    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setUId(1L);
        mockUser.setUsername("testuser");
        mockUser.setNickname("测试用户");
        mockUser.setPasswordHash(DigestUtils.md5DigestAsHex("password123".getBytes()));
        mockUser.setStatus(1); // 正常
        mockUser.setRole("student");
        mockUser.setPoints(200);
        mockUser.setLevel(1);
    }

    // ─────────────── getUserById ───────────────

    @Test
    @DisplayName("getUserById: 用户存在时正常返回")
    void getUserById_whenUserExists_returnsVO() {
        when(userMapper.selectById(1L)).thenReturn(mockUser);

        UserVO vo = userService.getUserById(1L);

        assertThat(vo).isNotNull();
        assertThat(vo.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("getUserById: 用户不存在时抛出 USER_NOT_FOUND")
    void getUserById_whenUserNotFound_throwsException() {
        when(userMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户不存在");
    }

    // ─────────────── login ───────────────

    @Test
    @DisplayName("login: 正确账密时返回 Token")
    void login_withCorrectCredentials_returnsAuthResponse() {
        LoginRequest request = new LoginRequest();
        request.setAccount("testuser");
        request.setPassword("password123");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);
        when(jwtUtil.generateToken(anyLong(), anyString(), anyString())).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(anyLong())).thenReturn("refresh-token");

        AuthResponse response = userService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    @DisplayName("login: 用户不存在时抛出异常")
    void login_whenUserNotFound_throwsException() {
        LoginRequest request = new LoginRequest();
        request.setAccount("nonexistent");
        request.setPassword("any");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户不存在");
    }

    @Test
    @DisplayName("login: 密码错误时抛出 PASSWORD_ERROR")
    void login_withWrongPassword_throwsException() {
        LoginRequest request = new LoginRequest();
        request.setAccount("testuser");
        request.setPassword("wrongpassword");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockUser);

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ResultCode.PASSWORD_ERROR.getMessage());
    }

    @Test
    @DisplayName("login: 账号被禁用时抛出 USER_DISABLED")
    void login_whenUserDisabled_throwsException() {
        mockUser.setStatus(0); // 禁用状态
        LoginRequest request = new LoginRequest();
        request.setAccount("testuser");
        request.setPassword("password123");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockUser);

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ResultCode.USER_DISABLED.getMessage());
    }

    // ─────────────── changePassword ───────────────

    @Test
    @DisplayName("changePassword: 旧密码正确时修改成功")
    void changePassword_withCorrectOldPassword_succeeds() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("password123");
        request.setNewPassword("newpassword456");

        when(userMapper.selectById(1L)).thenReturn(mockUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        userService.changePassword(1L, request);

        verify(userMapper, times(1)).updateById(argThat((User u) ->
                u.getPasswordHash().equals(DigestUtils.md5DigestAsHex("newpassword456".getBytes()))
        ));
    }

    @Test
    @DisplayName("changePassword: 旧密码错误时抛出异常")
    void changePassword_withWrongOldPassword_throwsException() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongold");
        request.setNewPassword("newpassword456");

        when(userMapper.selectById(1L)).thenReturn(mockUser);

        assertThatThrownBy(() -> userService.changePassword(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("旧密码不正确");
    }

    @Test
    @DisplayName("changePassword: 新旧密码相同时抛出异常")
    void changePassword_whenNewSameAsOld_throwsException() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("password123");
        request.setNewPassword("password123"); // 与旧密码相同

        when(userMapper.selectById(1L)).thenReturn(mockUser);

        assertThatThrownBy(() -> userService.changePassword(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("新密码不能与旧密码相同");
    }
}
