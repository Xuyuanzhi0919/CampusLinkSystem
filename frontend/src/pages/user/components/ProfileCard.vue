<template>
  <!-- ========== 个人名片(唯一主视觉) ========== -->
  <view class="profile-card">
    <!-- 🎯 身份信息 -->
    <view class="identity-section">
      <image
        class="avatar"
        :src="profile?.avatarUrl || defaultAvatar"
        mode="aspectFill"
        @click="handleAvatarClick"
      />
      <view class="identity-info">
        <view class="name-line">
          <text class="nickname">{{ profile?.nickname || '未设置昵称' }}</text>
          <LevelBadge :level="profile?.level || 1" />
        </view>
        <text class="user-meta">{{ userTitle }} · {{ profile?.schoolName || '未设置学校' }}</text>
      </view>
    </view>

    <!-- 🎯 数据横条(融合在名片中) -->
    <view class="stats-bar">
      <view class="stat-item" @click="$emit('pointsClick')">
        <text class="stat-value">{{ profile?.points || 0 }}</text>
        <text class="stat-label">积分</text>
      </view>
      <view class="stat-divider" />
      <view class="stat-item" @click="$emit('statClick', 'resources')">
        <text class="stat-value">{{ stats?.resourceCount || 0 }}</text>
        <text class="stat-label">资源</text>
      </view>
      <view class="stat-divider" />
      <view class="stat-item" @click="$emit('statClick', 'questions')">
        <text class="stat-value">{{ stats?.questionCount || 0 }}</text>
        <text class="stat-label">回答</text>
      </view>
      <view class="stat-divider" />
      <view class="stat-item" @click="$emit('statClick', 'favorites')">
        <text class="stat-value">{{ stats?.favoriteCount || 0 }}</text>
        <text class="stat-label">收藏</text>
      </view>
    </view>

    <!-- 🎯 进度条(融合在名片中) -->
    <view class="progress-section" @click="$emit('pointsClick')">
      <view class="progress-bar">
        <view class="progress-fill" :style="{ width: levelProgress + '%' }"></view>
      </view>
      <text class="progress-text">{{ levelProgressText }}</text>
    </view>

    <!-- 🎯 主行动区(融合在名片中) -->
    <view class="action-buttons">
      <view class="primary-btn" @click="$emit('publish')">
        <Icon name="plus-circle" :size="18" class="btn-icon" />
        <text class="btn-text">发布内容</text>
      </view>
      <view class="secondary-btn" @click="$emit('pointsClick')">
        <Icon name="trending-up" :size="18" class="btn-icon" />
        <text class="btn-text">查看成长</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { UserProfileData, UserStatsData } from '@/types/user'
import LevelBadge from './LevelBadge.vue'
import Icon from '@/components/icons/index.vue'

interface Props {
  profile: UserProfileData | null
  stats: UserStatsData | null
}

const props = defineProps<Props>()

const emit = defineEmits<{
  editProfile: []
  pointsClick: []
  statClick: [key: string]
  publish: []
}>()

const defaultAvatar = 'https://via.placeholder.com/200x200.png?text=Avatar'

// 用户称号
const userTitle = computed(() => {
  const points = props.profile?.points || 0
  if (points > 5000) return '资深贡献者'
  if (points > 2000) return '活跃贡献者'
  if (points > 1000) return '热心助人者'
  if (points > 500) return '积极参与者'
  return '新手成员'
})

// 等级进度
const levelProgress = computed(() => {
  const points = props.profile?.points || 0
  const currentLevelPoints = Math.floor(points / 500) * 500
  const progressPoints = points - currentLevelPoints
  return Math.floor((progressPoints / 500) * 100)
})

const levelProgressText = computed(() => {
  const points = props.profile?.points || 0
  const currentLevelPoints = Math.floor(points / 500) * 500
  const nextLevelPoints = currentLevelPoints + 500
  const remainingPoints = nextLevelPoints - points
  return `距离下一级还差 ${remainingPoints} 分`
})

const handleAvatarClick = () => {
  emit('editProfile')
}
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

/* ========== 个人名片(唯一主视觉) ========== */
.profile-card {
  background: linear-gradient(180deg, #EFF6FF 0%, #DBEAFE 100%);
  padding: 48rpx 32rpx 40rpx;
  display: flex;
  flex-direction: column;
  gap: 32rpx;
}

/* 🎯 身份信息 */
.identity-section {
  display: flex;
  align-items: center;
  gap: 24rpx;
}

.avatar {
  width: 112rpx; // 🎯 加大头像
  height: 112rpx;
  border-radius: 50%;
  border: 4rpx solid $white;
  background: $white;
  flex-shrink: 0;
  box-shadow: 0 8rpx 24rpx rgba(37, 99, 235, 0.15);
  transition: transform 0.2s ease;

  &:active {
    transform: scale(0.95);
  }
}

.identity-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.name-line {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.nickname {
  font-size: 40rpx; // 🎯 加大昵称
  font-weight: 700;
  color: #111827;
  @include text-ellipsis(1);
}

.user-meta {
  font-size: 26rpx;
  color: #6B7280;
  font-weight: 500;
  @include text-ellipsis(1);
}

/* 🎯 数据横条 */
.stats-bar {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 24rpx 0;
  background: $white;
  border-radius: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
  cursor: pointer;
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.95);
  }
}

.stat-value {
  font-size: 36rpx;
  font-weight: 700;
  color: #2563EB; // 品牌蓝
  line-height: 1;
}

.stat-label {
  font-size: 24rpx;
  color: #9CA3AF;
  font-weight: 500;
}

.stat-divider {
  width: 1rpx;
  height: 48rpx;
  background: #E5E7EB;
}

/* 🎯 进度条 */
.progress-section {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  cursor: pointer;
}

.progress-bar {
  width: 100%;
  height: 12rpx; // 🎯 加粗进度条
  background: rgba(255, 255, 255, 0.5);
  border-radius: 6rpx;
  overflow: hidden;
  position: relative;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #2563EB 0%, #3B82F6 100%);
  border-radius: 6rpx;
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.progress-text {
  font-size: 24rpx;
  color: #6B7280;
  font-weight: 500;
  text-align: center;
}

/* 🎯 主行动按钮 */
.action-buttons {
  display: flex;
  gap: 16rpx;
}

.primary-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 20rpx;
  background: #F97316;
  border-radius: 20rpx;
  cursor: pointer;
  box-shadow: 0 4rpx 12rpx rgba(249, 115, 22, 0.2);
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.97);
    background: #EA580C;
  }
}

.secondary-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 20rpx;
  background: $white;
  border: 2rpx solid #E5E7EB;
  border-radius: 20rpx;
  cursor: pointer;
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.97);
    background: #F9FAFB;
  }
}

.btn-icon {
  flex-shrink: 0;

  .primary-btn & {
    color: $white;
  }

  .secondary-btn & {
    color: #2563EB;
  }
}

.btn-text {
  font-size: 28rpx;
  font-weight: 600;

  .primary-btn & {
    color: $white;
  }

  .secondary-btn & {
    color: #374151;
  }
}
</style>
