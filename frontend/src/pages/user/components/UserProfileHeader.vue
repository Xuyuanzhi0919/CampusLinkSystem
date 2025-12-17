<template>
  <view class="profile-header">
    <!-- ========== ① 顶部：身份卡（三段式纵向主线） ========== -->
    <view class="identity-card">
      <!-- 🎯 第一行：身份 + 成就 -->
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

      <!-- 🎯 第二行：当前成长状态 -->
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
        <!-- 🎯 进度条（非常重要） -->
        <view class="progress-bar-container">
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: levelProgress + '%' }"></view>
          </view>
          <text class="progress-label">{{ levelProgressPercent }}</text>
        </view>
      </view>

      <!-- 🎯 第三行：主行动按钮 -->
      <view class="action-section">
        <!-- 主按钮：发布内容（强调产出） -->
        <view class="primary-action-btn" @click="handlePublish">
          <Icon name="plus-circle" :size="20" class="action-icon" />
          <view class="action-content">
            <text class="action-title">发布内容</text>
            <text class="action-subtitle">分享你的知识</text>
          </view>
        </view>

        <!-- 次按钮组 -->
        <view class="secondary-actions">
          <view
            class="secondary-action-btn"
            :class="{ disabled: isCheckedIn }"
            @click="handleCheckIn"
          >
            <Icon :name="isCheckedIn ? 'check-circle' : 'calendar'" :size="16" class="action-icon-sm" />
            <text class="action-text-sm">{{ checkInButtonText }}</text>
          </view>
          <view class="secondary-action-btn" @click="handlePointsClick">
            <Icon name="trending-up" :size="16" class="action-icon-sm" />
            <text class="action-text-sm">查看成长</text>
          </view>
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
        <!-- 🎯 社会化比较反馈 -->
        <view v-if="stat.comparison" class="stat-comparison">
          <Icon :name="stat.comparison.icon" :size="12" class="comparison-icon" />
          <text class="comparison-text">{{ stat.comparison.text }}</text>
        </view>
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

// 🎯 用户称号（根据积分/等级）
const userTitle = computed(() => {
  const points = props.profile?.points || 0
  if (points > 5000) return '资深贡献者'
  if (points > 2000) return '活跃贡献者'
  if (points > 1000) return '热心助人者'
  if (points > 500) return '积极参与者'
  return '新手成员'
})

// 🎯 排名完整文本（全站前X%）
const rankingFullText = computed(() => {
  const points = props.profile?.points || 0
  if (points > 5000) return '全站前 5%'
  if (points > 2000) return '全站前 15%'
  if (points > 1000) return '全站前 30%'
  if (points > 500) return '全站前 50%'
  return '全站前 80%'
})

// 🎯 等级进度（距离下一级）
const levelProgress = computed(() => {
  // TODO: 后端提供真实等级规则
  // 这里简化：每500积分升1级
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

// 签到按钮文本
const checkInButtonText = computed(() => {
  return props.isCheckedIn ? '已签到' : '每日签到'
})

/**
 * 🎯 计算社会化比较反馈
 * 根据用户数据与平均水平对比,生成激励性文案
 */
const getComparison = (value: number, type: 'resources' | 'questions' | 'tasks' | 'favorites') => {
  // TODO: 后端提供真实的用户平均数据
  // 这里使用模拟数据来演示效果
  const averages = {
    resources: 3,   // 平均上传3个资源
    questions: 8,   // 平均回答8个问题
    tasks: 1,       // 平均进行中1个任务
    favorites: 5    // 平均收藏5个内容
  }

  const avg = averages[type]

  if (value === 0) {
    return {
      icon: 'alert-circle',
      text: '快来试试吧',
      type: 'neutral'
    }
  }

  const percentage = Math.floor((value / avg) * 100)

  if (percentage >= 200) {
    return {
      icon: 'trending-up',
      text: `超过 90% 同学`,
      type: 'excellent'
    }
  } else if (percentage >= 150) {
    return {
      icon: 'trending-up',
      text: `超过 75% 同学`,
      type: 'good'
    }
  } else if (percentage >= 100) {
    return {
      icon: 'trending-up',
      text: `超过 60% 同学`,
      type: 'average'
    }
  } else if (percentage >= 50) {
    return {
      icon: 'flame',
      text: `加油,追赶中`,
      type: 'below'
    }
  } else {
    return {
      icon: 'flame',
      text: `潜力无限`,
      type: 'below'
    }
  }
}

// 🎯 可操作化统计数据（四格卡片）
const actionableStats = computed(() => {
  const taskCount = (props.stats?.taskPublishCount || 0) + (props.stats?.taskAcceptedCount || 0)
  const ongoingTaskCount = props.stats?.taskAcceptedCount || 0 // 假设接受的任务就是进行中的
  const resourceCount = props.stats?.resourceCount || 0
  const questionCount = props.stats?.questionCount || 0
  const favoriteCount = props.stats?.favoriteCount || 0

  return [
    {
      key: 'resources',
      label: '我的资源',
      value: resourceCount,
      icon: 'file-text',
      color: '#2563EB', // primary
      actionText: '查看',
      highlight: false,
      comparison: getComparison(resourceCount, 'resources')
    },
    {
      key: 'questions',
      label: '我的回答',
      value: questionCount,
      icon: 'message-circle',
      color: '#16A34A', // success
      actionText: '查看',
      highlight: false,
      comparison: getComparison(questionCount, 'questions')
    },
    {
      key: 'tasks',
      label: '进行中任务',
      value: ongoingTaskCount,
      icon: 'clock',
      color: '#F59E0B', // warning
      actionText: ongoingTaskCount > 0 ? '去完成' : '查看',
      highlight: ongoingTaskCount > 0, // 🎯 有进行中任务时高亮
      comparison: getComparison(ongoingTaskCount, 'tasks')
    },
    {
      key: 'favorites',
      label: '我的收藏',
      value: favoriteCount,
      icon: 'heart',
      color: '#EF4444', // error
      actionText: '查看',
      highlight: false,
      comparison: getComparison(favoriteCount, 'favorites')
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

/* ========== ① 身份卡（三段式） ========== */
.identity-card {
  // 🎯 浅蓝灰背景:轻盈、校园气质
  background: #EEF2FF;
  border-radius: 24rpx;
  padding: 40rpx 32rpx 32rpx;
  margin: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04); // 极轻阴影
  position: relative;
  overflow: visible; // 改为visible,允许浮层突出
}

/* 🎯 第一行：身份 + 成就 */
.identity-section {
  display: flex;
  align-items: flex-start;
  gap: 24rpx;
  margin-bottom: 32rpx;
  position: relative;
  z-index: 1;
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

/* 🎯 第二行：当前成长状态 */
.growth-status {
  // 🎯 白色卡片浮层
  background: $white;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 32rpx;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  z-index: 1;
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
  margin-bottom: 16rpx;
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

/* 🎯 进度条（核心视觉元素） */
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

/* 🎯 第三行：主行动按钮 */
.action-section {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  position: relative;
  z-index: 1;
}

/* 主按钮：发布内容 */
.primary-action-btn {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx 24rpx; // 🎯 收窄高度
  // 🎯 唯一橙色 CTA
  background: #F97316;
  border-radius: 20rpx; // 🎯 更大圆角(社交感)
  cursor: pointer;
  box-shadow: 0 4rpx 12rpx rgba(249, 115, 22, 0.2); // 轻阴影
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.97);
    background: #EA580C; // 深一度
    box-shadow: 0 2rpx 8rpx rgba(249, 115, 22, 0.15);
  }
}

.action-icon {
  color: $white;
  flex-shrink: 0;
}

.action-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2rpx;
}

.action-title {
  font-size: 30rpx;
  font-weight: 700;
  color: $white;
  line-height: 1.2;
}

.action-subtitle {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.85);
  font-weight: 500;
}

/* 次按钮组（签到、查看成长） */
.secondary-actions {
  display: flex;
  gap: 12rpx;
}

.secondary-action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 16rpx;
  background: $white; // 默认白色
  border: 1rpx solid #E5E7EB;
  border-radius: 12rpx;
  cursor: pointer;
  transition: all 0.2s ease;

  // 🎯 签到按钮特殊处理(浅蓝背景)
  &:first-child:not(.disabled) {
    background: #EFF6FF; // 浅蓝背景
    border-color: #DBEAFE;

    .action-icon-sm {
      color: #2563EB; // 品牌蓝图标
    }

    .action-text-sm {
      color: #1E40AF; // 深蓝文字
    }
  }

  &:active:not(.disabled) {
    background: #F9FAFB;
    transform: scale(0.97);
  }

  &:first-child:active:not(.disabled) {
    background: #DBEAFE; // 签到按钮激活态
  }

  &.disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.action-icon-sm {
  color: #6B7280; // 🎯 中性灰图标
  flex-shrink: 0;
}

.action-text-sm {
  font-size: 26rpx;
  color: #374151; // 🎯 深灰文字
  font-weight: 600;
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
  border-radius: 16rpx; // 🎯 稍小圆角
  padding: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04); // 🎯 极轻阴影
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1rpx solid #F3F4F6; // 浅灰边框

  &:active {
    transform: translateY(-2rpx);
    box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
    border-color: #E5E7EB;
  }

  // 🎯 高亮状态（进行中任务）
  &.highlight {
    border-color: #F97316; // 橙色边框
    background: #FFF7ED; // 极浅橙背景

    .stat-action-text {
      color: #F97316;
      font-weight: 700;
    }

    .stat-action-arrow {
      color: #F97316;
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
  margin-bottom: 4rpx;
}

/* 🎯 社会化比较反馈 - 改为纯文本 */
.stat-comparison {
  display: none; // 🎯 暂时隐藏,降低信息密度
  // 如果需要保留,改为纯灰色文本
  align-items: center;
  gap: 4rpx;
  margin-bottom: 8rpx;
}

.comparison-icon {
  display: none; // 移除图标
}

.comparison-text {
  font-size: 20rpx;
  color: #9CA3AF; // 极淡灰色
  font-weight: 400;
  white-space: nowrap;
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
