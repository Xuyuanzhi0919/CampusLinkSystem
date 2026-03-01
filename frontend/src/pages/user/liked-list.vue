<template>
  <view class="liked-list-page">

    <!-- 导航栏 -->
    <view class="page-header">
      <view class="header-inner">
        <view class="back-btn" @click="handleBack">
          <svg viewBox="0 0 24 24" fill="none">
            <path d="M19 12H5M5 12L12 19M5 12L12 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </view>
        <text class="header-title">获赞</text>
        <view class="header-badge" v-if="totalLikes > 0">
          <text class="header-badge-text">{{ totalLikes >= 1000 ? (totalLikes / 1000).toFixed(1) + 'k' : totalLikes }}</text>
        </view>
      </view>
    </view>

    <!-- 骨架屏 -->
    <view v-if="loading" class="skeleton-wrap">
      <view v-for="i in 5" :key="i" class="sk-item">
        <view class="sk-icon" />
        <view class="sk-content">
          <view class="sk-line sk-line--title" />
          <view class="sk-line sk-line--sub" />
        </view>
        <view class="sk-num" />
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else-if="!loading && list.length === 0" class="empty-wrap">
      <view class="empty-icon">
        <svg viewBox="0 0 64 64" fill="none">
          <circle cx="32" cy="32" r="28" stroke="#E2E8F0" stroke-width="2"/>
          <path d="M32 20C25.373 20 20 25.373 20 32s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12z" stroke="#CBD5E1" stroke-width="2"/>
          <path d="M27 29l3 3 7-7" stroke="#CBD5E1" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </view>
      <text class="empty-title">还没有内容被点赞</text>
      <text class="empty-desc">上传资源或回答问题，让更多同学发现你的价值</text>
    </view>

    <!-- 列表 -->
    <scroll-view
      v-else
      class="list-scroll"
      scroll-y
      :lower-threshold="80"
      @scrolltolower="loadMore"
    >
      <view class="list-wrap">

        <!-- 汇总卡片 -->
        <view class="summary-card">
          <view class="summary-item">
            <text class="summary-num">{{ stats.resourceLikes }}</text>
            <text class="summary-lbl">资源获赞</text>
          </view>
          <view class="summary-divider" />
          <view class="summary-item">
            <text class="summary-num">{{ stats.answerLikes }}</text>
            <text class="summary-lbl">回答获赞</text>
          </view>
          <view class="summary-divider" />
          <view class="summary-item">
            <text class="summary-num">{{ totalLikes }}</text>
            <text class="summary-lbl">总获赞</text>
          </view>
        </view>

        <!-- 内容列表 -->
        <view class="section-label">
          <text class="section-label-text">全部内容</text>
        </view>

        <view
          v-for="item in list"
          :key="`${item.type}-${item.targetId}`"
          class="list-item"
          @click="handleItemClick(item)"
        >
          <!-- 类型图标 -->
          <view class="item-icon-wrap" :class="item.type === 'resource' ? 'item-icon-wrap--blue' : 'item-icon-wrap--teal'">
            <svg v-if="item.type === 'resource'" viewBox="0 0 24 24" fill="none">
              <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M14 2V8H20M16 13H8M16 17H8M10 9H8" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <svg v-else viewBox="0 0 24 24" fill="none">
              <path d="M21 15C21 15.5304 20.7893 16.0391 20.4142 16.4142C20.0391 16.7893 19.5304 17 19 17H7L3 21V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H19C19.5304 3 20.0391 3.21071 20.4142 3.58579C20.7893 3.96086 21 4.46957 21 5V15Z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </view>

          <!-- 内容 -->
          <view class="item-content">
            <view class="item-type-tag" :class="item.type === 'resource' ? 'item-type-tag--resource' : 'item-type-tag--answer'">
              <text>{{ item.type === 'resource' ? '资源' : '回答' }}</text>
            </view>
            <text class="item-title">{{ item.title }}</text>
            <text v-if="item.type === 'answer' && item.questionTitle" class="item-sub">
              问题：{{ item.questionTitle }}
            </text>
          </view>

          <!-- 点赞数 -->
          <view class="item-likes">
            <svg viewBox="0 0 24 24" fill="currentColor" class="likes-heart">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
            </svg>
            <text class="likes-num">{{ item.likes }}</text>
          </view>
        </view>

        <!-- 加载更多 -->
        <view v-if="loadingMore" class="load-more">
          <text class="load-more-text">加载中...</text>
        </view>
        <view v-else-if="noMore && list.length > 0" class="load-more">
          <text class="load-more-text">已加载全部</text>
        </view>
      </view>
    </scroll-view>

  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getLikedItems, type LikedItem } from '@/services/user'

const loading  = ref(true)
const loadingMore = ref(false)
const noMore   = ref(false)
const page     = ref(1)
const list     = ref<LikedItem[]>([])

const stats = computed(() => ({
  resourceLikes: list.value.filter(i => i.type === 'resource').reduce((s, i) => s + i.likes, 0),
  answerLikes:   list.value.filter(i => i.type === 'answer').reduce((s, i) => s + i.likes, 0),
}))

const totalLikes = computed(() => stats.value.resourceLikes + stats.value.answerLikes)

const fetchList = async (reset = false) => {
  if (reset) {
    page.value = 1
    noMore.value = false
    list.value = []
    loading.value = true
  } else {
    loadingMore.value = true
  }
  try {
    const res = await getLikedItems(page.value, 20)
    if (res && res.list) {
      list.value = reset ? res.list : [...list.value, ...res.list]
      noMore.value = list.value.length >= (res.total ?? 0)
    }
  } catch (err) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const loadMore = () => {
  if (loadingMore.value || noMore.value) return
  page.value++
  fetchList()
}

const handleItemClick = (item: LikedItem) => {
  if (item.type === 'resource') {
    uni.navigateTo({ url: `/pages/resource/detail?id=${item.targetId}` })
  } else {
    const qid = item.questionId
    if (qid) uni.navigateTo({ url: `/pages/question/detail?id=${qid}` })
  }
}

const handleBack = () => {
  uni.navigateBack({ fail: () => uni.switchTab({ url: '/pages/home/index' }) })
}

onMounted(() => fetchList(true))
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

.liked-list-page {
  min-height: 100vh;
  background: $gray-50;
  display: flex;
  flex-direction: column;
}

// ========== 导航栏 ==========
.page-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: $white;
  border-bottom: 1px solid $border-light;
  box-shadow: 0 2px 8px rgba($black, 0.04);
}

.header-inner {
  max-width: 720px;
  margin: 0 auto;
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.back-btn {
  width: 36px;
  height: 36px;
  border-radius: $radius-sm;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $text-secondary;
  cursor: pointer;
  flex-shrink: 0;
  transition: $transition-fast;

  svg { width: 20px; height: 20px; }
  &:hover { background: $gray-100; color: $text-primary; }
  &:active { transform: scale(0.95); }
}

.header-title {
  font-size: 18px;
  font-weight: $font-weight-semibold;
  color: $text-primary;
}

.header-badge {
  background: $error;
  border-radius: 100px;
  padding: 2px 10px;
  margin-left: 4px;
}

.header-badge-text {
  font-size: 13px;
  font-weight: 600;
  color: $white;
}

// ========== 主体 ==========
.list-scroll {
  flex: 1;
  height: calc(100vh - 60px);
}

.list-wrap {
  max-width: 720px;
  margin: 0 auto;
  padding: 20px 16px 48px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

// ========== 汇总卡片 ==========
.summary-card {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: $white;
  border-radius: $radius-xl;
  padding: 20px 16px;
  box-shadow: 0 2px 12px rgba($black, 0.06);
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  flex: 1;
}

.summary-num {
  font-size: 22px;
  font-weight: $font-weight-bold;
  color: $error;
}

.summary-lbl {
  font-size: 12px;
  color: $text-tertiary;
}

.summary-divider {
  width: 1px;
  height: 32px;
  background: $border-light;
}

// ========== 分区标签 ==========
.section-label {
  padding: 4px 2px 0;
}

.section-label-text {
  font-size: 13px;
  font-weight: 600;
  color: $text-tertiary;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

// ========== 列表项 ==========
.list-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  background: $white;
  border-radius: $radius-xl;
  padding: 16px;
  box-shadow: 0 2px 8px rgba($black, 0.04);
  cursor: pointer;
  transition: $transition-fast;

  &:active { background: $gray-50; transform: scale(0.99); }

  // #ifdef H5
  &:hover { background: $gray-50; }
  // #endif
}

.item-icon-wrap {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  svg { width: 20px; height: 20px; }

  &--blue { background: #EBF3FF; color: #3B82F6; }
  &--teal { background: #ECFDF5; color: #0D9488; }
}

.item-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.item-type-tag {
  display: inline-flex;
  align-items: center;
  padding: 1px 8px;
  border-radius: 4px;
  width: fit-content;

  text {
    font-size: 11px;
    font-weight: 600;
    letter-spacing: 0.02em;
  }

  &--resource { background: #EBF3FF; text { color: #3B82F6; } }
  &--answer   { background: #ECFDF5; text { color: #0D9488; } }
}

.item-title {
  font-size: 14px;
  font-weight: $font-weight-medium;
  color: $text-primary;
  line-height: 1.5;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.item-sub {
  font-size: 12px;
  color: $text-tertiary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-likes {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  flex-shrink: 0;
  padding-top: 4px;
}

.likes-heart {
  width: 16px;
  height: 16px;
  color: $error;
}

.likes-num {
  font-size: 13px;
  font-weight: 600;
  color: $error;
}

// ========== 加载更多 ==========
.load-more {
  padding: 16px 0;
  display: flex;
  justify-content: center;
}

.load-more-text {
  font-size: 13px;
  color: $text-quaternary;
}

// ========== 骨架屏 ==========
@keyframes shimmer {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

%shimmer {
  background: linear-gradient(90deg, rgba(0,0,0,0.04) 25%, rgba(0,0,0,0.08) 50%, rgba(0,0,0,0.04) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s ease-in-out infinite;
  border-radius: 6px;
}

.skeleton-wrap {
  max-width: 720px;
  margin: 0 auto;
  padding: 20px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sk-item {
  display: flex;
  align-items: center;
  gap: 12px;
  background: $white;
  border-radius: $radius-xl;
  padding: 16px;
}

.sk-icon {
  @extend %shimmer;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  flex-shrink: 0;
}

.sk-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sk-line {
  @extend %shimmer;
  height: 14px;

  &--title { width: 75%; }
  &--sub   { width: 50%; height: 11px; }
}

.sk-num {
  @extend %shimmer;
  width: 28px;
  height: 36px;
  border-radius: 8px;
}

// ========== 空状态 ==========
.empty-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 32px;
  gap: 12px;
}

.empty-icon svg {
  width: 72px;
  height: 72px;
}

.empty-title {
  font-size: 16px;
  font-weight: $font-weight-semibold;
  color: $text-secondary;
}

.empty-desc {
  font-size: 14px;
  color: $text-tertiary;
  text-align: center;
  line-height: 1.6;
}
</style>
