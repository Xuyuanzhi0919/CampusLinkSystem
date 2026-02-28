<template>
  <view class="my-resource-card" @click="handleClick">
    <view class="card-body">
      <!-- 顶部行：状态 Pill | 文件类型标签（仅 showStatus 时显示） -->
      <view v-if="showStatus" class="top-row">
        <view class="status-pill" :class="`s${resource.status ?? 1}`">
          <view class="s-dot" />
          <text class="s-text">{{ statusText }}</text>
        </view>
        <view class="file-tag">
          <text class="file-tag-text">{{ fileTypeConfig.name }}</text>
        </view>
      </view>

      <!-- 标题行（无状态时，文件类型标签内联显示） -->
      <view class="title-row">
        <text class="title">{{ resource.title }}</text>
        <view v-if="!showStatus" class="file-tag">
          <text class="file-tag-text">{{ fileTypeConfig.name }}</text>
        </view>
      </view>

      <!-- 描述 -->
      <text v-if="resource.description" class="desc">{{ resource.description }}</text>

      <!-- 拒绝原因（status=REJECTED 且有原因文本时显示） -->
      <view v-if="resource.status === ResourceStatus.REJECTED && resource.rejectReason" class="reject-note">
        <text class="reject-text">拒绝原因：{{ resource.rejectReason }}</text>
      </view>

      <!-- 元信息行：下载/评分 | 积分徽章 -->
      <view class="meta-row">
        <view class="stats">
          <view class="stat-group">
            <Icon name="download" :size="12" color="#94A3B8" />
            <text class="stat-num">{{ formatNumber(resource.downloads) }}</text>
          </view>
          <view v-if="resource.averageRating" class="stat-group">
            <Icon name="star" :size="12" color="#F59E0B" />
            <text class="rating-num">{{ resource.averageRating.toFixed(1) }}</text>
          </view>
        </view>
        <view class="points-badge" :class="{ free: !resource.score }">
          <text class="points-text">{{ resource.score ? `${resource.score} 分` : '免费' }}</text>
        </view>
      </view>
    </view>

    <!-- 分割线 + 操作按钮 -->
    <template v-if="showActions">
      <view class="divider" />
      <view class="card-actions">
        <view class="action-btn btn-delete" @click.stop="handleDelete">
          <Icon name="trash-2" :size="14" color="#94A3B8" />
          <text>删除</text>
        </view>
        <view class="action-btn btn-edit" @click.stop="handleEdit">
          <Icon name="pencil" :size="14" color="#FFFFFF" />
          <text>编辑</text>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Icon from '@/components/icons/index.vue'
import { ResourceStatus } from '@/types/resource'
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

// 状态文本
const statusText = computed(() => {
  const map: Record<ResourceStatus, string> = {
    [ResourceStatus.PENDING]:  '待审核',
    [ResourceStatus.APPROVED]: '已通过',
    [ResourceStatus.REJECTED]: '已拒绝',
  }
  return map[props.resource.status ?? ResourceStatus.APPROVED] || '未知'
})

// 文件类型配置
const fileTypeConfig = computed(() => {
  const type = (props.resource.fileType || 'default').toLowerCase()
  const configs: Record<string, { name: string }> = {
    pdf:  { name: 'PDF'   },
    doc:  { name: 'Word'  },
    docx: { name: 'Word'  },
    ppt:  { name: 'PPT'   },
    pptx: { name: 'PPT'   },
    xls:  { name: 'Excel' },
    xlsx: { name: 'Excel' },
    zip:  { name: 'ZIP'   },
    rar:  { name: 'RAR'   },
    txt:  { name: 'TXT'   },
  }
  return configs[type] || { name: '文档' }
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
  background: #FFFFFF;
  border-radius: 32rpx;
  box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.04);
  overflow: hidden;
  margin-bottom: 24rpx;

  &:active { opacity: 0.92; }

  // ---- 卡片主体 ----
  .card-body {
    display: flex;
    flex-direction: column;
    gap: 20rpx;
    padding: 32rpx;
  }

  // ---- 顶部行（有状态时：pill | 文件标签） ----
  .top-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  // ---- 标题行（无状态时：标题 + 文件标签） ----
  .title-row {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16rpx;
  }

  // ---- 状态 Pill ----
  .status-pill {
    display: flex;
    align-items: center;
    gap: 10rpx;
    padding: 8rpx 20rpx;
    border-radius: 100rpx;

    .s-dot {
      width: 14rpx;
      height: 14rpx;
      border-radius: 50%;
      flex-shrink: 0;
    }

    .s-text {
      font-size: 24rpx;
      font-weight: 500;
    }

    // 待审核
    &.s0 {
      background: #FFFBEB;
      .s-dot  { background: #F59E0B; }
      .s-text { color: #B45309; }
    }

    // 已通过
    &.s1 {
      background: #F0FDF4;
      .s-dot  { background: #22C55E; }
      .s-text { color: #16A34A; }
    }

    // 已拒绝
    &.s2 {
      background: #FEF2F2;
      .s-dot  { background: #EF4444; }
      .s-text { color: #DC2626; }
    }
  }

  // ---- 文件类型标签（top-row 和 title-row 共用） ----
  .file-tag {
    flex-shrink: 0;
    padding: 8rpx 20rpx;
    border-radius: 16rpx;
    background: #F1F5F9;

    .file-tag-text {
      font-size: 24rpx;
      font-weight: 500;
      color: #475569;
    }
  }

  // ---- 标题 ----
  .title {
    flex: 1;
    font-size: 32rpx;
    font-weight: 600;
    color: #0F172A;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  // ---- 描述 ----
  .desc {
    font-size: 26rpx;
    color: #64748B;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 1;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  // ---- 拒绝原因 ----
  .reject-note {
    background: #FFF5F5;
    border-radius: 16rpx;
    padding: 16rpx 24rpx;

    .reject-text {
      font-size: 22rpx;
      color: #DC2626;
      line-height: 1.5;
    }
  }

  // ---- 元信息行 ----
  .meta-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  // stats：统计数据组，组间 16rpx，组内 8rpx
  .stats {
    display: flex;
    align-items: center;
    gap: 16rpx;
  }

  .stat-group {
    display: flex;
    align-items: center;
    gap: 8rpx;
  }

  .stat-num {
    font-size: 24rpx;
    color: #94A3B8;
  }

  .rating-num {
    font-size: 24rpx;
    color: #F59E0B;
  }

  // ---- 积分徽章 ----
  .points-badge {
    padding: 8rpx 24rpx;
    border-radius: 100rpx;
    background: #FEF3C7;

    .points-text {
      font-size: 24rpx;
      font-weight: 600;
      color: #B45309;
    }

    &.free {
      background: #F0FDF4;
      .points-text { color: #16A34A; }
    }
  }

  // ---- 分割线（1rpx 与全局保持一致） ----
  .divider {
    height: 1rpx;
    background: #F1F5F9;
  }

  // ---- 操作按钮 ----
  .card-actions {
    display: flex;
    gap: 20rpx;
    padding: 20rpx 32rpx;

    .action-btn {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12rpx;
      padding: 20rpx;
      border-radius: 20rpx;
      font-size: 26rpx;
      font-weight: 500;

      &.btn-delete {
        background: #F8FAFC;
        border: 1rpx solid #E2E8F0;
        color: #64748B;
        &:active { background: #F1F5F9; }
      }

      &.btn-edit {
        background: #1E293B;
        color: #FFFFFF;
        &:active { background: #0F172A; }
      }
    }
  }
}

// 桌面端适配
@media (min-width: 768px) {
  .my-resource-card {
    .card-body { padding: 40rpx; }
    .title { font-size: 34rpx; }
    .card-actions .action-btn { padding: 24rpx 32rpx; }
  }
}
</style>
