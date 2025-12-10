<template>
  <view class="detail-sidebar">
    <!-- 提问者信息卡 -->
    <CCard variant="elevated" class="sidebar-card asker-card">
      <view class="card-title">
        <text class="title-icon">👤</text>
        <text class="title-text">提问者</text>
      </view>

      <view class="asker-info">
        <image
          :src="question.askerAvatar || '/static/default-avatar.png'"
          class="asker-avatar"
          mode="aspectFill"
        />
        <view class="asker-details">
          <text class="asker-name">{{ question.askerNickname }}</text>
          <text class="asker-school" v-if="question.askerSchool">{{ question.askerSchool }}</text>
          <text class="asker-time">{{ formatTime(question.createdAt) }} 提问</text>
        </view>
      </view>
    </CCard>

    <!-- 操作按钮卡 -->
    <CCard variant="elevated" class="sidebar-card actions-card">
      <view class="card-title">
        <text class="title-icon">⚡</text>
        <text class="title-text">快捷操作</text>
      </view>

      <view class="actions-list">
        <!-- 关注按钮 -->
        <CButton
          :type="question.isFollowing ? 'primary' : 'secondary'"
          size="md"
          block
          @click="handleFollow"
        >
          <text class="btn-icon">{{ question.isFollowing ? '⭐' : '☆' }}</text>
          {{ question.isFollowing ? '已关注' : '关注问题' }}
        </CButton>

        <!-- 收藏按钮 -->
        <CButton
          type="ghost"
          size="md"
          block
          @click="handleCollect"
        >
          <text class="btn-icon">🔖</text>
          收藏
        </CButton>

        <!-- 分享按钮 -->
        <CButton
          type="ghost"
          size="md"
          block
          @click="handleShare"
        >
          <text class="btn-icon">📤</text>
          分享
        </CButton>

        <!-- 举报按钮（非本人） -->
        <CButton
          v-if="!isMyQuestion"
          type="ghost"
          size="md"
          block
          @click="handleReport"
        >
          <text class="btn-icon">🚨</text>
          举报
        </CButton>

        <!-- 删除按钮（仅本人） -->
        <CButton
          v-if="isMyQuestion"
          type="danger"
          size="md"
          block
          plain
          @click="handleDelete"
        >
          <text class="btn-icon">🗑️</text>
          删除问题
        </CButton>
      </view>
    </CCard>

    <!-- 问题统计卡 -->
    <CCard variant="elevated" class="sidebar-card stats-card">
      <view class="card-title">
        <text class="title-icon">📊</text>
        <text class="title-text">数据统计</text>
      </view>

      <view class="stats-list">
        <view class="stat-item">
          <text class="stat-icon">👁️</text>
          <text class="stat-label">浏览量</text>
          <text class="stat-value">{{ formatNumber(question.views) }}</text>
        </view>

        <view class="stat-item">
          <text class="stat-icon">💬</text>
          <text class="stat-label">回答数</text>
          <text class="stat-value">{{ question.answerCount }}</text>
        </view>

        <view v-if="question.bounty > 0" class="stat-item stat-item--highlight">
          <text class="stat-icon">🎁</text>
          <text class="stat-label">悬赏积分</text>
          <text class="stat-value">{{ question.bounty }}</text>
        </view>

        <view v-if="question.status === 1" class="stat-item stat-item--success">
          <text class="stat-icon">✅</text>
          <text class="stat-label">状态</text>
          <text class="stat-value">已解决</text>
        </view>
      </view>
    </CCard>

    <!-- 相关问题推荐卡 -->
    <CCard variant="elevated" class="sidebar-card related-card">
      <view class="card-title">
        <text class="title-icon">🔗</text>
        <text class="title-text">相关推荐</text>
      </view>

      <view class="related-list">
        <view
          v-for="item in relatedQuestions"
          :key="item.id"
          class="related-item"
          @click="handleRelatedClick(item.id)"
        >
          <text class="related-title">{{ item.title }}</text>
          <view class="related-meta">
            <text class="related-answers">{{ item.answerCount }} 回答</text>
            <text class="related-divider">·</text>
            <text class="related-views">{{ formatNumber(item.views) }} 浏览</text>
          </view>
        </view>

        <view v-if="relatedQuestions.length === 0" class="related-empty">
          <text class="empty-text">暂无相关推荐</text>
        </view>
      </view>
    </CCard>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import type { QuestionDetail } from '@/types/question'
import { CCard, CButton } from '@/components/ui'
import { formatNumber, formatTime } from '@/utils/formatters'

// Props
const props = defineProps<{
  question: QuestionDetail
  isMyQuestion: boolean
}>()

// Emits
const emit = defineEmits<{
  follow: []
  collect: []
  share: []
  report: []
  delete: []
}>()

// 相关问题列表（模拟数据，实际应从后端获取）
const relatedQuestions = ref([
  // TODO: 从后端获取相关问题
])

// 处理关注
const handleFollow = () => {
  emit('follow')
}

// 处理收藏
const handleCollect = () => {
  emit('collect')
}

// 处理分享
const handleShare = () => {
  emit('share')
}

// 处理举报
const handleReport = () => {
  emit('report')
}

// 处理删除
const handleDelete = () => {
  emit('delete')
}

// 处理相关问题点击
const handleRelatedClick = (questionId: number) => {
  uni.navigateTo({
    url: `/pages/question/detail?id=${questionId}`
  })
}
</script>

<style lang="scss" scoped>
.detail-sidebar {
  display: flex;
  flex-direction: column;
  gap: $sp-6;
}

// ===================================
// 侧栏卡片
// ===================================
.sidebar-card {
  // 卡片基础样式由 CCard 提供
}

// 卡片标题
.card-title {
  display: flex;
  align-items: center;
  gap: $sp-2;
  margin-bottom: $sp-6;
  padding-bottom: $sp-4;
  border-bottom: 2rpx solid $gray-100;

  .title-icon {
    font-size: $font-size-lg;
  }

  .title-text {
    font-size: $font-size-base + 2rpx;
    font-weight: $font-weight-bold;
    color: $gray-900;
  }
}

// ===================================
// 提问者信息卡
// ===================================
.asker-info {
  display: flex;
  gap: $sp-4;

  .asker-avatar {
    width: 96rpx;
    height: 96rpx;
    border-radius: $radius-full;
    background: $gray-200;
    flex-shrink: 0;
  }

  .asker-details {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: $sp-2;
    min-width: 0;

    .asker-name {
      font-size: $font-size-base + 2rpx;
      font-weight: $font-weight-semibold;
      color: $gray-900;
      @include text-ellipsis(1);
    }

    .asker-school {
      font-size: $font-size-sm;
      color: $gray-600;
      @include text-ellipsis(1);
    }

    .asker-time {
      font-size: $font-size-sm;
      color: $gray-500;
    }
  }
}

// ===================================
// 操作按钮卡
// ===================================
.actions-list {
  display: flex;
  flex-direction: column;
  gap: $sp-3;

  .btn-icon {
    margin-right: $sp-2;
  }
}

// ===================================
// 统计卡
// ===================================
.stats-list {
  display: flex;
  flex-direction: column;
  gap: $sp-4;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: $sp-3;
  padding: $sp-3;
  background: $gray-50;
  border-radius: $radius-base;

  .stat-icon {
    font-size: $font-size-lg;
    flex-shrink: 0;
  }

  .stat-label {
    flex: 1;
    font-size: $font-size-sm + 2rpx;
    color: $gray-600;
  }

  .stat-value {
    font-size: $font-size-base;
    font-weight: $font-weight-semibold;
    color: $gray-900;
  }

  // 高亮样式（悬赏积分）
  &--highlight {
    background: linear-gradient(135deg, $accent-50 0%, $accent-100 100%);

    .stat-value {
      color: $accent;
    }
  }

  // 成功样式（已解决）
  &--success {
    background: linear-gradient(135deg, $success-50 0%, $success-100 100%);

    .stat-value {
      color: $success;
    }
  }
}

// ===================================
// 相关推荐卡
// ===================================
.related-list {
  display: flex;
  flex-direction: column;
  gap: $sp-4;
}

.related-item {
  padding: $sp-4;
  border-radius: $radius-base;
  background: $gray-50;
  cursor: pointer;
  transition: all $duration-base;

  &:hover {
    background: $gray-100;
    transform: translateX(4rpx);
  }

  &:active {
    transform: scale(0.98);
  }

  .related-title {
    display: block;
    font-size: $font-size-sm + 2rpx;
    color: $gray-800;
    line-height: $line-height-relaxed;
    margin-bottom: $sp-2;
    @include text-ellipsis(2);
  }

  .related-meta {
    display: flex;
    align-items: center;
    gap: $sp-2;
    font-size: $font-size-xs + 2rpx;
    color: $gray-500;

    .related-divider {
      color: $gray-300;
    }
  }
}

.related-empty {
  padding: $sp-12 $sp-6;
  text-align: center;

  .empty-text {
    font-size: $font-size-sm;
    color: $gray-400;
  }
}

// ===================================
// 响应式适配
// ===================================
@include mobile {
  .detail-sidebar {
    // 移动端隐藏侧栏，或折叠到底部
    display: none;
  }
}
</style>
