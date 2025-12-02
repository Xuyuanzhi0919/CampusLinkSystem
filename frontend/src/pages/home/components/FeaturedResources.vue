<template>
  <view class="featured-resources">
    <!-- 装饰元素 -->
    <view class="section-decoration">
      <text class="deco-emoji left">📚</text>
      <text class="deco-emoji right">✨</text>
    </view>

    <view class="section-header">
      <view class="title-wrap">
        <text class="section-title">精选资料</text>
        <view class="share-badge">
          <text class="badge-emoji">🔥</text>
          <text class="badge-text">同学们都在看</text>
        </view>
      </view>
      <view class="view-more" @click="handleViewMore">
        <text class="more-text">发现更多</text>
        <svg class="more-arrow" width="12" height="12" viewBox="0 0 24 24" fill="none">
          <path d="M5 12H19M19 12L12 5M19 12L12 19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
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
          <!-- 课件图标 -->
          <svg v-if="item.category === 'course'" class="cover-icon" width="32" height="32" viewBox="0 0 24 24" fill="none">
            <path d="M4 19.5V4.5C4 3.67157 4.67157 3 5.5 3H18.5C19.3284 3 20 3.67157 20 4.5V19.5C20 20.3284 19.3284 21 18.5 21H5.5C4.67157 21 4 20.3284 4 19.5Z" stroke="currentColor" stroke-width="2"/>
            <path d="M8 7H16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <path d="M8 11H16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <path d="M8 15H12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <!-- 试题图标 -->
          <svg v-else-if="item.category === 'exam'" class="cover-icon" width="32" height="32" viewBox="0 0 24 24" fill="none">
            <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M14 2V8H20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M9 15L11 17L15 13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <!-- 笔记图标 -->
          <svg v-else-if="item.category === 'notes'" class="cover-icon" width="32" height="32" viewBox="0 0 24 24" fill="none">
            <path d="M12 20H21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M16.5 3.50001C16.8978 3.10219 17.4374 2.87869 18 2.87869C18.2786 2.87869 18.5544 2.93356 18.8118 3.04017C19.0692 3.14677 19.303 3.30303 19.5 3.50001C19.697 3.697 19.8532 3.93085 19.9598 4.18822C20.0665 4.44559 20.1213 4.72144 20.1213 5.00001C20.1213 5.27859 20.0665 5.55444 19.9598 5.81181C19.8532 6.06918 19.697 6.30303 19.5 6.50001L7 19L3 20L4 16L16.5 3.50001Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <!-- 代码图标 -->
          <svg v-else class="cover-icon" width="32" height="32" viewBox="0 0 24 24" fill="none">
            <path d="M16 18L22 12L16 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M8 6L2 12L8 18" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
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
              <svg class="stat-icon" width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M21 15V19C21 20.1046 20.1046 21 19 21H5C3.89543 21 3 20.1046 3 19V15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M7 10L12 15L17 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M12 15V3" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <text class="stat-value">{{ item.downloads }}</text>
            </view>
            <view class="stat-item">
              <svg class="stat-icon" width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
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

// 图标现在直接在模板中使用 SVG

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
  margin-bottom: $module-gap-md;
  padding: 40px 0;
  position: relative;
  // 使用青绿色调（$campus-teal）作为资料区调味色
  background: linear-gradient(180deg, transparent 0%, rgba($campus-teal, 0.04) 30%, rgba($campus-teal, 0.06) 60%, transparent 100%);
  border-radius: $campus-radius-lg;

  // 装饰光斑
  &::before {
    content: '';
    position: absolute;
    top: 10%;
    right: 5%;
    width: 150px;
    height: 150px;
    background: radial-gradient(circle, rgba($campus-teal, 0.08) 0%, transparent 70%);
    border-radius: 50%;
    pointer-events: none;
  }
}

// 装饰emoji
.section-decoration {
  position: absolute;
  top: 24px;
  left: 0;
  right: 0;
  pointer-events: none;

  .deco-emoji {
    position: absolute;
    font-size: 20px;
    opacity: 0.5;
    animation: floatEmoji 3s ease-in-out infinite;

    &.left {
      left: 5%;
      animation-delay: 0s;
    }

    &.right {
      right: 5%;
      animation-delay: 1.5s;
    }
  }
}

@keyframes floatEmoji {
  0%, 100% { transform: translateY(0) rotate(-3deg); }
  50% { transform: translateY(-6px) rotate(3deg); }
}

.section-header {
  @include section-header;
  position: relative;
  z-index: 1;
}

.title-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-title {
  @include heading-h2;
}

.share-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: linear-gradient(135deg, rgba($campus-teal, 0.12), rgba($campus-teal, 0.2));
  border-radius: $campus-radius-sm;

  .badge-emoji {
    font-size: 12px;
  }

  .badge-text {
    font-size: 11px;
    font-weight: 500;
    color: $campus-teal;
  }
}

.view-more {
  @include view-more-link;
  color: $campus-teal;

  &:hover {
    color: darken($campus-teal, 10%);
  }
}

.resources-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $module-gap-sm;

  @media (max-width: 1200px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @include mobile {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}

.resource-card {
  background: $white;
  border-radius: $campus-radius;
  box-shadow: $campus-shadow-card;
  overflow: hidden;
  cursor: pointer;
  height: 240px;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: $campus-shadow-hover;

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
      height: 100px;
      flex-shrink: 0;
      background: linear-gradient(90deg, $gray-100 25%, $gray-50 50%, $gray-100 75%);
      background-size: 200% 100%;
      animation: shimmer 1.5s infinite;
    }

    .skeleton-content {
      padding: 16px;
      flex: 1;
    }

    .skeleton-title {
      height: 20px;
      width: 80%;
      background: linear-gradient(90deg, $gray-100 25%, $gray-50 50%, $gray-100 75%);
      background-size: 200% 100%;
      animation: shimmer 1.5s infinite;
      border-radius: $campus-radius-sm;
      margin-bottom: 8px;
    }

    .skeleton-meta {
      height: 16px;
      width: 50%;
      background: linear-gradient(90deg, $gray-100 25%, $gray-50 50%, $gray-100 75%);
      background-size: 200% 100%;
      animation: shimmer 1.5s infinite;
      border-radius: $campus-radius-sm;
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
  height: 100px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: $gray-50;

  // 课件 - 使用主色蓝
  &.course {
    background: rgba($campus-blue, 0.08);
    color: $campus-blue;
  }

  // 试题 - 使用强调色橙
  &.exam {
    background: rgba($campus-amber, 0.1);
    color: $campus-amber;
  }

  // 笔记 - 使用辅助色青绿
  &.notes {
    background: rgba($campus-teal, 0.1);
    color: $campus-teal;
  }

  // 代码 - 使用信息色紫
  &.code {
    background: rgba($campus-violet, 0.1);
    color: $campus-violet;
  }

  .cover-icon {
    width: 32px;
    height: 32px;
    transition: $transition-base;
  }

  .cover-badge {
    position: absolute;
    top: 8px;
    right: 8px;
    padding: 2px 8px;
    background: rgba($black, 0.6);
    border-radius: $campus-radius-sm;

    .badge-text {
      font-size: 11px;
      color: $white;
      font-weight: $font-weight-medium;
    }
  }
}

.resource-info {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.resource-title {
  display: block;
  font-size: 16px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  line-height: 1.4;
  margin-bottom: 6px;
  @include text-ellipsis(1);
}

.resource-desc {
  display: block;
  font-size: 14px;
  color: $text-tertiary;
  line-height: 1.5;
  margin-bottom: 12px;
  height: 42px;
  @include text-ellipsis(2);
}

.resource-stats {
  display: flex;
  gap: 12px;
  margin-top: auto;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;

  .stat-icon {
    width: 14px;
    height: 14px;
    color: $text-quaternary;
  }

  .stat-value {
    font-size: 12px;
    color: $text-quaternary;
  }
}

.resource-price {
  margin-left: auto;

  .price-free {
    font-size: 13px;
    color: $campus-teal;
    font-weight: $font-weight-medium;
  }

  .price-points {
    font-size: 13px;
    color: $campus-amber;
    font-weight: $font-weight-medium;
  }
}
</style>
