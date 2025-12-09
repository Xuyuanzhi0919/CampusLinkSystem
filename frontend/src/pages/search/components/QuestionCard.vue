<template>
  <view class="question-card" @click="$emit('click')">
    <!-- 状态标签 -->
    <view class="card-status" :class="statusClass">
      <text>{{ statusText }}</text>
    </view>

    <!-- 标题 -->
    <text class="card-title">{{ question.title }}</text>

    <!-- 内容预览 -->
    <text v-if="question.content" class="card-content">{{ question.content }}</text>

    <!-- 标签 -->
    <view v-if="question.tags && question.tags.length > 0" class="card-tags">
      <text v-for="tag in question.tags.slice(0, 3)" :key="tag" class="tag-item">
        {{ tag }}
      </text>
    </view>

    <!-- 底部信息 -->
    <view class="card-footer">
      <view class="footer-left">
        <text class="meta-item">👁️ {{ question.views }}</text>
        <text class="meta-item">💬 {{ question.answerCount }}</text>
      </view>
      <view v-if="question.bounty > 0" class="bounty-tag">
        <text>🎁 {{ question.bounty }} 积分</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { QuestionItem } from '@/types/question'
import { computed } from 'vue'

interface Props {
  question: QuestionItem
}

const props = defineProps<Props>()

defineEmits<{
  click: []
}>()

// 状态相关
const isSolved = computed(() => props.question.status === 1 || props.question.isSolved)
const statusClass = computed(() => isSolved.value ? 'status--solved' : 'status--open')
const statusText = computed(() => isSolved.value ? '已解决' : '待解答')
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.question-card {
  position: relative;
  padding: 28rpx;
  background: #FFFFFF;
  border-radius: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
  transition: all 0.2s ease;

  &:active {
    transform: scale(0.98);
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  }
}

.card-status {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  padding: 6rpx 16rpx;
  border-radius: 16rpx;
  font-size: 22rpx;
  font-weight: $font-weight-medium;

  &.status--solved {
    background: #DCFCE7;
    color: #16A34A;
  }

  &.status--open {
    background: #FEF3C7;
    color: #D97706;
  }
}

.card-title {
  display: block;
  font-size: 30rpx;
  font-weight: $font-weight-medium;
  color: $color-text-primary;
  line-height: 1.5;
  padding-right: 100rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-content {
  display: block;
  font-size: 26rpx;
  color: $color-text-tertiary;
  line-height: 1.5;
  margin-top: 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 16rpx;
}

.tag-item {
  padding: 6rpx 16rpx;
  background: $color-bg-hover;
  border-radius: 8rpx;
  font-size: 22rpx;
  color: $color-text-secondary;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 20rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid $color-divider;
}

.footer-left {
  display: flex;
  gap: 24rpx;
}

.meta-item {
  font-size: 24rpx;
  color: $color-text-quaternary;
}

.bounty-tag {
  padding: 6rpx 16rpx;
  background: linear-gradient(135deg, #FEF3C7, #FDE68A);
  border-radius: 16rpx;

  text {
    font-size: 22rpx;
    font-weight: $font-weight-medium;
    color: #D97706;
  }
}
</style>
