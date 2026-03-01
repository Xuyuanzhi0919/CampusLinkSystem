<template>
  <view class="page">
    <!-- 顶部导航 -->
    <CNavBar title="我的活动" />

    <!-- 状态筛选 Tab -->
    <view class="filter-tabs">
      <view
        v-for="tab in statusTabs"
        :key="tab.value"
        class="filter-tab"
        :class="{ 'filter-tab--active': currentStatus === tab.value }"
        @click="switchStatus(tab.value)"
      >
        <text class="tab-text">{{ tab.label }}</text>
        <view v-if="currentStatus === tab.value" class="tab-indicator" />
      </view>
    </view>

    <!-- 活动列表 -->
    <scroll-view
      class="list-scroll"
      scroll-y
      @scrolltolower="loadMore"
    >
      <!-- 骨架屏 -->
      <view v-if="loading && list.length === 0" class="list-inner">
        <view v-for="i in 5" :key="i" class="card card--skeleton">
          <view class="sk-date" />
          <view class="sk-body">
            <view class="sk-line sk-line--long" />
            <view class="sk-line sk-line--short" />
            <view class="sk-tag" />
          </view>
        </view>
      </view>

      <!-- 有数据 -->
      <view v-else-if="list.length > 0" class="list-inner">
        <view
          v-for="item in list"
          :key="item.activityId"
          class="card"
          :class="`card--${getStatusClass(item.status)}`"
          @click="toDetail(item.activityId)"
        >
          <!-- 左侧日期块 -->
          <view class="date-block" :class="`date-block--${getStatusClass(item.status)}`">
            <text class="date-month">{{ getMonth(item.startTime) }}</text>
            <text class="date-day">{{ getDay(item.startTime) }}</text>
          </view>

          <!-- 右侧内容 -->
          <view class="card-body">
            <text class="card-title">{{ item.title }}</text>

            <!-- 位置 + 时间合并一行 -->
            <view class="card-meta">
              <Icon name="map-pin" :size="11" color="#9CA3AF" />
              <text class="meta-text">{{ item.location || '待定' }}</text>
              <text class="meta-sep">·</text>
              <Icon name="clock" :size="11" color="#9CA3AF" />
              <text class="meta-text">{{ formatTime(item.startTime) }}</text>
            </view>

            <!-- 底部标签行 -->
            <view class="card-tags">
              <view class="tag" :class="`tag--${getStatusClass(item.status)}`">
                <text>{{ getStatusText(item.status) }}</text>
              </view>
              <view v-if="item.isSignedIn" class="tag tag--signed">
                <Icon name="check" :size="10" color="#059669" />
                <text>已签到</text>
              </view>
              <view v-if="item.rewardPoints" class="tag tag--points">
                <text>+{{ item.rewardPoints }}分</text>
              </view>
            </view>
          </view>

          <!-- 右侧箭头 -->
          <Icon name="chevron-right" :size="16" color="#D1D5DB" />
        </view>

        <!-- 底部提示 -->
        <view class="footer-tip">
          <view v-if="loading" class="loading-dots">
            <view v-for="i in 3" :key="i" class="dot" />
          </view>
          <text v-else class="footer-text">{{ hasMore ? '上拉加载更多' : '已显示全部' }}</text>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-else class="empty-state">
        <Icon name="calendar" :size="40" color="#D1D5DB" />
        <text class="empty-title">暂无活动记录</text>
        <text class="empty-hint">报名参与活动后会出现在这里</text>
        <view class="empty-action" @click="toActivityList">
          <text class="empty-action-text">去看看活动</text>
          <Icon name="chevron-right" :size="13" color="#377DFF" />
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { CNavBar } from '@/components/layout'
import Icon from '@/components/icons/index.vue'
import { getMyActivities } from '@/services/club'
import { useNavigation } from '@/composables/useNavigation'

const { toActivityDetail, toActivityList } = useNavigation()

const statusTabs = [
  { label: '全部',   value: -1 },
  { label: '报名中', value: 1 },
  { label: '进行中', value: 2 },
  { label: '已结束', value: 3 },
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
  loading.value = true
  try {
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
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

const loadMore = () => loadActivities(false)
const toDetail = (id: number) => toActivityDetail(id)

const getStatusText = (status: number) =>
  ({ 1: '报名中', 2: '进行中', 3: '已结束', 4: '已取消' }[status] ?? '未知')

const getStatusClass = (status: number) =>
  ({ 1: 'registering', 2: 'ongoing', 3: 'ended', 4: 'cancelled' }[status] ?? 'ended')

const getMonth = (dateStr: string) => dateStr ? `${new Date(dateStr).getMonth() + 1}月` : '--'
const getDay   = (dateStr: string) => dateStr ? String(new Date(dateStr).getDate()) : '--'

const formatTime = (dateStr: string) => {
  if (!dateStr) return '待定'
  const d = new Date(dateStr)
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

onMounted(() => loadActivities(true))
</script>

<style scoped lang="scss">
@import '@/styles/design-tokens.scss';

.page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: $color-bg-page;
  overflow: hidden;
}

/* ── 筛选 Tab ──────────────────────────────── */
.filter-tabs {
  display: flex;
  align-items: center;
  height: 44px;
  background: $color-bg-card;
  border-bottom: 1px solid $color-border;
  padding: 0 4px;
  flex-shrink: 0;
}

.filter-tab {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 0 18px;
  cursor: pointer;

  &--active .tab-text {
    font-weight: 700;
    color: $campus-blue;
  }
}

.tab-text {
  font-size: 14px;
  font-weight: 500;
  color: $color-text-tertiary;
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

/* ── 列表滚动区 ──────────────────────────────  */
.list-scroll {
  flex: 1;
}

.list-inner {
  padding: 12px 16px 24px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* ── 活动卡片 ──────────────────────────────── */
.card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 12px 14px 14px;
  background: $color-bg-card;
  border-radius: 14px;
  box-shadow: $shadow-card;
  cursor: pointer;

  &:active { opacity: 0.75; }

  &--skeleton {
    cursor: default;
    &:active { opacity: 1; }
  }
}

/* 左侧日期块 */
.date-block {
  flex-shrink: 0;
  width: 44px;
  height: 52px;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1px;

  &--registering {
    background: rgba($campus-blue, 0.1);
    .date-month, .date-day { color: $campus-blue; }
  }
  &--ongoing {
    background: rgba($color-success, 0.1);
    .date-month, .date-day { color: $color-success; }
  }
  &--ended, &--cancelled {
    background: $color-bg-hover;
    .date-month, .date-day { color: $color-text-quaternary; }
  }
}

.date-month {
  font-size: 10px;
  font-weight: 500;
}

.date-day {
  font-size: 19px;
  font-weight: 700;
  line-height: 1.1;
}

/* 右侧内容 */
.card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5px;
  min-width: 0;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: $color-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 位置 + 时间一行 */
.card-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: nowrap;
}

.meta-text {
  font-size: 12px;
  color: $color-text-tertiary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 80px;
}

.meta-sep {
  font-size: 11px;
  color: $color-text-quaternary;
}

/* 标签行 */
.card-tags {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  height: 18px;
  padding: 0 7px;
  border-radius: 9px;
  font-size: 11px;
  font-weight: 600;

  &--registering { color: $campus-blue;         background: rgba($campus-blue, 0.1); }
  &--ongoing     { color: $color-success;        background: rgba($color-success, 0.1); }
  &--ended       { color: $color-text-tertiary;  background: $color-bg-hover; }
  &--cancelled   { color: $color-danger;         background: rgba($color-danger, 0.08); }
  &--signed      { color: #059669;               background: rgba(#059669, 0.1); }
  &--points      { color: #D97706;               background: rgba(#F59E0B, 0.1); }
}

/* ── 骨架屏 ────────────────────────────────── */
.sk-date {
  width: 44px;
  height: 52px;
  border-radius: 10px;
  background: #EBEBEB;
  flex-shrink: 0;
  animation: shimmer 1.5s ease-in-out infinite;
}

.sk-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sk-line {
  height: 12px;
  border-radius: 6px;
  background: #EBEBEB;
  animation: shimmer 1.5s ease-in-out infinite;
  &--long  { width: 70%; }
  &--short { width: 50%; }
}

.sk-tag {
  width: 48px;
  height: 18px;
  border-radius: 9px;
  background: #EBEBEB;
  animation: shimmer 1.5s ease-in-out infinite;
}

@keyframes shimmer {
  0%, 100% { opacity: 1; }
  50%       { opacity: 0.45; }
}

/* ── 底部提示 ──────────────────────────────── */
.footer-tip {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 12px;
}

.footer-text {
  font-size: 12px;
  color: $color-text-quaternary;
}

.loading-dots {
  display: flex;
  gap: 5px;
  align-items: center;
}

.dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: $campus-blue;
  opacity: 0.4;
  animation: dotPulse 1.2s ease-in-out infinite;

  &:nth-child(2) { animation-delay: 0.2s; }
  &:nth-child(3) { animation-delay: 0.4s; }
}

@keyframes dotPulse {
  0%, 80%, 100% { transform: scale(0.8); opacity: 0.4; }
  40%           { transform: scale(1.1); opacity: 1; }
}

/* ── 空状态 ────────────────────────────────── */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  gap: 8px;
}

.empty-title {
  font-size: 14px;
  font-weight: 600;
  color: $color-text-secondary;
  margin-top: 4px;
}

.empty-hint {
  font-size: 12px;
  color: $color-text-quaternary;
}

.empty-action {
  display: flex;
  align-items: center;
  gap: 3px;
  margin-top: 6px;
  cursor: pointer;
}

.empty-action-text {
  font-size: 13px;
  font-weight: 500;
  color: $campus-blue;
}
</style>
