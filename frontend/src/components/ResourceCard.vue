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
    <!-- 📌 第一层:标题 + 价值指标 (点赞数) -->
    <view class="card-header">
      <view class="title-row">
        <!-- 资源类型图标 -->
        <view class="type-icon" :class="`type-${getCategoryClass(resource.category)}`">
          {{ getTypeIcon(resource.category) }}
        </view>

        <!-- 标题 -->
        <rich-text v-if="keyword" class="title" :nodes="highlightText(resource.title, keyword)" />
        <text v-else class="title">{{ resource.title }}</text>

        <!--  点赞数 (弱提示) -->
        <view class="likes-hint">
          <text class="like-icon-hint">♥</text>
          <text class="like-count">{{ formatNumber(resource.likes) }}</text>
        </view>
      </view>

      <!-- 审核状态标签 (如果有) -->
      <view
        v-if="resource.status !== undefined && resource.status !== 1"
        class="status-badge"
        :class="`status-${resource.status}`"
      >
        {{ getStatusText(resource.status) }}
      </view>
    </view>

    <!-- 📌 第二层:描述 + 文件类型标签 -->
    <view class="description-section">
      <view class="description">
        <rich-text v-if="keyword" :nodes="highlightText(resource.description, keyword)" />
        <text v-else>{{ resource.description }}</text>
      </view>

      <!-- 文件类型标签紧贴描述 -->
      <view class="file-type-badge" :class="`filetype-${resource.fileType}`">
        {{ getFileTypeIcon(resource.fileType) }} {{ getFileTypeText(resource.fileType) }}
      </view>
    </view>

    <!-- 📌 第三层:元信息 (课程/大小/上传者/时间) -->
    <view class="meta-section">
      <!-- 课程标签 -->
      <view v-if="resource.courseName" class="tag course-tag">
        📖 {{ resource.courseName }}
      </view>

      <!-- 文件大小 -->
      <view v-if="resource.fileSize" class="tag size-tag">
        {{ formatFileSize(resource.fileSize) }}
      </view>

      <!-- 分隔符 -->
      <view class="meta-divider">·</view>

      <!-- 上传者 -->
      <text class="meta-text">{{ resource.uploaderName }}</text>

      <!-- 时间 -->
      <view class="meta-divider">·</view>
      <text class="meta-text meta-time">{{ formatTime(resource.createdAt) }}</text>
    </view>

    <!-- 📌 第四层:行为区 (积分+下载量+操作按钮) -->
    <view class="action-section">
      <!-- 左侧:统计数据 -->
      <view class="stats-group">
        <!-- 积分价格 (突出显示) -->
        <view class="stat-item stat-points">
          <text class="stat-icon">💰</text>
          <text class="stat-value">{{ resource.score }}</text>
        </view>

        <!-- 下载量 -->
        <view class="stat-item stat-downloads">
          <text class="stat-icon">↓</text>
          <text class="stat-value">{{ formatNumber(resource.downloads) }}</text>
        </view>
      </view>

      <!-- 右侧:操作按钮组 (降权设计) -->
      <view class="action-buttons">
        <!-- 点赞按钮 (图标按钮) -->
        <view
          class="icon-btn like-btn"
          :class="{ 'is-liked': resource.isLiked }"
          @click.stop="handleLike"
        >
          <text class="icon-btn-icon">{{ resource.isLiked ? '♥' : '♡' }}</text>
        </view>

        <!-- 下载按钮 (图标按钮,降权) -->
        <view
          class="icon-btn download-btn"
          :class="{ 'is-downloaded': resource.isDownloaded }"
          @click.stop="handleDownload"
          :title="resource.isDownloaded ? '重新下载' : `下载 (${resource.score}积分)`"
        >
          <text class="icon-btn-icon">↓</text>
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

// Props
interface Props {
  resource: ResourceItem
  keyword?: string  // 搜索关键词,用于高亮
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  click: [resource: ResourceItem]
  download: [resource: ResourceItem]
  like: [resource: ResourceItem]
}>()

//  响应式状态
const isActive = ref(false)
const isMobile = ref(false)
const defaultAvatar = PLACEHOLDER_IMAGES.avatar

// 检测设备类型
onMounted(() => {
  checkDevice()
  window.addEventListener('resize', checkDevice)
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
      '实验报告': 'report'
    }
    return classMap[category] || '0'
  }
  return String(category || 0)
}

/**
 * 获取资源类型图标 - 支持统一颜色体系
 */
const getTypeIcon = (category: number | string | undefined): string => {
  // 如果是字符串类型，直接映射
  if (typeof category === 'string') {
    const stringIconMap: Record<string, string> = {
      '课件': '📚',
      '试卷': '📝',
      '笔记': '✍️',
      '教材': '📖',
      '实验报告': '🔬'
    }
    return stringIconMap[category] || '📦'
  }

  // 如果是数字类型，使用枚举映射
  const categoryNum = category || 0
  const iconMap: Record<number, string> = {
    [ResourceCategory.COURSEWARE]: '📚',
    [ResourceCategory.PAPER]: '📝',
    [ResourceCategory.NOTE]: '✍️'
  }
  return iconMap[categoryNum] || '📦'
}

/**
 *  获取审核状态文本
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
 *  获取文件类型图标
 */
const getFileTypeIcon = (fileType: string): string => {
  const iconMap: Record<string, string> = {
    pdf: '📄',
    docx: '📃',
    doc: '📃',
    pptx: '📊',
    ppt: '📊',
    zip: '🗜️',
    rar: '🗜️',
    other: '📎'
  }
  return iconMap[fileType] || '📎'
}

/**
 *  获取文件类型文本
 */
const getFileTypeText = (fileType: string): string => {
  return fileType?.toUpperCase() || 'FILE'
}

/**
 *  格式化文件大小
 */
const formatFileSize = (bytes: number): string => {
  if (!bytes || bytes === 0) return ''

  const kb = 1024
  const mb = kb * 1024
  const gb = mb * 1024

  if (bytes >= gb) {
    return `${(bytes / gb).toFixed(1)} GB`
  } else if (bytes >= mb) {
    return `${(bytes / mb).toFixed(1)} MB`
  } else if (bytes >= kb) {
    return `${(bytes / kb).toFixed(0)} KB`
  } else {
    return `${bytes} B`
  }
}

/**
 *  格式化数字 (1000 -> 1k)
 */
const formatNumber = (num: number): string => {
  if (!num || num === 0) return '0'

  if (num >= 10000) {
    return `${(num / 10000).toFixed(1)}w`
  } else if (num >= 1000) {
    return `${(num / 1000).toFixed(1)}k`
  } else {
    return num.toString()
  }
}

/**
 *  格式化时间
 */
const formatTime = (time: string): string => {
  if (!time) return ''

  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < 7 * day) {
    return `${Math.floor(diff / day)}天前`
  } else {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }
}

/**
 * 高亮关键词
 */
const highlightText = (text: string, keyword?: string): string => {
  if (!keyword || !text) return text

  try {
    // 转义特殊字符
    const escapedKeyword = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
    const regex = new RegExp(`(${escapedKeyword})`, 'gi')

    // 替换为带高亮样式的HTML（使用设计系统的 accent 色）
    return text.replace(regex, '<span style="color: #FF6B35; font-weight: 600; background: rgba(255, 107, 53, 0.1); padding: 0 4px; border-radius: 4px;">$1</span>')
  } catch (error) {
    return text
  }
}

/**
 *  头像加载失败处理
 */
const handleAvatarError = (e: any) => {
  e.target.src = defaultAvatar
}

/**
 *  触摸/鼠标交互
 */
const onTouchStart = () => {
  isActive.value = true
}

const onTouchEnd = () => {
  setTimeout(() => {
    isActive.value = false
  }, 150)
}

const onMouseEnter = () => {
  if (!isMobile.value) {
    isActive.value = true
  }
}

const onMouseLeave = () => {
  if (!isMobile.value) {
    isActive.value = false
  }
}

/**
 *  点击卡片
 */
const handleClick = () => {
  emit('click', props.resource)
}

/**
 *  点击下载按钮
 */
const handleDownload = () => {
  emit('download', props.resource)
}

/**
 *  点击点赞按钮
 */
const handleLike = () => {
  emit('like', props.resource)
}
</script>

<style scoped lang="scss">
.resource-card {
  background: #FDFDFE;
  border-radius: 16rpx;
  padding: 20rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
  overflow: hidden;

  // 悬停/激活效果
  &.is-active,
  &:hover {
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);
    transform: translateY(-4rpx);
  }

  // 移动端适配
  &.is-mobile {
    &.is-active {
      background: #F8F9FA;
      transform: scale(0.98);
    }
  }
}

// 顶部标题行
.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 8rpx;
}

.title-row {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12rpx;
  min-width: 0;
}

.type-icon {
  flex-shrink: 0;
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12rpx;
  font-size: 24rpx;

  // 不同类型的渐变背景 - 统一颜色体系
  // 课件 - 蓝色系
  &.type-0,
  &.type-courseware {
    background: linear-gradient(135deg, #4A90E2 0%, #357ABD 100%);
  }

  // 试题 - 橙色系
  &.type-1,
  &.type-paper {
    background: linear-gradient(135deg, #FF9500 0%, #FF6B35 100%);
  }

  // 笔记 - 紫色系
  &.type-2,
  &.type-note {
    background: linear-gradient(135deg, #9B59B6 0%, #8E44AD 100%);
  }

  // 教材 - 红色系
  &.type-3,
  &.type-textbook {
    background: linear-gradient(135deg, #E74C3C 0%, #C0392B 100%);
  }

  // 实验报告 - 青色系
  &.type-4,
  &.type-report {
    background: linear-gradient(135deg, #1ABC9C 0%, #16A085 100%);
  }
}

.title {
  flex: 1;
  font-size: 30rpx;
  font-weight: 600;
  color: #111827;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: break-word;
  letter-spacing: 0.2rpx;
}

// 点赞数弱提示 (标题右侧)
.likes-hint {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: 4rpx 10rpx;
  border-radius: 12rpx;
  background: rgba(248, 113, 113, 0.08);
  margin-left: 8rpx;

  .like-icon-hint {
    font-size: 18rpx;
    color: #F87171;
  }

  .like-count {
    font-size: 20rpx;
    color: #6B7280;
    font-weight: 500;
  }
}

// 审核状态标签
.status-badge {
  flex-shrink: 0;
  padding: 6rpx 12rpx;
  border-radius: 28rpx;
  font-size: 20rpx;
  font-weight: 500;
  white-space: nowrap;

  &.status-0 {
    background: rgba(156, 163, 175, 0.12);
    color: #6B7280;
  }

  &.status-1 {
    background: rgba(16, 185, 129, 0.12);
    color: #10B981;
  }

  &.status-2 {
    background: rgba(239, 68, 68, 0.12);
    color: #EF4444;
  }
}

// 📌 第二层:描述 + 文件类型
.description-section {
  display: flex;
  align-items: flex-start;
  gap: 10rpx;
  margin-bottom: 12rpx;
}

.description {
  flex: 1;
  font-size: 24rpx;
  color: #6B7280;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: break-word;
  letter-spacing: 0.1rpx;
}

.file-type-badge {
  flex-shrink: 0;
  padding: 4rpx 10rpx;
  border-radius: 10rpx;
  font-size: 20rpx;
  font-weight: 500;
  white-space: nowrap;
  background: rgba(59, 130, 246, 0.1);
  color: #3B82F6;

  // 不同文件类型的颜色
  &.filetype-pdf {
    background: rgba(239, 68, 68, 0.1);
    color: #EF4444;
  }

  &.filetype-docx,
  &.filetype-doc {
    background: rgba(37, 99, 235, 0.1);
    color: #2563EB;
  }

  &.filetype-pptx,
  &.filetype-ppt {
    background: rgba(249, 115, 22, 0.1);
    color: #F97316;
  }

  &.filetype-xlsx,
  &.filetype-xls {
    background: rgba(34, 197, 94, 0.1);
    color: #22C55E;
  }

  &.filetype-zip,
  &.filetype-rar {
    background: rgba(147, 51, 234, 0.1);
    color: #9333EA;
  }
}

// 📌 第三层:元信息 (课程/大小/上传者/时间)
.meta-section {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 12rpx;
  flex-wrap: wrap;
  font-size: 22rpx;
  color: #9CA3AF;
}

.tag {
  display: inline-flex;
  align-items: center;
  padding: 4rpx 10rpx;
  border-radius: 10rpx;
  font-size: 20rpx;
  font-weight: 500;
  white-space: nowrap;

  &.course-tag {
    background: rgba(99, 102, 241, 0.1);
    color: #6366F1;
  }

  &.size-tag {
    background: rgba(107, 114, 128, 0.1);
    color: #6B7280;
  }
}

.meta-divider {
  color: #D1D5DB;
  flex-shrink: 0;
}

.meta-text {
  font-size: 22rpx;
  color: #6B7280;
  white-space: nowrap;

  &.meta-time {
    color: #9CA3AF;
  }
}

// 📌 第四层:行为区 (积分+下载量+操作按钮)
.action-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
  padding-top: 10rpx;
  border-top: 1rpx solid #F3F4F6;
}

.stats-group {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex: 1;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4rpx;
  font-size: 22rpx;
  font-weight: 500;
  padding: 4rpx 10rpx;
  border-radius: 10rpx;
  transition: all 0.2s ease;

  &.stat-points {
    color: #F59E0B;
    font-weight: 600;
    background: linear-gradient(135deg, rgba(251, 191, 36, 0.15) 0%, rgba(245, 158, 11, 0.15) 100%);

    .stat-icon {
      font-size: 18rpx;
    }

    .stat-value {
      color: #D97706;
      font-weight: 700;
    }
  }

  &.stat-downloads {
    color: #6B7280;
    background: rgba(107, 114, 128, 0.08);

    .stat-icon {
      font-size: 16rpx;
      color: #9CA3AF;
    }
  }
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex-shrink: 0;
}

// 统一的图标按钮样式 (降权设计)
.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  background: transparent;
  border: 1.5rpx solid #E5E7EB;

  &:hover {
    transform: scale(1.1);
    border-color: #D1D5DB;
  }

  &:active {
    transform: scale(0.95);
  }

  .icon-btn-icon {
    font-size: 24rpx;
    color: #9CA3AF;
    line-height: 1;
    transition: all 0.2s;
  }

  &.like-btn {
    .icon-btn-icon {
      color: #F87171;
    }

    &.is-liked {
      background: #F87171;
      border-color: #F87171;
      box-shadow: 0 2rpx 8rpx rgba(248, 113, 113, 0.3);

      .icon-btn-icon {
        color: #FFFFFF;
        animation: like-bounce 0.4s ease-in-out;
      }

      &:hover {
        background: #EF4444;
        border-color: #EF4444;
        box-shadow: 0 4rpx 12rpx rgba(239, 68, 68, 0.4);
      }
    }
  }

  &.download-btn {
    .icon-btn-icon {
      color: #64748B;
    }

    &.is-downloaded {
      background: #10B981;
      border-color: #10B981;

      .icon-btn-icon {
        color: #FFFFFF;
      }

      &:hover {
        background: #059669;
        border-color: #059669;
      }
    }
  }
}

@keyframes like-bounce {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.15); }
}

//  响应式适配
@media (max-width: 768px) {
  .title {
    font-size: 28rpx;
    line-height: 1.5;
  }

  .description {
    font-size: 22rpx;
    line-height: 1.6;
  }

  .card-header {
    margin-bottom: 8rpx;
  }

  .meta-row {
    margin-bottom: 12rpx;
    gap: 8rpx;
    flex-direction: column;
    align-items: flex-start;
  }

  .tags-left {
    width: 100%;
  }

  .file-info {
    gap: 4rpx;
    max-width: 100%;
    width: 100%;
    justify-content: flex-start;
  }

  .tag {
    font-size: 18rpx;
    padding: 4rpx 10rpx;
  }

  .card-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 10rpx;
    padding-top: 10rpx;
  }

  .user-info {
    width: 100%;
  }

  .right-section {
    width: 100%;
    justify-content: space-between;
  }

  .stats-row {
    flex: 1;
    justify-content: flex-start;
  }

  .download-btn {
    padding: 6rpx 12rpx;
  }

  .download-icon,
  .download-text {
    font-size: 20rpx;
  }
}

//  深色模式适配
@media (prefers-color-scheme: dark) {
  .resource-card {
    background: rgba(31, 41, 55, 0.7);

    &.is-active,
    &:hover {
      background: rgba(31, 41, 55, 0.9);
    }
  }

  .title {
    color: #F9FAFB;
  }

  .description {
    color: #D1D5DB;
  }

  .card-footer {
    border-top-color: rgba(75, 85, 99, 0.5);
  }
}
</style>
