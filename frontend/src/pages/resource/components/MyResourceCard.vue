<template>
  <view class="my-resource-card" :class="statusClass" @click="handleClick">
    <!-- 卡片主体 -->
    <view class="card-main">
      <!-- 左侧：文件类型图标 -->
      <view class="file-icon" :style="{ background: fileTypeConfig.bgColor }">
        <Icon :name="fileTypeConfig.icon" :size="28" :color="fileTypeConfig.color" />
      </view>

      <!-- 右侧：内容区 -->
      <view class="card-content">
        <!-- 标题 + 审核状态 -->
        <view class="title-row">
          <text class="title">{{ resource.title }}</text>
          <view v-if="showStatus" class="status-badge" :class="`status-${resource.status}`">
            <view class="status-dot" />
            <text class="status-text">{{ statusText }}</text>
          </view>
        </view>

        <!-- 描述 -->
        <text v-if="resource.description" class="desc">{{ resource.description }}</text>

        <!-- 元信息行 -->
        <view class="meta-row">
          <!-- 文件类型标签 -->
          <view class="file-tag" :style="{ color: fileTypeConfig.color, background: fileTypeConfig.bgColor }">
            <text>{{ fileTypeConfig.name }}</text>
          </view>

          <view class="meta-stats">
            <!-- 下载数 -->
            <view class="stat">
              <Icon name="download" :size="12" color="#9CA3AF" />
              <text>{{ formatNumber(resource.downloads) }}</text>
            </view>
            <!-- 评分 -->
            <view v-if="resource.averageRating" class="stat rating">
              <Icon name="star" :size="12" color="#F59E0B" />
              <text>{{ resource.averageRating.toFixed(1) }}</text>
            </view>
            <!-- 积分 -->
            <view class="points-badge" :class="{ free: !resource.score }">
              <text>{{ resource.score ? `${resource.score} 分` : '免费' }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 操作按钮（仅上传列表） -->
    <view v-if="showActions" class="card-actions">
      <view class="action-btn btn-delete" @click.stop="handleDelete">
        <Icon name="trash-2" :size="14" />
        <text>删除</text>
      </view>
      <view class="action-btn btn-edit" @click.stop="handleEdit">
        <Icon name="edit-3" :size="14" />
        <text>编辑</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Icon from '@/components/icons/index.vue'
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

// 状态类
const statusClass = computed(() => ({
  'is-pending':  props.resource.status === 0,
  'is-approved': props.resource.status === 1,
  'is-rejected': props.resource.status === 2
}))

// 状态文本
const statusText = computed(() => {
  const map: Record<number, string> = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
  return map[props.resource.status ?? 1] || '未知'
})

// 文件类型配置（直接使用 Lucide 图标名）
const fileTypeConfig = computed(() => {
  const type = (props.resource.fileType || 'default').toLowerCase()
  const configs: Record<string, { icon: string; color: string; bgColor: string; name: string }> = {
    pdf:  { icon: 'file-text', color: '#E74C3C', bgColor: 'rgba(231,76,60,0.1)',  name: 'PDF'   },
    doc:  { icon: 'file-text', color: '#2B579A', bgColor: 'rgba(43,87,154,0.1)',  name: 'Word'  },
    docx: { icon: 'file-text', color: '#2B579A', bgColor: 'rgba(43,87,154,0.1)',  name: 'Word'  },
    ppt:  { icon: 'file-text', color: '#D24726', bgColor: 'rgba(210,71,38,0.1)',  name: 'PPT'   },
    pptx: { icon: 'file-text', color: '#D24726', bgColor: 'rgba(210,71,38,0.1)',  name: 'PPT'   },
    xls:  { icon: 'file-text', color: '#1D6F42', bgColor: 'rgba(29,111,66,0.1)',  name: 'Excel' },
    xlsx: { icon: 'file-text', color: '#1D6F42', bgColor: 'rgba(29,111,66,0.1)',  name: 'Excel' },
    zip:  { icon: 'folder',    color: '#F39C12', bgColor: 'rgba(243,156,18,0.1)', name: 'ZIP'   },
    rar:  { icon: 'folder',    color: '#F39C12', bgColor: 'rgba(243,156,18,0.1)', name: 'RAR'   },
    txt:  { icon: 'file-text', color: '#7F8C8D', bgColor: 'rgba(127,140,141,0.1)', name: 'TXT'  },
  }
  return configs[type] || { icon: 'file-text', color: '#7F8C8D', bgColor: 'rgba(127,140,141,0.1)', name: '文档' }
})

// 格式化数字
const formatNumber = (num: number): string => {
  if (num >= 10000) return `${(num / 10000).toFixed(1)}w`
  if (num >= 1000)  return `${(num / 1000).toFixed(1)}k`
  return String(num || 0)
}

const handleClick  = () => emit('click',  props.resource)
const handleDelete = () => emit('delete', props.resource)
const handleEdit   = () => emit('edit',   props.resource)
</script>

<style lang="scss" scoped>
.my-resource-card {
  background: #fff;
  border-radius: 20rpx;
  overflow: hidden;
  margin-bottom: 24rpx;
  border-left: 5rpx solid #E5E7EB;
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.05);
  transition: transform 0.15s ease;

  &:active { transform: scale(0.985); }

  // 审核状态左边框
  &.is-pending  { border-left-color: #FBBF24; }
  &.is-approved { border-left-color: #22C55E; }
  &.is-rejected { border-left-color: #EF4444; }

  // ---- 主体 ----
  .card-main {
    display: flex;
    align-items: flex-start;
    padding: 28rpx 28rpx 24rpx;
    gap: 20rpx;
  }

  .file-icon {
    flex-shrink: 0;
    width: 88rpx;
    height: 88rpx;
    border-radius: 18rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-top: 2rpx;
  }

  .card-content {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 10rpx;
  }

  // ---- 标题行 ----
  .title-row {
    display: flex;
    align-items: flex-start;
    gap: 12rpx;

    .title {
      flex: 1;
      font-size: 30rpx;
      font-weight: 600;
      color: #111827;
      line-height: 1.4;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
  }

  // ---- 审核状态徽章 ----
  .status-badge {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    gap: 6rpx;
    padding: 6rpx 14rpx;
    border-radius: 100rpx;

    .status-dot {
      width: 10rpx;
      height: 10rpx;
      border-radius: 50%;
    }

    .status-text {
      font-size: 22rpx;
      font-weight: 500;
    }

    &.status-0 {
      background: #FEF3C7;
      .status-dot  { background: #FBBF24; }
      .status-text { color: #D97706; }
    }

    &.status-1 {
      background: #DCFCE7;
      .status-dot  { background: #22C55E; }
      .status-text { color: #16A34A; }
    }

    &.status-2 {
      background: #FEE2E2;
      .status-dot  { background: #EF4444; }
      .status-text { color: #DC2626; }
    }
  }

  // ---- 描述 ----
  .desc {
    font-size: 26rpx;
    color: #6B7280;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 1;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  // ---- 元信息行 ----
  .meta-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 4rpx;
  }

  .file-tag {
    padding: 4rpx 14rpx;
    border-radius: 8rpx;
    font-size: 22rpx;
    font-weight: 500;
  }

  .meta-stats {
    display: flex;
    align-items: center;
    gap: 16rpx;
  }

  .stat {
    display: flex;
    align-items: center;
    gap: 6rpx;
    font-size: 24rpx;
    color: #9CA3AF;

    &.rating { color: #F59E0B; }
  }

  .points-badge {
    padding: 4rpx 14rpx;
    border-radius: 24rpx;
    font-size: 22rpx;
    font-weight: 500;
    background: rgba(251, 191, 36, 0.15);
    color: #D97706;

    &.free {
      background: rgba(34, 197, 94, 0.1);
      color: #16A34A;
    }
  }

  // ---- 操作按钮 ----
  .card-actions {
    display: flex;
    border-top: 1rpx solid #F3F4F6;
    padding: 16rpx 28rpx;
    gap: 16rpx;

    .action-btn {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 10rpx;
      padding: 18rpx;
      border-radius: 12rpx;
      font-size: 26rpx;
      font-weight: 500;

      &.btn-delete {
        background: rgba(239, 68, 68, 0.07);
        color: #EF4444;
        border: 1.5rpx solid rgba(239, 68, 68, 0.2);
        &:active { background: rgba(239, 68, 68, 0.14); }
      }

      &.btn-edit {
        background: #3B82F6;
        color: #fff;
        &:active { background: #2563EB; }
      }
    }
  }
}

// 桌面端适配
@media (min-width: 768px) {
  .my-resource-card {
    .card-main { padding: 32rpx; }
    .file-icon { width: 100rpx; height: 100rpx; }
    .title-row .title { font-size: 32rpx; }
    .card-actions .action-btn { padding: 22rpx 32rpx; }
  }
}
</style>
