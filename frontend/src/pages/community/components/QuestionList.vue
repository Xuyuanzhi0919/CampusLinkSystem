<template>
  <view class="question-list">
    <view v-if="loading && list.length === 0" class="loading-container">
      <view class="loading-skeleton" v-for="i in 5" :key="i">
        <view class="skeleton-line skeleton-title"></view>
        <view class="skeleton-line skeleton-content"></view>
        <view class="skeleton-line skeleton-footer"></view>
      </view>
    </view>

    <view v-else-if="list.length > 0" class="question-items">
      <view
        v-for="item in list"
        :key="item.questionId"
        class="question-card"
        @click="handleQuestionClick(item.questionId)"
      >
        <!-- 用户信息 -->
        <view class="user-info">
          <image class="avatar" :src="item.avatar || '/static/images/default-avatar.png'" mode="aspectFill" />
          <text class="username">{{ item.username }}</text>
          <text class="time">{{ formatTime(item.createdAt) }}</text>
        </view>

        <!-- 问题标题 -->
        <text class="question-title">{{ item.title }}</text>

        <!-- 问题内容预览 -->
        <text v-if="item.content" class="question-content">{{ item.content }}</text>

        <!-- 标签 -->
        <view v-if="item.tags && item.tags.length > 0" class="tags">
          <text v-for="tag in item.tags.slice(0, 3)" :key="tag" class="tag">{{ tag }}</text>
        </view>

        <!-- 底部统计 -->
        <view class="footer">
          <view class="stat-item">
            <text class="icon">💬</text>
            <text class="count">{{ item.answerCount || 0 }}</text>
          </view>
          <view class="stat-item">
            <text class="icon">👀</text>
            <text class="count">{{ item.viewCount || 0 }}</text>
          </view>
          <view v-if="item.isSolved" class="solved-badge">
            <text class="badge-text">✓ 已解决</text>
          </view>
        </view>
      </view>
    </view>

    <view v-else class="empty-container">
      <image class="empty-image" src="/static/images/empty-question.png" mode="aspectFit" />
      <text class="empty-text">暂无问答内容</text>
    </view>

    <view v-if="loading && list.length > 0" class="loading-more">
      <text class="loading-text">加载中...</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { useNavigation } from '@/composables/useNavigation'

interface Props {
  list: any[]
  loading: boolean
}

defineProps<Props>()

const { toQuestionDetail } = useNavigation()

/**
 * 点击问题卡片
 */
const handleQuestionClick = (questionId: number) => {
  toQuestionDetail(questionId)
}

/**
 * 格式化时间
 */
const formatTime = (time: string) => {
  const now = new Date().getTime()
  const target = new Date(time).getTime()
  const diff = now - target

  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < 7 * day) {
    return `${Math.floor(diff / day)}天前`
  } else {
    return time.split(' ')[0]
  }
}
</script>

<style scoped lang="scss">
.question-list {
  padding: 12px 16px;
}

/* ========== 骨架屏 ========== */
.loading-skeleton {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;

  .skeleton-line {
    background: linear-gradient(90deg, #F3F4F6 25%, #E5E7EB 50%, #F3F4F6 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
    border-radius: 4px;
    margin-bottom: 12px;
  }

  .skeleton-title {
    height: 20px;
    width: 70%;
  }

  .skeleton-content {
    height: 16px;
    width: 100%;
  }

  .skeleton-footer {
    height: 16px;
    width: 40%;
    margin-bottom: 0;
  }
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

/* ========== 问题卡片 ========== */
.question-card {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.98);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  }
}

.user-info {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-right: 8px;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #1F2937;
  margin-right: 8px;
}

.time {
  font-size: 12px;
  color: #9CA3AF;
}

.question-title {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  line-height: 1.5;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.question-content {
  display: block;
  font-size: 14px;
  color: #6B7280;
  line-height: 1.6;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.tag {
  padding: 4px 10px;
  background: #EFF6FF;
  color: #2563EB;
  font-size: 12px;
  border-radius: 4px;
}

.footer {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;

  .icon {
    font-size: 14px;
  }

  .count {
    font-size: 13px;
    color: #6B7280;
  }
}

.solved-badge {
  margin-left: auto;
  padding: 4px 10px;
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
  border-radius: 4px;

  .badge-text {
    font-size: 12px;
    font-weight: 600;
    color: #FFFFFF;
  }
}

/* ========== 空状态 ========== */
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.empty-image {
  width: 200px;
  height: 200px;
  margin-bottom: 16px;
  opacity: 0.6;
}

.empty-text {
  font-size: 14px;
  color: #9CA3AF;
}

/* ========== 加载更多 ========== */
.loading-more {
  padding: 20px;
  text-align: center;
}

.loading-text {
  font-size: 13px;
  color: #9CA3AF;
}
</style>
