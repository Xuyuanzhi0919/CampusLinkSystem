<template>
  <view class="badges-page">

    <!-- ══════════════════ 顶部导航栏 ══════════════════ -->
    <CNavBar title="我的徽章" :auto-back="true">
      <template #right>
        <view class="nav-rule-btn" @click="openRuleModal">
          <Icon name="info" :size="16" class="nav-rule-icon" />
          <text class="nav-rule-text">规则</text>
        </view>
      </template>
    </CNavBar>

    <!-- ══════════════════ 页面滚动区 ══════════════════ -->
    <scroll-view
      class="page-scroll"
      scroll-y
      ref="pageScrollRef"
      @mousedown="onScrollMouseDown"
    >
      <view class="page-inner">

        <!-- ── 加载中占位 ── -->
        <view v-if="badgeLoading" class="badge-loading">
          <view class="badge-loading__spinner" />
          <text class="badge-loading__text">加载徽章中...</text>
        </view>

        <!-- ── 成就概览 Banner ── -->
        <view class="overview-banner">
          <!-- 背景装饰圆 -->
          <view class="ov-deco ov-deco--1" />
          <view class="ov-deco ov-deco--2" />

          <!-- 三列核心数据 -->
          <view class="ov-stats-row">
            <view class="ov-stat">
              <text class="ov-stat-num">{{ unlockedCount }}</text>
              <text class="ov-stat-label">已解锁</text>
            </view>
            <view class="ov-divider-v" />
            <view class="ov-stat">
              <text class="ov-stat-num">{{ allBadges.length }}</text>
              <text class="ov-stat-label">徽章总数</text>
            </view>
            <view class="ov-divider-v" />
            <view class="ov-stat">
              <text class="ov-stat-num ov-stat-num--gold">+{{ totalRewardPoints }}</text>
              <text class="ov-stat-label">已获积分</text>
            </view>
          </view>

          <!-- 成就称号 -->
          <view class="ov-title-row">
            <view class="ov-badge-pill">
              <Icon name="award" :size="11" class="ov-badge-pill-icon" />
              <text class="ov-badge-pill-text">{{ achievementTitle }}</text>
            </view>
          </view>

          <!-- 进度条 -->
          <view class="ov-progress-area">
            <view class="ov-progress-header">
              <text class="ov-progress-label">成就进度</text>
              <text class="ov-progress-pct">{{ progressPercent }}%</text>
            </view>
            <view class="ov-track">
              <view class="ov-fill" :style="{ width: progressPercent + '%' }" />
            </view>
            <text v-if="remainCount > 0" class="ov-hint">
              再解锁 {{ remainCount }} 枚升至「{{ nextTitle }}」，可获 +{{ nextReward }} 积分
            </text>
            <text v-else class="ov-hint ov-hint--maxed">🎉 已解锁全部徽章！</text>
          </view>
        </view>

        <!-- ── 首页展示区（有固定徽章时显示）── -->
        <view v-if="pinnedIds.length > 0" class="pinned-section">
          <view class="pinned-header">
            <Icon name="star" :size="13" class="pinned-icon" />
            <text class="pinned-title">首页展示</text>
            <text class="pinned-count">{{ pinnedIds.length }}/{{ MAX_PINNED }}</text>
          </view>
          <view class="pinned-list">
            <view
              v-for="id in pinnedIds"
              :key="id"
              class="pinned-item"
              @click="openDetailModal(getBadge(id)!)"
            >
              <view
                class="pinned-icon-box"
                :class="`bc-color--${getBadge(id)?.color ?? 'blue'}`"
              >
                <Icon :name="getBadge(id)?.icon ?? 'star'" :size="20" class="bc-icon" :stroke-width="1.5" />
              </view>
              <text class="pinned-name">{{ getBadge(id)?.name }}</text>
            </view>
          </view>
        </view>

        <!-- ── 分类 Tab 栏（胶囊样式）── -->
        <view class="tab-bar-wrap">
          <scroll-view class="tab-scroll" scroll-x :show-scrollbar="false">
            <view class="tab-list">
              <view
                v-for="tab in TABS"
                :key="tab.key"
                class="tab-pill"
                :class="{ 'tab-pill--active': activeTab === tab.key }"
                :style="activeTab === tab.key
                  ? { '--tab-color': CATEGORY_COLORS[tab.key], background: CATEGORY_BG_COLORS[tab.key], borderColor: CATEGORY_COLORS[tab.key] + '55' }
                  : { '--tab-color': CATEGORY_COLORS[tab.key] }"
                @click="activeTab = tab.key"
              >
                <text class="tab-pill-label">{{ tab.label }}</text>
                <view class="tab-pill-badge" :class="{ 'tab-pill-badge--active': activeTab === tab.key }">
                  <text class="tab-pill-count">{{ tabCount(tab.key) }}</text>
                </view>
              </view>
            </view>
          </scroll-view>
        </view>

        <!-- ── 徽章网格 ── -->
        <view v-if="filteredBadges.length > 0" class="badge-grid">
          <view
            v-for="badge in filteredBadges"
            :key="badge.id"
            class="badge-card"
            :class="{ 'badge-card--locked': !badge.unlocked }"
            :style="badge.unlocked
              ? { '--bc-bg': BADGE_BG_MAP[badge.color] ?? '#F0F6FF', '--bc-border': BADGE_BORDER_MAP[badge.color] ?? 'rgba(55,125,255,0.2)' }
              : {}"
            @click="openDetailModal(badge)"
          >
            <!-- 已固定徽章：右上角星形角标 -->
            <view v-if="badge.unlocked && isPinned(badge.id)" class="bc-star-mark">
              <Icon name="star" :size="10" class="bc-star-icon" />
            </view>

            <!-- 图标区 -->
            <view
              class="bc-icon-wrap"
              :class="badge.unlocked ? `bc-color--${badge.color}` : 'bc-color--locked'"
            >
              <Icon :name="badge.icon" :size="26" class="bc-icon" :stroke-width="1.5" />
              <view v-if="badge.unlocked" class="bc-badge bc-badge--unlocked">
                <Icon name="check" :size="9" />
              </view>
              <view v-else class="bc-badge bc-badge--locked">
                <Icon name="lock" :size="9" />
              </view>
            </view>

            <!-- 徽章名称 -->
            <text class="bc-name">{{ badge.name }}</text>

            <!-- 解锁时间 / 去解锁 -->
            <text v-if="badge.unlocked && badge.unlockedAt" class="bc-date">{{ badge.unlockedAt }}</text>
            <text v-else-if="badge.unlocked" class="bc-date">已解锁</text>
            <text v-else class="bc-hint" :style="{ color: badgeCategoryColor(badge.category) }">去解锁 ›</text>
          </view>
        </view>

        <!-- ── 当前分类空状态 ── -->
        <view v-else-if="activeTab !== 'all'" class="empty-wrap">
          <text class="empty-emoji">🏅</text>
          <text class="empty-title">该分类下暂无徽章</text>
          <view class="empty-btn" @click="activeTab = 'all'">
            <text class="empty-btn-text">查看全部徽章</text>
          </view>
        </view>

        <!-- ── 全部为空（新用户）空状态 ── -->
        <view v-else class="empty-wrap">
          <text class="empty-emoji">🎯</text>
          <text class="empty-title">完成新手任务，解锁你的第一枚徽章！</text>
          <view class="empty-btn" @click="goToTask('growth')">
            <text class="empty-btn-text">去完成新手任务</text>
          </view>
        </view>

        <!-- 底部安全区 -->
        <view class="page-bottom-safe" />
      </view>
    </scroll-view>

    <!-- ══════════════════ 徽章详情弹窗 ══════════════════ -->
    <view
      v-if="showDetail"
      class="modal-overlay"
      :class="{ 'modal-overlay--dim': detailUp }"
      @click="closeDetailModal"
    >
      <view
        class="modal-card detail-card"
        :class="{ 'modal-card--up': detailUp }"
        @click.stop
      >
        <view class="modal-bar" />

        <!-- 徽章大图 + 名称 -->
        <view class="detail-hero">
          <view
            class="detail-icon-wrap"
            :class="detailBadge?.unlocked ? `bc-color--${detailBadge?.color}` : 'bc-color--locked'"
          >
            <Icon
              v-if="detailBadge"
              :name="detailBadge.icon"
              :size="40"
              class="detail-icon"
              :stroke-width="1.4"
            />
          </view>
          <text class="detail-name">{{ detailBadge?.name }}</text>
          <view v-if="detailBadge?.unlocked" class="detail-status-tag detail-status-tag--unlocked">
            <Icon name="check-circle" :size="13" class="detail-status-icon" />
            <text class="detail-status-text">已解锁</text>
          </view>
          <view v-else class="detail-status-tag detail-status-tag--locked">
            <Icon name="lock" :size="12" class="detail-status-icon" />
            <text class="detail-status-text">未解锁</text>
          </view>
        </view>

        <view class="detail-divider" />

        <!-- 信息区 -->
        <view class="detail-info">
          <view class="detail-row">
            <view class="detail-row-label">
              <Icon name="target" :size="14" class="detail-row-icon detail-row-icon--blue" />
              <text class="detail-row-key">解锁条件</text>
            </view>
            <text class="detail-row-val">{{ detailBadge?.condition }}</text>
          </view>

          <view v-if="detailBadge?.unlocked && detailBadge?.unlockedAt" class="detail-row">
            <view class="detail-row-label">
              <Icon name="calendar" :size="14" class="detail-row-icon detail-row-icon--teal" />
              <text class="detail-row-key">获得时间</text>
            </view>
            <text class="detail-row-val">{{ detailBadge.unlockedAt }}</text>
          </view>

          <view class="detail-row">
            <view class="detail-row-label">
              <Icon name="zap" :size="14" class="detail-row-icon detail-row-icon--amber" />
              <text class="detail-row-key">积分奖励</text>
            </view>
            <text class="detail-row-val detail-row-val--highlight">+{{ detailBadge?.reward }} 积分</text>
          </view>

          <view class="detail-desc-wrap">
            <text class="detail-desc">{{ detailBadge?.description }}</text>
          </view>
        </view>

        <!-- 操作按钮区 -->
        <view class="detail-actions">
          <template v-if="detailBadge?.unlocked">
            <view
              class="detail-btn detail-btn--secondary"
              :class="{ 'detail-btn--pinned': detailBadge && isPinned(detailBadge.id) }"
              @click="togglePin"
            >
              <Icon name="star" :size="15" class="detail-btn-icon" />
              <text class="detail-btn-text">
                {{ detailBadge && isPinned(detailBadge.id) ? '取消首页展示' : '设为首页展示' }}
              </text>
            </view>
            <view class="detail-btn detail-btn--ghost" @click="closeDetailModal">
              <text class="detail-btn-text">关闭</text>
            </view>
          </template>
          <template v-else>
            <view class="detail-btn detail-btn--primary" @click="handleGoTask">
              <Icon name="arrow-right" :size="15" class="detail-btn-icon" />
              <text class="detail-btn-text">去完成任务</text>
            </view>
            <view class="detail-btn detail-btn--ghost" @click="closeDetailModal">
              <text class="detail-btn-text">关闭</text>
            </view>
          </template>
        </view>

        <view class="modal-bottom-safe" />
      </view>
    </view>

    <!-- ══════════════════ 徽章规则弹窗 ══════════════════ -->
    <view
      v-if="showRule"
      class="modal-overlay"
      :class="{ 'modal-overlay--dim': ruleUp }"
      @click="closeRuleModal"
    >
      <view
        class="modal-card rule-card"
        :class="{ 'modal-card--up': ruleUp }"
        @click.stop
      >
        <view class="modal-bar" />
        <view class="rule-header">
          <text class="rule-title">徽章规则说明</text>
        </view>
        <scroll-view class="rule-scroll" scroll-y>
          <view class="rule-body">
            <view v-for="rule in RULE_SECTIONS" :key="rule.title" class="rule-section">
              <view class="rule-section-header">
                <Icon :name="rule.icon" :size="15" class="rule-section-icon" />
                <text class="rule-section-title">{{ rule.title }}</text>
              </view>
              <view class="rule-section-content">
                <text v-for="(line, i) in rule.lines" :key="i" class="rule-line">{{ line }}</text>
              </view>
            </view>
          </view>
        </scroll-view>
        <view class="rule-footer">
          <view class="detail-btn detail-btn--primary" @click="closeRuleModal">
            <text class="detail-btn-text">知道了</text>
          </view>
        </view>
        <view class="modal-bottom-safe" />
      </view>
    </view>

  </view>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted } from 'vue'
import CNavBar from '@/components/layout/CNavBar.vue'
import Icon from '@/components/icons/index.vue'
import { getUserBadges } from '@/services/user'

/* ──────────────────────────────────────────
   类型定义
────────────────────────────────────────── */
interface Badge {
  id: number
  name: string
  icon: string
  color: string          // blue | teal | violet | amber | rose
  category: string       // growth | contribution | interaction | limited
  unlocked: boolean
  unlockedAt?: string    // 解锁日期 'YYYY-MM-DD'
  description: string
  condition: string
  reward: number
}

/* ──────────────────────────────────────────
   徽章数据（从 API 加载）
────────────────────────────────────────── */
const badgeLoading = ref(true)
const allBadges = ref<Badge[]>([])

/* ──────────────────────────────────────────
   常量配置
────────────────────────────────────────── */
const TABS = [
  { key: 'all',          label: '全部'  },
  { key: 'growth',       label: '成长类' },
  { key: 'contribution', label: '贡献类' },
  { key: 'interaction',  label: '互动类' },
  { key: 'limited',      label: '限定类' },
]

const CATEGORY_COLORS: Record<string, string> = {
  all:          '#377DFF',
  growth:       '#377DFF',
  contribution: '#10B981',
  interaction:  '#8B5CF6',
  limited:      '#F97316',
}

const CATEGORY_BG_COLORS: Record<string, string> = {
  all:          'rgba(55, 125, 255, 0.08)',
  growth:       'rgba(55, 125, 255, 0.08)',
  contribution: 'rgba(16, 185, 129, 0.08)',
  interaction:  'rgba(139, 92, 246, 0.08)',
  limited:      'rgba(249, 115, 22, 0.08)',
}

const BADGE_BG_MAP: Record<string, string> = {
  blue:   '#EFF6FF',
  teal:   '#ECFDF5',
  violet: '#F5F3FF',
  amber:  '#FFFBEB',
  rose:   '#FFF1F2',
}

const BADGE_BORDER_MAP: Record<string, string> = {
  blue:   'rgba(59,130,246,0.25)',
  teal:   'rgba(16,185,129,0.25)',
  violet: 'rgba(139,92,246,0.25)',
  amber:  'rgba(245,158,11,0.22)',
  rose:   'rgba(244,63,94,0.22)',
}

const badgeCategoryColor = (category: string): string =>
  CATEGORY_COLORS[category] ?? CATEGORY_COLORS.all

const ACHIEVEMENT_TITLES = [
  { min: 0,  title: '探索者',   next: '初学者',   nextReward: 20  },
  { min: 1,  title: '初学者',   next: '进阶者',   nextReward: 30  },
  { min: 3,  title: '进阶者',   next: '知识达人', nextReward: 50  },
  { min: 6,  title: '知识达人', next: '贡献之星', nextReward: 80  },
  { min: 9,  title: '贡献之星', next: '徽章达人', nextReward: 100 },
  { min: 11, title: '徽章达人', next: '',         nextReward: 0   },
]

const BADGE_PINNED_KEY = 'cl_badge_pinned'
const MAX_PINNED = 4

const RULE_SECTIONS = [
  {
    icon: 'info',
    title: '徽章体系介绍',
    lines: [
      '徽章是 CampusLink 对用户在平台贡献与成长的认可。',
      '完成指定任务后即可解锁对应徽章，展示你的成就。',
    ],
  },
  {
    icon: 'grid',
    title: '徽章分类',
    lines: [
      '成长类：与账号成长、签到、等级提升相关。',
      '贡献类：上传资源、输出优质内容相关。',
      '互动类：互动获赞、帮助他人、积累收藏相关。',
      '限定类：限时活动、节日任务专属徽章。',
    ],
  },
  {
    icon: 'zap',
    title: '积分权益',
    lines: [
      '每枚徽章首次解锁即可获得对应积分奖励。',
      '积分可在积分商城中兑换实物或虚拟奖励。',
    ],
  },
  {
    icon: 'star',
    title: '首页展示',
    lines: [
      '每位用户可在「我的徽章」页面选择最多 4 枚徽章展示在个人主页。',
      '点击已解锁徽章 → 「设为首页展示」即可完成设置。',
    ],
  },
]

/* ──────────────────────────────────────────
   响应式状态
────────────────────────────────────────── */
const activeTab = ref<string>('all')

const loadPinned = (): number[] => {
  try {
    const raw = uni.getStorageSync(BADGE_PINNED_KEY)
    return raw ? JSON.parse(raw) : []
  } catch {
    return []
  }
}
const pinnedIds = ref<number[]>(loadPinned())

/* ── H5 鼠标拖拽滚动 ── */
const pageScrollRef = ref()
// #ifdef H5
const onScrollMouseDown = (e: MouseEvent) => {
  const el = (pageScrollRef.value as any)?.$el as HTMLElement | null
  if (!el) return
  const startY   = e.clientY
  const startTop = el.scrollTop
  let moved = false

  const handleMove = (ev: MouseEvent) => {
    const diff = ev.clientY - startY
    if (!moved && Math.abs(diff) < 4) return
    moved = true
    el.scrollTop = startTop - diff
    ev.preventDefault()
  }
  const handleUp = () => {
    window.removeEventListener('mousemove', handleMove)
    window.removeEventListener('mouseup',   handleUp)
  }
  window.addEventListener('mousemove', handleMove)
  window.addEventListener('mouseup',   handleUp)
}
// #endif

const showDetail  = ref(false)
const detailUp    = ref(false)
const detailBadge = ref<Badge | null>(null)

const showRule = ref(false)
const ruleUp   = ref(false)

/* ──────────────────────────────────────────
   计算属性
────────────────────────────────────────── */
const unlockedCount   = computed(() => allBadges.value.filter(b => b.unlocked).length)
const remainCount     = computed(() => allBadges.value.length - unlockedCount.value)
const progressPercent = computed(() =>
  allBadges.value.length ? Math.round((unlockedCount.value / allBadges.value.length) * 100) : 0
)
const totalRewardPoints = computed(() =>
  allBadges.value.filter(b => b.unlocked).reduce((sum, b) => sum + b.reward, 0)
)

const achievementTitle = computed(() => {
  const n = unlockedCount.value
  for (let i = ACHIEVEMENT_TITLES.length - 1; i >= 0; i--) {
    if (n >= ACHIEVEMENT_TITLES[i].min) return ACHIEVEMENT_TITLES[i].title
  }
  return '探索者'
})
const nextTitle = computed(() => {
  const n = unlockedCount.value
  for (let i = ACHIEVEMENT_TITLES.length - 1; i >= 0; i--) {
    if (n >= ACHIEVEMENT_TITLES[i].min) return ACHIEVEMENT_TITLES[i].next
  }
  return ''
})
const nextReward = computed(() => {
  const n = unlockedCount.value
  for (let i = ACHIEVEMENT_TITLES.length - 1; i >= 0; i--) {
    if (n >= ACHIEVEMENT_TITLES[i].min) return ACHIEVEMENT_TITLES[i].nextReward
  }
  return 0
})

const sortedBadges = computed(() => {
  const unlocked = allBadges.value
    .filter(b => b.unlocked)
    .sort((a, b) => {
      if (a.unlockedAt && b.unlockedAt) return b.unlockedAt.localeCompare(a.unlockedAt)
      if (a.unlockedAt) return -1
      if (b.unlockedAt) return 1
      return 0
    })
  return [...unlocked, ...allBadges.value.filter(b => !b.unlocked)]
})

const filteredBadges = computed(() => {
  if (activeTab.value === 'all') return sortedBadges.value
  return sortedBadges.value.filter(b => b.category === activeTab.value)
})

const tabCount = (key: string) => {
  if (key === 'all') return allBadges.value.length
  return allBadges.value.filter(b => b.category === key).length
}

/* ──────────────────────────────────────────
   工具方法
────────────────────────────────────────── */
const getBadge = (id: number): Badge | null =>
  allBadges.value.find(b => b.id === id) ?? null

/* ──────────────────────────────────────────
   首页固定
────────────────────────────────────────── */
const isPinned = (id: number) => pinnedIds.value.includes(id)

const savePinned = () =>
  uni.setStorageSync(BADGE_PINNED_KEY, JSON.stringify(pinnedIds.value))

const togglePin = () => {
  if (!detailBadge.value) return
  const id = detailBadge.value.id
  if (pinnedIds.value.includes(id)) {
    pinnedIds.value = pinnedIds.value.filter(i => i !== id)
    savePinned()
    uni.showToast({ title: '已取消首页展示', icon: 'none' })
  } else {
    if (pinnedIds.value.length >= MAX_PINNED) {
      uni.showModal({
        title: '首页展示已满',
        content: `最多展示 ${MAX_PINNED} 枚徽章，请先取消其他徽章的展示`,
        showCancel: false,
        confirmText: '知道了',
      })
      return
    }
    pinnedIds.value = [...pinnedIds.value, id]
    savePinned()
    uni.showToast({ title: '已设为首页展示', icon: 'none' })
  }
}

/* ──────────────────────────────────────────
   弹窗控制
────────────────────────────────────────── */
const openDetailModal = (badge: Badge | null) => {
  if (!badge) return
  detailBadge.value = badge
  showDetail.value = true
  nextTick(() => { detailUp.value = true })
}
const closeDetailModal = () => {
  detailUp.value = false
  setTimeout(() => { showDetail.value = false; detailBadge.value = null }, 300)
}

const openRuleModal = () => {
  showRule.value = true
  nextTick(() => { ruleUp.value = true })
}
const closeRuleModal = () => {
  ruleUp.value = false
  setTimeout(() => { showRule.value = false }, 300)
}

/* ──────────────────────────────────────────
   导航
────────────────────────────────────────── */
const CATEGORY_URL: Record<string, string> = {
  growth:       '/pages/user/index',
  contribution: '/pages/resource/upload',
  interaction:  '/pages/question/index',
  limited:      '',
}

const goToTask = (category: string) => {
  const url = CATEGORY_URL[category]
  if (!url) {
    uni.showToast({ title: '关注平台活动即可获得', icon: 'none' })
    return
  }
  uni.navigateTo({ url, fail: () => uni.showToast({ title: '页面开发中...', icon: 'none' }) })
}

const handleGoTask = () => {
  if (!detailBadge.value) return
  const category = detailBadge.value.category
  closeDetailModal()
  setTimeout(() => goToTask(category), 320)
}

/* ──────────────────────────────────────────
   生命周期
────────────────────────────────────────── */
onMounted(async () => {
  try {
    const data = await getUserBadges()
    allBadges.value = data || []
  } catch (e) {
    uni.showToast({ title: '徽章加载失败', icon: 'none' })
  } finally {
    badgeLoading.value = false
  }
})
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

/* ══════════════════════════════════════════
   页面骨架
══════════════════════════════════════════ */
.badges-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background: $color-bg-page;
}

.page-scroll {
  flex: 1;
  min-height: 0;
}

// #ifdef H5
.page-scroll {
  cursor: grab;
  &:active { cursor: grabbing; }
}
// #endif

.page-inner {
  padding: 20rpx 24rpx 0;
  display: flex;
  flex-direction: column;
  gap: 20rpx;

  @media (min-width: 1024px) {
    padding: 20px 0;
    max-width: 960px;
    margin: 0 auto;
    width: 100%;
    gap: 16px;
  }
}

.page-bottom-safe {
  height: 80rpx;
  @media (min-width: 1024px) { height: 40px; }
}

/* ══════════════════════════════════════════
   导航栏右侧按钮
══════════════════════════════════════════ */
.nav-rule-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 8rpx 16rpx;
  border-radius: 100rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.35);
  cursor: pointer;
  transition: background 0.15s ease;

  &:active { background: rgba(255, 255, 255, 0.15); }

  // #ifdef H5
  &:hover { background: rgba(255, 255, 255, 0.15); }
  // #endif

  @media (min-width: 1024px) {
    gap: 4px;
    padding: 5px 12px;
    border-radius: 100px;
  }
}

.nav-rule-icon {
  color: rgba(255, 255, 255, 0.85);
}

.nav-rule-text {
  font-size: 24rpx;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);

  @media (min-width: 1024px) { font-size: 13px; }
}

/* ══════════════════════════════════════════
   成就概览 Banner（渐变背景）
══════════════════════════════════════════ */
.overview-banner {
  background: linear-gradient(135deg, #1a2e72 0%, #2952c4 55%, #3d6ee8 100%);
  border-radius: 24rpx;
  padding: 36rpx 28rpx 28rpx;
  position: relative;
  overflow: hidden;

  @media (min-width: 1024px) {
    border-radius: 16px;
    padding: 28px 32px;
  }
}

/* 背景装饰圆 */
.ov-deco {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;

  &--1 {
    width: 280rpx;
    height: 280rpx;
    top: -80rpx;
    right: -60rpx;
    background: rgba(255, 255, 255, 0.06);

    @media (min-width: 1024px) {
      width: 200px;
      height: 200px;
      top: -60px;
      right: -40px;
    }
  }

  &--2 {
    width: 160rpx;
    height: 160rpx;
    top: 20rpx;
    right: 60rpx;
    background: rgba(255, 255, 255, 0.04);

    @media (min-width: 1024px) {
      width: 120px;
      height: 120px;
      top: 10px;
      right: 80px;
    }
  }
}

/* 三列统计数据 */
.ov-stats-row {
  display: flex;
  align-items: center;
  justify-content: space-around;
  margin-bottom: 28rpx;
  position: relative;
  z-index: 1;

  @media (min-width: 1024px) { margin-bottom: 20px; }
}

.ov-stat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;

  @media (min-width: 1024px) { gap: 5px; }
}

.ov-stat-num {
  font-size: 56rpx;
  font-weight: 800;
  color: #fff;
  line-height: 1;
  letter-spacing: -0.02em;

  &--gold {
    color: #FFD166;  // 金色：积分数字高亮
  }

  @media (min-width: 1024px) { font-size: 36px; }
}

.ov-stat-label {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.60);
  font-weight: 400;
  letter-spacing: 0.02em;

  @media (min-width: 1024px) { font-size: 12px; }
}

.ov-divider-v {
  width: 1rpx;
  height: 56rpx;
  background: rgba(255, 255, 255, 0.15);
  flex-shrink: 0;

  @media (min-width: 1024px) { width: 1px; height: 40px; }
}

/* 成就称号胶囊 */
.ov-title-row {
  display: flex;
  justify-content: center;
  margin-bottom: 22rpx;
  position: relative;
  z-index: 1;

  @media (min-width: 1024px) { margin-bottom: 16px; }
}

.ov-badge-pill {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  background: rgba(255, 255, 255, 0.12);
  border: 1rpx solid rgba(255, 255, 255, 0.22);
  border-radius: 100rpx;
  padding: 7rpx 18rpx 7rpx 13rpx;

  @media (min-width: 1024px) {
    gap: 5px;
    padding: 5px 14px 5px 10px;
    border-radius: 100px;
    border-width: 1px;
  }
}

.ov-badge-pill-icon {
  color: rgba(255, 255, 255, 0.80);
}

.ov-badge-pill-text {
  font-size: 21rpx;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.90);
  letter-spacing: 0.02em;

  @media (min-width: 1024px) { font-size: 13px; }
}

/* 进度区域 */
.ov-progress-area {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  position: relative;
  z-index: 1;

  @media (min-width: 1024px) { gap: 7px; }
}

.ov-progress-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.ov-progress-label {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.55);
  font-weight: 400;

  @media (min-width: 1024px) { font-size: 12px; }
}

.ov-progress-pct {
  font-size: 22rpx;
  font-weight: 700;
  color: #FFD166;

  @media (min-width: 1024px) { font-size: 13px; }
}

.ov-track {
  width: 100%;
  height: 8rpx;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 100rpx;
  overflow: hidden;

  @media (min-width: 1024px) { height: 6px; border-radius: 100px; }
}

.ov-fill {
  height: 100%;
  background: linear-gradient(90deg, #60A5FA 0%, #A78BFA 100%);
  border-radius: 100rpx;
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.ov-hint {
  font-size: 19rpx;
  color: rgba(255, 255, 255, 0.50);
  line-height: 1.4;

  &--maxed {
    color: #FFD166;
    font-weight: 500;
  }

  @media (min-width: 1024px) { font-size: 12px; }
}

/* ══════════════════════════════════════════
   首页展示区
══════════════════════════════════════════ */
.pinned-section {
  background: $color-bg-card;
  border-radius: 20rpx;
  border: 1rpx solid $color-border-light;
  box-shadow: $shadow-xs;
  padding: 24rpx 24rpx 20rpx;

  @media (min-width: 1024px) {
    border-radius: 14px;
    border-width: 1px;
    padding: 18px 20px 16px;
  }
}

.pinned-header {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 18rpx;

  @media (min-width: 1024px) { margin-bottom: 14px; gap: 6px; }
}

.pinned-icon {
  color: #F59E0B;
  flex-shrink: 0;
}

.pinned-title {
  font-size: 25rpx;
  font-weight: 600;
  color: $color-text-primary;
  flex: 1;

  @media (min-width: 1024px) { font-size: 14px; }
}

.pinned-count {
  font-size: 21rpx;
  color: $color-text-tertiary;
  font-weight: 400;

  @media (min-width: 1024px) { font-size: 12px; }
}

.pinned-list {
  display: flex;
  gap: 20rpx;

  @media (min-width: 1024px) { gap: 16px; }
}

.pinned-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  cursor: pointer;

  @media (min-width: 1024px) { gap: 7px; }
}

.pinned-icon-box {
  width: 72rpx;
  height: 72rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s ease;

  &:active { transform: scale(0.90); }

  // #ifdef H5
  &:hover { transform: translateY(-2px) scale(1.04); }
  // #endif

  @media (min-width: 1024px) {
    width: 48px;
    height: 48px;
    border-radius: 12px;
  }
}

.pinned-name {
  font-size: 19rpx;
  color: $color-text-secondary;
  text-align: center;
  white-space: nowrap;

  @media (min-width: 1024px) { font-size: 11px; }
}

/* ══════════════════════════════════════════
   分类 Tab 栏（胶囊样式）
══════════════════════════════════════════ */
.tab-bar-wrap {
  margin: 0 -24rpx;

  @media (min-width: 1024px) { margin: 0; }
}

.tab-scroll {
  width: 100%;
  white-space: nowrap;
}

.tab-list {
  display: flex;
  flex-direction: row;
  padding: 0 24rpx;
  gap: 10rpx;

  @media (min-width: 1024px) {
    padding: 0;
    gap: 8px;
  }
}

.tab-pill {
  --tab-color: #377DFF;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 7rpx;
  padding: 14rpx 22rpx;
  border-radius: 100rpx;
  background: $color-bg-hover;
  border: 1.5rpx solid transparent;
  cursor: pointer;
  transition: all 0.18s ease;

  &--active {
    .tab-pill-label { color: var(--tab-color); font-weight: 600; }
  }

  &:active { opacity: 0.7; }

  // #ifdef H5
  &:not(.tab-pill--active):hover {
    background: $color-border-light;
  }
  // #endif

  @media (min-width: 1024px) {
    padding: 8px 16px;
    gap: 5px;
    border-radius: 100px;
    border-width: 1px;
  }
}

.tab-pill-label {
  font-size: 25rpx;
  color: $color-text-secondary;
  font-weight: 500;
  transition: color 0.18s ease;

  @media (min-width: 1024px) { font-size: 13px; }
}

.tab-pill-badge {
  background: $color-border;
  border-radius: 100rpx;
  padding: 2rpx 10rpx;
  transition: background 0.18s ease;

  &--active { background: var(--tab-color); }

  @media (min-width: 1024px) {
    border-radius: 100px;
    padding: 1px 7px;
  }
}

.tab-pill-count {
  font-size: 18rpx;
  color: $color-text-tertiary;

  .tab-pill-badge--active & {
    color: #fff;
  }

  @media (min-width: 1024px) { font-size: 11px; }
}

/* ══════════════════════════════════════════
   徽章网格
══════════════════════════════════════════ */
.badge-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14rpx;

  @media (min-width: 600px) {
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
  }

  @media (min-width: 1024px) {
    grid-template-columns: repeat(5, 1fr);
    gap: 14px;
  }
}

/* ─── 单张徽章卡片 ─── */
.badge-card {
  --bc-bg:     #{$color-bg-card};
  --bc-border: #{$color-border-light};
  background: var(--bc-bg);
  border-radius: 18rpx;
  border: 1.5rpx solid var(--bc-border);
  box-shadow: $shadow-xs;
  padding: 24rpx 12rpx 18rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  cursor: pointer;
  position: relative;
  transition: box-shadow 0.2s ease, transform 0.2s ease;

  &:active {
    transform: translateY(1rpx) scale(0.98);
  }

  // #ifdef H5
  &:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.10), 0 2px 8px rgba(0, 0, 0, 0.05),
                0 0 0 2px rgba(55, 125, 255, 0.15);
    transform: translateY(-3px);
  }
  // #endif

  &--locked {
    background: #F7F8FA;
    opacity: 0.60;
    filter: grayscale(60%);

    &:active { transform: none; }

    // #ifdef H5
    &:hover {
      opacity: 0.80;
      filter: grayscale(20%);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.07);
      transform: none;
    }
    // #endif
  }

  @media (min-width: 1024px) {
    border-radius: 12px;
    padding: 20px 12px 16px;
    gap: 8px;
    border-width: 1px;
  }
}

/* 已固定：右上角星形角标 */
.bc-star-mark {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  width: 28rpx;
  height: 28rpx;
  border-radius: 50%;
  background: #F59E0B;
  display: flex;
  align-items: center;
  justify-content: center;

  @media (min-width: 1024px) {
    width: 18px;
    height: 18px;
    top: 6px;
    right: 6px;
  }
}

.bc-star-icon {
  color: #fff;
}

/* ─── 图标容器 ─── */
.bc-icon-wrap {
  width: 72rpx;
  height: 72rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  flex-shrink: 0;

  @media (min-width: 1024px) {
    width: 52px;
    height: 52px;
    border-radius: 13px;
  }
}

/* 图标颜色系统 */
.bc-color {
  &--blue   { background: #DBEAFE; .bc-icon { color: #2563EB; } }
  &--teal   { background: #D1FAE5; .bc-icon { color: #0D9488; } }
  &--violet { background: #EDE9FE; .bc-icon { color: #7C3AED; } }
  &--amber  { background: #FEF3C7; .bc-icon { color: #D97706; } }
  &--rose   { background: #FFE4E6; .bc-icon { color: #E11D48; } }
  &--locked {
    background: #EFEFEF;
    .bc-icon { color: #C4C4C4; }
  }
}

/* 角标（对勾 / 锁） */
.bc-badge {
  position: absolute;
  top: -6rpx;
  right: -6rpx;
  width: 26rpx;
  height: 26rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid $color-bg-card;

  &--unlocked { background: $color-success; color: #fff; }
  &--locked   { background: $color-text-quaternary; color: #fff; }

  @media (min-width: 1024px) {
    width: 18px;
    height: 18px;
    top: -4px;
    right: -4px;
    border-width: 1.5px;
  }
}

.bc-name {
  font-size: 22rpx;
  font-weight: 600;
  color: $color-text-primary;
  text-align: center;
  line-height: 1.3;

  @media (min-width: 1024px) { font-size: 12px; }
}

.bc-date {
  font-size: 18rpx;
  color: $color-text-tertiary;
  text-align: center;

  @media (min-width: 1024px) { font-size: 10px; }
}

.bc-hint {
  font-size: 18rpx;
  color: $campus-blue;
  font-weight: 500;

  @media (min-width: 1024px) { font-size: 10px; }
}

/* ══════════════════════════════════════════
   空状态
══════════════════════════════════════════ */
.empty-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 40rpx;
  gap: 20rpx;

  @media (min-width: 1024px) { padding: 60px 40px; gap: 16px; }
}

.empty-emoji {
  font-size: 80rpx;
  line-height: 1;

  @media (min-width: 1024px) { font-size: 56px; }
}

.empty-title {
  font-size: 28rpx;
  color: $color-text-tertiary;
  text-align: center;
  line-height: 1.5;

  @media (min-width: 1024px) { font-size: 15px; }
}

.empty-btn {
  background: $campus-blue;
  border-radius: 100rpx;
  padding: 18rpx 40rpx;
  cursor: pointer;
  transition: background 0.15s ease;

  &:active { background: $campus-blue-dark; }

  // #ifdef H5
  &:hover { background: $campus-blue-light; }
  // #endif

  @media (min-width: 1024px) {
    border-radius: 100px;
    padding: 10px 28px;
  }
}

.empty-btn-text {
  font-size: 26rpx;
  font-weight: 500;
  color: #fff;

  @media (min-width: 1024px) { font-size: 14px; }
}

/* ══════════════════════════════════════════
   弹窗通用：遮罩 + 卡片
══════════════════════════════════════════ */
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: $z-max;
  background: rgba(0, 0, 0, 0);
  transition: background 0.3s ease;
  pointer-events: none;
  display: flex;
  align-items: flex-end;
  justify-content: center;

  &--dim {
    background: rgba(0, 0, 0, 0.45);
    pointer-events: auto;
  }

  @media (min-width: 1024px) { align-items: center; }
}

.modal-card {
  position: relative;
  width: 100%;
  background: $white;
  border-radius: 32rpx 32rpx 0 0;
  box-shadow: 0 -8px 32px rgba(0, 0, 0, 0.12);
  transform: translateY(100%);
  transition: transform 0.3s cubic-bezier(0.32, 0.72, 0, 1);
  pointer-events: auto;
  overflow: hidden;

  &--up { transform: translateY(0); }

  @media (min-width: 1024px) {
    width: 460px;
    max-width: 90vw;
    border-radius: 20px;
    box-shadow: $shadow-xl;
    transform: scale(0.94);
    opacity: 0;
    transition: transform 0.22s cubic-bezier(0.34, 1.56, 0.64, 1), opacity 0.18s ease;

    &--up { transform: scale(1); opacity: 1; }
  }
}

.modal-bar {
  width: 48rpx;
  height: 5rpx;
  background: $color-border;
  border-radius: 100rpx;
  margin: 16rpx auto 0;

  @media (min-width: 1024px) { display: none; }
}

.modal-bottom-safe {
  height: 72rpx;
  @media (min-width: 1024px) { height: 8px; }
}

/* ══════════════════════════════════════════
   徽章详情弹窗内容
══════════════════════════════════════════ */
.detail-hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 28rpx 32rpx 20rpx;
  gap: 14rpx;

  @media (min-width: 1024px) {
    padding: 24px 32px 18px;
    gap: 10px;
  }
}

.detail-icon-wrap {
  width: 120rpx;
  height: 120rpx;
  border-radius: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;

  @media (min-width: 1024px) {
    width: 80px;
    height: 80px;
    border-radius: 20px;
  }
}

.detail-name {
  font-size: 36rpx;
  font-weight: 700;
  color: $color-text-primary;
  text-align: center;

  @media (min-width: 1024px) { font-size: 20px; }
}

.detail-status-tag {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  border-radius: 100rpx;
  padding: 6rpx 18rpx 6rpx 12rpx;

  &--unlocked {
    background: $color-success-bg;

    .detail-status-icon { color: $color-success; }
    .detail-status-text { color: $color-success; }
  }

  &--locked {
    background: $color-bg-hover;

    .detail-status-icon { color: $color-text-tertiary; }
    .detail-status-text { color: $color-text-tertiary; }
  }

  @media (min-width: 1024px) {
    gap: 4px;
    padding: 4px 14px 4px 10px;
    border-radius: 100px;
  }
}

.detail-status-icon {}

.detail-status-text {
  font-size: 22rpx;
  font-weight: 600;

  @media (min-width: 1024px) { font-size: 13px; }
}

.detail-divider {
  height: 1rpx;
  background: $color-divider;
  margin: 0 32rpx;

  @media (min-width: 1024px) { height: 1px; margin: 0 32px; }
}

.detail-info {
  padding: 22rpx 32rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;

  @media (min-width: 1024px) {
    padding: 18px 32px;
    gap: 13px;
  }
}

.detail-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12rpx;

  @media (min-width: 1024px) { gap: 10px; }
}

.detail-row-label {
  display: flex;
  align-items: center;
  gap: 8rpx;
  flex-shrink: 0;

  @media (min-width: 1024px) { gap: 6px; }
}

.detail-row-icon {
  flex-shrink: 0;

  &--blue   { color: #3B82F6; }
  &--teal   { color: #0D9488; }
  &--amber  { color: #D97706; }
}

.detail-row-key {
  font-size: 24rpx;
  color: $color-text-secondary;
  font-weight: 500;

  @media (min-width: 1024px) { font-size: 13px; }
}

.detail-row-val {
  font-size: 24rpx;
  color: $color-text-primary;
  text-align: right;
  flex: 1;
  line-height: 1.4;

  &--highlight {
    color: #D97706;
    font-weight: 600;
  }

  @media (min-width: 1024px) { font-size: 13px; }
}

.detail-desc-wrap {
  background: $color-bg-hover;
  border-radius: 12rpx;
  padding: 16rpx 20rpx;

  @media (min-width: 1024px) {
    border-radius: 8px;
    padding: 12px 16px;
  }
}

.detail-desc {
  font-size: 22rpx;
  color: $color-text-secondary;
  line-height: 1.6;

  @media (min-width: 1024px) { font-size: 13px; }
}

/* 操作按钮区 */
.detail-actions {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  padding: 8rpx 28rpx 0;

  @media (min-width: 1024px) {
    padding: 4px 28px 0;
    gap: 8px;
  }
}

.detail-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  height: 88rpx;
  border-radius: 16rpx;
  cursor: pointer;
  transition: all 0.15s ease;

  &--primary {
    background: $campus-blue;
    box-shadow: 0 4rpx 12rpx rgba($campus-blue, 0.30);

    .detail-btn-icon { color: #fff; }
    .detail-btn-text { color: #fff; }

    &:active { background: $campus-blue-dark; }
  }

  &--secondary {
    background: #FFFBEB;
    border: 1.5rpx solid #FDE68A;

    .detail-btn-icon { color: #D97706; }
    .detail-btn-text { color: #D97706; }

    &:active { background: #FEF3C7; }
  }

  &--secondary.detail-btn--pinned {
    background: #EFF6FF;
    border-color: #BFDBFE;

    .detail-btn-icon { color: $campus-blue; }
    .detail-btn-text { color: $campus-blue; }
  }

  &--ghost {
    background: $color-bg-hover;

    .detail-btn-text { color: $color-text-secondary; }

    &:active { background: $color-border-light; }
  }

  // #ifdef H5
  &--primary:hover   { background: $campus-blue-light; }
  &--secondary:hover { background: #FEF3C7; }
  &--ghost:hover     { background: $color-border-light; }
  // #endif

  @media (min-width: 1024px) {
    height: 44px;
    border-radius: 10px;
    gap: 6px;
    border-width: 1px;
  }
}

.detail-btn-text {
  font-size: 28rpx;
  font-weight: 600;

  @media (min-width: 1024px) { font-size: 14px; }
}

.detail-btn-icon {}

/* ══════════════════════════════════════════
   规则弹窗内容
══════════════════════════════════════════ */
.rule-header {
  padding: 24rpx 32rpx 16rpx;

  @media (min-width: 1024px) { padding: 20px 28px 14px; }
}

.rule-title {
  font-size: 32rpx;
  font-weight: 700;
  color: $color-text-primary;

  @media (min-width: 1024px) { font-size: 18px; }
}

.rule-scroll {
  max-height: 56vh;

  @media (min-width: 1024px) { max-height: 360px; }
}

.rule-body {
  padding: 0 32rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;

  @media (min-width: 1024px) {
    padding: 0 28px;
    gap: 18px;
  }
}

.rule-section {}

.rule-section-header {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 12rpx;

  @media (min-width: 1024px) {
    gap: 7px;
    margin-bottom: 8px;
  }
}

.rule-section-icon {
  color: $campus-blue;
  flex-shrink: 0;
}

.rule-section-title {
  font-size: 26rpx;
  font-weight: 600;
  color: $color-text-primary;

  @media (min-width: 1024px) { font-size: 14px; }
}

.rule-section-content {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  padding-left: 28rpx;

  @media (min-width: 1024px) { gap: 5px; padding-left: 20px; }
}

.rule-line {
  font-size: 23rpx;
  color: $color-text-secondary;
  line-height: 1.6;

  @media (min-width: 1024px) { font-size: 13px; }
}

.rule-footer {
  padding: 20rpx 28rpx;

  @media (min-width: 1024px) { padding: 16px 28px; }
}

.badge-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 0;
  gap: 16rpx;

  &__spinner {
    width: 48rpx;
    height: 48rpx;
    border: 4rpx solid #e5e7eb;
    border-top-color: #377DFF;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }

  &__text {
    font-size: 26rpx;
    color: #9ca3af;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
