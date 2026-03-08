<template>
  <view
    class="resource-card"
    :class="{ 'is-active': isActive, 'is-mobile': isMobile }"
    @click="handleClick"
    @touchstart="onTouchStart"
    @touchend="onTouchEnd"
    @mouseenter="onMouseEnter"
    @mouseleave="onMouseLeave"
  >
    <!-- 审核状态标签 (非已通过时显示) -->
    <view
      v-if="resource.status !== undefined && resource.status !== 1"
      class="status-badge"
      :class="`status-${resource.status}`"
    >
      {{ getStatusText(resource.status) }}
    </view>

    <!-- 积分/免费角标（右上角绝对定位） -->
    <view v-if="resource.score && resource.score > 0" class="score-corner">
      <Icon name="zap" :size="10" color="#FFFFFF" />
      <text class="score-corner-text">{{ resource.score }}</text>
    </view>
    <view v-else class="free-corner">
      <text class="free-corner-text">免费</text>
    </view>

    <!-- 卡片主体：左侧图标徽章 + 右侧信息 -->
    <view class="card-body">
      <!-- 左侧：类型图标徽章 -->
      <view class="type-badge" :class="`type-${getCategoryClass(resource.category)}`">
        <Icon :name="getTypeIconName(resource.category)" :size="20" color="#FFFFFF" />
      </view>

      <!-- 右侧：信息区 -->
      <view class="card-info">
        <!-- 标题 -->
        <rich-text v-if="keyword" class="title" :nodes="highlightText(resource.title, keyword)" />
        <text v-else class="title">{{ resource.title }}</text>

        <!-- 元信息行：时间 · 课程 · 格式 · 大小 -->
        <view class="meta-row">
          <text class="meta-text">{{ formatTime(resource.createdAt) }}</text>
          <template v-if="resource.courseName">
            <text class="meta-sep">·</text>
            <text class="meta-text">{{ resource.courseName }}</text>
          </template>
          <template v-if="resource.fileType">
            <text class="meta-sep">·</text>
            <text class="meta-text meta-filetype" :class="`filetype-${resource.fileType?.toLowerCase()}`">
              {{ getFileTypeText(resource.fileType) }}
            </text>
          </template>
          <template v-if="resource.fileSize">
            <text class="meta-sep">·</text>
            <text class="meta-text">{{ formatFileSize(resource.fileSize) }}</text>
          </template>
        </view>
      </view>
    </view>

    <!-- 底部 footer：统计 + 操作按钮 -->
    <view class="card-footer">
      <!-- 左侧：统计数据 -->
      <view class="stats-group">
        <view class="stat-item">
          <Icon name="download" :size="13" color="#9CA3AF" />
          <text class="stat-value">{{ formatNumber(resource.downloads) }}</text>
        </view>
        <view class="stat-item">
          <Icon name="heart" :size="13" color="#9CA3AF" />
          <text class="stat-value">{{ formatNumber(resource.likes) }}</text>
        </view>
        <view v-if="resource.favorites && resource.favorites > 0" class="stat-item">
          <Icon name="star" :size="13" color="#9CA3AF" />
          <text class="stat-value">{{ formatNumber(resource.favorites) }}</text>
        </view>
      </view>

      <!-- 右侧：操作按钮 -->
      <view class="action-buttons">
        <!-- 收藏按钮 -->
        <view
          class="icon-btn favorite-btn"
          :class="{ 'is-favorited': resource.isFavorited }"
          @click.stop="handleFavorite"
        >
          <Icon
            :name="resource.isFavorited ? 'star' : 'bookmark'"
            :size="15"
            :color="resource.isFavorited ? '#F59E0B' : '#6B7280'"
          />
        </view>

        <!-- 下载按钮 -->
        <view
          class="download-btn"
          :class="{ 'is-downloaded': resource.isDownloaded }"
          @click.stop="handleDownload"
        >
          <Icon
            name="download"
            :size="14"
            :color="resource.isDownloaded ? '#10B981' : '#FFFFFF'"
          />
          <text class="download-text">{{ resource.isDownloaded ? '已下载' : '下载' }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { ResourceItem } from '@/types/resource'
import { ResourceCategory, ResourceStatus } from '@/types/resource'
import Icon from '@/components/icons/index.vue'

interface Props {
  resource: ResourceItem
  keyword?: string
}

const props = defineProps<Props>()

const emit = defineEmits<{
  click: [resource: ResourceItem]
  download: [resource: ResourceItem]
  like: [resource: ResourceItem]
  favorite: [resource: ResourceItem]
}>()

const isActive = ref(false)
const isMobile = ref(false)

onMounted(() => {
  checkDevice()
  // #ifdef H5
  window.addEventListener('resize', checkDevice)
  // #endif
})

const checkDevice = () => {
  isMobile.value = window.innerWidth < 768
}

const getCategoryClass = (category: number | string | undefined): string => {
  if (typeof category === 'string') {
    const classMap: Record<string, string> = {
      '课件': 'courseware', '试卷': 'paper', '笔记': 'note',
      '教材': 'textbook', '实验报告': 'report', '视频': 'video'
    }
    return classMap[category] || '0'
  }
  return String(category || 0)
}

const getTypeIconName = (category: number | string | undefined): string => {
  if (typeof category === 'string') {
    const iconMap: Record<string, string> = {
      '课件': 'book-open', '试卷': 'file-text', '笔记': 'edit-3',
      '教材': 'book', '实验报告': 'code', '视频': 'layers', '其他': 'folder'
    }
    return iconMap[category] || 'file-text'
  }
  const num = category || 0
  const iconMap: Record<number, string> = {
    [ResourceCategory.COURSEWARE]: 'book-open',
    [ResourceCategory.PAPER]: 'file-text',
    [ResourceCategory.NOTE]: 'edit-3'
  }
  return iconMap[num] || 'file-text'
}

const getStatusText = (status: number): string => {
  const map: Record<number, string> = {
    [ResourceStatus.PENDING]: '待审核',
    [ResourceStatus.APPROVED]: '已通过',
    [ResourceStatus.REJECTED]: '已拒绝'
  }
  return map[status] || ''
}

const getFileTypeText = (fileType: string): string => fileType?.toUpperCase() || 'FILE'

const formatFileSize = (bytes: number): string => {
  if (!bytes || bytes === 0) return ''
  const mb = 1024 * 1024
  const kb = 1024
  if (bytes >= 1024 * mb) return `${(bytes / (1024 * mb)).toFixed(1)} GB`
  if (bytes >= mb) return `${(bytes / mb).toFixed(1)} MB`
  if (bytes >= kb) return `${(bytes / kb).toFixed(0)} KB`
  return `${bytes} B`
}

const formatNumber = (num: number): string => {
  if (!num || num === 0) return '0'
  if (num >= 10000) return `${(num / 10000).toFixed(1)}w`
  if (num >= 1000) return `${(num / 1000).toFixed(1)}k`
  return num.toString()
}

const formatTime = (time: string): string => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minute = 60 * 1000, hour = 60 * minute, day = 24 * hour
  if (diff < minute) return '刚刚'
  if (diff < hour) return `${Math.floor(diff / minute)}分钟前`
  if (diff < day) return `${Math.floor(diff / hour)}小时前`
  if (diff < 7 * day) return `${Math.floor(diff / day)}天前`
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

const highlightText = (text: string, keyword?: string): string => {
  if (!keyword || !text) return text
  try {
    const esc = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
    return text.replace(new RegExp(`(${esc})`, 'gi'),
      '<span style="color:#2563EB;font-weight:600;background:rgba(37,99,235,0.08);padding:0 2px;border-radius:3px;">$1</span>')
  } catch { return text }
}

const onTouchStart = () => { isActive.value = true }
const onTouchEnd = () => { setTimeout(() => { isActive.value = false }, 150) }
const onMouseEnter = () => { if (!isMobile.value) isActive.value = true }
const onMouseLeave = () => { if (!isMobile.value) isActive.value = false }

const handleClick = () => { emit('click', props.resource) }
const handleDownload = () => { emit('download', props.resource) }
const handleFavorite = () => { emit('favorite', props.resource) }
</script>

<style scoped lang="scss">
.resource-card {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  border: 1px solid #E4E4E7;
  position: relative;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s, transform 0.2s;

  &:hover,
  &.is-active {
    border-color: #2563EB;
    box-shadow: 0 6px 20px rgba(37, 99, 235, 0.10);
    transform: translateY(-2px);
  }

  &.is-mobile {
    margin-bottom: 10px;
    &:hover, &.is-active {
      box-shadow: none;
      background: #F9FAFB;
      transform: none;
    }
  }
}

// 审核状态标签
.status-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 500;
  &.status-0 { background: rgba(156,163,175,0.12); color: #6B7280; }
  &.status-2 { background: rgba(239,68,68,0.10); color: #EF4444; }
}

// 积分角标（右上角）
.score-corner {
  position: absolute;
  top: 12px;
  right: 12px;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 3px 8px;
  border-radius: 20px;
  background: #F59E0B;

  .score-corner-text {
    font-size: 11px;
    font-weight: 600;
    color: #FFFFFF;
  }
}

// 免费角标（右上角）
.free-corner {
  position: absolute;
  top: 12px;
  right: 12px;
  display: inline-flex;
  align-items: center;
  padding: 3px 8px;
  border-radius: 20px;
  background: rgba(34, 197, 94, 0.10);
  border: 1px solid rgba(34, 197, 94, 0.20);

  .free-corner-text {
    font-size: 11px;
    font-weight: 600;
    color: #16A34A;
  }
}

// 卡片主体
.card-body {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
  // 右侧留出角标空间
  padding-right: 56px;
}

// 左侧类型图标徽章
.type-badge {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;

  &.type-0, &.type-courseware { background: #2563EB; }
  &.type-1, &.type-paper      { background: #EF4444; }
  &.type-2, &.type-note       { background: #22C55E; }
  &.type-3, &.type-textbook   { background: #F97316; }
  &.type-4, &.type-report     { background: #06B6D4; }
  &.type-video                { background: #8B5CF6; }
}

// 右侧信息区
.card-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

// 标题
.title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  line-height: 1.45;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: break-word;
  transition: color 0.15s;

  .resource-card:hover & {
    color: #2563EB;
  }
}

// 元信息行（单行，分隔符分隔）
.meta-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}

.meta-text {
  font-size: 12px;
  color: #9CA3AF;
  white-space: nowrap;
}

.meta-sep {
  font-size: 12px;
  color: #D1D5DB;
}

// 格式类型带色
.meta-filetype {
  font-weight: 500;
  &.filetype-pdf  { color: #EF4444; }
  &.filetype-ppt,
  &.filetype-pptx { color: #F97316; }
  &.filetype-doc,
  &.filetype-docx { color: #2563EB; }
  &.filetype-xls,
  &.filetype-xlsx { color: #16A34A; }
}

// 底部 footer
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid #F3F4F6;
}

.stats-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;

  .stat-value {
    font-size: 12px;
    color: #9CA3AF;
  }
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}

// 收藏按钮
.icon-btn {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #E4E4E7;
  background: #FFFFFF;
  cursor: pointer;
  transition: all 0.2s;

  &:hover { border-color: #D1D5DB; background: #F9FAFB; }
  &.is-favorited { border-color: #FCD34D; background: rgba(251,191,36,0.08); }
}

// 下载按钮
.download-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border-radius: 8px;
  background: #2563EB;
  cursor: pointer;
  transition: all 0.2s;

  .download-text {
    font-size: 12px;
    font-weight: 500;
    color: #FFFFFF;
  }

  &:hover  { background: #1D4ED8; }
  &:active { background: #1E40AF; transform: scale(0.97); }

  &.is-downloaded {
    background: rgba(16,185,129,0.10);
    border: 1px solid rgba(16,185,129,0.30);

    .download-text { color: #10B981; }
    &:hover { background: rgba(16,185,129,0.15); }
  }
}
</style>
