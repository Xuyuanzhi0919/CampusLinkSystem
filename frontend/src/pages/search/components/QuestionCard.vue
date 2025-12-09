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

/* 优化6：移动端卡片紧凑化 */
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

  @media (max-width: 768px) {
    padding: 20rpx;
    border-radius: 16rpx;
  }
}

/* 优化5：统一标签尺寸和灰度，视觉更轻 */
.card-status {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  font-size: 20rpx;
  font-weight: $font-weight-medium;
  border: 1rpx solid;

  &.status--solved {
    background: rgba(22, 163, 74, 0.08);
    color: #16A34A;
    border-color: rgba(22, 163, 74, 0.15);
  }

  &.status--open {
    background: rgba(217, 119, 6, 0.08);
    color: #D97706;
    border-color: rgba(217, 119, 6, 0.15);
  }

  @media (max-width: 768px) {
    top: 16rpx;
    right: 16rpx;
    padding: 6rpx 12rpx;
    font-size: 20rpx;
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

  @media (max-width: 768px) {
    font-size: 28rpx;
    line-height: 1.4;
    padding-right: 90rpx;
  }
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

  @media (max-width: 768px) {
    font-size: 24rpx;
    margin-top: 8rpx;
  }
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 16rpx;

  @media (max-width: 768px) {
    gap: 8rpx;
    margin-top: 12rpx;
  }
}

.tag-item {
  padding: 6rpx 16rpx;
  background: $color-bg-hover;
  border-radius: 8rpx;
  font-size: 22rpx;
  color: $color-text-secondary;

  @media (max-width: 768px) {
    padding: 5rpx 12rpx;
    font-size: 20rpx;
  }
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 20rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid $color-divider;

  @media (max-width: 768px) {
    margin-top: 16rpx;
    padding-top: 12rpx;
  }
}

.footer-left {
  display: flex;
  gap: 24rpx;

  @media (max-width: 768px) {
    gap: 16rpx;
  }
}

.meta-item {
  font-size: 24rpx;
  color: $color-text-quaternary;

  @media (max-width: 768px) {
    font-size: 22rpx;
  }
}

/* 优化5：悬赏标签也改为浅底深字 */
.bounty-tag {
  padding: 8rpx 16rpx;
  background: rgba(255, 107, 53, 0.08);
  border-radius: 20rpx;
  border: 1rpx solid rgba(255, 107, 53, 0.15);

  text {
    font-size: 20rpx;
    font-weight: $font-weight-medium;
    color: #FF6B35;
  }

  @media (max-width: 768px) {
    padding: 6rpx 12rpx;
  }
}
</style>
