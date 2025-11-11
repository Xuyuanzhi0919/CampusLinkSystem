<template>
  <view class="user-avatar-wrapper" @click="handleClick">
    <!-- 头像按钮 -->
    <view class="avatar-button" :class="{ 'avatar-active': isActive }">
      <!-- 用户头像 -->
      <image v-if="avatarUrl" class="avatar-image" :src="avatarUrl" mode="aspectFill" />
      <!-- 默认渐变头像 -->
      <view v-else class="avatar-default">
        <text class="avatar-text">{{ displayText }}</text>
      </view>
      <!-- 在线状态指示器 -->
      <view v-if="showOnlineStatus" class="online-indicator"></view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  avatarUrl?: string
  nickname?: string
  isActive?: boolean
  showOnlineStatus?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  avatarUrl: '',
  nickname: '',
  isActive: false,
  showOnlineStatus: true
})

const emit = defineEmits(['click'])

// 显示文字（昵称首字母）
const displayText = computed(() => {
  if (props.nickname) {
    return props.nickname.charAt(0).toUpperCase()
  }
  return 'U'
})

const handleClick = () => {
  emit('click')
}
</script>

<style scoped lang="scss">
.user-avatar-wrapper {
  position: relative;
  cursor: pointer;
}

/* 头像按钮 */
.avatar-button {
  position: relative;
  width: 72rpx; /* 36px */
  height: 72rpx;
  border-radius: 50%;
  overflow: hidden;
  border: 2rpx solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
  transition: all 0.25s ease; /* 优化：缩短为0.25s，更敏捷 */
  cursor: pointer;

  &:hover {
    /* 优化：增强视觉反馈，明确可点击性 */
    box-shadow: 0 0 20rpx rgba(59, 130, 246, 0.4);
    transform: scale(1.05);
    border-color: rgba(59, 130, 246, 0.3);
  }

  &.avatar-active {
    /* 优化：点击后保持发光状态 */
    transform: scale(1.05);
    box-shadow: 0 0 24rpx rgba(59, 130, 246, 0.5);
    border-color: rgba(59, 130, 246, 0.5);
  }
}

/* 用户头像图片 */
.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 默认渐变头像 */
.avatar-default {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #3B82F6, #60A5FA);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-text {
  font-size: 32rpx; /* 16px */
  font-weight: 600;
  color: #FFFFFF;
  line-height: 1;
}

/* 在线状态指示器 */
.online-indicator {
  position: absolute;
  bottom: 2rpx;
  right: 2rpx;
  width: 16rpx; /* 8px */
  height: 16rpx;
  background: #22C55E;
  border: 2rpx solid #FFFFFF;
  border-radius: 50%;
  box-shadow: 0 2rpx 8rpx rgba(34, 197, 94, 0.4);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.8;
    transform: scale(0.9);
  }
}
</style>
