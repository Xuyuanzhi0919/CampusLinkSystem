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

    <!-- Loading State：骨架屏，横向布局匹配 ClResourceCard -->
    <view v-if="loading" class="skeleton-list">
      <view v-for="i in 4" :key="i" class="skeleton-card">
        <!-- 左侧图标区 -->
        <view class="skeleton-aside">
          <view class="skeleton-file-icon"></view>
          <view class="skeleton-type-badge"></view>
        </view>
        <!-- 右侧内容区 -->
        <view class="skeleton-main">
          <!-- 顶部：标题 + 积分徽章 -->
          <view class="skeleton-top">
            <view class="skeleton-line skeleton-line--title"></view>
            <view class="skeleton-points"></view>
          </view>
          <!-- 描述 -->
          <view class="skeleton-line skeleton-line--desc"></view>
          <view class="skeleton-line skeleton-line--desc-short"></view>
          <!-- 底部 footer -->
          <view class="skeleton-footer">
            <view class="skeleton-meta">
              <view class="skeleton-line skeleton-line--meta"></view>
              <view class="skeleton-line skeleton-line--meta"></view>
            </view>
            <view class="skeleton-btn"></view>
          </view>
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
      action-type="secondary"
      @action="handleGoUpload"
    />

    <!-- Resources List -->
    <view v-else class="resources-list">
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
defineExpose({ loadData })
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

.resources-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
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

/* 骨架屏：单列横向卡片列表 */
.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-4;
}

.skeleton-card {
  background: $color-bg-card;
  border-radius: $card-radius;
  border: 1px solid $card-border;
  box-shadow: $card-shadow;
  overflow: hidden;
  display: flex;
  flex-direction: row;
  min-height: 100px;
}

/* 左侧图标区 */
.skeleton-aside {
  flex-shrink: 0;
  width: 88px;
  background: #F8F8F8;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $spacing-3;
  padding: $spacing-4 0;
}

.skeleton-file-icon {
  width: 32px;
  height: 32px;
  border-radius: $radius-base;
  @include skeleton-block;
}

.skeleton-type-badge {
  width: 36px;
  height: 18px;
  border-radius: 6px;
  @include skeleton-block;
}

/* 右侧内容区 */
.skeleton-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: $spacing-2;
  padding: $spacing-5 $spacing-6 $spacing-5 $spacing-5;
  border-left: 1px solid $color-divider;
}

/* 顶部：标题 + 积分 */
.skeleton-top {
  display: flex;
  align-items: flex-start;
  gap: $spacing-3;
}

.skeleton-points {
  flex-shrink: 0;
  width: 52px;
  height: 22px;
  border-radius: 20px;
  @include skeleton-block;
}

/* 底部 footer */
.skeleton-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
  padding-top: $spacing-3;
  border-top: 1px solid $color-divider;
}

.skeleton-meta {
  display: flex;
  gap: $spacing-5;
}

.skeleton-btn {
  width: 60px;
  height: 28px;
  border-radius: $radius-lg;
  @include skeleton-block;
}

.skeleton-line {
  @include skeleton-block;

  &--title      { flex: 1; height: 15px; }
  &--desc       { width: 100%; height: 13px; }
  &--desc-short { width: 65%; height: 13px; }
  &--meta       { width: 32px; height: 12px; }
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
