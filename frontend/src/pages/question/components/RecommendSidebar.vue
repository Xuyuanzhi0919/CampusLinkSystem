<template>
  <view class="recommend-sidebar">
    <!-- 快捷操作模块 -->
    <CCard variant="default" class="sidebar-card quick-actions-card">
      <view class="card-header">
        <Icon name="zap" :size="18" class="header-icon" />
        <text class="header-title">快捷操作</text>
      </view>
      <view class="quick-actions">
        <view class="action-item primary-action" @click="handleAskQuestion">
          <Icon name="help-circle" :size="20" class="action-icon" />
          <text class="action-text">发起提问</text>
        </view>
        <view class="action-item secondary-action" @click="handleUploadResource">
          <Icon name="upload" :size="18" class="action-icon" />
          <text class="action-text">上传资源</text>
        </view>
        <view class="action-item secondary-action" @click="handleBrowseTasks">
          <Icon name="list" :size="18" class="action-icon" />
          <text class="action-text">浏览任务</text>
        </view>
      </view>
    </CCard>

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
      <view class="card-header">
        <Icon name="tag" :size="18" class="header-icon" />
        <text class="header-title">热门标签</text>
        <text class="view-more" @click="handleViewMoreTags">
          {{ showAllTags ? '收起' : '展开更多' }} {{ showAllTags ? '▴' : '▾' }}
        </text>
      </view>
      <view v-if="hotTags.length > 0" class="tags-grid">
        <view
          v-for="(tag, index) in displayedTags"
          :key="tag.name"
          class="tag-pill"
          :class="getTagLevelClass(index, tag.count)"
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
import { ref, computed, onMounted } from 'vue'
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
const showAllTags = ref(false) // 是否展开所有标签

// 活跃答主
const activeUsers = ref<ActiveUser[]>([])

// 热门问题
const hotQuestions = ref<HotQuestion[]>([])

// 显示的标签（限制数量）
const displayedTags = computed(() => {
  if (showAllTags.value) {
    return hotTags.value
  }
  return hotTags.value.slice(0, 6) // 默认只显示前6个
})

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

// 标签层级样式（按权重分层）
const getTagLevelClass = (index: number, count: number) => {
  // 一级标签：前2个或计数>10
  if (index < 2 || count > 10) return 'tag-level-1'
  // 二级标签：前5个或计数>5
  if (index < 5 || count > 5) return 'tag-level-2'
  // 三级标签：其他
  return 'tag-level-3'
}

// Emits
const emit = defineEmits<{
  filterByTag: [tag: string]
}>()

// 点击标签 - 触发父组件筛选
const handleTagClick = (tag: string) => {
  emit('filterByTag', tag)
}

// 展开/收起标签
const handleViewMoreTags = () => {
  showAllTags.value = !showAllTags.value
}

// 查看更多用户
const handleViewMoreUsers = () => {
  uni.showToast({ title: '查看更多用户功能开发中', icon: 'none' })
}

// 点击问题
const handleQuestionClick = (qid: number) => {
  uni.navigateTo({ url: `/pages/question/detail?id=${qid}` })
}

// 点击用户
const handleUserClick = (userId: number) => {
  uni.navigateTo({ url: `/pages/user/profile?id=${userId}` })
}

// 快捷操作
const handleAskQuestion = () => {
  uni.navigateTo({ url: '/pages/question/publish' })
}

const handleUploadResource = () => {
  uni.navigateTo({ url: '/pages/resource/upload' })
}

const handleBrowseTasks = () => {
  uni.switchTab({ url: '/pages/task/index' })
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

// ===================================
// 热门标签（按权重分层）
// ===================================
.tags-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  row-gap: 12px; // 行间距稍大
}

.tag-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  font-weight: 500;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(37, 99, 235, 0.2);
  }

  &:active {
    transform: translateY(0);
  }

  // 一级标签：深色背景 + 白色文字（高权重）
  &.tag-level-1 {
    background: linear-gradient(135deg, $primary 0%, darken($primary, 5%) 100%);
    border: none;
    padding: 10px 16px;
    font-size: 14px;
    animation: pulse-tag 3s ease-in-out infinite; // 添加脉冲动画

    .tag-text {
      color: $white;
      font-weight: 600;
    }

    .tag-count {
      background: rgba(255, 255, 255, 0.25);
      color: $white;
      font-weight: 700;
    }

    &:hover {
      background: linear-gradient(135deg, darken($primary, 5%) 0%, darken($primary, 10%) 100%);
      box-shadow: 0 8px 20px rgba(37, 99, 235, 0.35);
      animation: none; // hover时暂停动画
    }
  }

  // 二级标签：浅色边框 + 深色文字（中权重）
  &.tag-level-2 {
    background: $white;
    border: 1.5px solid lighten($primary, 30%);

    .tag-text {
      color: $primary;
      font-weight: 600;
    }

    .tag-count {
      background: rgba($primary, 0.1);
      color: $primary;
      font-weight: 600;
    }

    &:hover {
      border-color: $primary;
      background: lighten($primary, 48%);
    }
  }

  // 三级标签：灰色边框 + 灰色文字（低权重）
  &.tag-level-3 {
    background: $gray-50;
    border: 1px solid $gray-200;

    .tag-text {
      color: $gray-700;
      font-weight: 500;
    }

    .tag-count {
      background: $white;
      color: $gray-600;
      font-weight: 600;
      border: 1px solid $gray-200;
    }

    &:hover {
      background: $white;
      border-color: $gray-400;
    }
  }
}

.tag-text {
  font-size: 13px;
  letter-spacing: 0.3px;
  line-height: 1;
}

.tag-count {
  font-size: 11px;
  padding: 3px 7px;
  border-radius: 12px;
  min-width: 22px;
  text-align: center;
  line-height: 1;
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

// ===================================
// 动画效果
// ===================================
@keyframes pulse-tag {
  0%, 100% {
    box-shadow: 0 4px 12px rgba($primary, 0.25);
  }
  50% {
    box-shadow: 0 6px 20px rgba($primary, 0.4);
    transform: translateY(-1px);
  }
}

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
