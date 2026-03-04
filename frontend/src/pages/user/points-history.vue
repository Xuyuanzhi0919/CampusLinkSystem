<template>
  <view class="page">
    <!-- 顶部导航 -->
    <CNavBar title="积分明细" />

    <!-- 积分概览 Banner -->
    <view class="banner" :class="{ 'banner--hidden': bannerHidden }">
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

    <!-- 记录列表 -->
    <scroll-view
      class="content-scroll"
      scroll-y
      @scroll="handleScroll"
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
            <Icon
              :name="getReasonIcon(item.reason)"
              :size="18"
              :color="getReasonStyle(item.reason).color"
            />
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
import { CNavBar } from '@/components/layout'
import Icon from '@/components/icons/index.vue'
import { getPointsLog } from '@/services/user'
import type { PointsLogItem } from '@/types/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const bannerHidden = ref(false)
const handleScroll = (e: any) => {
  bannerHidden.value = e.detail.scrollTop > 50
}

const pointsList = ref<PointsLogItem[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const PAGE_SIZE = 20

const loadPoints = async (isMore = false) => {
  if (!isMore) {
    loading.value = true
    page.value = 1
    hasMore.value = true
  } else {
    if (!hasMore.value || loadingMore.value) return
    loadingMore.value = true
  }
  try {
    const result = await getPointsLog(page.value, PAGE_SIZE)
    if (isMore) {
      pointsList.value = [...pointsList.value, ...result.list]
    } else {
      pointsList.value = result.list
    }
    hasMore.value = page.value < result.totalPages
    page.value++
  } catch (e: any) {
    uni.showToast({ title: e.message || '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const handleLoadMore = () => loadPoints(true)

const handleCardClick = (item: PointsLogItem) => {
  if (!item.relatedType || !item.relatedId) return
  const routeMap: Record<string, string> = {
    resource: '/pages/resource/detail',
    question: '/pages/question/detail',
    task: '/pages/task/detail',
  }
  const url = routeMap[item.relatedType]
  if (url) uni.navigateTo({ url: `${url}?id=${item.relatedId}` })
}

const getReasonIcon = (reason: string): string => {
  if (reason.startsWith('积分兑换')) return 'zap'
  const map: Record<string, string> = {
    注册奖励: 'gift',
    上传资源: 'file-plus',
    下载资源: 'download',
    提问: 'help-circle',
    回答问题: 'message-circle',
    回答被采纳: 'badge-check',
    发布任务: 'file-text',
    完成任务: 'target',
    接受任务: 'thumbs-up',
    活动签到: 'calendar-check',
    每日签到: 'calendar',
  }
  return map[reason] ?? 'zap'
}

const getReasonStyle = (reason: string): { bg: string; color: string } => {
  if (reason.startsWith('积分兑换')) return { bg: '#FEE2E2', color: '#F43F5E' }
  const map: Record<string, { bg: string; color: string }> = {
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
  return map[reason] ?? { bg: '#F3F4F6', color: '#6B7280' }
}

const formatTime = (dateStr: string): string => {
  const d = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  const hm = `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
  if (diff < 86400000) return `今天 ${hm}`
  if (diff < 172800000) return `昨天 ${hm}`
  if (d.getFullYear() === now.getFullYear()) {
    return `${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')} ${hm}`
  }
  return `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')}`
}

onMounted(() => loadPoints())
</script>

<style lang="scss" scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #F1F5F9;
  overflow: hidden;
}

// ── Banner ────────────────────────────────────
.banner {
  flex-shrink: 0;
  padding: 10px 16px 22px;
  background: linear-gradient(135deg, #1E3A8A 0%, #2563EB 60%, #3B82F6 100%);
  border-radius: 0 0 24px 24px;
  overflow: hidden;
  max-height: 130px;
  opacity: 1;
  transition: max-height 0.3s ease, opacity 0.25s ease, padding 0.3s ease;

  &--hidden {
    max-height: 0;
    padding-top: 0;
    padding-bottom: 0;
    opacity: 0;
  }
}

.banner-card {
  position: relative;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 20px;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(29, 78, 216, 0.18), 0 2px 8px rgba(0, 0, 0, 0.06);
}

.banner-glow {
  position: absolute;
  top: -20px;
  right: -20px;
  width: 100px;
  height: 100px;
  background: rgba(96, 165, 250, 0.25);
  border-radius: 50%;
  filter: blur(28px);
  pointer-events: none;
}

.banner-star {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #FEF3C7, #FDE68A);
  border: 1.5px solid rgba(251, 191, 36, 0.4);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(251, 191, 36, 0.3);
}

.banner-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
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
  letter-spacing: 0.2px;
}

.banner-mall-btn {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 9px 15px;
  background: #2563EB;
  border-radius: 22px;
  cursor: pointer;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.35);

  &:active { opacity: 0.82; }
}

.banner-mall-text {
  font-size: 13px;
  color: #FFFFFF;
  font-weight: 600;
}

// ── 列表 ──────────────────────────────────────
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
  background: #FFFFFF;
  border-radius: 14px;
  padding: 14px 16px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06);

  &:active { background: #F8FAFF; }

  &--skeleton {
    min-height: 60px;
    animation: skeleton-pulse 1.5s ease-in-out infinite;
    background: #FFFFFF;
  }
}

.record-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.record-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.record-reason {
  font-size: 14px;
  font-weight: 600;
  color: #1E293B;
}

.record-meta {
  font-size: 11px;
  color: #94A3B8;
}

.record-amount {
  font-size: 17px;
  font-weight: 800;
  flex-shrink: 0;

  &--plus  { color: #10B981; }
  &--minus { color: #EF4444; }
}

// ── 骨架屏 ────────────────────────────────────
.skeleton-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: #E5E7EB;
  flex-shrink: 0;
}

.skeleton-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.skeleton-line {
  height: 11px;
  border-radius: 6px;
  background: #E5E7EB;

  &--long  { width: 60%; }
  &--short { width: 40%; }
}

.skeleton-amount {
  width: 40px;
  height: 18px;
  border-radius: 6px;
  background: #E5E7EB;
  flex-shrink: 0;
}

@keyframes skeleton-pulse {
  0%, 100% { opacity: 1; }
  50%       { opacity: 0.5; }
}

// ── 底部提示 & 空状态 ─────────────────────────
.footer-tip {
  display: flex;
  justify-content: center;
  padding: 14px;
}

.footer-text {
  font-size: 12px;
  color: #CBD5E1;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  gap: 10px;
}

.empty-text {
  font-size: 14px;
  color: #9CA3AF;
  font-weight: 500;
}

.empty-tip {
  font-size: 12px;
  color: #D1D5DB;
}
</style>
