<template>
  <view class="club-card" :class="{ active: isActive }" @click="handleClick" @touchstart="onTouchStart" @touchend="onTouchEnd">
    <!-- 社团 Logo 和信息 -->
    <view class="card-header">
      <image
        v-if="club.logoUrl"
        class="logo"
        :src="club.logoUrl"
        mode="aspectFill"
      />
      <view v-else class="logo-placeholder">
        <text class="icon">👥</text>
      </view>

      <view class="info">
        <text class="name">{{ club.clubName }}</text>
        <text class="school">{{ club.schoolName }}</text>
      </view>
    </view>

    <!-- 社团描述 -->
    <view class="description">{{ club.description }}</view>

    <!-- 底部信息 -->
    <view class="card-footer">
      <view class="member-count">
        <text class="icon">👤</text>
        <text class="value">{{ club.memberCount }}</text>
        <text class="unit">成员</text>
      </view>

      <view class="time">{{ formatTime(club.createdAt) }}</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { ClubItem } from '@/types/club'

// Props
interface Props {
  club: ClubItem
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  click: [club: ClubItem]
}>()

// 点击激活状态
const isActive = ref(false)

/**
 * 触摸开始
 */
const onTouchStart = () => {
  isActive.value = true
}

/**
 * 触摸结束
 */
const onTouchEnd = () => {
  setTimeout(() => {
    isActive.value = false
  }, 150)
}

/**
 * 格式化时间
 */
const formatTime = (time: string) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  
  if (diff < minute) {
    return '刚刚创建'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前创建`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前创建`
  } else if (diff < 7 * day) {
    return `${Math.floor(diff / day)}天前创建`
  } else {
    return `创建于 ${time.split(' ')[0]}`
  }
}

/**
 * 点击卡片
 */
const handleClick = () => {
  emit('click', props.club)
}
</script>

<style scoped>
.club-card {
  background: white;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  transition: all 0.2s ease;
  cursor: pointer;
}

.club-card.active {
  background: #F5F7FA;
  transform: scale(0.98);
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.logo {
  width: 80rpx;
  height: 80rpx;
  border-radius: 12rpx;
  margin-right: 20rpx;
}

.logo-placeholder {
  width: 80rpx;
  height: 80rpx;
  border-radius: 12rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.logo-placeholder .icon {
  font-size: 48rpx;
}

.info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.name {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.school {
  font-size: 26rpx;
  color: #999;
}

.description {
  font-size: 28rpx;
  color: #666;
  line-height: 1.6;
  margin-bottom: 16rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.member-count {
  display: flex;
  align-items: center;
  font-size: 26rpx;
  color: #666;
}

.member-count .icon {
  margin-right: 8rpx;
}

.member-count .value {
  font-weight: 600;
  color: #1890ff;
}

.member-count .unit {
  margin-left: 4rpx;
}

.time {
  font-size: 24rpx;
  color: #999;
}
</style>

