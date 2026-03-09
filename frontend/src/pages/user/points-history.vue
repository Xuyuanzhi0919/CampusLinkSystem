<template>
  <view class="page">

    <!-- ── 顶部导航栏 ── -->
    <CNavBar title="积分明细" />

    <!-- ── 内容居中容器 ── -->
    <view class="page-body">

    <!-- ── 积分卡区域 ── -->
    <view class="banner-section">
      <view class="banner-card">
        <!-- 顶部：标题 + 商城入口 -->
        <view class="banner-header">
          <view class="banner-title-group">
            <Icon name="zap" :size="14" color="#2563EB" />
            <text class="banner-title">我的积分</text>
          </view>
          <view class="banner-mall-btn" @click="uni.navigateTo({ url: '/pages/user/points-mall' })">
            <text class="banner-mall-text">积分商城</text>
            <Icon name="arrow-right" :size="12" color="#2563EB" />
          </view>
        </view>

        <!-- 主数值 -->
        <view class="banner-main">
          <text class="banner-value">{{ (userStore.userInfo?.points ?? 0).toLocaleString() }}</text>
          <text class="banner-unit">分</text>
        </view>
        <text class="banner-desc">可用于下载资源、发布悬赏任务</text>

        <!-- 积分获取提示 -->
        <view class="banner-tips">
          <view class="banner-tip">
            <Icon name="calendar-check" :size="11" color="#16A34A" />
            <text class="banner-tip-text">签到 +10</text>
          </view>
          <view class="banner-tip">
            <Icon name="file-plus" :size="11" color="#2563EB" />
            <text class="banner-tip-text">上传 +10</text>
          </view>
          <view class="banner-tip">
            <Icon name="badge-check" :size="11" color="#D97706" />
            <text class="banner-tip-text">采纳 +20</text>
          </view>
        </view>
      </view>
    </view>

    <!-- ── 筛选栏 ── -->
    <view class="filter-bar">
      <view
        v-for="f in FILTERS"
        :key="f.value"
        class="filter-tab"
        :class="{ 'filter-tab--active': activeFilter === f.value }"
        @click="setFilter(f.value)"
      >
        <Icon :name="f.icon" :size="13" class="filter-tab-icon" />
        <text class="filter-tab-text">{{ f.label }}</text>
      </view>
    </view>

    <!-- ── 列表滚动区 ── -->
    <scroll-view
      class="content-scroll"
      scroll-y
      @scrolltolower="handleLoadMore"
    >
      <!-- 骨架屏（首次加载） -->
      <view v-if="loading && pointsList.length === 0" class="list">
        <view v-for="i in 8" :key="i" class="record-card record-card--skeleton">
          <view class="skeleton-icon skeleton-shine" />
          <view class="skeleton-body">
            <view class="skeleton-line skeleton-line--long skeleton-shine" />
            <view class="skeleton-line skeleton-line--short skeleton-shine" />
          </view>
          <view class="skeleton-amount skeleton-shine" />
        </view>
      </view>

      <!-- 按日期分组的明细列表 -->
      <view v-else-if="groupedList.length > 0" class="list">
        <template v-for="group in groupedList" :key="group.dateLabel">
          <view class="date-divider">
            <text class="date-divider-text">{{ group.dateLabel }}</text>
          </view>
          <view
            v-for="item in group.items"
            :key="item.logId"
            class="record-card"
            @click="handleCardClick(item)"
          >
            <view class="record-icon" :style="{ background: getReasonStyle(item.reason).bg }">
              <Icon :name="getReasonIcon(item.reason)" :size="18" :color="getReasonStyle(item.reason).color" />
            </view>
            <view class="record-body">
              <text class="record-reason">{{ item.reason }}</text>
              <text class="record-meta">余额 {{ item.pointsAfter }} · {{ formatTime(item.createdAt) }}</text>
            </view>
            <text
              class="record-amount"
              :class="item.pointsChange > 0 ? 'record-amount--plus' : 'record-amount--minus'"
            >
              {{ item.pointsChange > 0 ? '+' : '' }}{{ item.pointsChange }}
            </text>
          </view>
        </template>

        <!-- 底部状态 -->
        <view class="footer-tip">
          <template v-if="loadingMore">
            <view class="footer-loading">
              <Icon name="loader" :size="14" class="footer-spinner" color="#94A3B8" />
              <text class="footer-text">加载中…</text>
            </view>
          </template>
          <template v-else-if="!hasMore">
            <view class="footer-end">
              <view class="footer-line" />
              <text class="footer-text">已显示全部记录</text>
              <view class="footer-line" />
            </view>
          </template>
        </view>
      </view>

      <!-- 筛选无结果（有数据但当前分类为空） -->
      <view v-else-if="!loading && pointsList.length > 0" class="empty-state">
        <view class="empty-icon-wrap">
          <Icon name="filter-x" :size="28" color="#94A3B8" />
        </view>
        <text class="empty-text">暂无此类记录</text>
        <text class="empty-tip">切换其他分类试试</text>
      </view>

      <!-- 完全空（无任何积分数据） -->
      <view v-else-if="!loading" class="empty-state">
        <view class="empty-icon-wrap">
          <Icon name="award" :size="28" color="#94A3B8" />
        </view>
        <text class="empty-text">暂无积分记录</text>
        <text class="empty-tip">完成任务、上传资源等可获得积分</text>
      </view>

    </scroll-view>

    </view><!-- /page-body -->

  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import Icon from '@/components/icons/index.vue'
import { CNavBar } from '@/components/layout'
import { getPointsLog } from '@/services/user'
import type { PointsLogItem } from '@/types/user'
import { useUserStore } from '@/stores/user'

// ── 筛选分类 ──────────────────────────────────
const FILTERS = [
  { label: '全部', value: 'all',     icon: 'layout-list' },
  { label: '签到', value: 'checkin', icon: 'calendar-check' },
  { label: '任务', value: 'task',    icon: 'target' },
  { label: '消费', value: 'consume', icon: 'shopping-cart' },
] as const

type FilterValue = typeof FILTERS[number]['value']

const TASK_REASONS = new Set(['发布任务', '完成任务', '接受任务', '回答问题', '回答被采纳'])

// ── 模块级常量 ────────────────────────────────
const REASON_ICON_MAP: Record<string, string> = {
  注册奖励:   'gift',
  上传资源:   'file-plus',
  下载资源:   'download',
  提问:       'help-circle',
  回答问题:   'message-circle',
  回答被采纳: 'badge-check',
  发布任务:   'file-text',
  完成任务:   'target',
  接受任务:   'thumbs-up',
  活动签到:   'calendar-check',
  每日签到:   'calendar',
}

const REASON_STYLE_MAP: Record<string, { bg: string; color: string }> = {
  注册奖励:   { bg: '#FEF3C7', color: '#D97706' },
  上传资源:   { bg: '#EFF6FF', color: '#2563EB' },
  下载资源:   { bg: '#F0FDF4', color: '#16A34A' },
  提问:       { bg: '#FFF7ED', color: '#EA580C' },
  回答问题:   { bg: '#EFF6FF', color: '#3B82F6' },
  回答被采纳: { bg: '#ECFDF5', color: '#059669' },
  发布任务:   { bg: '#F5F3FF', color: '#7C3AED' },
  完成任务:   { bg: '#FFF1F2', color: '#E11D48' },
  接受任务:   { bg: '#FFFBEB', color: '#D97706' },
  活动签到:   { bg: '#DCFCE7', color: '#16A34A' },
  每日签到:   { bg: '#DCFCE7', color: '#16A34A' },
}

const getReasonIcon = (reason: string) =>
  reason.startsWith('积分兑换') ? 'zap' : (REASON_ICON_MAP[reason] ?? 'zap')

const getReasonStyle = (reason: string) =>
  reason.startsWith('积分兑换')
    ? { bg: '#FEE2E2', color: '#F43F5E' }
    : (REASON_STYLE_MAP[reason] ?? { bg: '#F3F4F6', color: '#6B7280' })

const formatTime = (dateStr: string): string => {
  const d = new Date(dateStr)
  const now = new Date()
  const hm = `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  if (d.getFullYear() === now.getFullYear())
    return `${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${hm}`
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

// ── 响应式状态 ────────────────────────────────
const userStore    = useUserStore()
const pointsList   = ref<PointsLogItem[]>([])
const loading      = ref(false)
const loadingMore  = ref(false)
const hasMore      = ref(true)
const page         = ref(1)
const PAGE_SIZE    = 20
const activeFilter = ref<FilterValue>('all')

// ── 筛选 ──────────────────────────────────────
const filteredList = computed(() => {
  const list = pointsList.value
  switch (activeFilter.value) {
    case 'checkin': return list.filter(i => i.reason.includes('签到'))
    case 'task':    return list.filter(i => TASK_REASONS.has(i.reason))
    case 'consume': return list.filter(i => i.pointsChange < 0)
    default:        return list
  }
})

// ── 日期分组 ──────────────────────────────────
interface DateGroup { dateLabel: string; items: PointsLogItem[] }

const groupedList = computed<DateGroup[]>(() => {
  const now      = new Date()
  const todayStart = new Date(now); todayStart.setHours(0, 0, 0, 0)
  const yestStart  = new Date(todayStart); yestStart.setDate(todayStart.getDate() - 1)

  const groups: DateGroup[] = []
  const seen: Record<string, DateGroup> = {}

  for (const item of filteredList.value) {
    const d = new Date(item.createdAt)
    let label: string
    if (d >= todayStart)      label = '今天'
    else if (d >= yestStart)  label = '昨天'
    else {
      const m = d.getMonth() + 1
      const day = d.getDate()
      label = d.getFullYear() === now.getFullYear()
        ? `${m}月${day}日`
        : `${d.getFullYear()}年${m}月${day}日`
    }
    if (!seen[label]) {
      seen[label] = { dateLabel: label, items: [] }
      groups.push(seen[label])
    }
    seen[label].items.push(item)
  }
  return groups
})

const setFilter = (val: FilterValue) => { activeFilter.value = val }

// ── 交互 ──────────────────────────────────────
const handleCardClick = (item: PointsLogItem) => {
  if (!item.relatedType || !item.relatedId) return
  const routeMap: Record<string, string> = {
    resource: '/pages/resource/detail',
    question: '/pages/question/detail',
    task:     '/pages/task/detail',
  }
  const url = routeMap[item.relatedType]
  if (url) uni.navigateTo({ url: `${url}?id=${item.relatedId}` })
}

// ── 数据加载 ──────────────────────────────────
const loadPoints = async (isMore = false) => {
  if (!isMore) {
    loading.value = true
    page.value    = 1
    hasMore.value = true
  } else {
    if (!hasMore.value || loadingMore.value) return
    loadingMore.value = true
  }
  try {
    const result = await getPointsLog(page.value, PAGE_SIZE)
    pointsList.value = isMore
      ? [...pointsList.value, ...result.list]
      : result.list
    hasMore.value = page.value < result.totalPages
    page.value++
  } catch (e: any) {
    uni.showToast({ title: e.message || '加载失败', icon: 'none' })
  } finally {
    loading.value     = false
    loadingMore.value = false
  }
}

const handleLoadMore = () => loadPoints(true)

onMounted(() => loadPoints())
</script>

<style lang="scss" scoped>

// ── 页面容器 ──────────────────────────────────
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #F1F5F9;
  overflow: hidden;
}

.page-body {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  max-width: 680px;
  margin: 0 auto;
  width: 100%;
}

// ── 积分卡区域 ────────────────────────────────
.banner-section {
  flex-shrink: 0;
  padding: 12px 16px;
}

.banner-card {
  background: #FFFFFF;
  border-radius: 20px;
  padding: 16px 18px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid #EEF2FF;
}

.banner-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.banner-title-group {
  display: flex;
  align-items: center;
  gap: 5px;
}

.banner-title {
  font-size: 13px;
  color: #64748B;
  font-weight: 500;
}

.banner-mall-btn {
  display: flex;
  align-items: center;
  gap: 3px;
  padding: 5px 12px;
  background: #EFF6FF;
  border-radius: 20px;
  cursor: pointer;
  transition: background 0.15s;

  &:active { background: #DBEAFE; }

  // #ifdef H5
  &:hover { background: #DBEAFE; }
  // #endif
}

.banner-mall-text {
  font-size: 12px;
  font-weight: 500;
  color: #2563EB;
}

.banner-main {
  display: flex;
  align-items: baseline;
  gap: 5px;
  margin-bottom: 4px;
}

.banner-value {
  font-size: 48px;
  font-weight: 800;
  color: #2563EB;
  line-height: 1;
  letter-spacing: -2px;
}

.banner-unit {
  font-size: 18px;
  font-weight: 600;
  color: #93C5FD;
}

.banner-desc {
  font-size: 12px;
  color: #94A3B8;
  margin-bottom: 14px;
}

.banner-tips {
  display: flex;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #F1F5F9;
}

.banner-tip {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: #F8FAFC;
  border-radius: 12px;
  border: 1px solid #E2E8F0;
}

.banner-tip-text {
  font-size: 11px;
  color: #64748B;
  font-weight: 500;
}

// ── 筛选栏 ────────────────────────────────────
.filter-bar {
  flex-shrink: 0;
  display: flex;
  gap: 8px;
  padding: 12px 16px 8px;
  background: #F1F5F9;
}

.filter-tab {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 6px 14px;
  border-radius: 20px;
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  cursor: pointer;
  transition: all 0.15s;

  &:active { opacity: 0.75; }

  // #ifdef H5
  &:hover:not(.filter-tab--active) { border-color: #CBD5E1; }
  // #endif

  &--active {
    background: #2563EB;
    border-color: #2563EB;
    box-shadow: 0 2px 8px rgba(37, 99, 235, 0.28);

    .filter-tab-icon { color: #FFFFFF; }
    .filter-tab-text { color: #FFFFFF; }
  }
}

.filter-tab-icon { color: #94A3B8; }

.filter-tab-text {
  font-size: 13px;
  font-weight: 500;
  color: #64748B;
  white-space: nowrap;
}

// ── 列表区 ────────────────────────────────────
.content-scroll {
  flex: 1;
  height: 0;
  overflow-y: auto;
}

.list {
  display: flex;
  flex-direction: column;
  padding: 8px 16px 16px;
}

// ── 日期分隔 ──────────────────────────────────
.date-divider {
  padding: 12px 4px 6px;
  display: flex;
  align-items: center;
  gap: 8px;

  &:first-child { padding-top: 4px; }
}

.date-divider-text {
  font-size: 12px;
  font-weight: 600;
  color: #94A3B8;
}

// ── 记录卡片 ──────────────────────────────────
.record-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 13px 14px;
  background: #FFFFFF;
  border-radius: 14px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  margin-bottom: 8px;
  transition: box-shadow 0.15s;
  cursor: pointer;

  &:active { background: #F8FAFF; }

  // #ifdef H5
  &:hover { box-shadow: 0 3px 10px rgba(0, 0, 0, 0.09); }
  // #endif

  &--skeleton {
    min-height: 64px;
    box-shadow: none;
    cursor: default;
    &:active { background: #FFFFFF; }
  }
}

.record-icon {
  width: 42px;
  height: 42px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}

.record-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.record-reason {
  font-size: 14px;
  font-weight: 600;
  color: #1E293B;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.record-meta {
  font-size: 11px;
  color: #94A3B8;
}

.record-amount {
  flex-shrink: 0;
  font-size: 17px;
  font-weight: 800;
  letter-spacing: -0.5px;
  &--plus  { color: #10B981; }
  &--minus { color: #EF4444; }
}

// ── 骨架屏 ────────────────────────────────────
.skeleton-icon   { width: 42px; height: 42px; flex-shrink: 0; border-radius: 12px; }
.skeleton-body   { flex: 1; display: flex; flex-direction: column; gap: 8px; }
.skeleton-amount { width: 44px; height: 20px; flex-shrink: 0; border-radius: 6px; }

.skeleton-line {
  height: 12px;
  border-radius: 6px;
  &--long  { width: 55%; }
  &--short { width: 38%; }
}

.skeleton-shine {
  background: linear-gradient(90deg, #F1F5F9 25%, #E2E8F0 50%, #F1F5F9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}

@keyframes shimmer {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

// ── 底部状态 ──────────────────────────────────
.footer-tip { padding: 8px 0 16px; }

.footer-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.footer-spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}

.footer-end {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 4px;
}

.footer-line {
  flex: 1;
  height: 1px;
  background: #E2E8F0;
}

.footer-text {
  font-size: 12px;
  color: #CBD5E1;
  white-space: nowrap;
}

// ── 空状态 ────────────────────────────────────
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 72px 20px;
}

.empty-icon-wrap {
  width: 64px;
  height: 64px;
  border-radius: 20px;
  background: #F1F5F9;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
}

.empty-text { font-size: 14px; font-weight: 500; color: #94A3B8; }
.empty-tip  { font-size: 12px; color: #CBD5E1; }

</style>
