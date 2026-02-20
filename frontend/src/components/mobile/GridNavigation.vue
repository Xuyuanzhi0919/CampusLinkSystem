<template>
  <view class="grid-nav-wrapper">
    <view class="grid-nav">
      <view
        v-for="item in items"
        :key="item.id"
        class="nav-card"
        :class="[`nav-card--${item.theme}`, { 'nav-card--highlight': item.isHighlight }]"
        @click="handleClick(item)"
      >
        <!-- 高亮卡片（AI 助手）内部噪点纹理层 -->
        <view v-if="item.isHighlight" class="card-noise"></view>

        <!-- 图标区 -->
        <view class="card-icon-wrap">
          <!-- 热门活动：彩票 / 烟花 -->
          <svg v-if="item.id === 1" class="card-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2C8.5 2 5.5 4.5 5.5 8C5.5 10.5 7 12.7 9.2 13.7L8 22H16L14.8 13.7C17 12.7 18.5 10.5 18.5 8C18.5 4.5 15.5 2 12 2Z" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M9 8.5C9 8.5 10 7 12 7C14 7 15 8.5 15 8.5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
            <path d="M5 5L3 3M19 5L21 3M5 11L3 13M19 11L21 13" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
          </svg>

          <!-- 互助任务：握手 -->
          <svg v-else-if="item.id === 2" class="card-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M9 11L7 13L3 9L7 5L9 7" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M15 13L17 11L21 15L17 19L15 17" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M9 7L12 4L15 7L12 10L9 7Z" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M9 17L12 20L15 17L12 14L9 17Z" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M9 11L15 13" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
          </svg>

          <!-- 积分排行：奖杯 -->
          <svg v-else-if="item.id === 3" class="card-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M6 3H18V13C18 16.31 15.31 19 12 19C8.69 19 6 16.31 6 13V3Z" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M6 5H3C3 5 2 11 6 13" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M18 5H21C21 5 22 11 18 13" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M9 21H15M12 19V21" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
          </svg>

          <!-- AI 助手：闪光星 -->
          <svg v-else-if="item.id === 4" class="card-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 3L13.5 8.5H19L14.5 12L16 17.5L12 14.5L8 17.5L9.5 12L5 8.5H10.5L12 3Z" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M19 3L19.7 5.3L22 6L19.7 6.7L19 9L18.3 6.7L16 6L18.3 5.3L19 3Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M5 17L5.5 18.5L7 19L5.5 19.5L5 21L4.5 19.5L3 19L4.5 18.5L5 17Z" stroke="currentColor" stroke-width="1.4" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </view>

        <!-- 文字区 -->
        <view class="card-body">
          <text class="card-title">{{ item.label }}</text>
          <text class="card-desc">{{ item.desc }}</text>
        </view>

        <!-- 右箭头 -->
        <view class="card-arrow">
          <svg viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M6 12L10 8L6 4" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </view>

        <!-- 高亮卡片的装饰光斑 -->
        <view v-if="item.isHighlight" class="card-glow-orb"></view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
const items = [
  {
    id: 1,
    label: '热门活动',
    desc: '精彩校园正在进行',
    theme: 'amber',
    isHighlight: false,
    url: '/pages/club/activity-list'
  },
  {
    id: 2,
    label: '互助任务',
    desc: '发布 & 接单赚积分',
    theme: 'teal',
    isHighlight: false,
    url: '/pages/task/index'
  },
  {
    id: 3,
    label: '积分排行',
    desc: '看看谁最贡献',
    theme: 'rose',
    isHighlight: false,
    url: '/pages/user/ranking'
  },
  {
    id: 4,
    label: 'AI 助手',
    desc: '作业 · 考试 · 代码',
    theme: 'ai',
    isHighlight: true,
    url: '/pages/ai/chat'
  }
]

const handleClick = (item: typeof items[0]) => {
  uni.navigateTo({
    url: item.url,
    fail: () => uni.showToast({ title: '功能开发中', icon: 'none' })
  })
}
</script>

<style scoped lang="scss">
// ─── 容器 ────────────────────────────────────────────────────────────────────

.grid-nav-wrapper {
  width: 100%;
  padding: 6px 14px 10px;
  box-sizing: border-box;
  background: transparent;
}

// ─── 2×2 网格 ────────────────────────────────────────────────────────────────

.grid-nav {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

// ─── 卡片基础
// 所有普通卡片：白底 + 极浅彩色边框，与页面 #F8FAFC 背景融合
// AI 卡片：品牌主蓝深色，作为唯一视觉锚点
// ─────────────────────────────────────────────────────────────────────────────

.nav-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 13px 11px 13px 13px;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  box-sizing: border-box;
  border: 1px solid #E9EDF5;
  background: #FFFFFF;
  // 与页面其他白卡片（FeaturedSection 等）一致的轻阴影
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05), 0 1px 2px rgba(0, 0, 0, 0.03);
  transition: transform 0.16s cubic-bezier(0.34, 1.56, 0.64, 1),
              box-shadow 0.16s ease;

  &:active {
    transform: scale(0.958);
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
  }

  // ── 主题：琥珀（热门活动）
  // 白底卡片，仅图标区和图标本身带琥珀色，文字走系统色
  &--amber {
    .card-icon-wrap { background: #FEF3C7; }
    .card-icon      { color: #D97706; }
    .card-title     { color: #1A1A1A; }
    .card-desc      { color: #6B778C; }
    .card-arrow svg { color: #D1D5DB; }
  }

  // ── 主题：青绿（互助任务）
  &--teal {
    .card-icon-wrap { background: #CCFBF1; }
    .card-icon      { color: #0F766E; }
    .card-title     { color: #1A1A1A; }
    .card-desc      { color: #6B778C; }
    .card-arrow svg { color: #D1D5DB; }
  }

  // ── 主题：玫红（积分排行）
  &--rose {
    .card-icon-wrap { background: #FFE4E6; }
    .card-icon      { color: #BE123C; }
    .card-title     { color: #1A1A1A; }
    .card-desc      { color: #6B778C; }
    .card-arrow svg { color: #D1D5DB; }
  }

  // ── 主题：AI（AI 助手）
  // 全页唯一深色卡，用项目主色 #2563EB，与 Hero 区品牌色呼应
  &--ai {
    background: linear-gradient(140deg, #1D4ED8 0%, #2563EB 60%, #3B82F6 100%);
    border-color: rgba(37, 99, 235, 0.3);
    box-shadow: 0 4px 14px rgba(37, 99, 235, 0.28),
                0 1px 3px rgba(37, 99, 235, 0.15),
                inset 0 1px 0 rgba(255, 255, 255, 0.1);

    .card-icon-wrap { background: rgba(255, 255, 255, 0.18); }
    .card-icon      { color: #fff; }
    .card-title     { color: #fff; }
    .card-desc      { color: rgba(255, 255, 255, 0.68); }
    .card-arrow svg { color: rgba(255, 255, 255, 0.45); }

    &:active {
      box-shadow: 0 2px 8px rgba(37, 99, 235, 0.2);
    }
  }
}

// ─── 图标容器 ────────────────────────────────────────────────────────────────

.card-icon-wrap {
  width: 40px;
  height: 40px;
  border-radius: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: transform 0.15s ease;

  .nav-card:active & {
    transform: scale(0.92);
  }
}

.card-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

// ─── 文字区 ──────────────────────────────────────────────────────────────────

.card-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  line-height: 1.25;
  letter-spacing: -0.1px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-desc {
  font-size: 11.5px;
  font-weight: 400;
  line-height: 1.35;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  opacity: 0.85;
}

// ─── 右箭头 ──────────────────────────────────────────────────────────────────

.card-arrow {
  flex-shrink: 0;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.15s ease;

  svg {
    width: 14px;
    height: 14px;
    transition: transform 0.15s ease;
  }

  .nav-card:active & svg {
    transform: translateX(2px);
  }
}

// ─── AI 卡专属装饰 ────────────────────────────────────────────────────────────

// 噪点纹理（增加质感）
.card-noise {
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3CfeColorMatrix type='saturate' values='0'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)' opacity='0.04'/%3E%3C/svg%3E");
  border-radius: inherit;
  pointer-events: none;
  z-index: 0;
  opacity: 0.6;
}

// 右下角光斑（柔和光晕）
.card-glow-orb {
  position: absolute;
  bottom: -20px;
  right: -16px;
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(167, 139, 250, 0.55) 0%, transparent 70%);
  pointer-events: none;
  z-index: 0;
  animation: orbFloat 4s ease-in-out infinite;
}

@keyframes orbFloat {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50%       { transform: translate(-4px, -6px) scale(1.08); }
}

// AI 卡内容层叠到噪点/光斑之上
.nav-card--ai .card-icon-wrap,
.nav-card--ai .card-body,
.nav-card--ai .card-arrow {
  position: relative;
  z-index: 1;
}
</style>
