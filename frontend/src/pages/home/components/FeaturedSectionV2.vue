<template>
  <view class="featured-section-v2">
    <!-- Section Header -->
    <view class="section-header">
      <view class="header-left">
        <text class="section-title">精选推荐</text>
        <text class="section-subtitle">AI 推荐 + 精选内容</text>
      </view>
    </view>

    <!-- Loading State -->
    <view v-if="loading" class="loading-container">
      <text>加载中...</text>
    </view>

    <!-- Error State -->
    <view v-else-if="hasError" class="error-container">
      <text>推荐内容加载失败</text>
      <button @click="loadData">刷新试试</button>
    </view>

    <!-- Empty State -->
    <view v-else-if="featuredList.length === 0" class="empty-container">
      <text>暂无推荐内容</text>
    </view>

    <!-- Featured Grid -->
    <view v-else class="featured-grid">
      <!-- 小程序端：使用简化卡片 -->
      <!-- #ifdef MP-WEIXIN -->
      <SimpleFeaturedCard
        v-for="item in featuredList"
        :key="`${item.type}-${item.id || item.qid || item.resourceId || item.activityId}`"
        :type="item.type"
        :data="item"
        @click="handleCardClick(item)"
      />
      <!-- #endif -->

      <!-- H5 端：使用企业级卡片 -->
      <!-- #ifdef H5 -->
      <!-- 问答类型：使用 ClFeaturedQAItem -->
      <ClFeaturedQAItem
        v-for="item in featuredList.filter(i => i.type === 'question')"
        :key="`question-${item.id}`"
        :question="transformToQuestion(item)"
        @click="handleQuestionClick"
        @answer="handleAnswer"
        @user-click="handleUserClick"
        @like="handleLike"
        @comment="handleComment"
      />

      <!-- 资源类型：使用 ClResourceCard -->
      <ClResourceCard
        v-for="item in featuredList.filter(i => i.type === 'resource')"
        :key="`resource-${item.id}`"
        :resource="transformToResource(item)"
        @click="handleResourceClick"
        @download="handleDownload"
      />

      <!-- 活动类型：使用 ClEventCard -->
      <ClEventCard
        v-for="item in featuredList.filter(i => i.type === 'activity')"
        :key="`activity-${item.id}`"
        :event="transformToEvent(item)"
        @click="handleActivityClick"
        @register="handleRegister"
      />
      <!-- #endif -->
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

// H5 端导入企业级组件
// #ifdef H5
import { ClFeaturedQAItem, ClResourceCard, ClEventCard } from '@/components/cl'
// #endif

// 小程序端导入简化组件
// #ifdef MP-WEIXIN
import SimpleFeaturedCard from './SimpleFeaturedCard.vue'
// #endif

import { getQuestionList } from '@/services/question'
import { getResourceList } from '@/services/resource'
import { getActivityList } from '@/services/activity'
import { requireLogin } from '@/utils/auth'

const emit = defineEmits<{
  'item-click': [item: any]
}>()

const loading = ref(true)
const hasError = ref(false)
const featuredList = ref<any[]>([])

// 加载混合推荐内容
const loadData = async () => {
  try {
    loading.value = true
    hasError.value = false

    console.log('[FeaturedSection] 开始加载推荐内容...')

    // 并行加载问答、资源、活动数据（使用正确的 API 参数）
    const [questionsRes, resourcesRes, activitiesRes] = await Promise.all([
      getQuestionList({ page: 1, pageSize: 2 }).catch((err) => {
        console.error('[FeaturedSection] 问答列表加载失败:', err)
        return { list: [] }
      }),
      getResourceList({ page: 1, pageSize: 2, status: 1 }).catch((err) => {
        console.error('[FeaturedSection] 资源列表加载失败:', err)
        return { list: [] }
      }),
      getActivityList({ page: 1, pageSize: 2 }).catch((err) => {
        console.error('[FeaturedSection] 活动列表加载失败:', err)
        return { list: [] }
      })
    ])

    console.log('[FeaturedSection] 数据加载完成:', {
      questions: questionsRes.list?.length || 0,
      resources: resourcesRes.list?.length || 0,
      activities: activitiesRes.list?.length || 0
    })

    // 合并数据并添加类型标识
    const allItems = [
      ...(questionsRes.list || []).map((item: any) => ({ ...item, type: 'question' })),
      ...(resourcesRes.list || []).map((item: any) => ({ ...item, type: 'resource' })),
      ...(activitiesRes.list || []).map((item: any) => ({ ...item, type: 'activity' }))
    ]

    console.log('[FeaturedSection] 合并后数据总数:', allItems.length)

    // 打乱顺序（模拟 AI 推荐）
    featuredList.value = allItems.sort(() => Math.random() - 0.5).slice(0, 6)

    console.log('[FeaturedSection] 最终推荐列表:', featuredList.value.length)

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
  const now = new Date()
  const endTime = item.endTime ? new Date(item.endTime) : null
  const startTime = item.startTime ? new Date(item.startTime) : null
  // status: 0-未开始，1-进行中，2-已结束，3-已取消
  const isEnded = item.status === 2 || item.status === 3 || (endTime ? endTime < now : false)
  const isRegistering = item.status === 0 || (startTime ? startTime > now : false)

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
    isEnded: isEnded,
    isRegistering: isRegistering && !isEnded
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

// 小程序端统一卡片点击处理
// #ifdef MP-WEIXIN
const handleCardClick = (item: any) => {
  console.log('[FeaturedSection] 卡片点击:', item.type, item)

  if (item.type === 'question') {
    // 问答：跳转到问题详情
    const qid = item.qid || item.id
    if (qid) {
      uni.navigateTo({
        url: `/pages/question/detail?id=${qid}`
      })
    }
  } else if (item.type === 'resource') {
    // 资源：跳转到资源详情
    const rid = item.resourceId || item.id
    if (rid) {
      uni.navigateTo({
        url: `/pages/resource/detail?id=${rid}`
      })
    }
  } else if (item.type === 'activity') {
    // 活动：跳转到活动详情
    const aid = item.activityId || item.id
    if (aid) {
      uni.navigateTo({
        url: `/pages/club/activity-detail?id=${aid}`
      })
    }
  }
}
// #endif

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

.featured-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: $spacing-6;

  /* 移动端单列 */
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.loading-container,
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

// Debug info for mini program
/* #ifdef MP-WEIXIN */
.debug-info {
  padding: 24rpx;
  background: #FFF3E0;
  border-radius: 12rpx;
  margin-bottom: 24rpx;

  text {
    display: block;
    font-size: 24rpx;
    color: #F57C00;
    line-height: 1.8;
  }
}
/* #endif */
</style>
