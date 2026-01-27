<template>
  <view class="featured-resources-v2">
    <!-- Section Header -->
    <view class="section-header">
      <text class="section-title">精选资料</text>
      <view class="view-more" @click="handleViewMore">
        <text class="more-text">查看更多</text>
        <text class="more-arrow">→</text>
      </view>
    </view>

    <!-- Loading State -->
    <view v-if="loading" class="loading-container">
      <text>加载中...</text>
    </view>

    <!-- Error State -->
    <EmptyState
      v-else-if="hasError"
      type="error"
      size="medium"
      title="资料加载失败"
      description="服务暂时不可用，请稍后重试"
      action-text="重试"
      action-type="secondary"
      @action="loadData"
    />

    <!-- Empty State -->
    <EmptyState
      v-else-if="resourceList.length === 0"
      type="empty"
      size="medium"
      title="还没人上传资料"
      description="成为第一个分享者，帮助同学"
      action-text="上传资料"
      action-type="primary"
      @action="handleGoUpload"
    />

    <!-- Resources Grid -->
    <view v-else class="resources-grid">
      <!-- H5 端：使用企业级卡片 -->
      <!-- #ifdef H5 -->
      <ClResourceCard
        v-for="resource in resourceList"
        :key="resource.id"
        :resource="resource"
        @click="handleResourceClick"
        @download="handleDownload"
        @meta-click="handleMetaClick"
      />
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
import { ClResourceCard } from '@/components/cl'
// #endif

import { getResourceList } from '@/services/resource'
import { requireLogin } from '@/utils/auth'
import { useNavigation } from '@/composables/useNavigation'

const emit = defineEmits<{
  'resource-click': [resource: any]
  'view-more': []
}>()

const nav = useNavigation()

const loading = ref(true)
const hasError = ref(false)
const resourceList = ref<any[]>([])

// 加载数据
const loadData = async () => {
  try {
    loading.value = true
    hasError.value = false

    const response = await getResourceList({
      page: 1,
      pageSize: 6,
      sortBy: 'created_at',
      sortOrder: 'desc'
    })

    /**
     * 转换数据格式为 ClResourceCard 需要的格式
     * 后端实际返回字段：resourceId, title, description, uploaderName, uploaderAvatar, fileType, fileSize, category, courseName, averageRating, downloads, likes, views, favorites, status, createdAt
     */
    resourceList.value = response.list.map((item: any) => ({
      id: item.resourceId || item.id,
      title: item.title || '',
      description: item.description || '',
      fileType: item.fileType || getFileExtension(item.fileName || item.fileUrl || ''),
      tags: item.tags || [],
      downloads: item.downloads || 0,
      views: item.views || 0,
      favorites: item.favorites || 0,
      rating: item.averageRating || 0,  // 使用平均评分，score是积分要求不是评分
      createdAt: item.createdAt || '',
      points: item.score || item.pointsCost || item.points || 0  // score是下载所需积分
    }))
  } catch (error) {
    console.error('加载资源失败:', error)
    hasError.value = true
  } finally {
    loading.value = false
  }
}

// 获取文件扩展名
const getFileExtension = (filename: string): string => {
  const ext = filename.split('.').pop()?.toLowerCase() || ''
  return ext || 'pdf'
}

const handleResourceClick = (resource: any) => {
  if (!resource?.id) {
    console.warn('资源 ID 无效:', resource)
    return
  }
  emit('resource-click', resource)
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

const handleMetaClick = (item: any, resource: any) => {
  console.log('点击元数据:', item, resource)
}

const handleGoUpload = () => {
  nav.toPublish()
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

.featured-resources-v2 {
  width: 100%;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-8;
  padding: 0 $spacing-2;
}

.section-title {
  font-size: $font-size-2xl;
  font-weight: $font-weight-semibold;
  color: $color-text-primary;
}

.view-more {
  display: flex;
  align-items: center;
  gap: $spacing-2;
  padding: $spacing-3 $spacing-5;
  border-radius: $radius-md;
  background: transparent;
  color: $campus-blue;
  font-size: $font-size-sm;
  cursor: pointer;
  transition: $transition-bg;

  &:hover {
    background: $campus-blue-lighter;
  }
}

.more-text {
  font-weight: $font-weight-medium;
}

.more-arrow {
  font-size: $font-size-lg;
}

.resources-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
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
</style>
