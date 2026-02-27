<template>
  <view class="register-page">
    <!-- 蓝色背景 -->
    <view class="bg-blue">
      <view class="orb orb--1" />
      <view class="orb orb--2" />
    </view>

    <!-- 顶部导航 -->
    <view class="top-nav">
      <view class="back-btn" @click="handleBack">
        <Icon name="arrow-left" :size="40" color="#FFFFFF" />
      </view>
    </view>

    <!-- 页面内容 -->
    <view class="page-content">
      <!-- 品牌头部（紧凑版） -->
      <view class="brand-header">
        <text class="page-title">创建账号</text>
        <text class="page-subtitle">加入 CampusLink，开启校园互助之旅</text>
      </view>

      <!-- 表单卡片 -->
      <view class="form-card">

        <!-- 邮箱或手机号 -->
        <view class="form-item">
          <text class="field-label">邮箱 / 手机号</text>
          <view class="input-wrap" :class="{ focused: accountFocused, error: accountError }">
            <view class="input-icon"><Icon name="mail" :size="32" color="#9CA3AF" /></view>
            <input
              v-model="formData.account"
              class="input"
              type="text"
              placeholder="输入邮箱或手机号"
              @focus="accountFocused = true"
              @blur="handleAccountBlur"
            />
          </view>
        </view>

        <!-- 用户名 -->
        <view class="form-item">
          <text class="field-label">用户名</text>
          <view class="input-wrap" :class="{ focused: usernameFocused, error: usernameError }">
            <view class="input-icon"><Icon name="user" :size="32" color="#9CA3AF" /></view>
            <input
              v-model="formData.username"
              class="input"
              type="text"
              placeholder="3-20位字母、数字或下划线"
              @focus="usernameFocused = true"
              @blur="handleUsernameBlur"
            />
            <view v-if="usernameChecking" class="status-icon checking">
              <view class="mini-spinner" />
            </view>
            <view v-else-if="usernameAvailable === true" class="status-icon ok">
              <Icon name="check" :size="24" color="#10B981" />
            </view>
            <view v-else-if="usernameAvailable === false" class="status-icon err">
              <Icon name="x" :size="24" color="#EF4444" />
            </view>
          </view>
          <text v-if="usernameHint" class="hint-text" :class="{ 'hint-error': usernameAvailable === false }">
            {{ usernameHint }}
          </text>
        </view>

        <!-- 昵称（选填） -->
        <view class="form-item">
          <text class="field-label">昵称 <text class="optional">（选填）</text></text>
          <view class="input-wrap" :class="{ focused: nicknameFocused }">
            <view class="input-icon"><Icon name="smile" :size="32" color="#9CA3AF" /></view>
            <input
              v-model="formData.nickname"
              class="input"
              type="text"
              placeholder="默认与用户名相同"
              @focus="nicknameFocused = true"
              @blur="nicknameFocused = false"
            />
          </view>
        </view>

        <!-- 密码 -->
        <view class="form-item">
          <text class="field-label">设置密码</text>
          <view class="input-wrap" :class="{ focused: passwordFocused, error: passwordError }">
            <view class="input-icon"><Icon name="lock" :size="32" color="#9CA3AF" /></view>
            <input
              v-model="formData.password"
              class="input"
              :type="showPassword ? 'text' : 'password'"
              placeholder="至少6位"
              @focus="passwordFocused = true"
              @blur="passwordFocused = false"
            />
            <view class="eye-toggle" @click="showPassword = !showPassword">
              <Icon :name="showPassword ? 'eye' : 'eye-off'" :size="32" color="#9CA3AF" />
            </view>
          </view>
          <!-- 密码强度条 -->
          <view v-if="formData.password" class="strength-row">
            <view class="strength-bar">
              <view
                class="strength-fill"
                :class="`strength-${passwordStrength.level}`"
                :style="{ width: passwordStrength.percent + '%' }"
              />
            </view>
            <text class="strength-label" :class="`strength-${passwordStrength.level}`">
              {{ passwordStrength.text }}
            </text>
          </view>
        </view>

        <!-- 确认密码 -->
        <view class="form-item">
          <text class="field-label">确认密码</text>
          <view class="input-wrap" :class="{ focused: confirmFocused, error: confirmError }">
            <view class="input-icon"><Icon name="lock" :size="32" color="#9CA3AF" /></view>
            <input
              v-model="formData.confirmPassword"
              class="input"
              :type="showConfirm ? 'text' : 'password'"
              placeholder="再输入一次密码"
              @focus="confirmFocused = true"
              @blur="confirmFocused = false"
            />
            <view class="eye-toggle" @click="showConfirm = !showConfirm">
              <Icon :name="showConfirm ? 'eye' : 'eye-off'" :size="32" color="#9CA3AF" />
            </view>
          </view>
        </view>

        <!-- 验证码 -->
        <view class="form-item">
          <text class="field-label">验证码</text>
          <view class="code-row">
            <view class="input-wrap code-input" :class="{ focused: codeFocused }">
              <view class="input-icon"><Icon name="shield-check" :size="32" color="#9CA3AF" /></view>
              <input
                v-model="formData.code"
                class="input"
                type="text"
                placeholder="输入验证码"
                @focus="codeFocused = true"
                @blur="codeFocused = false"
              />
            </view>
            <view
              class="send-code-btn"
              :class="{ disabled: codeCountdown > 0 || !canSendCode }"
              @click="handleSendCode"
            >
              <text class="send-code-text">
                {{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}
              </text>
            </view>
          </view>
        </view>

        <!-- 注册按钮 -->
        <view
          class="register-btn"
          :class="{ disabled: loading }"
          @click="handleRegister"
        >
          <view v-if="loading" class="loading-spinner" />
          <text class="btn-text">{{ loading ? '注册中...' : '注册并登录' }}</text>
        </view>

        <!-- 已有账号 -->
        <view class="login-row" @click="handleBack">
          <text class="login-hint">已有账号?</text>
          <text class="login-link">立即登录 →</text>
        </view>

        <!-- 协议 -->
        <view class="agreement-row">
          <text class="agreement-text">注册即表示同意</text>
          <text class="agreement-link" @click="goToTerms">《用户协议》</text>
          <text class="agreement-text">和</text>
          <text class="agreement-link" @click="goToPrivacy">《隐私政策》</text>
        </view>
      </view>

      <view class="page-bottom" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { register, sendCode, type RegisterRequest, type AuthResponse } from '@/services/auth'
import Icon from '@/components/icons/index.vue'
import config from '@/config'

const userStore = useUserStore()

const formData = ref({
  account: '',
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  code: ''
})

const accountFocused = ref(false)
const usernameFocused = ref(false)
const nicknameFocused = ref(false)
const passwordFocused = ref(false)
const confirmFocused = ref(false)
const codeFocused = ref(false)

const accountError = ref(false)
const usernameError = ref(false)
const passwordError = ref(false)
const confirmError = ref(false)

const showPassword = ref(false)
const showConfirm = ref(false)
const loading = ref(false)
const codeCountdown = ref(0)
const usernameChecking = ref(false)
const usernameAvailable = ref<boolean | null>(null)
const usernameHint = ref('')

let countdownTimer: number | null = null

// 密码强度
const passwordStrength = computed(() => {
  const pwd = formData.value.password
  if (!pwd) return { level: 'weak', percent: 0, text: '' }
  let score = 0
  if (pwd.length >= 6) score += 25
  if (pwd.length >= 8) score += 25
  if (/[a-z]/.test(pwd)) score += 15
  if (/[A-Z]/.test(pwd)) score += 15
  if (/[0-9]/.test(pwd)) score += 10
  if (/[^a-zA-Z0-9]/.test(pwd)) score += 10
  if (score < 40) return { level: 'weak', percent: score, text: '弱' }
  if (score < 70) return { level: 'medium', percent: score, text: '中' }
  return { level: 'strong', percent: score, text: '强' }
})

// 是否可以发送验证码
const canSendCode = computed(() => {
  const account = formData.value.account
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  const phoneRegex = /^1[3-9]\d{9}$/
  return emailRegex.test(account) || phoneRegex.test(account)
})

// 生成用户名
const generateUsername = (account: string): string => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (emailRegex.test(account)) {
    return account.split('@')[0].replace(/[^a-zA-Z0-9_]/g, '_')
  }
  return 'user_' + account.replace(/[^0-9]/g, '')
}

// 账号失焦 - 自动填充用户名
const handleAccountBlur = () => {
  accountFocused.value = false
  if (formData.value.account && canSendCode.value) {
    if (!formData.value.username) {
      formData.value.username = generateUsername(formData.value.account)
      usernameHint.value = '已自动生成，可修改'
    }
    if (!formData.value.nickname) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      formData.value.nickname = emailRegex.test(formData.value.account)
        ? formData.value.account.split('@')[0].substring(0, 20)
        : formData.value.account.substring(0, 20)
    }
  }
}

// 用户名失焦 - 格式检验
const handleUsernameBlur = async () => {
  usernameFocused.value = false
  const username = formData.value.username.trim()
  usernameAvailable.value = null
  usernameHint.value = ''

  if (!username) {
    usernameError.value = true
    usernameHint.value = '用户名不能为空'
    return
  }
  if (username.length < 3 || username.length > 20) {
    usernameError.value = true
    usernameAvailable.value = false
    usernameHint.value = '用户名长度需要 3-20 个字符'
    return
  }
  if (!/^[a-zA-Z0-9_]+$/.test(username)) {
    usernameError.value = true
    usernameAvailable.value = false
    usernameHint.value = '只能包含字母、数字和下划线'
    return
  }
  usernameChecking.value = true
  await new Promise(resolve => setTimeout(resolve, 500))
  usernameChecking.value = false
  usernameError.value = false
  usernameAvailable.value = true
  usernameHint.value = '用户名可用'
}

// 发送验证码
const handleSendCode = async () => {
  if (!canSendCode.value || codeCountdown.value > 0) return
  try {
    await sendCode({ account: formData.value.account, type: 'register' })
    uni.showToast({ title: '验证码已发送', icon: 'success' })
    codeCountdown.value = 60
    countdownTimer = setInterval(() => {
      codeCountdown.value--
      if (codeCountdown.value <= 0 && countdownTimer) {
        clearInterval(countdownTimer)
        countdownTimer = null
      }
    }, 1000) as unknown as number
  } catch (error: any) {
    uni.showToast({ title: error.message || '发送失败，稍后重试', icon: 'none' })
  }
}

// 表单校验
const validateForm = () => {
  accountError.value = !formData.value.account || !canSendCode.value
  usernameError.value = !formData.value.username || formData.value.username.length < 3
  passwordError.value = !formData.value.password || formData.value.password.length < 6
  confirmError.value = formData.value.password !== formData.value.confirmPassword

  if (accountError.value) {
    uni.showToast({ title: '请填写正确的邮箱或手机号', icon: 'none' })
    return false
  }
  if (usernameError.value) {
    uni.showToast({ title: '用户名需要 3-20 个字符', icon: 'none' })
    return false
  }
  if (!/^[a-zA-Z0-9_]+$/.test(formData.value.username)) {
    uni.showToast({ title: '用户名只能包含字母、数字和下划线', icon: 'none' })
    return false
  }
  if (usernameAvailable.value === false) {
    uni.showToast({ title: '该用户名已被占用', icon: 'none' })
    return false
  }
  if (passwordError.value) {
    uni.showToast({ title: '密码至少需要 6 位', icon: 'none' })
    return false
  }
  if (confirmError.value) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' })
    return false
  }
  return true
}

// 注册
const handleRegister = async () => {
  if (!validateForm() || loading.value) return
  loading.value = true
  try {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    const isEmail = emailRegex.test(formData.value.account)
    const registerData: RegisterRequest = {
      username: formData.value.username.trim(),
      password: formData.value.password,
      email: isEmail ? formData.value.account : undefined,
      phone: !isEmail ? formData.value.account : undefined,
      nickname: formData.value.nickname.trim() || formData.value.username.trim(),
      schoolId: 1,
      code: formData.value.code || '000000'
    }
    const response: AuthResponse = await register(registerData)
    uni.setStorageSync(config.tokenKey, response.token)
    uni.setStorageSync(config.refreshTokenKey, response.refreshToken)
    uni.setStorageSync(config.userInfoKey, JSON.stringify(response.user))

    uni.showToast({ title: '注册成功', icon: 'success', duration: 1500 })
    setTimeout(() => {
      uni.switchTab({ url: '/pages/home/index' })
    }, 1500)
  } catch (error: any) {
    let msg = '注册失败，请稍后重试'
    if (error.message) {
      const m = error.message
      if (m.includes('已存在') || m.includes('已注册')) msg = '该账号已被注册，请直接登录'
      else if (m.includes('验证码')) msg = '验证码错误或已过期'
      else msg = m
    }
    uni.showToast({ title: msg, icon: 'none', duration: 2500 })
  } finally {
    loading.value = false
  }
}

const handleBack = () => {
  uni.navigateBack()
}

const goToTerms = () => {
  uni.navigateTo({ url: '/pages/about/terms' })
}

const goToPrivacy = () => {
  uni.navigateTo({ url: '/pages/about/privacy' })
}

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
})
</script>

<style scoped lang="scss">

// =============================================
// 页面基础
// =============================================

.register-page {
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
  height: 480rpx;
  background: #2355C8;
  border-radius: 0 0 72rpx 72rpx;
  overflow: hidden;
  z-index: 0;
}

.orb {
  position: absolute;
  border-radius: 50%;

  &--1 {
    width: 240rpx;
    height: 240rpx;
    top: -50rpx;
    right: -40rpx;
    background: rgba(147, 197, 253, 0.20);
  }

  &--2 {
    width: 180rpx;
    height: 180rpx;
    bottom: 40rpx;
    left: -50rpx;
    background: rgba(56, 189, 248, 0.15);
  }
}

// =============================================
// 顶部导航
// =============================================

.top-nav {
  position: relative;
  z-index: 10;
  padding: 80rpx 32rpx 0;
}

.back-btn {
  width: 72rpx;
  height: 72rpx;
  background: rgba(255, 255, 255, 0.15);
  border: 2rpx solid rgba(255, 255, 255, 0.25);
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;

  &:active {
    background: rgba(255, 255, 255, 0.25);
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
  padding: 0 30rpx;
}

// =============================================
// 品牌头部（紧凑）
// =============================================

.brand-header {
  width: 100%;
  padding: 28rpx 0 36rpx;
  display: flex;
  flex-direction: column;
}

.page-title {
  display: block;
  font-size: 48rpx;
  font-weight: 700;
  color: #FFFFFF;
  margin-bottom: 10rpx;
}

.page-subtitle {
  display: block;
  font-size: 24rpx;
  color: #BFDBFE;
}

// =============================================
// 表单卡片
// =============================================

.form-card {
  width: 100%;
  background: #FFFFFF;
  border-radius: 48rpx;
  padding: 44rpx 40rpx 40rpx;
  box-shadow:
    0 24rpx 80rpx rgba(29, 78, 216, 0.12),
    0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

// =============================================
// 表单字段
// =============================================

.form-item {
  margin-bottom: 20rpx;
}

.field-label {
  display: block;
  font-size: 24rpx;
  font-weight: 600;
  color: #374151;
  margin-bottom: 10rpx;

  .optional {
    font-weight: 400;
    color: #9CA3AF;
  }
}

.input-wrap {
  display: flex;
  align-items: center;
  height: 88rpx;
  background: #F8FAFF;
  border: 2rpx solid #E8EEFF;
  border-radius: 20rpx;
  padding: 0 20rpx;
  gap: 12rpx;
  transition: border-color 0.2s, box-shadow 0.2s, background 0.2s;

  &.focused {
    background: #FFFFFF;
    border-color: #2563EB;
    box-shadow: 0 0 0 6rpx rgba(37, 99, 235, 0.08);
  }

  &.error {
    border-color: #FCA5A5;
    background: #FFF5F5;
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
    padding: 6rpx;
    cursor: pointer;

    &:active { opacity: 0.6; }
  }
}

.status-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;

  &.checking { width: 32rpx; height: 32rpx; }
}

.mini-spinner {
  width: 28rpx;
  height: 28rpx;
  border: 2rpx solid rgba(37, 99, 235, 0.2);
  border-top-color: #2563EB;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

.hint-text {
  display: block;
  font-size: 22rpx;
  color: #10B981;
  margin-top: 8rpx;
  padding-left: 4rpx;

  &.hint-error { color: #EF4444; }
}

// =============================================
// 密码强度条
// =============================================

.strength-row {
  display: flex;
  align-items: center;
  gap: 14rpx;
  margin-top: 10rpx;
}

.strength-bar {
  flex: 1;
  height: 6rpx;
  background: #E5E7EB;
  border-radius: 3rpx;
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  border-radius: 3rpx;
  transition: width 0.3s ease;

  &.strength-weak   { background: #EF4444; }
  &.strength-medium { background: #F59E0B; }
  &.strength-strong { background: #10B981; }
}

.strength-label {
  font-size: 22rpx;
  font-weight: 600;
  min-width: 28rpx;

  &.strength-weak   { color: #EF4444; }
  &.strength-medium { color: #F59E0B; }
  &.strength-strong { color: #10B981; }
}

// =============================================
// 验证码行
// =============================================

.code-row {
  display: flex;
  gap: 16rpx;
}

.code-input {
  flex: 1;
}

.send-code-btn {
  flex-shrink: 0;
  height: 88rpx;
  padding: 0 28rpx;
  background: #EEF2FF;
  border: 2rpx solid #C7D2FE;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;

  &:active:not(.disabled) {
    background: #E0E7FF;
  }

  &.disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  .send-code-text {
    font-size: 24rpx;
    font-weight: 600;
    color: #4F46E5;
    white-space: nowrap;
  }
}

// =============================================
// 注册按钮
// =============================================

.register-btn {
  width: 100%;
  height: 96rpx;
  background: #2563EB;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  box-shadow: 0 10rpx 32rpx rgba(37, 99, 235, 0.35);
  margin-top: 8rpx;
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

.loading-spinner {
  width: 32rpx;
  height: 32rpx;
  border: 3rpx solid rgba(255, 255, 255, 0.35);
  border-top-color: #FFFFFF;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

// =============================================
// 已有账号
// =============================================

.login-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
  margin-bottom: 24rpx;
  cursor: pointer;

  &:active { opacity: 0.7; }

  .login-hint {
    font-size: 26rpx;
    color: #6B7280;
  }

  .login-link {
    font-size: 26rpx;
    font-weight: 600;
    color: #2563EB;
  }
}

// =============================================
// 用户协议
// =============================================

.agreement-row {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 4rpx;

  .agreement-text {
    font-size: 22rpx;
    color: #9CA3AF;
  }

  .agreement-link {
    font-size: 22rpx;
    color: #2563EB;
    cursor: pointer;
  }
}

// =============================================
// 页面底部间距
// =============================================

.page-bottom {
  height: 80rpx;
}
</style>
