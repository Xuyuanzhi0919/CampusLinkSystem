<template>
  <view class="login-page">
    <view class="login-container">
      <!-- Logo 和标题 -->
      <view class="header">
        <view class="logo-container">
          <text class="logo-emoji">🎓</text>
        </view>
        <text class="title">欢迎来到 CampusLink</text>
        <text class="subtitle">高校资源互助与问答平台</text>
      </view>

      <!-- 登录表单 -->
      <view class="form-container">
        <view class="form-item">
          <text class="label">用户名 / 邮箱 / 手机号</text>
          <input
            v-model="form.account"
            class="input"
            type="text"
            placeholder="请输入用户名、邮箱或手机号"
            :disabled="loginLoading"
          />
        </view>

        <view class="form-item">
          <text class="label">密码</text>
          <input
            v-model="form.password"
            class="input"
            type="password"
            placeholder="请输入密码"
            :disabled="loginLoading"
            @confirm="handleLogin"
          />
        </view>

        <view class="form-actions">
          <button
            class="login-btn"
            :loading="loginLoading"
            :disabled="!canSubmit"
            @click="handleLogin"
          >
            {{ loginLoading ? '登录中...' : '登录' }}
          </button>

          <view class="links">
            <text class="link" @click="handleGoToRegister">注册账号</text>
            <text class="separator">|</text>
            <text class="link" @click="handleForgotPassword">忘记密码</text>
          </view>
        </view>
      </view>

      <!-- 测试账号提示 -->
      <view class="test-accounts">
        <text class="test-title">测试账号：</text>
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

const userStore = useUserStore()

// 表单数据
const form = ref({
  account: '',
  password: ''
})

// 加载状态
const loginLoading = ref(false)

// 是否可以提交
const canSubmit = computed(() => {
  return form.value.account.trim() && form.value.password.trim()
})

/**
 * 处理登录
 */
const handleLogin = async () => {
  if (!canSubmit.value) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' })
    return
  }

  try {
    loginLoading.value = true

    console.log('[H5登录] 开始登录...', {
      account: form.value.account,
      password: '******'
    })

    // 调用登录 API
    const response = await login({
      account: form.value.account,
      password: form.value.password
    })

    console.log('[H5登录] 登录成功，用户信息:', response.user)

    // 保存登录信息
    userStore.login(response)

    // 提示成功
    uni.showToast({
      title: '登录成功',
      icon: 'success',
      duration: 1500
    })

    // 跳转到首页
    setTimeout(() => {
      // #ifdef H5
      uni.switchTab({ url: '/pages/home/index' })
      // #endif
    }, 1500)
  } catch (error: any) {
    console.error('[H5登录] 登录失败:', error)

    let errorMessage = '登录失败，请检查用户名和密码'
    if (error.message) {
      if (error.message.includes('用户名或密码错误')) {
        errorMessage = '用户名或密码错误'
      } else if (error.message.includes('网络')) {
        errorMessage = '网络连接失败，请检查网络'
      } else if (error.message.includes('账号')) {
        errorMessage = '账号不存在或已被禁用'
      }
    }

    uni.showToast({
      title: errorMessage,
      icon: 'none',
      duration: 2500
    })
  } finally {
    loginLoading.value = false
  }
}

/**
 * 填充测试账号
 */
const fillTestAccount = (username: string) => {
  if (username === 'admin') {
    form.value.account = 'admin'
    form.value.password = 'admin123'
  } else if (username === 'testuser001') {
    form.value.account = 'testuser001'
    form.value.password = 'password123'
  }

  uni.showToast({
    title: '已填充测试账号',
    icon: 'success',
    duration: 1000
  })
}

/**
 * 前往注册
 */
const handleGoToRegister = () => {
  uni.showToast({
    title: '注册功能开发中...',
    icon: 'none'
  })
  // TODO: 创建注册页面
  // uni.navigateTo({ url: '/pages/auth/register' })
}

/**
 * 忘记密码
 */
const handleForgotPassword = () => {
  uni.showToast({
    title: '找回密码功能开发中...',
    icon: 'none'
  })
  // TODO: 创建找回密码页面
  // uni.navigateTo({ url: '/pages/auth/forgot-password' })
}

/**
 * 微信登录（仅小程序）
 */
const handleWechatLogin = () => {
  uni.navigateTo({ url: '/pages/auth/mp-login' })
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #EFF6FF 0%, #FFFFFF 50%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
}

.login-container {
  width: 100%;
  max-width: 600rpx;
  background: #FFFFFF;
  border-radius: 32rpx;
  padding: 80rpx 60rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08);
}

// ========================================
// Header
// ========================================

.header {
  text-align: center;
  margin-bottom: 64rpx;

  .logo-container {
    width: 160rpx;
    height: 160rpx;
    margin: 0 auto 32rpx;
    background: linear-gradient(135deg, #2563EB 0%, #3B82F6 100%);
    border-radius: 40rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 16rpx 48rpx rgba(37, 99, 235, 0.2);
  }

  .logo-emoji {
    font-size: 80rpx;
  }

  .title {
    display: block;
    font-size: 44rpx;
    font-weight: 700;
    color: #0F172A;
    margin-bottom: 16rpx;
  }

  .subtitle {
    display: block;
    font-size: 26rpx;
    color: #64748B;
  }
}

// ========================================
// Form
// ========================================

.form-container {
  margin-bottom: 48rpx;
}

.form-item {
  margin-bottom: 32rpx;

  .label {
    display: block;
    font-size: 28rpx;
    font-weight: 600;
    color: #334155;
    margin-bottom: 12rpx;
  }

  .input {
    width: 100%;
    height: 88rpx;
    padding: 0 28rpx;
    background: #F8FAFC;
    border: 2rpx solid #E2E8F0;
    border-radius: 16rpx;
    font-size: 28rpx;
    color: #0F172A;
    transition: all 0.2s;

    &:focus {
      background: #FFFFFF;
      border-color: #2563EB;
      box-shadow: 0 0 0 6rpx rgba(37, 99, 235, 0.1);
    }

    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
  }
}

.form-actions {
  margin-top: 48rpx;

  .login-btn {
    width: 100%;
    height: 96rpx;
    background: linear-gradient(135deg, #2563EB 0%, #3B82F6 100%);
    border-radius: 48rpx;
    color: #FFFFFF;
    font-size: 32rpx;
    font-weight: 600;
    display: flex;
    align-items: center;
    justify-content: center;
    border: none;
    box-shadow: 0 8rpx 24rpx rgba(37, 99, 235, 0.3);
    margin-bottom: 32rpx;

    &::after {
      border: none;
    }

    &:active:not([disabled]) {
      transform: scale(0.98);
    }

    &[disabled] {
      opacity: 0.6;
    }
  }

  .links {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16rpx;

    .link {
      font-size: 26rpx;
      color: #2563EB;
      cursor: pointer;

      &:active {
        opacity: 0.7;
      }
    }

    .separator {
      font-size: 26rpx;
      color: #CBD5E1;
    }
  }
}

// ========================================
// Test Accounts
// ========================================

.test-accounts {
  background: #FEF3C7;
  border: 2rpx solid #FCD34D;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 32rpx;

  .test-title {
    display: block;
    font-size: 24rpx;
    color: #92400E;
    font-weight: 600;
    margin-bottom: 16rpx;
  }

  .test-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16rpx;
    background: #FFFBEB;
    border-radius: 12rpx;
    margin-bottom: 12rpx;
    cursor: pointer;

    &:last-child {
      margin-bottom: 0;
    }

    &:active {
      background: #FEF3C7;
    }

    .test-username {
      font-size: 26rpx;
      font-weight: 600;
      color: #78350F;
    }

    .test-password {
      font-size: 24rpx;
      color: #92400E;
      font-family: monospace;
    }
  }
}

</style>
