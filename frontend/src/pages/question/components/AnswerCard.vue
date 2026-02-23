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
        <view class="avatar-wrap">
          <image
            v-if="answer.responderAvatar"
            :src="answer.responderAvatar"
            class="responder-avatar"
            mode="aspectFill"
          />
          <view v-else class="avatar-fallback">
            <text class="avatar-letter">{{ answer.responderName?.[0] || '?' }}</text>
          </view>
        </view>
        <view class="responder-details">
          <text class="responder-name">{{ answer.responderName }}</text>
          <text class="responder-time">{{ formatTime(answer.createdAt) }}</text>
        </view>
      </view>

      <!-- 右侧：更多操作按钮 -->
      <view class="more-actions" @click="showMoreMenu">
        <Icon name="more-vertical" :size="20" :stroke-width="1.5" class="more-icon" />
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
      <!-- 左侧：点赞 + 回复按钮 -->
      <view class="footer-left">
        <view class="like-button" :class="{ 'like-button--active': answer.isLiked }" @click="handleLike">
          <Icon name="thumbs-up" :size="18" class="like-icon" />
          <text class="like-count">{{ formatNumber(answer.likes) }}</text>
        </view>
        <view class="reply-button" @click="handleReply">
          <Icon name="message-circle" :size="18" class="reply-icon" />
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
          <Icon name="check" :size="16" :stroke-width="1.5" class="action-icon" />
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
          <Icon name="trash-2" :size="16" :stroke-width="1.5" class="action-icon" />
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
          <Icon name="file-text" :size="20" class="menu-icon" />
          <text class="menu-label">复制内容</text>
        </view>

        <!-- 分享回答 -->
        <view class="menu-item" @click="handleShare">
          <Icon name="share" :size="20" class="menu-icon" />
          <text class="menu-label">分享回答</text>
          <Icon name="chevron-down" :size="16" class="menu-arrow" />
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
          <Icon name="x" :size="20" class="menu-icon" />
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
  reply: [answerId: number, responderName: string]
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

// 回复
const handleReply = () => {
  emit('reply', props.answer.answerId, props.answer.responderName)
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
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04) !important;
  border-radius: 24rpx !important; // 12px rounded corners
  padding: 48rpx !important; // 24px internal padding
  border: 1rpx solid $gray-100;

  &:hover {
    box-shadow: 0 6rpx 16rpx rgba(0, 0, 0, 0.08) !important;
    transform: translateY(-2rpx);
  }

  // 最佳答案样式 - 清新现代风
  &--accepted {
    background-color: #FFFBEB !important;
    border-color: #F59E0B !important;
    box-shadow: 0 6rpx 18rpx rgba(245, 158, 11, 0.1) !important;
    position: relative;
    overflow: hidden;

    &::before {
      content: "";
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 8rpx; // 加粗到 4px，视觉更突出
      background: linear-gradient(90deg, #F59E0B 0%, #FBBF24 50%, #FDE68A 100%);
    }

    .answer-content {
      color: $gray-900;
    }

    &:hover {
      box-shadow: 0 8rpx 24rpx rgba(245, 158, 11, 0.15) !important;
    }
  }

  // 移动端：卡片间用间隙区分（背景色透出）
  @include mobile {
    margin-bottom: 16rpx !important;
    border-radius: 0 !important;
    border-left: none !important;
    border-right: none !important;
    border-top: none !important;
    border-bottom: 1rpx solid $gray-100 !important;
    box-shadow: none !important;
    padding: 32rpx 28rpx !important;
    margin-left: 0 !important;
    margin-right: 0 !important;

    &:hover {
      transform: none;
      box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04) !important;
    }

    // 最佳答案移动端：保留顶部金色条+黄色背景
    &--accepted {
      border-top: 6rpx solid #F59E0B !important;
      border-bottom: 1rpx solid rgba(#F59E0B, 0.2) !important;

      &::before {
        display: none; // 移动端用 border-top 替代伪元素
      }
    }
  }
}

.accepted-badge-wrapper {
  display: inline-flex;
  margin-bottom: $sp-4;
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
  margin-bottom: $sp-5;
}

// 头像容器
.avatar-wrap {
  flex-shrink: 0;
  width: 72rpx;
  height: 72rpx;
  border-radius: $radius-full;
  overflow: hidden;

  .responder-avatar {
    width: 100%;
    height: 100%;
    border-radius: $radius-full;
    display: block;
  }

  // 无头像时的彩色字母占位
  .avatar-fallback {
    width: 100%;
    height: 100%;
    border-radius: $radius-full;
    background: linear-gradient(135deg, #DBEAFE, #93C5FD);
    display: flex;
    align-items: center;
    justify-content: center;

    .avatar-letter {
      font-size: 28rpx;
      font-weight: 700;
      color: #1D4ED8;
      line-height: 1;
    }
  }
}

// 回答者信息
.responder-info {
  display: flex;
  align-items: center;
  gap: 20rpx;
  flex: 1;
  min-width: 0;

  .responder-details {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 6rpx;
    min-width: 0;

    .responder-name {
      font-size: 28rpx;
      font-weight: 600;
      color: $gray-900;
      @include text-ellipsis(1);
    }

    .responder-time {
      font-size: 22rpx;
      color: $gray-400;
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
    color: $gray-600; // P1: 统一图标颜色
    flex-shrink: 0;
    transition: color 0.2s;

    &:hover {
      color: $gray-900;
    }
  }
}

// ===================================
// 回答内容
// ===================================
.answer-content {
  font-size: $font-size-base;
  color: $gray-700;
  line-height: 1.7;
  margin-bottom: $sp-5;
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
  margin-top: $sp-1;
  padding-top: $sp-4;
  border-top: 1rpx solid $gray-100;

  .answer-card--accepted & {
    border-top-color: rgba(#F59E0B, 0.15);
  }
}

.footer-left {
  display: flex;
  align-items: center;
  gap: $sp-3;

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

// 回复按钮（与点赞按钮完全一致的结构）
.reply-button {
  display: inline-flex;
  align-items: center;
  gap: 12rpx;
  padding: 12rpx 24rpx;
  border-radius: 40rpx;
  background: $gray-50;
  border: 2rpx solid $gray-200;
  cursor: pointer;
  transition: all $duration-base;

  .reply-icon {
    color: $gray-500;
    transition: all $duration-base;
  }

  &:hover {
    background: lighten($primary, 48%);
    border-color: lighten($primary, 35%);

    .reply-icon {
      color: $primary;
      transform: scale(1.1);
    }
  }

  &:active {
    transform: scale(0.98);
  }
}

.footer-right {
  display: flex;
  align-items: center;
  gap: $sp-3;
}

.action-icon {
  margin-right: $sp-1;
  flex-shrink: 0;
  // 颜色由父按钮控制（success按钮绿色，danger按钮红色）
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
  background: rgba(0, 0, 0, 0.6); // 加深到 60%
  z-index: $z-modal;
  display: flex;
  align-items: flex-end;
  animation: fadeIn 0.25s ease-out;
  backdrop-filter: blur(4rpx);
}

.menu-content {
  width: 100%;
  background: $white;
  border-radius: 24rpx 24rpx 0 0; // 减小到 12px
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
    display: flex;
    align-items: center;
    justify-content: center;
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

  // 右箭头（用于分享等操作）
  .menu-arrow {
    color: $gray-400;
    margin-left: auto;
    flex-shrink: 0;
    transition: color $duration-base;
  }

  &:hover .menu-arrow {
    color: $gray-600;
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
    margin-top: 16rpx; // 增加到 8px，更独立
    padding: 32rpx; // 稍微加大内边距

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
    // padding/margin 已在 .answer-card 的 @include mobile 块中处理
  }

  .responder-info {
    gap: 16rpx;
  }

  .avatar-wrap {
    width: 64rpx;
    height: 64rpx;

    .avatar-letter {
      font-size: 26rpx !important;
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
      font-size: 26rpx !important;
    }
  }

  .reply-button {
    padding: 10rpx 14rpx !important; // 无文字，缩小水平 padding
  }
}
</style>

