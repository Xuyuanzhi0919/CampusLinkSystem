<template>
  <view class="page">

    <!-- 状态栏占位 -->
    <view class="status-bar" />

    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="back-btn" @click="handleBack">
        <text class="back-arrow">←</text>
      </view>
      <view class="nav-title-row">
        <text class="nav-title">获赞</text>
        <text class="nav-heart">♥</text>
        <view v-if="totalLikes > 0" class="nav-badge">
          <text class="nav-badge-text">{{ totalLikes >= 1000 ? (totalLikes / 1000).toFixed(1) + 'k' : totalLikes }}</text>
        </view>
      </view>
    </view>

    <!-- 骨架屏 -->
    <scroll-view v-if="loading" class="scroll" scroll-y>
      <view class="content">
        <view class="sk-summary" />
        <view v-for="i in 4" :key="i" class="sk-card">
          <view class="sk-accent" />
          <view class="sk-inner">
            <view class="sk-icon" />
            <view class="sk-body">
              <view class="sk-tag" />
              <view class="sk-title" />
              <view class="sk-sub" />
            </view>
            <view class="sk-badge" />
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 空状态 -->
    <view v-else-if="list.length === 0" class="empty">
      <view class="empty-ring">
        <text class="empty-heart">♥</text>
      </view>
      <text class="empty-title">还没有内容被点赞</text>
      <text class="empty-desc">上传资源或回答问题，让更多同学发现你的价值</text>
    </view>

    <!-- 列表 -->
    <scroll-view
      v-else
      class="scroll"
      scroll-y
      :lower-threshold="80"
      @scrolltolower="loadMore"
    >
      <view class="content">

        <!-- 汇总卡片 -->
        <view class="summary-card">
          <view class="summary-col">
            <view class="summary-circle summary-circle--sm">
              <text class="summary-icon-text">≡</text>
            </view>
            <text class="summary-num">{{ stats.resourceLikes }}</text>
            <text class="summary-lbl">资源获赞</text>
          </view>
          <view class="summary-divider" />
          <view class="summary-col">
            <view class="summary-circle summary-circle--lg">
              <text class="summary-heart-text">♥</text>
            </view>
            <text class="summary-num summary-num--total">{{ totalLikes }}</text>
            <text class="summary-lbl">总获赞</text>
          </view>
          <view class="summary-divider" />
          <view class="summary-col">
            <view class="summary-circle summary-circle--sm">
              <text class="summary-icon-text">◎</text>
            </view>
            <text class="summary-num">{{ stats.answerLikes }}</text>
            <text class="summary-lbl">回答获赞</text>
          </view>
        </view>

        <!-- 分区标签 -->
        <view class="section-row">
          <view class="section-accent-line" />
          <text class="section-label">全部内容</text>
          <view class="section-count-pill">
            <text class="section-count-text">共 {{ list.length }} 条</text>
          </view>
        </view>

        <!-- 列表项 -->
        <view
          v-for="item in list"
          :key="`${item.type}-${item.targetId}`"
          class="list-card"
          @click="handleItemClick(item)"
        >
          <!-- 左侧 accent 线 -->
          <view class="card-accent" :class="`card-accent--${item.type}`" />
          <!-- 内容 -->
          <view class="card-inner">
            <view class="card-icon" :class="`card-icon--${item.type}`">
              <text :class="`card-icon-symbol card-icon-symbol--${item.type}`">
                {{ item.type === 'resource' ? '≡' : '◎' }}
              </text>
            </view>
            <view class="card-body">
              <view class="card-tag" :class="`card-tag--${item.type}`">
                <text class="card-tag-text">{{ item.type === 'resource' ? '资源' : '回答' }}</text>
              </view>
              <text class="card-title">{{ item.title }}</text>
              <text v-if="item.type === 'answer' && item.questionTitle" class="card-sub">
                {{ item.questionTitle }}
              </text>
            </view>
            <view class="card-like-badge">
              <text class="badge-heart">♥</text>
              <text class="badge-num">{{ item.likes }}</text>
            </view>
          </view>
        </view>

        <!-- 加载更多 / 结尾 -->
        <view v-if="loadingMore" class="end-row">
          <view class="dot-wrap">
            <view class="dot dot-1" /><view class="dot dot-2" /><view class="dot dot-3" />
          </view>
        </view>
        <view v-else-if="noMore && list.length > 0" class="end-row">
          <view class="end-line" />
          <text class="end-text">已加载全部</text>
          <view class="end-line" />
        </view>

      </view>
    </scroll-view>

  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getLikedItems, type LikedItem } from '@/services/user'

const loading     = ref(true)
const loadingMore = ref(false)
const noMore      = ref(false)
const page        = ref(1)
const list        = ref<LikedItem[]>([])

const stats = computed(() => ({
  resourceLikes: list.value.filter(i => i.type === 'resource').reduce((s, i) => s + i.likes, 0),
  answerLikes:   list.value.filter(i => i.type === 'answer').reduce((s, i) => s + i.likes, 0),
}))

const totalLikes = computed(() => stats.value.resourceLikes + stats.value.answerLikes)

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
// ─── 设计令牌（与 Pencil 稿对齐）────────────────────────────
$bg:          #F5F4F1;
$white:       #FFFFFF;
$rose:        #F43F5E;
$rose-dark:   #E11D48;
$rose-50:     #FFF1F2;
$rose-100:    #FFE4E6;
$blue:        #3B82F6;
$blue-50:     #EFF6FF;
$teal:        #0D9488;
$teal-50:     #F0FDFA;
$text-1:      #1A1918;
$text-2:      #6D6C6A;
$text-3:      #9C9B99;
$text-4:      #A8A7A5;
$border:      #EDEDEB;
$muted-bg:    #EDECEA;
$muted-fill:  #F0EFEC;

// ─── 页面 ────────────────────────────────────────────────────
.page {
  min-height: 100vh;
  background: $bg;
  display: flex;
  flex-direction: column;
}

// ─── 状态栏 ──────────────────────────────────────────────────
.status-bar {
  // #ifdef H5
  display: none;
  // #endif
  height: var(--status-bar-height, 0px);
  background: $bg;
}

// ─── 导航栏 ──────────────────────────────────────────────────
.nav-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  height: 56px;
  background: $white;
  border-bottom: 1px solid $border;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 20px 0 16px;
  // #ifdef H5
  top: 0;
  // #endif
}

.back-btn {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: $muted-fill;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  cursor: pointer;
  transition: opacity 0.12s;
  &:active { opacity: 0.6; }
}
.back-arrow {
  font-size: 17px;
  font-weight: 500;
  color: $text-2;
  line-height: 1;
}

.nav-title-row {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
}
.nav-title {
  font-size: 18px;
  font-weight: 700;
  color: $text-1;
  font-family: 'Outfit', sans-serif;
  letter-spacing: -0.3px;
}
.nav-heart {
  font-size: 15px;
  color: $rose;
  line-height: 1;
}
.nav-badge {
  background: $rose;
  border-radius: 100px;
  padding: 2px 9px;
}
.nav-badge-text {
  font-size: 12px;
  font-weight: 700;
  color: $white;
  font-family: 'Outfit', sans-serif;
}

// ─── 滚动区 ──────────────────────────────────────────────────
.scroll {
  flex: 1;
  overflow: hidden;
  // #ifdef H5
  height: calc(100vh - 56px);
  overflow-y: auto;
  // #endif
}

.content {
  max-width: 480px;
  margin: 0 auto;
  padding: 16px 16px 48px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

// ─── 汇总卡片 ────────────────────────────────────────────────
.summary-card {
  background: $rose;
  border-radius: 20px;
  height: 118px;
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 0 4px;
}

.summary-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.summary-circle {
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.22);
  display: flex;
  align-items: center;
  justify-content: center;

  &--sm { width: 34px; height: 34px; }
  &--lg { width: 42px; height: 42px; background: rgba(255, 255, 255, 0.28); }
}

.summary-icon-text {
  font-size: 16px;
  font-weight: 700;
  color: $white;
  line-height: 1;
}
.summary-heart-text {
  font-size: 20px;
  color: $white;
  line-height: 1;
}

.summary-num {
  font-size: 22px;
  font-weight: 700;
  color: $white;
  font-family: 'Outfit', sans-serif;
  letter-spacing: -0.8px;
  line-height: 1;

  &--total {
    font-size: 28px;
    letter-spacing: -1px;
  }
}
.summary-lbl {
  font-size: 11px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.78);
  font-family: 'Outfit', sans-serif;
}
.summary-divider {
  width: 1px;
  height: 46px;
  background: rgba(255, 255, 255, 0.28);
  flex-shrink: 0;
}

// ─── 分区标签 ────────────────────────────────────────────────
.section-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0 2px;
}
.section-accent-line {
  width: 3px;
  height: 15px;
  border-radius: 2px;
  background: $rose;
  flex-shrink: 0;
}
.section-label {
  flex: 1;
  font-size: 13px;
  font-weight: 700;
  color: $text-2;
  font-family: 'Outfit', sans-serif;
}
.section-count-pill {
  background: $muted-bg;
  border-radius: 100px;
  padding: 2px 8px;
}
.section-count-text {
  font-size: 11px;
  font-weight: 600;
  color: $text-3;
  font-family: 'Outfit', sans-serif;
}

// ─── 列表项 ──────────────────────────────────────────────────
.list-card {
  background: $white;
  border-radius: 16px;
  display: flex;
  align-items: stretch;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05), 0 2px 8px rgba(0, 0, 0, 0.03);
  cursor: pointer;
  transition: transform 0.12s ease, box-shadow 0.12s ease;

  &:active {
    transform: scale(0.985);
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  }
  // #ifdef H5
  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.09);
  }
  // #endif
}

.card-accent {
  width: 3px;
  flex-shrink: 0;
  &--resource { background: $blue; }
  &--answer   { background: $teal; }
}

.card-inner {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 12px;
  min-width: 0;
}

.card-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  &--resource { background: $blue-50; }
  &--answer   { background: $teal-50; }
}
.card-icon-symbol {
  font-size: 18px;
  font-weight: 700;
  line-height: 1;
  &--resource { color: $blue; }
  &--answer   { color: $teal; }
}

.card-body {
  flex: 1;
  min-width: 0;
  flex-shrink: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow: hidden;
}

.card-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 18px;
  border-radius: 4px;
  padding: 0 7px;
  width: fit-content;
  &--resource { background: $blue-50; }
  &--answer   { background: $teal-50; }
}
.card-tag--resource .card-tag-text { color: $blue; }
.card-tag--answer   .card-tag-text { color: $teal; }

.card-tag-text {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.4px;
  font-family: 'Outfit', sans-serif;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: $text-1;
  font-family: 'Outfit', sans-serif;
  line-height: 1.5;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.card-sub {
  font-size: 11px;
  color: $text-3;
  font-family: 'Outfit', sans-serif;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-like-badge {
  width: 44px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  background: $rose-50;
  border: 1px solid $rose-100;
  border-radius: 10px;
  padding: 8px 0;
}
.badge-heart {
  font-size: 13px;
  color: $rose;
  line-height: 1;
}
.badge-num {
  font-size: 13px;
  font-weight: 700;
  color: $rose-dark;
  font-family: 'Outfit', sans-serif;
  line-height: 1;
}

// ─── 结尾行 ──────────────────────────────────────────────────
.end-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 12px 0 6px;
}
.end-line {
  flex: 1;
  height: 1px;
  background: #E5E4E1;
  max-width: 72px;
}
.end-text {
  font-size: 12px;
  color: $text-4;
  font-family: 'Outfit', sans-serif;
}

// 加载中小点
@keyframes bounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40%           { transform: scale(1);   opacity: 1; }
}
.dot-wrap {
  display: flex;
  gap: 4px;
  align-items: center;
}
.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: $rose;
  animation: bounce 1.2s ease-in-out infinite;
  &-1 { animation-delay: 0s; }
  &-2 { animation-delay: 0.2s; }
  &-3 { animation-delay: 0.4s; }
}

// ─── 空状态 ──────────────────────────────────────────────────
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50%       { transform: translateY(-6px); }
}
.empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  padding: 80px 32px;
}
.empty-ring {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  background: $rose-50;
  border: 2px dashed $rose-100;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: float 3s ease-in-out infinite;
  margin-bottom: 4px;
}
.empty-heart {
  font-size: 32px;
  color: #FDA4AF;
  line-height: 1;
}
.empty-title {
  font-size: 16px;
  font-weight: 700;
  color: #6D6C6A;
  font-family: 'Outfit', sans-serif;
}
.empty-desc {
  font-size: 13px;
  color: $text-3;
  text-align: center;
  line-height: 1.7;
  max-width: 240px;
  font-family: 'Outfit', sans-serif;
}

// ─── 骨架屏 ──────────────────────────────────────────────────
@keyframes shimmer {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
%sk {
  background: linear-gradient(90deg, #EDECEA 25%, #E0DFDB 50%, #EDECEA 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s ease-in-out infinite;
  border-radius: 6px;
}

.sk-summary {
  @extend %sk;
  height: 118px;
  border-radius: 20px;
}

.sk-card {
  background: $white;
  border-radius: 16px;
  display: flex;
  align-items: stretch;
  overflow: hidden;
}
.sk-accent {
  @extend %sk;
  width: 3px;
  border-radius: 0;
}
.sk-inner {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 12px;
}
.sk-icon {
  @extend %sk;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  flex-shrink: 0;
}
.sk-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 7px;
}
.sk-tag   { @extend %sk; width: 36px; height: 18px; border-radius: 4px; }
.sk-title { @extend %sk; width: 80%; height: 14px; }
.sk-sub   { @extend %sk; width: 55%; height: 11px; }
.sk-badge {
  @extend %sk;
  width: 44px;
  height: 50px;
  border-radius: 10px;
  flex-shrink: 0;
}
</style>
