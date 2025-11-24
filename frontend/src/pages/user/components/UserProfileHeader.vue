<template>
  <view class="profile-header">
    <!-- 用户基本信息 -->
    <view class="user-info">
      <!-- 头像 -->
      <image
        class="avatar"
        :src="profile?.avatarUrl || defaultAvatar"
        mode="aspectFill"
        @click="handleAvatarClick"
        @longpress="handleEditProfile"
      />

      <!-- 用户资料 -->
      <view class="info-content">
        <view class="name-row">
          <text class="nickname">{{ profile?.nickname || '未设置昵称' }}</text>
          <LevelBadge :level="profile?.level || 1" />
        </view>

        <view class="meta-row">
          <text class="student-id">{{ studentIdText }}</text>
          <text class="school-name">{{ profile?.schoolName || '未设置学校' }}</text>
        </view>

        <view class="points-row" @click="handlePointsClick">
          <text class="points-label">积分</text>
          <text class="points-value">{{ profile?.points || 0 }}</text>
          <text class="points-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 统计数据 -->
    <view class="stats-grid">
      <view
        v-for="stat in statsData"
        :key="stat.key"
        class="stat-item"
        @click="handleStatClick(stat.key)"
      >
        <text class="stat-value">{{ stat.value }}</text>
        <text class="stat-label">{{ stat.label }}</text>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <view class="btn btn-edit" @click="handleEditProfile">
        <text class="btn-icon">✏️</text>
        <text class="btn-text">编辑资料</text>
      </view>

      <view class="btn btn-checkin" :class="{ disabled: isCheckedIn }" @click="handleCheckIn">
        <text class="btn-icon">📅</text>
        <text class="btn-text">{{ checkInButtonText }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { UserProfileData, UserStatsData } from '@/types/user'
import LevelBadge from './LevelBadge.vue'

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
}>()

// 默认头像
const defaultAvatar = 'https://via.placeholder.com/200x200.png?text=Avatar'

// 学号文本
const studentIdText = computed(() => {
  return props.profile?.studentId || '未设置学号'
})

// 签到按钮文本
const checkInButtonText = computed(() => {
  return props.isCheckedIn ? '已签到' : '每日签到'
})

// 统计数据配置
const statsData = computed(() => [
  {
    key: 'resources',
    label: '资源',
    value: props.stats?.resourceCount || 0
  },
  {
    key: 'questions',
    label: '问答',
    value: props.stats?.questionCount || 0
  },
  {
    key: 'tasks',
    label: '任务',
    value: (props.stats?.taskPublishCount || 0) + (props.stats?.taskAcceptedCount || 0)
  },
  {
    key: 'favorites',
    label: '收藏',
    value: props.stats?.favoriteCount || 0
  }
])

/**
 * 处理编辑资料
 */
const handleEditProfile = () => {
  emit('editProfile')
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
.profile-header {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 32rpx;
  margin: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
}

// 用户基本信息
.user-info {
  display: flex;
  gap: 24rpx;
  margin-bottom: 32rpx;
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 60rpx;
  background: #F3F4F6;
  flex-shrink: 0;
  cursor: pointer;
  transition: transform 0.2s;

  &:active {
    transform: scale(0.95);
  }
}

.info-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.nickname {
  font-size: 36rpx;
  font-weight: 600;
  color: #1F2937;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-top: 8rpx;
}

.student-id,
.school-name {
  font-size: 24rpx;
  color: #6B7280;
}

.school-name {
  &::before {
    content: '•';
    margin-right: 8rpx;
  }
}

.points-row {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 12rpx;
  padding: 8rpx 12rpx 8rpx 0;
  cursor: pointer;
  transition: all 0.2s;
  border-radius: 8rpx;
  margin-left: -12rpx;
  padding-left: 12rpx;

  &:active {
    background: #FEF3C7;
    transform: scale(0.98);
  }
}

.points-label {
  font-size: 24rpx;
  color: #6B7280;
}

.points-value {
  font-size: 28rpx;
  font-weight: 600;
  color: #F59E0B;
}

.points-arrow {
  font-size: 32rpx;
  color: #D97706;
  font-weight: bold;
  margin-left: 4rpx;
}

// 统计数据
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24rpx;
  padding: 24rpx 0;
  border-top: 1rpx solid #F3F4F6;
  border-bottom: 1rpx solid #F3F4F6;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  cursor: pointer;
  transition: transform 0.2s;

  &:active {
    transform: scale(0.95);
  }
}

.stat-value {
  font-size: 32rpx;
  font-weight: 600;
  color: #2563EB;
}

.stat-label {
  font-size: 24rpx;
  color: #6B7280;
}

// 操作按钮
.action-buttons {
  display: flex;
  gap: 16rpx;
  margin-top: 24rpx;
}

.btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  height: 72rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  cursor: pointer;
  transition: all 0.2s;

  &:active {
    transform: scale(0.98);
  }
}

.btn-edit {
  background: #EFF6FF;
  color: #2563EB;

  &:active {
    background: #DBEAFE;
  }
}

.btn-checkin {
  background: linear-gradient(135deg, #2563EB 0%, #1D4ED8 100%);
  color: #FFFFFF;

  &.disabled {
    background: #E5E7EB;
    color: #9CA3AF;
    cursor: not-allowed;

    &:active {
      transform: none;
    }
  }
}

.btn-icon {
  font-size: 32rpx;
}

.btn-text {
  font-weight: 500;
}
</style>
