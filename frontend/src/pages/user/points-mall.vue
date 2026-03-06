<template>
  <view class="mall-page">

    <!-- ── 统一头部（导航 + 积分卡共享渐变背景）── -->
    <view class="page-header">

      <!-- 导航行 -->
      <view class="header-nav">
        <view class="nav-back" @click="goBack">
          <Icon name="arrow-left" :size="20" color="#FFFFFF" />
        </view>
        <text class="nav-title">积分商城</text>
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
            <text class="banner-value">{{ currentPoints.toLocaleString() }}</text>
            <text class="banner-desc">
              <text v-if="affordableCount > 0">可兑换 {{ affordableCount }} 件商品</text>
              <text v-else>积分不足以兑换任何商品</text>
            </text>
          </view>
          <view class="banner-detail-btn" @click="goHistory">
            <text class="banner-detail-text">明细</text>
            <Icon name="chevron-right" :size="13" color="#FFFFFF" />
          </view>
        </view>
      </view>

    </view>

    <!-- ── 主 Tab：商城 / 我的兑换 ── -->
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

      <!-- 分类筛选 -->
      <scroll-view class="category-scroll" scroll-x>
        <view class="category-list">
          <view
            v-for="cat in CATEGORIES"
            :key="cat.key"
            class="category-chip"
            :class="{ 'category-chip--active': activeCategory === cat.key }"
            @click="handleCategoryChange(cat.key)"
          >
            <text class="category-text">{{ cat.label }}</text>
          </view>
        </view>
      </scroll-view>

      <!-- 商品列表 -->
      <scroll-view class="items-scroll" scroll-y>

        <!-- 骨架屏 -->
        <view v-if="itemsLoading" class="items-grid">
          <view v-for="i in 6" :key="i" class="item-card item-card--skeleton">
            <view class="skeleton-icon" />
            <view class="skeleton-line skeleton-line--long" />
            <view class="skeleton-line skeleton-line--short" />
            <view class="skeleton-btn" />
          </view>
        </view>

        <!-- 商品网格 -->
        <view v-else-if="items.length > 0" class="items-grid">
          <view
            v-for="item in items"
            :key="item.itemId"
            class="item-card"
            @click="openConfirm(item)"
          >
            <view class="item-icon-wrap" :class="`item-icon-wrap--${item.category}`">
              <Icon :name="getCategoryIcon(item.category)" :size="28" color="white" />
            </view>
            <text class="item-name">{{ item.name }}</text>
            <text class="item-desc">{{ item.description }}</text>
            <text v-if="item.stock > 0 && item.stock <= 20" class="item-stock">
              仅剩 {{ item.stock }} 件
            </text>
            <view class="item-footer">
              <view class="item-cost">
                <Icon name="star" :size="13" color="#F59E0B" />
                <text class="item-cost-num">{{ item.pointsCost }}</text>
              </view>
              <view class="item-btn" :class="{ 'item-btn--disabled': currentPoints < item.pointsCost }">
                <text class="item-btn-text">
                  {{ currentPoints >= item.pointsCost ? '立即兑换' : '积分不足' }}
                </text>
              </view>
            </view>
          </view>
        </view>

        <!-- 空状态 -->
        <view v-else class="empty-state">
          <Icon name="shopping-bag" :size="48" color="#D1D5DB" />
          <text class="empty-text">暂无商品</text>
        </view>

      </scroll-view>
    </view>

    <!-- ═══ 兑换记录视图 ═══ -->
    <view v-else class="records-view">
      <scroll-view class="records-scroll" scroll-y @scrolltolower="handleRecordsLoadMore">

        <!-- 骨架屏 -->
        <view v-if="recordsLoading" class="records-list">
          <view v-for="i in 5" :key="i" class="record-card record-card--skeleton">
            <view class="skeleton-icon skeleton-icon--sm" />
            <view class="skeleton-block">
              <view class="skeleton-line skeleton-line--long" />
              <view class="skeleton-line skeleton-line--short" />
            </view>
          </view>
        </view>

        <!-- 记录列表 -->
        <view v-else-if="records.length > 0" class="records-list">
          <view v-for="rec in records" :key="rec.recordId" class="record-card">
            <view class="record-icon-wrap" :class="`record-icon-wrap--${getEffectCategory(rec.effectType)}`">
              <Icon :name="getEffectIcon(rec.effectType)" :size="20" color="white" />
            </view>
            <view class="record-info">
              <text class="record-name">{{ rec.itemName }}</text>
              <text class="record-meta">
                {{ EFFECT_LABELS[rec.effectType] || '权益' }}：{{ rec.effectValue }} ·
                {{ formatDate(rec.createdAt) }}
              </text>
            </view>
            <view class="record-right">
              <text class="record-cost">-{{ rec.pointsCost }}</text>
              <view class="record-status" :class="`record-status--${rec.status}`">
                <text class="record-status-text">{{ STATUS_LABELS[rec.status] }}</text>
              </view>
            </view>
          </view>

          <view v-if="recordsHasMore" class="footer-tip">
            <text class="footer-text">{{ recordsLoadingMore ? '加载中…' : '上拉加载更多' }}</text>
          </view>
          <view v-else class="footer-tip">
            <text class="footer-text">已显示全部记录</text>
          </view>
        </view>

        <!-- 空状态 -->
        <view v-else class="empty-state">
          <Icon name="inbox" :size="48" color="#D1D5DB" />
          <text class="empty-text">暂无兑换记录</text>
          <text class="empty-sub" @click="activeMainTab = 'mall'">去逛商城 →</text>
        </view>

      </scroll-view>
    </view>

    <!-- ═══ 兑换确认弹窗 ═══ -->
    <view v-if="confirmItem" class="mask" @click="closeConfirm" />
    <view class="confirm-sheet" :class="{ 'confirm-sheet--show': !!confirmItem }">
      <view v-if="confirmItem">
        <view class="sheet-handle" />
        <view class="sheet-item-info">
          <view class="sheet-icon-wrap" :class="`item-icon-wrap--${confirmItem.category}`">
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
import { useUserStore } from '@/stores/user'
import { getRewardItems, redeemItem, getRedeemRecords } from '@/services/reward'
import type { RewardItem, RedeemRecord } from '@/types/reward'
import { CATEGORIES, EFFECT_LABELS } from '@/types/reward'

// ── 模块级常量 ────────────────────────────────
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

const getCategoryIcon    = (cat: string)    => CATEGORY_ICON_MAP[cat]    ?? 'gift'
const getEffectCategory  = (type: string)   => EFFECT_CATEGORY_MAP[type] ?? 'download'
const getEffectIcon      = (type: string)   => EFFECT_ICON_MAP[type]     ?? 'gift'

const formatDate = (dateStr: string) => {
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
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

// ── 交互 ──────────────────────────────────────
const goBack = () =>
  uni.navigateBack({ fail: () => uni.switchTab({ url: '/pages/home/index' }) })

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

// ── 页面容器 ──────────────────────────────────
.mall-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #F1F5F9;
  overflow: hidden;
}

// ── 统一头部 ──────────────────────────────────
.page-header {
  flex-shrink: 0;
  background: linear-gradient(160deg, #3B82F6 0%, #60A5FA 55%, #93C5FD 100%);
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

.banner-detail-btn {
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

.banner-detail-text {
  font-size: 13px;
  font-weight: 600;
  color: #FFFFFF;
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
    background: #3B82F6;
    border-radius: 2px;
  }
}

.main-tab-text {
  font-size: 14px;
  font-weight: 500;
  color: #94A3B8;

  .main-tab-item--active & {
    color: #3B82F6;
    font-weight: 700;
  }
}

// ── 商城视图 ──────────────────────────────────
.mall-view {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

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
  padding: 5px 14px;
  border-radius: 20px;
  background: #F1F5F9;
  cursor: pointer;
  transition: all 0.15s;

  &--active {
    background: #3B82F6;
    .category-text { color: white; }
  }
}

.category-text {
  font-size: 13px;
  color: #64748B;
  font-weight: 500;
  white-space: nowrap;
}

.items-scroll { flex: 1; }

.items-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  padding: 16px;
}

// ── 商品卡片 ──────────────────────────────────
.item-card {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  background: white;
  border-radius: 16px;
  padding: 16px;
  cursor: pointer;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06);
  transition: transform 0.12s;

  &:active { transform: scale(0.97); }
  &--skeleton { cursor: default; &:active { transform: none; } }
}

.item-icon-wrap {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;

  &--download  { background: linear-gradient(135deg, #60A5FA, #3B82F6); }
  &--privilege { background: linear-gradient(135deg, #FFB766, #F39C12); }
  &--badge     { background: linear-gradient(135deg, #A78BFA, #7C3AED); }
  &--coupon    { background: linear-gradient(135deg, #34D399, #059669); }
}

.item-name {
  font-size: 14px;
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

.item-stock {
  font-size: 10px;
  color: #EF4444;
  font-weight: 600;
  background: #FEF2F2;
  padding: 2px 6px;
  border-radius: 6px;
}

.item-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  margin-top: 4px;
}

.item-cost {
  display: flex;
  align-items: center;
  gap: 3px;
}

.item-cost-num {
  font-size: 15px;
  font-weight: 800;
  color: #F59E0B;
}

.item-btn {
  padding: 5px 10px;
  background: #3B82F6;
  border-radius: 8px;
  cursor: pointer;

  &--disabled { background: #E2E8F0; cursor: not-allowed; }
}

.item-btn-text {
  font-size: 11px;
  font-weight: 600;
  color: white;
  .item-btn--disabled & { color: #94A3B8; }
}

// ── 骨架屏 ────────────────────────────────────
.skeleton-icon {
  width: 52px; height: 52px; border-radius: 14px;
  background: #E2E8F0; animation: skeleton-pulse 1.5s ease-in-out infinite;
}

.skeleton-icon--sm {
  width: 40px; height: 40px; border-radius: 12px;
  background: #E2E8F0; flex-shrink: 0;
  animation: skeleton-pulse 1.5s ease-in-out infinite;
}

.skeleton-line {
  height: 12px; border-radius: 6px;
  background: #E2E8F0; animation: skeleton-pulse 1.5s ease-in-out infinite;
  &--long  { width: 80%; }
  &--short { width: 50%; }
}

.skeleton-btn {
  height: 30px; border-radius: 8px; align-self: stretch; margin-top: 4px;
  background: #E2E8F0; animation: skeleton-pulse 1.5s ease-in-out infinite;
}

.skeleton-block { flex: 1; display: flex; flex-direction: column; gap: 8px; }

@keyframes skeleton-pulse {
  0%, 100% { opacity: 1; }
  50%       { opacity: 0.5; }
}

// ── 兑换记录视图 ──────────────────────────────
.records-view { flex: 1; overflow: hidden; }
.records-scroll { height: 100%; }

.records-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px 16px;
}

.record-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background: white;
  border-radius: 14px;
  padding: 14px 16px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.06);

  &--skeleton {
    min-height: 64px;
    animation: skeleton-pulse 1.5s ease-in-out infinite;
    background: white;
  }
}

.record-icon-wrap {
  width: 40px; height: 40px; border-radius: 12px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;

  &--download  { background: linear-gradient(135deg, #60A5FA, #3B82F6); }
  &--privilege { background: linear-gradient(135deg, #FFB766, #F39C12); }
  &--badge     { background: linear-gradient(135deg, #A78BFA, #7C3AED); }
  &--coupon    { background: linear-gradient(135deg, #34D399, #059669); }
}

.record-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }

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

// ── 底部提示 & 空状态 ─────────────────────────
.footer-tip  { display: flex; justify-content: center; padding: 14px; }
.footer-text { font-size: 12px; color: #CBD5E1; }

.empty-state {
  display: flex; flex-direction: column; align-items: center;
  justify-content: center; padding: 60px 20px; gap: 12px;
}

.empty-text { font-size: 14px; color: #94A3B8; }

.empty-sub {
  font-size: 13px; color: #3B82F6; font-weight: 600;
  cursor: pointer; text-decoration: underline;
}

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

.sheet-icon-wrap {
  width: 56px; height: 56px; border-radius: 16px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
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
