<template>
  <view class="hot-ranking">
    <!-- 标题和Tab切换 -->
    <view class="header">
      <text class="title">热门榜单</text>
      <view class="tabs">
        <view
          v-for="(tab, index) in tabs"
          :key="index"
          class="tab-item"
          :class="{ active: currentTab === index }"
          @click="switchTab(index)"
        >
          {{ tab.name }}
        </view>
      </view>
    </view>

    <!-- 榜单列表 -->
    <view class="ranking-list">
      <!-- 热门问答 -->
      <view v-if="currentTab === 0" class="list-content">
        <view
          v-for="(item, index) in questionList"
          :key="item.id"
          class="ranking-item"
          @click="goToDetail('question', item.id)"
        >
          <view class="rank-number" :class="'rank-' + (index + 1)">
            <text class="rank-text">{{ index + 1 }}</text>
          </view>
          <view class="item-content">
            <text class="item-title">{{ item.title }}</text>
            <view class="item-meta">
              <text class="meta-author">{{ item.author || '匿名用户' }}</text>
              <text class="meta-dot">·</text>
              <text class="meta-time">{{ item.time || '刚刚' }}</text>
            </view>
          </view>
          <!-- 右侧统计信息 -->
          <view class="item-stats">
            <view class="stat-row">
              <text class="stat-icon">👁</text>
              <text class="stat-value">{{ formatNumber(item.views) }}</text>
            </view>
            <view class="stat-row">
              <text class="stat-icon">💬</text>
              <text class="stat-value">{{ item.answers }}</text>
            </view>
          </view>
          <view v-if="item.hot" class="hot-tag">
            <text class="hot-text">热</text>
          </view>
        </view>
      </view>

      <!-- 精选资料 -->
      <view v-if="currentTab === 1" class="list-content">
        <view
          v-for="(item, index) in resourceList"
          :key="item.id"
          class="ranking-item"
          @click="goToDetail('resource', item.id)"
        >
          <view class="rank-number" :class="'rank-' + (index + 1)">
            <text class="rank-text">{{ index + 1 }}</text>
          </view>
          <view class="item-content">
            <text class="item-title">{{ item.title }}</text>
            <view class="item-meta">
              <text class="meta-author">{{ item.author || '匿名用户' }}</text>
              <text class="meta-dot">·</text>
              <text class="meta-time">{{ item.time || '刚刚' }}</text>
            </view>
          </view>
          <!-- 右侧统计信息 -->
          <view class="item-stats">
            <view class="stat-row">
              <text class="stat-icon">📥</text>
              <text class="stat-value">{{ formatNumber(item.downloads) }}</text>
            </view>
            <view class="stat-row">
              <text class="stat-icon">💰</text>
              <text class="stat-value">{{ item.score }}</text>
            </view>
          </view>
          <view v-if="item.hot" class="hot-tag">
            <text class="hot-text">热</text>
          </view>
        </view>
      </view>

      <!-- 紧急任务 -->
      <view v-if="currentTab === 2" class="list-content">
        <view
          v-for="(item, index) in taskList"
          :key="item.id"
          class="ranking-item"
          @click="goToDetail('task', item.id)"
        >
          <view class="rank-number" :class="'rank-' + (index + 1)">
            {{ index + 1 }}
          </view>
          <view class="item-content">
            <text class="item-title">{{ item.title }}</text>
            <view class="item-meta">
              <text class="meta-item">{{ item.reward }} 积分・</text>
              <text class="meta-item urgent">{{ item.deadline }}</text>
            </view>
          </view>
          <view class="urgent-tag">
            <text class="urgent-text">急</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

// Tab 配置
const tabs = [
  { name: '热门问答', key: 'question' },
  { name: '精选资料', key: 'resource' },
  { name: '紧急任务', key: 'task' },
]

const currentTab = ref(0)

// 模拟数据
const questionList = ref([
  { id: 1, title: '如何学习数据结构与算法？', views: 1520, answers: 23, hot: true, author: '张三', time: '2小时前' },
  { id: 2, title: '大学生如何提高编程能力？', views: 1230, answers: 18, hot: true, author: '李四', time: '3小时前' },
  { id: 3, title: '考研数学如何复习？', views: 980, answers: 15, hot: true, author: '王五', time: '5小时前' },
  { id: 4, title: '英语四级如何备考？', views: 856, answers: 12, hot: false, author: '赵六', time: '昨天' },
  { id: 5, title: '如何平衡学习和社团活动？', views: 720, answers: 10, hot: false, author: '钱七', time: '2天前' },
])

const resourceList = ref([
  { id: 1, title: '数据结构课件-完整版', downloads: 2340, score: 5, hot: true, author: '计算机学院', time: '1周前' },
  { id: 2, title: '高等数学历年真题', downloads: 1890, score: 3, hot: true, author: '数学系', time: '2周前' },
  { id: 3, title: '计算机网络笔记', downloads: 1560, score: 2, hot: true, author: '网络工程', time: '3周前' },
  { id: 4, title: '操作系统期末复习资料', downloads: 1230, score: 4, hot: false, author: '软件工程', time: '1个月前' },
  { id: 5, title: '英语四级词汇表', downloads: 980, score: 1, hot: false, author: '外语学院', time: '1个月前' },
])

const taskList = ref([
  { id: 1, title: '帮忙取快递（菜鸟驿站）', reward: 5, deadline: '2小时后' },
  { id: 2, title: '代打印资料（图书馆）', reward: 3, deadline: '3小时后' },
  { id: 3, title: '求组队参加编程比赛', reward: 10, deadline: '5小时后' },
  { id: 4, title: '帮忙占座（自习室）', reward: 2, deadline: '1小时后' },
  { id: 5, title: '求借计算器（考试用）', reward: 5, deadline: '4小时后' },
])

/**
 * 切换 Tab
 */
const switchTab = (index: number) => {
  currentTab.value = index
}

/**
 * 格式化数字（1000+ 显示为 1k）
 */
const formatNumber = (num: number): string => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

/**
 * 跳转到详情页
 */
const goToDetail = (type: string, id: number) => {
  uni.navigateTo({
    url: `/pages/${type}/detail?id=${id}`,
    fail: () => {
      uni.showToast({
        title: '功能开发中',
        icon: 'none',
      })
    },
  })
}
</script>

<style scoped>
.hot-ranking {
  /* 轻卡片容器：白色背景 + 4px圆角 + 轻微阴影 */
  background: white;
  border-radius: 8rpx; /* 从 12rpx 调整为 8rpx (4px) */
  padding: 30rpx;
  margin: 0 30rpx 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04); /* 轻微阴影 */
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1D2129;
}

.tabs {
  display: flex;
  gap: 20rpx;
}

.tab-item {
  font-size: 26rpx;
  color: #86909C;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
  transition: all 0.3s;
}

.tab-item.active {
  color: #409EFF;
  background: #E8F4FF;
  font-weight: 500;
}

.ranking-list {
  min-height: 400rpx;
}

.list-content {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.ranking-item {
  display: flex;
  align-items: center; /* 使用 flex 居中对齐，确保序号与文字对齐 */
  gap: 20rpx;
  padding: 10rpx 16rpx; /* 从 12rpx 调整为 10rpx，压缩至 36px (72rpx) 行高 */
  border-radius: 8rpx;
  transition: all 0.3s;
  min-height: 72rpx; /* 确保行高为 36px (72rpx) */
}

.ranking-item:active {
  background: #F5F7FA;
}

.rank-number {
  width: 48rpx;
  height: 48rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24rpx;
  font-weight: 700; /* 从 600 加粗到 700 */
  color: white; /* 默认白色文字 */
  background: #C9CDD4; /* 默认灰色背景 */
  border-radius: 8rpx;
  flex-shrink: 0;
}

/* 第1名：金色背景 */
.rank-number.rank-1 {
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
}

/* 第2名：银色背景 */
.rank-number.rank-2 {
  background: linear-gradient(135deg, #C0C0C0 0%, #A8A8A8 100%);
}

/* 第3名：铜色背景 */
.rank-number.rank-3 {
  background: linear-gradient(135deg, #CD7F32 0%, #B8860B 100%);
}

.rank-text {
  color: white;
  line-height: 1;
}

.item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx; /* 从 8rpx 调整为 6rpx，压缩文字上下间距 */
  overflow: hidden;
}

.item-title {
  font-size: 28rpx;
  font-weight: 500; /* 添加字重，增强标题层级 */
  color: #1D2129;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4; /* 添加行高，确保文字垂直居中 */
}

.item-meta {
  display: flex;
  gap: 8rpx;
  align-items: center; /* 确保元数据垂直居中 */
}

.meta-author {
  font-size: 22rpx;
  color: #86909C;
  line-height: 1.4;
}

.meta-dot {
  font-size: 22rpx;
  color: #C9CDD4;
}

.meta-time {
  font-size: 22rpx;
  color: #86909C;
  line-height: 1.4;
}

.meta-item {
  font-size: 24rpx;
  color: #86909C;
  line-height: 1.4; /* 添加行高，确保文字垂直居中 */
}

.meta-item.urgent {
  color: #FF4D4F;
}

/* 右侧统计信息 */
.item-stats {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  align-items: flex-end;
  flex-shrink: 0;
}

.stat-row {
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.stat-icon {
  font-size: 20rpx;
  line-height: 1;
}

.stat-value {
  font-size: 22rpx;
  color: #86909C;
  font-weight: 600;
  line-height: 1;
}

/* 热门标签 - 简化为橙色背景+"热"字 */
.hot-tag {
  padding: 4rpx 12rpx;
  background: #FF7D00; /* 辅助色：活力橙 */
  border-radius: 8rpx;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hot-text {
  font-size: 20rpx;
  font-weight: 600;
  color: white;
  line-height: 1;
}

/* 紧急标签 - 红色背景+"急"字 */
.urgent-tag {
  padding: 4rpx 12rpx;
  background: #FF4D4F; /* 警告色 */
  border-radius: 8rpx;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.urgent-text {
  font-size: 20rpx;
  font-weight: 600;
  color: white;
  line-height: 1;
}
</style>

