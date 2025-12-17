<template>
  <!-- ========== 最近活动(唯一主内容块) ========== -->
  <view v-if="recentActivities.length > 0" class="recent-activity-section">
    <view class="section-header">
      <text class="section-title">你的校园足迹</text>
      <text class="section-subtitle">最近 {{ recentActivities.length }} 条动态</text>
    </view>

    <view class="activities-list">
      <view
        v-for="(activity, index) in recentActivities"
        :key="index"
        class="activity-item"
        @click="handleActivityClick(activity)"
      >
        <view class="activity-icon-wrapper" :class="activity.type">
          <Icon :name="activity.icon" :size="18" class="activity-icon" />
        </view>
        <view class="activity-content">
          <text class="activity-text">{{ activity.text }}</text>
          <text class="activity-time">{{ activity.time }}</text>
        </view>
        <Icon name="chevron-right" :size="16" class="activity-arrow" />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Icon from '@/components/icons/index.vue'

/**
 * 🎯 最近活动动态
 * TODO: 后端提供真实的用户活动记录API
 */
interface RecentActivity {
  type: 'resource' | 'question' | 'task' | 'activity'
  icon: string
  text: string
  time: string
  path?: string
}

const recentActivities = computed<RecentActivity[]>(() => {
  // TODO: 从后端获取真实的用户活动记录
  const mockActivities: RecentActivity[] = [
    {
      type: 'resource',
      icon: 'file-text',
      text: '上传了《数据结构期末复习笔记》',
      time: '2小时前',
      path: '/pages/resource/detail?id=123'
    },
    {
      type: 'question',
      icon: 'message-circle',
      text: '回答了问题《如何理解红黑树？》',
      time: '5小时前',
      path: '/pages/question/detail?id=456'
    },
    {
      type: 'task',
      icon: 'clock',
      text: '接受了任务《帮忙打印课件》',
      time: '昨天',
      path: '/pages/task/detail?id=789'
    }
  ]

  return mockActivities.slice(0, 3)
})

/**
 * 处理活动项点击
 */
const handleActivityClick = (activity: RecentActivity) => {
  if (activity.path) {
    uni.navigateTo({
      url: activity.path,
      fail: () => {
        uni.showToast({
          title: '页面开发中...',
          icon: 'none'
        })
      }
    })
  }
}
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

/* ========== 最近活动(唯一主内容块) ========== */
.recent-activity-section {
  background: $white;
  border-radius: 24rpx;
  margin: 0 24rpx 48rpx; // 🎯 下边距48rpx,拉开距离
  overflow: hidden;
  // 🎯 最强阴影,视觉锚点
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
  border: 1rpx solid #E5E7EB;
}

/* 分区标题 */
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 32rpx 24rpx; // 🎯 加大内边距
  border-bottom: 1rpx solid #F3F4F6;
}

.section-title {
  font-size: 32rpx; // 🎯 加大字号
  font-weight: 700; // 🎯 加粗
  color: #111827;
}

.section-subtitle {
  font-size: 24rpx;
  color: #9CA3AF;
  font-weight: 500;
}

/* 活动列表 */
.activities-list {
  padding: 0;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 20rpx; // 🎯 增加间距
  padding: 28rpx 32rpx; // 🎯 加大内边距
  cursor: pointer;
  transition: all 0.2s ease;
  border-bottom: 1rpx solid #F9FAFB;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background: #F9FAFB;
  }
}

.activity-icon-wrapper {
  width: 52rpx; // 🎯 加大图标容器
  height: 52rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.resource {
    background: linear-gradient(135deg, #EFF6FF 0%, #DBEAFE 100%);
  }

  &.question {
    background: linear-gradient(135deg, #F0FDF4 0%, #DCFCE7 100%);
  }

  &.task {
    background: linear-gradient(135deg, #FFF7ED 0%, #FFEDD5 100%);
  }

  &.activity {
    background: linear-gradient(135deg, #FEF2F2 0%, #FEE2E2 100%);
  }
}

.activity-icon {
  flex-shrink: 0;

  .resource & {
    color: #2563EB;
  }

  .question & {
    color: #16A34A;
  }

  .task & {
    color: #F97316;
  }

  .activity & {
    color: #EF4444;
  }
}

.activity-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.activity-text {
  font-size: 28rpx; // 🎯 加大字号
  color: #111827;
  font-weight: 500;
  line-height: 1.4;
  @include text-ellipsis(1);
}

.activity-time {
  font-size: 24rpx;
  color: #9CA3AF;
  font-weight: 400;
}

.activity-arrow {
  color: #D1D5DB;
  flex-shrink: 0;
}
</style>
