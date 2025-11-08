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
            {{ index + 1 }}
          </view>
          <view class="item-content">
            <text class="item-title">{{ item.title }}</text>
            <view class="item-meta">
              <text class="meta-item">{{ item.views }}浏览</text>
              <text class="meta-item">{{ item.answers }}回答</text>
            </view>
          </view>
          <view v-if="item.hot" class="hot-tag">🔥</view>
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
            {{ index + 1 }}
          </view>
          <view class="item-content">
            <text class="item-title">{{ item.title }}</text>
            <view class="item-meta">
              <text class="meta-item">{{ item.downloads }}下载</text>
              <text class="meta-item">{{ item.score }}积分</text>
            </view>
          </view>
          <view v-if="item.hot" class="hot-tag">🔥</view>
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
              <text class="meta-item">{{ item.reward }}积分</text>
              <text class="meta-item urgent">{{ item.deadline }}</text>
            </view>
          </view>
          <view class="urgent-tag">⚡</view>
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
  { id: 1, title: '如何学习数据结构与算法？', views: 1520, answers: 23, hot: true },
  { id: 2, title: '大学生如何提高编程能力？', views: 1230, answers: 18, hot: true },
  { id: 3, title: '考研数学如何复习？', views: 980, answers: 15, hot: true },
  { id: 4, title: '英语四级如何备考？', views: 856, answers: 12, hot: false },
  { id: 5, title: '如何平衡学习和社团活动？', views: 720, answers: 10, hot: false },
])

const resourceList = ref([
  { id: 1, title: '数据结构课件-完整版', downloads: 2340, score: 5, hot: true },
  { id: 2, title: '高等数学历年真题', downloads: 1890, score: 3, hot: true },
  { id: 3, title: '计算机网络笔记', downloads: 1560, score: 2, hot: true },
  { id: 4, title: '操作系统期末复习资料', downloads: 1230, score: 4, hot: false },
  { id: 5, title: '英语四级词汇表', downloads: 980, score: 1, hot: false },
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
  background: white;
  border-radius: 12rpx;
  padding: 30rpx;
  margin: 20rpx 30rpx;
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
  align-items: center;
  gap: 20rpx;
  padding: 16rpx;
  border-radius: 8rpx;
  transition: all 0.3s;
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
  font-weight: 600;
  color: #86909C;
  background: #F5F7FA;
  border-radius: 8rpx;
  flex-shrink: 0;
}

.rank-number.rank-1 {
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  color: white;
}

.rank-number.rank-2 {
  background: linear-gradient(135deg, #C0C0C0 0%, #A8A8A8 100%);
  color: white;
}

.rank-number.rank-3 {
  background: linear-gradient(135deg, #CD7F32 0%, #B8860B 100%);
  color: white;
}

.item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  overflow: hidden;
}

.item-title {
  font-size: 28rpx;
  color: #1D2129;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  display: flex;
  gap: 24rpx;
}

.meta-item {
  font-size: 24rpx;
  color: #86909C;
}

.meta-item.urgent {
  color: #FF4D4F;
}

.hot-tag,
.urgent-tag {
  font-size: 28rpx;
  flex-shrink: 0;
}
</style>

