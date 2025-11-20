<template>
  <view class="comment-list">
    <!-- 评论输入区 -->
    <view class="comment-input-section">
      <view class="input-wrapper">
        <textarea
          v-model="newComment"
          class="comment-textarea"
          placeholder="发表你的看法..."
          :maxlength="500"
          auto-height
        />
        <view class="input-footer">
          <text class="char-count">{{ newComment.length }}/500</text>
          <button class="submit-btn" :disabled="!newComment.trim()" @click="handleSubmitComment">
            发表评论
          </button>
        </view>
      </view>
    </view>

    <!-- 评论列表 -->
    <view class="comments-wrapper">
      <view v-if="loading" class="loading-container">
        <text class="loading-text">加载中...</text>
      </view>

      <view v-else-if="comments.length === 0" class="empty-state">
        <text class="empty-text">暂无评论，快来发表第一条评论吧~</text>
      </view>

      <view v-else class="comments-container">
        <view
          v-for="comment in comments"
          :key="comment.commentId"
          class="comment-item"
        >
          <!-- Comment header -->
          <view class="comment-header">
            <image
              :src="comment.userAvatar || defaultAvatar"
              class="user-avatar"
              mode="aspectFill"
            />
            <view class="user-info">
              <text class="user-name">{{ comment.userName }}</text>
              <text class="comment-time">{{ formatTime(comment.createdAt) }}</text>
            </view>
            <view
              v-if="canDelete(comment)"
              class="delete-btn"
              @click="handleDeleteComment(comment.commentId)"
            >
              <text class="delete-icon">×</text>
            </view>
          </view>

          <!-- Comment content -->
          <view class="comment-content">
            <text class="content-text">{{ comment.content }}</text>
          </view>

          <!-- Comment actions -->
          <view class="comment-actions">
            <view class="action-btn" @click="handleReply(comment)">
              <text class="action-icon">↵</text>
              <text class="action-text">回复 {{ comment.replyCount > 0 ? `(${comment.replyCount})` : '' }}</text>
            </view>
          </view>

          <!-- Replies -->
          <view v-if="comment.replies && comment.replies.length > 0" class="replies-container">
            <view
              v-for="reply in comment.replies"
              :key="reply.commentId"
              class="reply-item"
            >
              <view class="reply-header">
                <image
                  :src="reply.userAvatar || defaultAvatar"
                  class="reply-avatar"
                  mode="aspectFill"
                />
                <view class="reply-user-info">
                  <text class="reply-user-name">{{ reply.userName }}</text>
                  <text class="reply-time">{{ formatTime(reply.createdAt) }}</text>
                </view>
                <view
                  v-if="canDelete(reply)"
                  class="delete-btn"
                  @click="handleDeleteComment(reply.commentId)"
                >
                  <text class="delete-icon">×</text>
                </view>
              </view>

              <view class="reply-content">
                <text class="reply-text">{{ reply.content }}</text>
              </view>
            </view>
          </view>

          <!-- Reply input (shown when replying) -->
          <view v-if="replyingTo === comment.commentId" class="reply-input-section">
            <view class="reply-input-wrapper">
              <textarea
                v-model="replyContent"
                class="reply-textarea"
                :placeholder="`回复 ${comment.userName}...`"
                :maxlength="500"
                auto-height
              />
              <view class="reply-actions">
                <button class="cancel-btn" @click="cancelReply">取消</button>
                <button
                  class="reply-submit-btn"
                  :disabled="!replyContent.trim()"
                  @click="handleSubmitReply(comment.commentId)"
                >
                  发送
                </button>
              </view>
            </view>
          </view>
        </view>

        <!-- Load more -->
        <view v-if="hasMore" class="load-more" @click="loadMore">
          <text class="load-more-text">加载更多</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { ResourceComment } from '@/types/comment'
import { getResourceComments, addComment, deleteComment } from '@/services/comment'
import { PLACEHOLDER_IMAGES } from '@/config/images'
import config from '@/config'

interface Props {
  resourceId: number
}

const props = defineProps<Props>()

// 定义事件
const emit = defineEmits<{
  'update:count': [count: number]
}>()

const comments = ref<ResourceComment[]>([])
const loading = ref(false)
const newComment = ref('')
const replyContent = ref('')
const replyingTo = ref<number | null>(null)
const currentPage = ref(1)
const totalPages = ref(1)
const hasMore = ref(false)
const defaultAvatar = PLACEHOLDER_IMAGES.avatar

// Current user ID (from storage)
const currentUserId = ref<number | null>(null)

onMounted(() => {
  loadCurrentUser()
  loadComments()
})

const loadCurrentUser = () => {
  try {
    const userInfo = uni.getStorageSync('userInfo')
    if (userInfo && userInfo.userId) {
      currentUserId.value = userInfo.userId
    }
  } catch (e) {
    console.error('Failed to load user info:', e)
  }
}

const loadComments = async (page = 1) => {
  loading.value = true
  try {
    const res = await getResourceComments(props.resourceId, {
      page,
      pageSize: 10
    })

    if (page === 1) {
      comments.value = res.list
    } else {
      comments.value.push(...res.list)
    }

    currentPage.value = page
    totalPages.value = res.totalPages
    hasMore.value = page < res.totalPages

    // 触发评论数更新事件
    emit('update:count', res.total || 0)
  } catch (error: any) {
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

const handleSubmitComment = async () => {
  const token = uni.getStorageSync(config.tokenKey)
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => uni.reLaunch({ url: '/pages/home/index' }), 2000)
    return
  }

  if (!newComment.value.trim()) return

  try {
    await addComment(props.resourceId, {
      content: newComment.value.trim()
    })

    newComment.value = ''
    uni.showToast({ title: '评论成功', icon: 'success' })

    // Reload comments
    await loadComments(1)
  } catch (error: any) {
    uni.showToast({
      title: error.message || '评论失败',
      icon: 'none'
    })
  }
}

const handleReply = (comment: ResourceComment) => {
  const token = uni.getStorageSync(config.tokenKey)
  if (!token) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    setTimeout(() => uni.reLaunch({ url: '/pages/home/index' }), 2000)
    return
  }

  replyingTo.value = comment.commentId
  replyContent.value = ''
}

const cancelReply = () => {
  replyingTo.value = null
  replyContent.value = ''
}

const handleSubmitReply = async (parentId: number) => {
  if (!replyContent.value.trim()) return

  try {
    await addComment(props.resourceId, {
      parentId,
      content: replyContent.value.trim()
    })

    replyContent.value = ''
    replyingTo.value = null
    uni.showToast({ title: '回复成功', icon: 'success' })

    // Reload comments
    await loadComments(1)
  } catch (error: any) {
    uni.showToast({
      title: error.message || '回复失败',
      icon: 'none'
    })
  }
}

const handleDeleteComment = async (commentId: number) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除这条评论吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteComment(commentId)
          uni.showToast({ title: '删除成功', icon: 'success' })
          await loadComments(1)
        } catch (error: any) {
          uni.showToast({
            title: error.message || '删除失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

const canDelete = (comment: ResourceComment) => {
  return currentUserId.value && comment.userId === currentUserId.value
}

const loadMore = () => {
  if (!loading.value && hasMore.value) {
    loadComments(currentPage.value + 1)
  }
}

const formatTime = (dateStr: string) => {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < 7 * day) {
    return `${Math.floor(diff / day)}天前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}
</script>

<style scoped lang="scss">
.comment-list {
  width: 100%;
}

.comment-input-section {
  padding: 20rpx 24rpx;
  background: #FAFAFA;
  border-bottom: 1rpx solid #F0F0F0;
}

.input-wrapper {
  width: 100%;
}

.comment-textarea {
  width: 100%;
  min-height: 120rpx;
  padding: 16rpx;
  background: #FFFFFF;
  border: 1rpx solid #E0E0E0;
  border-radius: 10rpx;
  font-size: 28rpx;
  line-height: 1.6;
  transition: border-color 0.2s;

  &:focus {
    border-color: #FF6B35;
  }
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12rpx;
}

.char-count {
  font-size: 22rpx;
  color: #999999;
}

.submit-btn {
  padding: 10rpx 28rpx;
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C42 100%);
  color: #FFFFFF;
  border: none;
  border-radius: 24rpx;
  font-size: 26rpx;
  font-weight: 500;
  box-shadow: 0 2rpx 8rpx rgba(255, 107, 53, 0.2);

  &:disabled {
    opacity: 0.5;
    box-shadow: none;
  }

  &:active:not(:disabled) {
    opacity: 0.9;
    transform: scale(0.98);
  }
}

.comments-wrapper {
  background: #FFFFFF;
}

.loading-container,
.empty-state {
  padding: 80rpx 0;
  text-align: center;
}

.loading-text,
.empty-text {
  font-size: 28rpx;
  color: #999999;
}

.comments-container {
  padding: 24rpx;
}

.comment-item {
  margin-bottom: 32rpx;
  padding-bottom: 32rpx;
  border-bottom: 1rpx solid #F0F0F0;

  &:last-child {
    border-bottom: none;
  }
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.user-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  margin-right: 16rpx;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.user-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #333333;
}

.comment-time {
  font-size: 22rpx;
  color: #999999;
}

.delete-btn {
  padding: 8rpx;
  cursor: pointer;
}

.delete-icon {
  font-size: 48rpx;
  color: #999999;
  line-height: 1;
}

.comment-content {
  margin-bottom: 16rpx;
  padding-left: 80rpx;
}

.content-text {
  font-size: 28rpx;
  line-height: 1.6;
  color: #333333;
  word-break: break-all;
}

.comment-actions {
  display: flex;
  gap: 24rpx;
  padding-left: 80rpx;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  cursor: pointer;
}

.action-icon {
  font-size: 24rpx;
}

.action-text {
  font-size: 24rpx;
  color: #666666;
}

.replies-container {
  margin-top: 24rpx;
  padding-left: 80rpx;
  background: #F8F8F8;
  border-radius: 12rpx;
  padding: 16rpx;
}

.reply-item {
  margin-bottom: 24rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.reply-header {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.reply-avatar {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  margin-right: 12rpx;
}

.reply-user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.reply-user-name {
  font-size: 24rpx;
  font-weight: 500;
  color: #333333;
}

.reply-time {
  font-size: 20rpx;
  color: #999999;
}

.reply-content {
  padding-left: 60rpx;
}

.reply-text {
  font-size: 26rpx;
  line-height: 1.6;
  color: #333333;
  word-break: break-all;
}

.reply-input-section {
  margin-top: 24rpx;
  padding-left: 80rpx;
}

.reply-input-wrapper {
  background: #F8F8F8;
  border-radius: 12rpx;
  padding: 16rpx;
}

.reply-textarea {
  width: 100%;
  min-height: 80rpx;
  background: transparent;
  font-size: 26rpx;
  line-height: 1.6;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16rpx;
  margin-top: 12rpx;
}

.cancel-btn,
.reply-submit-btn {
  padding: 8rpx 24rpx;
  border-radius: 24rpx;
  font-size: 24rpx;
  border: none;
}

.cancel-btn {
  background: #E0E0E0;
  color: #666666;
}

.reply-submit-btn {
  background: linear-gradient(135deg, #FF6B35 0%, #FF8C42 100%);
  color: #FFFFFF;

  &:disabled {
    opacity: 0.5;
  }
}

.load-more {
  padding: 32rpx 0;
  text-align: center;
  cursor: pointer;
}

.load-more-text {
  font-size: 26rpx;
  color: #FF6B35;
}
</style>
