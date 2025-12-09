<template>
  <PageContainer
    nav-title="问题详情"
    :show-back="true"
    :show-tabbar="false"
    :refresher-enabled="true"
    :refresher-triggered="refreshing"
    @back="goBack"
    @refresh="handleRefresh"
  >
    <!-- 导航栏右侧：更多按钮 -->
    <template #navbar-right>
      <view class="nav-more-btn" @click="showMoreMenu">
        <text class="nav-more-icon">⋯</text>
      </view>
    </template>

    <!-- 页面内容 -->
    <view class="detail-content">
      <!-- 加载状态 -->
      <view v-if="loading && !question" class="loading-container">
        <view class="skeleton-question">
          <view class="skeleton-title" />
          <view class="skeleton-content" />
          <view class="skeleton-tags" />
        </view>
      </view>

      <!-- 问题详情 -->
      <template v-else-if="question">
        <!-- 问题头部区块 -->
        <QuestionHeader
          :question="question"
          :is-my-question="isMyQuestion"
          @follow="handleFollow"
          @collect="handleCollect"
          @share="handleShare"
          @report="handleReportQuestion"
          @delete="handleDeleteQuestion"
          @breadcrumb-click="handleBreadcrumbClick"
          @tag-click="handleTagClick"
          @preview-image="handlePreviewQuestionImage"
        />

        <!-- AI 回答区（如果有） -->
        <CCard v-if="question.aiAnswer" variant="elevated" class="ai-answer-card">
          <view class="ai-header">
            <text class="ai-icon">🤖</text>
            <text class="ai-title">AI 智能答复</text>
            <text class="ai-time">{{ formatTime(question.aiGeneratedAt!) }}</text>
          </view>
          <view class="ai-content">{{ question.aiAnswer }}</view>
        </CCard>

        <!-- 回答导航条 -->
        <view class="answers-nav">
          <view class="answers-nav-left">
            <text class="answers-count">{{ question.answerCount }} 个回答</text>
          </view>

          <view class="answers-nav-right">
            <!-- 排序按钮 -->
            <CButton
              :type="sortBy === 'likes' ? 'primary' : 'ghost'"
              size="sm"
              :plain="sortBy !== 'likes'"
              @click="handleSortChange('likes')"
            >
              <text class="sort-icon">👍</text>
              点赞
            </CButton>

            <CButton
              :type="sortBy === 'created_at' ? 'primary' : 'ghost'"
              size="sm"
              :plain="sortBy !== 'created_at'"
              @click="handleSortChange('created_at')"
            >
              <text class="sort-icon">🕐</text>
              时间
            </CButton>
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

        <!-- 底部占位（为固定输入框留出空间） -->
        <view class="bottom-placeholder" />
      </template>

      <!-- 错误状态 -->
      <view v-else-if="error" class="error-container">
        <text class="error-icon">
          {{ error.type === 'not-found' ? '😕' : error.type === 'network' ? '📡' : '⚠️' }}
        </text>
        <text class="error-text">{{ error.message }}</text>
        <view class="error-actions">
          <CButton
            v-if="error.type === 'network' || error.type === 'unknown'"
            type="primary"
            size="md"
            @click="handleRetry"
          >
            重试
          </CButton>
          <CButton type="ghost" size="md" @click="handleGoBack">返回</CButton>
        </view>
      </view>
    </view>

    <!-- 更多菜单弹出层 -->
    <view v-if="showMorePopup" class="more-menu-overlay" @click="closeMoreMenu">
      <view class="more-menu-content" @click.stop>
        <view v-if="isMyQuestion" class="menu-item" @click="handleEditQuestion">
          <text class="menu-icon">✏️</text>
          <text class="menu-label">编辑问题</text>
        </view>
        <view class="menu-item" @click="handleReportQuestionFromMenu">
          <text class="menu-icon">🚨</text>
          <text class="menu-label">举报</text>
        </view>
        <view class="menu-item" @click="handleShareFromMenu">
          <text class="menu-icon">📤</text>
          <text class="menu-label">分享</text>
        </view>
        <view class="menu-item menu-item--cancel" @click="closeMoreMenu">
          <text class="menu-label">取消</text>
        </view>
      </view>
    </view>

    <!-- 固定底部回答输入框（问题未解决时显示） -->
    <AnswerInput
      v-if="question && question.status !== 1"
      ref="answerInputRef"
      :submitting="submittingAnswer"
      @submit="handleSubmitAnswer"
    />

    <!-- 已解决提示（问题已解决时显示） -->
    <view v-else-if="question && question.status === 1" class="solved-notice">
      <text class="solved-icon">✅</text>
      <text class="solved-text">该问题已解决</text>
    </view>
  </PageContainer>
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
import { PageContainer } from '@/components/layout'
import { CCard, CButton } from '@/components/ui'
import QuestionHeader from './components/QuestionHeader.vue'
import AnswerCard from './components/AnswerCard.vue'
import AnswerInput from './components/AnswerInput.vue'
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
const answerInputRef = ref<InstanceType<typeof AnswerInput>>()
const submittingAnswer = ref(false)

// 问题ID
const questionId = ref(0)

// 更多菜单状态
const showMorePopup = ref(false)

// 是否是我的问题
const isMyQuestion = computed(() => {
  return !!(question.value && userStore.userInfo?.userId === question.value.askerId)
})

// 是否可以采纳答案
const canAcceptAnswer = computed(() => {
  return isMyQuestion.value && question.value !== null && question.value.status !== 1
})

// 是否有更多回答
const hasMoreAnswers = computed(() => {
  return answers.value.length < answerTotal.value
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

// 提交回答
const handleSubmitAnswer = async (data: { content: string; images: string[] }) => {
  try {
    submittingAnswer.value = true

    await answerQuestion(questionId.value, {
      content: data.content,
      images: data.images
    })

    uni.showToast({
      title: '回答成功',
      icon: 'success'
    })

    // 清空输入
    answerInputRef.value?.clear()

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
    submittingAnswer.value = false
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

          // 延迟后返回问答广场（使用智能返回逻辑）
          setTimeout(() => {
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
              // 默认跳转到问答中心
              uni.switchTab({
                url: '/pages/question/index'
              })
            }
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

// 关注问题
const handleFollow = () => {
  // TODO: 实现关注功能
  uni.showToast({
    title: '关注功能开发中',
    icon: 'none'
  })
}

// 收藏问题
const handleCollect = () => {
  // TODO: 实现收藏功能
  uni.showToast({
    title: '收藏功能开发中',
    icon: 'none'
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

// 举报问题
const handleReportQuestion = () => {
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

// 预览问题图片
const handlePreviewQuestionImage = (index: number) => {
  if (!question.value?.images) return

  uni.previewImage({
    current: index,
    urls: question.value.images
  })
}

// 面包屑点击
const handleBreadcrumbClick = (type: 'home' | 'question' | 'category') => {
  if (type === 'home') {
    uni.switchTab({
      url: '/pages/home/index'
    })
  } else if (type === 'question') {
    uni.switchTab({
      url: '/pages/question/index'
    })
  } else if (type === 'category') {
    // TODO: 跳转到分类筛选页面
    uni.switchTab({
      url: '/pages/question/index'
    })
  }
}

// 标签点击
const handleTagClick = (tag: string) => {
  // TODO: 跳转到标签筛选页面
  uni.navigateTo({
    url: `/pages/question/index?tag=${tag}`
  })
}

// 返回
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

// 从更多菜单举报问题
const handleReportQuestionFromMenu = () => {
  closeMoreMenu()
  handleReportQuestion()
}

// 从更多菜单分享
const handleShareFromMenu = () => {
  closeMoreMenu()
  handleShare()
}

// 重试加载
const handleRetry = () => {
  error.value = null
  loadQuestionDetail()
  loadAnswers()
}
</script>

<style lang="scss" scoped>
// ===================================
// 页面内容容器
// ===================================
.detail-content {
  min-height: 100vh;
  padding: $sp-6;
  padding-bottom: calc($sp-6 + 200rpx); // 为固定输入框留出空间

  @include mobile {
    padding: $sp-4;
    padding-bottom: calc($sp-4 + 200rpx);
  }
}

// ===================================
// 导航栏右侧按钮
// ===================================
.nav-more-btn {
  width: 72rpx;
  height: 72rpx;
  @include flex-center;
  cursor: pointer;
  transition: background $duration-base;

  &:hover {
    background: rgba($gray-900, 0.05);
    border-radius: $radius-full;
  }

  &:active {
    background: rgba($gray-900, 0.1);
  }

  .nav-more-icon {
    font-size: $font-size-2xl;
    color: $gray-700;
    font-weight: $font-weight-bold;
    letter-spacing: 2rpx;
  }
}

// ===================================
// 加载状态
// ===================================
.loading-container {
  @include flex-center;
  flex-direction: column;
  min-height: 80vh;
  padding: $sp-12;
}

.skeleton-question {
  width: 100%;
  max-width: 800rpx;

  .skeleton-title,
  .skeleton-content,
  .skeleton-tags {
    background: linear-gradient(90deg, $gray-100 25%, $gray-200 50%, $gray-100 75%);
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s infinite;
    border-radius: $radius-sm;
  }

  .skeleton-title {
    width: 80%;
    height: 60rpx;
    margin-bottom: $sp-6;
  }

  .skeleton-content {
    width: 100%;
    height: 200rpx;
    margin-bottom: $sp-6;
  }

  .skeleton-tags {
    width: 60%;
    height: 48rpx;
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

// ===================================
// AI 回答卡片
// ===================================
.ai-answer-card {
  margin-bottom: $sp-6;
  background: linear-gradient(135deg, $primary-50 0%, $primary-100 100%);
}

.ai-header {
  display: flex;
  align-items: center;
  gap: $sp-3;
  margin-bottom: $sp-4;
  padding-bottom: $sp-4;
  border-bottom: 1rpx solid rgba($primary, 0.2);

  .ai-icon {
    font-size: $font-size-2xl;
  }

  .ai-title {
    flex: 1;
    font-size: $font-size-base + 2rpx;
    font-weight: $font-weight-bold;
    color: $primary;
  }

  .ai-time {
    font-size: $font-size-sm;
    color: $gray-500;
  }
}

.ai-content {
  font-size: $font-size-base;
  color: $gray-700;
  line-height: $line-height-loose;
  white-space: pre-wrap;
  word-wrap: break-word;
}

// ===================================
// 回答导航条
// ===================================
.answers-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $sp-6;
  padding: $sp-6;
  background: $white;
  border-radius: $radius-base;
  box-shadow: $shadow-sm;

  @include mobile {
    flex-direction: column;
    gap: $sp-4;
    align-items: flex-start;
  }
}

.answers-nav-left {
  .answers-count {
    font-size: $font-size-lg;
    font-weight: $font-weight-bold;
    color: $gray-900;
  }
}

.answers-nav-right {
  display: flex;
  align-items: center;
  gap: $sp-3;

  .sort-icon {
    margin-right: $sp-1;
  }
}

// ===================================
// 回答列表
// ===================================
.answers-list {
  display: flex;
  flex-direction: column;
}

// 加载更多
.load-more {
  text-align: center;
  padding: $sp-8;
  font-size: $font-size-base;
  color: $gray-500;
  cursor: pointer;
  transition: color $duration-base;

  &:hover {
    color: $primary;
  }
}

// 空状态
.empty-answers {
  @include flex-center;
  flex-direction: column;
  gap: $sp-6;
  padding: $sp-30 $sp-12;
  background: $white;
  border-radius: $radius-base;

  .empty-icon {
    font-size: 120rpx;
    opacity: 0.5;
  }

  .empty-text {
    font-size: $font-size-base;
    color: $gray-500;
  }
}

// ===================================
// 错误状态
// ===================================
.error-container {
  @include flex-center;
  flex-direction: column;
  min-height: 80vh;
  padding: $sp-12;

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
// 已解决提示
// ===================================
.solved-notice {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  @include flex-center;
  gap: $sp-3;
  padding: $sp-6;
  padding-bottom: calc($sp-6 + env(safe-area-inset-bottom));
  background: linear-gradient(135deg, $success-50 0%, $success-100 100%);
  border-top: 2rpx solid $success;
  z-index: $z-dropdown;

  .solved-icon {
    font-size: $font-size-xl;
  }

  .solved-text {
    font-size: $font-size-base;
    color: $success;
    font-weight: $font-weight-semibold;
  }
}

// ===================================
// 底部占位
// ===================================
.bottom-placeholder {
  height: 200rpx;
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
</style>
