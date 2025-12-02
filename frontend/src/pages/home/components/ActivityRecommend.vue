<template>
  <view class="activity-recommend">
    <!-- 装饰元素 -->
    <view class="section-decoration">
      <text class="deco-emoji left">🎉</text>
      <text class="deco-emoji right">🎊</text>
    </view>

    <view class="section-header">
      <view class="title-wrap">
        <text class="section-title">社团活动推荐</text>
        <view class="title-badge">
          <text class="badge-dot"></text>
          <text class="badge-text">热门</text>
        </view>
      </view>
      <view class="view-more" @click="handleViewMore">
        <text class="more-text">探索更多</text>
        <svg class="more-arrow" width="12" height="12" viewBox="0 0 24 24" fill="none">
          <path d="M5 12H19M19 12L12 5M19 12L12 19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
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
        <view class="activity-cover" :class="getActivityType(item.icon)">
          <view class="cover-placeholder">
            <!-- 电脑/编程活动 -->
            <svg v-if="item.icon === '💻'" class="cover-icon" width="40" height="40" viewBox="0 0 24 24" fill="none">
              <rect x="2" y="3" width="20" height="14" rx="2" stroke="currentColor" stroke-width="2"/>
              <path d="M8 21H16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M12 17V21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M7 10L9 12L7 14" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M13 14H17" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <!-- 英语/交流活动 -->
            <svg v-else-if="item.icon === '🗣️'" class="cover-icon" width="40" height="40" viewBox="0 0 24 24" fill="none">
              <path d="M21 15C21 15.5304 20.7893 16.0391 20.4142 16.4142C20.0391 16.7893 19.5304 17 19 17H7L3 21V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H19C19.5304 3 20.0391 3.21071 20.4142 3.58579C20.7893 3.96086 21 4.46957 21 5V15Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <circle cx="8" cy="10" r="1" fill="currentColor"/>
              <circle cx="12" cy="10" r="1" fill="currentColor"/>
              <circle cx="16" cy="10" r="1" fill="currentColor"/>
            </svg>
            <!-- 摄影活动 -->
            <svg v-else-if="item.icon === '📷'" class="cover-icon" width="40" height="40" viewBox="0 0 24 24" fill="none">
              <path d="M23 19C23 19.5304 22.7893 20.0391 22.4142 20.4142C22.0391 20.7893 21.5304 21 21 21H3C2.46957 21 1.96086 20.7893 1.58579 20.4142C1.21071 20.0391 1 19.5304 1 19V8C1 7.46957 1.21071 6.96086 1.58579 6.58579C1.96086 6.21071 2.46957 6 3 6H7L9 3H15L17 6H21C21.5304 6 22.0391 6.21071 22.4142 6.58579C22.7893 6.96086 23 7.46957 23 8V19Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <circle cx="12" cy="13" r="4" stroke="currentColor" stroke-width="2"/>
            </svg>
            <!-- 默认活动 -->
            <svg v-else class="cover-icon" width="40" height="40" viewBox="0 0 24 24" fill="none">
              <rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
              <path d="M16 2V6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M8 2V6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M3 10H21" stroke="currentColor" stroke-width="2"/>
              <path d="M8 14H10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M14 14H16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M8 18H10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
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
              <svg class="meta-icon" width="14" height="14" viewBox="0 0 24 24" fill="none">
                <rect x="3" y="4" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
                <path d="M16 2V6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M8 2V6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M3 10H21" stroke="currentColor" stroke-width="2"/>
              </svg>
              <text class="meta-text">{{ item.time }}</text>
            </view>
            <view class="meta-row">
              <svg class="meta-icon" width="14" height="14" viewBox="0 0 24 24" fill="none">
                <path d="M21 10C21 17 12 23 12 23C12 23 3 17 3 10C3 7.61305 3.94821 5.32387 5.63604 3.63604C7.32387 1.94821 9.61305 1 12 1C14.3869 1 16.6761 1.94821 18.364 3.63604C20.0518 5.32387 21 7.61305 21 10Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="12" cy="10" r="3" stroke="currentColor" stroke-width="2"/>
              </svg>
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

// 获取活动类型样式
const getActivityType = (icon: string) => {
  const types: Record<string, string> = {
    '💻': 'type-code',
    '🗣️': 'type-talk',
    '📷': 'type-photo'
  }
  return types[icon] || 'type-default'
}

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
  margin: 0 calc(-1 * $sp-16);
  padding: 48px $sp-16 56px;
  // 使用橙色调（$campus-amber）作为活动区调味色
  background: linear-gradient(180deg, rgba($campus-amber, 0.04) 0%, rgba($campus-amber, 0.08) 30%, rgba($campus-amber, 0.04) 60%, $white 100%);
  border-radius: $campus-radius-lg;
  position: relative;
  overflow: hidden;

  // 装饰性背景光斑
  &::before {
    content: '';
    position: absolute;
    top: 20%;
    left: -5%;
    width: 200px;
    height: 200px;
    background: radial-gradient(circle, rgba($campus-amber, 0.1) 0%, transparent 70%);
    border-radius: 50%;
    pointer-events: none;
  }

  &::after {
    content: '';
    position: absolute;
    bottom: 10%;
    right: -5%;
    width: 180px;
    height: 180px;
    background: radial-gradient(circle, rgba($campus-violet, 0.08) 0%, transparent 70%);
    border-radius: 50%;
    pointer-events: none;
  }

  @media (max-width: 1440px) {
    margin: 0 calc(-1 * $sp-12);
    padding: 40px $sp-12 48px;
  }

  @include mobile {
    margin: 0 calc(-1 * $sp-4);
    padding: 32px $sp-4 40px;
  }
}

// 装饰emoji
.section-decoration {
  position: absolute;
  top: 32px;
  left: 0;
  right: 0;
  pointer-events: none;

  .deco-emoji {
    position: absolute;
    font-size: 24px;
    opacity: 0.6;
    animation: floatEmoji 3s ease-in-out infinite;

    &.left {
      left: 10%;
      animation-delay: 0s;
    }

    &.right {
      right: 10%;
      animation-delay: 1.5s;
    }
  }
}

@keyframes floatEmoji {
  0%, 100% { transform: translateY(0) rotate(-5deg); }
  50% { transform: translateY(-8px) rotate(5deg); }
}

.section-header {
  @include section-header;
  position: relative;
  z-index: 1;
}

.title-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-title {
  @include heading-h2;
}

.title-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: linear-gradient(135deg, rgba($campus-amber, 0.12), rgba($campus-amber, 0.2));
  border-radius: $campus-radius-sm;

  .badge-dot {
    width: 6px;
    height: 6px;
    background: $campus-amber;
    border-radius: 50%;
    animation: pulseDot 1.5s ease-in-out infinite;
  }

  .badge-text {
    font-size: 11px;
    font-weight: 500;
    color: $campus-amber;
  }
}

@keyframes pulseDot {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(0.9); }
}

.view-more {
  @include view-more-link;
  color: $campus-amber;

  &:hover {
    color: darken($campus-amber, 10%);
  }
}

.activities-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $module-gap-sm;

  @media (max-width: 1200px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @include mobile {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}

.activity-card {
  background: $white;
  border-radius: $campus-radius;
  box-shadow: $campus-shadow-card;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: $campus-shadow-hover;
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
      border-radius: $campus-radius-sm;
      margin-bottom: 12rpx;
    }

    .skeleton-info {
      height: 60rpx;
      width: 100%;
      background: linear-gradient(90deg, $gray-100 25%, $gray-50 50%, $gray-100 75%);
      background-size: 200% 100%;
      animation: shimmer 1.5s infinite;
      border-radius: $campus-radius-sm;
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
  background: $gray-50;

  // 编程活动 - 使用主色蓝
  &.type-code {
    background: linear-gradient(135deg, rgba($campus-blue, 0.08), rgba($campus-blue, 0.15));
    color: $campus-blue;
  }

  // 交流活动 - 使用强调色橙
  &.type-talk {
    background: linear-gradient(135deg, rgba($campus-amber, 0.1), rgba($campus-amber, 0.18));
    color: $campus-amber;
  }

  // 摄影活动 - 使用信息色紫
  &.type-photo {
    background: linear-gradient(135deg, rgba($campus-violet, 0.1), rgba($campus-violet, 0.18));
    color: $campus-violet;
  }

  // 默认活动 - 使用辅助色青绿
  &.type-default {
    background: linear-gradient(135deg, rgba($campus-teal, 0.1), rgba($campus-teal, 0.18));
    color: $campus-teal;
  }

  .cover-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;

    .cover-icon {
      width: 40px;
      height: 40px;
    }
  }

  .status-tag {
    position: absolute;
    top: 12rpx;
    left: 12rpx;
    padding: 4rpx 12rpx;
    border-radius: $campus-radius-sm;

    // 即将开始 - 使用主色蓝
    &.status-upcoming {
      background: $campus-blue;

      .tag-text {
        color: $white;
      }
    }

    // 进行中 - 使用辅助色青绿
    &.status-ongoing {
      background: $campus-teal;

      .tag-text {
        color: $white;
      }
    }

    // 已结束 - 使用灰色
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
  font-size: 16px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
  line-height: 1.4;
  margin-bottom: 12px;
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
  background: $campus-blue;
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
  gap: 6px;
  margin-bottom: 4rpx;

  &:last-child {
    margin-bottom: 0;
  }

  .meta-icon {
    width: 14px;
    height: 14px;
    color: $text-tertiary;
    flex-shrink: 0;
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
