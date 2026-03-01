<template>
  <view class="liked-list-page">

    <!-- 导航栏 -->
    <view class="page-header">
      <view class="status-bar-placeholder" />
      <view class="header-inner">
        <view class="back-btn" @click="handleBack">
          <svg viewBox="0 0 24 24" fill="none">
            <path d="M19 12H5M5 12L12 19M5 12L12 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </view>
        <view class="header-title-wrap">
          <text class="header-title">获赞</text>
          <view class="header-heart-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
            </svg>
          </view>
          <view v-if="totalLikes > 0" class="header-badge">
            <text class="header-badge-text">{{ totalLikes >= 1000 ? (totalLikes / 1000).toFixed(1) + 'k' : totalLikes }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 骨架屏 -->
    <view v-if="loading" class="skeleton-wrap">
      <view class="sk-summary" />
      <view v-for="i in 4" :key="i" class="sk-item">
        <view class="sk-icon" />
        <view class="sk-content">
          <view class="sk-line sk-line--tag" />
          <view class="sk-line sk-line--title" />
          <view class="sk-line sk-line--sub" />
        </view>
        <view class="sk-num" />
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else-if="!loading && list.length === 0" class="empty-wrap">
      <view class="empty-icon-wrap">
        <view class="empty-icon-ring">
          <svg viewBox="0 0 24 24" fill="none" class="empty-heart-svg">
            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" stroke="#FDA4AF" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </view>
        <!-- 装饰小点 -->
        <view class="empty-dot empty-dot--tl" />
        <view class="empty-dot empty-dot--tr" />
        <view class="empty-dot empty-dot--bl" />
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
          <view class="summary-bg-blur" />
          <view class="summary-row">
            <view class="summary-item">
              <view class="summary-icon summary-icon--resource">
                <svg viewBox="0 0 24 24" fill="none">
                  <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M14 2V8H20M16 13H8M16 17H8M10 9H8" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </view>
              <text class="summary-num">{{ stats.resourceLikes }}</text>
              <text class="summary-lbl">资源获赞</text>
            </view>

            <view class="summary-divider" />

            <view class="summary-item summary-item--center">
              <view class="summary-heart-wrap">
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                </svg>
              </view>
              <text class="summary-num summary-num--total">{{ totalLikes }}</text>
              <text class="summary-lbl">总获赞</text>
            </view>

            <view class="summary-divider" />

            <view class="summary-item">
              <view class="summary-icon summary-icon--answer">
                <svg viewBox="0 0 24 24" fill="none">
                  <path d="M21 15C21 15.5304 20.7893 16.0391 20.4142 16.4142C20.0391 16.7893 19.5304 17 19 17H7L3 21V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H19C19.5304 3 20.0391 3.21071 20.4142 3.58579C20.7893 3.96086 21 4.46957 21 5V15Z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </view>
              <text class="summary-num">{{ stats.answerLikes }}</text>
              <text class="summary-lbl">回答获赞</text>
            </view>
          </view>
        </view>

        <!-- 分区标签 -->
        <view class="section-label">
          <view class="section-label-line" />
          <text class="section-label-text">全部内容</text>
          <view class="section-label-count">
            <text class="section-label-count-text">{{ list.length }} 条</text>
          </view>
        </view>

        <!-- 内容列表 -->
        <view
          v-for="item in list"
          :key="`${item.type}-${item.targetId}`"
          class="list-item"
          :class="`list-item--${item.type}`"
          @click="handleItemClick(item)"
        >
          <!-- 类型图标 -->
          <view class="item-icon-wrap" :class="`item-icon-wrap--${item.type}`">
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
            <view class="item-type-tag" :class="`item-type-tag--${item.type}`">
              <text>{{ item.type === 'resource' ? '资源' : '回答' }}</text>
            </view>
            <text class="item-title">{{ item.title }}</text>
            <text v-if="item.type === 'answer' && item.questionTitle" class="item-sub">
              {{ item.questionTitle }}
            </text>
          </view>

          <!-- 点赞数角标 -->
          <view class="item-likes-badge">
            <svg viewBox="0 0 24 24" fill="currentColor" class="likes-heart">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
            </svg>
            <text class="likes-num">{{ item.likes }}</text>
          </view>
        </view>

        <!-- 加载更多 -->
        <view v-if="loadingMore" class="load-more">
          <view class="load-more-dots">
            <view class="dot dot--1" /><view class="dot dot--2" /><view class="dot dot--3" />
          </view>
        </view>
        <view v-else-if="noMore && list.length > 0" class="load-more">
          <view class="load-more-divider" />
          <text class="load-more-text">已加载全部</text>
          <view class="load-more-divider" />
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

// ============================================================
//  色彩语义（本页专用）
// ============================================================
$rose-50:  #FFF1F2;
$rose-100: #FFE4E6;
$rose-400: #FB7185;
$rose-500: #F43F5E;
$rose-600: #E11D48;
$blue-50:  #EFF6FF;
$blue-100: #DBEAFE;
$blue-500: #3B82F6;
$teal-50:  #F0FDFA;
$teal-100: #CCFBF1;
$teal-600: #0D9488;

.liked-list-page {
  min-height: 100vh;
  background: #F8FAFC;
  display: flex;
  flex-direction: column;
}

// ============================================================
//  导航栏
// ============================================================
.status-bar-placeholder {
  // #ifdef H5
  display: none;
  // #endif
  height: var(--status-bar-height, 0px);
}

.page-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.header-inner {
  max-width: 720px;
  margin: 0 auto;
  padding: 0 16px;
  height: 56px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.back-btn {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $text-secondary;
  cursor: pointer;
  flex-shrink: 0;
  transition: background 0.15s, color 0.15s;
  svg { width: 20px; height: 20px; }
  &:active { background: $gray-100; }
  // #ifdef H5
  &:hover { background: $gray-100; color: $text-primary; }
  // #endif
}

.header-title-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  color: $text-primary;
  letter-spacing: -0.2px;
}

.header-heart-icon {
  width: 18px;
  height: 18px;
  color: $rose-500;
  svg { width: 100%; height: 100%; }
}

.header-badge {
  background: $rose-500;
  border-radius: 100px;
  padding: 2px 8px;
  margin-left: 2px;
}
.header-badge-text {
  font-size: 12px;
  font-weight: 700;
  color: #fff;
}

// ============================================================
//  主滚动区
// ============================================================
.list-scroll {
  flex: 1;
  height: calc(100vh - 56px);
}

.list-wrap {
  max-width: 720px;
  margin: 0 auto;
  padding: 16px 14px 60px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

// ============================================================
//  汇总卡片
// ============================================================
.summary-card {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  background: linear-gradient(135deg, #FF6B8A 0%, #FF4F7B 40%, #E91E8C 100%);
  padding: 24px 16px 20px;
  box-shadow: 0 8px 24px rgba(244, 63, 94, 0.35);
}

.summary-bg-blur {
  position: absolute;
  inset: 0;
  // 装饰光晕
  &::before {
    content: '';
    position: absolute;
    width: 120px;
    height: 120px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.12);
    top: -30px;
    right: -20px;
  }
  &::after {
    content: '';
    position: absolute;
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.08);
    bottom: -20px;
    left: 20px;
  }
}

.summary-row {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-around;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  flex: 1;

  &--center {
    .summary-num { font-size: 28px; }
  }
}

.summary-icon {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 2px;
  svg { width: 16px; height: 16px; }

  &--resource { background: rgba(255,255,255,0.22); color: #fff; }
  &--answer   { background: rgba(255,255,255,0.22); color: #fff; }
}

.summary-heart-wrap {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 2px;
  svg {
    width: 20px;
    height: 20px;
    color: #fff;
    filter: drop-shadow(0 2px 4px rgba(0,0,0,0.2));
  }
}

.summary-num {
  font-size: 22px;
  font-weight: 800;
  color: #fff;
  letter-spacing: -0.5px;
  line-height: 1;

  &--total {
    font-size: 28px;
    text-shadow: 0 2px 8px rgba(0,0,0,0.15);
  }
}

.summary-lbl {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 500;
}

.summary-divider {
  width: 1px;
  height: 40px;
  background: rgba(255, 255, 255, 0.25);
}

// ============================================================
//  分区标签
// ============================================================
.section-label {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 2px 2px;
}

.section-label-line {
  width: 3px;
  height: 14px;
  border-radius: 2px;
  background: $rose-500;
}

.section-label-text {
  font-size: 13px;
  font-weight: 700;
  color: $text-secondary;
  flex: 1;
}

.section-label-count {
  background: $gray-100;
  border-radius: 100px;
  padding: 2px 8px;
}
.section-label-count-text {
  font-size: 11px;
  color: $text-tertiary;
  font-weight: 600;
}

// ============================================================
//  列表项
// ============================================================
.list-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  background: $white;
  border-radius: 16px;
  padding: 14px 12px 14px 14px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05), 0 2px 8px rgba(0,0,0,0.03);
  cursor: pointer;
  transition: transform 0.12s ease, box-shadow 0.12s ease;
  border-left: 3px solid transparent;

  &--resource { border-left-color: $blue-500; }
  &--answer   { border-left-color: $teal-600; }

  &:active {
    transform: scale(0.985);
    box-shadow: 0 1px 2px rgba(0,0,0,0.04);
  }
  // #ifdef H5
  &:hover {
    box-shadow: 0 4px 16px rgba(0,0,0,0.09);
    transform: translateY(-1px);
  }
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
  svg { width: 18px; height: 18px; }

  &--resource { background: $blue-50; color: $blue-500; }
  &--answer   { background: $teal-50; color: $teal-600; }
}

.item-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-type-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 7px;
  border-radius: 4px;
  width: fit-content;

  text {
    font-size: 10px;
    font-weight: 700;
    letter-spacing: 0.05em;
  }

  &--resource { background: $blue-50; text { color: $blue-500; } }
  &--answer   { background: $teal-50; text { color: $teal-600; } }
}

.item-title {
  font-size: 14px;
  font-weight: 600;
  color: $text-primary;
  line-height: 1.5;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.item-sub {
  font-size: 11px;
  color: $text-tertiary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  &::before {
    content: '问题：';
    font-weight: 500;
  }
}

// 点赞数角标
.item-likes-badge {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  flex-shrink: 0;
  background: $rose-50;
  border: 1px solid $rose-100;
  border-radius: 10px;
  padding: 6px 10px;
  min-width: 44px;
}

.likes-heart {
  width: 14px;
  height: 14px;
  color: $rose-500;
}

.likes-num {
  font-size: 13px;
  font-weight: 700;
  color: $rose-600;
  line-height: 1;
}

// ============================================================
//  加载更多
// ============================================================
.load-more {
  padding: 20px 0 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.load-more-text {
  font-size: 12px;
  color: $text-quaternary;
}

.load-more-divider {
  flex: 1;
  height: 1px;
  background: $border-light;
  max-width: 60px;
}

// 加载中小点动画
@keyframes bounce-dot {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40%           { transform: scale(1);   opacity: 1; }
}

.load-more-dots {
  display: flex;
  gap: 4px;
  align-items: center;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: $rose-400;
  animation: bounce-dot 1.2s ease-in-out infinite;

  &--1 { animation-delay: 0s; }
  &--2 { animation-delay: 0.2s; }
  &--3 { animation-delay: 0.4s; }
}

// ============================================================
//  骨架屏
// ============================================================
@keyframes shimmer {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

%shimmer {
  background: linear-gradient(90deg, #F1F5F9 25%, #E2E8F0 50%, #F1F5F9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s ease-in-out infinite;
  border-radius: 6px;
}

.skeleton-wrap {
  max-width: 720px;
  margin: 0 auto;
  padding: 16px 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sk-summary {
  @extend %shimmer;
  height: 112px;
  border-radius: 20px;
}

.sk-item {
  display: flex;
  align-items: center;
  gap: 12px;
  background: $white;
  border-radius: 16px;
  padding: 14px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
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
  gap: 7px;
}

.sk-line {
  @extend %shimmer;

  &--tag   { width: 36px; height: 18px; border-radius: 4px; }
  &--title { width: 78%; height: 14px; }
  &--sub   { width: 52%; height: 11px; }
}

.sk-num {
  @extend %shimmer;
  width: 44px;
  height: 48px;
  border-radius: 10px;
  flex-shrink: 0;
}

// ============================================================
//  空状态
// ============================================================
@keyframes float-heart {
  0%, 100% { transform: translateY(0); }
  50%       { transform: translateY(-6px); }
}

.empty-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 32px;
  gap: 14px;
}

.empty-icon-wrap {
  position: relative;
  width: 88px;
  height: 88px;
  margin-bottom: 4px;
}

.empty-icon-ring {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  background: $rose-50;
  border: 2px dashed $rose-100;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: float-heart 3s ease-in-out infinite;

  .empty-heart-svg {
    width: 36px;
    height: 36px;
  }
}

.empty-dot {
  position: absolute;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: $rose-100;

  &--tl { top: 4px;  left: 8px;  width: 6px; height: 6px; }
  &--tr { top: 8px;  right: 4px; background: $rose-50; border: 1.5px solid $rose-100; }
  &--bl { bottom: 6px; left: 2px; background: $rose-50; border: 1.5px solid $rose-100; width: 10px; height: 10px; }
}

.empty-title {
  font-size: 16px;
  font-weight: 700;
  color: $text-secondary;
}

.empty-desc {
  font-size: 13px;
  color: $text-tertiary;
  text-align: center;
  line-height: 1.7;
  max-width: 240px;
}
</style>
