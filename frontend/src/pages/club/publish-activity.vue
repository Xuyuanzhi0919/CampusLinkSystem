<template>
  <view class="publish-activity-page">

    <!-- 统一导航栏 -->
    <CNavBar title="发布活动" :auto-back="false" @back="handleCancel">
      <template #right>
        <!-- PC 端右上角发布按钮（移动端隐藏） -->
        <view class="navbar-publish">
          <CButton
            type="primary"
            size="sm"
            :disabled="!isFormValid"
            :loading="submitting"
            @click="handleSubmit"
          >
            {{ submitting ? '发布中...' : '发布活动' }}
          </CButton>
        </view>
      </template>
    </CNavBar>

    <scroll-view class="content-area" scroll-y>
      <view class="page-layout">
      <view class="form-container">

        <!-- 所属社团 -->
        <CCard variant="elevated" class="form-card">
          <view class="card-header">
            <Icon name="users" :size="20" class="header-icon" />
            <text class="header-title">所属社团</text>
          </view>
          <view class="form-group">
            <view class="club-selector" @click="showClubPicker = true">
              <view v-if="selectedClub" class="club-selected">
                <image :src="selectedClub.logoUrl || '/static/default-avatar.png'" class="club-avatar-sm" mode="aspectFill" />
                <text class="club-selected-name">{{ selectedClub.clubName }}</text>
              </view>
              <text v-else class="picker-placeholder">请选择所属社团</text>
              <Icon name="chevron-right" :size="16" color="#9CA3AF" />
            </view>
            <text v-if="errors.clubId" class="error-text">{{ errors.clubId }}</text>
          </view>
        </CCard>

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
                :class="{ active: formData.rewardPoints === points }"
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

      </view>

      <!-- PC 端右侧栏（移动端隐藏） -->
      <view class="activity-sidebar">

        <!-- 积分奖励 -->
        <view class="sidebar-card">
          <view class="sidebar-card__header">
            <Icon name="gift" :size="16" class="sidebar-card__icon" />
            <text class="sidebar-card__title">活动奖励</text>
          </view>
          <view class="reward-list">
            <view class="reward-row">
              <text class="reward-dot">•</text>
              <text class="reward-desc">参与者签到后获得 <text class="reward-em">+10 积分</text></text>
            </view>
            <view class="reward-row">
              <text class="reward-dot">•</text>
              <text class="reward-desc">可自定义额外奖励积分（最高 50）</text>
            </view>
            <view class="reward-row">
              <text class="reward-dot">•</text>
              <text class="reward-desc">活动越精彩，社团影响力越高</text>
            </view>
          </view>
        </view>

        <!-- 发布建议 -->
        <view class="sidebar-card">
          <view class="sidebar-card__header">
            <Icon name="lightbulb" :size="16" class="sidebar-card__icon" />
            <text class="sidebar-card__title">填写建议</text>
          </view>
          <view class="guide-steps">
            <view class="guide-step">
              <view class="step-num">1</view>
              <text class="step-text">标题简洁明了，突出活动亮点</text>
            </view>
            <view class="guide-step">
              <view class="step-num">2</view>
              <text class="step-text">描述包含参与要求和注意事项</text>
            </view>
            <view class="guide-step">
              <view class="step-num">3</view>
              <text class="step-text">地点填写具体楼栋/房间号</text>
            </view>
            <view class="guide-step">
              <view class="step-num">4</view>
              <text class="step-text">结束时间至少比开始晚 30 分钟</text>
            </view>
          </view>
        </view>

        <!-- 注意事项 -->
        <view class="sidebar-card">
          <view class="sidebar-card__header">
            <Icon name="alert-circle" :size="16" class="sidebar-card__icon sidebar-card__icon--warn" />
            <text class="sidebar-card__title">注意事项</text>
          </view>
          <view class="notice-list">
            <view class="notice-item">
              <Icon name="clock" :size="13" class="notice-icon" />
              <text class="notice-text">活动开始时间须为未来时间</text>
            </view>
            <view class="notice-item">
              <Icon name="users" :size="13" class="notice-icon" />
              <text class="notice-text">参与人数上限建议结合场地容量设置</text>
            </view>
            <view class="notice-item">
              <Icon name="info" :size="13" class="notice-icon" />
              <text class="notice-text">发布后活动信息将对全体用户可见</text>
            </view>
            <view class="notice-item">
              <Icon name="alert-triangle" :size="13" class="notice-icon notice-icon--warn" />
              <text class="notice-text">虚假/违规活动将被下架并扣除积分</text>
            </view>
          </view>
        </view>

      </view>
      </view><!-- end page-layout -->
    </scroll-view>

    <!-- 底部操作栏（移动端固定，PC 端隐藏） -->
    <view class="bottom-bar">
      <view class="bottom-bar-inner">
        <CButton type="ghost" size="lg" class="btn-cancel" @click="handleCancel">取消</CButton>
        <CButton
          type="primary"
          size="lg"
          class="btn-submit"
          :disabled="!isFormValid"
          :loading="submitting"
          @click="handleSubmit"
        >
          {{ submitting ? '发布中...' : '发布活动' }}
        </CButton>
      </view>
    </view>

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
          >
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
import CCard from '@/components/ui/CCard.vue'
import CButton from '@/components/ui/CButton.vue'
import Icon from '@/components/icons/index.vue'
import CNavBar from '@/components/layout/CNavBar.vue'
import { createActivity, getMyManagedClubs } from '@/services/club'
import type { ClubItem } from '@/types/club'
import { uploadToOSS } from '@/utils/upload'

// 表单数据（clubId 仅用于 UI 状态，提交时作为路径参数）
const formData = ref({
  clubId: 0,
  title: '',
  description: '',
  coverImage: '',
  location: '',
  startTime: '',
  endTime: '',
  maxParticipants: 50,
  rewardPoints: 0,
})

// 表单错误
const errors = ref<Record<string, string>>({})

// 提交状态
const submitting = ref(false)
// 社团选择
const showClubPicker = ref(false)
const myClubs = ref<ClubItem[]>([])
// 当前选中的社团
const selectedClub = computed(() =>
  myClubs.value.find(c => c.clubId === formData.value.clubId) || null
)
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
/**
 * 选择奖励积分
 */
const handleRewardSelect = (points: number) => {
  formData.value.rewardPoints = points
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
          // 先获取OSS签名
          const { getOSSSignature } = await import('@/utils/upload')
          const signature = await getOSSSignature(tempFilePath.split('/').pop() || 'cover.jpg')
          const imageUrl = await uploadToOSS(tempFilePath, signature)
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
    const result = await getMyManagedClubs({ page: 1, pageSize: 100 })
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
    const { clubId, ...activityData } = formData.value
    const response = await createActivity(clubId, activityData)
    uni.showToast({
      title: '发布成功',
      icon: 'success',
      duration: 1500
    })
    setTimeout(() => {
      uni.redirectTo({
        url: `/pages/club/activity-detail?id=${response}`
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
})
</script>
<style lang="scss" scoped>
@import '@/styles/variables.scss';

.publish-activity-page {
  height: 100vh;
  background: #F1F5F9;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

// ── 导航栏发布按钮（PC 端显示，移动端隐藏）──
.navbar-publish {
  @media (max-width: 749px) {
    display: none;
  }
}

.content-area {
  flex: 1;
  height: 0;
  width: 100%;
}

// PC 端双栏布局容器
.page-layout {
  display: flex;
  align-items: flex-start;
  gap: 24px;
  width: 100%;
  max-width: 1100px;
  margin: 0 auto;
  padding: $sp-4;
  box-sizing: border-box;

  @media (max-width: 749px) {
    flex-direction: column;
    padding: 0;
    gap: 0;
  }
}

.form-container {
  flex: 1;
  min-width: 0;

  @media (max-width: 749px) {
    padding: $sp-4;
    padding-bottom: 80px;
  }
}

// PC 端右侧栏
.activity-sidebar {
  width: 300px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 16px;

  @media (max-width: 749px) {
    display: none;
  }
}

.sidebar-card {
  background: $white;
  border: 1px solid $gray-200;
  border-radius: $radius-lg;
  padding: $sp-4;

  &__header {
    display: flex;
    align-items: center;
    gap: $sp-2;
    margin-bottom: $sp-3;
    padding-bottom: $sp-3;
    border-bottom: 1px solid $gray-100;
  }

  &__icon {
    color: $primary;
    flex-shrink: 0;

    &--warn {
      color: $warning;
    }
  }

  &__title {
    font-size: $font-size-sm;
    font-weight: 600;
    color: $gray-800;
  }
}

.reward-list {
  display: flex;
  flex-direction: column;
  gap: $sp-2;
}

.reward-row {
  display: flex;
  align-items: flex-start;
  gap: $sp-2;
  font-size: $font-size-xs;
  color: $gray-600;
  line-height: 1.5;
}

.reward-dot {
  color: $gray-400;
  flex-shrink: 0;
  font-weight: bold;
}

.reward-em {
  color: $accent;
  font-weight: 600;
}

.guide-steps {
  display: flex;
  flex-direction: column;
  gap: $sp-2;
}

.guide-step {
  display: flex;
  align-items: flex-start;
  gap: $sp-2;
}

.step-num {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: $primary;
  color: $white;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 1px;
}

.step-text {
  font-size: $font-size-xs;
  color: $gray-600;
  line-height: 1.5;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: $sp-2;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  gap: $sp-2;
  font-size: $font-size-xs;
  color: $gray-600;
  line-height: 1.5;
}

.notice-icon {
  color: $gray-400;
  flex-shrink: 0;
  margin-top: 1px;

  &--warn {
    color: $warning;
  }
}

.notice-text {
  flex: 1;
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

// 社团选择器
.club-selector {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 $sp-3;
  background: $gray-50;
  border: 1px solid $gray-200;
  border-radius: $radius-base;
  cursor: pointer;
  &:active { background: $gray-100; }
}

.club-selected {
  display: flex;
  align-items: center;
  gap: $sp-2;
  flex: 1;
}

.club-avatar-sm {
  width: 28px;
  height: 28px;
  border-radius: 8px;
}

.club-selected-name {
  font-size: $font-size-base;
  color: $gray-900;
  font-weight: 500;
}

.picker-placeholder {
  font-size: $font-size-base;
  color: $gray-400;
  flex: 1;
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

.label-hint {
  font-size: $font-size-sm;
  color: $gray-400;
  margin-left: $sp-1;
}

.required {
  color: $error;
  margin-left: $sp-1;
}

.optional {
  font-size: $font-size-sm;
  color: $gray-400;
  margin-left: $sp-1;
}

.form-input {
  width: 100%;
  height: 44px;
  padding: 0 $sp-3;
  background: $white;
  border: 1px solid $gray-200;
  border-radius: $radius-base;
  font-size: $font-size-base;
  color: $gray-900;
  transition: border-color 0.2s;
  box-sizing: border-box;
  &:focus { border-color: $primary; }
  &::placeholder { color: $gray-400; }
}

.form-textarea {
  width: 100%;
  min-height: 100px;
  padding: $sp-3 $sp-4;
  background: $white;
  border: 1px solid $gray-200;
  border-radius: $radius-base;
  font-size: $font-size-base;
  color: $gray-900;
  line-height: 1.6;
  transition: border-color 0.2s;
  box-sizing: border-box;
  &:focus { border-color: $primary; }
  &::placeholder { color: $gray-400; }
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
  &.count-warning { color: $warning; }
}

.error-text {
  font-size: $font-size-xs;
  color: $error;
  margin-top: $sp-1;
  display: block;
}

// 选择器触发器
.picker-trigger {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 44px;
  padding: 0 $sp-3;
  background: $white;
  border: 1px solid $gray-200;
  border-radius: $radius-base;
  transition: border-color 0.2s;
  &.error { border-color: $error; }
  .placeholder { color: $gray-400; }
  .arrow { color: $gray-400; font-size: $font-size-sm; }
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
  border: 1px solid $gray-200;
  border-radius: $radius-md;
  cursor: pointer;
  transition: all 0.2s;
  &.active {
    background: rgba($primary, 0.08);
    border-color: $primary;
    .reward-points { color: $primary; }
  }
  &:active { transform: scale(0.95); }
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

.hint-icon { margin-right: $sp-1; }
.hint-text { font-size: $font-size-xs; color: $gray-500; }

// 封面图片
.cover-preview {
  position: relative;
  border-radius: $radius-lg;
  overflow: hidden;
}

.cover-image { width: 100%; height: 300rpx; }

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
  &.danger { color: $error; }
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
  &:active { background: $gray-50; border-color: $primary; }
}

.upload-icon { color: $gray-400; margin-bottom: $sp-2; }
.upload-text { font-size: $font-size-base; color: $gray-600; margin-bottom: $sp-1; }
.upload-hint { font-size: $font-size-xs; color: $gray-400; }

// ── 底部操作栏（移动端固定，PC 端隐藏）──
.bottom-bar {
  @media (min-width: 750px) {
    display: none;
  }

  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: $white;
  border-top: 1px solid $gray-200;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.06);
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.bottom-bar-inner {
  display: flex;
  gap: $sp-3;
  padding: $sp-4 $sp-5;
}

.btn-cancel {
  flex: 0 0 auto;
}

.btn-submit {
  flex: 1;
}

// 社团选择弹窗
.picker-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
}

.picker-sheet {
  position: absolute;
  bottom: 0; left: 0; right: 0;
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
  border-bottom: 1px solid $gray-100;
}

.picker-title { font-size: $font-size-lg; font-weight: 600; color: $gray-900; }
.picker-close { padding: $sp-2; cursor: pointer; }

.picker-content { max-height: 60vh; padding: $sp-2; }

.club-item {
  display: flex;
  align-items: center;
  padding: $sp-3 $sp-4;
  border-radius: $radius-lg;
  cursor: pointer;
  transition: background 0.2s;
  &:active { background: rgba($primary, 0.1); }
}

.club-avatar { width: 80rpx; height: 80rpx; border-radius: $radius-md; margin-right: $sp-3; }
.club-info { flex: 1; }
.club-name { display: block; font-size: $font-size-base; font-weight: 500; color: $gray-900; margin-bottom: $sp-1; }
.club-members { font-size: $font-size-sm; color: $gray-500; }

.empty-clubs { display: flex; flex-direction: column; align-items: center; padding: $sp-8; }
.empty-text { font-size: $font-size-base; color: $gray-500; margin-bottom: $sp-4; }
</style>
