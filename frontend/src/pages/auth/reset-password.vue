<template>
  <view class="reset-page">
    <!-- 蓝色背景装饰 -->
    <view class="bg-blue">
      <view class="orb orb--1" />
      <view class="orb orb--2" />
    </view>

    <!-- 顶部导航 -->
    <view class="top-nav">
      <view class="back-btn" @click="handleBack">
        <Icon name="arrow-left" :size="20" color="#FFFFFF" />
      </view>
    </view>

    <!-- 页面内容 -->
    <view class="page-content">
      <view class="brand-header">
        <text class="page-title">找回密码</text>
        <text class="page-subtitle">通过邮箱验证码重置你的账号密码</text>
      </view>

      <view class="form-card">

        <!-- 步骤一：输入邮箱并发送验证码 -->
        <template v-if="step === 1">
          <view class="form-item">
            <text class="field-label">注册邮箱</text>
            <view class="input-wrap" :class="{ focused: emailFocused, error: emailError }">
              <view class="input-icon"><Icon name="mail" :size="18" color="#9CA3AF" /></view>
              <input
                v-model="email"
                class="input"
                type="text"
                placeholder="输入注册时使用的邮箱"
                @focus="emailFocused = true"
                @blur="emailFocused = false"
              />
            </view>
            <text v-if="emailError" class="error-hint">{{ emailError }}</text>
          </view>

          <view class="form-item">
            <text class="field-label">验证码</text>
            <view class="input-wrap code-wrap" :class="{ focused: codeFocused, error: codeError }">
              <view class="input-icon"><Icon name="shield" :size="18" color="#9CA3AF" /></view>
              <input
                v-model="code"
                class="input"
                type="number"
                placeholder="6位验证码"
                maxlength="6"
                @focus="codeFocused = true"
                @blur="codeFocused = false"
              />
              <view class="send-code-btn" :class="{ disabled: sendCooldown > 0 || sendLoading }" @click="handleSendCode">
                <text class="send-code-text">
                  {{ sendLoading ? '发送中...' : sendCooldown > 0 ? `${sendCooldown}s` : '发送验证码' }}
                </text>
              </view>
            </view>
            <text v-if="codeError" class="error-hint">{{ codeError }}</text>
          </view>

          <view class="submit-btn" :class="{ loading: submitting }" @click="handleVerifyCode">
            <view v-if="submitting" class="btn-spinner" />
            <text class="btn-text">{{ submitting ? '验证中...' : '下一步' }}</text>
          </view>
        </template>

        <!-- 步骤二：输入新密码 -->
        <template v-else>
          <view class="step-hint">
            <Icon name="check-circle" :size="16" color="#10B981" />
            <text class="step-hint-text">邮箱验证通过，请设置新密码</text>
          </view>

          <view class="form-item">
            <text class="field-label">新密码</text>
            <view class="input-wrap" :class="{ focused: pwdFocused, error: pwdError }">
              <view class="input-icon"><Icon name="lock" :size="18" color="#9CA3AF" /></view>
              <input
                v-model="newPassword"
                class="input"
                :type="showPwd ? 'text' : 'password'"
                placeholder="6-20位密码"
                @focus="pwdFocused = true"
                @blur="pwdFocused = false"
              />
              <view class="toggle-eye" @click="showPwd = !showPwd">
                <Icon :name="showPwd ? 'eye-off' : 'eye'" :size="18" color="#9CA3AF" />
              </view>
            </view>
            <text v-if="pwdError" class="error-hint">{{ pwdError }}</text>
          </view>

          <view class="form-item">
            <text class="field-label">确认新密码</text>
            <view class="input-wrap" :class="{ focused: confirmFocused, error: confirmError }">
              <view class="input-icon"><Icon name="lock" :size="18" color="#9CA3AF" /></view>
              <input
                v-model="confirmPassword"
                class="input"
                :type="showConfirmPwd ? 'text' : 'password'"
                placeholder="再次输入新密码"
                @focus="confirmFocused = true"
                @blur="confirmFocused = false"
              />
              <view class="toggle-eye" @click="showConfirmPwd = !showConfirmPwd">
                <Icon :name="showConfirmPwd ? 'eye-off' : 'eye'" :size="18" color="#9CA3AF" />
              </view>
            </view>
            <text v-if="confirmError" class="error-hint">{{ confirmError }}</text>
          </view>

          <view class="submit-btn" :class="{ loading: submitting }" @click="handleResetPassword">
            <view v-if="submitting" class="btn-spinner" />
            <text class="btn-text">{{ submitting ? '重置中...' : '重置密码' }}</text>
          </view>
        </template>

        <!-- 跳转登录 -->
        <view class="form-footer">
          <text class="footer-text">想起密码了？</text>
          <text class="footer-link" @click="goToLogin">去登录</text>
        </view>

      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Icon from '@/components/icons/index.vue'
import { sendCode, resetPassword } from '@/services/auth'

const step = ref<1 | 2>(1)

// 邮箱 + 验证码
const email = ref('')
const code = ref('')
const emailFocused = ref(false)
const codeFocused = ref(false)
const emailError = ref('')
const codeError = ref('')
const sendLoading = ref(false)
const sendCooldown = ref(0)
let cooldownTimer: ReturnType<typeof setInterval> | null = null

// 新密码
const newPassword = ref('')
const confirmPassword = ref('')
const pwdFocused = ref(false)
const confirmFocused = ref(false)
const pwdError = ref('')
const confirmError = ref('')
const showPwd = ref(false)
const showConfirmPwd = ref(false)

const submitting = ref(false)

// 发送验证码
const handleSendCode = async () => {
  emailError.value = ''
  if (!email.value.trim()) {
    emailError.value = '请输入邮箱'
    return
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) {
    emailError.value = '邮箱格式不正确'
    return
  }
  if (sendCooldown.value > 0 || sendLoading.value) return

  sendLoading.value = true
  try {
    await sendCode({ account: email.value, type: 'reset' })
    uni.showToast({ title: '验证码已发送', icon: 'success' })
    sendCooldown.value = 60
    cooldownTimer = setInterval(() => {
      sendCooldown.value--
      if (sendCooldown.value <= 0) {
        clearInterval(cooldownTimer!)
        cooldownTimer = null
      }
    }, 1000)
  } catch (e: any) {
    emailError.value = e?.message || '发送失败，请稍后重试'
  } finally {
    sendLoading.value = false
  }
}

// 步骤一：校验验证码，进入步骤二
const handleVerifyCode = async () => {
  emailError.value = ''
  codeError.value = ''

  if (!email.value.trim()) { emailError.value = '请输入邮箱'; return }
  if (!code.value.trim() || code.value.length !== 6) { codeError.value = '请输入6位验证码'; return }

  // 前端只做格式校验，实际验证在后端重置密码时完成
  step.value = 2
}

// 步骤二：重置密码
const handleResetPassword = async () => {
  pwdError.value = ''
  confirmError.value = ''

  if (!newPassword.value || newPassword.value.length < 6 || newPassword.value.length > 20) {
    pwdError.value = '密码长度需在6-20位之间'
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    confirmError.value = '两次输入的密码不一致'
    return
  }

  submitting.value = true
  try {
    await resetPassword({
      email: email.value,
      code: code.value,
      newPassword: newPassword.value,
    })
    uni.showToast({ title: '密码重置成功', icon: 'success' })
    setTimeout(() => {
      uni.redirectTo({ url: '/pages/auth/login' })
    }, 1200)
  } catch (e: any) {
    const msg = e?.message || '重置失败，请重试'
    // 验证码错误则退回步骤一
    if (msg.includes('验证码')) {
      step.value = 1
      codeError.value = msg
    } else {
      pwdError.value = msg
    }
  } finally {
    submitting.value = false
  }
}

const handleBack = () => {
  uni.navigateBack({ fail: () => uni.navigateTo({ url: '/pages/auth/login' }) })
}

const goToLogin = () => {
  uni.redirectTo({ url: '/pages/auth/login' })
}
</script>

<style lang="scss" scoped>
.reset-page {
  min-height: 100vh;
  background: #F0F4FF;
  position: relative;
  overflow: hidden;
}

.bg-blue {
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 320rpx;
  background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
  z-index: 0;

  .orb {
    position: absolute;
    border-radius: 50%;
    opacity: 0.2;
  }
  .orb--1 { width: 300rpx; height: 300rpx; background: #fff; top: -80rpx; right: -60rpx; }
  .orb--2 { width: 200rpx; height: 200rpx; background: #fff; top: 120rpx; left: -40rpx; }
}

.top-nav {
  position: relative;
  z-index: 10;
  padding: 80rpx 40rpx 20rpx;
}

.back-btn {
  width: 72rpx; height: 72rpx;
  border-radius: 50%;
  background: rgba(255,255,255,0.2);
  display: flex; align-items: center; justify-content: center;
}

.page-content {
  position: relative;
  z-index: 10;
  padding: 0 40rpx 80rpx;
}

.brand-header {
  padding: 20rpx 0 48rpx;
  .page-title {
    display: block;
    font-size: 52rpx;
    font-weight: 700;
    color: #FFFFFF;
    margin-bottom: 12rpx;
  }
  .page-subtitle {
    font-size: 28rpx;
    color: rgba(255,255,255,0.85);
  }
}

.form-card {
  background: #FFFFFF;
  border-radius: 32rpx;
  padding: 48rpx 40rpx;
  box-shadow: 0 16rpx 48rpx rgba(0,0,0,0.12);
}

.form-item {
  margin-bottom: 32rpx;
  .field-label {
    display: block;
    font-size: 26rpx;
    font-weight: 600;
    color: #374151;
    margin-bottom: 12rpx;
  }
}

.input-wrap {
  display: flex;
  align-items: center;
  background: #F9FAFB;
  border: 2rpx solid #E5E7EB;
  border-radius: 16rpx;
  padding: 0 24rpx;
  height: 96rpx;
  transition: border-color 0.2s;

  &.focused { border-color: #3B82F6; background: #EFF6FF; }
  &.error { border-color: #EF4444; background: #FFF5F5; }
}

.input-icon { margin-right: 16rpx; flex-shrink: 0; }

.input {
  flex: 1;
  font-size: 28rpx;
  color: #1F2937;
  background: transparent;
}

.code-wrap {
  .send-code-btn {
    flex-shrink: 0;
    padding: 12rpx 24rpx;
    background: #3B82F6;
    border-radius: 12rpx;

    &.disabled { background: #9CA3AF; }
    .send-code-text { font-size: 24rpx; color: #fff; white-space: nowrap; }
  }
}

.toggle-eye { padding: 8rpx; flex-shrink: 0; }

.error-hint {
  display: block;
  font-size: 24rpx;
  color: #EF4444;
  margin-top: 8rpx;
}

.step-hint {
  display: flex;
  align-items: center;
  gap: 10rpx;
  background: #ECFDF5;
  padding: 16rpx 20rpx;
  border-radius: 12rpx;
  margin-bottom: 32rpx;

  .step-hint-text { font-size: 26rpx; color: #065F46; }
}

.submit-btn {
  height: 96rpx;
  background: linear-gradient(135deg, #3B82F6, #2563EB);
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  margin-top: 8rpx;
  box-shadow: 0 8rpx 24rpx rgba(59,130,246,0.4);

  &.loading { opacity: 0.75; }
  .btn-text { font-size: 32rpx; font-weight: 600; color: #fff; }
}

.btn-spinner {
  width: 36rpx; height: 36rpx;
  border: 4rpx solid rgba(255,255,255,0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

.form-footer {
  display: flex;
  justify-content: center;
  gap: 8rpx;
  margin-top: 40rpx;
  .footer-text { font-size: 26rpx; color: #9CA3AF; }
  .footer-link { font-size: 26rpx; color: #3B82F6; font-weight: 500; }
}
</style>
