<template>
  <view class="user-reputation">
    <view class="reputation-header">
      <view class="header-left">
        <text class="header-icon">🏆</text>
        <text class="header-title">信誉等级</text>
      </view>
      <view class="level-badge" :style="{ background: getLevelColor() }">
        <text class="level-text">{{ getLevelText() }}</text>
      </view>
    </view>
    <view class="reputation-content">
      <!-- 信誉分数 -->
      <view class="score-section">
        <view class="score-display">
          <text class="score-number">{{ score }}</text>
          <text class="score-max">/1000</text>
        </view>
        <view class="score-progress">
          <view class="progress-bar">
            <view class="progress-fill" :style="{ width: `${(score / 1000) * 100}%` }"></view>
          </view>
        </view>
      </view>

      <!-- 信誉指标 -->
      <view class="metrics-section">
        <view class="metric-item">
          <view class="metric-icon-wrapper" style="background: #DBEAFE">
            <text class="metric-icon">✅</text>
          </view>
          <view class="metric-details">
            <text class="metric-label">完成任务</text>
            <text class="metric-value">{{ completedTasks }}次</text>
          </view>
        </view>
        <view class="metric-item">
          <view class="metric-icon-wrapper" style="background: #D1FAE5">
            <text class="metric-icon">⭐</text>
          </view>
          <view class="metric-details">
            <text class="metric-label">好评率</text>
            <text class="metric-value">{{ positiveRate }}%</text>
          </view>
        </view>
        <view class="metric-item">
          <view class="metric-icon-wrapper" style="background: #FEF3C7">
            <text class="metric-icon">🎯</text>
          </view>
          <view class="metric-details">
            <text class="metric-label">响应速度</text>
            <text class="metric-value">{{ responseTime }}</text>
          </view>
        </view>
      </view>

      <!-- 信誉徽章 -->
      <view class="badges-section" v-if="badges.length > 0">
        <text class="badges-title">获得徽章</text>
        <view class="badges-list">
          <view
            v-for="badge in badges"
            :key="badge.id"
            class="badge-item"
            :title="badge.name"
          >
            <text class="badge-emoji">{{ badge.emoji }}</text>
            <text class="badge-name">{{ badge.name }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
interface Badge {
  id: number
  name: string
  emoji: string
}

interface Props {
  score: number // 0-1000
  completedTasks: number
  positiveRate: number // 0-100
  responseTime: string
  badges?: Badge[]
}

const props = withDefaults(defineProps<Props>(), {
  badges: () => []
})

const getLevelText = (): string => {
  const { score } = props
  if (score >= 900) return '钻石'
  if (score >= 700) return '黄金'
  if (score >= 500) return '白银'
  if (score >= 300) return '青铜'
  return '新手'
}

const getLevelColor = (): string => {
  const { score } = props
  if (score >= 900) return 'linear-gradient(135deg, #A78BFA 0%, #8B5CF6 100%)'
  if (score >= 700) return 'linear-gradient(135deg, #FCD34D 0%, #F59E0B 100%)'
  if (score >= 500) return 'linear-gradient(135deg, #D1D5DB 0%, #9CA3AF 100%)'
  if (score >= 300) return 'linear-gradient(135deg, #FCA5A5 0%, #EF4444 100%)'
  return 'linear-gradient(135deg, #93C5FD 0%, #3B82F6 100%)'
}
</script>

<style lang="scss" scoped>
.user-reputation {
  background: #FFFFFF;
  border-radius: 24rpx;
  border-left: 4rpx solid #8B5CF6;
  box-shadow: 0 8rpx 24rpx rgba(23, 63, 128, 0.06);
  overflow: hidden;
}

.reputation-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 96rpx;
  padding: 0 32rpx;
  background: linear-gradient(135deg, #F5F3FF 0%, #EDE9FE 100%);
  border-bottom: 2rpx solid #DDD6FE;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.header-icon {
  font-size: 28rpx;
}

.header-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #5B21B6;
}

.level-badge {
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(139, 92, 246, 0.2);
}

.level-text {
  font-size: 24rpx;
  color: #FFFFFF;
  font-weight: 600;
}

.reputation-content {
  padding: 32rpx;
}

/* 分数区域 */
.score-section {
  margin-bottom: 32rpx;
}

.score-display {
  display: flex;
  align-items: baseline;
  justify-content: center;
  margin-bottom: 16rpx;
}

.score-number {
  font-size: 80rpx;
  font-weight: 700;
  background: linear-gradient(135deg, #8B5CF6 0%, #A78BFA 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.score-max {
  font-size: 32rpx;
  color: #9CA3AF;
  margin-left: 8rpx;
}

.score-progress {
  width: 100%;
}

.progress-bar {
  height: 12rpx;
  background: #F3F4F6;
  border-radius: 6rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #8B5CF6 0%, #A78BFA 100%);
  border-radius: 6rpx;
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 0 16rpx rgba(139, 92, 246, 0.4);
}

/* 指标区域 */
.metrics-section {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-bottom: 32rpx;
}

.metric-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx;
  background: #F9FAFB;
  border-radius: 16rpx;
  transition: all 0.3s ease;

  &:hover {
    background: #F3F4F6;
    transform: translateX(4rpx);
  }
}

.metric-icon-wrapper {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.metric-icon {
  font-size: 32rpx;
}

.metric-details {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.metric-label {
  font-size: 26rpx;
  color: #6B7280;
}

.metric-value {
  font-size: 28rpx;
  color: #1F2937;
  font-weight: 600;
}

/* 徽章区域 */
.badges-section {
  padding-top: 32rpx;
  border-top: 2rpx solid #F3F4F6;
}

.badges-title {
  font-size: 26rpx;
  color: #6B7280;
  font-weight: 500;
  margin-bottom: 16rpx;
  display: block;
}

.badges-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.badge-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 16rpx;
  background: linear-gradient(135deg, #F9FAFB 0%, #F3F4F6 100%);
  border-radius: 16rpx;
  min-width: 120rpx;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4rpx);
    box-shadow: 0 8rpx 16rpx rgba(139, 92, 246, 0.15);
  }
}

.badge-emoji {
  font-size: 48rpx;
}

.badge-name {
  font-size: 22rpx;
  color: #6B7280;
  text-align: center;
}

/* PC端适配 */
@media screen and (min-width: 768px) {
  .metrics-section {
    gap: 20rpx;
  }

  .metric-item {
    padding: 20rpx;

    &:hover {
      transform: translateX(8rpx);
    }
  }
}
</style>
