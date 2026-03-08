<template>
  <view class="recommend-sidebar">
    <!-- 社区统计模块 -->
    <view class="sidebar-card stats-card">
      <view class="card-header">
        <Icon name="bar-chart-2" :size="16" class="header-icon" />
        <text class="header-title">社区统计</text>
      </view>
      <view class="stats-grid">
        <view class="stat-item">
          <text class="stat-num">{{ communityStats.totalQuestions }}</text>
          <text class="stat-label">总问题数</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ communityStats.totalAnswers }}</text>
          <text class="stat-label">回答总数</text>
        </view>
        <view class="stat-item stat-item--highlight">
          <text class="stat-num stat-num--highlight">{{ communityStats.solveRate }}</text>
          <text class="stat-label stat-label--highlight">解决率</text>
        </view>
      </view>
    </view>

    <!-- 精选推荐轮播模块 -->
    <FeaturedCarousel
      v-if="featuredQuestions.length > 0 && !isFeaturedDismissed"
      :questions="featuredQuestions"
      :autoplay="true"
      :interval="5000"
      @click="handleFeaturedClick"
      @refresh="handleFeaturedRefresh"
    />

    <!-- 活跃答主模块 -->
    <CCard variant="default" class="sidebar-card">
      <view class="card-header">
        <Icon name="users" :size="18" class="header-icon" />
        <text class="header-title">活跃答主</text>
        <text class="view-more" @click="handleViewMoreUsers">查看更多 ></text>
      </view>
      <view v-if="activeUsers.length > 0" class="active-users">
        <view
          v-for="(user, index) in activeUsers"
          :key="user.userId"
          class="user-item"
          @click="handleUserClick(user.userId)"
        >
          <text class="rank-num" :class="getRankNumClass(index)">{{ index + 1 }}</text>
          <view class="avatar-wrap">
            <view class="avatar-placeholder" :style="getAvatarBg(user.nickname)">
              <text class="avatar-char">{{ user.nickname?.charAt(0)?.toUpperCase() || '?' }}</text>
            </view>
            <image v-if="user.avatar" :src="user.avatar" class="user-avatar" mode="aspectFill" />
          </view>
          <view class="user-info">
            <text class="user-name">{{ user.nickname }}</text>
            <view class="stats-line">
              <Icon name="message-circle" :size="11" class="stat-icon" />
              <text class="user-answers">{{ user.answerCount }} 回答</text>
            </view>
          </view>
          <view v-if="user.badge" class="user-badge" :class="getBadgeClass(user.badge)">
            <text class="badge-text">{{ user.badge }}</text>
          </view>
        </view>
      </view>
      <view v-else class="empty-state">
        <Icon name="users" :size="32" class="empty-icon" />
        <text class="empty-text">去回答问题，成为活跃答主</text>
        <view class="empty-action" @click="handleGoAnswer">
          <Icon name="message-circle" :size="13" />
          <text>去回答</text>
        </view>
      </view>
    </CCard>

    <!-- 热门标签模块 -->
    <CCard variant="default" class="sidebar-card">
      <TagCloud
        :tags="hotTags"
        title="热门标签"
        header-icon="tag"
        :show-header="true"
        :show-badge="false"
        :show-count="true"
        :dynamic-size="false"
        :collapsible="true"
        :max-display="6"
        :empty-text="hotTagsEmptyText"
        @tag-click="handleTagCloudClick"
      />
      <view v-if="hotTags.length === 0" class="tag-empty-action" @click="handleGoAsk">
        <Icon name="edit-3" :size="13" />
        <text>去提问并添加标签</text>
      </view>
    </CCard>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import Icon from '@/components/icons/index.vue'
import { CCard } from '@/components/ui'
import FeaturedCarousel from '@/components/FeaturedCarousel.vue'
import TagCloud from '@/components/TagCloud.vue'
import type { TagItem } from '@/components/TagCloud.vue'
import { getQuestionList, getHotTags, getActiveUsers, getFeaturedQuestions } from '@/services/question'
import { getTotalStats } from '@/services/stats'

interface ActiveUser {
  userId: number
  nickname: string
  avatar: string
  answerCount: number
  badge: string | null
}

interface FeaturedQuestionData {
  qid: number
  title: string
  username: string
  avatar: string
  category: string
  answerCount: number
  views: number
  likes: number
  createdAt?: string  // 可选字段
}

// 社区统计
const communityStats = ref({
  totalQuestions: '--',
  totalAnswers: '--',
  solveRate: '--'
})

// 精选问题列表（真实 API 数据）
const featuredQuestions = ref<FeaturedQuestionData[]>([])
const isFeaturedDismissed = ref(false)

// 热门标签
const hotTags = ref<TagItem[]>([])

// 活跃答主
const activeUsers = ref<ActiveUser[]>([])

// 热门标签空状态提示文案
const hotTagsEmptyText = '暂无热门标签，提问时记得添加标签哦'


// 排名徽章样式已移至 HotQuestions 组件内部

// 头像背景色（基于昵称首字符 hash）
const AVATAR_COLORS = ['#1677FF', '#52C41A', '#FF6B35', '#722ED1', '#EB2F96', '#13C2C2', '#FA8C16']
const getAvatarBg = (nickname: string) => {
  const idx = nickname ? nickname.charCodeAt(0) % AVATAR_COLORS.length : 0
  return { background: AVATAR_COLORS[idx] }
}

// 排名数字样式
const getRankNumClass = (index: number) => {
  if (index === 0) return 'rank-1'
  if (index === 1) return 'rank-2'
  if (index === 2) return 'rank-3'
  return 'rank-other'
}

// 徽章样式类
const getBadgeClass = (badge: string) => {
  if (badge === '优质答主') return 'badge-premium'
  if (badge === '热心答主') return 'badge-active'
  if (badge === '活跃答主') return 'badge-regular'
  return ''
}

// Emits
const emit = defineEmits<{
  filterByTag: [tag: string]
}>()

// 点击标签 - 触发父组件筛选 (TagCloud组件回调)
const handleTagCloudClick = (tag: TagItem) => {
  emit('filterByTag', tag.name)
}

// 查看更多用户
const handleViewMoreUsers = () => {
  uni.showToast({ title: '查看更多用户功能开发中', icon: 'none' })
}

// 点击用户
const handleUserClick = (userId: number) => {
  uni.navigateTo({ url: `/pages/user/profile?id=${userId}` })
}

// 空状态引导：去回答
const handleGoAnswer = () => {
  uni.switchTab({ url: '/pages/question/index' })
}

// 空状态引导：去提问
const handleGoAsk = () => {
  uni.navigateTo({ url: '/pages/question/ask' })
}

// 格式化数字（超过 1000 显示 1.2k）
const formatNumber = (n: number): string => {
  if (n >= 10000) return (n / 10000).toFixed(1).replace(/\.0$/, '') + 'w'
  if (n >= 1000) return (n / 1000).toFixed(1).replace(/\.0$/, '') + 'k'
  return String(n)
}

// 加载社区统计（/stats/total 接口 + 解决率从问题列表派生）
const loadCommunityStats = async () => {
  try {
    const [statsRes, solvedRes] = await Promise.all([
      getTotalStats(),
      getQuestionList({ page: 1, pageSize: 1, isSolved: 1, sortBy: 'created_at', sortOrder: 'desc' })
    ])
    const total = statsRes.newQuestions ?? 0
    const totalAnswers = statsRes.totalAnswers ?? 0
    const solved = solvedRes.total ?? 0
    const rate = total > 0 ? Math.round((solved / total) * 100) : 0
    communityStats.value = {
      totalQuestions: formatNumber(total),
      totalAnswers: formatNumber(totalAnswers),
      solveRate: rate + '%'
    }
  } catch {
    // 加载失败保持 '--' 占位
  }
}

// 加载热门标签
const loadHotTags = async () => {
  try {
    const res = await getHotTags(8)
    hotTags.value = res
  } catch (error) {
    console.error('[RecommendSidebar] 加载热门标签失败:', error)
    // 失败时保持空数组，显示空状态
    hotTags.value = []
  }
}

// 加载活跃答主
const loadActiveUsers = async () => {
  try {
    const res = await getActiveUsers(4, '7d')
    activeUsers.value = res
  } catch (error) {
    console.error('[RecommendSidebar] 加载活跃答主失败:', error)
    // 失败时保持空数组，显示空状态
    activeUsers.value = []
  }
}

// 加载精选问题列表（真实 API，返回多条用于轮播）
const loadFeaturedQuestion = async () => {
  // 检查是否在 24 小时内关闭过
  const dismissedTime = uni.getStorageSync('featured_question_dismissed')
  if (dismissedTime) {
    const now = Date.now()
    const diff = now - dismissedTime
    const TWENTY_FOUR_HOURS = 24 * 60 * 60 * 1000
    if (diff < TWENTY_FOUR_HOURS) {
      isFeaturedDismissed.value = true
      return
    }
  }

  try {
    const res = await getFeaturedQuestions(5)  // 获取 5 条精选问题用于轮播
    featuredQuestions.value = res || []  // 如果返回 null/undefined，设为空数组
  } catch (error) {
    console.error('[RecommendSidebar] 加载精选问题失败:', error)
    // API 调用失败，不显示卡片
    featuredQuestions.value = []
  }
}

// 点击精选问题
const handleFeaturedClick = (qid: number) => {
  uni.navigateTo({ url: `/pages/question/detail?id=${qid}` })
}

// 刷新精选问题（换一换）
const handleFeaturedRefresh = () => {
  // 重新加载精选问题
  loadFeaturedQuestion()
}

// 组件挂载时加载数据
onMounted(() => {
  loadCommunityStats()
  loadFeaturedQuestion()
  loadHotTags()
  loadActiveUsers()
})
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.recommend-sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;  // 统一使用px，分组间距
  height: auto;  // 确保高度自动适应内容
  min-height: 0;  // 允许flex子元素缩小
}

// ===================================
// 分组样式（简化）
// ===================================
.sidebar-section {
  display: flex;
  flex-direction: column;
  gap: 12px;  // 卡片之间间距
}

.section-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 4px 8px;
  border-bottom: 2px solid $gray-200;  // 加深分隔线
}

.section-icon {
  color: $primary;
  flex-shrink: 0;
}

.section-title {
  font-size: 14px;  // 减小字号
  font-weight: $font-weight-semibold;  // 从bold降为semibold
  color: $gray-700;  // 从900降为700
  letter-spacing: 0;
  flex: 1;
}

// ===================================
// 卡片通用样式（统一规范）
// ===================================
.sidebar-card {
  padding: 20px;  // 增加内边距到20px，更宽松
  background: $white;
  border-radius: 12px;
  border: 1px solid $gray-100; // 更浅的边框
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); // 更明显的阴影
  transition: all 0.2s ease-out;

  &:hover {
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
    border-color: $gray-200;
    transform: translateY(-1px); // hover时轻微上浮
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid $gray-100;
}

.header-icon {
  color: $primary;
}

.header-title {
  font-size: 15px;
  font-weight: $font-weight-semibold;
  color: $gray-800;
  letter-spacing: 0;
  flex: 1;
}

.view-more {
  font-size: 12px;
  color: $primary;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;

  &:hover {
    color: darken($primary, 10%);
    transform: translateX(2px);
  }

  &:active {
    transform: scale(0.95);
  }
}

// ===================================
// 社区统计
// ===================================
.stats-card {
  .card-header {
    margin-bottom: 14px;
    padding-bottom: 0;
    border-bottom: none;
  }
}

.stats-grid {
  display: flex;
  gap: 10px;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 12px 8px;
  background: #F8FAFC;
  border-radius: 8px;

  &--highlight {
    background: #EFF6FF;
  }
}

.stat-num {
  font-size: 20px;
  font-weight: 700;
  color: $gray-900;
  line-height: 1;

  &--highlight {
    color: $primary;
  }
}

.stat-label {
  font-size: 12px;
  color: $gray-500;
  white-space: nowrap;

  &--highlight {
    color: $primary;
  }
}

// 热门问题样式已移至 HotQuestions 组件内部

// ===================================
// 活跃答主
// ===================================
.active-users {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.15s ease;

  &:hover {
    background: $gray-50;

    .user-name {
      color: $primary;
    }
  }

  &:active {
    background: $gray-100;
  }
}

// 排名序号
.rank-num {
  width: 18px;
  text-align: center;
  font-size: 13px;
  font-weight: 700;
  flex-shrink: 0;

  &.rank-1 { color: #F59E0B; }
  &.rank-2 { color: #9CA3AF; }
  &.rank-3 { color: #F97316; }
  &.rank-other { color: $gray-300; font-weight: 500; }
}

.avatar-wrap {
  position: relative;
  width: 36px;
  height: 36px;
  flex-shrink: 0;
}

.avatar-placeholder {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-char {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  line-height: 1;
}

.user-avatar {
  position: absolute;
  inset: 0;
  width: 36px;
  height: 36px;
  border-radius: 50%;
}

.user-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.user-name {
  font-size: 13px;
  font-weight: 500;
  color: $gray-800;
  @include text-ellipsis(1);
  transition: color 0.15s;
}

.stats-line {
  display: flex;
  align-items: center;
  gap: 3px;
}

.stat-icon {
  color: $gray-400;
}

.user-answers {
  font-size: 12px;
  color: $gray-500;
}

.user-badge {
  flex-shrink: 0;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 500;

  &.badge-premium {
    background: rgba(139, 92, 246, 0.1);
    color: #7C3AED;
  }

  &.badge-active {
    background: rgba(236, 72, 153, 0.1);
    color: #DB2777;
  }

  &.badge-regular {
    background: rgba(16, 185, 129, 0.1);
    color: #059669;
  }

  .badge-text {
    font-size: 11px;
  }
}

// ===================================
// 空状态
// ===================================
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px 16px;
  gap: 8px;
}

.empty-icon {
  color: $gray-300;
  opacity: 0.6;
}

.empty-text {
  font-size: 13px;
  color: $gray-500;
  text-align: center;
  line-height: 1.5;
}

.empty-action {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 4px;
  padding: 6px 16px;
  background: $primary;
  color: $white;
  font-size: 13px;
  font-weight: 500;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: darken($primary, 8%);
    transform: translateY(-1px);
    box-shadow: 0 3px 8px rgba($primary, 0.3);
  }

  &:active {
    transform: scale(0.96);
  }
}

.tag-empty-action {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  padding: 5px 14px;
  background: rgba($primary, 0.08);
  color: $primary;
  font-size: 12px;
  font-weight: 500;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: rgba($primary, 0.15);
  }
}

</style>
