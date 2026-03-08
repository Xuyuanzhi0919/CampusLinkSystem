<template>
  <view class="activity-list">

    <!-- ========== 骨架屏 ========== -->
    <view v-if="loading && list.length === 0" class="skeleton-container">
      <view class="sk-banner"></view>
      <view class="sk-section-title"></view>
      <view class="sk-hot-row">
        <view class="sk-hot-card" v-for="i in 3" :key="i"></view>
      </view>
      <view class="sk-section-title" style="margin-top: 20px;"></view>
      <view class="activity-skeleton" v-for="i in 3" :key="i">
        <view class="sk-icon"></view>
        <view class="sk-body">
          <view class="sk-line sk-title"></view>
          <view class="sk-line sk-meta1"></view>
          <view class="sk-line sk-meta2"></view>
        </view>
      </view>
    </view>

    <!-- ========== 正式内容 ========== -->
    <template v-else-if="list.length > 0">

      <!-- ── Banner 区（展示最热活动） ── -->
      <view
        class="activity-banner"
        :style="{ background: getBannerGradient(featuredActivity.type) }"
        @click="handleActivityClick(featuredActivity.activityId)"
      >
        <!-- 装饰 -->
        <view class="banner-deco deco-1"></view>
        <view class="banner-deco deco-2"></view>
        <view class="banner-content">
          <view class="banner-top">
            <view class="banner-badge">
              <view class="badge-dot" :style="{ background: getTypeConfig(featuredActivity.type).dotColor }"></view>
              <text class="badge-text">{{ getActivityType(featuredActivity.type) }}</text>
            </view>
            <view class="banner-status" :class="`status--${featuredActivity.status ?? 0}`">
              <text>{{ getStatusText(featuredActivity.status) }}</text>
            </view>
          </view>

          <text class="banner-title">{{ featuredActivity.title }}</text>
          <text class="banner-organizer" v-if="featuredActivity.organizer || featuredActivity.clubName">
            {{ featuredActivity.organizer || featuredActivity.clubName }}
          </text>

          <view class="banner-info">
            <view class="banner-info-item">
              <Icon name="clock" :size="13" class="info-icon" />
              <text>{{ formatDate(featuredActivity.startTime) }}</text>
            </view>
            <view class="banner-info-item">
              <Icon name="map-pin" :size="13" class="info-icon" />
              <text>{{ featuredActivity.location || '待定' }}</text>
            </view>
          </view>

          <view class="banner-footer">
            <view class="banner-participants">
              <Icon name="users" :size="13" class="info-icon" />
              <text>{{ featuredActivity.currentParticipants || 0 }}/{{ featuredActivity.maxParticipants || '不限' }} 人报名</text>
            </view>
            <!-- 可报名状态显示报名按钮，否则显示查看详情 -->
            <view
              v-if="(featuredActivity.status ?? 0) < 2"
              class="banner-signup-btn"
              :class="{
                'banner-signup-btn--joined': featuredActivity.isJoined,
                'banner-signup-btn--loading': signingIds.has(featuredActivity.activityId)
              }"
              @click.stop="handleSignUp(featuredActivity)"
            >
              <text class="banner-signup-text">
                {{ signingIds.has(featuredActivity.activityId) ? '···' : (featuredActivity.isJoined ? '✓ 已报名' : '立即报名') }}
              </text>
            </view>
            <view v-else class="banner-action">
              <text>查看详情 →</text>
            </view>
          </view>
        </view>
      </view>

      <!-- ── 即将开始横滑 ── -->
      <view class="section-header">
        <view class="section-title-wrap">
          <view class="section-title-bar" style="background: #27AE60;"></view>
          <text class="section-title">即将开始</text>
        </view>
        <text class="section-more">全部 →</text>
      </view>

      <scroll-view class="hot-scroll" scroll-x :show-scrollbar="false">
        <view class="hot-list">
          <view
            v-for="item in upcomingActivities"
            :key="item.activityId"
            class="upcoming-card"
            :style="{ '--accent': getTypeConfig(item.type).accent }"
            @click="handleActivityClick(item.activityId)"
          >
            <!-- 顶部色块 -->
            <view class="upcoming-card__header" :style="{ background: getTypeConfig(item.type).bgGradient }">
              <ClActivityDefaultCover :type="item.type" :size="72" />
              <view class="upcoming-status" :class="`status--${item.status ?? 0}`">
                <text>{{ getStatusText(item.status) }}</text>
              </view>
            </view>
            <view class="upcoming-card__body">
              <text class="upcoming-title">{{ item.title }}</text>
              <text class="upcoming-date">{{ formatDate(item.startTime) }}</text>
              <view class="upcoming-loc">
                <Icon name="map-pin" :size="11" class="upcoming-loc-icon" />
                <text>{{ item.location || '待定' }}</text>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>

      <!-- ── 搜索 + 状态筛选 ── -->
      <view class="filter-wrap">
        <view class="act-search">
          <Icon name="search" :size="13" class="act-search__icon" />
          <input
            v-model="searchKeyword"
            class="act-search__input"
            placeholder="搜索活动名称、地点..."
          />
          <view v-if="searchKeyword" class="act-search__clear" @click="searchKeyword = ''">
            <Icon name="x" :size="12" />
          </view>
        </view>
        <scroll-view class="status-scroll" scroll-x :show-scrollbar="false">
          <view class="status-tabs">
            <view
              v-for="sf in statusFilters"
              :key="sf.value"
              class="status-tab"
              :class="{ active: activeStatus === sf.value }"
              @click="activeStatus = sf.value"
            >
              <text class="status-tab-label">{{ sf.label }}</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- ── 全部活动 ── -->
      <view class="section-header" style="margin-top: 8px;">
        <view class="section-title-wrap">
          <view class="section-title-bar" style="background: #6C5CE7;"></view>
          <text class="section-title">全部活动</text>
        </view>
        <text class="section-count">{{ filteredList.length }} 场</text>
      </view>

      <!-- 筛选无结果 -->
      <view v-if="filteredList.length === 0" class="filter-empty">
        <Icon name="calendar" :size="32" class="filter-empty__icon" />
        <text class="filter-empty__text">没有找到匹配的活动</text>
        <view class="filter-empty__reset" @click="searchKeyword = ''; activeStatus = -1">
          <text>清除筛选</text>
        </view>
      </view>

      <view v-else class="activity-items">
        <view
          v-for="item in filteredList"
          :key="item.activityId"
          class="activity-card"
          :style="{ '--type-accent': getTypeConfig(item.type).accent }"
          @click="handleActivityClick(item.activityId)"
        >
          <!-- 左侧图标封面 -->
          <view class="activity-card__cover" :style="{ background: getTypeConfig(item.type).bgGradient }">
            <ClActivityDefaultCover :type="item.type" :size="56" />
          </view>

          <!-- 右侧信息区 -->
          <view class="activity-card__body">
            <!-- 类型色条 -->
            <view class="activity-card__accent-bar"></view>
            <view class="activity-card__top">
              <text class="activity-card__title">{{ item.title }}</text>
              <view class="activity-card__status" :class="`status--${item.status ?? 0}`">
                <text>{{ getStatusText(item.status) }}</text>
              </view>
            </view>
            <view class="activity-card__meta-row">
              <Icon name="clock" :size="11" class="meta-icon" />
              <text class="meta-text">{{ formatDate(item.startTime) }}</text>
            </view>
            <view class="activity-card__meta-row">
              <Icon name="map-pin" :size="11" class="meta-icon" />
              <text class="meta-text meta-text--loc">{{ item.location || '待定' }}</text>
            </view>
            <!-- 底部：报名人数 + 操作按钮 -->
            <view class="activity-card__action-row">
              <view class="activity-card__participants">
                <Icon name="users" :size="10" class="meta-icon" />
                <text class="meta-text">
                  {{ item.currentParticipants || 0 }}<template v-if="item.maxParticipants">/{{ item.maxParticipants }}</template> 人
                </text>
              </view>
              <view
                class="activity-card__btn"
                :class="{
                  'activity-card__btn--joined': item.isJoined,
                  'activity-card__btn--ended': (item.status ?? 0) >= 2,
                  'activity-card__btn--loading': signingIds.has(item.activityId)
                }"
                @click.stop="(item.status ?? 0) < 2 ? handleSignUp(item) : handleActivityClick(item.activityId)"
              >
                <text class="activity-card__btn-text">
                  {{ signingIds.has(item.activityId) ? '···' : ((item.status ?? 0) >= 2 ? '查看详情' : (item.isJoined ? '✓ 已报名' : '报名')) }}
                </text>
              </view>
            </view>
          </view>
        </view>
      </view>

    </template>

    <!-- ========== 空状态 ========== -->
    <view v-else class="empty-container">
      <view class="empty-icon-wrap">
        <Icon name="calendar" :size="40" color="#D1D5DB" />
      </view>
      <text class="empty-title">暂无活动</text>
      <text class="empty-hint">敬请期待更多精彩活动</text>
    </view>

    <!-- ========== 底部状态区 ========== -->
    <view class="list-footer">
      <view v-if="loading && list.length > 0" class="loading-more">
        <view class="loading-dots">
          <view class="dot"></view>
          <view class="dot"></view>
          <view class="dot"></view>
        </view>
      </view>
      <view v-else-if="!hasMore && list.length > 0" class="no-more">
        <view class="no-more-line"></view>
        <text class="no-more-text">已加载全部活动</text>
        <view class="no-more-line"></view>
      </view>
    </view>

  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useNavigation } from '@/composables/useNavigation'
import { joinActivity, cancelActivity } from '@/services/activity'
import { requireLogin } from '@/utils/auth'
import Icon from '@/components/icons/index.vue'
import ClActivityDefaultCover from '@/components/cl/ClActivityDefaultCover.vue'

interface Props {
  list: any[]
  loading: boolean
  hasMore?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  hasMore: true
})
const emit = defineEmits<{ (e: 'refresh'): void }>()

const { toActivityDetail } = useNavigation()
const signingIds = ref<Set<number>>(new Set())
const searchKeyword = ref('')
const activeStatus = ref(-1)

const statusFilters = [
  { value: -1, label: '全部' },
  { value: 0, label: '未开始' },
  { value: 1, label: '进行中' },
  { value: 2, label: '已结束' },
]

const filteredList = computed(() => {
  let result = props.list
  if (activeStatus.value !== -1) {
    result = result.filter((a: any) => a.status === activeStatus.value)
  }
  if (searchKeyword.value.trim()) {
    const kw = searchKeyword.value.trim().toLowerCase()
    result = result.filter((a: any) =>
      a.title?.toLowerCase().includes(kw) ||
      a.location?.toLowerCase().includes(kw) ||
      (a.organizer || a.clubName)?.toLowerCase().includes(kw)
    )
  }
  return result
})

const handleActivityClick = (activityId: number) => {
  toActivityDetail(activityId)
}

const handleSignUp = async (activity: any) => {
  if (!requireLogin('join')) return
  if (!activity.activityId || signingIds.value.has(activity.activityId)) return
  signingIds.value.add(activity.activityId)
  try {
    if (activity.isJoined) {
      await cancelActivity(activity.activityId)
      uni.showToast({ title: '已取消报名', icon: 'success', duration: 1500 })
    } else {
      await joinActivity(activity.activityId)
      uni.showToast({ title: '报名成功！', icon: 'success', duration: 1500 })
    }
    emit('refresh')
  } catch (error: any) {
    uni.showToast({ title: error?.message || '操作失败，请重试', icon: 'none', duration: 2000 })
  } finally {
    signingIds.value.delete(activity.activityId)
  }
}

// 活动类型配置（lucide 图标名 + 渐变色）
const getTypeConfig = (type: number) => {
  const configs: Record<number, { icon: string; bgGradient: string; accent: string; dotColor: string }> = {
    1: { icon: 'book-open', bgGradient: 'linear-gradient(135deg, #667EEA 0%, #764BA2 100%)', accent: '#667EEA', dotColor: '#9B59B6' },
    2: { icon: 'trophy',    bgGradient: 'linear-gradient(135deg, #F093FB 0%, #F5576C 100%)', accent: '#F5576C', dotColor: '#E74C3C' },
    3: { icon: 'smile',     bgGradient: 'linear-gradient(135deg, #4FACFE 0%, #00F2FE 100%)', accent: '#4FACFE', dotColor: '#3498DB' },
    4: { icon: 'heart',     bgGradient: 'linear-gradient(135deg, #FA709A 0%, #FEE140 100%)', accent: '#FA709A', dotColor: '#E91E63' },
    5: { icon: 'calendar',  bgGradient: 'linear-gradient(135deg, #27AE60 0%, #2ECC71 100%)', accent: '#27AE60', dotColor: '#27AE60' },
  }
  return configs[type] || configs[5]
}

const getBannerGradient = (type: number) => {
  const gradients: Record<number, string> = {
    1: 'linear-gradient(135deg, #1A1A2E 0%, #16213E 50%, #4A235A 100%)',
    2: 'linear-gradient(135deg, #1A0A0A 0%, #2D1515 50%, #7B241C 100%)',
    3: 'linear-gradient(135deg, #0A1628 0%, #154360 50%, #1A5276 100%)',
    4: 'linear-gradient(135deg, #1A0A14 0%, #6C0F45 50%, #922B5E 100%)',
    5: 'linear-gradient(135deg, #0A1A0A 0%, #1E5631 50%, #196F3D 100%)',
  }
  return gradients[type] || gradients[5]
}

const getActivityType = (type: number) => {
  const types: Record<number, string> = { 1: '讲座', 2: '比赛', 3: '社交', 4: '公益', 5: '其他' }
  return types[type] || '活动'
}

const getStatusText = (status: number) => {
  // 与后端 convertToResponse() 动态计算的状态对齐：0=未开始 1=进行中 2=已结束 3=已取消
  const map: Record<number, string> = { 0: '未开始', 1: '进行中', 2: '已结束', 3: '已取消' }
  return map[status] || '未知'
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '待定'
  const d = new Date(dateStr)
  const m = d.getMonth() + 1
  const day = d.getDate()
  const h = d.getHours().toString().padStart(2, '0')
  const min = d.getMinutes().toString().padStart(2, '0')
  return `${m}月${day}日 ${h}:${min}`
}

// computed：精选活动（Banner）
const featuredActivity = computed(() => props.list[0] || {})

// 即将开始（状态0/1，最多5条）
const upcomingActivities = computed(() =>
  props.list.filter((a: any) => a.status === 0 || a.status === 1).slice(0, 5)
)
</script>

<style scoped lang="scss">
@import '@/styles/design-tokens.scss';

.activity-list {
  padding: 0 0 80px;
}

/* ========== 骨架屏 ========== */
@keyframes shimmer {
  0%   { background-position: -200% 0; }
  100% { background-position:  200% 0; }
}

%shimmer {
  background: linear-gradient(90deg, #F3F4F6 25%, #E5E7EB 50%, #F3F4F6 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.sk-banner {
  @extend %shimmer;
  height: 190px;
  margin: 12px 16px;
  border-radius: 16px;
}

.sk-section-title {
  @extend %shimmer;
  height: 18px;
  width: 100px;
  margin: 16px 16px 12px;
  border-radius: 4px;
}

.sk-hot-row {
  display: flex;
  gap: 12px;
  padding: 0 16px;
  overflow: hidden;
}

.sk-hot-card {
  @extend %shimmer;
  width: 140px;
  height: 160px;
  border-radius: 14px;
  flex-shrink: 0;
}

.activity-skeleton {
  display: flex;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid $color-divider;
}

.sk-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  flex-shrink: 0;
  @extend %shimmer;
}

.sk-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: center;
}

.sk-line {
  border-radius: 4px;
  @extend %shimmer;
}
.sk-title { height: 16px; width: 70%; }
.sk-meta1 { height: 13px; width: 50%; }
.sk-meta2 { height: 13px; width: 40%; }

/* ========== Banner 区 ========== */
.activity-banner {
  position: relative;
  margin: 12px 16px 0;
  border-radius: 18px;
  overflow: hidden;
  min-height: 190px;
  cursor: pointer;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.banner-deco {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.05);
}
.deco-1 {
  width: 180px;
  height: 180px;
  right: -50px;
  top: -50px;
}
.deco-2 {
  width: 100px;
  height: 100px;
  right: 30px;
  bottom: -30px;
  background: rgba(255, 255, 255, 0.04);
}

.banner-content {
  position: relative;
  padding: 18px 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.banner-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.banner-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 4px 10px;
}

.badge-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.badge-text {
  font-size: 11px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
}

.banner-status {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;

  &.status--0 { background: rgba(250,173,20,0.3);   color: #FFE58F; }              // 未开始 orange
  &.status--1 { background: rgba(39, 174, 96, 0.3);  color: #A9EFC5; }              // 进行中 green
  &.status--2 { background: rgba(255,255,255, 0.1);  color: rgba(255,255,255,0.5); } // 已结束 gray
  &.status--3 { background: rgba(255, 92, 92, 0.3);  color: #FFAEAE; }              // 已取消 red
}

.banner-title {
  font-size: 19px;
  font-weight: 800;
  color: #FFFFFF;
  line-height: 1.35;
  letter-spacing: -0.3px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.banner-organizer {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.65);
}

.banner-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 2px;
}

.banner-info-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.75);
}

.info-icon {
  color: rgba(255, 255, 255, 0.75);
  flex-shrink: 0;
}

.banner-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 4px;
  padding-top: 10px;
  border-top: 1px solid rgba(255, 255, 255, 0.12);
}

.banner-participants {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.75);
}

/* 立即报名按钮 */
.banner-signup-btn {
  display: inline-flex;
  align-items: center;
  height: 30px;
  padding: 0 14px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.25);
  transition: transform 0.15s ease, opacity 0.15s ease;
  flex-shrink: 0;

  &:active { transform: scale(0.91); }

  &--joined {
    background: rgba(255, 255, 255, 0.12);
    border: 1px solid rgba(255, 255, 255, 0.35);
    box-shadow: none;

    .banner-signup-text { color: rgba(255, 255, 255, 0.8); }
  }

  &--loading {
    opacity: 0.5;
    pointer-events: none;
  }
}

.banner-signup-text {
  font-size: 12px;
  font-weight: 800;
  color: #111;
  letter-spacing: 0.2px;
  white-space: nowrap;
}

/* 仅查看详情（已结束状态） */
.banner-action {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 16px;
  padding: 5px 14px;
  font-size: 12px;
  font-weight: 600;
  color: #FFFFFF;
}

/* ========== Section Header ========== */
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 16px 12px;
}

.section-title-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title-bar {
  width: 4px;
  height: 16px;
  border-radius: 2px;
  background: #27AE60;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: $color-text-primary;
}

.section-more {
  font-size: 13px;
  color: #27AE60;
}

.section-count {
  font-size: 13px;
  color: $color-text-quaternary;
}

/* ========== 即将开始横滑 ========== */
.hot-scroll {
  width: 100%;
}

.hot-list {
  display: flex;
  gap: 12px;
  padding: 4px 16px 8px;
  width: max-content;
}

.upcoming-card {
  width: 140px;
  border-radius: 18px;
  overflow: hidden;
  background: #FFFFFF;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.07), 0 0 0 1px rgba(0,0,0,0.04);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1),
              box-shadow 0.2s ease,
              opacity 0.15s ease;
  flex-shrink: 0;

  /* #ifdef H5 */
  &:hover {
    transform: translateY(-4px) scale(1.02);
    box-shadow: 0 10px 28px rgba(0, 0, 0, 0.13), 0 0 0 1px rgba(0,0,0,0.05);

    .upcoming-card__header {
      transform: scale(1.04);
    }

    .upcoming-card__body::after {
      opacity: 1;
      height: 4px;
    }
  }
  /* #endif */

  &:active {
    transform: scale(0.94);
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
    opacity: 0.9;

    .upcoming-card__header {
      transform: scale(0.98);
    }
  }
}

/* 顶部渐变色区 */
.upcoming-card__header {
  height: 88px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  transform-origin: center top;
}

/* 状态标签：绝对定位到右上角 */
.upcoming-status {
  position: absolute;
  top: 8px;
  right: 8px;
  display: inline-flex;
  align-items: center;
  height: 20px;
  padding: 0 7px;
  border-radius: 7px;
  font-size: 10px;
  font-weight: 700;

  &.status--0 { background: rgba(250,173,20,0.3);  color: #FFE58F; }              // 未开始 orange
  &.status--1 { background: rgba(39,174,96,0.35);   color: #A9EFC5; }              // 进行中 green
  &.status--2 { background: rgba(255,255,255,0.1);  color: rgba(255,255,255,0.5); } // 已结束 gray
  &.status--3 { background: rgba(255,92,92,0.3);    color: #FFAEAE; }              // 已取消 red
}

/* 白色信息区 */
.upcoming-card__body {
  flex: 1;
  padding: 10px 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  position: relative;
}

/* 底部类型色条 */
.upcoming-card__body::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: var(--accent, #27AE60);
  border-radius: 0 0 18px 18px;
  opacity: 0.6;
  transition: opacity 0.2s ease, height 0.2s ease;
}

.upcoming-title {
  font-size: 13px;
  font-weight: 700;
  color: #1A1A2E;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  letter-spacing: -0.1px;
}

.upcoming-date {
  font-size: 11px;
  color: var(--accent);
  font-weight: 600;
  margin-top: 1px;
}

.upcoming-loc {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 11px;
  color: #9CA3AF;
  overflow: hidden;
}

.upcoming-loc-icon {
  color: #9CA3AF;
  flex-shrink: 0;
}

/* ========== 搜索 + 状态筛选 ========== */
.filter-wrap {
  margin: 4px 0 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.act-search {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #F9FAFB;
  border: 1px solid #E5E7EB;
  border-radius: 24px;
  padding: 8px 14px;
  margin: 0 16px;
  transition: border-color 0.15s;

  &:focus-within {
    border-color: #377DFF;
    background: #fff;
  }
}

.act-search__icon { color: #9CA3AF; flex-shrink: 0; }

.act-search__input {
  flex: 1;
  font-size: 13px;
  color: #374151;
  background: transparent;
  border: none;
  outline: none;
  padding: 0;
  min-width: 0;

  &::placeholder { color: #D1D5DB; }
}

.act-search__clear {
  color: #9CA3AF;
  flex-shrink: 0;
  cursor: pointer;
  display: flex;
  align-items: center;
  &:active { opacity: 0.6; }
}

.status-scroll {
  white-space: nowrap;
  /* #ifdef H5 */
  &::-webkit-scrollbar { display: none; }
  /* #endif */
}

.status-tabs {
  display: inline-flex;
  gap: 8px;
  padding: 2px 16px;
}

.status-tab {
  display: inline-flex;
  align-items: center;
  padding: 5px 14px;
  border-radius: 20px;
  background: #F3F4F6;
  cursor: pointer;
  transition: all 0.15s;
  flex-shrink: 0;

  &.active {
    background: #27AE60;
    .status-tab-label { color: #fff; }
  }

  &:active { transform: scale(0.95); }
}

.status-tab-label {
  font-size: 12px;
  color: #6B7280;
  font-weight: 500;
}

/* 筛选空状态 */
.filter-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48px 16px;
  gap: 12px;
}

.filter-empty__icon { color: #D1D5DB; }

.filter-empty__text {
  font-size: 14px;
  color: #9CA3AF;
}

.filter-empty__reset {
  padding: 6px 20px;
  border-radius: 16px;
  border: 1px solid #E5E7EB;
  font-size: 13px;
  color: #6B7280;
  cursor: pointer;
  &:active { background: #F3F4F6; }
}

/* ========== 全部活动列表 ========== */
.activity-items {
  padding: 0 16px 4px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 卡片：白色圆角卡片 */
.activity-card {
  position: relative;
  border-radius: 18px;
  overflow: hidden;
  cursor: pointer;
  background: #FFFFFF;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.07), 0 0 0 1px rgba(0,0,0,0.04);
  display: flex;
  flex-direction: row;
  transition: transform 0.18s cubic-bezier(0.34, 1.56, 0.64, 1),
              box-shadow 0.18s ease,
              opacity 0.15s ease;

  /* #ifdef H5 */
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.11), 0 0 0 1px rgba(0,0,0,0.05);

    .activity-card__cover {
      filter: brightness(1.06);
    }

    .activity-card__accent-bar {
      width: 32px;
    }
  }
  /* #endif */

  &:active {
    transform: scale(0.97);
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
    opacity: 0.92;

    .activity-card__cover {
      filter: brightness(0.95);
    }
  }
}

/* 左侧图标封面 */
.activity-card__cover {
  width: 80px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: filter 0.2s ease;
  align-self: stretch;
}

/* 右侧信息区 */
.activity-card__body {
  flex: 1;
  min-width: 0;
  padding: 12px 12px 12px 14px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 3px;
}

/* 类型主色细线 */
.activity-card__accent-bar {
  width: 18px;
  height: 2.5px;
  border-radius: 2px;
  background: var(--type-accent, #27AE60);
  margin-bottom: 2px;
  transition: width 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.activity-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.activity-card__title {
  font-size: 14px;
  font-weight: 700;
  color: #1A1A2E;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  letter-spacing: -0.2px;
}

/* 状态标签 */
.activity-card__status {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  height: 20px;
  padding: 0 8px;
  border-radius: 6px;
  font-size: 10px;
  font-weight: 700;

  &.status--0 { color: #D97706;              background: rgba(250,173,20,0.12); }  // 未开始 orange
  &.status--1 { color: $color-success;       background: rgba(39, 174, 96, 0.1); } // 进行中 green
  &.status--2 { color: $color-text-tertiary; background: rgba(0,0,0,0.05); }       // 已结束 gray
  &.status--3 { color: $color-danger;        background: rgba(255, 92, 92, 0.1); } // 已取消 red
}

.activity-card__meta-row {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 底部操作行：报名人数 + 按钮 */
.activity-card__action-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 4px;
}

.activity-card__participants {
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-icon {
  color: #9CA3AF;
  flex-shrink: 0;
}

.meta-text {
  font-size: 11px;
  color: #9CA3AF;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  &--loc { max-width: 100%; }
}

/* 报名按钮 */
.activity-card__btn {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  height: 26px;
  padding: 0 13px;
  border-radius: 8px;
  background: #27AE60;
  transition: opacity 0.15s ease, transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);

  &:active {
    opacity: 0.8;
    transform: scale(0.92);
  }

  /* 已报名：镂空描边 */
  &--joined {
    background: transparent;
    border: 1.5px solid #E5E7EB;
  }

  /* 已结束/已取消：灰色 */
  &--ended {
    background: transparent;
    border: 1.5px solid #E5E7EB;
  }

  &--loading {
    opacity: 0.45;
    pointer-events: none;
  }
}

.activity-card__btn-text {
  font-size: 12px;
  font-weight: 700;
  color: #FFFFFF;
  letter-spacing: 0.2px;

  .activity-card__btn--joined &,
  .activity-card__btn--ended & {
    color: #9CA3AF;
    font-weight: 500;
  }
}

/* ========== 空状态 ========== */
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  gap: 10px;
}

.empty-icon-wrap {
  width: 80px;
  height: 80px;
  background: $color-bg-hover;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
}

.empty-title {
  font-size: 15px;
  font-weight: 600;
  color: $color-text-secondary;
}

.empty-hint {
  font-size: 13px;
  color: $color-text-quaternary;
}

/* ========== 底部状态区 ========== */
.list-footer {
  padding: 16px 0;
}

.loading-more {
  display: flex;
  justify-content: center;
  padding: 8px 0;
}

.loading-dots {
  display: flex;
  gap: 6px;
  align-items: center;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #27AE60;
  opacity: 0.4;
  animation: dotPulse 1.2s ease-in-out infinite;

  &:nth-child(2) { animation-delay: 0.2s; }
  &:nth-child(3) { animation-delay: 0.4s; }
}

@keyframes dotPulse {
  0%, 80%, 100% { transform: scale(0.8); opacity: 0.4; }
  40% { transform: scale(1.1); opacity: 1; }
}

.no-more {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 20px;
}

.no-more-line {
  flex: 1;
  height: 1px;
  background: $color-divider;
}

.no-more-text {
  font-size: 12px;
  color: $color-text-quaternary;
  white-space: nowrap;
}

/* ========== PC 端适配 ========== */
@media (min-width: 1024px) {
  .activity-banner {
    margin: 16px 80px 0;
  }

  .section-header {
    padding: 20px 80px 12px;
  }

  .hot-list {
    padding: 4px 80px 8px;
  }

  .act-search {
    margin: 0 80px;
  }

  .status-tabs {
    padding: 2px 80px;
  }

  .activity-items {
    padding: 0 80px;
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}
</style>
