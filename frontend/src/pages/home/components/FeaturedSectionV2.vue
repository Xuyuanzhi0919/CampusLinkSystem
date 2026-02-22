<template>
  <view class="featured-section-v2">
    <!-- Section Header -->
    <view class="section-header">
      <view class="header-left">
        <text class="section-title">精选推荐</text>
        <text class="section-subtitle">AI 推荐 + 精选内容</text>
      </view>
      <view class="header-right">
        <view class="refresh-btn" :class="{ 'refresh-btn--loading': loading }" @click="!loading && refreshFeatured()">
          <text class="refresh-icon">↻</text>
          <text class="refresh-text">换一批</text>
        </view>
        <view class="view-more" @click="handleViewMore">
          <text class="more-text">查看更多</text>
          <text class="more-arrow">→</text>
        </view>
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

// ===== 已展示 ID 缓存（TTL 24小时） =====
const SEEN_CACHE_KEY = 'featured_seen_ids'
const SEEN_CACHE_TTL = 24 * 60 * 60 * 1000 // 24小时

interface SeenCache {
  ids: string[]
  expireAt: number
}

const getSeenIds = (): Set<string> => {
  try {
    const raw = uni.getStorageSync(SEEN_CACHE_KEY) as SeenCache | ''
    if (raw && raw.expireAt > Date.now()) {
      return new Set(raw.ids)
    }
  } catch {}
  return new Set()
}

const saveSeenIds = (ids: Set<string>) => {
  try {
    const cache: SeenCache = {
      ids: [...ids],
      expireAt: Date.now() + SEEN_CACHE_TTL
    }
    uni.setStorageSync(SEEN_CACHE_KEY, cache)
  } catch {}
}

const markAsSeen = (items: any[]) => {
  const seen = getSeenIds()
  for (const item of items) {
    const key = `${item.type}-${item.qid || item.resourceId || item.activityId || item.id}`
    seen.add(key)
  }
  // 最多保留 200 条，防止无限增长
  if (seen.size > 200) {
    const arr = [...seen]
    const trimmed = arr.slice(arr.length - 200)
    saveSeenIds(new Set(trimmed))
  } else {
    saveSeenIds(seen)
  }
}

// Fisher-Yates 洗牌，从数组中随机取 n 条
const randomPick = <T>(arr: T[], n: number): T[] => {
  const pool = [...arr]
  for (let i = pool.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [pool[i], pool[j]] = [pool[j], pool[i]]
  }
  return pool.slice(0, n)
}

// 生成某个 item 的唯一缓存 key
const getItemKey = (item: any): string => {
  const id = item.qid || item.resourceId || item.activityId || item.id
  return `${item.type}-${id}`
}

// 加载混合推荐内容
// 策略：
//   候选池扩大到50条，过滤已展示过的内容，保证每次换一批真的能换出新内容
//   问答  — 按浏览量 取前50，再按悬赏量 取前50，合并去重后过滤已看，随机抽2条
//   资源  — 按下载量 取前50，过滤已看，随机抽2条
//   活动  — 取最近进行中的活动（最多20条过滤后随机抽最多2条）
//   每次进入页面重新随机，刷新可见不同内容
const loadData = async () => {
  try {
    loading.value = true
    hasError.value = false

    const now = new Date()
    const seenIds = getSeenIds()

    // 并行拉取候选池（扩大到50条，增加可换出内容的概率）
    const [hotQuestionsRes, bountyQuestionsRes, resourcesRes, activitiesRes] = await Promise.all([
      getQuestionList({ page: 1, pageSize: 50, sortBy: 'views', sortOrder: 'desc' }).catch(() => ({ list: [] })),
      getQuestionList({ page: 1, pageSize: 50, sortBy: 'bounty', sortOrder: 'desc' }).catch(() => ({ list: [] })),
      getResourceList({ page: 1, pageSize: 50, sortBy: 'downloads', sortOrder: 'desc' }).catch(() => ({ list: [] })),
      getActivityList({ page: 1, pageSize: 20 }).catch(() => ({ list: [] }))
    ])

    // 问答候选池：合并热门+高悬赏，按 qid 去重，过滤已看，随机抽2条
    const seenQids = new Set<number>()
    let questionPool: any[] = []
    for (const item of [...(hotQuestionsRes.list || []), ...(bountyQuestionsRes.list || [])]) {
      const id = item.qid || item.questionId || item.id
      if (!seenQids.has(id)) {
        seenQids.add(id)
        questionPool.push({ ...item, type: 'question' })
      }
    }
    // 优先从未看过的里面选，全看完了则重置 seen 后从全量里选
    const freshQuestions = questionPool.filter(item => !seenIds.has(getItemKey(item)))
    const questionSource = freshQuestions.length >= 2 ? freshQuestions : questionPool
    const questions = randomPick(questionSource, 2)

    // 资源候选池：过滤已看，随机抽2条
    let resourcePool = (resourcesRes.list || []).map((item: any) => ({ ...item, type: 'resource' }))
    const freshResources = resourcePool.filter((item: any) => !seenIds.has(getItemKey(item)))
    const resourceSource = freshResources.length >= 2 ? freshResources : resourcePool
    const resources = randomPick(resourceSource, 2)

    // 活动：过滤已结束，过滤已看，随机抽最多2条
    let activityPool = (activitiesRes.list || [])
      .filter((item: any) => {
        if (item.status === 2 || item.status === 3) return false
        if (item.endTime && new Date(item.endTime) < now) return false
        return true
      })
      .map((item: any) => ({ ...item, type: 'activity' }))
    const freshActivities = activityPool.filter((item: any) => !seenIds.has(getItemKey(item)))
    const activitySource = freshActivities.length >= 1 ? freshActivities : activityPool
    const activities = randomPick(activitySource, 2)

    // 交替合并：问答 → 资源 → 活动，保证类型不连续，最多4条
    const interleaved: any[] = []
    const maxLen = Math.max(questions.length, resources.length, activities.length)
    for (let i = 0; i < maxLen && interleaved.length < 4; i++) {
      if (questions[i])                            interleaved.push(questions[i])
      if (interleaved.length < 4 && resources[i])  interleaved.push(resources[i])
      if (interleaved.length < 4 && activities[i]) interleaved.push(activities[i])
    }
    featuredList.value = interleaved

    // 将本次展示的内容记录到 seen 缓存
    markAsSeen(interleaved)

  } catch (error) {
    console.error('[FeaturedSection] 加载推荐内容失败:', error)
    hasError.value = true
  } finally {
    loading.value = false
  }
}

// 换一批：把当前展示内容追加进 seen，再重新筛选
const refreshFeatured = async () => {
  await loadData()
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
    remainingSlots: item.remainingSlots ?? item.maxParticipants - item.currentParticipants ?? undefined,
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
defineExpose({ loadData })
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

.header-right {
  display: flex;
  align-items: center;
  gap: $spacing-4;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: $spacing-1;
  cursor: pointer;
  color: $color-text-secondary;
  font-size: $font-size-sm;
  padding: $spacing-1 $spacing-3;
  border-radius: $radius-button;
  border: 1px solid $color-border;
  background: transparent;
  transition: all 0.2s;

  &:hover {
    color: $campus-blue;
    border-color: $campus-blue;
  }

  &--loading {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.refresh-icon {
  font-size: $font-size-base;
  display: inline-block;
  transition: transform 0.4s ease;

  .refresh-btn:not(.refresh-btn--loading):active & {
    transform: rotate(360deg);
  }
}

.refresh-text {
  font-weight: $font-weight-medium;
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

  /* H5 移动端双列，缩小间距 */
  /* #ifdef H5 */
  @media (max-width: 768px) {
    gap: 10px;
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
    gap: 10px;
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
