<template>
  <view class="my-resource-card" :class="statusClass" @click="handleClick">
    <!-- 状态条（审核状态） -->
    <view v-if="showStatus" class="status-bar" :class="`status-${resource.status}`">
      <view class="status-indicator">
        <view class="status-dot" />
        <text class="status-text">{{ statusText }}</text>
      </view>
    </view>

    <!-- 卡片主体 -->
    <view class="card-body">
      <!-- 左侧：文件图标 -->
      <view class="file-icon-wrap" :style="{ background: fileTypeConfig.bgColor }">
        <Icon :name="fileTypeConfig.icon" :size="28" :style="{ color: fileTypeConfig.color }" />
      </view>

      <!-- 右侧：内容区 -->
      <view class="content-area">
        <!-- 标题行 -->
        <view class="title-row">
          <text class="title">{{ resource.title }}</text>
          <view class="file-type-tag" :style="{ background: fileTypeConfig.bgColor, color: fileTypeConfig.color }">
            {{ fileTypeConfig.name }}
          </view>
        </view>

        <!-- 描述（可选） -->
        <text v-if="resource.description" class="description">
          {{ truncateText(resource.description, 50) }}
        </text>

        <!-- 底部信息行 -->
        <view class="meta-row">
          <!-- 左侧：积分 -->
          <view class="points-badge" :class="{ free: !resource.score }">
            <text v-if="resource.score">{{ resource.score }} 积分</text>
            <text v-else>免费</text>
          </view>

          <!-- 右侧：统计数据 -->
          <view class="stats">
            <view class="stat-item">
              <Icon name="download" :size="14" class="stat-icon" />
              <text>{{ formatNumber(resource.downloads) }}</text>
            </view>
            <view v-if="resource.averageRating" class="stat-item rating">
              <Icon name="star" :size="14" class="stat-icon" />
              <text>{{ resource.averageRating.toFixed(1) }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 操作按钮（可选） -->
    <view v-if="showActions" class="card-actions">
      <view class="action-btn delete" @click.stop="handleDelete">
        <Icon name="trash-2" :size="16" />
        <text>删除</text>
      </view>
      <view class="action-btn primary" @click.stop="handleEdit">
        <Icon name="edit-3" :size="16" />
        <text>编辑</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Icon from '@/components/icons/index.vue'
import { getFileTypeIcon } from '@/config/icons'
import type { ResourceItem } from '@/types/resource'

interface Props {
  resource: ResourceItem
  showStatus?: boolean
  showActions?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showStatus: true,
  showActions: false
})

const emit = defineEmits<{
  click: [resource: ResourceItem]
  delete: [resource: ResourceItem]
  edit: [resource: ResourceItem]
}>()

// 状态样式类
const statusClass = computed(() => ({
  'is-pending': props.resource.status === 0,
  'is-approved': props.resource.status === 1,
  'is-rejected': props.resource.status === 2
}))

// 状态文本
const statusText = computed(() => {
  const statusMap: Record<number, string> = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝'
  }
  return statusMap[props.resource.status ?? 1] || '未知'
})

// 文件类型配置
const fileTypeConfig = computed(() => {
  const config = getFileTypeIcon(props.resource.fileType)
  const colorMap: Record<string, { color: string; bgColor: string; name: string }> = {
    '#E74C3C': { color: '#E74C3C', bgColor: 'rgba(231, 76, 60, 0.1)', name: 'PDF' },
    '#2B579A': { color: '#2B579A', bgColor: 'rgba(43, 87, 154, 0.1)', name: 'Word' },
    '#D24726': { color: '#D24726', bgColor: 'rgba(210, 71, 38, 0.1)', name: 'PPT' },
    '#1D6F42': { color: '#1D6F42', bgColor: 'rgba(29, 111, 66, 0.1)', name: 'Excel' },
    '#F39C12': { color: '#F39C12', bgColor: 'rgba(243, 156, 18, 0.1)', name: 'ZIP' },
    '#7F8C8D': { color: '#7F8C8D', bgColor: 'rgba(127, 140, 141, 0.1)', name: '其他' }
  }
  return { ...colorMap[config.color] || colorMap['#7F8C8D'], icon: config.icon }
})

// 格式化数字
const formatNumber = (num: number): string => {
  if (num >= 10000) return `${(num / 10000).toFixed(1)}w`
  if (num >= 1000) return `${(num / 1000).toFixed(1)}k`
  return String(num)
}

// 截断文本
const truncateText = (text: string, maxLength: number): string => {
  if (!text) return ''
  return text.length > maxLength ? text.slice(0, maxLength) + '...' : text
}

// 事件处理
const handleClick = () => emit('click', props.resource)
const handleDelete = () => emit('delete', props.resource)
const handleEdit = () => emit('edit', props.resource)
</script>

<style lang="scss" scoped>
.my-resource-card {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.98);
  }

  // 状态条
  .status-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16rpx 24rpx;
    font-size: 24rpx;

    &.status-0 {
      background: linear-gradient(135deg, rgba(251, 191, 36, 0.15), rgba(251, 191, 36, 0.05));
      .status-dot { background: #FBBF24; }
      .status-text { color: #D97706; }
    }

    &.status-1 {
      background: linear-gradient(135deg, rgba(34, 197, 94, 0.15), rgba(34, 197, 94, 0.05));
      .status-dot { background: #22C55E; }
      .status-text { color: #16A34A; }
    }

    &.status-2 {
      background: linear-gradient(135deg, rgba(239, 68, 68, 0.15), rgba(239, 68, 68, 0.05));
      .status-dot { background: #EF4444; }
      .status-text { color: #DC2626; }
    }

    .status-indicator {
      display: flex;
      align-items: center;
      gap: 12rpx;
    }

    .status-dot {
      width: 12rpx;
      height: 12rpx;
      border-radius: 50%;
    }

    .reject-reason {
      color: #DC2626;
      font-size: 22rpx;
    }
  }

  // 卡片主体
  .card-body {
    display: flex;
    padding: 24rpx;
    gap: 24rpx;
  }

  // 文件图标
  .file-icon-wrap {
    flex-shrink: 0;
    width: 96rpx;
    height: 96rpx;
    border-radius: 16rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  // 内容区
  .content-area {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 12rpx;
  }

  // 标题行
  .title-row {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16rpx;

    .title {
      flex: 1;
      font-size: 30rpx;
      font-weight: 600;
      color: #1f2937;
      line-height: 1.4;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }

    .file-type-tag {
      flex-shrink: 0;
      padding: 6rpx 16rpx;
      border-radius: 8rpx;
      font-size: 22rpx;
      font-weight: 500;
    }
  }

  // 描述
  .description {
    font-size: 26rpx;
    color: #6b7280;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 1;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  // 底部信息行
  .meta-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 8rpx;
  }

  // 积分徽章
  .points-badge {
    padding: 8rpx 20rpx;
    border-radius: 24rpx;
    font-size: 24rpx;
    font-weight: 500;
    background: linear-gradient(135deg, rgba(251, 191, 36, 0.2), rgba(251, 191, 36, 0.1));
    color: #D97706;

    &.free {
      background: rgba(34, 197, 94, 0.1);
      color: #16A34A;
    }
  }

  // 统计数据
  .stats {
    display: flex;
    align-items: center;
    gap: 24rpx;

    .stat-item {
      display: flex;
      align-items: center;
      gap: 8rpx;
      font-size: 24rpx;
      color: #9ca3af;

      .stat-icon {
        color: #9ca3af;
      }

      &.rating {
        color: #F59E0B;
        .stat-icon { color: #F59E0B; }
      }
    }
  }

  // 操作按钮
  .card-actions {
    display: flex;
    border-top: 1rpx solid #f3f4f6;
    padding: 20rpx 24rpx;
    gap: 20rpx;

    .action-btn {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12rpx;
      padding: 20rpx;
      border-radius: 12rpx;
      font-size: 26rpx;
      font-weight: 500;
      transition: all 0.2s;

      &.delete {
        background: rgba(239, 68, 68, 0.08);
        color: #EF4444;

        &:active {
          background: rgba(239, 68, 68, 0.15);
        }
      }

      &.primary {
        background: #6366f1;
        color: #fff;

        &:active {
          background: #4f46e5;
        }
      }
    }
  }

  // 状态变体
  &.is-pending {
    border-left: 6rpx solid #FBBF24;
  }

  &.is-approved {
    border-left: 6rpx solid #22C55E;
  }

  &.is-rejected {
    border-left: 6rpx solid #EF4444;
  }
}

// 桌面端适配
@media (min-width: 768px) {
  .my-resource-card {
    .card-body {
      padding: 32rpx;
    }

    .file-icon-wrap {
      width: 120rpx;
      height: 120rpx;
    }

    .title-row .title {
      font-size: 32rpx;
    }

    .card-actions .action-btn {
      padding: 24rpx 32rpx;
    }
  }
}
</style>
