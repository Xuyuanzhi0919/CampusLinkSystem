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
      <!-- 左侧：点赞按钮 -->
      <view class="footer-left">
        <CButton
          :type="answer.isLiked ? 'primary' : 'info'"
          size="sm"
          :plain="!answer.isLiked"
          @click="handleLike"
          custom-style="border: none; background: transparent; padding-left: 0;"
        >
          <text class="action-icon">{{ answer.isLiked ? '👍' : '👍🏻' }}</text>
          <text class="action-label" :class="{ 'text-primary': answer.isLiked }">{{ formatNumber(answer.likes) }}</text>
        </CButton>
      </view>

      <!-- 右侧：采纳/删除按钮 -->
      <view class="footer-right">
        <!-- ?????????????????? -->
        <CButton
          v-if="canAccept && !answer.isAccepted"
          type="success"
          size="sm"
          plain
          class="accept-btn"
          @click="handleAccept"
        >
          <ClIcon name="icon-check" size="xs" class="action-icon" />
          <text class="action-label">??</text>
        </CButton>

        <!-- ?????????????? -->
        <CButton
          v-if="isMyAnswer"
          type="ghost"
          size="sm"
          class="ghost-danger"
          @click="handleDelete"
        >
          <ClIcon name="icon-trash-2" size="xs" class="action-icon" />
          <text class="action-label">??</text>
        </CButton>
      </view>
    </view>

    <!-- 更多菜单弹出层 -->
    <view v-if="showMenu" class="menu-overlay" @click="hideMoreMenu">
      <view class="menu-content" @click.stop>
        <!-- 复制内容 -->
        <view class="menu-item" @click="handleCopy">
          <text class="menu-icon">📋</text>
          <text class="menu-label">复制内容</text>
        </view>

        <!-- 分享回答 -->
        <view class="menu-item" @click="handleShare">
          <text class="menu-icon">📤</text>
          <text class="menu-label">分享回答</text>
        </view>

        <!-- 举报回答（非本人） -->
        <view v-if="!isMyAnswer" class="menu-item" @click="handleReport">
          <text class="menu-icon">🚨</text>
          <text class="menu-label">举报</text>
        </view>

        <!-- 取消 -->
        <view class="menu-item menu-item--cancel" @click="hideMoreMenu">
          <text class="menu-label">取消</text>
        </view>
      </view>
    </view>
  </CCard>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { AnswerItem } from '@/types/question'
import { useUserStore } from '@/stores/user'
import { CCard, CButton } from '@/components/ui'
import { ClIcon } from '@/components/cl'
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
// 回答卡片容器
// ===================================
.answer-card {
  margin-bottom: $sp-6;
  background: $bg-surface;
  transition: all $duration-base $ease-out;
  // border: 1rpx solid $gray-200; // Removed to use CCard's border

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

// 回答者信息
.responder-info {
  display: flex;
  align-items: center;
  gap: $sp-3;
  flex: 1;
  min-width: 0;

  .responder-avatar {
    width: 72rpx; // 稍微调小一点，更精致
    height: 72rpx;
    border-radius: $radius-full;
    background: $gray-100;
    flex-shrink: 0;
    border: 1rpx solid $gray-200;
  }

  .responder-details {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 2rpx;
    min-width: 0;

    .responder-name {
      font-size: $font-size-base;
      font-weight: $font-weight-semibold;
      color: $gray-800;
      @include text-ellipsis(1);
    }

    .responder-time {
      font-size: $font-size-xs; // 12px
      color: $gray-500;
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
  // 点赞按钮自定义样式
  .action-label {
    margin-left: $sp-1;
    font-size: $font-size-sm;
    color: $gray-500;
    font-weight: $font-weight-medium;

    &.text-primary {
      color: $primary;
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
// 更多菜单弹出层
// ===================================
.menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: $z-modal;
  display: flex;
  align-items: flex-end;
  animation: fadeIn 0.2s ease-out;
}

.menu-content {
  width: 100%;
  background: $white;
  border-radius: $radius-xl $radius-xl 0 0;
  padding: $sp-4 0;
  animation: slideUp 0.2s ease-out;
  padding-bottom: env(safe-area-inset-bottom);
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 24rpx 48rpx;
  transition: background $duration-base;
  cursor: pointer;

  &:active {
    background: $gray-50;
  }

  &--cancel {
    justify-content: center;
    border-top: 12rpx solid $gray-50;
    margin-top: $sp-2;
    padding: 32rpx;

    .menu-label {
      color: $gray-500;
      font-weight: 500;
    }
  }
}

.menu-icon {
  font-size: 40rpx;
  margin-right: $sp-4;
  width: 40rpx;
  text-align: center;
}

.menu-label {
  font-size: 32rpx;
  color: $gray-800;
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
  .responder-info {
    .responder-avatar {
      width: 64rpx;
      height: 64rpx;
    }
  }

  .answer-images {
    .answer-image {
      width: 31%; // 3列布局
      height: 200rpx;
    }
  }
}
</style>

