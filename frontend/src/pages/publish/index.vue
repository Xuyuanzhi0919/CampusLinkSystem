<template>
  <view class="publish-page">

    <!-- ══════════════ 移动端布局 ══════════════ -->
    <template v-if="!isDesktop">
      <!-- 蓝色渐变 Header -->
      <view class="mobile-header">
        <view class="header-nav">
          <view class="nav-back" @click="handleBack">
            <Icon name="arrow-left" :size="18" color="#FFFFFF" />
          </view>
          <text class="nav-title">发布内容</text>
          <view class="nav-placeholder" />
        </view>
        <view class="header-hero">
          <text class="hero-title">你想做什么？</text>
          <text class="hero-sub">参与互助，分享成就校园</text>
        </view>
      </view>

      <!-- 滚动内容区 -->
      <scroll-view class="mobile-content" scroll-y>
        <view class="mobile-inner">
          <text class="section-label">选择发布类型</text>

          <view class="publish-grid">
            <view
              v-for="card in publishCards"
              :key="card.id"
              class="publish-card"
              @click="handleNavigate(card.id)"
            >
              <!-- 顶行：图标 + badge -->
              <view class="card-top">
                <view class="card-icon-bg" :class="card.type">
                  <Icon :name="card.icon" :size="18" :color="cardColor(card.type)" />
                </view>
                <view class="card-badge" :class="card.type">{{ card.badge }}</view>
              </view>
              <!-- 标题 -->
              <text class="card-title">{{ card.title }}</text>
              <!-- 描述 -->
              <text class="card-desc">{{ card.desc }}</text>
              <!-- 积分提示条 -->
              <view class="card-benefit" :class="card.type">
                <Icon :name="card.benefitIcon" :size="12" :color="cardColor(card.type)" />
                <text>{{ card.benefitText }}</text>
              </view>
              <!-- 底行：标签 + 箭头 -->
              <view class="card-bottom">
                <view class="card-tags">
                  <view v-for="tag in card.tags" :key="tag.text" class="card-tag">
                    <Icon :name="tag.icon" :size="11" color="#64748B" />
                    <text>{{ tag.text }}</text>
                  </view>
                </view>
                <Icon name="chevron-right" :size="14" color="#94A3B8" />
              </view>
            </view>
          </view>

          <!-- 底部提示 -->
          <view class="bottom-tip">
            <Icon name="lightbulb" :size="14" color="#2563EB" />
            <text class="tip-text">选择合适的发布方式，可以更快获得帮助和积分</text>
          </view>
        </view>
      </scroll-view>

      <CustomTabBar />
    </template>

    <!-- ══════════════ PC 端布局 ══════════════ -->
    <!-- #ifdef H5 -->
    <template v-else>
      <WebHeader />

      <view class="pc-page">
        <!-- 左侧 Hero -->
        <view class="pc-hero">
          <view class="pc-label-row">
            <view class="pc-label-bar" />
            <text class="pc-label-text">发布中心</text>
          </view>
          <text class="pc-title">你想做什么？</text>
          <text class="pc-subtitle">参与校园互助，让每一个问题都有解答</text>
          <view class="pc-accent">
            <view class="pc-accent-line" />
            <view class="pc-accent-dot" />
          </view>
          <view class="pc-stats">
            <view class="pc-stat">
              <text class="pc-stat-num">1,200+</text>
              <text class="pc-stat-label">学生互助</text>
            </view>
            <view class="pc-stat-divider" />
            <view class="pc-stat">
              <text class="pc-stat-num">98%</text>
              <text class="pc-stat-label">问题得到解答</text>
            </view>
          </view>
          <view class="pc-tip-box">
            <Icon name="lightbulb" :size="14" color="#2563EB" />
            <text class="pc-tip-text">选择合适的方式，更快获得帮助和积分</text>
          </view>
        </view>

        <!-- 右侧 2×2 卡片网格 -->
        <view class="pc-grid">
          <view v-for="(row, ri) in gridRows" :key="ri" class="pc-grid-row">
            <view
              v-for="card in row"
              :key="card.id"
              class="pc-card"
              @click="handleNavigate(card.id)"
            >
              <view class="card-top">
                <view class="card-icon-bg" :class="card.type">
                  <Icon :name="card.icon" :size="18" :color="cardColor(card.type)" />
                </view>
                <view class="card-badge" :class="card.type">{{ card.badge }}</view>
              </view>
              <text class="card-title">{{ card.title }}</text>
              <text class="card-desc">{{ card.desc }}</text>
              <view class="card-benefit" :class="card.type">
                <Icon :name="card.benefitIcon" :size="12" :color="cardColor(card.type)" />
                <text>{{ card.benefitText }}</text>
              </view>
              <view class="card-bottom">
                <view class="card-tags">
                  <view v-for="tag in card.tags" :key="tag.text" class="card-tag">
                    <Icon :name="tag.icon" :size="11" color="#64748B" />
                    <text>{{ tag.text }}</text>
                  </view>
                </view>
                <Icon name="arrow-right" :size="15" color="#94A3B8" />
              </view>
            </view>
          </view>
        </view>
      </view>
    </template>
    <!-- #endif -->

  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Icon from '@/components/icons/index.vue'
import { CustomTabBar } from '@/components/mobile'

// #ifdef H5
import { WebHeader } from '@/components/desktop'
// #endif

// ── 平台判断 ──
const isDesktop = computed(() => {
  // #ifdef H5
  return window.innerWidth >= 1024
  // #endif
  // #ifndef H5
  return false
  // #endif
})

// ── 卡片颜色映射 ──
const TYPE_COLORS: Record<string, string> = {
  question: '#2563EB',
  resource: '#14B8A6',
  activity: '#16A34A',
  task:     '#FF6B35',
}
const cardColor = (type: string) => TYPE_COLORS[type] ?? '#2563EB'

// ── 卡片数据 ──
const publishCards = [
  {
    id: 'question', type: 'question',
    title: '提问', badge: '常用',
    icon: 'message-circle',
    desc: '作业·考试·代码，快速获得解答',
    benefitIcon: 'zap', benefitText: '平均 15 分钟内获得回答',
    tags: [{ icon: 'book-open', text: '作业答疑' }, { icon: 'laptop', text: '代码调试' }],
  },
  {
    id: 'resource', type: 'resource',
    title: '资源', badge: '常用',
    icon: 'file-text',
    desc: '上传课件资料，获取积分和认可',
    benefitIcon: 'gift', benefitText: '通过审核 +10 积分',
    tags: [{ icon: 'trending-up', text: '提升等级' }, { icon: 'star', text: '建立声誉' }],
  },
  {
    id: 'activity', type: 'activity',
    title: '活动', badge: '热门',
    icon: 'users',
    desc: '组织社团活动，扩大影响力',
    benefitIcon: 'zap', benefitText: '参与活动 +10 积分',
    tags: [{ icon: 'users', text: '快速召集' }, { icon: 'map-pin', text: '线下聚会' }],
  },
  {
    id: 'task', type: 'task',
    title: '悬赏', badge: '赚积分',
    icon: 'award',
    desc: '发布任务快速获得积分奖励',
    benefitIcon: 'gift', benefitText: '完成后立即获得 1-100 积分',
    tags: [{ icon: 'target', text: '精准匹配' }, { icon: 'zap', text: '快速响应' }],
  },
]

// PC 端 2×2 行分组
const gridRows = [publishCards.slice(0, 2), publishCards.slice(2, 4)]

// ── 导航 ──
const handleNavigate = (type: string) => {
  const routes: Record<string, string> = {
    question: '/pages/question/ask',
    resource: '/pages/resource/upload',
    activity: '/pages/club/publish-activity',
    task:     '/pages/task/publish',
  }
  const url = routes[type]
  if (url) uni.navigateTo({ url })
  else uni.showToast({ title: '功能开发中', icon: 'none' })
}

const handleBack = () => {
  const pages = getCurrentPages()
  if (pages.length > 1) uni.navigateBack()
  else uni.switchTab({ url: '/pages/home/index' })
}
</script>

<style lang="scss" scoped>
@import '@/styles/variables.scss';

// ════════════════════════════════════
//  页面基础
// ════════════════════════════════════
.publish-page {
  min-height: 100vh;
  background: #F8FAFC;
  display: flex;
  flex-direction: column;
}

// ════════════════════════════════════
//  移动端 Header
// ════════════════════════════════════
.mobile-header {
  flex-shrink: 0;
  background: linear-gradient(160deg, #2563EB 0%, #3B82F6 60%, #60A5FA 100%);
  border-radius: 0 0 20px 20px;
}

.header-nav {
  display: flex;
  align-items: center;
  height: 56px;
  padding: 0 16px 0 12px;
}

.nav-back {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  &:active { opacity: 0.65; }
}

.nav-title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.9);
}

.nav-placeholder { width: 36px; }

.header-hero {
  padding: 10px 24px 22px;
}

.hero-title {
  display: block;
  font-size: 26px;
  font-weight: 800;
  color: #FFFFFF;
  letter-spacing: -0.5px;
  margin-bottom: 4px;
}

.hero-sub {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.78);
}

// ════════════════════════════════════
//  移动端内容区
// ════════════════════════════════════
.mobile-content {
  flex: 1;
}

.mobile-inner {
  padding: 16px 16px 32px;
}

.section-label {
  display: block;
  font-size: 11px;
  font-weight: 700;
  color: #94A3B8;
  letter-spacing: 1.5px;
  text-transform: uppercase;
  margin-bottom: 12px;
}

// ════════════════════════════════════
//  卡片网格（移动端 2列）
// ════════════════════════════════════
.publish-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

// ════════════════════════════════════
//  卡片（移动 & PC 共用结构）
// ════════════════════════════════════
.publish-card,
.pc-card {
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  cursor: pointer;
  transition: box-shadow 0.2s, border-color 0.2s, transform 0.2s;

  &:hover {
    border-color: #BFDBFE;
    box-shadow: 0 4px 16px rgba(37, 99, 235, 0.08);
    transform: translateY(-2px);
  }
  &:active { transform: translateY(0); }
}

// 顶行：图标 + badge
.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

// 图标背景
.card-icon-bg {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.question { background: #EFF6FF; }
  &.resource  { background: #F0FDFA; }
  &.activity  { background: #F0FDF4; }
  &.task      { background: #FFF0EB; }
}

// badge 标签
.card-badge {
  font-size: 10px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 5px;

  &.question { background: #EFF6FF; color: #2563EB; }
  &.resource  { background: #F0FDFA; color: #14B8A6; }
  &.activity  { background: #F0FDF4; color: #16A34A; }
  &.task      { background: #FFF0EB; color: #FF6B35; }
}

// 标题
.card-title {
  font-size: 16px;
  font-weight: 700;
  color: #0F172A;
}

// 描述
.card-desc {
  font-size: 12px;
  color: #475569;
  line-height: 1.5;
}

// 积分提示条
.card-benefit {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border-radius: 7px;
  font-size: 11px;
  font-weight: 600;

  &.question { background: #EFF6FF; color: #2563EB; }
  &.resource  { background: #F0FDFA; color: #14B8A6; }
  &.activity  { background: #F0FDF4; color: #16A34A; }
  &.task      { background: #FFF0EB; color: #FF6B35; }
}

// 底行：标签 + 箭头
.card-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
}

.card-tags {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.card-tag {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 3px 7px;
  background: #F1F5F9;
  border-radius: 5px;
  font-size: 10px;
  color: #64748B;
}

// ════════════════════════════════════
//  底部提示（移动端）
// ════════════════════════════════════
.bottom-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding: 12px 14px;
  background: #EFF6FF;
  border-radius: 10px;
}

.tip-text {
  font-size: 12px;
  color: #475569;
  line-height: 1.4;
}

// ════════════════════════════════════
//  PC 端整体布局
// ════════════════════════════════════
.pc-page {
  display: flex;
  align-items: stretch;
  gap: 64px;
  padding: 56px 160px;
  min-height: calc(100vh - 64px);
  background: #F8FAFC;
}

// ── PC Hero 左侧 ──
.pc-hero {
  width: 360px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 20px;
}

.pc-label-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pc-label-bar {
  width: 3px;
  height: 16px;
  border-radius: 2px;
  background: #2563EB;
}

.pc-label-text {
  font-size: 11px;
  font-weight: 700;
  color: #2563EB;
  letter-spacing: 2px;
  text-transform: uppercase;
}

.pc-title {
  font-size: 44px;
  font-weight: 800;
  color: #0F172A;
  letter-spacing: -1px;
  line-height: 1.08;
}

.pc-subtitle {
  font-size: 14px;
  color: #475569;
  line-height: 1.65;
}

.pc-accent {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pc-accent-line {
  width: 48px;
  height: 4px;
  border-radius: 2px;
  background: #2563EB;
}

.pc-accent-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #2563EB;
}

.pc-stats {
  display: flex;
  align-items: center;
  gap: 24px;
}

.pc-stat {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.pc-stat-num {
  font-size: 26px;
  font-weight: 800;
  color: #0F172A;
  font-family: -apple-system, BlinkMacSystemFont, sans-serif;
}

.pc-stat-label {
  font-size: 12px;
  color: #94A3B8;
}

.pc-stat-divider {
  width: 1px;
  height: 36px;
  background: #E2E8F0;
}

.pc-tip-box {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: #EFF6FF;
  border-radius: 12px;
}

.pc-tip-text {
  font-size: 12px;
  color: #475569;
  line-height: 1.4;
}

// ── PC 卡片网格右侧 ──
.pc-grid {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.pc-grid-row {
  display: flex;
  gap: 16px;
  flex: 1;
}

.pc-card {
  flex: 1;
  padding: 20px;
  border-radius: 12px;
  gap: 14px;

  .card-title {
    font-size: 18px;
  }
  .card-desc {
    font-size: 13px;
  }
  .card-benefit {
    font-size: 12px;
  }
  .card-tag {
    font-size: 11px;
  }
}
</style>
