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
      <Icon name="alert-triangle" :size="48" :stroke-width="1.5" class="error-icon" />
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
        <!-- 标题 + 标签 + 收藏按钮 -->
        <view class="title-section">
          <view class="title-row">
            <text class="activity-title">{{ activity.title }}</text>
            <view
              class="favorite-btn"
              :class="{ 'favorited': activity.isFavorited }"
              @click="toggleFavorite"
            >
              <Icon name="heart" :size="20" class="favorite-icon" :class="{ 'filled': activity.isFavorited }" />
            </view>
          </view>
          <view class="activity-tags">
            <view v-if="isHot" class="tag tag-hot">
              <Icon name="flame" :size="12" class="tag-icon" />
              <text class="tag-text">热门</text>
            </view>
            <view v-if="isNew" class="tag tag-new">
              <Icon name="sparkles" :size="12" class="tag-icon" />
              <text class="tag-text">新发布</text>
            </view>
            <view v-if="isUrgent" class="tag tag-urgent">
              <Icon name="clock" :size="12" class="tag-icon" />
              <text class="tag-text">即将开始</text>
            </view>
            <view v-if="isCrowded" class="tag tag-crowded">
              <Icon name="zap" :size="12" class="tag-icon" />
              <text class="tag-text">名额紧张</text>
            </view>
          </view>
        </view>

        <!-- 基本信息卡片 -->
        <view class="info-card">
          <view class="info-item">
            <Icon name="calendar" :size="18" class="info-icon" />
            <view class="info-content">
              <text class="info-label">活动时间</text>
              <text class="info-value">{{ formatTime(activity.startTime) }} ~ {{ formatTime(activity.endTime) }}</text>
              <!-- 🎯 时间倒计时 -->
              <view v-if="countdown.show" class="countdown-box" :class="`countdown-${countdown.status}`">
                <Icon :name="countdown.iconName" :size="11" class="countdown-icon" />
                <text class="countdown-text">{{ countdown.text }}</text>
              </view>
            </view>
          </view>

          <view class="info-item">
            <Icon name="map-pin" :size="18" class="info-icon" />
            <view class="info-content">
              <text class="info-label">活动地点</text>
              <text class="info-value">{{ activity.location }}</text>
            </view>
          </view>

          <view class="info-item" @click="goToClub">
            <Icon name="users" :size="18" class="info-icon" />
            <view class="info-content">
              <text class="info-label">主办方</text>
              <text class="info-value clickable">{{ activity.clubName }}</text>
            </view>
            <Icon name="chevron-right" :size="14" class="info-arrow" />
          </view>

          <view class="info-item">
            <Icon name="ticket" :size="18" class="info-icon" />
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { getActivityDetail, joinActivity, cancelActivity, checkInActivity } from '@/services/activity'
import { addFavorite, removeFavorite } from '@/services/favorite'
import { cache, CACHE_KEYS } from '@/utils/cache'
import config from '@/config'
import Icon from '@/components/icons/index.vue'

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

// 🎯 倒计时状态
const countdown = ref({
  show: false,
  status: 'upcoming', // upcoming | ongoing | ended
  iconName: 'clock', // Lucide icon name
  text: ''
})

let countdownTimer: number | null = null

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
  isSignedIn: boolean  // 🎯 新增：是否已签到
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
  isSignedIn: false,  // 🎯 新增：签到状态初始值
  isFavorited: false,
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
type ButtonStatus = 'available' | 'registered' | 'full' | 'ongoing' | 'ended' | 'checkin' | 'checkedin'

const buttonStatus = computed<ButtonStatus>(() => {
  const now = Date.now()
  const endTime = activity.value.endTime ? new Date(activity.value.endTime).getTime() : 0
  const startTime = activity.value.startTime ? new Date(activity.value.startTime).getTime() : 0

  // 🎯 双重验证：前端时间判断 + 后端状态
  // 1. 如果活动已结束（时间判断或后端状态）
  if (now > endTime || activity.value.status === 2) return 'ended'

  // 2. 如果活动进行中（时间判断或后端状态）
  const isOngoing = (now >= startTime && now <= endTime) || activity.value.status === 1

  if (isOngoing) {
    // 2.1 活动进行中 + 已报名 + 已签到 → 已签到
    if (activity.value.isJoined && activity.value.isSignedIn) return 'checkedin'

    // 2.2 活动进行中 + 已报名 + 未签到 → 签到
    if (activity.value.isJoined && !activity.value.isSignedIn) return 'checkin'

    // 2.3 活动进行中 + 未报名 → 活动进行中（禁用）
    return 'ongoing'
  }

  // 3. 如果用户已报名（活动未开始）
  if (activity.value.isJoined) return 'registered'

  // 4. 如果名额已满
  if (remainingSlots.value <= 0) return 'full'

  // 5. 可以报名
  return 'available'
})

const buttonText = computed(() => {
  const textMap = {
    available: '立即报名',
    registered: '已报名',
    full: '名额已满',
    ongoing: '活动进行中',
    ended: '活动已结束',
    checkin: '立即签到',      // 🎯 新增：签到按钮
    checkedin: '已签到'        // 🎯 新增：已签到状态
  }
  return textMap[buttonStatus.value]
})

const buttonClass = computed(() => {
  return `button-${buttonStatus.value}`
})

const buttonDisabled = computed(() => {
  return ['full', 'ongoing', 'ended', 'checkedin'].includes(buttonStatus.value)  // 🎯 已签到时禁用
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

    // 🎯 启动倒计时
    startCountdownTimer()
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
  } else if (buttonStatus.value === 'checkin') {
    // 🎯 签到
    await handleCheckIn()
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
 * 🎯 活动签到
 */
const handleCheckIn = async () => {
  try {
    await checkInActivity(activityId.value)

    // 更新签到状态
    activity.value.isSignedIn = true

    uni.showToast({
      title: '签到成功 +10积分',
      icon: 'success',
      duration: 2000
    })

    // 🎯 触发全局事件，通知更新用户积分
    uni.$emit('points-changed', {
      change: +10,
      reason: '活动签到'
    })
  } catch (err: any) {
    console.error('签到失败:', err)
    uni.showToast({
      title: err.message || '签到失败',
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
 * 🎯 切换收藏状态
 */
const toggleFavorite = async () => {
  const token = uni.getStorageSync(config.tokenKey)
  if (!token) {
    uni.showToast({
      title: '请先登录',
      icon: 'none',
      duration: 1500
    })
    return
  }

  const currentFavStatus = activity.value.isFavorited

  try {
    // 乐观更新
    activity.value.isFavorited = !currentFavStatus

    if (currentFavStatus) {
      await removeFavorite('activity', activity.value.activityId)
      uni.showToast({
        title: '已取消收藏',
        icon: 'success',
        duration: 1500
      })
    } else {
      await addFavorite('activity', activity.value.activityId)
      uni.showToast({
        title: '收藏成功',
        icon: 'success',
        duration: 1500
      })
    }

    // 🎯 清除缓存,确保下次加载时获取最新的收藏状态
    cache.remove(CACHE_KEYS.ACTIVITIES)

    // 🎯 触发全局事件,通知首页更新收藏状态
    uni.$emit('activity-favorite-changed', {
      activityId: activity.value.activityId,
      isFavorited: !currentFavStatus
    })

  } catch (error: any) {
    // 失败时回滚
    activity.value.isFavorited = currentFavStatus
    uni.showToast({
      title: error.message || '操作失败',
      icon: 'none',
      duration: 2000
    })
  }
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
 * 🎯 更新倒计时
 */
const updateCountdown = () => {
  if (!activity.value.startTime || !activity.value.endTime) {
    countdown.value.show = false
    return
  }

  const now = Date.now()
  const startTime = new Date(activity.value.startTime).getTime()
  const endTime = new Date(activity.value.endTime).getTime()

  // 已结束
  if (now > endTime) {
    countdown.value.show = true
    countdown.value.status = 'ended'
    countdown.value.iconName = 'check-circle'
    countdown.value.text = '活动已结束'
    return
  }

  // 进行中
  if (now >= startTime && now <= endTime) {
    const remainingMs = endTime - now
    const hours = Math.floor(remainingMs / (1000 * 60 * 60))
    const minutes = Math.floor((remainingMs % (1000 * 60 * 60)) / (1000 * 60))

    countdown.value.show = true
    countdown.value.status = 'ongoing'
    countdown.value.iconName = 'flame'
    countdown.value.text = `活动进行中，剩余 ${hours}小时${minutes}分钟`
    return
  }

  // 即将开始
  const remainingMs = startTime - now
  const days = Math.floor(remainingMs / (1000 * 60 * 60 * 24))
  const hours = Math.floor((remainingMs % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  const minutes = Math.floor((remainingMs % (1000 * 60 * 60)) / (1000 * 60))
  const seconds = Math.floor((remainingMs % (1000 * 60)) / 1000)

  countdown.value.show = true
  countdown.value.status = 'upcoming'
  countdown.value.iconName = 'clock'

  if (days > 0) {
    countdown.value.text = `距离开始还有 ${days}天 ${hours}小时`
  } else if (hours > 0) {
    countdown.value.text = `距离开始还有 ${hours}小时 ${minutes}分钟`
  } else if (minutes > 0) {
    countdown.value.text = `距离开始还有 ${minutes}分${seconds}秒`
  } else {
    countdown.value.text = `距离开始还有 ${seconds}秒`
  }
}

/**
 * 🎯 启动倒计时定时器
 */
const startCountdownTimer = () => {
  // 立即更新一次
  updateCountdown()

  // 每秒更新
  countdownTimer = setInterval(() => {
    updateCountdown()
  }, 1000) as unknown as number
}

/**
 * 🎯 停止倒计时定时器
 */
const stopCountdownTimer = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
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

/**
 * 🎯 页面卸载
 */
onUnmounted(() => {
  // 清理倒计时定时器
  stopCountdownTimer()
})
</script>

<style scoped lang="scss">
// 变量已通过 uni.scss 全局注入

/* ========== 页面容器 ========== */
.activity-detail-page {
  min-height: 100vh;
  background: $bg-page;
  padding-bottom: 120rpx;
}

/* ========== 加载骨架屏 ========== */
.loading-container {
  padding: 0;
}

.skeleton-banner {
  width: 100%;
  height: 400rpx;
  background: linear-gradient(90deg, $gray-100 25%, $gray-200 50%, $gray-100 75%);
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
  background: linear-gradient(90deg, $gray-100 25%, $gray-200 50%, $gray-100 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: $radius-sm;
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
  color: $warning; // P1: 统一图标颜色（警告橙色）
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

/* ========== 活动Banner ========== */
.activity-banner {
  width: 100%;
  height: 280rpx;
  position: relative;
  overflow: hidden;
  border-radius: 0 0 $radius-lg $radius-lg;

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 80rpx;
    background: linear-gradient(to top, rgba($black, 0.08), transparent);
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
  background: $bg-surface;
  border-radius: $radius-xl $radius-xl 0 0;
  margin-top: -$sp-8;
  position: relative;
  z-index: 1;
  padding: $sp-12 $sp-8 $sp-24;
  max-width: 860px;
  margin-left: auto;
  margin-right: auto;
}

/* ========== 标题区域 ========== */
.title-section {
  margin-bottom: 32rpx; /* 🎯 最终版：标签区 → 信息卡片 16px */
}

.title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 24rpx; /* 🎯 最终版：标题 → 标签 12px */
}

.activity-title {
  font-size: $font-size-3xl;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  line-height: $line-height-snug;
  flex: 1;
}

/* 收藏按钮 */
.favorite-btn {
  flex-shrink: 0;
  width: 80rpx;
  height: 80rpx;
  @include flex-center;
  background: linear-gradient(135deg, $accent-50 0%, $accent-100 100%);
  border-radius: $radius-full;
  cursor: pointer;
  transition: $transition-base;
  box-shadow: 0 4rpx 12rpx rgba($accent, 0.15);

  &:hover {
    transform: scale(1.1);
    box-shadow: 0 6rpx 16rpx rgba($accent, 0.25);
  }

  &:active {
    transform: scale(0.95);
  }

  &.favorited {
    background: linear-gradient(135deg, $error 0%, $error-light 100%);
    box-shadow: 0 4rpx 12rpx rgba($error, 0.3);
    animation: heart-beat 0.6s $ease-out;

    &:hover {
      box-shadow: 0 6rpx 16rpx rgba($error, 0.4);
    }
  }
}

.favorite-icon {
  color: $white;
  transition: all 0.3s ease;

  &.filled :deep(path) {
    fill: currentColor;
  }
}

.favorite-btn.favorited .favorite-icon {
  transform: scale(1.1);
  color: $white;
}

@keyframes heart-beat {
  0%, 100% {
    transform: scale(1);
  }
  25% {
    transform: scale(1.2);
  }
  50% {
    transform: scale(1.1);
  }
  75% {
    transform: scale(1.15);
  }
}

.activity-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
}

.tag {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 16rpx;
  border-radius: 12rpx;
  font-size: 24rpx;
  font-weight: 600;
  line-height: 1;
}

.tag-icon {
  color: currentColor;
  flex-shrink: 0;
}

.tag-text {
  line-height: 1;
}

.tag-hot {
  background: $accent;
  color: $text-inverse;
}

.tag-new {
  background: $primary-light;
  color: $text-inverse;
}

.tag-urgent {
  background: $warning;
  color: $text-inverse;
}

.tag-crowded {
  background: $accent;
  color: $text-inverse;
}

/* ========== 基本信息卡片 ========== */
.info-card {
  background: $bg-surface;
  border-radius: $radius-lg;
  padding: $sp-10;
  margin-bottom: $sp-8;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: $sp-6;
  padding: $sp-6 0;
  border-bottom: 1rpx solid $border-color;

  &:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }
}

.info-icon {
  flex-shrink: 0;
  color: $primary;
}

.info-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $sp-2;
}

.info-label {
  font-size: $font-size-sm;
  color: $text-tertiary;
  line-height: 1;
}

.info-value {
  font-size: $font-size-md;
  color: $text-primary;
  font-weight: $font-weight-medium;
  line-height: $line-height-snug;
  transition: $transition-base;
}

/* 🎯 数字高亮闪烁动画 */
.info-value.number-highlight {
  animation: number-pulse 0.8s ease-out;
}

@keyframes number-pulse {
  0%, 100% {
    color: $text-primary;
    transform: scale(1);
  }
  25% {
    color: $accent;
    transform: scale(1.15);
    font-weight: $font-weight-bold;
  }
  50% {
    color: $accent;
    transform: scale(1.1);
  }
  75% {
    color: $accent-light;
    transform: scale(1.05);
  }
}

.info-value.clickable {
  color: $primary;
}

.info-arrow {
  flex-shrink: 0;
  color: $gray-400;
}

/* 🎯 倒计时盒子 */
.countdown-box {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 6rpx 12rpx;
  border-radius: 6rpx;
  margin-top: 8rpx;
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

/* ========== 报名状态条 ========== */
.signup-status {
  background: transparent; /* 🎯 最终版：透明背景 */
  padding: 0;
  margin-bottom: 64rpx; /* 🎯 最终版：进度条 → 活动详情 32px */
}

.status-bar {
  width: 100%;
  height: $sp-4;
  background: $gray-200;
  border-radius: $radius-base;
  overflow: hidden;
  margin-bottom: $sp-4;
}

.status-progress {
  height: 100%;
  background: $primary;
  border-radius: $radius-base;
  transition: width $duration-slow $ease-smooth;
}

.status-text {
  @include flex-between;
}

.remaining {
  font-size: $font-size-sm;
  color: $text-secondary;
  font-weight: $font-weight-semibold;
}

.filled {
  font-size: $font-size-sm;
  color: $text-secondary;
  font-weight: $font-weight-normal;
}

/* ========== 活动详情 ========== */
.detail-section {
  margin-bottom: $sp-8;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $text-primary;
  display: block;
  margin-bottom: $sp-4;
}

.section-content {
  font-size: $font-size-base;
  color: $text-primary;
  line-height: $line-height-relaxed;
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
  @include gradient-primary;
  color: $text-inverse;
  box-shadow: 0 4rpx 16rpx rgba($primary, 0.3);
  transition: $transition-base;

  &:hover {
    box-shadow: 0 6rpx 20rpx rgba($primary, 0.4);
    transform: translateY(-2rpx);
  }
}

.button-registered {
  background: linear-gradient(135deg, $success 0%, $success-light 100%);
  color: $text-inverse;
}

/* 签到按钮样式 */
.button-checkin {
  @include gradient-accent;
  color: $text-inverse;
  box-shadow: 0 4rpx 16rpx rgba($accent, 0.3);
  transition: $transition-base;

  &:hover {
    box-shadow: 0 6rpx 20rpx rgba($accent, 0.4);
    transform: translateY(-2rpx);
  }
}

/* 已签到状态 */
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
