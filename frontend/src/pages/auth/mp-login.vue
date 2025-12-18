<template>
  <view class="mp-login-page">
    <!-- Logo 和标题 -->
    <view class="header">
      <view class="logo-container">
        <text class="logo-emoji">🎓</text>
      </view>
      <text class="title">欢迎来到 CampusLink</text>
      <text class="subtitle">高校资源互助与问答平台</text>
    </view>

    <!-- 权益说明 -->
    <view class="benefits">
      <view class="benefit-item">
        <text class="benefit-icon">📚</text>
        <text class="benefit-text">海量学习资源免费下载</text>
      </view>
      <view class="benefit-item">
        <text class="benefit-icon">💬</text>
        <text class="benefit-text">快速获取问题解答</text>
      </view>
      <view class="benefit-item">
        <text class="benefit-icon">🎯</text>
        <text class="benefit-text">发布任务赚取积分</text>
      </view>
      <view class="benefit-item">
        <text class="benefit-icon">🎉</text>
        <text class="benefit-text">参与社团活动交友</text>
      </view>
    </view>

    <!-- 登录按钮 -->
    <view class="login-actions">
      <button
        class="wechat-login-btn"
        :loading="loginLoading"
        @click="handleWechatLogin"
      >
        <text class="btn-icon">👤</text>
        <text class="btn-text">微信一键登录</text>
      </button>

      <view class="agreement">
        <text class="agreement-text">登录即表示同意</text>
        <text class="agreement-link" @click="handleViewAgreement('user')">《用户协议》</text>
        <text class="agreement-text">和</text>
        <text class="agreement-link" @click="handleViewAgreement('privacy')">《隐私政策》</text>
      </view>
    </view>

    <!-- 跳过登录 -->
    <view class="skip-login" @click="handleSkipLogin">
      <text class="skip-text">暂不登录，先逛逛</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { wechatLogin } from '@/services/auth'

const userStore = useUserStore()
const loginLoading = ref(false)

/**
 * 微信一键登录
 *
 * 流程说明：
 * 1. 调用 uni.login() 获取微信临时凭证 code
 * 2. 将 code 发送到后端，后端调用微信接口换取 openid
 * 3. 后端根据 openid 查询用户，不存在则自动注册
 * 4. 返回 JWT Token 和用户信息
 * 5. 保存登录状态并跳转到"我的"页面
 */
const handleWechatLogin = async () => {
  try {
    loginLoading.value = true

    // 1. 获取微信登录 code
    console.log('[微信登录] 开始获取微信登录凭证...')
    const loginRes = await uni.login({ provider: 'weixin' }) as any

    if (!loginRes.code) {
      throw new Error('获取微信登录凭证失败，请重试')
    }

    console.log('[微信登录] 成功获取 code:', loginRes.code.substring(0, 10) + '...')

    // 2. 调用后端登录接口
    console.log('[微信登录] 调用后端登录接口...')
    const response = await wechatLogin({
      code: loginRes.code
      // 可选：传递用户昵称和头像（需要用户授权）
      // nickname: userInfo.nickName,
      // avatarUrl: userInfo.avatarUrl
    })

    // 3. 保存登录信息到 Store
    userStore.login(response)

    console.log('[微信登录] 登录成功，用户ID:', response.user.uId || response.user.userId)

    // 4. 提示登录成功
    uni.showToast({
      title: '登录成功',
      icon: 'success',
      duration: 1500
    })

    // 5. 延迟跳转到"我的"页面
    setTimeout(() => {
      uni.switchTab({ url: '/pages/user/index' })
    }, 1500)

  } catch (error: any) {
    console.error('[微信登录] 登录失败:', error)

    // 根据错误类型显示不同提示
    let errorMessage = '登录失败，请重试'

    if (error.message) {
      if (error.message.includes('code')) {
        errorMessage = '微信登录凭证无效，请重试'
      } else if (error.message.includes('网络')) {
        errorMessage = '网络连接失败，请检查网络'
      } else if (error.message.includes('频率')) {
        errorMessage = '登录过于频繁，请稍后再试'
      } else {
        errorMessage = error.message
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

// 查看协议
const handleViewAgreement = (type: 'user' | 'privacy') => {
  uni.showModal({
    title: type === 'user' ? '用户协议' : '隐私政策',
    content: '协议页面开发中...',
    showCancel: false
  })

  // 后续跳转到真实协议页面
  /*
  const url = type === 'user'
    ? '/pages/about/user-agreement'
    : '/pages/about/privacy-policy'
  uni.navigateTo({ url })
  */
}

// 跳过登录
const handleSkipLogin = () => {
  uni.switchTab({ url: '/pages/home/index' })
}
</script>

<style scoped lang="scss">
.mp-login-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #EFF6FF 0%, #FFFFFF 50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 64rpx 80rpx;
  position: relative;

  // 背景装饰
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 600rpx;
    background: radial-gradient(ellipse 70% 50% at 50% 0%, rgba(37, 99, 235, 0.08) 0%, transparent 70%);
    pointer-events: none;
  }
}

.header {
  text-align: center;
  margin-bottom: 96rpx;
  position: relative;
  z-index: 1;

  .logo-container {
    width: 160rpx;
    height: 160rpx;
    margin: 0 auto 40rpx;
    background: linear-gradient(135deg, #2563EB 0%, #3B82F6 100%);
    border-radius: 40rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 16rpx 48rpx rgba(37, 99, 235, 0.2);
    position: relative;

    &::after {
      content: '';
      position: absolute;
      inset: 0;
      background: linear-gradient(180deg, rgba(255, 255, 255, 0.3) 0%, transparent 50%);
      border-radius: 40rpx;
    }
  }

  .logo-emoji {
    font-size: 80rpx;
    position: relative;
    z-index: 1;
  }

  .title {
    display: block;
    font-size: 48rpx;
    font-weight: 700;
    color: #0F172A;
    margin-bottom: 20rpx;
    letter-spacing: 0.5rpx;
  }

  .subtitle {
    display: block;
    font-size: 28rpx;
    color: #64748B;
    font-weight: 500;
  }
}

.benefits {
  width: 100%;
  margin-bottom: 96rpx;
  position: relative;
  z-index: 1;

  .benefit-item {
    display: flex;
    align-items: center;
    padding: 28rpx 0;
    transition: transform 0.2s;

    &:active {
      transform: translateX(8rpx);
    }
  }

  .benefit-icon {
    font-size: 44rpx;
    margin-right: 28rpx;
    flex-shrink: 0;
  }

  .benefit-text {
    font-size: 30rpx;
    color: #334155;
    font-weight: 500;
  }
}

.login-actions {
  width: 100%;
  margin-bottom: 48rpx;
  position: relative;
  z-index: 1;

  .wechat-login-btn {
    width: 100%;
    height: 96rpx;
    background: linear-gradient(135deg, #10B981 0%, #059669 100%);
    border-radius: 48rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 8rpx 24rpx rgba(16, 185, 129, 0.3);
    border: none;
    margin-bottom: 32rpx;
    position: relative;
    overflow: hidden;

    &::before {
      content: '';
      position: absolute;
      inset: 0;
      background: linear-gradient(180deg, rgba(255, 255, 255, 0.2) 0%, transparent 50%);
    }

    &::after {
      border: none;
    }

    &:active {
      transform: scale(0.98);
    }

    .btn-icon {
      font-size: 36rpx;
      margin-right: 12rpx;
      position: relative;
      z-index: 1;
    }

    .btn-text {
      font-size: 32rpx;
      font-weight: 600;
      color: #FFFFFF;
      position: relative;
      z-index: 1;
    }
  }

  .agreement {
    text-align: center;
    line-height: 1.6;
    padding: 0 32rpx;

    .agreement-text {
      font-size: 24rpx;
      color: #94A3B8;
    }

    .agreement-link {
      font-size: 24rpx;
      color: #2563EB;
      text-decoration: underline;
    }
  }
}

.skip-login {
  position: relative;
  z-index: 1;
  padding: 16rpx;

  .skip-text {
    font-size: 28rpx;
    color: #64748B;
    text-decoration: underline;
  }

  &:active {
    opacity: 0.7;
  }
}
</style>
