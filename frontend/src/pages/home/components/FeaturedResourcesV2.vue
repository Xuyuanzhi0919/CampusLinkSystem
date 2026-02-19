<template>
  <view class="featured-resources-v2">
    <!-- Section Header -->
    <view class="section-header">
      <view class="header-left">
        <text class="section-title">精选资料</text>
        <text class="section-subtitle">优质学习资源分享</text>
      </view>
      <view class="view-more" @click="handleViewMore">
        <text class="more-text">查看更多</text>
        <text class="more-arrow">→</text>
      </view>
    </view>

    <!-- Loading State：骨架屏，2×2 网格匹配 ClResourceCard 结构 -->
    <view v-if="loading" class="skeleton-grid">
      <view v-for="i in 4" :key="i" class="skeleton-card">
        <!-- 顶部色条 -->
        <view class="skeleton-type-bar"></view>
        <!-- Header：文件图标 + 类型胶囊 -->
        <view class="skeleton-header">
          <view class="skeleton-file-icon"></view>
          <view class="skeleton-capsule"></view>
        </view>
        <!-- Body：标题 + 描述 + 标签 -->
        <view class="skeleton-body">
          <view class="skeleton-line skeleton-line--title"></view>
          <view class="skeleton-line skeleton-line--title-short"></view>
          <view class="skeleton-line skeleton-line--desc"></view>
          <view class="skeleton-tags">
            <view class="skeleton-tag"></view>
            <view class="skeleton-tag"></view>
          </view>
        </view>
        <!-- Meta + 下载按钮 -->
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
      pageSize: 4,
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
  grid-template-columns: repeat(2, 1fr);
  gap: $spacing-6;

  /* 移动端单列 */
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
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
  background: #F0F0F0;
  border-radius: 0;
  margin-bottom: $spacing-4;
}

.skeleton-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.skeleton-file-icon {
  width: 40px;
  height: 40px;
  border-radius: $radius-base;
  @include skeleton-block;
}

.skeleton-capsule {
  width: 48px;
  height: 20px;
  border-radius: 10px;
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
  width: 44px;
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

  &--title       { width: 85%; height: 16px; }
  &--title-short { width: 60%; height: 16px; }
  &--desc        { width: 100%; height: 13px; }
  &--meta        { width: 36px; height: 13px; }
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
