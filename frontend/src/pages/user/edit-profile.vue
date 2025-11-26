<template>
  <view class="edit-profile-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar">
      <view class="navbar-content">
        <view class="navbar-left" @click="handleBack">
          <text class="back-icon">←</text>
          <text class="back-text">返回</text>
        </view>
        <text class="navbar-title">编辑资料</text>
        <view class="navbar-right">
          <button
            class="save-button"
            :class="{ disabled: !canSave }"
            :disabled="!canSave || saving"
            @click="handleSave"
          >
            <text v-if="!saving">保存</text>
            <text v-else>保存中...</text>
          </button>
        </view>
      </view>
    </view>

    <!-- 内容区域 -->
    <scroll-view class="content-scroll" scroll-y>
      <!-- 头像区域 -->
      <view class="avatar-section">
        <view class="section-title">头像</view>
        <view class="avatar-container" @click="handleChooseAvatar">
          <image
            v-if="formData.avatarUrl"
            :src="formData.avatarUrl"
            class="avatar-image"
            mode="aspectFill"
          />
          <view v-else class="avatar-placeholder">
            <text class="placeholder-text">👤</text>
          </view>
          <view v-if="uploading" class="upload-mask">
            <text class="upload-text">上传中...</text>
          </view>
          <view class="avatar-tip">
            <text class="tip-text">点击更换头像</text>
          </view>
        </view>
      </view>

      <!-- 基本信息 -->
      <view class="form-section">
        <view class="section-title">基本信息</view>

        <!-- 昵称 -->
        <view class="form-item" :class="{ error: errors.nickname }">
          <view class="item-label">
            <text class="label-text">昵称</text>
            <text class="required-mark">*</text>
          </view>
          <input
            v-model="formData.nickname"
            class="item-input"
            placeholder="请输入昵称 (2-20字符)"
            maxlength="20"
            @blur="validateField('nickname')"
          />
          <text v-if="errors.nickname" class="error-text">{{ errors.nickname }}</text>
        </view>

        <!-- 学号/工号 -->
        <view class="form-item" :class="{ error: errors.studentId }">
          <view class="item-label">
            <text class="label-text">学号/工号</text>
          </view>
          <input
            v-model="formData.studentId"
            class="item-input"
            placeholder="请输入学号或工号"
            @blur="validateField('studentId')"
          />
          <text v-if="errors.studentId" class="error-text">{{ errors.studentId }}</text>
        </view>

        <!-- 专业 -->
        <view class="form-item">
          <view class="item-label">
            <text class="label-text">专业</text>
          </view>
          <input
            v-model="formData.major"
            class="item-input"
            placeholder="请输入专业"
            maxlength="50"
          />
        </view>

        <!-- 年级 -->
        <view class="form-item">
          <view class="item-label">
            <text class="label-text">年级</text>
          </view>
          <picker
            mode="selector"
            :range="gradeOptions"
            range-key="label"
            :value="gradePickerIndex"
            @change="handleGradeChange"
          >
            <view class="picker-input">
              <text :class="formData.grade ? 'picker-text' : 'picker-placeholder'">
                {{ gradeLabel || '请选择年级' }}
              </text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>

        <!-- 手机号 -->
        <view class="form-item" :class="{ error: errors.phone }">
          <view class="item-label">
            <text class="label-text">手机号</text>
          </view>
          <input
            v-model="formData.phone"
            class="item-input"
            type="text"
            placeholder="请输入手机号（如需修改请清空后重新输入）"
            maxlength="11"
            @blur="validateField('phone')"
          />
          <text v-if="errors.phone" class="error-text">{{ errors.phone }}</text>
        </view>
      </view>

      <!-- 底部安全距离 -->
      <view class="safe-area-bottom" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import type { UpdateProfileRequest } from '@/types/user'
import { getUserProfile, updateUserProfile } from '@/services/user'
import { getUploadSignature } from '@/services/resource'

const userStore = useUserStore()

// 表单数据
const formData = ref<UpdateProfileRequest & { avatarUrl?: string }>({
  nickname: '',
  avatarUrl: '',
  studentId: '',
  major: '',
  grade: undefined,
  phone: ''
})

// 原始数据 (用于判断是否有修改)
const originalData = ref<string>('')

// 错误信息
const errors = ref<Record<string, string>>({})

// 状态
const saving = ref(false)
const uploading = ref(false)

// 年级选项
const gradeOptions = [
  { value: 1, label: '大一' },
  { value: 2, label: '大二' },
  { value: 3, label: '大三' },
  { value: 4, label: '大四' },
  { value: 5, label: '研一' },
  { value: 6, label: '研二' },
  { value: 7, label: '研三' }
]

// 年级选择器索引
const gradePickerIndex = computed(() => {
  if (!formData.value.grade) return 0
  return gradeOptions.findIndex(item => item.value === formData.value.grade)
})

// 年级显示文本
const gradeLabel = computed(() => {
  if (!formData.value.grade) return ''
  const option = gradeOptions.find(item => item.value === formData.value.grade)
  return option?.label || ''
})

// 是否可以保存
const canSave = computed(() => {
  // 必填字段校验
  if (!formData.value.nickname || formData.value.nickname.trim().length < 2) {
    return false
  }

  // 判断是否有修改
  const currentData = JSON.stringify(formData.value)
  if (currentData === originalData.value) {
    return false
  }

  // 检查是否有错误
  if (Object.keys(errors.value).length > 0) {
    return false
  }

  return true
})

/**
 * 加载用户资料
 */
const loadUserProfile = async () => {
  try {
    const profile = await getUserProfile()

    formData.value = {
      nickname: profile.nickname || '',
      avatarUrl: profile.avatarUrl || '',
      studentId: profile.studentId || '',
      major: profile.major || '',
      grade: profile.grade || undefined,
      phone: profile.phone || ''
    }

    // 保存原始数据
    originalData.value = JSON.stringify(formData.value)
  } catch (error: any) {
    console.error('加载用户资料失败:', error)
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
  }
}

/**
 * 字段校验
 */
const validateField = (field: string) => {
  const value = formData.value[field as keyof typeof formData.value]

  switch (field) {
    case 'nickname':
      if (!value || (value as string).trim().length < 2) {
        errors.value.nickname = '昵称长度为 2-20 个字符'
      } else if ((value as string).trim().length > 20) {
        errors.value.nickname = '昵称长度为 2-20 个字符'
      } else {
        delete errors.value.nickname
      }
      break

    case 'studentId':
      if (value && !/^[a-zA-Z0-9]+$/.test(value as string)) {
        errors.value.studentId = '学号格式不正确'
      } else {
        delete errors.value.studentId
      }
      break

    case 'phone':
      // 如果包含星号（脱敏状态），跳过验证
      if (value && (value as string).includes('*')) {
        delete errors.value.phone
      } else if (value && !/^1[3-9]\d{9}$/.test(value as string)) {
        errors.value.phone = '请输入正确的手机号'
      } else {
        delete errors.value.phone
      }
      break
  }
}

/**
 * 选择头像
 */
const handleChooseAvatar = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0]
      uploadAvatar(tempFilePath)
    },
    fail: (err) => {
      console.error('选择图片失败:', err)
      uni.showToast({
        title: '选择图片失败',
        icon: 'none'
      })
    }
  })
}

/**
 * 上传头像到 OSS
 */
const uploadAvatar = async (filePath: string) => {
  try {
    uploading.value = true

    // 生成文件名
    const timestamp = Date.now()
    const random = Math.random().toString(36).substring(2, 8)
    const ext = filePath.split('.').pop()
    const fileName = `avatar_${timestamp}_${random}.${ext}`

    // 获取 OSS 签名
    const signature = await getUploadSignature(fileName)

    // 上传到 OSS
    uni.uploadFile({
      url: signature.host,
      filePath,
      name: 'file',
      formData: {
        key: signature.key,
        policy: signature.policy,
        OSSAccessKeyId: signature.accessId,
        signature: signature.signature,
        success_action_status: '200'
      },
      success: (uploadRes) => {
        if (uploadRes.statusCode === 200) {
          // 构建完整的图片 URL
          const avatarUrl = `${signature.host}/${signature.key}`
          formData.value.avatarUrl = avatarUrl

          uni.showToast({
            title: '头像上传成功',
            icon: 'success'
          })
        } else {
          throw new Error('上传失败')
        }
      },
      fail: (err) => {
        console.error('上传失败:', err)
        uni.showToast({
          title: '头像上传失败',
          icon: 'none'
        })
      },
      complete: () => {
        uploading.value = false
      }
    })
  } catch (error: any) {
    console.error('上传头像失败:', error)
    uploading.value = false
    uni.showToast({
      title: error.message || '上传失败',
      icon: 'none'
    })
  }
}

/**
 * 年级变更
 */
const handleGradeChange = (e: any) => {
  const index = e.detail.value
  formData.value.grade = gradeOptions[index].value
}

/**
 * 返回
 */
const handleBack = () => {
  // 检查是否有未保存的修改
  const currentData = JSON.stringify(formData.value)
  if (currentData !== originalData.value) {
    uni.showModal({
      title: '提示',
      content: '您有未保存的修改,确定要离开吗?',
      success: (res) => {
        if (res.confirm) {
          uni.navigateBack()
        }
      }
    })
  } else {
    uni.navigateBack()
  }
}

/**
 * 保存资料
 */
const handleSave = async () => {
  if (!canSave.value || saving.value) return

  try {
    saving.value = true

    // 最终校验
    validateField('nickname')
    validateField('studentId')
    validateField('phone')

    if (Object.keys(errors.value).length > 0) {
      uni.showToast({
        title: '请检查表单填写',
        icon: 'none'
      })
      return
    }

    // 构建请求数据
    const updateData: UpdateProfileRequest = {
      nickname: formData.value.nickname?.trim()
    }

    if (formData.value.avatarUrl) {
      updateData.avatarUrl = formData.value.avatarUrl
    }
    if (formData.value.studentId) {
      updateData.studentId = formData.value.studentId
    }
    if (formData.value.major) {
      updateData.major = formData.value.major
    }
    if (formData.value.grade) {
      updateData.grade = formData.value.grade
    }
    // 只有当手机号不为空且不包含星号（不是脱敏状态）时才提交
    if (formData.value.phone && !formData.value.phone.includes('*')) {
      updateData.phone = formData.value.phone
    }

    // 调用更新接口
    const updatedProfile = await updateUserProfile(updateData)

    // 更新 store 中的用户信息
    userStore.setUserInfo(updatedProfile)

    // 显示成功提示
    uni.showToast({
      title: '保存成功',
      icon: 'success',
      duration: 1500
    })

    // 延迟返回
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (error: any) {
    console.error('保存资料失败:', error)
    uni.showToast({
      title: error.message || '保存失败',
      icon: 'none'
    })
  } finally {
    saving.value = false
  }
}

// 页面加载时获取用户资料
onMounted(() => {
  loadUserProfile()
})
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

.edit-profile-page {
  min-height: 100vh;
  background: $gray-50;
}

// 自定义导航栏
.custom-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: $white;
  border-bottom: 1rpx solid $gray-200;
  z-index: $z-modal;
}

.navbar-content {
  @include flex-between;
  height: 88rpx;
  padding: 0 $sp-8;
  padding-top: env(safe-area-inset-top);
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: $sp-2;
}

.back-icon {
  font-size: 40rpx;
  color: $gray-800;
}

.back-text {
  font-size: $font-size-base;
  color: $gray-800;
}

.navbar-title {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $gray-800;
}

.navbar-right {
  width: 120rpx;
  display: flex;
  justify-content: flex-end;
}

.save-button {
  padding: $sp-3 $sp-6;
  background: $primary;
  color: $white;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  border-radius: $radius-base;
  border: none;

  &.disabled {
    background: $gray-200;
    color: $gray-400;
  }

  &::after {
    border: none;
  }
}

// 内容区域
.content-scroll {
  height: 100vh;
  padding-top: calc(88rpx + env(safe-area-inset-top));
}

// 头像区域
.avatar-section {
  padding: $sp-12 $sp-8;
  background: $white;
  margin-bottom: $sp-5;
}

.section-title {
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: $gray-800;
  margin-bottom: $sp-8;
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.avatar-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: $radius-full;
  background: $gray-100;
}

.avatar-placeholder {
  width: 160rpx;
  height: 160rpx;
  border-radius: $radius-full;
  background: $gray-100;
  @include flex-center;
}

.placeholder-text {
  font-size: 80rpx;
}

.upload-mask {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 160rpx;
  height: 160rpx;
  border-radius: $radius-full;
  background: rgba($gray-900, 0.5);
  @include flex-center;
}

.upload-text {
  font-size: $font-size-sm;
  color: $white;
}

.avatar-tip {
  margin-top: $sp-4;
}

.tip-text {
  font-size: $font-size-sm;
  color: $primary;
}

// 表单区域
.form-section {
  background: $white;
  padding: $sp-8;
}

.form-item {
  margin-bottom: $sp-10;

  &:last-child {
    margin-bottom: 0;
  }

  &.error {
    .item-input,
    .picker-input {
      border-color: $error;
    }
  }
}

.item-label {
  display: flex;
  align-items: center;
  margin-bottom: $sp-4;
}

.label-text {
  font-size: $font-size-base;
  color: $gray-800;
  font-weight: $font-weight-medium;
}

.required-mark {
  color: $error;
  margin-left: $sp-2;
}

.item-input {
  width: 100%;
  height: 80rpx;
  padding: 0 $sp-6;
  background: $gray-50;
  border: 1rpx solid $gray-200;
  border-radius: $radius-base;
  font-size: $font-size-base;
  color: $gray-800;
}

.picker-input {
  @include flex-between;
  height: 80rpx;
  padding: 0 $sp-6;
  background: $gray-50;
  border: 1rpx solid $gray-200;
  border-radius: $radius-base;
}

.picker-text {
  font-size: $font-size-base;
  color: $gray-800;
}

.picker-placeholder {
  font-size: $font-size-base;
  color: $gray-400;
}

.picker-arrow {
  font-size: 48rpx;
  color: $gray-400;
  transform: rotate(90deg);
}

.error-text {
  display: block;
  margin-top: $sp-3;
  font-size: $font-size-sm;
  color: $error;
}

.safe-area-bottom {
  height: $sp-8;
}
</style>
