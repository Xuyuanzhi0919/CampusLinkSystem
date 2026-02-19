<template>
  <view class="featured-section-v2">
    <!-- Section Header -->
    <view class="section-header">
      <view class="header-left">
        <text class="section-title">精选推荐</text>
        <text class="section-subtitle">AI 推荐 + 精选内容</text>
      </view>
      <view class="view-more" @click="handleViewMore">
        <text class="more-text">查看更多</text>
        <text class="more-arrow">→</text>
      </view>
    </view>

    <!-- Loading State：骨架屏，2×2 网格匹配精选卡片结构 -->
    <view v-if="loading" class="skeleton-grid">
      <view v-for="i in 4" :key="i" class="skeleton-card">
        <!-- 顶部色条 -->
        <view class="skeleton-type-bar"></view>
        <!-- Header：头像 + 用户名 + 胶囊 -->
        <view class="skeleton-header">
          <view class="skeleton-avatar"></view>
          <view class="skeleton-user-info">
            <view class="skeleton-line skeleton-line--name"></view>
            <view class="skeleton-line skeleton-line--role"></view>
          </view>
          <view class="skeleton-capsule"></view>
        </view>
        <!-- Body：标题 + 描述 + 标签 -->
        <view class="skeleton-body">
          <view class="skeleton-line skeleton-line--title"></view>
          <view class="skeleton-line skeleton-line--title-short"></view>
          <view class="skeleton-line skeleton-line--desc"></view>
          <view class="skeleton-line skeleton-line--desc-short"></view>
          <view class="skeleton-tags">
            <view class="skeleton-tag"></view>
            <view class="skeleton-tag"></view>
          </view>
        </view>
        <!-- Meta + Actions -->
        <view class="skeleton-footer">
          <view class="skeleton-meta">
            <view class="skeleton-line skeleton-line--meta"></view>
            <view class="skeleton-line skeleton-line--meta"></view>
            <view class="skeleton-line skeleton-line--meta"></view>
          </view>
          <view class="skeleton-btn"></view>
        </view>
      </view>
    </view>

    <!-- Error State -->
    <EmptyState
      v-else-if="hasError"
      type="error"
      size="medium"
      title="推荐内容加载失败"
      description="服务连接异常，请稍后再试"
      action-text="重试"
      action-type="secondary"
      @action="loadData"
    />

    <!-- Empty State -->
    <EmptyState
      v-else-if="featuredList.length === 0"
      type="empty"
      size="medium"
      title="今天还没有精选内容"
      description="先看看最新问答和热门资源吧"
      action-text="去提问"
      action-type="primary"
      @action="handleGoAsk"
    />

    <!-- Featured Grid -->
    <view v-else class="featured-grid">
      <!-- H5 端：按顺序逐项渲染，保证类型交替可控 -->
      <!-- #ifdef H5 -->
      <template v-for="item in featuredList" :key="`${item.type}-${item.id}`">
        <ClFeaturedQAItem
          v-if="item.type === 'question'"
          :question="transformToQuestion(item)"
          @click="handleQuestionClick"
          @answer="handleAnswer"
          @user-click="handleUserClick"
          @like="handleLike"
          @comment="handleComment"
        />
        <ClResourceCard
          v-else-if="item.type === 'resource'"
          :resource="transformToResource(item)"
          @click="handleResourceClick"
          @download="handleDownload"
        />
        <ClEventCard
          v-else-if="item.type === 'activity'"
          :event="transformToEvent(item)"
          @click="handleActivityClick"
          @register="handleRegister"
        />
      </template>
      <!-- #endif -->
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

// 空状态组件
import { EmptyState } from '@/components/empty-state'

// H5 端导入企业级组件
// #ifdef H5
import { ClFeaturedQAItem, ClResourceCard, ClEventCard } from '@/components/cl'
// #endif
import { getQuestionList } from '@/services/question'
import { getResourceList } from '@/services/resource'
import { getActivityList } from '@/services/activity'
import { requireLogin } from '@/utils/auth'
import { useNavigation } from '@/composables/useNavigation'

const emit = defineEmits<{
  'item-click': [item: any]
  'view-more': []
}>()

const nav = useNavigation()

const loading = ref(true)
const hasError = ref(false)
const featuredList = ref<any[]>([])

// 加载混合推荐内容
const loadData = async () => {
  try {
    loading.value = true
    hasError.value = false

    // 并行加载问答、资源、活动数据（使用正确的 API 参数）
    const now = new Date()
    const [questionsRes, resourcesRes, activitiesRes] = await Promise.all([
      getQuestionList({ page: 1, pageSize: 2 }).catch((err) => {
        console.error('[FeaturedSection] 问答列表加载失败:', err)
        return { list: [] }
      }),
      getResourceList({ page: 1, pageSize: 2 }).catch((err) => {
        console.error('[FeaturedSection] 资源列表加载失败:', err)
        return { list: [] }
      }),
      // 多请求一些，过滤后保证有足够数量
      getActivityList({ page: 1, pageSize: 6 }).catch((err) => {
        console.error('[FeaturedSection] 活动列表加载失败:', err)
        return { list: [] }
      })
    ])

    // 按类型分组，固定交替顺序：问答 → 资源 → 活动 → 问答
    const questions = (questionsRes.list || []).map((item: any) => ({ ...item, type: 'question' }))
    const resources = (resourcesRes.list || []).map((item: any) => ({ ...item, type: 'resource' }))
    // 过滤掉已结束（status=2/3）和 endTime 已过期的活动
    const activities = (activitiesRes.list || [])
      .filter((item: any) => {
        if (item.status === 2 || item.status === 3) return false
        if (item.endTime && new Date(item.endTime) < now) return false
        return true
      })
      .slice(0, 2)
      .map((item: any) => ({ ...item, type: 'activity' }))

    // 交替插入，保证同类型卡片不连续，最多取 4 条
    const interleaved: any[] = []
    const maxLen = Math.max(questions.length, resources.length, activities.length)
    for (let i = 0; i < maxLen && interleaved.length < 4; i++) {
      if (questions[i]) interleaved.push(questions[i])
      if (interleaved.length < 4 && resources[i]) interleaved.push(resources[i])
      if (interleaved.length < 4 && activities[i]) interleaved.push(activities[i])
    }
    featuredList.value = interleaved

  } catch (error) {
    console.error('[FeaturedSection] 加载推荐内容失败:', error)
    hasError.value = true
  } finally {
    loading.value = false
  }
}

/**
 * 转换为问答卡片数据格式
 * 后端实际返回字段：qid, title, category, bounty, views, answerCount, status, askerNickname, createdAt
 */
const transformToQuestion = (item: any) => {
  return {
    id: item.qid || item.questionId || item.id,
    title: item.title || '',
    description: item.content?.slice(0, 100) || '',
    user: {
      id: item.askerId || 0,
      username: item.askerNickname || item.askerName || '匿名用户',
      avatar: item.askerAvatar || '',
      role: 'student',
      verified: false
    },
    tags: item.tags || [],
    views: item.views || item.viewCount || 0,
    comments: item.answerCount || 0,
    likes: item.likes || 0,
    createdAt: item.createdAt || '',
    isHot: (item.views || item.viewCount || 0) > 100,
    isRecommended: true,
    rewardPoints: item.bounty || 0
  }
}

/**
 * 转换为资源卡片数据格式
 * 后端实际返回字段：resourceId, title, description, uploaderName, uploaderAvatar, fileType, fileSize, category, courseName, score, downloads, likes, status, createdAt
 */
const transformToResource = (item: any) => {
  return {
    id: item.resourceId || item.id,
    title: item.title || '',
    description: item.description || '',
    fileType: item.fileType || getFileExtension(item.fileName || item.fileUrl || ''),
    tags: item.tags || [],
    downloads: item.downloads || 0,
    rating: item.score || item.rating || 0,
    createdAt: item.createdAt || '',
    points: item.pointsCost || item.points || 0
  }
}

/**
 * 转换为活动卡片数据格式
 * 后端实际返回字段：activityId, clubId, clubName, title, description, location, startTime, endTime, maxParticipants, currentParticipants, remainingSlots, rewardPoints, coverImage, status, isJoined, isSignedIn, isFavorited, createdAt
 */
const transformToEvent = (item: any) => {
  const startTime = item.startTime ? new Date(item.startTime) : null
  // 到这里的活动已经过滤掉已结束和已取消，isEnded 固定为 false
  const isRegistering = item.status === 0 || (startTime ? startTime > new Date() : false)

  return {
    id: item.activityId || item.id,
    title: item.title || '',
    organizer: item.clubName || item.organizerName || '社团',
    type: item.activityType || 'other',
    startTime: item.startTime || '',
    endTime: item.endTime || '',
    location: item.location || '待定',
    participants: item.currentParticipants || 0,
    views: item.viewCount || 0,
    isEnded: false,
    isRegistering: isRegistering
  }
}

// 获取文件扩展名
const getFileExtension = (filename: string): string => {
  if (!filename) return 'pdf'
  const ext = filename.split('.').pop()?.toLowerCase() || ''
  return ext || 'pdf'
}

// 点击问答卡片
const handleQuestionClick = (question: any) => {
  if (!question?.id) {
    console.warn('问题 ID 无效:', question)
    return
  }
  uni.navigateTo({
    url: `/pages/question/detail?id=${question.id}`
  })
}

// 回答问题（需要登录）
const handleAnswer = (question: any) => {
  if (!question?.id) {
    console.warn('问题 ID 无效:', question)
    return
  }
  if (!requireLogin('answer')) return
  uni.navigateTo({
    url: `/pages/question/detail?id=${question.id}&action=answer`
  })
}

// 点击用户
const handleUserClick = (user: any) => {
  if (user?.id) {
    uni.navigateTo({
      url: `/pages/user/profile?id=${user.id}`
    })
  }
}

// 点赞问题（需要登录）
const handleLike = (question: any) => {
  if (!question?.id) return
  if (!requireLogin('like')) return
  // TODO: 实现点赞逻辑
  console.log('点赞问题:', question.id)
}

// 评论问题（需要登录）
const handleComment = (question: any) => {
  if (!question?.id) {
    console.warn('问题 ID 无效:', question)
    return
  }
  if (!requireLogin('comment')) return
  uni.navigateTo({
    url: `/pages/question/detail?id=${question.id}&action=comment`
  })
}

// 点击资源卡片
const handleResourceClick = (resource: any) => {
  if (!resource?.id) {
    console.warn('资源 ID 无效:', resource)
    return
  }
  uni.navigateTo({
    url: `/pages/resource/detail?id=${resource.id}`
  })
}

// 下载资源（需要登录）
const handleDownload = (resource: any) => {
  if (!resource?.id) {
    console.warn('资源 ID 无效:', resource)
    return
  }
  if (!requireLogin('download')) return
  uni.navigateTo({
    url: `/pages/resource/detail?id=${resource.id}&action=download`
  })
}

// 点击活动卡片
const handleActivityClick = (event: any) => {
  if (!event?.id) {
    console.warn('活动 ID 无效:', event)
    return
  }
  uni.navigateTo({
    url: `/pages/club/activity-detail?id=${event.id}`
  })
}

// 报名活动（需要登录）
const handleGoAsk = () => {
  nav.toPublish()
}

const handleRegister = (event: any) => {
  if (!event?.id) {
    console.warn('活动 ID 无效:', event)
    return
  }
  // 已结束的活动可以直接查看
  if (event.isEnded) {
    uni.navigateTo({
      url: `/pages/club/activity-detail?id=${event.id}`
    })
    return
  }
  if (!requireLogin('register')) return
  uni.navigateTo({
    url: `/pages/club/activity-detail?id=${event.id}&action=register`
  })
}

const handleViewMore = () => {
  emit('view-more')
}

// 初始化
onMounted(() => {
  loadData()
})

// 暴露刷新方法
defineExpose({
  refresh: loadData
})
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.featured-section-v2 {
  width: 100%;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-8;
  padding: 0 $spacing-2;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: $spacing-2;
}

.section-title {
  font-size: $font-size-2xl;
  font-weight: $font-weight-semibold;
  color: $color-text-primary;
}

.section-subtitle {
  font-size: $font-size-sm;
  color: $color-text-tertiary;
}

.view-more {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  cursor: pointer;
  color: $campus-blue;
  font-size: $font-size-sm;
  transition: opacity 0.2s;

  &:hover {
    opacity: 0.7;
  }
}

.more-text {
  font-weight: $font-weight-medium;
}

.more-arrow {
  font-size: $font-size-lg;
}

.featured-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-6;

  /* H5 移动端单列 */
  /* #ifdef H5 */
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
  /* #endif */

}

// ========== 骨架屏 ==========
@keyframes skeleton-shimmer {
  0% { background-position: -400px 0; }
  100% { background-position: 400px 0; }
}

@mixin skeleton-block {
  background: linear-gradient(90deg, #F0F0F0 25%, #E8E8E8 50%, #F0F0F0 75%);
  background-size: 800px 100%;
  animation: skeleton-shimmer 1.5s infinite linear;
  border-radius: $radius-sm;
}

.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-6;

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.skeleton-card {
  background: $color-bg-card;
  border-radius: $radius-xl;
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: $shadow-base;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
  padding: $spacing-5;
  padding-top: 0;
}

.skeleton-type-bar {
  height: 4px;
  margin: 0 (-$spacing-5);
  @include skeleton-block;
  border-radius: 0;
  animation: none;
  background: #F0F0F0;
  margin-bottom: $spacing-4;
}

.skeleton-header {
  display: flex;
  align-items: center;
  gap: $spacing-3;
}

.skeleton-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  flex-shrink: 0;
  @include skeleton-block;
}

.skeleton-user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: $spacing-1;
}

.skeleton-capsule {
  width: 52px;
  height: 20px;
  border-radius: 10px;
  flex-shrink: 0;
  @include skeleton-block;
}

.skeleton-body {
  display: flex;
  flex-direction: column;
  gap: $spacing-2;
  flex: 1;
}

.skeleton-tags {
  display: flex;
  gap: $spacing-2;
  margin-top: $spacing-1;
}

.skeleton-tag {
  width: 48px;
  height: 20px;
  border-radius: $radius-base;
  @include skeleton-block;
}

.skeleton-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: $spacing-2;
}

.skeleton-meta {
  display: flex;
  gap: $spacing-4;
}

.skeleton-btn {
  width: 64px;
  height: 28px;
  border-radius: $radius-lg;
  @include skeleton-block;
}

.skeleton-line {
  @include skeleton-block;

  &--name   { width: 60%; height: 13px; }
  &--role   { width: 35%; height: 11px; }
  &--title  { width: 90%; height: 16px; }
  &--title-short { width: 65%; height: 16px; }
  &--desc   { width: 100%; height: 13px; }
  &--desc-short { width: 75%; height: 13px; }
  &--meta   { width: 36px; height: 13px; }
}

.error-container,
.empty-container {
  padding: $spacing-16;
  text-align: center;
  color: $color-text-tertiary;
}

.error-container {
  button {
    margin-top: $spacing-4;
    padding: $spacing-3 $spacing-6;
    background: $campus-blue;
    color: white;
    border: none;
    border-radius: $radius-button;
    cursor: pointer;

    &:hover {
      background: $campus-blue-light;
    }
  }
}
</style>
