<template>
  <view class="featured-section">
    <view class="section-header">
      <text class="section-title">精选推荐</text>
      <text class="section-subtitle">AI 推荐 + 精选内容</text>
    </view>

    <!-- 使用 gp-skeleton 骨架屏 -->
    <gp-skeleton
      type="list"
      :loading="loading"
      :configs="{
        padding: '0',
        gridRows: 2,
        gridColumns: 2,
        gridRowsGap: '24rpx',
        gridColumnsGap: '24rpx',
        itemDirection: 'row',
        itemGap: '24rpx',
        itemAlign: 'flex-start',
        headShow: true,
        headWidth: '80rpx',
        headHeight: '80rpx',
        headBorderRadius: '12rpx',
        textShow: true,
        textRows: 3,
        textRowsGap: '16rpx',
        textWidth: ['40%', '100%', '60%'],
        textHeight: ['24rpx', '28rpx', '20rpx'],
        textBorderRadius: '6rpx'
      }"
    >
      <!-- 错误状态 -->
      <EmptyState
        v-if="hasError"
        type="error"
        title="推荐内容加载失败"
        description="小助手暂时离开一下～ 稍后再试试吧"
        button-text="刷新试试"
        :show-recommendations="false"
        @action="loadData"
      />

      <!-- 空状态 -->
      <EmptyState
        v-else-if="!loading && featuredList.length === 0"
        type="empty"
        title="暂无推荐内容"
        description="AI 小助手正在为你准备精彩内容..."
        button-text="刷新看看"
        :show-recommendations="false"
        @action="loadData"
      />

      <!-- 内容列表 -->
      <view v-else class="featured-grid">
        <view
          v-for="(item, index) in featuredList"
          :key="`${item.type}-${item.id}`"
          class="featured-card"
          :class="{ 'is-hovered': hoveredId === item.id }"
          @mouseenter="hoveredId = item.id"
          @mouseleave="hoveredId = null"
          @click="handleItemClick(featuredList[index])"
        >
          <!-- 状态角标 -->
          <view class="card-status-badge" :class="getStatusClass(featuredList[index])">
            <text class="status-text">{{ getStatusLabel(featuredList[index]) }}</text>
          </view>

          <!-- 收藏按钮 -->
          <view
            class="card-favorite-btn"
            :class="{ 'is-favorited': featuredList[index].isFavorited }"
            @click.stop="handleFavorite(index)"
          >
            <svg v-if="featuredList[index].isFavorited" class="favorite-icon filled" width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
            </svg>
            <svg v-else class="favorite-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
            </svg>
          </view>

          <!-- 卡片头部：图标 + 发布者信息 -->
          <view class="card-header">
            <!-- 卡片图标 -->
            <view class="card-icon-wrapper" :class="featuredList[index].type">
              <svg v-if="featuredList[index].type === 'question'" class="card-icon" width="24" height="24" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                <path d="M9.09 9C9.3251 8.33167 9.78915 7.76811 10.4 7.40913C11.0108 7.05016 11.7289 6.91894 12.4272 7.03871C13.1255 7.15849 13.7588 7.52152 14.2151 8.06353C14.6713 8.60553 14.9211 9.29152 14.92 10C14.92 12 11.92 13 11.92 13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <circle cx="12" cy="17" r="1" fill="currentColor"/>
              </svg>
              <svg v-else-if="featuredList[index].type === 'resource'" class="card-icon" width="24" height="24" viewBox="0 0 24 24" fill="none">
                <path d="M4 19.5V4.5C4 3.67157 4.67157 3 5.5 3H18.5C19.3284 3 20 3.67157 20 4.5V19.5C20 20.3284 19.3284 21 18.5 21H5.5C4.67157 21 4 20.3284 4 19.5Z" stroke="currentColor" stroke-width="2"/>
                <path d="M8 7H16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M8 11H16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M8 15H12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <svg v-else class="card-icon" width="24" height="24" viewBox="0 0 24 24" fill="none">
                <rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
                <path d="M16 2V6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M8 2V6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M3 10H21" stroke="currentColor" stroke-width="2"/>
                <path d="M9 16L11 18L15 14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </view>

            <!-- 发布者信息 -->
            <view class="author-info">
              <image
                class="author-avatar"
                :src="featuredList[index].authorAvatar || '/static/default-avatar.png'"
                mode="aspectFill"
              />
              <view class="author-details">
                <text class="author-name">{{ featuredList[index].authorName || '匿名用户' }}</text>
                <text class="author-meta">{{ featuredList[index].authorMeta || '' }}</text>
              </view>
            </view>
          </view>

          <!-- 卡片内容 -->
          <view class="card-content">
            <!-- 类型标签 + 主题标签 -->
            <view class="card-tags">
              <view class="type-tag" :class="featuredList[index].type">
                <text class="tag-text">{{ getTypeLabel(featuredList[index].type) }}</text>
              </view>
              <view v-for="tag in (featuredList[index].tags || []).slice(0, 2)" :key="tag" class="topic-tag">
                <text class="tag-text">#{{ tag }}</text>
              </view>
            </view>

            <!-- 标题 -->
            <text class="card-title">{{ featuredList[index].title }}</text>

            <!-- 描述/摘要 -->
            <text class="card-desc">{{ featuredList[index].description }}</text>

            <!-- AI 推荐理由（如果有） -->
            <view v-if="featuredList[index].aiReason" class="ai-reason">
              <svg class="ai-icon" width="14" height="14" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                <path d="M12 8V12L15 15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <text class="ai-text">{{ featuredList[index].aiReason }}</text>
            </view>

            <!-- 社交互动数据 -->
            <view class="card-stats">
              <view class="stat-item">
                <svg class="stat-icon" width="14" height="14" viewBox="0 0 24 24" fill="none">
                  <path d="M1 12C1 12 5 4 12 4C19 4 23 12 23 12C23 12 19 20 12 20C5 20 1 12 1 12Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
                </svg>
                <text>{{ formatCount(featuredList[index].views) }}</text>
              </view>
              <view class="stat-item" @click.stop="handleLike(index)">
                <svg class="stat-icon" :class="{ 'is-liked': featuredList[index].isLiked }" width="14" height="14" viewBox="0 0 24 24" fill="none">
                  <path d="M14 9V5C14 3.89543 13.1046 3 12 3C10.8954 3 10 3.89543 10 5V9" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <path d="M18 13V11C18 9.89543 17.1046 9 16 9H8C6.89543 9 6 9.89543 6 11V13C6 17.4183 9.58172 21 14 21H10C14.4183 21 18 17.4183 18 13Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <text>{{ formatCount(featuredList[index].likes) }}</text>
              </view>
              <view class="stat-item">
                <svg class="stat-icon" width="14" height="14" viewBox="0 0 24 24" fill="none">
                  <path d="M21 15C21 15.5304 20.7893 16.0391 20.4142 16.4142C20.0391 16.7893 19.5304 17 19 17H7L3 21V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H19C19.5304 3 20.0391 3.21071 20.4142 3.58579C20.7893 3.96086 21 4.46957 21 5V15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <text>{{ formatCount(featuredList[index].comments || 0) }}</text>
              </view>
            </view>
          </view>

          <!-- 主行为按钮 (CTA) -->
          <view class="card-cta" :class="featuredList[index].type" @click.stop="handleCTA(featuredList[index])">
            <text class="cta-text">{{ getCTALabel(featuredList[index].type) }}</text>
            <svg class="cta-arrow" width="14" height="14" viewBox="0 0 24 24" fill="none">
              <path d="M5 12H19M19 12L12 5M19 12L12 19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>
        </view>
      </view>
    </gp-skeleton>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getQuestionList } from '@/services/question'
import { getResourceList } from '@/services/resource'
import { getActivityList } from '@/services/activity'
import { addFavorite, removeFavorite, checkFavorite } from '@/services/favorite'
import { likeResource, unlikeResource } from '@/services/resource'
import { useUserStore } from '@/stores/user'
import EmptyState from '@/components/EmptyState.vue'

const userStore = useUserStore()

interface FeaturedItem {
  id: number
  type: 'question' | 'resource' | 'activity'
  title: string
  description: string
  views: number
  likes: number
  comments?: number
  tags?: string[]
  // 发布者信息
  authorName?: string
  authorAvatar?: string
  authorMeta?: string
  // 状态信息
  status?: string
  isHot?: boolean
  isNew?: boolean
  // AI 推荐
  aiReason?: string
  // 交互状态
  isFavorited?: boolean
  isLiked?: boolean
}

const emit = defineEmits<{
  (e: 'item-click', item: FeaturedItem): void
  (e: 'cta-click', item: FeaturedItem): void
  (e: 'favorite', item: FeaturedItem): void
  (e: 'like', item: FeaturedItem): void
}>()

const loading = ref(true)
const featuredList = ref<FeaturedItem[]>([])
const hasError = ref(false)
const hoveredId = ref<number | null>(null)

// 获取类型标签
const getTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    question: '问答',
    resource: '资料',
    activity: '活动'
  }
  return labels[type] || '内容'
}

// 获取主CTA标签
const getCTALabel = (type: string) => {
  const labels: Record<string, string> = {
    question: '我要回答',
    resource: '立即下载',
    activity: '报名参加'
  }
  return labels[type] || '查看详情'
}

// 获取状态标签
const getStatusLabel = (item: FeaturedItem) => {
  if (item.isHot) return '热门'
  if (item.isNew) return '最新'
  if (item.type === 'question') {
    return item.status === 'solved' ? '已解决' : '待回答'
  }
  if (item.type === 'activity') {
    return '报名中'
  }
  if (item.type === 'resource') {
    return 'AI推荐'
  }
  return ''
}

// 获取状态样式类
const getStatusClass = (item: FeaturedItem) => {
  if (item.isHot) return 'status-hot'
  if (item.isNew) return 'status-new'
  if (item.type === 'question' && item.status === 'solved') return 'status-solved'
  if (item.type === 'activity') return 'status-active'
  return 'status-ai'
}

// 格式化数字
const formatCount = (count: number) => {
  if (count >= 10000) return (count / 10000).toFixed(1) + 'w'
  if (count >= 1000) return (count / 1000).toFixed(1) + 'k'
  return count.toString()
}

const handleItemClick = (item: FeaturedItem) => {
  emit('item-click', item)
}

const handleCTA = (item: FeaturedItem) => {
  emit('cta-click', item)
}

// 收藏功能 - 调用真实API
const handleFavorite = async (index: number) => {
  // 检查登录状态
  if (!userStore.isLoggedIn) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }

  const item = featuredList.value[index]
  const wasFavorited = item.isFavorited

  // 乐观更新UI - 直接修改数组元素
  featuredList.value[index] = { ...item, isFavorited: !wasFavorited }

  try {
    if (wasFavorited) {
      await removeFavorite(item.type, item.id)
    } else {
      await addFavorite(item.type, item.id)
    }
    uni.showToast({
      title: wasFavorited ? '已取消收藏' : '收藏成功',
      icon: 'none'
    })
    emit('favorite', featuredList.value[index])
  } catch (err: any) {
    // 回滚UI
    featuredList.value[index] = { ...featuredList.value[index], isFavorited: wasFavorited }
    uni.showToast({
      title: err?.message || '操作失败',
      icon: 'none'
    })
  }
}

// 点赞功能 - 调用真实API
const handleLike = async (index: number) => {
  // 检查登录状态
  if (!userStore.isLoggedIn) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }

  const item = featuredList.value[index]
  const wasLiked = item.isLiked
  const originalLikes = item.likes || 0

  // 乐观更新UI - 直接修改数组元素
  featuredList.value[index] = {
    ...item,
    isLiked: !wasLiked,
    likes: wasLiked ? originalLikes - 1 : originalLikes + 1
  }

  try {
    // 根据类型调用不同的API
    if (item.type === 'resource') {
      if (wasLiked) {
        await unlikeResource(item.id)
      } else {
        await likeResource(item.id)
      }
    }
    // 问答和活动的点赞暂不处理，保持乐观更新
    emit('like', featuredList.value[index])
  } catch (err: any) {
    // 回滚UI
    featuredList.value[index] = { ...featuredList.value[index], isLiked: wasLiked, likes: originalLikes }
    uni.showToast({
      title: err?.message || '操作失败',
      icon: 'none'
    })
  }
}

// 合并后端收藏状态，避免已收藏仍显示空心
const enrichFavoriteStatus = async (items: FeaturedItem[]) => {
  if (!userStore.isLoggedIn || items.length === 0) return items

  const results = await Promise.all(
    items.map((item) =>
      checkFavorite(item.type, item.id)
        .then((res) => !!res?.isFavorited)
        .catch(() => item.isFavorited || false)
    )
  )

  return items.map((item, index) => ({
    ...item,
    isFavorited: results[index] ?? item.isFavorited ?? false
  }))
}

// AI 推荐理由模板
const aiReasons = [
  '与你最近浏览的内容相关',
  '适合你的学习阶段',
  '你关注的领域热门内容',
  '同专业同学都在看'
]

// 从后端加载数据
const loadData = async () => {
  loading.value = true
  hasError.value = false

  try {
    // 并行请求三个模块的数据
    const [questionsRes, resourcesRes, activitiesRes] = await Promise.all([
      getQuestionList({ page: 1, pageSize: 2, sortBy: 'views', sortOrder: 'desc' }),
      getResourceList({ page: 1, pageSize: 2, sortBy: 'download_count', sortOrder: 'desc' }),
      getActivityList({ page: 1, pageSize: 1, status: 1 })
    ])

    const items: FeaturedItem[] = []

    // 处理问答数据
    const questionList = questionsRes?.list || questionsRes?.records || []
    if (questionList.length) {
      questionList.slice(0, 1).forEach((q: any, index: number) => {
        items.push({
          id: q.qid || q.questionId || 0,
          type: 'question',
          title: q.title,
          description: q.content || '快来回答这个问题吧~',
          // 和详情页字段对应：浏览量=views，回答数=answerCount
          views: q.views || 0,
          likes: 0,
          comments: q.answerCount || 0,
          tags: q.tags || ['学习', '求助'],
          authorName: q.askerNickname || q.askerName || '匿名同学',
          authorAvatar: q.askerAvatar,
          authorMeta: q.category ? `${q.category}` : '',
          status: q.status === 1 ? 'solved' : 'pending',
          isHot: (q.views || 0) > 100,
          isNew: false,
          aiReason: aiReasons[index % aiReasons.length],
          // 从后端数据读取收藏和点赞状态
          isFavorited: q.isFavorited || false,
          isLiked: q.isLiked || false
        })
      })
    }

    // 处理资源数据
    const resourceList = resourcesRes?.list || resourcesRes?.records || []
    if (resourceList.length) {
      resourceList.slice(0, 2).forEach((r: any, index: number) => {
        // 获取分类名称
        const categoryLabels: Record<number, string> = {
          0: '课件',
          1: '试题',
          2: '笔记'
        }
        const categoryName = categoryLabels[r.category] || '资料'

        items.push({
          id: r.resourceId,
          type: 'resource',
          title: r.title,
          description: r.description || '',
          // 与资源详情页保持一致：浏览量优先 views，否则回落 downloads
          views: (r.views as number) || r.downloads || 0,
          likes: r.likes || 0,
          comments: r.commentCount || 0,
          tags: r.courseName ? [categoryName, r.courseName] : [categoryName],
          authorName: r.uploaderName || '热心同学',
          authorAvatar: r.uploaderAvatar,
          authorMeta: r.schoolName || '',
          isHot: (r.downloads || 0) > 50,
          isNew: false,
          aiReason: aiReasons[(index + 1) % aiReasons.length],
          // 从后端数据读取收藏和点赞状态
          isFavorited: r.isFavorited || false,
          isLiked: r.isLiked || false
        })
      })
    }

    // 处理活动数据
    const activityList = activitiesRes?.list || activitiesRes?.records || []
    if (activityList.length) {
      activityList.slice(0, 1).forEach((a: any, index: number) => {
        items.push({
          id: a.activityId || a.id,
          type: 'activity',
          title: a.title || a.name,
          description: a.description || '',
          // 与活动详情页对应：显示报名人数/名额
          views: a.currentParticipants || a.participants || 0,
          likes: a.maxParticipants || 0,
          comments: 0,
          tags: [a.clubName || '社团活动'],
          authorName: a.clubName || '校园活动',
          authorAvatar: a.clubLogo,
          authorMeta: a.location || '',
          status: 'active',
          isHot: (a.participants || 0) > 20,
          isNew: true,
          aiReason: aiReasons[(index + 2) % aiReasons.length],
          // 从后端数据读取收藏和点赞状态
          isFavorited: a.isFavorited || false,
          isLiked: a.isLiked || false
        })
      })
    }

    featuredList.value = await enrichFavoriteStatus(items)

    if (items.length === 0) {
      console.log('[FeaturedSection] 暂无精选推荐数据')
    }
  } catch (error) {
    console.error('[FeaturedSection] 加载精选推荐失败:', error)
    hasError.value = true
    featuredList.value = []
  } finally {
    loading.value = false
  }
}

// 暴露刷新方法
defineExpose({ loadData })

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.featured-section {
  margin-bottom: 48px;
}

.section-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 24px;
}

.section-title {
  @include heading-h2;
}

.section-subtitle {
  @include label-text;
}

.featured-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: $module-gap-sm;

  @include mobile {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}

.featured-card {
  @include card-base;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  cursor: pointer;
  position: relative;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover,
  &.is-hovered {
    transform: translateY(-4px);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12);

    .card-cta {
      opacity: 1;
      transform: translateY(0);
    }
  }
}

// 状态角标
.card-status-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
  z-index: 2;

  &.status-hot {
    background: linear-gradient(135deg, #FF6B35, #FF8F5A);
    color: white;
  }

  &.status-new {
    background: linear-gradient(135deg, $campus-teal, lighten($campus-teal, 10%));
    color: white;
  }

  &.status-solved {
    background: linear-gradient(135deg, $success, lighten($success, 10%));
    color: white;
  }

  &.status-active {
    background: linear-gradient(135deg, $campus-blue, lighten($campus-blue, 10%));
    color: white;
  }

  &.status-ai {
    background: linear-gradient(135deg, #8B5CF6, #A78BFA);
    color: white;
  }

  .status-text {
    font-size: inherit;
    color: inherit;
  }
}

// 收藏按钮
.card-favorite-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  z-index: 2;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  &:hover {
    transform: scale(1.1);
    background: white;
  }

  &.is-favorited {
    .favorite-icon {
      color: #FF4757;
    }
  }

  .favorite-icon {
    color: $gray-400;
    transition: color 0.2s ease;

    &.filled {
      color: #FF4757;
    }
  }
}

// 卡片头部
.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 24px; // 给状态角标留空间
}

.card-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.question {
    background: rgba($campus-blue, 0.1);
    color: $campus-blue;
  }

  &.resource {
    background: rgba($campus-amber, 0.1);
    color: $campus-amber;
  }

  &.activity {
    background: rgba($campus-teal, 0.1);
    color: $campus-teal;
  }

  .card-icon {
    width: 24px;
    height: 24px;
  }
}

// 发布者信息
.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.author-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: $gray-100;
  flex-shrink: 0;
}

.author-details {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.author-name {
  font-size: 13px;
  font-weight: 500;
  color: $text-primary;
  @include text-ellipsis(1);
}

.author-meta {
  font-size: 11px;
  color: $text-quaternary;
  @include text-ellipsis(1);
}

// 卡片内容
.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

// 标签区域
.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.type-tag {
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;

  &.question {
    background: rgba($campus-blue, 0.1);
    color: $campus-blue;
  }

  &.resource {
    background: rgba($campus-amber, 0.1);
    color: $campus-amber;
  }

  &.activity {
    background: rgba($campus-teal, 0.1);
    color: $campus-teal;
  }
}

.topic-tag {
  padding: 3px 8px;
  background: $gray-100;
  border-radius: 4px;

  .tag-text {
    font-size: 11px;
    color: $text-tertiary;
  }
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
  line-height: 1.4;
  @include text-ellipsis(2);
}

.card-desc {
  font-size: 13px;
  color: $text-tertiary;
  line-height: 1.5;
  @include text-ellipsis(2);
}

// AI 推荐理由
.ai-reason {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.08), rgba(167, 139, 250, 0.05));
  border-radius: 8px;
  border: 1px solid rgba(139, 92, 246, 0.15);

  .ai-icon {
    color: #8B5CF6;
    flex-shrink: 0;
  }

  .ai-text {
    font-size: 12px;
    color: #7C3AED;
    line-height: 1.4;
  }
}

// 社交互动数据
.card-stats {
  display: flex;
  gap: 16px;
  padding-top: 8px;
  border-top: 1px solid $border-light;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: $text-quaternary;
  cursor: pointer;
  transition: color 0.2s ease;

  &:hover {
    color: $text-secondary;
  }

  .stat-icon {
    width: 14px;
    height: 14px;

    &.is-liked {
      color: #FF4757;
    }
  }
}

// 主行为按钮 CTA
.card-cta {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 20px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  opacity: 0;
  transform: translateY(8px);

  &.question {
    background: linear-gradient(135deg, $campus-blue, lighten($campus-blue, 8%));
  }

  &.resource {
    background: linear-gradient(135deg, $campus-amber, lighten($campus-amber, 8%));
  }

  &.activity {
    background: linear-gradient(135deg, $campus-teal, lighten($campus-teal, 8%));
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }

  .cta-text {
    font-size: 14px;
    font-weight: 600;
    color: white;
  }

  .cta-arrow {
    color: white;
  }
}

// 移动端始终显示 CTA
@include mobile {
  .card-cta {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
