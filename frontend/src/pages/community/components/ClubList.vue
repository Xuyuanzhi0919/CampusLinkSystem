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
              <!-- 装饰圆圈 -->
              <view class="banner-deco deco-1"></view>
              <view class="banner-deco deco-2"></view>
              <view class="banner-deco deco-3"></view>
              <view class="banner-content">
                <view class="banner-badge">
                  <text class="badge-dot"></text>
                  <text class="badge-text">社区精选</text>
                </view>
                <text class="banner-title">{{ club.clubName }}</text>
                <text class="banner-desc">{{ club.description || '加入我们，一起创造校园精彩' }}</text>
                <view class="banner-meta">
                  <view class="banner-meta-item">
                    <Icon name="users" :size="13" class="banner-meta-icon" />
                    <text>{{ club.memberCount || 0 }} 名成员</text>
                  </view>
                  <view class="banner-meta-item">
                    <Icon name="target" :size="13" class="banner-meta-icon" />
                    <text>{{ club.activityCount || 0 }} 场活动</text>
                  </view>
                </view>
                <view class="banner-btn" @click.stop="handleJoinClub(club)">
                  <text>{{ club.isMember ? '已加入' : '立即加入' }}</text>
                </view>
              </view>
            </view>
          </swiper-item>
        </swiper>

        <!-- 指示点 -->
        <view class="swiper-dots">
          <view
            v-for="(_, idx) in bannerClubs"
            :key="idx"
            class="swiper-dot"
            :class="{ 'swiper-dot--active': idx === swiperCurrent }"
          ></view>
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
          <view
            v-for="(item, idx) in hotClubs"
            :key="item.clubId"
            class="hot-card"
            :style="{ '--card-color': hotCardColors[idx % hotCardColors.length] }"
            @click="handleClubClick(item.clubId)"
          >
            <view class="hot-card__bg"></view>
            <view class="hot-card__rank">{{ idx + 1 }}</view>
            <view class="hot-card__icon">
              <image v-if="item.logoUrl" :src="item.logoUrl" class="hot-card__img" mode="aspectFill" />
              <view v-else class="hot-card__icon-placeholder">
                <text class="placeholder-text">{{ item.clubName?.slice(0, 1) }}</text>
              </view>
            </view>
            <text class="hot-card__name">{{ item.clubName }}</text>
            <text class="hot-card__count">{{ item.memberCount || 0 }}人</text>
          </view>
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
          v-for="item in list"
          :key="item.clubId"
          class="club-card"
          @click="handleClubClick(item.clubId)"
        >
          <!-- 左侧图标 -->
          <view class="club-card__left">
            <view class="club-card__icon">
              <image v-if="item.logoUrl" :src="item.logoUrl" class="club-cover-img" mode="aspectFill" />
              <view v-else class="club-icon-placeholder">
                <text class="placeholder-text">{{ item.clubName?.slice(0, 1) }}</text>
              </view>
            </view>
          </view>

          <!-- 右侧信息 -->
          <view class="club-card__right">
            <view class="club-card__top">
              <text class="club-card__title">{{ item.clubName }}</text>
              <view class="club-card__capsule">
                <text>{{ item.category || '综合' }}</text>
              </view>
            </view>
            <text v-if="item.description" class="club-card__desc">{{ item.description }}</text>
            <view class="club-card__bottom">
              <view class="club-card__meta">
                <Icon name="users" :size="12" class="meta-icon" />
                <text class="meta-text">{{ item.memberCount || 0 }}人</text>
                <text class="meta-dot">·</text>
                <Icon name="target" :size="12" class="meta-icon" />
                <text class="meta-text">{{ item.activityCount || 0 }}场</text>
              </view>
              <view
                class="club-card__btn"
                :class="{
                  'club-card__btn--joined': item.isMember,
                  'club-card__btn--loading': joiningIds.has(item.clubId)
                }"
                @click.stop="handleJoinClub(item)"
              >
                <text>{{ joiningIds.has(item.clubId) ? '...' : (item.isMember ? '已加入' : '加入') }}</text>
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

// 热门社团（前 6 个，横滑展示）
const hotClubs = computed(() => props.list.slice(0, 6))

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
  height: 172px;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(55, 125, 255, 0.2);
}

.club-banner {
  position: relative;
  width: 100%;
  height: 100%;
  cursor: pointer;
}

/* 装饰圆圈 */
.banner-deco {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.06);
}
.deco-1 {
  width: 160px;
  height: 160px;
  right: -40px;
  top: -40px;
}
.deco-2 {
  width: 100px;
  height: 100px;
  right: 40px;
  bottom: -30px;
  background: rgba(255, 255, 255, 0.04);
}
.deco-3 {
  width: 60px;
  height: 60px;
  left: 30%;
  top: -20px;
  background: rgba(255, 255, 255, 0.08);
}

.banner-content {
  position: relative;
  padding: 18px 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-sizing: border-box;
}

.banner-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 3px 10px;
  width: fit-content;
}

.badge-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #FFD700;
  display: inline-block;
  animation: dotBlink 1.5s ease-in-out infinite;
}

@keyframes dotBlink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.badge-text {
  font-size: 11px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  letter-spacing: 0.5px;
}

.banner-title {
  font-size: 20px;
  font-weight: 800;
  color: #FFFFFF;
  line-height: 1.3;
  margin-top: 4px;
  letter-spacing: -0.3px;
}

.banner-desc {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.5;
  margin-top: 2px;
  flex: 1;
}

.banner-meta {
  display: flex;
  gap: 16px;
  margin-top: 4px;
}

.banner-meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.banner-meta-icon {
  color: rgba(255, 255, 255, 0.8);
  flex-shrink: 0;
}

.banner-btn {
  position: absolute;
  right: 20px;
  bottom: 18px;
  background: rgba(255, 255, 255, 0.95);
  color: #1A1A2E;
  font-size: 12px;
  font-weight: 700;
  padding: 6px 16px;
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);

  &:active { transform: scale(0.95); }
}

/* ========== 轮播指示点 ========== */
.swiper-dots {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 5px;
  margin-top: 10px;
}

.swiper-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: rgba(55, 125, 255, 0.25);
  transition: all 0.3s ease;

  &--active {
    width: 16px;
    border-radius: 3px;
    background: $campus-blue;
  }
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
  padding: 4px 16px 8px;
  width: max-content;
}

.hot-card {
  position: relative;
  width: 100px;
  padding: 14px 12px;
  border-radius: 14px;
  background: #FFFFFF;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  border: 1px solid rgba(0, 0, 0, 0.04);
  overflow: hidden;

  &:active {
    transform: scale(0.96);
  }
}

.hot-card__bg {
  position: absolute;
  inset: 0;
  opacity: 0.06;
  background: var(--card-color);
}

.hot-card__rank {
  position: absolute;
  top: 8px;
  left: 8px;
  font-size: 11px;
  font-weight: 800;
  color: var(--card-color);
  line-height: 1;
}

.hot-card__icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  margin-top: 4px;
  background: var(--card-color);
  display: flex;
  align-items: center;
  justify-content: center;
}

.hot-card__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder-text {
  font-size: 22px;
  font-weight: 800;
  color: #FFFFFF;
}

.hot-card__name {
  font-size: 12px;
  font-weight: 600;
  color: $color-text-primary;
  text-align: center;
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hot-card__count {
  font-size: 11px;
  color: $color-text-quaternary;
}

/* ========== 全部社团列表（横向信息流） ========== */
.club-items {
  padding: 0 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.club-card {
  display: flex;
  gap: 14px;
  padding: 14px;
  background: #FFFFFF;
  border-radius: 14px;
  cursor: pointer;
  transition: box-shadow 0.15s ease, transform 0.15s ease;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);

  &:active {
    transform: scale(0.985);
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  }
}

.club-card__left {
  flex-shrink: 0;
}

.club-card__icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  overflow: hidden;
  background: $campus-blue-lighter;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(55, 125, 255, 0.15);
}

.club-cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.club-icon-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, $campus-blue 0%, $campus-blue-light 100%);
  display: flex;
  align-items: center;
  justify-content: center;

  .placeholder-text {
    font-size: 22px;
    font-weight: 800;
    color: #FFFFFF;
  }
}

.club-card__right {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
  justify-content: center;
}

.club-card__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.club-card__title {
  font-size: 15px;
  font-weight: 700;
  color: $color-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.club-card__capsule {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  height: 20px;
  padding: 0 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
  color: $campus-blue;
  background: rgba($campus-blue, 0.1);
}

.club-card__desc {
  font-size: 12px;
  color: $color-text-tertiary;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.club-card__bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 2px;
}

.club-card__meta {
  display: flex;
  align-items: center;
  gap: 6px;
}

.meta-icon {
  color: $color-text-quaternary;
  flex-shrink: 0;
}

.meta-text {
  font-size: 11px;
  color: $color-text-quaternary;
}

.meta-dot {
  font-size: 11px;
  color: $color-text-quaternary;
}

.club-card__btn {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  padding: 5px 14px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 600;
  background: $campus-blue;
  color: #FFFFFF;
  transition: opacity 0.15s ease;

  &:active { opacity: 0.8; }

  &--joined {
    background: transparent;
    color: $color-text-tertiary;
    border: 1px solid $color-border;
  }

  &--loading {
    opacity: 0.55;
    pointer-events: none;
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

  .hot-list {
    padding: 4px 80px 8px;
  }

  .club-items {
    padding: 0 80px;
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }
}
</style>
