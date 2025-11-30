<template>
  <view class="activity-recommend">
    <view class="section-header">
      <text class="section-title">社团活动推荐</text>
      <view class="view-more" @click="handleViewMore">
        <text class="more-text">查看更多</text>
        <text class="more-arrow">→</text>
      </view>
    </view>

    <!-- 骨架屏 -->
    <view v-if="loading" class="activities-grid">
      <view v-for="i in 3" :key="i" class="activity-card skeleton">
        <view class="skeleton-cover"></view>
        <view class="skeleton-content">
          <view class="skeleton-title"></view>
          <view class="skeleton-info"></view>
        </view>
      </view>
    </view>

    <!-- 活动列表 -->
    <view v-else class="activities-grid">
      <view
        v-for="item in activityList"
        :key="item.id"
        class="activity-card"
        @click="handleActivityClick(item)"
      >
        <!-- 封面图 -->
        <view class="activity-cover">
          <view class="cover-placeholder">
            <text class="cover-icon">{{ item.icon }}</text>
          </view>
          <!-- 状态标签 -->
          <view class="status-tag" :class="getStatusClass(item.status)">
            <text class="tag-text">{{ getStatusText(item.status) }}</text>
          </view>
        </view>

        <!-- 活动信息 -->
        <view class="activity-info">
          <text class="activity-title">{{ item.title }}</text>

          <!-- 社团信息 -->
          <view class="club-info">
            <view class="club-avatar">
              <text class="avatar-text">{{ item.clubName.charAt(0) }}</text>
            </view>
            <text class="club-name">{{ item.clubName }}</text>
          </view>

          <!-- 时间地点 -->
          <view class="activity-meta">
            <view class="meta-row">
              <text class="meta-icon">📅</text>
              <text class="meta-text">{{ item.time }}</text>
            </view>
            <view class="meta-row">
              <text class="meta-icon">📍</text>
              <text class="meta-text">{{ item.location }}</text>
            </view>
          </view>

          <!-- 参与人数 -->
          <view class="activity-footer">
            <view class="participants">
              <view class="avatar-stack">
                <view v-for="i in 3" :key="i" class="mini-avatar">
                  <text class="mini-text">{{ i }}</text>
                </view>
              </view>
              <text class="participant-count">{{ item.participants }}人已报名</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface Activity {
  id: number
  title: string
  clubName: string
  icon: string
  time: string
  location: string
  participants: number
  status: 'upcoming' | 'ongoing' | 'ended'
}

const emit = defineEmits<{
  (e: 'activity-click', item: Activity): void
  (e: 'view-more'): void
}>()

const loading = ref(true)
const activityList = ref<Activity[]>([])

// 获取状态样式类
const getStatusClass = (status: string) => {
  const classes: Record<string, string> = {
    'upcoming': 'status-upcoming',
    'ongoing': 'status-ongoing',
    'ended': 'status-ended'
  }
  return classes[status] || ''
}

// 获取状态文本
const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    'upcoming': '即将开始',
    'ongoing': '进行中',
    'ended': '已结束'
  }
  return texts[status] || ''
}

const handleActivityClick = (item: Activity) => {
  emit('activity-click', item)
}

const handleViewMore = () => {
  emit('view-more')
}

// 模拟数据加载
const loadData = async () => {
  loading.value = true

  await new Promise(resolve => setTimeout(resolve, 700))

  activityList.value = [
    {
      id: 1,
      title: 'ACM 程序设计竞赛集训',
      clubName: '计算机协会',
      icon: '💻',
      time: '12月15日 14:00',
      location: '信息楼 A301',
      participants: 45,
      status: 'upcoming'
    },
    {
      id: 2,
      title: '英语角周末交流会',
      clubName: '英语社',
      icon: '🗣️',
      time: '12月16日 19:00',
      location: '外语楼 B201',
      participants: 32,
      status: 'upcoming'
    },
    {
      id: 3,
      title: '摄影技巧分享沙龙',
      clubName: '摄影社',
      icon: '📷',
      time: '12月17日 15:00',
      location: '艺术楼 C102',
      participants: 28,
      status: 'ongoing'
    }
  ]

  loading.value = false
}

defineExpose({ loadData })

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.activity-recommend {
  margin-bottom: $sp-12;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $sp-6;
}

.section-title {
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.view-more {
  display: flex;
  align-items: center;
  gap: 4rpx;
  cursor: pointer;
  transition: $transition-fast;

  &:hover {
    .more-text,
    .more-arrow {
      color: $primary;
    }

    .more-arrow {
      transform: translateX(4rpx);
    }
  }

  .more-text {
    font-size: $font-size-sm;
    color: $text-tertiary;
    transition: $transition-fast;
  }

  .more-arrow {
    font-size: $font-size-sm;
    color: $text-tertiary;
    transition: $transition-fast;
  }
}

.activities-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $sp-6;

  @media (max-width: 1200px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @include mobile {
    grid-template-columns: 1fr;
    gap: $sp-4;
  }
}

.activity-card {
  background: $bg-surface;
  border-radius: $radius-md;
  overflow: hidden;
  cursor: pointer;
  transition: $transition-base;
  border: 1px solid $border-light;

  &:hover {
    border-color: $primary-200;
    box-shadow: $shadow-md;
    transform: translateY(-4rpx);
  }

  // 骨架屏
  &.skeleton {
    pointer-events: none;

    .skeleton-cover {
      height: 180rpx;
      background: linear-gradient(90deg, $gray-100 25%, $gray-50 50%, $gray-100 75%);
      background-size: 200% 100%;
      animation: shimmer 1.5s infinite;
    }

    .skeleton-content {
      padding: $sp-5;
    }

    .skeleton-title {
      height: 32rpx;
      width: 80%;
      background: linear-gradient(90deg, $gray-100 25%, $gray-50 50%, $gray-100 75%);
      background-size: 200% 100%;
      animation: shimmer 1.5s infinite;
      border-radius: $radius-xs;
      margin-bottom: 12rpx;
    }

    .skeleton-info {
      height: 60rpx;
      width: 100%;
      background: linear-gradient(90deg, $gray-100 25%, $gray-50 50%, $gray-100 75%);
      background-size: 200% 100%;
      animation: shimmer 1.5s infinite;
      border-radius: $radius-xs;
    }
  }
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.activity-cover {
  height: 180rpx;
  position: relative;
  background: linear-gradient(135deg, $pink-100, $pink-50);

  .cover-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;

    .cover-icon {
      font-size: 64rpx;
    }
  }

  .status-tag {
    position: absolute;
    top: 12rpx;
    left: 12rpx;
    padding: 4rpx 12rpx;
    border-radius: $radius-xs;

    &.status-upcoming {
      background: $primary;

      .tag-text {
        color: $white;
      }
    }

    &.status-ongoing {
      background: $success;

      .tag-text {
        color: $white;
      }
    }

    &.status-ended {
      background: $gray-400;

      .tag-text {
        color: $white;
      }
    }

    .tag-text {
      font-size: $font-size-xs;
      font-weight: $font-weight-medium;
    }
  }
}

.activity-info {
  padding: $sp-5;
}

.activity-title {
  display: block;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $text-primary;
  line-height: $line-height-snug;
  margin-bottom: 12rpx;
  @include text-ellipsis(1);
}

.club-info {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 12rpx;
}

.club-avatar {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, $accent, $accent-light);
  display: flex;
  align-items: center;
  justify-content: center;

  .avatar-text {
    font-size: 18rpx;
    color: $white;
    font-weight: $font-weight-medium;
  }
}

.club-name {
  font-size: $font-size-sm;
  color: $text-secondary;
}

.activity-meta {
  margin-bottom: 12rpx;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 6rpx;
  margin-bottom: 4rpx;

  &:last-child {
    margin-bottom: 0;
  }

  .meta-icon {
    font-size: 20rpx;
  }

  .meta-text {
    font-size: $font-size-xs;
    color: $text-tertiary;
  }
}

.activity-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.participants {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.avatar-stack {
  display: flex;
  margin-left: 8rpx;
}

.mini-avatar {
  width: 28rpx;
  height: 28rpx;
  border-radius: 50%;
  background: $gray-200;
  border: 2rpx solid $white;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: -8rpx;

  &:first-child {
    margin-left: 0;
  }

  .mini-text {
    font-size: 14rpx;
    color: $text-quaternary;
  }
}

.participant-count {
  font-size: $font-size-xs;
  color: $text-quaternary;
}
</style>
