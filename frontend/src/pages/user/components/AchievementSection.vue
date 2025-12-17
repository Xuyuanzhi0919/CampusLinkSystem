<template>
  <!-- ========== 成就展示区(Achievement Section) ========== -->
  <view class="achievement-section">
    <!-- 等级进度卡片 -->
    <view class="level-card">
      <view class="level-info">
        <view class="level-header">
          <text class="level-title">Lv.{{ level }}</text>
          <text class="level-name">{{ levelName }}</text>
        </view>
        <text class="level-desc">距离下一级还需 {{ expToNextLevel }} 经验</text>
      </view>

      <!-- 进度条 -->
      <view class="progress-wrapper">
        <view class="progress-bar">
          <view
            class="progress-fill"
            :style="{ width: progressPercent + '%' }"
          ></view>
        </view>
        <text class="progress-text">{{ currentExp }}/{{ nextLevelExp }}</text>
      </view>
    </view>

    <!-- 数据统计网格(4列) -->
    <view class="stats-grid">
      <view
        v-for="stat in stats"
        :key="stat.key"
        class="stat-item"
        @click="handleStatClick(stat.key)"
      >
        <text class="stat-value">{{ stat.value }}</text>
        <text class="stat-label">{{ stat.label }}</text>
      </view>
    </view>

    <!-- 徽章墙 -->
    <view class="badges-section">
      <view class="section-header">
        <text class="header-title">我的徽章</text>
        <text class="header-action" @click="$emit('viewAllBadges')">
          查看全部
          <Icon name="chevron-right" :size="16" :stroke-width="2" />
        </text>
      </view>

      <view class="badges-grid">
        <view
          v-for="badge in displayBadges"
          :key="badge.id"
          class="badge-item"
          :class="{ locked: !badge.unlocked }"
          @click="handleBadgeClick(badge)"
        >
          <view class="badge-icon">
            <Icon :name="badge.icon" :size="32" :stroke-width="1.5" />
          </view>
          <text class="badge-name">{{ badge.name }}</text>
          <view v-if="!badge.unlocked" class="badge-lock">
            <Icon name="lock" :size="16" />
          </view>
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
  stats: () => [
    { key: 'resources', label: '资源', value: 0 },
    { key: 'answers', label: '回答', value: 0 },
    { key: 'likes', label: '获赞', value: 0 },
    { key: 'collections', label: '收藏', value: 0 }
  ],
  badges: () => []
})

const emit = defineEmits<{
  statClick: [key: string]
  badgeClick: [badge: Badge]
  viewAllBadges: []
}>()

// 计算进度百分比
const progressPercent = computed(() => {
  return Math.min((props.currentExp / props.nextLevelExp) * 100, 100)
})

// 计算距离下一级的经验
const expToNextLevel = computed(() => {
  return props.nextLevelExp - props.currentExp
})

// 显示前5个徽章
const displayBadges = computed(() => {
  return props.badges.slice(0, 5)
})

const handleStatClick = (key: string) => {
  emit('statClick', key)
}

const handleBadgeClick = (badge: Badge) => {
  emit('badgeClick', badge)
}
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

/* ========== Achievement Section ========== */
.achievement-section {
  padding: 0 32rpx;
  margin-bottom: 32rpx;
}

/* 🎯 等级进度卡片 */
.level-card {
  background: linear-gradient(135deg, #F97316 0%, #FF8A5B 100%);
  border-radius: 20rpx;
  padding: 32rpx;
  margin-bottom: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(249, 115, 22, 0.2);
}

.level-info {
  margin-bottom: 24rpx;

  .level-header {
    display: flex;
    align-items: baseline;
    gap: 12rpx;
    margin-bottom: 8rpx;

    .level-title {
      font-size: 40rpx;
      font-weight: 700;
      color: #fff;
    }

    .level-name {
      font-size: 28rpx;
      font-weight: 500;
      color: rgba(255, 255, 255, 0.9);
    }
  }

  .level-desc {
    font-size: 24rpx;
    color: rgba(255, 255, 255, 0.75);
  }
}

/* 进度条 */
.progress-wrapper {
  display: flex;
  align-items: center;
  gap: 16rpx;

  .progress-bar {
    flex: 1;
    height: 12rpx;
    background: rgba(255, 255, 255, 0.25);
    border-radius: 12rpx;
    overflow: hidden;

    .progress-fill {
      height: 100%;
      background: #fff;
      border-radius: 12rpx;
      transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
      box-shadow: 0 0 12rpx rgba(255, 255, 255, 0.5);
    }
  }

  .progress-text {
    font-size: 22rpx;
    font-weight: 600;
    color: #fff;
    white-space: nowrap;
  }
}

/* 🎯 数据统计网格(4列) */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16rpx;
  margin-bottom: 32rpx;
}

.stat-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx 16rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
  cursor: pointer;

  &:active {
    transform: scale(0.95);
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  }

  .stat-value {
    font-size: 32rpx;
    font-weight: 700;
    color: #2563EB; // primary
    line-height: 1;
  }

  .stat-label {
    font-size: 22rpx;
    color: #6B7280; // text-secondary
  }
}

/* 🎯 徽章墙 */
.badges-section {
  margin-bottom: 32rpx;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;

  .header-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #0F172A; // text-primary
  }

  .header-action {
    font-size: 24rpx;
    color: #6B7280;
    display: flex;
    align-items: center;
    gap: 4rpx;
    cursor: pointer;
    transition: color 0.2s ease;

    &:active {
      color: #2563EB;
    }
  }
}

.badges-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16rpx;
}

.badge-item {
  position: relative;
  background: #fff;
  border-radius: 16rpx;
  padding: 20rpx 12rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.95);
  }

  &.locked {
    opacity: 0.4;

    .badge-icon {
      color: #9CA3AF;
    }
  }

  .badge-icon {
    width: 64rpx;
    height: 64rpx;
    border-radius: 50%;
    background: linear-gradient(135deg, #EFF6FF 0%, #DBEAFE 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #2563EB;
  }

  .badge-name {
    font-size: 20rpx;
    color: #0F172A;
    text-align: center;
    line-height: 1.2;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .badge-lock {
    position: absolute;
    top: 16rpx;
    right: 16rpx;
    width: 32rpx;
    height: 32rpx;
    background: rgba(0, 0, 0, 0.5);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
  }
}

// 🎯 响应式适配
@media (min-width: 768px) {
  .achievement-section {
    max-width: 1200rpx;
    margin: 0 auto 48rpx;
  }

  .level-card {
    padding: 48rpx;
  }

  .stats-grid {
    gap: 24rpx;
  }

  .stat-item {
    padding: 32rpx 24rpx;
  }

  .badges-grid {
    gap: 24rpx;
  }
}
</style>
