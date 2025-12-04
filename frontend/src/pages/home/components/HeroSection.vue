<template>
  <view class="hero-section">
    <!-- 背景层 -->
    <view class="hero-bg"></view>

    <view class="hero-container">
      <!-- 左侧：品牌叙事区（7列） -->
      <view class="hero-left">
        <!-- 1. 状态标签条 -->
        <view class="status-bar">
          <view class="status-indicator">
            <view class="live-dot"></view>
            <text class="status-text">校园学习互助进行中</text>
          </view>
          <view class="status-scroll">
            <view class="scroll-track" :style="{ transform: `translateX(-${scrollIndex * 100}%)` }">
              <text class="scroll-item">今天已有 {{ todayQuestions }} 位同学发起提问</text>
              <text class="scroll-item">已有 {{ schoolCount }} 所高校加入 CampusLink</text>
              <text class="scroll-item">{{ solvedRate }}% 问题在 3 分钟内获得响应</text>
            </view>
          </view>
        </view>

        <!-- 2. 主标题 + 副标题 -->
        <view class="headline-section">
          <view class="headline">
            <text class="title-line-1">学习不掉线</text>
            <view class="title-line-2">
              <text class="title-prefix">校园里，</text>
              <text class="title-highlight">随时有人解答</text>
            </view>
          </view>
          <text class="subtitle">
            不只是问答平台，更是你所在高校的「<text class="emphasis">学习互助中枢</text>」
          </text>
        </view>

        <!-- 3. 价值点三连 -->
        <view class="value-points">
          <view class="value-item" v-for="item in valuePoints" :key="item.label">
            <view class="value-icon">
              <svg v-if="item.type === 'solved'" width="24" height="24" viewBox="0 0 24 24" fill="none">
                <path d="M9 12L11 14L15 10" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"/>
              </svg>
              <svg v-else-if="item.type === 'users'" width="24" height="24" viewBox="0 0 24 24" fill="none">
                <path d="M17 21V19C17 17.9391 16.5786 16.9217 15.8284 16.1716C15.0783 15.4214 14.0609 15 13 15H5C3.93913 15 2.92172 15.4214 2.17157 16.1716C1.42143 16.9217 1 17.9391 1 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                <path d="M23 21V19C22.9993 18.1137 22.7044 17.2528 22.1614 16.5523C21.6184 15.8519 20.8581 15.3516 20 15.13" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M16 3.13C16.8604 3.35031 17.623 3.85071 18.1676 4.55232C18.7122 5.25392 19.0078 6.11683 19.0078 7.005C19.0078 7.89318 18.7122 8.75608 18.1676 9.45769C17.623 10.1593 16.8604 10.6597 16 10.88" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="none">
                <path d="M13 2L3 14H12L11 22L21 10H12L13 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </view>
            <view class="value-data">
              <text class="value-number">{{ item.displayValue }}</text>
              <text class="value-label">{{ item.label }}</text>
            </view>
          </view>
        </view>

        <!-- 4. 主 CTA 区 -->
        <view class="cta-section">
          <view class="cta-buttons">
            <view class="cta-primary" @click="handleAsk">
              <text class="cta-text">立即提问</text>
              <svg class="cta-arrow" width="18" height="18" viewBox="0 0 24 24" fill="none">
                <path d="M5 12H19M19 12L12 5M19 12L12 19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </view>
            <view class="cta-secondary" @click="handleBrowse">
              <text class="cta-text">先逛逛学习社区</text>
            </view>
          </view>
          <text class="cta-hint">平均 3 分钟内收到 1–3 条同学回复（含 AI 参考答案）</text>
        </view>

        <!-- 5. 社交证明条 -->
        <view class="social-proof">
          <view class="avatar-stack">
            <view class="stack-avatar" v-for="(avatar, index) in avatars" :key="index" :style="{ backgroundColor: avatar.color, zIndex: 5 - index }">
              <text class="avatar-char">{{ avatar.char }}</text>
            </view>
          </view>
          <text class="proof-text">
            来自 <text class="school-names">北大 / 上交 / 浙大</text> 的同学正在使用 CampusLink
          </text>
        </view>
      </view>

      <!-- 右侧：实时互助动态墙（5列） -->
      <view class="hero-right">
        <!-- 背景装饰 -->
        <view class="feed-bg">
          <view class="bg-blob blob-1"></view>
          <view class="bg-blob blob-2"></view>
          <view class="deco-icon deco-1">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
              <path d="M9 12H15M12 9V15M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z" stroke="currentColor" stroke-width="2"/>
            </svg>
          </view>
          <view class="deco-icon deco-2">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
              <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" stroke="currentColor" stroke-width="2"/>
            </svg>
          </view>
          <view class="deco-dot dot-1"></view>
          <view class="deco-dot dot-2"></view>
        </view>

        <!-- 动态墙卡片 -->
        <view class="feed-container">
          <!-- 标题区 -->
          <view class="feed-header">
            <text class="feed-title">实时学习动态</text>
            <view class="feed-tabs">
              <view
                v-for="tab in feedTabs"
                :key="tab.key"
                class="feed-tab"
                :class="{ active: currentTab === tab.key }"
                @click="currentTab = tab.key"
              >
                <text>{{ tab.label }}</text>
              </view>
            </view>
          </view>

          <!-- 动态列表 -->
          <view class="feed-list" @mouseenter="pauseAutoPlay" @mouseleave="resumeAutoPlay">
            <view class="feed-timeline">
              <view class="timeline-line"></view>
            </view>
            <view class="feed-items">
              <transition-group name="feed-slide">
                <view
                  v-for="(item, index) in visibleFeedItems"
                  :key="item.id"
                  class="feed-item"
                  :class="[item.type, { 'is-featured': index === 0 }]"
                  @mouseenter="hoveredItem = item.id"
                  @mouseleave="hoveredItem = null"
                >
                  <!-- 时间节点 -->
                  <view class="feed-time-node">
                    <view class="time-dot"></view>
                    <text class="time-text">{{ item.time }}</text>
                  </view>

                  <!-- 问题卡片 -->
                  <view v-if="item.type === 'question'" class="feed-card question-card">
                    <view class="card-header">
                      <view class="user-avatar" :style="{ backgroundColor: item.avatarColor }">
                        <text>{{ item.avatar }}</text>
                      </view>
                      <view class="user-info">
                        <text class="user-name">{{ item.author }}</text>
                        <text class="user-meta">{{ item.department }}</text>
                      </view>
                      <view class="card-badge waiting">
                        <text>等待回答</text>
                      </view>
                    </view>
                    <text class="card-content">{{ item.content }}</text>
                    <view class="card-tags">
                      <text v-for="tag in item.tags" :key="tag" class="tag">{{ tag }}</text>
                    </view>
                  </view>

                  <!-- AI 助手卡片 -->
                  <view v-else-if="item.type === 'ai'" class="feed-card ai-card">
                    <view class="card-header">
                      <view class="ai-avatar">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                          <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                          <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                          <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                      </view>
                      <view class="user-info">
                        <text class="user-name">AI 学习助手</text>
                        <text class="user-meta">智能匹配中...</text>
                      </view>
                      <view class="card-badge responding">
                        <view class="pulse-dot"></view>
                        <text>响应中</text>
                      </view>
                    </view>
                    <text class="card-content">{{ item.content }}</text>
                    <view class="ai-progress">
                      <view class="progress-bar">
                        <view class="progress-fill" :style="{ width: item.progress + '%' }"></view>
                      </view>
                      <text class="progress-text">{{ item.progressText }}</text>
                    </view>
                  </view>

                  <!-- 资料推荐卡片 -->
                  <view v-else-if="item.type === 'resource'" class="feed-card resource-card">
                    <view class="resource-header">
                      <view class="resource-icon">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                          <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="currentColor" stroke-width="2"/>
                          <path d="M14 2V8H20" stroke="currentColor" stroke-width="2"/>
                        </svg>
                      </view>
                      <view class="resource-meta">
                        <text class="resource-format">{{ item.format }}</text>
                        <text class="resource-size">{{ item.size }}</text>
                      </view>
                      <view class="resource-rating">
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
                          <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"/>
                        </svg>
                        <text>{{ item.rating }}</text>
                      </view>
                    </view>
                    <text class="resource-title">{{ item.title }}</text>
                    <text class="resource-desc">{{ item.description }}</text>
                  </view>

                  <!-- 社团/活动卡片 -->
                  <view v-else-if="item.type === 'activity'" class="feed-card activity-card">
                    <view class="card-header">
                      <view class="activity-icon" :style="{ backgroundColor: item.iconBg }">
                        <text>{{ item.icon }}</text>
                      </view>
                      <view class="user-info">
                        <text class="user-name">{{ item.title }}</text>
                        <text class="user-meta">{{ item.organizer }}</text>
                      </view>
                      <view class="card-badge activity">
                        <text>{{ item.badge }}</text>
                      </view>
                    </view>
                    <text class="card-content">{{ item.content }}</text>
                    <view class="activity-meta">
                      <text class="meta-item">{{ item.participants }} 人参与</text>
                      <text class="meta-item">{{ item.deadline }}</text>
                    </view>
                  </view>
                </view>
              </transition-group>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'

const emit = defineEmits<{
  (e: 'ask'): void
  (e: 'browse'): void
}>()

// ==================== 左侧数据 ====================
// 状态标签条滚动
const scrollIndex = ref(0)
const todayQuestions = ref(132)
const schoolCount = ref(56)
const solvedRate = ref(95)

// 价值点数据
interface ValuePoint {
  type: string
  value: number
  displayValue: string
  label: string
  suffix?: string
}

const valuePoints = ref<ValuePoint[]>([
  { type: 'solved', value: 3420, displayValue: '0', label: '已解决问题', suffix: '+' },
  { type: 'users', value: 8960, displayValue: '0', label: '参与同学', suffix: '+' },
  { type: 'response', value: 95, displayValue: '0', label: '问题 3 分钟内有人响应', suffix: '%' }
])

// 社交证明头像
const avatars = ref([
  { char: '王', color: '#2563EB' },
  { char: '李', color: '#10B981' },
  { char: '张', color: '#8B5CF6' },
  { char: '陈', color: '#F59E0B' },
  { char: '+', color: '#E5E7EB' }
])

// ==================== 右侧动态墙数据 ====================
const feedTabs = [
  { key: 'asking', label: '正在提问' },
  { key: 'hot', label: '热答中' },
  { key: 'solved', label: '已解决' }
]
const currentTab = ref('asking')
const hoveredItem = ref<number | null>(null)

// 动态数据
interface FeedItem {
  id: number
  type: 'question' | 'ai' | 'resource' | 'activity'
  time: string
  avatar?: string
  avatarColor?: string
  author?: string
  department?: string
  content?: string
  tags?: string[]
  progress?: number
  progressText?: string
  title?: string
  format?: string
  size?: string
  rating?: number
  description?: string
  icon?: string
  iconBg?: string
  organizer?: string
  badge?: string
  participants?: number
  deadline?: string
}

const allFeedItems = ref<FeedItem[]>([
  {
    id: 1,
    type: 'question',
    time: '1分钟前',
    avatar: '晓',
    avatarColor: '#2563EB',
    author: '小明同学',
    department: '计科 · 大二',
    content: '有推荐的数据库复习资料吗？想要冲击高分',
    tags: ['#数据库', '#期末复习']
  },
  {
    id: 2,
    type: 'ai',
    time: '刚刚',
    content: '已为你找到 3 份高质量资料和 2 位学长的回复，正在整理中...',
    progress: 75,
    progressText: '已生成 3 份推荐资料'
  },
  {
    id: 3,
    type: 'resource',
    time: '3分钟前',
    title: '《数据结构期末总复习》',
    format: 'PDF',
    size: '2.3MB',
    rating: 4.9,
    description: '128 人收藏 · 学长推荐'
  },
  {
    id: 4,
    type: 'activity',
    time: '5分钟前',
    icon: '💻',
    iconBg: 'rgba(37, 99, 235, 0.1)',
    title: 'ACM 程序设计集训',
    organizer: 'ACM 协会',
    badge: '招募中',
    content: '每周六下午，一起刷题提升算法能力',
    participants: 32,
    deadline: '还剩 3 天'
  },
  {
    id: 5,
    type: 'question',
    time: '8分钟前',
    avatar: '雯',
    avatarColor: '#10B981',
    author: '学习小雯',
    department: '软工 · 大三',
    content: 'Spring Boot 的自动配置原理是什么？求详解',
    tags: ['#Java', '#Spring Boot']
  }
])

const feedItemIndex = ref(0)
const visibleFeedItems = computed(() => {
  const items = allFeedItems.value
  const count = 3
  const result: FeedItem[] = []
  for (let i = 0; i < count; i++) {
    const idx = (feedItemIndex.value + i) % items.length
    result.push(items[idx])
  }
  return result
})

// 自动轮播
let autoPlayTimer: ReturnType<typeof setInterval> | null = null
const isAutoPlaying = ref(true)

const startAutoPlay = () => {
  if (autoPlayTimer) return
  autoPlayTimer = setInterval(() => {
    if (isAutoPlaying.value) {
      feedItemIndex.value = (feedItemIndex.value + 1) % allFeedItems.value.length
    }
  }, 8000)
}

const pauseAutoPlay = () => {
  isAutoPlaying.value = false
}

const resumeAutoPlay = () => {
  isAutoPlaying.value = true
}

// 状态标签条滚动
let scrollTimer: ReturnType<typeof setInterval> | null = null
const startScrollAnimation = () => {
  scrollTimer = setInterval(() => {
    scrollIndex.value = (scrollIndex.value + 1) % 3
  }, 4000)
}

// 数字动画
const animateNumbers = () => {
  const duration = 1800
  const steps = 60
  const interval = duration / steps

  let currentStep = 0
  const timer = setInterval(() => {
    currentStep += 1
    const progress = currentStep / steps
    const eased = 1 - Math.pow(1 - progress, 3)

    valuePoints.value.forEach((point) => {
      const currentValue = Math.floor(point.value * eased)
      point.displayValue = currentValue.toLocaleString() + (point.suffix || '')
    })

    todayQuestions.value = Math.floor(132 * eased)
    schoolCount.value = Math.floor(56 * eased)
    solvedRate.value = Math.floor(95 * eased)

    if (currentStep >= steps) {
      clearInterval(timer)
      valuePoints.value.forEach((point) => {
        point.displayValue = point.value.toLocaleString() + (point.suffix || '')
      })
      todayQuestions.value = 132
      schoolCount.value = 56
      solvedRate.value = 95
    }
  }, interval)
}

// 事件处理
const handleAsk = () => emit('ask')
const handleBrowse = () => emit('browse')

onMounted(() => {
  setTimeout(animateNumbers, 400)
  startScrollAnimation()
  startAutoPlay()
})

onUnmounted(() => {
  if (scrollTimer) clearInterval(scrollTimer)
  if (autoPlayTimer) clearInterval(autoPlayTimer)
})
</script>

<style lang="scss">
@import '@/uni.scss';

// ==================== 变量定义 ====================
$hero-blue: #2563EB;
$hero-blue-light: #3B82F6;
$hero-teal: #10B981;
$hero-violet: #8B5CF6;
$hero-amber: #F59E0B;

// ==================== 主容器 ====================
.hero-section {
  position: relative;
  overflow: hidden;
  margin-top: 64px;
  min-height: 540px;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, #F8FAFC 0%, #FFFFFF 100%);
  z-index: 0;
}

.hero-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 56px 64px;
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 7fr 5fr;
  gap: 48px;
  align-items: flex-start;

  @media (max-width: 1200px) {
    padding: 48px 36px;
    gap: 36px;
  }

  @media (max-width: 1024px) {
    grid-template-columns: 1fr;
    padding: 40px 24px;
    gap: 32px;
  }
}

// ==================== 左侧叙事区 ====================
.hero-left {
  display: flex;
  flex-direction: column;
  gap: 24px;

  @media (max-width: 1024px) {
    align-items: center;
    text-align: center;
  }
}

// 1. 状态标签条
.status-bar {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  background: #F1F5F9;
  padding: 6px 16px 6px 10px;
  border-radius: 999px;
  max-width: 100%;

  @media (max-width: 1024px) {
    justify-content: center;
  }
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.live-dot {
  width: 8px;
  height: 8px;
  background: #22C55E;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

.status-text {
  font-size: 12px;
  font-weight: 500;
  color: $text-secondary;
  white-space: nowrap;
}

.status-scroll {
  overflow: hidden;
  flex: 1;
  min-width: 0;
  max-width: 260px;
}

.scroll-track {
  display: flex;
  transition: transform 0.5s ease;
}

.scroll-item {
  flex-shrink: 0;
  width: 100%;
  font-size: 12px;
  color: $text-tertiary;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

// 2. 主标题 + 副标题
.headline-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.headline {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.title-line-1 {
  font-size: 48px;
  font-weight: 700;
  color: $text-primary;
  line-height: 1.2;
  letter-spacing: -0.5px;

  @media (max-width: 768px) {
    font-size: 36px;
  }
}

.title-line-2 {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;

  @media (max-width: 1024px) {
    justify-content: center;
  }
}

.title-prefix {
  font-size: 48px;
  font-weight: 700;
  color: $text-primary;
  line-height: 1.2;
  letter-spacing: -0.5px;

  @media (max-width: 768px) {
    font-size: 36px;
  }
}

.title-highlight {
  font-size: 48px;
  font-weight: 800;
  background: linear-gradient(135deg, $hero-blue 0%, $hero-blue-light 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.2;
  letter-spacing: -0.5px;

  @media (max-width: 768px) {
    font-size: 36px;
  }
}

.subtitle {
  font-size: 16px;
  color: $text-secondary;
  line-height: 1.6;

  .emphasis {
    color: $hero-blue;
    font-weight: 600;
  }
}

// 3. 价值点三连
.value-points {
  display: flex;
  gap: 32px;
  margin-top: 8px;

  @media (max-width: 768px) {
    gap: 20px;
    flex-wrap: wrap;
    justify-content: center;
  }
}

.value-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.value-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: rgba($hero-blue, 0.08);
  color: $hero-blue;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.value-data {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.value-number {
  font-size: 18px;
  font-weight: 700;
  color: $text-primary;
  line-height: 1.2;
}

.value-label {
  font-size: 12px;
  color: $text-tertiary;
  white-space: nowrap;
}

// 4. 主 CTA 区
.cta-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 8px;

  @media (max-width: 1024px) {
    align-items: center;
  }
}

.cta-buttons {
  display: flex;
  gap: 12px;

  @media (max-width: 640px) {
    flex-direction: column;
    width: 100%;
    max-width: 300px;
  }
}

.cta-primary {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px 40px;
  background: $hero-blue;
  color: $white;
  border-radius: 999px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 8px 24px rgba($hero-blue, 0.3);

  &:hover {
    background: darken($hero-blue, 5%);
    transform: translateY(-2px);
    box-shadow: 0 12px 32px rgba($hero-blue, 0.4);

    .cta-arrow {
      transform: translateX(4px);
    }
  }

  &:active {
    transform: translateY(0);
  }
}

.cta-arrow {
  transition: transform 0.3s ease;
}

.cta-secondary {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px 32px;
  background: $white;
  color: $text-primary;
  border: 1.5px solid $gray-300;
  border-radius: 999px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    border-color: $hero-blue;
    color: $hero-blue;
    background: rgba($hero-blue, 0.04);
  }
}

.cta-hint {
  font-size: 13px;
  color: $text-tertiary;
}

// 5. 社交证明条
.social-proof {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;

  @media (max-width: 1024px) {
    flex-direction: column;
    gap: 8px;
  }
}

.avatar-stack {
  display: flex;
}

.stack-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid $white;
  margin-left: -8px;
  box-shadow: 0 2px 8px rgba($black, 0.08);

  &:first-child {
    margin-left: 0;
  }
}

.avatar-char {
  font-size: 12px;
  font-weight: 600;
  color: $white;

  .stack-avatar:last-child & {
    color: $text-tertiary;
  }
}

.proof-text {
  font-size: 13px;
  color: $text-tertiary;
}

.school-names {
  color: $text-secondary;
  font-weight: 500;
}

// ==================== 右侧动态墙 ====================
.hero-right {
  position: relative;
  min-height: 460px;

  @media (max-width: 1024px) {
    width: 100%;
    max-width: 480px;
    margin: 0 auto;
  }
}

// 背景装饰
.feed-bg {
  position: absolute;
  inset: -20px;
  background: linear-gradient(135deg, rgba(#F5F7FF, 0.8) 0%, rgba(#ECFEFF, 0.6) 100%);
  border-radius: 24px;
  z-index: 0;
  overflow: hidden;
}

.bg-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(40px);

  &.blob-1 {
    width: 150px;
    height: 150px;
    background: rgba($hero-blue, 0.08);
    top: 10%;
    right: 10%;
  }

  &.blob-2 {
    width: 100px;
    height: 100px;
    background: rgba($hero-teal, 0.08);
    bottom: 20%;
    left: 5%;
  }
}

.deco-icon {
  position: absolute;
  color: rgba($hero-blue, 0.15);
  opacity: 0.6;

  &.deco-1 {
    top: 15%;
    left: 8%;
    animation: floatSlow 6s ease-in-out infinite;
  }

  &.deco-2 {
    bottom: 25%;
    right: 12%;
    color: rgba($hero-amber, 0.2);
    animation: floatSlow 6s ease-in-out infinite 1s;
  }
}

.deco-dot {
  position: absolute;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  animation: breathe 3s ease-in-out infinite;

  &.dot-1 {
    background: rgba($hero-blue, 0.3);
    top: 30%;
    right: 20%;
  }

  &.dot-2 {
    background: rgba($hero-teal, 0.3);
    bottom: 35%;
    left: 15%;
    animation-delay: 1.5s;
  }
}

@keyframes floatSlow {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

@keyframes breathe {
  0%, 100% { transform: scale(1); opacity: 0.3; }
  50% { transform: scale(1.2); opacity: 0.6; }
}

// 动态墙容器
.feed-container {
  position: relative;
  z-index: 1;
  background: $white;
  border-radius: 20px;
  box-shadow: 0 20px 50px rgba($black, 0.08);
  overflow: hidden;
}

// 标题区
.feed-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid $gray-100;
}

.feed-title {
  font-size: 15px;
  font-weight: 600;
  color: $text-primary;
}

.feed-tabs {
  display: flex;
  gap: 4px;
}

.feed-tab {
  padding: 6px 12px;
  font-size: 12px;
  color: $text-tertiary;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    color: $text-secondary;
    background: $gray-50;
  }

  &.active {
    color: $hero-blue;
    background: rgba($hero-blue, 0.08);
    font-weight: 500;
  }
}

// 动态列表
.feed-list {
  display: flex;
  padding: 16px 20px;
  min-height: 380px;
}

.feed-timeline {
  position: relative;
  width: 24px;
  flex-shrink: 0;
}

.timeline-line {
  position: absolute;
  left: 11px;
  top: 8px;
  bottom: 8px;
  width: 2px;
  background: linear-gradient(180deg, $gray-200 0%, transparent 100%);
}

.feed-items {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

// 动态项
.feed-item {
  display: flex;
  gap: 12px;
  animation: slideIn 0.4s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.feed-time-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  width: 60px;
  flex-shrink: 0;
}

.time-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: $hero-blue;
  border: 2px solid $white;
  box-shadow: 0 0 0 2px rgba($hero-blue, 0.2);
}

.time-text {
  font-size: 11px;
  color: $text-quaternary;
  white-space: nowrap;
}

// 卡片基础样式
.feed-card {
  flex: 1;
  background: $white;
  border: 1px solid $gray-100;
  border-radius: 12px;
  padding: 14px;
  transition: all 0.3s ease;

  &:hover {
    border-color: $gray-200;
    box-shadow: 0 4px 12px rgba($black, 0.06);
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $white;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.ai-avatar {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, $hero-violet, darken($hero-violet, 10%));
  display: flex;
  align-items: center;
  justify-content: center;
  color: $white;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-size: 13px;
  font-weight: 600;
  color: $text-primary;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-meta {
  font-size: 11px;
  color: $text-tertiary;
}

.card-badge {
  flex-shrink: 0;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;

  &.waiting {
    background: rgba($hero-amber, 0.1);
    color: $hero-amber;
  }

  &.responding {
    background: rgba($hero-blue, 0.1);
    color: $hero-blue;
  }

  &.activity {
    background: rgba($hero-teal, 0.1);
    color: $hero-teal;
  }
}

.pulse-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
  animation: pulse 1.5s ease-in-out infinite;
}

.card-content {
  font-size: 14px;
  color: $text-secondary;
  line-height: 1.5;
  display: block;
  margin-bottom: 10px;
}

.card-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.tag {
  font-size: 11px;
  color: $hero-blue;
  background: rgba($hero-blue, 0.06);
  padding: 4px 8px;
  border-radius: 4px;
}

// AI 卡片进度条
.ai-progress {
  margin-top: 8px;
}

.progress-bar {
  height: 4px;
  background: $gray-100;
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 6px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, $hero-blue, $hero-violet);
  border-radius: 2px;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 11px;
  color: $text-tertiary;
}

// 资源卡片
.resource-card {
  .resource-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 10px;
  }

  .resource-icon {
    width: 36px;
    height: 36px;
    border-radius: 8px;
    background: rgba($hero-teal, 0.1);
    color: $hero-teal;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .resource-meta {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  .resource-format {
    font-size: 11px;
    color: $text-tertiary;
    font-weight: 500;
  }

  .resource-size {
    font-size: 10px;
    color: $text-quaternary;
  }

  .resource-rating {
    display: flex;
    align-items: center;
    gap: 2px;
    color: $hero-amber;
    font-size: 12px;
    font-weight: 600;
  }

  .resource-title {
    font-size: 14px;
    font-weight: 600;
    color: $text-primary;
    margin-bottom: 4px;
    display: block;
  }

  .resource-desc {
    font-size: 12px;
    color: $text-tertiary;
    display: block;
  }
}

// 活动卡片
.activity-card {
  .activity-icon {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    flex-shrink: 0;
  }

  .activity-meta {
    display: flex;
    gap: 12px;
    margin-top: 8px;
  }

  .meta-item {
    font-size: 11px;
    color: $text-tertiary;
  }
}

// 过渡动画
.feed-slide-enter-active,
.feed-slide-leave-active {
  transition: all 0.4s ease;
}

.feed-slide-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.feed-slide-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

// 通用动画
@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.3);
    opacity: 0.6;
  }
}
</style>
