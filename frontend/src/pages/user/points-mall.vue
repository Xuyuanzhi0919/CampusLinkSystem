<template>
  <view class="mall-page">

    <!-- 统一导航栏 -->
    <CNavBar title="积分商城" />

    <!-- 内容居中容器 -->
    <view class="page-body">

    <!-- 积分卡 -->
    <view class="banner-section">
      <view class="banner-card">
        <view class="banner-header">
          <text class="banner-label">我的积分</text>
          <view class="banner-history-btn" @click="goHistory">
            <text class="banner-history-text">积分明细</text>
            <Icon name="chevron-right" :size="12" color="#2563EB" />
          </view>
        </view>
        <view class="banner-main">
          <text class="banner-value">{{ currentPoints.toLocaleString() }}</text>
          <text class="banner-unit">分</text>
        </view>
        <text class="banner-desc">
          <text v-if="affordableCount > 0">当前可兑换 {{ affordableCount }} 件商品</text>
          <text v-else>积分不足以兑换任何商品，继续加油赚积分</text>
        </text>
        <view class="banner-tips">
          <view class="banner-tip-chip">
            <Icon name="calendar-check" :size="11" color="#10B981" />
            <text class="tip-chip-text">签到 +10</text>
          </view>
          <view class="banner-tip-chip">
            <Icon name="upload" :size="11" color="#3B82F6" />
            <text class="tip-chip-text">上传 +10</text>
          </view>
          <view class="banner-tip-chip">
            <Icon name="check-circle" :size="11" color="#8B5CF6" />
            <text class="tip-chip-text">采纳 +20</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 主 Tab：商城 / 我的兑换 -->
    <view class="main-tabs">
      <view
        v-for="tab in mainTabs"
        :key="tab.key"
        class="main-tab-item"
        :class="{ 'main-tab-item--active': activeMainTab === tab.key }"
        @click="activeMainTab = tab.key"
      >
        <text class="main-tab-text">{{ tab.label }}</text>
      </view>
    </view>

    <!-- ═══ 商城视图 ═══ -->
    <view v-if="activeMainTab === 'mall'" class="mall-view">

      <!-- 分类筛选（带图标） -->
      <scroll-view class="category-scroll" scroll-x>
        <view class="category-list">
          <view
            v-for="cat in CATEGORIES"
            :key="cat.key"
            class="category-chip"
            :class="{ 'category-chip--active': activeCategory === cat.key }"
            @click="handleCategoryChange(cat.key)"
          >
            <Icon
              :name="cat.icon"
              :size="13"
              :color="activeCategory === cat.key ? '#FFFFFF' : '#64748B'"
            />
            <text class="category-text">{{ cat.label }}</text>
          </view>
        </view>
      </scroll-view>

      <!-- 商品列表 -->
      <scroll-view class="items-scroll" scroll-y>

        <!-- 骨架屏 -->
        <view v-if="itemsLoading" class="items-grid">
          <view v-for="i in 6" :key="i" class="item-card-skeleton">
            <view class="sk-header shimmer" />
            <view class="sk-body">
              <view class="sk-line sk-line--long shimmer" />
              <view class="sk-line sk-line--short shimmer" />
              <view class="sk-footer shimmer" />
            </view>
          </view>
        </view>

        <!-- 商品网格 -->
        <view v-else-if="items.length > 0" class="items-grid">
          <view
            v-for="item in items"
            :key="item.itemId"
            class="item-card"
            :class="[`item-card--${item.category}`, { 'item-card--locked': currentPoints < item.pointsCost }]"
            @click="openConfirm(item)"
          >
            <!-- 彩色头部区 -->
            <view class="item-header">
              <view class="item-icon-wrap">
                <Icon :name="getCategoryIcon(item.category)" :size="24" color="white" />
              </view>
              <view v-if="item.stock > 0 && item.stock <= 20" class="item-stock-badge">
                <text class="item-stock-text">仅剩 {{ item.stock }}</text>
              </view>
            </view>

            <!-- 内容区 -->
            <view class="item-body">
              <text class="item-name">{{ item.name }}</text>
              <text class="item-desc">{{ item.description }}</text>
              <view class="item-footer">
                <view class="item-cost">
                  <Icon name="star" :size="12" color="#F59E0B" />
                  <text class="item-cost-num">{{ item.pointsCost }}</text>
                </view>
                <view v-if="currentPoints >= item.pointsCost" class="item-btn">
                  <text class="item-btn-text">兑换</text>
                </view>
                <view v-else class="item-btn item-btn--locked">
                  <text class="item-btn-text item-btn-text--locked">差 {{ item.pointsCost - currentPoints }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 空状态 -->
        <view v-else class="empty-state">
          <view class="empty-visual">
            <view class="empty-orb">
              <view class="empty-orb-inner">
                <Icon name="shopping-bag" :size="32" color="#93C5FD" />
              </view>
            </view>
            <view class="empty-float empty-float--1" />
            <view class="empty-float empty-float--2" />
            <view class="empty-float empty-float--3" />
          </view>
          <view class="empty-copy">
            <text class="empty-title">暂无商品</text>
            <text class="empty-subtitle">该分类下暂时没有可兑换商品，去其他分类看看吧</text>
          </view>
          <view class="empty-cta" @click="handleCategoryChange('all')">
            <text class="empty-cta-text">查看全部商品</text>
          </view>
        </view>

        <view style="height: 24px;" />
      </scroll-view>
    </view>

    <!-- ═══ 兑换记录视图 ═══ -->
    <view v-else class="records-view">
      <scroll-view class="records-scroll" scroll-y @scrolltolower="handleRecordsLoadMore">

        <!-- 骨架屏 -->
        <view v-if="recordsLoading" class="records-list">
          <view v-for="i in 5" :key="i" class="record-card record-card--skeleton">
            <view class="sk-icon shimmer" />
            <view class="sk-block">
              <view class="sk-line sk-line--long shimmer" />
              <view class="sk-line sk-line--short shimmer" />
            </view>
          </view>
        </view>

        <!-- 记录列表（按日期分组） -->
        <view v-else-if="groupedRecords.length > 0" class="records-list">
          <template v-for="group in groupedRecords" :key="group.dateLabel">
            <!-- 日期分隔 -->
            <view class="date-divider">
              <text class="date-divider-text">{{ group.dateLabel }}</text>
            </view>

            <view v-for="rec in group.items" :key="rec.recordId" class="record-card">
              <view class="record-icon-wrap" :class="`record-icon-wrap--${getEffectCategory(rec.effectType)}`">
                <Icon :name="getEffectIcon(rec.effectType)" :size="20" color="white" />
              </view>
              <view class="record-info">
                <text class="record-name">{{ rec.itemName }}</text>
                <text class="record-meta">
                  {{ EFFECT_LABELS[rec.effectType] || '权益' }}：{{ rec.effectValue }} · {{ formatTime(rec.createdAt) }}
                </text>
              </view>
              <view class="record-right">
                <text class="record-cost">-{{ rec.pointsCost }}</text>
                <view class="record-status" :class="`record-status--${rec.status}`">
                  <text class="record-status-text">{{ STATUS_LABELS[rec.status] }}</text>
                </view>
              </view>
            </view>
          </template>

          <!-- 底部状态 -->
          <view v-if="recordsLoadingMore" class="footer-loader">
            <view class="footer-spinner">
              <Icon name="loader" :size="16" color="#94A3B8" />
            </view>
            <text class="footer-loader-text">加载中…</text>
          </view>
          <view v-else-if="!recordsHasMore" class="footer-end">
            <view class="footer-line" />
            <text class="footer-end-text">已显示全部记录</text>
            <view class="footer-line" />
          </view>
        </view>

        <!-- 空状态 -->
        <view v-else class="empty-state">
          <view class="empty-visual">
            <view class="empty-orb">
              <view class="empty-orb-inner">
                <Icon name="inbox" :size="32" color="#93C5FD" />
              </view>
            </view>
            <view class="empty-float empty-float--1" />
            <view class="empty-float empty-float--2" />
            <view class="empty-float empty-float--3" />
          </view>
          <view class="empty-copy">
            <text class="empty-title">暂无兑换记录</text>
            <text class="empty-subtitle">你还没有兑换过任何商品，去商城挑选心仪的权益吧</text>
          </view>
          <view class="empty-cta" @click="activeMainTab = 'mall'">
            <text class="empty-cta-text">去逛商城</text>
          </view>
        </view>

        <view style="height: 24px;" />
      </scroll-view>
    </view>

    </view><!-- /page-body -->

    <!-- ═══ 兑换确认弹窗 ═══ -->
    <view v-if="confirmItem" class="mask" @click="closeConfirm" />
    <view class="confirm-sheet" :class="{ 'confirm-sheet--show': !!confirmItem }">
      <view v-if="confirmItem">
        <view class="sheet-handle" />
        <view class="sheet-item-info">
          <view class="sheet-icon-wrap" :class="`item-card--${confirmItem.category}`">
            <Icon :name="getCategoryIcon(confirmItem.category)" :size="32" color="white" />
          </view>
          <view class="sheet-item-text">
            <text class="sheet-item-name">{{ confirmItem.name }}</text>
            <text class="sheet-item-desc">{{ confirmItem.description }}</text>
          </view>
        </view>
        <view class="sheet-points-preview">
          <view class="preview-row">
            <text class="preview-label">当前积分</text>
            <text class="preview-val">{{ currentPoints }}</text>
          </view>
          <view class="preview-row">
            <text class="preview-label">消耗积分</text>
            <text class="preview-val preview-val--minus">-{{ confirmItem.pointsCost }}</text>
          </view>
          <view class="preview-divider" />
          <view class="preview-row">
            <text class="preview-label preview-label--bold">兑换后剩余</text>
            <text
              class="preview-val preview-val--result"
              :class="{ 'preview-val--warn': currentPoints - confirmItem.pointsCost < 0 }"
            >
              {{ currentPoints - confirmItem.pointsCost }}
            </text>
          </view>
        </view>
        <view class="sheet-actions">
          <view class="sheet-btn sheet-btn--cancel" @click="closeConfirm">
            <text>取消</text>
          </view>
          <view
            class="sheet-btn sheet-btn--confirm"
            :class="{ 'sheet-btn--loading': redeeming }"
            @click="handleRedeem"
          >
            <text>{{ redeeming ? '兑换中…' : '确认兑换' }}</text>
          </view>
        </view>
      </view>
    </view>

  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import Icon from '@/components/icons/index.vue'
import CNavBar from '@/components/layout/CNavBar.vue'
import { useUserStore } from '@/stores/user'
import { getRewardItems, redeemItem, getRedeemRecords } from '@/services/reward'
import type { RewardItem, RedeemRecord } from '@/types/reward'
import { CATEGORIES, EFFECT_LABELS } from '@/types/reward'

// ── 常量 ──────────────────────────────────────
const STATUS_LABELS: Record<number, string> = { 0: '待发放', 1: '已发放', 2: '已失效' }

const CATEGORY_ICON_MAP: Record<string, string> = {
  download: 'download', privilege: 'crown', badge: 'badge-check', coupon: 'ticket',
}
const EFFECT_CATEGORY_MAP: Record<string, string> = {
  extra_download: 'download', question_top: 'privilege',
  badge_expert: 'badge', task_bonus: 'coupon', vip_trial: 'privilege',
}
const EFFECT_ICON_MAP: Record<string, string> = {
  extra_download: 'download', question_top: 'trending-up',
  badge_expert: 'badge-check', task_bonus: 'ticket', vip_trial: 'crown',
}

const getCategoryIcon   = (cat: string)  => CATEGORY_ICON_MAP[cat]    ?? 'gift'
const getEffectCategory = (type: string) => EFFECT_CATEGORY_MAP[type] ?? 'download'
const getEffectIcon     = (type: string) => EFFECT_ICON_MAP[type]     ?? 'gift'

const formatTime = (dateStr: string) => {
  const d = new Date(dateStr)
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

// ── 状态 ──────────────────────────────────────
const userStore     = useUserStore()
const currentPoints = ref(userStore.userInfo?.points ?? 0)

const activeMainTab = ref<'mall' | 'records'>('mall')
const mainTabs = [
  { key: 'mall',    label: '积分商城' },
  { key: 'records', label: '我的兑换' },
]

const activeCategory = ref<string>('all')
const items          = ref<RewardItem[]>([])
const itemsLoading   = ref(false)

const records            = ref<RedeemRecord[]>([])
const recordsLoading     = ref(false)
const recordsLoadingMore = ref(false)
const recordsHasMore     = ref(true)
const recordsPage        = ref(1)

const confirmItem = ref<RewardItem | null>(null)
const redeeming   = ref(false)

const affordableCount = computed(() =>
  items.value.filter(i => i.pointsCost <= currentPoints.value).length
)

// 兑换记录按日期分组
interface RecordGroup { dateLabel: string; items: RedeemRecord[] }
const groupedRecords = computed<RecordGroup[]>(() => {
  const groups: RecordGroup[] = []
  const today = new Date(); today.setHours(0, 0, 0, 0)
  const yesterday = new Date(today); yesterday.setDate(yesterday.getDate() - 1)

  records.value.forEach(rec => {
    const d = new Date(rec.createdAt); d.setHours(0, 0, 0, 0)
    let label: string
    if (d.getTime() === today.getTime()) label = '今天'
    else if (d.getTime() === yesterday.getTime()) label = '昨天'
    else if (d.getFullYear() === today.getFullYear()) label = `${d.getMonth() + 1}月${d.getDate()}日`
    else label = `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`

    const group = groups.find(g => g.dateLabel === label)
    if (group) group.items.push(rec)
    else groups.push({ dateLabel: label, items: [rec] })
  })
  return groups
})

// ── 交互 ──────────────────────────────────────
const goHistory = () => uni.navigateTo({ url: '/pages/user/points-history' })

const openConfirm  = (item: RewardItem) => { confirmItem.value = item }
const closeConfirm = () => { if (!redeeming.value) confirmItem.value = null }

const handleCategoryChange = (key: string) => {
  if (activeCategory.value === key) return
  activeCategory.value = key
  loadItems()
}

const handleRecordsLoadMore = () => {
  if (recordsHasMore.value && !recordsLoadingMore.value) loadRecords(true)
}

watch(activeMainTab, (tab) => {
  if (tab === 'records' && records.value.length === 0 && !recordsLoading.value) loadRecords()
})

// ── 数据加载 ──────────────────────────────────
const loadItems = async () => {
  itemsLoading.value = true
  try {
    items.value = await getRewardItems(activeCategory.value)
  } catch (e: any) {
    uni.showToast({ title: e.message || '加载失败', icon: 'none' })
  } finally {
    itemsLoading.value = false
  }
}

const loadRecords = async (isMore = false) => {
  if (!isMore) {
    recordsLoading.value = true
    recordsPage.value = 1
  } else {
    recordsLoadingMore.value = true
  }
  try {
    const res = await getRedeemRecords(recordsPage.value, 10)
    records.value = isMore ? [...records.value, ...res.list] : res.list
    recordsHasMore.value = recordsPage.value * 10 < res.total
    recordsPage.value++
  } catch (e: any) {
    uni.showToast({ title: e.message || '加载失败', icon: 'none' })
  } finally {
    recordsLoading.value = false
    recordsLoadingMore.value = false
  }
}

const handleRedeem = async () => {
  if (!confirmItem.value || redeeming.value) return
  if (currentPoints.value < confirmItem.value.pointsCost) {
    uni.showToast({ title: '积分不足', icon: 'none' })
    return
  }
  redeeming.value = true
  try {
    await redeemItem(confirmItem.value.itemId)
    currentPoints.value -= confirmItem.value.pointsCost
    if (userStore.userInfo) userStore.userInfo.points = currentPoints.value
    const redeemed = confirmItem.value
    closeConfirm()
    uni.showToast({ title: '兑换成功！', icon: 'success' })
    if (redeemed.stock !== -1) loadItems()
    loadRecords()
  } catch (e: any) {
    uni.showToast({ title: e.message || '兑换失败，请重试', icon: 'none' })
  } finally {
    redeeming.value = false
  }
}

onMounted(async () => {
  loadItems()
  try {
    await userStore.fetchUserInfo()
    currentPoints.value = userStore.userInfo?.points ?? currentPoints.value
  } catch {}
})
</script>

<style lang="scss" scoped>

// ── 页面 & 居中容器 ───────────────────────────
.mall-page {
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

// ── 积分卡 ────────────────────────────────────
.banner-section {
  flex-shrink: 0;
  padding: 12px 16px;
}

.banner-card {
  background: #FFFFFF;
  border: 1px solid #EEF2FF;
  border-radius: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.banner-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.banner-label {
  font-size: 13px;
  color: #94A3B8;
  font-weight: 500;
}

.banner-history-btn {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 5px 12px;
  background: #EFF6FF;
  border-radius: 20px;
  cursor: pointer;
  &:active { opacity: 0.7; }
}

.banner-history-text {
  font-size: 12px;
  font-weight: 600;
  color: #2563EB;
}

.banner-main {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-top: 2px;
}

.banner-value {
  font-size: 48px;
  font-weight: 800;
  color: #2563EB;
  letter-spacing: -1px;
  line-height: 1;
}

.banner-unit {
  font-size: 18px;
  font-weight: 600;
  color: #93C5FD;
}

.banner-desc {
  font-size: 12px;
  color: #94A3B8;
  margin-top: 2px;
}

.banner-tips {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-top: 10px;
  margin-top: 4px;
  border-top: 1px solid #F1F5F9;
}

.banner-tip-chip {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 20px;
}

.tip-chip-text {
  font-size: 11px;
  color: #64748B;
  font-weight: 500;
}

// ── 主 Tab ────────────────────────────────────
.main-tabs {
  display: flex;
  background: white;
  border-bottom: 1px solid #E2E8F0;
  flex-shrink: 0;
}

.main-tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 44px;
  cursor: pointer;
  position: relative;

  &--active::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 20%;
    right: 20%;
    height: 2px;
    background: #2563EB;
    border-radius: 2px;
  }
}

.main-tab-text {
  font-size: 14px;
  font-weight: 500;
  color: #94A3B8;
  .main-tab-item--active & { color: #2563EB; font-weight: 700; }
}

// ── 商城视图 ──────────────────────────────────
.mall-view {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

// 分类筛选（带图标）
.category-scroll {
  flex-shrink: 0;
  background: white;
  border-bottom: 1px solid #E2E8F0;
}

.category-list {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  gap: 8px;
  white-space: nowrap;
}

.category-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 12px;
  border-radius: 20px;
  background: #F1F5F9;
  cursor: pointer;
  transition: all 0.15s;

  &--active {
    background: #2563EB;
    .category-text { color: white; }
  }
}

.category-text {
  font-size: 13px;
  color: #64748B;
  font-weight: 500;
  white-space: nowrap;
}

// 商品网格
.items-scroll { flex: 1; }

.items-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  padding: 16px;
}

// ── 商品卡片（彩色头部设计）────────────────────
.item-card {
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: transform 0.12s, box-shadow 0.12s;

  &:active { transform: scale(0.97); }

  // #ifdef H5
  &:hover { box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1); }
  // #endif

  &--locked { opacity: 0.75; }
}

// 头部彩色区
.item-header {
  padding: 14px 14px 10px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;

  .item-card--download &  { background: linear-gradient(135deg, #EFF6FF, #DBEAFE); }
  .item-card--privilege & { background: linear-gradient(135deg, #FFFBEB, #FEF3C7); }
  .item-card--badge &     { background: linear-gradient(135deg, #F5F3FF, #EDE9FE); }
  .item-card--coupon &    { background: linear-gradient(135deg, #F0FDF4, #DCFCE7); }
}

.item-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;

  .item-card--download &  { background: linear-gradient(135deg, #60A5FA, #3B82F6); }
  .item-card--privilege & { background: linear-gradient(135deg, #FBBF24, #F59E0B); }
  .item-card--badge &     { background: linear-gradient(135deg, #A78BFA, #7C3AED); }
  .item-card--coupon &    { background: linear-gradient(135deg, #34D399, #059669); }
}

.item-stock-badge {
  background: #FEF2F2;
  border: 1px solid #FECACA;
  border-radius: 8px;
  padding: 2px 6px;
  flex-shrink: 0;
}

.item-stock-text {
  font-size: 10px;
  color: #EF4444;
  font-weight: 600;
}

// 内容区
.item-body {
  padding: 10px 14px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.item-name {
  font-size: 13px;
  font-weight: 700;
  color: #1E293B;
  line-height: 1.3;
}

.item-desc {
  font-size: 11px;
  color: #94A3B8;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 6px;
}

.item-cost {
  display: flex;
  align-items: center;
  gap: 3px;
}

.item-cost-num {
  font-size: 14px;
  font-weight: 800;
  color: #F59E0B;
}

.item-btn {
  padding: 4px 9px;
  background: #2563EB;
  border-radius: 8px;
  cursor: pointer;
  &--locked { background: #F1F5F9; }
}

.item-btn-text {
  font-size: 11px;
  font-weight: 600;
  color: white;
  &--locked { color: #94A3B8; }
}

// ── 骨架屏（shimmer）─────────────────────────
@keyframes shimmer {
  0%   { background-position: -200% 0; }
  100% { background-position:  200% 0; }
}

.shimmer {
  background: linear-gradient(90deg, #F1F5F9 25%, #E2E8F0 50%, #F1F5F9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s ease-in-out infinite;
}

// 商品骨架
.item-card-skeleton {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.06);
}

.sk-header { height: 80px; }

.sk-body {
  padding: 10px 14px 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sk-line {
  height: 11px;
  border-radius: 6px;
  &--long  { width: 75%; }
  &--short { width: 50%; }
}

.sk-footer { height: 26px; border-radius: 8px; margin-top: 4px; }

// 记录骨架
.sk-icon  { width: 40px; height: 40px; border-radius: 12px; flex-shrink: 0; }
.sk-block { flex: 1; display: flex; flex-direction: column; gap: 8px; }

// ── 兑换记录视图 ──────────────────────────────
.records-view { flex: 1; overflow: hidden; }
.records-scroll { height: 100%; }

.records-list {
  display: flex;
  flex-direction: column;
  padding: 8px 16px;
}

// 日期分隔
.date-divider {
  padding: 12px 4px 6px;
}

.date-divider-text {
  font-size: 12px;
  font-weight: 600;
  color: #94A3B8;
}

.record-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background: white;
  border-radius: 14px;
  padding: 14px 16px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06);
  margin-bottom: 8px;

  &--skeleton { min-height: 64px; display: flex; align-items: center; gap: 12px; }
}

.record-icon-wrap {
  width: 40px; height: 40px; border-radius: 12px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;

  &--download  { background: linear-gradient(135deg, #60A5FA, #3B82F6); }
  &--privilege { background: linear-gradient(135deg, #FBBF24, #F59E0B); }
  &--badge     { background: linear-gradient(135deg, #A78BFA, #7C3AED); }
  &--coupon    { background: linear-gradient(135deg, #34D399, #059669); }
}

.record-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 3px; }

.record-name {
  font-size: 14px; font-weight: 600; color: #1E293B;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}

.record-meta { font-size: 12px; color: #94A3B8; }

.record-right {
  display: flex; flex-direction: column; align-items: flex-end; gap: 4px; flex-shrink: 0;
}

.record-cost { font-size: 15px; font-weight: 700; color: #EF4444; }

.record-status {
  padding: 2px 8px; border-radius: 20px;
  &--0 { background: #FFF7ED; }
  &--1 { background: #ECFDF5; }
  &--2 { background: #F1F5F9; }
}

.record-status-text {
  font-size: 11px; font-weight: 500;
  .record-status--0 & { color: #F59E0B; }
  .record-status--1 & { color: #10B981; }
  .record-status--2 & { color: #94A3B8; }
}

// ── 底部状态 ──────────────────────────────────
.footer-loader {
  display: flex; align-items: center; justify-content: center; gap: 6px; padding: 16px;
}

.footer-spinner { animation: spin 1s linear infinite; }

@keyframes spin {
  from { transform: rotate(0deg); }
  to   { transform: rotate(360deg); }
}

.footer-loader-text { font-size: 12px; color: #94A3B8; }

.footer-end {
  display: flex; align-items: center; gap: 10px; padding: 16px 20px;
}

.footer-line { flex: 1; height: 1px; background: #E2E8F0; }

.footer-end-text { font-size: 11px; color: #CBD5E1; white-space: nowrap; }

// ── 空状态 ────────────────────────────────────
.empty-state {
  display: flex; flex-direction: column; align-items: center;
  padding: 48px 32px 32px; gap: 16px;
}

.empty-visual { position: relative; width: 88px; height: 88px; }

.empty-orb {
  width: 88px; height: 88px; border-radius: 50%;
  background: linear-gradient(135deg, #EFF6FF, #DBEAFE);
  display: flex; align-items: center; justify-content: center;
}

.empty-orb-inner {
  width: 64px; height: 64px; border-radius: 50%;
  background: linear-gradient(135deg, #DBEAFE, #BFDBFE);
  display: flex; align-items: center; justify-content: center;
}

.empty-float {
  position: absolute; border-radius: 50%;
  animation: float-bob 2.4s ease-in-out infinite;
  &--1 { width: 10px; height: 10px; background: #BFDBFE; top: 4px; right: 8px; animation-delay: 0s; }
  &--2 { width: 7px; height: 7px; background: #93C5FD; bottom: 8px; right: 4px; animation-delay: 0.6s; }
  &--3 { width: 8px; height: 8px; background: #DBEAFE; bottom: 6px; left: 6px; animation-delay: 1.2s; }
}

@keyframes float-bob {
  0%, 100% { transform: translateY(0); opacity: 0.7; }
  50%       { transform: translateY(-5px); opacity: 1; }
}

.empty-copy { display: flex; flex-direction: column; align-items: center; gap: 6px; }

.empty-title { font-size: 16px; font-weight: 700; color: #1E293B; }

.empty-subtitle { font-size: 13px; color: #94A3B8; line-height: 1.5; text-align: center; }

.empty-cta {
  padding: 10px 28px; background: #2563EB; border-radius: 24px;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.28); cursor: pointer;
  &:active { opacity: 0.82; }
}

.empty-cta-text { font-size: 14px; font-weight: 600; color: white; }

// ── 确认弹窗 ──────────────────────────────────
.mask {
  position: fixed; inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 200;
  animation: mask-in 0.2s ease;
}

@keyframes mask-in {
  from { opacity: 0; }
  to   { opacity: 1; }
}

.confirm-sheet {
  position: fixed; bottom: 0; left: 0; right: 0;
  background: white;
  border-radius: 24px 24px 0 0;
  z-index: 201;
  padding: 0 20px 40px;
  transform: translateY(100%);
  transition: transform 0.3s cubic-bezier(0.32, 0.72, 0, 1);
  &--show { transform: translateY(0); }
}

.sheet-handle {
  width: 36px; height: 4px;
  background: #E2E8F0; border-radius: 2px;
  margin: 12px auto 20px;
}

.sheet-item-info {
  display: flex; align-items: center; gap: 14px;
  padding-bottom: 20px; border-bottom: 1px solid #E2E8F0;
}

// 弹窗图标复用商品卡片头部颜色
.sheet-icon-wrap {
  width: 56px; height: 56px; border-radius: 16px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  &.item-card--download  { background: linear-gradient(135deg, #60A5FA, #3B82F6); }
  &.item-card--privilege { background: linear-gradient(135deg, #FBBF24, #F59E0B); }
  &.item-card--badge     { background: linear-gradient(135deg, #A78BFA, #7C3AED); }
  &.item-card--coupon    { background: linear-gradient(135deg, #34D399, #059669); }
}

.sheet-item-text { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.sheet-item-name { font-size: 17px; font-weight: 700; color: #1E293B; }
.sheet-item-desc { font-size: 13px; color: #94A3B8; line-height: 1.4; }

.sheet-points-preview { display: flex; flex-direction: column; gap: 12px; padding: 20px 0; }

.preview-row { display: flex; align-items: center; justify-content: space-between; }

.preview-label {
  font-size: 14px; color: #64748B;
  &--bold { font-weight: 600; color: #1E293B; }
}

.preview-val {
  font-size: 16px; font-weight: 600; color: #1E293B;
  &--minus  { color: #EF4444; }
  &--result { color: #10B981; }
  &--warn   { color: #EF4444; }
}

.preview-divider { height: 1px; background: #E2E8F0; }

.sheet-actions { display: flex; gap: 12px; padding-top: 8px; }

.sheet-btn {
  flex: 1; height: 50px;
  display: flex; align-items: center; justify-content: center;
  border-radius: 14px; cursor: pointer;
  font-size: 15px; font-weight: 600;
  transition: opacity 0.12s;
  &:active { opacity: 0.7; }
  &--cancel  { background: #F1F5F9; color: #64748B; }
  &--confirm { background: #2563EB; color: white; }
  &--loading { opacity: 0.6; cursor: not-allowed; }
}

</style>
