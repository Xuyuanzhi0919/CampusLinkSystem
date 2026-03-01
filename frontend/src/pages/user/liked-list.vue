<template>
  <view class="page">

    <!-- 导航栏 -->
    <CNavBar title-align="left" :auto-back="false" @back="handleBack">
      <template #title>
        <text class="nav-title">获赞</text>
        <Heart :size="16" :stroke-width="2" color="#F43F5E" fill="#F43F5E" />
        <view v-if="totalLikes > 0" class="nav-badge">
          <text class="nav-badge-text">{{ totalLikes >= 1000 ? (totalLikes / 1000).toFixed(1) + 'k' : totalLikes }}</text>
        </view>
      </template>
    </CNavBar>

    <!-- 骨架屏 -->
    <scroll-view v-if="loading" class="scroll" scroll-y>
      <view class="content">
        <view class="sk-summary" />
        <view v-for="i in 4" :key="i" class="sk-card">
          <view class="sk-title-line" />
          <view class="sk-title-line sk-title-line--short" />
          <view class="sk-meta-line" />
        </view>
      </view>
    </scroll-view>

    <!-- 空状态 -->
    <view v-else-if="list.length === 0" class="empty">
      <view class="empty-ring">
        <Heart :size="34" :stroke-width="1.5" color="#FDA4AF" />
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
              <FileText :size="16" :stroke-width="2" color="white" />
            </view>
            <text class="summary-num">{{ stats.resourceLikes }}</text>
            <text class="summary-lbl">资源获赞</text>
          </view>
          <view class="summary-divider" />
          <view class="summary-col">
            <view class="summary-circle summary-circle--lg">
              <Heart :size="22" :stroke-width="2" color="white" fill="white" />
            </view>
            <text class="summary-num summary-num--total">{{ totalLikes }}</text>
            <text class="summary-lbl">总获赞</text>
          </view>
          <view class="summary-divider" />
          <view class="summary-col">
            <view class="summary-circle summary-circle--sm">
              <MessageCircle :size="16" :stroke-width="2" color="white" />
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
          <text class="card-title">{{ item.title }}</text>
          <text v-if="item.type === 'answer' && item.questionTitle" class="card-sub">
            {{ item.questionTitle }}
          </text>
          <view class="card-meta">
            <text class="card-meta-type" :class="`card-meta-type--${item.type}`">
              {{ item.type === 'resource' ? '资源' : '回答' }}
            </text>
            <text class="card-meta-dot">·</text>
            <view class="card-meta-likes">
              <Heart :size="12" :stroke-width="2.5" color="#F43F5E" fill="#F43F5E" />
              <text class="card-meta-likes-count">{{ item.likes }}</text>
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
import { Heart, FileText, MessageCircle } from 'lucide-vue-next'
import { CNavBar } from '@/components/layout'

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

// ─── 导航栏 title slot 内容 ──────────────────────────────────
.nav-title {
  font-size: 18px;
  font-weight: 700;
  color: $text-1;
  font-family: 'Outfit', sans-serif;
  letter-spacing: -0.3px;
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
  border-radius: 14px;
  padding: 14px 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 5px;
  cursor: pointer;
  transition: opacity 0.12s;

  &:active { opacity: 0.7; }
  // #ifdef H5
  &:hover { background: #FAFAF8; }
  // #endif
}

.card-title {
  font-size: 14px;
  font-weight: 500;
  color: $text-1;
  line-height: 1.55;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-sub {
  font-size: 12px;
  color: $text-3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-top: 2px;
}

.card-meta-type {
  font-size: 12px;
  font-weight: 500;
  &--resource { color: $blue; }
  &--answer   { color: $teal; }
}

.card-meta-dot {
  font-size: 12px;
  color: $text-4;
  line-height: 1;
}

.card-meta-likes {
  display: flex;
  align-items: center;
  gap: 3px;
}
.card-meta-likes-count {
  font-size: 12px;
  color: $rose;
  font-weight: 500;
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
  border-radius: 14px;
  padding: 14px 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sk-title-line {
  @extend %sk;
  height: 14px;
  width: 90%;

  &--short { width: 60%; height: 14px; }
}

.sk-meta-line {
  @extend %sk;
  height: 12px;
  width: 30%;
  margin-top: 2px;
}
</style>
