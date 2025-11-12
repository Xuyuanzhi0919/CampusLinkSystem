<template>
  <view class="edit-profile-page">
    <!-- 头部导航 -->
    <view class="page-header">
      <view class="header-left" @click="goBack">
        <text class="back-icon">←</text>
      </view>
      <text class="header-title">编辑资料</text>
      <view class="header-right"></view>
    </view>

    <!-- 表单内容 -->
    <view class="form-wrapper">
      <!-- 头像 -->
      <view class="form-section">
        <view class="section-item avatar-item" @click="selectAvatar">
          <text class="item-label">头像</text>
          <view class="item-right">
            <image :src="form.avatar || defaultAvatar" class="avatar-preview" mode="aspectFill" />
            <text class="arrow-icon">›</text>
          </view>
        </view>
      </view>

      <!-- 基本信息 -->
      <view class="form-section">
        <view class="section-title">基本信息</view>

        <view class="section-item">
          <text class="item-label">昵称</text>
          <input
            v-model="form.nickname"
            class="item-input"
            type="text"
            placeholder="请输入昵称"
            maxlength="20"
          />
        </view>

        <view class="section-item">
          <text class="item-label">个性签名</text>
          <input
            v-model="form.bio"
            class="item-input"
            type="text"
            placeholder="写点什么吧~"
            maxlength="50"
          />
        </view>

        <view class="section-item" @click="showGenderPicker">
          <text class="item-label">性别</text>
          <view class="item-right">
            <text class="item-value">{{ getGenderText(form.gender) }}</text>
            <text class="arrow-icon">›</text>
          </view>
        </view>
      </view>

      <!-- 学校信息 -->
      <view class="form-section">
        <view class="section-title">学校信息</view>

        <view class="section-item" @click="showSchoolPicker">
          <text class="item-label">学校</text>
          <view class="item-right">
            <text class="item-value">{{ form.schoolName || '请选择学校' }}</text>
            <text class="arrow-icon">›</text>
          </view>
        </view>

        <view class="section-item">
          <text class="item-label">专业</text>
          <input
            v-model="form.major"
            class="item-input"
            type="text"
            placeholder="请输入专业"
            maxlength="30"
          />
        </view>

        <view class="section-item" @click="showYearPicker">
          <text class="item-label">入学年份</text>
          <view class="item-right">
            <text class="item-value">{{ form.enrollmentYear || '请选择年份' }}</text>
            <text class="arrow-icon">›</text>
          </view>
        </view>
      </view>

      <!-- 联系方式 -->
      <view class="form-section">
        <view class="section-title">联系方式</view>

        <view class="section-item">
          <text class="item-label">邮箱</text>
          <input
            v-model="form.email"
            class="item-input"
            type="text"
            placeholder="请输入邮箱"
          />
        </view>

        <view class="section-item">
          <text class="item-label">手机号</text>
          <view class="item-right">
            <text class="item-value phone">{{ formatPhone(form.phone) }}</text>
            <text v-if="form.phone" class="verified-badge">已验证</text>
          </view>
        </view>
      </view>

      <!-- 保存按钮 -->
      <view class="save-button" @click="saveProfile">
        <text class="button-text">保存</text>
      </view>
    </view>

    <!-- 性别选择器 -->
    <picker
      v-if="showPicker === 'gender'"
      mode="selector"
      :range="genderOptions"
      range-key="label"
      @change="onGenderChange"
      @cancel="showPicker = ''"
    >
      <view></view>
    </picker>

    <!-- 年份选择器 -->
    <picker
      v-if="showPicker === 'year'"
      mode="selector"
      :range="yearOptions"
      @change="onYearChange"
      @cancel="showPicker = ''"
    >
      <view></view>
    </picker>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 表单数据
const form = ref({
  avatar: '',
  nickname: '',
  bio: '',
  gender: 0, // 0=未知, 1=男, 2=女
  schoolId: null as number | null,
  schoolName: '',
  major: '',
  enrollmentYear: null as number | null,
  email: '',
  phone: '',
})

// 默认头像
const defaultAvatar = 'https://via.placeholder.com/120/2563EB/FFFFFF?text=User'

// 选择器状态
const showPicker = ref('')

// 性别选项
const genderOptions = [
  { value: 0, label: '保密' },
  { value: 1, label: '男' },
  { value: 2, label: '女' },
]

// 年份选项（生成最近20年）
const currentYear = new Date().getFullYear()
const yearOptions = Array.from({ length: 20 }, (_, i) => currentYear - i)

// 初始化表单
onMounted(() => {
  const userInfo = userStore.userInfo
  if (userInfo) {
    form.value = {
      avatar: userInfo.avatar || '',
      nickname: userInfo.nickname || '',
      bio: userInfo.bio || '',
      gender: userInfo.gender || 0,
      schoolId: userInfo.schoolId || null,
      schoolName: userInfo.schoolName || '',
      major: userInfo.major || '',
      enrollmentYear: userInfo.enrollmentYear || null,
      email: userInfo.email || '',
      phone: userInfo.phone || '',
    }
  }
})

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 选择头像
const selectAvatar = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      // TODO: 上传到服务器
      form.value.avatar = res.tempFilePaths[0]
      uni.showToast({ title: '头像上传功能开发中', icon: 'none' })
    },
  })
}

// 显示选择器
const showGenderPicker = () => {
  showPicker.value = 'gender'
  // 延迟触发picker
  setTimeout(() => {
    const picker = document.querySelector('picker')
    if (picker) {
      picker.click()
    }
  }, 100)
}

const showSchoolPicker = () => {
  // TODO: 跳转到学校选择页面
  uni.showToast({ title: '学校选择功能开发中', icon: 'none' })
}

const showYearPicker = () => {
  showPicker.value = 'year'
  setTimeout(() => {
    const picker = document.querySelector('picker')
    if (picker) {
      picker.click()
    }
  }, 100)
}

// 选择器变更
const onGenderChange = (e: any) => {
  const index = e.detail.value
  form.value.gender = genderOptions[index].value
  showPicker.value = ''
}

const onYearChange = (e: any) => {
  const index = e.detail.value
  form.value.enrollmentYear = yearOptions[index]
  showPicker.value = ''
}

// 工具函数
const getGenderText = (gender: number) => {
  const option = genderOptions.find((o) => o.value === gender)
  return option?.label || '保密'
}

const formatPhone = (phone: string) => {
  if (!phone) return '未绑定'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 保存资料
const saveProfile = async () => {
  // 表单验证
  if (!form.value.nickname || form.value.nickname.trim() === '') {
    uni.showToast({ title: '请输入昵称', icon: 'none' })
    return
  }

  if (form.value.email && !isValidEmail(form.value.email)) {
    uni.showToast({ title: '邮箱格式不正确', icon: 'none' })
    return
  }

  try {
    uni.showLoading({ title: '保存中...' })

    // TODO: 调用后端API更新用户信息
    await new Promise((resolve) => setTimeout(resolve, 1000))

    // 更新本地用户信息
    userStore.setUserInfo({
      ...userStore.userInfo,
      ...form.value,
    })

    uni.hideLoading()
    uni.showToast({ title: '保存成功', icon: 'success' })

    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (error) {
    uni.hideLoading()
    uni.showToast({ title: '保存失败', icon: 'none' })
    console.error('保存失败:', error)
  }
}

// 邮箱验证
const isValidEmail = (email: string) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}
</script>

<style scoped lang="scss">
.edit-profile-page {
  min-height: 100vh;
  background: #F5F6FA;
}

/* 头部 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 24rpx;
  background: #FFFFFF;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.header-left,
.header-right {
  width: 80rpx;
}

.back-icon {
  font-size: 48rpx;
  color: #1E293B;
  font-weight: 300;
}

.header-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1E293B;
}

/* 表单 */
.form-wrapper {
  padding: 24rpx;
  padding-bottom: 48rpx;
}

.form-section {
  background: #FFFFFF;
  border-radius: 24rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
}

.section-title {
  padding: 32rpx 32rpx 16rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #64748B;
}

.section-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28rpx 32rpx;
  border-bottom: 1rpx solid #F1F5F9;
  min-height: 100rpx;

  &:last-child {
    border-bottom: none;
  }

  &.avatar-item {
    min-height: 140rpx;
  }
}

.item-label {
  font-size: 28rpx;
  color: #1E293B;
  font-weight: 500;
  width: 160rpx;
  flex-shrink: 0;
}

.item-input {
  flex: 1;
  font-size: 28rpx;
  color: #1E293B;
  text-align: right;

  &::placeholder {
    color: #CBD5E1;
  }
}

.item-right {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;
  justify-content: flex-end;
}

.item-value {
  font-size: 28rpx;
  color: #475569;

  &.phone {
    color: #1E293B;
  }
}

.arrow-icon {
  font-size: 48rpx;
  color: #CBD5E1;
  font-weight: 300;
  line-height: 1;
}

.avatar-preview {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: #F3F4F6;
  border: 2rpx solid #E2E8F0;
}

.verified-badge {
  padding: 4rpx 12rpx;
  background: linear-gradient(135deg, #DBEAFE 0%, #BFDBFE 100%);
  border-radius: 8rpx;
  font-size: 22rpx;
  font-weight: 500;
  color: #2563EB;
}

/* 保存按钮 */
.save-button {
  margin-top: 48rpx;
  padding: 28rpx;
  background: linear-gradient(135deg, #2563EB 0%, #3B82F6 100%);
  border-radius: 16rpx;
  box-shadow: 0 8rpx 16rpx rgba(37, 99, 235, 0.2);
  text-align: center;
  transition: all 0.2s;

  &:active {
    transform: scale(0.98);
  }
}

.button-text {
  font-size: 32rpx;
  font-weight: 600;
  color: #FFFFFF;
}

/* PC端适配 */
@media (min-width: 768px) {
  .form-wrapper {
    max-width: 800rpx;
    margin: 0 auto;
    padding-top: 48rpx;
  }

  .save-button {
    &:hover {
      transform: translateY(-2rpx);
      box-shadow: 0 12rpx 24rpx rgba(37, 99, 235, 0.3);
    }
  }
}
</style>
