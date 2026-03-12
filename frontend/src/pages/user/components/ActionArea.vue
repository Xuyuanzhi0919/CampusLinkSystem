<template>
  <!-- 发布内容入口：单一职责，不展示数据（数据由下方 AchievementSection 负责） -->
  <view class="publish-entry" @click="handlePublish">

    <!-- 左：图标 + 文字 -->
    <view class="publish-entry__left">
      <view class="publish-entry__icon">
        <Icon name="edit-3" :size="17" class="publish-entry__icon-svg" />
      </view>
      <view class="publish-entry__text">
        <text class="publish-entry__title">发布内容</text>
        <text class="publish-entry__sub">分享知识 · 贡献社区</text>
      </view>
    </view>

    <!-- 右：积分激励 + 箭头 -->
    <view class="publish-entry__right">
      <text class="publish-entry__reward">+10 积分</text>
      <svg class="publish-entry__arrow" viewBox="0 0 10 10" fill="none">
        <path d="M3.5 7.5L6.5 5L3.5 2.5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
      </svg>
    </view>

  </view>
</template>

<script setup lang="ts">
import Icon from '@/components/icons/index.vue'

const emit = defineEmits<{
  publish: []
}>()

const handlePublish = () => emit('publish')
</script>

<style lang="scss" scoped>
.publish-entry {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18rpx 20rpx;
  cursor: pointer;
  transition: background 0.13s;

  &:active { background: #FFF7ED; }

  // #ifdef H5
  &:hover { background: #FFF7ED; }
  // #endif
}

.publish-entry__left {
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.publish-entry__icon {
  width: 48rpx;
  height: 48rpx;
  background: rgba(249, 115, 22, 0.1);
  border-radius: 13rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.publish-entry__icon-svg { color: #F97316; }

.publish-entry__text {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.publish-entry__title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1F2937;
  line-height: 1.2;
}

.publish-entry__sub {
  font-size: 21rpx;
  color: #9CA3AF;
  line-height: 1;
}

.publish-entry__right {
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex-shrink: 0;
}

.publish-entry__reward {
  font-size: 21rpx;
  font-weight: 600;
  color: #F97316;
  background: rgba(249, 115, 22, 0.08);
  padding: 4rpx 14rpx;
  border-radius: 20rpx;
}

.publish-entry__arrow {
  width: 20rpx;
  height: 20rpx;
  color: #CBD5E1;
}
</style>
