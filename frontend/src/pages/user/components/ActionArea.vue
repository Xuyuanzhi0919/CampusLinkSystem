<template>
  <view class="action-area">

    <!-- ── 主 CTA：发布内容（全宽） ── -->
    <view class="publish-btn" @click="handlePublish">
      <view class="publish-btn__left">
        <view class="publish-btn__icon-wrap">
          <Icon name="plus-circle" :size="22" class="publish-btn__icon" />
        </view>
        <view class="publish-btn__text">
          <text class="publish-btn__title">发布内容</text>
          <text class="publish-btn__sub">分享知识 · 贡献社区 · 赢积分</text>
        </view>
      </view>
      <svg class="publish-btn__arrow" viewBox="0 0 16 16" fill="none">
        <path d="M6 3l5 5-5 5" stroke="currentColor" stroke-width="2"
          stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    </view>

    <!-- ── 次级入口：两列并排 ── -->
    <view class="quick-grid">
      <!-- 查看成长 -->
      <view class="quick-item quick-item--growth" @click="handlePointsClick">
        <view class="quick-item__icon-wrap">
          <Icon name="trending-up" :size="18" class="quick-item__icon" />
        </view>
        <view class="quick-item__texts">
          <text class="quick-item__label">查看成长</text>
          <text class="quick-item__desc">积分 / 等级</text>
        </view>
        <svg class="quick-item__arrow" viewBox="0 0 10 10" fill="none">
          <path d="M3.5 7.5L6.5 5L3.5 2.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
        </svg>
      </view>

      <!-- 我的徽章 -->
      <view class="quick-item quick-item--badge" @click="handleBadgesClick">
        <view class="quick-item__icon-wrap">
          <Icon name="award" :size="18" class="quick-item__icon" />
        </view>
        <view class="quick-item__texts">
          <text class="quick-item__label">我的徽章</text>
          <text class="quick-item__desc">荣誉 / 成就</text>
        </view>
        <svg class="quick-item__arrow" viewBox="0 0 10 10" fill="none">
          <path d="M3.5 7.5L6.5 5L3.5 2.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
        </svg>
      </view>
    </view>

  </view>
</template>

<script setup lang="ts">
import Icon from '@/components/icons/index.vue'

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
  padding: 20rpx 24rpx 24rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

// ── 主按钮
.publish-btn {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 24rpx;
  background: linear-gradient(135deg, #F97316 0%, #EA580C 100%);
  border-radius: 20rpx;
  box-shadow: 0 6rpx 20rpx rgba(249, 115, 22, 0.32),
              inset 0 1rpx 0 rgba(255, 255, 255, 0.18);
  cursor: pointer;
  transition: transform 0.14s ease, box-shadow 0.14s ease;

  &:active {
    transform: scale(0.97);
    box-shadow: 0 2rpx 8rpx rgba(249, 115, 22, 0.22);
  }
}

.publish-btn__left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.publish-btn__icon-wrap {
  width: 56rpx;
  height: 56rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 14rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.publish-btn__icon {
  color: #fff;
}

.publish-btn__text {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.publish-btn__title {
  font-size: 30rpx;
  font-weight: 700;
  color: #fff;
  line-height: 1.2;
}

.publish-btn__sub {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.82);
  font-weight: 400;
}

.publish-btn__arrow {
  width: 32rpx;
  height: 32rpx;
  color: rgba(255, 255, 255, 0.8);
  flex-shrink: 0;
}

// ── 次级入口网格
.quick-grid {
  display: flex;
  gap: 12rpx;
}

.quick-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 18rpx 16rpx;
  border-radius: 16rpx;
  border: 1rpx solid transparent;
  cursor: pointer;
  transition: transform 0.13s ease;

  &:active { transform: scale(0.96); }
}

.quick-item--growth {
  background: linear-gradient(135deg, #FFF7ED 0%, #FFEDD5 100%);
  border-color: rgba(249, 115, 22, 0.16);

  .quick-item__icon-wrap { background: rgba(249, 115, 22, 0.12); }
  .quick-item__icon { color: #F97316; }
  .quick-item__label { color: #92400E; }
  .quick-item__desc { color: #C2410C; opacity: 0.65; }
  .quick-item__arrow { color: #F97316; opacity: 0.6; }
}

.quick-item--badge {
  background: linear-gradient(135deg, #EEF2FF 0%, #E0E7FF 100%);
  border-color: rgba(99, 102, 241, 0.16);

  .quick-item__icon-wrap { background: rgba(99, 102, 241, 0.12); }
  .quick-item__icon { color: #6366F1; }
  .quick-item__label { color: #3730A3; }
  .quick-item__desc { color: #4338CA; opacity: 0.65; }
  .quick-item__arrow { color: #6366F1; opacity: 0.6; }
}

.quick-item__icon-wrap {
  width: 46rpx;
  height: 46rpx;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.quick-item__texts {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 3rpx;
  min-width: 0;
}

.quick-item__label {
  font-size: 26rpx;
  font-weight: 700;
  line-height: 1.2;
}

.quick-item__desc {
  font-size: 20rpx;
  font-weight: 400;
  line-height: 1.2;
}

.quick-item__arrow {
  width: 20rpx;
  height: 20rpx;
  flex-shrink: 0;
}
</style>
