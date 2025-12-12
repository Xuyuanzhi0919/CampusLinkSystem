<template>
  <CCard
    variant="outlined"
    :hoverable="true"
    class="answer-card"
    :class="{ 'answer-card--accepted': answer.isAccepted }"
  >
    <!-- 最佳答案标识 (放在最顶部) -->
    <view v-if="answer.isAccepted" class="accepted-badge-wrapper">
      <BestAnswerBadge class="answer-badge" />
    </view>

    <!-- 回答者信息 + 更多操作 -->
    <view class="answer-header">
      <!-- 左侧：头像 + 昵称 + 时间 -->
      <view class="responder-info">
        <image
          :src="answer.responderAvatar || '/static/default-avatar.png'"
          class="responder-avatar"
          mode="aspectFill"
        />
        <view class="responder-details">
          <text class="responder-name">{{ answer.responderName }}</text>
          <text class="responder-time">{{ formatTime(answer.createdAt) }}</text>
        </view>
      </view>

      <!-- 右侧：更多操作按钮 -->
      <view class="more-actions" @click="showMoreMenu">
        <ClIcon name="icon-more" size="sm" class="more-icon" color="#9CA3AF" />
      </view>
    </view>

    <!-- 回答内容 -->
    <view class="answer-content">{{ answer.content }}</view>

    <!-- 图片列表 -->
    <view v-if="answer.images && answer.images.length > 0" class="answer-images">
      <image
        v-for="(img, index) in answer.images"
        :key="index"
        :src="img"
        class="answer-image"
        mode="aspectFill"
        @click="handlePreviewImage(index)"
      />
    </view>

    <!-- 底部操作栏 -->
    <view class="answer-footer">
      <!-- 左侧：点赞按钮 - 强化版 -->
      <view class="footer-left">
        <view class="like-button" :class="{ 'like-button--active': answer.isLiked }" @click="handleLike">
          <Icon :name="answer.isLiked ? 'thumbs-up' : 'thumbs-up'" :size="18" class="like-icon" />
          <text class="like-count">{{ formatNumber(answer.likes) }}</text>
        </view>
      </view>

      <!-- 右侧：采纳/删除按钮 -->
      <view class="footer-right">
        <!-- 采纳按钮（仅提问者可见且问题未解决） -->
        <CButton
          v-if="canAccept && !answer.isAccepted"
          type="success"
          size="sm"
          plain
          class="accept-btn"
          @click="handleAccept"
        >
          <ClIcon name="icon-check" size="xs" class="action-icon" />
          <text class="action-label">采纳</text>
        </CButton>

        <!-- 删除按钮（仅回答者本人可见） -->
        <CButton
          v-if="isMyAnswer"
          type="ghost"
          size="sm"
          class="ghost-danger"
          @click="handleDelete"
        >
          <ClIcon name="icon-trash-2" size="xs" class="action-icon" />
          <text class="action-label">删除</text>
        </CButton>
      </view>
    </view>

  </CCard>

  <!-- 更多菜单弹出层 - 优化版（移到 CCard 外部） -->
  <view v-if="showMenu" class="menu-overlay" @click="hideMoreMenu">
    <view class="menu-content" @click.stop>
      <!-- 常规操作组 -->
      <view class="menu-group">
        <!-- 复制内容 -->
        <view class="menu-item" @click="handleCopy">
          <Icon name="copy" :size="20" class="menu-icon" />
          <text class="menu-label">复制内容</text>
        </view>

        <!-- 分享回答 -->
        <view class="menu-item" @click="handleShare">
          <Icon name="share-2" :size="20" class="menu-icon" />
          <text class="menu-label">分享回答</text>
        </view>
      </view>

      <!-- 危险操作组（非本人） -->
      <view v-if="!isMyAnswer" class="menu-group menu-group--danger">
        <view class="menu-item menu-item--danger" @click="handleReport">
          <Icon name="flag" :size="20" class="menu-icon" />
          <text class="menu-label">举报</text>
        </view>
      </view>

      <!-- 取消按钮 -->
      <view class="menu-group">
        <view class="menu-item menu-item--cancel" @click="hideMoreMenu">
          <text class="menu-label">取消</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { AnswerItem } from '@/types/question'
import { useUserStore } from '@/stores/user'
import { CCard, CButton } from '@/components/ui'
import { ClIcon } from '@/components/cl'
import Icon from '@/components/icons/index.vue'
import BestAnswerBadge from './BestAnswerBadge.vue'
import { formatNumber, formatTime } from '@/utils/formatters'

// Props
interface Props {
  answer: AnswerItem
  canAccept: boolean // 是否可以采纳（仅提问者且问题未解决）
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  like: [answerId: number]
  accept: [answerId: number]
  delete: [answerId: number]
}>()

// Store
const userStore = useUserStore()

// 更多菜单状态
const showMenu = ref(false)

// 是否是我的回答
const isMyAnswer = computed(() => {
  return userStore.userInfo?.userId === props.answer.responderId
})

// 点赞
const handleLike = () => {
  emit('like', props.answer.answerId)
}

// 采纳
const handleAccept = () => {
  emit('accept', props.answer.answerId)
}

// 删除
const handleDelete = () => {
  emit('delete', props.answer.answerId)
}

// 预览图片
const handlePreviewImage = (index: number) => {
  if (!props.answer.images) return

  uni.previewImage({
    current: index,
    urls: props.answer.images
  })
}

// 显示更多菜单
const showMoreMenu = () => {
  showMenu.value = true
}

// 隐藏更多菜单
const hideMoreMenu = () => {
  showMenu.value = false
}

// 复制内容
const handleCopy = () => {
  hideMoreMenu()
  uni.setClipboardData({
    data: props.answer.content,
    success: () => {
      uni.showToast({
        title: '已复制到剪贴板',
        icon: 'success'
      })
    }
  })
}

// 分享回答
const handleShare = () => {
  hideMoreMenu()
  uni.showToast({
    title: '分享功能开发中',
    icon: 'none'
  })
}

// 举报回答
const handleReport = () => {
  hideMoreMenu()
  uni.showModal({
    title: '举报',
    content: '确定要举报这个回答吗？',
    success: (res) => {
      if (res.confirm) {
        uni.showToast({
          title: '举报成功，我们会尽快处理',
          icon: 'success'
        })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
// ===================================
// 回答卡片容器 - 强化版（与首页卡片统一风格）
// ===================================
.answer-card {
  margin-bottom: 32rpx; // 16-20px spacing between cards
  background: $bg-surface;
  transition: all $duration-base $ease-out;

  // 统一卡片阴影和圆角
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04) !important; // Enhanced shadow
  border-radius: 24rpx !important; // 12px rounded corners
  padding: 48rpx !important; // 24px internal padding
  border: 1rpx solid $gray-100; // Subtle border

  &:hover {
    box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.08) !important;
    transform: translateY(-2rpx);
  }

  // 最佳答案样式 - 清新现代风
  &--accepted {
    background-color: #FFFDF5 !important;
    border-color: #FACC15 !important;
    box-shadow: 0 6rpx 18rpx rgba(0, 0, 0, 0.06) !important;
    position: relative;
    overflow: hidden;

    &::before {
      content: "";
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 6rpx;
      background: linear-gradient(90deg, #FACC15 0%, #FDE68A 100%);
    }

    .answer-content {
      color: $gray-900;
    }

    &:hover {
      box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08) !important;
    }
  }
}

.accepted-badge-wrapper {
  display: inline-flex;
  margin-bottom: $sp-3;
}

.answer-badge {
  display: inline-flex;
}

// ===================================
// 回答头部（头像 + 昵称 + 更多按钮）
// ===================================
.answer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $sp-4;
  padding-bottom: 0; // 移除内边距，更紧凑
}

// 回答者信息 - 强化版（更突出的头像和昵称）
.responder-info {
  display: flex;
  align-items: center;
  gap: 24rpx; // Increased gap from 12rpx to 24rpx
  flex: 1;
  min-width: 0;

  .responder-avatar {
    width: 64rpx; // 32px - Enhanced from 36rpx
    height: 64rpx;
    border-radius: $radius-full;
    background: $gray-100;
    flex-shrink: 0;
    border: 2rpx solid $gray-200; // Slightly thicker border
    transition: all $duration-base;

    &:hover {
      border-color: $primary;
      transform: scale(1.05);
    }
  }

  .responder-details {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 4rpx; // Increased from 2rpx
    min-width: 0;

    .responder-name {
      font-size: 30rpx; // 15-16px - Enhanced from 28rpx
      font-weight: 600; // Bolder weight
      color: $gray-900; // Darker color for more prominence
      @include text-ellipsis(1);
      transition: color $duration-base;

      &:hover {
        color: $primary;
      }
    }

    .responder-time {
      font-size: 24rpx; // 12px
      color: $gray-500;
      font-weight: 400;
    }
  }
}

// 更多操作按钮
.more-actions {
  flex-shrink: 0;
  width: 56rpx;
  height: 56rpx;
  @include flex-center;
  border-radius: $radius-full;
  cursor: pointer;
  color: $gray-400;
  transition: all $duration-base;

  &:hover {
    background: $gray-100;
    color: $gray-600;
  }

  .more-icon {
    font-size: $font-size-xl;
    font-weight: bold;
    line-height: 1;
  }
}

// ===================================
// 回答内容
// ===================================
.answer-content {
  font-size: $font-size-base;
  color: $gray-700; // 稍微柔和一点的黑色
  line-height: 1.6;
  margin-bottom: $sp-4;
  white-space: pre-wrap;
  word-wrap: break-word;
}

// ===================================
// 图片列表
// ===================================
.answer-images {
  display: flex;
  flex-wrap: wrap;
  gap: $sp-3;
  margin-bottom: $sp-4;

  .answer-image {
    width: 200rpx;
    height: 200rpx;
    border-radius: $radius-sm;
    background: $gray-100;
    cursor: pointer;
    transition: transform $duration-base;

    &:hover {
      transform: scale(1.02);
    }
  }
}

// ===================================
// 底部操作栏
// ===================================
.answer-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: $sp-2;
  padding-top: $sp-3;
  border-top: 1rpx solid $gray-100;

  // 如果是最佳答案，底部分割线颜色微调
  .answer-card--accepted & {
    border-top-color: rgba(#FACC15, 0.2);
  }
}

.footer-left {
  // 点赞按钮 - 强化版（品牌色突出）
  .like-button {
    display: inline-flex;
    align-items: center;
    gap: 12rpx;
    padding: 12rpx 24rpx;
    border-radius: 40rpx;
    background: $gray-50;
    border: 2rpx solid $gray-200;
    cursor: pointer;
    transition: all $duration-base;

    .like-icon {
      color: $gray-500;
      transition: all $duration-base;
    }

    .like-count {
      font-size: 28rpx; // 14px
      font-weight: 500;
      color: $gray-700;
      transition: all $duration-base;
    }

    &:hover {
      background: lighten($primary, 48%);
      border-color: lighten($primary, 35%);

      .like-icon {
        color: $primary;
        transform: scale(1.1);
      }

      .like-count {
        color: $primary;
      }
    }

    &--active {
      background: lighten($primary, 48%);
      border-color: $primary;

      .like-icon {
        color: $primary;
      }

      .like-count {
        color: $primary;
        font-weight: 600;
      }

      &:hover {
        background: lighten($primary, 45%);
        transform: scale(1.02);
      }
    }
  }
}

.footer-right {
  display: flex;
  align-items: center;
  gap: $sp-3;
}

.action-icon {
  margin-right: $sp-1;
  font-size: $font-size-base;
}

.accept-btn {
  color: #166534;
  background: #ecfdf3;
  border: 2rpx solid #86efac;
  transition: background $duration-base, border-color $duration-base, color $duration-base;
}

.accept-btn:hover {
  background: #d1fae5;
  border-color: #4ade80;
}

.ghost-danger {
  color: #b91c1c;
  background: transparent;
  border: 2rpx solid #fca5a5;
  transition: background $duration-base, border-color $duration-base, color $duration-base;
}

.ghost-danger:hover {
  background: rgba(#f87171, 0.08);
  border-color: #f87171;
}

.action-label {
  font-size: $font-size-sm;
}

// ===================================
// 更多菜单弹出层 - 优化版
// ===================================
.menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: $z-modal;
  display: flex;
  align-items: flex-end;
  animation: fadeIn 0.25s ease-out;
  backdrop-filter: blur(4rpx);
}

.menu-content {
  width: 100%;
  background: $white;
  border-radius: 32rpx 32rpx 0 0;
  padding: 16rpx 0;
  padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  animation: slideUp 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 -8rpx 32rpx rgba(0, 0, 0, 0.12);
}

// 菜单分组
.menu-group {
  padding: 8rpx 0;

  &:not(:last-child) {
    border-bottom: 1rpx solid $gray-100;
  }

  // 危险操作组
  &--danger {
    background: lighten($error, 52%);
  }
}

// 菜单项
.menu-item {
  display: flex;
  align-items: center;
  padding: 28rpx 40rpx;
  transition: all $duration-base;
  cursor: pointer;
  min-height: 96rpx;

  &:active {
    background: $gray-100;
    transform: scale(0.98);
  }

  // 菜单图标
  .menu-icon {
    width: 40rpx;
    height: 40rpx;
    margin-right: 24rpx;
    color: $gray-700;
    flex-shrink: 0;
  }

  // 菜单文字
  .menu-label {
    flex: 1;
    font-size: 32rpx;
    color: $gray-900;
    font-weight: 500;
    letter-spacing: 0.5rpx;
  }

  // 危险操作样式
  &--danger {
    .menu-icon {
      color: $error;
    }

    .menu-label {
      color: $error;
    }

    &:active {
      background: lighten($error, 48%);
    }
  }

  // 取消按钮样式
  &--cancel {
    justify-content: center;
    margin-top: 8rpx;

    .menu-label {
      color: $gray-600;
      font-weight: 600;
    }

    &:active {
      background: $gray-50;
    }
  }
}

// ===================================
// 动画定义
// ===================================
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

// ===================================
// 响应式适配
// ===================================
@include mobile {
  .answer-card {
    padding: 32rpx !important; // Reduce padding on mobile
    margin-bottom: 24rpx; // Slightly less spacing on mobile
  }

  .responder-info {
    gap: 16rpx; // Reduce gap on mobile

    .responder-avatar {
      width: 56rpx; // 28px on mobile
      height: 56rpx;
    }

    .responder-details {
      .responder-name {
        font-size: 28rpx; // 14px on mobile
      }

      .responder-time {
        font-size: 22rpx; // 11px on mobile
      }
    }
  }

  .answer-images {
    .answer-image {
      width: 31%; // 3列布局
      height: 200rpx;
    }
  }

  .like-button {
    padding: 10rpx 20rpx !important;
    gap: 8rpx !important;

    .like-count {
      font-size: 26rpx !important; // 13px on mobile
    }
  }
}
</style>

