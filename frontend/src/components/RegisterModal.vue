<template>
  <view v-if="visible" class="register-modal-mask" @tap="handleMaskClick">
    <view class="register-modal-container" :class="{ 'modal-show': showAnimation }" @tap.stop>
      <!-- 关闭按钮 -->
      <view class="close-btn" @tap.stop="handleClose">
        <Icon name="x" :size="18" class="close-icon" />
      </view>

      <!-- 品牌头部 -->
      <view class="modal-header">
        <view class="brand-logo-wrapper">
          <image class="brand-logo-image" src="/static/logo.png" mode="aspectFit"></image>
        </view>
        <view class="slogan-container">
          <text class="welcome-slogan">欢迎加入 CampusLink</text>
          <text class="welcome-subtitle">让学习与分享更高效</text>
        </view>
        <view class="brand-divider"></view>
      </view>

      <!-- 注册表单 -->
      <view class="register-form">
        <!-- 邮箱或手机号 -->
        <view class="input-group" :class="{ 'input-focus': accountFocused, 'input-error': accountError }">
          <view class="input-icon-wrapper">
            <Icon name="mail" :size="16" class="field-icon" />
          </view>
          <input
            class="form-input"
            type="text"
            placeholder="邮箱或手机号"
            v-model="formData.account"
            @focus="accountFocused = true"
            @blur="handleAccountBlur"
          />
        </view>

        <!-- 用户名 -->
        <view class="input-group" :class="{ 'input-focus': usernameFocused, 'input-error': usernameError }">
          <view class="input-icon-wrapper">
            <Icon name="user" :size="16" class="field-icon" />
          </view>
          <input
            class="form-input"
            type="text"
            placeholder="用户名（3-20字符，字母数字下划线）"
            v-model="formData.username"
            @focus="usernameFocused = true"
            @blur="handleUsernameBlur"
          />
          <view v-if="usernameChecking" class="status-spin" />
          <Icon v-else-if="usernameAvailable === true" name="check" :size="16" class="success-icon" />
          <Icon v-else-if="usernameAvailable === false" name="x" :size="16" class="error-icon" />
        </view>
        <view v-if="usernameHint" class="hint-text" :class="{ 'hint-error': usernameAvailable === false }">
          {{ usernameHint }}
        </view>

        <!-- 昵称 -->
        <view class="input-group" :class="{ 'input-focus': nicknameFocused, 'input-error': nicknameError }">
          <view class="input-icon-wrapper">
            <Icon name="smile" :size="16" class="field-icon" />
          </view>
          <input
            class="form-input"
            type="text"
            placeholder="昵称（选填，默认与用户名相同）"
            v-model="formData.nickname"
            @focus="nicknameFocused = true"
            @blur="nicknameFocused = false"
          />
        </view>

        <!-- 所属学校（选填） -->
        <view class="school-picker-wrapper">
          <view
            class="input-group"
            :class="{ 'input-focus': showSchoolDropdown }"
            @tap="showSchoolDropdown = !showSchoolDropdown"
          >
            <view class="input-icon-wrapper">
              <Icon name="building" :size="16" class="field-icon" />
            </view>
            <text class="form-input school-display" :class="{ placeholder: !formData.schoolName }">
              {{ formData.schoolName || '所在学校（选填）' }}
            </text>
            <Icon name="chevron-down" :size="16" class="dropdown-arrow" :class="{ open: showSchoolDropdown }" />
          </view>
          <!-- 学校下拉列表 -->
          <view v-if="showSchoolDropdown" class="school-dropdown">
            <view class="school-search-row">
              <input
                class="school-search-input"
                placeholder="搜索学校..."
                v-model="schoolSearchKeyword"
                @tap.stop
              />
            </view>
            <scroll-view class="school-list" scroll-y>
              <view v-if="filteredSchools.length === 0" class="school-empty">暂无匹配</view>
              <view
                v-for="s in filteredSchools"
                :key="s.schoolId"
                class="school-item"
                :class="{ active: formData.schoolId === s.schoolId }"
                @tap.stop="selectSchool(s)"
              >
                <text class="school-item-name">{{ s.schoolName }}</text>
                <Icon v-if="formData.schoolId === s.schoolId" name="check" :size="14" class="school-item-check" />
              </view>
            </scroll-view>
          </view>
        </view>

        <!-- 设置密码 -->
        <view class="input-group" :class="{ 'input-focus': passwordFocused, 'input-error': passwordError }">
          <view class="input-icon-wrapper">
            <Icon name="lock" :size="16" class="field-icon lock-icon" />
          </view>
          <input
            class="form-input"
            :type="showPassword ? 'text' : 'password'"
            placeholder="设置密码"
            v-model="formData.password"
            @focus="passwordFocused = true"
            @blur="passwordFocused = false"
            @input="handlePasswordInput"
          />
          <view class="password-toggle" @tap="showPassword = !showPassword">
            <Icon :name="showPassword ? 'eye' : 'eye-off'" :size="16" />
          </view>
        </view>

        <!-- 密码强度条 -->
        <view v-if="formData.password" class="password-strength-bar">
          <view class="strength-bar-container">
            <view
              class="strength-bar-fill"
              :class="[`strength-${passwordStrength.level}`]"
              :style="{ width: passwordStrength.percent + '%' }"
            ></view>
          </view>
          <text class="strength-text" :class="[`strength-${passwordStrength.level}`]">
            {{ passwordStrength.text }}
          </text>
        </view>

        <!-- 确认密码 -->
        <view class="input-group" :class="{ 'input-focus': confirmPasswordFocused, 'input-error': confirmPasswordError }">
          <view class="input-icon-wrapper">
            <Icon name="lock" :size="16" class="field-icon" />
          </view>
          <input
            class="form-input"
            :type="showConfirmPassword ? 'text' : 'password'"
            placeholder="确认密码"
            v-model="formData.confirmPassword"
            @focus="confirmPasswordFocused = true"
            @blur="confirmPasswordFocused = false"
          />
          <view class="password-toggle" @tap="showConfirmPassword = !showConfirmPassword">
            <Icon :name="showConfirmPassword ? 'eye' : 'eye-off'" :size="16" />
          </view>
        </view>

        <!-- 验证码 -->
        <view class="verification-group">
          <view class="input-group verification-input" :class="{ 'input-focus': codeFocused, 'input-error': codeError }">
            <view class="input-icon-wrapper">
              <Icon name="shield-check" :size="16" class="field-icon" />
            </view>
            <input
              class="form-input"
              type="text"
              placeholder="验证码"
              v-model="formData.code"
              @focus="codeFocused = true"
              @blur="codeFocused = false"
            />
          </view>
          <button
            class="code-btn"
            :class="{ 'code-btn-disabled': codeCountdown > 0 || !canSendCode }"
            :disabled="codeCountdown > 0 || !canSendCode"
            @tap="handleSendCode"
          >
            {{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}
          </button>
        </view>

        <!-- 注册按钮 -->
        <button
          class="register-btn"
          :class="{ 'btn-loading': loading, 'btn-success': registerSuccess }"
          @tap="handleRegister"
        >
          <text v-if="!loading && !registerSuccess">注册并登录</text>
          <text v-else-if="loading" class="loading-text">注册中...</text>
          <text v-else class="success-text">注册成功 ✓</text>
        </button>

        <!-- 已有账号引导 -->
        <view class="login-tip">
          <text class="tip-text">已有账号？</text>
          <text class="login-link" @tap="handleGoToLogin">立即登录 →</text>
        </view>

        <!-- 用户协议 -->
        <view class="agreement-tip">
          <text class="agreement-text">注册即表示同意</text>
          <text class="agreement-link">《用户协议》</text>
          <text class="agreement-text">和</text>
          <text class="agreement-link">《隐私政策》</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch, computed, onUnmounted } from 'vue'
import { register, sendCode, checkUsernameAvailable, type RegisterRequest, type AuthResponse, type SendCodeRequest } from '@/services/auth'
import config from '@/config'
import { getAllSchools, type SchoolItem } from '@/services/school'
import Icon from '@/components/icons/index.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'register-success', 'go-to-login'])

// 表单数据
const formData = ref({
  account: '',
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  code: '',
  schoolId: 0,
  schoolName: ''
})

// 学校选择
const schools = ref<SchoolItem[]>([])
const showSchoolDropdown = ref(false)
const schoolSearchKeyword = ref('')

const filteredSchools = computed(() => {
  const kw = schoolSearchKeyword.value.trim()
  if (!kw) return schools.value
  return schools.value.filter(s => s.schoolName.includes(kw))
})

const loadSchools = async () => {
  if (schools.value.length > 0) return
  try {
    schools.value = await getAllSchools()
  } catch (e) { /* 加载失败不阻塞注册 */ }
}

const selectSchool = (s: SchoolItem) => {
  formData.value.schoolId = s.schoolId
  formData.value.schoolName = s.schoolName
  showSchoolDropdown.value = false
  schoolSearchKeyword.value = ''
}

// 状态管理
const showAnimation = ref(false)
const accountFocused = ref(false)
const usernameFocused = ref(false)
const nicknameFocused = ref(false)
const passwordFocused = ref(false)
const confirmPasswordFocused = ref(false)
const codeFocused = ref(false)
const accountError = ref(false)
const usernameError = ref(false)
const nicknameError = ref(false)
const passwordError = ref(false)
const confirmPasswordError = ref(false)
const codeError = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const loading = ref(false)
const registerSuccess = ref(false)
const codeCountdown = ref(0)
const usernameChecking = ref(false)
const usernameAvailable = ref<boolean | null>(null)
const usernameHint = ref('')
let countdownTimer: number | null = null

// 密码强度计算
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

// 是否可以发送验证码
const canSendCode = computed(() => {
  const account = formData.value.account
  // 检查是否是邮箱
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  // 检查是否是手机号
  const phoneRegex = /^1[3-9]\d{9}$/
  return emailRegex.test(account) || phoneRegex.test(account)
})

// 监听弹窗显示状态
watch(() => props.visible, (newVal) => {
  if (newVal) {
    setTimeout(() => { showAnimation.value = true }, 10)
    loadSchools()
    // #ifdef H5
    if (typeof document !== 'undefined') {
      document.body.style.overflow = 'hidden'
    }
    // #endif
  } else {
    showAnimation.value = false
    showSchoolDropdown.value = false
    resetForm()
    // #ifdef H5
    if (typeof document !== 'undefined') {
      document.body.style.overflow = ''
    }
    // #endif
  }
})

// 企业级Toast通用方法（与登录页面保持一致）
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

// 生成用户名
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

// 账号失焦处理 - 自动填充用户名和昵称
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
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      const isEmail = emailRegex.test(formData.value.account)
      if (isEmail) {
        formData.value.nickname = formData.value.account.split('@')[0].substring(0, 20)
      } else {
        formData.value.nickname = formData.value.account.substring(0, 20)
      }
    }
  }
}

// 用户名失焦处理 - 检查用户名是否可用
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

  usernameChecking.value = true
  try {
    const available = await checkUsernameAvailable(username)
    if (available) {
      usernameError.value = false
      usernameAvailable.value = true
      usernameHint.value = '用户名可用 ✓'
    } else {
      usernameError.value = true
      usernameAvailable.value = false
      usernameHint.value = '用户名已被占用，请换一个'
    }
  } catch {
    // 网络异常时静默降级，不阻塞注册流程
    usernameError.value = false
    usernameAvailable.value = true
    usernameHint.value = ''
  } finally {
    usernameChecking.value = false
  }
}

// 重置表单
const resetForm = () => {
  formData.value = {
    account: '',
    username: '',
    nickname: '',
    password: '',
    confirmPassword: '',
    code: '',
    schoolId: 0,
    schoolName: ''
  }
  schoolSearchKeyword.value = ''
  accountError.value = false
  usernameError.value = false
  nicknameError.value = false
  passwordError.value = false
  confirmPasswordError.value = false
  codeError.value = false
  registerSuccess.value = false
  usernameAvailable.value = null
  usernameHint.value = ''
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
    codeCountdown.value = 0
  }
}

// 关闭弹窗
const handleClose = () => {
  showAnimation.value = false
  setTimeout(() => {
    emit('update:visible', false)
  }, 300)
}

// 点击遮罩关闭
const handleMaskClick = () => {
  handleClose()
}

// 密码输入处理
const handlePasswordInput = () => {
  // 实时更新密码强度
}

// 发送验证码
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
    let errorMessage = '验证码发送失败，稍后再试吧 ~'
    let errorType: 'error' | 'warning' = 'error'

    if (error.message) {
      const msg = error.message
      if (msg.includes('频繁') || msg.includes('次数')) {
        errorMessage = '发送太频繁啦，休息一下再试吧 ~'
        errorType = 'warning'
      } else if (msg.includes('不存在') || msg.includes('无效')) {
        errorMessage = '这个账号好像有问题，检查一下格式？'
        errorType = 'warning'
      } else if (msg.includes('网络')) {
        errorMessage = '网络好像断了，检查一下连接吧 ~'
        errorType = 'warning'
      } else {
        errorMessage = msg
      }
    }

    showToast(errorMessage, errorType)
  }
}

// 表单验证（品牌化友好提示）
const validateForm = () => {
  accountError.value = !formData.value.account
  usernameError.value = !formData.value.username || formData.value.username.length < 3 || formData.value.username.length > 20
  passwordError.value = !formData.value.password || formData.value.password.length < 6
  confirmPasswordError.value = formData.value.password !== formData.value.confirmPassword
  codeError.value = !formData.value.code || formData.value.code.length !== 6

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

  if (codeError.value) {
    showToast('别忘了填写验证码哦 ~', 'info')
    return false
  }

  return true
}

// 注册处理
const handleRegister = async () => {
  if (!validateForm()) return

  loading.value = true

  try {
    // 判断是邮箱还是手机号
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    const isEmail = emailRegex.test(formData.value.account)

    const registerData: RegisterRequest = {
      username: formData.value.username.trim(), // 使用用户填写的用户名
      password: formData.value.password,
      email: isEmail ? formData.value.account : undefined,
      phone: !isEmail ? formData.value.account : undefined,
      nickname: formData.value.nickname.trim() || formData.value.username.trim(), // 使用用户填写的昵称，如果为空则使用用户名
      schoolId: formData.value.schoolId || undefined,
      code: formData.value.code
    }

    const response: AuthResponse = await register(registerData)

    // 存储Token（自动登录）
    uni.setStorageSync(config.tokenKey, response.token)
    uni.setStorageSync(config.refreshTokenKey, response.refreshToken)
    uni.setStorageSync(config.userInfoKey, JSON.stringify(response.user))

    // 显示成功状态
    registerSuccess.value = true

    // 移除这里的toast,由父组件统一处理欢迎提示
    emit('register-success', response)
    handleClose()
  } catch (error: any) {
    // 根据不同错误类型显示友好提示（品牌化语气）
    let errorMessage = '注册遇到问题了，稍后再试试吧 ~'
    let errorType: 'error' | 'warning' = 'error'

    if (error.message) {
      const msg = error.message

      if (msg.includes('用户名已存在') || msg.includes('已被注册') || msg.includes('已存在')) {
        errorMessage = '这个账号已经被注册过啦，要不试试登录？'
        errorType = 'warning'
      } else if (msg.includes('邮箱') && msg.includes('已')) {
        errorMessage = '这个邮箱已经注册过了哦 ~'
        errorType = 'warning'
      } else if (msg.includes('手机号') && msg.includes('已')) {
        errorMessage = '这个手机号已经注册过了哦 ~'
        errorType = 'warning'
      } else if (msg.includes('验证码') && (msg.includes('错误') || msg.includes('无效'))) {
        errorMessage = '验证码好像不对，再检查一下？'
        errorType = 'warning'
      } else if (msg.includes('验证码') && msg.includes('过期')) {
        errorMessage = '验证码过期啦，重新获取一个吧 ~'
        errorType = 'warning'
      } else if (msg.includes('格式') || msg.includes('不正确')) {
        errorMessage = '信息格式好像有问题，检查一下吧 ~'
        errorType = 'warning'
      } else if (msg.includes('网络') || msg.includes('Network')) {
        errorMessage = '网络好像断了，检查一下连接吧 ~'
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

// 切换到登录
const handleGoToLogin = () => {
  emit('go-to-login')
  handleClose()
}

// 组件卸载时清理定时器并恢复滚动
onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
  // #ifdef H5
  if (typeof document !== 'undefined') {
    document.body.style.overflow = ''
  }
  // #endif
})
</script>

<style lang="scss" scoped>
/* 继承登录弹窗的所有样式 */

/* ========== 遮罩层 ========== */
.register-modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
  background: radial-gradient(
    ellipse at center,
    rgba(56, 189, 248, 0.08) 0%,
    rgba(37, 99, 235, 0.12) 30%,
    rgba(15, 23, 42, 0.75) 100%
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
.register-modal-container {
  position: relative;
  width: 840rpx;
  max-height: 92vh;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(36rpx);
  border-radius: 52rpx;
  box-shadow: 0 16rpx 64rpx rgba(37, 99, 235, 0.15),
              0 0 2rpx rgba(37, 99, 235, 0.1);
  padding: 44rpx 48rpx 40rpx;
  box-sizing: border-box;
  overflow-y: auto;
  transform: scale(0.95);
  opacity: 0;
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);

  &.modal-show {
    transform: scale(1);
    opacity: 1;
  }

  &::-webkit-scrollbar {
    display: none;
  }
  scrollbar-width: none;
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
  z-index: 10;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    background: rgba(239, 246, 255, 0.9);
    transform: scale(1.1) rotate(45deg);
    box-shadow: 0 4rpx 12rpx rgba(37, 99, 235, 0.15);
  }

  &:active {
    transform: scale(0.95) rotate(45deg);
  }

  .close-icon {
    color: #64748B;
    transition: color 0.2s ease;
    pointer-events: none;
  }

  &:hover .close-icon {
    color: #2563EB;
  }
}

/* ========== 品牌头部 ========== */
.modal-header {
  text-align: center;
  margin-bottom: 40rpx;
}

.brand-logo-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20rpx;
  filter: drop-shadow(0 6rpx 24rpx rgba(37, 99, 235, 0.12));
}

.brand-logo-image {
  width: 304rpx;
  height: 120rpx;
  object-fit: contain;
}

.slogan-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
  margin-bottom: 16rpx;
}

.welcome-slogan {
  font-size: 36rpx;
  color: #1F2937;
  font-weight: 600;
  letter-spacing: 1rpx;
  line-height: 1.5;
}

.welcome-subtitle {
  font-size: 26rpx;
  color: #9CA3AF;
  font-weight: 400;
  letter-spacing: 0.8rpx;
  line-height: 1.5;
}

.brand-divider {
  width: 120rpx;
  height: 2rpx;
  background: linear-gradient(90deg, transparent 0%, rgba(100, 116, 139, 0.2) 50%, transparent 100%);
  margin: 0 auto 8rpx;
}

/* ========== 注册表单 ========== */
.register-form {
  width: 100%;
}

/* 输入框组 */
.input-group {
  position: relative;
  display: flex;
  align-items: center;
  height: 84rpx;
  background: rgba(255, 255, 255, 0.7);
  border: 2rpx solid rgba(203, 213, 225, 0.5);
  border-radius: 22rpx;
  padding: 0 28rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &.input-focus {
    background: rgba(255, 255, 255, 0.98);
    border: 2rpx solid transparent;
    background-image:
      linear-gradient(rgba(255, 255, 255, 0.98), rgba(255, 255, 255, 0.98)),
      linear-gradient(90deg, #2563EB, #06B6D4);
    background-origin: border-box;
    background-clip: padding-box, border-box;
    box-shadow: 0 0 0 6rpx rgba(59, 130, 246, 0.18),
                0 0 28rpx rgba(37, 99, 235, 0.2),
                0 4rpx 16rpx rgba(37, 99, 235, 0.15);
  }

  &.input-error {
    border-color: rgba(239, 68, 68, 0.6);
    background: rgba(254, 242, 242, 0.5);
    box-shadow: 0 0 0 4rpx rgba(239, 68, 68, 0.12),
                inset 0 2rpx 8rpx rgba(239, 68, 68, 0.08);
    animation: shake 0.4s cubic-bezier(0.36, 0.07, 0.19, 0.97);
  }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-6rpx); }
  75% { transform: translateX(6rpx); }
}

.input-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
  position: relative;
}

.field-icon {
  color: #94A3B8;
  transition: color 0.25s ease, transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
}

.input-group.input-focus .field-icon {
  color: #2563EB;
  transform: scale(1.08);
}

.input-group.input-focus .lock-icon {
  animation: lockBounce 0.4s cubic-bezier(0.36, 0, 0.66, -0.56);
}

@keyframes lockBounce {
  0%   { transform: translateY(0) scale(1); }
  50%  { transform: translateY(-4rpx) scale(1.08); }
  100% { transform: translateY(0) scale(1.08); }
}

.form-input {
  flex: 1;
  height: 100%;
  font-size: 28rpx;
  color: #374151;
  font-weight: 500;
  border: none;
  background: transparent;

  &::placeholder {
    color: #9CA3AF;
    font-size: 28rpx;
    font-weight: 400;
  }
}

.password-toggle {
  display: flex;
  align-items: center;
  color: #94A3B8;
  cursor: pointer;
  padding: 4rpx;
  border-radius: 8rpx;
  transition: color 0.2s ease, transform 0.2s ease;

  &:hover {
    color: #2563EB;
    transform: scale(1.08);
  }

  &:active {
    transform: scale(0.95);
  }
}

/* ========== 密码强度条 ========== */
.password-strength-bar {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 16rpx;
  padding: 0 8rpx;
}

.strength-bar-container {
  flex: 1;
  height: 8rpx;
  background: rgba(203, 213, 225, 0.3);
  border-radius: 4rpx;
  overflow: hidden;
}

.strength-bar-fill {
  height: 100%;
  border-radius: 4rpx;
  transition: all 0.3s ease;

  &.strength-weak {
    background: linear-gradient(90deg, #EF4444 0%, #F87171 100%);
  }

  &.strength-medium {
    background: linear-gradient(90deg, #F59E0B 0%, #FBBF24 100%);
  }

  &.strength-strong {
    background: linear-gradient(90deg, #10B981 0%, #34D399 100%);
  }
}

.strength-text {
  font-size: 24rpx;
  font-weight: 600;
  min-width: 40rpx;

  &.strength-weak {
    color: #EF4444;
  }

  &.strength-medium {
    color: #F59E0B;
  }

  &.strength-strong {
    color: #10B981;
  }
}

/* ========== 验证码组 ========== */
.verification-group {
  display: flex;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.verification-input {
  flex: 1;
  margin-bottom: 0;
}

.code-btn {
  width: 200rpx;
  height: 84rpx;
  background: linear-gradient(90deg, #2563EB 0%, #3B82F6 100%);
  border: none;
  border-radius: 22rpx;
  color: #fff;
  font-size: 26rpx;
  font-weight: 600;
  box-shadow: 0 4rpx 16rpx rgba(37, 99, 235, 0.25);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:not(.code-btn-disabled):hover {
    background: linear-gradient(90deg, #1D4ED8 0%, #3B82F6 100%);
    filter: brightness(1.1);
    box-shadow: 0 6rpx 20rpx rgba(37, 99, 235, 0.35);
    transform: translateY(-2rpx);
  }

  &:active:not(.code-btn-disabled) {
    transform: translateY(0) scale(0.98);
  }

  &.code-btn-disabled {
    background: linear-gradient(90deg, #94A3B8 0%, #CBD5E1 100%);
    opacity: 0.6;
    cursor: not-allowed;
    box-shadow: none;
  }
}

/* ========== 注册按钮 ========== */
.register-btn {
  width: 100%;
  height: 84rpx;
  background: linear-gradient(135deg, #2563EB 0%, #38BDF8 100%);
  border: none;
  border-radius: 22rpx;
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
  text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.1);
  box-shadow: 0 8rpx 28rpx rgba(37, 99, 235, 0.35),
              0 4rpx 12rpx rgba(37, 99, 235, 0.2);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  margin-bottom: 28rpx;

  /* 中心光晕效果 */
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

  &:hover:not(.btn-loading):not(.btn-success) {
    background: linear-gradient(135deg, #1D4ED8 0%, #38BDF8 100%);
    filter: brightness(1.1);
    box-shadow: 0 12rpx 40rpx rgba(37, 99, 235, 0.5),
                0 4rpx 16rpx rgba(37, 99, 235, 0.3),
                0 0 0 8rpx rgba(56, 189, 248, 0.2);
    transform: translateY(-4rpx) scale(1.02);

    &::before {
      opacity: 1;
    }
  }

  &:active:not(.btn-loading):not(.btn-success) {
    background: linear-gradient(135deg, #1E40AF 0%, #2563EB 100%);
    filter: brightness(0.95);
    transform: translateY(-2rpx) scale(0.98);
  }

  &.btn-loading {
    opacity: 0.7;
    cursor: not-allowed;
  }

  &.btn-success {
    background: linear-gradient(135deg, #10B981 0%, #34D399 100%);
    box-shadow: 0 8rpx 28rpx rgba(16, 185, 129, 0.35),
                0 4rpx 12rpx rgba(16, 185, 129, 0.2);
  }

  .loading-text {
    animation: pulse 1.5s cubic-bezier(0.4, 0, 0.6, 1) infinite;
  }

  .success-text {
    animation: scaleIn 0.3s ease;
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

@keyframes scaleIn {
  0% {
    transform: scale(0.8);
    opacity: 0;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

/* ========== 登录引导 ========== */
.login-tip {
  text-align: center;
  font-size: 26rpx;
  line-height: 1.7;
  letter-spacing: 0.5rpx;
  margin-bottom: 20rpx;

  .tip-text {
    color: #6B7280;
    font-weight: 500;
    margin-right: 8rpx;
  }

  .login-link {
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

/* ========== 用户协议 ========== */
.agreement-tip {
  text-align: center;
  font-size: 24rpx;
  line-height: 1.6;
  color: #9CA3AF;

  .agreement-text {
    color: #9CA3AF;
  }

  .agreement-link {
    color: #2563EB;
    text-decoration: underline;
    cursor: pointer;
    transition: color 0.2s ease;

    &:hover {
      color: #1D4ED8;
    }
  }
}

/* ========== 用户名检查图标 ========== */
.status-spin {
  width: 28rpx;
  height: 28rpx;
  border: 2rpx solid rgba(37, 99, 235, 0.2);
  border-top-color: #2563EB;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  flex-shrink: 0;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.success-icon {
  color: #10B981;
  flex-shrink: 0;
  animation: scaleIn 0.3s ease;
}

.error-icon {
  color: #EF4444;
  flex-shrink: 0;
  animation: scaleIn 0.3s ease;
}

/* ========== 提示文本 ========== */
.hint-text {
  font-size: 24rpx;
  color: #10B981;
  padding: 0 8rpx;
  margin-top: -8rpx;
  margin-bottom: 8rpx;
  animation: fadeIn 0.3s ease;

  &.hint-error {
    color: #EF4444;
  }
}

/* ========== 学校选择器 ========== */
.school-picker-wrapper {
  position: relative;
  margin-bottom: 16rpx;

  .input-group {
    margin-bottom: 0;
    cursor: pointer;
    user-select: none;
  }
}

.school-display {
  line-height: 84rpx;
  cursor: pointer;
  &.placeholder { color: #9CA3AF; }
}

.dropdown-arrow {
  color: #9CA3AF;
  transition: transform 0.2s ease;
  flex-shrink: 0;

  &.open { transform: rotate(180deg); }
}

.school-dropdown {
  position: absolute;
  top: calc(100% + 8rpx);
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.98);
  border: 2rpx solid rgba(37, 99, 235, 0.15);
  border-radius: 20rpx;
  box-shadow: 0 12rpx 40rpx rgba(37, 99, 235, 0.15), 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  z-index: 100;
  overflow: hidden;
  animation: dropdownIn 0.18s ease;
}

@keyframes dropdownIn {
  from { opacity: 0; transform: translateY(-8rpx); }
  to   { opacity: 1; transform: translateY(0); }
}

.school-search-row {
  padding: 16rpx 20rpx 12rpx;
  border-bottom: 1rpx solid rgba(203, 213, 225, 0.4);

  .school-search-input {
    width: 100%;
    height: 64rpx;
    padding: 0 16rpx;
    font-size: 26rpx;
    color: #374151;
    background: #F8FAFF;
    border: 2rpx solid #E8EEFF;
    border-radius: 14rpx;
    box-sizing: border-box;

    &::placeholder { color: #9CA3AF; }
  }
}

.school-list {
  max-height: 360rpx;
  padding: 8rpx 0;
}

.school-empty {
  text-align: center;
  font-size: 26rpx;
  color: #9CA3AF;
  padding: 40rpx 0;
}

.school-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22rpx 28rpx;
  cursor: pointer;
  transition: background 0.15s;

  &:hover { background: #F8FAFF; }

  &.active {
    background: #EEF2FF;
    .school-item-name { color: #2563EB; font-weight: 600; }
  }

  .school-item-name {
    font-size: 28rpx;
    color: #374151;
    flex: 1;
  }

  .school-item-check {
    color: #2563EB;
    flex-shrink: 0;
    margin-left: 12rpx;
  }
}
</style>
