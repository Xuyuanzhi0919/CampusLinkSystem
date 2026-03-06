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

    <!-- 快捷操作模块 -->
    <CCard variant="default" class="sidebar-card quick-actions-card">
      <view class="card-header">
        <Icon name="zap" :size="18" class="header-icon" />
        <text class="header-title">快捷操作</text>
      </view>
      <view class="quick-actions">
        <view class="action-item primary-action" @click="handleUploadResource">
          <Icon name="file-text" :size="20" class="action-icon" />
          <text class="action-text">上传资源</text>
        </view>
        <view class="action-item secondary-action" @click="handleMyTasks">
          <Icon name="check-circle" :size="18" class="action-icon" />
          <text class="action-text">我的任务</text>
        </view>
        <view class="action-item secondary-action" @click="handleMyCollections">
          <Icon name="bookmark" :size="18" class="action-icon" />
          <text class="action-text">我的收藏</text>
        </view>
        <view class="action-item secondary-action" @click="handleMyPoints">
          <Icon name="gift" :size="18" class="action-icon" />
          <text class="action-text">我的积分</text>
        </view>
      </view>
    </CCard>

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
          :class="getUserRankClass(index)"
          @click="handleUserClick(user.userId)"
        >
          <view class="avatar-wrapper">
            <image :src="user.avatar" class="user-avatar" mode="aspectFill" />
            <view v-if="index < 3" class="rank-crown" :class="`crown-${index + 1}`">
              <Icon :name="getCrownIcon(index)" :size="12" />
            </view>
          </view>
          <view class="user-info">
            <view class="name-line">
              <text class="user-name">{{ user.nickname }}</text>
              <view v-if="index === 0" class="top-badge">TOP1</view>
            </view>
            <view class="stats-line">
              <Icon name="message-circle" :size="11" class="stat-icon" />
              <text class="user-answers">{{ user.answerCount }} 回答</text>
            </view>
          </view>
          <view v-if="user.badge" class="user-badge" :class="getBadgeClass(user.badge)">
            <Icon :name="getBadgeIcon(user.badge)" :size="11" />
            <text class="badge-text">{{ user.badge }}</text>
          </view>
        </view>
      </view>
      <view v-else class="empty-state">
        <Icon name="users" :size="32" class="empty-icon" />
        <text class="empty-text">暂无活跃答主</text>
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
        empty-text="暂无热门标签"
        @tag-click="handleTagCloudClick"
      />
    </CCard>

    <!-- 热门问题模块 -->
    <CCard variant="default" class="sidebar-card">
      <HotQuestions
        :questions="hotQuestions"
        title="热门问题"
        header-icon="trending-up"
        :show-header="true"
        :show-badge="false"
        :show-view-more="false"
        :show-bounty="false"
        :max-display="5"
        empty-text="暂无热门问题"
        @question-click="handleHotQuestionClick"
      />
    </CCard>

    <!-- 热门搜索模块 -->
    <CCard variant="default" class="sidebar-card">
      <view class="card-header">
        <Icon name="trending-up" :size="18" class="header-icon" />
        <text class="header-title">热门搜索</text>
      </view>
      <view class="hot-searches">
        <view
          v-for="(search, index) in hotSearches"
          :key="index"
          class="search-item"
          @click="handleSearchClick(search.keyword)"
        >
          <view class="search-rank" :class="getSearchRankClass(index)">{{ index + 1 }}</view>
          <text class="search-keyword">{{ search.keyword }}</text>
          <view v-if="search.isHot" class="hot-badge">HOT</view>
          <view v-if="search.isNew" class="new-badge">NEW</view>
        </view>
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
import HotQuestions from '@/components/HotQuestions.vue'
import type { HotQuestionItem } from '@/components/HotQuestions.vue'
import { getQuestionList, getHotTags, getActiveUsers, getFeaturedQuestions } from '@/services/question'
import { getTotalStats } from '@/services/stats'

interface ActiveUser {
  userId: number
  nickname: string
  avatar: string
  answerCount: number
  badge: string | null
}

// HotQuestionItem 已从 HotQuestions 组件导入,移除重复定义

interface HotSearch {
  keyword: string
  isHot?: boolean
  isNew?: boolean
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

// 热门问题
const hotQuestions = ref<HotQuestionItem[]>([])

// 热门搜索
const hotSearches = ref<HotSearch[]>([
  { keyword: '期末复习资料', isHot: true },
  { keyword: 'Java多线程', isNew: true },
  { keyword: '数据结构算法', isHot: true },
  { keyword: '计算机网络', isNew: false },
  { keyword: 'Python爬虫', isHot: false },
  { keyword: '操作系统', isNew: false },
  { keyword: 'MySQL优化', isHot: true },
  { keyword: '前端框架', isNew: false }
])


// 排名徽章样式已移至 HotQuestions 组件内部

// 用户排名样式
const getUserRankClass = (index: number) => {
  if (index === 0) return 'user-rank-1'
  if (index === 1) return 'user-rank-2'
  if (index === 2) return 'user-rank-3'
  return ''
}

// 皇冠图标
const getCrownIcon = (index: number) => {
  if (index === 0) return 'award'  // 金色皇冠
  if (index === 1) return 'award'  // 银色皇冠
  return 'award'  // 铜色皇冠
}

// 徽章样式类
const getBadgeClass = (badge: string) => {
  if (badge === '优质答主') return 'badge-premium'
  if (badge === '热心答主') return 'badge-active'
  if (badge === '活跃答主') return 'badge-regular'
  return ''
}

// 徽章图标
const getBadgeIcon = (badge: string) => {
  if (badge === '优质答主') return 'star'
  if (badge === '热心答主') return 'heart'
  if (badge === '活跃答主') return 'zap'
  return 'award'
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

// 点击热门问题 (HotQuestions组件回调)
const handleHotQuestionClick = (question: HotQuestionItem) => {
  uni.navigateTo({ url: `/pages/question/detail?id=${question.qid}` })
}

// 点击用户
const handleUserClick = (userId: number) => {
  uni.navigateTo({ url: `/pages/user/profile?id=${userId}` })
}

// 快捷操作
const handleUploadResource = () => {
  // 🎯 跳转到统一发布入口
  uni.navigateTo({ url: '/pages/publish/index' })
}

const handleMyCollections = () => {
  uni.navigateTo({ url: '/pages/user/favorites' })
}

const handleMyPoints = () => {
  uni.navigateTo({ url: '/pages/user/points-history' })
}

const handleMyTasks = () => {
  uni.navigateTo({ url: '/pages/task/my' })
}

// 热门搜索相关
const handleSearchClick = (keyword: string) => {
  emit('filterByTag', keyword)  // 复用标签筛选逻辑
}

const getSearchRankClass = (index: number) => {
  if (index === 0) return 'rank-1'
  if (index === 1) return 'rank-2'
  if (index === 2) return 'rank-3'
  return ''
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

// 加载热门问题
const loadHotQuestions = async () => {
  try {
    const res = await getQuestionList({
      page: 1,
      pageSize: 5,
      sortBy: 'views',
      sortOrder: 'desc'
    })
    hotQuestions.value = res.list.map(q => ({
      qid: q.qid,
      title: q.title,
      views: q.views,
      answerCount: q.answerCount
    }))
  } catch (error) {
    console.error('[RecommendSidebar] 加载热门问题失败:', error)
    // 失败时保持空数组，显示空状态
    hotQuestions.value = []
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
  loadHotQuestions()
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

// ===================================
// 快捷操作
// ===================================
.quick-actions-card {
  background: linear-gradient(135deg, #f8fafc 0%, #ffffff 100%);
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease-out;
  border: 1px solid transparent;

  .action-icon {
    flex-shrink: 0;
    transition: transform 0.2s;
  }

  .action-text {
    font-size: 14px;
    font-weight: 500;
    flex: 1;
  }

  &:active {
    transform: scale(0.98);
  }
}

.primary-action {
  background: linear-gradient(135deg, $primary 0%, lighten($primary, 5%) 100%);
  color: $white;
  box-shadow: 0 2px 8px rgba($primary, 0.25);

  .action-icon {
    color: $white;
  }

  .action-text {
    color: $white;
    font-weight: 600;
  }

  &:hover {
    box-shadow: 0 4px 12px rgba($primary, 0.35);
    transform: translateY(-2px);

    .action-icon {
      transform: scale(1.1);
    }
  }
}

.secondary-action {
  background: $white;
  border-color: $gray-200;

  .action-icon {
    color: $primary;
  }

  .action-text {
    color: $gray-700;
  }

  &:hover {
    background: $gray-50;
    border-color: $primary;
    box-shadow: 0 2px 8px rgba($primary, 0.15);

    .action-icon {
      transform: scale(1.1);
    }

    .action-text {
      color: $primary;
    }
  }
}

// 热门问题样式已移至 HotQuestions 组件内部

// ===================================
// 活跃答主
// ===================================
.active-users {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: $white;
  border: 1px solid transparent;

  &:hover {
    transform: translateX(4px) scale(1.02);
    background: linear-gradient(135deg, #F8FAFC 0%, #F1F5F9 100%);
    border-color: $gray-200;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);

    .user-avatar {
      transform: scale(1.1);
      box-shadow: 0 4px 16px rgba($primary, 0.25);
    }
  }

  // 第1名特殊样式 - 金色
  &.user-rank-1 {
    background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 50%, #FEF3C7 100%);
    border: 2px solid #F59E0B;
    box-shadow: 0 4px 12px rgba(245, 158, 11, 0.25);

    &:hover {
      box-shadow: 0 6px 20px rgba(245, 158, 11, 0.4);
      transform: translateY(-2px);
    }

    .user-name {
      color: #92400E;  // 深金色文字
      font-weight: 700;
    }
  }

  // 第2名特殊样式 - 银色(增强对比度)
  &.user-rank-2 {
    background: linear-gradient(135deg, #E5E7EB 0%, #D1D5DB 50%, #E5E7EB 100%);
    border: 2px solid #9CA3AF;
    box-shadow: 0 3px 10px rgba(107, 114, 128, 0.2);

    &:hover {
      box-shadow: 0 5px 16px rgba(107, 114, 128, 0.3);
      transform: translateY(-2px);
    }

    .user-name {
      color: #374151;  // 深灰色文字
      font-weight: 600;
    }
  }

  // 第3名特殊样式 - 铜色(增强对比度)
  &.user-rank-3 {
    background: linear-gradient(135deg, #FED7AA 0%, #FDBA74 50%, #FED7AA 100%);
    border: 2px solid #F97316;
    box-shadow: 0 3px 10px rgba(249, 115, 22, 0.2);

    &:hover {
      box-shadow: 0 5px 16px rgba(249, 115, 22, 0.3);
      transform: translateY(-2px);
    }

    .user-name {
      color: #9A3412;  // 深橙色文字
      font-weight: 600;
    }
  }
}

.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: 3px solid $white;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.rank-crown {
  position: absolute;
  top: -4px;
  right: -4px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid $white;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);

  &.crown-1 {
    background: linear-gradient(135deg, #FBBF24 0%, #F59E0B 100%);
    color: $white;
    animation: pulse-gold 2s infinite;
  }

  &.crown-2 {
    background: linear-gradient(135deg, #D1D5DB 0%, #9CA3AF 100%);
    color: $white;
  }

  &.crown-3 {
    background: linear-gradient(135deg, #FB923C 0%, #F97316 100%);
    color: $white;
  }
}

@keyframes pulse-gold {
  0%, 100% {
    box-shadow: 0 2px 6px rgba(245, 158, 11, 0.3);
  }
  50% {
    box-shadow: 0 4px 12px rgba(245, 158, 11, 0.6);
    transform: scale(1.1);
  }
}

.user-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.name-line {
  display: flex;
  align-items: center;
  gap: 6px;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: $gray-800;
  @include text-ellipsis(1);
  transition: color 0.2s;

  .user-item:hover & {
    color: $primary;
  }
}

.top-badge {
  padding: 2px 8px;
  background: linear-gradient(135deg, #DC2626 0%, #EF4444 100%);
  color: $white;
  font-size: 10px;
  font-weight: 700;
  border-radius: 10px;
  letter-spacing: 0.5px;
  box-shadow: 0 2px 4px rgba(220, 38, 38, 0.3);
}

.stats-line {
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-icon {
  color: $primary;  // 改为主色,更醒目
}

.user-answers {
  font-size: 13px;  // 从12px增加
  color: $gray-700;  // 从600加深到700
  font-weight: 600;  // 从500加粗到600
}

.user-badge {
  flex-shrink: 0;
  padding: 6px 12px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 600;
  transition: all 0.2s;

  &.badge-premium {
    background: linear-gradient(135deg, #8B5CF6 0%, #A78BFA 100%);
    color: $white;
    box-shadow: 0 2px 6px rgba(139, 92, 246, 0.3);
  }

  &.badge-active {
    background: linear-gradient(135deg, #EC4899 0%, #F472B6 100%);
    color: $white;
    box-shadow: 0 2px 6px rgba(236, 72, 153, 0.3);
  }

  &.badge-regular {
    background: linear-gradient(135deg, #10B981 0%, #34D399 100%);
    color: $white;
    box-shadow: 0 2px 6px rgba(16, 185, 129, 0.3);
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
  padding: 32px 16px;
  gap: 8px;
}

.empty-icon {
  color: $gray-300;
  opacity: 0.6;
}

.empty-text {
  font-size: 13px;
  color: $gray-400;
  text-align: center;
}

// ===================================
// 热门搜索
// ===================================
.hot-searches {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.search-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-radius: 8px;
  background: $gray-50;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: lighten($primary, 48%);
    transform: translateX(3px);

    .search-keyword {
      color: $primary;
    }
  }
}

.search-rank {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  border-radius: 4px;
  background: $gray-200;
  color: $gray-600;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;

  &.rank-1 {
    background: linear-gradient(135deg, #FBBF24 0%, #F59E0B 100%);
    color: $white;
  }

  &.rank-2 {
    background: linear-gradient(135deg, #D1D5DB 0%, #9CA3AF 100%);
    color: $white;
  }

  &.rank-3 {
    background: linear-gradient(135deg, #FB923C 0%, #F97316 100%);
    color: $white;
  }
}

.search-keyword {
  flex: 1;
  font-size: 13px;
  color: $gray-800;
  font-weight: 500;
  @include text-ellipsis(1);
  transition: color 0.2s;
}

.hot-badge {
  flex-shrink: 0;
  padding: 2px 6px;
  background: linear-gradient(135deg, #EF4444 0%, #DC2626 100%);
  color: $white;
  font-size: 10px;
  font-weight: 700;
  border-radius: 4px;
  letter-spacing: 0.5px;
}

.new-badge {
  flex-shrink: 0;
  padding: 2px 6px;
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
  color: $white;
  font-size: 10px;
  font-weight: 700;
  border-radius: 4px;
  letter-spacing: 0.5px;
}

// ===================================
// 动画效果
// ===================================
@keyframes pulse-gold {
  0%, 100% {
    box-shadow: 0 2px 6px rgba(245, 158, 11, 0.3);
  }
  50% {
    box-shadow: 0 4px 12px rgba(245, 158, 11, 0.5);
    transform: scale(1.05);
  }
}

@keyframes shimmer {
  0% {
    background-position: -200% center;
  }
  100% {
    background-position: 200% center;
  }
}
</style>
