<template>
  <view class="task-header">
    <!-- 顶部导航栏 -->
    <view class="nav-bar" :class="{ 'nav-bar-sticky': isSticky }">
      <view class="nav-content">
        <view class="nav-left" @click="handleBack">
          <text class="icon-back">←</text>
          <text class="nav-text">返回</text>
        </view>
        <view class="nav-title" v-if="isSticky">{{ title }}</view>
        <view class="nav-right">
          <view class="nav-icon" @click="$emit('favorite')">
            <text class="icon">{{ isFavorited ? '★' : '☆' }}</text>
          </view>
          <view class="nav-icon" @click="$emit('share')">
            <text class="icon">⋯</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 任务标题区 -->
    <view class="title-section">
      <view class="title-row">
        <text class="task-title">{{ title }}</text>
        <view class="category-tag" :style="{ backgroundColor: categoryColor }">
          <text class="category-icon">{{ categoryIcon }}</text>
          <text class="category-text">{{ categoryText }}</text>
        </view>
      </view>

      <!-- 状态徽章 -->
      <view class="status-badge" :class="statusClass">
        <view class="badge-dot" v-if="status === 0 || status === 1"></view>
        <text class="badge-text">{{ statusText }}</text>
      </view>
    </view>

    <!-- 紧凑状态卡片 -->
    <view class="status-card-compact">
      <view class="status-main">
        <view class="status-icon-large" :style="{ background: currentStatusConfig.gradient }">
          <text class="status-emoji">{{ currentStatusConfig.icon }}</text>
        </view>
        <view class="status-content">
          <view class="status-label-row">
            <text class="status-label">当前状态</text>
            <view class="status-badge-inline" :style="{ background: currentStatusConfig.bgColor, color: currentStatusConfig.color }">
              <view class="badge-dot-inline" :style="{ background: currentStatusConfig.color }"></view>
              <text class="badge-text-inline">{{ statusText }}</text>
            </view>
          </view>
          <text class="status-hint">{{ getStatusHint() }}</text>
          <text class="status-next" v-if="getNextStep()">
            <text class="next-arrow">→</text> {{ getNextStep() }}
          </text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface Props {
  title: string
  status: number // 0待接单 1进行中 2已完成 3已取消
  category?: string
  isFavorited?: boolean
  publisherName: string
  publisherAvatar?: string
  publisherVerified?: boolean
  publishTime: string
}

const props = withDefaults(defineProps<Props>(), {
  category: 'run',
  isFavorited: false,
  publisherVerified: false
})

const emit = defineEmits<{
  back: []
  favorite: []
  share: []
}>()

const isSticky = ref(false)

// 分类配置
const categoryConfig: Record<string, { icon: string; text: string; color: string }> = {
  run: { icon: '🏃', text: '跑腿代办', color: '#3B82F6' },
  resource: { icon: '📚', text: '资源共享', color: '#10B981' },
  help: { icon: '🤝', text: '互助帮忙', color: '#F59E0B' },
  other: { icon: '💡', text: '其他', color: '#8B5CF6' }
}

const categoryIcon = computed(() => categoryConfig[props.category]?.icon || '💡')
const categoryText = computed(() => categoryConfig[props.category]?.text || '其他')
const categoryColor = computed(() => categoryConfig[props.category]?.color || '#8B5CF6')

// 状态配置
const statusConfig = [
  { text: '待接单', class: 'status-pending' },
  { text: '进行中', class: 'status-active' },
  { text: '已完成', class: 'status-completed' },
  { text: '已取消', class: 'status-cancelled' }
]

const statusText = computed(() => statusConfig[props.status]?.text || '未知')
const statusClass = computed(() => statusConfig[props.status]?.class || '')

// 当前状态配置（用于紧凑卡片）
const statusConfigDetail = [
  {
    icon: '🕒',
    gradient: 'linear-gradient(135deg, #DBEAFE 0%, #BFDBFE 100%)',
    bgColor: '#DBEAFE',
    color: '#3B82F6',
    text: '待接单'
  },
  {
    icon: '🚚',
    gradient: 'linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%)',
    bgColor: '#FEF3C7',
    color: '#F59E0B',
    text: '进行中'
  },
  {
    icon: '🎯',
    gradient: 'linear-gradient(135deg, #D1FAE5 0%, #A7F3D0 100%)',
    bgColor: '#D1FAE5',
    color: '#10B981',
    text: '已完成'
  },
  {
    icon: '🚫',
    gradient: 'linear-gradient(135deg, #F3F4F6 0%, #E5E7EB 100%)',
    bgColor: '#F3F4F6',
    color: '#6B7280',
    text: '已取消'
  }
]

const currentStatusConfig = computed(() => statusConfigDetail[props.status] || statusConfigDetail[0])

// 获取状态提示文案
const getStatusHint = (): string => {
  const now = new Date()
  const publishDate = new Date(props.publishTime)
  const hours = Math.floor((now.getTime() - publishDate.getTime()) / (1000 * 60 * 60))

  switch (props.status) {
    case 0: // 待接单
      if (hours < 1) return '任务刚刚发布，赶快抢单吧'
      if (hours < 24) return `已发布${hours}小时，建议尽快接取`
      return `发布已超过${Math.floor(hours / 24)}天，可能即将过期`
    case 1: // 进行中
      return '任务正在进行，请按时完成并提交结果'
    case 2: // 已完成
      return '任务已圆满完成，感谢参与'
    case 3: // 已取消
      return '任务已被取消，无法再进行操作'
    default:
      return ''
  }
}

// 获取下一步提示
const getNextStep = (): string => {
  switch (props.status) {
    case 0: // 待接单
      return '点击"立即接取"开始任务'
    case 1: // 进行中
      return '完成后提交任务结果等待确认'
    case 2: // 已完成
    case 3: // 已取消
      return '' // 终态不显示下一步
    default:
      return ''
  }
}

// 返回处理
const handleBack = () => {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uni.switchTab({ url: '/pages/home/index' })
  }
  emit('back')
}

// 时间格式化
const formatTime = (time: string): string => {
  if (!time) return ''

  const now = new Date()
  const publishDate = new Date(time)
  const diff = now.getTime() - publishDate.getTime()

  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`

  return publishDate.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 监听滚动实现吸顶效果（在父页面中调用）
const updateStickyState = (scrollTop: number) => {
  isSticky.value = scrollTop > 200
}

defineExpose({ updateStickyState })
</script>

<style lang="scss" scoped>
.task-header {
  width: 100%;
}

/* 导航栏 */
.nav-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20rpx);
  padding: 20rpx 32rpx;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &.nav-bar-sticky {
    box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.08);
  }
}

.nav-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1200rpx;
  margin: 0 auto;
}

.nav-left {
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: opacity 0.2s;

  &:active {
    opacity: 0.7;
  }
}

.icon-back {
  font-size: 40rpx;
  color: #1F2937;
  margin-right: 8rpx;
}

.nav-text {
  font-size: 28rpx;
  color: #1F2937;
}

.nav-title {
  flex: 1;
  text-align: center;
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding: 0 32rpx;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.nav-icon {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: transform 0.2s;

  &:active {
    transform: scale(0.9);
  }

  .icon {
    font-size: 40rpx;
    color: #6B7280;
  }
}

/* 标题区 */
.title-section {
  padding: 120rpx 32rpx 32rpx;
  background: linear-gradient(180deg, #F9FBFF 0%, #FFFFFF 100%);
}

.title-row {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.task-title {
  flex: 1;
  font-size: 44rpx;
  font-weight: 600;
  color: #1F2937;
  line-height: 1.4;
}

.category-tag {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  border-radius: 12rpx;
  white-space: nowrap;
}

.category-icon {
  font-size: 28rpx;
}

.category-text {
  font-size: 24rpx;
  color: #FFFFFF;
  font-weight: 500;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
  border-radius: 20rpx;
  font-size: 26rpx;
  font-weight: 500;

  &.status-pending {
    background: #DBEAFE;
    color: #3B82F6;

    .badge-dot {
      animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
    }
  }

  &.status-active {
    background: #FEF3C7;
    color: #F59E0B;

    .badge-dot {
      animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
    }
  }

  &.status-completed {
    background: #D1FAE5;
    color: #10B981;
  }

  &.status-cancelled {
    background: #F3F4F6;
    color: #6B7280;
  }
}

.badge-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: currentColor;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

/* 紧凑状态卡片 */
.status-card-compact {
  margin: 0 32rpx 24rpx;
  padding: 24rpx 32rpx;
  background: linear-gradient(135deg, #F0F9FF 0%, #E0F2FE 100%);
  border-radius: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  border: 1rpx solid #BAE6FD;
}

.status-main {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.status-icon-large {
  width: 80rpx;
  height: 80rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.status-emoji {
  font-size: 44rpx;
}

.status-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.status-label-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 4rpx;
}

.status-label {
  font-size: 22rpx;
  color: #6B7280;
  font-weight: 500;
}

.status-badge-inline {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 6rpx 16rpx;
  border-radius: 12rpx;
  font-size: 24rpx;
  font-weight: 600;
}

.badge-dot-inline {
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  animation: pulse-dot 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse-dot {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.badge-text-inline {
  font-size: 24rpx;
  font-weight: 600;
}

.status-hint {
  font-size: 26rpx;
  color: #1F2937;
  font-weight: 500;
  line-height: 1.5;
}

.status-next {
  font-size: 24rpx;
  color: #6B7280;
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 4rpx;
}

.next-arrow {
  color: #3B82F6;
  font-weight: 600;
  font-size: 22rpx;
}

/* PC端适配 */
@media screen and (min-width: 768px) {
  .nav-bar {
    padding: 24rpx 48rpx;
  }

  .title-section {
    padding: 140rpx 48rpx 48rpx;
  }

  .task-title {
    font-size: 56rpx;
  }

  .status-card-compact {
    margin: 0 48rpx 32rpx;
    padding: 32rpx 40rpx;
  }

  .status-icon-large {
    width: 96rpx;
    height: 96rpx;
  }

  .status-emoji {
    font-size: 52rpx;
  }

  .status-hint {
    font-size: 28rpx;
  }
}
</style>
