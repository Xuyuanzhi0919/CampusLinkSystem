<template>
  <view class="my-resource-card" @click="handleClick">
    <view class="card-body">

      <!-- 顶行：标题（flex:1）| 文件类型 + 积分（横排紧凑） -->
      <view class="card-top">
        <text class="title">{{ resource.title }}</text>
        <view class="top-badges">
          <view class="file-tag">
            <text class="file-tag-text">{{ fileTypeConfig.name }}</text>
          </view>
          <view class="points-badge" :class="{ free: !resource.score }">
            <text class="points-text">{{ resource.score ? `${resource.score}分` : '免费' }}</text>
          </view>
        </view>
      </view>

      <!-- 中行：描述 -->
      <text v-if="resource.description" class="desc">{{ resource.description }}</text>

      <!-- 拒绝原因 -->
      <view v-if="resource.status === ResourceStatus.REJECTED && resource.rejectReason" class="reject-note">
        <text class="reject-text">拒绝原因：{{ resource.rejectReason }}</text>
      </view>

      <!-- 底行：状态标签 + 下载数 | 操作图标 -->
      <view class="card-bottom">
        <view class="bottom-left">
          <!-- 上传资源：审核状态标签 -->
          <view v-if="showStatus" class="status-pill" :class="`s${resource.status ?? 1}`">
            <view class="s-dot" />
            <text class="s-text">{{ statusText }}</text>
          </view>
          <!-- 下载历史：已下载标签 -->
          <view v-else class="status-pill s-downloaded">
            <view class="s-dot" />
            <text class="s-text">已下载</text>
          </view>
          <!-- 下载数（有数据时才显示） -->
          <view v-if="resource.downloads" class="stat-group">
            <Icon name="download" :size="12" color="#94A3B8" />
            <text class="stat-num">{{ formatNumber(resource.downloads) }}</text>
          </view>
          <view v-if="resource.averageRating" class="stat-group">
            <Icon name="star" :size="12" color="#F59E0B" />
            <text class="rating-num">{{ resource.averageRating.toFixed(1) }}</text>
          </view>
        </view>

        <!-- 操作图标（纯线性，无底色） -->
        <view v-if="showActions" class="icon-actions">
          <view class="icon-btn btn-edit" title="编辑资源" @click.stop="handleEdit">
            <Icon name="pencil" :size="14" color="#94A3B8" />
          </view>
          <view class="icon-btn btn-delete" title="删除资源" @click.stop="handleDelete">
            <Icon name="trash-2" :size="14" color="#94A3B8" />
          </view>
        </view>
      </view>

    </view>
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
  return String(num)
}

const handleClick  = () => emit('click',  props.resource)
const handleDelete = () => emit('delete', props.resource)
const handleEdit   = () => emit('edit',   props.resource)
</script>

<style lang="scss" scoped>
.my-resource-card {
  background: #FFFFFF;
  border-radius: 24rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  margin-bottom: 24rpx;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;

  &:active { opacity: 0.92; }
}

// ---- 卡片主体（垂直三行） ----
.card-body {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  padding: 32rpx;
}

// ---- 顶行：标题 + 右侧标签组 ----
.card-top {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
}

.top-badges {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 10rpx;
}

// ---- 标题 ----
.title {
  flex: 1;
  font-size: 30rpx;
  font-weight: 600;
  color: #1E293B;
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
  border-radius: 12rpx;
  padding: 12rpx 16rpx;

  .reject-text {
    font-size: 22rpx;
    color: #DC2626;
    line-height: 1.5;
  }
}

// ---- 底行 ----
.card-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.bottom-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

// ---- 状态 Pill ----
.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 6rpx 14rpx;
  border-radius: 100rpx;

  .s-dot {
    width: 12rpx;
    height: 12rpx;
    border-radius: 50%;
    flex-shrink: 0;
  }

  .s-text {
    font-size: 22rpx;
    font-weight: 500;
  }

  &.s0          { background: #FFFBEB; .s-dot { background: #F59E0B; } .s-text { color: #B45309; } }
  &.s1          { background: #F0FDF4; .s-dot { background: #22C55E; } .s-text { color: #16A34A; } }
  &.s2          { background: #FEF2F2; .s-dot { background: #EF4444; } .s-text { color: #DC2626; } }
  &.s-downloaded{ background: #F1F5F9; .s-dot { background: #94A3B8; } .s-text { color: #64748B; } }
}

// ---- 统计数据 ----
.stat-group {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.stat-num   { font-size: 24rpx; color: #94A3B8; }
.rating-num { font-size: 24rpx; color: #F59E0B; }

// ---- 文件类型标签（边框样式，不抢视觉） ----
.file-tag {
  padding: 6rpx 14rpx;
  border-radius: 12rpx;
  border: 1.5rpx solid #E2E8F0;

  .file-tag-text {
    font-size: 22rpx;
    color: #94A3B8;
  }
}

// ---- 积分徽章（实心色，突出价值感） ----
.points-badge {
  padding: 6rpx 16rpx;
  border-radius: 100rpx;
  background: #F59E0B;

  .points-text {
    font-size: 22rpx;
    font-weight: 600;
    color: #FFFFFF;
  }

  &.free {
    background: #10B981;
    .points-text { color: #FFFFFF; }
  }
}

// ---- 操作图标（纯线性，无底色） ----
.icon-actions {
  display: flex;
  gap: 32rpx;
  align-items: center;
}

.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40rpx;
  height: 40rpx;
  border-radius: 8rpx;

  &:active { opacity: 0.5; }
}

// 桌面端适配
@media (min-width: 768px) {
  .card-body { padding: 36rpx; }
  .title { font-size: 32rpx; }
}

/* #ifdef H5 */
.my-resource-card {
  cursor: pointer;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8rpx 28rpx rgba(0, 0, 0, 0.1);
  }
}

.icon-btn {
  cursor: pointer;
  transition: background 0.15s;

  &.btn-edit:hover  { background: rgba(55, 125, 255, 0.10); }
  &.btn-delete:hover{ background: rgba(239, 68, 68, 0.10);  }
}
/* #endif */
</style>
