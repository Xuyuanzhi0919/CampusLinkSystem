<template>
  <!-- 加载中骨架 -->
  <view v-if="loading" class="recent-activity-section">
    <view class="section-header">
      <text class="section-title">你的校园足迹</text>
    </view>
    <view v-for="i in 3" :key="i" class="activity-item skeleton-item">
      <view class="skeleton-icon" />
      <view class="skeleton-content">
        <view class="skeleton-line long" />
        <view class="skeleton-line short" />
      </view>
    </view>
  </view>

  <!-- 有数据时 -->
  <view v-else-if="activities.length > 0" class="recent-activity-section">
    <view class="section-header">
      <text class="section-title">你的校园足迹</text>
      <text class="section-subtitle">最近 {{ activities.length }} 条动态</text>
    </view>

    <view class="activities-list">
      <view
        v-for="(item, index) in activities"
        :key="index"
        class="activity-item"
        @click="handleClick(item)"
      >
        <view class="activity-icon-wrapper" :class="item.type">
          <Icon :name="item.icon" :size="18" class="activity-icon" />
        </view>
        <view class="activity-content">
          <text class="activity-text">{{ item.text }}</text>
          <text class="activity-time">{{ item.time }}</text>
        </view>
        <view class="activity-points" :class="item.pointsChange > 0 ? 'gain' : 'cost'">
          {{ item.pointsChange > 0 ? '+' : '' }}{{ item.pointsChange }}
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Icon from '@/components/icons/index.vue'
import { getPointsLog } from '@/services/user'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

interface ActivityItem {
  type: 'resource' | 'question' | 'task' | 'signin' | 'other'
  icon: string
  text: string
  time: string
  pointsChange: number
  path?: string
}

const loading = ref(true)
const activities = ref<ActivityItem[]>([])

const TYPE_MAP: Record<string, { type: ActivityItem['type']; icon: string; pathPrefix: string }> = {
  resource: { type: 'resource', icon: 'file-text',     pathPrefix: '/pages/resource/detail?id=' },
  question: { type: 'question', icon: 'message-circle', pathPrefix: '/pages/question/detail?id=' },
  task:     { type: 'task',     icon: 'clock',          pathPrefix: '/pages/task/detail?id=' },
  signin:   { type: 'signin',   icon: 'star',           pathPrefix: '' },
}

onMounted(async () => {
  try {
    const res = await getPointsLog(1, 5)
    activities.value = (res.records ?? []).map(log => {
      const meta = TYPE_MAP[log.relatedType ?? ''] ?? { type: 'other', icon: 'zap', pathPrefix: '' }
      return {
        type: meta.type as ActivityItem['type'],
        icon: meta.icon,
        text: log.reason || '积分变动',
        time: dayjs(log.createdAt).fromNow(),
        pointsChange: log.pointsChange,
        path: meta.pathPrefix && log.relatedId ? meta.pathPrefix + log.relatedId : undefined,
      }
    })
  } catch {
    // 接口失败静默处理，不展示模块
  } finally {
    loading.value = false
  }
})

const handleClick = (item: ActivityItem) => {
  if (item.path) {
    uni.navigateTo({ url: item.path })
  }
}
</script>

<style lang="scss" scoped>
.recent-activity-section {
  background: $white;
  border-radius: 24rpx;
  margin: 0 24rpx 48rpx;
  overflow: hidden;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
  border: 1rpx solid #E5E7EB;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 32rpx 24rpx;
  border-bottom: 1rpx solid #F3F4F6;
}

.section-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #111827;
}

.section-subtitle {
  font-size: 24rpx;
  color: #9CA3AF;
  font-weight: 500;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 28rpx 32rpx;
  border-bottom: 1rpx solid #F9FAFB;
  &:last-child { border-bottom: none; }
  &:active { background: #F9FAFB; }
}

.activity-icon-wrapper {
  width: 52rpx; height: 52rpx;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;

  &.resource  { background: linear-gradient(135deg, #EFF6FF 0%, #DBEAFE 100%); }
  &.question  { background: linear-gradient(135deg, #F0FDF4 0%, #DCFCE7 100%); }
  &.task      { background: linear-gradient(135deg, #FFF7ED 0%, #FFEDD5 100%); }
  &.signin    { background: linear-gradient(135deg, #FEFCE8 0%, #FEF08A 100%); }
  &.other     { background: linear-gradient(135deg, #F5F3FF 0%, #EDE9FE 100%); }
}

.activity-icon {
  .resource & { color: #2563EB; }
  .question & { color: #16A34A; }
  .task &     { color: #F97316; }
  .signin &   { color: #CA8A04; }
  .other &    { color: #7C3AED; }
}

.activity-content {
  flex: 1; min-width: 0;
  display: flex; flex-direction: column; gap: 6rpx;
}

.activity-text {
  font-size: 28rpx; color: #111827; font-weight: 500;
  line-height: 1.4;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

.activity-time {
  font-size: 24rpx; color: #9CA3AF; font-weight: 400;
}

.activity-points {
  font-size: 26rpx; font-weight: 700; flex-shrink: 0;
  &.gain { color: #16A34A; }
  &.cost { color: #DC2626; }
}

/* 骨架屏 */
.skeleton-item { cursor: default; }
.skeleton-icon {
  width: 52rpx; height: 52rpx; border-radius: 50%;
  background: #F3F4F6; flex-shrink: 0;
}
.skeleton-content { flex: 1; display: flex; flex-direction: column; gap: 10rpx; }
.skeleton-line {
  height: 24rpx; border-radius: 6rpx; background: #F3F4F6;
  &.long  { width: 70%; }
  &.short { width: 40%; }
}
</style>
