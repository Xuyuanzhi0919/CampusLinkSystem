<template>
  <view class="empty-state" :class="'type-' + type">
    <!-- SVG 插画 -->
    <view class="empty-illustration">
      <EmptyIllustration :type="type" />
    </view>

    <!-- 主文案 -->
    <text class="empty-title">{{ title || defaultTitle }}</text>

    <!-- 副文案 -->
    <text v-if="description || defaultDescription" class="empty-description">
      {{ description || defaultDescription }}
    </text>

    <!-- 智能推荐 -->
    <view v-if="showRecommendations && recommendations.length > 0" class="recommendations">
      <text class="recommendations-title">你可能感兴趣</text>
      <view class="recommendation-list">
        <view
          v-for="item in recommendations"
          :key="item.id"
          class="recommendation-item"
          @click="handleRecommendationClick(item)"
        >
          <text class="rec-icon">{{ item.icon }}</text>
          <text class="rec-text">{{ item.text }}</text>
        </view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view v-if="showButton" class="empty-actions">
      <view
        class="action-btn"
        :class="'btn-' + type"
        @click="handleAction"
      >
        <!-- SVG 图标 -->
        <view v-if="showSvgIcon" class="btn-icon-svg">
          <!-- 刷新图标 -->
          <svg v-if="type === 'empty' || type === 'error' || type === 'network'"
               class="icon-svg" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path d="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"
                  fill="currentColor"/>
          </svg>

          <!-- 创建图标 -->
          <svg v-else-if="type === 'create'"
               class="icon-svg" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z" fill="currentColor"/>
          </svg>

          <!-- 搜索图标 -->
          <svg v-else-if="type === 'search'"
               class="icon-svg" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"
                  fill="currentColor"/>
          </svg>

          <!-- 筛选图标 -->
          <svg v-else-if="type === 'filter'"
               class="icon-svg" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
            <path d="M10 18h4v-2h-4v2zM3 6v2h18V6H3zm3 7h12v-2H6v2z" fill="currentColor"/>
          </svg>
        </view>

        <!-- 自定义图标 -->
        <text v-else class="btn-icon">{{ buttonIcon }}</text>

        <text class="btn-text">{{ buttonText || defaultButtonText }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import EmptyIllustration from './EmptyIllustration.vue'

// Props
interface Props {
  type?: 'empty' | 'error' | 'network' | 'search' | 'filter' | 'create'
  icon?: string
  title?: string
  description?: string
  buttonText?: string
  buttonIcon?: string
  showButton?: boolean
  showRecommendations?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: 'empty',
  showButton: true,
  buttonIcon: '',
  showRecommendations: true
})

// Emits
const emit = defineEmits<{
  action: []
  recommendationClick: [item: any]
}>()

// 智能推荐数据
const recommendations = ref<any[]>([])

// 根据类型生成推荐
const generateRecommendations = () => {
  const recommendationMap: Record<string, any[]> = {
    empty: [
      { id: 1, icon: '📚', text: '浏览热门资源' },
      { id: 2, icon: '💬', text: '查看问答社区' },
      { id: 3, icon: '🎯', text: '发现任务' }
    ],
    filter: [
      { id: 1, icon: '🔄', text: '重置筛选' },
      { id: 2, icon: '📋', text: '查看全部' },
      { id: 3, icon: '🔍', text: '换个条件试试' }
    ],
    search: [
      { id: 1, icon: '🔥', text: '热门搜索' },
      { id: 2, icon: '📖', text: '浏览分类' },
      { id: 3, icon: '✨', text: '发现精选' }
    ],
    create: [
      { id: 1, icon: '📤', text: '上传资料' },
      { id: 2, icon: '❓', text: '提个问题' },
      { id: 3, icon: '🤝', text: '发布任务' }
    ],
    error: [],
    network: []
  }

  recommendations.value = recommendationMap[props.type] || []
}

// 默认文案
const defaultTitle = computed(() => {
  const titles: Record<string, string> = {
    empty: '暂无内容',
    error: '加载失败',
    network: '网络连接失败',
    search: '未找到相关内容',
    filter: '暂无符合条件的内容',
    create: '还没有内容哦'
  }
  return titles[props.type]
})

const defaultDescription = computed(() => {
  const descriptions: Record<string, string> = {
    empty: '我们正在为你寻找精彩内容...',
    error: '数据加载出错了，请稍后重试',
    network: '请检查网络连接后重试',
    search: '换个关键词试试吧',
    filter: '试试调整筛选条件',
    create: '成为第一个分享者吧！'
  }
  return descriptions[props.type]
})

const defaultButtonText = computed(() => {
  const texts: Record<string, string> = {
    empty: '刷新试试',
    error: '重新加载',
    network: '重新连接',
    search: '清空搜索',
    filter: '重置筛选',
    create: '立即创建'
  }
  return texts[props.type]
})

// 默认按钮图标（使用 SVG）
const defaultButtonIcon = computed(() => {
  // 如果用户提供了自定义图标，使用自定义的
  if (props.buttonIcon) {
    return props.buttonIcon
  }

  // 否则返回空字符串，使用 SVG 图标
  return ''
})

// 是否显示 SVG 图标
const showSvgIcon = computed(() => {
  return !props.buttonIcon
})

// 处理按钮点击
const handleAction = () => {
  // 记录空状态交互
  logEmptyStateAction('button_click')
  emit('action')
}

// 处理推荐点击
const handleRecommendationClick = (item: any) => {
  // 记录推荐点击
  logEmptyStateAction('recommendation_click', item)
  emit('recommendationClick', item)
}

// 错误追踪：记录空状态出现
const logEmptyStateAction = (action: string, data?: any) => {
  const logData = {
    type: props.type,
    action,
    timestamp: Date.now(),
    data
  }

  // 存储到本地（用于分析）
  try {
    const logs = uni.getStorageSync('empty_state_logs') || []
    logs.push(logData)
    // 只保留最近 100 条
    if (logs.length > 100) {
      logs.shift()
    }
    uni.setStorageSync('empty_state_logs', logs)
  } catch (e) {
    console.error('记录空状态日志失败:', e)
  }

  // 可以在这里上报到服务器
  console.log('[EmptyState]', logData)
}

// 组件挂载时
onMounted(() => {
  generateRecommendations()
  // 记录空状态展示
  logEmptyStateAction('show')
})
</script>

<style scoped lang="scss">
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 40rpx;
  min-height: 400rpx;
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* SVG 插画 */
.empty-illustration {
  margin-bottom: 32rpx;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10rpx);
  }
}

/* 文案 */
.empty-title {
  font-size: 32rpx;
  font-weight: 600;
  color: var(--cl-gray-900, #1F2937);
  margin-bottom: 16rpx;
  text-align: center;
}

.empty-description {
  font-size: 28rpx;
  color: var(--cl-gray-600, #4B5563);
  line-height: 1.6;
  text-align: center;
  margin-bottom: 40rpx;
  max-width: 500rpx;
}

/* 智能推荐 */
.recommendations {
  width: 100%;
  max-width: 600rpx;
  margin-bottom: 32rpx;
  animation: slideUp 0.4s ease-out 0.2s both;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.recommendations-title {
  font-size: 24rpx;
  color: var(--cl-gray-600, #4B5563);
  margin-bottom: 16rpx;
  text-align: center;
  display: block;
}

.recommendation-list {
  display: flex;
  gap: 16rpx;
  justify-content: center;
  flex-wrap: wrap;
}

.recommendation-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 16rpx 24rpx;
  background: var(--cl-gray-50, #F8FAFC);
  border: 1px solid var(--cl-divider, #E5E7EB);
  border-radius: var(--radius-md, 12px);
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    background: var(--cl-primary-50, #F2F7FF);
    border-color: var(--cl-primary, #2E7CF6);
    transform: translateY(-2rpx);
    box-shadow: 0 4px 12px rgba(46, 124, 246, 0.15);
  }

  &:active {
    transform: scale(0.98);
  }
}

.rec-icon {
  font-size: 24rpx;
  line-height: 1;
}

.rec-text {
  font-size: 26rpx;
  color: var(--cl-gray-700, #374151);
  line-height: 1;
  white-space: nowrap;
}

/* 操作按钮 */
.empty-actions {
  display: flex;
  gap: 24rpx;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 20rpx 32rpx;
  border-radius: var(--radius-lg, 16px);
  background: var(--cl-primary, #2E7CF6);
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(46, 124, 246, 0.2);
  position: relative;
  overflow: hidden;

  /* 光泽效果 */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
    transition: left 0.5s;
  }

  &:hover {
    transform: translateY(-2rpx);
    box-shadow: 0 6px 16px rgba(46, 124, 246, 0.3);

    &::before {
      left: 100%;
    }
  }

  &:active {
    transform: scale(0.98);
  }
}

/* SVG 图标容器 */
.btn-icon-svg {
  width: 32rpx;
  height: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-svg {
  width: 100%;
  height: 100%;
  color: #FFFFFF;
  animation: iconRotate 0.6s ease-in-out;
}

@keyframes iconRotate {
  0% {
    transform: rotate(0deg);
    opacity: 0.8;
  }
  50% {
    transform: rotate(180deg);
    opacity: 1;
  }
  100% {
    transform: rotate(360deg);
    opacity: 1;
  }
}

/* 刷新图标特殊动画 */
.action-btn:hover .icon-svg {
  animation: iconSpin 0.8s ease-in-out;
}

@keyframes iconSpin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.btn-icon {
  font-size: 28rpx;
  line-height: 1;
}

.btn-text {
  font-size: 28rpx;
  font-weight: 600;
  color: #FFFFFF;
  line-height: 1;
}

/* 不同类型的样式 */
.type-error .empty-icon {
  background: rgba(220, 38, 38, 0.08);
}

.type-error .action-btn {
  background: var(--cl-error, #DC2626);
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.2);
}

.type-network .empty-icon {
  background: rgba(245, 158, 11, 0.08);
}

.type-network .action-btn {
  background: var(--cl-warning, #F59E0B);
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.2);
}

.type-create .empty-icon {
  background: rgba(16, 185, 129, 0.08);
}

.type-create .action-btn {
  background: var(--cl-success, #10B981);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
}

/* 深色模式 */
@media (prefers-color-scheme: dark) {
  .empty-icon {
    background: rgba(255, 255, 255, 0.05);
  }

  .empty-title {
    color: var(--cl-gray-100, #F3F4F6);
  }

  .empty-description {
    color: var(--cl-gray-400, #9CA3AF);
  }
}
</style>

