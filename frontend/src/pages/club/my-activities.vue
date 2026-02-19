<template>
  <view class="my-activities-page">

    <!-- 状态筛选 Tab -->
    <view class="filter-tabs">
      <view
        v-for="tab in statusTabs"
        :key="tab.value"
        class="filter-tab"
        :class="{ active: currentStatus === tab.value }"
        @click="switchStatus(tab.value)"
      >
        <text class="tab-text">{{ tab.label }}</text>
        <view v-if="currentStatus === tab.value" class="tab-indicator"></view>
      </view>
    </view>

    <!-- 活动列表 -->
    <scroll-view
      class="list-scroll"
      scroll-y
      @scrolltolower="loadMore"
    >
      <!-- 骨架屏 -->
      <view v-if="loading && list.length === 0" class="skeleton-list">
        <view class="activity-skeleton" v-for="i in 4" :key="i">
          <view class="sk-left">
            <view class="sk-date-block"></view>
          </view>
          <view class="sk-body">
            <view class="sk-line sk-title"></view>
            <view class="sk-line sk-meta"></view>
            <view class="sk-line sk-status"></view>
          </view>
        </view>
      </view>

      <!-- 活动列表 -->
      <view v-else-if="list.length > 0" class="list-content">
        <view
          v-for="item in list"
          :key="item.activityId"
          class="activity-item"
          @click="toDetail(item.activityId)"
        >
          <!-- 左侧日期块 -->
          <view class="date-block" :class="`date-block--${getStatusClass(item.status)}`">
            <text class="date-month">{{ getMonth(item.startTime) }}</text>
            <text class="date-day">{{ getDay(item.startTime) }}</text>
          </view>

          <!-- 右侧内容 -->
          <view class="item-body">
            <text class="item-title">{{ item.title }}</text>

            <view class="item-meta">
              <view class="meta-row">
                <Icon name="map-pin" :size="12" class="meta-icon" />
                <text class="meta-text">{{ item.location || '待定' }}</text>
              </view>
              <view class="meta-row">
                <Icon name="clock" :size="12" class="meta-icon" />
                <text class="meta-text">{{ formatTime(item.startTime) }}</text>
              </view>
            </view>

            <view class="item-footer">
              <!-- 状态标签 -->
              <view class="status-tag" :class="`status-tag--${getStatusClass(item.status)}`">
                <text>{{ getStatusText(item.status) }}</text>
              </view>
              <!-- 签到状态 -->
              <view v-if="item.isSignedIn" class="signed-badge">
                <Icon name="check" :size="11" color="#27AE60" />
                <text>已签到</text>
              </view>
              <!-- 积分奖励 -->
              <view v-if="item.rewardPoints" class="points-badge">
                <text>+{{ item.rewardPoints }}分</text>
              </view>
            </view>
          </view>
        </view>

        <!-- 加载更多 -->
        <view v-if="loading" class="loading-more">
          <view class="loading-dots">
            <view class="dot"></view>
            <view class="dot"></view>
            <view class="dot"></view>
          </view>
        </view>

        <!-- 没有更多 -->
        <view v-else-if="!hasMore" class="no-more">
          <text>已经到底了</text>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-else class="empty-state">
        <view class="empty-icon-wrap">
          <Icon name="calendar" :size="40" color="#D1D5DB" />
        </view>
        <text class="empty-title">暂无活动记录</text>
        <text class="empty-hint">报名参与活动后会出现在这里</text>
        <view class="empty-action" @click="toActivityList">
          <text>去看看活动</text>
          <Icon name="chevron-right" :size="14" color="#377DFF" />
        </view>
      </view>

    </scroll-view>

  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Icon from '@/components/icons/index.vue'
import { getMyActivities } from '@/services/club'
import { useNavigation } from '@/composables/useNavigation'

const { toActivityDetail, toActivityList } = useNavigation()

const statusTabs = [
  { label: '全部', value: -1 },
  { label: '报名中', value: 1 },
  { label: '进行中', value: 2 },
  { label: '已结束', value: 3 }
]

const currentStatus = ref(-1)
const list = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const hasMore = ref(true)

const switchStatus = (status: number) => {
  if (currentStatus.value === status) return
  currentStatus.value = status
  list.value = []
  page.value = 1
  hasMore.value = true
  loadActivities(true)
}

const loadActivities = async (isRefresh = false) => {
  if (loading.value) return
  if (!isRefresh && !hasMore.value) return

  try {
    loading.value = true
    const currentPage = isRefresh ? 1 : page.value
    const params: any = { page: currentPage, pageSize: 15 }
    if (currentStatus.value !== -1) params.status = currentStatus.value

    const res = await getMyActivities(params)

    if (isRefresh) {
      list.value = res.list
      page.value = 1
    } else {
      list.value.push(...res.list)
    }

    page.value = currentPage + 1
    hasMore.value = res.list.length >= 15
  } catch (error) {
    console.error('加载我的活动失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const loadMore = () => loadActivities(false)

const toDetail = (id: number) => toActivityDetail(id)

const getStatusText = (status: number) => {
  const map: Record<number, string> = { 1: '报名中', 2: '进行中', 3: '已结束', 4: '已取消' }
  return map[status] || '未知'
}

const getStatusClass = (status: number) => {
  const map: Record<number, string> = { 1: 'registering', 2: 'ongoing', 3: 'ended', 4: 'cancelled' }
  return map[status] || 'ended'
}

const getMonth = (dateStr: string) => {
  if (!dateStr) return '--'
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}月`
}

const getDay = (dateStr: string) => {
  if (!dateStr) return '--'
  return String(new Date(dateStr).getDate())
}

const formatTime = (dateStr: string) => {
  if (!dateStr) return '待定'
  const d = new Date(dateStr)
  const h = String(d.getHours()).padStart(2, '0')
  const m = String(d.getMinutes()).padStart(2, '0')
  return `${h}:${m}`
}

onMounted(() => {
  loadActivities(true)
})
</script>

<style scoped lang="scss">
@import '@/styles/design-tokens.scss';

.my-activities-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: $color-bg-page;
}

/* ========== 状态筛选 Tab ========== */
.filter-tabs {
  display: flex;
  align-items: center;
  height: 46px;
  background: $color-bg-card;
  border-bottom: 1px solid $color-border;
  padding: 0 16px;
  gap: 0;
  flex-shrink: 0;
}

.filter-tab {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 0 16px;
  cursor: pointer;

  .tab-text {
    font-size: 14px;
    font-weight: 500;
    color: $color-text-tertiary;
    transition: $transition-color;
  }

  &.active .tab-text {
    font-weight: 600;
    color: $campus-blue;
  }
}

.tab-indicator {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 2px;
  background: $campus-blue;
  border-radius: 2px;
}

/* ========== 列表滚动区 ========== */
.list-scroll {
  flex: 1;
}

.list-content {
  padding: 12px 16px 80px;
}

/* ========== 骨架屏 ========== */
.skeleton-list {
  padding: 12px 16px;
}

.activity-skeleton {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: $color-bg-card;
  border-radius: 12px;
  margin-bottom: 10px;
  box-shadow: $shadow-card;
}

.sk-left {
  flex-shrink: 0;
}

.sk-date-block {
  width: 48px;
  height: 56px;
  border-radius: 8px;
  background: linear-gradient(90deg, #F3F4F6 25%, #E5E7EB 50%, #F3F4F6 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.sk-body {
  flex: 1;
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
.sk-title  { height: 16px; width: 75%; }
.sk-meta   { height: 13px; width: 55%; }
.sk-status { height: 22px; width: 50px; border-radius: 11px; }

@keyframes shimmer {
  0%   { background-position: -200% 0; }
  100% { background-position:  200% 0; }
}

/* ========== 活动条目 ========== */
.activity-item {
  display: flex;
  gap: 14px;
  padding: 16px;
  background: $color-bg-card;
  border-radius: 12px;
  margin-bottom: 10px;
  box-shadow: $shadow-card;
  cursor: pointer;
  transition: $transition-all;

  &:active {
    transform: scale(0.98);
    box-shadow: $shadow-card-hover;
  }
}

/* 左侧日期块 */
.date-block {
  flex-shrink: 0;
  width: 48px;
  height: 56px;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1px;

  &--registering {
    background: rgba($campus-blue, 0.1);
    .date-month { color: $campus-blue; }
    .date-day   { color: $campus-blue; }
  }
  &--ongoing {
    background: rgba($color-success, 0.1);
    .date-month { color: $color-success; }
    .date-day   { color: $color-success; }
  }
  &--ended, &--cancelled {
    background: $color-bg-hover;
    .date-month { color: $color-text-quaternary; }
    .date-day   { color: $color-text-quaternary; }
  }
}

.date-month {
  font-size: 11px;
  font-weight: 500;
}

.date-day {
  font-size: 20px;
  font-weight: 700;
  line-height: 1.1;
}

/* 右侧内容 */
.item-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.item-title {
  font-size: 15px;
  font-weight: 600;
  color: $color-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 4px;

  .meta-icon { color: $color-text-quaternary; flex-shrink: 0; }
  .meta-text { font-size: 12px; color: $color-text-tertiary; }
}

.item-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 2px;
}

/* 状态标签 */
.status-tag {
  display: inline-flex;
  align-items: center;
  height: 20px;
  padding: 0 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;

  &--registering { color: $campus-blue;    background: rgba($campus-blue, 0.1); }
  &--ongoing     { color: $color-success;  background: rgba($color-success, 0.1); }
  &--ended       { color: $color-text-tertiary; background: $color-bg-hover; }
  &--cancelled   { color: $color-danger;   background: rgba($color-danger, 0.08); }
}

/* 签到标记 */
.signed-badge {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  height: 20px;
  padding: 0 8px;
  border-radius: 10px;
  background: rgba($color-success, 0.1);
  font-size: 11px;
  color: $color-success;
  font-weight: 600;
}

/* 积分奖励 */
.points-badge {
  display: inline-flex;
  align-items: center;
  height: 20px;
  padding: 0 8px;
  border-radius: 10px;
  background: rgba(#F59E0B, 0.1);
  font-size: 11px;
  color: #D97706;
  font-weight: 600;
}

/* ========== 加载更多 ========== */
.loading-more {
  padding: 20px;
  display: flex;
  justify-content: center;
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
  background: $campus-blue;
  opacity: 0.4;
  animation: dotPulse 1.2s ease-in-out infinite;

  &:nth-child(2) { animation-delay: 0.2s; }
  &:nth-child(3) { animation-delay: 0.4s; }
}

@keyframes dotPulse {
  0%, 80%, 100% { transform: scale(0.8); opacity: 0.4; }
  40% { transform: scale(1.1); opacity: 1; }
}

.no-more {
  padding: 20px;
  text-align: center;
  font-size: 13px;
  color: $color-text-quaternary;
}

/* ========== 空状态 ========== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
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

.empty-action {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  cursor: pointer;

  text {
    font-size: 14px;
    font-weight: 500;
    color: $campus-blue;
  }
}

/* ========== PC 端适配 ========== */
@media (min-width: 1024px) {
  .list-content {
    max-width: 720px;
    margin: 0 auto;
    padding: 20px 0 80px;
  }
}
</style>
