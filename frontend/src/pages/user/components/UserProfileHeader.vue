<template>
  <!-- ========== A层:身份与成长(全宽,高度160-180px) ========== -->
  <view class="identity-growth-layer">
    <!-- 🎯 第一段:身份信息 -->
    <view class="identity-section">
      <!-- 头像 -->
      <image
        class="avatar"
        :src="profile?.avatarUrl || defaultAvatar"
        mode="aspectFill"
        @click="handleAvatarClick"
      />

      <!-- 身份信息 -->
      <view class="identity-info">
        <view class="name-line">
          <text class="nickname">{{ profile?.nickname || '未设置昵称' }}</text>
          <LevelBadge :level="profile?.level || 1" />
        </view>
        <view class="title-line">
          <text class="user-title">{{ userTitle }}</text>
          <view class="ranking-badge">
            <Icon name="trending-up" :size="12" class="ranking-icon" />
            <text class="ranking-text">{{ rankingFullText }}</text>
          </view>
        </view>
        <text class="user-meta">{{ studentIdText }} · {{ profile?.schoolName || '未设置学校' }}</text>
      </view>
    </view>

    <!-- 🎯 第二段:积分进度条(横向) -->
    <view class="growth-status" @click="handlePointsClick">
      <view class="status-header">
        <view class="points-info">
          <text class="points-label">当前积分</text>
          <text class="points-value">{{ profile?.points || 0 }}</text>
        </view>
        <view class="level-progress-info">
          <text class="progress-text">{{ levelProgressText }}</text>
          <Icon name="chevron-right" :size="14" class="status-arrow" />
        </view>
      </view>
      <!-- 🎯 进度条 -->
      <view class="progress-bar-container">
        <view class="progress-bar">
          <view class="progress-fill" :style="{ width: levelProgress + '%' }"></view>
        </view>
        <text class="progress-label">{{ levelProgressPercent }}</text>
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
  pointsClick: []
}>()

// 默认头像
const defaultAvatar = 'https://via.placeholder.com/200x200.png?text=Avatar'

// 学号文本
const studentIdText = computed(() => {
  return props.profile?.studentId || '未设置学号'
})

// 🎯 用户称号(根据积分/等级)
const userTitle = computed(() => {
  const points = props.profile?.points || 0
  if (points > 5000) return '资深贡献者'
  if (points > 2000) return '活跃贡献者'
  if (points > 1000) return '热心助人者'
  if (points > 500) return '积极参与者'
  return '新手成员'
})

// 🎯 排名完整文本(全站前X%)
const rankingFullText = computed(() => {
  const points = props.profile?.points || 0
  if (points > 5000) return '全站前 5%'
  if (points > 2000) return '全站前 15%'
  if (points > 1000) return '全站前 30%'
  if (points > 500) return '全站前 50%'
  return '全站前 80%'
})

// 🎯 等级进度(距离下一级)
const levelProgress = computed(() => {
  // TODO: 后端提供真实等级规则
  // 这里简化:每500积分升1级
  const points = props.profile?.points || 0
  const currentLevelPoints = Math.floor(points / 500) * 500
  const nextLevelPoints = currentLevelPoints + 500
  const progressPoints = points - currentLevelPoints
  return Math.floor((progressPoints / 500) * 100)
})

// 等级进度文本
const levelProgressText = computed(() => {
  const points = props.profile?.points || 0
  const currentLevelPoints = Math.floor(points / 500) * 500
  const nextLevelPoints = currentLevelPoints + 500
  const remainingPoints = nextLevelPoints - points
  return `距离下一级还差 ${remainingPoints} 分`
})

// 等级进度百分比
const levelProgressPercent = computed(() => {
  return `${levelProgress.value}%`
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
 * 处理积分点击
 */
const handlePointsClick = () => {
  emit('pointsClick')
}
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

/* ========== A层:身份与成长(全宽,高度160-180px) ========== */
.identity-growth-layer {
  // 🎯 浅蓝灰背景:轻盈、校园气质
  background: #EEF2FF;
  padding: 40rpx 32rpx 32rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

/* 🎯 第一段:身份信息 */
.identity-section {
  display: flex;
  align-items: flex-start;
  gap: 24rpx;
}

.avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  border: 4rpx solid $white; // 🎯 白色边框(从浅蓝灰中跳出)
  background: $white;
  flex-shrink: 0;
  box-shadow: 0 4rpx 12rpx rgba(37, 99, 235, 0.12); // 淡蓝色阴影
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
  font-size: 36rpx;
  font-weight: 700;
  color: #111827; // 🎯 深灰文字(浅背景上)
  @include text-ellipsis(1);
}

.title-line {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-wrap: wrap;
}

.user-title {
  font-size: 24rpx;
  color: #9CA3AF; // 🎯 中性灰
  font-weight: 500;
  padding: 4rpx 12rpx;
  background: #F3F4F6; // 浅灰背景
  border-radius: 8rpx;
  white-space: nowrap;
}

.ranking-badge {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: 4rpx 10rpx;
  background: #DBEAFE; // 浅蓝背景
  border-radius: 8rpx;
}

.ranking-icon {
  color: #2563EB; // 🎯 品牌蓝
}

.ranking-text {
  font-size: 22rpx;
  color: #2563EB; // 🎯 品牌蓝
  font-weight: 600;
  white-space: nowrap;
}

.user-meta {
  font-size: 24rpx;
  color: #6B7280; // 🎯 中性灰
  @include text-ellipsis(1);
  margin-top: 2rpx;
}

/* 🎯 第二段:积分进度条 */
.growth-status {
  // 🎯 白色卡片浮层
  background: $white;
  border-radius: 16rpx;
  padding: 20rpx 24rpx; // 🎯 收窄高度
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);

  &:active {
    background: #F9FAFB;
    transform: scale(0.98);
  }
}

.status-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx; // 🎯 缩小间距
}

.points-info {
  display: flex;
  align-items: baseline;
  gap: 8rpx;
}

.points-label {
  font-size: 24rpx;
  color: #6B7280; // 🎯 中性灰
  font-weight: 500;
}

.points-value {
  font-size: 40rpx;
  font-weight: 700;
  color: #111827; // 🎯 深灰(白卡片上)
}

.level-progress-info {
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.progress-text {
  font-size: 22rpx;
  color: #6B7280; // 🎯 中性灰
  font-weight: 500;
}

.status-arrow {
  color: #9CA3AF; // 🎯 浅灰
  flex-shrink: 0;
}

/* 🎯 进度条(核心视觉元素) */
.progress-bar-container {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.progress-bar {
  flex: 1;
  height: 8rpx;
  background: #E5E7EB; // 🎯 中性灰背景
  border-radius: 4rpx;
  overflow: hidden;
  position: relative;
}

.progress-fill {
  height: 100%;
  background: #2563EB; // 🎯 品牌蓝(唯一强调色)
  border-radius: 4rpx;
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: none; // 🎯 移除发光
  position: relative;
}

.progress-label {
  font-size: 22rpx;
  color: #6B7280; // 🎯 中性灰
  font-weight: 600;
  white-space: nowrap;
  min-width: 60rpx;
  text-align: right;
}
</style>
