<template>
  <view class="change-password-page">
    <view class="container">
      <!-- 表单卡片 -->
      <view class="form-card">
        <!-- 旧密码 -->
        <view class="form-group">
          <text class="form-label">🔑 旧密码</text>
          <view class="input-wrapper">
            <input
              v-model="formData.oldPassword"
              :type="showOldPassword ? 'text' : 'password'"
              class="form-input"
              placeholder="请输入旧密码"
              @blur="validateField('oldPassword')"
            />
            <text
              class="eye-icon"
              @click="togglePasswordVisibility('old')"
            >
              {{ showOldPassword ? '👁' : '👁‍🗨' }}
            </text>
          </view>
          <text v-if="errors.oldPassword" class="error-text">{{ errors.oldPassword }}</text>
        </view>

        <!-- 新密码 -->
        <view class="form-group">
          <text class="form-label">🆕 新密码</text>
          <view class="input-wrapper">
            <input
              v-model="formData.newPassword"
              :type="showNewPassword ? 'text' : 'password'"
              class="form-input"
              placeholder="6-20位，含大小写字母和数字"
              @input="onNewPasswordInput"
              @blur="validateField('newPassword')"
            />
            <text
              class="eye-icon"
              @click="togglePasswordVisibility('new')"
            >
              {{ showNewPassword ? '👁' : '👁‍🗨' }}
            </text>
          </view>
          <!-- 密码强度指示器 -->
          <view v-if="formData.newPassword" class="strength-indicator">
            <text class="strength-label">强度: </text>
            <view class="strength-bar">
              <view
                class="strength-fill"
                :class="`strength-${passwordStrength}`"
                :style="{ width: getStrengthWidth() }"
              />
            </view>
            <text class="strength-text" :class="`strength-${passwordStrength}`">
              {{ getStrengthText() }}
            </text>
          </view>
          <text v-if="errors.newPassword" class="error-text">{{ errors.newPassword }}</text>
        </view>

        <!-- 确认新密码 -->
        <view class="form-group">
          <text class="form-label">✅ 确认新密码</text>
          <view class="input-wrapper">
            <input
              v-model="formData.confirmPassword"
              :type="showConfirmPassword ? 'text' : 'password'"
              class="form-input"
              placeholder="请再次输入新密码"
              @blur="validateField('confirmPassword')"
            />
            <text
              class="eye-icon"
              @click="togglePasswordVisibility('confirm')"
            >
              {{ showConfirmPassword ? '👁' : '👁‍🗨' }}
            </text>
          </view>
          <text v-if="errors.confirmPassword" class="error-text">{{ errors.confirmPassword }}</text>
        </view>

        <!-- 提交按钮 -->
        <button
          class="submit-btn"
          :class="{ disabled: !isFormValid || submitting }"
          :disabled="!isFormValid || submitting"
          @click="handleSubmit"
        >
          {{ submitting ? '提交中...' : '确认修改' }}
        </button>
      </view>

      <!-- 密码要求说明 -->
      <view class="tips-card">
        <text class="tips-title">💡 密码要求：</text>
        <text class="tips-item">• 长度为 6-20 个字符</text>
        <text class="tips-item">• 必须包含大小写字母和数字</text>
        <text class="tips-item">• 新密码不能与旧密码相同</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onBeforeUnmount } from 'vue'
import { changePassword } from '@/services/user'
import { useUserStore } from '@/stores/user'

// Store
const userStore = useUserStore()

// 表单数据
const formData = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码显示状态
const showOldPassword = ref(false)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)

// 提交状态
const submitting = ref(false)

// 密码强度
type PasswordStrength = 'weak' | 'medium' | 'strong'
const passwordStrength = ref<PasswordStrength>('weak')

// 表单错误
const errors = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 用户是否修改过表单
const hasChanges = ref(false)

/**
 * 密码格式验证正则
 */
const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{6,20}$/

/**
 * 验证密码格式
 */
const validatePassword = (password: string): boolean => {
  return passwordRegex.test(password)
}

/**
 * 计算密码强度
 */
const calculatePasswordStrength = (password: string): PasswordStrength => {
  if (password.length < 6) return 'weak'

  const hasLower = /[a-z]/.test(password)
  const hasUpper = /[A-Z]/.test(password)
  const hasNumber = /\d/.test(password)

  const strength = [hasLower, hasUpper, hasNumber].filter(Boolean).length

  if (strength >= 3) return 'strong'
  if (strength >= 2) return 'medium'
  return 'weak'
}

/**
 * 获取强度宽度
 */
const getStrengthWidth = (): string => {
  const widthMap = {
    weak: '33%',
    medium: '66%',
    strong: '100%'
  }
  return widthMap[passwordStrength.value]
}

/**
 * 获取强度文本
 */
const getStrengthText = (): string => {
  const textMap = {
    weak: '弱',
    medium: '中',
    strong: '强'
  }
  return textMap[passwordStrength.value]
}

/**
 * 新密码输入时
 */
const onNewPasswordInput = () => {
  hasChanges.value = true
  passwordStrength.value = calculatePasswordStrength(formData.value.newPassword)
}

/**
 * 验证单个字段
 */
const validateField = (field: 'oldPassword' | 'newPassword' | 'confirmPassword') => {
  switch (field) {
    case 'oldPassword':
      if (!formData.value.oldPassword) {
        errors.value.oldPassword = '请输入旧密码'
      } else {
        errors.value.oldPassword = ''
      }
      break

    case 'newPassword':
      if (!formData.value.newPassword) {
        errors.value.newPassword = '请输入新密码'
      } else if (!validatePassword(formData.value.newPassword)) {
        errors.value.newPassword = '密码格式不正确（6-20位，含大小写字母和数字）'
      } else if (formData.value.newPassword === formData.value.oldPassword) {
        errors.value.newPassword = '新密码不能与旧密码相同'
      } else {
        errors.value.newPassword = ''
      }

      // 如果确认密码已填写，重新验证
      if (formData.value.confirmPassword) {
        validateField('confirmPassword')
      }
      break

    case 'confirmPassword':
      if (!formData.value.confirmPassword) {
        errors.value.confirmPassword = '请确认新密码'
      } else if (formData.value.confirmPassword !== formData.value.newPassword) {
        errors.value.confirmPassword = '两次输入的新密码不一致'
      } else {
        errors.value.confirmPassword = ''
      }
      break
  }
}

/**
 * 表单是否有效
 */
const isFormValid = computed(() => {
  return (
    formData.value.oldPassword &&
    formData.value.newPassword &&
    formData.value.confirmPassword &&
    !errors.value.oldPassword &&
    !errors.value.newPassword &&
    !errors.value.confirmPassword
  )
})

/**
 * 切换密码显示
 */
const togglePasswordVisibility = (field: 'old' | 'new' | 'confirm') => {
  switch (field) {
    case 'old':
      showOldPassword.value = !showOldPassword.value
      break
    case 'new':
      showNewPassword.value = !showNewPassword.value
      break
    case 'confirm':
      showConfirmPassword.value = !showConfirmPassword.value
      break
  }
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  // 再次验证所有字段
  validateField('oldPassword')
  validateField('newPassword')
  validateField('confirmPassword')

  if (!isFormValid.value) {
    uni.showToast({
      title: '请正确填写所有字段',
      icon: 'none'
    })
    return
  }

  try {
    submitting.value = true

    await changePassword(formData.value.oldPassword, formData.value.newPassword)

    uni.showToast({
      title: '密码修改成功',
      icon: 'success',
      duration: 1500
    })

    // 延迟跳转并清除认证信息
    setTimeout(() => {
      // 调用 logout 清除所有认证信息并跳转登录页
      userStore.logout()
    }, 1500)
  } catch (error: any) {
    console.error('修改密码失败:', error)
    uni.showToast({
      title: error.message || '修改失败',
      icon: 'none'
    })
  } finally {
    submitting.value = false
  }
}

/**
 * 页面卸载前确认
 */
onBeforeUnmount(() => {
  if (hasChanges.value && !submitting.value) {
    uni.showModal({
      title: '提示',
      content: '确定要放弃修改吗？',
      success: (res) => {
        if (!res.confirm) {
          // 用户取消，阻止返回（但 uni-app 没有直接方法阻止，这里只是示例）
        }
      }
    })
  }
})
</script>

<style lang="scss" scoped>
.change-password-page {
  min-height: 100vh;
  background: #F9FAFB;
  padding: 32rpx;
}

.container {
  max-width: 750rpx;
}

// 表单卡片
.form-card {
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 48rpx 32rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
}

// 表单组
.form-group {
  margin-bottom: 32rpx;

  &:last-of-type {
    margin-bottom: 48rpx;
  }
}

.form-label {
  display: block;
  font-size: 28rpx;
  color: #1F2937;
  margin-bottom: 16rpx;
  font-weight: 500;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  border: 2rpx solid #E5E7EB;
  border-radius: 8rpx;
  overflow: hidden;
  background: #FFFFFF;

  &:focus-within {
    border-color: #2563EB;
  }
}

.form-input {
  flex: 1;
  height: 88rpx;
  padding: 0 48rpx 0 24rpx;
  font-size: 32rpx;
  color: #1F2937;

  &::placeholder {
    color: #9CA3AF;
  }
}

.eye-icon {
  position: absolute;
  right: 24rpx;
  font-size: 40rpx;
  cursor: pointer;
  user-select: none;
}

.error-text {
  display: block;
  font-size: 24rpx;
  color: #EF4444;
  margin-top: 12rpx;
}

// 密码强度指示器
.strength-indicator {
  display: flex;
  align-items: center;
  margin-top: 16rpx;
  gap: 12rpx;
}

.strength-label {
  font-size: 24rpx;
  color: #6B7280;
}

.strength-bar {
  flex: 1;
  height: 8rpx;
  background: #E5E7EB;
  border-radius: 4rpx;
  overflow: hidden;
}

.strength-fill {
  height: 100%;
  transition: width 0.3s, background-color 0.3s;

  &.strength-weak {
    background: #EF4444;
  }

  &.strength-medium {
    background: #F59E0B;
  }

  &.strength-strong {
    background: #10B981;
  }
}

.strength-text {
  font-size: 24rpx;
  font-weight: 500;
  min-width: 48rpx;

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

// 提交按钮
.submit-btn {
  width: 100%;
  height: 88rpx;
  background: #2563EB;
  color: #FFFFFF;
  font-size: 32rpx;
  font-weight: 600;
  border-radius: 12rpx;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;

  &:active:not(.disabled) {
    opacity: 0.8;
  }

  &.disabled {
    background: #D1D5DB;
    color: #9CA3AF;
  }
}

// 提示卡片
.tips-card {
  background: #FEF3C7;
  border-radius: 12rpx;
  padding: 24rpx 32rpx;
  border-left: 4rpx solid #F59E0B;
}

.tips-title {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #92400E;
  margin-bottom: 12rpx;
}

.tips-item {
  display: block;
  font-size: 24rpx;
  color: #78350F;
  line-height: 40rpx;
}
</style>
