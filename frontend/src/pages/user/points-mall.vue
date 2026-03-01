<template>
  <view class="mall-page">
    <!-- 顶部导航 -->
    <CNavBar title="积分商城" />

    <!-- 积分概览 -->
    <view class="points-banner">
      <text class="banner-label">我的积分</text>
      <view class="banner-value-row">
        <Icon name="star" :size="26" color="#F59E0B" />
        <text class="banner-value">{{ currentPoints.toLocaleString() }}</text>
      </view>
      <view class="banner-sub-row">
        <text class="banner-sub">
          <text v-if="affordableCount > 0">可兑换 {{ affordableCount }} 件商品</text>
          <text v-else>积分不足以兑换任何商品</text>
        </text>
        <view class="banner-history-btn" @click="goHistory">
          <text class="banner-history-text">明细</text>
          <Icon name="chevron-right" :size="12" color="rgba(255,255,255,0.7)" />
        </view>
      </view>
    </view>

    <!-- 主体 tab：商城 / 我的兑换 -->
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
      <scroll-view class="items-scroll" scroll-y @scrolltolower="() => {}">
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
            <!-- 图标区 -->
            <view class="item-icon-wrap" :class="`item-icon-wrap--${item.category}`">
              <Icon :name="getCategoryIcon(item.category)" :size="28" color="white" />
            </view>

            <!-- 名称 & 描述 -->
            <text class="item-name">{{ item.name }}</text>
            <text class="item-desc">{{ item.description }}</text>

            <!-- 库存提示 -->
            <text v-if="item.stock > 0 && item.stock <= 20" class="item-stock">
              仅剩 {{ item.stock }} 件
            </text>

            <!-- 积分 + 兑换按钮 -->
            <view class="item-footer">
              <view class="item-cost">
                <Icon name="star" :size="13" color="#F59E0B" />
                <text class="item-cost-num">{{ item.pointsCost }}</text>
              </view>
              <view
                class="item-btn"
                :class="{ 'item-btn--disabled': currentPoints < item.pointsCost }"
              >
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
      <scroll-view
        class="records-scroll"
        scroll-y
        @scrolltolower="handleRecordsLoadMore"
      >
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
          <view
            v-for="rec in records"
            :key="rec.recordId"
            class="record-card"
          >
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

          <!-- 加载更多 -->
          <view v-if="recordsHasMore" class="load-more" @click="handleRecordsLoadMore">
            <text class="load-more-text">{{ recordsLoadingMore ? '加载中…' : '查看更多' }}</text>
          </view>
          <view v-else class="no-more">
            <text class="no-more-text">已显示全部记录</text>
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
        <!-- 拖拽条 -->
        <view class="sheet-handle" />

        <!-- 商品信息 -->
        <view class="sheet-item-info">
          <view class="sheet-icon-wrap" :class="`item-icon-wrap--${confirmItem.category}`">
            <Icon :name="getCategoryIcon(confirmItem.category)" :size="32" color="white" />
          </view>
          <view class="sheet-item-text">
            <text class="sheet-item-name">{{ confirmItem.name }}</text>
            <text class="sheet-item-desc">{{ confirmItem.description }}</text>
          </view>
        </view>

        <!-- 积分预览 -->
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

        <!-- 操作按钮 -->
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
import { CNavBar } from '@/components/layout'
import Icon from '@/components/icons/index.vue'
import { useUserStore } from '@/stores/user'
import { getRewardItems, redeemItem, getRedeemRecords } from '@/services/reward'
import type { RewardItem, RedeemRecord } from '@/types/reward'
import { CATEGORIES, EFFECT_LABELS } from '@/types/reward'

// ─── 状态 ──────────────────────────────────────────────
const userStore = useUserStore()
const currentPoints = ref(userStore.userInfo?.points ?? 0)

const activeMainTab = ref<'mall' | 'records'>('mall')
const mainTabs = [
  { key: 'mall',    label: '积分商城' },
  { key: 'records', label: '我的兑换' },
]

// 商城
const activeCategory = ref<string>('all')
const items = ref<RewardItem[]>([])
const itemsLoading = ref(false)

// 兑换记录
const records = ref<RedeemRecord[]>([])
const recordsLoading = ref(false)
const recordsLoadingMore = ref(false)
const recordsHasMore = ref(true)
const recordsPage = ref(1)

// 可负担商品数量（用于 banner 副标题）
const affordableCount = computed(() =>
  items.value.filter(item => item.pointsCost <= currentPoints.value).length
)

// 确认弹窗
const confirmItem = ref<RewardItem | null>(null)
const redeeming = ref(false)

// ─── 常量 ──────────────────────────────────────────────
const STATUS_LABELS: Record<number, string> = {
  0: '待发放',
  1: '已发放',
  2: '已失效',
}

// 切换到「我的兑换」tab 时懒加载记录（首次或数据为空时触发）
watch(activeMainTab, (tab) => {
  if (tab === 'records' && records.value.length === 0 && !recordsLoading.value) {
    loadRecords()
  }
})

// ─── 商品逻辑 ──────────────────────────────────────────
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

const handleCategoryChange = (key: string) => {
  if (activeCategory.value === key) return
  activeCategory.value = key
  loadItems()
}

// ─── 兑换记录逻辑 ──────────────────────────────────────
const loadRecords = async (isMore = false) => {
  if (!isMore) {
    recordsLoading.value = true
    recordsPage.value = 1
  } else {
    recordsLoadingMore.value = true
  }
  try {
    const res = await getRedeemRecords(recordsPage.value, 10)
    if (isMore) {
      records.value = [...records.value, ...res.list]
    } else {
      records.value = res.list
    }
    recordsHasMore.value = recordsPage.value * 10 < res.total
    recordsPage.value++
  } catch (e: any) {
    uni.showToast({ title: e.message || '加载失败', icon: 'none' })
  } finally {
    recordsLoading.value = false
    recordsLoadingMore.value = false
  }
}

const handleRecordsLoadMore = () => {
  if (recordsHasMore.value && !recordsLoadingMore.value) {
    loadRecords(true)
  }
}

// ─── 确认弹窗逻辑 ──────────────────────────────────────
const openConfirm = (item: RewardItem) => {
  confirmItem.value = item
}

const closeConfirm = () => {
  if (redeeming.value) return
  confirmItem.value = null
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

    // 更新本地积分余额
    currentPoints.value -= confirmItem.value.pointsCost
    if (userStore.userInfo) {
      userStore.userInfo.points = currentPoints.value
    }

    const redeemed = confirmItem.value
    closeConfirm()

    uni.showToast({ title: '兑换成功！', icon: 'success' })

    // 有限库存商品兑换后刷新列表（无限库存无需刷新）
    if (redeemed.stock !== -1) {
      loadItems()
    }

    // 预加载一次记录，方便切换到记录 tab 时立即显示
    loadRecords()
  } catch (e: any) {
    uni.showToast({ title: e.message || '兑换失败，请重试', icon: 'none' })
  } finally {
    redeeming.value = false
  }
}

// ─── 辅助函数 ──────────────────────────────────────────
const goHistory = () => uni.navigateTo({ url: '/pages/user/points-history' })

const getCategoryIcon = (cat: string) => {
  const map: Record<string, string> = {
    download:  'download',
    privilege: 'crown',
    badge:     'badge-check',
    coupon:    'ticket',
  }
  return map[cat] ?? 'gift'
}

const getEffectCategory = (effectType: string) => {
  const map: Record<string, string> = {
    extra_download: 'download',
    question_top:   'privilege',
    badge_expert:   'badge',
    task_bonus:     'coupon',
    vip_trial:      'privilege',
  }
  return map[effectType] ?? 'download'
}

const getEffectIcon = (effectType: string) => {
  const map: Record<string, string> = {
    extra_download: 'download',
    question_top:   'trending-up',
    badge_expert:   'badge-check',
    task_bonus:     'ticket',
    vip_trial:      'crown',
  }
  return map[effectType] ?? 'gift'
}

const formatDate = (dateStr: string) => {
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

// ─── 生命周期 ──────────────────────────────────────────
onMounted(async () => {
  // 并行加载商品和刷新用户积分
  loadItems()
  try {
    await userStore.fetchUserInfo()
    currentPoints.value = userStore.userInfo?.points ?? currentPoints.value
  } catch {}
})
</script>

<style lang="scss" scoped>
.mall-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #F9FAFB;
  overflow: hidden;
}

// ── 积分概览 ──────────────────────────────────
.points-banner {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 20px 20px 22px;
  background: linear-gradient(135deg, #377DFF 0%, #2563EB 100%);
  flex-shrink: 0;
}

.banner-label {
  font-size: 12px;
  color: rgba(255,255,255,0.65);
  letter-spacing: 0.5px;
}

.banner-value-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.banner-value {
  font-size: 36px;
  font-weight: 800;
  color: #FFFFFF;
  letter-spacing: -1px;
  line-height: 1;
}

.banner-sub-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 2px;
}

.banner-sub {
  font-size: 13px;
  color: rgba(255,255,255,0.75);
}

.banner-history-btn {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 4px 10px;
  background: rgba(255,255,255,0.15);
  border-radius: 20px;
  cursor: pointer;
}

.banner-history-text {
  font-size: 12px;
  color: rgba(255,255,255,0.85);
  font-weight: 500;
}

// ── 主 Tab ────────────────────────────────────
.main-tabs {
  display: flex;
  background: white;
  border-bottom: 1px solid #EDEDEB;
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
    background: #377DFF;
    border-radius: 2px;
  }
}

.main-tab-text {
  font-size: 14px;
  font-weight: 500;
  color: #9CA3AF;

  .main-tab-item--active & {
    color: #377DFF;
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

// 分类筛选
.category-scroll {
  flex-shrink: 0;
  background: white;
  border-bottom: 1px solid #EDEDEB;
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
  background: #F3F4F6;
  cursor: pointer;
  transition: all 0.15s;

  &--active {
    background: #377DFF;
    .category-text { color: white; }
  }
}

.category-text {
  font-size: 13px;
  color: #6D6C6A;
  font-weight: 500;
  white-space: nowrap;
}

// 商品列表滚动区
.items-scroll { flex: 1; }

// 商品网格
.items-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  padding: 16px;
}

// 商品卡片
.item-card {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  background: white;
  border-radius: 16px;
  padding: 16px;
  cursor: pointer;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  transition: transform 0.12s;

  &:active { transform: scale(0.97); }

  &--skeleton {
    cursor: default;
    &:active { transform: none; }
  }
}

// 商品图标区
.item-icon-wrap {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;

  &--download  { background: linear-gradient(135deg, #5A8BFF, #377DFF); }
  &--privilege { background: linear-gradient(135deg, #FFB766, #F39C12); }
  &--badge     { background: linear-gradient(135deg, #8B5CF6, #7C3AED); }
  &--coupon    { background: linear-gradient(135deg, #3CCB7F, #27AE60); }
}

.item-name {
  font-size: 14px;
  font-weight: 700;
  color: #1A1A1A;
  line-height: 1.3;
}

.item-desc {
  font-size: 11px;
  color: #9CA3AF;
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
  background: #377DFF;
  border-radius: 8px;
  cursor: pointer;

  &--disabled {
    background: #E5E7EB;
    cursor: not-allowed;
  }
}

.item-btn-text {
  font-size: 11px;
  font-weight: 600;
  color: white;

  .item-btn--disabled & { color: #9CA3AF; }
}

// ── 骨架屏 ────────────────────────────────────
.skeleton-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: #F3F4F6;
  animation: skeleton-pulse 1.5s ease-in-out infinite;
}

.skeleton-icon--sm {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: #F3F4F6;
  flex-shrink: 0;
  animation: skeleton-pulse 1.5s ease-in-out infinite;
}

.skeleton-line {
  height: 12px;
  border-radius: 6px;
  background: #F3F4F6;
  animation: skeleton-pulse 1.5s ease-in-out infinite;

  &--long  { width: 80%; }
  &--short { width: 50%; }
}

.skeleton-btn {
  height: 30px;
  border-radius: 8px;
  background: #F3F4F6;
  animation: skeleton-pulse 1.5s ease-in-out infinite;
  align-self: stretch;
  margin-top: 4px;
}

.skeleton-block {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

@keyframes skeleton-pulse {
  0%, 100% { opacity: 1; }
  50%       { opacity: 0.5; }
}

// ── 兑换记录视图 ──────────────────────────────
.records-view {
  flex: 1;
  overflow: hidden;
}

.records-scroll { height: 100%; }

.records-list {
  display: flex;
  flex-direction: column;
  gap: 1px;
  padding: 12px 16px;
  gap: 10px;
}

.record-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background: white;
  border-radius: 14px;
  padding: 14px 16px;

  &--skeleton {
    min-height: 64px;
    animation: skeleton-pulse 1.5s ease-in-out infinite;
    background: #F3F4F6;
  }
}

.record-icon-wrap {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &--download  { background: linear-gradient(135deg, #5A8BFF, #377DFF); }
  &--privilege { background: linear-gradient(135deg, #FFB766, #F39C12); }
  &--badge     { background: linear-gradient(135deg, #8B5CF6, #7C3AED); }
  &--coupon    { background: linear-gradient(135deg, #3CCB7F, #27AE60); }
}

.record-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.record-name {
  font-size: 14px;
  font-weight: 600;
  color: #1A1A1A;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.record-meta {
  font-size: 12px;
  color: #9CA3AF;
}

.record-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  flex-shrink: 0;
}

.record-cost {
  font-size: 15px;
  font-weight: 700;
  color: #EF4444;
}

.record-status {
  padding: 2px 8px;
  border-radius: 20px;

  &--0 { background: #FFF5E6; }
  &--1 { background: #E6F9F0; }
  &--2 { background: #F3F4F6; }
}

.record-status-text {
  font-size: 11px;
  font-weight: 500;

  .record-status--0 & { color: #FFB766; }
  .record-status--1 & { color: #3CCB7F; }
  .record-status--2 & { color: #9CA3AF; }
}

// ── 加载更多 & 空状态 ──────────────────────────
.load-more {
  display: flex;
  justify-content: center;
  padding: 16px;
  cursor: pointer;
}

.load-more-text { font-size: 13px; color: #6D6C6A; }

.no-more {
  display: flex;
  justify-content: center;
  padding: 16px;
}

.no-more-text { font-size: 12px; color: #D1D5DB; }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  gap: 12px;
}

.empty-text { font-size: 14px; color: #9CA3AF; }

.empty-sub {
  font-size: 13px;
  color: #377DFF;
  font-weight: 600;
  cursor: pointer;
  text-decoration: underline;
}

// ── 确认弹窗 ──────────────────────────────────
.mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 200;
  animation: mask-in 0.2s ease;
}

@keyframes mask-in {
  from { opacity: 0; }
  to   { opacity: 1; }
}

.confirm-sheet {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  border-radius: 24px 24px 0 0;
  z-index: 201;
  padding: 0 20px 40px;
  transform: translateY(100%);
  transition: transform 0.3s cubic-bezier(0.32, 0.72, 0, 1);

  &--show { transform: translateY(0); }
}

.sheet-handle {
  width: 36px;
  height: 4px;
  background: #E5E7EB;
  border-radius: 2px;
  margin: 12px auto 20px;
}

.sheet-item-info {
  display: flex;
  align-items: center;
  gap: 14px;
  padding-bottom: 20px;
  border-bottom: 1px solid #EDEDEB;
}

.sheet-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.sheet-item-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sheet-item-name {
  font-size: 17px;
  font-weight: 700;
  color: #1A1A1A;
}

.sheet-item-desc {
  font-size: 13px;
  color: #9CA3AF;
  line-height: 1.4;
}

.sheet-points-preview {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px 0;
}

.preview-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.preview-label {
  font-size: 14px;
  color: #6B778C;

  &--bold {
    font-weight: 600;
    color: #1A1A1A;
  }
}

.preview-val {
  font-size: 16px;
  font-weight: 600;
  color: #1A1A1A;

  &--minus  { color: #FF5C5C; }
  &--result { color: #3CCB7F; }
  &--warn   { color: #FF5C5C; }
}

.preview-divider {
  height: 1px;
  background: #EDEDEB;
}

.sheet-actions {
  display: flex;
  gap: 12px;
  padding-top: 8px;
}

.sheet-btn {
  flex: 1;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  transition: opacity 0.12s;

  &:active { opacity: 0.7; }

  &--cancel  { background: #F3F4F6; color: #6B778C; }
  &--confirm { background: #377DFF; color: white; }
  &--loading { opacity: 0.6; cursor: not-allowed; }
}
</style>
