<template>
  <view class="detail-page">
    <!-- 加载状态 -->
    <view v-if="loading && !question" class="loading-container">
      <view class="skeleton-question">
        <view class="skeleton-title" />
        <view class="skeleton-content" />
        <view class="skeleton-tags" />
      </view>
    </view>

    <!-- 问题详情 -->
    <scroll-view
      v-else-if="question"
      class="detail-scroll"
      scroll-y
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="handleRefresh"
    >
      <!-- 问题内容区 -->
      <view class="question-section">
        <!-- 分类标识 -->
        <view class="category-badge" :class="`category-${getCategoryType(question.category)}`">
          <text class="category-icon">{{ getCategoryIcon(question.category) }}</text>
          <text class="category-label">{{ question.category }}</text>
        </view>

        <!-- 问题标题 -->
        <view class="question-title">{{ question.title }}</view>

        <!-- 问题内容 -->
        <view class="question-content">{{ question.content }}</view>

        <!-- 图片列表 -->
        <view v-if="question.images && question.images.length > 0" class="question-images">
          <image
            v-for="(img, index) in question.images"
            :key="index"
            :src="img"
            class="question-image"
            mode="aspectFill"
            @click="handlePreviewImage(index)"
          />
        </view>

        <!-- 标签列表 -->
        <view v-if="question.tags && question.tags.length > 0" class="question-tags">
          <view v-for="(tag, index) in question.tags" :key="index" class="tag">
            #{{ tag }}
          </view>
        </view>

        <!-- 统计信息 -->
        <view class="question-stats">
          <view class="stat-item">
            <text class="stat-icon">👁️</text>
            <text class="stat-value">{{ formatNumber(question.views) }} 浏览</text>
          </view>
          <view class="stat-item">
            <text class="stat-icon">💬</text>
            <text class="stat-value">{{ question.answerCount }} 回答</text>
          </view>
          <view v-if="question.bounty > 0" class="stat-item reward">
            <text class="stat-icon">🎁</text>
            <text class="stat-value">{{ question.bounty }} 积分</text>
          </view>
          <view v-if="question.status === 1" class="stat-item solved">
            <text class="stat-icon">✅</text>
            <text class="stat-value">已解决</text>
          </view>
        </view>

        <!-- 提问者信息 -->
        <view class="asker-info">
          <image :src="question.askerAvatar || '/static/default-avatar.png'" class="asker-avatar" mode="aspectFill" />
          <view class="asker-details">
            <text class="asker-name">{{ question.askerNickname }}</text>
            <text class="asker-time">{{ formatTime(question.createdAt) }} 提问</text>
          </view>
        </view>

        <!-- 操作按钮 -->
        <view class="question-actions">
          <view class="action-btn" @click="handleShare">
            <text class="action-icon">📤</text>
            <text class="action-label">分享</text>
          </view>
          <view v-if="isMyQuestion" class="action-btn danger" @click="handleDeleteQuestion">
            <text class="action-icon">🗑️</text>
            <text class="action-label">删除</text>
          </view>
        </view>
      </view>

      <!-- AI 回答区（如果有） -->
      <view v-if="question.aiAnswer" class="ai-answer-section">
        <view class="ai-header">
          <text class="ai-icon">🤖</text>
          <text class="ai-title">AI 智能答复</text>
          <text class="ai-time">{{ formatTime(question.aiGeneratedAt!) }}</text>
        </view>
        <view class="ai-content">{{ question.aiAnswer }}</view>
      </view>

      <!-- 回答列表区 -->
      <view class="answers-section">
        <!-- 回答头部 -->
        <view class="answers-header">
          <view class="answers-title">
            <text class="answers-count">{{ question.answerCount }} 个回答</text>
          </view>
          <view class="answers-sort">
            <view
              class="sort-btn"
              :class="{ active: sortBy === 'likes' }"
              @click="handleSortChange('likes')"
            >
              <text class="sort-icon">👍</text>
              <text class="sort-label">点赞数</text>
            </view>
            <view
              class="sort-btn"
              :class="{ active: sortBy === 'created_at' }"
              @click="handleSortChange('created_at')"
            >
              <text class="sort-icon">🕐</text>
              <text class="sort-label">时间</text>
            </view>
          </view>
        </view>

        <!-- 回答列表 -->
        <view v-if="answers.length > 0" class="answers-list">
          <AnswerCard
            v-for="answer in sortedAnswers"
            :key="answer.answerId"
            :answer="answer"
            :can-accept="canAcceptAnswer"
            @accept="handleAcceptAnswer"
            @like="handleLikeAnswer"
            @delete="handleDeleteAnswer"
          />

          <!-- 加载更多 -->
          <view v-if="hasMoreAnswers" class="load-more" @click="handleLoadMoreAnswers">
            <text v-if="loadingAnswers">加载中...</text>
            <text v-else>加载更多回答</text>
          </view>
        </view>

        <!-- 空状态 -->
        <view v-else class="empty-answers">
          <text class="empty-icon">💭</text>
          <text class="empty-text">还没有回答，快来抢沙发吧！</text>
        </view>
      </view>

      <!-- 底部占位 -->
      <view class="bottom-placeholder" />
    </scroll-view>

    <!-- 错误状态 -->
    <view v-else class="error-container">
      <text class="error-icon">😕</text>
      <text class="error-text">问题不存在或已被删除</text>
      <view class="error-btn" @click="handleGoBack">返回</view>
    </view>

    <!-- 固定底部回答输入框 -->
    <view v-if="question && question.status !== 1" class="answer-input-bar">
      <view class="input-wrapper">
        <input
          v-model="answerContent"
          class="answer-input"
          placeholder="写下你的回答..."
          :focus="inputFocused"
          @focus="handleInputFocus"
          @blur="handleInputBlur"
        />
        <text class="char-count">{{ answerContent.length }}/500</text>
      </view>
      <view class="answer-actions">
        <view class="action-icon-btn" @click="handleChooseImage">
          <text class="icon">🖼️</text>
        </view>
        <view
          class="submit-btn"
          :class="{ disabled: !canSubmitAnswer }"
          @click="handleSubmitAnswer"
        >
          发布
        </view>
      </view>
    </view>

    <!-- 已解决提示 -->
    <view v-else-if="question && question.status === 1" class="solved-notice">
      <text class="solved-icon">✅</text>
      <text class="solved-text">该问题已解决</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useQuestionStore } from '@/stores/question'
import { useUserStore } from '@/stores/user'
import type { AnswerItem } from '@/types/question'
import {
  getAnswerList,
  answerQuestion,
  acceptAnswer,
  likeAnswer,
  unlikeAnswer,
  deleteQuestion,
  deleteAnswer
} from '@/services/question'
import AnswerCard from './components/AnswerCard.vue'
import { formatNumber, formatTime } from '@/utils/formatters'

// Stores
const questionStore = useQuestionStore()
const userStore = useUserStore()

// 问题详情
const question = computed(() => questionStore.currentQuestion)

// 页面状态
const loading = ref(true)
const refreshing = ref(false)

// 回答列表
const answers = ref<AnswerItem[]>([])
const answerPage = ref(1)
const answerPageSize = ref(20)
const answerTotal = ref(0)
const loadingAnswers = ref(false)
const sortBy = ref<'likes' | 'created_at'>('likes')

// 回答输入
const answerContent = ref('')
const answerImages = ref<string[]>([])
const inputFocused = ref(false)

// 问题ID
const questionId = ref(0)

// 是否是我的问题
const isMyQuestion = computed(() => {
  return question.value && userStore.userInfo?.userId === question.value.askerId
})

// 是否可以采纳答案
const canAcceptAnswer = computed(() => {
  return isMyQuestion.value && question.value?.status !== 1
})

// 是否有更多回答
const hasMoreAnswers = computed(() => {
  return answers.value.length < answerTotal.value
})

// 是否可以提交回答
const canSubmitAnswer = computed(() => {
  return answerContent.value.trim().length > 0
})

// 排序后的回答列表（已采纳答案置顶）
const sortedAnswers = computed(() => {
  const list = [...answers.value]
  return list.sort((a, b) => {
    // 已采纳答案始终在最前面
    if (a.isAccepted && !b.isAccepted) return -1
    if (!a.isAccepted && b.isAccepted) return 1

    // 非采纳答案按照选择的排序方式排序
    if (sortBy.value === 'likes') {
      // 按点赞数降序
      return (b.likes || 0) - (a.likes || 0)
    } else {
      // 按时间降序（最新的在前）
      return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    }
  })
})

// 页面加载
onLoad((options) => {
  if (options?.id) {
    questionId.value = parseInt(options.id)
    loadQuestionDetail()
    loadAnswers()
  }
})

// 加载问题详情
const loadQuestionDetail = async () => {
  try {
    loading.value = true
    await questionStore.loadQuestionDetail(questionId.value)
  } catch (error: any) {
    console.error('加载问题详情失败:', error)
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 加载回答列表
const loadAnswers = async (refresh = false) => {
  try {
    if (refresh) {
      answerPage.value = 1
      answers.value = []
    }

    loadingAnswers.value = true

    const res = await getAnswerList(questionId.value, {
      page: answerPage.value,
      pageSize: answerPageSize.value,
      sortBy: sortBy.value
    })

    // 兼容后端返回的数组格式
    const list = Array.isArray(res) ? res : (res.list || [])
    const total = Array.isArray(res) ? res.length : (res.total || 0)

    if (refresh) {
      answers.value = list
    } else {
      answers.value.push(...list)
    }

    answerTotal.value = total
  } catch (error: any) {
    console.error('加载回答列表失败:', error)
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
  } finally {
    loadingAnswers.value = false
  }
}

// 下拉刷新
const handleRefresh = async () => {
  refreshing.value = true
  try {
    await Promise.all([
      questionStore.loadQuestionDetail(questionId.value, false), // 强制刷新，不使用缓存
      loadAnswers(true)
    ])
  } finally {
    refreshing.value = false
  }
}

// 排序切换
const handleSortChange = (value: 'likes' | 'created_at') => {
  sortBy.value = value
  loadAnswers(true)
}

// 加载更多回答
const handleLoadMoreAnswers = () => {
  if (loadingAnswers.value || !hasMoreAnswers.value) return
  answerPage.value++
  loadAnswers()
}

// 输入框聚焦
const handleInputFocus = () => {
  inputFocused.value = true
}

// 输入框失焦
const handleInputBlur = () => {
  inputFocused.value = false
}

// 选择图片
const handleChooseImage = () => {
  uni.chooseImage({
    count: 9 - answerImages.value.length,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      // TODO: 上传图片到 OSS
      answerImages.value.push(...res.tempFilePaths)
    }
  })
}

// 提交回答
const handleSubmitAnswer = async () => {
  if (!canSubmitAnswer.value) return

  try {
    uni.showLoading({ title: '发布中...' })

    await answerQuestion(questionId.value, {
      content: answerContent.value.trim(),
      images: answerImages.value
    })

    uni.showToast({
      title: '回答成功',
      icon: 'success'
    })

    // 清空输入
    answerContent.value = ''
    answerImages.value = []

    // 刷新回答列表
    await loadAnswers(true)

    // 更新问题详情（回答数+1）
    await questionStore.loadQuestionDetail(questionId.value, false)
  } catch (error: any) {
    console.error('发布回答失败:', error)
    uni.showToast({
      title: error.message || '发布失败',
      icon: 'none'
    })
  } finally {
    uni.hideLoading()
  }
}

// 采纳回答
const handleAcceptAnswer = (answerId: number) => {
  uni.showModal({
    title: '确认采纳',
    content: '采纳后将无法修改，确定要采纳这个回答吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '采纳中...' })

          await acceptAnswer(questionId.value, answerId)

          uni.showToast({
            title: '采纳成功',
            icon: 'success'
          })

          // 刷新数据
          await Promise.all([
            questionStore.loadQuestionDetail(questionId.value, false),
            loadAnswers(true)
          ])
        } catch (error: any) {
          console.error('采纳失败:', error)
          uni.showToast({
            title: error.message || '采纳失败',
            icon: 'none'
          })
        } finally {
          uni.hideLoading()
        }
      }
    }
  })
}

// 点赞回答（乐观更新）
const handleLikeAnswer = async (answerId: number) => {
  const answer = answers.value.find(a => a.answerId === answerId)
  if (!answer) return

  const wasLiked = answer.isLiked

  // 乐观更新
  answer.isLiked = !wasLiked
  answer.likes += wasLiked ? -1 : 1

  try {
    if (wasLiked) {
      await unlikeAnswer(answerId)
    } else {
      await likeAnswer(answerId)
    }
  } catch (error: any) {
    // 回滚
    answer.isLiked = wasLiked
    answer.likes += wasLiked ? 1 : -1

    console.error('点赞失败:', error)
    uni.showToast({
      title: error.message || '操作失败',
      icon: 'none'
    })
  }
}

// 删除回答
const handleDeleteAnswer = (answerId: number) => {
  uni.showModal({
    title: '确认删除',
    content: '删除后无法恢复，确定要删除这个回答吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '删除中...' })

          await deleteAnswer(answerId)

          uni.showToast({
            title: '删除成功',
            icon: 'success'
          })

          // 刷新回答列表
          await loadAnswers(true)

          // 更新问题详情（回答数-1）
          await questionStore.loadQuestionDetail(questionId.value, false)
        } catch (error: any) {
          console.error('删除失败:', error)
          uni.showToast({
            title: error.message || '删除失败',
            icon: 'none'
          })
        } finally {
          uni.hideLoading()
        }
      }
    }
  })
}

// 删除问题
const handleDeleteQuestion = () => {
  uni.showModal({
    title: '确认删除',
    content: '删除问题后所有回答也将被删除，确定要删除吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '删除中...' })

          await deleteQuestion(questionId.value)

          uni.showToast({
            title: '删除成功',
            icon: 'success'
          })

          // 返回上一页
          setTimeout(() => {
            uni.navigateBack()
          }, 1500)
        } catch (error: any) {
          console.error('删除失败:', error)
          uni.showToast({
            title: error.message || '删除失败',
            icon: 'none'
          })
        } finally {
          uni.hideLoading()
        }
      }
    }
  })
}

// 分享
const handleShare = () => {
  // TODO: 实现分享功能（生成链接/二维码）
  uni.showToast({
    title: '分享功能开发中',
    icon: 'none'
  })
}

// 预览图片
const handlePreviewImage = (index: number) => {
  if (!question.value?.images) return

  uni.previewImage({
    current: index,
    urls: question.value.images
  })
}

// 返回
const handleGoBack = () => {
  uni.navigateBack()
}

// 获取分类类型
const getCategoryType = (category: string): string => {
  const categoryMap: Record<string, string> = {
    '学习': 'study',
    '生活': 'life',
    '技术': 'tech',
    '其他': 'other'
  }
  return categoryMap[category] || 'other'
}

// 获取分类图标
const getCategoryIcon = (category: string): string => {
  const iconMap: Record<string, string> = {
    '学习': '📚',
    '生活': '🏠',
    '技术': '💻',
    '其他': '📌'
  }
  return iconMap[category] || '📌'
}
</script>

<style lang="scss" scoped>
.detail-page {
  min-height: 100vh;
  background: #FBFCFE;
  padding-bottom: 120rpx;
}

// ===================================
// 加载状态
// ===================================
.loading-container,
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 48rpx;
}

.skeleton-question {
  width: 100%;
  padding: 24rpx;

  .skeleton-title {
    width: 80%;
    height: 60rpx;
    background: linear-gradient(90deg, #F0F0F0 25%, #E0E0E0 50%, #F0F0F0 75%);
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s infinite;
    border-radius: 8rpx;
    margin-bottom: 24rpx;
  }

  .skeleton-content {
    width: 100%;
    height: 200rpx;
    background: linear-gradient(90deg, #F0F0F0 25%, #E0E0E0 50%, #F0F0F0 75%);
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s infinite;
    border-radius: 8rpx;
    margin-bottom: 24rpx;
  }

  .skeleton-tags {
    width: 60%;
    height: 48rpx;
    background: linear-gradient(90deg, #F0F0F0 25%, #E0E0E0 50%, #F0F0F0 75%);
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s infinite;
    border-radius: 8rpx;
  }
}

@keyframes skeleton-loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

// 错误状态
.error-icon {
  font-size: 120rpx;
  margin-bottom: 24rpx;
}

.error-text {
  font-size: 28rpx;
  color: #666;
  margin-bottom: 48rpx;
}

.error-btn {
  padding: 16rpx 48rpx;
  background: #1E5FFF;
  color: #FFF;
  border-radius: 48rpx;
  font-size: 28rpx;
}

// ===================================
// 滚动容器
// ===================================
.detail-scroll {
  height: calc(100vh - 120rpx);
}

.bottom-placeholder {
  height: 40rpx;
}

// ===================================
// 问题内容区
// ===================================
.question-section {
  background: #FFF;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

// 分类标识
.category-badge {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 6rpx 16rpx;
  border-radius: 16rpx;
  font-size: 24rpx;
  margin-bottom: 16rpx;

  .category-icon {
    font-size: 24rpx;
  }

  .category-label {
    font-size: 24rpx;
    font-weight: 600;
  }

  &.category-study {
    background: linear-gradient(135deg, #E8F4FF 0%, #F0F8FF 100%);
    color: #1E5FFF;
  }

  &.category-life {
    background: linear-gradient(135deg, #FFF5E6 0%, #FFFAF0 100%);
    color: #FF7A00;
  }

  &.category-tech {
    background: linear-gradient(135deg, #ECFDF5 0%, #F0FDF9 100%);
    color: #10B981;
  }

  &.category-other {
    background: linear-gradient(135deg, #F5F5F5 0%, #FAFAFA 100%);
    color: #6B7280;
  }
}

// 问题标题
.question-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1D1D1F;
  line-height: 1.5;
  margin-bottom: 16rpx;
}

// 问题内容
.question-content {
  font-size: 28rpx;
  color: #333;
  line-height: 1.8;
  margin-bottom: 24rpx;
  white-space: pre-wrap;
}

// 图片列表
.question-images {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  margin-bottom: 24rpx;

  .question-image {
    width: 216rpx;
    height: 216rpx;
    border-radius: 12rpx;
    background: #F5F5F5;
  }
}

// 标签列表
.question-tags {
  display: flex;
  gap: 12rpx;
  margin-bottom: 24rpx;
  flex-wrap: wrap;

  .tag {
    padding: 6rpx 16rpx;
    background: rgba(30, 95, 255, 0.1);
    color: #1E5FFF;
    font-size: 24rpx;
    border-radius: 12rpx;
  }
}

// 统计信息
.question-stats {
  display: flex;
  gap: 32rpx;
  margin-bottom: 24rpx;
  padding-bottom: 24rpx;
  border-bottom: 1rpx solid #F0F0F0;
  flex-wrap: wrap;

  .stat-item {
    display: flex;
    align-items: center;
    gap: 6rpx;
    font-size: 26rpx;
    color: #666;

    .stat-icon {
      font-size: 26rpx;
    }

    &.reward {
      color: #FF7A00;
      font-weight: 700;
    }

    &.solved {
      color: #10B981;
      font-weight: 700;
    }
  }
}

// 提问者信息
.asker-info {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 24rpx;

  .asker-avatar {
    width: 72rpx;
    height: 72rpx;
    border-radius: 50%;
    background: #F5F5F5;
  }

  .asker-details {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 6rpx;

    .asker-name {
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
    }

    .asker-time {
      font-size: 24rpx;
      color: #999;
    }
  }
}

// 操作按钮
.question-actions {
  display: flex;
  gap: 16rpx;

  .action-btn {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8rpx;
    padding: 16rpx;
    background: #F5F5F5;
    border-radius: 12rpx;
    transition: all 0.3s;

    .action-icon {
      font-size: 28rpx;
    }

    .action-label {
      font-size: 26rpx;
      color: #666;
    }

    &:active {
      background: #E5E5E5;
    }

    &.danger {
      background: rgba(255, 77, 79, 0.1);

      .action-label {
        color: #FF4D4F;
      }

      &:active {
        background: rgba(255, 77, 79, 0.2);
      }
    }
  }
}

// ===================================
// AI 回答区
// ===================================
.ai-answer-section {
  background: linear-gradient(135deg, #F0F8FF 0%, #E8F4FF 100%);
  padding: 24rpx;
  margin-bottom: 16rpx;
  border-radius: 16rpx;
  margin: 0 24rpx 16rpx;
}

.ai-header {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 16rpx;

  .ai-icon {
    font-size: 32rpx;
  }

  .ai-title {
    flex: 1;
    font-size: 28rpx;
    font-weight: 600;
    color: #1E5FFF;
  }

  .ai-time {
    font-size: 24rpx;
    color: #999;
  }
}

.ai-content {
  font-size: 26rpx;
  color: #666;
  line-height: 1.8;
  white-space: pre-wrap;
}

// ===================================
// 回答列表区
// ===================================
.answers-section {
  padding: 24rpx;
}

// 回答头部
.answers-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;

  .answers-count {
    font-size: 30rpx;
    font-weight: 700;
    color: #333;
  }

  .answers-sort {
    display: flex;
    gap: 16rpx;

    .sort-btn {
      display: flex;
      align-items: center;
      gap: 6rpx;
      padding: 8rpx 16rpx;
      background: #F5F5F5;
      border-radius: 24rpx;
      transition: all 0.3s;

      .sort-icon {
        font-size: 24rpx;
      }

      .sort-label {
        font-size: 24rpx;
        color: #666;
      }

      &.active {
        background: linear-gradient(135deg, #E8F4FF 0%, #F0F8FF 100%);

        .sort-label {
          color: #1E5FFF;
          font-weight: 600;
        }
      }

      &:active {
        transform: scale(0.95);
      }
    }
  }
}

// 回答列表
.answers-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

// 加载更多
.load-more {
  text-align: center;
  padding: 32rpx;
  font-size: 26rpx;
  color: #999;
}

// 空状态
.empty-answers {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
  padding: 120rpx 48rpx;

  .empty-icon {
    font-size: 120rpx;
    opacity: 0.5;
  }

  .empty-text {
    font-size: 28rpx;
    color: #999;
  }
}

// ===================================
// 固定底部回答输入框
// ===================================
.answer-input-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #FFF;
  padding: 16rpx 24rpx;
  border-top: 1rpx solid #F0F0F0;
  display: flex;
  align-items: center;
  gap: 16rpx;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.05);
  z-index: 100;

  .input-wrapper {
    flex: 1;
    position: relative;
    display: flex;
    align-items: center;
  }

  .answer-input {
    flex: 1;
    height: 72rpx;
    padding: 0 110rpx 0 24rpx;
    background: #F5F5F5;
    border-radius: 36rpx;
    font-size: 28rpx;
  }

  .char-count {
    position: absolute;
    right: 24rpx;
    font-size: 22rpx;
    color: #999;
  }

  .answer-actions {
    display: flex;
    align-items: center;
    gap: 16rpx;

    .action-icon-btn {
      width: 72rpx;
      height: 72rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #F5F5F5;
      border-radius: 50%;
      transition: all 0.3s;

      .icon {
        font-size: 32rpx;
      }

      &:active {
        background: #E5E5E5;
      }
    }

    .submit-btn {
      padding: 16rpx 32rpx;
      background: linear-gradient(135deg, #1E5FFF 0%, #4A90FF 100%);
      color: #FFF;
      font-size: 28rpx;
      font-weight: 600;
      border-radius: 36rpx;
      transition: all 0.3s;

      &:active {
        transform: scale(0.95);
      }

      &.disabled {
        background: #CCC;
        opacity: 0.6;
      }
    }
  }
}

// 已解决提示
.solved-notice {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(135deg, #ECFDF5 0%, #F0FDF9 100%);
  padding: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  border-top: 1rpx solid #10B981;
  z-index: 100;

  .solved-icon {
    font-size: 32rpx;
  }

  .solved-text {
    font-size: 28rpx;
    color: #10B981;
    font-weight: 600;
  }
}
</style>
