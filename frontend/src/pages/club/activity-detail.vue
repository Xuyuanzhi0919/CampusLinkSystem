<template>
  <view class="activity-detail-page">
    <!-- 顶部导航栏 -->
    <CNavBar
      :title="activity.title || '活动详情'"
      :show-back="true"
      :border="false"
    />

    <!-- 🎯 加载骨架屏 -->
    <view v-if="loading" class="loading-container">
      <view class="skeleton-banner"></view>
      <view class="skeleton-info-grid">
        <view class="skeleton-info-card skeleton-info-card--full"></view>
        <view class="skeleton-info-card"></view>
        <view class="skeleton-info-card"></view>
      </view>
      <view class="skeleton-content">
        <view class="skeleton-line skeleton-title"></view>
        <view class="skeleton-line skeleton-text"></view>
        <view class="skeleton-line skeleton-text"></view>
        <view class="skeleton-line skeleton-text-short"></view>
      </view>
    </view>

    <!-- 🎯 错误状态 -->
    <view v-else-if="error" class="error-container">
      <Icon name="alert-triangle" :size="48" :stroke-width="1.5" class="error-icon" />
      <text class="error-title">加载失败</text>
      <text class="error-desc">{{ errorMessage }}</text>
      <view class="error-btn" @click="loadActivityDetail">
        <text class="error-btn-text">重试</text>
      </view>
    </view>

    <!-- 🎯 正常内容 -->
    <view v-else class="detail-content">

      <!-- ① Hero Banner：图片 + 渐变遮罩 + 标题/状态/收藏 -->
      <view class="activity-banner">
        <image
          class="banner-image"
          :src="activity.coverImage || defaultCover"
          mode="aspectFill"
        />
        <!-- 渐变遮罩层 -->
        <view class="banner-overlay">
          <!-- 顶部行：状态芯片 + 标签 -->
          <view class="banner-top-row">
            <view class="status-chip" :class="`status-chip--${activity.status}`">
              <text class="status-chip-text">{{ statusLabel }}</text>
            </view>
            <view v-if="hasAnyTag" class="banner-tags">
              <view v-if="isNew" class="banner-tag banner-tag--new">新发布</view>
              <view v-if="isUrgent" class="banner-tag banner-tag--urgent">即将开始</view>
              <view v-if="isCrowded" class="banner-tag banner-tag--crowded">名额紧张</view>
              <view v-if="isHot" class="banner-tag banner-tag--hot">热门</view>
            </view>
          </view>

          <!-- 底部行：标题 + 社团名 + 收藏按钮 -->
          <view class="banner-bottom">
            <view class="banner-text">
              <text class="banner-title">{{ activity.title }}</text>
              <view class="banner-club" @click.stop="goToClub">
                <Icon name="users" :size="13" class="banner-club-icon" />
                <text class="banner-club-name">{{ activity.clubName }}</text>
              </view>
            </view>
            <view
              class="favorite-btn"
              :class="{ 'favorited': activity.isFavorited }"
              @click.stop="toggleFavorite"
            >
              <Icon
                name="heart"
                :size="22"
                class="favorite-icon"
                :class="{ 'filled': activity.isFavorited }"
              />
            </view>
          </view>
        </view>
      </view>

      <!-- ② 信息卡片网格 + 进度条 + 详情 -->
      <view class="activity-main">

        <!-- 信息网格 -->
        <view class="info-grid">
          <!-- 时间（全宽，含倒计时） -->
          <view class="info-grid-card info-grid-card--full">
            <view class="info-icon-wrap info-icon-wrap--blue">
              <Icon name="calendar" :size="18" class="info-grid-icon" />
            </view>
            <view class="info-grid-content">
              <text class="info-grid-label">活动时间</text>
              <text class="info-grid-value">{{ formatTime(activity.startTime) }}</text>
              <text class="info-grid-value info-grid-value--sub">→ {{ formatTime(activity.endTime) }}</text>
              <view v-if="countdown.show" class="countdown-box" :class="`countdown-${countdown.status}`">
                <Icon :name="countdown.iconName" :size="11" class="countdown-icon" />
                <text class="countdown-text">{{ countdown.text }}</text>
              </view>
            </view>
          </view>

          <!-- 地点 -->
          <view class="info-grid-card">
            <view class="info-icon-wrap info-icon-wrap--green">
              <Icon name="map-pin" :size="18" class="info-grid-icon" />
            </view>
            <view class="info-grid-content">
              <text class="info-grid-label">活动地点</text>
              <text class="info-grid-value">{{ activity.location }}</text>
            </view>
          </view>

          <!-- 主办社团（可点击） -->
          <view class="info-grid-card info-grid-card--clickable" @click="goToClub">
            <view class="info-icon-wrap info-icon-wrap--purple">
              <Icon name="users" :size="18" class="info-grid-icon" />
            </view>
            <view class="info-grid-content">
              <text class="info-grid-label">主办社团</text>
              <text class="info-grid-value info-grid-value--link">{{ activity.clubName }}</text>
            </view>
            <Icon name="chevron-right" :size="14" class="info-grid-arrow" />
          </view>

          <!-- 报名名额 -->
          <view class="info-grid-card">
            <view class="info-icon-wrap info-icon-wrap--orange">
              <Icon name="ticket" :size="18" class="info-grid-icon" />
            </view>
            <view class="info-grid-content">
              <text class="info-grid-label">报名名额</text>
              <view class="info-participants">
                <text
                  class="info-grid-value info-participants-current"
                  :class="{ 'number-highlight': numberHighlight }"
                >{{ activity.currentParticipants }}</text>
                <text class="info-participants-sep"> / {{ activity.maxParticipants }}</text>
              </view>
            </view>
          </view>
        </view>

        <!-- ③ 进度条（增强版：颜色状态 + 百分比） -->
        <view class="signup-status">
          <view class="status-bar-header">
            <text class="remaining">剩余名额 {{ remainingSlots }} 人</text>
            <text class="progress-pct" :class="progressColorClass">{{ Math.round(progressWidth) }}%</text>
          </view>
          <view class="status-bar">
            <view
              class="status-progress"
              :class="progressColorClass"
              :style="{ width: progressWidth + '%' }"
            ></view>
          </view>
          <text class="filled-hint">已报名 {{ activity.currentParticipants }} / {{ activity.maxParticipants }} 人</text>
        </view>

        <!-- ④ 活动详情（左侧装饰条 + 描述内容） -->
        <view class="detail-section">
          <view class="section-title-row">
            <view class="section-accent-bar"></view>
            <text class="section-title">活动详情</text>
          </view>
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
          <!-- 波纹效果 -->
          <view v-if="showRipple" class="ripple" :style="rippleStyle"></view>
        </button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { getActivityDetail, joinActivity, cancelActivity, checkInActivity } from '@/services/activity'
import { addFavorite, removeFavorite } from '@/services/favorite'
import { cache, CACHE_KEYS } from '@/utils/cache'
import config from '@/config'
import Icon from '@/components/icons/index.vue'
import { CNavBar } from '@/components/layout'

// 🎯 路由参数
const activityId = ref<number>(0)

// 🎯 页面状态
const loading = ref(true)
const error = ref(false)
const errorMessage = ref('')

// 🎯 按钮动画状态
const isButtonClicked = ref(false)
const showRipple = ref(false)
const rippleStyle = ref({})

// 🎯 数字闪烁动画状态
const numberHighlight = ref(false)

// 🎯 倒计时状态
const countdown = ref({
  show: false,
  status: 'upcoming', // upcoming | ongoing | ended
  iconName: 'clock',
  text: ''
})

let countdownTimer: number | null = null

// 🎯 活动数据接口
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
  isSignedIn: boolean
  isFavorited: boolean
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
  isSignedIn: false,
  isFavorited: false,
  status: 0,
  createdAt: ''
})

const defaultCover = 'https://picsum.photos/750/400?random=detail'

/**
 * 活动状态文本标签
 */
const statusLabel = computed(() => {
  const labelMap: Record<number, string> = {
    0: '未开始',
    1: '进行中',
    2: '已结束',
    3: '已取消'
  }
  return labelMap[activity.value.status] ?? '未知'
})

/**
 * 是否有任何标签显示
 */
const hasAnyTag = computed(() => isHot.value || isNew.value || isUrgent.value || isCrowded.value)

/**
 * 剩余名额
 */
const remainingSlots = computed(() => {
  return Math.max(0, activity.value.maxParticipants - activity.value.currentParticipants)
})

/**
 * 进度百分比
 */
const progressWidth = computed(() => {
  if (activity.value.maxParticipants === 0) return 0
  const pct = (activity.value.currentParticipants / activity.value.maxParticipants) * 100
  return Math.min(100, Math.max(0, pct))
})

/**
 * 进度条颜色 class：绿(<70%) / 橙(70-90%) / 红(>90%)
 */
const progressColorClass = computed(() => {
  const pct = progressWidth.value
  if (pct >= 90) return 'progress--danger'
  if (pct >= 70) return 'progress--warning'
  return 'progress--success'
})

/**
 * 活动状态标签 computed
 */
const isHot = computed(() => {
  return activity.value.currentParticipants / activity.value.maxParticipants > 0.5
})

const isNew = computed(() => {
  const createdTime = new Date(activity.value.createdAt).getTime()
  return Date.now() - createdTime < 24 * 60 * 60 * 1000
})

const isUrgent = computed(() => {
  const startTime = new Date(activity.value.startTime).getTime()
  const now = Date.now()
  return startTime - now < 3 * 24 * 60 * 60 * 1000 && startTime > now
})

const isCrowded = computed(() => {
  return remainingSlots.value / activity.value.maxParticipants < 0.2 && remainingSlots.value > 0
})

/**
 * 按钮状态
 */
type ButtonStatus = 'available' | 'registered' | 'full' | 'ongoing' | 'ended' | 'checkin' | 'checkedin'

const buttonStatus = computed<ButtonStatus>(() => {
  const now = Date.now()
  const endTime = activity.value.endTime ? new Date(activity.value.endTime).getTime() : 0
  const startTime = activity.value.startTime ? new Date(activity.value.startTime).getTime() : 0

  if (now > endTime || activity.value.status === 2) return 'ended'

  const isOngoing = (now >= startTime && now <= endTime) || activity.value.status === 1

  if (isOngoing) {
    if (activity.value.isJoined && activity.value.isSignedIn) return 'checkedin'
    if (activity.value.isJoined && !activity.value.isSignedIn) return 'checkin'
    return 'ongoing'
  }

  if (activity.value.isJoined) return 'registered'
  if (remainingSlots.value <= 0) return 'full'
  return 'available'
})

const buttonText = computed(() => {
  const textMap: Record<ButtonStatus, string> = {
    available: '立即报名',
    registered: '已报名（点击取消）',
    full: '名额已满',
    ongoing: '活动进行中',
    ended: '活动已结束',
    checkin: '立即签到',
    checkedin: '已签到 ✓'
  }
  return textMap[buttonStatus.value]
})

const buttonClass = computed(() => `button-${buttonStatus.value}`)

const buttonDisabled = computed(() => {
  return ['full', 'ongoing', 'ended', 'checkedin'].includes(buttonStatus.value)
})

/**
 * 加载活动详情
 */
const loadActivityDetail = async () => {
  loading.value = true
  error.value = false

  try {
    const res = await getActivityDetail(activityId.value)
    activity.value = res
    loading.value = false
    startCountdownTimer()
  } catch (err: any) {
    console.error('加载活动详情失败:', err)
    error.value = true
    errorMessage.value = err.message || '网络异常，请稍后重试'
    loading.value = false
  }
}

/**
 * 处理报名/签到操作
 */
const handleSignupAction = async (e?: any) => {
  isButtonClicked.value = true
  setTimeout(() => { isButtonClicked.value = false }, 300)

  if (!buttonDisabled.value && e) {
    const rect = e.currentTarget?.getBoundingClientRect?.() || { left: 0, top: 0 }
    const x = (e.clientX || e.touches?.[0]?.clientX || 0) - rect.left
    const y = (e.clientY || e.touches?.[0]?.clientY || 0) - rect.top
    rippleStyle.value = { left: x + 'px', top: y + 'px' }
    showRipple.value = true
    setTimeout(() => { showRipple.value = false }, 600)
  }

  if (buttonStatus.value === 'available') {
    await handleJoinActivity()
  } else if (buttonStatus.value === 'registered') {
    uni.showModal({
      title: '取消报名',
      content: '确定要取消报名吗？',
      success: (res) => {
        if (res.confirm) handleCancelActivity()
      }
    })
  } else if (buttonStatus.value === 'checkin') {
    await handleCheckIn()
  }
}

/**
 * 报名活动
 */
const handleJoinActivity = async () => {
  try {
    await joinActivity(activityId.value)
    uni.showToast({ title: '报名成功', icon: 'success', duration: 2000 })
    activity.value.isJoined = true
    activity.value.currentParticipants++
    triggerNumberHighlight()
    uni.$emit('activity-status-changed', { activityId: activityId.value, isJoined: true })
    setTimeout(() => {
      uni.showModal({
        title: '报名成功',
        content: '是否添加到日历提醒？',
        success: (res) => {
          if (res.confirm) uni.showToast({ title: '已添加提醒', icon: 'success' })
        }
      })
    }, 1500)
  } catch (err: any) {
    uni.showToast({ title: err.message || '报名失败', icon: 'none', duration: 2000 })
  }
}

/**
 * 取消报名
 */
const handleCancelActivity = async () => {
  try {
    await cancelActivity(activityId.value)
    uni.showToast({ title: '已取消报名', icon: 'success', duration: 2000 })
    activity.value.isJoined = false
    activity.value.currentParticipants--
    triggerNumberHighlight()
    uni.$emit('activity-status-changed', { activityId: activityId.value, isJoined: false })
  } catch (err: any) {
    uni.showToast({ title: err.message || '取消失败', icon: 'none', duration: 2000 })
  }
}

/**
 * 活动签到
 */
const handleCheckIn = async () => {
  try {
    await checkInActivity(activityId.value)
    activity.value.isSignedIn = true
    uni.showToast({ title: '签到成功 +10积分', icon: 'success', duration: 2000 })
    uni.$emit('points-changed', { change: +10, reason: '活动签到' })
  } catch (err: any) {
    uni.showToast({ title: err.message || '签到失败', icon: 'none', duration: 2000 })
  }
}

/**
 * 触发数字高亮动画
 */
const triggerNumberHighlight = () => {
  numberHighlight.value = true
  setTimeout(() => { numberHighlight.value = false }, 800)
}

/**
 * 切换收藏状态
 */
const toggleFavorite = async () => {
  const token = uni.getStorageSync(config.tokenKey)
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none', duration: 1500 })
    return
  }

  const currentFavStatus = activity.value.isFavorited
  try {
    activity.value.isFavorited = !currentFavStatus
    if (currentFavStatus) {
      await removeFavorite('activity', activity.value.activityId)
      uni.showToast({ title: '已取消收藏', icon: 'success', duration: 1500 })
    } else {
      await addFavorite('activity', activity.value.activityId)
      uni.showToast({ title: '收藏成功', icon: 'success', duration: 1500 })
    }
    cache.remove(CACHE_KEYS.ACTIVITIES)
    uni.$emit('activity-favorite-changed', { activityId: activity.value.activityId, isFavorited: !currentFavStatus })
  } catch (err: any) {
    activity.value.isFavorited = currentFavStatus
    uni.showToast({ title: (err as any).message || '操作失败', icon: 'none', duration: 2000 })
  }
}

/**
 * 跳转到社团主页
 */
const goToClub = () => {
  uni.navigateTo({
    url: `/pages/club/detail?id=${activity.value.clubId}`,
    fail: () => { uni.showToast({ title: '功能开发中', icon: 'none' }) }
  })
}

/**
 * 格式化时间
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
 * 更新倒计时
 */
const updateCountdown = () => {
  if (!activity.value.startTime || !activity.value.endTime) {
    countdown.value.show = false
    return
  }

  const now = Date.now()
  const startTime = new Date(activity.value.startTime).getTime()
  const endTime = new Date(activity.value.endTime).getTime()

  if (now > endTime) {
    countdown.value = { show: true, status: 'ended', iconName: 'check-circle', text: '活动已结束' }
    return
  }

  if (now >= startTime && now <= endTime) {
    const remainingMs = endTime - now
    const hours = Math.floor(remainingMs / (1000 * 60 * 60))
    const minutes = Math.floor((remainingMs % (1000 * 60 * 60)) / (1000 * 60))
    countdown.value = {
      show: true, status: 'ongoing', iconName: 'flame',
      text: `活动进行中，剩余 ${hours}小时${minutes}分钟`
    }
    return
  }

  const remainingMs = startTime - now
  const days = Math.floor(remainingMs / (1000 * 60 * 60 * 24))
  const hours = Math.floor((remainingMs % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  const minutes = Math.floor((remainingMs % (1000 * 60 * 60)) / (1000 * 60))
  const seconds = Math.floor((remainingMs % (1000 * 60)) / 1000)

  let text = ''
  if (days > 0) text = `距离开始还有 ${days}天 ${hours}小时`
  else if (hours > 0) text = `距离开始还有 ${hours}小时 ${minutes}分钟`
  else if (minutes > 0) text = `距离开始还有 ${minutes}分${seconds}秒`
  else text = `距离开始还有 ${seconds}秒`

  countdown.value = { show: true, status: 'upcoming', iconName: 'clock', text }
}

const startCountdownTimer = () => {
  updateCountdown()
  countdownTimer = setInterval(updateCountdown, 1000) as unknown as number
}

const stopCountdownTimer = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

onMounted(() => {
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

onUnmounted(() => {
  stopCountdownTimer()
})
</script>

<style scoped lang="scss">

/* ========== 页面容器 ========== */
.activity-detail-page {
  min-height: 100vh;
  background: $bg-page;
  padding-bottom: 128rpx;
}

/* ========== 骨架屏 ========== */
.loading-container {
  padding: 0;
}

.skeleton-banner {
  width: 100%;
  height: 480rpx;
  background: linear-gradient(90deg, $gray-100 25%, $gray-200 50%, $gray-100 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
  padding: 24rpx 32rpx;
}

.skeleton-info-card {
  height: 128rpx;
  background: linear-gradient(90deg, $gray-100 25%, $gray-200 50%, $gray-100 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: $radius-lg;

  &--full {
    grid-column: 1 / -1;
    height: 160rpx;
  }
}

.skeleton-content {
  padding: 0 32rpx 32rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.skeleton-line {
  background: linear-gradient(90deg, $gray-100 25%, $gray-200 50%, $gray-100 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: $radius-sm;
}

.skeleton-title {
  width: 50%;
  height: 40rpx;
  margin-bottom: 8rpx;
}

.skeleton-text {
  width: 100%;
  height: 32rpx;
}

.skeleton-text-short {
  width: 70%;
  height: 32rpx;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
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
  color: $warning;
  margin-bottom: 32rpx;
  flex-shrink: 0;
}

.error-title {
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  margin-bottom: $sp-4;
}

.error-desc {
  font-size: $font-size-base;
  color: $text-secondary;
  text-align: center;
  margin-bottom: $sp-12;
}

.error-btn {
  padding: $sp-5 $sp-16;
  @include gradient-primary;
  border-radius: $radius-button;
  cursor: pointer;
}

.error-btn-text {
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $text-inverse;
}

/* ========== Hero Banner ========== */
.activity-banner {
  width: 100%;
  height: 480rpx;
  position: relative;
  overflow: hidden;
}

.banner-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

/* 渐变遮罩层 */
.banner-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to bottom,
    rgba(0, 0, 0, 0.15) 0%,
    rgba(0, 0, 0, 0.05) 30%,
    rgba(0, 0, 0, 0.45) 65%,
    rgba(0, 0, 0, 0.72) 100%
  );
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 0;
}

/* 顶部行：状态芯片 + 标签 */
.banner-top-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 24rpx 32rpx;
  flex-wrap: wrap;
}

.status-chip {
  display: inline-flex;
  align-items: center;
  padding: 6rpx 18rpx;
  border-radius: 40rpx;
  font-size: 22rpx;
  font-weight: $font-weight-semibold;
}

.status-chip-text {
  line-height: 1;
}

.status-chip--0 {
  background: rgba(59, 130, 246, 0.85);
  color: #fff;
}

.status-chip--1 {
  background: rgba(16, 185, 129, 0.85);
  color: #fff;
}

.status-chip--2 {
  background: rgba(107, 114, 128, 0.85);
  color: #fff;
}

.status-chip--3 {
  background: rgba(239, 68, 68, 0.75);
  color: #fff;
}

.banner-tags {
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex-wrap: wrap;
}

.banner-tag {
  padding: 4rpx 14rpx;
  border-radius: 30rpx;
  font-size: 20rpx;
  font-weight: 600;
  color: #fff;
  background: rgba(255, 255, 255, 0.22);
  backdrop-filter: blur(4px);
}

.banner-tag--new {
  background: rgba(99, 102, 241, 0.75);
}

.banner-tag--urgent {
  background: rgba(245, 158, 11, 0.8);
}

.banner-tag--crowded {
  background: rgba(239, 68, 68, 0.75);
}

.banner-tag--hot {
  background: rgba(249, 115, 22, 0.8);
}

/* 底部行：标题 + 社团 + 收藏 */
.banner-bottom {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24rpx;
  padding: 32rpx 32rpx 40rpx;
}

.banner-text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.banner-title {
  font-size: 40rpx;
  font-weight: $font-weight-bold;
  color: #fff;
  line-height: $line-height-snug;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.4);
  // 防止超长标题溢出
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
  overflow: hidden;
}

.banner-club {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  cursor: pointer;
}

.banner-club-icon {
  color: rgba(255, 255, 255, 0.75);
  flex-shrink: 0;
}

.banner-club-name {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.85);
  font-weight: $font-weight-medium;
}

/* 收藏按钮 */
.favorite-btn {
  flex-shrink: 0;
  width: 88rpx;
  height: 88rpx;
  @include flex-center;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(8px);
  border-radius: $radius-full;
  cursor: pointer;
  transition: $transition-base;
  border: 2rpx solid rgba(255, 255, 255, 0.3);

  &:active {
    transform: scale(0.92);
  }

  &.favorited {
    background: rgba(239, 68, 68, 0.85);
    border-color: rgba(239, 68, 68, 0.5);
    animation: heart-beat 0.6s $ease-out;
  }
}

.favorite-icon {
  color: #fff;
  transition: all 0.3s ease;

  &.filled :deep(path) {
    fill: currentColor;
  }
}

@keyframes heart-beat {
  0%, 100% { transform: scale(1); }
  25% { transform: scale(1.25); }
  50% { transform: scale(1.1); }
  75% { transform: scale(1.18); }
}

/* ========== 主体内容区 ========== */
.activity-main {
  background: $bg-page;
  padding: $sp-6 $sp-6 $sp-24;
  max-width: 860px;
  margin: 0 auto;
}

/* ========== 信息卡片网格 ========== */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
  margin-bottom: $sp-6;
}

.info-grid-card {
  background: $bg-surface;
  border-radius: $radius-lg;
  padding: $sp-6;
  display: flex;
  align-items: flex-start;
  gap: $sp-4;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);

  &--full {
    grid-column: 1 / -1;
  }

  &--clickable {
    cursor: pointer;
    position: relative;

    &:active {
      background: $gray-50;
    }
  }
}

/* 图标容器（彩色背景） */
.info-icon-wrap {
  flex-shrink: 0;
  width: 64rpx;
  height: 64rpx;
  @include flex-center;
  border-radius: $radius-md;
}

.info-icon-wrap--blue {
  background: rgba(59, 130, 246, 0.1);

  .info-grid-icon { color: #3B82F6; }
}

.info-icon-wrap--green {
  background: rgba(16, 185, 129, 0.1);

  .info-grid-icon { color: #10B981; }
}

.info-icon-wrap--purple {
  background: rgba(139, 92, 246, 0.1);

  .info-grid-icon { color: #8B5CF6; }
}

.info-icon-wrap--orange {
  background: rgba(249, 115, 22, 0.1);

  .info-grid-icon { color: #F97316; }
}

.info-grid-icon {
  flex-shrink: 0;
}

.info-grid-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: $sp-1;
}

.info-grid-label {
  font-size: $font-size-xs;
  color: $text-tertiary;
  line-height: 1;
}

.info-grid-value {
  font-size: $font-size-md;
  color: $text-primary;
  font-weight: $font-weight-medium;
  line-height: $line-height-snug;
  word-break: break-all;

  &--sub {
    font-size: $font-size-sm;
    color: $text-secondary;
    font-weight: $font-weight-normal;
  }

  &--link {
    color: $primary;
  }

  &--dim {
    color: $text-tertiary;
    font-weight: $font-weight-normal;
    font-size: 0.9em;
  }
}

/* 数字高亮动画 */
.info-participants {
  display: flex;
  align-items: baseline;
  gap: 0;
}

.info-participants-current {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-primary;
  transition: $transition-base;

  &.number-highlight {
    animation: number-pulse 0.8s ease-out;
  }
}

.info-participants-sep {
  font-size: $font-size-sm;
  color: $text-tertiary;
  font-weight: $font-weight-normal;
}

@keyframes number-pulse {
  0%, 100% { color: $text-primary; transform: scale(1); }
  25% { color: $accent; transform: scale(1.2); }
  50% { color: $accent; transform: scale(1.1); }
  75% { color: $accent-light; transform: scale(1.05); }
}

.info-grid-arrow {
  flex-shrink: 0;
  color: $gray-400;
  align-self: center;
}

/* 倒计时 */
.countdown-box {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 6rpx 12rpx;
  border-radius: 6rpx;
  margin-top: 10rpx;
  font-size: 22rpx;
  font-weight: 500;
}

.countdown-upcoming {
  background: $warning-50;
  color: $accent-dark;
}

.countdown-ongoing {
  background: $success-50;
  color: $success;
}

.countdown-ended {
  background: $gray-100;
  color: $text-secondary;
}

.countdown-icon {
  flex-shrink: 0;
  color: currentColor;
}

.countdown-text {
  line-height: 1.2;
}

/* ========== 进度条（增强版） ========== */
.signup-status {
  background: $bg-surface;
  border-radius: $radius-lg;
  padding: $sp-6 $sp-6 $sp-5;
  margin-bottom: $sp-6;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.status-bar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $sp-3;
}

.remaining {
  font-size: $font-size-sm;
  color: $text-secondary;
  font-weight: $font-weight-semibold;
}

.progress-pct {
  font-size: $font-size-sm;
  font-weight: $font-weight-bold;
}

/* 进度颜色状态 */
.progress--success {
  color: #10B981;
}

.progress--warning {
  color: #F59E0B;
}

.progress--danger {
  color: #EF4444;
}

.status-bar {
  width: 100%;
  height: $sp-3;
  background: $gray-200;
  border-radius: $radius-full;
  overflow: hidden;
  margin-bottom: $sp-3;
}

.status-progress {
  height: 100%;
  border-radius: $radius-full;
  transition: width 0.8s $ease-smooth;

  &.progress--success {
    background: linear-gradient(90deg, #10B981, #34D399);
  }

  &.progress--warning {
    background: linear-gradient(90deg, #F59E0B, #FBBF24);
  }

  &.progress--danger {
    background: linear-gradient(90deg, #EF4444, #F87171);
  }
}

.filled-hint {
  font-size: $font-size-xs;
  color: $text-tertiary;
}

/* ========== 活动详情（左侧装饰条） ========== */
.detail-section {
  background: $bg-surface;
  border-radius: $radius-lg;
  padding: $sp-6;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.section-title-row {
  display: flex;
  align-items: center;
  gap: $sp-3;
  margin-bottom: $sp-5;
}

.section-accent-bar {
  width: 6rpx;
  height: 36rpx;
  background: $primary;
  border-radius: $radius-sm;
  flex-shrink: 0;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-primary;
  line-height: 1;
}

.section-content {
  font-size: $font-size-base;
  color: $text-primary;
  line-height: $line-height-relaxed;
  white-space: pre-wrap;
}

/* ========== 底部固定按钮 ========== */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: $bg-surface;
  padding: 20rpx 32rpx;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.08);
  z-index: 100;
  display: flex;
  justify-content: center;
}

.button-wrapper {
  position: relative;
  width: 100%;
  max-width: 1440rpx;
}

.signup-button {
  position: relative;
  width: 100%;
  height: 96rpx;
  border-radius: 16rpx;
  font-size: 32rpx;
  font-weight: 600;
  line-height: 96rpx;
  text-align: center;
  border: none;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.signup-button.button-clicked {
  transform: scale(0.96);
}

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
  0% { transform: translate(-50%, -50%) scale(0); opacity: 1; }
  100% { transform: translate(-50%, -50%) scale(40); opacity: 0; }
}

.button-available {
  @include gradient-primary;
  color: $text-inverse;
  box-shadow: 0 4rpx 16rpx rgba($primary, 0.3);

  &:hover {
    box-shadow: 0 6rpx 20rpx rgba($primary, 0.4);
    transform: translateY(-2rpx);
  }
}

.button-registered {
  background: linear-gradient(135deg, $success 0%, $success-light 100%);
  color: $text-inverse;
}

.button-checkin {
  @include gradient-accent;
  color: $text-inverse;
  box-shadow: 0 4rpx 16rpx rgba($accent, 0.3);

  &:hover {
    box-shadow: 0 6rpx 20rpx rgba($accent, 0.4);
    transform: translateY(-2rpx);
  }
}

.button-checkedin {
  background: linear-gradient(135deg, $warning-light 0%, $accent-100 100%);
  color: $text-inverse;
  cursor: not-allowed;
  opacity: 0.8;
}

.button-full,
.button-ongoing,
.button-ended {
  background: $gray-200;
  color: $text-placeholder;
  cursor: not-allowed;
}

/* ========== 响应式 ========== */
@media (min-width: 860px) {
  .activity-banner {
    height: 560rpx;
  }

  .banner-title {
    font-size: 48rpx;
  }

  .activity-main {
    padding: $sp-8 $sp-8 $sp-24;
  }
}

@media (max-width: 750px) {
  .activity-banner {
    height: 480rpx;
  }

  .banner-title {
    font-size: 36rpx;
  }

  .signup-button {
    height: 104rpx;
    line-height: 104rpx;
    border-radius: 0;
    font-size: 34rpx;
  }
}
</style>
