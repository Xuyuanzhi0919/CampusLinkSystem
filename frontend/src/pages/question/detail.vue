<template>
  <view class="detail-page">
    <!-- 顶部导航栏 -->
    <view class="nav-bar">
      <view class="nav-left" @click="goBack">
        <text class="nav-icon">‹</text>
        <text class="nav-text">返回</text>
      </view>
      <view class="nav-center">
        <text class="nav-title">问题详情</text>
      </view>
      <view class="nav-right" @click="showMoreMenu">
        <text class="nav-icon">•••</text>
      </view>
    </view>

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
    <view v-else-if="error" class="error-container">
      <text class="error-icon">
        {{ error.type === 'not-found' ? '😕' : error.type === 'network' ? '📡' : '⚠️' }}
      </text>
      <text class="error-text">{{ error.message }}</text>
      <view class="error-actions">
        <view
          v-if="error.type === 'network' || error.type === 'unknown'"
          class="error-btn error-btn--primary"
          @click="handleRetry"
        >
          重试
        </view>
        <view class="error-btn" @click="handleGoBack">返回</view>
      </view>
    </view>

    <!-- 更多菜单弹出层 -->
    <view v-if="showMorePopup" class="more-menu-overlay" @click="closeMoreMenu">
      <view class="more-menu-content" @click.stop>
        <view v-if="isMyQuestion" class="menu-item" @click="handleEditQuestion">
          <text class="menu-icon">✏️</text>
          <text class="menu-label">编辑问题</text>
        </view>
        <view class="menu-item" @click="handleReportQuestion">
          <text class="menu-icon">🚨</text>
          <text class="menu-label">举报</text>
        </view>
        <view class="menu-item" @click="handleShareQuestion">
          <text class="menu-icon">📤</text>
          <text class="menu-label">分享</text>
        </view>
        <view class="menu-item menu-item--cancel" @click="closeMoreMenu">
          <text class="menu-label">取消</text>
        </view>
      </view>
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
const error = ref<{
  type: 'not-found' | 'network' | 'unknown'
  message: string
} | null>(null)

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

// 更多菜单状态
const showMorePopup = ref(false)

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
    error.value = null
    await questionStore.loadQuestionDetail(questionId.value)
  } catch (err: any) {
    console.error('加载问题详情失败:', err)

    // 判断错误类型
    if (err.statusCode === 404 || err.message?.includes('不存在') || err.message?.includes('已删除')) {
      error.value = {
        type: 'not-found',
        message: '问题不存在或已被删除'
      }
    } else if (!navigator.onLine || err.message?.includes('网络') || err.message?.includes('timeout')) {
      error.value = {
        type: 'network',
        message: '网络连接失败,请检查网络后重试'
      }
    } else {
      error.value = {
        type: 'unknown',
        message: err.message || '加载失败,请稍后重试'
      }
    }
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
// 返回上一页（智能返回逻辑）
const goBack = () => {
  const pages = getCurrentPages()

  // 检查页面栈，决定返回方式
  if (pages.length === 1) {
    // 页面栈只有一页，直接跳转到问答中心
    uni.switchTab({
      url: '/pages/question/index'
    })
  } else if (pages.length >= 2) {
    // 获取上一页的路径
    const prevPage = pages[pages.length - 2]
    const prevRoute = prevPage.route || ''

    // 如果上一页是问答中心（tabBar页面），使用 switchTab
    if (prevRoute === 'pages/question/index') {
      uni.switchTab({
        url: '/pages/question/index'
      })
    } else {
      // 否则正常返回
      uni.navigateBack()
    }
  } else {
    // 默认返回
    uni.navigateBack()
  }
}

// 兼容旧函数名（错误状态中使用）
const handleGoBack = goBack

// 显示更多菜单
const showMoreMenu = () => {
  showMorePopup.value = true
}

// 关闭更多菜单
const closeMoreMenu = () => {
  showMorePopup.value = false
}

// 编辑问题
const handleEditQuestion = () => {
  closeMoreMenu()
  uni.navigateTo({
    url: `/pages/question/ask?id=${questionId.value}`
  })
}

// 举报问题
const handleReportQuestion = () => {
  closeMoreMenu()
  uni.showModal({
    title: '举报',
    content: '确定要举报这个问题吗？',
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

// 分享问题（从更多菜单）
const handleShareQuestion = () => {
  closeMoreMenu()
  handleShare()
}

// 重试加载
const handleRetry = () => {
  error.value = null
  loadQuestionDetail()
  loadAnswers()
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
// 变量已通过 uni.scss 全局注入

.detail-page {
  min-height: 100vh;
  background: $bg-page;
  padding-bottom: $sp-30;
}

// ===================================
// 顶部导航栏
// ===================================
.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 $sp-8;
  background: $white;
  box-shadow: 0 2rpx 16rpx rgba($gray-900, 0.05);
  position: sticky;
  top: 0;
  z-index: $z-dropdown;
}

.nav-left,
.nav-right {
  display: flex;
  align-items: center;
  padding: $sp-2;
  cursor: pointer;
  transition: opacity $duration-base;

  &:active {
    opacity: 0.6;
  }
}

.nav-icon {
  font-size: $font-size-2xl;
  color: $gray-800;
}

.nav-text {
  font-size: $font-size-base;
  color: $gray-800;
  margin-left: $sp-1;
}

.nav-center {
  flex: 1;
  text-align: center;
}

.nav-title {
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  color: $gray-800;
}

// ===================================
// 更多菜单
// ===================================
.more-menu-overlay {
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

.more-menu-content {
  width: 100%;
  background: $white;
  border-radius: $radius-xl $radius-xl 0 0;
  padding: $sp-6 0;
  animation: slideUp $duration-slow $ease-out;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: $sp-8 $sp-12;
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
      color: $gray-400;
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
// 加载状态
// ===================================
.loading-container,
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: $sp-12;
}

.skeleton-question {
  width: 100%;
  padding: $sp-6;

  .skeleton-title {
    width: 80%;
    height: 60rpx;
    background: linear-gradient(90deg, $gray-100 25%, $gray-200 50%, $gray-100 75%);
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s infinite;
    border-radius: $radius-sm;
    margin-bottom: $sp-6;
  }

  .skeleton-content {
    width: 100%;
    height: 200rpx;
    background: linear-gradient(90deg, $gray-100 25%, $gray-200 50%, $gray-100 75%);
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s infinite;
    border-radius: $radius-sm;
    margin-bottom: $sp-6;
  }

  .skeleton-tags {
    width: 60%;
    height: 48rpx;
    background: linear-gradient(90deg, $gray-100 25%, $gray-200 50%, $gray-100 75%);
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s infinite;
    border-radius: $radius-sm;
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
  margin-bottom: $sp-6;
}

.error-text {
  font-size: $font-size-base;
  color: $gray-600;
  margin-bottom: $sp-12;
  text-align: center;
  line-height: $line-height-relaxed;
}

.error-actions {
  display: flex;
  gap: $sp-6;
}

.error-btn {
  padding: $sp-4 $sp-12;
  background: $gray-100;
  color: $gray-600;
  border-radius: $radius-2xl;
  font-size: $font-size-base;
  transition: $transition-base;

  &:active {
    transform: scale(0.95);
  }

  &--primary {
    background: $primary;
    color: $white;
  }
}

// ===================================
// 滚动容器
// ===================================
.detail-scroll {
  height: calc(100vh - 120rpx);
}

.bottom-placeholder {
  height: $sp-10;
}

// ===================================
// 问题内容区
// ===================================
.question-section {
  background: $white;
  padding: $sp-6;
  margin-bottom: $sp-4;
}

// 分类标识
.category-badge {
  display: inline-flex;
  align-items: center;
  gap: $sp-1;
  padding: $sp-1 $sp-4;
  border-radius: $radius-md;
  font-size: $font-size-sm;
  margin-bottom: $sp-4;

  .category-icon {
    font-size: $font-size-sm;
  }

  .category-label {
    font-size: $font-size-sm;
    font-weight: $font-weight-semibold;
  }

  &.category-study {
    background: linear-gradient(135deg, $primary-50 0%, $primary-100 100%);
    color: $primary;
  }

  &.category-life {
    background: linear-gradient(135deg, $accent-50 0%, $accent-100 100%);
    color: $accent;
  }

  &.category-tech {
    background: linear-gradient(135deg, $success-50 0%, $success-100 100%);
    color: $success;
  }

  &.category-other {
    background: linear-gradient(135deg, $gray-100 0%, $gray-50 100%);
    color: $gray-500;
  }
}

// 问题标题
.question-title {
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: $gray-900;
  line-height: $line-height-relaxed;
  margin-bottom: $sp-4;
}

// 问题内容
.question-content {
  font-size: $font-size-base;
  color: $gray-800;
  line-height: $line-height-loose;
  margin-bottom: $sp-6;
  white-space: pre-wrap;
}

// 图片列表
.question-images {
  display: flex;
  flex-wrap: wrap;
  gap: $sp-4;
  margin-bottom: $sp-6;

  .question-image {
    width: 216rpx;
    height: 216rpx;
    border-radius: $radius-base;
    background: $gray-100;
  }
}

// 标签列表
.question-tags {
  display: flex;
  gap: $sp-3;
  margin-bottom: $sp-6;
  flex-wrap: wrap;

  .tag {
    padding: $sp-1 $sp-4;
    background: rgba($primary, 0.1);
    color: $primary;
    font-size: $font-size-sm;
    border-radius: $radius-base;
  }
}

// 统计信息
.question-stats {
  display: flex;
  gap: $sp-8;
  margin-bottom: $sp-6;
  padding-bottom: $sp-6;
  border-bottom: 1rpx solid $gray-100;
  flex-wrap: wrap;

  .stat-item {
    display: flex;
    align-items: center;
    gap: $sp-1;
    font-size: $font-size-sm + 2rpx;
    color: $gray-600;

    .stat-icon {
      font-size: $font-size-sm + 2rpx;
    }

    &.reward {
      color: $accent;
      font-weight: $font-weight-bold;
    }

    &.solved {
      color: $success;
      font-weight: $font-weight-bold;
    }
  }
}

// 提问者信息
.asker-info {
  display: flex;
  align-items: center;
  gap: $sp-4;
  margin-bottom: $sp-6;

  .asker-avatar {
    width: 72rpx;
    height: 72rpx;
    border-radius: $radius-full;
    background: $gray-100;
  }

  .asker-details {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: $sp-1;

    .asker-name {
      font-size: $font-size-base;
      font-weight: $font-weight-semibold;
      color: $gray-800;
    }

    .asker-time {
      font-size: $font-size-sm;
      color: $gray-400;
    }
  }
}

// 操作按钮
.question-actions {
  display: flex;
  gap: $sp-4;

  .action-btn {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: $sp-2;
    padding: $sp-4;
    background: $gray-100;
    border-radius: $radius-base;
    transition: $transition-slow;

    .action-icon {
      font-size: $font-size-base;
    }

    .action-label {
      font-size: $font-size-sm + 2rpx;
      color: $gray-600;
    }

    &:active {
      background: $gray-200;
    }

    &.danger {
      background: rgba($error, 0.1);

      .action-label {
        color: $error;
      }

      &:active {
        background: rgba($error, 0.2);
      }
    }
  }
}

// ===================================
// AI 回答区
// ===================================
.ai-answer-section {
  background: linear-gradient(135deg, $primary-50 0%, $primary-100 100%);
  padding: $sp-6;
  margin-bottom: $sp-4;
  border-radius: $radius-md;
  margin: 0 $sp-6 $sp-4;
}

.ai-header {
  display: flex;
  align-items: center;
  gap: $sp-3;
  margin-bottom: $sp-4;

  .ai-icon {
    font-size: $font-size-lg;
  }

  .ai-title {
    flex: 1;
    font-size: $font-size-base;
    font-weight: $font-weight-semibold;
    color: $primary;
  }

  .ai-time {
    font-size: $font-size-sm;
    color: $gray-400;
  }
}

.ai-content {
  font-size: $font-size-sm + 2rpx;
  color: $gray-600;
  line-height: $line-height-loose;
  white-space: pre-wrap;
}

// ===================================
// 回答列表区
// ===================================
.answers-section {
  padding: $sp-6;
}

// 回答头部
.answers-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $sp-6;

  .answers-count {
    font-size: $font-size-base + 2rpx;
    font-weight: $font-weight-bold;
    color: $gray-800;
  }

  .answers-sort {
    display: flex;
    gap: $sp-4;

    .sort-btn {
      display: flex;
      align-items: center;
      gap: $sp-1;
      padding: $sp-2 $sp-4;
      background: $gray-100;
      border-radius: $radius-xl;
      transition: $transition-slow;

      .sort-icon {
        font-size: $font-size-sm;
      }

      .sort-label {
        font-size: $font-size-sm;
        color: $gray-600;
      }

      &.active {
        background: linear-gradient(135deg, $primary-50 0%, $primary-100 100%);

        .sort-label {
          color: $primary;
          font-weight: $font-weight-semibold;
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
  gap: $sp-4;
}

// 加载更多
.load-more {
  text-align: center;
  padding: $sp-8;
  font-size: $font-size-sm + 2rpx;
  color: $gray-400;
}

// 空状态
.empty-answers {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $sp-4;
  padding: $sp-30 $sp-12;

  .empty-icon {
    font-size: 120rpx;
    opacity: 0.5;
  }

  .empty-text {
    font-size: $font-size-base;
    color: $gray-400;
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
  background: $white;
  padding: $sp-4 $sp-6;
  border-top: 1rpx solid $gray-100;
  display: flex;
  flex-direction: column;
  gap: $sp-3;
  box-shadow: 0 -4rpx 16rpx rgba($gray-900, 0.05);
  z-index: $z-dropdown;

  .format-toolbar {
    display: flex;
    align-items: center;
    gap: $sp-2;
    padding: $sp-2 0;
    border-bottom: 1rpx solid $gray-100;

    .toolbar-item {
      min-width: 60rpx;
      height: 48rpx;
      @include flex-center;
      background: $gray-100;
      border-radius: $radius-sm;
      padding: 0 $sp-3;
      transition: $transition-base;

      .toolbar-icon {
        font-size: $font-size-sm;
        font-weight: $font-weight-semibold;
        color: $gray-800;
      }

      &:active {
        background: $gray-200;
        transform: scale(0.95);
      }
    }
  }

  .input-wrapper {
    flex: 1;
    position: relative;
    display: flex;
    align-items: flex-end;
    gap: $sp-4;
  }

  .answer-input {
    flex: 1;
    min-height: 72rpx;
    max-height: 200rpx;
    padding: $sp-4 110rpx $sp-4 $sp-6;
    background: $gray-100;
    border-radius: $radius-md;
    font-size: $font-size-base;
    line-height: $line-height-relaxed;
  }

  .char-count {
    position: absolute;
    right: $sp-6;
    bottom: $sp-4;
    font-size: $font-size-xs + 2rpx;
    color: $gray-400;
  }

  .answer-actions {
    display: flex;
    align-items: center;
    gap: $sp-4;
    flex-shrink: 0;

    .action-icon-btn {
      width: 72rpx;
      height: 72rpx;
      @include flex-center;
      background: $gray-100;
      border-radius: $radius-full;
      transition: $transition-slow;

      .icon {
        font-size: $font-size-lg;
      }

      &:active {
        background: $gray-200;
      }
    }

    .submit-btn {
      padding: $sp-4 $sp-8;
      @include gradient-primary;
      color: $white;
      font-size: $font-size-base;
      font-weight: $font-weight-semibold;
      border-radius: $radius-2xl;
      transition: $transition-slow;

      &:active {
        transform: scale(0.95);
      }

      &.disabled {
        background: $gray-300;
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
  background: linear-gradient(135deg, $success-50 0%, $success-100 100%);
  padding: $sp-6;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $sp-3;
  border-top: 1rpx solid $success;
  z-index: $z-dropdown;

  .solved-icon {
    font-size: $font-size-lg;
  }

  .solved-text {
    font-size: $font-size-base;
    color: $success;
    font-weight: $font-weight-semibold;
  }
}
</style>
