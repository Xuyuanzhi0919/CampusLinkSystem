<template>
  <view class="profile-header">
    <!-- ========== ① 顶部：身份卡（重设计） ========== -->
    <view class="identity-card">
      <!-- 用户基本信息行 -->
      <view class="user-basic-info">
        <!-- 头像 -->
        <image
          class="avatar"
          :src="profile?.avatarUrl || defaultAvatar"
          mode="aspectFill"
          @click="handleAvatarClick"
        />

        <!-- 姓名+等级+徽章 -->
        <view class="user-name-section">
          <view class="name-row">
            <text class="nickname">{{ profile?.nickname || '未设置昵称' }}</text>
            <LevelBadge :level="profile?.level || 1" />
            <text v-if="profile?.isVip" class="vip-badge">⭐</text>
          </view>
          <text class="user-meta">{{ studentIdText }} · {{ profile?.schoolName || '未设置学校' }}</text>
        </view>
      </view>

      <!-- 成就数据行（积分+排名） -->
      <view class="achievement-bar" @click="handlePointsClick">
        <view class="achievement-item">
          <text class="achievement-label">积分</text>
          <text class="achievement-value primary">{{ profile?.points || 0 }}</text>
        </view>
        <view class="achievement-divider"></view>
        <view class="achievement-item">
          <text class="achievement-label">排名</text>
          <text class="achievement-value accent">{{ rankingText }}</text>
        </view>
        <Icon name="chevron-right" :size="16" class="achievement-arrow" />
      </view>

      <!-- 🎯 核心行为按钮（取代编辑资料） -->
      <view class="primary-actions">
        <view
          class="action-btn check-in-btn"
          :class="{ disabled: isCheckedIn }"
          @click="handleCheckIn"
        >
          <Icon :name="isCheckedIn ? 'check-circle' : 'calendar'" :size="18" class="action-icon" />
          <text class="action-text">{{ checkInButtonText }}</text>
        </view>

        <view class="action-btn publish-btn" @click="handlePublish">
          <Icon name="plus-circle" :size="18" class="action-icon" />
          <text class="action-text">发布内容</text>
        </view>
      </view>
    </view>

    <!-- ========== ② 中部：可操作化数据卡 ========== -->
    <view class="action-stats-grid">
      <view
        v-for="stat in actionableStats"
        :key="stat.key"
        class="action-stat-card"
        :class="{ highlight: stat.highlight }"
        @click="handleStatClick(stat.key)"
      >
        <view class="stat-header">
          <Icon :name="stat.icon" :size="20" class="stat-icon" :style="{ color: stat.color }" />
          <text class="stat-title">{{ stat.label }}</text>
        </view>
        <text class="stat-number">{{ stat.value }}</text>
        <view class="stat-action">
          <text class="stat-action-text">{{ stat.actionText }}</text>
          <Icon name="arrow-right" :size="14" class="stat-action-arrow" />
        </view>
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
  isCheckedIn: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  editProfile: []
  checkIn: []
  statClick: [key: string]
  pointsClick: []
  publish: []
}>()

// 默认头像
const defaultAvatar = 'https://via.placeholder.com/200x200.png?text=Avatar'

// 学号文本
const studentIdText = computed(() => {
  return props.profile?.studentId || '未设置学号'
})

// 🎯 排名文本（百分比）
const rankingText = computed(() => {
  // TODO: 后端提供真实排名数据
  // 这里暂时用积分模拟排名：积分越高排名越靠前
  const points = props.profile?.points || 0
  if (points > 5000) return 'Top 5%'
  if (points > 2000) return 'Top 15%'
  if (points > 1000) return 'Top 30%'
  if (points > 500) return 'Top 50%'
  return 'Top 80%'
})

// 签到按钮文本
const checkInButtonText = computed(() => {
  return props.isCheckedIn ? '今日已签' : '每日签到'
})

// 🎯 可操作化统计数据（四格卡片）
const actionableStats = computed(() => {
  const taskCount = (props.stats?.taskPublishCount || 0) + (props.stats?.taskAcceptedCount || 0)
  const ongoingTaskCount = props.stats?.taskAcceptedCount || 0 // 假设接受的任务就是进行中的

  return [
    {
      key: 'resources',
      label: '我的资源',
      value: props.stats?.resourceCount || 0,
      icon: 'file-text',
      color: '#2563EB', // primary
      actionText: '查看',
      highlight: false
    },
    {
      key: 'questions',
      label: '我的回答',
      value: props.stats?.questionCount || 0,
      icon: 'message-circle',
      color: '#16A34A', // success
      actionText: '查看',
      highlight: false
    },
    {
      key: 'tasks',
      label: '进行中任务',
      value: ongoingTaskCount,
      icon: 'clock',
      color: '#F59E0B', // warning
      actionText: ongoingTaskCount > 0 ? '去完成' : '查看',
      highlight: ongoingTaskCount > 0 // 🎯 有进行中任务时高亮
    },
    {
      key: 'favorites',
      label: '我的收藏',
      value: props.stats?.favoriteCount || 0,
      icon: 'heart',
      color: '#EF4444', // error
      actionText: '查看',
      highlight: false
    }
  ]
})

/**
 * 🎯 处理头像点击 - 预览头像
 */
const handleAvatarClick = () => {
  const avatarUrl = props.profile?.avatarUrl || defaultAvatar

  uni.previewImage({
    current: avatarUrl,
    urls: [avatarUrl],
    longPressActions: {
      itemList: ['保存图片', '编辑资料'],
      success: (data) => {
        if (data.tapIndex === 1) {
          // 点击"编辑资料"
          emit('editProfile')
        } else if (data.tapIndex === 0) {
          // 点击"保存图片"
          uni.showToast({
            title: '长按图片可保存',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 处理签到
 */
const handleCheckIn = () => {
  if (!props.isCheckedIn) {
    emit('checkIn')
  }
}

/**
 * 🎯 处理发布内容（强转化按钮）
 */
const handlePublish = () => {
  emit('publish')
}

/**
 * 处理统计项点击
 */
const handleStatClick = (key: string) => {
  emit('statClick', key)
}

/**
 * 处理积分点击
 */
const handlePointsClick = () => {
  emit('pointsClick')
}
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

.profile-header {
  background: transparent;
  padding: 0;
  margin: 0;
}

/* ========== ① 身份卡 ========== */
.identity-card {
  background: linear-gradient(135deg, #2563EB 0%, #1D4ED8 100%);
  border-radius: 24rpx;
  padding: 40rpx 32rpx;
  margin: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(37, 99, 235, 0.25);
  position: relative;
  overflow: hidden;

  // 🎯 装饰性背景
  &::before {
    content: '';
    position: absolute;
    top: -100rpx;
    right: -100rpx;
    width: 300rpx;
    height: 300rpx;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
    border-radius: 50%;
    pointer-events: none;
  }
}

/* 用户基本信息行 */
.user-basic-info {
  display: flex;
  align-items: center;
  gap: 24rpx;
  margin-bottom: 32rpx;
  position: relative;
  z-index: 1;
}

.avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.2);
  background: $white;
  flex-shrink: 0;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
  transition: transform 0.2s ease;

  &:active {
    transform: scale(0.95);
  }
}

.user-name-section {
  flex: 1;
  min-width: 0;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 8rpx;
}

.nickname {
  font-size: 36rpx;
  font-weight: 700;
  color: $white;
  @include text-ellipsis(1);
}

.vip-badge {
  font-size: 24rpx;
  margin-left: -4rpx;
}

.user-meta {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
  @include text-ellipsis(1);
}

/* 成就数据行 */
.achievement-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 16rpx;
  padding: 20rpx 24rpx;
  margin-bottom: 24rpx;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  z-index: 1;

  &:active {
    background: rgba(255, 255, 255, 0.2);
    transform: scale(0.98);
  }
}

.achievement-item {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  flex: 1;
}

.achievement-label {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.7);
}

.achievement-value {
  font-size: 32rpx;
  font-weight: 700;

  &.primary {
    color: #FFD699; // 橙色（积分）
  }

  &.accent {
    color: #A5F3FC; // 青色（排名）
  }
}

.achievement-divider {
  width: 1rpx;
  height: 40rpx;
  background: rgba(255, 255, 255, 0.2);
  margin: 0 16rpx;
}

.achievement-arrow {
  color: rgba(255, 255, 255, 0.6);
  flex-shrink: 0;
}

/* 🎯 核心行为按钮 */
.primary-actions {
  display: flex;
  gap: 16rpx;
  position: relative;
  z-index: 1;
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  height: 72rpx;
  border-radius: 16rpx;
  cursor: pointer;
  transition: all 0.2s ease;
  font-weight: 600;
}

.check-in-btn {
  background: rgba(255, 255, 255, 0.2);
  border: 1rpx solid rgba(255, 255, 255, 0.3);

  .action-icon {
    color: $white;
  }

  .action-text {
    color: $white;
    font-size: 28rpx;
  }

  &:active:not(.disabled) {
    background: rgba(255, 255, 255, 0.25);
    transform: scale(0.97);
  }

  &.disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.publish-btn {
  background: linear-gradient(135deg, #FF6B35 0%, #F59E0B 100%);
  box-shadow: 0 4rpx 16rpx rgba(255, 107, 53, 0.3);

  .action-icon {
    color: $white;
  }

  .action-text {
    color: $white;
    font-size: 28rpx;
  }

  &:active {
    transform: scale(0.97);
    box-shadow: 0 2rpx 8rpx rgba(255, 107, 53, 0.2);
  }
}

/* ========== ② 可操作化数据卡 ========== */
.action-stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;
  padding: 0 24rpx 24rpx;
}

.action-stat-card {
  background: $white;
  border-radius: 20rpx;
  padding: 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2rpx solid transparent;

  &:active {
    transform: translateY(-2rpx);
    box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.1);
  }

  // 🎯 高亮状态（进行中任务）
  &.highlight {
    border-color: $accent;
    background: linear-gradient(135deg, #FFF3E0 0%, #FFE8CC 100%);

    .stat-action-text {
      color: $accent;
      font-weight: 700;
    }

    .stat-action-arrow {
      color: $accent;
    }
  }
}

.stat-header {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 12rpx;
}

.stat-icon {
  flex-shrink: 0;
}

.stat-title {
  font-size: 26rpx;
  color: $gray-600;
  font-weight: 500;
}

.stat-number {
  display: block;
  font-size: 48rpx;
  font-weight: 700;
  color: $gray-900;
  line-height: 1.2;
  margin-bottom: 8rpx;
}

.stat-action {
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.stat-action-text {
  font-size: 24rpx;
  color: $primary;
  font-weight: 500;
}

.stat-action-arrow {
  color: $primary;
  flex-shrink: 0;
  transition: transform 0.2s ease;
}

.action-stat-card:active .stat-action-arrow {
  transform: translateX(4rpx);
}
</style>
