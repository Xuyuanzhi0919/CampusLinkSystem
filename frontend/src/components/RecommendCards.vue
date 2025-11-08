<template>
  <view class="recommend-cards">
    <!-- 标题 -->
    <view class="header">
      <text class="title">为你推荐</text>
      <view class="refresh-btn" @click="handleRefresh">
        <text class="refresh-icon">🔄</text>
        <text class="refresh-text">换一批</text>
      </view>
    </view>

    <!-- 卡片列表（双列布局） -->
    <view class="cards-grid">
      <view
        v-for="item in items"
        :key="item.id"
        class="card-item"
        @click="handleCardClick(item)"
      >
        <!-- 类型图标 -->
        <view class="type-badge" :class="'type-' + item.type">
          <text class="type-icon">{{ getTypeIcon(item.type) }}</text>
        </view>

        <!-- 内容 -->
        <view class="card-content">
          <text class="card-title">{{ item.title }}</text>
          <text class="card-desc">{{ item.description }}</text>
        </view>

        <!-- 底部信息 -->
        <view class="card-footer">
          <view class="user-info">
            <image class="avatar" :src="item.avatar" mode="aspectFill" />
            <text class="username">{{ item.username }}</text>
          </view>
          <view class="points">
            <text class="points-icon">💰</text>
            <text class="points-text">{{ item.points }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

// 推荐项类型
interface RecommendItem {
  id: number
  type: 'resource' | 'question' | 'task'
  title: string
  description: string
  avatar: string
  username: string
  points: number
}

// 模拟数据
const items = ref<RecommendItem[]>([
  {
    id: 1,
    type: 'resource',
    title: '数据结构课件-完整版',
    description: '包含所有章节的PPT和代码示例',
    avatar: 'https://via.placeholder.com/80',
    username: '张同学',
    points: 5,
  },
  {
    id: 2,
    type: 'question',
    title: '如何学习算法？',
    description: '求推荐学习路线和资料',
    avatar: 'https://via.placeholder.com/80',
    username: '李同学',
    points: 10,
  },
  {
    id: 3,
    type: 'task',
    title: '帮忙取快递',
    description: '菜鸟驿站，今天下午',
    avatar: 'https://via.placeholder.com/80',
    username: '王同学',
    points: 3,
  },
  {
    id: 4,
    type: 'resource',
    title: '高等数学历年真题',
    description: '近5年期末考试真题及答案',
    avatar: 'https://via.placeholder.com/80',
    username: '赵同学',
    points: 3,
  },
])

/**
 * 获取类型图标
 */
const getTypeIcon = (type: string) => {
  const icons = {
    resource: '📚',
    question: '💡',
    task: '🤝',
  }
  return icons[type as keyof typeof icons] || '📄'
}

/**
 * 刷新推荐
 */
const handleRefresh = () => {
  uni.showToast({
    title: '刷新中...',
    icon: 'loading',
    duration: 1000,
  })
  // TODO: 调用 API 刷新数据
}

/**
 * 点击卡片
 */
const handleCardClick = (item: RecommendItem) => {
  const typeMap = {
    resource: 'resource',
    question: 'question',
    task: 'task',
  }
  
  uni.navigateTo({
    url: `/pages/${typeMap[item.type]}/detail?id=${item.id}`,
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
.recommend-cards {
  padding: 30rpx;
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

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  background: #F5F7FA;
  border-radius: 20rpx;
}

.refresh-icon {
  font-size: 24rpx;
}

.refresh-text {
  font-size: 24rpx;
  color: #409EFF;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.card-item {
  background: white;
  border-radius: 12rpx;
  padding: 20rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  transition: all 0.3s;
}

.card-item:active {
  transform: scale(0.98);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.type-badge {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12rpx;
}

.type-badge.type-resource {
  background: linear-gradient(135deg, #409EFF 0%, #66B1FF 100%);
}

.type-badge.type-question {
  background: linear-gradient(135deg, #FF7D00 0%, #FFA940 100%);
}

.type-badge.type-task {
  background: linear-gradient(135deg, #00B42A 0%, #52C41A 100%);
}

.type-icon {
  font-size: 32rpx;
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.card-title {
  font-size: 28rpx;
  font-weight: 500;
  color: #1D2129;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
}

.card-desc {
  font-size: 24rpx;
  color: #86909C;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12rpx;
  border-top: 1rpx solid #F5F7FA;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.avatar {
  width: 32rpx;
  height: 32rpx;
  border-radius: 50%;
}

.username {
  font-size: 22rpx;
  color: #86909C;
}

.points {
  display: flex;
  align-items: center;
  gap: 4rpx;
}

.points-icon {
  font-size: 20rpx;
}

.points-text {
  font-size: 24rpx;
  font-weight: 600;
  color: #FF7D00;
}
</style>

