<template>
  <view class="recommend-sidebar">
    <!-- 活跃答主模块 -->
    <CCard variant="default" class="sidebar-card">
      <view class="card-header">
        <Icon name="users" :size="18" class="header-icon" />
        <text class="header-title">活跃答主</text>
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
      <view class="card-header">
        <Icon name="tag" :size="18" class="header-icon" />
        <text class="header-title">热门标签</text>
      </view>
      <view v-if="hotTags.length > 0" class="tags-grid">
        <view
          v-for="tag in hotTags"
          :key="tag.name"
          class="tag-pill"
          @click="handleTagClick(tag.name)"
        >
          <text class="tag-text">{{ tag.name }}</text>
          <text class="tag-count">{{ tag.count }}</text>
        </view>
      </view>
      <view v-else class="empty-state">
        <Icon name="tag" :size="32" class="empty-icon" />
        <text class="empty-text">暂无热门标签</text>
      </view>
    </CCard>

    <!-- 热门问题模块 -->
    <CCard variant="default" class="sidebar-card">
      <view class="card-header">
        <Icon name="trending-up" :size="18" class="header-icon" />
        <text class="header-title">热门问题</text>
      </view>
      <view v-if="hotQuestions.length > 0" class="hot-questions">
        <view
          v-for="(question, index) in hotQuestions"
          :key="question.qid"
          class="hot-question-item"
          @click="handleQuestionClick(question.qid)"
        >
          <view class="rank-badge" :class="getRankClass(index)">
            {{ index + 1 }}
          </view>
          <view class="question-content">
            <text class="question-title">{{ question.title }}</text>
            <view class="question-meta">
              <Icon name="eye" :size="12" class="meta-icon" />
              <text class="meta-text">{{ question.views }}</text>
              <Icon name="message-circle" :size="12" class="meta-icon" />
              <text class="meta-text">{{ question.answerCount }}</text>
            </view>
          </view>
        </view>
      </view>
      <view v-else class="empty-state">
        <Icon name="help-circle" :size="32" class="empty-icon" />
        <text class="empty-text">暂无热门问题</text>
      </view>
    </CCard>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Icon from '@/components/icons/index.vue'
import { CCard } from '@/components/ui'
import { getQuestionList, getHotTags, getActiveUsers } from '@/services/question'

// 定义类型
interface HotTag {
  name: string
  count: number
}

interface ActiveUser {
  userId: number
  nickname: string
  avatar: string
  answerCount: number
  badge: string | null
}

interface HotQuestion {
  qid: number
  title: string
  views: number
  answerCount: number
}

// 热门标签
const hotTags = ref<HotTag[]>([])

// 活跃答主
const activeUsers = ref<ActiveUser[]>([])

// 热门问题
const hotQuestions = ref<HotQuestion[]>([])

// 排名徽章样式（问题）
const getRankClass = (index: number) => {
  if (index === 0) return 'rank-1'
  if (index === 1) return 'rank-2'
  if (index === 2) return 'rank-3'
  return ''
}

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

// 点击标签 - 触发父组件筛选
const handleTagClick = (tag: string) => {
  emit('filterByTag', tag)
}

// 点击问题
const handleQuestionClick = (qid: number) => {
  uni.navigateTo({ url: `/pages/question/detail?id=${qid}` })
}

// 点击用户
const handleUserClick = (userId: number) => {
  uni.navigateTo({ url: `/pages/user/profile?id=${userId}` })
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

// 组件挂载时加载数据
onMounted(() => {
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
  padding: 16px;  // 统一内边距为16px
  background: $white;
  border-radius: 12px;
  border: 1px solid $gray-200;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  transition: all 0.2s ease-out;

  &:hover {
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
    border-color: $gray-300;
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid $gray-100;
}

.header-icon {
  color: $primary;
}

.header-title {
  font-size: 15px;  // 减小字号
  font-weight: $font-weight-semibold;
  color: $gray-800;  // 从900降为800
  letter-spacing: 0;
  flex: 1;
}

// ===================================
// 热门标签
// ===================================
.tags-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;  // 统一使用px
}

.tag-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: linear-gradient(135deg, #F0F9FF 0%, #E0F2FE 100%);
  border-radius: 20px;
  border: 1.5px solid #BAE6FD;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;

  // 热度前3的标签使用不同配色
  &:nth-child(1) {
    background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
    border-color: #FCD34D;

    .tag-text { color: #92400E; }
    .tag-count {
      background: rgba(217, 119, 6, 0.15);
      color: #D97706;
    }
  }

  &:nth-child(2) {
    background: linear-gradient(135deg, #FEE2E2 0%, #FECACA 100%);
    border-color: #FCA5A5;

    .tag-text { color: #991B1B; }
    .tag-count {
      background: rgba(239, 68, 68, 0.15);
      color: #EF4444;
    }
  }

  &:nth-child(3) {
    background: linear-gradient(135deg, #E0E7FF 0%, #C7D2FE 100%);
    border-color: #A5B4FC;

    .tag-text { color: #3730A3; }
    .tag-count {
      background: rgba(99, 102, 241, 0.15);
      color: #6366F1;
    }
  }

  &:hover {
    transform: translateY(-2px) scale(1.05);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
    border-color: $primary;
  }

  &:active {
    transform: translateY(-1px) scale(1.02);
  }

  // 闪烁动画（仅前3个）
  &:nth-child(-n+3)::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
    animation: shimmer 3s infinite;
  }
}

@keyframes shimmer {
  0%, 100% { left: -100%; }
  50% { left: 100%; }
}

.tag-text {
  font-size: 13px;
  color: #1E40AF;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.tag-count {
  font-size: 11px;
  font-weight: 700;
  color: #3B82F6;
  background: rgba(59, 130, 246, 0.15);
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 20px;
  text-align: center;
}

// ===================================
// 热门问题
// ===================================
.hot-questions {
  display: flex;
  flex-direction: column;
  gap: 12px;  // 统一使用px
}

.hot-question-item {
  display: flex;
  gap: 8px;  // 统一使用px
  cursor: pointer;
  transition: transform 0.2s ease-out;

  &:hover {
    transform: translateX(2px);  // 统一使用px

    .question-title {
      color: $primary;
    }
  }
}

.rank-badge {
  flex-shrink: 0;
  width: 20px;  // 统一使用px，缩小尺寸
  height: 20px;
  border-radius: 6px;
  background: $gray-100;
  color: $gray-600;
  font-size: 12px;
  font-weight: $font-weight-semibold;
  @include flex-center;

  &.rank-1 {
    background: $rank-gold;  // 去除渐变
    color: $white;
    box-shadow: none;  // 去除阴影
  }

  &.rank-2 {
    background: $rank-silver;  // 去除渐变
    color: $white;
    box-shadow: none;
  }

  &.rank-3 {
    background: $rank-bronze;  // 去除渐变
    color: $white;
    box-shadow: none;
  }
}

.question-content {
  flex: 1;
  min-width: 0;
}

.question-title {
  @include text-ellipsis(2);
  font-size: 13px;  // 统一使用px
  color: $gray-800;
  line-height: 1.4;
  margin-bottom: 6px;  // 统一使用px
  transition: color 0.2s ease-out;
}

.question-meta {
  display: flex;
  align-items: center;
  gap: 6px;  // 统一使用px
}

.meta-icon {
  color: $gray-400;
}

.meta-text {
  font-size: 11px;  // 统一使用px
  color: $gray-500;
  margin-right: 8px;  // 统一使用px
}

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

  // 第1名特殊样式
  &.user-rank-1 {
    background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 50%, #FEF3C7 100%);
    border-color: #F59E0B;
    box-shadow: 0 2px 8px rgba(245, 158, 11, 0.2);

    &:hover {
      box-shadow: 0 6px 20px rgba(245, 158, 11, 0.3);
    }
  }

  // 第2名特殊样式
  &.user-rank-2 {
    background: linear-gradient(135deg, #F3F4F6 0%, #E5E7EB 50%, #F3F4F6 100%);
    border-color: #9CA3AF;
    box-shadow: 0 2px 8px rgba(156, 163, 175, 0.15);
  }

  // 第3名特殊样式
  &.user-rank-3 {
    background: linear-gradient(135deg, #FDEDE8 0%, #FED7C3 50%, #FDEDE8 100%);
    border-color: #F97316;
    box-shadow: 0 2px 8px rgba(249, 115, 22, 0.15);
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
  color: $gray-400;
}

.user-answers {
  font-size: 12px;
  color: $gray-600;
  font-weight: 500;
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
</style>
