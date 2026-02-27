<template>
  <view class="login-page">
    <!-- 蓝色背景 -->
    <view class="bg-blue">
      <view class="orb orb--1" />
      <view class="orb orb--2" />
    </view>

    <!-- 页面内容 -->
    <view class="page-content">
      <!-- 品牌头部 -->
      <view class="brand-header">
        <view class="logo-box">
          <Icon name="graduation-cap" :size="48" color="#FFFFFF" />
        </view>
        <text class="brand-title">CampusLink</text>
        <text class="brand-subtitle">高校资源互助 · 问答社区</text>
      </view>

      <!-- 表单卡片 -->
      <view class="form-card">
        <text class="card-title">账号登录</text>
        <text class="card-subtitle">用你的账号，继续连接校园</text>

        <!-- 账号输入框 -->
        <view class="form-item">
          <view class="input-wrap" :class="{ focused: accountFocused }">
            <view class="input-icon">
              <Icon name="user" :size="34" color="#9CA3AF" />
            </view>
            <input
              v-model="form.account"
              class="input"
              type="text"
              placeholder="用户名 / 邮箱 / 手机号"
              :disabled="loginLoading"
              @focus="accountFocused = true"
              @blur="accountFocused = false"
            />
          </view>
        </view>

        <!-- 密码输入框 -->
        <view class="form-item">
          <view class="input-wrap" :class="{ focused: passwordFocused }">
            <view class="input-icon">
              <Icon name="lock" :size="34" color="#9CA3AF" />
            </view>
            <input
              v-model="form.password"
              class="input"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码"
              :disabled="loginLoading"
              @focus="passwordFocused = true"
              @blur="passwordFocused = false"
              @confirm="handleLogin"
            />
            <view class="eye-toggle" @click="showPassword = !showPassword">
              <Icon :name="showPassword ? 'eye' : 'eye-off'" :size="34" color="#9CA3AF" />
            </view>
          </view>
        </view>

        <!-- 记住账号 & 忘记密码 -->
        <view class="form-options">
          <view class="remember-me" @click="form.rememberMe = !form.rememberMe">
            <view class="checkbox" :class="{ 'checkbox-checked': form.rememberMe }">
              <Icon v-if="form.rememberMe" name="check" :size="20" class="checkbox-icon" />
            </view>
            <text class="option-text">记住账号</text>
          </view>
          <text class="forgot-link" @click="handleForgotPassword">忘记密码?</text>
        </view>

        <!-- 登录按钮 -->
        <view
          class="login-btn"
          :class="{ disabled: !canSubmit || loginLoading }"
          @click="handleLogin"
        >
          <view v-if="loginLoading" class="loading-spinner" />
          <text class="btn-text">{{ loginLoading ? '登录中...' : '登录' }}</text>
        </view>

        <!-- 分隔线 -->
        <view class="divider">
          <view class="divider-line" />
          <text class="divider-text">或</text>
          <view class="divider-line" />
        </view>

        <!-- 注册行 -->
        <view class="register-row" @click="handleGoToRegister">
          <text class="register-hint">还没有账号?</text>
          <text class="register-link">立即注册 →</text>
        </view>
      </view>

      <view class="page-bottom" />
    </view>

  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { login } from '@/services/auth'
import Icon from '@/components/icons/index.vue'

const userStore = useUserStore()

const form = ref({
  account: '',
  password: '',
  rememberMe: false
})

const loginLoading = ref(false)
const showPassword = ref(false)
const accountFocused = ref(false)
const passwordFocused = ref(false)

const canSubmit = computed(() => {
  return form.value.account.trim() && form.value.password.trim()
})

onMounted(() => {
  const remembered = uni.getStorageSync('campuslink_remember_account')
  if (remembered) {
    form.value.account = remembered
    form.value.rememberMe = true
  }
})

const handleLogin = async () => {
  if (!canSubmit.value || loginLoading.value) return

  if (!form.value.account.trim()) {
    uni.showToast({ title: '请输入账号', icon: 'none' })
    return
  }
  if (!form.value.password.trim()) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return
  }

  try {
    loginLoading.value = true

    const response = await login({
      account: form.value.account,
      password: form.value.password
    })

    userStore.login(response)

    if (form.value.rememberMe) {
      uni.setStorageSync('campuslink_remember_account', form.value.account)
    } else {
      uni.removeStorageSync('campuslink_remember_account')
    }

    uni.showToast({
      title: '登录成功',
      icon: 'success',
      duration: 1500
    })

    setTimeout(() => {
      // #ifdef H5
      uni.switchTab({ url: '/pages/home/index' })
      // #endif
    }, 1500)
  } catch (error: any) {
    let errorMessage = '登录失败，请检查账号和密码'
    if (error.message) {
      const msg = error.message
      if (msg.includes('用户不存在') || msg.includes('账号不存在') || msg.includes('用户名或密码错误')) {
        errorMessage = '账号或密码错误，请重新输入'
      } else if (msg.includes('密码错误') || msg.includes('密码不正确')) {
        errorMessage = '密码不正确，请重试'
      } else if (msg.includes('账号已被禁用') || msg.includes('已禁用')) {
        errorMessage = '账号已被禁用，请联系管理员'
      } else if (msg.includes('网络') || msg.includes('Network')) {
        errorMessage = '网络连接失败，请检查网络后重试'
      } else {
        errorMessage = msg
      }
    }
    uni.showToast({ title: errorMessage, icon: 'none', duration: 2500 })
  } finally {
    loginLoading.value = false
  }
}

const handleGoToRegister = () => {
  uni.navigateTo({ url: '/pages/auth/register' })
}

const handleForgotPassword = () => {
  uni.showModal({
    title: '找回密码',
    content: '找回密码功能正在开发中，如需帮助请发送邮件至 support@campuslink.com',
    showCancel: false,
    confirmText: '知道了'
  })
}
</script>

<style scoped lang="scss">

// =============================================
// 页面基础
// =============================================

.login-page {
  position: relative;
  min-height: 100vh;
  background: #EEF2FF;
  overflow-x: hidden;
}

// =============================================
// 蓝色背景
// =============================================

.bg-blue {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 660rpx;
  background: #2355C8;
  border-radius: 0 0 72rpx 72rpx;
  overflow: hidden;
  z-index: 0;
}

.orb {
  position: absolute;
  border-radius: 50%;

  &--1 {
    width: 280rpx;
    height: 280rpx;
    top: -60rpx;
    right: -50rpx;
    background: rgba(147, 197, 253, 0.20);
  }

  &--2 {
    width: 220rpx;
    height: 220rpx;
    bottom: 60rpx;
    left: -60rpx;
    background: rgba(56, 189, 248, 0.15);
  }
}

// =============================================
// 页面内容
// =============================================

.page-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

// =============================================
// 品牌头部
// =============================================

.brand-header {
  width: 100%;
  padding: 100rpx 48rpx 56rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.logo-box {
  width: 120rpx;
  height: 120rpx;
  background: rgba(255, 255, 255, 0.18);
  border: 3rpx solid rgba(255, 255, 255, 0.35);
  border-radius: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24rpx;
}

.brand-title {
  display: block;
  font-size: 44rpx;
  font-weight: 700;
  color: #FFFFFF;
  letter-spacing: 1rpx;
  margin-bottom: 10rpx;
}

.brand-subtitle {
  display: block;
  font-size: 24rpx;
  color: #BFDBFE;
  letter-spacing: 0.5rpx;
}

// =============================================
// 表单卡片
// =============================================

.form-card {
  width: 690rpx;
  background: #FFFFFF;
  border-radius: 48rpx;
  padding: 44rpx 44rpx 44rpx;
  box-shadow:
    0 24rpx 80rpx rgba(29, 78, 216, 0.12),
    0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.card-title {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: #111827;
  margin-bottom: 8rpx;
}

.card-subtitle {
  display: block;
  font-size: 24rpx;
  color: #9CA3AF;
  margin-bottom: 36rpx;
}

// =============================================
// 输入框
// =============================================

.form-item {
  margin-bottom: 20rpx;
}

.input-wrap {
  display: flex;
  align-items: center;
  height: 92rpx;
  background: #F8FAFF;
  border: 2rpx solid #E8EEFF;
  border-radius: 20rpx;
  padding: 0 24rpx;
  gap: 14rpx;
  transition: border-color 0.2s, box-shadow 0.2s, background 0.2s;

  &.focused {
    background: #FFFFFF;
    border-color: #2563EB;
    box-shadow: 0 0 0 6rpx rgba(37, 99, 235, 0.08);
  }

  .input-icon {
    flex-shrink: 0;
    display: flex;
    align-items: center;
  }

  .input {
    flex: 1;
    height: 100%;
    font-size: 28rpx;
    color: #111827;
    background: transparent;
  }

  .eye-toggle {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    padding: 8rpx;
    cursor: pointer;

    &:active {
      opacity: 0.6;
    }
  }
}

// =============================================
// 记住账号 & 忘记密码
// =============================================

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32rpx;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 14rpx;
  cursor: pointer;

  .checkbox {
    width: 36rpx;
    height: 36rpx;
    background: #EEF2FF;
    border: 2rpx solid #C7D2FE;
    border-radius: 8rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background 0.15s, border-color 0.15s;

    &.checkbox-checked {
      background: #2563EB;
      border-color: #2563EB;
    }

    .checkbox-icon {
      color: #FFFFFF;
    }
  }

  .option-text {
    font-size: 26rpx;
    color: #6B7280;
  }
}

.forgot-link {
  font-size: 26rpx;
  color: #4F46E5;
  cursor: pointer;

  &:active {
    opacity: 0.7;
  }
}

// =============================================
// 登录按钮
// =============================================

.login-btn {
  width: 100%;
  height: 96rpx;
  background: #2563EB;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  box-shadow: 0 10rpx 32rpx rgba(37, 99, 235, 0.35);
  margin-bottom: 32rpx;
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;

  &:active:not(.disabled) {
    transform: scale(0.98);
    box-shadow: 0 4rpx 16rpx rgba(37, 99, 235, 0.25);
  }

  &.disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  .btn-text {
    font-size: 30rpx;
    font-weight: 600;
    color: #FFFFFF;
  }
}

// =============================================
// Loading Spinner
// =============================================

.loading-spinner {
  width: 36rpx;
  height: 36rpx;
  border: 3rpx solid rgba(255, 255, 255, 0.35);
  border-top-color: #FFFFFF;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

// =============================================
// 分隔线
// =============================================

.divider {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 28rpx;

  .divider-line {
    flex: 1;
    height: 2rpx;
    background: #F3F4F6;
  }

  .divider-text {
    font-size: 24rpx;
    color: #9CA3AF;
  }
}

// =============================================
// 注册行
// =============================================

.register-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  cursor: pointer;

  &:active {
    opacity: 0.7;
  }

  .register-hint {
    font-size: 28rpx;
    color: #6B7280;
  }

  .register-link {
    font-size: 28rpx;
    font-weight: 600;
    color: #2563EB;
  }
}

// =============================================
// 页面底部间距
// =============================================

.page-bottom {
  height: 80rpx;
}
</style>
