<template>
  <view v-if="visible" class="login-modal-mask" @tap="handleMaskClick">
    <view class="login-modal-container" :class="{ 'modal-show': showAnimation }" @tap.stop>
      <!-- 关闭按钮 -->
      <view class="close-btn" @tap.stop="handleClose">
        <text class="close-icon">✕</text>
      </view>

      <!-- 品牌头部 -->
      <view class="modal-header">
        <!-- 优化：Logo 横向布局 -->
        <view class="brand-logo-wrapper">
          <image class="brand-logo-image" src="/static/logo.png" mode="aspectFit"></image>
        </view>
        <view class="slogan-container">
          <text class="welcome-slogan">连接校园每一刻</text>
          <text class="welcome-subtitle">让学习与分享更简单</text>
        </view>
        <!-- 品牌区分割线 -->
        <view class="brand-divider"></view>
      </view>

      <!-- 登录表单 -->
      <view class="login-form">
        <!-- 邮箱输入 -->
        <view class="input-group" :class="{ 'input-focus': emailFocused, 'input-error': emailError }">
          <view class="input-icon-wrapper">
            <Icon name="user" :size="16" class="field-icon gradient-icon" />
          </view>
          <input
            class="form-input"
            type="text"
            placeholder="邮箱或手机号"
            v-model="formData.account"
            @focus="emailFocused = true"
            @blur="emailFocused = false"
          />
        </view>

        <!-- 密码输入 -->
        <view class="input-group" :class="{ 'input-focus': passwordFocused, 'input-error': passwordError }">
          <view class="input-icon-wrapper">
            <Icon name="lock" :size="16" class="field-icon gradient-icon lock-icon" />
          </view>
          <input
            class="form-input"
            :type="showPassword ? 'text' : 'password'"
            placeholder="密码"
            v-model="formData.password"
            @focus="passwordFocused = true"
            @blur="passwordFocused = false"
          />
          <view class="password-toggle" @tap="showPassword = !showPassword">
            <Icon :name="showPassword ? 'eye' : 'eye-off'" :size="16" />
          </view>
        </view>

        <!-- 记住账号和忘记密码 -->
        <view class="form-options">
          <view class="remember-me" @tap="formData.rememberMe = !formData.rememberMe">
            <view class="checkbox" :class="{ 'checkbox-checked': formData.rememberMe }">
              <text v-if="formData.rememberMe" class="checkbox-icon">✓</text>
            </view>
            <text class="option-text">记住账号</text>
          </view>
          <text class="forgot-password" @tap="handleForgotPassword">忘记密码?</text>
        </view>

        <!-- 登录按钮 -->
        <button class="login-btn" :class="{ 'btn-loading': loading }" @tap="handleLogin">
          <text v-if="!loading">登录</text>
          <text v-else class="loading-text">登录中...</text>
        </button>

        <!-- 分隔线 -->
        <view class="divider">
          <view class="divider-line"></view>
          <text class="divider-text">或使用其他方式登录</text>
          <view class="divider-line"></view>
        </view>

        <!-- 第三方登录 -->
        <view class="social-login">
          <view class="social-btn wechat-btn" @tap="handleWechatLogin">
            <text class="social-icon">💬</text>
            <text class="social-text">微信登录</text>
          </view>
          <view class="social-btn qq-btn" @tap="handleQQLogin">
            <text class="social-icon">🐧</text>
            <text class="social-text">QQ登录</text>
          </view>
        </view>

        <!-- 注册引导 -->
        <view class="register-tip">
          <text class="tip-text">还没有账号?</text>
          <text class="register-link" @tap="handleRegister">立即注册</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { login, type LoginRequest, type AuthResponse } from '@/services/auth'
import { useUserStore } from '@/stores/user'
import config from '@/config'
import Icon from '@/components/icons/index.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'login-success', 'register', 'forgot-password'])

// 获取用户 store
const userStore = useUserStore()

// 表单数据
const formData = ref({
  account: '',
  password: '',
  rememberMe: false
})

// 状态管理
const showAnimation = ref(false)
const emailFocused = ref(false)
const passwordFocused = ref(false)
const emailError = ref(false)
const passwordError = ref(false)
const showPassword = ref(false)
const loading = ref(false)

// 关闭弹窗
const handleClose = (_e?: Event) => {
  showAnimation.value = false
  setTimeout(() => {
    emit('update:visible', false)
  }, 300) // 等待动画结束
}

// 点击遮罩关闭
const handleMaskClick = (e: Event) => {
  handleClose(e)
}

// ESC 键监听处理函数
const handleKeyDown = (e: KeyboardEvent) => {
  if (e.key === 'Escape' && props.visible) {
    handleClose()
  }
}

// 监听弹窗显示状态,添加入场动画和键盘事件
watch(() => props.visible, (newVal) => {
  if (newVal) {
    setTimeout(() => {
      showAnimation.value = true
    }, 10)
    // 弹窗打开时添加键盘监听（仅 H5 平台）
    // #ifdef H5
    if (typeof window !== 'undefined') {
      window.addEventListener('keydown', handleKeyDown)
    }
    // #endif
  } else {
    showAnimation.value = false
    // 弹窗关闭时移除键盘监听（仅 H5 平台）
    // #ifdef H5
    if (typeof window !== 'undefined') {
      window.removeEventListener('keydown', handleKeyDown)
    }
    // #endif
  }
})

// 组件挂载时加载记住的账号
onMounted(() => {
  const rememberedAccount = uni.getStorageSync('campuslink_remember_account')
  if (rememberedAccount) {
    formData.value.account = rememberedAccount
    formData.value.rememberMe = true
  }
})

// 组件卸载时清理键盘监听
onUnmounted(() => {
  // #ifdef H5
  if (typeof window !== 'undefined') {
    window.removeEventListener('keydown', handleKeyDown)
  }
  // #endif
})

// 显示Toast的通用方法（企业级多状态配色体系）
const showToast = (message: string, type: 'success' | 'error' | 'warning' | 'info' = 'info') => {
  // #ifdef H5
  const toastDiv = document.createElement('div')

  // 企业级配色标准：背景色、图标色、文本色、边框色
  const typeConfig = {
    success: {
      icon: '✓',
      bg: 'rgba(34, 197, 94, 0.15)',
      iconColor: '#22C55E',
      textColor: '#166534',
      border: '#22C55E',
      iconBg: 'linear-gradient(135deg, #22C55E 0%, #16A34A 100%)'
    },
    error: {
      icon: '✕',
      bg: 'rgba(239, 68, 68, 0.15)',
      iconColor: '#EF4444',
      textColor: '#7F1D1D',
      border: '#EF4444',
      iconBg: 'linear-gradient(135deg, #EF4444 0%, #DC2626 100%)'
    },
    warning: {
      icon: '⚠',
      bg: 'rgba(250, 204, 21, 0.15)',
      iconColor: '#FACC15',
      textColor: '#92400E',
      border: '#FACC15',
      iconBg: 'linear-gradient(135deg, #FACC15 0%, #EAB308 100%)'
    },
    info: {
      icon: 'ℹ',
      bg: 'rgba(59, 130, 246, 0.15)',
      iconColor: '#3B82F6',
      textColor: '#1E3A8A',
      border: '#3B82F6',
      iconBg: 'linear-gradient(135deg, #3B82F6 0%, #2563EB 100%)'
    }
  }

  const config = typeConfig[type]

  // 创建Toast容器（优化图标与文字层次）
  toastDiv.innerHTML = `
    <div style="display: flex; align-items: center; gap: 12px;">
      <span style="
        display: inline-flex;
        align-items: center;
        justify-content: center;
        width: 24px;
        height: 24px;
        border-radius: 50%;
        background: ${config.iconBg};
        color: white;
        font-size: 13px;
        font-weight: 700;
        flex-shrink: 0;
        box-shadow: 0 2px 8px ${config.iconColor}40;
      ">${config.icon}</span>
      <span style="flex: 1; line-height: 1.6; color: ${config.textColor};">${message}</span>
    </div>
  `

  // 优化阴影与模糊，增强层次感
  toastDiv.style.cssText = `
    position: fixed;
    top: 20%;
    left: 50%;
    transform: translate(-50%, -8px);
    background: ${config.bg};
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    padding: 16px 24px;
    border-radius: 12px;
    border-left: 3px solid ${config.border};
    font-size: 14px;
    z-index: 10000;
    max-width: 420px;
    min-width: 300px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12), 0 2px 8px rgba(0, 0, 0, 0.08);
    opacity: 0;
    font-weight: 500;
    letter-spacing: 0.3px;
    transition: opacity 0.25s ease-out, transform 0.25s ease-out;
  `

  document.body.appendChild(toastDiv)

  // 滑入动画（平滑淡入）
  requestAnimationFrame(() => {
    requestAnimationFrame(() => {
      toastDiv.style.opacity = '1'
      toastDiv.style.transform = 'translate(-50%, 0)'
    })
  })

  // 3秒后滑出并移除
  setTimeout(() => {
    toastDiv.style.opacity = '0'
    toastDiv.style.transform = 'translate(-50%, -8px)'
    setTimeout(() => {
      if (document.body.contains(toastDiv)) {
        document.body.removeChild(toastDiv)
      }
    }, 250)
  }, 3000)
  // #endif

  // #ifndef H5
  uni.showToast({
    title: message,
    icon: type === 'success' ? 'success' : 'none',
    duration: 3000
  })
  // #endif
}

// 表单验证
const validateForm = () => {
  emailError.value = !formData.value.account
  passwordError.value = !formData.value.password

  if (emailError.value || passwordError.value) {
    showToast('请填写账号和密码哦 ~', 'info')
    return false
  }

  return true
}

// 登录处理
const handleLogin = async () => {
  if (!validateForm()) return

  loading.value = true

  try {
    // 调用后端登录API
    const loginData: LoginRequest = {
      account: formData.value.account,
      password: formData.value.password
    }

    const response: AuthResponse = await login(loginData)

    // 更新 Pinia store（同时也会存储到 localStorage）
    userStore.login({
      token: response.token,
      refreshToken: response.refreshToken,
      userInfo: response.user
    })

    // 如果选择记住账号，存储账号信息
    if (formData.value.rememberMe) {
      uni.setStorageSync('campuslink_remember_account', formData.value.account)
    } else {
      uni.removeStorageSync('campuslink_remember_account')
    }

    // 移除这里的toast,由父组件统一处理欢迎提示
    emit('login-success', response)
    handleClose()
  } catch (error: any) {
    // 根据不同错误类型显示友好提示（品牌化语气）
    let errorMessage = '登录遇到问题了，稍后再试试吧 ~'
    let errorType: 'error' | 'warning' | 'info' = 'error'

    if (error.message) {
      const msg = error.message

      if (msg.includes('用户不存在') || msg.includes('账号不存在')) {
        errorMessage = '没找到这个账号哦，确认一下邮箱或手机号？'
        errorType = 'warning'
      } else if (msg.includes('密码错误') || msg.includes('密码不正确')) {
        errorMessage = '密码好像不对呢，再想想？'
        errorType = 'warning'
      } else if (msg.includes('账号已被禁用') || msg.includes('已禁用')) {
        errorMessage = '账号暂时无法使用，有疑问可以联系管理员哦'
        errorType = 'error'
      } else if (msg.includes('网络') || msg.includes('Network')) {
        errorMessage = '网络好像断了，检查一下网络连接吧 ~'
        errorType = 'warning'
      } else {
        errorMessage = msg
        errorType = 'error'
      }
    }

    showToast(errorMessage, errorType)
  } finally {
    loading.value = false
  }
}


// QQ登录
const handleQQLogin = () => {
  uni.showToast({
    title: 'QQ登录功能开发中',
    icon: 'none'
  })
}

// 忘记密码
const handleForgotPassword = () => {
  emit('forgot-password')
  handleClose()
}

// 注册
const handleRegister = () => {
  emit('register')
  handleClose()
}
</script>

<style lang="scss" scoped>
/* ========== 遮罩层 ========== */
.login-modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
  /* 优化：强化径向渐变 - 中心淡蓝亮区 → 边缘深灰，营造"聚光舞台感" */
  background: radial-gradient(
    ellipse at center,
    rgba(56, 189, 248, 0.08) 0%,      /* 中心：青色淡光 */
    rgba(37, 99, 235, 0.12) 30%,      /* 过渡：蓝色 */
    rgba(15, 23, 42, 0.75) 100%       /* 边缘：深灰 */
  );
  backdrop-filter: blur(14px);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* ========== 弹窗容器 ========== */
.login-modal-container {
  position: relative;
  width: 840rpx; /* 420px */
  max-height: 92vh;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(36rpx); /* 18px */
  border-radius: 52rpx; /* 优化：从 48rpx 增加到 52rpx (26px)，更柔和 */
  box-shadow: 0 16rpx 64rpx rgba(37, 99, 235, 0.15),
              0 0 2rpx rgba(37, 99, 235, 0.1);
  padding: 44rpx 48rpx 40rpx; /* 优化：增加上下内边距 (22px 24px 20px)，提升呼吸感 */
  box-sizing: border-box;
  overflow-y: auto;
  /* 优化：淡入缩放动效 - 从 scale(0.95) 开始 */
  transform: scale(0.95);
  opacity: 0;
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1); /* 优化：使用弹性缓动 */

  &.modal-show {
    transform: scale(1);
    opacity: 1;
  }

  /* #ifdef H5 */
  /* 优化：隐藏滚动条 */
  &::-webkit-scrollbar {
    display: none;
  }
  scrollbar-width: none;
  /* #endif */
}

/* ========== 关闭按钮 ========== */
.close-btn {
  position: absolute;
  top: 32rpx;
  right: 32rpx;
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  /* 优化：确保按钮在最上层 */
  z-index: 10;
  /* 优化：添加旋转过渡 */
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    background: rgba(239, 246, 255, 0.9); /* 优化：从 0.8 增加到 0.9 */
    transform: scale(1.1) rotate(45deg); /* 优化：从 90° 改为 45°，更优雅 */
    box-shadow: 0 4rpx 12rpx rgba(37, 99, 235, 0.15); /* 优化：添加阴影 */
  }

  &:active {
    transform: scale(0.95) rotate(45deg);
  }

  .close-icon {
    font-size: 36rpx; /* 18px */
    color: #64748B;
    font-weight: 300;
    transition: color 0.2s ease;
    /* 优化：禁用指针事件，让点击穿透到父元素 */
    pointer-events: none;
  }

  &:hover .close-icon {
    color: #2563EB; /* 优化：hover 时图标变蓝 */
  }
}

/* ========== 品牌头部 ========== */
.modal-header {
  text-align: center;
  margin-bottom: 48rpx; /* 优化：从 36rpx 增加到 48rpx (24px)，拉开呼吸空间 */
}

/* 优化：Logo 横向显示 (152x60 尺寸) */
.brand-logo-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx; /* 优化：从 20rpx 增加到 24rpx (12px)，Logo 与文案间距增加 */
  /* 优化：添加轻微投影，营造"漂浮感" */
  filter: drop-shadow(0 6rpx 24rpx rgba(37, 99, 235, 0.12));
}

.brand-logo-image {
  width: 304rpx; /* 152px - 按照 152x60 比例 */
  height: 120rpx; /* 60px */
  object-fit: contain; /* 保持比例 */
}

/* Slogan 容器：两行结构 */
.slogan-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx; /* 两行间距 3px */
  margin-bottom: 20rpx; /* 优化：从 16rpx 增加到 20rpx (10px)，标语和分割线间距增加 */
}

.welcome-slogan {
  font-size: 36rpx; /* 优化：从 26rpx 增加到 36rpx (18px)，提升品牌主线辨识度 */
  color: #1F2937; /* 优化：从 #475569 加深到 #1F2937，更稳重 */
  font-weight: 600;
  letter-spacing: 1rpx;
  line-height: 1.5;
}

.welcome-subtitle {
  font-size: 26rpx; /* 优化：从 20rpx 增加到 26rpx (13px)，更易读 */
  color: #9CA3AF; /* 优化：使用标准灰色，保持层次 */
  font-weight: 400;
  letter-spacing: 0.8rpx;
  line-height: 1.5;
}

/* 品牌区分割线 */
.brand-divider {
  width: 120rpx; /* 优化：从 100rpx 增加到 120rpx (60px) */
  height: 2rpx; /* 1px */
  background: linear-gradient(90deg, transparent 0%, rgba(100, 116, 139, 0.2) 50%, transparent 100%); /* 优化：使用灰色系分割线 */
  margin: 0 auto 8rpx; /* 优化：从 6rpx 增加到 8rpx (4px) */
}

/* ========== 登录表单 ========== */
.login-form {
  width: 100%;
}

/* 输入框组 */
.input-group {
  position: relative;
  display: flex;
  align-items: center;
  height: 84rpx; /* 优化：从 88rpx 减少到 84rpx (42px) */
  background: rgba(255, 255, 255, 0.7); /* 优化：从 0.6 增加到 0.7，提升对比度 */
  border: 2rpx solid rgba(203, 213, 225, 0.5);
  border-radius: 22rpx; /* 优化：从 24rpx 减少到 22rpx (11px) */
  padding: 0 28rpx; /* 优化：从 32rpx 减少到 28rpx (14px) */
  margin-bottom: 16rpx; /* 优化：从 20rpx 减少到 16rpx (8px) */
  /* 优化：增强浅投影，增加层次感 */
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); /* 优化：增加时长，配合图标动效 */

  &.input-focus {
    background: rgba(255, 255, 255, 0.98); /* 优化：从 0.95 增加到 0.98，更清晰 */
    /* 优化：品牌渐变描边（蓝→青） */
    border: 2rpx solid transparent;
    background-image:
      linear-gradient(rgba(255, 255, 255, 0.98), rgba(255, 255, 255, 0.98)),
      linear-gradient(90deg, #2563EB, #06B6D4);
    background-origin: border-box;
    background-clip: padding-box, border-box;
    /* 优化：增强光晕效果 + 渐变外光 */
    box-shadow: 0 0 0 6rpx rgba(59, 130, 246, 0.18),
                0 0 28rpx rgba(37, 99, 235, 0.2),
                0 4rpx 16rpx rgba(37, 99, 235, 0.15);
  }

  &.input-error {
    border-color: rgba(239, 68, 68, 0.6); /* 优化：半透明红描边，更柔和 */
    background: rgba(254, 242, 242, 0.5); /* 优化：添加淡红背景 */
    /* 优化：柔和红光 + 内阴影 */
    box-shadow: 0 0 0 4rpx rgba(239, 68, 68, 0.12),
                inset 0 2rpx 8rpx rgba(239, 68, 68, 0.08);
    animation: shake 0.4s cubic-bezier(0.36, 0.07, 0.19, 0.97);
  }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-6rpx); } /* 优化：从 -8rpx 减少到 -6rpx，抖动幅度更小 */
  75% { transform: translateX(6rpx); } /* 优化：从 8rpx 减少到 6rpx */
}

.input-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
  flex-shrink: 0;
}

.field-icon {
  color: #94A3B8;
  transition: color 0.25s ease, transform 0.25s ease;
}

.gradient-icon {
  color: #94A3B8;
}

.input-group.input-focus .gradient-icon {
  color: #2563EB;
  transform: scale(1.1);
}

.form-input {
  flex: 1;
  height: 100%;
  font-size: 28rpx; /* 优化：调整为 28rpx (14px)，提升阅读对比度 */
  color: #374151; /* 优化：从 #1E293B 调整为 #374151，在模糊背景下更清晰 */
  font-weight: 500;
  border: none;
  background: transparent;

  &::placeholder {
    color: #9CA3AF; /* 优化：使用标准灰色，保持一致性 */
    font-size: 28rpx; /* 优化：与输入内容一致，避免交互时"跳变" */
    font-weight: 400;
  }
}

.password-toggle {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #94A3B8;
  transition: color 0.2s ease, transform 0.2s ease;
  padding: 4rpx;

  &:hover {
    color: #2563EB;
    transform: scale(1.1);
  }

  &:active {
    transform: scale(0.95);
  }
}

/* 表单选项行 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx; /* 优化：从 24rpx 减少到 20rpx (10px) */
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 12rpx;
  cursor: pointer;
}

.checkbox {
  width: 32rpx;
  height: 32rpx;
  border: 2rpx solid #CBD5E1;
  border-radius: 6rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;

  &.checkbox-checked {
    background: linear-gradient(135deg, #2563EB 0%, #3B82F6 100%);
    border-color: #2563EB;
  }

  .checkbox-icon {
    color: #fff;
    font-size: 20rpx;
    font-weight: 600;
  }
}

.option-text {
  font-size: 26rpx; /* 优化：从 22rpx 增加到 26rpx (13px)，保持次要但不失读 */
  color: #64748B;
  font-weight: 500;
}

.forgot-password {
  font-size: 26rpx; /* 优化：从 22rpx 增加到 26rpx (13px) */
  color: #6B7280; /* 优化：常态使用更轻的灰蓝，避免干扰视觉焦点 */
  cursor: pointer;
  position: relative;
  font-weight: 500;
  transition: all 0.2s ease;

  &:hover {
    /* 优化：hover 时亮起品牌色 */
    background: linear-gradient(90deg, #2563EB 0%, #3B82F6 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    filter: brightness(1.1);
    text-decoration: underline;
  }
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 84rpx;
  /* 优化：清新校园感渐变（蓝 → 青） */
  background: linear-gradient(135deg, #2563EB 0%, #38BDF8 100%);
  border: none;
  border-radius: 22rpx;
  color: #fff;
  font-size: 32rpx; /* 优化：从 30rpx 增加到 32rpx (16px)，按钮应是视觉焦点 */
  font-weight: 600; /* 优化：保持 600 字重，让主操作更有力量 */
  /* 优化：文字添加轻微阴影，增强可点击感 */
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.1);
  /* 优化：增强阴影，营造"亮区"感 */
  box-shadow: 0 8rpx 28rpx rgba(37, 99, 235, 0.35),
              0 4rpx 12rpx rgba(37, 99, 235, 0.2);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  margin-bottom: 40rpx; /* 优化：从 28rpx 增加到 40rpx (20px)，与社交登录拉开距离 */

  /* 优化：中心光晕效果（呼吸感）*/
  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 200rpx;
    height: 200rpx;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.3) 0%, transparent 70%);
    transform: translate(-50%, -50%);
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  /* 优化：涟漪扩散效果 */
  &::after {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 0;
    height: 0;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.3);
    transform: translate(-50%, -50%);
    transition: width 0.6s ease, height 0.6s ease;
  }

  &:hover {
    /* 优化：hover 时渐变更亮 + 中心光晕显现 */
    background: linear-gradient(135deg, #1D4ED8 0%, #38BDF8 100%);
    filter: brightness(1.1);
    box-shadow: 0 12rpx 40rpx rgba(37, 99, 235, 0.5),
                0 4rpx 16rpx rgba(37, 99, 235, 0.3),
                0 0 0 8rpx rgba(56, 189, 248, 0.2); /* 优化：青色发光边 */
    transform: translateY(-4rpx) scale(1.02);

    &::before {
      opacity: 1; /* 显示中心光晕 */
    }
  }

  &:active {
    /* 优化：点击时变深 */
    background: linear-gradient(135deg, #1E40AF 0%, #2563EB 100%);
    filter: brightness(0.95);
    transform: translateY(-2rpx) scale(0.98);

    /* 优化：点击时触发涟漪扩散 */
    &::after {
      width: 600rpx;
      height: 600rpx;
      opacity: 0;
    }
  }

  &.btn-loading {
    opacity: 0.7;
    cursor: not-allowed;
  }

  .loading-text {
    animation: pulse 1.5s cubic-bezier(0.4, 0, 0.6, 1) infinite;
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

/* 分隔线 */
.divider {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin: 32rpx 0;
}

.divider-line {
  flex: 1;
  height: 1rpx;
  background: linear-gradient(to right, transparent, #CBD5E1, transparent);
}

.divider-text {
  font-size: 24rpx;
  color: #94A3B8;
  white-space: nowrap;
}

/* 第三方登录 */
.social-login {
  display: flex;
  gap: 24rpx; /* 优化：从 20rpx 增加到 24rpx (12px)，让布局更呼吸 */
  margin-bottom: 28rpx; /* 优化：从 32rpx 减少到 28rpx (14px) */
}

.social-btn {
  flex: 1;
  height: 76rpx; /* 优化：从 80rpx 减少到 76rpx (38px) */
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx; /* 优化：从 10rpx 减少到 8rpx (4px) */
  border-radius: 18rpx; /* 优化：从 20rpx 减少到 18rpx (9px) */
  cursor: pointer;
  /* 优化：白底 + 内外投影，增强层次 */
  background: #FFFFFF;
  box-shadow: inset 0 0 0 2rpx rgba(0, 0, 0, 0.04),
              0 4rpx 12rpx rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &.wechat-btn {
    &:hover {
      /* 优化：hover 时淡彩渐变 + 轻微上浮 */
      background: linear-gradient(135deg, rgba(7, 193, 96, 0.08), rgba(7, 193, 96, 0.12));
      transform: translateY(-3rpx) scale(1.02); /* 优化：添加轻微放大 */
      box-shadow: 0 8rpx 24rpx rgba(7, 193, 96, 0.25);

      /* 优化：hover 时文字和图标亮度上升 */
      .social-text {
        color: #05A850;
      }

      .social-icon {
        /* 优化：添加呼吸动画 */
        animation: breathe 1.5s ease-in-out infinite;
      }
    }

    .social-text {
      color: #07C160;
      transition: color 0.2s ease;
    }

    .social-icon {
      color: #07C160;
    }
  }

  &.qq-btn {
    &:hover {
      /* 优化：hover 时淡彩渐变 + 轻微上浮 */
      background: linear-gradient(135deg, rgba(18, 183, 245, 0.08), rgba(18, 183, 245, 0.12));
      transform: translateY(-3rpx) scale(1.02); /* 优化：添加轻微放大 */
      box-shadow: 0 8rpx 24rpx rgba(18, 183, 245, 0.25);

      /* 优化：hover 时文字和图标亮度上升 */
      .social-text {
        color: #0E9DD8;
      }

      .social-icon {
        /* 优化：添加呼吸动画 */
        animation: breathe 1.5s ease-in-out infinite;
      }
    }

    .social-text {
      color: #12B7F5;
      transition: color 0.2s ease;
    }

    .social-icon {
      color: #12B7F5;
    }
  }
}

/* 优化：图标呼吸动画 */
@keyframes breathe {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.85;
  }
}

.social-icon {
  font-size: 28rpx; /* 14px */
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.social-text {
  font-size: 28rpx; /* 优化：从 22rpx 增加到 28rpx (14px)，与输入框一致 */
  font-weight: 500;
  color: #374151; /* 优化：统一使用深灰，品牌感更强 */
}

/* 注册引导 */
.register-tip {
  text-align: center;
  font-size: 26rpx; /* 优化：设置为 26rpx (13px)，保持次级层级 */
  line-height: 1.7;
  letter-spacing: 0.5rpx;

  .tip-text {
    color: #6B7280; /* 优化：使用更轻的灰色，避免干扰视觉焦点 */
    font-weight: 500;
    margin-right: 8rpx;
  }

  .register-link {
    color: #6B7280; /* 优化：常态使用灰色 */
    font-weight: 500;
    cursor: pointer;
    position: relative;
    transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);

    /* 优化：添加下划线效果（默认隐藏）*/
    &::after {
      content: '';
      position: absolute;
      left: 0;
      right: 0;
      bottom: -4rpx;
      height: 2rpx;
      background: linear-gradient(90deg, #2563EB 0%, #3B82F6 100%);
      transform: scaleX(0);
      transform-origin: center;
      transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }

    &:hover {
      /* 优化：hover 时亮起品牌渐变色 + 下划线展开 */
      background: linear-gradient(90deg, #2563EB 0%, #3B82F6 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      filter: drop-shadow(0 0 8rpx rgba(37, 99, 235, 0.3));
      transform: scale(1.02);

      &::after {
        background: linear-gradient(90deg, #2563EB 0%, #3B82F6 100%);
        transform: scaleX(1);
      }
    }
  }
}
</style>
