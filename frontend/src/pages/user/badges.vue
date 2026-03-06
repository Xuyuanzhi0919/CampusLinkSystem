<template>
  <view class="badges-page">

    <!-- ══════════════════ 顶部导航栏 ══════════════════ -->
    <CNavBar title="我的徽章" :auto-back="true">
      <template #right>
        <view class="nav-rule-btn" @click="openRuleModal">
          <text class="nav-rule-text">徽章规则</text>
        </view>
      </template>
    </CNavBar>

    <!-- ══════════════════ 页面滚动区 ══════════════════ -->
    <scroll-view class="page-scroll" scroll-y>
      <view class="page-inner">

        <!-- ── 成就概览卡片 ── -->
        <view class="overview-card">
          <view class="ov-left">
            <view class="ov-count-row">
              <text class="ov-count">{{ unlockedCount }}</text>
              <text class="ov-count-total"> / {{ allBadges.length }} 枚</text>
            </view>
            <view class="ov-title-row">
              <view class="ov-badge-tag">
                <Icon name="award" :size="12" class="ov-badge-icon" />
                <text class="ov-badge-label">{{ achievementTitle }}</text>
              </view>
            </view>
            <view class="ov-reward-row">
              <Icon name="zap" :size="12" class="ov-reward-icon" />
              <text class="ov-reward-text">已通过徽章获得 {{ totalRewardPoints }} 积分</text>
            </view>
          </view>
          <view class="ov-right">
            <view class="ov-progress-header">
              <text class="ov-progress-label">成就进度</text>
              <text class="ov-progress-pct">{{ progressPercent }}%</text>
            </view>
            <view class="ov-progress-track">
              <view class="ov-progress-fill" :style="{ width: progressPercent + '%' }" />
            </view>
            <view v-if="remainCount > 0" class="ov-hint-row">
              <text class="ov-progress-hint">再解锁 {{ remainCount }} 枚，升级为「{{ nextTitle }}」，解锁</text>
              <text class="ov-reward-highlight"> +{{ nextReward }} 积分</text>
            </view>
            <text v-else class="ov-progress-hint ov-progress-hint--maxed">
              已解锁全部徽章 🎉
            </text>
          </view>
        </view>

        <!-- ── 分类 Tab 栏 ── -->
        <view class="tab-bar-wrap">
          <scroll-view class="tab-scroll" scroll-x :show-scrollbar="false">
            <view class="tab-list">
              <view
                v-for="tab in TABS"
                :key="tab.key"
                class="tab-item"
                :class="{ 'tab-item--active': activeTab === tab.key }"
                :style="{ '--tab-color': CATEGORY_COLORS[tab.key] ?? '#377DFF' }"
                @click="activeTab = tab.key"
              >
                <text class="tab-label">{{ tab.label }}</text>
                <text class="tab-count">({{ tabCount(tab.key) }})</text>
              </view>
            </view>
          </scroll-view>
          <view class="tab-divider" />
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
            <!-- 图标区 -->
            <view
              class="bc-icon-wrap"
              :class="badge.unlocked ? `bc-color--${badge.color}` : 'bc-color--locked'"
            >
              <Icon :name="badge.icon" :size="26" class="bc-icon" :stroke-width="1.5" />
              <!-- 已解锁：对勾角标 -->
              <view v-if="badge.unlocked" class="bc-badge bc-badge--unlocked">
                <Icon name="check" :size="9" />
              </view>
              <!-- 未解锁：锁角标 -->
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

            <!-- 首页展示标签（正常流，不遮内容） -->
            <view class="bc-pinned-row">
              <view v-if="badge.unlocked && isPinned(badge.id)" class="bc-pinned-tag">
                <text class="bc-pinned-text">首页展示</text>
              </view>
            </view>
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
        <!-- 拖动条（移动端） -->
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
          <view v-if="detailBadge?.unlocked" class="detail-unlocked-tag">
            <Icon name="check-circle" :size="13" class="detail-unlocked-icon" />
            <text class="detail-unlocked-text">已解锁</text>
          </view>
          <view v-else class="detail-locked-tag">
            <Icon name="lock" :size="12" class="detail-locked-icon" />
            <text class="detail-locked-text">未解锁</text>
          </view>
        </view>

        <!-- 分割线 -->
        <view class="detail-divider" />

        <!-- 信息区 -->
        <view class="detail-info">
          <!-- 解锁条件 -->
          <view class="detail-row">
            <view class="detail-row-label">
              <Icon name="target" :size="14" class="detail-row-icon detail-row-icon--blue" />
              <text class="detail-row-key">解锁条件</text>
            </view>
            <text class="detail-row-val">{{ detailBadge?.condition }}</text>
          </view>

          <!-- 获得时间（已解锁时显示） -->
          <view v-if="detailBadge?.unlocked && detailBadge?.unlockedAt" class="detail-row">
            <view class="detail-row-label">
              <Icon name="calendar" :size="14" class="detail-row-icon detail-row-icon--teal" />
              <text class="detail-row-key">获得时间</text>
            </view>
            <text class="detail-row-val">{{ detailBadge.unlockedAt }}</text>
          </view>

          <!-- 积分奖励 -->
          <view class="detail-row">
            <view class="detail-row-label">
              <Icon name="zap" :size="14" class="detail-row-icon detail-row-icon--amber" />
              <text class="detail-row-key">积分奖励</text>
            </view>
            <text class="detail-row-val detail-row-val--highlight">+{{ detailBadge?.reward }} 积分</text>
          </view>

          <!-- 徽章介绍 -->
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
              <Icon
                :name="detailBadge && isPinned(detailBadge.id) ? 'star' : 'star'"
                :size="15"
                class="detail-btn-icon"
              />
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
import { ref, computed, nextTick } from 'vue'
import CNavBar from '@/components/layout/CNavBar.vue'
import Icon from '@/components/icons/index.vue'

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
  description: string    // 简短描述（卡片用）
  condition: string      // 详细解锁条件（弹窗用）
  reward: number         // 积分奖励
}

/* ──────────────────────────────────────────
   徽章数据（完整定义，后续可替换为 API 数据）
────────────────────────────────────────── */
const allBadges: Badge[] = [
  // ─── 成长类 ───
  { id: 1,  name: '新人报到',  icon: 'star',          color: 'amber',  category: 'growth',       unlocked: true,  unlockedAt: '2024-09-01', description: '完成首次登录，踏入校园社区',       condition: '完成首次登录注册账号',             reward: 20 },
  { id: 5,  name: '签到达人',  icon: 'calendar-check', color: 'blue',   category: 'growth',       unlocked: false, description: '坚持签到，养成学习好习惯',         condition: '连续签到 7 天',                    reward: 30 },
  { id: 6,  name: '学习标兵',  icon: 'book-open',     color: 'teal',   category: 'growth',       unlocked: false, description: '高频签到，学习态度满分',           condition: '连续签到 30 天',                   reward: 50 },
  { id: 7,  name: '等级达人',  icon: 'trending-up',   color: 'violet', category: 'growth',       unlocked: false, description: '积分快速成长，等级大幅提升',       condition: '账号等级达到 Lv.5',                reward: 30 },
  // ─── 贡献类 ───
  { id: 2,  name: '资源贡献',  icon: 'file-text',     color: 'blue',   category: 'contribution', unlocked: false, description: '分享知识，让更多人受益',           condition: '上传并通过审核 5 个资源',           reward: 30 },
  { id: 8,  name: '优质内容',  icon: 'award',         color: 'amber',  category: 'contribution', unlocked: false, description: '内容质量过硬，广受好评',           condition: '上传的资源累计获得 10 次收藏',      reward: 40 },
  { id: 9,  name: '资源达人',  icon: 'layers',        color: 'violet', category: 'contribution', unlocked: false, description: '持续输出，成为社区知识库',         condition: '累计上传 20 个优质资源',            reward: 80 },
  // ─── 互动类 ───
  { id: 3,  name: '热心助人',  icon: 'heart',         color: 'rose',   category: 'interaction',  unlocked: false, description: '积极回答，传递知识的温度',         condition: '累计回答 10 个问题',               reward: 30 },
  { id: 4,  name: '人气王',    icon: 'users',         color: 'blue',   category: 'interaction',  unlocked: false, description: '内容深受欢迎，人气爆棚',           condition: '累计获得 50 个点赞',               reward: 50 },
  { id: 10, name: '获赞达人',  icon: 'thumbs-up',     color: 'teal',   category: 'interaction',  unlocked: false, description: '高质量内容持续输出，获赞无数',     condition: '累计获得 200 个点赞',              reward: 100 },
  { id: 11, name: '收藏达人',  icon: 'bookmark',      color: 'violet', category: 'interaction',  unlocked: false, description: '广泛收集精华内容，学习资料丰富',   condition: '累计收藏 50 个内容',               reward: 30 },
  // ─── 限定类 ───
  { id: 12, name: '开学季',    icon: 'graduation-cap', color: 'teal',  category: 'limited',      unlocked: false, description: '2024 年开学季限定徽章',           condition: '参与 2024 年开学季活动',           reward: 50 },
  { id: 13, name: '新年快乐',  icon: 'gift',           color: 'rose',  category: 'limited',      unlocked: false, description: '新年限定徽章，满满的祝福',         condition: '参与新年活动并完成签到',             reward: 50 },
]

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

/** 各分类主题色：Tab 选中/hover + 「去解锁」文字 */
const CATEGORY_COLORS: Record<string, string> = {
  all:          '#377DFF',  // 主蓝（全部 Tab）
  growth:       '#377DFF',  // 蓝 — 成长类
  contribution: '#10B981',  // 绿 — 贡献类
  interaction:  '#A78BFA',  // 紫 — 互动类
  limited:      '#F97316',  // 橙 — 限定类
}

/** 已解锁卡片浅色背景（按徽章色系） */
const BADGE_BG_MAP: Record<string, string> = {
  blue:   '#EFF6FF',
  teal:   '#ECFDF5',
  violet: '#F5F3FF',
  amber:  '#FFFBEB',
  rose:   '#FFF1F2',
}

/** 已解锁卡片彩色描边 */
const BADGE_BORDER_MAP: Record<string, string> = {
  blue:   'rgba(59,130,246,0.25)',
  teal:   'rgba(16,185,129,0.25)',
  violet: 'rgba(139,92,246,0.25)',
  amber:  'rgba(245,158,11,0.22)',
  rose:   'rgba(244,63,94,0.22)',
}

/** 根据徽章分类返回对应主题色 */
const badgeCategoryColor = (category: string): string =>
  CATEGORY_COLORS[category] ?? CATEGORY_COLORS.all

const ACHIEVEMENT_TITLES = [
  { min: 0,  title: '探索者',    next: '初学者',   nextReward: 20  },
  { min: 1,  title: '初学者',    next: '进阶者',   nextReward: 30  },
  { min: 3,  title: '进阶者',    next: '知识达人', nextReward: 50  },
  { min: 6,  title: '知识达人',  next: '贡献之星', nextReward: 80  },
  { min: 9,  title: '贡献之星',  next: '徽章达人', nextReward: 100 },
  { min: 11, title: '徽章达人',  next: '',         nextReward: 0   },
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

// 已固定到首页的徽章 ID 列表
const loadPinned = (): number[] => {
  try {
    const raw = uni.getStorageSync(BADGE_PINNED_KEY)
    return raw ? JSON.parse(raw) : []
  } catch {
    return []
  }
}
const pinnedIds = ref<number[]>(loadPinned())

// 弹窗状态（detail）
const showDetail = ref(false)
const detailUp   = ref(false)
const detailBadge = ref<Badge | null>(null)

// 弹窗状态（rule）
const showRule = ref(false)
const ruleUp   = ref(false)

/* ──────────────────────────────────────────
   计算属性
────────────────────────────────────────── */
const unlockedCount = computed(() => allBadges.filter(b => b.unlocked).length)
const remainCount   = computed(() => allBadges.length - unlockedCount.value)
const progressPercent = computed(() =>
  Math.round((unlockedCount.value / allBadges.length) * 100)
)
const totalRewardPoints = computed(() =>
  allBadges.filter(b => b.unlocked).reduce((sum, b) => sum + b.reward, 0)
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

// 排序：已解锁（最新解锁在前）→ 未解锁
const sortedBadges = computed(() => {
  const unlocked = allBadges
    .filter(b => b.unlocked)
    .sort((a, b) => {
      if (a.unlockedAt && b.unlockedAt) return b.unlockedAt.localeCompare(a.unlockedAt)
      if (a.unlockedAt) return -1
      if (b.unlockedAt) return 1
      return 0
    })
  const locked = allBadges.filter(b => !b.unlocked)
  return [...unlocked, ...locked]
})

const filteredBadges = computed(() => {
  const base = sortedBadges.value
  if (activeTab.value === 'all') return base
  return base.filter(b => b.category === activeTab.value)
})

const tabCount = (key: string) => {
  if (key === 'all') return allBadges.length
  return allBadges.filter(b => b.category === key).length
}

/* ──────────────────────────────────────────
   方法：首页固定
────────────────────────────────────────── */
const isPinned = (id: number) => pinnedIds.value.includes(id)

const savePinned = () =>
  uni.setStorageSync(BADGE_PINNED_KEY, JSON.stringify(pinnedIds.value))

const togglePin = () => {
  if (!detailBadge.value) return
  const id = detailBadge.value.id
  const idx = pinnedIds.value.indexOf(id)
  if (idx >= 0) {
    // 取消固定
    pinnedIds.value = pinnedIds.value.filter(i => i !== id)
    savePinned()
    uni.showToast({ title: '已取消首页展示', icon: 'none' })
  } else {
    // 固定
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
   方法：弹窗控制
────────────────────────────────────────── */
const openDetailModal = (badge: Badge) => {
  detailBadge.value = badge
  showDetail.value = true
  nextTick(() => { detailUp.value = true })
}
const closeDetailModal = () => {
  detailUp.value = false
  setTimeout(() => {
    showDetail.value = false
    detailBadge.value = null
  }, 300)
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
   方法：导航
────────────────────────────────────────── */
const CATEGORY_URL: Record<string, string> = {
  growth:       '/pages/user/index',
  contribution: '/pages/resource/publish',
  interaction:  '/pages/question/index',
  limited:      '',
}

const goToTask = (category: string) => {
  const url = CATEGORY_URL[category]
  if (!url) {
    uni.showToast({ title: '关注平台活动即可获得', icon: 'none' })
    return
  }
  uni.navigateTo({
    url,
    fail: () => uni.showToast({ title: '页面开发中...', icon: 'none' }),
  })
}

const handleGoTask = () => {
  if (!detailBadge.value) return
  const category = detailBadge.value.category
  closeDetailModal()
  setTimeout(() => goToTask(category), 320)
}
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

/* ══════════════════════════════════════════
   页面骨架
══════════════════════════════════════════ */
.badges-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: $color-bg-page;
}

.page-scroll {
  flex: 1;
  height: 0;
}

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

  @media (min-width: 1024px) { height: 32px; }
}

/* ══════════════════════════════════════════
   导航栏右侧按钮
══════════════════════════════════════════ */
.nav-rule-btn {
  padding: 8rpx 16rpx;
  border-radius: 12rpx;
  cursor: pointer;
  transition: background $transition-fast $transition-ease-in-out;

  &:active { background: rgba(255, 255, 255, 0.2); }

  // #ifdef H5
  &:hover { background: rgba(255, 255, 255, 0.2); }
  // #endif
}

.nav-rule-text {
  font-size: 26rpx;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);

  @media (min-width: 1024px) { font-size: 13px; }
}

/* ══════════════════════════════════════════
   成就概览卡片
══════════════════════════════════════════ */
.overview-card {
  background: $color-bg-card;
  border-radius: 24rpx;
  border: 1rpx solid $color-border-light;
  box-shadow: $shadow-base;
  padding: 32rpx 28rpx;
  display: flex;
  align-items: center;
  gap: 24rpx;

  @media (min-width: 1024px) {
    border-radius: 16px;
    padding: 24px 28px;
    gap: 40px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.07);
  }
}

.ov-left {
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  gap: 10rpx;

  @media (min-width: 1024px) { gap: 8px; }
}

.ov-count-row {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
}

.ov-count {
  font-size: 56rpx;
  font-weight: 800;
  color: #F97316;          // 橙色：与积分/进度条统一
  line-height: 1;
  letter-spacing: -0.02em;

  @media (min-width: 1024px) { font-size: 36px; }
}

.ov-count-total {
  font-size: 22rpx;
  color: $color-text-tertiary;
  font-weight: 400;

  @media (min-width: 1024px) { font-size: 13px; }
}

.ov-title-row {
  display: flex;
  align-items: center;
}

.ov-badge-tag {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  background: $campus-blue;    // 实心蓝：仪式感更强
  border-radius: 100rpx;
  padding: 6rpx 16rpx 6rpx 10rpx;
  box-shadow: 0 2rpx 8rpx rgba($campus-blue, 0.30);

  @media (min-width: 1024px) {
    gap: 4px;
    padding: 4px 12px 4px 8px;
    border-radius: 100px;
    box-shadow: 0 2px 6px rgba(55, 125, 255, 0.28);
  }
}

.ov-badge-icon {
  color: rgba(255, 255, 255, 0.85);  // 白色图标
}

.ov-badge-label {
  font-size: 20rpx;
  font-weight: 600;
  color: #fff;               // 白色文字

  @media (min-width: 1024px) { font-size: 12px; }
}

.ov-reward-row {
  display: flex;
  align-items: center;
  gap: 6rpx;
}

.ov-reward-icon {
  color: #f59e0b;
  flex-shrink: 0;
}

.ov-reward-text {
  font-size: 19rpx;
  color: $color-text-tertiary;

  @media (min-width: 1024px) { font-size: 11px; }
}

.ov-right {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 10rpx;

  @media (min-width: 1024px) { gap: 8px; }
}

.ov-progress-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.ov-progress-label {
  font-size: 21rpx;
  color: $color-text-tertiary;
  font-weight: 500;

  @media (min-width: 1024px) { font-size: 12px; }
}

.ov-progress-pct {
  font-size: 24rpx;
  font-weight: 700;
  color: #F97316;

  @media (min-width: 1024px) { font-size: 14px; }
}

.ov-progress-track {
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

.ov-progress-fill {
  height: 100%;
  /* 橙色渐变：与积分体系视觉统一 */
  background: linear-gradient(90deg, #F97316 0%, #FFB766 100%);
  border-radius: 100rpx;
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.ov-hint-row {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 0;
}

.ov-progress-hint {
  font-size: 19rpx;
  color: $color-text-tertiary;
  line-height: 1.4;

  &--maxed {
    color: #f59e0b;
    font-weight: 500;
  }

  @media (min-width: 1024px) { font-size: 11px; }
}

.ov-reward-highlight {
  font-size: 19rpx;
  font-weight: 700;
  color: #F97316;            // 橙色高亮：积分奖励一眼可见

  @media (min-width: 1024px) { font-size: 11px; }
}

/* ══════════════════════════════════════════
   分类 Tab 栏
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
  gap: 4rpx;

  @media (min-width: 1024px) {
    padding: 0;
    gap: 4px;
  }
}

.tab-item {
  --tab-color: #377DFF; // fallback：覆盖 JS 未注入时
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  padding: 18rpx 24rpx 16rpx;
  cursor: pointer;
  position: relative;
  transition: color $transition-fast $transition-ease-in-out;

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 24rpx;
    right: 24rpx;
    height: 3rpx;
    background: var(--tab-color);   // 随分类变色
    border-radius: 100rpx;
    transform: scaleX(0);
    transition: transform 0.2s $transition-ease-in-out;
  }

  &--active {
    &::after { transform: scaleX(1); }

    .tab-label { color: var(--tab-color); font-weight: 600; }
    .tab-count { color: var(--tab-color); opacity: 0.75; }
  }

  // #ifdef H5
  &:not(.tab-item--active):hover {
    .tab-label { color: var(--tab-color); }
  }
  // #endif

  @media (min-width: 1024px) {
    padding: 12px 20px 10px;

    &::after {
      left: 20px;
      right: 20px;
      height: 2px;
      border-radius: 100px;
    }
  }
}

.tab-label {
  font-size: 26rpx;
  color: $color-text-tertiary;
  font-weight: 400;
  transition: color $transition-fast $transition-ease-in-out;

  @media (min-width: 1024px) { font-size: 14px; }
}

.tab-count {
  font-size: 20rpx;
  color: $color-text-placeholder;
  transition: color $transition-fast $transition-ease-in-out;

  @media (min-width: 1024px) { font-size: 12px; }
}

.tab-divider {
  height: 1rpx;
  background: $color-divider;
  margin: 0 24rpx;

  @media (min-width: 1024px) { margin: 0; height: 1px; }
}

/* ══════════════════════════════════════════
   徽章网格
══════════════════════════════════════════ */
.badge-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;

  @media (min-width: 640px) {
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
  }

  @media (min-width: 1024px) {
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
  }
}

/* ─── 单张徽章卡片 ─── */
.badge-card {
  --bc-bg:     #{$color-bg-card};
  --bc-border: #{$color-border-light};
  background: var(--bc-bg);
  border-radius: 20rpx;
  border: 1.5rpx solid var(--bc-border);
  box-shadow: $shadow-xs;
  padding: 28rpx 16rpx 22rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  cursor: pointer;
  position: relative;
  transition: box-shadow 0.2s $transition-ease-in-out, transform 0.2s $transition-ease-out;

  &:active {
    transform: translateY(2rpx);
    box-shadow: $shadow-xs;
  }

  // #ifdef H5
  &:hover {
    box-shadow: 0 8px 28px rgba(0, 0, 0, 0.10), 0 2px 8px rgba(0, 0, 0, 0.05),
                0 0 0 2px rgba(55, 125, 255, 0.18);
    transform: translateY(-3px);
  }
  // #endif

  /* 未解锁：灰度 + 弱化阴影，hover 保持静止 */
  &--locked {
    background: #F9F9F9;
    opacity: 0.62;
    filter: grayscale(55%);

    &:active { transform: none; }

    // #ifdef H5
    &:hover {
      /* 灰度降低，提示卡片可交互 */
      opacity: 0.82;
      filter: grayscale(20%);
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.07);
      transform: none;

      .bc-hint {
        color: $campus-blue-dark;
        text-decoration: underline;
        text-underline-offset: 2px;
      }
    }
    // #endif
  }

  @media (min-width: 1024px) {
    border-radius: 14px;
    padding: 24px 16px 20px;
    gap: 10px;

    &:hover {
      transform: translateY(-3px);
    }
  }
}

/* ─── 图标容器 ─── */
.bc-icon-wrap {
  width: 76rpx;
  height: 76rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  flex-shrink: 0;

  @media (min-width: 1024px) {
    width: 56px;
    height: 56px;
    border-radius: 14px;
  }
}

/* 图标颜色系统（已解锁：更饱和的背景 + 更鲜明的前景色） */
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
  width: 28rpx;
  height: 28rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid $color-bg-card;

  &--unlocked {
    background: $color-success;
    color: #fff;
  }

  &--locked {
    background: $color-text-quaternary;
    color: #fff;
  }

  @media (min-width: 1024px) {
    width: 20px;
    height: 20px;
    top: -5px;
    right: -5px;
    border-width: 2px;
  }
}

.bc-name {
  font-size: 24rpx;
  font-weight: 600;
  color: $color-text-primary;
  text-align: center;
  line-height: 1.3;

  @media (min-width: 1024px) { font-size: 13px; }
}

.bc-date {
  font-size: 19rpx;
  color: $color-text-tertiary;
  text-align: center;

  @media (min-width: 1024px) { font-size: 11px; }
}

.bc-hint {
  font-size: 19rpx;
  color: $campus-blue;
  font-weight: 500;

  @media (min-width: 1024px) { font-size: 11px; }
}

/* 首页展示行 — 占位容器保持网格高度一致 */
.bc-pinned-row {
  height: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;

  @media (min-width: 1024px) { height: 22px; }
}

.bc-pinned-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.bc-pinned-text {
  font-size: 17rpx;
  color: $campus-blue;
  background: $campus-blue-lighter;
  border-radius: 100rpx;
  padding: 3rpx 14rpx;
  font-weight: 500;

  @media (min-width: 1024px) {
    font-size: 10px;
    padding: 2px 10px;
    border-radius: 100px;
  }
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
  transition: background $transition-fast $transition-ease-in-out;

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

  @media (min-width: 1024px) {
    align-items: center;
  }
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

  &--up {
    transform: translateY(0);
  }

  @media (min-width: 1024px) {
    width: 480px;
    max-width: 90vw;
    border-radius: 20px;
    box-shadow: $shadow-xl;
    transform: scale(0.94);
    opacity: 0;
    transition: transform 0.22s cubic-bezier(0.34, 1.56, 0.64, 1), opacity 0.18s ease;

    &--up {
      transform: scale(1);
      opacity: 1;
    }
  }
}

/* 拖动条 */
.modal-bar {
  width: 48rpx;
  height: 6rpx;
  background: $color-border;
  border-radius: 100rpx;
  margin: 16rpx auto 0;

  @media (min-width: 1024px) { display: none; }
}

/* 底部安全区 */
.modal-bottom-safe {
  height: 72rpx;

  @media (min-width: 1024px) { height: 8px; }
}

/* ══════════════════════════════════════════
   徽章详情弹窗内容
══════════════════════════════════════════ */
.detail-card {
  @media (min-width: 1024px) { padding-bottom: 0; }
}

.detail-hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 28rpx 32rpx 24rpx;
  gap: 14rpx;

  @media (min-width: 1024px) {
    padding: 28px 32px 20px;
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

.detail-icon {
  // color inherited from parent .bc-color--* class
}

.detail-name {
  font-size: 36rpx;
  font-weight: 700;
  color: $color-text-primary;
  text-align: center;

  @media (min-width: 1024px) { font-size: 20px; }
}

.detail-unlocked-tag,
.detail-locked-tag {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  border-radius: 100rpx;
  padding: 6rpx 18rpx 6rpx 12rpx;

  @media (min-width: 1024px) {
    gap: 4px;
    padding: 4px 14px 4px 10px;
    border-radius: 100px;
  }
}

.detail-unlocked-tag {
  background: $color-success-bg;
}

.detail-unlocked-icon {
  color: $color-success;
}

.detail-unlocked-text {
  font-size: 22rpx;
  font-weight: 600;
  color: $color-success;

  @media (min-width: 1024px) { font-size: 13px; }
}

.detail-locked-tag {
  background: $color-bg-hover;
}

.detail-locked-icon {
  color: $color-text-tertiary;
}

.detail-locked-text {
  font-size: 22rpx;
  font-weight: 500;
  color: $color-text-tertiary;

  @media (min-width: 1024px) { font-size: 13px; }
}

.detail-divider {
  height: 1rpx;
  background: $color-divider;
  margin: 0 32rpx;

  @media (min-width: 1024px) { height: 1px; margin: 0 32px; }
}

.detail-info {
  padding: 24rpx 32rpx;
  display: flex;
  flex-direction: column;
  gap: 18rpx;

  @media (min-width: 1024px) {
    padding: 20px 32px;
    gap: 14px;
  }
}

.detail-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12rpx;

  @media (min-width: 1024px) { gap: 8px; }
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

  &--blue   { color: $campus-blue; }
  &--teal   { color: #0D9488; }
  &--amber  { color: #B45309; }
}

.detail-row-key {
  font-size: 24rpx;
  font-weight: 500;
  color: $color-text-tertiary;
  white-space: nowrap;

  @media (min-width: 1024px) { font-size: 13px; }
}

.detail-row-val {
  font-size: 24rpx;
  color: $color-text-primary;
  text-align: right;
  line-height: 1.4;

  &--highlight {
    color: #f59e0b;
    font-weight: 600;
  }

  @media (min-width: 1024px) { font-size: 13px; }
}

.detail-desc-wrap {
  background: $color-bg-page;
  border-radius: 12rpx;
  padding: 18rpx 20rpx;

  @media (min-width: 1024px) {
    border-radius: 8px;
    padding: 12px 16px;
  }
}

.detail-desc {
  font-size: 24rpx;
  color: $color-text-secondary;
  line-height: 1.6;

  @media (min-width: 1024px) { font-size: 13px; }
}

.detail-actions {
  display: flex;
  gap: 16rpx;
  padding: 0 32rpx 8rpx;

  @media (min-width: 1024px) {
    gap: 12px;
    padding: 4px 32px 8px;
  }
}

/* ─── 操作按钮 ─── */
.detail-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  border-radius: 100rpx;
  padding: 22rpx 0;
  cursor: pointer;
  transition: opacity 0.15s ease, transform 0.1s ease;

  &:active {
    opacity: 0.85;
    transform: scale(0.98);
  }

  // #ifdef H5
  &:hover { opacity: 0.9; }
  // #endif

  &--primary {
    background: $campus-blue;
    .detail-btn-icon { color: #fff; }
    .detail-btn-text { color: #fff; }

    // #ifdef H5
    &:hover { background: $campus-blue-light; opacity: 1; }
    // #endif
  }

  &--secondary {
    background: $campus-blue-lighter;
    .detail-btn-icon { color: $campus-blue; }
    .detail-btn-text { color: $campus-blue; }
  }

  &--pinned {
    background: $color-warning-bg;
    .detail-btn-icon { color: $color-warning; }
    .detail-btn-text { color: $color-warning; }
  }

  &--ghost {
    background: $color-bg-hover;
    .detail-btn-icon { color: $color-text-tertiary; }
    .detail-btn-text { color: $color-text-tertiary; }
  }

  @media (min-width: 1024px) {
    border-radius: 100px;
    padding: 12px 0;
    gap: 6px;
  }
}

.detail-btn-icon {
  flex-shrink: 0;
}

.detail-btn-text {
  font-size: 26rpx;
  font-weight: 500;

  @media (min-width: 1024px) { font-size: 14px; }
}

/* ══════════════════════════════════════════
   规则弹窗内容
══════════════════════════════════════════ */
.rule-header {
  padding: 24rpx 32rpx 16rpx;

  @media (min-width: 1024px) { padding: 20px 32px 12px; }
}

.rule-title {
  font-size: 32rpx;
  font-weight: 700;
  color: $color-text-primary;

  @media (min-width: 1024px) { font-size: 18px; }
}

.rule-scroll {
  max-height: 60vh;
  overflow: hidden;

  @media (min-width: 1024px) { max-height: 400px; }
}

.rule-body {
  padding: 0 32rpx;
  display: flex;
  flex-direction: column;
  gap: 24rpx;

  @media (min-width: 1024px) {
    padding: 0 32px;
    gap: 20px;
  }
}

.rule-section {
  display: flex;
  flex-direction: column;
  gap: 10rpx;

  @media (min-width: 1024px) { gap: 8px; }
}

.rule-section-header {
  display: flex;
  align-items: center;
  gap: 10rpx;

  @media (min-width: 1024px) { gap: 8px; }
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
  padding-left: 28rpx;
  display: flex;
  flex-direction: column;
  gap: 6rpx;

  @media (min-width: 1024px) {
    padding-left: 22px;
    gap: 4px;
  }
}

.rule-line {
  font-size: 24rpx;
  color: $color-text-secondary;
  line-height: 1.6;

  @media (min-width: 1024px) { font-size: 13px; }
}

.rule-footer {
  padding: 20rpx 32rpx 8rpx;
  display: flex;

  .detail-btn {
    padding: 22rpx 0;
  }

  @media (min-width: 1024px) {
    padding: 16px 32px 4px;

    .detail-btn { padding: 12px 0; }
  }
}
</style>
