<template>
  <view class="action-area">

    <!-- ── 数据概览：等级 / 积分 / 徽章（三列直接展示） ── -->
    <view class="stats-row">

      <view class="stat-item" @click="handlePointsClick">
        <view class="stat-icon stat-icon--lv">
          <svg viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M9 1L11.09 6.26L17 7.27L13 11.14L13.9 17L9 14.37L4.1 17L5 11.14L1 7.27L6.91 6.26L9 1Z"
              stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </view>
        <text class="stat-val">Lv.{{ level || 1 }}</text>
        <text class="stat-label">{{ levelName || '校园新星' }}</text>
      </view>

      <view class="stat-sep" />

      <view class="stat-item" @click="handlePointsClick">
        <view class="stat-icon stat-icon--pts">
          <svg viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="9" cy="9" r="7.5" stroke="currentColor" stroke-width="1.6"/>
            <path d="M9 5.5v7M6.5 7.5C6.5 6.4 7.6 5.5 9 5.5s2.5.9 2.5 2c0 2-5 2-5 4 0 1.1 1.1 2 2.5 2s2.5-.9 2.5-2"
              stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
          </svg>
        </view>
        <text class="stat-val">{{ points.toLocaleString() }}</text>
        <text class="stat-label">积分</text>
      </view>

      <view class="stat-sep" />

      <view class="stat-item" @click="handleBadgesClick">
        <view class="stat-icon stat-icon--badge">
          <svg viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M11.6 9.67l1.14 6.4a.38.38 0 0 1-.61.35l-2.69-2.01a.75.75 0 0 0-.9 0L5.86 16.42a.38.38 0 0 1-.61-.35l1.14-6.4"
              stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="9" cy="6.5" r="4.5" stroke="currentColor" stroke-width="1.6"/>
          </svg>
        </view>
        <text class="stat-val">{{ badgeCount }}</text>
        <text class="stat-label">枚徽章</text>
      </view>

    </view>

    <!-- ── 分隔线 ── -->
    <view class="area-divider" />

    <!-- ── 发布入口（底部次级操作行） ── -->
    <view class="publish-row" @click="handlePublish">
      <view class="publish-row__left">
        <view class="publish-row__icon">
          <Icon name="edit-3" :size="15" class="publish-row__icon-svg" />
        </view>
        <view class="publish-row__texts">
          <text class="publish-row__title">发布内容</text>
          <text class="publish-row__sub">分享知识 · 贡献社区</text>
        </view>
      </view>
      <view class="publish-row__right">
        <text class="publish-row__reward">+10 积分</text>
        <svg class="publish-row__arrow" viewBox="0 0 10 10" fill="none">
          <path d="M3.5 7.5L6.5 5L3.5 2.5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
        </svg>
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
  level: 1,
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
  padding: 20rpx 0 0;
}

// ── 三列数据行
.stats-row {
  display: flex;
  align-items: stretch;
  padding: 4rpx 0 20rpx;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 7rpx;
  padding: 8rpx 0;
  cursor: pointer;
  transition: opacity 0.15s;

  &:active { opacity: 0.65; }
}

.stat-sep {
  width: 1rpx;
  background: #F0F0F0;
  align-self: stretch;
  margin: 8rpx 0;
}

// 图标圆点
.stat-icon {
  width: 52rpx;
  height: 52rpx;
  border-radius: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;

  svg {
    width: 20rpx;
    height: 20rpx;
  }

  &--lv {
    background: #FFF7ED;
    svg { color: #F97316; }
  }

  &--pts {
    background: #FEFCE8;
    svg { color: #CA8A04; }
  }

  &--badge {
    background: #F5F3FF;
    svg { color: #7C3AED; }
  }
}

// 数值
.stat-val {
  font-size: 30rpx;
  font-weight: 800;
  color: #111827;
  line-height: 1;
  letter-spacing: -0.5rpx;
}

// 标签
.stat-label {
  font-size: 20rpx;
  color: #9CA3AF;
  font-weight: 400;
  line-height: 1;
  white-space: nowrap;
}

// ── 分隔线
.area-divider {
  height: 1rpx;
  background: #F3F4F6;
  margin: 0 20rpx;
}

// ── 发布行
.publish-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18rpx 20rpx 20rpx;
  cursor: pointer;
  transition: background 0.13s;
  border-radius: 0 0 16rpx 16rpx;

  &:active { background: #FFF7ED; }
}

.publish-row__left {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.publish-row__icon {
  width: 44rpx;
  height: 44rpx;
  background: rgba(249, 115, 22, 0.1);
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.publish-row__icon-svg { color: #F97316; }

.publish-row__texts {
  display: flex;
  flex-direction: column;
  gap: 3rpx;
}

.publish-row__title {
  font-size: 26rpx;
  font-weight: 600;
  color: #374151;
  line-height: 1.2;
}

.publish-row__sub {
  font-size: 20rpx;
  color: #9CA3AF;
}

.publish-row__right {
  display: flex;
  align-items: center;
  gap: 6rpx;
  flex-shrink: 0;
}

.publish-row__reward {
  font-size: 22rpx;
  font-weight: 600;
  color: #F97316;
  background: rgba(249, 115, 22, 0.08);
  padding: 3rpx 12rpx;
  border-radius: 20rpx;
}

.publish-row__arrow {
  width: 20rpx;
  height: 20rpx;
  color: #D1D5DB;
}
</style>
