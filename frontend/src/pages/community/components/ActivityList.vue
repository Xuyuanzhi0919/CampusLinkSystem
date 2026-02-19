<template>
  <view class="activity-list">
    <view v-if="loading && list.length === 0" class="loading-container">
      <view class="loading-skeleton" v-for="i in 5" :key="i">
        <view class="skeleton-image"></view>
        <view class="skeleton-line skeleton-title"></view>
        <view class="skeleton-line skeleton-content"></view>
      </view>
    </view>

    <view v-else-if="list.length > 0" class="activity-items">
      <view
        v-for="item in list"
        :key="item.activityId"
        class="activity-card"
        @click="handleActivityClick(item.activityId)"
      >
        <!-- 活动封面 -->
        <image class="activity-cover" :src="item.cover || '/static/images/default-activity.png'" mode="aspectFill" />

        <!-- 活动类型标签 -->
        <view class="type-tag" :class="`type-${item.type || 1}`">
          <text class="tag-text">{{ getActivityType(item.type) }}</text>
        </view>

        <!-- 活动信息 -->
        <view class="activity-info">
          <text class="activity-title">{{ item.title }}</text>

          <view class="meta-info">
            <view class="meta-item">
              <Icon name="map-pin" :size="13" class="meta-icon" />
              <text class="text">{{ item.location || '待定' }}</text>
            </view>
            <view class="meta-item">
              <Icon name="clock" :size="13" class="meta-icon" />
              <text class="text">{{ formatDate(item.startTime) }}</text>
            </view>
          </view>

          <view class="footer">
            <view class="participants">
              <Icon name="users" :size="13" class="meta-icon" />
              <text class="count">{{ item.registeredCount || 0 }}/{{ item.maxParticipants || '不限' }}</text>
            </view>
            <view class="status-badge" :class="`status-${item.status || 1}`">
              <text class="status-text">{{ getStatusText(item.status) }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view v-else class="empty-container">
      <image class="empty-image" src="/static/images/empty-activity.png" mode="aspectFit" />
      <text class="empty-text">暂无活动信息</text>
    </view>

    <view v-if="loading && list.length > 0" class="loading-more">
      <text class="loading-text">加载中...</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { useNavigation } from '@/composables/useNavigation'
import Icon from '@/components/icons/index.vue'

interface Props {
  list: any[]
  loading: boolean
}

defineProps<Props>()

const { toActivityDetail } = useNavigation()

const handleActivityClick = (activityId: number) => {
  toActivityDetail(activityId)
}

const getActivityType = (type: number) => {
  const types: Record<number, string> = {
    1: '讲座',
    2: '比赛',
    3: '社交',
    4: '公益',
    5: '其他'
  }
  return types[type] || '活动'
}

const getStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    1: '报名中',
    2: '进行中',
    3: '已结束',
    4: '已取消'
  }
  return statusMap[status] || '未知'
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '待定'
  const date = new Date(dateStr)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = date.getHours()
  const minute = date.getMinutes()
  return `${month}月${day}日 ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`
}
</script>

<style scoped lang="scss">
.activity-list {
  padding: 12px 16px;
}

/* ========== 骨架屏 ========== */
.loading-skeleton {
  background: #FFFFFF;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 12px;
}

.skeleton-image {
  width: 100%;
  height: 180px;
  background: linear-gradient(90deg, #F3F4F6 25%, #E5E7EB 50%, #F3F4F6 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-line {
  background: linear-gradient(90deg, #F3F4F6 25%, #E5E7EB 50%, #F3F4F6 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  margin: 12px 16px;
}

.skeleton-title {
  height: 20px;
  width: 70%;
}

.skeleton-content {
  height: 16px;
  width: 100%;
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

/* ========== 活动卡片 ========== */
.activity-card {
  position: relative;
  background: #FFFFFF;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.98);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  }
}

.activity-cover {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.type-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 4px 12px;
  border-radius: 4px;
  backdrop-filter: blur(10px);

  &.type-1 {
    background: rgba(37, 99, 235, 0.9);
  }

  &.type-2 {
    background: rgba(220, 38, 38, 0.9);
  }

  &.type-3 {
    background: rgba(168, 85, 247, 0.9);
  }

  &.type-4 {
    background: rgba(16, 185, 129, 0.9);
  }

  &.type-5 {
    background: rgba(107, 114, 128, 0.9);
  }

  .tag-text {
    font-size: 12px;
    font-weight: 600;
    color: #FFFFFF;
  }
}

.activity-info {
  padding: 16px;
}

.activity-title {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  line-height: 1.5;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.meta-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;

  .meta-icon {
    color: #9CA3AF;
    flex-shrink: 0;
  }

  .text {
    font-size: 13px;
    color: #6B7280;
  }
}

.footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.participants {
  display: flex;
  align-items: center;
  gap: 4px;

  .meta-icon {
    color: #9CA3AF;
    flex-shrink: 0;
  }

  .count {
    font-size: 13px;
    color: #6B7280;
  }
}

.status-badge {
  padding: 4px 12px;
  border-radius: 4px;

  &.status-1 {
    background: #DBEAFE;

    .status-text {
      color: #1E40AF;
    }
  }

  &.status-2 {
    background: #D1FAE5;

    .status-text {
      color: #065F46;
    }
  }

  &.status-3 {
    background: #F3F4F6;

    .status-text {
      color: #6B7280;
    }
  }

  &.status-4 {
    background: #FEE2E2;

    .status-text {
      color: #991B1B;
    }
  }

  .status-text {
    font-size: 12px;
    font-weight: 600;
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
