<template>
  <view class="recommend-section">
    <!-- 标题栏 -->
    <view class="section-header">
      <text class="section-title">为你推荐</text>
      
      <!-- 筛选标签 -->
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

      <!-- 换一批按钮 -->
      <view class="refresh-btn" @click="handleRefresh">
        <text class="refresh-icon" :class="{ rotating: isRefreshing }">🔄</text>
        <text class="refresh-text">换一批</text>
      </view>
    </view>

    <!-- 推荐卡片列表 -->
    <view class="recommend-list">
      <view
        v-for="item in currentList"
        :key="item.id"
        class="recommend-card"
        @click="handleItemClick(item)"
      >
        <!-- 类型标签 -->
        <view class="type-tag" :class="'type-' + item.type">
          <text class="type-icon">{{ getTypeIcon(item.type) }}</text>
          <text class="type-text">{{ getTypeName(item.type) }}</text>
        </view>

        <!-- 卡片内容 -->
        <text class="card-title">{{ item.title }}</text>
        <text class="card-intro">{{ item.intro }}</text>

        <!-- 资源信息 -->
        <view class="card-meta">
          <view class="meta-item">
            <text class="meta-icon">👤</text>
            <text class="meta-text">{{ item.author }}</text>
          </view>
          <view class="meta-item">
            <text class="meta-icon">📥</text>
            <text class="meta-text">{{ item.downloads || item.views }}</text>
          </view>
        </view>

        <!-- 操作按钮 -->
        <view class="card-actions">
          <view class="action-btn" @click.stop="handleCollect(item)">
            <text class="action-icon">{{ item.collected ? '❤️' : '🤍' }}</text>
          </view>
          <view class="action-btn" @click.stop="handleDownload(item)">
            <text class="action-icon">📥</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

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

// 模拟数据
const allList = ref([
  {
    id: 1,
    type: 'resource',
    title: '数据结构与算法完整笔记',
    intro: '涵盖所有常见数据结构和算法，配有详细图解和代码实现',
    author: '计算机学院',
    downloads: 2340,
    collected: false,
  },
  {
    id: 2,
    type: 'question',
    title: '如何高效学习高等数学？',
    intro: '分享我从60分到95分的学习方法和技巧',
    author: '数学系学霸',
    views: 1520,
    collected: true,
  },
  {
    id: 3,
    type: 'task',
    title: '帮忙取快递（菜鸟驿站）',
    intro: '今天下午3点前，酬劳5积分',
    author: '张同学',
    downloads: 0,
    collected: false,
  },
  {
    id: 4,
    type: 'resource',
    title: '英语四级高频词汇表',
    intro: '精选1000个高频词汇，附例句和记忆技巧',
    author: '外语学院',
    downloads: 1890,
    collected: false,
  },
  {
    id: 5,
    type: 'question',
    title: '考研应该如何准备？',
    intro: '985学长的考研经验分享，从择校到复试全流程',
    author: '研究生学长',
    views: 980,
    collected: false,
  },
  {
    id: 6,
    type: 'resource',
    title: '计算机网络课件PPT',
    intro: '完整的计算机网络课程PPT，包含所有章节',
    author: '网络工程',
    downloads: 1560,
    collected: true,
  },
])

// 当前列表（根据 Tab 筛选）
const currentList = computed(() => {
  if (currentTab.value === 0) return allList.value
  const type = tabs[currentTab.value].key
  return allList.value.filter((item) => item.type === type)
})

/**
 * 切换 Tab
 */
const switchTab = (index: number) => {
  currentTab.value = index
}

/**
 * 换一批
 */
const handleRefresh = () => {
  isRefreshing.value = true
  setTimeout(() => {
    isRefreshing.value = false
    uni.showToast({ title: '已刷新', icon: 'success' })
  }, 500)
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
 * 下载
 */
const handleDownload = (item: any) => {
  uni.showToast({ title: '下载中...', icon: 'loading' })
}

/**
 * 获取类型图标
 */
const getTypeIcon = (type: string) => {
  const icons: Record<string, string> = {
    resource: '📚',
    question: '💡',
    task: '🤝',
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
</script>

<style scoped lang="scss">
.recommend-section {
  background: white;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

/* 标题栏 */
.section-header {
  display: flex;
  align-items: center;
  gap: 32rpx;
  margin-bottom: 32rpx;
  padding-bottom: 24rpx;
  border-bottom: 2rpx solid #F2F3F5;
}

.section-title {
  font-size: 36rpx; /* 18px */
  font-weight: 700;
  color: #1D2129;
  line-height: 1;
}

/* 筛选标签 */
.filter-tags {
  flex: 1;
  display: flex;
  gap: 24rpx;
}

.filter-tag {
  font-size: 28rpx; /* 14px */
  color: #86909C;
  padding: 12rpx 24rpx;
  border-radius: 16rpx;
  cursor: pointer;
  transition: all 0.3s;
}

.filter-tag.active {
  color: #FF7D00;
  background: #FFF7E6;
  font-weight: 600;
}

/* 换一批按钮 */
.refresh-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
  color: #FF7D00;
  cursor: pointer;
  transition: all 0.3s;
}

.refresh-btn:active {
  opacity: 0.7;
}

.refresh-icon {
  font-size: 28rpx;
  line-height: 1;
  transition: transform 0.5s;
}

.refresh-icon.rotating {
  animation: rotate 0.5s linear;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.refresh-text {
  font-size: 28rpx;
  font-weight: 600;
  line-height: 1;
}

/* 推荐列表 */
.recommend-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32rpx;
}

.recommend-card {
  position: relative;
  padding: 24rpx;
  background: white;
  border: 2rpx solid #E5E7EB;
  border-radius: 24rpx;
  cursor: pointer;
  transition: all 0.3s;
  min-height: 240rpx;
}

.recommend-card:hover {
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
  border-color: #409EFF;
}

/* 类型标签 */
.type-tag {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 16rpx;
  border-radius: 16rpx;
  margin-bottom: 16rpx;
  font-size: 22rpx;
  font-weight: 600;
}

.type-tag.type-resource {
  background: #E6F4FF;
  color: #409EFF;
}

.type-tag.type-question {
  background: #FFF7E6;
  color: #FF7D00;
}

.type-tag.type-task {
  background: #E8F7ED;
  color: #52C41A;
}

.type-icon {
  font-size: 20rpx;
  line-height: 1;
}

.type-text {
  line-height: 1;
}

/* 卡片内容 */
.card-title {
  display: block;
  font-size: 32rpx; /* 16px */
  font-weight: 600;
  color: #1D2129;
  line-height: 1.5;
  margin-bottom: 12rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-intro {
  display: block;
  font-size: 28rpx; /* 14px */
  color: #86909C;
  line-height: 1.5;
  margin-bottom: 16rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 资源信息 */
.card-meta {
  display: flex;
  gap: 24rpx;
  margin-bottom: 16rpx;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.meta-icon {
  font-size: 20rpx;
  line-height: 1;
}

.meta-text {
  font-size: 24rpx; /* 12px */
  color: #86909C;
  line-height: 1;
}

/* 操作按钮 */
.card-actions {
  display: flex;
  gap: 16rpx;
}

.action-btn {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F5F7FA;
  border-radius: 12rpx;
  cursor: pointer;
  transition: all 0.3s;
}

.action-btn:active {
  transform: scale(0.9);
}

.action-icon {
  font-size: 28rpx;
  line-height: 1;
}

/* H5 端适配 */
@media (max-width: 750px) {
  .recommend-list {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-wrap: wrap;
  }

  .filter-tags {
    order: 3;
    width: 100%;
  }
}
</style>

