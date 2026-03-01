<template>
  <view class="recommend-page">
    <!-- 顶部导航 -->
    <CNavBar title="全部推荐" />

    <!-- Tab 切换 -->
    <view class="tabs-container">
      <scroll-view class="tabs-scroll" scroll-x>
        <view class="tabs">
          <view
            v-for="(tab, index) in tabs"
            :key="index"
            class="tab-item"
            :class="{ active: currentTab === index }"
            @click="switchTab(index)"
          >
            <text class="tab-text">{{ tab.label }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 内容区 -->
    <view class="content-container">
      <!-- 加载中 -->
      <view v-if="isLoading" class="loading-container">
        <SkeletonCard v-for="i in 6" :key="i" type="resource" />
      </view>

      <!-- 空状态 -->
      <EmptyState
        v-else-if="!isLoading && currentList.length === 0"
        type="search"
        title="暂无推荐内容"
        description="换个分类试试吧"
      />

      <!-- 推荐列表 -->
      <view v-else class="recommend-list">
        <view
          v-for="item in currentList"
          :key="item.id"
          class="recommend-card"
          @click="handleItemClick(item)"
        >
          <!-- 图标 -->
          <view class="card-icon" :style="{ background: item.color }">
            <text class="icon-text">{{ item.icon }}</text>
          </view>

          <!-- 内容 -->
          <view class="card-content">
            <text class="card-title">{{ item.title }}</text>
            <text class="card-desc">{{ item.description }}</text>
            <view class="card-meta">
              <text class="meta-item">{{ item.type }}</text>
              <text class="meta-dot">·</text>
              <text class="meta-item">{{ item.count }} 人浏览</text>
            </view>
          </view>

          <!-- 箭头 -->
          <text class="card-arrow">→</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import EmptyState from '@/components/EmptyState.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'
import { CNavBar } from '@/components/layout'

// Tab 配置
const tabs = [
  { label: '全部', key: 'all' },
  { label: '资源', key: 'resource' },
  { label: '问答', key: 'question' },
  { label: '任务', key: 'task' }
]

// 状态
const currentTab = ref(0)
const isLoading = ref(false)
const allList = ref<any[]>([])

// 当前列表
const currentList = computed(() => {
  if (currentTab.value === 0) return allList.value
  const type = tabs[currentTab.value].key
  return allList.value.filter((item) => item.type === type)
})

/**
 * 加载推荐数据
 */
const loadData = async () => {
  isLoading.value = true
  try {
    // 模拟数据
    await new Promise((resolve) => setTimeout(resolve, 800))
    
    allList.value = [
      {
        id: 1,
        icon: '📚',
        title: '高等数学复习资料',
        description: '期末考试必备，包含历年真题和详细解析',
        type: 'resource',
        color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        count: 1234
      },
      {
        id: 2,
        icon: '💬',
        title: '如何准备英语四级考试？',
        description: '分享备考经验和学习方法',
        type: 'question',
        color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
        count: 856
      },
      {
        id: 3,
        icon: '📝',
        title: '帮忙代取快递',
        description: '明天下午有空的同学帮忙取一下',
        type: 'task',
        color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
        count: 432
      },
      {
        id: 4,
        icon: '📖',
        title: '计算机网络课件分享',
        description: '老师上课的PPT，整理好了',
        type: 'resource',
        color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
        count: 2341
      },
      {
        id: 5,
        icon: '❓',
        title: '数据结构树的遍历问题',
        description: '前序、中序、后序遍历的区别',
        type: 'question',
        color: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
        count: 678
      },
      {
        id: 6,
        icon: '🎯',
        title: '寻找组队参加编程比赛',
        description: 'ACM比赛，需要2名队友',
        type: 'task',
        color: 'linear-gradient(135deg, #30cfd0 0%, #330867 100%)',
        count: 234
      }
    ]
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
 * 返回
 */
const goBack = () => {
  uni.navigateBack()
}

/**
 * 点击推荐项
 */
const handleItemClick = (item: any) => {
  const routes: Record<string, string> = {
    resource: '/pages/resource/detail?id=' + item.id,
    question: '/pages/question/detail?id=' + item.id,
    task: '/pages/task/detail?id=' + item.id
  }
  
  const url = routes[item.type]
  if (url) {
    uni.navigateTo({ url })
  }
}

// 页面加载
onLoad((options: any) => {
  if (options.tab) {
    currentTab.value = parseInt(options.tab)
  }
  loadData()
})
</script>

<style scoped lang="scss">
.recommend-page {
  min-height: 100vh;
  background: var(--cl-bg, #F7F8FA);
  padding-bottom: 120rpx;
}

/* Tab 切换 */
.tabs-container {
  background: white;
  border-bottom: 1px solid var(--cl-divider, #E5E7EB);
}

.tabs-scroll {
  white-space: nowrap;
}

.tabs {
  display: inline-flex;
  padding: 0 32rpx;
}

.tab-item {
  padding: 24rpx 32rpx;
  font-size: 28rpx;
  color: var(--cl-text-sub, #6B7280);
  position: relative;
  transition: all 0.2s;
  cursor: pointer;
  
  &.active {
    color: var(--cl-primary, #2E7CF6);
    font-weight: 600;
    
    &::after {
      content: '';
      position: absolute;
      left: 50%;
      bottom: 0;
      transform: translateX(-50%);
      width: 40rpx;
      height: 4rpx;
      background: var(--cl-primary, #2E7CF6);
      border-radius: 2rpx;
    }
  }
}

/* 内容区 */
.content-container {
  padding: 24rpx 32rpx;
}

.loading-container {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

/* 推荐列表 */
.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.recommend-card {
  display: flex;
  align-items: center;
  gap: 24rpx;
  background: white;
  border-radius: var(--radius-md, 12px);
  padding: 32rpx;
  box-shadow: var(--shadow-1, 0 2px 8px rgba(0,0,0,.06));
  transition: all 0.2s;
  cursor: pointer;
  
  &:active {
    transform: scale(0.98);
  }
}

.card-icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-text {
  font-size: 48rpx;
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.card-title {
  font-size: 30rpx;
  font-weight: 600;
  color: var(--cl-text, #0F172A);
  line-height: 1.4;
}

.card-desc {
  font-size: 24rpx;
  color: var(--cl-text-sub, #6B7280);
  line-height: 1.5;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-top: 4rpx;
}

.meta-item {
  font-size: 22rpx;
  color: var(--cl-gray-400, #9CA3AF);
}

.meta-dot {
  font-size: 22rpx;
  color: var(--cl-gray-400, #9CA3AF);
}

.card-arrow {
  font-size: 32rpx;
  color: var(--cl-gray-300, #D1D5DB);
  flex-shrink: 0;
}
</style>

