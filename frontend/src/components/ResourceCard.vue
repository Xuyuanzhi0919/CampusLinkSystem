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

    <!-- 卡片主体：左侧图标徽章 + 右侧信息 -->
    <view class="card-body">
      <!-- 左侧：类型图标徽章 -->
      <view class="type-badge" :class="`type-${getCategoryClass(resource.category)}`">
        <ClIcon :name="getTypeIconName(resource.category)" size="base" color="#FFFFFF" />
      </view>

      <!-- 右侧：信息区 -->
      <view class="card-info">
        <!-- 标题行：标题 + 积分chip -->
        <view class="title-row">
          <rich-text v-if="keyword" class="title" :nodes="highlightText(resource.title, keyword)" />
          <text v-else class="title">{{ resource.title }}</text>

          <!-- 积分 chip（需要积分时显示） -->
          <view v-if="resource.score && resource.score > 0" class="score-chip">
            <ClIcon name="icon-coin" size="xs" color="#D97706" />
            <text class="score-text">{{ resource.score }}</text>
          </view>
          <!-- 免费标签 -->
          <view v-else class="free-chip">
            <text class="free-text">免费</text>
          </view>
        </view>

        <!-- 元信息行：学校 · 时间 -->
        <view class="meta-row">
          <view v-if="resource.uploaderSchool" class="meta-item">
            <ClIcon name="icon-building" size="xs" color="#9CA3AF" />
            <text class="meta-text">{{ resource.uploaderSchool }}</text>
          </view>
          <text v-if="resource.uploaderSchool" class="meta-dot">·</text>
          <text class="meta-time">{{ formatTime(resource.createdAt) }}</text>
        </view>

        <!-- Tag 行：课程名 + 文件类型 + 课程大小 -->
        <view class="tags-row">
          <view v-if="resource.courseName" class="tag-pill">
            <text>{{ resource.courseName }}</text>
          </view>
          <view v-if="resource.fileType" class="tag-pill" :class="`tag-filetype-${resource.fileType?.toLowerCase()}`">
            <text>{{ getFileTypeText(resource.fileType) }}</text>
          </view>
          <view v-if="resource.fileSize" class="tag-pill tag-size">
            <text>{{ formatFileSize(resource.fileSize) }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部 footer：统计 + 操作按钮 -->
    <view class="card-footer">
      <!-- 左侧：统计数据 -->
      <view class="stats-group">
        <view class="stat-item">
          <ClIcon name="icon-download" size="xs" color="#9CA3AF" />
          <text class="stat-value">{{ formatNumber(resource.downloads) }}</text>
        </view>
        <view class="stat-item">
          <ClIcon name="icon-heart" size="xs" color="#9CA3AF" />
          <text class="stat-value">{{ formatNumber(resource.likes) }}</text>
        </view>
        <view v-if="resource.favorites && resource.favorites > 0" class="stat-item">
          <ClIcon name="icon-star" size="xs" color="#9CA3AF" />
          <text class="stat-value">{{ formatNumber(resource.favorites) }}</text>
        </view>
      </view>

      <!-- 右侧：操作按钮 -->
      <view class="action-buttons">
        <!-- 收藏按钮：圆形图标按钮 -->
        <view
          class="icon-btn favorite-btn"
          :class="{ 'is-favorited': resource.isFavorited }"
          @click.stop="handleFavorite"
        >
          <ClIcon
            :name="resource.isFavorited ? 'icon-star' : 'icon-bookmark'"
            size="sm"
            :color="resource.isFavorited ? '#F59E0B' : '#6B7280'"
          />
        </view>

        <!-- 下载按钮：蓝色圆角矩形 -->
        <view
          class="download-btn"
          :class="{ 'is-downloaded': resource.isDownloaded }"
          @click.stop="handleDownload"
        >
          <ClIcon
            name="icon-download"
            size="sm"
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
import { PLACEHOLDER_IMAGES } from '@/config/images'
import ClIcon from '@/components/cl/ClIcon.vue'

// Props
interface Props {
  resource: ResourceItem
  keyword?: string  // 搜索关键词，用于高亮
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  click: [resource: ResourceItem]
  download: [resource: ResourceItem]
  like: [resource: ResourceItem]
  favorite: [resource: ResourceItem]
}>()

// 响应式状态
const isActive = ref(false)
const isMobile = ref(false)
const defaultAvatar = PLACEHOLDER_IMAGES.avatar

// 检测设备类型
onMounted(() => {
  checkDevice()
  // #ifdef H5
  window.addEventListener('resize', checkDevice)
  // #endif
})

const checkDevice = () => {
  isMobile.value = window.innerWidth < 768
}

/**
 * 获取分类的英文类名（用于 CSS 类名，避免中文在小程序中报错）
 */
const getCategoryClass = (category: number | string | undefined): string => {
  if (typeof category === 'string') {
    const classMap: Record<string, string> = {
      '课件': 'courseware',
      '试卷': 'paper',
      '笔记': 'note',
      '教材': 'textbook',
      '实验报告': 'report',
      '视频': 'video'
    }
    return classMap[category] || '0'
  }
  return String(category || 0)
}

/**
 * 获取资源类型图标名称（ClIcon 使用）
 */
const getTypeIconName = (category: number | string | undefined): string => {
  if (typeof category === 'string') {
    const stringIconMap: Record<string, string> = {
      '课件': 'icon-file-ppt',
      '试卷': 'icon-file-text',
      '笔记': 'icon-edit',
      '教材': 'icon-book',
      '实验报告': 'icon-file-text',
      '视频': 'icon-play-circle'
    }
    return stringIconMap[category] || 'icon-file'
  }
  const categoryNum = category || 0
  const iconMap: Record<number, string> = {
    [ResourceCategory.COURSEWARE]: 'icon-file-ppt',
    [ResourceCategory.PAPER]: 'icon-file-text',
    [ResourceCategory.NOTE]: 'icon-edit'
  }
  return iconMap[categoryNum] || 'icon-file'
}

/**
 * 获取审核状态文本
 */
const getStatusText = (status: number): string => {
  const statusMap: Record<number, string> = {
    [ResourceStatus.PENDING]: '待审核',
    [ResourceStatus.APPROVED]: '已通过',
    [ResourceStatus.REJECTED]: '已拒绝'
  }
  return statusMap[status] || ''
}

/**
 * 获取文件类型文本
 */
const getFileTypeText = (fileType: string): string => {
  return fileType?.toUpperCase() || 'FILE'
}

/**
 * 格式化文件大小
 */
const formatFileSize = (bytes: number): string => {
  if (!bytes || bytes === 0) return ''
  const kb = 1024
  const mb = kb * 1024
  const gb = mb * 1024
  if (bytes >= gb) return `${(bytes / gb).toFixed(1)} GB`
  if (bytes >= mb) return `${(bytes / mb).toFixed(1)} MB`
  if (bytes >= kb) return `${(bytes / kb).toFixed(0)} KB`
  return `${bytes} B`
}

/**
 * 格式化数字 (1000 -> 1k)
 */
const formatNumber = (num: number): string => {
  if (!num || num === 0) return '0'
  if (num >= 10000) return `${(num / 10000).toFixed(1)}w`
  if (num >= 1000) return `${(num / 1000).toFixed(1)}k`
  return num.toString()
}

/**
 * 格式化时间
 */
const formatTime = (time: string): string => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  if (diff < minute) return '刚刚'
  if (diff < hour) return `${Math.floor(diff / minute)}分钟前`
  if (diff < day) return `${Math.floor(diff / hour)}小时前`
  if (diff < 7 * day) return `${Math.floor(diff / day)}天前`
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${d}`
}

/**
 * 高亮关键词
 */
const highlightText = (text: string, keyword?: string): string => {
  if (!keyword || !text) return text
  try {
    const escapedKeyword = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
    const regex = new RegExp(`(${escapedKeyword})`, 'gi')
    return text.replace(regex, '<span style="color: #2563EB; font-weight: 600; background: rgba(37, 99, 235, 0.08); padding: 0 2px; border-radius: 3px;">$1</span>')
  } catch (error) {
    return text
  }
}

// 触摸/鼠标交互
const onTouchStart = () => { isActive.value = true }
const onTouchEnd = () => { setTimeout(() => { isActive.value = false }, 150) }
const onMouseEnter = () => { if (!isMobile.value) isActive.value = true }
const onMouseLeave = () => { if (!isMobile.value) isActive.value = false }

// 事件处理
const handleClick = () => { emit('click', props.resource) }
const handleDownload = () => { emit('download', props.resource) }
const handleFavorite = () => { emit('favorite', props.resource) }
</script>

<style scoped lang="scss">
// ===== 卡片容器 =====
.resource-card {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  border: 1.5px solid #E4E4E7;
  position: relative;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;

  &:hover,
  &.is-active {
    border-color: #2563EB;
    box-shadow: 0 4px 16px rgba(37, 99, 235, 0.10);
  }

  // 移动端：无圆角、底部 border 分隔
  &.is-mobile {
    border-radius: 0;
    margin-bottom: 0;
    border-left: none;
    border-right: none;
    border-top: none;
    border-bottom: 1px solid #E4E4E7;
    box-shadow: none;

    &:hover,
    &.is-active {
      border-color: transparent;
      border-bottom-color: #E4E4E7;
      box-shadow: none;
      background: #F9FAFB;
    }
  }
}

// 审核状态标签
.status-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 500;
  white-space: nowrap;

  &.status-0 {
    background: rgba(156, 163, 175, 0.12);
    color: #6B7280;
  }

  &.status-2 {
    background: rgba(239, 68, 68, 0.10);
    color: #EF4444;
  }
}

// ===== 卡片主体 =====
.card-body {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

// 左侧类型图标徽章
.type-badge {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;

  // 课件 - 蓝色
  &.type-0,
  &.type-courseware {
    background: #2563EB;
  }

  // 试卷/试题 - 红色
  &.type-1,
  &.type-paper {
    background: #EF4444;
  }

  // 笔记 - 绿色
  &.type-2,
  &.type-note {
    background: #22C55E;
  }

  // 教材 - 橙色
  &.type-3,
  &.type-textbook {
    background: #F97316;
  }

  // 实验报告 - 青色
  &.type-4,
  &.type-report {
    background: #06B6D4;
  }

  // 视频 - 紫色
  &.type-video {
    background: #8B5CF6;
  }
}

// 右侧信息区
.card-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

// 标题行
.title-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.title {
  flex: 1;
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: break-word;
}

// 积分 chip
.score-chip {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 2px 8px;
  border-radius: 20px;
  background: rgba(245, 158, 11, 0.10);
  border: 1px solid rgba(245, 158, 11, 0.20);

  .score-text {
    font-size: 11px;
    font-weight: 600;
    color: #D97706;
  }
}

// 免费标签
.free-chip {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 20px;
  background: rgba(34, 197, 94, 0.10);
  border: 1px solid rgba(34, 197, 94, 0.20);

  .free-text {
    font-size: 11px;
    font-weight: 600;
    color: #16A34A;
  }
}

// 元信息行
.meta-row {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}

.meta-text {
  font-size: 12px;
  color: #6B7280;
}

.meta-dot {
  font-size: 12px;
  color: #D1D5DB;
}

.meta-time {
  font-size: 12px;
  color: #9CA3AF;
}

// Tags 行
.tags-row {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.tag-pill {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 20px;
  border: 1px solid #E4E4E7;
  background: #FAFAFA;

  text {
    font-size: 11px;
    color: #6B7280;
  }

  // PDF 红色
  &.tag-filetype-pdf {
    background: rgba(239, 68, 68, 0.06);
    border-color: rgba(239, 68, 68, 0.20);
    text { color: #EF4444; }
  }

  // PPT/PPTX 橙色
  &.tag-filetype-ppt,
  &.tag-filetype-pptx {
    background: rgba(249, 115, 22, 0.06);
    border-color: rgba(249, 115, 22, 0.20);
    text { color: #F97316; }
  }

  // DOC/DOCX 蓝色
  &.tag-filetype-doc,
  &.tag-filetype-docx {
    background: rgba(37, 99, 235, 0.06);
    border-color: rgba(37, 99, 235, 0.20);
    text { color: #2563EB; }
  }

  // XLS/XLSX 绿色
  &.tag-filetype-xls,
  &.tag-filetype-xlsx {
    background: rgba(34, 197, 94, 0.06);
    border-color: rgba(34, 197, 94, 0.20);
    text { color: #16A34A; }
  }
}

// ===== 底部 footer =====
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid #F3F4F6;
}

// 左侧统计
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
    font-weight: 400;
  }
}

// 右侧操作按钮
.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}

// 收藏按钮：32×32 圆形
.icon-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1.5px solid #E4E4E7;
  background: #FFFFFF;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: #D1D5DB;
    background: #F9FAFB;
  }

  &.is-favorited {
    border-color: #FCD34D;
    background: rgba(251, 191, 36, 0.08);
  }
}

// 下载按钮：蓝色圆角矩形
.download-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border-radius: 8px;
  background: #2563EB;
  cursor: pointer;
  transition: all 0.2s;
  border: none;

  .download-text {
    font-size: 12px;
    font-weight: 500;
    color: #FFFFFF;
  }

  &:hover {
    background: #1D4ED8;
  }

  &:active {
    background: #1E40AF;
    transform: scale(0.97);
  }

  &.is-downloaded {
    background: rgba(16, 185, 129, 0.10);
    border: 1.5px solid rgba(16, 185, 129, 0.30);

    .download-text {
      color: #10B981;
    }

    &:hover {
      background: rgba(16, 185, 129, 0.15);
    }
  }
}
</style>
