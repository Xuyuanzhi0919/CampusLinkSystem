<template>
  <view class="login-page">
    <view class="login-container">
      <!-- Logo 和标题 -->
      <view class="header">
        <view class="logo-container">
          <Icon name="graduation-cap" :size="80" color="#FFFFFF" />
        </view>
        <text class="title">欢迎来到 CampusLink</text>
        <text class="subtitle">高校资源互助与问答平台</text>
      </view>

      <!-- 登录表单 -->
      <view class="form-container">
        <!-- 账号输入框 -->
        <view class="form-item">
          <text class="label">账号</text>
          <view class="input-wrap" :class="{ focused: accountFocused }">
            <view class="input-icon">
              <Icon name="user" :size="36" />
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
          <text class="label">密码</text>
          <view class="input-wrap" :class="{ focused: passwordFocused }">
            <view class="input-icon">
              <Icon name="lock" :size="36" />
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
              <Icon :name="showPassword ? 'eye' : 'eye-off'" :size="36" />
            </view>
          </view>
        </view>

        <!-- 登录按钮 -->
        <view class="form-actions">
          <view
            class="login-btn"
            :class="{ disabled: !canSubmit || loginLoading, loading: loginLoading }"
            @click="handleLogin"
          >
            <view v-if="loginLoading" class="loading-spinner" />
            <text class="btn-text">{{ loginLoading ? '登录中...' : '登录' }}</text>
          </view>

          <view class="links">
            <text class="link" @click="handleGoToRegister">注册账号</text>
            <text class="separator">|</text>
            <text class="link" @click="handleForgotPassword">忘记密码</text>
          </view>
        </view>
      </view>

      <!-- 测试账号提示（低调化） -->
      <view class="test-accounts">
        <view class="test-header">
          <Icon name="info" :size="28" class="test-icon" />
          <text class="test-title">测试账号</text>
        </view>
        <view class="test-item" @click="fillTestAccount('admin')">
          <text class="test-username">admin</text>
          <text class="test-password">admin123</text>
        </view>
        <view class="test-item" @click="fillTestAccount('testuser001')">
          <text class="test-username">testuser001</text>
          <text class="test-password">password123</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { login } from '@/services/auth'
import Icon from '@/components/icons/index.vue'

const userStore = useUserStore()

const form = ref({
  account: '',
  password: ''
})

const loginLoading = ref(false)
const showPassword = ref(false)
const accountFocused = ref(false)
const passwordFocused = ref(false)

const canSubmit = computed(() => {
  return form.value.account.trim() && form.value.password.trim()
})

const handleLogin = async () => {
  if (!canSubmit.value || loginLoading.value) return

  try {
    loginLoading.value = true

    const response = await login({
      account: form.value.account,
      password: form.value.password
    })

    userStore.login(response)

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
      if (error.message.includes('用户名或密码错误')) {
        errorMessage = '账号或密码错误'
      } else if (error.message.includes('网络')) {
        errorMessage = '网络连接失败，请稍后重试'
      } else if (error.message.includes('账号')) {
        errorMessage = '账号不存在或已被禁用'
      }
    }
    uni.showToast({ title: errorMessage, icon: 'none', duration: 2500 })
  } finally {
    loginLoading.value = false
  }
}

const fillTestAccount = (username: string) => {
  if (username === 'admin') {
    form.value.account = 'admin'
    form.value.password = 'admin123'
  } else if (username === 'testuser001') {
    form.value.account = 'testuser001'
    form.value.password = 'password123'
  }
  uni.showToast({ title: '已填充', icon: 'success', duration: 800 })
}

const handleGoToRegister = () => {
  uni.showToast({ title: '注册功能开发中...', icon: 'none' })
}

const handleForgotPassword = () => {
  uni.showToast({ title: '找回密码功能开发中...', icon: 'none' })
}
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.login-page {
  min-height: 100vh;
  background: linear-gradient(160deg, $primary-50 0%, $bg-surface 55%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $sp-10;
}

.login-container {
  width: 100%;
  max-width: 600rpx;
  background: $bg-surface;
  border-radius: $radius-xl;
  padding: $sp-20 $sp-12;
  box-shadow: 0 8rpx 40rpx rgba($primary, 0.08), 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

// ========================================
// Header
// ========================================

.header {
  text-align: center;
  margin-bottom: $sp-16;

  .logo-container {
    width: 160rpx;
    height: 160rpx;
    margin: 0 auto $sp-8;
    background: linear-gradient(135deg, $primary 0%, $primary-light 100%);
    border-radius: $radius-2xl;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 16rpx 48rpx rgba($primary, 0.25);
  }

  .title {
    display: block;
    font-size: $font-size-2xl;
    font-weight: $font-weight-bold;
    color: $gray-900;
    margin-bottom: $sp-3;
    letter-spacing: -0.5rpx;
  }

  .subtitle {
    display: block;
    font-size: $font-size-sm;
    color: $gray-500;
  }
}

// ========================================
// Form
// ========================================

.form-container {
  margin-bottom: $sp-12;
}

.form-item {
  margin-bottom: $sp-8;

  .label {
    display: block;
    font-size: $font-size-base;
    font-weight: $font-weight-semibold;
    color: $gray-700;
    margin-bottom: $sp-3;
  }
}

.input-wrap {
  display: flex;
  align-items: center;
  height: 88rpx;
  padding: 0 $sp-7;
  background: $gray-100;
  border: 2rpx solid transparent;
  border-radius: $radius-md;
  transition: all 0.2s;
  gap: $sp-4;

  &.focused {
    background: $bg-surface;
    border-color: $primary;
    box-shadow: 0 0 0 6rpx rgba($primary, 0.1);
  }

  .input-icon {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    color: $gray-400;
  }

  .input {
    flex: 1;
    height: 100%;
    font-size: $font-size-base;
    color: $gray-900;
    background: transparent;
    border: none;
    outline: none;
  }

  .eye-toggle {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    padding: $sp-2;
    color: $gray-400;
    cursor: pointer;
    transition: color 0.15s;

    &:active {
      color: $gray-600;
    }
  }
}

.form-actions {
  margin-top: $sp-12;

  .login-btn {
    width: 100%;
    height: 96rpx;
    background: linear-gradient(135deg, $primary 0%, $primary-light 100%);
    border-radius: $radius-full;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: $sp-3;
    box-shadow: 0 8rpx 24rpx rgba($primary, 0.3);
    margin-bottom: $sp-8;
    transition: all 0.2s;
    cursor: pointer;

    &:active:not(.disabled) {
      transform: scale(0.98);
      box-shadow: 0 4rpx 12rpx rgba($primary, 0.25);
    }

    &.disabled {
      opacity: 0.55;
      cursor: not-allowed;
    }

    .btn-text {
      font-size: $font-size-lg;
      font-weight: $font-weight-semibold;
      color: $white;
    }
  }

  .links {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: $sp-4;

    .link {
      font-size: $font-size-sm;
      color: $primary;
      cursor: pointer;

      &:active {
        opacity: 0.7;
      }
    }

    .separator {
      font-size: $font-size-sm;
      color: $gray-300;
    }
  }
}

// Loading spinner
.loading-spinner {
  width: 36rpx;
  height: 36rpx;
  border: 3rpx solid rgba(255, 255, 255, 0.4);
  border-top-color: $white;
  border-radius: $radius-full;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

// ========================================
// Test Accounts（低调化）
// ========================================

.test-accounts {
  background: $gray-50;
  border: 1.5rpx solid $gray-200;
  border-radius: $radius-md;
  padding: $sp-6;

  .test-header {
    display: flex;
    align-items: center;
    gap: $sp-2;
    margin-bottom: $sp-4;
    color: $gray-500;

    .test-icon {
      color: $gray-400;
    }

    .test-title {
      font-size: $font-size-xs;
      color: $gray-500;
      font-weight: $font-weight-medium;
    }
  }

  .test-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: $sp-3 $sp-4;
    border-radius: $radius-sm;
    margin-bottom: $sp-2;
    cursor: pointer;
    transition: background 0.15s;

    &:last-child {
      margin-bottom: 0;
    }

    &:active {
      background: $gray-100;
    }

    .test-username {
      font-size: $font-size-sm;
      font-weight: $font-weight-medium;
      color: $gray-700;
      font-family: monospace;
    }

    .test-password {
      font-size: $font-size-xs;
      color: $gray-400;
      font-family: monospace;
    }
  }
}
</style>
