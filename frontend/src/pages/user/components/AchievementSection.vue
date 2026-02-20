<template>
  <!-- ========== 成就展示区 ========== -->
  <view class="achievement-section">

    <!-- ① 数据统计横排 -->
    <view class="stats-row">
      <view
        v-for="(stat, i) in stats"
        :key="stat.key"
        class="stat-cell"
        :style="{ animationDelay: `${i * 0.08}s` }"
        @click="$emit('statClick', stat.key)"
      >
        <text class="stat-num">{{ formatNum(stat.value) }}</text>
        <text class="stat-lbl">{{ stat.label }}</text>
        <view class="stat-bar">
          <view class="stat-bar-fill" :style="{ width: barWidth(stat.value) }" />
        </view>
      </view>
    </view>

    <!-- ② 等级进度卡片 -->
    <view class="level-card">
      <!-- 左侧：等级信息 -->
      <view class="level-left">
        <view class="level-badge-wrap">
          <text class="level-num">{{ level }}</text>
          <view class="level-ring" />
        </view>
        <view class="level-info">
          <text class="level-name">{{ levelName }}</text>
          <text class="level-sub">{{ expToNextLevel }} 经验升级</text>
        </view>
      </view>

      <!-- 右侧：进度条 -->
      <view class="level-right">
        <view class="prog-track">
          <view
            class="prog-fill"
            :style="{ width: progressPercent + '%' }"
          />
          <view
            class="prog-thumb"
            :style="{ left: progressPercent + '%' }"
          />
        </view>
        <view class="prog-labels">
          <text class="prog-cur">{{ currentExp }}</text>
          <text class="prog-max">{{ nextLevelExp }}</text>
        </view>
      </view>
    </view>

    <!-- ③ 徽章墙 -->
    <view class="badges-wrap">
      <view class="section-hd">
        <view class="section-hd-left">
          <view class="hd-dot" />
          <text class="section-hd-title">我的徽章</text>
        </view>
        <view class="section-hd-action" @click="$emit('viewAllBadges')">
          <text class="action-text">全部</text>
          <Icon name="chevron-right" :size="14" class="action-icon" />
        </view>
      </view>

      <view class="badges-list">
        <view
          v-for="(badge, i) in displayBadges"
          :key="badge.id"
          class="badge-card"
          :class="{ 'badge-card--locked': !badge.unlocked }"
          :style="{ animationDelay: `${i * 0.06}s` }"
          @click="$emit('badgeClick', badge)"
        >
          <view class="badge-icon-wrap" :class="{ 'badge-icon-wrap--locked': !badge.unlocked }">
            <Icon :name="badge.icon" :size="26" class="badge-icon" />
            <view v-if="!badge.unlocked" class="badge-lock-overlay">
              <Icon name="lock" :size="14" class="badge-lock-icon" />
            </view>
          </view>
          <text class="badge-name">{{ badge.name }}</text>
          <view v-if="badge.unlocked" class="badge-unlocked-dot" />
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

const expToNextLevel = computed(() => props.nextLevelExp - props.currentExp)

const displayBadges = computed(() => props.badges.slice(0, 5))

const maxStatValue = computed(() => Math.max(...props.stats.map(s => s.value), 1))

const barWidth = (val: number) => `${Math.round((val / maxStatValue.value) * 100)}%`

const formatNum = (n: number) => n >= 1000 ? (n / 1000).toFixed(1) + 'k' : n.toString()
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

.achievement-section {
  padding: 0 20rpx;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  animation: sectionIn 0.5s ease-out both;
}

@keyframes sectionIn {
  from { opacity: 0; transform: translateY(16rpx); }
  to   { opacity: 1; transform: translateY(0); }
}

/* ========== ① 数据统计横排 ========== */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12rpx;
}

.stat-cell {
  background: $color-bg-card;
  border-radius: 20rpx;
  padding: 28rpx 16rpx 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
  box-shadow: $shadow-sm;
  border: 1rpx solid $color-border-light;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4,0,0.2,1);
  animation: cellIn 0.4s ease-out both;

  &:active {
    transform: scale(0.95);
    box-shadow: $shadow-md;
  }

  // #ifdef H5
  &:hover {
    transform: translateY(-4rpx);
    box-shadow: $shadow-md;
    border-color: rgba($campus-blue, 0.2);
  }
  // #endif
}

@keyframes cellIn {
  from { opacity: 0; transform: translateY(12rpx); }
  to   { opacity: 1; transform: translateY(0); }
}

.stat-num {
  font-size: 36rpx;
  font-weight: 700;
  color: $color-text-primary;
  line-height: 1;
  letter-spacing: -0.02em;
}

.stat-lbl {
  font-size: 20rpx;
  color: $color-text-tertiary;
  font-weight: 400;
}

.stat-bar {
  width: 100%;
  height: 4rpx;
  background: $color-border-light;
  border-radius: 4rpx;
  margin-top: 6rpx;
  overflow: hidden;
}

.stat-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, $campus-blue 0%, $campus-blue-light 100%);
  border-radius: 4rpx;
  transition: width 0.8s cubic-bezier(0.4,0,0.2,1);
}

/* ========== ② 等级进度卡片 ========== */
.level-card {
  background: $color-bg-card;
  border-radius: 24rpx;
  padding: 28rpx 28rpx 28rpx 24rpx;
  box-shadow: $shadow-sm;
  border: 1rpx solid $color-border-light;
  display: flex;
  align-items: center;
  gap: 24rpx;
  overflow: hidden;
  position: relative;

  // 左侧装饰渐变条
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 4rpx;
    background: linear-gradient(180deg, $campus-blue 0%, #63CAB7 100%);
    border-radius: 4rpx;
  }
}

.level-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex-shrink: 0;
}

.level-badge-wrap {
  position: relative;
  width: 72rpx;
  height: 72rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.level-num {
  font-size: 32rpx;
  font-weight: 800;
  color: $campus-blue;
  line-height: 1;
  z-index: 1;
  position: relative;
}

.level-ring {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 3rpx solid;
  border-color: $campus-blue transparent $campus-blue transparent;
  animation: levelSpin 3s linear infinite;
  opacity: 0.4;
}

@keyframes levelSpin {
  to { transform: rotate(360deg); }
}

.level-info {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.level-name {
  font-size: 26rpx;
  font-weight: 600;
  color: $color-text-primary;
}

.level-sub {
  font-size: 20rpx;
  color: $color-text-tertiary;
}

.level-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.prog-track {
  position: relative;
  width: 100%;
  height: 10rpx;
  background: $color-border-light;
  border-radius: 10rpx;
  overflow: visible;
}

.prog-fill {
  height: 100%;
  background: linear-gradient(90deg, $campus-blue 0%, #63CAB7 100%);
  border-radius: 10rpx;
  transition: width 0.8s cubic-bezier(0.4,0,0.2,1);
  box-shadow: 0 0 8rpx rgba($campus-blue, 0.4);
}

.prog-thumb {
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 18rpx;
  height: 18rpx;
  background: #fff;
  border: 3rpx solid $campus-blue;
  border-radius: 50%;
  box-shadow: 0 2rpx 6rpx rgba($campus-blue, 0.3);
  transition: left 0.8s cubic-bezier(0.4,0,0.2,1);
}

.prog-labels {
  display: flex;
  justify-content: space-between;
}

.prog-cur, .prog-max {
  font-size: 20rpx;
  color: $color-text-tertiary;
}

.prog-cur {
  color: $campus-blue;
  font-weight: 600;
}

/* ========== ③ 徽章墙 ========== */
.badges-wrap {
  background: $color-bg-card;
  border-radius: 24rpx;
  padding: 28rpx 24rpx;
  box-shadow: $shadow-sm;
  border: 1rpx solid $color-border-light;
}

.section-hd {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.section-hd-left {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.hd-dot {
  width: 8rpx;
  height: 28rpx;
  background: linear-gradient(180deg, $campus-blue 0%, $campus-blue-light 100%);
  border-radius: 4rpx;
}

.section-hd-title {
  font-size: 28rpx;
  font-weight: 600;
  color: $color-text-primary;
}

.section-hd-action {
  display: flex;
  align-items: center;
  gap: 4rpx;
  cursor: pointer;
  padding: 6rpx 12rpx;
  border-radius: 9999rpx;
  transition: background 0.2s;

  &:active {
    background: $color-bg-hover;
  }
}

.action-text {
  font-size: 22rpx;
  color: $color-text-tertiary;
}

.action-icon {
  color: $color-text-tertiary;
}

.badges-list {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12rpx;
}

.badge-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  cursor: pointer;
  padding: 16rpx 8rpx;
  border-radius: 16rpx;
  transition: all 0.25s ease;
  animation: badgeIn 0.4s ease-out both;
  position: relative;

  &:active {
    transform: scale(0.93);
    background: $color-bg-hover;
  }

  // #ifdef H5
  &:hover {
    background: $color-bg-hover;
    transform: translateY(-2rpx);
  }
  // #endif

  &--locked {
    opacity: 0.38;
  }
}

@keyframes badgeIn {
  from { opacity: 0; transform: scale(0.8); }
  to   { opacity: 1; transform: scale(1); }
}

.badge-icon-wrap {
  width: 72rpx;
  height: 72rpx;
  border-radius: 18rpx;
  background: linear-gradient(135deg, $campus-blue-lighter 0%, rgba($campus-blue, 0.15) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: all 0.2s ease;

  &--locked {
    background: $color-bg-hover;
  }
}

.badge-icon {
  color: $campus-blue;

  .badge-icon-wrap--locked & {
    color: $color-text-tertiary;
  }
}

.badge-lock-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba($color-bg-card, 0.6);
  border-radius: 18rpx;
}

.badge-lock-icon {
  color: $color-text-tertiary;
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
}

.badge-unlocked-dot {
  width: 8rpx;
  height: 8rpx;
  background: $color-success;
  border-radius: 50%;
  flex-shrink: 0;
}

/* ========== 响应式 ========== */
@media (min-width: 1024px) {
  .achievement-section {
    gap: 24rpx;
  }

  .stats-row {
    gap: 16rpx;
  }

  .stat-cell {
    padding: 36rpx 20rpx 30rpx;
  }

  .stat-num {
    font-size: 44rpx;
  }
}
</style>
