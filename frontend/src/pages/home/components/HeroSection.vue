<template>
  <view class="hero-section">
    <!-- 背景层 -->
    <view class="hero-bg">
      <view class="bg-gradient"></view>
      <view class="bg-pattern"></view>
    </view>

    <view class="hero-container">
      <!-- 左侧：品牌叙事区 -->
      <view class="hero-left">
        <!-- 1. 状态标签条 -->
        <view class="status-bar">
          <view class="status-indicator">
            <view class="live-dot"></view>
            <text class="status-text">校园学习互助进行中</text>
          </view>
          <view class="status-divider"></view>
          <view class="status-scroll">
            <view class="scroll-track" :style="{ transform: `translateX(-${scrollIndex * 100}%)` }">
              <text class="scroll-item">今天已有 <text class="highlight">{{ todayQuestions }}</text> 位同学发起提问</text>
              <text class="scroll-item">已有 <text class="highlight">{{ schoolCount }}</text> 所高校加入</text>
              <text class="scroll-item"><text class="highlight">{{ solvedRate }}%</text> 问题 3 分钟内响应</text>
            </view>
          </view>
        </view>

        <!-- 2. 主标题 + 副标题 -->
        <view class="headline-section">
          <view class="headline">
            <text class="title-line-1">学习不掉线</text>
            <view class="title-line-2">
              <text class="title-normal">校园里，</text>
              <view class="title-highlight-wrapper">
                <text class="title-highlight">随时有人解答</text>
                <view class="highlight-underline"></view>
              </view>
            </view>
          </view>
          <text class="subtitle">
            不只是问答平台，更是你所在高校的「<text class="emphasis">学习互助中枢</text>」
          </text>
        </view>

        <!-- 3. 价值点三连 -->
        <view class="value-points">
          <view class="value-item" v-for="item in valuePoints" :key="item.label">
            <view class="value-icon" :class="item.type">
              <svg v-if="item.type === 'solved'" width="22" height="22" viewBox="0 0 24 24" fill="none">
                <path d="M9 12L11 14L15 10" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="2"/>
              </svg>
              <svg v-else-if="item.type === 'users'" width="22" height="22" viewBox="0 0 24 24" fill="none">
                <path d="M17 21V19C17 16.7909 15.2091 15 13 15H5C2.79086 15 1 16.7909 1 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
                <path d="M23 21V19C23 17.1362 21.7252 15.5701 20 15.126" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M16 3.12602C17.7252 3.57006 19 5.13616 19 7C19 8.86384 17.7252 10.4299 16 10.874" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <svg v-else width="22" height="22" viewBox="0 0 24 24" fill="none">
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
              <view class="cta-glow"></view>
              <text class="cta-text">立即提问</text>
              <view class="cta-icon">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                  <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                  <path d="M9.09 9C9.3251 8.33167 9.78915 7.76811 10.4 7.40913C11.0108 7.05016 11.7289 6.91894 12.4272 7.03871C13.1255 7.15849 13.7588 7.52152 14.2151 8.06353C14.6713 8.60553 14.9211 9.29152 14.92 10C14.92 12 11.92 13 11.92 13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  <circle cx="12" cy="17" r="1" fill="currentColor"/>
                </svg>
              </view>
            </view>
            <view class="cta-secondary" @click="handleBrowse">
              <text class="cta-text">先逛逛学习社区</text>
              <svg class="cta-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none">
                <path d="M5 12H19M19 12L12 5M19 12L12 19" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </view>
          </view>
          <view class="cta-hint">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <path d="M13 2L3 14H12L11 22L21 10H12L13 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <text>平均 3 分钟内收到 1–3 条同学回复（含 AI 参考答案）</text>
          </view>
        </view>

        <!-- 5. 社交证明条 -->
        <view class="social-proof">
          <view class="avatar-stack">
            <view
              class="stack-avatar"
              v-for="(avatar, index) in avatars"
              :key="index"
              :style="{ background: avatar.color, zIndex: 6 - index }"
            >
              <text class="avatar-char">{{ avatar.char }}</text>
            </view>
          </view>
          <view class="proof-content">
            <text class="proof-text">
              来自 <text class="school-names">北大 / 上交 / 浙大</text> 等高校的同学正在使用
            </text>
            <view class="online-indicator">
              <view class="online-dot"></view>
              <text class="online-count">{{ onlineCount }} 人在线</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 右侧：实时互助动态墙 -->
      <view class="hero-right">
        <!-- 动态墙容器 -->
        <view class="feed-wrapper">
          <!-- 背景装饰 -->
          <view class="feed-decoration">
            <view class="deco-circle circle-1"></view>
            <view class="deco-circle circle-2"></view>
            <view class="deco-icon icon-1">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.5"/>
                <path d="M9.09 9C9.3251 8.33167 9.78915 7.76811 10.4 7.40913C11.0108 7.05016 11.7289 6.91894 12.4272 7.03871C13.1255 7.15849 13.7588 7.52152 14.2151 8.06353C14.6713 8.60553 14.9211 9.29152 14.92 10C14.92 12 11.92 13 11.92 13" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                <circle cx="12" cy="17" r="1" fill="currentColor"/>
              </svg>
            </view>
            <view class="deco-icon icon-2">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                <path d="M14 2H6C4.89543 2 4 2.89543 4 4V20C4 21.1046 4.89543 22 6 22H18C19.1046 22 20 21.1046 20 20V8L14 2Z" stroke="currentColor" stroke-width="1.5"/>
                <path d="M14 2V8H20" stroke="currentColor" stroke-width="1.5"/>
              </svg>
            </view>
            <view class="deco-icon icon-3">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                <path d="M17 21V19C17 16.7909 15.2091 15 13 15H5C2.79086 15 1 16.7909 1 19V21" stroke="currentColor" stroke-width="1.5"/>
                <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="1.5"/>
              </svg>
            </view>
            <view class="deco-dot dot-1"></view>
            <view class="deco-dot dot-2"></view>
            <view class="deco-dot dot-3"></view>
          </view>

          <!-- 动态墙卡片 -->
          <view class="feed-container">
            <!-- 标题区 -->
            <view class="feed-header">
              <view class="feed-title-section">
                <text class="feed-title">实时学习动态</text>
                <view class="feed-live-badge">
                  <view class="live-pulse"></view>
                  <text>LIVE</text>
                </view>
              </view>
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
              <view class="feed-items">
                <view
                  v-for="item in visibleFeedItems"
                  :key="item.id"
                  class="feed-item"
                  :class="[item.type, { 'is-hovered': hoveredItem === item.id }]"
                  @mouseenter="hoveredItem = item.id"
                  @mouseleave="hoveredItem = null"
                >
                  <!-- 问题卡片 -->
                  <view v-if="item.type === 'question'" class="feed-card question-card">
                    <view class="card-header">
                      <view class="user-avatar" :style="{ background: item.avatarGradient || item.avatarColor }">
                        <text>{{ item.avatar }}</text>
                      </view>
                      <view class="user-info">
                        <text class="user-name">{{ item.author }}</text>
                        <text class="user-meta">{{ item.department }} · {{ item.time }}</text>
                      </view>
                      <view class="card-badge" :class="item.status">
                        <text>{{ item.statusText }}</text>
                      </view>
                    </view>
                    <text class="card-content">{{ item.content }}</text>
                    <view class="card-footer">
                      <view class="card-tags">
                        <text v-for="tag in item.tags" :key="tag" class="tag">{{ tag }}</text>
                      </view>
                      <view class="card-stats">
                        <view class="stat-item">
                          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
                            <path d="M21 15C21 15.5304 20.7893 16.0391 20.4142 16.4142C20.0391 16.7893 19.5304 17 19 17H7L3 21V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H19C19.5304 3 20.0391 3.21071 20.4142 3.58579C20.7893 3.96086 21 4.46957 21 5V15Z" stroke="currentColor" stroke-width="2"/>
                          </svg>
                          <text>{{ item.replies || 0 }}</text>
                        </view>
                      </view>
                    </view>
                  </view>

                  <!-- AI 助手卡片 -->
                  <view v-else-if="item.type === 'ai'" class="feed-card ai-card">
                    <view class="card-header">
                      <view class="ai-avatar">
                        <view class="ai-glow"></view>
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                          <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                          <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                          <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                      </view>
                      <view class="user-info">
                        <text class="user-name">AI 学习助手</text>
                        <text class="user-meta">{{ item.time }}</text>
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
                        <view class="progress-glow" :style="{ left: item.progress + '%' }"></view>
                      </view>
                      <view class="progress-info">
                        <text class="progress-text">{{ item.progressText }}</text>
                        <text class="progress-percent">{{ item.progress }}%</text>
                      </view>
                    </view>
                  </view>

                  <!-- 资料推荐卡片 -->
                  <view v-else-if="item.type === 'resource'" class="feed-card resource-card">
                    <view class="resource-preview">
                      <view class="resource-icon">
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                          <path d="M14 2H6C4.89543 2 4 2.89543 4 4V20C4 21.1046 4.89543 22 6 22H18C19.1046 22 20 21.1046 20 20V8L14 2Z" stroke="currentColor" stroke-width="2"/>
                          <path d="M14 2V8H20" stroke="currentColor" stroke-width="2"/>
                          <path d="M16 13H8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                          <path d="M16 17H8" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        </svg>
                      </view>
                      <view class="resource-badge">{{ item.format }}</view>
                    </view>
                    <view class="resource-info">
                      <text class="resource-title">{{ item.title }}</text>
                      <view class="resource-meta">
                        <view class="resource-rating">
                          <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
                            <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"/>
                          </svg>
                          <text>{{ item.rating }}</text>
                        </view>
                        <text class="resource-downloads">{{ item.downloads }} 次下载</text>
                      </view>
                    </view>
                  </view>

                  <!-- 社团/活动卡片 -->
                  <view v-else-if="item.type === 'activity'" class="feed-card activity-card">
                    <view class="card-header">
                      <view class="activity-icon" :style="{ background: item.iconBg }">
                        <text>{{ item.icon }}</text>
                      </view>
                      <view class="user-info">
                        <text class="user-name">{{ item.title }}</text>
                        <text class="user-meta">{{ item.organizer }} · {{ item.time }}</text>
                      </view>
                      <view class="card-badge activity">
                        <text>{{ item.badge }}</text>
                      </view>
                    </view>
                    <text class="card-content">{{ item.content }}</text>
                    <view class="activity-footer">
                      <view class="activity-avatars">
                        <view class="mini-avatar" v-for="n in 3" :key="n" :style="{ backgroundColor: ['#2563EB', '#10B981', '#F59E0B'][n-1] }"></view>
                        <text class="avatar-more">+{{ item.participants }}</text>
                      </view>
                      <text class="activity-deadline">{{ item.deadline }}</text>
                    </view>
                  </view>
                </view>
              </view>
            </view>

            <!-- 底部提示 -->
            <view class="feed-footer">
              <text class="footer-text">数据每 10 秒自动刷新</text>
              <view class="refresh-indicator">
                <view class="refresh-dot" :class="{ active: isAutoPlaying }"></view>
              </view>
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
const scrollIndex = ref(0)
const todayQuestions = ref(132)
const schoolCount = ref(56)
const solvedRate = ref(95)
const onlineCount = ref(892)

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
  { type: 'response', value: 95, displayValue: '0', label: '3分钟响应率', suffix: '%' }
])

const avatars = ref([
  { char: '王', color: 'linear-gradient(135deg, #2563EB, #3B82F6)' },
  { char: '李', color: 'linear-gradient(135deg, #10B981, #34D399)' },
  { char: '张', color: 'linear-gradient(135deg, #8B5CF6, #A78BFA)' },
  { char: '陈', color: 'linear-gradient(135deg, #F59E0B, #FBBF24)' },
  { char: '周', color: 'linear-gradient(135deg, #EC4899, #F472B6)' },
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

interface FeedItem {
  id: number
  type: 'question' | 'ai' | 'resource' | 'activity'
  time: string
  avatar?: string
  avatarColor?: string
  avatarGradient?: string
  author?: string
  department?: string
  content?: string
  tags?: string[]
  status?: string
  statusText?: string
  replies?: number
  progress?: number
  progressText?: string
  title?: string
  format?: string
  size?: string
  rating?: number
  downloads?: number
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
    avatarGradient: 'linear-gradient(135deg, #2563EB, #3B82F6)',
    author: '小明同学',
    department: '计科 · 大二',
    content: '有推荐的数据库复习资料吗？想要冲击高分 💪',
    tags: ['#数据库', '#期末复习'],
    status: 'waiting',
    statusText: '等待回答',
    replies: 0
  },
  {
    id: 2,
    type: 'ai',
    time: '刚刚',
    content: '已为你找到 3 份高质量资料和 2 位学长的回复，正在整理推荐理由...',
    progress: 75,
    progressText: '已匹配 3 份资料'
  },
  {
    id: 3,
    type: 'resource',
    time: '3分钟前',
    title: '《数据结构期末总复习》',
    format: 'PDF',
    size: '2.3MB',
    rating: 4.9,
    downloads: 1280,
    description: '学长推荐 · 高分必备'
  },
  {
    id: 4,
    type: 'activity',
    time: '5分钟前',
    icon: '💻',
    iconBg: 'linear-gradient(135deg, rgba(37, 99, 235, 0.15), rgba(59, 130, 246, 0.1))',
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
    avatarGradient: 'linear-gradient(135deg, #10B981, #34D399)',
    author: '学习小雯',
    department: '软工 · 大三',
    content: 'Spring Boot 的自动配置原理是什么？求详细讲解',
    tags: ['#Java', '#Spring Boot'],
    status: 'hot',
    statusText: '热门',
    replies: 5
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
  const duration = 2000
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
    onlineCount.value = Math.floor(892 * eased)

    if (currentStep >= steps) {
      clearInterval(timer)
      valuePoints.value.forEach((point) => {
        point.displayValue = point.value.toLocaleString() + (point.suffix || '')
      })
      todayQuestions.value = 132
      schoolCount.value = 56
      solvedRate.value = 95
      onlineCount.value = 892
    }
  }, interval)
}

const handleAsk = () => emit('ask')
const handleBrowse = () => emit('browse')

onMounted(() => {
  setTimeout(animateNumbers, 300)
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
$hero-blue-dark: #1D4ED8;
$hero-teal: #10B981;
$hero-violet: #8B5CF6;
$hero-amber: #F59E0B;
$hero-red: #EF4444;

// ==================== 主容器 ====================
.hero-section {
  position: relative;
  overflow: hidden;
  margin-top: 64px;
  min-height: 580px;
}

.hero-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.bg-gradient {
  position: absolute;
  inset: 0;
  // 透明背景，与页面统一雾化背景融合
  background: transparent;
}

.bg-pattern {
  position: absolute;
  inset: 0;
  // 轻微的局部装饰，不破坏整体统一性
  background-image:
    radial-gradient(circle at 30% 40%, rgba($hero-blue, 0.02) 0%, transparent 40%);
}

.hero-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 56px 64px;
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1fr 420px;
  gap: 56px;
  align-items: flex-start;

  @media (max-width: 1200px) {
    padding: 48px 36px;
    grid-template-columns: 1fr 380px;
    gap: 40px;
  }

  @media (max-width: 1024px) {
    grid-template-columns: 1fr;
    padding: 40px 24px;
    gap: 40px;
  }
}

// ==================== 左侧叙事区 ====================
.hero-left {
  display: flex;
  flex-direction: column;
  gap: 28px;
  padding-top: 8px;

  @media (max-width: 1024px) {
    align-items: center;
    text-align: center;
  }
}

// 状态标签条
.status-bar {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  background: $white;
  padding: 6px 14px;
  border-radius: 999px;
  box-shadow: 0 2px 8px rgba($black, 0.04);
  border: 1px solid rgba($gray-200, 0.6);
  max-width: fit-content;

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
  box-shadow: 0 0 0 3px rgba(#22C55E, 0.2);
  animation: livePulse 2s infinite;
}

@keyframes livePulse {
  0%, 100% { box-shadow: 0 0 0 3px rgba(#22C55E, 0.2); }
  50% { box-shadow: 0 0 0 6px rgba(#22C55E, 0.1); }
}

.status-text {
  font-size: 13px;
  font-weight: 600;
  color: $text-primary;
  white-space: nowrap;
}

.status-divider {
  width: 1px;
  height: 16px;
  background: $gray-200;
}

.status-scroll {
  overflow: hidden;
  flex: 1;
  min-width: 0;
  max-width: 220px;
}

.scroll-track {
  display: flex;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.scroll-item {
  flex-shrink: 0;
  width: 100%;
  font-size: 13px;
  color: $text-secondary;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  .highlight {
    color: $hero-blue;
    font-weight: 600;
  }
}

// 主标题区
.headline-section {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.headline {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.title-line-1 {
  font-size: 52px;
  font-weight: 800;
  color: $text-primary;
  line-height: 1.15;
  letter-spacing: -1.5px;

  @media (max-width: 768px) {
    font-size: 36px;
  }
}

.title-line-2 {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 0;

  @media (max-width: 1024px) {
    justify-content: center;
  }
}

.title-normal {
  font-size: 52px;
  font-weight: 800;
  color: $text-primary;
  line-height: 1.15;
  letter-spacing: -1.5px;

  @media (max-width: 768px) {
    font-size: 36px;
  }
}

.title-highlight-wrapper {
  position: relative;
  display: inline-block;
}

.title-highlight {
  font-size: 52px;
  font-weight: 800;
  background: linear-gradient(135deg, $hero-blue 0%, $hero-blue-light 40%, #0EA5E9 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.15;
  letter-spacing: -1.5px;
  // 添加轻微的文字阴影增强立体感
  filter: drop-shadow(0 2px 4px rgba($hero-blue, 0.15));

  @media (max-width: 768px) {
    font-size: 36px;
  }
}

.highlight-underline {
  position: absolute;
  bottom: 6px;
  left: -4px;
  right: -4px;
  height: 12px;
  background: linear-gradient(90deg,
    rgba($hero-blue, 0.15) 0%,
    rgba($hero-blue-light, 0.2) 50%,
    rgba($hero-blue, 0.08) 100%
  );
  border-radius: 6px;
  z-index: -1;
  // 添加轻微动画
  animation: underlineBreath 3s ease-in-out infinite;
}

@keyframes underlineBreath {
  0%, 100% { opacity: 1; transform: scaleX(1); }
  50% { opacity: 0.8; transform: scaleX(0.98); }
}

.subtitle {
  font-size: 16px;
  color: $text-secondary;
  line-height: 1.6;
  max-width: 440px;

  .emphasis {
    color: $hero-blue;
    font-weight: 600;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      bottom: -2px;
      left: 0;
      right: 0;
      height: 2px;
      background: linear-gradient(90deg, $hero-blue, transparent);
      border-radius: 1px;
    }
  }

  @media (max-width: 1024px) {
    max-width: 100%;
  }
}

// 价值点
.value-points {
  display: flex;
  gap: 32px;
  margin-top: 0;

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
  padding: 8px 12px 8px 8px;
  border-radius: 14px;
  transition: all 0.3s ease;
  cursor: default;

  &:hover {
    background: rgba($white, 0.7);
    box-shadow: 0 4px 16px rgba($hero-blue, 0.08);

    .value-icon {
      transform: scale(1.08) translateY(-2px);
    }

    .value-number {
      color: $hero-blue;
    }
  }
}

.value-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.3s ease;

  &.solved {
    background: linear-gradient(135deg, rgba($hero-teal, 0.12), rgba($hero-teal, 0.06));
    color: $hero-teal;
    box-shadow: 0 2px 8px rgba($hero-teal, 0.15);
  }

  &.users {
    background: linear-gradient(135deg, rgba($hero-blue, 0.12), rgba($hero-blue, 0.06));
    color: $hero-blue;
    box-shadow: 0 2px 8px rgba($hero-blue, 0.15);
  }

  &.response {
    background: linear-gradient(135deg, rgba($hero-amber, 0.12), rgba($hero-amber, 0.06));
    color: $hero-amber;
    box-shadow: 0 2px 8px rgba($hero-amber, 0.15);
  }
}

.value-data {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.value-number {
  font-size: 20px;
  font-weight: 700;
  color: $text-primary;
  line-height: 1.2;
  letter-spacing: -0.5px;
  transition: color 0.3s ease;
}

.value-label {
  font-size: 12px;
  color: $text-tertiary;
  white-space: nowrap;
}

// CTA 区
.cta-section {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-top: 4px;

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
    max-width: 320px;
  }
}

.cta-primary {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px 32px;
  background: linear-gradient(135deg, $hero-blue 0%, $hero-blue-dark 100%);
  color: $white;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow:
    0 4px 12px rgba($hero-blue, 0.25),
    0 8px 24px rgba($hero-blue, 0.15),
    inset 0 1px 0 rgba($white, 0.15);
  overflow: hidden;

  &:hover {
    transform: translateY(-2px);
    box-shadow:
      0 6px 16px rgba($hero-blue, 0.3),
      0 12px 32px rgba($hero-blue, 0.2),
      inset 0 1px 0 rgba($white, 0.2);

    .cta-glow {
      opacity: 1;
    }

    .cta-icon {
      transform: scale(1.1);
    }
  }

  &:active {
    transform: translateY(0);
  }
}

.cta-glow {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba($white, 0.15), transparent 60%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.cta-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease;
}

.cta-secondary {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 16px 24px;
  background: rgba($white, 0.8);
  backdrop-filter: blur(8px);
  color: $text-primary;
  border: 1.5px solid rgba($gray-200, 0.8);
  border-radius: 14px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    border-color: $hero-blue;
    color: $hero-blue;
    background: rgba($white, 0.95);
    box-shadow: 0 4px 12px rgba($hero-blue, 0.1);

    .cta-arrow {
      transform: translateX(4px);
    }
  }

  .cta-arrow {
    transition: transform 0.3s ease;
  }
}

.cta-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: $text-tertiary;

  svg {
    color: $hero-amber;
  }
}

// 社交证明
.social-proof {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
  padding: 10px 14px;
  background: rgba($white, 0.6);
  backdrop-filter: blur(8px);
  border-radius: 16px;
  border: 1px solid rgba($gray-200, 0.5);

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
  box-shadow: 0 2px 6px rgba($black, 0.12);
  transition: all 0.3s ease;
  animation: avatarFloat 3s ease-in-out infinite;

  &:first-child {
    margin-left: 0;
    animation-delay: 0s;
  }

  &:nth-child(2) { animation-delay: 0.2s; }
  &:nth-child(3) { animation-delay: 0.4s; }
  &:nth-child(4) { animation-delay: 0.6s; }
  &:nth-child(5) { animation-delay: 0.8s; }
  &:nth-child(6) { animation-delay: 1s; }

  &:hover {
    transform: translateY(-4px) scale(1.1);
    z-index: 10;
  }
}

@keyframes avatarFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-2px); }
}

.avatar-char {
  font-size: 12px;
  font-weight: 600;
  color: $white;

  .stack-avatar:last-child & {
    color: $text-tertiary;
  }
}

.proof-content {
  display: flex;
  flex-direction: column;
  gap: 2px;

  @media (max-width: 1024px) {
    align-items: center;
  }
}

.proof-text {
  font-size: 12px;
  color: $text-tertiary;
}

.school-names {
  color: $text-secondary;
  font-weight: 500;
}

.online-indicator {
  display: flex;
  align-items: center;
  gap: 5px;
}

.online-dot {
  width: 6px;
  height: 6px;
  background: $hero-teal;
  border-radius: 50%;
  box-shadow: 0 0 0 2px rgba($hero-teal, 0.2);
  animation: onlinePulse 1.5s ease-in-out infinite;
}

@keyframes onlinePulse {
  0%, 100% {
    opacity: 1;
    box-shadow: 0 0 0 2px rgba($hero-teal, 0.2);
  }
  50% {
    opacity: 0.8;
    box-shadow: 0 0 0 4px rgba($hero-teal, 0.1);
  }
}

.online-count {
  font-size: 11px;
  color: $hero-teal;
  font-weight: 600;
}

// ==================== 右侧动态墙 ====================
.hero-right {
  position: relative;

  @media (max-width: 1024px) {
    width: 100%;
    max-width: 420px;
    margin: 0 auto;
  }
}

.feed-wrapper {
  position: relative;
  // 外层容器：浅蓝色背景 + 内阴影，形成"动态窗格"效果
  padding: 16px;
  background: linear-gradient(145deg,
    rgba($hero-blue, 0.04) 0%,
    rgba($hero-teal, 0.02) 50%,
    rgba($hero-blue, 0.03) 100%
  );
  border-radius: 28px;
  box-shadow:
    inset 0 2px 8px rgba($hero-blue, 0.06),
    inset 0 -2px 8px rgba($hero-teal, 0.04);
}

// 装饰元素 - 简化，与页面背景融合
.feed-decoration {
  position: absolute;
  inset: -20px;
  pointer-events: none;
  z-index: 0;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;

  &.circle-1 {
    width: 120px;
    height: 120px;
    background: radial-gradient(circle, rgba($hero-blue, 0.08) 0%, transparent 70%);
    top: -10px;
    right: -20px;
  }

  &.circle-2 {
    width: 100px;
    height: 100px;
    background: radial-gradient(circle, rgba($hero-teal, 0.06) 0%, transparent 70%);
    bottom: 40px;
    left: -20px;
  }
}

.deco-icon {
  position: absolute;
  color: rgba($hero-blue, 0.15);
  animation: floatIcon 5s ease-in-out infinite;

  &.icon-1 {
    top: 10px;
    left: 0;
  }

  &.icon-2 {
    top: 45%;
    right: -10px;
    color: rgba($hero-teal, 0.15);
    animation-delay: 1.5s;
  }

  &.icon-3 {
    bottom: 20%;
    left: -5px;
    color: rgba($hero-violet, 0.12);
    animation-delay: 3s;
  }
}

@keyframes floatIcon {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-8px) rotate(3deg); }
}

.deco-dot {
  position: absolute;
  border-radius: 50%;
  animation: breatheDot 3s ease-in-out infinite;

  &.dot-1 {
    width: 6px;
    height: 6px;
    background: rgba($hero-blue, 0.35);
    top: 25%;
    right: 5%;
  }

  &.dot-2 {
    width: 5px;
    height: 5px;
    background: rgba($hero-teal, 0.35);
    bottom: 35%;
    left: 3%;
    animation-delay: 1s;
  }

  &.dot-3 {
    width: 4px;
    height: 4px;
    background: rgba($hero-amber, 0.35);
    top: 55%;
    right: 3%;
    animation-delay: 2s;
  }
}

@keyframes breatheDot {
  0%, 100% { transform: scale(1); opacity: 0.4; }
  50% { transform: scale(1.3); opacity: 0.7; }
}

// 动态墙容器
.feed-container {
  position: relative;
  z-index: 1;
  background: rgba($white, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow:
    0 2px 4px rgba($black, 0.02),
    0 8px 16px rgba($black, 0.04),
    0 16px 32px rgba($black, 0.03);
  overflow: hidden;
  border: 1px solid rgba($white, 0.8);
}

// 标题区
.feed-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid $gray-100;
  background: linear-gradient(180deg, $white, rgba($gray-50, 0.5));
  gap: 12px;
  flex-wrap: nowrap;
}

.feed-title-section {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.feed-title {
  font-size: 14px;
  font-weight: 700;
  color: $text-primary;
  white-space: nowrap;
}

.feed-live-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 3px 8px;
  background: rgba($hero-red, 0.1);
  border-radius: 4px;
  font-size: 10px;
  font-weight: 700;
  color: $hero-red;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.live-pulse {
  width: 6px;
  height: 6px;
  background: $hero-red;
  border-radius: 50%;
  animation: livePulse 1.5s infinite;
}

.feed-tabs {
  display: flex;
  gap: 2px;
  background: $gray-100;
  padding: 2px;
  border-radius: 6px;
  flex-shrink: 0;
}

.feed-tab {
  padding: 5px 10px;
  font-size: 11px;
  color: $text-tertiary;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-weight: 500;
  white-space: nowrap;

  &:hover {
    color: $text-secondary;
  }

  &.active {
    color: $hero-blue;
    background: $white;
    box-shadow: 0 1px 3px rgba($black, 0.08);
  }
}

// 动态列表
.feed-list {
  padding: 16px;
  min-height: 400px;
  max-height: 420px;
  overflow: hidden;
}

.feed-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.feed-item {
  transition: all 0.3s ease;

  &.is-hovered {
    transform: translateX(4px);
  }
}

// 卡片基础样式
.feed-card {
  background: $white;
  border: 1px solid rgba($gray-100, 0.8);
  border-radius: 12px;
  padding: 14px;
  transition: all 0.3s ease;

  &:hover {
    border-color: rgba($hero-blue, 0.15);
    box-shadow: 0 4px 12px rgba($hero-blue, 0.06);
    background: rgba($white, 1);
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $white;
  font-size: 15px;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba($black, 0.1);
}

.ai-avatar {
  position: relative;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: linear-gradient(135deg, $hero-violet, darken($hero-violet, 15%));
  display: flex;
  align-items: center;
  justify-content: center;
  color: $white;
  flex-shrink: 0;
  overflow: hidden;
}

.ai-glow {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba($white, 0.3), transparent);
  animation: aiGlow 2s ease-in-out infinite;
}

@keyframes aiGlow {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 0.6; }
}

.user-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: $text-primary;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-meta {
  font-size: 12px;
  color: $text-quaternary;
}

.card-badge {
  flex-shrink: 0;
  padding: 5px 10px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;

  &.waiting {
    background: linear-gradient(135deg, rgba($hero-amber, 0.1), rgba($hero-amber, 0.05));
    color: $hero-amber;
  }

  &.hot {
    background: linear-gradient(135deg, rgba($hero-red, 0.1), rgba($hero-red, 0.05));
    color: $hero-red;
  }

  &.responding {
    background: linear-gradient(135deg, rgba($hero-blue, 0.1), rgba($hero-blue, 0.05));
    color: $hero-blue;
  }

  &.activity {
    background: linear-gradient(135deg, rgba($hero-teal, 0.1), rgba($hero-teal, 0.05));
    color: $hero-teal;
  }
}

.pulse-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
  animation: pulseDot 1.5s ease-in-out infinite;
}

@keyframes pulseDot {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.3); opacity: 0.6; }
}

.card-content {
  font-size: 14px;
  color: $text-secondary;
  line-height: 1.6;
  display: block;
  margin-bottom: 12px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  border-radius: 6px;
  font-weight: 500;
}

.card-stats {
  display: flex;
  gap: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: $text-quaternary;
}

// AI 进度条
.ai-progress {
  margin-top: 4px;
}

.progress-bar {
  position: relative;
  height: 6px;
  background: $gray-100;
  border-radius: 3px;
  overflow: visible;
  margin-bottom: 8px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, $hero-blue, $hero-violet);
  border-radius: 3px;
  transition: width 0.5s ease;
}

.progress-glow {
  position: absolute;
  top: -2px;
  width: 10px;
  height: 10px;
  background: $hero-violet;
  border-radius: 50%;
  filter: blur(4px);
  opacity: 0.6;
  transition: left 0.5s ease;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-text {
  font-size: 11px;
  color: $text-tertiary;
}

.progress-percent {
  font-size: 11px;
  color: $hero-violet;
  font-weight: 600;
}

// 资源卡片
.resource-card {
  display: flex;
  gap: 14px;
}

.resource-preview {
  position: relative;
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba($hero-teal, 0.1), rgba($hero-teal, 0.05));
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: $hero-teal;
}

.resource-badge {
  position: absolute;
  bottom: -4px;
  right: -4px;
  padding: 2px 6px;
  background: $hero-teal;
  color: $white;
  font-size: 9px;
  font-weight: 700;
  border-radius: 4px;
  text-transform: uppercase;
}

.resource-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 6px;
}

.resource-title {
  font-size: 14px;
  font-weight: 600;
  color: $text-primary;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.resource-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.resource-rating {
  display: flex;
  align-items: center;
  gap: 3px;
  color: $hero-amber;
  font-size: 12px;
  font-weight: 600;
}

.resource-downloads {
  font-size: 12px;
  color: $text-quaternary;
}

// 活动卡片
.activity-card {
  .activity-icon {
    width: 40px;
    height: 40px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
    flex-shrink: 0;
  }

  .activity-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 8px;
  }

  .activity-avatars {
    display: flex;
    align-items: center;
  }

  .mini-avatar {
    width: 20px;
    height: 20px;
    border-radius: 50%;
    border: 2px solid $white;
    margin-left: -6px;

    &:first-child {
      margin-left: 0;
    }
  }

  .avatar-more {
    font-size: 11px;
    color: $text-tertiary;
    margin-left: 6px;
  }

  .activity-deadline {
    font-size: 11px;
    color: $hero-amber;
    font-weight: 500;
  }
}

// 底部
.feed-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  padding: 10px;
  border-top: 1px dashed rgba($gray-200, 0.6);
  background: transparent;
}

.footer-text {
  font-size: 10px;
  color: $text-quaternary;
  opacity: 0.8;
}

.refresh-indicator {
  display: flex;
  align-items: center;
}

.refresh-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: $gray-300;
  transition: background 0.3s ease;

  &.active {
    background: $hero-teal;
    animation: refreshPulse 1s infinite;
  }
}

@keyframes refreshPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}
</style>
