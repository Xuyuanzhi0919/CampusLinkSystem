<template>
  <!-- ========== B层:行动区(居中容器内) ========== -->
  <view class="action-area">
    <!-- 🎯 主按钮:发布内容(独立成行,宽度60%) -->
    <view class="primary-action-container">
      <view class="primary-action-btn" @click="handlePublish">
        <Icon name="plus-circle" :size="20" class="action-icon" />
        <view class="action-content">
          <text class="action-title">发布内容</text>
          <text class="action-subtitle">分享你的知识 ｜ 获得积分</text>
        </view>
      </view>
    </view>

    <!-- 次按钮组(签到、查看成长) -->
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
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Icon from '@/components/icons/index.vue'

interface Props {
  isCheckedIn: boolean
}

const props = defineProps<Props>()

const emit = defineEmits<{
  checkIn: []
  pointsClick: []
  publish: []
}>()

// 签到按钮文本
const checkInButtonText = computed(() => {
  return props.isCheckedIn ? '已签到' : '每日签到'
})

/**
 * 处理签到
 */
const handleCheckIn = () => {
  if (!props.isCheckedIn) {
    emit('checkIn')
  }
}

/**
 * 🎯 处理发布内容(强转化按钮)
 */
const handlePublish = () => {
  emit('publish')
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

/* ========== B层:行动区(居中容器内) ========== */
.action-area {
  padding: 32rpx 24rpx 24rpx; // 🎯 增加上边距,拉开呼吸感
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

/* 🎯 主按钮容器(居中,宽度60%) */
.primary-action-container {
  display: flex;
  justify-content: center;
  margin-bottom: 8rpx; // 下方留足空白
}

/* 🎯 主按钮:发布内容(独立成行) */
.primary-action-btn {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 18rpx 32rpx; // 🎯 左右padding加大
  width: 60%; // 🎯 宽度限制为60%
  max-width: 480rpx;
  // 🎯 唯一橙色 CTA
  background: #F97316;
  border-radius: 24rpx; // 🎯 更大圆角
  cursor: pointer;
  box-shadow: 0 4rpx 12rpx rgba(249, 115, 22, 0.2);
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.97);
    background: #EA580C;
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
  font-weight: 600;
  color: $white;
  line-height: 1.2;
}

.action-subtitle {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 400;
}

/* 次按钮组(签到、查看成长) */
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
  padding: 14rpx; // 🎯 略微缩小高度
  background: $white;
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
</style>
