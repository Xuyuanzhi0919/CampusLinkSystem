<template>
  <view class="club-list">

    <!-- ========== 骨架屏 ========== -->
    <view v-if="loading && list.length === 0" class="skeleton-container">
      <!-- Banner 骨架 -->
      <view class="sk-banner"></view>
      <!-- 横滑骨架 -->
      <view class="sk-section-title"></view>
      <view class="sk-hot-row">
        <view class="sk-hot-card" v-for="i in 3" :key="i"></view>
      </view>
      <!-- 列表骨架 -->
      <view class="sk-section-title" style="margin-top: 20px;"></view>
      <view class="club-skeleton" v-for="i in 3" :key="i">
        <view class="sk-icon"></view>
        <view class="sk-body">
          <view class="sk-line sk-title"></view>
          <view class="sk-line sk-desc"></view>
          <view class="sk-line sk-meta"></view>
        </view>
      </view>
    </view>

    <!-- ========== 正式内容 ========== -->
    <template v-else-if="list.length > 0">

      <!-- ── Banner 轮播区 ── -->
      <view class="banner-wrap">
        <swiper
          class="club-swiper"
          :autoplay="true"
          :interval="4000"
          :duration="500"
          :circular="true"
          @change="onSwiperChange"
        >
          <swiper-item
            v-for="(club, idx) in bannerClubs"
            :key="club.clubId"
            @click="handleClubClick(club.clubId)"
          >
            <view class="club-banner" :style="{ background: bannerGradients[idx % bannerGradients.length] }">
              <!-- 大号序号水印 -->
              <text class="banner-watermark">{{ String(idx + 1).padStart(2, '0') }}</text>
              <!-- 斜切色条装饰 -->
              <view class="banner-slash"></view>
              <!-- 内容区 -->
              <view class="banner-content">
                <!-- 顶部：仅保留精选 badge -->
                <view class="banner-top-row">
                  <view class="banner-badge">
                    <text class="badge-dot"></text>
                    <text class="badge-text">社区精选</text>
                  </view>
                </view>
                <!-- 中部：社团名 + 简介 -->
                <view class="banner-main">
                  <text class="banner-title">{{ club.clubName }}</text>
                  <text class="banner-desc">{{ club.description || '加入我们，一起创造校园精彩' }}</text>
                </view>
                <!-- 底部：统计数据 + 加入按钮 -->
                <view class="banner-stats">
                  <view class="banner-stat">
                    <text class="banner-stat-num">{{ club.memberCount || 0 }}</text>
                    <text class="banner-stat-label">成员</text>
                  </view>
                  <view class="banner-stat-divider"></view>
                  <view class="banner-stat">
                    <text class="banner-stat-num">{{ club.activityCount || 0 }}</text>
                    <text class="banner-stat-label">活动</text>
                  </view>
                  <view class="banner-stat-spacer"></view>
                  <view
                    class="banner-btn"
                    :class="{ 'banner-btn--joined': club.isMember, 'banner-btn--loading': joiningIds.has(club.clubId) }"
                    @click.stop="handleJoinClub(club)"
                  >
                    <text class="banner-btn-text">{{ joiningIds.has(club.clubId) ? '···' : (club.isMember ? '已加入' : '加入社团') }}</text>
                  </view>
                </view>
              </view>
            </view>
          </swiper-item>
        </swiper>

        <!-- 指示器：进度条 + 数字 -->
        <view class="swiper-indicator">
          <view class="swiper-track">
            <view
              class="swiper-progress"
              :style="{ width: `${((swiperCurrent + 1) / bannerClubs.length) * 100}%` }"
            ></view>
          </view>
          <text class="swiper-counter">
            <text class="swiper-counter-cur">{{ String(swiperCurrent + 1).padStart(2, '0') }}</text>
            <text class="swiper-counter-sep"> / </text>
            <text class="swiper-counter-total">{{ String(bannerClubs.length).padStart(2, '0') }}</text>
          </text>
        </view>
      </view>

      <!-- ── 热门社团横滑 ── -->
      <view class="section-header">
        <view class="section-title-wrap">
          <view class="section-title-bar"></view>
          <text class="section-title">热门社团</text>
        </view>
      </view>

      <scroll-view class="hot-scroll" scroll-x :show-scrollbar="false">
        <view class="hot-list">
          <view class="hot-list-pad"></view>
          <view
            v-for="(item, idx) in hotClubs"
            :key="item.clubId"
            class="hot-card"
            :style="{
              '--card-color': hotCardColors[idx % hotCardColors.length],
              '--card-color-dim': hotCardColorsDim[idx % hotCardColorsDim.length]
            }"
            @click="handleClubClick(item.clubId)"
          >
            <!-- 封面全图 -->
            <image v-if="item.logoUrl" :src="item.logoUrl" class="hot-card__img" mode="aspectFill" />
            <!-- 无图占位 -->
            <view v-else class="hot-card__placeholder">
              <text class="hot-card__initial">{{ item.clubName?.slice(0, 1) || '社' }}</text>
              <view class="hot-card__pl-ring hot-card__pl-ring--1"></view>
              <view class="hot-card__pl-ring hot-card__pl-ring--2"></view>
            </view>
            <!-- 全卡渐变遮罩 -->
            <view class="hot-card__overlay"></view>
            <!-- 左上排名角标 -->
            <view class="hot-card__rank" :class="idx < 3 ? 'hot-card__rank--hot' : ''">
              <text class="hot-card__rank-text">{{ idx < 3 ? ['TOP1','TOP2','TOP3'][idx] : `NO.${idx+1}` }}</text>
            </view>
            <!-- 底部信息 -->
            <view class="hot-card__bottom">
              <text class="hot-card__name">{{ item.clubName }}</text>
              <view class="hot-card__meta">
                <Icon name="users" :size="10" class="hot-card__meta-icon" />
                <text class="hot-card__count">{{ formatMemberCount(item.memberCount) }}人</text>
              </view>
              <!-- 主题色光条 -->
              <view class="hot-card__glow-bar"></view>
            </view>
            <!-- 前3名脉冲光晕 -->
            <view v-if="idx < 3" class="hot-card__pulse"></view>
          </view>
          <view class="hot-list-pad"></view>
        </view>
      </scroll-view>

      <!-- ── 全部社团 ── -->
      <view class="section-header" style="margin-top: 8px;">
        <view class="section-title-wrap">
          <view class="section-title-bar" style="background: #6C5CE7;"></view>
          <text class="section-title">全部社团</text>
        </view>
        <text class="section-count">{{ list.length }} 个</text>
      </view>

      <view class="club-items">
        <view
          v-for="(item, idx) in list"
          :key="item.clubId"
          class="club-card"
          :style="{
            '--cat-color': getCategoryColor(item.category),
            '--cat-color-dim': getCategoryColorDim(item.category)
          }"
          @click="handleClubClick(item.clubId)"
        >
          <!-- 封面图 / 占位 -->
          <image v-if="item.logoUrl" :src="item.logoUrl" class="club-card__bg" mode="aspectFill" />
          <view v-else class="club-card__bg-placeholder">
            <text class="club-card__bg-initial">{{ item.clubName?.slice(0, 1) }}</text>
            <view class="club-card__bg-ring club-card__bg-ring--1"></view>
            <view class="club-card__bg-ring club-card__bg-ring--2"></view>
          </view>

          <!-- 全卡渐变遮罩 -->
          <view class="club-card__overlay"></view>

          <!-- 右上：分类标签 -->
          <view class="club-card__cat">
            <text class="club-card__cat-text">{{ item.category || '综合' }}</text>
          </view>

          <!-- 底部内容区 -->
          <view class="club-card__bottom">
            <!-- 主题色光条 -->
            <view class="club-card__glow-bar"></view>
            <text class="club-card__title">{{ item.clubName }}</text>
            <text v-if="item.description" class="club-card__desc">{{ item.description }}</text>
            <view class="club-card__footer">
              <view class="club-card__meta">
                <Icon name="users" :size="10" class="club-meta-icon" />
                <text class="club-meta-val">{{ formatMemberCount(item.memberCount) }}</text>
                <view class="club-meta-dot"></view>
                <Icon name="zap" :size="10" class="club-meta-icon" />
                <text class="club-meta-val">{{ item.activityCount || 0 }}场</text>
              </view>
              <view
                class="club-card__btn"
                :class="{
                  'club-card__btn--joined': item.isMember,
                  'club-card__btn--loading': joiningIds.has(item.clubId)
                }"
                @click.stop="handleJoinClub(item)"
              >
                <text class="club-card__btn-text">{{ joiningIds.has(item.clubId) ? '···' : (item.isMember ? '✓ 已加入' : '加入') }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

    </template>

    <!-- ========== 空状态 ========== -->
    <view v-else class="empty-container">
      <view class="empty-icon-wrap">
        <Icon name="users" :size="40" color="#D1D5DB" />
      </view>
      <text class="empty-title">暂无社团</text>
      <text class="empty-hint">快来创建第一个社团吧</text>
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
        <text class="no-more-text">已加载全部社团</text>
        <view class="no-more-line"></view>
      </view>
    </view>

  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useNavigation } from '@/composables/useNavigation'
import { joinClub, quitClub } from '@/services/club'
import { requireLogin } from '@/utils/auth'
import Icon from '@/components/icons/index.vue'

interface Props {
  list: any[]
  loading: boolean
  hasMore?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  hasMore: true
})
const emit = defineEmits<{ (e: 'refresh'): void }>()

const { toClubDetail } = useNavigation()
const joiningIds = ref<Set<number>>(new Set())
const swiperCurrent = ref(0)

const onSwiperChange = (e: any) => {
  swiperCurrent.value = e.detail.current
}

// 热门色彩方案（每张卡片不同主色）
const hotCardColors = [
  '#377DFF',
  '#6C5CE7',
  '#00B894',
  '#E17055',
  '#FDCB6E',
]

// 各主色对应的深色版（用于渐变底部遮罩）
const hotCardColorsDim = [
  '#0A2A6E',
  '#2A1870',
  '#004D3A',
  '#6B2A18',
  '#7A5A00',
]

// Banner 轮播渐变配色（每张不同）
const bannerGradients = [
  'linear-gradient(135deg, #1A1A2E 0%, #16213E 40%, #0F3460 70%, #377DFF 100%)',
  'linear-gradient(135deg, #1A0E2E 0%, #2D1657 40%, #6C3FBA 70%, #9B59B6 100%)',
  'linear-gradient(135deg, #0A1F1A 0%, #0D3B2E 40%, #0E7460 70%, #00B894 100%)',
  'linear-gradient(135deg, #1F0E0A 0%, #3B1A0D 40%, #A04010 70%, #E17055 100%)',
  'linear-gradient(135deg, #1A150A 0%, #3B2F0D 40%, #7A6010 70%, #FDCB6E 100%)',
]

// 精选社团（轮播展示前 5 个）
const bannerClubs = computed(() => props.list.slice(0, 5))

// 热门社团（前 8 个，横滑展示）
const hotClubs = computed(() => props.list.slice(0, 8))

const categoryColorMap: Record<string, string> = {
  '文艺': '#E91E8C',
  '体育': '#00B894',
  '学术': '#377DFF',
  '志愿': '#FF6B35',
  '科技': '#6C5CE7',
  '综合': '#FDCB6E',
}
const categoryColorDimMap: Record<string, string> = {
  '文艺': '#5A0030',
  '体育': '#004D3A',
  '学术': '#0A2A6E',
  '志愿': '#6B2510',
  '科技': '#2A1870',
  '综合': '#7A5A00',
}
const getCategoryColor = (cat?: string) =>
  categoryColorMap[cat || ''] || '#FDCB6E'
const getCategoryColorDim = (cat?: string) =>
  categoryColorDimMap[cat || ''] || '#7A5A00'

const formatMemberCount = (count: number) => {
  if (!count) return '0'
  if (count >= 10000) return `${(count / 10000).toFixed(1)}w`
  if (count >= 1000) return `${(count / 1000).toFixed(1)}k`
  return String(count)
}

const handleClubClick = (clubId: number) => {
  if (!clubId) return
  toClubDetail(clubId)
}

const handleJoinClub = async (club: any) => {
  if (!requireLogin('join')) return
  if (!club.clubId || joiningIds.value.has(club.clubId)) return

  joiningIds.value.add(club.clubId)
  try {
    if (club.isMember) {
      await quitClub(club.clubId)
      uni.showToast({ title: '已退出社团', icon: 'success', duration: 1500 })
    } else {
      await joinClub(club.clubId)
      uni.showToast({ title: '加入成功', icon: 'success', duration: 1500 })
    }
    emit('refresh')
  } catch (error: any) {
    uni.showToast({ title: error?.message || '操作失败，请重试', icon: 'none', duration: 2000 })
  } finally {
    joiningIds.value.delete(club.clubId)
  }
}
</script>

<style scoped lang="scss">
@import '@/styles/design-tokens.scss';

.club-list {
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
  height: 180px;
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
  width: 100px;
  height: 120px;
  border-radius: 14px;
  flex-shrink: 0;
}

.club-skeleton {
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
.sk-title { height: 16px; width: 55%; }
.sk-desc  { height: 13px; width: 90%; }
.sk-meta  { height: 13px; width: 40%; }

/* ========== Banner 轮播区 ========== */
.banner-wrap {
  position: relative;
  margin: 12px 16px 0;
}

.club-swiper {
  width: 100%;
  height: 190px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.22);
}

.club-banner {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  cursor: pointer;
}

/* 大号序号水印 */
.banner-watermark {
  position: absolute;
  right: -8px;
  top: -16px;
  font-size: 96px;
  font-weight: 900;
  color: rgba(255, 255, 255, 0.06);
  line-height: 1;
  letter-spacing: -4px;
  pointer-events: none;
  user-select: none;
}

/* 斜切光条装饰 */
.banner-slash {
  position: absolute;
  bottom: -10px;
  left: -20px;
  width: 120px;
  height: 120px;
  background: rgba(255, 255, 255, 0.06);
  transform: rotate(35deg);
  pointer-events: none;
  border-radius: 8px;
}

/* 内容 */
.banner-content {
  position: relative;
  padding: 16px 18px 14px;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-sizing: border-box;
}

.banner-top-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.banner-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 6px;
  padding: 4px 10px;
}

.badge-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #FFD700;
  display: inline-block;
  animation: dotBlink 1.8s ease-in-out infinite;
}

@keyframes dotBlink {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.3; transform: scale(0.7); }
}

.badge-text {
  font-size: 10px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.85);
  letter-spacing: 1px;
  text-transform: uppercase;
}

/* 加入按钮 */
.banner-btn {
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

  /* 已加入：镂空白色描边 */
  &--joined {
    background: rgba(255, 255, 255, 0.12);
    border: 1px solid rgba(255, 255, 255, 0.35);
    box-shadow: none;

    .banner-btn-text { color: rgba(255, 255, 255, 0.8); }
  }

  /* 加载中 */
  &--loading {
    opacity: 0.5;
    pointer-events: none;
  }
}

.banner-btn-text {
  font-size: 12px;
  font-weight: 800;
  color: #111;
  letter-spacing: 0.2px;
  white-space: nowrap;
}

/* 中部大字区 */
.banner-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding-bottom: 4px;
}

.banner-title {
  font-size: 22px;
  font-weight: 900;
  color: #FFFFFF;
  line-height: 1.2;
  letter-spacing: -0.5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
}

.banner-desc {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  line-height: 1.4;
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 底部数据统计 + 按钮一行 */
.banner-stats {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-top: 10px;
  border-top: 1px solid rgba(255, 255, 255, 0.12);
}

.banner-stat {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.banner-stat-num {
  font-size: 17px;
  font-weight: 900;
  color: #FFFFFF;
  line-height: 1;
  letter-spacing: -0.5px;
}

.banner-stat-label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
  font-weight: 500;
}

.banner-stat-divider {
  width: 1px;
  height: 18px;
  background: rgba(255, 255, 255, 0.18);
}

/* 推开按钮到右侧 */
.banner-stat-spacer {
  flex: 1;
}

/* ========== 轮播指示器（进度条 + 计数） ========== */
.swiper-indicator {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 12px;
  padding: 0 2px;
}

.swiper-track {
  flex: 1;
  height: 2px;
  background: rgba(0, 0, 0, 0.08);
  border-radius: 2px;
  overflow: hidden;
}

.swiper-progress {
  height: 100%;
  background: $campus-blue;
  border-radius: 2px;
  transition: width 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.swiper-counter {
  flex-shrink: 0;
}

.swiper-counter-cur {
  font-size: 13px;
  font-weight: 800;
  color: $campus-blue;
}

.swiper-counter-sep {
  font-size: 11px;
  color: rgba(0, 0, 0, 0.2);
}

.swiper-counter-total {
  font-size: 11px;
  font-weight: 500;
  color: rgba(0, 0, 0, 0.3);
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
  background: $campus-blue;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: $color-text-primary;
}


.section-count {
  font-size: 13px;
  color: $color-text-quaternary;
}

/* ========== 热门横滑 ========== */
.hot-scroll {
  width: 100%;
}

.hot-list {
  display: flex;
  gap: 12px;
  padding: 8px 0 20px;
  width: max-content;
}

.hot-list-pad {
  width: 4px;
  flex-shrink: 0;
}

/* ── 卡片主体：浅色卡片式 ── */
.hot-card {
  position: relative;
  width: 120px;
  border-radius: 18px;
  overflow: hidden;
  flex-shrink: 0;
  cursor: pointer;
  background: #FFFFFF;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.07), 0 0 0 1px rgba(0,0,0,0.04);
  transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.2s ease;
  display: flex;
  flex-direction: column;

  &:active {
    transform: scale(0.94);
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  }
}

/* 封面图：卡片上半部分 */
.hot-card__img {
  width: 100%;
  height: 100px;
  object-fit: cover;
  display: block;
  flex-shrink: 0;
}

/* 无图占位：主题色渐变 */
.hot-card__placeholder {
  width: 100%;
  height: 100px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(
    145deg,
    var(--card-color) 0%,
    var(--card-color-dim, #111) 100%
  );
  overflow: hidden;
  position: relative;
}

.hot-card__initial {
  position: relative;
  z-index: 2;
  font-size: 42px;
  font-weight: 900;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1;
  letter-spacing: -2px;
}

/* 装饰圆环（占位时） */
.hot-card__pl-ring {
  position: absolute;
  border-radius: 50%;
  border: 1.5px solid rgba(255, 255, 255, 0.15);

  &--1 {
    width: 120px;
    height: 120px;
    bottom: -40px;
    right: -30px;
  }

  &--2 {
    width: 70px;
    height: 70px;
    top: -20px;
    left: -20px;
    border-color: rgba(255, 255, 255, 0.08);
  }
}

/* 全卡渐变遮罩：不需要，改浅色后移除覆盖层 */
.hot-card__overlay {
  display: none;
}

/* ── 排名角标：卡片封面左上 ── */
.hot-card__rank {
  position: absolute;
  top: 8px;
  left: 8px;
  z-index: 3;
  height: 22px;
  padding: 0 8px;
  display: flex;
  align-items: center;
  background: rgba(0, 0, 0, 0.38);
  backdrop-filter: blur(6px);
  border-radius: 7px;

  &--hot {
    background: var(--card-color);

    .hot-card__rank-text {
      color: #fff;
    }
  }
}

.hot-card__rank-text {
  font-size: 10px;
  font-weight: 800;
  color: rgba(255, 255, 255, 0.9);
  letter-spacing: 0.3px;
  line-height: 1;
}

/* ── 卡片信息区（图片下方白色区域） ── */
.hot-card__bottom {
  flex: 1;
  padding: 10px 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.hot-card__name {
  font-size: 13px;
  font-weight: 700;
  color: #1A1A2E;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  letter-spacing: -0.2px;
}

.hot-card__meta {
  display: flex;
  align-items: center;
  gap: 3px;
}

.hot-card__meta-icon {
  color: #9CA3AF;
}

.hot-card__count {
  font-size: 11px;
  font-weight: 500;
  color: #9CA3AF;
  line-height: 1;
}

/* 主题色底部色条（替代发光效果） */
.hot-card__glow-bar {
  width: 100%;
  height: 3px;
  border-radius: 0 0 18px 18px;
  background: var(--card-color);
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  opacity: 0.7;
}

/* 前3名：主题色顶部细边线替代脉冲动画 */
.hot-card__pulse {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: var(--card-color);
  border-radius: 18px 18px 0 0;
  pointer-events: none;
}

/* ========== 全部社团列表 ========== */
.club-items {
  padding: 0 16px 4px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* ── 全部社团卡片：浅色横向卡，与热门卡片语言统一 ── */
.club-card {
  position: relative;
  border-radius: 18px;
  overflow: hidden;
  cursor: pointer;
  background: #FFFFFF;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.07), 0 0 0 1px rgba(0,0,0,0.04);
  display: flex;
  flex-direction: row;
  transition: transform 0.18s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.18s ease;

  &:active {
    transform: scale(0.97);
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  }
}

/* 左侧封面图 */
.club-card__bg {
  width: 90px;
  height: 90px;
  object-fit: cover;
  flex-shrink: 0;
  display: block;
  align-self: center;
  margin: 10px 0 10px 10px;
  border-radius: 12px;
}

/* 无图占位 */
.club-card__bg-placeholder {
  width: 90px;
  height: 90px;
  flex-shrink: 0;
  align-self: center;
  margin: 10px 0 10px 10px;
  border-radius: 12px;
  background: linear-gradient(
    145deg,
    var(--cat-color, #377DFF) 0%,
    var(--cat-color-dim, #0A2A6E) 100%
  );
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}

.club-card__bg-initial {
  font-size: 36px;
  font-weight: 900;
  color: rgba(255, 255, 255, 0.9);
  letter-spacing: -1.5px;
  line-height: 1;
  position: relative;
  z-index: 2;
}

.club-card__bg-ring {
  position: absolute;
  border-radius: 50%;
  border: 1.5px solid rgba(255, 255, 255, 0.15);

  &--1 {
    width: 110px;
    height: 110px;
    bottom: -35px;
    right: -30px;
  }

  &--2 {
    width: 65px;
    height: 65px;
    top: -20px;
    left: -15px;
    border-color: rgba(255, 255, 255, 0.08);
  }
}

/* 遮罩层不需要 */
.club-card__overlay { display: none; }

/* 右上角分类标签 */
.club-card__cat {
  position: absolute;
  top: 10px;
  right: 12px;
  z-index: 3;
  display: inline-flex;
  align-items: center;
  height: 20px;
  padding: 0 8px;
  border-radius: 6px;
  background: var(--cat-color, #377DFF);
}

.club-card__cat-text {
  font-size: 10px;
  font-weight: 700;
  color: #FFFFFF;
  letter-spacing: 0.5px;
}

/* 右侧内容区 */
.club-card__bottom {
  flex: 1;
  min-width: 0;
  padding: 14px 12px 12px 12px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
}

/* 分类色顶部细线 */
.club-card__glow-bar {
  width: 20px;
  height: 2.5px;
  border-radius: 2px;
  background: var(--cat-color, #377DFF);
  margin-bottom: 2px;
}

.club-card__title {
  font-size: 15px;
  font-weight: 700;
  color: #1A1A2E;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  letter-spacing: -0.2px;
  // 为右上角分类标签留出空间
  padding-right: 56px;
}

.club-card__desc {
  font-size: 11px;
  color: #9CA3AF;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}

/* 底行：统计 + 按钮 */
.club-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 3px;
}

.club-card__meta {
  display: flex;
  align-items: center;
  gap: 4px;
}

.club-meta-icon {
  color: #9CA3AF;
  flex-shrink: 0;
}

.club-meta-val {
  font-size: 11px;
  font-weight: 500;
  color: #9CA3AF;
  line-height: 1;
}

.club-meta-dot {
  width: 2.5px;
  height: 2.5px;
  border-radius: 50%;
  background: #D1D5DB;
  margin: 0 2px;
}

/* 加入按钮：与热门卡片底部色条同系，分类色实心 */
.club-card__btn {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  height: 26px;
  padding: 0 13px;
  border-radius: 8px;
  background: var(--cat-color, #377DFF);
  transition: opacity 0.15s ease, transform 0.12s ease;

  &:active {
    opacity: 0.8;
    transform: scale(0.92);
  }

  &--joined {
    background: transparent;
    border: 1.5px solid #E5E7EB;
  }

  &--loading {
    opacity: 0.45;
    pointer-events: none;
  }
}

.club-card__btn-text {
  font-size: 12px;
  font-weight: 700;
  color: #FFFFFF;
  letter-spacing: 0.2px;

  .club-card__btn--joined & {
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
  background: $campus-blue;
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

/* ========== PC 端双列布局 ========== */
@media (min-width: 1024px) {
  .banner-wrap {
    margin: 16px 80px 0;
  }

  .club-swiper {
    height: 200px;
  }

  .section-header {
    padding: 20px 80px 12px;
  }

  .hot-list-pad {
    width: 68px;
  }

  .club-items {
    padding: 0 80px;
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}
</style>
