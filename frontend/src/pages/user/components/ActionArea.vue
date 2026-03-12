<template>
  <view class="action-area">

    <!-- ── 发布内容（弱化版：浅橙卡片，非强渐变） ── -->
    <view class="publish-card" @click="handlePublish">
      <view class="publish-card__left">
        <view class="publish-card__icon-wrap">
          <Icon name="plus-circle" :size="20" class="publish-card__icon" />
        </view>
        <view class="publish-card__text">
          <text class="publish-card__title">发布内容</text>
          <text class="publish-card__sub">分享知识 · 贡献社区 · 赢积分</text>
        </view>
      </view>
      <view class="publish-card__badge">
        <text class="publish-card__badge-text">+10</text>
        <svg class="publish-card__arrow" viewBox="0 0 10 10" fill="none">
          <path d="M3.5 7.5L6.5 5L3.5 2.5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
        </svg>
      </view>
    </view>

    <!-- ── 成长统一卡片（查看成长 + 我的徽章） ── -->
    <view class="growth-card">

      <!-- 查看成长 -->
      <view class="growth-row" @click="handlePointsClick">
        <view class="growth-row__icon-wrap growth-row__icon-wrap--orange">
          <Icon name="trending-up" :size="17" class="growth-row__icon" />
        </view>
        <view class="growth-row__texts">
          <text class="growth-row__label">查看成长</text>
          <text class="growth-row__meta">
            {{ levelName ? `${levelName} · ` : '' }}{{ points.toLocaleString() }} 积分
          </text>
        </view>
        <view class="growth-row__right">
          <text v-if="level" class="growth-row__badge growth-row__badge--lv">Lv.{{ level }}</text>
          <svg class="growth-row__arrow" viewBox="0 0 10 10" fill="none">
            <path d="M3.5 7.5L6.5 5L3.5 2.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </view>
      </view>

      <!-- 分隔线 -->
      <view class="growth-divider" />

      <!-- 我的徽章 -->
      <view class="growth-row" @click="handleBadgesClick">
        <view class="growth-row__icon-wrap growth-row__icon-wrap--purple">
          <Icon name="award" :size="17" class="growth-row__icon" />
        </view>
        <view class="growth-row__texts">
          <text class="growth-row__label">我的徽章</text>
          <text class="growth-row__meta">荣誉 · 成就展示</text>
        </view>
        <view class="growth-row__right">
          <text v-if="badgeCount > 0" class="growth-row__badge growth-row__badge--badge">{{ badgeCount }}枚</text>
          <svg class="growth-row__arrow" viewBox="0 0 10 10" fill="none">
            <path d="M3.5 7.5L6.5 5L3.5 2.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </view>
      </view>

    </view>

  </view>
</template>

<script setup lang="ts">
import Icon from '@/components/icons/index.vue'

withDefaults(defineProps<{
  points?: number
  level?: number
  levelName?: string
  badgeCount?: number
}>(), {
  points: 0,
  level: 0,
  badgeCount: 0,
})

const emit = defineEmits<{
  pointsClick: []
  publish: []
}>()

const handlePublish = () => emit('publish')
const handlePointsClick = () => emit('pointsClick')
const handleBadgesClick = () =>
  uni.navigateTo({ url: '/pages/user/badges' })
</script>

<style lang="scss" scoped>
.action-area {
  padding: 16rpx 24rpx 20rpx;
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

// ── 发布卡片（弱化：浅橙 + 橙色描边）
.publish-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18rpx 20rpx 18rpx 16rpx;
  background: #FFF7ED;
  border: 1rpx solid rgba(249, 115, 22, 0.22);
  border-left: 4rpx solid #F97316;
  border-radius: 16rpx;
  cursor: pointer;
  transition: background 0.14s ease;

  &:active { background: #FFEDD5; }
}

.publish-card__left {
  display: flex;
  align-items: center;
  gap: 14rpx;
  min-width: 0;
}

.publish-card__icon-wrap {
  width: 48rpx;
  height: 48rpx;
  background: rgba(249, 115, 22, 0.12);
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.publish-card__icon { color: #F97316; }

.publish-card__text {
  display: flex;
  flex-direction: column;
  gap: 3rpx;
}

.publish-card__title {
  font-size: 28rpx;
  font-weight: 700;
  color: #EA580C;
  line-height: 1.2;
}

.publish-card__sub {
  font-size: 22rpx;
  color: #9CA3AF;
}

.publish-card__badge {
  display: flex;
  align-items: center;
  gap: 4rpx;
  flex-shrink: 0;
}

.publish-card__badge-text {
  font-size: 20rpx;
  font-weight: 700;
  color: #F97316;
  background: rgba(249, 115, 22, 0.1);
  padding: 2rpx 10rpx;
  border-radius: 20rpx;
}

.publish-card__arrow {
  width: 20rpx;
  height: 20rpx;
  color: #F97316;
  opacity: 0.6;
}

// ── 成长统一卡片
.growth-card {
  background: #fff;
  border: 1rpx solid #F3F4F6;
  border-radius: 16rpx;
  box-shadow: 0 1rpx 8rpx rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.growth-divider {
  height: 1rpx;
  background: #F3F4F6;
  margin: 0 20rpx;
}

// ── 成长行
.growth-row {
  display: flex;
  align-items: center;
  gap: 14rpx;
  padding: 18rpx 20rpx;
  cursor: pointer;
  transition: background 0.13s ease;

  &:active { background: #F9FAFB; }

  // #ifdef H5
  &:hover { background: #F9FAFB; }
  // #endif
}

.growth-row__icon-wrap {
  width: 48rpx;
  height: 48rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &--orange {
    background: #FFF7ED;
    .growth-row__icon { color: #F97316; }
  }

  &--purple {
    background: #EEF2FF;
    .growth-row__icon { color: #6366F1; }
  }
}

.growth-row__texts {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 3rpx;
  min-width: 0;
}

.growth-row__label {
  font-size: 27rpx;
  font-weight: 600;
  color: #1F2937;
  line-height: 1.2;
}

.growth-row__meta {
  font-size: 21rpx;
  color: #9CA3AF;
  line-height: 1.2;
}

.growth-row__right {
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex-shrink: 0;
}

.growth-row__badge {
  font-size: 20rpx;
  font-weight: 600;
  padding: 3rpx 12rpx;
  border-radius: 20rpx;

  &--lv {
    color: #F97316;
    background: rgba(249, 115, 22, 0.1);
  }

  &--badge {
    color: #6366F1;
    background: rgba(99, 102, 241, 0.1);
  }
}

.growth-row__arrow {
  width: 20rpx;
  height: 20rpx;
  color: #D1D5DB;
}
</style>
