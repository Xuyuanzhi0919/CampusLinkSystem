<template>
  <!-- H5底部固定操作栏 -->
  <view class="task-action-bar">
    <view class="action-bar-left">
      <view class="status-info">
        <text class="status-label">状态：</text>
        <text class="status-text" :class="`status-${status}`">{{ statusText }}</text>
      </view>
      <text class="deadline-hint" v-if="deadlineText">{{ deadlineText }}</text>
    </view>

    <view class="action-bar-right">
      <view class="action-btn secondary" @click="$emit('contact')" v-if="!isPublisher">
        <text class="btn-text">联系发布者</text>
      </view>

      <view
        class="action-btn"
        :class="buttonClass"
        @click="handleMainAction"
      >
        <text class="btn-text">{{ buttonText }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  status: number // 0待接单 1进行中 2已完成 3已取消
  deadline?: string
  isPublisher?: boolean
  isAccepter?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  isPublisher: false,
  isAccepter: false
})

const emit = defineEmits<{
  contact: []
  accept: []
  complete: []
  cancel: []
}>()

const statusText = computed(() => {
  const statusMap: Record<number, string> = {
    0: '待接单',
    1: '进行中',
    2: '已完成',
    3: '已取消'
  }
  return statusMap[props.status] || '未知'
})

const deadlineText = computed(() => {
  if (!props.deadline || props.status >= 2) return ''

  const now = new Date().getTime()
  const deadline = new Date(props.deadline).getTime()
  const diff = deadline - now

  if (diff <= 0) return '已截止'

  const hours = Math.floor(diff / 3600000)
  if (hours < 2) return `${Math.floor(diff / 60000)}分钟后截止`
  if (hours < 24) return `${hours}小时后截止`

  return ''
})

const buttonText = computed(() => {
  if (props.isPublisher) {
    return props.status === 1 ? '等待完成' : '已发布'
  }

  if (props.isAccepter) {
    return props.status === 1 ? '标记完成' : '已接单'
  }

  switch (props.status) {
    case 0: return '立即接取'
    case 1: return '进行中'
    case 2: return '已完成'
    case 3: return '已取消'
    default: return '未知'
  }
})

const buttonClass = computed(() => {
  if (props.status === 0 && !props.isPublisher) return 'primary'
  if (props.status === 1 && props.isAccepter) return 'warning'
  return 'disabled'
})

const handleMainAction = () => {
  if (props.status === 0 && !props.isPublisher && !props.isAccepter) {
    emit('accept')
  } else if (props.status === 1 && props.isAccepter) {
    emit('complete')
  }
}
</script>

<style lang="scss" scoped>
.task-action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #FFFFFF;
  padding: 20rpx 32rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24rpx;
  z-index: 999;
}

.action-bar-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.status-info {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.status-label {
  font-size: 24rpx;
  color: #9CA3AF;
}

.status-text {
  font-size: 26rpx;
  font-weight: 600;

  &.status-0 { color: #3B82F6; }
  &.status-1 { color: #F59E0B; }
  &.status-2 { color: #10B981; }
  &.status-3 { color: #6B7280; }
}

.deadline-hint {
  font-size: 22rpx;
  color: #EF4444;
}

.action-bar-right {
  display: flex;
  gap: 16rpx;
}

.action-btn {
  padding: 20rpx 32rpx;
  border-radius: 16rpx;
  font-size: 28rpx;
  font-weight: 500;
  white-space: nowrap;
  transition: all 0.2s;

  &.secondary {
    background: #F3F4F6;
    color: #4B5563;
    &:active { background: #E5E7EB; }
  }

  &.primary {
    background: #3B82F6;
    color: #FFFFFF;
    &:active { background: #2563EB; }
  }

  &.warning {
    background: #F59E0B;
    color: #FFFFFF;
    &:active { background: #D97706; }
  }

  &.disabled {
    background: #9CA3AF;
    color: #FFFFFF;
    opacity: 0.5;
  }
}
</style>
