<template>
  <view class="publish-activity-page">
    <!-- 顶部导航栏 -->
    <CNavBar title="发布活动" :auto-back="false" @back="handleCancel" />

    <scroll-view class="content-area" scroll-y>
      <view class="form-container">
        <!-- 活动标题 -->
        <CCard variant="elevated" class="form-card">
          <view class="card-header">
            <Icon name="type" :size="20" class="header-icon" />
            <text class="header-title">基本信息</text>
          </view>

          <view class="form-group">
            <view class="form-label">
              <text class="label-text">活动标题</text>
              <text class="required">*</text>
            </view>
            <input
              v-model="formData.title"
              class="form-input"
              placeholder="请输入活动标题，5-200个字符"
              maxlength="200"
            />
            <view class="input-footer">
              <text class="input-hint">简洁明了的标题能吸引更多人参与</text>
              <text class="input-count">{{ formData.title.length }}/200</text>
            </view>
            <text v-if="errors.title" class="error-text">{{ errors.title }}</text>
          </view>

          <!-- 活动描述 -->
          <view class="form-group">
            <view class="form-label">
              <text class="label-text">活动描述</text>
              <text class="required">*</text>
            </view>
            <textarea
              v-model="formData.description"
              class="form-textarea"
              placeholder="请详细描述活动内容、参与要求、注意事项等..."
              maxlength="2000"
              :auto-height="true"
            />
            <view class="input-footer">
              <text class="input-hint">详细描述有助于用户了解活动详情</text>
              <text class="input-count" :class="{ 'count-warning': formData.description.length > 1900 }">{{ formData.description.length }}/2000</text>
            </view>
            <text v-if="errors.description" class="error-text">{{ errors.description }}</text>
          </view>

          <!-- 活动地点 -->
          <view class="form-group">
            <view class="form-label">
              <text class="label-text">活动地点</text>
              <text class="required">*</text>
            </view>
            <input
              v-model="formData.location"
              class="form-input"
              placeholder="请输入活动地点"
              maxlength="200"
            />
            <text v-if="errors.location" class="error-text">{{ errors.location }}</text>
          </view>
        </CCard>

        <!-- 时间设置 -->
        <CCard variant="elevated" class="form-card">
          <view class="card-header">
            <Icon name="calendar" :size="20" class="header-icon" />
            <text class="header-title">时间设置</text>
          </view>

          <!-- 开始时间 -->
          <view class="form-group">
            <view class="form-label">
              <text class="label-text">开始时间</text>
              <text class="required">*</text>
            </view>
            <picker
              mode="multiSelector"
              :range="[dateRange, timeRange]"
              :value="[startDateIndex, startTimeIndex]"
              @change="handleStartTimeChange"
              @columnchange="handleStartColumnChange"
            >
              <view class="picker-trigger" :class="{ error: errors.startTime }">
                <text :class="{ placeholder: !formData.startTime }">{{ formData.startTime ? formatDateTime(formData.startTime) : '请选择开始时间' }}</text>
                <text class="arrow">▼</text>
              </view>
            </picker>
            <text v-if="errors.startTime" class="error-text">{{ errors.startTime }}</text>
          </view>

          <!-- 结束时间 -->
          <view class="form-group">
            <view class="form-label">
              <text class="label-text">结束时间</text>
              <text class="required">*</text>
            </view>
            <picker
              mode="multiSelector"
              :range="[dateRange, timeRange]"
              :value="[endDateIndex, endTimeIndex]"
              @change="handleEndTimeChange"
              @columnchange="handleEndColumnChange"
            >
              <view class="picker-trigger" :class="{ error: errors.endTime }">
                <text :class="{ placeholder: !formData.endTime }">{{ formData.endTime ? formatDateTime(formData.endTime) : '请选择结束时间' }}</text>
                <text class="arrow">▼</text>
              </view>
            </picker>
            <text v-if="errors.endTime" class="error-text">{{ errors.endTime }}</text>
          </view>

          <!-- 报名截止时间 -->
          <view class="form-group">
            <view class="form-label">
              <text class="label-text">报名截止</text>
              <text class="required">*</text>
            </view>
            <picker
              mode="multiSelector"
              :range="[dateRange, timeRange]"
              :value="[deadlineDateIndex, deadlineTimeIndex]"
              @change="handleDeadlineChange"
              @columnchange="handleDeadlineColumnChange"
            >
              <view class="picker-trigger" :class="{ error: errors.signupDeadline }">
                <text :class="{ placeholder: !formData.signupDeadline }">{{ formData.signupDeadline ? formatDateTime(formData.signupDeadline) : '请选择报名截止时间' }}</text>
                <text class="arrow">▼</text>
              </view>
            </picker>
            <text v-if="errors.signupDeadline" class="error-text">{{ errors.signupDeadline }}</text>
          </view>
        </CCard>

        <!-- 参与设置 -->
        <CCard variant="elevated" class="form-card">
          <view class="card-header">
            <Icon name="users" :size="20" class="header-icon" />
            <text class="header-title">参与设置</text>
          </view>

          <!-- 最大参与人数 -->
          <view class="form-group">
            <view class="form-label">
              <text class="label-text">最大参与人数</text>
              <text class="required">*</text>
            </view>
            <input
              v-model.number="formData.maxParticipants"
              class="form-input"
              type="number"
              placeholder="请输入最大参与人数"
              min="1"
            />
            <view class="input-footer">
              <text class="input-hint">设置合理的参与人数上限</text>
            </view>
            <text v-if="errors.maxParticipants" class="error-text">{{ errors.maxParticipants }}</text>
          </view>

          <!-- 参与奖励积分 -->
          <view class="form-group">
            <view class="form-label">
              <text class="label-text">参与奖励</text>
              <text class="label-hint">（积分）</text>
            </view>
            <view class="reward-selector">
              <view
                v-for="points in rewardOptions"
                :key="points"
                class="reward-item"
                :class="{ active: formData.checkInPoints === points }"
                @click="handleRewardSelect(points)"
              >
                <text class="reward-points">{{ points }}</text>
                <text class="reward-label">积分</text>
              </view>
            </view>
            <view class="reward-hint">
              <text class="hint-icon">💡</text>
              <text class="hint-text">参与者完成签到后可获得奖励积分</text>
            </view>
          </view>
        </CCard>

        <!-- 封面图片（可选） -->
        <CCard variant="elevated" class="form-card">
          <view class="card-header">
            <Icon name="image" :size="20" class="header-icon" />
            <text class="header-title">封面图片</text>
            <text class="header-optional">（可选）</text>
          </view>

          <view class="form-group">
            <!-- 已上传封面 -->
            <view v-if="formData.coverImage" class="cover-preview">
              <image :src="formData.coverImage" class="cover-image" mode="aspectFill" />
              <view class="cover-actions">
                <view class="action-btn" @click="handleRemoveCover">
                  <Icon name="trash-2" :size="16" color="#EF4444" />
                  <text class="action-text danger">移除</text>
                </view>
              </view>
            </view>
            <!-- 上传按钮 -->
            <view v-else class="upload-cover-btn" @click="handleUploadCover">
              <Icon name="image-plus" :size="24" class="upload-icon" />
              <text class="upload-text">上传封面图片</text>
              <text class="upload-hint">建议尺寸 16:9，支持 JPG/PNG</text>
            </view>
          </view>
        </CCard>

        <!-- 提交按钮 -->
        <view class="submit-section">
          <CButton
            type="primary"
            size="lg"
            block
            :disabled="!isFormValid"
            :loading="submitting"
            @click="handleSubmit"
          >
            发布活动
          </CButton>
          <text class="submit-hint">发布后可在活动详情页进行管理</text>
        </view>
      </view>
    </scroll-view>

    <!-- 社团选择弹窗 -->
    <view v-if="showClubPicker" class="picker-mask" @click="showClubPicker = false">
      <view class="picker-sheet" @click.stop>
        <view class="picker-header">
          <text class="picker-title">选择社团</text>
          <view class="picker-close" @click="showClubPicker = false">
            <Icon name="x" :size="20" />
          </view>
        </view>
        <scroll-view class="picker-content" scroll-y>
          <view
            v-for="club in myClubs"
            :key="club.clubId"
            class="club-item"
            :class="{ active: formData.clubId === club.clubId }"
            @click="handleSelectClub(club)"
            <image :src="club.logoUrl || '/static/default-avatar.png'" class="club-avatar" mode="aspectFill" />
            <view class="club-info">
              <text class="club-name">{{ club.clubName }}</text>
              <text class="club-members">{{ club.memberCount }} 成员</text>
            </view>
            <Icon v-if="formData.clubId === club.clubId" name="check" :size="20" color="#2355C8" />
          </view>
          <view v-if="myClubs.length === 0" class="empty-clubs">
            <text class="empty-text">您还不是任何社团的管理员</text>
            <CButton type="primary" size="sm" @click="handleCreateClub">创建社团</CButton>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { CNavBar } from '@/components/layout'
import CCard from '@/components/ui/CCard.vue'
import CButton from '@/components/ui/CButton.vue'
import Icon from '@/components/icons/index.vue'
import { createActivity } from '@/services/club'
import type { ActivityCreateParams, ClubItem } from '@/types/club'
import { uploadToOSS } from '@/utils/upload'

// 表单数据
const formData = ref<ActivityCreateParams>({
  clubId: 0,
  title: '',
    description: '',
    coverImage: '',
    location: '',
    startTime: '',
    endTime: '',
    maxParticipants: 50,
    signupDeadline: '',
    checkInPoints: 0
  })

// 表单错误
const errors = ref<Record<string, string>>({})

// 提交状态
const submitting = ref(false)
// 社团选择
const showClubPicker = ref(false)
const myClubs = ref<ClubItem[]>([])
// 奖励积分选项
const rewardOptions = [0, 5, 10, 20, 30, 50]
// 日期时间选择器数据
const dateRange = ref<string[]>([])
const timeRange = ref<string[]>([])
// 开始时间选择索引
const startDateIndex = ref(0)
const startTimeIndex = ref(0)
// 结束时间选择索引
const endDateIndex = ref(0)
const endTimeIndex = ref(0)
// 报名截止时间选择索引
const deadlineDateIndex = ref(0)
const deadlineTimeIndex = ref(0)
// 初始化日期时间范围
const initDateTimeRange = () => {
  // 生成未来30天的日期
  const dates: string[] = []
  const today = new Date()
  for (let i = 0; i < 30; i++) {
    const date = new Date(today)
    date.setDate(today.getDate() + i)
    const month = (date.getMonth() + 1).toString().padStart(2, '0')
    const day = date.getDate().toString().padStart(2, '0')
    const weekDay = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][date.getDay()]
    dates.push(`${month}-${day} ${weekDay}`)
  }
  dateRange.value = dates

  // 生成时间选项（每30分钟）
  const times: string[] = []
  for (let h = 0; h < 24; h++) {
    for (let m = 0; m < 60; m += 30) {
      const hour = h.toString().padStart(2, '0')
      const minute = m.toString().padStart(2, '0')
      times.push(`${hour}:${minute}`)
    }
  }
  timeRange.value = times
}
/**
 * 格式化日期时间显示
 */
const formatDateTime = (isoString: string): string => {
  if (!isoString) return ''
  const date = new Date(isoString)
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
    const day = date.getDate().toString().padStart(2, '0')
    const hour = date.getHours().toString().padStart(2, '0')
    const minute = date.getMinutes().toString().padStart(2, '0')
    const weekDay = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][date.getDay()]
    return `${month}-${day} ${weekDay} ${hour}:${minute}`
}
/**
 * 从选择器索引构建 ISO 日期字符串
 */
const buildISODateTime = (dateIdx: number, timeIdx: number): string => {
  const today = new Date()
    const targetDate = new Date(today)
    targetDate.setDate(today.getDate() + dateIdx)
  
  const timeStr = timeRange.value[timeIdx]
  const [hour, minute] = timeStr.split(':')
  
  targetDate.setHours(parseInt(hour), parseInt(minute), 0, 0)
    return targetDate.toISOString()
}
// 开始时间选择处理
const handleStartColumnChange = (e: any) => {
  if (e.detail.column === 0) {
    startDateIndex.value = e.detail.value
  } else {
    startTimeIndex.value = e.detail.value
  }
}
const handleStartTimeChange = (e: any) => {
  const [dateIdx, timeIdx] = e.detail.value
  startDateIndex.value = dateIdx
  startTimeIndex.value = timeIdx
  formData.value.startTime = buildISODateTime(dateIdx, timeIdx)
    errors.value.startTime = ''
  }
}
// 结束时间选择处理
const handleEndColumnChange = (e: any) => {
  if (e.detail.column === 0) {
    endDateIndex.value = e.detail.value
  } else {
    endTimeIndex.value = e.detail.value
  }
}
const handleEndTimeChange = (e: any) => {
  const [dateIdx, timeIdx] = e.detail.value
    endDateIndex.value = dateIdx
    endTimeIndex.value = timeIdx
    formData.value.endTime = buildISODateTime(dateIdx, timeIdx)
    errors.value.endTime = ''
  }
}
// 报名截止时间选择处理
const handleDeadlineColumnChange = (e: any) => {
  if (e.detail.column === 0) {
    deadlineDateIndex.value = e.detail.value
  } else {
    deadlineTimeIndex.value = e.detail.value
  }
}
const handleDeadlineChange = (e: any) => {
  const [dateIdx, timeIdx] = e.detail.value
    deadlineDateIndex.value = dateIdx
    deadlineTimeIndex.value = timeIdx
    formData.value.signupDeadline = buildISODateTime(dateIdx, timeIdx)
    errors.value.signupDeadline = ''
  }
}
/**
 * 选择奖励积分
 */
const handleRewardSelect = (points: number) => {
  formData.value.checkInPoints = points
}
/**
 * 上传封面图片
 */
const handleUploadCover = async () => {
  try {
    uni.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: async (res) => {
        const tempFilePath = res.tempFilePaths[0]
        uni.showLoading({ title: '上传中...' })
        try {
          const imageUrl = await uploadToOSS(tempFilePath, 'activity-cover')
          formData.value.coverImage = imageUrl
          uni.hideLoading()
          uni.showToast({ title: '上传成功', icon: 'success' })
        } catch (error) {
          uni.hideLoading()
          uni.showToast({ title: '上传失败', icon: 'none' })
        }
      }
    })
  } catch (error) {
    console.error('选择图片失败:', error)
  }
}
/**
 * 移除封面图片
 */
const handleRemoveCover = () => {
  formData.value.coverImage = ''
}
/**
 * 选择社团
 */
const handleSelectClub = (club: ClubItem) => {
  formData.value.clubId = club.clubId
  showClubPicker.value = false
  errors.value.clubId = ''
}
/**
 * 创建社团
 */
const handleCreateClub = () => {
  showClubPicker.value = false
  uni.navigateTo({ url: '/pages/club/create' })
}
/**
 * 加载我的社团列表
 */
const loadMyClubs = async () => {
  try {
    const result = await getMyClubs({ page: 1, pageSize: 100 })
    myClubs.value = result.list
    // 如果只有一个社团，自动选中
    if (myClubs.value.length === 1) {
      formData.value.clubId = myClubs.value[0].clubId
    }
  } catch (error) {
    console.error('加载社团列表失败:', error)
  }
}
/**
 * 表单验证
 */
const validateForm = (): boolean => {
  errors.value = {}
  let isValid = true

  if (!formData.value.clubId) {
    errors.value.clubId = '请选择社团'
    isValid = false
  }
  if (formData.value.title.length < 5) {
    errors.value.title = '标题长度至少5个字符'
    isValid = false
  }
  if (formData.value.description.length < 10) {
    errors.value.description = '描述长度至少10个字符'
    isValid = false
  }
  if (!formData.value.location) {
    errors.value.location = '请输入活动地点'
    isValid = false
  }
  if (!formData.value.startTime) {
    errors.value.startTime = '请选择开始时间'
    isValid = false
  }
  if (!formData.value.endTime) {
    errors.value.endTime = '请选择结束时间'
    isValid = false
  }
  if (formData.value.startTime && formData.value.endTime) {
    if (new Date(formData.value.startTime) >= new Date(formData.value.endTime)) {
      errors.value.endTime = '结束时间必须晚于开始时间'
      isValid = false
    }
  }
  if (!formData.value.signupDeadline) {
    errors.value.signupDeadline = '请选择报名截止时间'
    isValid = false
  }
  if (formData.value.signupDeadline && formData.value.startTime) {
    if (new Date(formData.value.signupDeadline) > new Date(formData.value.startTime)) {
      errors.value.signupDeadline = '报名截止时间不能晚于活动开始时间'
      isValid = false
    }
  }
  if (!formData.value.maxParticipants || formData.value.maxParticipants < 1) {
    errors.value.maxParticipants = '参与人数至少为1'
    isValid = false
  }
  return isValid
}
/**
 * 表单是否有效
 */
const isFormValid = computed(() => {
  return (
    formData.value.clubId > 0 &&
    formData.value.title.length >= 5 &&
    formData.value.description.length >= 10 &&
    formData.value.location.length > 0 &&
    formData.value.startTime &&
    formData.value.endTime &&
    formData.value.signupDeadline &&
    formData.value.maxParticipants >= 1
  )
})
/**
 * 检查表单是否有未保存的内容
 */
const hasUnsavedChanges = computed(() => {
  return !!(
    formData.value.title ||
    formData.value.description ||
    formData.value.location ||
    formData.value.startTime ||
    formData.value.endTime ||
    formData.value.coverImage
 )
  })
/**
 * 取消发布
 */
const handleCancel = () => {
  if (hasUnsavedChanges.value) {
    uni.showModal({
      title: '提示',
      content: '您有未保存的内容，确定要离开吗？',
      success: (res) => {
        if (res.confirm) {
          uni.navigateBack({
            fail: () => {
              uni.switchTab({ url: '/pages/home/index' })
            }
          })
        }
      }
    })
  } else {
    uni.navigateBack({
      fail: () => {
        uni.switchTab({ url: '/pages/home/index' })
      }
    })
  }
}
/**
 * 提交表单
 */
const handleSubmit = async () => {
  if (!validateForm()) return
  try {
    submitting.value = true
    const response = await createActivity(formData.value)
    uni.showToast({
      title: '发布成功',
      icon: 'success',
      duration: 1500
    })
    setTimeout(() => {
      uni.redirectTo({
        url: `/pages/club/activity-detail?id=${response.activityId}`
      })
    }, 1500)
  } catch (error: any) {
    console.error('发布活动失败:', error)
    uni.showToast({
      title: error.message || '发布失败，请重试',
      icon: 'none',
      duration: 2000
    })
  } finally {
    submitting.value = false
  }
}
// 页面加载
onLoad((options) => {
  initDateTimeRange()
  loadMyClubs()
  // 如果传入了clubId，直接选中
  if (options?.clubId) {
    formData.value.clubId = Number(options.clubId)
  }
  }
  }
  }
  }
  if (options && options.clubId) {
      formData.value.clubId = Number(options.clubId)
    }
  }
  })
  }
  }
  }
  }
  if (!options) {
      initDateTimeRange()
      loadMyClubs()
    }
  }
  }
}
</script>
<style lang="scss" scoped>
@import '@/styles/variables.scss';

.publish-activity-page {
  min-height: 100vh;
  background: $gray-50;
}
.content-area {
  height: calc(100vh - 56px);
}
.form-container {
  padding: $sp-4;
  padding-bottom: 100rpx;
}
// 表单卡片
.form-card {
  margin-bottom: $sp-4;
}
.card-header {
  display: flex;
  align-items: center;
  margin-bottom: $sp-4;
}
.header-icon {
  color: $primary;
    margin-right: $sp-2;
}
.header-title {
    font-size: $font-size-lg;
    font-weight: 600;
    color: $gray-900;
}
.header-optional {
    font-size: $font-size-sm;
    color: $gray-400;
    margin-left: $sp-2;
}
// 表单组
.form-group {
    margin-bottom: $sp-4;
}
.form-label {
    display: flex;
    align-items: center;
    margin-bottom: $sp-2;
}
.label-text {
    font-size: $font-size-base;
    color: $gray-700;
    font-weight: 500;
}
.required {
    color: $danger;
    margin-left: $sp-1;
}
.optional {
    font-size: $font-size-sm;
    color: $gray-400;
    margin-left: $sp-1;
}
.form-input {
  width: 100%;
  height: 88rpx;
    padding: $sp-3 $sp-4;
    background: $white;
    border: 2rpx solid $gray-200;
    border-radius: $radius-lg;
    font-size: $font-size-base;
    color: $gray-900;
    transition: border-color 0.2s;
  &:focus {
    border-color: $primary;
    outline: none;
  }
  &::placeholder {
    color: $gray-400;
  }
}
.form-textarea {
  width: 100%;
  min-height: 200rpx;
    padding: $sp-3$sp-4;
    background: $white;
    border: 2rpx solid $gray-200;
    border-radius: $radius-lg;
    font-size: $font-size-base;
    color: $gray-900;
    line-height: 1.6;
    transition: border-color 0.2s;
  &:focus {
    border-color: $primary;
    outline: none;
  }
  &::placeholder {
    color: $gray-400;
  }
}
.input-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: $sp-1;
}
.input-hint {
    font-size: $font-size-xs;
    color: $gray-400;
}
.input-count {
    font-size: $font-size-xs;
    color: $gray-400;
  &.count-warning {
    color: $warning;
  }
}
.error-text {
    font-size: $font-size-xs;
    color: $danger;
    margin-top: $sp-1;
}
// 选择器触发器
.picker-trigger {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 88rpx;
    padding: $sp-3$sp-4;
    background: $white;
    border: 2rpx solid $gray-200;
    border-radius: $radius-lg;
    transition: border-color 0.2s;
  &.error {
    border-color: $danger;
  }
  .placeholder {
    color: $gray-400;
  }
  .arrow {
    color: $gray-400;
    font-size: $font-size-sm;
  }
}
// 奖励选择器
.reward-selector {
    display: flex;
    flex-wrap: wrap;
    gap: $sp-2;
}
.reward-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: calc((100% - 40rpx) / 6);
    padding: $sp-2;
    background: $white;
    border: 2rpx solid $gray-200;
    border-radius: $radius-md;
    cursor: pointer;
    transition: all 0.2s;
  &:active {
    transform: scale(0.95);
    background: rgba($primary, 0.1);
    border-color: $primary;
  }
}
.reward-points {
    font-size: $font-size-lg;
    font-weight: 600;
    color: $gray-900;
}
.reward-label {
    font-size: $font-size-xs;
    color: $gray-500;
}
.reward-hint {
    display: flex;
    align-items: center;
    margin-top: $sp-2;
    padding: $sp-2 $sp-3;
    background: $gray-50;
    border-radius: $radius-sm;
}
.hint-icon {
    margin-right: $sp-1;
}
.hint-text {
    font-size: $font-size-xs;
    color: $gray-500;
  }
// 封面图片
.cover-preview {
    position: relative;
    border-radius: $radius-lg;
    overflow: hidden;
}
.cover-image {
    width: 100%;
    height: 300rpx;
}
.cover-actions {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    display: flex;
    justify-content: flex-end;
    padding: $sp-2;
    background: linear-gradient(transparent, rgba(0, 0, 0, 0.5));
}
.action-btn {
    display: flex;
    align-items: center;
    padding: $sp-1 $sp-2;
    background: rgba(255, 255, 255, 0.9);
    border-radius: $radius-sm;
}
.action-text {
    font-size: $font-size-xs;
    margin-left: $sp-1;
  &.danger {
    color: $danger;
  }
}
.upload-cover-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 300rpx;
    background: $white;
    border: 2rpx dashed $gray-300;
    border-radius: $radius-lg;
    cursor: pointer;
    transition: all 0.2s;
  &:active {
    background: $gray-50;
    border-color: $primary;
  }
}
.upload-icon {
    color: $gray-400;
    margin-bottom: $sp-2;
}
.upload-text {
    font-size: $font-size-base;
    color: $gray-600;
    margin-bottom: $sp-1;
}
.upload-hint {
    font-size: $font-size-xs;
    color: $gray-400;
  }
// 提交区域
.submit-section {
    margin-top: $sp-6;
    padding: $sp-4;
    background: $white;
    border-radius: $radius-lg;
}
.submit-hint {
    display: block;
    text-align: center;
    font-size: $font-size-xs;
    color: $gray-400;
    margin-top: $sp-2;
  }
// 社团选择弹窗
.picker-mask {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    z-index: 1000;
}
.picker-sheet {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    max-height: 70vh;
    background: $white;
    border-radius: $radius-xl $radius-xl 0 0;
    overflow: hidden;
}
.picker-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: $sp-4;
    border-bottom: 2rpx solid $gray-100;
}
.picker-title {
    font-size: $font-size-lg;
    font-weight: 600;
    color: $gray-900;
}
.picker-close {
    padding: $sp-2;
    cursor: pointer;
}
.picker-content {
    max-height: 60vh;
    padding: $sp-2;
}
.club-item {
    display: flex;
    align-items: center;
    padding: $sp-3$sp-4;
    border-radius: $radius-lg;
    cursor: pointer;
    transition: background 0.2s;
  &:active {
    background: rgba($primary, 0.1);
  }
}
.club-avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: $radius-md;
    margin-right: $sp-3;
}
.club-info {
    flex: 1;
}
.club-name {
    display: block;
    font-size: $font-size-base;
    font-weight: 500;
    color: $gray-900;
    margin-bottom: $sp-1;
}
.club-members {
    font-size: $font-size-sm;
    color: $gray-500;
  }
.empty-clubs {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: $sp-8;
}
.empty-text {
    font-size: $font-size-base;
    color: $gray-500;
    margin-bottom: $sp-4;
  }
</style>
