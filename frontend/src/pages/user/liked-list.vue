<template>
  <view class="page">

    <!-- ═══ 顶部导航栏 ═══ -->
    <CNavBar title="获赞列表" @back="handleBack">
      <template #right>
        <view v-if="!loading && totalLikes > 0" class="nav-badge">
          <Heart :size="11" color="#fff" fill="#fff" />
          <text class="nav-badge-text">{{ fmtNum(totalLikes) }}</text>
        </view>
      </template>
    </CNavBar>

    <!-- ═══ 骨架屏 ═══ -->
    <view v-if="loading" class="page-body">
      <view class="center-wrap">
        <view class="sk-hero" />
        <view class="sk-tabs" />
        <view v-for="i in 5" :key="i" class="sk-card">
          <view class="sk-line sk-line--w80" />
          <view class="sk-line sk-line--w55" />
          <view class="sk-line sk-line--w30 sk-line--sm" />
        </view>
      </view>
    </view>

    <!-- ═══ 主内容 ═══ -->
    <scroll-view
      v-else
      class="page-body"
      scroll-y
      :lower-threshold="80"
      @scrolltolower="loadMore"
    >
      <view class="center-wrap">

        <!-- ── 数据汇总 Hero ── -->
        <view class="hero-card">
          <view class="hero-deco hero-deco--1" />
          <view class="hero-deco hero-deco--2" />
          <view class="hero-stats">
            <view class="hero-stat">
              <view class="hero-stat-icon">
                <FileText :size="15" color="#fff" />
              </view>
              <text class="hero-stat-num">{{ fmtNum(stats.resourceLikes) }}</text>
              <text class="hero-stat-lbl">资源获赞</text>
            </view>

            <view class="hero-center">
              <view class="hero-center-ring">
                <Heart :size="26" color="#fff" fill="#fff" />
              </view>
              <text class="hero-total-num">{{ fmtNum(totalLikes) }}</text>
              <text class="hero-total-lbl">总获赞</text>
            </view>

            <view class="hero-stat">
              <view class="hero-stat-icon">
                <MessageCircle :size="15" color="#fff" />
              </view>
              <text class="hero-stat-num">{{ fmtNum(stats.answerLikes) }}</text>
              <text class="hero-stat-lbl">回答获赞</text>
            </view>
          </view>
        </view>

        <!-- ── 类型筛选 Tab ── -->
        <view class="tab-bar">
          <view
            v-for="tab in tabs"
            :key="tab.key"
            class="tab-item"
            :class="{ active: activeTab === tab.key }"
            @click="activeTab = tab.key"
          >
            <component :is="tab.icon" :size="13" />
            <text class="tab-label">{{ tab.label }}</text>
            <text v-if="tab.count > 0" class="tab-count">{{ tab.count }}</text>
          </view>
        </view>

        <!-- ── 空状态 ── -->
        <view v-if="filteredList.length === 0" class="empty">
          <view class="empty-icon">
            <Heart :size="32" color="#FDA4AF" :stroke-width="1.5" />
          </view>
          <text class="empty-title">
            {{ activeTab === 'all' ? '还没有内容被点赞' : `暂无${activeTab === 'resource' ? '资源' : '回答'}获赞记录` }}
          </text>
          <text class="empty-desc">上传资源或回答问题，让更多同学发现你的价值</text>
        </view>

        <!-- ── 列表 ── -->
        <template v-else>
          <view class="list-header">
            <view class="list-header-line" />
            <text class="list-header-text">共 {{ filteredList.length }} 条</text>
            <view class="list-header-line" />
          </view>

          <view class="list-grid">
            <view
              v-for="item in filteredList"
              :key="`${item.type}-${item.targetId}`"
              class="list-card"
              @click="handleItemClick(item)"
            >
              <view class="card-badge" :class="`card-badge--${item.type}`">
                <component :is="item.type === 'resource' ? FileText : MessageCircle" :size="11" />
                <text class="card-badge-text">{{ item.type === 'resource' ? '资源' : '回答' }}</text>
              </view>

              <text class="card-title">{{ item.title }}</text>

              <view v-if="item.type === 'answer' && item.questionTitle" class="card-sub-wrap">
                <text class="card-sub-prefix">问题：</text>
                <text class="card-sub">{{ item.questionTitle }}</text>
              </view>

              <view class="card-footer">
                <view class="card-likes">
                  <Heart :size="12" color="#F43F5E" fill="#F43F5E" />
                  <text class="card-likes-num">{{ fmtNum(item.likes) }}</text>
                  <text class="card-likes-lbl">点赞</text>
                </view>
                <ChevronRight :size="14" class="card-arrow" />
              </view>
            </view>
          </view>

          <view class="list-end">
            <template v-if="loadingMore">
              <view class="dot-row">
                <view class="dot dot-1" /><view class="dot dot-2" /><view class="dot dot-3" />
              </view>
            </template>
            <template v-else-if="noMore">
              <view class="end-line" /><text class="end-text">已加载全部</text><view class="end-line" />
            </template>
          </view>
        </template>

      </view>
    </scroll-view>

  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getLikedItems, type LikedItem } from '@/services/user'
import { Heart, FileText, MessageCircle, ChevronRight } from 'lucide-vue-next'
import { CNavBar } from '@/components/layout'

const loading     = ref(true)
const loadingMore = ref(false)
const noMore      = ref(false)
const page        = ref(1)
const list        = ref<LikedItem[]>([])
const activeTab   = ref<'all' | 'resource' | 'answer'>('all')

const fmtNum = (n: number) => n >= 1000 ? (n / 1000).toFixed(1) + 'k' : String(n)

const stats = computed(() => ({
  resourceLikes: list.value.filter(i => i.type === 'resource').reduce((s, i) => s + i.likes, 0),
  answerLikes:   list.value.filter(i => i.type === 'answer').reduce((s, i) => s + i.likes, 0),
}))

const totalLikes = computed(() => stats.value.resourceLikes + stats.value.answerLikes)

const tabs = computed(() => [
  { key: 'all'      as const, label: '全部', icon: Heart,         count: list.value.length },
  { key: 'resource' as const, label: '资源', icon: FileText,      count: list.value.filter(i => i.type === 'resource').length },
  { key: 'answer'   as const, label: '回答', icon: MessageCircle, count: list.value.filter(i => i.type === 'answer').length },
])

const filteredList = computed(() =>
  activeTab.value === 'all' ? list.value : list.value.filter(i => i.type === activeTab.value)
)

const fetchList = async (reset = false) => {
  if (reset) {
    page.value = 1; noMore.value = false; list.value = []; loading.value = true
  } else {
    loadingMore.value = true
  }
  try {
    const res = await getLikedItems(page.value, 20)
    if (res?.list) {
      list.value = reset ? res.list : [...list.value, ...res.list]
      noMore.value = list.value.length >= (res.total ?? 0)
    }
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false; loadingMore.value = false
  }
}

const loadMore = () => {
  if (loadingMore.value || noMore.value) return
  page.value++; fetchList()
}

const handleItemClick = (item: LikedItem) => {
  if (item.type === 'resource') {
    uni.navigateTo({ url: `/pages/resource/detail?id=${item.targetId}` })
  } else if (item.questionId) {
    uni.navigateTo({ url: `/pages/question/detail?id=${item.questionId}` })
  }
}

const handleBack = () => {
  uni.navigateBack({ fail: () => uni.switchTab({ url: '/pages/home/index' }) })
}

onMounted(() => fetchList(true))
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

$rose:      #F43F5E;
$rose-dark: #E11D48;
$blue:      #3B82F6;
$teal:      #0D9488;

// ═══════════════════════════════════════════
// 页面骨架
// ═══════════════════════════════════════════
.page {
  min-height: 100vh;
  background: $color-bg-page;
  display: flex;
  flex-direction: column;
}

.nav-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  background: $rose;
  border-radius: 100px;
  padding: 3px 10px 3px 7px;
}
.nav-badge-text {
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  line-height: 1;
}

.page-body {
  flex: 1;
  // #ifdef H5
  height: calc(100vh - 56px);
  overflow-y: auto;
  // #endif
}

// 内容居中容器
.center-wrap {
  width: 100%;
  max-width: 860px;
  margin: 0 auto;
  padding: 16px 16px 64px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 12px;

  @media (min-width: 768px) {
    padding: 28px 32px 80px;
    gap: 18px;
  }
}

// ═══════════════════════════════════════════
// Hero 汇总卡片
// ═══════════════════════════════════════════
.hero-card {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, $rose-dark 0%, $rose 60%, #FB7185 100%);
  border-radius: 20px;
  padding: 28px 16px;
  box-shadow: 0 8px 28px rgba($rose, 0.26);

  @media (min-width: 768px) {
    border-radius: 24px;
    padding: 36px 48px;
  }
}

.hero-deco {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;

  &--1 {
    width: 260px;
    height: 260px;
    top: -100px;
    right: -60px;
    background: radial-gradient(circle, rgba(255,255,255,0.12) 0%, transparent 65%);
  }
  &--2 {
    width: 160px;
    height: 160px;
    bottom: -70px;
    left: -40px;
    background: radial-gradient(circle, rgba(255,255,255,0.08) 0%, transparent 65%);
  }
}

.hero-stats {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-around;
}

.hero-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.hero-stat-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255,255,255,0.18);
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-stat-num {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  line-height: 1;
  letter-spacing: -0.5px;
}
.hero-stat-lbl {
  font-size: 11px;
  color: rgba(255,255,255,0.72);
  font-weight: 500;
}

.hero-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.hero-center-ring {
  width: 58px;
  height: 58px;
  border-radius: 50%;
  background: rgba(255,255,255,0.2);
  border: 2px solid rgba(255,255,255,0.32);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 14px rgba(0,0,0,0.1);
}
.hero-total-num {
  font-size: 36px;
  font-weight: 800;
  color: #fff;
  line-height: 1;
  letter-spacing: -1px;
}
.hero-total-lbl {
  font-size: 12px;
  color: rgba(255,255,255,0.78);
  font-weight: 500;
}

// ═══════════════════════════════════════════
// 筛选 Tab
// ═══════════════════════════════════════════
.tab-bar {
  display: flex;
  gap: 6px;
  background: $color-bg-card;
  border-radius: 16px;
  padding: 5px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.05);
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  padding: 9px 10px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.18s;
  color: $color-text-tertiary;

  // #ifdef H5
  &:hover:not(.active) {
    background: $color-bg-page;
    color: $color-text-secondary;
  }
  // #endif

  &.active {
    background: $rose;
    color: #fff;
    box-shadow: 0 3px 10px rgba($rose, 0.28);

    .tab-count {
      background: rgba(255,255,255,0.22);
      color: #fff;
    }
  }
}

.tab-label {
  font-size: 13px;
  font-weight: 600;
  line-height: 1;
}

.tab-count {
  font-size: 11px;
  font-weight: 700;
  background: $color-bg-page;
  color: $color-text-tertiary;
  border-radius: 100px;
  padding: 1px 6px;
  line-height: 1.5;
  transition: all 0.18s;
}

// ═══════════════════════════════════════════
// 列表
// ═══════════════════════════════════════════
.list-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 2px 0;
}
.list-header-line {
  flex: 1;
  height: 1px;
  background: $color-border-light;
}
.list-header-text {
  font-size: 12px;
  color: $color-text-quaternary;
  white-space: nowrap;
}

// 移动端单列，PC 端双列
.list-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;

  @media (min-width: 768px) {
    grid-template-columns: 1fr 1fr;
    gap: 14px;
  }
}

.list-card {
  background: $color-bg-card;
  border-radius: 16px;
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  cursor: pointer;
  border: 1.5px solid transparent;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  transition: all 0.18s;

  &:active { opacity: 0.72; }

  // #ifdef H5
  &:hover {
    border-color: rgba($rose, 0.18);
    box-shadow: 0 4px 20px rgba($rose, 0.1);
    transform: translateY(-1px);
  }
  // #endif
}

.card-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 8px;
  border-radius: 100px;
  align-self: flex-start;

  &--resource { background: #EFF6FF; color: $blue; }
  &--answer   { background: #F0FDFA; color: $teal; }
}
.card-badge-text {
  font-size: 11px;
  font-weight: 600;
  line-height: 1;
}

.card-title {
  font-size: 14px;
  font-weight: 500;
  color: $color-text-primary;
  line-height: 1.55;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-sub-wrap {
  display: flex;
  gap: 3px;
  overflow: hidden;
}
.card-sub-prefix {
  font-size: 12px;
  color: $color-text-quaternary;
  flex-shrink: 0;
}
.card-sub {
  font-size: 12px;
  color: $color-text-tertiary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 8px;
  border-top: 1px solid $color-border-light;
  margin-top: 2px;
}
.card-likes {
  display: flex;
  align-items: center;
  gap: 4px;
}
.card-likes-num {
  font-size: 13px;
  font-weight: 700;
  color: $rose;
  line-height: 1;
}
.card-likes-lbl {
  font-size: 12px;
  color: $color-text-quaternary;
}
.card-arrow {
  color: $color-text-quaternary;
}

// ─── 结尾 ───────────────────────────────
.list-end {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 16px 0 8px;
}
.end-line {
  flex: 1;
  max-width: 60px;
  height: 1px;
  background: $color-border-light;
}
.end-text {
  font-size: 12px;
  color: $color-text-quaternary;
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40%           { transform: scale(1);   opacity: 1; }
}
.dot-row { display: flex; gap: 5px; align-items: center; }
.dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: $rose;
  animation: bounce 1.2s ease-in-out infinite;
  &-1 { animation-delay: 0s; }
  &-2 { animation-delay: 0.2s; }
  &-3 { animation-delay: 0.4s; }
}

// ═══════════════════════════════════════════
// 空状态
// ═══════════════════════════════════════════
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50%       { transform: translateY(-6px); }
}
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 64px 32px 32px;
}
.empty-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #FFF1F2;
  border: 2px dashed #FFE4E6;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: float 3s ease-in-out infinite;
  margin-bottom: 4px;
}
.empty-title {
  font-size: 15px;
  font-weight: 600;
  color: $color-text-secondary;
}
.empty-desc {
  font-size: 13px;
  color: $color-text-tertiary;
  text-align: center;
  line-height: 1.7;
  max-width: 220px;
}

// ═══════════════════════════════════════════
// 骨架屏
// ═══════════════════════════════════════════
@keyframes shimmer {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
%sk-base {
  background: linear-gradient(90deg, #EDECEA 25%, #E0DFDB 50%, #EDECEA 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s ease-in-out infinite;
  border-radius: 8px;
}

.sk-hero {
  @extend %sk-base;
  height: 128px;
  border-radius: 20px;
}
.sk-tabs {
  @extend %sk-base;
  height: 50px;
  border-radius: 16px;
}
.sk-card {
  background: $color-bg-card;
  border-radius: 16px;
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}
.sk-line {
  @extend %sk-base;
  height: 14px;
  &--w80 { width: 82%; }
  &--w55 { width: 55%; }
  &--w30 { width: 30%; }
  &--sm  { height: 11px; }
}
</style>
