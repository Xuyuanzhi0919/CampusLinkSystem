<template>
  <CCard
    variant="outlined"
    :hoverable="true"
    class="answer-card"
    :class="{ 'answer-card--accepted': answer.isAccepted }"
  >
    <!-- 最佳答案标识 -->
    <BestAnswerBadge v-if="answer.isAccepted" />

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
        <text class="more-icon">⋯</text>
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
          :type="answer.isLiked ? 'primary' : 'ghost'"
          size="sm"
          :plain="!answer.isLiked"
          @click="handleLike"
        >
          <text class="action-icon">{{ answer.isLiked ? '👍' : '👍🏻' }}</text>
          <text class="action-label">{{ formatNumber(answer.likes) }}</text>
        </CButton>
      </view>

      <!-- 右侧：采纳/删除按钮 -->
      <view class="footer-right">
        <!-- 采纳按钮（仅提问者可见且问题未解决） -->
        <CButton
          v-if="canAccept && !answer.isAccepted"
          type="success"
          size="sm"
          @click="handleAccept"
        >
          <text class="action-icon">✅</text>
          采纳
        </CButton>

        <!-- 删除按钮（仅回答者本人可见） -->
        <CButton
          v-if="isMyAnswer"
          type="danger"
          size="sm"
          plain
          @click="handleDelete"
        >
          <text class="action-icon">🗑️</text>
          删除
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
  transition: all $duration-base $ease-out;

  // 最佳答案样式增强
  &--accepted {
    border-color: #FFD700 !important;
    box-shadow: 0 6rpx 20rpx rgba(#FFD700, 0.25) !important;
  }
}

// ===================================
// 回答头部（头像 + 昵称 + 更多按钮）
// ===================================
.answer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $sp-6;
  padding-bottom: $sp-4;
  border-bottom: 1rpx solid $gray-100;
}

// 回答者信息
.responder-info {
  display: flex;
  align-items: center;
  gap: $sp-4;
  flex: 1;
  min-width: 0;

  .responder-avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: $radius-full;
    background: $gray-200;
    flex-shrink: 0;
  }

  .responder-details {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: $sp-1;
    min-width: 0;

    .responder-name {
      font-size: $font-size-base;
      font-weight: $font-weight-semibold;
      color: $gray-800;
      @include text-ellipsis(1);
    }

    .responder-time {
      font-size: $font-size-sm;
      color: $gray-500;
    }
  }
}

// 更多操作按钮
.more-actions {
  flex-shrink: 0;
  width: 64rpx;
  height: 64rpx;
  @include flex-center;
  border-radius: $radius-full;
  cursor: pointer;
  transition: background $duration-base;

  &:hover {
    background: $gray-100;
  }

  &:active {
    background: $gray-200;
  }

  .more-icon {
    font-size: $font-size-2xl;
    color: $gray-600;
    font-weight: $font-weight-bold;
    letter-spacing: 2rpx;
  }
}

// ===================================
// 回答内容
// ===================================
.answer-content {
  font-size: $font-size-base;
  color: $gray-800;
  line-height: $line-height-loose;
  margin-bottom: $sp-6;
  white-space: pre-wrap;
  word-wrap: break-word;
}

// ===================================
// 图片列表
// ===================================
.answer-images {
  display: flex;
  flex-wrap: wrap;
  gap: $sp-4;
  margin-bottom: $sp-6;

  .answer-image {
    width: 200rpx;
    height: 200rpx;
    border-radius: $radius-base;
    background: $gray-100;
    cursor: pointer;
    transition: transform $duration-base, box-shadow $duration-base;

    &:hover {
      transform: scale(1.05);
      box-shadow: $shadow-md;
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
  padding-top: $sp-4;
  border-top: 1rpx solid $gray-100;
}

.footer-left,
.footer-right {
  display: flex;
  align-items: center;
  gap: $sp-3;
}

.action-icon {
  margin-right: $sp-1;
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
  background: rgba($gray-900, 0.5);
  z-index: $z-modal;
  display: flex;
  align-items: flex-end;
  animation: fadeIn $duration-slow $ease-out;
}

.menu-content {
  width: 100%;
  background: $white;
  border-radius: $radius-xl $radius-xl 0 0;
  padding: $sp-6 0;
  animation: slideUp $duration-slow $ease-out;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: $sp-6 $sp-12;
  transition: background $duration-base;
  cursor: pointer;

  &:active {
    background: $gray-50;
  }

  &--cancel {
    justify-content: center;
    border-top: 1rpx solid $gray-100;
    margin-top: $sp-4;
    padding-top: $sp-8;

    .menu-label {
      color: $gray-500;
    }
  }
}

.menu-icon {
  font-size: $font-size-2xl;
  margin-right: $sp-6;
}

.menu-label {
  font-size: $font-size-lg;
  color: $gray-800;
}

// ===================================
// 动画定义
// ===================================
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

// ===================================
// 响应式适配
// ===================================
@include mobile {
  .responder-info {
    .responder-avatar {
      width: 72rpx;
      height: 72rpx;
    }
  }

  .answer-images {
    .answer-image {
      width: 160rpx;
      height: 160rpx;
    }
  }
}
</style>
