<template>
  <!-- ========== 快速操作区(Quick Actions) ========== -->
  <view class="quick-actions">
    <scroll-view scroll-x class="actions-scroll" show-scrollbar="{{false}}">
      <view class="action-item" @click="$emit('publishResource')">
        <view class="action-icon">
          <Icon name="file-plus" :size="20" :stroke-width="1.5" />
        </view>
        <text class="action-label">发布资源</text>
      </view>

      <view class="action-item accent" @click="$emit('askQuestion')">
        <view class="action-icon">
          <Icon name="help-circle" :size="20" />
        </view>
        <text class="action-label">提个问题</text>
      </view>

      <view class="action-item" @click="$emit('publishTask')">
        <view class="action-icon">
          <Icon name="briefcase" :size="20" />
        </view>
        <text class="action-label">发布任务</text>
      </view>

      <view class="action-item" @click="$emit('joinActivity')">
        <view class="action-icon">
          <Icon name="calendar" :size="20" />
        </view>
        <text class="action-label">报名活动</text>
      </view>

      <view class="action-item" @click="$emit('goToMall')">
        <view class="action-icon">
          <Icon name="shopping-bag" :size="20" />
        </view>
        <text class="action-label">积分商城</text>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import Icon from '@/components/icons/index.vue'

defineEmits<{
  publishResource: []
  askQuestion: []
  publishTask: []
  joinActivity: []
  goToMall: []
}>()
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

/* ========== Quick Actions ========== */
.quick-actions {
  margin-top: -40rpx; // 🎯 向上覆盖 Hero 区域,制造层次感
  padding: 0 32rpx; // 0 $sp-8
  margin-bottom: 32rpx; // $sp-8
}

.actions-scroll {
  white-space: nowrap;
  width: 100%;

  // 隐藏滚动条
  &::-webkit-scrollbar {
    display: none;
  }
}

.action-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  margin-right: 24rpx; // $sp-6

  &:first-child {
    margin-left: 0;
  }

  &:last-child {
    margin-right: 0;
  }

  .action-icon {
    width: 112rpx;
    height: 112rpx;
    border-radius: 16rpx; // $radius-card
    background: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.06); // $shadow-card
    color: #2563EB; // $primary
    transition: all 0.3s ease;

    &:active {
      transform: scale(0.95);
    }
  }

  .action-label {
    margin-top: 12rpx;
    font-size: 22rpx;
    color: #64748B; // $text-secondary
  }

  // 🎯 强调操作(提问)
  &.accent {
    .action-icon {
      background: linear-gradient(135deg, #F97316 0%, #FF8A5B 100%); // accent gradient
      color: #fff;
      box-shadow: 0 8rpx 24rpx rgba(249, 115, 22, 0.3);
    }

    .action-label {
      color: #F97316; // accent
      font-weight: 600;
    }
  }
}
</style>
