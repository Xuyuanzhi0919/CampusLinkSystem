<template>
  <view class="featured-resources">
    <view class="section-header">
      <text class="section-title">精选资料</text>
      <view class="view-more" @click="handleViewMore">
        <text class="more-text">查看更多</text>
        <text class="more-arrow">→</text>
      </view>
    </view>

    <!-- 骨架屏 -->
    <view v-if="loading" class="resources-grid">
      <view v-for="i in 3" :key="i" class="resource-card skeleton">
        <view class="skeleton-cover"></view>
        <view class="skeleton-content">
          <view class="skeleton-title"></view>
          <view class="skeleton-meta"></view>
        </view>
      </view>
    </view>

    <!-- 资料列表 -->
    <view v-else class="resources-grid">
      <view
        v-for="item in resourceList"
        :key="item.id"
        class="resource-card"
        @click="handleResourceClick(item)"
      >
        <!-- 封面图 -->
        <view class="resource-cover" :class="item.category">
          <text class="cover-icon">{{ getCategoryIcon(item.category) }}</text>
          <view class="cover-badge">
            <text class="badge-text">{{ item.format }}</text>
          </view>
        </view>

        <!-- 资料信息 -->
        <view class="resource-info">
          <text class="resource-title">{{ item.title }}</text>
          <text class="resource-desc">{{ item.description }}</text>

          <!-- 统计数据 -->
          <view class="resource-stats">
            <view class="stat-item">
              <text class="stat-icon">⬇️</text>
              <text class="stat-value">{{ item.downloads }}</text>
            </view>
            <view class="stat-item">
              <text class="stat-icon">⭐</text>
              <text class="stat-value">{{ item.rating }}</text>
            </view>
          </view>

          <!-- 价格/积分 -->
          <view class="resource-price">
            <text v-if="item.points === 0" class="price-free">免费</text>
            <text v-else class="price-points">{{ item.points }} 积分</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface Resource {
  id: number
  title: string
  description: string
  category: string
  format: string
  downloads: number
  rating: number
  points: number
}

const emit = defineEmits<{
  (e: 'resource-click', item: Resource): void
  (e: 'view-more'): void
}>()

const loading = ref(true)
const resourceList = ref<Resource[]>([])

// 获取分类图标
const getCategoryIcon = (category: string) => {
  const icons: Record<string, string> = {
    'course': '📖',
    'exam': '📝',
    'notes': '📒',
    'code': '💻'
  }
  return icons[category] || '📄'
}

const handleResourceClick = (item: Resource) => {
  emit('resource-click', item)
}

const handleViewMore = () => {
  emit('view-more')
}

// 模拟数据加载
const loadData = async () => {
  loading.value = true

  await new Promise(resolve => setTimeout(resolve, 700))

  resourceList.value = [
    {
      id: 1,
      title: '操作系统期末复习精华',
      description: '涵盖进程管理、内存管理、文件系统等核心章节',
      category: 'notes',
      format: 'PDF',
      downloads: 1234,
      rating: 4.8,
      points: 0
    },
    {
      id: 2,
      title: '数据库原理实验报告模板',
      description: 'MySQL 实验报告完整模板，包含 SQL 语句示例',
      category: 'exam',
      format: 'DOCX',
      downloads: 856,
      rating: 4.5,
      points: 5
    },
    {
      id: 3,
      title: 'Spring Boot 项目实战代码',
      description: '完整的电商项目后端代码，含详细注释',
      category: 'code',
      format: 'ZIP',
      downloads: 2341,
      rating: 4.9,
      points: 10
    }
  ]

  loading.value = false
}

defineExpose({ loadData })

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.featured-resources {
  margin-bottom: $sp-12;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $sp-6;
}

.section-title {
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.view-more {
  display: flex;
  align-items: center;
  gap: 4rpx;
  cursor: pointer;
  transition: $transition-fast;

  &:hover {
    .more-text,
    .more-arrow {
      color: $primary;
    }

    .more-arrow {
      transform: translateX(4rpx);
    }
  }

  .more-text {
    font-size: $font-size-sm;
    color: $text-tertiary;
    transition: $transition-fast;
  }

  .more-arrow {
    font-size: $font-size-sm;
    color: $text-tertiary;
    transition: $transition-fast;
  }
}

.resources-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $sp-6;

  @media (max-width: 1200px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @include mobile {
    grid-template-columns: 1fr;
    gap: $sp-4;
  }
}

.resource-card {
  background: $bg-surface;
  border-radius: $radius-md;
  overflow: hidden;
  cursor: pointer;
  transition: $transition-base;
  border: 1px solid $border-light;

  &:hover {
    border-color: $primary-200;
    box-shadow: $shadow-md;
    transform: translateY(-4rpx);

    .resource-cover {
      .cover-icon {
        transform: scale(1.1);
      }
    }
  }

  // 骨架屏
  &.skeleton {
    pointer-events: none;

    .skeleton-cover {
      height: 160rpx;
      background: linear-gradient(90deg, $gray-100 25%, $gray-50 50%, $gray-100 75%);
      background-size: 200% 100%;
      animation: shimmer 1.5s infinite;
    }

    .skeleton-content {
      padding: $sp-5;
    }

    .skeleton-title {
      height: 32rpx;
      width: 80%;
      background: linear-gradient(90deg, $gray-100 25%, $gray-50 50%, $gray-100 75%);
      background-size: 200% 100%;
      animation: shimmer 1.5s infinite;
      border-radius: $radius-xs;
      margin-bottom: 12rpx;
    }

    .skeleton-meta {
      height: 24rpx;
      width: 50%;
      background: linear-gradient(90deg, $gray-100 25%, $gray-50 50%, $gray-100 75%);
      background-size: 200% 100%;
      animation: shimmer 1.5s infinite;
      border-radius: $radius-xs;
    }
  }
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.resource-cover {
  height: 160rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;

  &.course {
    background: linear-gradient(135deg, $primary-100, $primary-50);
  }

  &.exam {
    background: linear-gradient(135deg, $warning-100, $warning-50);
  }

  &.notes {
    background: linear-gradient(135deg, $success-100, $success-50);
  }

  &.code {
    background: linear-gradient(135deg, $info-100, $info-50);
  }

  .cover-icon {
    font-size: 56rpx;
    transition: $transition-base;
  }

  .cover-badge {
    position: absolute;
    top: 12rpx;
    right: 12rpx;
    padding: 4rpx 12rpx;
    background: rgba($black, 0.6);
    border-radius: $radius-xs;

    .badge-text {
      font-size: $font-size-xs;
      color: $white;
      font-weight: $font-weight-medium;
    }
  }
}

.resource-info {
  padding: $sp-5;
}

.resource-title {
  display: block;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $text-primary;
  line-height: $line-height-snug;
  margin-bottom: 8rpx;
  @include text-ellipsis(1);
}

.resource-desc {
  display: block;
  font-size: $font-size-sm;
  color: $text-tertiary;
  line-height: $line-height-normal;
  margin-bottom: 12rpx;
  @include text-ellipsis(2);
}

.resource-stats {
  display: flex;
  gap: 16rpx;
  margin-bottom: 12rpx;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4rpx;

  .stat-icon {
    font-size: 20rpx;
  }

  .stat-value {
    font-size: $font-size-xs;
    color: $text-quaternary;
  }
}

.resource-price {
  .price-free {
    font-size: $font-size-sm;
    color: $success;
    font-weight: $font-weight-medium;
  }

  .price-points {
    font-size: $font-size-sm;
    color: $accent;
    font-weight: $font-weight-medium;
  }
}
</style>
