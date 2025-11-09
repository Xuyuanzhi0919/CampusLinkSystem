<template>
  <view v-if="visible" class="auth-modal-overlay" @click="handleOverlayClick">
    <view class="auth-modal" :class="{ 'modal-enter': isEntering }" @click.stop>
      <!-- 关闭按钮 -->
      <view class="close-btn" :class="{ 'close-rotate': isClosing }" @click="close">
        <text class="iconfont icon-close">×</text>
      </view>

      <!-- Logo 和标题区域 -->
      <view class="header">
        <view class="logo-box">
          <image class="logo" src="/static/logo.png" mode="aspectFit" />
        </view>
        <text class="title">{{ currentMode === 'login' ? '欢迎回来' : '创建你的 CampusLink 账户' }}</text>
        <text class="subtitle">{{
          currentMode === 'login'
            ? '一起加入 100万大学生的互助学习圈'
            : '3 秒完成注册,解锁下载、提问与互助任务'
        }}</text>
      </view>

      <!-- Tab 切换 -->
      <view class="tab-container">
        <view
          class="tab-item"
          :class="{ active: currentMode === 'login' }"
          @click="switchMode('login')"
        >
          登录
        </view>
        <view
          class="tab-item"
          :class="{ active: currentMode === 'register' }"
          @click="switchMode('register')"
        >
          注册
        </view>
        <view class="tab-slider" :class="{ 'slide-right': currentMode === 'register' }"></view>
      </view>

      <!-- 登录表单 -->
      <view v-if="currentMode === 'login'" class="form-container" :class="{ 'fade-in': isFadingIn }">
        <!-- 用户名 -->
        <view class="form-item">
          <view class="input-wrapper" :class="{ focused: focusField === 'username', error: errors.username }">
            <text class="iconfont icon-user">👤</text>
            <input
              class="input"
              type="text"
              v-model="loginForm.username"
              placeholder="输入用户名/邮箱/手机号"
              @focus="focusField = 'username'"
              @blur="focusField = ''"
            />
          </view>
          <text v-if="errors.username" class="error-text">{{ errors.username }}</text>
        </view>

        <!-- 密码 -->
        <view class="form-item">
          <view class="input-wrapper" :class="{ focused: focusField === 'password', error: errors.password }">
            <text class="iconfont icon-lock">🔒</text>
            <input
              class="input"
              :type="showPassword ? 'text' : 'password'"
              v-model="loginForm.password"
              placeholder="输入密码"
              @focus="focusField = 'password'"
              @blur="focusField = ''"
            />
            <text class="eye-icon" @click="showPassword = !showPassword">
              {{ showPassword ? '👁' : '🙈' }}
            </text>
          </view>
          <text v-if="errors.password" class="error-text">{{ errors.password }}</text>
        </view>

        <!-- 记住我 & 忘记密码 -->
        <view class="form-options">
          <label class="checkbox-label">
            <checkbox :checked="rememberMe" @change="rememberMe = !rememberMe" />
            <text>记住我（7天）</text>
          </label>
          <text class="link-text">忘记密码?</text>
        </view>

        <!-- 安全提示 -->
        <view class="security-tip">
          <text class="tip-icon">ℹ️</text>
          <text class="tip-text">请勿在公共设备勾选记住密码</text>
        </view>

        <!-- 登录按钮 -->
        <button
          class="primary-btn"
          :class="{ loading: isLoading, success: isSuccess }"
          :disabled="isLoading"
          @click="handleLogin"
        >
          <view v-if="isLoading" class="loading-spinner"></view>
          <text v-else-if="isSuccess">✓</text>
          <text v-else>登录</text>
        </button>
      </view>

      <!-- 注册表单 -->
      <view v-if="currentMode === 'register'" class="form-container" :class="{ 'fade-in': isFadingIn }">
        <!-- 昵称 -->
        <view class="form-item">
          <view class="input-wrapper" :class="{ focused: focusField === 'nickname', error: errors.nickname }">
            <text class="iconfont">😊</text>
            <input
              class="input"
              type="text"
              v-model="registerForm.nickname"
              placeholder="输入昵称（2-16字）"
              @focus="focusField = 'nickname'"
              @blur="focusField = ''"
            />
          </view>
          <text v-if="errors.nickname" class="error-text">{{ errors.nickname }}</text>
        </view>

        <!-- 用户名 -->
        <view class="form-item">
          <view class="input-wrapper" :class="{ focused: focusField === 'reg-username', error: errors.regUsername }">
            <text class="iconfont">👤</text>
            <input
              class="input"
              type="text"
              v-model="registerForm.username"
              placeholder="输入用户名（唯一登录ID）"
              @focus="focusField = 'reg-username'"
              @blur="focusField = ''"
            />
          </view>
          <text v-if="errors.regUsername" class="error-text">{{ errors.regUsername }}</text>
        </view>

        <!-- 邮箱 -->
        <view class="form-item">
          <view class="input-wrapper" :class="{ focused: focusField === 'email', error: errors.email }">
            <text class="iconfont">📧</text>
            <input
              class="input"
              type="text"
              v-model="registerForm.email"
              placeholder="输入邮箱"
              @focus="focusField = 'email'"
              @blur="focusField = ''"
            />
          </view>
          <text v-if="errors.email" class="error-text">{{ errors.email }}</text>
        </view>

        <!-- 手机号 -->
        <view class="form-item">
          <view class="input-wrapper" :class="{ focused: focusField === 'phone', error: errors.phone }">
            <text class="iconfont">📱</text>
            <input
              class="input"
              type="number"
              v-model="registerForm.phone"
              placeholder="输入手机号"
              @focus="focusField = 'phone'"
              @blur="focusField = ''"
            />
          </view>
          <text v-if="errors.phone" class="error-text">{{ errors.phone }}</text>
        </view>

        <!-- 学校选择 -->
        <view class="form-item">
          <view class="input-wrapper" :class="{ focused: focusField === 'school', error: errors.school }">
            <text class="iconfont">🏫</text>
            <picker
              mode="selector"
              :range="schools"
              range-key="schoolName"
              @change="handleSchoolChange"
            >
              <view class="picker-view">
                {{ selectedSchool?.schoolName || '选择学校' }}
              </view>
            </picker>
          </view>
          <text v-if="errors.school" class="error-text">{{ errors.school }}</text>
        </view>

        <!-- 学号 -->
        <view class="form-item">
          <view class="input-wrapper" :class="{ focused: focusField === 'studentId', error: errors.studentId }">
            <text class="iconfont">🎓</text>
            <input
              class="input"
              type="text"
              v-model="registerForm.studentId"
              placeholder="输入学号"
              @focus="focusField = 'studentId'"
              @blur="focusField = ''"
            />
          </view>
          <text v-if="errors.studentId" class="error-text">{{ errors.studentId }}</text>
        </view>

        <!-- 密码 -->
        <view class="form-item">
          <view class="input-wrapper" :class="{ focused: focusField === 'reg-password', error: errors.regPassword }">
            <text class="iconfont">🔒</text>
            <input
              class="input"
              :type="showPassword ? 'text' : 'password'"
              v-model="registerForm.password"
              placeholder="输入密码（≥8位）"
              @focus="focusField = 'reg-password'"
              @blur="focusField = ''"
            />
            <text class="eye-icon" @click="showPassword = !showPassword">
              {{ showPassword ? '👁' : '🙈' }}
            </text>
          </view>
          <text v-if="errors.regPassword" class="error-text">{{ errors.regPassword }}</text>
          <view v-if="registerForm.password" class="password-strength">
            <view class="strength-bar" :class="passwordStrength"></view>
            <text class="strength-text">
              {{ passwordStrength === 'weak' ? '弱' : passwordStrength === 'medium' ? '中' : '强' }}
            </text>
          </view>
        </view>

        <!-- 确认密码 -->
        <view class="form-item">
          <view class="input-wrapper" :class="{ focused: focusField === 'confirmPassword', error: errors.confirmPassword }">
            <text class="iconfont">🔒</text>
            <input
              class="input"
              :type="showPassword ? 'text' : 'password'"
              v-model="registerForm.confirmPassword"
              placeholder="再次输入密码"
              @focus="focusField = 'confirmPassword'"
              @blur="focusField = ''"
            />
          </view>
          <text v-if="errors.confirmPassword" class="error-text">{{ errors.confirmPassword }}</text>
        </view>

        <!-- 协议 -->
        <view class="agreement">
          <checkbox :checked="agreeTerms" @change="agreeTerms = !agreeTerms" />
          <text class="agreement-text">
            我已阅读并同意
            <text class="link-text">《用户协议》</text>
            和
            <text class="link-text">《隐私政策》</text>
          </text>
        </view>

        <!-- 注册按钮 -->
        <button
          class="primary-btn"
          :class="{ loading: isLoading, success: isSuccess }"
          :disabled="isLoading || !agreeTerms"
          @click="handleRegister"
        >
          <view v-if="isLoading" class="loading-spinner"></view>
          <text v-else-if="isSuccess">✓</text>
          <text v-else>注册并登录</text>
        </button>
      </view>

      <!-- 底部切换 -->
      <view class="footer">
        <text class="footer-text">
          {{ currentMode === 'login' ? '还没有账号？' : '已有账号？' }}
          <text class="link-text" @click="switchMode(currentMode === 'login' ? 'register' : 'login')">
            {{ currentMode === 'login' ? '去注册' : '去登录' }}
          </text>
        </text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { login, register, getSchoolList, type School } from '@/services/auth'

// Props
interface Props {
  visible: boolean
  defaultMode?: 'login' | 'register'
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  defaultMode: 'login'
})

// Emits
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}>()

// Store
const userStore = useUserStore()

// State
const currentMode = ref<'login' | 'register'>(props.defaultMode)
const focusField = ref('')
const showPassword = ref(false)
const rememberMe = ref(false)
const agreeTerms = ref(false)
const isLoading = ref(false)
const isSuccess = ref(false)
const isEntering = ref(false)
const isClosing = ref(false)
const isFadingIn = ref(false)

// 学校列表
const schools = ref<School[]>([])
const selectedSchool = ref<School | null>(null)

// 登录表单
const loginForm = ref({
  username: '',
  password: ''
})

// 注册表单
const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  nickname: '',
  schoolId: 0,
  studentId: ''
})

// 错误信息
const errors = ref<Record<string, string>>({})

// 计算密码强度
const passwordStrength = computed(() => {
  const pwd = registerForm.value.password
  if (pwd.length < 8) return 'weak'

  let strength = 0
  if (/[a-z]/.test(pwd)) strength++
  if (/[A-Z]/.test(pwd)) strength++
  if (/[0-9]/.test(pwd)) strength++
  if (/[^a-zA-Z0-9]/.test(pwd)) strength++

  if (strength <= 2) return 'weak'
  if (strength === 3) return 'medium'
  return 'strong'
})

// 监听 visible 变化
watch(() => props.visible, (newVal) => {
  if (newVal) {
    isEntering.value = true
    setTimeout(() => {
      isEntering.value = false
    }, 200)
  }
})

// 监听模式切换
watch(currentMode, () => {
  // 清空表单和错误
  loginForm.value = { username: '', password: '' }
  registerForm.value = {
    username: '',
    password: '',
    confirmPassword: '',
    email: '',
    phone: '',
    nickname: '',
    schoolId: 0,
    studentId: ''
  }
  errors.value = {}

  // 淡入动画
  isFadingIn.value = true
  setTimeout(() => {
    isFadingIn.value = false
  }, 150)
})

// 加载学校列表
const loadSchools = async () => {
  try {
    const res = await getSchoolList({ pageSize: 100 })
    schools.value = res.records
  } catch (error) {
    console.error('加载学校列表失败:', error)
  }
}

onMounted(() => {
  loadSchools()
})

// 切换模式
const switchMode = (mode: 'login' | 'register') => {
  currentMode.value = mode
}

// 学校选择
const handleSchoolChange = (e: any) => {
  const index = e.detail.value
  selectedSchool.value = schools.value[index]
  registerForm.value.schoolId = selectedSchool.value.schoolId
  errors.value.school = ''
}

// 验证登录表单
const validateLoginForm = () => {
  errors.value = {}

  if (!loginForm.value.username) {
    errors.value.username = '请输入用户名/邮箱/手机号'
    return false
  }

  if (!loginForm.value.password) {
    errors.value.password = '请输入密码'
    return false
  }

  return true
}

// 验证注册表单
const validateRegisterForm = () => {
  errors.value = {}

  if (!registerForm.value.nickname || registerForm.value.nickname.length < 2 || registerForm.value.nickname.length > 16) {
    errors.value.nickname = '昵称应为2-16个字符'
    return false
  }

  if (!registerForm.value.username) {
    errors.value.regUsername = '请输入用户名'
    return false
  }

  if (!registerForm.value.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registerForm.value.email)) {
    errors.value.email = '邮箱格式不正确'
    return false
  }

  if (!registerForm.value.phone || !/^1[3-9]\d{9}$/.test(registerForm.value.phone)) {
    errors.value.phone = '手机号格式不正确'
    return false
  }

  if (!registerForm.value.schoolId) {
    errors.value.school = '请选择学校'
    return false
  }

  if (!registerForm.value.studentId) {
    errors.value.studentId = '请输入学号'
    return false
  }

  if (!registerForm.value.password || registerForm.value.password.length < 8) {
    errors.value.regPassword = '密码至少8位'
    return false
  }

  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    errors.value.confirmPassword = '两次输入的密码不一致'
    return false
  }

  if (!agreeTerms.value) {
    uni.showToast({
      title: '请同意用户协议和隐私政策',
      icon: 'none'
    })
    return false
  }

  return true
}

// 处理登录
const handleLogin = async () => {
  if (!validateLoginForm()) return

  isLoading.value = true

  try {
    const res = await login({
      username: loginForm.value.username,
      password: loginForm.value.password
    })

    // 保存登录信息
    userStore.login({
      token: res.token,
      refreshToken: res.refreshToken,
      userInfo: {
        userId: res.userId,
        username: res.username,
        nickname: res.nickname,
        avatar: '',
        schoolId: 0,
        points: 0,
        role: res.role
      }
    })

    // 成功动画
    isSuccess.value = true

    setTimeout(() => {
      isLoading.value = false
      isSuccess.value = false
      emit('success')
      close()
    }, 600)

    uni.showToast({
      title: '登录成功',
      icon: 'success'
    })
  } catch (error: any) {
    isLoading.value = false
    errors.value.password = error.message || '登录失败'
  }
}

// 处理注册
const handleRegister = async () => {
  if (!validateRegisterForm()) return

  isLoading.value = true

  try {
    const res = await register({
      username: registerForm.value.username,
      password: registerForm.value.password,
      email: registerForm.value.email,
      phone: registerForm.value.phone,
      nickname: registerForm.value.nickname,
      schoolId: registerForm.value.schoolId,
      studentId: registerForm.value.studentId
    })

    // 保存登录信息
    userStore.login({
      token: res.token,
      refreshToken: res.refreshToken,
      userInfo: {
        userId: res.userId,
        username: res.username,
        nickname: registerForm.value.nickname,
        avatar: '',
        schoolId: registerForm.value.schoolId,
        points: 100, // 注册奖励
        role: 'user'
      }
    })

    // 成功动画
    isSuccess.value = true

    setTimeout(() => {
      isLoading.value = false
      isSuccess.value = false
      emit('success')
      close()
    }, 600)

    uni.showToast({
      title: '注册成功',
      icon: 'success'
    })
  } catch (error: any) {
    isLoading.value = false
    uni.showToast({
      title: error.message || '注册失败',
      icon: 'none'
    })
  }
}

// 处理遮罩点击
const handleOverlayClick = () => {
  close()
}

// 关闭弹窗
const close = () => {
  isClosing.value = true
  setTimeout(() => {
    isClosing.value = false
    emit('update:visible', false)
  }, 150)
}
</script>

<style lang="scss" scoped>
// 遮罩层
.auth-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(10, 15, 30, 0.38);
  backdrop-filter: blur(6px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  animation: fadeIn 200ms ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

// 模态框
.auth-modal {
  width: 90%;
  max-width: 480px;
  background: #FFFFFF;
  border-radius: 16px;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.18);
  padding: 48rpx;
  position: relative;
  transform: scale(0.98);
  opacity: 0;
  transition: all 200ms ease-out;

  &.modal-enter {
    transform: scale(1);
    opacity: 1;
  }
}

// 关闭按钮
.close-btn {
  position: absolute;
  top: 24rpx;
  right: 24rpx;
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: transform 150ms ease-out;

  &.close-rotate {
    transform: rotate(90deg);
  }

  .iconfont {
    font-size: 48rpx;
    color: #64748B;
  }
}

// 头部
.header {
  text-align: center;
  margin-bottom: 48rpx;

  .logo-box {
    margin-bottom: 24rpx;

    .logo {
      width: 120rpx;
      height: 120rpx;
    }
  }

  .title {
    display: block;
    font-size: 36rpx;
    font-weight: 600;
    color: #0F172A;
    margin-bottom: 16rpx;
  }

  .subtitle {
    display: block;
    font-size: 28rpx;
    color: #64748B;
  }
}

// Tab 切换
.tab-container {
  display: flex;
  position: relative;
  background: #F5F7FA;
  border-radius: 18rpx;
  padding: 6rpx;
  margin-bottom: 48rpx;

  .tab-item {
    flex: 1;
    height: 72rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28rpx;
    color: #64748B;
    cursor: pointer;
    position: relative;
    z-index: 2;
    transition: color 200ms;

    &.active {
      color: #2563EB;
      font-weight: 500;
    }
  }

  .tab-slider {
    position: absolute;
    left: 6rpx;
    top: 6rpx;
    width: calc(50% - 6rpx);
    height: 72rpx;
    background: #FFFFFF;
    border-radius: 12rpx;
    transition: transform 200ms ease-out;
    z-index: 1;
    box-shadow: 0 2px 8px rgba(37, 99, 235, 0.12);

    &.slide-right {
      transform: translateX(100%);
    }
  }
}

// 表单容器
.form-container {
  opacity: 1;
  transition: opacity 150ms;

  &.fade-in {
    opacity: 0;
  }
}

// 表单项
.form-item {
  margin-bottom: 32rpx;

  .input-wrapper {
    height: 88rpx;
    border: 2rpx solid #E5E7EB;
    border-radius: 20rpx;
    display: flex;
    align-items: center;
    padding: 0 32rpx;
    transition: all 200ms;

    &.focused {
      border-color: #2563EB;
      box-shadow: 0 0 0 6rpx rgba(37, 99, 235, 0.12);
    }

    &.error {
      border-color: #DC2626;
      animation: shake 300ms;
    }

    .iconfont {
      font-size: 32rpx;
      margin-right: 16rpx;
    }

    .input {
      flex: 1;
      font-size: 28rpx;
      color: #0F172A;

      &::placeholder {
        color: #94A3B8;
      }
    }

    .picker-view {
      flex: 1;
      font-size: 28rpx;
      color: #0F172A;
    }

    .eye-icon {
      font-size: 32rpx;
      cursor: pointer;
    }
  }

  .error-text {
    display: block;
    margin-top: 12rpx;
    margin-left: 32rpx;
    font-size: 24rpx;
    color: #DC2626;
  }

  .password-strength {
    display: flex;
    align-items: center;
    margin-top: 12rpx;

    .strength-bar {
      flex: 1;
      height: 6rpx;
      background: #E5E7EB;
      border-radius: 3rpx;
      margin-right: 16rpx;
      position: relative;
      overflow: hidden;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 0;
        height: 100%;
        transition: all 300ms;
      }

      &.weak::before {
        width: 33%;
        background: #DC2626;
      }

      &.medium::before {
        width: 66%;
        background: #FF7D00;
      }

      &.strong::before {
        width: 100%;
        background: #00B42A;
      }
    }

    .strength-text {
      font-size: 24rpx;
      color: #64748B;
    }
  }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-4rpx); }
  75% { transform: translateX(4rpx); }
}

// 表单选项
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;

  .checkbox-label {
    display: flex;
    align-items: center;
    font-size: 28rpx;
    color: #64748B;

    checkbox {
      margin-right: 12rpx;
    }
  }

  .link-text {
    font-size: 28rpx;
    color: #2563EB;
    cursor: pointer;
  }
}

// 安全提示
.security-tip {
  display: flex;
  align-items: center;
  padding: 16rpx 24rpx;
  background: #FFF7E6;
  border-radius: 12rpx;
  margin-bottom: 32rpx;

  .tip-icon {
    font-size: 28rpx;
    margin-right: 12rpx;
  }

  .tip-text {
    font-size: 24rpx;
    color: #FF7D00;
  }
}

// 协议
.agreement {
  display: flex;
  align-items: flex-start;
  margin-bottom: 32rpx;

  checkbox {
    margin-right: 12rpx;
    margin-top: 4rpx;
  }

  .agreement-text {
    font-size: 26rpx;
    color: #64748B;
    line-height: 1.6;

    .link-text {
      color: #2563EB;
    }
  }
}

// 主按钮
.primary-btn {
  width: 100%;
  height: 88rpx;
  background: #2563EB;
  border-radius: 24rpx;
  border: none;
  font-size: 32rpx;
  font-weight: 500;
  color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 200ms;

  &:active {
    transform: translateY(2rpx);
  }

  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }

  &.loading {
    background: #94A3B8;
  }

  &.success {
    background: #00B42A;
  }

  .loading-spinner {
    width: 32rpx;
    height: 32rpx;
    border: 4rpx solid rgba(255, 255, 255, 0.3);
    border-top-color: #FFFFFF;
    border-radius: 50%;
    animation: spin 600ms linear infinite;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

// 底部
.footer {
  text-align: center;
  margin-top: 32rpx;
  padding-top: 32rpx;
  border-top: 2rpx solid #E5E7EB;

  .footer-text {
    font-size: 28rpx;
    color: #64748B;

    .link-text {
      color: #2563EB;
      cursor: pointer;
    }
  }
}
</style>
