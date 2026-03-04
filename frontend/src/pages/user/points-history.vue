<template>
  <view class="page">

    <!-- ── 统一头部（导航 + 积分卡共享渐变背景）── -->
    <view class="page-header">

      <!-- 导航行 -->
      <view class="header-nav">
        <view class="nav-back" @click="goBack">
          <Icon name="arrow-left" :size="20" color="#FFFFFF" />
        </view>
        <text class="nav-title">积分明细</text>
        <view class="nav-placeholder" />
      </view>

      <!-- 积分卡 -->
      <view class="banner-wrap">
        <view class="banner-card">
          <view class="banner-glow" />
          <view class="banner-star">
            <Icon name="star" :size="36" color="#FBBF24" />
          </view>
          <view class="banner-info">
            <text class="banner-value">{{ (userStore.userInfo?.points ?? 0).toLocaleString() }}</text>
            <text class="banner-desc">可用于下载资源、发布悬赏</text>
          </view>
          <view class="banner-mall-btn" @click="uni.navigateTo({ url: '/pages/user/points-mall' })">
            <text class="banner-mall-text">去商城</text>
            <Icon name="chevron-right" :size="13" color="#FFFFFF" />
          </view>
        </view>
      </view>

    </view>

    <!-- ── 列表滚动区 ── -->
    <scroll-view
      class="content-scroll"
      scroll-y
      @scrolltolower="handleLoadMore"
    >
      <!-- 骨架屏 -->
      <view v-if="loading && pointsList.length === 0" class="list">
        <view v-for="i in 8" :key="i" class="record-card record-card--skeleton">
          <view class="skeleton-icon" />
          <view class="skeleton-body">
            <view class="skeleton-line skeleton-line--long" />
            <view class="skeleton-line skeleton-line--short" />
          </view>
          <view class="skeleton-amount" />
        </view>
      </view>

      <!-- 记录列表 -->
      <view v-else-if="pointsList.length > 0" class="list">
        <view
          v-for="item in pointsList"
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
          <text class="record-amount" :class="item.pointsChange > 0 ? 'record-amount--plus' : 'record-amount--minus'">
            {{ item.pointsChange > 0 ? '+' : '' }}{{ item.pointsChange }}
          </text>
        </view>
        <view class="footer-tip">
          <text class="footer-text">{{ loadingMore ? '加载中…' : hasMore ? '上拉加载更多' : '已显示全部记录' }}</text>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-else class="empty-state">
        <Icon name="award" :size="44" color="#D1D5DB" />
        <text class="empty-text">暂无积分记录</text>
        <text class="empty-tip">完成任务、上传资源等可获得积分</text>
      </view>
    </scroll-view>

  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Icon from '@/components/icons/index.vue'
import { getPointsLog } from '@/services/user'
import type { PointsLogItem } from '@/types/user'
import { useUserStore } from '@/stores/user'

// ── 模块级常量，不随组件重建 ──────────────────
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
  const diff = now.getTime() - d.getTime()
  const hm = `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  if (diff < 86400000)  return `今天 ${hm}`
  if (diff < 172800000) return `昨天 ${hm}`
  if (d.getFullYear() === now.getFullYear())
    return `${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${hm}`
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

// ── 响应式状态 ────────────────────────────────
const userStore = useUserStore()
const pointsList   = ref<PointsLogItem[]>([])
const loading      = ref(false)
const loadingMore  = ref(false)
const hasMore      = ref(true)
const page         = ref(1)
const PAGE_SIZE    = 20

// ── 交互 ──────────────────────────────────────
const goBack = () =>
  uni.navigateBack({ fail: () => uni.switchTab({ url: '/pages/home/index' }) })

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

// ── 统一头部 ──────────────────────────────────
.page-header {
  flex-shrink: 0;
  background: linear-gradient(160deg, #1E3A8A 0%, #2563EB 55%, #3B82F6 100%);
  border-radius: 0 0 24px 24px;
}

.header-nav {
  display: flex;
  align-items: center;
  height: 56px;
  padding: 0 16px 0 12px;
}

.nav-back {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  cursor: pointer;
  &:active { opacity: 0.6; }
}

.nav-title {
  flex: 1;
  text-align: center;
  font-size: 17px;
  font-weight: 700;
  color: #FFFFFF;
}

.nav-placeholder { width: 36px; flex-shrink: 0; }

// ── 积分卡 ────────────────────────────────────
.banner-wrap {
  padding: 0 16px 16px;
}

// ── 积分卡 ────────────────────────────────────
.banner-card {
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(29, 78, 216, 0.18), 0 2px 8px rgba(0, 0, 0, 0.06);
}

.banner-glow {
  position: absolute;
  top: -20px;
  right: -20px;
  width: 100px;
  height: 100px;
  background: rgba(96, 165, 250, 0.22);
  border-radius: 50%;
  filter: blur(28px);
  pointer-events: none;
}

.banner-star {
  width: 56px;
  height: 56px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FEF3C7, #FDE68A);
  border: 1.5px solid rgba(251, 191, 36, 0.4);
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(251, 191, 36, 0.28);
}

.banner-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.banner-value {
  font-size: 32px;
  font-weight: 800;
  color: #1E293B;
  letter-spacing: -1px;
  line-height: 1;
}

.banner-desc {
  font-size: 11px;
  color: #94A3B8;
}

.banner-mall-btn {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 9px 15px;
  background: #2563EB;
  border-radius: 22px;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.35);
  cursor: pointer;
  &:active { opacity: 0.82; }
}

.banner-mall-text {
  font-size: 13px;
  font-weight: 600;
  color: #FFFFFF;
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
  gap: 8px;
  padding: 12px 16px;
}

// ── 记录卡片 ──────────────────────────────────
.record-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: #FFFFFF;
  border-radius: 14px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06);
  &:active { background: #F8FAFF; }

  &--skeleton {
    min-height: 60px;
    animation: skeleton-pulse 1.5s ease-in-out infinite;
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
  &--plus  { color: #10B981; }
  &--minus { color: #EF4444; }
}

// ── 骨架屏 ────────────────────────────────────
.skeleton-icon   { width: 42px; height: 42px; flex-shrink: 0; border-radius: 12px; background: #E5E7EB; }
.skeleton-body   { flex: 1; display: flex; flex-direction: column; gap: 8px; }
.skeleton-amount { width: 40px; height: 18px; flex-shrink: 0; border-radius: 6px; background: #E5E7EB; }

.skeleton-line {
  height: 11px;
  border-radius: 6px;
  background: #E5E7EB;
  &--long  { width: 60%; }
  &--short { width: 40%; }
}

@keyframes skeleton-pulse {
  0%, 100% { opacity: 1; }
  50%       { opacity: 0.5; }
}

// ── 底部 & 空状态 ─────────────────────────────
.footer-tip  { display: flex; justify-content: center; padding: 14px; }
.footer-text { font-size: 12px; color: #CBD5E1; }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 80px 20px;
}

.empty-text { font-size: 14px; font-weight: 500; color: #9CA3AF; }
.empty-tip  { font-size: 12px; color: #D1D5DB; }

</style>
