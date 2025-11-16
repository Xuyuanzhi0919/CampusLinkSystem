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

    <!-- 🎯 标签区域 -->
    <view class="tags-row">
      <!-- 课程名称标签 -->
      <view v-if="resource.courseName" class="tag course-tag">
        📖 {{ resource.courseName }}
      </view>

      <!-- 文件类型标签 -->
      <view class="tag file-tag">
        {{ getFileTypeIcon(resource.fileType) }} {{ getFileTypeText(resource.fileType) }}
      </view>

      <!-- 文件大小标签 -->
      <view v-if="resource.fileSize" class="tag size-tag">
        {{ formatFileSize(resource.fileSize) }}
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

      <!-- 右侧：统计信息 -->
      <view class="stats-row">
        <!-- 下载量 -->
        <view class="stat-item downloads">
          <text class="stat-icon">📥</text>
          <text class="stat-value">{{ formatNumber(resource.downloads) }}</text>
        </view>

        <!-- 点赞数 -->
        <view class="stat-item likes">
          <text class="stat-icon">❤️</text>
          <text class="stat-value">{{ formatNumber(resource.likes) }}</text>
        </view>

        <!-- 积分 -->
        <view class="stat-item points">
          <text class="stat-icon">💰</text>
          <text class="stat-value">{{ resource.score }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { ResourceItem } from '@/types/resource'
import { ResourceCategory, ResourceStatus } from '@/types/resource'

// Props
interface Props {
  resource: ResourceItem
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  click: [resource: ResourceItem]
}>()

// 🎯 响应式状态
const isActive = ref(false)
const isMobile = ref(false)
const defaultAvatar = 'https://via.placeholder.com/80x80/FF6B35/FFFFFF?text=U'

// 🎯 检测设备类型
onMounted(() => {
  checkDevice()
  window.addEventListener('resize', checkDevice)
})

const checkDevice = () => {
  isMobile.value = window.innerWidth < 768
}

/**
 * 🎯 获取资源类型图标
 */
const getTypeIcon = (category: number | string | undefined): string => {
  const categoryNum = typeof category === 'string' ? parseInt(category) : (category || 0)
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
</script>

<style scoped lang="scss">
.resource-card {
  background: #FDFDFE;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
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
  margin-bottom: 16rpx;
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

  // 🎯 不同类型的渐变背景
  &.type-0 {
    background: linear-gradient(135deg, #667EEA 0%, #764BA2 100%);
  }

  &.type-1 {
    background: linear-gradient(135deg, #F093FB 0%, #F5576C 100%);
  }

  &.type-2 {
    background: linear-gradient(135deg, #FFA500 0%, #FF6B35 100%);
  }
}

.title {
  flex: 1;
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2937;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: break-word;
}

// 🎯 审核状态标签
.status-badge {
  flex-shrink: 0;
  padding: 8rpx 16rpx;
  border-radius: 24rpx;
  font-size: 22rpx;
  font-weight: 500;
  white-space: nowrap;

  &.status-0 {
    background: rgba(156, 163, 175, 0.15);
    color: #6B7280;
  }

  &.status-1 {
    background: rgba(16, 185, 129, 0.15);
    color: #10B981;
  }

  &.status-2 {
    background: rgba(239, 68, 68, 0.15);
    color: #EF4444;
  }
}

// 🎯 描述区域
.description {
  font-size: 28rpx;
  color: #6B7280;
  line-height: 1.6;
  margin-bottom: 16rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: break-word;
}

// 🎯 标签行
.tags-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.tag {
  display: inline-flex;
  align-items: center;
  padding: 8rpx 16rpx;
  border-radius: 24rpx;
  font-size: 22rpx;
  font-weight: 500;
  white-space: nowrap;

  &.course-tag {
    background: rgba(99, 102, 241, 0.1);
    color: #6366F1;
  }

  &.file-tag {
    background: rgba(59, 130, 246, 0.1);
    color: #3B82F6;
  }

  &.size-tag {
    background: rgba(107, 114, 128, 0.1);
    color: #6B7280;
  }
}

// 🎯 底部元数据行
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
  padding-top: 16rpx;
  border-top: 2rpx solid #F3F4F6;
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
  gap: 24rpx;
  flex-shrink: 0;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4rpx;
  font-size: 24rpx;
  font-weight: 500;

  &.downloads {
    color: #3B82F6;
  }

  &.likes {
    color: #EF4444;
  }

  &.points {
    color: #F59E0B;
    font-weight: 600;
  }
}

.stat-icon {
  font-size: 28rpx;
}

.stat-value {
  font-size: 24rpx;
}

// 🎯 响应式适配
@media (max-width: 768px) {
  .resource-card {
    border-radius: 12rpx;
    padding: 20rpx;
  }

  .title {
    font-size: 30rpx;
  }

  .description {
    font-size: 26rpx;
  }

  .card-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 12rpx;
  }

  .user-info {
    width: 100%;
  }

  .stats-row {
    width: 100%;
    justify-content: flex-end;
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
