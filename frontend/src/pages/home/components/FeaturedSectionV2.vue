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
      <gp-skeleton :loading="true" type="card" :count="4" />
    </view>

    <!-- Error State (使用统一企业级组件) -->
    <ClError
      v-else-if="hasError"
      type="server"
      size="compact"
      variant="card"
      title="推荐内容加载失败"
      retry-text="刷新试试"
      @retry="loadData"
    />

    <!-- Empty State (使用统一企业级组件) -->
    <ClEmpty
      v-else-if="featuredList.length === 0"
      type="default"
      size="compact"
      variant="card"
      title="暂无推荐内容"
      description="探索更多精彩内容"
    />

    <!-- Featured Grid (使用企业级卡片) -->
    <view v-else class="featured-grid">
      <!-- 问答类型：使用 ClFeaturedQAItem -->
      <ClFeaturedQAItem
        v-for="item in featuredList.filter(i => i.type === 'question')"
        :key="`question-${item.id}`"
        :question="transformToQuestion(item)"
        @click="handleItemClick"
        @answer="handleAnswer"
        @user-click="handleUserClick"
        @meta-click="handleMetaClick"
      />

      <!-- 资源类型：使用 ClResourceCard -->
      <ClResourceCard
        v-for="item in featuredList.filter(i => i.type === 'resource')"
        :key="`resource-${item.id}`"
        :resource="transformToResource(item)"
        @click="handleItemClick"
        @download="handleDownload"
        @meta-click="handleMetaClick"
      />

      <!-- 活动类型：使用 ClEventCard -->
      <ClEventCard
        v-for="item in featuredList.filter(i => i.type === 'activity')"
        :key="`activity-${item.id}`"
        :event="transformToEvent(item)"
        @click="handleItemClick"
        @register="handleRegister"
        @meta-click="handleMetaClick"
      />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ClFeaturedQAItem, ClResourceCard, ClEventCard, ClEmpty, ClError } from '@/components/cl'
import { getQuestionList } from '@/services/question'
import { getResourceList } from '@/services/resource'
import { getActivityList } from '@/services/activity'

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

    // 并行加载问答、资源、活动数据
    const [questions, resources, activities] = await Promise.all([
      getQuestionList({ page: 1, pageSize: 2, sortBy: 'views', order: 'desc' }).catch(() => ({ list: [] })),
      getResourceList({ page: 1, pageSize: 2, sortBy: 'downloads', order: 'desc', status: 1 }).catch(() => ({ list: [] })),
      getActivityList({ page: 1, pageSize: 2, sortBy: 'startTime', order: 'desc' }).catch(() => ({ list: [] }))
    ])

    // 合并数据并添加类型标识
    const allItems = [
      ...questions.list.map((item: any) => ({ ...item, type: 'question' })),
      ...resources.list.map((item: any) => ({ ...item, type: 'resource' })),
      ...activities.list.map((item: any) => ({ ...item, type: 'activity' }))
    ]

    // 打乱顺序（模拟 AI 推荐）
    featuredList.value = allItems.sort(() => Math.random() - 0.5).slice(0, 6)

  } catch (error) {
    console.error('加载推荐内容失败:', error)
    hasError.value = true
  } finally {
    loading.value = false
  }
}

// 转换为问答卡片数据格式
const transformToQuestion = (item: any) => {
  return {
    id: item.id,
    title: item.title,
    description: item.summary || item.content?.slice(0, 100),
    user: {
      id: item.userId,
      username: item.author || '匿名用户',
      avatar: item.authorAvatar,
      role: item.userRole || 'student',
      verified: item.verified || false
    },
    tags: item.tags || [],
    views: item.views || 0,
    comments: item.comments || 0,
    likes: item.likes || 0,
    createdAt: item.createdAt,
    isHot: item.views > 1000,
    isRecommended: true,
    reason: 'AI 根据你的浏览记录推荐',
    rewardPoints: item.reward
  }
}

// 转换为资源卡片数据格式
const transformToResource = (item: any) => {
  return {
    id: item.id,
    title: item.title,
    description: item.description,
    fileType: getFileExtension(item.fileName || item.fileUrl || ''),
    tags: item.tags || [],
    downloads: item.downloads || 0,
    rating: item.rating || 0,
    createdAt: item.createdAt,
    points: item.points || 0
  }
}

// 转换为活动卡片数据格式
const transformToEvent = (item: any) => {
  const now = new Date()
  return {
    id: item.id,
    title: item.title,
    organizer: item.organizer || item.clubName || '社团',
    type: item.type || 'other',
    startTime: item.startTime,
    endTime: item.endTime,
    location: item.location || '待定',
    participants: item.participants || 0,
    views: item.views || 0,
    isEnded: item.endTime ? new Date(item.endTime) < now : false,
    isRegistering: item.status === 1
  }
}

// 获取文件扩展名
const getFileExtension = (filename: string): string => {
  const ext = filename.split('.').pop()?.toLowerCase() || ''
  return ext || 'pdf'
}

const handleItemClick = (item: any) => {
  emit('item-click', item)
}

const handleAnswer = (question: any) => {
  console.log('回答问题:', question)
  // TODO: 跳转到回答页面
}

const handleUserClick = (user: any) => {
  console.log('点击用户:', user)
  // TODO: 跳转到用户主页
}

const handleDownload = (resource: any) => {
  console.log('下载资源:', resource)
  // TODO: 处理下载
}

const handleRegister = (event: any) => {
  console.log('报名活动:', event)
  // TODO: 处理报名
}

const handleMetaClick = (item: any, data: any) => {
  console.log('点击元数据:', item, data)
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
  // 统一白色卡片容器（与右侧栏视觉统一）
  background: $section-card-bg;
  border: 1px solid $section-card-border;
  border-radius: $section-card-radius;
  box-shadow: $section-card-shadow;
  padding: $section-card-padding;
  // 毛玻璃效果
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-6;
  padding: 0;
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

.loading-container {
  padding: $spacing-8;
}
</style>
