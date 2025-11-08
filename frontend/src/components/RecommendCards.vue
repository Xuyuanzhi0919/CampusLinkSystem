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
        <!-- 类型标签 -->
        <view class="type-badge" :class="'type-' + item.type">
          <text class="type-icon">{{ getTypeIcon(item.type) }}</text>
          <text class="type-label">{{ getTypeLabel(item.type) }}</text>
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
import { getRandomAvatar } from '@/config/images'

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
    avatar: getRandomAvatar('male'),
    username: '张同学',
    points: 5,
  },
  {
    id: 2,
    type: 'question',
    title: '如何学习算法？',
    description: '求推荐学习路线和资料',
    avatar: getRandomAvatar('female'),
    username: '李同学',
    points: 10,
  },
  {
    id: 3,
    type: 'task',
    title: '帮忙取快递',
    description: '菜鸟驿站，今天下午',
    avatar: getRandomAvatar('male'),
    username: '王同学',
    points: 3,
  },
  {
    id: 4,
    type: 'resource',
    title: '高等数学历年真题',
    description: '近5年期末考试真题及答案',
    avatar: getRandomAvatar('female'),
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
 * 获取类型标签文字
 */
const getTypeLabel = (type: string) => {
  const labels = {
    resource: '课件',
    question: '问答',
    task: '任务',
  }
  return labels[type as keyof typeof labels] || '其他'
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
  /* 轻卡片容器：白色背景 + 4px圆角 + 轻微阴影 */
  background: white;
  border-radius: 8rpx; /* 4px */
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

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 8rpx 16rpx;
  background: #FFF7E6; /* 从浅灰改为浅橙色背景 */
  border-radius: 20rpx;
  transition: all 0.2s ease; /* 添加过渡效果 */
  cursor: pointer;
}

.refresh-btn:hover {
  background: #FFE7BA; /* hover时背景加深 */
  transform: translateY(-2rpx); /* 上浮2px */
}

.refresh-btn:active {
  transform: translateY(0); /* 点击时恢复 */
}

.refresh-icon {
  font-size: 24rpx;
}

.refresh-text {
  font-size: 24rpx;
  font-weight: 600; /* 加粗文字 */
  color: #FF7D00; /* 从青春蓝改为活力橙，更醒目 */
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.card-item {
  background: white;
  border-radius: 16rpx; /* 从 12rpx 调整为 16rpx (8px) */
  border: 2rpx solid #E5E7EB; /* 1px 浅灰边框 */
  padding: 24rpx; /* 从 20rpx 调整为 24rpx，增加内边距 */
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04); /* 减轻阴影 */
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  transition: all 0.3s;
  min-height: 240rpx; /* 设置最小高度，确保卡片等高，视觉更规整 */
}

.card-item:active {
  transform: scale(0.98);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
  border-color: #D1D5DB; /* active 时边框颜色加深 */
}

.type-badge {
  padding: 8rpx 16rpx; /* 从固定尺寸改为 padding */
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  border-radius: 8rpx;
  font-size: 22rpx;
  font-weight: 600;
  line-height: 1;
}

/* 课件标签：青春蓝 */
.type-badge.type-resource {
  background: #E6F4FF; /* 浅蓝背景 */
  color: #409EFF; /* 蓝色文字 */
}

/* 问答标签：活力橙 */
.type-badge.type-question {
  background: #FFF7E6; /* 浅橙背景 */
  color: #FF7D00; /* 橙色文字 */
}

/* 任务标签：薄荷绿 */
.type-badge.type-task {
  background: #E8F7ED; /* 浅绿背景 */
  color: #52C41A; /* 绿色文字 */
}

.type-icon {
  font-size: 20rpx;
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.card-title {
  font-size: 30rpx; /* 从 28rpx 调整为 30rpx，更醒目 */
  font-weight: 600; /* 从 500 调整为 600，加粗标题 */
  color: #1D2129;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5; /* 从 1.4 调整为 1.5，增加行高 */
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

