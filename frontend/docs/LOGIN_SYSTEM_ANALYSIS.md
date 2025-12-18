# 前端登录/注册系统完整分析

## 📋 目录

1. [系统概览](#1-系统概览)
2. [LoginModal 详细分析](#2-loginmodal-详细分析)
3. [RegisterModal 详细分析](#3-registermodal-详细分析)
4. [模态系统使用示例](#4-模态系统使用示例)
5. [与页面登录的对比](#5-与页面登录的对比)
6. [发现的问题与建议](#6-发现的问题与建议)
7. [最佳实践](#7-最佳实践)

---

## 1. 系统概览

### 1.1 现有登录/注册方式

CampusLink 前端目前支持 **3 种登录/注册方式**:

| 登录方式 | 文件路径 | 适用平台 | 使用场景 |
|---------|---------|---------|---------|
| **H5 登录弹窗** | `components/LoginModal.vue` | H5 | 用户在首页/其他页面快速登录 ✅ **推荐** |
| **H5 注册弹窗** | `components/RegisterModal.vue` | H5 | 用户在首页/其他页面注册账号 ✅ **推荐** |
| **H5 登录页面** | `pages/auth/login.vue` | H5 | 独立登录页面（**可能冗余**）⚠️ |
| **小程序登录** | `pages/auth/mp-login.vue` | 微信小程序 | 微信一键登录 ✅ |

### 1.2 模态系统架构

```
首页 (pages/home/index.vue)
  ├── LoginModal (登录弹窗)
  │     ├── 事件: @login-success
  │     ├── 事件: @register (跳转注册弹窗)
  │     └── 事件: @forgot-password
  │
  └── RegisterModal (注册弹窗)
        ├── 事件: @register-success
        └── 事件: @go-to-login (返回登录弹窗)
```

**弹窗调用位置**（`pages/home/index.vue`）:
```vue
<!-- 登录弹窗 -->
<LoginModal
  :visible="showLoginModal"
  @update:visible="showLoginModal = $event"
  @login-success="handleLoginSuccess"
  @register="handleRegister"
  @forgot-password="handleForgotPassword"
/>

<!-- 注册弹窗 -->
<RegisterModal
  :visible="showRegisterModal"
  @update:visible="showRegisterModal = $event"
  @register-success="handleRegisterSuccess"
  @go-to-login="handleSwitchToLogin"
/>
```

**触发方式**:
```vue
<!-- WebHeader 点击登录按钮 -->
<WebHeader @login="showLoginModal = true" />
```

---

## 2. LoginModal 详细分析

### 2.1 UI 特性

#### **品牌头部**（行 10-21）
- 横向 Logo：304x120rpx (152x60px)
- 双行标语：
  - 主标语：「连接校园每一刻」(36rpx, #1F2937, 字重 600)
  - 副标语：「让学习与分享更简单」(26rpx, #9CA3AF, 字重 400)
- 分割线：120rpx 宽，灰色渐变

#### **表单输入**（行 24-92）
```vue
<!-- 邮箱/手机号输入 -->
<input v-model="formData.account" placeholder="邮箱或手机号" />

<!-- 密码输入（支持显示/隐藏）-->
<input :type="showPassword ? 'text' : 'password'" v-model="formData.password" />
<text class="password-toggle" @tap="showPassword = !showPassword">
  {{ showPassword ? '👁' : '👁‍🗨' }}
</text>
```

**输入框焦点效果**（行 613-626）:
- 未聚焦：`background: rgba(255, 255, 255, 0.7)`，灰色边框
- 聚焦时：
  - 背景变白：`rgba(255, 255, 255, 0.98)`
  - 蓝色渐变边框：`#2563EB → #06B6D4`
  - 蓝色光晕：`box-shadow: 0 0 0 6rpx rgba(59, 130, 246, 0.18)`
  - 图标变蓝：渐变从灰色变为蓝色 + 发光效果

**图标动画**（行 670-697）:
- 聚焦时图标放大：`transform: scale(1.08)`
- 锁图标特殊动效：`lockBounce` 动画（上弹 -4rpx）
- 密码切换图标 hover：旋转 10° + 发光

#### **记住账号 + 忘记密码**（行 58-67）
```vue
<view class="form-options">
  <view class="remember-me" @tap="formData.rememberMe = !formData.rememberMe">
    <view class="checkbox" :class="{ 'checkbox-checked': formData.rememberMe }">
      <text v-if="formData.rememberMe">✓</text>
    </view>
    <text>记住账号</text>
  </view>
  <text class="forgot-password" @tap="handleForgotPassword">忘记密码?</text>
</view>
```

**记住账号逻辑**（行 182-188, 353-357）:
- 挂载时：从 `uni.storage` 读取 `campuslink_remember_account`
- 登录成功：如果勾选「记住账号」，保存 `account` 到 storage
- 清空逻辑：如果不勾选，删除 storage

#### **登录按钮**（行 70-73, 805-891）
```vue
<button class="login-btn" :class="{ 'btn-loading': loading }" @tap="handleLogin">
  <text v-if="!loading">登录</text>
  <text v-else class="loading-text">登录中...</text>
</button>
```

**按钮动画效果**:
- 默认：蓝→青渐变 (`#2563EB → #38BDF8`)
- Hover：渐变更深 + 上浮 `-4rpx` + 放大 `scale(1.02)` + 青色发光边
- 点击：涟漪扩散动画（`::after` 伪元素从 0 扩散到 600rpx）
- Loading：半透明 + 禁用点击 + 文字脉冲动画

#### **第三方登录**（行 82-92）
```vue
<view class="social-login">
  <view class="wechat-btn" @tap="handleWechatLogin">
    <text class="social-icon">💬</text>
    <text>微信登录</text>
  </view>
  <view class="qq-btn" @tap="handleQQLogin">
    <text class="social-icon">🐧</text>
    <text>QQ登录</text>
  </view>
</view>
```

**按钮 Hover 动画**（行 941-994）:
- 微信：淡绿色渐变背景 + 图标呼吸动画
- QQ：淡蓝色渐变背景 + 图标呼吸动画

#### **注册引导**（行 95-98）
```vue
<view class="register-tip">
  <text class="tip-text">还没有账号?</text>
  <text class="register-link" @tap="handleRegister">立即注册</text>
</view>
```

**文字 Hover 效果**（行 1054-1067）:
- 常态：灰色 (`#6B7280`)
- Hover：品牌蓝渐变 + 下划线展开动画 + 轻微放大

---

### 2.2 核心功能

#### **企业级 Toast 通知系统**（行 199-315）

**4 种状态配色方案**:
```typescript
const typeConfig = {
  success: {
    icon: '✓',
    bg: 'rgba(34, 197, 94, 0.15)',      // 淡绿背景
    iconColor: '#22C55E',                // 绿色图标
    textColor: '#166534',                // 深绿文字
    border: '#22C55E',                   // 绿色左边框
    iconBg: 'linear-gradient(135deg, #22C55E 0%, #16A34A 100%)' // 渐变圆形图标背景
  },
  error: {
    icon: '✕',
    bg: 'rgba(239, 68, 68, 0.15)',      // 淡红背景
    iconColor: '#EF4444',
    textColor: '#7F1D1D',
    border: '#EF4444',
    iconBg: 'linear-gradient(135deg, #EF4444 0%, #DC2626 100%)'
  },
  warning: {
    icon: '⚠',
    bg: 'rgba(250, 204, 21, 0.15)',     // 淡黄背景
    iconColor: '#FACC15',
    textColor: '#92400E',
    border: '#FACC15',
    iconBg: 'linear-gradient(135deg, #FACC15 0%, #EAB308 100%)'
  },
  info: {
    icon: 'ℹ',
    bg: 'rgba(59, 130, 246, 0.15)',     // 淡蓝背景
    iconColor: '#3B82F6',
    textColor: '#1E3A8A',
    border: '#3B82F6',
    iconBg: 'linear-gradient(135deg, #3B82F6 0%, #2563EB 100%)'
  }
}
```

**Toast 结构** (HTML):
```html
<div style="
  position: fixed;
  top: 20%;
  left: 50%;
  transform: translate(-50%, 0);
  background: ${config.bg};
  backdrop-filter: blur(10px);         /* 背景模糊 */
  padding: 16px 24px;
  border-radius: 12px;
  border-left: 3px solid ${config.border};
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
  z-index: 10000;
  opacity: 1;
">
  <!-- 渐变圆形图标 -->
  <span style="
    width: 24px;
    height: 24px;
    border-radius: 50%;
    background: ${config.iconBg};
    color: white;
    box-shadow: 0 2px 8px ${config.iconColor}40;
  ">${config.icon}</span>

  <!-- 消息文字 -->
  <span style="color: ${config.textColor};">${message}</span>
</div>
```

**动画效果**（行 288-305）:
- 淡入：从 `opacity: 0` + `translateY(-8px)` → `opacity: 1` + `translateY(0)`
- 停留：3 秒
- 淡出：`opacity: 0` + `translateY(-8px)` → 250ms 后移除 DOM

**使用示例**:
```typescript
// 成功提示
showToast('登录成功 ✓', 'success')

// 错误提示
showToast('密码好像不对呢，再想想？', 'warning')

// 信息提示
showToast('请填写账号和密码哦 ~', 'info')
```

#### **表单验证**（行 318-328）
```typescript
const validateForm = () => {
  emailError.value = !formData.value.account
  passwordError.value = !formData.value.password

  if (emailError.value || passwordError.value) {
    showToast('请填写账号和密码哦 ~', 'info')
    return false
  }

  return true
}
```

#### **登录处理**（行 331-392）
```typescript
const handleLogin = async () => {
  if (!validateForm()) return

  loading.value = true

  try {
    // 1. 调用登录 API
    const response: AuthResponse = await login({
      account: formData.value.account,
      password: formData.value.password
    })

    // 2. 保存到 Store（同时存储到 localStorage）
    userStore.login({
      token: response.token,
      refreshToken: response.refreshToken,
      userInfo: response.user  // ⚠️ 注意：使用 userInfo 字段
    })

    // 3. 记住账号
    if (formData.value.rememberMe) {
      uni.setStorageSync('campuslink_remember_account', formData.value.account)
    } else {
      uni.removeStorageSync('campuslink_remember_account')
    }

    // 4. 触发成功事件（由父组件处理欢迎提示和跳转）
    emit('login-success', response)
    handleClose()
  } catch (error: any) {
    // 5. 错误处理（品牌化友好提示）
    let errorMessage = '登录遇到问题了，稍后再试试吧 ~'
    let errorType: 'error' | 'warning' | 'info' = 'error'

    if (error.message.includes('用户不存在')) {
      errorMessage = '没找到这个账号哦，确认一下邮箱或手机号？'
      errorType = 'warning'
    } else if (error.message.includes('密码错误')) {
      errorMessage = '密码好像不对呢，再想想？'
      errorType = 'warning'
    } else if (error.message.includes('账号已被禁用')) {
      errorMessage = '账号暂时无法使用，有疑问可以联系管理员哦'
      errorType = 'error'
    } else if (error.message.includes('网络')) {
      errorMessage = '网络好像断了，检查一下网络连接吧 ~'
      errorType = 'warning'
    }

    showToast(errorMessage, errorType)
  } finally {
    loading.value = false
  }
}
```

#### **微信登录**（行 394-409）
```typescript
const handleWechatLogin = () => {
  // #ifdef MP-WEIXIN
  uni.showToast({
    title: '微信登录',
    icon: 'none'
  })
  // #endif

  // #ifndef MP-WEIXIN
  uni.showToast({
    title: '请在微信中打开',
    icon: 'none'
  })
  // #endif
}
```

**注意**: 这里只是提示，实际微信登录在 `pages/auth/mp-login.vue` 中实现。

#### **ESC 键关闭弹窗**（行 152-178）
```typescript
// H5 平台支持 ESC 键关闭
const handleKeyDown = (e: KeyboardEvent) => {
  if (e.key === 'Escape' && props.visible) {
    handleClose()
  }
}

// 弹窗打开时添加键盘监听
watch(() => props.visible, (newVal) => {
  if (newVal) {
    // #ifdef H5
    if (typeof window !== 'undefined') {
      window.addEventListener('keydown', handleKeyDown)
    }
    // #endif
  } else {
    // #ifdef H5
    if (typeof window !== 'undefined') {
      window.removeEventListener('keydown', handleKeyDown)
    }
    // #endif
  }
})
```

---

### 2.3 样式设计

#### **遮罩层渐变背景**（行 434-453）
```scss
.login-modal-mask {
  background: radial-gradient(
    ellipse at center,
    rgba(56, 189, 248, 0.08) 0%,      // 中心：青色淡光
    rgba(37, 99, 235, 0.12) 30%,      // 过渡：蓝色
    rgba(15, 23, 42, 0.75) 100%       // 边缘：深灰
  );
  backdrop-filter: blur(14px);        // 背景模糊
}
```

**设计思路**: 径向渐变营造"聚光舞台感"，中心亮区吸引用户注意力到弹窗。

#### **弹窗容器**（行 465-494）
```scss
.login-modal-container {
  width: 840rpx; // 420px
  max-height: 92vh;
  background: rgba(255, 255, 255, 0.88);  // 半透明白
  backdrop-filter: blur(36rpx);           // 背景模糊
  border-radius: 52rpx;                   // 26px，柔和圆角
  box-shadow:
    0 16rpx 64rpx rgba(37, 99, 235, 0.15),
    0 0 2rpx rgba(37, 99, 235, 0.1);
  padding: 44rpx 48rpx 40rpx;

  // 入场动画
  transform: scale(0.95);
  opacity: 0;
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1); // 弹性缓动

  &.modal-show {
    transform: scale(1);
    opacity: 1;
  }
}
```

**设计思路**: 玻璃拟态（Glassmorphism）+ 弹性缓动，营造轻盈感。

---

## 3. RegisterModal 详细分析

### 3.1 UI 特性

#### **表单字段**（行 22-148）
```vue
<!-- 1. 邮箱或手机号 -->
<input v-model="formData.account" placeholder="邮箱或手机号" />

<!-- 2. 用户名（自动生成 + 手动修改）-->
<input v-model="formData.username" placeholder="用户名（3-20字符，字母数字下划线）" />
<text v-if="usernameChecking">⏳</text>
<text v-else-if="usernameAvailable === true">✓</text>
<text v-else-if="usernameAvailable === false">✕</text>

<!-- 3. 昵称（可选）-->
<input v-model="formData.nickname" placeholder="昵称（选填，默认与用户名相同）" />

<!-- 4. 密码 -->
<input :type="showPassword ? 'text' : 'password'" v-model="formData.password" />

<!-- 5. 密码强度条 -->
<view class="password-strength-bar">
  <view class="strength-bar-fill" :class="[`strength-${passwordStrength.level}`]" :style="{ width: passwordStrength.percent + '%' }"></view>
  <text class="strength-text">{{ passwordStrength.text }}</text>
</view>

<!-- 6. 确认密码 -->
<input :type="showConfirmPassword ? 'text' : 'password'" v-model="formData.confirmPassword" />

<!-- 7. 验证码 -->
<input v-model="formData.code" placeholder="验证码" />
<button :disabled="codeCountdown > 0 || !canSendCode" @tap="handleSendCode">
  {{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}
</button>
```

#### **密码强度计算**（行 228-252）
```typescript
const passwordStrength = computed(() => {
  const pwd = formData.value.password
  if (!pwd) return { level: 'weak', percent: 0, text: '' }

  let score = 0
  // 长度检查
  if (pwd.length >= 6) score += 25
  if (pwd.length >= 8) score += 25
  // 包含小写字母
  if (/[a-z]/.test(pwd)) score += 15
  // 包含大写字母
  if (/[A-Z]/.test(pwd)) score += 15
  // 包含数字
  if (/[0-9]/.test(pwd)) score += 10
  // 包含特殊字符
  if (/[^a-zA-Z0-9]/.test(pwd)) score += 10

  if (score < 40) {
    return { level: 'weak', percent: score, text: '弱' }
  } else if (score < 70) {
    return { level: 'medium', percent: score, text: '中' }
  } else {
    return { level: 'strong', percent: score, text: '强' }
  }
})
```

**强度条配色**（行 1017-1046）:
- **弱**：红色渐变 (`#EF4444 → #F87171`)
- **中**：黄色渐变 (`#F59E0B → #FBBF24`)
- **强**：绿色渐变 (`#10B981 → #34D399`)

#### **用户名自动生成**（行 394-431）
```typescript
// 生成用户名规则
const generateUsername = (account: string): string => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  const isEmail = emailRegex.test(account)

  if (isEmail) {
    // 邮箱：提取@前的部分，去除特殊字符，只保留字母数字下划线
    return account.split('@')[0].replace(/[^a-zA-Z0-9_]/g, '_')
  } else {
    // 手机号：添加user_前缀
    return 'user_' + account.replace(/[^0-9]/g, '')
  }
}

// 账号失焦时自动填充
const handleAccountBlur = () => {
  accountFocused.value = false

  if (formData.value.account && canSendCode.value) {
    // 只在用户名为空时自动填充
    if (!formData.value.username) {
      const generatedUsername = generateUsername(formData.value.account)
      formData.value.username = generatedUsername
      usernameHint.value = '已自动生成，可修改'
    }

    // 只在昵称为空时自动填充
    if (!formData.value.nickname) {
      // 邮箱取@前的部分，手机号取前20字符
      const isEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.value.account)
      if (isEmail) {
        formData.value.nickname = formData.value.account.split('@')[0].substring(0, 20)
      } else {
        formData.value.nickname = formData.value.account.substring(0, 20)
      }
    }
  }
}
```

**示例**:
- 输入邮箱 `zhangsan@qq.com` → 自动生成用户名 `zhangsan`，昵称 `zhangsan`
- 输入手机号 `13812345678` → 自动生成用户名 `user_13812345678`，昵称 `13812345678`

#### **用户名可用性检查**（行 434-475）
```typescript
const handleUsernameBlur = async () => {
  usernameFocused.value = false

  const username = formData.value.username.trim()

  // 清空之前的状态
  usernameAvailable.value = null
  usernameHint.value = ''

  if (!username) {
    usernameError.value = true
    usernameHint.value = '用户名不能为空'
    return
  }

  // 验证格式
  if (username.length < 3 || username.length > 20) {
    usernameError.value = true
    usernameAvailable.value = false
    usernameHint.value = '用户名长度需要3-20个字符'
    return
  }

  if (!/^[a-zA-Z0-9_]+$/.test(username)) {
    usernameError.value = true
    usernameAvailable.value = false
    usernameHint.value = '只能包含字母、数字和下划线'
    return
  }

  // TODO: 调用后端API检查用户名是否已被占用
  // 暂时跳过后端检查，假设都可用
  usernameChecking.value = true

  // 模拟API调用延迟
  await new Promise(resolve => setTimeout(resolve, 500))

  usernameChecking.value = false
  usernameError.value = false
  usernameAvailable.value = true
  usernameHint.value = '用户名可用 ✓'
}
```

**视觉反馈**:
- 检查中：⏳ 旋转动画
- 可用：✓ 绿色图标 + 「用户名可用 ✓」
- 不可用：✕ 红色图标 + 错误提示

#### **验证码倒计时**（行 522-564）
```typescript
const handleSendCode = async () => {
  if (!canSendCode.value || codeCountdown.value > 0) return

  try {
    await sendCode({
      account: formData.value.account,
      type: 'register'
    })

    showToast('验证码已发送到您的邮箱或手机 ✓', 'success')

    // 开始倒计时
    codeCountdown.value = 60
    countdownTimer = setInterval(() => {
      codeCountdown.value--
      if (codeCountdown.value <= 0 && countdownTimer) {
        clearInterval(countdownTimer)
        countdownTimer = null
      }
    }, 1000) as unknown as number
  } catch (error: any) {
    // 错误处理（品牌化友好提示）
    let errorMessage = '验证码发送失败，稍后再试吧 ~'
    if (error.message.includes('频繁')) {
      errorMessage = '发送太频繁啦，休息一下再试吧 ~'
    }
    showToast(errorMessage, 'error')
  }
}
```

**按钮状态**:
- 默认：蓝色渐变「获取验证码」
- 倒计时：灰色渐变「60s」→「59s」→ ... →「1s」
- 倒计时结束：恢复蓝色「获取验证码」

---

### 3.2 注册流程

#### **表单验证**（行 567-617）
```typescript
const validateForm = () => {
  accountError.value = !formData.value.account
  usernameError.value = !formData.value.username || formData.value.username.length < 3 || formData.value.username.length > 20
  passwordError.value = !formData.value.password || formData.value.password.length < 6
  confirmPasswordError.value = formData.value.password !== formData.value.confirmPassword

  if (accountError.value) {
    showToast('请填写邮箱或手机号哦 ~', 'info')
    return false
  }

  if (!canSendCode.value) {
    showToast('邮箱或手机号格式好像不对呢 🤔', 'warning')
    return false
  }

  if (usernameError.value) {
    showToast('用户名需要3-20个字符哦 ~', 'info')
    return false
  }

  if (!/^[a-zA-Z0-9_]+$/.test(formData.value.username)) {
    showToast('用户名只能包含字母、数字和下划线 ~', 'warning')
    return false
  }

  if (usernameAvailable.value === false) {
    showToast('这个用户名已经被占用了，换一个试试？', 'warning')
    return false
  }

  if (passwordError.value) {
    showToast('密码至少需要6位哦，安全第一 ~', 'info')
    return false
  }

  if (confirmPasswordError.value) {
    showToast('两次输入的密码不一样，再确认一下？', 'warning')
    return false
  }

  return true
}
```

#### **注册处理**（行 620-693）
```typescript
const handleRegister = async () => {
  if (!validateForm()) return

  loading.value = true

  try {
    // 1. 判断是邮箱还是手机号
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    const isEmail = emailRegex.test(formData.value.account)

    // 2. 构造注册数据
    const registerData: RegisterRequest = {
      username: formData.value.username.trim(),
      password: formData.value.password,
      email: isEmail ? formData.value.account : undefined,
      phone: !isEmail ? formData.value.account : undefined,
      nickname: formData.value.nickname.trim() || formData.value.username.trim(), // 如果昵称为空则使用用户名
      schoolId: 1, // TODO: 后续添加学校选择
      code: formData.value.code || '000000' // 临时处理：验证码可选
    }

    // 3. 调用注册 API
    const response: AuthResponse = await register(registerData)

    // 4. 存储 Token（自动登录）
    uni.setStorageSync(config.tokenKey, response.token)
    uni.setStorageSync(config.refreshTokenKey, response.refreshToken)
    uni.setStorageSync(config.userInfoKey, JSON.stringify(response.user))

    // 5. 显示成功状态
    registerSuccess.value = true

    // 6. 触发成功事件（由父组件处理欢迎提示和跳转）
    emit('register-success', response)
    handleClose()
  } catch (error: any) {
    // 7. 错误处理（品牌化友好提示）
    let errorMessage = '注册遇到问题了，稍后再试试吧 ~'
    let errorType: 'error' | 'warning' = 'error'

    if (error.message.includes('用户名已存在')) {
      errorMessage = '这个账号已经被注册过啦，要不试试登录？'
      errorType = 'warning'
    } else if (error.message.includes('邮箱') && error.message.includes('已')) {
      errorMessage = '这个邮箱已经注册过了哦 ~'
      errorType = 'warning'
    } else if (error.message.includes('验证码错误')) {
      errorMessage = '验证码好像不对，再检查一下？'
      errorType = 'warning'
    }

    showToast(errorMessage, errorType)
  } finally {
    loading.value = false
  }
}
```

---

## 4. 模态系统使用示例

### 4.1 在首页中使用

**完整示例**（`pages/home/index.vue`）:
```vue
<template>
  <view class="home-page">
    <!-- 顶部导航栏 -->
    <WebHeader @login="showLoginModal = true" />

    <!-- 主要内容 -->
    <view class="main-content">
      ...
    </view>

    <!-- 登录弹窗 -->
    <LoginModal
      :visible="showLoginModal"
      @update:visible="showLoginModal = $event"
      @login-success="handleLoginSuccess"
      @register="handleRegister"
      @forgot-password="handleForgotPassword"
    />

    <!-- 注册弹窗 -->
    <RegisterModal
      :visible="showRegisterModal"
      @update:visible="showRegisterModal = $event"
      @register-success="handleRegisterSuccess"
      @go-to-login="handleSwitchToLogin"
    />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import LoginModal from '@/components/LoginModal.vue'
import RegisterModal from '@/components/RegisterModal.vue'

const userStore = useUserStore()
const showLoginModal = ref(false)
const showRegisterModal = ref(false)

// 登录成功
const handleLoginSuccess = (response: AuthResponse) => {
  // 1. 显示欢迎提示
  uni.showToast({
    title: `欢迎回来，${response.user.nickname || response.user.username} 🎉`,
    icon: 'success',
    duration: 2000
  })

  // 2. 刷新页面数据（如果需要）
  refreshPageData()
}

// 注册成功
const handleRegisterSuccess = (response: AuthResponse) => {
  // 1. 更新 Store（如果注册弹窗没有自动登录）
  userStore.login({
    token: response.token,
    refreshToken: response.refreshToken,
    userInfo: response.user
  })

  // 2. 显示欢迎提示
  uni.showToast({
    title: `注册成功，欢迎加入 CampusLink！🎉`,
    icon: 'success',
    duration: 2000
  })

  // 3. 刷新页面数据
  refreshPageData()
}

// 切换到注册弹窗
const handleRegister = () => {
  showLoginModal.value = false
  showRegisterModal.value = true
}

// 切换到登录弹窗
const handleSwitchToLogin = () => {
  showRegisterModal.value = false
  showLoginModal.value = true
}

// 忘记密码
const handleForgotPassword = () => {
  uni.showToast({
    title: '找回密码功能开发中...',
    icon: 'none'
  })
  // TODO: 创建找回密码页面
}
</script>
```

---

### 4.2 在其他页面中使用

**示例：资源详情页需要登录才能下载**:
```vue
<template>
  <view class="resource-detail-page">
    <!-- 下载按钮 -->
    <button @tap="handleDownload">下载资源</button>

    <!-- 登录弹窗 -->
    <LoginModal
      :visible="showLoginModal"
      @update:visible="showLoginModal = $event"
      @login-success="handleLoginSuccess"
      @register="handleRegister"
    />

    <!-- 注册弹窗 -->
    <RegisterModal
      :visible="showRegisterModal"
      @update:visible="showRegisterModal = $event"
      @register-success="handleRegisterSuccess"
      @go-to-login="handleSwitchToLogin"
    />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const showLoginModal = ref(false)
const showRegisterModal = ref(false)

const handleDownload = () => {
  // 检查是否登录
  if (!userStore.isLoggedIn) {
    showLoginModal.value = true
    return
  }

  // 已登录，执行下载
  downloadResource()
}

const handleLoginSuccess = () => {
  // 登录成功后自动下载
  downloadResource()
}

const downloadResource = () => {
  // 下载资源逻辑
  uni.showToast({
    title: '开始下载...',
    icon: 'success'
  })
}
</script>
```

---

## 5. 与页面登录的对比

### 5.1 功能对比表

| 特性 | LoginModal (弹窗) | pages/auth/login.vue (页面) |
|------|------------------|---------------------------|
| **UI 形式** | 弹窗/模态框 | 独立页面 |
| **适用场景** | H5 快速登录（首页/详情页等）| 小程序/独立登录入口 |
| **用户体验** | ⭐⭐⭐⭐⭐ 无需跳转，不打断浏览流程 | ⭐⭐⭐ 需要页面跳转，打断浏览 |
| **动画效果** | ⭐⭐⭐⭐⭐ 渐变图标/涟漪/呼吸/锁动画 | ⭐⭐⭐ 基础淡入 |
| **Toast 系统** | ✅ 企业级 4 色体系（H5 自定义 Toast）| ❌ 使用 uni.showToast |
| **第三方登录** | ✅ 微信 + QQ（条件编译）| ✅ 微信（条件编译）|
| **表单字段** | account + password | account + password |
| **记住账号** | ✅ 保存到 `campuslink_remember_account` | ❌ |
| **ESC 关闭** | ✅ (H5) | ❌ |
| **测试账号快速填充** | ❌ | ✅ (admin/admin123, testuser001/password123) |
| **Store 集成** | `userInfo: response.user` | `user: response.user` ⚠️ **字段名不一致** |
| **关闭方式** | ESC / 点击遮罩 / 关闭按钮 | 无（页面返回）|
| **品牌头部** | ✅ 横向 Logo + 双行标语 | ✅ Logo + 标题 + 副标题 |
| **注册引导** | ✅ 点击跳转注册弹窗 | ✅ 注册链接（TODO）|

---

### 5.2 代码复用性对比

#### **LoginModal（弹窗）**
- ✅ **可在任意页面使用**：首页、详情页、评论区等
- ✅ **不打断用户操作**：登录成功后留在当前页面
- ✅ **统一的 Toast 风格**：企业级多状态配色
- ✅ **更好的视觉反馈**：渐变动画、聚焦效果、涟漪扩散

#### **pages/auth/login.vue（页面）**
- ⚠️ **需要页面跳转**：`uni.navigateTo({ url: '/pages/auth/login' })`
- ⚠️ **登录后跳转逻辑复杂**：需要判断从哪个页面来，登录后返回原页面
- ✅ **独立路由**：可以直接访问 `http://localhost:5173/#/pages/auth/login`
- ✅ **测试账号快速填充**：方便测试

---

### 5.3 建议的使用场景

| 场景 | 推荐方式 | 理由 |
|------|---------|------|
| **H5 首页登录** | `LoginModal` | 不打断浏览流程 ✅ |
| **H5 详情页需登录** | `LoginModal` | 登录后留在当前页面 ✅ |
| **H5 独立登录入口** | `pages/auth/login.vue` | 用户主动访问登录页面（可选）⚠️ |
| **小程序登录** | `pages/auth/mp-login.vue` | 微信一键登录 ✅ |
| **H5 测试登录** | `pages/auth/login.vue` | 测试账号快速填充方便测试 ✅ |

---

## 6. 发现的问题与建议

### 6.1 字段名不一致问题

#### **问题描述**

不同登录方式使用的字段名不一致：

| 登录方式 | Store 字段名 | 文件位置 |
|---------|------------|---------|
| LoginModal（H5 弹窗）| `userInfo: response.user` | `components/LoginModal.vue:349` |
| RegisterModal（H5 弹窗）| 直接存储到 storage（未使用 Store）| `components/RegisterModal.vue:644-646` |
| 页面登录（H5 页面）| `user: response.user` | `pages/auth/login.vue:132` |
| 微信登录（小程序）| `user: response.user` | `pages/auth/mp-login.vue` |

#### **影响**

虽然 `stores/user.ts` 已做兼容处理（同时接受 `userInfo` 和 `user`），但不同页面使用不同字段名会：
1. 增加维护成本
2. 容易造成混淆
3. 新开发者不知道该用哪个字段

#### **建议方案**

**方案 1：统一使用 `userInfo`**（推荐）
```typescript
// 所有登录方式统一使用 userInfo
userStore.login({
  token: response.token,
  refreshToken: response.refreshToken,
  userInfo: response.user  // ← 统一字段名
})
```

**优点**:
- 符合前端命名习惯（用户信息通常叫 `userInfo`）
- LoginModal 已经在用，改动最小

**方案 2：统一使用 `user`**
```typescript
// 所有登录方式统一使用 user
userStore.login({
  token: response.token,
  refreshToken: response.refreshToken,
  user: response.user  // ← 统一字段名
})
```

**优点**:
- 与后端返回字段一致（`data.user`）
- 微信登录和页面登录已经在用

---

### 6.2 RegisterModal 未使用 Store

#### **问题描述**

`RegisterModal.vue` 注册成功后，直接将 Token 和用户信息存储到 `uni.storage`，没有使用 `userStore.login()`:

```typescript
// RegisterModal.vue:644-646
uni.setStorageSync(config.tokenKey, response.token)
uni.setStorageSync(config.refreshTokenKey, response.refreshToken)
uni.setStorageSync(config.userInfoKey, JSON.stringify(response.user))
```

#### **影响**

1. **Store 状态未更新**：虽然 storage 有数据，但 `userStore` 的响应式状态（`isLoggedIn`、`userInfo`）未更新
2. **需要刷新页面**：用户注册成功后需要刷新页面才能看到登录状态
3. **行为不一致**：LoginModal 使用 Store，RegisterModal 不使用

#### **建议修复**

修改 `RegisterModal.vue:644-646`:
```typescript
// 修改前
uni.setStorageSync(config.tokenKey, response.token)
uni.setStorageSync(config.refreshTokenKey, response.refreshToken)
uni.setStorageSync(config.userInfoKey, JSON.stringify(response.user))

// 修改后
import { useUserStore } from '@/stores/user'
const userStore = useUserStore()

userStore.login({
  token: response.token,
  refreshToken: response.refreshToken,
  userInfo: response.user  // 或 user: response.user，统一即可
})
```

**优点**:
1. Store 响应式状态实时更新
2. 组件可以立即响应登录状态变化
3. 不需要刷新页面

---

### 6.3 pages/auth/login.vue 可能冗余

#### **问题描述**

用户提到: **"但是h5端有弹窗方式进行登录"**，说明 H5 端的首选登录方式是 `LoginModal`（弹窗），而不是 `pages/auth/login.vue`（页面）。

#### **影响**

1. **代码冗余**：两套 H5 登录 UI，维护成本增加
2. **用户体验不一致**：同一个平台（H5）有两种登录方式，容易混淆
3. **路由配置冗余**：`pages.json` 中多了一个不常用的路由

#### **建议方案**

**方案 1：保留页面登录（推荐）**

保留 `pages/auth/login.vue`，但仅用于特定场景：
- 独立登录入口（如：`http://example.com/#/pages/auth/login`）
- 测试账号快速填充（方便测试）
- 从其他平台跳转过来的登录（如：邮件链接）

**使用场景**:
```typescript
// 场景 1：首页快速登录 → 使用弹窗
const handleQuickLogin = () => {
  showLoginModal.value = true
}

// 场景 2：独立登录页面 → 使用页面
const handleStandaloneLogin = () => {
  uni.navigateTo({ url: '/pages/auth/login' })
}

// 场景 3：测试登录 → 使用页面（测试账号快速填充）
// 直接访问: http://localhost:5173/#/pages/auth/login
```

**方案 2：移除页面登录**

删除 `pages/auth/login.vue`，统一使用 `LoginModal`:
- 移除 `pages/auth/login.vue`
- 从 `pages.json` 中删除路由
- 所有 H5 登录场景统一使用 `LoginModal`

**优点**:
- 代码库更简洁
- 用户体验统一
- 维护成本降低

**缺点**:
- 失去测试账号快速填充功能（可以在弹窗中添加）
- 失去独立登录入口（可以创建一个单独的登录页面，仅包含 `LoginModal` 弹窗自动打开）

---

### 6.4 测试账号快速填充功能缺失

#### **问题描述**

`LoginModal` 没有测试账号快速填充功能，而 `pages/auth/login.vue` 有：

```vue
<!-- pages/auth/login.vue -->
<view class="test-accounts">
  <text class="test-title">测试账号：</text>
  <view class="test-item" @click="fillTestAccount('admin')">
    <text>admin</text>
    <text>admin123</text>
  </view>
  <view class="test-item" @click="fillTestAccount('testuser001')">
    <text>testuser001</text>
    <text>password123</text>
  </view>
</view>
```

#### **影响**

1. **测试不便**：开发/测试时需要手动输入账号密码
2. **体验不一致**：页面登录有快速填充，弹窗登录没有

#### **建议修复**

在 `LoginModal.vue` 中添加测试账号快速填充功能（开发环境显示）:

```vue
<!-- LoginModal.vue 添加 -->
<view v-if="isDev" class="test-accounts">
  <text class="test-title">测试账号：</text>
  <view class="test-item" @click="fillTestAccount('admin')">
    <text>admin</text>
    <text>admin123</text>
  </view>
  <view class="test-item" @click="fillTestAccount('testuser001')">
    <text>testuser001</text>
    <text>password123</text>
  </view>
</view>
```

```typescript
// LoginModal.vue script
import config from '@/config'

const isDev = ref(config.env === 'development')

const fillTestAccount = (username: string) => {
  if (username === 'admin') {
    formData.value.account = 'admin'
    formData.value.password = 'admin123'
  } else if (username === 'testuser001') {
    formData.value.account = 'testuser001'
    formData.value.password = 'password123'
  }

  uni.showToast({
    title: '已填充测试账号',
    icon: 'success',
    duration: 1000
  })
}
```

**注意**: 只在开发环境显示，生产环境自动隐藏。

---

### 6.5 用户名检查 API 缺失

#### **问题描述**

`RegisterModal.vue` 的用户名可用性检查（行 464-475）目前是假数据：

```typescript
// TODO: 调用后端API检查用户名是否已被占用
// 暂时跳过后端检查，假设都可用
usernameChecking.value = true

// 模拟API调用延迟
await new Promise(resolve => setTimeout(resolve, 500))

usernameChecking.value = false
usernameError.value = false
usernameAvailable.value = true
usernameHint.value = '用户名可用 ✓'
```

#### **影响**

1. **无法检测重复用户名**：用户填写已存在的用户名，直到提交注册时才报错
2. **用户体验差**：失败时需要重新填写整个表单

#### **建议修复**

**后端添加 API**:
```java
// UserController.java
@GetMapping("/auth/check-username")
@Operation(summary = "检查用户名是否可用")
public Result<Boolean> checkUsername(@RequestParam String username) {
    boolean available = userService.isUsernameAvailable(username);
    return Result.success(available);
}
```

**前端调用**:
```typescript
// services/auth.ts
export const checkUsername = (username: string) => {
  return request.get<boolean>('/auth/check-username', { params: { username } })
}
```

```typescript
// RegisterModal.vue
import { checkUsername } from '@/services/auth'

const handleUsernameBlur = async () => {
  usernameFocused.value = false

  const username = formData.value.username.trim()

  // ... 格式验证 ...

  // 调用后端API检查
  usernameChecking.value = true

  try {
    const available = await checkUsername(username)
    usernameChecking.value = false

    if (available) {
      usernameAvailable.value = true
      usernameHint.value = '用户名可用 ✓'
      usernameError.value = false
    } else {
      usernameAvailable.value = false
      usernameHint.value = '这个用户名已经被占用了'
      usernameError.value = true
    }
  } catch (error) {
    usernameChecking.value = false
    usernameHint.value = '检查失败，请稍后重试'
  }
}
```

---

## 7. 最佳实践

### 7.1 推荐的登录方式选择

```typescript
// 登录方式决策树
if (平台 === '微信小程序') {
  使用: pages/auth/mp-login.vue  // 微信一键登录
} else if (平台 === 'H5') {
  if (场景 === '首页快速登录' || 场景 === '详情页需登录') {
    使用: LoginModal  // 弹窗登录，不打断浏览 ✅ 推荐
  } else if (场景 === '独立登录入口' || 场景 === '测试登录') {
    使用: pages/auth/login.vue  // 页面登录（可选）
  }
}
```

### 7.2 统一字段名规范

**建议统一使用 `userInfo`**:

```typescript
// ✅ 正确：所有登录方式统一
userStore.login({
  token: response.token,
  refreshToken: response.refreshToken,
  userInfo: response.user
})

// ❌ 错误：字段名不一致
// LoginModal 用 userInfo
// 微信登录用 user
// 页面登录用 user
```

### 7.3 Toast 提示文案规范

**品牌化友好提示**（参考 LoginModal 实现）:

```typescript
// ✅ 正确：品牌化、友好、具体
'密码好像不对呢，再想想？'
'网络好像断了，检查一下网络连接吧 ~'
'这个用户名已经被占用了，换一个试试？'

// ❌ 错误：冷冰冰、不友好
'密码错误'
'网络错误'
'用户名重复'
```

**Toast 类型选择**:
- `success`: 操作成功（登录成功、注册成功、验证码发送成功）
- `error`: 严重错误（账号被禁用、服务器错误）
- `warning`: 用户输入错误（密码错误、验证码错误、格式错误）
- `info`: 提示信息（请填写完整信息、请先登录）

### 7.4 弹窗关闭逻辑规范

**正确的关闭方式**:
```typescript
const handleClose = () => {
  showAnimation.value = false
  setTimeout(() => {
    emit('update:visible', false)
  }, 300) // 等待动画结束
}
```

**原因**: 先播放退出动画，再关闭弹窗，避免突兀消失。

### 7.5 表单重置规范

**弹窗关闭时重置表单**:
```typescript
watch(() => props.visible, (newVal) => {
  if (newVal) {
    // 打开弹窗时的逻辑
  } else {
    // 关闭弹窗时重置表单
    resetForm()
  }
})

const resetForm = () => {
  formData.value = {
    account: '',
    password: '',
    rememberMe: false
  }
  emailError.value = false
  passwordError.value = false
}
```

**原因**: 下次打开弹窗时不会显示上次的输入内容。

---

## 8. 总结

### 8.1 现有系统优点

1. ✅ **企业级 UI 设计**: 渐变图标、涟漪动画、玻璃拟态、弹性缓动
2. ✅ **完善的 Toast 系统**: 4 种状态、渐变配色、背景模糊、平滑动画
3. ✅ **友好的错误提示**: 品牌化语气、具体明确、用户友好
4. ✅ **多平台适配**: H5 + 小程序，条件编译处理差异
5. ✅ **丰富的交互反馈**: 聚焦效果、密码显示、记住账号、ESC 关闭
6. ✅ **智能表单生成**: 注册时自动生成用户名和昵称
7. ✅ **密码强度检测**: 实时计算密码强度，可视化进度条

### 8.2 需要改进的地方

1. ⚠️ **字段名不一致**: 统一使用 `userInfo` 或 `user`
2. ⚠️ **RegisterModal 未使用 Store**: 注册成功后未更新响应式状态
3. ⚠️ **页面登录冗余**: 建议保留但明确使用场景
4. ⚠️ **测试账号功能缺失**: LoginModal 缺少快速填充功能
5. ⚠️ **用户名检查 API 缺失**: 后端需要提供检查接口

### 8.3 下一步行动计划

1. **统一字段名**（高优先级）:
   - 修改所有登录方式使用统一字段名（`userInfo`）
   - 更新文档说明

2. **修复 RegisterModal Store 问题**（高优先级）:
   - 修改 `RegisterModal.vue:644-646` 使用 `userStore.login()`

3. **添加测试账号快速填充**（中优先级）:
   - 在 `LoginModal.vue` 添加测试账号功能（仅开发环境）

4. **后端添加用户名检查 API**（中优先级）:
   - 添加 `/auth/check-username` 接口
   - 前端调用接口实时检查

5. **明确页面登录使用场景**（低优先级）:
   - 在文档中明确说明何时使用弹窗、何时使用页面
   - 或考虑移除页面登录

---

**文档版本**: v1.0
**创建时间**: 2025-12-18
**作者**: Claude Code
**状态**: ✅ 完成
