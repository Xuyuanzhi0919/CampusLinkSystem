<template>
  <view class="task-comments">
    <view class="comments-header">
      <view class="header-icon">💬</view>
      <text class="header-title">任务交流</text>
      <text class="comments-count">({{ comments.length }})</text>
    </view>

    <view class="comments-content">
      <!-- 权限提示 -->
      <view class="permission-notice" v-if="!canComment">
        <text class="notice-icon">🔒</text>
        <text class="notice-text">{{ permissionTip }}</text>
      </view>

      <!-- 评论列表 -->
      <view class="comments-list" v-if="comments.length > 0">
        <view
          v-for="(comment, index) in comments"
          :key="comment.commentId || index"
          class="comment-item"
        >
          <!-- 楼层标识 -->
          <view class="floor-badge">
            <text class="floor-text">{{ comments.length - index }}楼</text>
          </view>

          <!-- 评论内容 -->
          <view class="comment-main">
            <image
              class="commenter-avatar"
              :src="comment.avatar || '/static/default-avatar.png'"
              mode="aspectFill"
            />
            <view class="comment-body">
              <view class="comment-header-row">
                <view class="commenter-info">
                  <text class="commenter-name">{{ comment.nickname }}</text>
                  <view class="author-badge" v-if="comment.userId === publisherId">
                    <text class="badge-text">楼主</text>
                  </view>
                </view>
                <text class="comment-time">{{ formatTime(comment.createdAt) }}</text>
              </view>

              <text class="comment-text">{{ comment.content }}</text>

              <!-- 评论操作 -->
              <view class="comment-actions">
                <view
                  class="action-item"
                  :class="{ 'action-active': comment.isLiked }"
                  @click="handleLike(comment)"
                >
                  <text class="action-icon">{{ comment.isLiked ? '❤️' : '🤍' }}</text>
                  <text class="action-text">{{ comment.likeCount || 0 }}</text>
                </view>

                <view class="action-item" @click="handleReply(comment)" v-if="canComment">
                  <text class="action-icon">💬</text>
                  <text class="action-text">回复</text>
                </view>

                <view class="action-item" @click="handleDelete(comment)" v-if="canDelete(comment)">
                  <text class="action-icon">🗑️</text>
                  <text class="action-text">删除</text>
                </view>

                <view class="action-item" @click="handleReport(comment)">
                  <text class="action-icon">⚠️</text>
                  <text class="action-text">举报</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="empty-comments" v-else>
        <text class="empty-icon">📭</text>
        <text class="empty-text">暂无评论，抢先发表你的看法吧</text>
      </view>

      <!-- 评论输入框 -->
      <view class="comment-input-section" v-if="canComment">
        <view class="input-wrapper">
          <image
            class="current-user-avatar"
            :src="currentUserAvatar || '/static/default-avatar.png'"
            mode="aspectFill"
          />
          <input
            class="comment-input"
            v-model="commentContent"
            :placeholder="replyTarget ? `回复 @${replyTarget.nickname}` : '友善交流，理性评论...'"
            :focus="inputFocused"
            @focus="inputFocused = true"
            @blur="inputFocused = false"
          />
        </view>
        <view class="input-actions">
          <view class="cancel-btn" v-if="replyTarget" @click="handleCancelReply">
            <text class="btn-text">取消回复</text>
          </view>
          <view
            class="submit-btn"
            :class="{ 'btn-disabled': !commentContent.trim() }"
            @click="handleSubmit"
          >
            <text class="btn-text">{{ isSubmitting ? '发送中...' : '发送' }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface Comment {
  commentId: number | string
  userId: number
  nickname: string
  avatar?: string
  content: string
  createdAt: string
  likeCount?: number
  isLiked?: boolean
}

interface Props {
  comments: Comment[]
  publisherId: number
  currentUserId?: number
  currentUserAvatar?: string
  canComment?: boolean
  permissionTip?: string
}

const props = withDefaults(defineProps<Props>(), {
  canComment: true,
  permissionTip: '接单后可发表评论'
})

const emit = defineEmits<{
  submit: [content: string, replyTo?: Comment]
  like: [comment: Comment]
  unlike: [comment: Comment]
  delete: [comment: Comment]
  report: [comment: Comment]
}>()

const commentContent = ref('')
const replyTarget = ref<Comment | null>(null)
const inputFocused = ref(false)
const isSubmitting = ref(false)

// 判断是否可以删除评论
const canDelete = (comment: Comment): boolean => {
  return props.currentUserId === comment.userId || props.currentUserId === props.publisherId
}

// 格式化时间
const formatTime = (time: string): string => {
  if (!time) return ''

  const now = new Date()
  const commentDate = new Date(time)
  const diff = now.getTime() - commentDate.getTime()

  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`

  return commentDate.toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 点赞/取消点赞
const handleLike = (comment: Comment) => {
  if (comment.isLiked) {
    emit('unlike', comment)
  } else {
    emit('like', comment)
  }
}

// 回复评论
const handleReply = (comment: Comment) => {
  replyTarget.value = comment
  inputFocused.value = true
}

// 取消回复
const handleCancelReply = () => {
  replyTarget.value = null
}

// 删除评论
const handleDelete = (comment: Comment) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这条评论吗？',
    success: (res) => {
      if (res.confirm) {
        emit('delete', comment)
      }
    }
  })
}

// 举报评论
const handleReport = (comment: Comment) => {
  emit('report', comment)
}

// 提交评论
const handleSubmit = async () => {
  const content = commentContent.value.trim()
  if (!content) return

  if (isSubmitting.value) return

  isSubmitting.value = true

  try {
    emit('submit', content, replyTarget.value || undefined)

    // 清空输入
    commentContent.value = ''
    replyTarget.value = null
    inputFocused.value = false
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.task-comments {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(23, 63, 128, 0.06);
  border-left: 8rpx solid #10B981;
}

.comments-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.header-icon {
  width: 56rpx;
  height: 56rpx;
  background: #10B981;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
}

.header-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2937;
}

.comments-count {
  font-size: 28rpx;
  color: #9CA3AF;
}

.comments-content {
  padding-left: 72rpx;
}

/* 权限提示 */
.permission-notice {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 24rpx;
  background: #FEF3C7;
  border-radius: 12rpx;
  margin-bottom: 24rpx;
}

.notice-icon {
  font-size: 32rpx;
}

.notice-text {
  font-size: 26rpx;
  color: #92400E;
}

/* 评论列表 */
.comments-list {
  display: flex;
  flex-direction: column;
  gap: 32rpx;
  margin-bottom: 32rpx;
}

.comment-item {
  position: relative;
}

.floor-badge {
  position: absolute;
  top: 0;
  left: -72rpx;
  width: 56rpx;
  height: 32rpx;
  background: #E5E7EB;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.floor-text {
  font-size: 20rpx;
  color: #6B7280;
  font-weight: 500;
}

.comment-main {
  display: flex;
  gap: 20rpx;
}

.commenter-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: #F3F4F6;
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.comment-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.commenter-info {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.commenter-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #1F2937;
}

.author-badge {
  padding: 4rpx 12rpx;
  background: #FEE2E2;
  border-radius: 8rpx;
}

.badge-text {
  font-size: 20rpx;
  color: #EF4444;
  font-weight: 500;
}

.comment-time {
  font-size: 24rpx;
  color: #9CA3AF;
}

.comment-text {
  font-size: 28rpx;
  color: #4B5563;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: 32rpx;
  margin-top: 8rpx;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  cursor: pointer;
  transition: all 0.2s;

  &:active {
    transform: scale(0.95);
  }

  &.action-active {
    .action-icon {
      transform: scale(1.2);
    }
  }
}

.action-icon {
  font-size: 28rpx;
  transition: transform 0.2s;
}

.action-text {
  font-size: 24rpx;
  color: #6B7280;
}

/* 空状态 */
.empty-comments {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  padding: 64rpx 0;
}

.empty-icon {
  font-size: 80rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #9CA3AF;
}

/* 评论输入 */
.comment-input-section {
  padding-top: 32rpx;
  border-top: 2rpx solid #F3F4F6;
}

.input-wrapper {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-bottom: 16rpx;
}

.current-user-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: #F3F4F6;
  flex-shrink: 0;
}

.comment-input {
  flex: 1;
  padding: 20rpx 24rpx;
  background: #F9FAFB;
  border-radius: 16rpx;
  font-size: 28rpx;
  color: #1F2937;
  border: 2rpx solid transparent;
  transition: all 0.2s;

  &:focus {
    background: #FFFFFF;
    border-color: #3B82F6;
  }
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
}

.cancel-btn,
.submit-btn {
  padding: 16rpx 32rpx;
  border-radius: 12rpx;
  font-size: 26rpx;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;

  &:active {
    transform: scale(0.98);
  }
}

.cancel-btn {
  background: #F3F4F6;
  color: #6B7280;

  &:active {
    background: #E5E7EB;
  }
}

.submit-btn {
  background: #3B82F6;
  color: #FFFFFF;

  &:active {
    background: #2563EB;
  }

  &.btn-disabled {
    background: #9CA3AF;
    opacity: 0.5;
    cursor: not-allowed;
  }
}

/* PC端适配 */
@media screen and (min-width: 768px) {
  .task-comments {
    padding: 48rpx;
  }

  .comments-content {
    padding-left: 88rpx;
  }

  .comment-main {
    gap: 24rpx;
  }

  .commenter-avatar {
    width: 88rpx;
    height: 88rpx;
  }
}
</style>
