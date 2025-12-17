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
    <view v-else-if="hasError" class="error-container">
      <text>加载失败</text>
      <button @click="loadData">重试</button>
    </view>

    <!-- Empty State -->
    <view v-else-if="resourceList.length === 0" class="empty-container">
      <text>暂无资料</text>
    </view>

    <!-- Resources Grid -->
    <view v-else class="resources-grid">
      <!-- 小程序端：使用简化卡片 -->
      <!-- #ifdef MP-WEIXIN -->
      <view
        v-for="resource in resourceList"
        :key="resource.id"
        class="simple-resource-item"
        @click="handleResourceClick(resource)"
      >
        <view class="resource-header">
          <text class="resource-tag">{{ getResourceTypeText(resource.resourceType) }}</text>
          <text class="resource-rating">⭐ {{ resource.rating || 0 }}</text>
        </view>
        <view class="resource-title">{{ resource.title }}</view>
        <view class="resource-meta">
          <text>📥 {{ resource.downloadCount || 0 }}</text>
          <text>👁 {{ resource.viewCount || 0 }}</text>
          <text v-if="resource.uploaderNickname">by {{ resource.uploaderNickname }}</text>
        </view>
      </view>
      <!-- #endif -->

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

// H5 端导入企业级组件
// #ifdef H5
import { ClResourceCard } from '@/components/cl'
// #endif

import { getResourceList } from '@/services/resource'
import { requireLogin } from '@/utils/auth'

const emit = defineEmits<{
  'resource-click': [resource: any]
  'view-more': []
}>()

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
      sortBy: 'createdAt',
      order: 'desc',
      status: 1  // 仅显示已审核通过的资源
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

const handleViewMore = () => {
  emit('view-more')
}

// 小程序端辅助方法
// #ifdef MP-WEIXIN
const getResourceTypeText = (type: number) => {
  const typeMap: Record<number, string> = {
    1: '课件',
    2: '试题',
    3: '笔记',
    4: '其他'
  }
  return typeMap[type] || '资源'
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

// 小程序简化样式
/* #ifdef MP-WEIXIN */
.resources-grid {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.simple-resource-item {
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  transition: all 0.2s;

  &:active {
    transform: scale(0.98);
  }
}

.resource-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.resource-tag {
  padding: 4rpx 16rpx;
  font-size: 22rpx;
  font-weight: 500;
  color: #16A34A;
  background: rgba(22, 163, 74, 0.1);
  border-radius: 8rpx;
}

.resource-rating {
  font-size: 24rpx;
  color: #F59E0B;
  font-weight: 500;
}

.resource-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #0F172A;
  line-height: 1.5;
  margin-bottom: 12rpx;

  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
}

.resource-meta {
  display: flex;
  align-items: center;
  gap: 24rpx;
  font-size: 24rpx;
  color: #94A3B8;

  text {
    display: flex;
    align-items: center;
    gap: 4rpx;
  }
}
/* #endif */

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
