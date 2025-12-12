<template>
  <view class="detail-sidebar">
    <!-- 提问者信息卡 - 强化版 -->
    <CCard variant="elevated" class="sidebar-card asker-card">
      <view class="asker-info">
        <image
          :src="question.askerAvatar || '/static/default-avatar.png'"
          class="asker-avatar"
          mode="aspectFill"
          @click="handleAskerClick"
        />
        <view class="asker-details">
          <view class="asker-name-wrapper">
            <text class="asker-name" @click="handleAskerClick">{{ question.askerNickname }}</text>
            <view class="asker-level">Lv.{{ askerLevel }}</view>
          </view>
          <text class="asker-school" v-if="question.askerSchool">
            <Icon name="map-pin" :size="12" class="school-icon" />
            {{ question.askerSchool }}
          </text>
          <view class="asker-stats">
            <text class="stat-badge">{{ askerQuestionCount }} 提问</text>
            <text class="stat-divider">·</text>
            <text class="stat-badge">{{ askerAnswerCount }} 回答</text>
          </view>
        </view>
      </view>

      <!-- 关注提问者按钮 -->
      <CButton
        v-if="!isMyQuestion"
        :type="isFollowingAsker ? 'secondary' : 'primary'"
        size="sm"
        block
        class="follow-asker-btn"
        @click="handleFollowAsker"
      >
        <Icon :name="isFollowingAsker ? 'user-check' : 'user-plus'" :size="16" class="btn-icon" />
        {{ isFollowingAsker ? '已关注' : '关注 TA' }}
      </CButton>
    </CCard>

    <!-- 问题信息卡 - 合并统计和操作 -->
    <CCard variant="elevated" class="sidebar-card info-card">
      <view class="card-title">
        <Icon name="info" :size="18" class="title-icon" />
        <text class="title-text">问题信息</text>
      </view>

      <!-- 核心统计数据 -->
      <view class="quick-stats">
        <view class="quick-stat-item">
          <text class="quick-stat-value">{{ formatNumber(question.views) }}</text>
          <text class="quick-stat-label">浏览</text>
        </view>
        <view class="quick-stat-divider" />
        <view class="quick-stat-item">
          <text class="quick-stat-value">{{ question.answerCount }}</text>
          <text class="quick-stat-label">回答</text>
        </view>
        <view v-if="question.bounty > 0" class="quick-stat-divider" />
        <view v-if="question.bounty > 0" class="quick-stat-item quick-stat-item--accent">
          <text class="quick-stat-value">{{ question.bounty }}</text>
          <text class="quick-stat-label">悬赏</text>
        </view>
      </view>

      <!-- 状态标签 -->
      <view v-if="question.status === 1" class="status-badge status-badge--solved">
        <Icon name="check-circle" :size="14" class="status-icon" />
        <text class="status-text">已解决</text>
      </view>

      <!-- 快捷操作按钮 -->
      <view class="action-buttons">
        <view
          class="action-btn"
          :class="{ 'action-btn--active': question.isFollowing }"
          @click="handleFollow"
        >
          <Icon name="star" :size="16" class="action-icon" />
          <text class="action-text">{{ question.isFollowing ? '已关注' : '关注' }}</text>
        </view>
        <view class="action-btn" @click="handleCollect">
          <Icon name="bookmark" :size="16" class="action-icon" />
          <text class="action-text">收藏</text>
        </view>
        <view class="action-btn" @click="handleShare">
          <Icon name="share-2" :size="16" class="action-icon" />
          <text class="action-text">分享</text>
        </view>
      </view>
    </CCard>

    <!-- 相关问题推荐卡 - 强化版 -->
    <CCard variant="elevated" class="sidebar-card related-card">
      <view class="card-title">
        <Icon name="layers" :size="18" class="title-icon" />
        <text class="title-text">相似问题</text>
        <text class="title-count" v-if="relatedQuestions.length > 0">{{ relatedQuestions.length }}</text>
      </view>

      <view class="related-list">
        <view
          v-for="item in relatedQuestions"
          :key="item.id"
          class="related-item"
          @click="handleRelatedClick(item.id)"
        >
          <view class="related-header">
            <text class="related-title">{{ item.title }}</text>
            <view v-if="item.status === 1" class="related-solved-badge">
              <Icon name="check" :size="12" />
            </view>
          </view>
          <view class="related-meta">
            <view class="related-meta-item">
              <Icon name="message-square" :size="12" class="meta-icon" />
              <text class="meta-text">{{ item.answerCount }}</text>
            </view>
            <view class="related-meta-item">
              <Icon name="eye" :size="12" class="meta-icon" />
              <text class="meta-text">{{ formatNumber(item.views) }}</text>
            </view>
            <view v-if="item.bounty > 0" class="related-meta-item related-meta-item--bounty">
              <Icon name="gift" :size="12" class="meta-icon" />
              <text class="meta-text">{{ item.bounty }}</text>
            </view>
          </view>
        </view>

        <view v-if="relatedQuestions.length === 0" class="related-empty">
          <Icon name="inbox" :size="32" class="empty-icon" />
          <text class="empty-text">暂无相似问题</text>
        </view>
      </view>
    </CCard>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import type { QuestionDetail } from '@/types/question'
import { CCard, CButton } from '@/components/ui'
import Icon from '@/components/icons/index.vue'
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
  askerClick: []
}>()

// 提问者等级（基于积分或问答数量计算）
const askerLevel = computed(() => {
  // TODO: 从后端获取或根据提问者积分计算
  return 5
})

// 提问者历史统计（模拟数据）
const askerQuestionCount = ref(23) // TODO: 从后端获取
const askerAnswerCount = ref(47)  // TODO: 从后端获取

// 是否关注提问者
const isFollowingAsker = ref(false) // TODO: 从后端获取

// 相关问题列表（模拟数据，实际应从后端获取）
const relatedQuestions = ref([
  // TODO: 从后端获取强相关问题（基于分类、标签、关键词等）
  // 示例数据结构：
  // {
  //   id: 1,
  //   title: "如何使用 Vue 3 的 Composition API？",
  //   answerCount: 12,
  //   views: 1234,
  //   bounty: 50,
  //   status: 1
  // }
])

// 处理点击提问者
const handleAskerClick = () => {
  emit('askerClick')
}

// 处理关注提问者
const handleFollowAsker = () => {
  isFollowingAsker.value = !isFollowingAsker.value
  uni.showToast({
    title: isFollowingAsker.value ? '已关注' : '已取消关注',
    icon: 'success'
  })
}

// 处理关注问题
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
  gap: 32rpx; // 16px spacing between cards
}

// ===================================
// 侧栏卡片通用样式
// ===================================
.sidebar-card {
  // 卡片基础样式由 CCard 提供，这里添加额外样式
  transition: all $duration-base;

  &:hover {
    transform: translateY(-2rpx);
  }
}

// 卡片标题 - 统一样式
.card-title {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 24rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid $gray-100;

  .title-icon {
    color: $primary;
    flex-shrink: 0;
  }

  .title-text {
    flex: 1;
    font-size: 28rpx; // 14px
    font-weight: 600;
    color: $gray-900;
  }

  .title-count {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 40rpx;
    height: 36rpx;
    padding: 0 12rpx;
    background: lighten($primary, 48%);
    color: $primary;
    font-size: 22rpx;
    font-weight: 600;
    border-radius: 18rpx;
  }
}

// ===================================
// 提问者信息卡 - 强化版
// ===================================
.asker-card {
  background: linear-gradient(135deg, lighten($primary, 50%) 0%, $white 50%);
}

.asker-info {
  display: flex;
  gap: 24rpx;
  margin-bottom: 24rpx;

  .asker-avatar {
    width: 96rpx; // 48px
    height: 96rpx;
    border-radius: $radius-full;
    background: $gray-200;
    flex-shrink: 0;
    border: 3rpx solid $white;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
    cursor: pointer;
    transition: all $duration-base;

    &:hover {
      transform: scale(1.05);
      box-shadow: 0 4rpx 12rpx rgba($primary, 0.2);
    }
  }

  .asker-details {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8rpx;
    min-width: 0;

    .asker-name-wrapper {
      display: flex;
      align-items: center;
      gap: 12rpx;

      .asker-name {
        font-size: 30rpx; // 15px
        font-weight: 600;
        color: $gray-900;
        @include text-ellipsis(1);
        cursor: pointer;
        transition: color $duration-base;

        &:hover {
          color: $primary;
        }
      }

      .asker-level {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        height: 32rpx;
        padding: 0 12rpx;
        background: linear-gradient(135deg, $accent 0%, lighten($accent, 10%) 100%);
        color: $white;
        font-size: 20rpx;
        font-weight: 600;
        border-radius: 16rpx;
        box-shadow: 0 2rpx 6rpx rgba($accent, 0.3);
      }
    }

    .asker-school {
      display: flex;
      align-items: center;
      gap: 6rpx;
      font-size: 24rpx; // 12px
      color: $gray-600;
      @include text-ellipsis(1);

      .school-icon {
        color: $gray-500;
        flex-shrink: 0;
      }
    }

    .asker-stats {
      display: flex;
      align-items: center;
      gap: 12rpx;
      font-size: 22rpx;

      .stat-badge {
        color: $gray-600;
        font-weight: 500;
      }

      .stat-divider {
        color: $gray-300;
      }
    }
  }
}

.follow-asker-btn {
  margin-top: 12rpx;

  .btn-icon {
    margin-right: 8rpx;
  }
}

// ===================================
// 问题信息卡 - 合并版
// ===================================
.info-card {
  .quick-stats {
    display: flex;
    align-items: center;
    justify-content: space-around;
    padding: 24rpx;
    background: linear-gradient(135deg, lighten($primary, 50%) 0%, lighten($primary, 52%) 100%);
    border-radius: 16rpx;
    margin-bottom: 20rpx;

    .quick-stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8rpx;

      .quick-stat-value {
        font-size: 40rpx; // 20px
        font-weight: 700;
        color: $gray-900;
      }

      .quick-stat-label {
        font-size: 22rpx; // 11px
        color: $gray-600;
        font-weight: 500;
      }

      &--accent {
        .quick-stat-value {
          color: $accent;
        }

        .quick-stat-label {
          color: $accent;
        }
      }
    }

    .quick-stat-divider {
      width: 1rpx;
      height: 56rpx;
      background: $gray-200;
    }
  }

  .status-badge {
    display: inline-flex;
    align-items: center;
    gap: 8rpx;
    padding: 12rpx 20rpx;
    border-radius: 20rpx;
    margin-bottom: 20rpx;

    &--solved {
      background: linear-gradient(135deg, lighten($success, 50%) 0%, lighten($success, 45%) 100%);
      color: darken($success, 10%);
      border: 1rpx solid $success;

      .status-icon {
        color: $success;
      }

      .status-text {
        font-size: 24rpx;
        font-weight: 600;
      }
    }
  }

  .action-buttons {
    display: flex;
    gap: 16rpx;
    justify-content: space-between;

    .action-btn {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8rpx;
      padding: 16rpx 8rpx;
      background: $gray-50;
      border: 1rpx solid $gray-200;
      border-radius: 12rpx;
      cursor: pointer;
      transition: all $duration-base;

      .action-icon {
        color: $gray-500;
        transition: color $duration-base;
      }

      .action-text {
        font-size: 22rpx;
        color: $gray-700;
        font-weight: 500;
        transition: color $duration-base;
      }

      &:hover {
        background: lighten($primary, 48%);
        border-color: $primary;

        .action-icon {
          color: $primary;
          transform: scale(1.1);
        }

        .action-text {
          color: $primary;
        }
      }

      &--active {
        background: lighten($primary, 48%);
        border-color: $primary;

        .action-icon {
          color: $primary;
        }

        .action-text {
          color: $primary;
          font-weight: 600;
        }
      }
    }
  }
}

// ===================================
// 相关问题推荐卡 - 强化版
// ===================================
.related-card {
  .related-list {
    display: flex;
    flex-direction: column;
    gap: 20rpx;
  }

  .related-item {
    padding: 24rpx;
    border-radius: 16rpx;
    background: $white;
    border: 1rpx solid $gray-200;
    cursor: pointer;
    transition: all $duration-base;

    &:hover {
      background: lighten($primary, 50%);
      border-color: $primary;
      transform: translateX(4rpx);
      box-shadow: 0 2rpx 8rpx rgba($primary, 0.08);
    }

    &:active {
      transform: scale(0.98);
    }

    .related-header {
      display: flex;
      align-items: flex-start;
      gap: 12rpx;
      margin-bottom: 16rpx;

      .related-title {
        flex: 1;
        font-size: 26rpx; // 13px
        color: $gray-800;
        line-height: 1.5;
        font-weight: 500;
        @include text-ellipsis(2);
        min-width: 0;
      }

      .related-solved-badge {
        flex-shrink: 0;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        width: 32rpx;
        height: 32rpx;
        background: linear-gradient(135deg, $success 0%, lighten($success, 10%) 100%);
        color: $white;
        border-radius: 50%;
        box-shadow: 0 2rpx 6rpx rgba($success, 0.3);
      }
    }

    .related-meta {
      display: flex;
      align-items: center;
      gap: 20rpx;
      flex-wrap: wrap;

      .related-meta-item {
        display: flex;
        align-items: center;
        gap: 6rpx;

        .meta-icon {
          color: $gray-400;
          flex-shrink: 0;
        }

        .meta-text {
          font-size: 22rpx; // 11px
          color: $gray-600;
          font-weight: 500;
        }

        &--bounty {
          .meta-icon {
            color: $accent;
          }

          .meta-text {
            color: $accent;
            font-weight: 600;
          }
        }
      }
    }
  }

  .related-empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16rpx;
    padding: 60rpx 32rpx;
    text-align: center;

    .empty-icon {
      color: $gray-300;
    }

    .empty-text {
      font-size: 24rpx;
      color: $gray-400;
    }
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
