<template>
  <view class="activity-detail-page">
    <!-- 🎯 加载中状态 -->
    <view v-if="loading" class="loading-container">
      <view class="skeleton-banner"></view>
      <view class="skeleton-content">
        <view class="skeleton-line skeleton-title"></view>
        <view class="skeleton-line skeleton-text"></view>
        <view class="skeleton-line skeleton-text"></view>
      </view>
    </view>

    <!-- 🎯 错误状态 -->
    <view v-else-if="error" class="error-container">
      <text class="error-icon">⚠️</text>
      <text class="error-title">加载失败</text>
      <text class="error-desc">{{ errorMessage }}</text>
      <view class="error-btn" @click="loadActivityDetail">
        <text class="error-btn-text">重试</text>
      </view>
    </view>

    <!-- 🎯 正常内容 -->
    <view v-else class="detail-content">
      <!-- ① 活动Banner大图 -->
      <view class="activity-banner">
        <image
          class="banner-image"
          :src="activity.coverImage || defaultCover"
          mode="aspectFill"
        />
      </view>

      <!-- ② 活动主体信息 -->
      <view class="activity-main">
        <!-- 标题 + 标签 -->
        <view class="title-section">
          <text class="activity-title">{{ activity.title }}</text>
          <view class="activity-tags">
            <view v-if="isHot" class="tag tag-hot">🔥 热门</view>
            <view v-if="isNew" class="tag tag-new">🆕 新发布</view>
            <view v-if="isUrgent" class="tag tag-urgent">⏰ 即将开始</view>
            <view v-if="isCrowded" class="tag tag-crowded">🎯 名额紧张</view>
          </view>
        </view>

        <!-- 基本信息卡片 -->
        <view class="info-card">
          <view class="info-item">
            <text class="info-icon">📅</text>
            <view class="info-content">
              <text class="info-label">活动时间</text>
              <text class="info-value">{{ formatTime(activity.startTime) }} ~ {{ formatTime(activity.endTime) }}</text>
            </view>
          </view>

          <view class="info-item">
            <text class="info-icon">📍</text>
            <view class="info-content">
              <text class="info-label">活动地点</text>
              <text class="info-value">{{ activity.location }}</text>
            </view>
          </view>

          <view class="info-item" @click="goToClub">
            <text class="info-icon">👥</text>
            <view class="info-content">
              <text class="info-label">主办方</text>
              <text class="info-value clickable">{{ activity.clubName }}</text>
            </view>
            <text class="info-arrow">→</text>
          </view>

          <view class="info-item">
            <text class="info-icon">🎫</text>
            <view class="info-content">
              <text class="info-label">报名人数</text>
              <text class="info-value" :class="{ 'number-highlight': numberHighlight }">
                {{ activity.currentParticipants }} / {{ activity.maxParticipants }}
              </text>
            </view>
          </view>
        </view>

        <!-- 报名状态条 -->
        <view class="signup-status">
          <view class="status-bar">
            <view class="status-progress" :style="{ width: progressWidth + '%' }"></view>
          </view>
          <view class="status-text">
            <text class="remaining">剩余名额：{{ remainingSlots }}</text>
            <text class="filled">已报名：{{ activity.currentParticipants }} 人</text>
          </view>
        </view>

        <!-- 活动详情 -->
        <view class="detail-section">
          <text class="section-title">活动详情</text>
          <text class="section-content">{{ activity.description }}</text>
        </view>
      </view>
    </view>

    <!-- ③ 底部固定报名按钮 -->
    <view v-if="!loading && !error" class="bottom-bar">
      <view class="button-wrapper" @click="handleSignupAction">
        <button
          class="signup-button"
          :class="[buttonClass, { 'button-clicked': isButtonClicked }]"
          :disabled="buttonDisabled"
        >
          {{ buttonText }}
          <!-- 🎯 波纹效果容器 -->
          <view v-if="showRipple" class="ripple" :style="rippleStyle"></view>
        </button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getActivityDetail, joinActivity, cancelActivity } from '@/services/activity'

// 🎯 路由参数
const activityId = ref<number>(0)

// 🎯 页面状态
const loading = ref(true)
const error = ref(false)
const errorMessage = ref('')

// 🎯 按钮点击动画状态
const isButtonClicked = ref(false)
const showRipple = ref(false)
const rippleStyle = ref({})

// 🎯 数字变化闪烁动画状态
const numberHighlight = ref(false)

// 🎯 活动数据
interface ActivityDetail {
  activityId: number
  title: string
  description: string
  coverImage?: string
  location: string
  startTime: string
  endTime: string
  currentParticipants: number
  maxParticipants: number
  clubId: number
  clubName: string
  isJoined: boolean
  status: number  // 0=未开始 1=进行中 2=已结束
  createdAt: string
}

const activity = ref<ActivityDetail>({
  activityId: 0,
  title: '',
  description: '',
  location: '',
  startTime: '',
  endTime: '',
  currentParticipants: 0,
  maxParticipants: 0,
  clubId: 0,
  clubName: '',
  isJoined: false,
  status: 0,
  createdAt: ''
})

const defaultCover = 'https://picsum.photos/750/400?random=detail'

/**
 * 🎯 计算属性：剩余名额
 */
const remainingSlots = computed(() => {
  return Math.max(0, activity.value.maxParticipants - activity.value.currentParticipants)
})

/**
 * 🎯 计算属性：进度百分比
 */
const progressWidth = computed(() => {
  if (activity.value.maxParticipants === 0) return 0
  const percentage = (activity.value.currentParticipants / activity.value.maxParticipants) * 100
  return Math.min(100, Math.max(0, percentage))
})

/**
 * 🎯 计算属性：活动状态标签
 */
const isHot = computed(() => {
  // 热门：报名人数超过50%
  return activity.value.currentParticipants / activity.value.maxParticipants > 0.5
})

const isNew = computed(() => {
  // 新发布：创建时间小于24小时
  const createdTime = new Date(activity.value.createdAt).getTime()
  const now = Date.now()
  return now - createdTime < 24 * 60 * 60 * 1000
})

const isUrgent = computed(() => {
  // 即将开始：距开始时间小于3天
  const startTime = new Date(activity.value.startTime).getTime()
  const now = Date.now()
  return startTime - now < 3 * 24 * 60 * 60 * 1000 && startTime > now
})

const isCrowded = computed(() => {
  // 名额紧张：剩余名额小于20%
  return remainingSlots.value / activity.value.maxParticipants < 0.2 && remainingSlots.value > 0
})

/**
 * 🎯 计算属性：按钮状态
 */
type ButtonStatus = 'available' | 'registered' | 'full' | 'ongoing' | 'ended'

const buttonStatus = computed<ButtonStatus>(() => {
  if (activity.value.status === 2) return 'ended'
  if (activity.value.status === 1) return 'ongoing'
  if (activity.value.isJoined) return 'registered'
  if (remainingSlots.value <= 0) return 'full'
  return 'available'
})

const buttonText = computed(() => {
  const textMap = {
    available: '立即报名',
    registered: '已报名',
    full: '名额已满',
    ongoing: '活动进行中',
    ended: '活动已结束'
  }
  return textMap[buttonStatus.value]
})

const buttonClass = computed(() => {
  return `button-${buttonStatus.value}`
})

const buttonDisabled = computed(() => {
  return ['full', 'ongoing', 'ended'].includes(buttonStatus.value)
})

/**
 * 🎯 加载活动详情
 */
const loadActivityDetail = async () => {
  loading.value = true
  error.value = false

  try {
    const res = await getActivityDetail(activityId.value)
    activity.value = res
    loading.value = false
  } catch (err: any) {
    console.error('加载活动详情失败:', err)
    error.value = true
    errorMessage.value = err.message || '网络异常，请稍后重试'
    loading.value = false
  }
}

/**
 * 🎯 处理报名操作
 */
const handleSignupAction = async (e?: any) => {
  // 🎯 触发点击缩放动画
  isButtonClicked.value = true
  setTimeout(() => {
    isButtonClicked.value = false
  }, 300)

  // 🎯 触发波纹效果（仅在可点击状态）
  if (!buttonDisabled.value && e) {
    // 计算波纹位置（相对按钮）
    const rect = e.currentTarget?.getBoundingClientRect?.() || { left: 0, top: 0 }
    const x = (e.clientX || e.touches?.[0]?.clientX || 0) - rect.left
    const y = (e.clientY || e.touches?.[0]?.clientY || 0) - rect.top

    rippleStyle.value = {
      left: x + 'px',
      top: y + 'px'
    }

    showRipple.value = true
    setTimeout(() => {
      showRipple.value = false
    }, 600)
  }

  if (buttonStatus.value === 'available') {
    // 报名
    await handleJoinActivity()
  } else if (buttonStatus.value === 'registered') {
    // 取消报名（二次确认）
    uni.showModal({
      title: '取消报名',
      content: '确定要取消报名吗？',
      success: (res) => {
        if (res.confirm) {
          handleCancelActivity()
        }
      }
    })
  }
}

/**
 * 🎯 报名活动
 */
const handleJoinActivity = async () => {
  try {
    await joinActivity(activityId.value)
    uni.showToast({
      title: '报名成功',
      icon: 'success',
      duration: 2000
    })

    // 更新状态
    activity.value.isJoined = true
    activity.value.currentParticipants++

    // 🎯 触发数字闪烁动画
    triggerNumberHighlight()

    // 🎯 触发全局事件，通知首页刷新活动数据
    uni.$emit('activity-status-changed', {
      activityId: activityId.value,
      isJoined: true
    })

    // 可选：弹窗询问是否加入日历提醒
    setTimeout(() => {
      uni.showModal({
        title: '报名成功',
        content: '是否添加到日历提醒？',
        success: (res) => {
          if (res.confirm) {
            // TODO: 添加到日历功能
            uni.showToast({ title: '已添加提醒', icon: 'success' })
          }
        }
      })
    }, 1500)
  } catch (err: any) {
    console.error('报名失败:', err)
    uni.showToast({
      title: err.message || '报名失败',
      icon: 'none',
      duration: 2000
    })
  }
}

/**
 * 🎯 取消报名
 */
const handleCancelActivity = async () => {
  try {
    await cancelActivity(activityId.value)
    uni.showToast({
      title: '已取消报名',
      icon: 'success',
      duration: 2000
    })

    // 更新状态
    activity.value.isJoined = false
    activity.value.currentParticipants--

    // 🎯 触发数字闪烁动画
    triggerNumberHighlight()

    // 🎯 触发全局事件，通知首页刷新活动数据
    uni.$emit('activity-status-changed', {
      activityId: activityId.value,
      isJoined: false
    })
  } catch (err: any) {
    console.error('取消报名失败:', err)
    uni.showToast({
      title: err.message || '取消失败',
      icon: 'none',
      duration: 2000
    })
  }
}

/**
 * 🎯 触发数字高亮动画
 */
const triggerNumberHighlight = () => {
  numberHighlight.value = true
  setTimeout(() => {
    numberHighlight.value = false
  }, 800)
}

/**
 * 🎯 跳转到社团主页
 */
const goToClub = () => {
  uni.navigateTo({
    url: `/pages/club/detail?id=${activity.value.clubId}`,
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    }
  })
}

/**
 * 🎯 格式化时间
 */
const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = date.getHours().toString().padStart(2, '0')
  const minute = date.getMinutes().toString().padStart(2, '0')
  return `${month}月${day}日 ${hour}:${minute}`
}

/**
 * 🎯 页面加载
 */
onMounted(() => {
  // 获取路由参数
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1] as any
  activityId.value = Number(currentPage.options.id || currentPage.options.activityId)

  if (!activityId.value) {
    error.value = true
    errorMessage.value = '活动ID不存在'
    loading.value = false
    return
  }

  loadActivityDetail()
})
</script>

<style scoped lang="scss">
/* ========== 🎯 设计系统变量（最终版统一规范）========== */
:root {
  /* 圆角 */
  --radius-large: 24rpx; /* 12px */
  --radius-medium: 16rpx; /* 8px */

  /* 间距 */
  --spacing-xs: 16rpx; /* 8px */
  --spacing-sm: 24rpx; /* 12px */
  --spacing-md: 32rpx; /* 16px */
  --spacing-lg: 40rpx; /* 20px */
  --spacing-xl: 48rpx; /* 24px */

  /* 颜色 */
  --color-primary: #2F6AFF;
  --color-accent: #FF7A00;
  --color-yellow: #FFB800;
  --color-blue-light: #5B8DFF;
  --color-bg-card: #FFFFFF;
  --color-bg-page: #F6F8FB;
  --color-text-main: #1A1A1A;
  --color-text-secondary: #666666;
  --color-text-tertiary: #777777;
  --color-border: #E5E7EB;
}

/* ========== 页面容器 ========== */
.activity-detail-page {
  min-height: 100vh;
  background: var(--color-bg-page);
  padding-bottom: 120rpx; /* 为底部按钮留空间 */
}

/* ========== 加载骨架屏 ========== */
.loading-container {
  padding: 0;
}

.skeleton-banner {
  width: 100%;
  height: 400rpx;
  background: linear-gradient(90deg, #F0F0F0 25%, #E0E0E0 50%, #F0F0F0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-content {
  padding: 32rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.skeleton-line {
  height: 32rpx;
  background: linear-gradient(90deg, #F0F0F0 25%, #E0E0E0 50%, #F0F0F0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 8rpx;
}

.skeleton-title {
  width: 80%;
  height: 48rpx;
}

.skeleton-text {
  width: 100%;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* ========== 错误状态 ========== */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
  padding: 64rpx 32rpx;
}

.error-icon {
  font-size: 120rpx;
  line-height: 1;
  margin-bottom: 32rpx;
}

.error-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #1A1A1A;
  margin-bottom: 16rpx;
}

.error-desc {
  font-size: 28rpx;
  color: #666666;
  text-align: center;
  margin-bottom: 48rpx;
}

.error-btn {
  padding: 20rpx 64rpx;
  background: #2F6AFF;
  border-radius: 48rpx;
  cursor: pointer;
}

.error-btn-text {
  font-size: 32rpx;
  font-weight: 600;
  color: white;
}

/* ========== 活动Banner ========== */
.activity-banner {
  width: 100%;
  height: 280rpx; /* 🎯 优化：从 400rpx 缩至 280rpx，减少空旷感 */
  position: relative;
  overflow: hidden;
  border-radius: 0 0 24rpx 24rpx; /* 🎯 最终版：底部圆角 12px，衔接更自然 */

  /* 🎯 最终版：底部渐变遮罩，让内容区更稳 */
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 80rpx;
    background: linear-gradient(to top, rgba(0, 0, 0, 0.08), transparent); /* 🎯 从 0.15 降至 0.08，更自然轻盈 */
    pointer-events: none;
  }
}

.banner-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* ========== 活动主体内容 ========== */
.activity-main {
  background: white;
  border-radius: 32rpx 32rpx 0 0;
  margin-top: -32rpx;
  position: relative;
  z-index: 1;
  padding: 48rpx 32rpx 96rpx; /* 🎯 最终版：顶部 24px，底部 60-80px 预留空隙 */
  max-width: 860px; /* 🎯 最终版：Web 端 740-860px */
  margin-left: auto;
  margin-right: auto;
}

/* ========== 标题区域 ========== */
.title-section {
  margin-bottom: 32rpx; /* 🎯 最终版：标签区 → 信息卡片 16px */
}

.activity-title {
  font-size: 48rpx; /* 🎯 最终版：Web 24px，H5 20px */
  font-weight: 600; /* 🎯 最终版：600 字重 */
  color: #1A1A1A;
  line-height: 1.4;
  display: block;
  margin-bottom: 24rpx; /* 🎯 最终版：标题 → 标签 12px */
}

.activity-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
}

.tag {
  padding: 8rpx 16rpx; /* 🎯 最终版：4px 8px */
  border-radius: 12rpx; /* 🎯 最终版：6px */
  font-size: 24rpx; /* 🎯 最终版：12px */
  font-weight: 600;
  line-height: 1;
}

.tag-hot {
  background: #FF7A00; /* 🎯 最终版：热门活动橙色 */
  color: white;
}

.tag-new {
  background: #5B8DFF; /* 🎯 最终版：新发布浅蓝色 */
  color: white;
}

.tag-urgent {
  background: #FFB800; /* 🎯 最终版：即将开始黄色 */
  color: white;
}

.tag-crowded {
  background: #FF7A00; /* 🎯 最终版：名额紧张橙色 */
  color: white;
}

/* ========== 基本信息卡片 ========== */
.info-card {
  background: #FFFFFF; /* 🎯 最终版：纯白背景 */
  border-radius: 24rpx; /* 🎯 最终版：12px 圆角 */
  padding: 40rpx; /* 🎯 最终版：Web 20px, H5 16px */
  margin-bottom: 32rpx; /* 🎯 最终优化：信息卡 → 进度条 16px（减少空白） */
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 24rpx; /* 🎯 最终版：图标与文本间距 12px */
  padding: 24rpx 0; /* 🎯 最终版：行间距 Web 12px, H5 10px */
  border-bottom: 1rpx solid #E5E7EB;
}

.info-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.info-icon {
  font-size: 36rpx; /* 🎯 最终版：图标 16-18px */
  line-height: 1;
  flex-shrink: 0;
  color: #5B8DFF; /* 🎯 最终版：统一主蓝色 */
}

.info-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.info-label {
  font-size: 26rpx; /* 🎯 最终版：13px */
  color: #777777; /* 🎯 最终版：#777 */
  line-height: 1;
}

.info-value {
  font-size: 30rpx; /* 🎯 最终版：14-15px */
  color: #333333;
  font-weight: 500;
  line-height: 1.4;
  transition: all 0.3s ease;
}

/* 🎯 数字高亮闪烁动画 */
.info-value.number-highlight {
  animation: number-pulse 0.8s ease-out;
}

@keyframes number-pulse {
  0%, 100% {
    color: #333333;
    transform: scale(1);
  }
  25% {
    color: #FF6B00;
    transform: scale(1.15);
    font-weight: 700;
  }
  50% {
    color: #FF6B00;
    transform: scale(1.1);
  }
  75% {
    color: #FF8A3D;
    transform: scale(1.05);
  }
}

.info-value.clickable {
  color: #2F6AFF;
}

.info-arrow {
  font-size: 24rpx;
  color: #999999;
  line-height: 1;
}

/* ========== 报名状态条 ========== */
.signup-status {
  background: transparent; /* 🎯 最终版：透明背景 */
  padding: 0;
  margin-bottom: 64rpx; /* 🎯 最终版：进度条 → 活动详情 32px */
}

.status-bar {
  width: 100%;
  height: 16rpx; /* 🎯 最终版：Web 6px + H5 8px，使用 8px = 16rpx 中间值 */
  background: #E5E7EB;
  border-radius: 12rpx; /* 🎯 最终版：6px 圆角 */
  overflow: hidden;
  margin-bottom: 16rpx; /* 🎯 最终版：进度条 → 名额文案 8px */
}

.status-progress {
  height: 100%;
  background: #2F6AFF; /* 🎯 最终版：纯色，不再用渐变 */
  border-radius: 12rpx;
  transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.status-text {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.remaining {
  font-size: 26rpx; /* 🎯 最终版：13px */
  color: #444444; /* 🎯 最终版：#444 */
  font-weight: 600;
}

.filled {
  font-size: 26rpx; /* 🎯 最终版：13px */
  color: #444444; /* 🎯 最终版：#444 */
  font-weight: 400;
}

/* ========== 活动详情 ========== */
.detail-section {
  margin-bottom: 32rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1A1A1A;
  display: block;
  margin-bottom: 16rpx;
}

.section-content {
  font-size: 28rpx;
  color: #333333;
  line-height: 1.8;
  white-space: pre-wrap;
}

/* ========== 底部固定报名按钮 ========== */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 24rpx 32rpx;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.08);
  z-index: 100;

  /* 🎯 优化：Web 端居中显示，避免过宽 */
  display: flex;
  justify-content: center;
}

.button-wrapper {
  position: relative;
  width: 100%;
  max-width: 1440rpx; /* 🎯 最终版：720px Web 端按钮最大宽度 */
}

.signup-button {
  position: relative;
  width: 100%;
  height: 96rpx; /* 🎯 最终版：48px Web 端 / 52px H5 端 */
  border-radius: 16rpx; /* 🎯 最终版：8px 圆角 */
  font-size: 32rpx; /* 🎯 最终版：16px */
  font-weight: 600; /* 🎯 最终版：600 字重 */
  line-height: 96rpx;
  text-align: center;
  border: none;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

/* 🎯 点击缩放动画 */
.signup-button.button-clicked {
  transform: scale(0.96);
}

/* 🎯 波纹效果 */
.ripple {
  position: absolute;
  width: 20rpx;
  height: 20rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.6);
  transform: translate(-50%, -50%) scale(0);
  animation: ripple-animation 0.6s ease-out;
  pointer-events: none;
}

@keyframes ripple-animation {
  0% {
    transform: translate(-50%, -50%) scale(0);
    opacity: 1;
  }
  100% {
    transform: translate(-50%, -50%) scale(40);
    opacity: 0;
  }
}

/* 按钮状态样式 */
.button-available {
  background: linear-gradient(135deg, #2F6AFF 0%, #5B8DFF 100%);
  color: white;
  box-shadow: 0 4rpx 16rpx rgba(47, 106, 255, 0.3);
  transition: all 0.3s ease;
}

.button-available:hover {
  box-shadow: 0 6rpx 20rpx rgba(47, 106, 255, 0.4);
  transform: translateY(-2rpx);
}

.button-registered {
  background: linear-gradient(135deg, #2FBD6A 0%, #4DD88E 100%);
  color: white;
}

.button-full,
.button-ongoing,
.button-ended {
  background: #E5E5E5;
  color: #999999;
  cursor: not-allowed;
}

/* ========== 响应式适配 ========== */
@media (max-width: 750px) {
  .activity-banner {
    height: 440rpx;
  }

  .activity-title {
    font-size: 40rpx;
  }

  /* 🎯 最终版：H5 端按钮规范 */
  .signup-button {
    height: 104rpx; /* 52px H5 端 */
    line-height: 104rpx;
    border-radius: 0; /* H5 端无圆角，全宽悬浮 */
    font-size: 34rpx; /* 17px */
  }
}
</style>
