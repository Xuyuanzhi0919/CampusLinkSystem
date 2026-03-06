<template>
  <!-- ========== 成就展示区 ========== -->
  <view class="achievement-section">

    <!-- ①② 数据统计 + 等级进度：合并为「个人数据概览」单张卡片，减少信息割裂感 -->
    <view class="data-overview-card" :class="{ 'data-overview-card--maxed': isMaxed }">

      <!-- 上半：4 项统计数据 -->
      <view class="stats-row">
        <view
          v-for="(stat, i) in stats"
          :key="stat.key"
          class="stat-cell"
          :style="{ animationDelay: `${i * 0.07}s` }"
          @click="$emit('statClick', stat.key)"
        >
          <text class="stat-num">{{ formatNum(stat.value) }}</text>
          <text class="stat-lbl">{{ stat.label }}</text>
        </view>
      </view>

      <!-- 内部分隔线 -->
      <view class="overview-divider" />

      <!-- 下半：等级 + 进度条 -->
      <view class="level-section">
        <view class="level-head">
          <view class="level-lhs">
            <view class="level-icon">
              <text class="level-icon-label">Lv</text>
              <text class="level-icon-num">{{ level }}</text>
            </view>
            <view class="level-text">
              <text class="level-name">{{ levelName }}</text>
              <text class="level-hint">{{ isMaxed ? '已达最高等级 🎉' : `还差 ${expToNextLevel.toLocaleString()} 积分升级` }}</text>
            </view>
          </view>
          <view class="level-pct-wrap">
            <text class="level-pct" :class="{ 'level-pct--maxed': isMaxed }">{{ progressPercent }}</text>
            <text class="level-pct-sign" :class="{ 'level-pct--maxed': isMaxed }">%</text>
          </view>
        </view>
        <view class="prog-track">
          <view
            class="prog-fill"
            :class="{ 'prog-fill--maxed': isMaxed }"
            :style="{ width: progressPercent + '%' }"
          />
        </view>
        <view class="prog-labels">
          <text class="prog-cur">{{ currentExp.toLocaleString() }} / {{ nextLevelExp.toLocaleString() }}</text>
          <text v-if="!isMaxed" class="prog-max">下一级 Lv.{{ level + 1 }}</text>
          <text v-else class="prog-max prog-max--maxed">已满级 ✦</text>
        </view>
      </view>
    </view>

    <!-- ③ 徽章 — 统一图标风格 -->
    <view class="badges-card">
      <view class="badges-head">
        <text class="badges-title">我的徽章</text>
        <view class="badges-more" @click="$emit('viewAllBadges')">
          <text class="badges-more-text">全部</text>
          <Icon name="chevron-right" :size="13" class="badges-more-icon" />
        </view>
      </view>
      <scroll-view class="badges-scroll" scroll-x :show-scrollbar="false">
        <view class="badges-list">
          <view
            v-for="(badge, i) in displayBadges"
            :key="badge.id"
            class="badge-item"
            :class="{ 'badge-item--locked': !badge.unlocked }"
            :style="{ animationDelay: `${i * 0.05}s` }"
            @click="$emit('badgeClick', badge)"
          >
            <view class="badge-icon-wrap" :class="badge.unlocked ? `badge-icon-wrap--${badgeColors[i % badgeColors.length]}` : 'badge-icon-wrap--locked'">
              <Icon :name="badge.icon" :size="20" class="badge-icon" :stroke-width="1.6" />
            </view>
            <text class="badge-name">{{ badge.name }}</text>
            <view v-if="badge.unlocked" class="badge-unlocked-dot" />
          </view>
        </view>
      </scroll-view>
    </view>

  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Icon from '@/components/icons/index.vue'

interface Badge {
  id: number
  name: string
  icon: string
  unlocked: boolean
  description?: string
}

interface Stat {
  key: string
  label: string
  value: number
}

interface Props {
  level: number
  levelName?: string
  currentExp: number
  nextLevelExp: number
  stats: Stat[]
  badges: Badge[]
}

const props = withDefaults(defineProps<Props>(), {
  levelName: '校园新星',
  level: 1,
  currentExp: 0,
  nextLevelExp: 100,
  stats: () => [],
  badges: () => []
})

defineEmits<{
  statClick: [key: string]
  badgeClick: [badge: Badge]
  viewAllBadges: []
}>()

const badgeColors = ['blue', 'teal', 'violet', 'amber', 'rose']

const progressPercent = computed(() =>
  Math.min(Math.round((props.currentExp / props.nextLevelExp) * 100), 100)
)

const expToNextLevel = computed(() => Math.max(props.nextLevelExp - props.currentExp, 0))

const isMaxed = computed(() => props.currentExp >= props.nextLevelExp)

const displayBadges = computed(() => props.badges)

const formatNum = (n: number) => n >= 1000 ? (n / 1000).toFixed(1) + 'k' : String(n)
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.achievement-section {
  padding: 0 20rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;

  @media (min-width: 1024px) {
    padding: 0;
    gap: 12px;
  }
}

/* ─── 个人数据概览卡片（统计 + 等级合并） ─── */
.data-overview-card {
  background: $color-bg-card;
  border-radius: 20rpx;
  box-shadow: $shadow-sm;
  border: 1rpx solid $color-border-light;
  overflow: hidden;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;

  /* 满级时：金色边框光晕 */
  &--maxed {
    border-color: rgba(251, 191, 36, 0.35);
    box-shadow: $shadow-sm, 0 0 0 1px rgba(251, 191, 36, 0.12);
  }

  @media (min-width: 1024px) {
    border-radius: 14px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.07), 0 1px 3px rgba(0, 0, 0, 0.04);

    &--maxed {
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.07), 0 0 0 1px rgba(251, 191, 36, 0.18);
    }
  }
}

/* ─── 上半：统计行（去除独立卡片外观，融入 overview-card） ─── */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
}

.stat-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 28rpx 8rpx 24rpx;
  cursor: pointer;
  position: relative;
  transition: background 0.18s;

  &:not(:last-child)::after {
    content: '';
    position: absolute;
    right: 0;
    top: 20%;
    bottom: 20%;
    width: 1rpx;
    background: $color-divider;
  }

  &:active { background: $color-bg-hover; }

  // #ifdef H5
  &:hover { background: $color-bg-hover; }
  // #endif

  @media (min-width: 1024px) {
    padding: 20px 8px 16px;
  }
}

.stat-num {
  font-size: 32rpx;
  font-weight: 700;
  color: $color-text-primary;
  line-height: 1;
  letter-spacing: -0.01em;

  @media (min-width: 1024px) {
    font-size: 21px;
    color: $campus-blue;
  }
}

.stat-lbl {
  margin-top: 8rpx;
  font-size: 20rpx;
  color: $color-text-tertiary;
  font-weight: 400;

  @media (min-width: 1024px) {
    margin-top: 5px;
    font-size: 12px;
  }
}

/* ─── 卡片内分隔线 ─── */
.overview-divider {
  height: 1rpx;
  background: $color-divider;
  margin: 0 24rpx;

  @media (min-width: 1024px) {
    height: 1px;
    margin: 0 20px;
  }
}

/* ─── 下半：等级 section ─── */
.level-section {
  padding: 24rpx 28rpx 22rpx;

  @media (min-width: 1024px) {
    padding: 18px 24px 16px;
  }
}

.level-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18rpx;

  @media (min-width: 1024px) {
    margin-bottom: 12px;
  }
}

.level-lhs {
  display: flex;
  align-items: center;
  gap: 18rpx;

  @media (min-width: 1024px) {
    gap: 13px;
  }
}

.level-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 16rpx;
  background: linear-gradient(135deg, $campus-blue 0%, $campus-blue-light 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  flex-shrink: 0;
  gap: 0;
  box-shadow: 0 4rpx 12rpx rgba($campus-blue, 0.28);

  @media (min-width: 1024px) {
    width: 40px;
    height: 40px;
    border-radius: 11px;
  }
}

.level-icon-label {
  font-size: 13rpx;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1;
  letter-spacing: 0.04em;

  @media (min-width: 1024px) {
    font-size: 8px;
  }
}

.level-icon-num {
  font-size: 28rpx;
  font-weight: 800;
  color: #fff;
  line-height: 1;

  @media (min-width: 1024px) {
    font-size: 17px;
  }
}

.level-text {
  display: flex;
  flex-direction: column;
  gap: 5rpx;

  @media (min-width: 1024px) {
    gap: 3px;
  }
}

.level-name {
  font-size: 28rpx;
  font-weight: 600;
  color: $color-text-primary;
  line-height: 1;

  @media (min-width: 1024px) {
    font-size: 14px;
  }
}

.level-hint {
  font-size: 20rpx;
  color: $color-text-tertiary;
  font-weight: 400;

  @media (min-width: 1024px) {
    font-size: 11px;
  }
}

.level-pct-wrap {
  display: flex;
  align-items: baseline;
  gap: 1px;
}

.level-pct {
  font-size: 30rpx;
  font-weight: 700;
  color: $campus-blue;
  line-height: 1;
  transition: color 0.3s;

  &--maxed { color: #f59e0b; }

  @media (min-width: 1024px) {
    font-size: 18px;
  }
}

.level-pct-sign {
  font-size: 18rpx;
  font-weight: 600;
  color: rgba($campus-blue, 0.7);
  transition: color 0.3s;

  &.level-pct--maxed { color: rgba(#f59e0b, 0.8); }

  @media (min-width: 1024px) {
    font-size: 11px;
  }
}

/* 进度条 */
.prog-track {
  width: 100%;
  height: 7rpx;
  background: $color-border-light;
  border-radius: 100rpx;
  overflow: hidden;

  @media (min-width: 1024px) {
    height: 5px;
    border-radius: 100px;
  }
}

.prog-fill {
  height: 100%;
  background: linear-gradient(90deg, $campus-blue 0%, $campus-blue-light 100%);
  border-radius: 100rpx;
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);

  /* 满级：金色渐变 + 光晕 */
  &--maxed {
    background: linear-gradient(90deg, #f59e0b 0%, #fcd34d 50%, #fbbf24 100%);
    box-shadow: 0 0 8px rgba(251, 191, 36, 0.45);
  }
}

.prog-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 12rpx;

  @media (min-width: 1024px) {
    margin-top: 7px;
  }
}

.prog-cur {
  font-size: 19rpx;
  color: $color-text-tertiary;
  font-weight: 400;

  @media (min-width: 1024px) {
    font-size: 11px;
  }
}

.prog-max {
  font-size: 19rpx;
  color: $campus-blue;
  font-weight: 500;

  &--maxed {
    color: #f59e0b;
    font-weight: 600;
  }

  @media (min-width: 1024px) {
    font-size: 11px;
  }
}

/* ─── ③ 徽章 ─── */
.badges-card {
  background: $color-bg-card;
  border-radius: 20rpx;
  padding: 26rpx 24rpx 22rpx;
  box-shadow: $shadow-sm;
  border: 1rpx solid $color-border-light;

  @media (min-width: 1024px) {
    border-radius: 14px;
    padding: 18px 20px 14px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.07), 0 1px 3px rgba(0, 0, 0, 0.04);
  }
}

.badges-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18rpx;

  @media (min-width: 1024px) {
    margin-bottom: 14px;
  }
}

.badges-title {
  font-size: 28rpx;
  font-weight: 600;
  color: $color-text-primary;

  @media (min-width: 1024px) {
    font-size: 14px;
  }
}

.badges-more {
  display: flex;
  align-items: center;
  gap: 2rpx;
  cursor: pointer;
  padding: 4rpx 8rpx;
  border-radius: 8rpx;
  transition: background 0.16s;

  &:active { background: $color-bg-hover; }

  // #ifdef H5
  &:hover { background: $color-bg-hover; }
  // #endif
}

.badges-more-text {
  font-size: 20rpx;
  color: $color-text-tertiary;

  @media (min-width: 1024px) {
    font-size: 12px;
  }
}

.badges-more-icon {
  color: $color-text-tertiary;
}

/* 横向滚动容器 */
.badges-scroll {
  width: 100%;
  overflow: hidden;
}

/* 单行 flex，不换行，靠滚动容器撑开水平方向 */
.badges-list {
  display: flex;
  flex-direction: row;
  flex-wrap: nowrap;
  gap: 6rpx;

  @media (min-width: 1024px) {
    gap: 4px;
  }
}

.badge-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
  width: 110rpx;   /* 固定宽：横向滚动时每格等宽 */
  gap: 10rpx;
  padding: 16rpx 4rpx 14rpx;
  border-radius: 14rpx;
  cursor: pointer;
  transition: background 0.18s;
  position: relative;

  &:active { background: $color-bg-hover; }

  // #ifdef H5
  &:hover { background: $color-bg-hover; }
  // #endif

  /* 未解锁：灰度 + 低透明度，已解锁保持完全可见 */
  &--locked {
    opacity: 0.28;
    filter: grayscale(40%);
  }

  @media (min-width: 1024px) {
    width: 72px;
    padding: 12px 4px 10px;
    border-radius: 10px;
    gap: 7px;
  }
}

.badge-icon-wrap {
  width: 64rpx;
  height: 64rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &--locked {
    background: $color-bg-hover;
    .badge-icon { color: $color-text-placeholder; }
  }

  &--blue   { background: #EBF3FF; .badge-icon { color: #3B82F6; } }
  &--teal   { background: #ECFDF5; .badge-icon { color: #0D9488; } }
  &--violet { background: #F5F3FF; .badge-icon { color: #7C3AED; } }
  &--amber  { background: #FFFBEB; .badge-icon { color: #B45309; } }
  &--rose   { background: #FFF1F2; .badge-icon { color: #E11D48; } }

  @media (min-width: 1024px) {
    width: 40px;
    height: 40px;
    border-radius: 11px;
  }
}

.badge-name {
  font-size: 18rpx;
  color: $color-text-secondary;
  text-align: center;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  width: 100%;

  @media (min-width: 1024px) {
    font-size: 11px;
  }
}

.badge-unlocked-dot {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  width: 10rpx;
  height: 10rpx;
  background: #22c55e;
  border-radius: 50%;
  border: 2px solid $color-bg-card;
  box-shadow: 0 1px 4px rgba(34, 197, 94, 0.4);

  @media (min-width: 1024px) {
    width: 7px;
    height: 7px;
    top: 8px;
    right: 8px;
  }
}
</style>
