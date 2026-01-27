<template>
  <view
    class="resource-card"
    :class="{ 'is-active': isActive, 'is-mobile': isMobile, 'is-hot': isHotResource }"
    @click="handleClick"
    @touchstart="onTouchStart"
    @touchend="onTouchEnd"
    @mouseenter="onMouseEnter"
    @mouseleave="onMouseLeave"
  >
    <!-- P1-3: 高价值资源角标 -->
    <view v-if="isHotResource" class="hot-badge">
      <text class="hot-badge-icon">🔥</text>
      <text class="hot-badge-text">HOT</text>
    </view>

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

    <!-- 📌 第四层:行为区 (积分+下载量) —— P1优化:移除操作按钮,只保留信息展示 -->
    <view class="action-section">
      <!-- 左侧:统计数据 (社会证明) -->
      <view class="stats-group">
        <!-- 积分价格 (弱化显示) -->
        <view class="stat-item stat-points">
          <text class="stat-icon">💰</text>
          <text class="stat-value">{{ resource.score }}</text>
        </view>

        <!-- 下载量 -->
        <view class="stat-item stat-downloads">
          <text class="stat-icon">↓</text>
          <text class="stat-value">{{ formatNumber(resource.downloads) }}</text>
        </view>

        <!-- 收藏数 (新增) -->
        <view v-if="resource.favorites && resource.favorites > 0" class="stat-item stat-favorites">
          <text class="stat-icon">★</text>
          <text class="stat-value">{{ formatNumber(resource.favorites) }}</text>
        </view>
      </view>

      <!-- 右侧:操作按钮组 —— P1优化:改为hover显示 -->
      <view class="action-buttons action-buttons--hover">
        <!-- 收藏按钮 (hover显示) -->
        <view
          class="icon-btn favorite-btn"
          :class="{ 'is-favorited': resource.isFavorited }"
          @click.stop="handleFavorite"
          :title="resource.isFavorited ? '取消收藏' : '收藏'"
        >
          <text class="icon-btn-icon">{{ resource.isFavorited ? '★' : '☆' }}</text>
        </view>

        <!-- 下载按钮 (hover显示) -->
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
import { ref, computed, onMounted } from 'vue'
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
  favorite: [resource: ResourceItem]
}>()

//  响应式状态
const isActive = ref(false)
const isMobile = ref(false)
const defaultAvatar = PLACEHOLDER_IMAGES.avatar

// P1-3: 判断是否为热门资源 (下载量 >= 100 或 收藏数 >= 50)
const isHotResource = computed(() => {
  return (props.resource.downloads >= 100) || ((props.resource.favorites || 0) >= 50)
})

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

/**
 *  点击收藏按钮 - P1新增
 */
const handleFavorite = () => {
  emit('favorite', props.resource)
}
</script>

<style scoped lang="scss">
.resource-card {
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 24rpx; // 20rpx→24rpx,增加内边距,减少拥挤感
  margin-bottom: 20rpx; // 16rpx→20rpx,增加卡片间距,增强呼吸感
  border: 1rpx solid #E5E7EB; // 🎯 新增:边框增强边界感
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04); // 🎯 优化:降低默认阴影强度
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
  overflow: hidden;

  // 悬停/激活效果
  &.is-active,
  &:hover {
    border-color: #2563EB; // 🎯 新增:品牌色边框呼应
    box-shadow: 0 8rpx 24rpx rgba(37, 99, 235, 0.12); // 🎯 优化:品牌色阴影
    transform: translateY(-4rpx);
  }

  // P1-3: 热门资源特殊样式
  &.is-hot {
    border-color: #FCA5A5; // 淡红色边框
    background: linear-gradient(135deg, #FFFFFF 0%, #FEF2F2 100%); // 微红渐变背景

    &:hover {
      border-color: #EF4444;
      box-shadow: 0 8rpx 24rpx rgba(239, 68, 68, 0.15);
    }
  }

  // 移动端适配
  &.is-mobile {
    &.is-active {
      background: #F9FAFB; // 🎯 优化:更柔和的激活态背景
      border-color: #2563EB;
      transform: scale(0.98);
    }
  }
}

// P1-3: 热门角标
.hot-badge {
  position: absolute;
  top: -2rpx;
  right: -2rpx;
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: 6rpx 12rpx;
  background: linear-gradient(135deg, #EF4444 0%, #DC2626 100%);
  border-radius: 0 16rpx 0 16rpx; // 右上角贴合卡片
  box-shadow: 0 4rpx 12rpx rgba(239, 68, 68, 0.3);
  z-index: 10;

  .hot-badge-icon {
    font-size: 20rpx;
    line-height: 1;
  }

  .hot-badge-text {
    font-size: 20rpx;
    font-weight: 700;
    color: #FFFFFF;
    letter-spacing: 0.5rpx;
    text-shadow: 0 1rpx 2rpx rgba(0, 0, 0, 0.2);
  }
}

// 顶部标题行
.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 12rpx; // 8rpx→12rpx,增加层级间距
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
  width: 52rpx; // 48rpx→52rpx,增加图标容器尺寸
  height: 52rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12rpx;
  font-size: 26rpx; // 24rpx→26rpx,增加emoji图标尺寸

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
  font-size: 32rpx; // 30rpx→32rpx,参考HotQuestions
  font-weight: 600; // 保持600,与HotQuestions一致
  color: #111827;
  line-height: 1.6; // 1.5→1.6,增强可读性
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
  padding: 6rpx 12rpx; // 4rpx 10rpx→6rpx 12rpx,增加内边距
  border-radius: 12rpx;
  background: rgba(248, 113, 113, 0.08);
  margin-left: 8rpx;

  .like-icon-hint {
    font-size: 20rpx; // 18rpx→20rpx,增加图标尺寸
    color: #F87171;
  }

  .like-count {
    font-size: 22rpx; // 20rpx→22rpx,增加字体尺寸
    color: #6B7280;
    font-weight: 500;
  }
}

// 审核状态标签
.status-badge {
  flex-shrink: 0;
  padding: 6rpx 14rpx; // 12rpx→14rpx,增加内边距
  border-radius: 28rpx;
  font-size: 22rpx; // 20rpx→22rpx,增加字体尺寸
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
  gap: 12rpx; // 10rpx→12rpx,增加间距
  margin-bottom: 16rpx; // 12rpx→16rpx,增加层级间距
}

.description {
  flex: 1;
  font-size: 26rpx; // 24rpx→26rpx,增加可读性
  font-weight: 400; // 明确字重
  color: #6B7280;
  line-height: 1.7; // 1.6→1.7,增强可读性
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
  font-size: 22rpx; // 20rpx→22rpx,增强可读性
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
  gap: 16rpx; // 🎯 优化:10rpx→16rpx,增强呼吸感
  margin-bottom: 14rpx; // 12rpx→14rpx,增强呼吸感
  flex-wrap: wrap;
  font-size: 24rpx; // 22rpx→24rpx
  color: #6B7280; // 🎯 优化:#9CA3AF→#6B7280,增强对比度
}

.tag {
  display: inline-flex;
  align-items: center;
  padding: 6rpx 14rpx; // 🎯 优化:4rpx 12rpx→6rpx 14rpx,增加舒适度
  border-radius: 10rpx;
  font-size: 22rpx; // 20rpx→22rpx
  font-weight: 500;
  white-space: nowrap;

  &.course-tag {
    background: rgba(37, 99, 235, 0.08); // 🎯 优化:使用品牌色系
    color: #2563EB;
  }

  &.size-tag {
    background: rgba(107, 114, 128, 0.08); // 🎯 优化:降低背景透明度
    color: #4B5563; // 🎯 优化:加深文字颜色,增强对比度
  }
}

.meta-divider {
  color: #D1D5DB;
  flex-shrink: 0;
}

.meta-text {
  font-size: 24rpx; // 22rpx→24rpx,增强可读性
  font-weight: 400; // 明确字重
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
  padding-top: 14rpx; // 10rpx→14rpx,增加顶部间距
  border-top: 1rpx solid #F3F4F6;
}

.stats-group {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex: 1;
}

// P1优化:统计数据弱化显示,不抢眼
.stat-item {
  display: flex;
  align-items: center;
  gap: 4rpx;
  font-size: 22rpx; // 从 24rpx → 22rpx,弱化
  font-weight: 400; // 从 500 → 400,弱化
  padding: 4rpx 10rpx; // 从 6rpx 12rpx → 4rpx 10rpx,缩小
  border-radius: 8rpx;
  transition: all 0.2s ease;

  .stat-icon {
    font-size: 18rpx;
  }

  .stat-value {
    color: #6B7280; // 统一为灰色
    font-weight: 500;
  }

  // 积分价格 - P1优化:大幅弱化
  &.stat-points {
    background: rgba(107, 114, 128, 0.06); // 从橙色渐变 → 灰色背景
    color: #9CA3AF; // 从橙色 → 灰色

    .stat-icon {
      opacity: 0.6; // 金币图标半透明
    }

    .stat-value {
      color: #6B7280; // 从橙色 → 灰色
      font-weight: 500; // 从 700 → 500
    }
  }

  // 下载量 - 保持原样
  &.stat-downloads {
    background: rgba(107, 114, 128, 0.06);
    color: #6B7280;
  }

  // 收藏数 - 新增
  &.stat-favorites {
    background: rgba(251, 191, 36, 0.1);
    color: #F59E0B;

    .stat-icon {
      color: #F59E0B;
    }
  }
}

// P1-2优化:操作按钮容器 - hover才显示
.action-buttons {
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex-shrink: 0;
  opacity: 0; // 默认隐藏
  transform: translateX(8rpx); // 默认右移
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  pointer-events: none; // 隐藏时不可点击

  // 卡片hover时显示
  .resource-card:hover &,
  .resource-card.is-active & {
    opacity: 1;
    transform: translateX(0);
    pointer-events: auto;
  }

  // 移动端:始终显示(因为没有hover)
  .resource-card.is-mobile & {
    opacity: 1;
    transform: translateX(0);
    pointer-events: auto;
  }
}

// P1优化:图标按钮样式 - 更轻量,不抢眼
.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52rpx; // 从 56rpx → 52rpx,缩小
  height: 52rpx;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  background: #FFFFFF; // 从浅灰 → 纯白
  border: 1.5rpx solid #E5E7EB;
  box-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.04); // 添加微弱阴影

  &:hover {
    transform: scale(1.1);
    box-shadow: 0 4rpx 8rpx rgba(0, 0, 0, 0.08);
    border-color: #D1D5DB;
  }

  &:active {
    transform: scale(0.95);
    box-shadow: 0 1rpx 2rpx rgba(0, 0, 0, 0.06);
  }

  .icon-btn-icon {
    font-size: 26rpx; // 从 28rpx → 26rpx,缩小
    color: #9CA3AF; // 从 #6B7280 → #9CA3AF,更浅
    line-height: 1;
    transition: all 0.2s;
  }

  // 收藏按钮 - 新增
  &.favorite-btn {
    .icon-btn-icon {
      color: #F59E0B;
    }

    &:hover:not(.is-favorited) {
      background: rgba(251, 191, 36, 0.08);
      border-color: #FCD34D;
    }

    &.is-favorited {
      background: #F59E0B;
      border-color: #F59E0B;
      box-shadow: 0 4rpx 12rpx rgba(245, 158, 11, 0.25);

      .icon-btn-icon {
        color: #FFFFFF;
      }

      &:hover {
        background: #D97706;
        border-color: #D97706;
        box-shadow: 0 6rpx 16rpx rgba(217, 119, 6, 0.35);
      }
    }
  }

  // 下载按钮 - P1优化:弱化处理
  &.download-btn {
    .icon-btn-icon {
      color: #2563EB;
    }

    &:hover:not(.is-downloaded) {
      background: rgba(37, 99, 235, 0.08);
      border-color: #93C5FD;
    }

    &.is-downloaded {
      background: #10B981;
      border-color: #10B981;
      box-shadow: 0 4rpx 12rpx rgba(16, 185, 129, 0.25);

      .icon-btn-icon {
        color: #FFFFFF;
      }

      &:hover {
        background: #059669;
        border-color: #059669;
        box-shadow: 0 6rpx 16rpx rgba(5, 150, 105, 0.35);
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
  .resource-card {
    padding: 20rpx; // 移动端稍微减少内边距
  }

  .title {
    font-size: 30rpx; // 28rpx→30rpx,移动端保持可读性
    line-height: 1.55;
  }

  .description {
    font-size: 24rpx; // 22rpx→24rpx,移动端保持可读性
    line-height: 1.6;
  }

  .card-header {
    margin-bottom: 10rpx;
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
