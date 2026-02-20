<template>
  <!-- ========== 成就展示区 ========== -->
  <view class="achievement-section">

    <!-- ① 数据统计 -->
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

    <!-- ② 等级进度 -->
    <view class="level-card">
      <view class="level-head">
        <view class="level-lhs">
          <view class="level-icon">
            <text class="level-icon-text">Lv</text>
            <text class="level-icon-num">{{ level }}</text>
          </view>
          <view class="level-text">
            <text class="level-name">{{ levelName }}</text>
            <text class="level-hint">还差 {{ expToNextLevel }} 升级</text>
          </view>
        </view>
        <text class="level-pct">{{ progressPercent }}%</text>
      </view>
      <view class="prog-track">
        <view class="prog-fill" :style="{ width: progressPercent + '%' }" />
      </view>
      <view class="prog-labels">
        <text class="prog-cur">{{ currentExp }} 积分</text>
        <text class="prog-max">目标 {{ nextLevelExp }}</text>
      </view>
    </view>

    <!-- ③ 徽章 -->
    <view class="badges-card">
      <view class="badges-head">
        <text class="badges-title">我的徽章</text>
        <view class="badges-more" @click="$emit('viewAllBadges')">
          <text class="badges-more-text">全部</text>
          <Icon name="chevron-right" :size="13" class="badges-more-icon" />
        </view>
      </view>
      <view class="badges-list">
        <view
          v-for="(badge, i) in displayBadges"
          :key="badge.id"
          class="badge-item"
          :class="{ 'badge-item--locked': !badge.unlocked }"
          :style="{ animationDelay: `${i * 0.05}s` }"
          @click="$emit('badgeClick', badge)"
        >
          <view class="badge-icon-wrap" :class="{ 'badge-icon-wrap--locked': !badge.unlocked }">
            <Icon :name="badge.icon" :size="22" class="badge-icon" />
          </view>
          <text class="badge-name">{{ badge.name }}</text>
          <view v-if="badge.unlocked" class="badge-dot" />
        </view>
      </view>
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

const progressPercent = computed(() =>
  Math.min(Math.round((props.currentExp / props.nextLevelExp) * 100), 100)
)

const expToNextLevel = computed(() => Math.max(props.nextLevelExp - props.currentExp, 0))

const displayBadges = computed(() => props.badges.slice(0, 5))

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

/* ─── ① 数据统计 ─── */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  background: $color-bg-card;
  border-radius: 20rpx;
  box-shadow: $shadow-sm;
  border: 1rpx solid $color-border-light;
  overflow: hidden;

  @media (min-width: 1024px) {
    border-radius: 14px;
  }
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
    padding: 22px 8px 18px;
  }
}

.stat-num {
  font-size: 34rpx;
  font-weight: 700;
  color: $color-text-primary;
  line-height: 1;
  letter-spacing: -0.01em;

  @media (min-width: 1024px) {
    font-size: 22px;
  }
}

.stat-lbl {
  margin-top: 7rpx;
  font-size: 20rpx;
  color: $color-text-tertiary;
  font-weight: 400;

  @media (min-width: 1024px) {
    margin-top: 5px;
    font-size: 12px;
  }
}

/* ─── ② 等级进度 ─── */
.level-card {
  background: $color-bg-card;
  border-radius: 20rpx;
  padding: 28rpx 28rpx 24rpx;
  box-shadow: $shadow-sm;
  border: 1rpx solid $color-border-light;

  @media (min-width: 1024px) {
    border-radius: 14px;
    padding: 20px 24px 18px;
  }
}

.level-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;

  @media (min-width: 1024px) {
    margin-bottom: 14px;
  }
}

.level-lhs {
  display: flex;
  align-items: center;
  gap: 16rpx;

  @media (min-width: 1024px) {
    gap: 12px;
  }
}

.level-icon {
  width: 60rpx;
  height: 60rpx;
  border-radius: 14rpx;
  background: linear-gradient(135deg, $campus-blue 0%, $campus-blue-light 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  flex-shrink: 0;
  gap: 0;

  @media (min-width: 1024px) {
    width: 40px;
    height: 40px;
    border-radius: 10px;
  }
}

.level-icon-text {
  font-size: 14rpx;
  font-weight: 600;
  color: rgba(255,255,255,0.75);
  line-height: 1;
  letter-spacing: 0.02em;

  @media (min-width: 1024px) {
    font-size: 9px;
  }
}

.level-icon-num {
  font-size: 26rpx;
  font-weight: 800;
  color: #fff;
  line-height: 1;

  @media (min-width: 1024px) {
    font-size: 16px;
  }
}

.level-text {
  display: flex;
  flex-direction: column;
  gap: 4rpx;

  @media (min-width: 1024px) {
    gap: 2px;
  }
}

.level-name {
  font-size: 26rpx;
  font-weight: 600;
  color: $color-text-primary;
  line-height: 1;

  @media (min-width: 1024px) {
    font-size: 15px;
  }
}

.level-hint {
  font-size: 20rpx;
  color: $color-text-tertiary;

  @media (min-width: 1024px) {
    font-size: 12px;
  }
}

.level-pct {
  font-size: 26rpx;
  font-weight: 700;
  color: $campus-blue;

  @media (min-width: 1024px) {
    font-size: 15px;
  }
}

/* 进度条 */
.prog-track {
  width: 100%;
  height: 8rpx;
  background: $color-border-light;
  border-radius: 100rpx;
  overflow: hidden;

  @media (min-width: 1024px) {
    height: 6px;
    border-radius: 100px;
  }
}

.prog-fill {
  height: 100%;
  background: linear-gradient(90deg, $campus-blue 0%, $campus-blue-light 100%);
  border-radius: 100rpx;
  transition: width 0.8s cubic-bezier(0.4,0,0.2,1);
}

.prog-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 12rpx;

  @media (min-width: 1024px) {
    margin-top: 8px;
  }
}

.prog-cur {
  font-size: 20rpx;
  color: $campus-blue;
  font-weight: 500;

  @media (min-width: 1024px) {
    font-size: 12px;
  }
}

.prog-max {
  font-size: 20rpx;
  color: $color-text-placeholder;

  @media (min-width: 1024px) {
    font-size: 12px;
  }
}

/* ─── ③ 徽章 ─── */
.badges-card {
  background: $color-bg-card;
  border-radius: 20rpx;
  padding: 24rpx 24rpx 20rpx;
  box-shadow: $shadow-sm;
  border: 1rpx solid $color-border-light;

  @media (min-width: 1024px) {
    border-radius: 14px;
    padding: 18px 20px 16px;
  }
}

.badges-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;

  @media (min-width: 1024px) {
    margin-bottom: 14px;
  }
}

.badges-title {
  font-size: 26rpx;
  font-weight: 600;
  color: $color-text-primary;

  @media (min-width: 1024px) {
    font-size: 15px;
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

.badges-list {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8rpx;

  @media (min-width: 1024px) {
    gap: 6px;
  }
}

.badge-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 14rpx 4rpx 12rpx;
  border-radius: 14rpx;
  cursor: pointer;
  transition: background 0.18s;
  position: relative;

  &:active { background: $color-bg-hover; }

  // #ifdef H5
  &:hover { background: $color-bg-hover; }
  // #endif

  &--locked { opacity: 0.35; }

  @media (min-width: 1024px) {
    padding: 12px 4px 10px;
    border-radius: 10px;
    gap: 6px;
  }
}

.badge-icon-wrap {
  width: 64rpx;
  height: 64rpx;
  border-radius: 15rpx;
  background: linear-gradient(135deg, #EFF6FF 0%, #DBEAFE 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &--locked {
    background: $color-bg-hover;
  }

  @media (min-width: 1024px) {
    width: 44px;
    height: 44px;
    border-radius: 11px;
  }
}

.badge-icon {
  color: $campus-blue;

  .badge-icon-wrap--locked & {
    color: $color-text-placeholder;
  }
}

.badge-name {
  font-size: 18rpx;
  color: $color-text-secondary;
  text-align: center;
  line-height: 1.2;
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

.badge-dot {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  width: 8rpx;
  height: 8rpx;
  background: #22c55e;
  border-radius: 50%;

  @media (min-width: 1024px) {
    width: 6px;
    height: 6px;
    top: 8px;
    right: 8px;
  }
}
</style>
