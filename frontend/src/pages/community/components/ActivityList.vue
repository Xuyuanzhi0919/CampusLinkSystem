<template>
  <view class="activity-list">

    <!-- 骨架屏：首次加载时显示 -->
    <view v-if="loading && list.length === 0" class="skeleton-container">
      <view class="activity-skeleton" v-for="i in 3" :key="i">
        <view class="sk-cover"></view>
        <view class="sk-body">
          <view class="sk-line sk-title"></view>
          <view class="sk-line sk-meta1"></view>
          <view class="sk-line sk-meta2"></view>
        </view>
      </view>
    </view>

    <!-- 活动列表 -->
    <view v-else-if="list.length > 0" class="activity-items">
      <view
        v-for="item in list"
        :key="item.activityId"
        class="activity-card"
        @click="handleActivityClick(item.activityId)"
      >
        <!-- 顶部类型色条（绿色 = 活动） -->
        <view class="activity-card__type-bar"></view>

        <!-- Header: 活动图标 + 状态胶囊 -->
        <view class="activity-card__header">
          <view class="activity-card__icon" :style="{ background: getTypeConfig(item.type).bgGradient }">
            <Icon :name="getTypeConfig(item.type).icon" :size="22" color="#FFFFFF" />
          </view>
          <view class="activity-card__capsule" :class="`capsule--status-${item.status || 1}`">
            <text>{{ getStatusText(item.status) }}</text>
          </view>
        </view>

        <!-- Body: 标题 + 主办方 -->
        <view class="activity-card__body">
          <text class="activity-card__title">{{ item.title }}</text>
          <view v-if="item.organizer || item.clubName" class="activity-card__organizer">
            <Icon name="users" :size="13" class="org-icon" />
            <text>{{ item.organizer || item.clubName }}</text>
          </view>
        </view>

        <!-- 时间地点 -->
        <view class="activity-card__info">
          <view class="info-item">
            <Icon name="clock" :size="13" class="info-icon" />
            <text>{{ formatDate(item.startTime) }}</text>
          </view>
          <view class="info-item">
            <Icon name="map-pin" :size="13" class="info-icon" />
            <text>{{ item.location || '待定' }}</text>
          </view>
        </view>

        <!-- Meta: 报名人数 + 活动类型标签 -->
        <view class="activity-card__meta">
          <view class="activity-card__meta-item">
            <Icon name="users" :size="13" class="meta-icon" />
            <text>{{ item.registeredCount || 0 }}/{{ item.maxParticipants || '不限' }} 人</text>
          </view>
          <view class="activity-type-tag" :class="`type-tag--${item.type || 1}`">
            <text>{{ getActivityType(item.type) }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else class="empty-container">
      <view class="empty-icon-wrap">
        <Icon name="calendar" :size="40" color="#D1D5DB" />
      </view>
      <text class="empty-title">暂无活动</text>
      <text class="empty-hint">敬请期待更多精彩活动</text>
    </view>

    <!-- 底部状态区 -->
    <view class="list-footer">
      <!-- 加载中 -->
      <view v-if="loading && list.length > 0" class="loading-more">
        <view class="loading-dots">
          <view class="dot"></view>
          <view class="dot"></view>
          <view class="dot"></view>
        </view>
        <text class="footer-text">加载中...</text>
      </view>
      <!-- 已到底部 -->
      <view v-else-if="!hasMore && list.length > 0" class="no-more">
        <view class="no-more-line"></view>
        <text class="no-more-text">已加载全部活动</text>
        <view class="no-more-line"></view>
      </view>
    </view>

  </view>
</template>

<script setup lang="ts">
import { useNavigation } from '@/composables/useNavigation'
import Icon from '@/components/icons/index.vue'

interface Props {
  list: any[]
  loading: boolean
  hasMore?: boolean
}

withDefaults(defineProps<Props>(), {
  hasMore: true
})

const { toActivityDetail } = useNavigation()

const handleActivityClick = (activityId: number) => {
  toActivityDetail(activityId)
}

// 活动类型配置（图标 + 渐变色）
const getTypeConfig = (type: number) => {
  const configs: Record<number, { icon: string; bgGradient: string }> = {
    1: { icon: 'book-open',   bgGradient: 'linear-gradient(135deg, #667EEA 0%, #764BA2 100%)' }, // 讲座 - 紫
    2: { icon: 'trophy',      bgGradient: 'linear-gradient(135deg, #F093FB 0%, #F5576C 100%)' }, // 比赛 - 红
    3: { icon: 'smile',       bgGradient: 'linear-gradient(135deg, #4FACFE 0%, #00F2FE 100%)' }, // 社交 - 青
    4: { icon: 'heart',       bgGradient: 'linear-gradient(135deg, #FA709A 0%, #FEE140 100%)' }, // 公益 - 粉
    5: { icon: 'calendar',    bgGradient: 'linear-gradient(135deg, #27AE60 0%, #2ECC71 100%)' }, // 其他 - 绿
  }
  return configs[type] || configs[5]
}

const getActivityType = (type: number) => {
  const types: Record<number, string> = { 1: '讲座', 2: '比赛', 3: '社交', 4: '公益', 5: '其他' }
  return types[type] || '活动'
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = { 1: '报名中', 2: '进行中', 3: '已结束', 4: '已取消' }
  return map[status] || '未知'
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '待定'
  const d = new Date(dateStr)
  const m = d.getMonth() + 1
  const day = d.getDate()
  const h = d.getHours().toString().padStart(2, '0')
  const min = d.getMinutes().toString().padStart(2, '0')
  return `${m}月${day}日 ${h}:${min}`
}
</script>

<style scoped lang="scss">
@import '@/styles/design-tokens.scss';

.activity-list {
  padding: 12px 16px 80px;
}

/* ========== 骨架屏 ========== */
.activity-skeleton {
  background: $color-bg-card;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 12px;
  box-shadow: $shadow-card;
}

.sk-cover {
  width: 100%;
  height: 160px;
  background: linear-gradient(90deg, #F3F4F6 25%, #E5E7EB 50%, #F3F4F6 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.sk-body {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sk-line {
  border-radius: 4px;
  background: linear-gradient(90deg, #F3F4F6 25%, #E5E7EB 50%, #F3F4F6 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}
.sk-title { height: 18px; width: 70%; }
.sk-meta1 { height: 14px; width: 50%; }
.sk-meta2 { height: 14px; width: 40%; }

@keyframes shimmer {
  0%   { background-position: -200% 0; }
  100% { background-position:  200% 0; }
}

/* ========== 活动卡片 ========== */
.activity-card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: $card-bg;
  backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid $card-border;
  border-radius: $card-radius;
  box-shadow: $card-shadow;
  padding: 20px;
  margin-bottom: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: $transition-all;

  &:hover {
    box-shadow: $card-shadow-hover;
    transform: translateY(-2px);
  }

  &:active {
    transform: scale(0.98);
  }
}

/* 顶部类型色条 - 绿色（活动） */
.activity-card__type-bar {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #27AE60 0%, #2ECC71 100%);
  border-radius: $card-radius $card-radius 0 0;
}

/* Header */
.activity-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 4px;
}

.activity-card__icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(39, 174, 96, 0.25);
  flex-shrink: 0;
}

/* 状态胶囊 */
.activity-card__capsule {
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;

  &.capsule--status-1 {
    color: $campus-blue;
    background: rgba($campus-blue, 0.1);
  }
  &.capsule--status-2 {
    color: $color-success;
    background: rgba($color-success, 0.1);
  }
  &.capsule--status-3 {
    color: $color-text-tertiary;
    background: rgba($color-text-tertiary, 0.08);
  }
  &.capsule--status-4 {
    color: $color-danger;
    background: rgba($color-danger, 0.1);
  }
}

/* Body */
.activity-card__body {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.activity-card__title {
  font-size: 16px;
  font-weight: 700;
  color: $color-text-primary;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-card__organizer {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  font-weight: 500;
  color: $color-text-secondary;

  .org-icon { color: $campus-blue; flex-shrink: 0; }
}

/* 时间地点 */
.activity-card__info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: $color-text-tertiary;

  .info-icon { color: $color-text-quaternary; flex-shrink: 0; }
}

/* Meta */
.activity-card__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 8px;
  border-top: 1px solid $color-divider;
}

.activity-card__meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: $color-text-tertiary;

  .meta-icon { color: $color-text-quaternary; flex-shrink: 0; }
}

/* 活动类型标签 */
.activity-type-tag {
  display: inline-flex;
  align-items: center;
  height: 22px;
  padding: 0 9px;
  border-radius: 11px;
  font-size: 11px;
  font-weight: 600;

  &.type-tag--1 { color: #764BA2; background: rgba(118, 75, 162, 0.1); }  // 讲座
  &.type-tag--2 { color: #F5576C; background: rgba(245, 87, 108, 0.1); }  // 比赛
  &.type-tag--3 { color: #0097A7; background: rgba(0, 151, 167, 0.1);  }  // 社交
  &.type-tag--4 { color: #E91E63; background: rgba(233, 30, 99, 0.1);  }  // 公益
  &.type-tag--5 { color: #27AE60; background: rgba(39, 174, 96, 0.1);  }  // 其他
}

/* ========== 空状态 ========== */
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 72px 20px;
  gap: 8px;
}

.empty-icon-wrap {
  width: 80px;
  height: 80px;
  background: $color-bg-hover;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
}

.empty-title {
  font-size: 15px;
  font-weight: 600;
  color: $color-text-secondary;
}

.empty-hint {
  font-size: 13px;
  color: $color-text-quaternary;
}

/* ========== 底部状态区 ========== */
.list-footer {
  padding: 8px 0 16px;
}

/* 加载中 */
.loading-more {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 12px 0;
}

.loading-dots {
  display: flex;
  gap: 6px;
  align-items: center;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #27AE60;
  opacity: 0.4;
  animation: dotPulse 1.2s ease-in-out infinite;

  &:nth-child(2) { animation-delay: 0.2s; }
  &:nth-child(3) { animation-delay: 0.4s; }
}

@keyframes dotPulse {
  0%, 80%, 100% { transform: scale(0.8); opacity: 0.4; }
  40% { transform: scale(1.1); opacity: 1; }
}

.footer-text {
  font-size: 12px;
  color: $color-text-quaternary;
}

/* 已到底部 */
.no-more {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
}

.no-more-line {
  flex: 1;
  height: 1px;
  background: $color-divider;
}

.no-more-text {
  font-size: 12px;
  color: $color-text-quaternary;
  white-space: nowrap;
}

/* ========== PC 端双列布局 ========== */
@media (min-width: 1024px) {
  .activity-list {
    padding: 20px 80px 80px;
  }

  .activity-items {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  .activity-card {
    margin-bottom: 0;
  }
}
</style>
