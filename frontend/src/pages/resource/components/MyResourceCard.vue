<template>
  <view class="my-resource-card" @click="handleClick">
    <view class="card-body">

      <!-- 左侧：核心信息区 -->
      <view class="card-left">
        <!-- 状态标签（仅 showStatus 时） -->
        <view v-if="showStatus" class="status-pill" :class="`s${resource.status ?? 1}`">
          <view class="s-dot" />
          <text class="s-text">{{ statusText }}</text>
        </view>

        <!-- 标题 -->
        <text class="title">{{ resource.title }}</text>

        <!-- 描述 -->
        <text v-if="resource.description" class="desc">{{ resource.description }}</text>

        <!-- 拒绝原因 -->
        <view v-if="resource.status === ResourceStatus.REJECTED && resource.rejectReason" class="reject-note">
          <text class="reject-text">拒绝原因：{{ resource.rejectReason }}</text>
        </view>

        <!-- 元信息：下载 / 评分 -->
        <view class="meta-row">
          <view class="stat-group">
            <Icon name="download" :size="12" color="#94A3B8" />
            <text class="stat-num">{{ formatNumber(resource.downloads) }}</text>
          </view>
          <view v-if="resource.averageRating" class="stat-group">
            <Icon name="star" :size="12" color="#F59E0B" />
            <text class="rating-num">{{ resource.averageRating.toFixed(1) }}</text>
          </view>
        </view>
      </view>

      <!-- 右侧：属性与操作区 -->
      <view class="card-right">
        <!-- 上层：属性标签 -->
        <view class="card-tags">
          <view class="file-tag">
            <text class="file-tag-text">{{ fileTypeConfig.name }}</text>
          </view>
          <view class="points-badge" :class="{ free: !resource.score }">
            <text class="points-text">{{ resource.score ? `${resource.score}分` : '免费' }}</text>
          </view>
        </view>

        <!-- 下层：图标操作按钮（仅 showActions 时） -->
        <view v-if="showActions" class="icon-actions">
          <view class="icon-btn btn-edit" @click.stop="handleEdit">
            <Icon name="pencil" :size="16" color="#377DFF" />
          </view>
          <view class="icon-btn btn-delete" @click.stop="handleDelete">
            <Icon name="trash-2" :size="16" color="#94A3B8" />
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
  return String(num || 0)
}

const handleClick  = () => emit('click',  props.resource)
const handleDelete = () => emit('delete', props.resource)
const handleEdit   = () => emit('edit',   props.resource)
</script>

<style lang="scss" scoped>
.my-resource-card {
  background: #FFFFFF;
  border-radius: 24rpx;
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.04);
  margin-bottom: 20rpx;
  overflow: hidden;
  transition: transform 0.2s, box-shadow 0.2s;

  &:active { opacity: 0.92; }
}

// ---- 卡片主体（水平分栏） ----
.card-body {
  display: flex;
  flex-direction: row;
  align-items: stretch;
  gap: 20rpx;
  padding: 28rpx 28rpx 28rpx 32rpx;
}

// ---- 左侧：核心信息区 ----
.card-left {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

// ---- 右侧：属性与操作区 ----
.card-right {
  flex-shrink: 0;
  width: 112rpx;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: space-between;
}

// ---- 状态 Pill ----
.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 6rpx 16rpx;
  border-radius: 100rpx;
  align-self: flex-start;

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

  &.s0 { background: #FFFBEB; .s-dot { background: #F59E0B; } .s-text { color: #B45309; } }
  &.s1 { background: #F0FDF4; .s-dot { background: #22C55E; } .s-text { color: #16A34A; } }
  &.s2 { background: #FEF2F2; .s-dot { background: #EF4444; } .s-text { color: #DC2626; } }
}

// ---- 标题 ----
.title {
  font-size: 30rpx;
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
  font-size: 24rpx;
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

// ---- 元信息行 ----
.meta-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.stat-group {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.stat-num   { font-size: 22rpx; color: #94A3B8; }
.rating-num { font-size: 22rpx; color: #F59E0B; }

// ---- 属性标签区 ----
.card-tags {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10rpx;
}

// ---- 文件类型标签 ----
.file-tag {
  padding: 6rpx 16rpx;
  border-radius: 12rpx;
  background: #F1F5F9;

  .file-tag-text {
    font-size: 22rpx;
    font-weight: 500;
    color: #475569;
  }
}

// ---- 积分徽章 ----
.points-badge {
  padding: 6rpx 16rpx;
  border-radius: 100rpx;
  background: #FEF3C7;

  .points-text {
    font-size: 22rpx;
    font-weight: 600;
    color: #B45309;
  }

  &.free {
    background: #F0FDF4;
    .points-text { color: #16A34A; }
  }
}

// ---- 图标操作按钮 ----
.icon-actions {
  display: flex;
  gap: 10rpx;
  align-items: center;
}

.icon-btn {
  width: 52rpx;
  height: 52rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;

  &.btn-edit {
    background: rgba(55, 125, 255, 0.08);
    border: 1rpx solid rgba(55, 125, 255, 0.2);
    &:active { background: rgba(55, 125, 255, 0.18); }
  }

  &.btn-delete {
    background: #F8FAFC;
    border: 1rpx solid #E2E8F0;
    &:active { background: #FEF2F2; border-color: #FECACA; }
  }
}

// 桌面端适配
@media (min-width: 768px) {
  .card-body {
    padding: 32rpx 36rpx 32rpx 40rpx;
  }
  .title { font-size: 32rpx; }
  .card-right { width: 128rpx; }
}

/* #ifdef H5 */
.my-resource-card {
  cursor: pointer;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
  }
}

.icon-btn {
  transition: background 0.15s, border-color 0.15s, transform 0.15s;

  &.btn-edit:hover {
    background: rgba(55, 125, 255, 0.15);
    border-color: rgba(55, 125, 255, 0.35);
    transform: scale(1.05);
  }

  &.btn-delete:hover {
    background: #FEF2F2;
    border-color: #FECACA;
  }
}
/* #endif */
</style>
