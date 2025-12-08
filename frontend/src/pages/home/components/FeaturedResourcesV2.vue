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
      <gp-skeleton :loading="true" type="card" :count="4" />
    </view>

    <!-- Error State (使用统一企业级组件) -->
    <ClError
      v-else-if="hasError"
      type="network"
      size="compact"
      variant="card"
      @retry="loadData"
    />

    <!-- Empty State (使用统一企业级组件) -->
    <ClEmpty
      v-else-if="resourceList.length === 0"
      type="resource"
      size="compact"
      variant="card"
      :show-action="true"
      action-text="去上传"
      action-icon="plus"
      @action="handleViewMore"
    />

    <!-- Resources Grid (使用企业级卡片) -->
    <view v-else class="resources-grid">
      <ClResourceCard
        v-for="resource in resourceList"
        :key="resource.id"
        :resource="resource"
        @click="handleResourceClick"
        @download="handleDownload"
        @meta-click="handleMetaClick"
      />
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ClResourceCard, ClEmpty, ClError } from '@/components/cl'
import { getResourceList } from '@/services/resource'

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

    // 转换数据格式为 ClResourceCard 需要的格式
    resourceList.value = response.list.map((item: any) => ({
      id: item.id,
      title: item.title,
      description: item.description,
      fileType: getFileExtension(item.fileName || item.fileUrl || ''),
      tags: item.tags || [],
      downloads: item.downloads || 0,
      rating: item.rating || 0,
      createdAt: item.createdAt,
      points: item.points || 0
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
  emit('resource-click', resource)
}

const handleDownload = (resource: any) => {
  console.log('下载资源:', resource)
  // TODO: 处理下载逻辑（积分扣除、下载链接等）
}

const handleMetaClick = (item: any, resource: any) => {
  console.log('点击元数据:', item, resource)
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

.loading-container {
  padding: $spacing-8;
}
</style>
