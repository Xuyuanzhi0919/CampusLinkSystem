<template>
  <view class="hot-ranking-panel">
    <!-- 切换标签 -->
    <view class="tab-bar">
      <text
        v-for="(tab, index) in tabs"
        :key="tab.key"
        class="tab-item"
        :class="{ active: currentTab === index }"
        @click="switchTab(index)"
      >
        {{ tab.name }}
      </text>
    </view>

    <!-- 榜单列表 -->
    <view class="ranking-list">
      <view
        v-for="(item, index) in currentList"
        :key="item.id"
        class="ranking-item"
        @click="handleItemClick(item)"
      >
        <!-- 排名序号 -->
        <view class="rank-number" :class="'rank-' + (index + 1)">
          <text class="rank-text">{{ index + 1 }}</text>
        </view>

        <!-- 内容信息 -->
        <view class="item-content">
          <text class="item-title">{{ item.title }}</text>
          <view class="item-meta">
            <text class="meta-text">{{ item.views || item.downloads }} {{ item.views ? '浏览' : '下载' }}</text>
            <text class="meta-dot">·</text>
            <text class="meta-text">{{ item.answers || item.score }} {{ item.answers ? '回答' : '积分' }}</text>
          </view>
        </view>

        <!-- 快速操作按钮 -->
        <view class="quick-btn" @click.stop="handleQuickAction(item)">
          <text class="quick-text">{{ getQuickText(currentTab) }}</text>
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
  { name: '热门问答', key: 'question' },
  { name: '精选资料', key: 'resource' },
  { name: '紧急任务', key: 'task' },
]

const currentTab = ref(0)

// 模拟数据
const questionList = ref([
  { id: 1, title: '如何学习数据结构与算法？', views: 1520, answers: 23, type: 'question' },
  { id: 2, title: '大学生如何提高编程能力？', views: 1230, answers: 18, type: 'question' },
  { id: 3, title: '考研数学如何复习？', views: 980, answers: 15, type: 'question' },
  { id: 4, title: '英语四级如何备考？', views: 856, answers: 12, type: 'question' },
  { id: 5, title: '如何平衡学习和社团活动？', views: 720, answers: 10, type: 'question' },
])

const resourceList = ref([
  { id: 1, title: '数据结构课件-完整版', downloads: 2340, score: 5, type: 'resource' },
  { id: 2, title: '高等数学历年真题', downloads: 1890, score: 3, type: 'resource' },
  { id: 3, title: '计算机网络笔记', downloads: 1560, score: 2, type: 'resource' },
  { id: 4, title: '操作系统期末复习资料', downloads: 1230, score: 4, type: 'resource' },
  { id: 5, title: '英语四级词汇表', downloads: 980, score: 1, type: 'resource' },
])

const taskList = ref([
  { id: 1, title: '帮忙取快递（菜鸟驿站）', views: 0, score: 5, type: 'task' },
  { id: 2, title: '代打印资料（图书馆）', views: 0, score: 3, type: 'task' },
  { id: 3, title: '帮忙占座（自习室）', views: 0, score: 2, type: 'task' },
  { id: 4, title: '代取外卖（宿舍楼下）', views: 0, score: 4, type: 'task' },
  { id: 5, title: '帮忙送文件（教学楼）', views: 0, score: 3, type: 'task' },
])

// 当前列表
const currentList = computed(() => {
  if (currentTab.value === 0) return questionList.value
  if (currentTab.value === 1) return resourceList.value
  return taskList.value
})

/**
 * 切换 Tab
 */
const switchTab = (index: number) => {
  currentTab.value = index
}

/**
 * 条目点击
 */
const handleItemClick = (item: any) => {
  emit('itemClick', item)
}

/**
 * 快速操作
 */
const handleQuickAction = (item: any) => {
  const actions = ['回答', '下载', '接单']
  uni.showToast({
    title: `${actions[currentTab.value]}中...`,
    icon: 'loading',
  })
}

/**
 * 获取快速操作文字
 */
const getQuickText = (tabIndex: number) => {
  const texts = ['答', '载', '抢']
  return texts[tabIndex]
}
</script>

<style scoped lang="scss">
.hot-ranking-panel {
  background: white;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.05);
  transition: all 0.2s ease;
  animation: fadeInUp 0.4s ease-out 0.1s both;
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

/* 切换标签 */
.tab-bar {
  display: flex;
  gap: 32rpx;
  margin-bottom: 32rpx;
  padding-bottom: 24rpx;
  border-bottom: 2rpx solid #E5E6EB;
}

.tab-item {
  position: relative;
  font-size: 28rpx; /* 14px - 正文规范 */
  color: #86909C;
  padding-bottom: 8rpx;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab-item:hover {
  color: #FFA940;
}

.tab-item.active {
  color: #FFA940;
  font-weight: 600;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 4rpx;
  background: linear-gradient(90deg, #FFA940 0%, #FFB64B 100%);
  border-radius: 2rpx;
  box-shadow: 0 2rpx 8rpx rgba(255, 169, 64, 0.3);
  animation: slideIn 0.2s ease-out;
}

@keyframes slideIn {
  from {
    transform: scaleX(0);
  }
  to {
    transform: scaleX(1);
  }
}

/* 榜单列表 */
.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 16rpx;
  border-radius: 16rpx;
  cursor: pointer;
  transition: all 0.2s ease;
}

.ranking-item:hover {
  background: #F5F6FA;
  transform: translateX(4rpx);
}

/* 排名序号 */
.rank-number {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12rpx;
  flex-shrink: 0;
  background: #F5F6FA;
  transition: all 0.2s ease;
  position: relative;
}

/* 第1名：橙色渐变 + 光晕 */
.rank-number.rank-1 {
  background: linear-gradient(135deg, #FFA940 0%, #FFB64B 100%);
  box-shadow: 0 4rpx 12rpx rgba(255, 169, 64, 0.3);
}

/* 第2名：蓝色渐变 */
.rank-number.rank-2 {
  background: linear-gradient(135deg, #1E5FFF 0%, #5A7FFF 100%);
  box-shadow: 0 4rpx 12rpx rgba(30, 95, 255, 0.2);
}

/* 第3名：绿色渐变 */
.rank-number.rank-3 {
  background: linear-gradient(135deg, #52C41A 0%, #73D13D 100%);
  box-shadow: 0 4rpx 12rpx rgba(82, 196, 26, 0.2);
}

.rank-text {
  font-size: 24rpx;
  font-weight: 700;
  color: #86909C;
  line-height: 1;
}

.rank-number.rank-1 .rank-text,
.rank-number.rank-2 .rank-text,
.rank-number.rank-3 .rank-text {
  color: white;
  text-shadow: 0 1rpx 2rpx rgba(0, 0, 0, 0.1);
}

/* 内容信息 */
.item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  overflow: hidden;
}

.item-title {
  font-size: 28rpx; /* 14px */
  color: #1D2129;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.meta-text {
  font-size: 24rpx; /* 12px */
  color: #86909C;
  line-height: 1;
}

.meta-dot {
  font-size: 24rpx;
  color: #C9CDD4;
  line-height: 1;
}

/* 快速操作按钮 */
.quick-btn {
  width: 80rpx; /* 40px */
  height: 48rpx; /* 24px */
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FF7D00 0%, #FFA940 100%);
  border-radius: 24rpx;
  cursor: pointer;
  transition: all 0.3s;
  flex-shrink: 0;
}

.quick-btn:active {
  transform: scale(0.95);
}

.quick-text {
  font-size: 24rpx;
  font-weight: 600;
  color: white;
  line-height: 1;
}
</style>

