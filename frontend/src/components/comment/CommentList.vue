<template>
  <view class="comment-list">

    <!-- ① 输入区 -->
    <view class="input-card">
      <view class="input-inner" :class="{ 'is-focused': inputFocused }">
        <textarea
          v-model="newComment"
          class="comment-textarea"
          placeholder="发表你的看法..."
          :maxlength="500"
          auto-height
          @focus="inputFocused = true"
          @blur="inputFocused = false"
        />
        <view class="input-footer">
          <text class="char-count" :class="{ 'near-limit': newComment.length > 450 }">
            {{ newComment.length }}/500
          </text>
          <view
            class="submit-btn"
            :class="{ 'is-disabled': !newComment.trim() }"
            @click="handleSubmitComment"
          >
            <Icon name="send" :size="15" :stroke-width="2" class="send-icon" />
            <text class="submit-text">发表</text>
          </view>
        </view>
      </view>
    </view>

    <!-- ② 评论列表 -->
    <view class="comments-body">

      <!-- 加载中 -->
      <view v-if="loading" class="state-container">
        <view class="skeleton-list">
          <view v-for="i in 3" :key="i" class="skeleton-item">
            <view class="skeleton-avatar" />
            <view class="skeleton-content">
              <view class="skeleton-line skeleton-line--name" />
              <view class="skeleton-line skeleton-line--text" />
              <view class="skeleton-line skeleton-line--text skeleton-line--short" />
            </view>
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-else-if="comments.length === 0" class="state-container empty-state">
        <Icon name="message-circle" :size="40" :stroke-width="1" class="empty-icon" />
        <text class="empty-title">暂无评论</text>
        <text class="empty-hint">成为第一个留言的人吧</text>
      </view>

      <!-- 评论列表 -->
      <view v-else class="comments-list">
        <view
          v-for="(comment, index) in comments"
          :key="comment.commentId"
          class="comment-item"
          :class="{ 'comment-item--last': index === comments.length - 1 }"
        >
          <!-- 头像 + 用户信息 -->
          <view class="comment-header">
            <image
              :src="comment.userAvatar || defaultAvatar"
              class="user-avatar"
              mode="aspectFill"
            />
            <view class="user-meta">
              <text class="user-name">{{ comment.userName }}</text>
              <text class="comment-time">{{ formatTime(comment.createdAt) }}</text>
            </view>
            <view
              v-if="canDelete(comment)"
              class="delete-btn"
              @click="handleDeleteComment(comment.commentId)"
            >
              <Icon name="trash-2" :size="15" :stroke-width="1.5" class="delete-icon" />
            </view>
          </view>

          <!-- 正文 -->
          <view class="comment-body">
            <text class="comment-text">{{ comment.content }}</text>
          </view>

          <!-- 操作栏 -->
          <view class="comment-footer">
            <view class="action-btn" @click="handleReply(comment)">
              <Icon name="corner-down-left" :size="13" :stroke-width="2" class="action-icon" />
              <text class="action-label">
                回复{{ comment.replyCount > 0 ? ` · ${comment.replyCount}` : '' }}
              </text>
            </view>
          </view>

          <!-- 回复列表 -->
          <view v-if="comment.replies && comment.replies.length > 0" class="replies-wrap">
            <view
              v-for="reply in comment.replies"
              :key="reply.commentId"
              class="reply-item"
            >
              <image
                :src="reply.userAvatar || defaultAvatar"
                class="reply-avatar"
                mode="aspectFill"
              />
              <view class="reply-body">
                <view class="reply-header">
                  <text class="reply-name">{{ reply.userName }}</text>
                  <text class="reply-time">{{ formatTime(reply.createdAt) }}</text>
                  <view
                    v-if="canDelete(reply)"
                    class="delete-btn delete-btn--sm"
                    @click="handleDeleteComment(reply.commentId)"
                  >
                    <Icon name="trash-2" :size="13" :stroke-width="1.5" class="delete-icon" />
                  </view>
                </view>
                <text class="reply-text">{{ reply.content }}</text>
              </view>
            </view>
          </view>

          <!-- 回复输入框 -->
          <view v-if="replyingTo === comment.commentId" class="reply-input-box">
            <view class="reply-input-inner" :class="{ 'is-focused': replyFocused }">
              <textarea
                v-model="replyContent"
                class="reply-textarea"
                :placeholder="`回复 ${comment.userName}...`"
                :maxlength="500"
                auto-height
                @focus="replyFocused = true"
                @blur="replyFocused = false"
              />
              <view class="reply-actions">
                <view class="cancel-btn" @click="cancelReply">取消</view>
                <view
                  class="reply-submit-btn"
                  :class="{ 'is-disabled': !replyContent.trim() }"
                  @click="handleSubmitReply(comment.commentId)"
                >
                  <text>发送</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 加载更多 -->
        <view v-if="hasMore" class="load-more-btn" @click="loadMore">
          <Icon name="chevrons-down" :size="15" :stroke-width="2" class="load-more-icon" />
          <text class="load-more-text">加载更多评论</text>
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
import { requireLogin } from '@/utils/auth'
import Icon from '@/components/icons/index.vue'

interface Props {
  resourceId: number
}

const props = defineProps<Props>()

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

// 焦点状态
const inputFocused = ref(false)
const replyFocused = ref(false)

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
    const res = await getResourceComments(props.resourceId, { page, pageSize: 10 })
    if (page === 1) {
      comments.value = res.list
    } else {
      comments.value.push(...res.list)
    }
    currentPage.value = page
    totalPages.value = res.totalPages
    hasMore.value = page < res.totalPages
    emit('update:count', res.total || 0)
  } catch (error: any) {
    const msg = error?.message || ''
    if (!msg.includes('未授权') && !msg.includes('请先登录')) {
      uni.showToast({ title: msg || '加载评论失败', icon: 'none' })
    }
  } finally {
    loading.value = false
  }
}

const handleSubmitComment = async () => {
  if (!requireLogin('comment')) return
  if (!newComment.value.trim()) return
  try {
    await addComment(props.resourceId, { content: newComment.value.trim() })
    newComment.value = ''
    uni.showToast({ title: '评论成功', icon: 'success' })
    await loadComments(1)
  } catch (error: any) {
    uni.showToast({ title: error.message || '评论失败', icon: 'none' })
  }
}

const handleReply = (comment: ResourceComment) => {
  if (!requireLogin('comment')) return
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
    await addComment(props.resourceId, { parentId, content: replyContent.value.trim() })
    replyContent.value = ''
    replyingTo.value = null
    uni.showToast({ title: '回复成功', icon: 'success' })
    await loadComments(1)
  } catch (error: any) {
    uni.showToast({ title: error.message || '回复失败', icon: 'none' })
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
          uni.showToast({ title: error.message || '删除失败', icon: 'none' })
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
  if (diff < minute) return '刚刚'
  if (diff < hour) return `${Math.floor(diff / minute)}分钟前`
  if (diff < day) return `${Math.floor(diff / hour)}小时前`
  if (diff < 7 * day) return `${Math.floor(diff / day)}天前`
  return date.toLocaleDateString('zh-CN')
}
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

// ============ 根容器 ============
.comment-list {
  width: 100%;
}

// ============ ① 输入区 ============
.input-card {
  padding: $sp-5 $sp-6;
  border-bottom: 1rpx solid $gray-100;
}

.input-inner {
  background: $white;
  border: 1.5rpx solid $gray-200;
  border-radius: $radius-lg;
  padding: $sp-4 $sp-5;
  transition: border-color $duration-fast $ease-out, box-shadow $duration-fast $ease-out;

  &.is-focused {
    border-color: $accent;
    box-shadow: 0 0 0 4rpx rgba($accent, 0.12);
  }
}

.comment-textarea {
  width: 100%;
  min-height: 80rpx;
  font-size: $font-size-sm;
  line-height: $line-height-relaxed;
  color: $gray-800;
  background: transparent;
  border: none;

  &::placeholder {
    color: $gray-400;
  }
}

.input-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: $sp-3;
}

.char-count {
  font-size: $font-size-xs;
  color: $gray-400;
  transition: color $duration-fast;

  &.near-limit {
    color: $warning;
  }
}

.submit-btn {
  display: flex;
  align-items: center;
  gap: $sp-1;
  padding: $sp-2 $sp-5;
  @include gradient-accent;
  border-radius: $radius-full;
  cursor: pointer;
  transition: opacity $duration-fast $ease-out, transform $duration-fast $ease-out;

  &.is-disabled {
    opacity: 0.4;
    pointer-events: none;
  }

  &:active:not(.is-disabled) {
    transform: scale(0.97);
    opacity: 0.9;
  }

  .send-icon {
    color: $white;
  }

  .submit-text {
    font-size: $font-size-xs;
    font-weight: $font-weight-semibold;
    color: $white;
    letter-spacing: 0.5rpx;
  }
}

// ============ ② 评论主体 ============
.comments-body {
  background: $white;
}

// ---- 状态容器（加载 / 空状态）----
.state-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $sp-16 $sp-8;
  gap: $sp-3;
}

// 骨架屏
.skeleton-list {
  width: 100%;
  padding: $sp-6;
  display: flex;
  flex-direction: column;
  gap: $sp-8;
}

.skeleton-item {
  display: flex;
  gap: $sp-4;
}

.skeleton-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: $radius-full;
  background: $gray-100;
  flex-shrink: 0;
  animation: skeleton-pulse 1.4s ease-in-out infinite;
}

.skeleton-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $sp-2;
  padding-top: $sp-1;
}

.skeleton-line {
  height: 24rpx;
  border-radius: $radius-sm;
  background: $gray-100;
  animation: skeleton-pulse 1.4s ease-in-out infinite;

  &--name {
    width: 25%;
    height: 28rpx;
  }

  &--text {
    width: 90%;
  }

  &--short {
    width: 60%;
  }
}

@keyframes skeleton-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.45; }
}

// 空状态
.empty-state {
  .empty-icon {
    color: $gray-300;
    margin-bottom: $sp-2;
  }

  .empty-title {
    font-size: $font-size-base;
    font-weight: $font-weight-semibold;
    color: $gray-500;
  }

  .empty-hint {
    font-size: $font-size-sm;
    color: $gray-400;
  }
}

// ---- 评论列表 ----
.comments-list {
  padding: $sp-2 $sp-6 $sp-6;
}

// ---- 单条评论 ----
.comment-item {
  padding: $sp-6 0;
  border-bottom: 1rpx solid $gray-50;

  &--last {
    border-bottom: none;
  }
}

.comment-header {
  display: flex;
  align-items: center;
  gap: $sp-3;
  margin-bottom: $sp-3;
}

.user-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: $radius-full;
  border: 2rpx solid $gray-100;
  flex-shrink: 0;
  background: $gray-100;
}

.user-meta {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.user-name {
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
  color: $gray-900;
  @include text-ellipsis(1);
}

.comment-time {
  font-size: $font-size-xs;
  color: $gray-400;
}

.delete-btn {
  width: 52rpx;
  height: 52rpx;
  @include flex-center;
  border-radius: $radius-full;
  cursor: pointer;
  flex-shrink: 0;
  transition: background $duration-fast $ease-out;

  &:hover {
    background: $error-50;
  }

  &:active {
    background: $error-100;
  }

  &--sm {
    width: 44rpx;
    height: 44rpx;
  }
}

.delete-icon {
  color: $gray-400;
  transition: color $duration-fast;

  .delete-btn:hover & {
    color: $error;
  }
}

.comment-body {
  padding-left: calc(64rpx + #{$sp-3});
  margin-bottom: $sp-3;
}

.comment-text {
  font-size: $font-size-sm;
  line-height: $line-height-relaxed;
  color: $gray-700;
  word-break: break-word;
  white-space: pre-wrap;
}

.comment-footer {
  padding-left: calc(64rpx + #{$sp-3});
  display: flex;
  gap: $sp-3;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: $sp-1;
  padding: $sp-1 $sp-3;
  border-radius: $radius-full;
  cursor: pointer;
  transition: background $duration-fast $ease-out, color $duration-fast $ease-out;

  &:hover {
    background: $accent-50;

    .action-icon,
    .action-label {
      color: $accent;
    }
  }

  &:active {
    background: $accent-100;
  }
}

.action-icon {
  color: $gray-400;
  transition: color $duration-fast;
}

.action-label {
  font-size: $font-size-xs;
  color: $gray-400;
  transition: color $duration-fast;
}

// ---- 回复列表 ----
.replies-wrap {
  margin-top: $sp-4;
  margin-left: calc(64rpx + #{$sp-3});
  padding: $sp-4 $sp-4 $sp-2;
  background: $gray-50;
  border-radius: $radius-md;
  border-left: 3rpx solid $accent-200;
}

.reply-item {
  display: flex;
  gap: $sp-3;
  padding: $sp-3 0;
  border-bottom: 1rpx solid $gray-100;

  &:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }

  &:first-child {
    padding-top: 0;
  }
}

.reply-avatar {
  width: 40rpx;
  height: 40rpx;
  border-radius: $radius-full;
  border: 1.5rpx solid $gray-100;
  flex-shrink: 0;
  background: $gray-100;
}

.reply-body {
  flex: 1;
  min-width: 0;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: $sp-2;
  margin-bottom: $sp-1;
}

.reply-name {
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  color: $gray-800;
  @include text-ellipsis(1);
}

.reply-time {
  font-size: 20rpx;
  color: $gray-400;
  flex-shrink: 0;
}

.reply-text {
  font-size: $font-size-xs;
  line-height: $line-height-relaxed;
  color: $gray-600;
  word-break: break-word;
  white-space: pre-wrap;
}

// ---- 回复输入框 ----
.reply-input-box {
  margin-top: $sp-4;
  margin-left: calc(64rpx + #{$sp-3});
}

.reply-input-inner {
  background: $white;
  border: 1.5rpx solid $gray-200;
  border-radius: $radius-md;
  padding: $sp-3 $sp-4;
  transition: border-color $duration-fast $ease-out, box-shadow $duration-fast $ease-out;

  &.is-focused {
    border-color: $accent;
    box-shadow: 0 0 0 3rpx rgba($accent, 0.10);
  }
}

.reply-textarea {
  width: 100%;
  min-height: 64rpx;
  font-size: $font-size-xs;
  line-height: $line-height-relaxed;
  color: $gray-800;
  background: transparent;
  border: none;

  &::placeholder {
    color: $gray-400;
  }
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: $sp-2;
  margin-top: $sp-2;
}

.cancel-btn {
  padding: $sp-2 $sp-4;
  border-radius: $radius-full;
  font-size: $font-size-xs;
  color: $gray-500;
  background: $gray-100;
  cursor: pointer;
  transition: background $duration-fast;

  &:hover {
    background: $gray-200;
  }
}

.reply-submit-btn {
  display: flex;
  align-items: center;
  padding: $sp-2 $sp-5;
  @include gradient-accent;
  border-radius: $radius-full;
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  color: $white;
  cursor: pointer;
  transition: opacity $duration-fast $ease-out;

  &.is-disabled {
    opacity: 0.4;
    pointer-events: none;
  }

  &:active:not(.is-disabled) {
    opacity: 0.88;
  }
}

// ---- 加载更多 ----
.load-more-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $sp-2;
  margin-top: $sp-4;
  padding: $sp-3 $sp-8;
  background: $gray-50;
  border: 1.5rpx solid $gray-200;
  border-radius: $radius-full;
  cursor: pointer;
  width: fit-content;
  margin-left: auto;
  margin-right: auto;
  transition: background $duration-fast $ease-out, border-color $duration-fast $ease-out;

  &:hover {
    background: $accent-50;
    border-color: $accent-200;

    .load-more-icon,
    .load-more-text {
      color: $accent;
    }
  }

  &:active {
    background: $accent-100;
  }
}

.load-more-icon {
  color: $gray-400;
  transition: color $duration-fast;
}

.load-more-text {
  font-size: $font-size-xs;
  color: $gray-500;
  transition: color $duration-fast;
}
</style>
