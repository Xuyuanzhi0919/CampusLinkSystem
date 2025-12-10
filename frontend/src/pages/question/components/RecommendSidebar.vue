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
          v-for="user in activeUsers"
          :key="user.userId"
          class="user-item"
          @click="handleUserClick(user.userId)"
        >
          <image :src="user.avatar" class="user-avatar" mode="aspectFill" />
          <view class="user-info">
            <text class="user-name">{{ user.nickname }}</text>
            <text class="user-answers">回答 {{ user.answerCount }} 个问题</text>
          </view>
          <view v-if="user.badge" class="user-badge">
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

// 排名徽章样式
const getRankClass = (index: number) => {
  if (index === 0) return 'rank-1'
  if (index === 1) return 'rank-2'
  if (index === 2) return 'rank-3'
  return ''
}

// 点击标签
const handleTagClick = (tag: string) => {
  uni.showToast({ title: `点击标签: ${tag}`, icon: 'none' })
  // TODO: 触发父组件的标签筛选
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
  gap: 4px;
  padding: 6px 10px;  // 统一使用px
  background: $gray-50;
  border-radius: 16px;  // 统一使用px
  border: 1px solid $gray-200;
  cursor: pointer;
  transition: all 0.2s ease-out;

  &:hover {
    background: rgba($primary, 0.06);  // 淡化hover背景
    border-color: $primary;
    transform: translateY(-1px);
  }

  &:active {
    transform: translateY(0);
  }
}

.tag-text {
  font-size: 13px;  // 统一使用px
  color: $gray-700;
  font-weight: $font-weight-medium;
}

.tag-count {
  font-size: 11px;
  color: $gray-500;
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
  gap: 12px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.2s ease-out;

  &:hover {
    transform: translateX(2px);

    .user-name {
      color: $primary;
    }
  }
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
  border: 2px solid $gray-100;
  transition: border-color 0.2s;

  .user-item:hover & {
    border-color: $primary;
  }
}

.user-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-size: 13px;
  font-weight: $font-weight-medium;
  color: $gray-800;
  @include text-ellipsis(1);
  transition: color 0.2s;
}

.user-answers {
  font-size: 11px;
  color: $gray-500;
}

.user-badge {
  flex-shrink: 0;
  padding: 4px 10px;
  background: linear-gradient(135deg, $accent 0%, lighten($accent, 5%) 100%);
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba($accent, 0.2);

  .badge-text {
    font-size: 11px;
    color: $white;
    font-weight: $font-weight-semibold;
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
