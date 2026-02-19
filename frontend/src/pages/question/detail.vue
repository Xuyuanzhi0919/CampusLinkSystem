<template>
  <PageContainer
    nav-title="问题详情"
    :show-back="true"
    back-text="返回"
    :show-tabbar="false"
    :refresher-enabled="true"
    :refresher-triggered="refreshing"
    :custom-back="true"
    @back="goBack"
    @refresh="handleRefresh"
  >
    <!-- 导航栏右侧：更多按钮 -->
    <template #navbar-right>
      <view class="nav-action-btn" @click="showMoreMenu">
        <Icon name="more-horizontal" :size="20" :stroke-width="1.5" class="nav-action-icon" />
      </view>
    </template>

    <!-- 页面内容（居中容器） -->
    <view class="detail-page">
      <!-- 加载状态 -->
      <view v-if="loading && !question" class="loading-container">
        <view class="skeleton-question">
          <view class="skeleton-title" />
          <view class="skeleton-content" />
          <view class="skeleton-tags" />
        </view>
      </view>

      <!-- 主内容（左右栏布局） -->
      <view v-else-if="question" class="detail-wrapper">
        <!-- 面包屑导航（独立行，在内容之上） -->
        <view class="breadcrumb">
          <text class="breadcrumb-item" @click="handleBreadcrumbClick('home')">首页</text>
          <text class="breadcrumb-divider">/</text>
          <text class="breadcrumb-item" @click="handleBreadcrumbClick('question')">问答广场</text>
          <text class="breadcrumb-divider">/</text>
          <text class="breadcrumb-item" @click="handleBreadcrumbClick('category')">{{ question.category }}</text>
          <text class="breadcrumb-divider">/</text>
          <text class="breadcrumb-item breadcrumb-item--current">问题详情</text>
        </view>

        <!-- 左右两栏容器 -->
        <view class="detail-container">
          <!-- 左侧：主内容区 -->
        <view class="main-content">
          <!-- 问题头部卡片（移除内部面包屑） -->
          <QuestionHeader
            :question="question"
            :show-breadcrumb="false"
            @breadcrumb-click="handleBreadcrumbClick"
            @tag-click="handleTagClick"
            @preview-image="handlePreviewQuestionImage"
          />

          <!-- AI 回答卡片 -->
          <CCard v-if="question.aiAnswer" variant="elevated" class="ai-answer-card">
            <view class="ai-header">
              <text class="ai-icon">🤖</text>
              <text class="ai-title">AI 智能答复</text>
              <text class="ai-time">{{ formatTime(question.aiGeneratedAt!) }}</text>
            </view>
            <view class="ai-content">{{ question.aiAnswer }}</view>
          </CCard>

          <!-- 回答导航条 -->
          <CCard variant="elevated" class="answers-nav-card">
            <view class="answers-nav">
              <view class="answers-nav-left">
                <text class="answers-count">{{ question.answerCount }} 个回答</text>
              </view>

              <view class="answers-nav-right">
                <CButton
                  :type="sortBy === 'likes' ? 'primary' : 'ghost'"
                  size="sm"
                  :plain="sortBy !== 'likes'"
                  @click="handleSortChange('likes')"
                >
                  <Icon name="thumbs-up" :size="16" class="sort-icon" />
                  点赞
                </CButton>

                <CButton
                  :type="sortBy === 'created_at' ? 'primary' : 'ghost'"
                  size="sm"
                  :plain="sortBy !== 'created_at'"
                  @click="handleSortChange('created_at')"
                >
                  <Icon name="clock" :size="16" class="sort-icon" />
                  时间
                </CButton>
              </view>
            </view>
          </CCard>

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
          <CCard v-else variant="elevated" class="empty-answers-card">
            <view class="empty-answers">
              <Icon name="message-circle" :size="64" class="empty-icon" />
              <text class="empty-text">还没有回答，快来抢沙发吧！</text>
            </view>
          </CCard>

          <!-- 底部占位 -->
          <view class="bottom-placeholder" />
        </view>

        <!-- 右侧：侧栏 -->
        <view class="sidebar-content">
          <DetailSidebar
            :question="question"
            :is-my-question="isMyQuestion"
            @follow="handleFollow"
            @collect="handleCollect"
            @share="handleShare"
          />
        </view>
        </view>
        <!-- 结束 detail-container -->
      </view>
      <!-- 结束 detail-wrapper -->

      <!-- 错误状态 -->
      <view v-else-if="error" class="error-container">
        <Icon :name="getErrorIcon(error.type)" :size="64" class="error-icon" />
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
        <!-- 编辑问题（仅提问者可见，且问题未解决） -->
        <view v-if="isMyQuestion && question?.status !== 1" class="menu-item" @click="handleEditQuestion">
          <Icon name="edit" :size="20" class="menu-icon" />
          <text class="menu-label">编辑问题</text>
        </view>

        <!-- 删除问题（仅提问者可见，且问题未解决） -->
        <view v-if="isMyQuestion && question?.status !== 1" class="menu-item menu-item--danger" @click="handleDeleteQuestionFromMenu">
          <Icon name="trash" :size="20" class="menu-icon" />
          <text class="menu-label">删除问题</text>
        </view>

        <!-- 已解决提示（仅提问者，且问题已解决） -->
        <view v-if="isMyQuestion && question?.status === 1" class="menu-item menu-item--disabled">
          <Icon name="info" :size="20" class="menu-icon" />
          <text class="menu-label">已解决的问题无法编辑和删除</text>
        </view>

        <!-- 举报（非提问者可见） -->
        <view v-if="!isMyQuestion" class="menu-item" @click="handleReportQuestionFromMenu">
          <Icon name="flag" :size="20" class="menu-icon" />
          <text class="menu-label">举报</text>
        </view>

        <!-- 取消 -->
        <view class="menu-item menu-item--cancel" @click="closeMoreMenu">
          <text class="menu-label">取消</text>
        </view>
      </view>
    </view>

    <!-- 固定底部回答输入框 -->
    <AnswerInput
      v-if="question && question.status !== 1"
      ref="answerInputRef"
      :submitting="submittingAnswer"
      @submit="handleSubmitAnswer"
    />

    <!-- 已解决提示 -->
    <view v-else-if="question && question.status === 1" class="solved-notice">
      <Icon name="check-circle" :size="20" class="solved-icon" />
      <text class="solved-text">该问题已解决</text>
    </view>

    <!-- 登录引导弹窗（在当前页展示，不跳回首页） -->
    <ClLoginGuideModal
      v-model:visible="showLoginGuide"
      :action-type="loginGuideActionType"
      :title="loginGuideTitle"
      :content="loginGuideContent"
      @confirm="handleLoginGuideConfirm"
    />

    <!-- 登录弹窗 -->
    <LoginModal
      :visible="showLoginModal"
      @update:visible="showLoginModal = $event"
      @login-success="handleLoginSuccess"
    />
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
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
import Icon from '@/components/icons/index.vue'
import QuestionHeader from './components/QuestionHeader.vue'
import DetailSidebar from './components/DetailSidebar.vue'
import AnswerCard from './components/AnswerCard.vue'
import AnswerInput from './components/AnswerInput.vue'
import { formatNumber, formatTime } from '@/utils/formatters'
import { requireLogin } from '@/utils/auth'
import { ClLoginGuideModal } from '@/components/cl'
import LoginModal from '@/components/LoginModal.vue'

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

// 登录引导弹窗状态
const showLoginGuide = ref(false)
const loginGuideActionType = ref('default')
const loginGuideTitle = ref('需要登录')
const loginGuideContent = ref('登录后即可继续操作')
const showLoginModal = ref(false)

onMounted(() => {
  uni.$on('show-login-guide', (data: any) => {
    loginGuideActionType.value = data?.actionType || 'default'
    loginGuideTitle.value = data?.title || '需要登录'
    loginGuideContent.value = data?.content || '登录后即可继续操作'
    showLoginGuide.value = true
  })
  uni.$on('show-login-modal', () => {
    showLoginModal.value = true
  })
})

onUnmounted(() => {
  uni.$off('show-login-guide')
  uni.$off('show-login-modal')
})

const handleLoginGuideConfirm = () => {
  showLoginGuide.value = false
  showLoginModal.value = true
}

const handleLoginSuccess = () => {
  showLoginModal.value = false
}

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

// 排序后的回答列表
const sortedAnswers = computed(() => {
  const list = [...answers.value]
  return list.sort((a, b) => {
    if (a.isAccepted && !b.isAccepted) return -1
    if (!a.isAccepted && b.isAccepted) return 1

    if (sortBy.value === 'likes') {
      return (b.likes || 0) - (a.likes || 0)
    } else {
      return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    }
  })
})

// 获取错误图标
const getErrorIcon = (type: 'not-found' | 'network' | 'unknown'): string => {
  const iconMap: Record<string, string> = {
    'not-found': 'search',
    'network': 'wifi-off',
    'unknown': 'alert-triangle'
  }
  return iconMap[type] || 'alert-triangle'
}

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
    // 401 由 request.ts 统一处理（引导登录），此处不渲染错误页以避免闪烁
    if (err.message?.includes('未授权') || err.message?.includes('请先登录') || err.message?.includes('Token')) {
      return
    }
    if (err.statusCode === 404 || err.message?.includes('不存在') || err.message?.includes('已删除')) {
      error.value = { type: 'not-found', message: '问题不存在或已被删除' }
    } else if (!navigator.onLine || err.message?.includes('网络') || err.message?.includes('timeout')) {
      error.value = { type: 'network', message: '网络连接失败,请检查网络后重试' }
    } else {
      error.value = { type: 'unknown', message: err.message || '加载失败,请稍后重试' }
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
    uni.showToast({ title: error.message || '加载失败', icon: 'none' })
  } finally {
    loadingAnswers.value = false
  }
}

// 下拉刷新
const handleRefresh = async () => {
  refreshing.value = true
  try {
    await Promise.all([
      questionStore.loadQuestionDetail(questionId.value, false),
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
  if (!requireLogin('answer')) return
  try {
    submittingAnswer.value = true
    await answerQuestion(questionId.value, { content: data.content, images: data.images })
    uni.showToast({ title: '回答成功', icon: 'success' })
    answerInputRef.value?.clear()
    await loadAnswers(true)
    await questionStore.loadQuestionDetail(questionId.value, false)
  } catch (error: any) {
    console.error('发布回答失败:', error)
    uni.showToast({ title: error.message || '发布失败', icon: 'none' })
  } finally {
    submittingAnswer.value = false
  }
}

// 采纳回答
const handleAcceptAnswer = (answerId: number) => {
  if (!requireLogin('default')) return
  uni.showModal({
    title: '确认采纳',
    content: '采纳后将无法修改，确定要采纳这个回答吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '采纳中...' })
          await acceptAnswer(questionId.value, answerId)
          uni.showToast({ title: '采纳成功', icon: 'success' })
          await Promise.all([
            questionStore.loadQuestionDetail(questionId.value, false),
            loadAnswers(true)
          ])
        } catch (error: any) {
          console.error('采纳失败:', error)
          uni.showToast({ title: error.message || '采纳失败', icon: 'none' })
        } finally {
          uni.hideLoading()
        }
      }
    }
  })
}

// 点赞回答
const handleLikeAnswer = async (answerId: number) => {
  if (!requireLogin('like')) return
  const answer = answers.value.find(a => a.answerId === answerId)
  if (!answer) return
  const wasLiked = answer.isLiked
  answer.isLiked = !wasLiked
  answer.likes += wasLiked ? -1 : 1
  try {
    if (wasLiked) {
      await unlikeAnswer(answerId)
    } else {
      await likeAnswer(answerId)
    }
  } catch (error: any) {
    answer.isLiked = wasLiked
    answer.likes += wasLiked ? 1 : -1
    console.error('点赞失败:', error)
    uni.showToast({ title: error.message || '操作失败', icon: 'none' })
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
          uni.showToast({ title: '删除成功', icon: 'success' })
          await loadAnswers(true)
          await questionStore.loadQuestionDetail(questionId.value, false)
        } catch (error: any) {
          console.error('删除失败:', error)
          uni.showToast({ title: error.message || '删除失败', icon: 'none' })
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
          uni.showToast({ title: '删除成功', icon: 'success' })
          setTimeout(() => {
            const pages = getCurrentPages()
            if (pages.length > 1) {
              uni.navigateBack()
            } else {
              uni.switchTab({ url: '/pages/home/index' })
            }
          }, 1500)
        } catch (error: any) {
          console.error('删除失败:', error)
          uni.showToast({ title: error.message || '删除失败', icon: 'none' })
        } finally {
          uni.hideLoading()
        }
      }
    }
  })
}

// 其他操作
const handleFollow = () => {
  uni.showToast({ title: '关注功能开发中', icon: 'none' })
}

const handleCollect = () => {
  uni.showToast({ title: '收藏功能开发中', icon: 'none' })
}

const handleShare = () => {
  uni.showToast({ title: '分享功能开发中', icon: 'none' })
}

const handleReportQuestion = () => {
  uni.showModal({
    title: '举报',
    content: '确定要举报这个问题吗？',
    success: (res) => {
      if (res.confirm) {
        uni.showToast({ title: '举报成功，我们会尽快处理', icon: 'success' })
      }
    }
  })
}

const handlePreviewQuestionImage = (index: number) => {
  if (!question.value?.images) return
  uni.previewImage({ current: index, urls: question.value.images })
}

const handleBreadcrumbClick = (type: 'home' | 'question' | 'category') => {
  if (type === 'home') {
    uni.switchTab({ url: '/pages/home/index' })
  } else if (type === 'question') {
    uni.navigateTo({ url: '/pages/question/index' })
  } else if (type === 'category') {
    uni.navigateTo({ url: '/pages/question/index' })
  }
}

const handleTagClick = (tag: string) => {
  uni.navigateTo({ url: `/pages/question/index?tag=${tag}` })
}

const goBack = () => {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    // H5 里 navigateBack 底层是 router.go(-1)，无 fail 回调
    // 额外检查浏览器历史栈，避免 go(-1) 跳到 "/" 触发 404
    // #ifdef H5
    if (window.history.length <= 1) {
      uni.switchTab({ url: '/pages/home/index' })
      return
    }
    // #endif
    uni.navigateBack()
  } else {
    uni.switchTab({ url: '/pages/home/index' })
  }
}

const handleGoBack = goBack

const showMoreMenu = () => {
  showMorePopup.value = true
}

const closeMoreMenu = () => {
  showMorePopup.value = false
}

const handleEditQuestion = () => {
  closeMoreMenu()
  uni.navigateTo({ url: `/pages/question/ask?id=${questionId.value}` })
}

const handleReportQuestionFromMenu = () => {
  closeMoreMenu()
  handleReportQuestion()
}

const handleDeleteQuestionFromMenu = () => {
  closeMoreMenu()
  handleDeleteQuestion()
}

const handleRetry = () => {
  error.value = null
  loadQuestionDetail()
  loadAnswers()
}
</script>

<style lang="scss" scoped>
// ===================================
// 页面容器（统一背景）
// ===================================
.detail-page {
  min-height: 100vh;
  background: $bg-page;
  padding: $sp-6 $sp-8;
  padding-bottom: calc($sp-6 + 200rpx);

  @include mobile {
    padding: $sp-4;
    padding-bottom: calc($sp-4 + 200rpx);
  }
}

// ===================================
// 外层包裹容器 - 包含面包屑和主内容
// ===================================
.detail-wrapper {
  max-width: 1440px; // 容器最大宽度
  margin: 0 auto;
  padding: 0 48rpx; // 两侧留白 48px

  // 中等屏幕适配（1200px - 1440px）
  @media (max-width: 1440px) {
    padding: 0 32rpx; // 缩小两侧留白
  }

  // 小屏幕适配（< 1200px）
  @media (max-width: 1200px) {
    padding: 0 24rpx; // 进一步缩小留白
  }

  // 移动端
  @include mobile {
    padding: 0 24rpx;
  }
}

// ===================================
// 居中内容容器（主布局）- 统一栅格系统
// ===================================
// 栅格系统规范（与首页对齐）：
// - 容器宽度：1440px
// - 左侧内容区：860px (flex: 1, max-width)
// - 中间间隙：24px (gap: 48rpx)
// - 右侧边栏：360px (固定宽度)
// - 两侧留白：48px × 2
// - 总计：48 + 860 + 24 + 360 + 48 = 1340px (留有 100px 弹性空间)
// ===================================
.detail-container {
  display: flex;
  gap: 48rpx; // 24px 中间间距

  // 中等屏幕适配（1200px - 1440px）
  @media (max-width: 1440px) {
    gap: 40rpx; // 缩小间距到 20px
  }

  // 小屏幕适配（< 1200px）
  @media (max-width: 1200px) {
    gap: 32rpx; // 间距缩小到 16px
  }

  // 移动端：纵向布局
  @include mobile {
    flex-direction: column;
    gap: 32rpx;
  }
}

// ===================================
// 面包屑导航（独立行，跨越整个容器宽度）
// ===================================
.breadcrumb {
  display: flex;
  align-items: center;
  gap: $sp-2;
  padding-bottom: $sp-4;
  margin-bottom: $sp-6;
  border-bottom: 1rpx solid $gray-100;

  // 移动端：隐藏
  @include mobile {
    display: none;
  }
}

.breadcrumb-item {
  font-size: $font-size-sm;
  color: $gray-500;
  cursor: pointer;
  transition: color $duration-base;

  &:hover {
    color: $primary;
  }

  &--current {
    color: $gray-900;
    font-weight: $font-weight-semibold;
    cursor: default;

    &:hover {
      color: $gray-900; // 当前页不变色
    }
  }
}

.breadcrumb-divider {
  font-size: $font-size-sm;
  color: $gray-300;
  user-select: none;
}

// ===================================
// 主内容区（左侧）- 固定宽度范围
// ===================================
.main-content {
  flex: 1;
  min-width: 0;
  max-width: 860px; // 与首页统一：800-860px
  display: flex;
  flex-direction: column;
  gap: 32rpx; // 16px 统一卡片间距

  @include mobile {
    max-width: 100%;
    gap: 24rpx;
  }
}

// ===================================
// 侧栏（右侧）- 固定宽度（与首页统一）
// ===================================
.sidebar-content {
  width: 360px; // 与首页统一：320-360px
  flex-shrink: 0;

  // 中等屏幕：缩小到 320px
  @media (max-width: 1200px) {
    width: 320px;
  }

  // 移动端：全宽
  @include mobile {
    width: 100%;
  }
}

// ===================================
// 导航栏右侧按钮
// ===================================
// 导航栏右侧操作区
.nav-actions {
  display: flex;
  align-items: center;
  gap: $sp-2;
}

.nav-action-btn {
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
}

.nav-action-icon {
  color: $gray-700; // 统一图标颜色
  flex-shrink: 0;
  transition: color 0.2s;

  .nav-action-btn:hover & {
    color: $gray-900;
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
.answers-nav-card {
  // 卡片基础样式由 CCard 提供
}

.answers-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;

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
    margin-right: $sp-2;
    color: currentColor;
  }
}

// ===================================
// 回答列表
// ===================================
.answers-list {
  display: flex;
  flex-direction: column;
}

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

// ===================================
// 空状态
// ===================================
.empty-answers-card {
  // 卡片基础样式由 CCard 提供
}

.empty-answers {
  @include flex-center;
  flex-direction: column;
  gap: $sp-6;
  padding: $sp-30 $sp-12;

  .empty-icon {
    opacity: 0.5;
    color: $gray-400;
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
    color: $gray-400;
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

  &--danger {
    .menu-icon {
      color: $error;
    }

    .menu-label {
      color: $error;
    }

    &:active {
      background: rgba($error, 0.05);
    }
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

  &--disabled {
    cursor: not-allowed;
    background: $gray-50;

    .menu-icon {
      color: $info;
    }

    .menu-label {
      color: $gray-600;
      font-size: $font-size-base;
    }

    &:active {
      background: $gray-50;
    }
  }
}

.menu-icon {
  color: $gray-700;
  margin-right: $sp-4;
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
    color: $success;
    margin-right: $sp-2;
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
  height: 100rpx;
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
