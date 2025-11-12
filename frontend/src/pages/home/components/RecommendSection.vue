<template>
  <view class="recommend-section">
    <!-- 优化：标题栏 - 增强视觉层级和导航感 -->
    <view class="section-header">
      <view class="title-wrapper">
        <text class="section-title">为你推荐</text>
        <text class="section-subtitle">小编精选 · 根据你的学习兴趣推荐</text>
      </view>

      <!-- 优化：筛选标签 - 添加 hover 底边条效果 -->
      <view class="filter-tags">
        <text
          v-for="(tab, index) in tabs"
          :key="tab.key"
          class="filter-tag"
          :class="{ active: currentTab === index }"
          @click="switchTab(index)"
        >
          {{ tab.name }}
        </text>
      </view>
    </view>

    <!-- 推荐卡片列表 -->
    <view class="recommend-list">
      <!-- 加载中：骨架屏 -->
      <template v-if="isLoading">
        <SkeletonCard
          v-for="i in 6"
          :key="'skeleton-' + i"
          layout="recommend"
        />
      </template>

      <!-- 优化：显示卡片（显示前 6 条，响应式网格）- stagger 30ms -->
      <template v-else-if="currentList.length > 0">
        <view
          v-for="(item, index) in displayList"
          :key="item.id"
          class="recommend-card"
          :class="{ 'swiping': swipingCardIndex === index }"
          :style="{
            animationDelay: `${index * 30}ms`,
            transform: swipingCardIndex === index ? `translateX(${swipeOffset}px)` : 'translateX(0)'
          }"
          @click="handleItemClick(item)"
          @touchstart="handleTouchStart($event, index)"
          @touchmove="handleTouchMove($event, index)"
          @touchend="handleTouchEnd($event, index)"
        >
          <!-- 重构：顶部色条（根据类型自动着色）-->
          <view class="card-top-bar" :class="'bar-' + item.type"></view>

          <!-- 优化：右上角徽标（热门/截止中）-->
          <view v-if="item.badge" class="card-badge" :class="'badge-' + item.badge.type">
            <text class="badge-icon">{{ item.badge.icon }}</text>
            <text class="badge-text">{{ item.badge.text }}</text>
          </view>

          <!-- 重构：类型标签 - 去掉背景色，仅保留图标+文字 -->
          <view class="type-tag" :class="'type-' + item.type">
            <text class="type-icon">{{ getTypeIcon(item.type) }}</text>
            <text class="type-text">{{ getTypeName(item.type) }}</text>
          </view>

          <!-- 优化：卡片内容 - 分层结构 -->
          <view class="card-content">
            <!-- 一级：标题（黑体）-->
            <text class="card-title">{{ item.title }}</text>
            <!-- 二级：摘要（灰度中等）-->
            <text class="card-intro">{{ item.intro }}</text>
          </view>

          <!-- 优化：三级 Meta 信息（灰度最轻）- 统一线性图标 -->
          <view class="card-meta">
            <view class="meta-item">
              <text class="meta-icon">👤</text>
              <text class="meta-text">{{ item.author }}</text>
            </view>

            <!-- 课件：显示下载数 -->
            <view v-if="item.type === 'resource'" class="meta-item">
              <text class="meta-icon">↓</text>
              <text class="meta-text">{{ item.downloads || 0 }}</text>
            </view>

            <!-- 问答：显示浏览数 · 回答数 -->
            <template v-else-if="item.type === 'question'">
              <view class="meta-item">
                <text class="meta-icon">👁</text>
                <text class="meta-text">{{ item.views || 0 }}</text>
              </view>
              <text class="meta-dot">·</text>
              <view class="meta-item">
                <text class="meta-icon">💬</text>
                <text class="meta-text">{{ item.answers || 0 }}</text>
              </view>
            </template>

            <!-- 任务：显示积分 · 截止时间 -->
            <template v-else-if="item.type === 'task'">
              <view class="meta-item meta-highlight">
                <text class="meta-icon">¥</text>
                <text class="meta-text">{{ item.points || 10 }}</text>
              </view>
              <text v-if="item.deadline" class="meta-dot">·</text>
              <view v-if="item.deadline" class="meta-item meta-urgent">
                <text class="meta-icon">⏱</text>
                <text class="meta-text">{{ item.deadline }}</text>
              </view>
            </template>
          </view>

          <!-- 重构：移除浮层，卡片整体可点击，hover 时显示箭头提示 -->
          <view class="card-click-indicator">
            <text class="indicator-arrow">→</text>
          </view>

          <!-- 操作按钮（合并成一个分组）- 文档规范：线性图标 -->
          <view class="card-actions">
            <view class="action-group">
              <view class="action-btn" @click.stop="handleCollect(item)">
                <text class="action-icon">{{ item.collected ? '♥' : '♡' }}</text>
                <text class="action-label">{{ item.collected ? '已收藏' : '收藏' }}</text>
              </view>
              <view class="action-divider"></view>
              <view class="action-btn" @click.stop="handleLike(item)">
                <text class="action-icon">↑</text>
                <text class="action-label">{{ item.likes || 0 }}</text>
              </view>
            </view>
          </view>
        </view>
      </template>

      <!-- 无数据：空状态 -->
      <template v-else>
        <view class="empty-wrapper">
          <EmptyState
            :type="currentTab === 0 ? 'empty' : 'filter'"
            @action="handleRefresh"
            @recommendation-click="handleRecommendationClick"
          />
        </view>
      </template>
    </view>

    <!-- 优化：换一批/查看全部条 - 淡蓝底 #F1F5FF，增强存在感 -->
    <view v-if="!isLoading && currentList.length > 0" class="action-bar">
      <!-- 精修：换一批按钮 - 精美旋转图标 -->
      <view class="action-btn refresh-btn" @click="handleRefresh">
        <text class="action-icon refresh-icon" :class="{ rotating: isRefreshing }">↻</text>
        <text class="action-text">换一批</text>
      </view>

      <!-- 分隔线 -->
      <view class="action-divider"></view>

      <!-- 优化：查看全部按钮 - 深蓝 + 箭头 -->
      <view class="action-btn view-all-btn" @click="viewAllRecommendations">
        <text class="action-text">查看全部</text>
        <text class="action-icon arrow-icon">→</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getResourceList } from '@/services/resource'
import { getQuestionList } from '@/services/question'
import { getTaskList } from '@/services/task'
import EmptyState from '@/components/EmptyState.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'

// Props & Emits
const emit = defineEmits<{
  itemClick: [item: any]
}>()

// Tab 配置
const tabs = [
  { name: '全部', key: 'all' },
  { name: '课件', key: 'resource' },
  { name: '问答', key: 'question' },
  { name: '任务', key: 'task' },
]

const currentTab = ref(0)
const isRefreshing = ref(false)
const isLoading = ref(false)

// 数据列表
const allList = ref<any[]>([])

// 滑动相关状态
const swipingCardIndex = ref<number | null>(null)
const swipeOffset = ref(0)
const touchStartX = ref(0)
const touchStartY = ref(0)
const isSwiping = ref(false)

// 当前列表（根据 Tab 筛选）
const currentList = computed(() => {
  if (currentTab.value === 0) return allList.value
  const type = tabs[currentTab.value].key
  return allList.value.filter((item) => item.type === type)
})

// 显示列表（显示前 6 条 - 2×3 网格）
const displayList = computed(() => {
  return currentList.value.slice(0, 6)
})

/**
 * 加载推荐数据
 */
const loadRecommendData = async () => {
  if (isLoading.value) return

  isLoading.value = true
  try {
    // 并行请求三个接口
    const [resourceRes, questionRes, taskRes] = await Promise.all([
      getResourceList({ page: 1, pageSize: 6, sortBy: 'downloads', sortOrder: 'desc' }),
      getQuestionList({ page: 1, pageSize: 6, sortBy: 'created_at', sortOrder: 'desc' }),
      getTaskList({ page: 1, pageSize: 6, status: 0 }) // 只获取待接单的任务
    ])

    // 优化：转换资源数据 - 添加徽标信息
    const resources = (resourceRes?.list || []).map((item: any) => ({
      id: item.resourceId,
      type: 'resource',
      title: item.title,
      intro: item.description || '',
      author: item.uploaderName || '匿名',
      downloads: item.downloads || 0,
      collected: item.isLiked || false,
      // 优化：添加徽标（热门资源）
      badge: (item.downloads || 0) > 100 ? { type: 'hot', icon: '🔥', text: '热门' } : null,
    }))

    // 优化：转换问答数据 - 添加徽标信息
    const questions = (questionRes?.list || []).map((item: any) => ({
      id: item.questionId,
      type: 'question',
      title: item.title,
      intro: item.content?.substring(0, 50) || '',
      author: item.askerName || '匿名',
      views: item.viewCount || 0,
      answers: item.answerCount || 0, // 回答数
      collected: false,
      // 优化：添加徽标（热门问答）
      badge: (item.viewCount || 0) > 200 ? { type: 'hot', icon: '🔥', text: '热门' } : null,
    }))

    // 优化：转换任务数据 - 添加徽标信息
    const tasks = (taskRes?.list || []).map((item: any) => {
      return {
        id: item.tid || item.taskId, // 修复:使用 tid 字段
        type: 'task',
        title: item.title,
        intro: item.description || '', // 使用 description 字段
        author: item.publisherNickname || item.publisherName || '匿名', // 修复:使用 publisherNickname
        points: item.rewardPoints || 10,
        deadline: item.deadline ? formatDeadline(item.deadline) : '', // 截止时间
        collected: false,
        // 优化：添加徽标（截止中）
        badge: item.deadline && isDeadlineSoon(item.deadline) ? { type: 'urgent', icon: '⏳', text: '截止中' } : null,
      }
    })

    // 合并并随机排序
    allList.value = [...resources, ...questions, ...tasks].sort(() => Math.random() - 0.5)
  } catch (error) {
    console.error('加载推荐数据失败:', error)
    allList.value = []
  } finally {
    isLoading.value = false
  }
}

/**
 * 切换 Tab
 */
const switchTab = (index: number) => {
  currentTab.value = index
}

/**
 * 换一批 - 带旋转动画 + 淡入淡出过渡
 */
const handleRefresh = async () => {
  if (isRefreshing.value) return

  isRefreshing.value = true

  // 延迟 300ms 让旋转动画完成
  setTimeout(async () => {
    await loadRecommendData()
    isRefreshing.value = false
    uni.showToast({ title: '已刷新', icon: 'success', duration: 1500 })
  }, 300)
}

/**
 * 卡片点击
 */
const handleItemClick = (item: any) => {
  emit('itemClick', item)
}

/**
 * 收藏
 */
const handleCollect = (item: any) => {
  item.collected = !item.collected
  uni.showToast({
    title: item.collected ? '已收藏' : '已取消收藏',
    icon: 'success',
  })
}

/**
 * 点赞
 */
const handleLike = (item: any) => {
  if (!item.likes) item.likes = 0
  item.liked = !item.liked
  item.likes += item.liked ? 1 : -1
  uni.showToast({
    title: item.liked ? '已点赞' : '已取消点赞',
    icon: 'success',
  })
}

/**
 * 格式化截止时间
 */
const formatDeadline = (deadline: string) => {
  const now = new Date()
  const end = new Date(deadline)
  const diff = end.getTime() - now.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days < 0) return '已截止'
  if (days === 0) return '今天截止'
  if (days === 1) return '明天截止'
  if (days <= 3) return `${days}天后`
  return `${days}天后`
}

/**
 * 优化：判断是否即将截止（3天内）
 */
const isDeadlineSoon = (deadline: string) => {
  const now = new Date()
  const end = new Date(deadline)
  const diff = end.getTime() - now.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  return days >= 0 && days <= 3
}

/**
 * 优化：获取类型图标 - 更新为新的图标
 */
const getTypeIcon = (type: string) => {
  const icons: Record<string, string> = {
    resource: '📘', // 课程 - 蓝色书本
    question: '💡', // 问答 - 灯泡
    task: '💼',     // 任务 - 公文包
  }
  return icons[type] || '📄'
}

/**
 * 获取类型名称
 */
const getTypeName = (type: string) => {
  const names: Record<string, string> = {
    resource: '课件',
    question: '问答',
    task: '任务',
  }
  return names[type] || '其他'
}

/**
 * 处理推荐点击
 */
const handleRecommendationClick = (item: any) => {
  console.log('推荐点击:', item)

  // 根据推荐类型执行不同操作
  const actionMap: Record<string, () => void> = {
    '浏览热门资源': () => switchTab(1),
    '查看问答社区': () => switchTab(2),
    '发现任务': () => switchTab(3),
    '重置筛选': () => switchTab(0),
    '查看全部': () => switchTab(0),
    '换个条件试试': () => handleRefresh()
  }

  const action = actionMap[item.text]
  if (action) {
    action()
  } else {
    uni.showToast({ title: item.text, icon: 'none' })
  }
}

/**
 * 查看全部推荐
 */
const viewAllRecommendations = () => {
  // TODO: 跳转到推荐列表页面
  uni.navigateTo({
    url: '/pages/recommend/index?tab=' + currentTab.value
  })
}

/**
 * 触摸开始 - 记录起始位置
 */
const handleTouchStart = (event: any, index: number) => {
  const touch = event.touches[0]
  touchStartX.value = touch.clientX
  touchStartY.value = touch.clientY
  swipingCardIndex.value = index
  isSwiping.value = false
}

/**
 * 触摸移动 - 实时更新偏移量
 */
const handleTouchMove = (event: any, index: number) => {
  if (swipingCardIndex.value !== index) return

  const touch = event.touches[0]
  const deltaX = touch.clientX - touchStartX.value
  const deltaY = touch.clientY - touchStartY.value

  // 判断是否为横向滑动（横向位移大于纵向位移）
  if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > 10) {
    isSwiping.value = true
    swipeOffset.value = deltaX

    // 阻止页面滚动
    event.preventDefault()
  }
}

/**
 * 触摸结束 - 判断是否触发切换
 */
const handleTouchEnd = (_event: any, index: number) => {
  if (swipingCardIndex.value !== index) return

  const SWIPE_THRESHOLD = 100 // 滑动阈值（像素）
  const offset = swipeOffset.value

  // 判断是否达到切换阈值
  if (Math.abs(offset) > SWIPE_THRESHOLD && isSwiping.value) {
    // 滑动距离足够，执行切换动画
    if (offset > 0) {
      // 向右滑动 - 显示上一个内容
      handleSwipeToNextContent(index, 'prev')
    } else {
      // 向左滑动 - 显示下一个内容
      handleSwipeToNextContent(index, 'next')
    }
  } else {
    // 滑动距离不足，恢复原位
    swipeOffset.value = 0
  }

  // 重置状态
  setTimeout(() => {
    swipingCardIndex.value = null
    isSwiping.value = false
  }, 300)
}

/**
 * 切换到下一个/上一个内容
 */
const handleSwipeToNextContent = (currentIndex: number, direction: 'prev' | 'next') => {
  const currentItem = displayList.value[currentIndex]
  const currentListItems = currentList.value
  const currentItemIndex = currentListItems.findIndex(item => item.id === currentItem.id)

  let targetIndex: number
  if (direction === 'next') {
    targetIndex = (currentItemIndex + 1) % currentListItems.length
  } else {
    targetIndex = (currentItemIndex - 1 + currentListItems.length) % currentListItems.length
  }

  const targetItem = currentListItems[targetIndex]

  // 执行滑出动画
  const exitOffset = direction === 'next' ? -500 : 500
  swipeOffset.value = exitOffset

  // 延迟替换内容
  setTimeout(() => {
    // 替换当前位置的内容
    allList.value = allList.value.map(item =>
      item.id === currentItem.id ? targetItem : item
    )

    // 重置偏移
    swipeOffset.value = 0
  }, 250)

  // 显示提示
  uni.showToast({
    title: direction === 'next' ? '下一个' : '上一个',
    icon: 'none',
    duration: 800
  })
}

// 组件挂载时加载数据
onMounted(() => {
  loadRecommendData()
})
</script>

<style scoped lang="scss">
/* 优化：为「为你推荐」区添加统一背景层（浅灰 #F8FAFC），让其独立于顶部 */
.recommend-section {
  background: #F8FAFC; /* 浅灰背景 */
  border-radius: 24rpx; /* 12px */
  padding: 48rpx; /* 24px */
  /* 优化：增加轻微阴影 */
  box-shadow: inset 0 2rpx 8rpx rgba(0, 0, 0, 0.02);
  transition: all 0.2s ease;
  animation: fadeInUp 0.4s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 优化：标题栏 - 增加底部分割线，增强栏目感 */
.section-header {
  display: flex;
  align-items: center;
  gap: 32rpx;
  margin-bottom: 32rpx;
  padding-bottom: 24rpx;
  position: relative;
  /* 优化：添加底部分割线 */
  border-bottom: 1px solid #E2E8F0;

  /* 优化：移除原有的底线装饰，改用完整的分割线 */
  /* &::after {
    content: '';
    position: absolute;
    left: 0;
    bottom: 0;
    width: 80rpx;
    height: 3rpx;
    background: var(--cl-primary, #3B82F6);
    border-radius: 2rpx;
  } */
}

/* 标题包装器 */
.title-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8rpx; /* 标题与副标题间距 */
}

/* 优化：标题 - 增强字重和视觉锚点 + 渐变文字 */
.section-title {
  font-size: 40rpx; /* 20px - 标题规范 */
  font-weight: 700; /* 优化：增强到 700，更突出 */
  /* 精修：渐变文字效果 - 从深蓝到亮蓝 */
  background: linear-gradient(135deg, #1E3A8A 0%, #3B82F6 50%, #60A5FA 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  line-height: 1;
  position: relative;
  padding-left: 24rpx; /* 优化：增加左侧间距，为色条留更多空间 */
  /* 精修：轻微文字阴影，增强立体感 */
  filter: drop-shadow(0 2rpx 4rpx rgba(59, 130, 246, 0.1));

  /* 优化：左侧淡蓝渐变底条装饰线 */
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 8rpx; /* 4px 宽度 - 增大宽度 */
    height: 40rpx; /* 20px 高度 */
    background: linear-gradient(180deg, #3B82F6 0%, #60A5FA 100%); /* 蓝色渐变 */
    border-radius: 4rpx; /* 圆角 */
    /* 精修：装饰条呼吸动画 */
    animation: pulse 2s ease-in-out infinite;
  }
}

/* 装饰条呼吸动画 */
@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

/* 优化：副标题 - 淡化灰度，字距稍加宽 */
.section-subtitle {
  font-size: 24rpx; /* 12px */
  font-weight: 400;
  color: #94A3B8; /* 优化：更淡的灰度 */
  line-height: 1.5;
  padding-left: 24rpx; /* 与标题对齐 */
  letter-spacing: 0.5rpx; /* 优化：字距稍加宽 */
}

/* 优化：筛选标签 - 增强导航感 */
.filter-tags {
  flex: 1;
  display: flex;
  gap: 24rpx;
}

.filter-tag {
  font-size: 28rpx; /* 14px - 正文规范 */
  color: var(--cl-gray-600, #64748B);
  padding: 10rpx 20rpx;
  border-radius: 12rpx;
  cursor: pointer;
  transition: all 0.3s ease; /* 优化：延长过渡时间 */
  position: relative;
  background: transparent;
  font-weight: 500;
  /* 优化：添加底边条（默认透明）*/
  border-bottom: 2px solid transparent;

  /* 优化：hover 时添加轻柔底边条（2px 蓝色）*/
  &:hover {
    color: var(--cl-primary, #2563EB);
    background: var(--cl-gray-100, #F1F5F9);
    border-bottom-color: #2563EB; /* 蓝色底边条 */
  }

  &.active {
    color: var(--cl-primary, #3B82F6);
    background: var(--cl-primary-100, #DBEAFE);
    font-weight: 600;

    /* 精修：激活状态添加底部渐变条 */
    &::after {
      content: '';
      position: absolute;
      bottom: -2px;
      left: 0;
      right: 0;
      height: 2px;
      background: linear-gradient(90deg, #2563EB 0%, #60A5FA 100%);
      border-radius: 1px;
      animation: slideIn 0.3s ease-out;
    }
  }
}

/* 渐变条滑入动画 */
@keyframes slideIn {
  from {
    transform: scaleX(0);
    opacity: 0;
  }
  to {
    transform: scaleX(1);
    opacity: 1;
  }
}

/* 文档规范：换一批/查看全部条 - 浅蓝底 #EEF2FF，圆角 12 */
.action-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  margin-top: 32rpx; /* 与卡片列表间距 */
  padding: 16rpx 24rpx;
  background: #EEF2FF; /* 文档规范：浅蓝底 */
  border-radius: 24rpx; /* 文档规范：圆角 12px */
  transition: all var(--transition-hover, 150ms ease);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
  background: transparent;
  color: var(--cl-primary, #2563EB);
  border: none;
  cursor: pointer;
  /* 精修：统一过渡时间，添加弹性缓动 */
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 500;
  border-radius: 16rpx; /* 精修：预设圆角 */

  &:hover {
    /* 精修：hover 背景更深，与底栏区分 */
    background: #E0EDFF;
    /* 精修：轻微放大，增强交互感 */
    transform: scale(1.02);
  }

  &:active {
    transform: scale(0.96);
    transition: transform 0.1s ease;
  }
}

.action-icon {
  font-size: 28rpx;
  line-height: 1;
  transition: transform var(--transition-hover, 150ms ease);
}

.action-text {
  font-size: 28rpx; /* 14px */
  font-weight: 500;
  line-height: 1;
}

.action-divider {
  width: 1px;
  height: 32rpx; /* 16px */
  background: var(--cl-gray-300, #D1D5DB);
  margin: 0 16rpx;
}

/* 精修：刷新按钮特殊样式 */
.refresh-btn {
  position: relative;

  /* 精修：hover 时刷新图标旋转 */
  &:hover .refresh-icon {
    transform: rotate(180deg);
  }
}

/* 精修：刷新图标过渡 */
.refresh-icon {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: inline-block;
  /* 精修：增强图标视觉效果 */
  font-size: 32rpx; /* 稍大一点，16px */
  font-weight: 600;
  color: var(--cl-primary, #2563EB);
  /* 精修：轻微阴影，增强立体感 */
  text-shadow: 0 1px 2px rgba(37, 99, 235, 0.1);

  /* 精修：刷新中的旋转动画 */
  &.rotating {
    animation: rotate360 0.6s cubic-bezier(0.4, 0, 0.2, 1) infinite;
  }
}

@keyframes rotate360 {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 精修：推荐列表 - 响应式网格布局 + 呼吸感间距 */
.recommend-list {
  display: grid;
  /* 优化：响应式网格 - 3列（大屏）、2列（中屏）、1列（手机）*/
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 48rpx; /* 精修：行间距 24px，增强呼吸感 */
  column-gap: 40rpx; /* 精修：列间距 20px，保持紧凑 */
  min-height: 400rpx;
  /* 精修：增加顶部间距，与标题区分 */
  margin-top: 32rpx;

  /* 内容交叉淡入淡出过渡 */
  transition: opacity 0.3s ease;

  /* 响应式断点 */
  @media (max-width: 1440px) {
    grid-template-columns: repeat(2, 1fr); /* 中屏：2列 */
    gap: 40rpx;
    column-gap: 32rpx;
  }

  @media (max-width: 960px) {
    grid-template-columns: 1fr; /* 小屏：1列 */
    gap: 48rpx; /* 精修：移动端保持 48rpx (24px)，避免拥挤 */
  }
}

/* 空状态容器 */
.empty-wrapper {
  grid-column: 1 / -1;
}

/* 重构：卡片 - 整体可点击，hover 轻微上浮+光感边框 */
.recommend-card {
  position: relative;
  padding: 32rpx; /* 内边距 16px */
  background: #FFFFFF; /* 纯白卡片 */
  border: 1px solid transparent; /* 重构：默认透明边框，为 hover 留空间 */
  border-radius: 24rpx; /* 圆角 12px */
  cursor: pointer;
  transition: all 0.25s ease; /* 统一过渡时间为 0.25s */
  min-height: 336rpx; /* 168px - 最小高度 */
  /* 重构：统一阴影标准 - 0 4px 10px rgba(0,0,0,0.05) */
  box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.05);
  overflow: visible; /* 重构：允许色条溢出 */
  display: flex;
  flex-direction: column;
  gap: 16rpx; /* 统一间距 8px */

  /* 淡入动画（stagger 30ms）*/
  animation: fadeInCard 240ms ease-out both;

  /* 滑动状态 - 禁用过渡以实现实时跟手效果 */
  &.swiping {
    transition: none;
    box-shadow: 0 12rpx 32rpx rgba(0, 0, 0, 0.12);
    opacity: 0.9;
  }

  /* 重构：Hover 状态 - 上浮 4px + 光感渐变边框 */
  &:hover {
    transform: translateY(-8rpx); /* 上浮 4px */
    /* 重构：hover 阴影标准 - 0 8px 24px rgba(59,130,246,0.08) */
    box-shadow: 0 16rpx 48rpx rgba(59, 130, 246, 0.08);
    /* 重构：光感边框 - 半透明蓝色 */
    border-color: rgba(59, 130, 246, 0.25);

    .card-title {
      /* 精修：标题渐变色呼应主题色 */
      background: linear-gradient(135deg, #2563EB 0%, #3B82F6 50%, #60A5FA 100%);
      -webkit-background-clip: text;
      background-clip: text;
      -webkit-text-fill-color: transparent;
    }

    /* 重构：hover 时显示箭头指示器 */
    .card-click-indicator {
      opacity: 1;
      transform: translateY(-50%) translateX(0); /* 修复：保持垂直居中 */
    }

    /* 重构：顶部色条加深 */
    .card-top-bar {
      opacity: 1;
    }
  }

  /* Active 状态 */
  &:active {
    transform: translateY(-4rpx) scale(0.98);
    transition: all 0.15s ease;
  }
}

/* 文档规范：移除卡片背景渐变，统一使用纯白卡 */
/* 已在 .recommend-card 中统一设置 background: #FFFFFF */

/* 卡片淡入动画 */
@keyframes fadeInCard {
  from {
    opacity: 0;
    transform: translateY(10rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 精修：顶部色条 - 6px 高度，柔化色彩，统一亮度 */
.card-top-bar {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 12rpx; /* 精修：从 8rpx (4px) 增加到 12rpx (6px) */
  border-radius: 24rpx 24rpx 0 0; /* 顶部圆角与卡片一致 */
  opacity: 0.8; /* 默认半透明 */
  transition: opacity 0.25s ease;
  z-index: 1;

  /* 精修：课件 - 柔和蓝色（亮度 70%）*/
  &.bar-resource {
    background: #60A5FA; /* 从 #3B82F6 柔化 */
  }

  /* 精修：问答 - 柔和橙色（亮度 73%）*/
  &.bar-question {
    background: #FDBA74; /* 从 #F59E0B 柔化 */
  }

  /* 精修：任务 - 柔和绿色（亮度 68%）*/
  &.bar-task {
    background: #4ADE80; /* 从 #22C55E 柔化 */
  }
}

/* 精修：类型标签 - 去掉背景色，颜色与色条同步 */
.type-tag {
  display: inline-flex;
  align-items: center;
  gap: 8rpx; /* 图标与文字间距 */
  padding: 0; /* 重构：去掉内边距 */
  font-size: 24rpx; /* 12px */
  font-weight: 500; /* 重构：从 600 降到 500 */
  transition: all 0.25s ease;
  flex-shrink: 0;

  /* 精修：课程 - 柔和蓝色，与色条同步 */
  &.type-resource {
    color: #60A5FA; /* 精修：从 #3B82F6 柔化 */
  }

  /* 精修：问答 - 柔和橙色，与色条同步 */
  &.type-question {
    color: #FDBA74; /* 精修：从 #F59E0B 柔化 */
  }

  /* 精修：任务 - 柔和绿色，与色条同步 */
  &.type-task {
    color: #4ADE80; /* 精修：从 #22C55E 柔化 */
  }
}

.type-icon {
  font-size: 24rpx; /* 图标大小 */
  line-height: 1;
}

.type-text {
  line-height: 1;
}

/* 重构：点击指示器（左侧与标题对齐）*/
.card-click-indicator {
  position: absolute;
  /* 修复：改为左侧定位，与卡片标题垂直对齐 */
  top: 50%;
  transform: translateY(-50%) translateX(-8rpx);
  right: 32rpx;
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(59, 130, 246, 0.08);
  border-radius: 50%;
  opacity: 0;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  pointer-events: none;
  z-index: 5; /* 修复：降低层级，避免遮挡徽标 */
}

.indicator-arrow {
  font-size: 32rpx;
  color: #3B82F6;
  line-height: 1;
  font-weight: 600;
}

/* 优化：卡片内容包装器 - 分层结构 */
.card-content {
  display: flex;
  flex-direction: column;
  gap: 12rpx; /* 6px - 标题与摘要间距 */
  flex: 1;
}

/* 精修：一级 - 卡片标题（加粗 + 紧凑行距）*/
.card-title {
  font-size: 32rpx; /* 16px */
  font-weight: 700; /* 精修：从 600 增强到 700，更突出 */
  color: #0F172A; /* 深色 */
  line-height: 1.5; /* 精修：从 1.6 缩小到 1.5，更紧凑 */
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2; /* 最多两行 */
  -webkit-box-orient: vertical;
  transition: all var(--transition-hover, 250ms ease);
}

/* 精修：二级 - 卡片摘要（灰度中等 + 紧凑行距）*/
.card-intro {
  font-size: 28rpx; /* 14px */
  font-weight: 400;
  color: #64748B; /* 灰度中等 */
  line-height: 1.4; /* 精修：从 1.6 缩小到 1.4，提升信息密度 */
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2; /* 最多 2 行 */
  -webkit-box-orient: vertical;
}

/* 优化：三级 - Meta 信息（灰度最轻）*/
.card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx 24rpx;
  margin-top: auto; /* 推到底部 */
  padding-top: 16rpx;
  border-top: 1px solid rgba(0, 0, 0, 0.04);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.meta-icon {
  font-size: 24rpx; /* 12px */
  line-height: 1;
  opacity: 0.6; /* 图标线性风格 */
}

.meta-text {
  font-size: 24rpx; /* 12px */
  color: var(--cl-gray-500, #94A3B8); /* 统一灰 500 */
  line-height: 1;
  font-weight: 400;
}

/* 分隔符 - 文档规范：统一使用 "·" */
.meta-dot {
  font-size: 24rpx;
  color: var(--cl-gray-400, #CBD5E1);
  line-height: 1;
  margin: 0 4rpx;
}

/* 高亮元信息（积分）*/
.meta-item.meta-highlight {
  .meta-icon {
    opacity: 1;
  }

  .meta-text {
    color: var(--cl-accent-orange, #F59E0B); /* 暖黄色 */
    font-weight: 600;
  }
}

/* 紧急元信息（截止时间）*/
.meta-item.meta-urgent {
  .meta-icon {
    opacity: 1;
  }

  .meta-text {
    color: #EF4444; /* 红色 */
    font-weight: 600;
  }
}

/* 操作按钮 - 文档规范（合并成一个分组，减少噪点）*/
.card-actions {
  display: flex;
  margin-top: 16rpx;
}

.action-group {
  display: flex;
  align-items: center;
  gap: 0;
  background: var(--cl-gray-50, #F8FAFC);
  border-radius: 16rpx; /* 8px */
  padding: 4rpx;
  border: 1px solid var(--cl-gray-200, #E5E7EB);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 16rpx;
  background: transparent;
  border-radius: 12rpx; /* 6px */
  cursor: pointer;
  transition: all var(--transition-hover, 150ms ease);

  &:hover {
    background: white;
    transform: translateY(-1rpx);
  }

  &:active {
    transform: scale(0.95);
  }
}

.action-divider {
  width: 1px;
  height: 24rpx; /* 12px */
  background: var(--cl-gray-200, #E5E7EB);
}

.action-icon {
  font-size: 24rpx; /* 12px */
  line-height: 1;
}

.action-label {
  font-size: 22rpx; /* 11px */
  font-weight: 500;
  color: var(--cl-gray-600, #64748B);
  line-height: 1;
}

/* ========== 响应式适配 - 文档规范 ========== */

/* <960：网格改单列 + 4 条展示 */
@media (max-width: 960px) {
  .recommend-list {
    grid-template-columns: 1fr; /* 文档规范：单列布局 */
  }

  /* 文档规范：只显示 4 条 */
  .recommend-card:nth-child(n+5) {
    display: none;
  }

  /* 文档规范：查看全部变为满宽胶囊按钮 */
  .action-bar {
    border-radius: 999rpx; /* 胶囊按钮 */
  }
}

/* 移动端（< 750px）*/
@media (max-width: 750px) {
  .recommend-list {
    grid-template-columns: 1fr;
    gap: 24rpx;
  }

  .section-header {
    flex-wrap: wrap;
    gap: 12rpx;
  }

  .filter-tags {
    order: 3;
    width: 100%;
  }

  .recommend-card {
    min-height: 280rpx;
  }

  /* 移动端：操作栏满宽 */
  .action-bar {
    width: 100%;
    border-radius: 999rpx;
  }
}

/* ========== 优化：新增样式 ========== */

/* 优化：右上角徽标（热门/截止中）*/
.card-badge {
  position: absolute;
  top: 16rpx; /* 8px */
  right: 16rpx; /* 8px */
  display: flex;
  align-items: center;
  gap: 6rpx; /* 3px */
  padding: 6rpx 12rpx; /* 3px 6px */
  border-radius: 12rpx; /* 6px */
  font-size: 20rpx; /* 10px */
  font-weight: 600;
  z-index: 2;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);

  /* 热门徽标 */
  &.badge-hot {
    background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%); /* 黄色渐变 */
    color: #CA8A04; /* 深黄 */
  }

  /* 截止中徽标 */
  &.badge-urgent {
    background: linear-gradient(135deg, #FEE2E2 0%, #FECACA 100%); /* 红色渐变 */
    color: #DC2626; /* 深红 */
  }
}

.badge-icon {
  font-size: 20rpx; /* 10px */
  line-height: 1;
}

.badge-text {
  line-height: 1;
}

/* 重构：移除原有的浮层样式，已被点击指示器替代 */

/* 优化：底部操作栏 - 淡蓝底 #F1F5FF，增强存在感 */
.action-bar {
  background: #F1F5FF; /* 优化：从 #EEF2FF 改为 #F1F5FF */
}

/* 优化：查看全部按钮 - 深蓝 + 箭头动画 */
.view-all-btn {
  .action-text {
    color: var(--cl-primary, #2563EB);
    font-weight: 500;
  }

  .arrow-icon {
    transition: transform 0.3s ease;
  }

  &:hover .arrow-icon {
    transform: translateX(6rpx); /* 箭头向右移动 */
  }
}
</style>

