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
    <!-- 🎯 顶部标题行 -->
    <view class="card-header">
      <view class="title-row">
        <!-- 资源类型图标 -->
        <view class="type-icon" :class="`type-${resource.category || 0}`">
          {{ getTypeIcon(resource.category) }}
        </view>

        <!-- 标题 -->
        <text class="title">{{ resource.title }}</text>
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

    <!-- 🎯 资源描述 -->
    <view class="description">{{ resource.description }}</view>

    <!-- 🎯 标签和文件信息行 -->
    <view class="meta-row">
      <!-- 左侧：课程标签 -->
      <view class="tags-left">
        <view v-if="resource.courseName" class="tag course-tag">
          📖 {{ resource.courseName }}
        </view>
      </view>

      <!-- 右侧：文件信息 -->
      <view class="file-info">
        <view class="tag file-tag">
          {{ getFileTypeIcon(resource.fileType) }} {{ getFileTypeText(resource.fileType) }}
        </view>
        <view v-if="resource.fileSize" class="tag size-tag">
          {{ formatFileSize(resource.fileSize) }}
        </view>
      </view>
    </view>

    <!-- 🎯 底部元数据行 -->
    <view class="card-footer">
      <!-- 左侧：作者信息 -->
      <view class="user-info">
        <image
          class="avatar"
          :src="resource.uploaderAvatar || defaultAvatar"
          mode="aspectFill"
          @error="handleAvatarError"
        />
        <text class="username">{{ resource.uploaderName }}</text>
        <text class="time-separator">·</text>
        <text class="time">{{ formatTime(resource.createdAt) }}</text>
      </view>

      <!-- 右侧：统计信息和下载按钮 -->
      <view class="right-section">
        <!-- 统计信息 -->
        <view class="stats-row">
          <!-- 积分价格 -->
          <view v-if="!resource.isDownloaded" class="stat-item points">
            <text class="stat-icon">💰</text>
            <text class="stat-value">{{ resource.score }}</text>
          </view>

          <!-- 下载量 -->
          <view class="stat-item downloads">
            <text class="stat-icon">↓</text>
            <text class="stat-value">{{ formatNumber(resource.downloads) }}</text>
          </view>

          <!-- 点赞数 -->
          <view class="stat-item likes">
            <text class="stat-icon">♥</text>
            <text class="stat-value">{{ formatNumber(resource.likes) }}</text>
          </view>
        </view>

        <!-- 点赞按钮 -->
        <view
          class="like-btn"
          :class="{ 'is-liked': resource.isLiked }"
          @click.stop="handleLike"
        >
          <text class="like-icon">{{ resource.isLiked ? '♥' : '♡' }}</text>
        </view>

        <!-- 下载按钮 -->
        <view
          class="download-btn"
          :class="{ 'is-downloaded': resource.isDownloaded }"
          @click.stop="handleDownload"
        >
          <text class="download-icon">{{ resource.isDownloaded ? '↓' : '↓' }}</text>
          <text class="download-text">
            {{ resource.isDownloaded ? '重新下载' : `${resource.score}积分` }}
          </text>
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
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  click: [resource: ResourceItem]
  download: [resource: ResourceItem]
  like: [resource: ResourceItem]
}>()

// 🎯 响应式状态
const isActive = ref(false)
const isMobile = ref(false)
const defaultAvatar = PLACEHOLDER_IMAGES.avatar

// 🎯 检测设备类型
onMounted(() => {
  checkDevice()
  window.addEventListener('resize', checkDevice)
})

const checkDevice = () => {
  isMobile.value = window.innerWidth < 768
}

/**
 * 🎯 获取资源类型图标 - 支持统一颜色体系
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
 * 🎯 获取审核状态文本
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
 * 🎯 获取文件类型图标
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
 * 🎯 获取文件类型文本
 */
const getFileTypeText = (fileType: string): string => {
  return fileType?.toUpperCase() || 'FILE'
}

/**
 * 🎯 格式化文件大小
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
 * 🎯 格式化数字 (1000 -> 1k)
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
 * 🎯 格式化时间
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
 * 🎯 头像加载失败处理
 */
const handleAvatarError = (e: any) => {
  e.target.src = defaultAvatar
}

/**
 * 🎯 触摸/鼠标交互
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
 * 🎯 点击卡片
 */
const handleClick = () => {
  emit('click', props.resource)
}

/**
 * 🎯 点击下载按钮
 */
const handleDownload = () => {
  emit('download', props.resource)
}

/**
 * 🎯 点击点赞按钮
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

  // 🎯 悬停/激活效果
  &.is-active,
  &:hover {
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);
    transform: translateY(-4rpx);
  }

  // 🎯 移动端适配
  &.is-mobile {
    &.is-active {
      background: #F8F9FA;
      transform: scale(0.98);
    }
  }
}

// 🎯 顶部标题行
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

  // 🎯 不同类型的渐变背景 - 统一颜色体系
  // 课件 - 蓝色系
  &.type-0,
  &.type-课件 {
    background: linear-gradient(135deg, #4A90E2 0%, #357ABD 100%);
  }

  // 试题 - 橙色系
  &.type-1,
  &.type-试卷 {
    background: linear-gradient(135deg, #FF9500 0%, #FF6B35 100%);
  }

  // 笔记 - 紫色系
  &.type-2,
  &.type-笔记 {
    background: linear-gradient(135deg, #9B59B6 0%, #8E44AD 100%);
  }

  // 教材 - 红色系
  &.type-教材 {
    background: linear-gradient(135deg, #E74C3C 0%, #C0392B 100%);
  }

  // 实验报告 - 青色系
  &.type-实验报告 {
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

// 🎯 审核状态标签
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

// 🎯 描述区域
.description {
  font-size: 24rpx;
  color: #6B7280;
  line-height: 1.6;
  margin-bottom: 10rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: break-word;
  letter-spacing: 0.1rpx;
}

// 🎯 标签和文件信息行
.meta-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12rpx;
  margin-bottom: 12rpx;
  min-width: 0;
  overflow: hidden;
}

.tags-left {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 6rpx;
  flex-shrink: 1;
  min-width: 0;
  max-width: 50%;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.tag {
  display: inline-flex;
  align-items: center;
  padding: 6rpx 12rpx;
  border-radius: 28rpx;
  font-size: 20rpx;
  font-weight: 500;
  white-space: nowrap;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;

  &.course-tag {
    background: rgba(99, 102, 241, 0.1);
    color: #6366F1;
  }

  &.file-tag {
    background: rgba(59, 130, 246, 0.1);
    color: #3B82F6;
    flex-shrink: 1;
  }

  &.size-tag {
    background: rgba(107, 114, 128, 0.1);
    color: #6B7280;
    flex-shrink: 0;
  }
}

// 🎯 底部元数据行
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  padding-top: 10rpx;
  border-top: 1rpx solid #F3F4F6;
}

.user-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8rpx;
  min-width: 0;
}

.avatar {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  flex-shrink: 0;
  background: #E5E7EB;
}

.username {
  font-size: 24rpx;
  color: #6B7280;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.time-separator {
  color: #D1D5DB;
  font-size: 24rpx;
  flex-shrink: 0;
}

.time {
  font-size: 24rpx;
  color: #9CA3AF;
  flex-shrink: 0;
}

// 🎯 统计信息行
.stats-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex-shrink: 0;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4rpx;
  font-size: 22rpx;
  font-weight: 500;
  padding: 2rpx 6rpx;
  border-radius: 8rpx;
  transition: all 0.2s ease;

  &.downloads {
    color: #64748B;
    background: rgba(100, 116, 139, 0.06);

    .stat-icon {
      color: #64748B;
    }

    &:hover {
      background: rgba(100, 116, 139, 0.12);
    }
  }

  &.likes {
    color: #94A3B8;
    background: rgba(248, 113, 113, 0.06);

    .stat-icon {
      color: #F87171;
    }

    &:hover {
      background: rgba(248, 113, 113, 0.12);
    }
  }

  &.points {
    color: #F59E0B;
    font-weight: 700;
    background: linear-gradient(135deg, rgba(251, 191, 36, 0.15) 0%, rgba(245, 158, 11, 0.15) 100%);
    padding: 4rpx 8rpx;
    border-radius: 12rpx;

    .stat-icon {
      color: #F59E0B;
      font-size: 20rpx;
    }

    .stat-value {
      color: #D97706;
      font-weight: 700;
    }
  }
}

.stat-icon {
  font-size: 24rpx;
  font-weight: 400;
}

.stat-value {
  font-size: 22rpx;
}

// 🎯 右侧区域（统计+点赞+下载按钮）
.right-section {
  display: flex;
  align-items: center;
  gap: 10rpx;
  flex-shrink: 0;
  padding-right: 4rpx;
}

// 🎯 点赞按钮
.like-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48rpx;
  height: 48rpx;
  background: rgba(248, 113, 113, 0.12);
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1rpx solid rgba(248, 113, 113, 0.2);

  &:hover {
    transform: scale(1.15);
    background: rgba(248, 113, 113, 0.2);
    border-color: rgba(248, 113, 113, 0.4);
    box-shadow: 0 4rpx 12rpx rgba(248, 113, 113, 0.25);
  }

  &:active {
    transform: scale(0.95);
  }

  &.is-liked {
    background: linear-gradient(135deg, #F87171 0%, #EF4444 100%);
    box-shadow: 0 4rpx 12rpx rgba(239, 68, 68, 0.35);
    border-color: transparent;

    .like-icon {
      color: #FFFFFF;
      animation: like-bounce 0.5s ease-in-out;
    }

    &:hover {
      box-shadow: 0 6rpx 16rpx rgba(239, 68, 68, 0.45);
      transform: scale(1.15);
    }
  }
}

.like-icon {
  font-size: 26rpx;
  color: #F87171;
  line-height: 1;
  transition: all 0.3s ease;
  filter: drop-shadow(0 1rpx 2rpx rgba(0, 0, 0, 0.1));
}

@keyframes like-bounce {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
}

// 🎯 下载按钮
.download-btn {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: 6rpx 14rpx;
  background: linear-gradient(135deg, #FFA726 0%, #FFB74D 100%);
  border-radius: 32rpx;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2rpx 6rpx rgba(255, 167, 38, 0.12);

  &:hover {
    transform: translateY(-2rpx);
    box-shadow: 0 4rpx 10rpx rgba(255, 167, 38, 0.2);
    background: linear-gradient(135deg, #FF9800 0%, #FFA726 100%);
  }

  &:active {
    transform: scale(0.96);
  }

  &.is-downloaded {
    background: linear-gradient(135deg, #26A69A 0%, #4DB6AC 100%);
    box-shadow: 0 2rpx 6rpx rgba(38, 166, 154, 0.12);

    &:hover {
      box-shadow: 0 4rpx 10rpx rgba(38, 166, 154, 0.2);
      background: linear-gradient(135deg, #00897B 0%, #26A69A 100%);
    }
  }
}

.download-icon {
  font-size: 22rpx;
  font-weight: 600;
  color: #FFFFFF;
  line-height: 1;
}

.download-text {
  font-size: 22rpx;
  font-weight: 600;
  color: #FFFFFF;
  white-space: nowrap;
}

// 🎯 响应式适配
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

// 🎯 深色模式适配
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
