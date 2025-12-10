<template>
  <view class="recommend-sidebar">
    <!-- 热门标签模块 -->
    <CCard variant="elevated" class="sidebar-card">
      <view class="card-header">
        <Icon name="tag" :size="18" class="header-icon" />
        <text class="header-title">热门标签</text>
      </view>
      <view class="tags-grid">
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
    </CCard>

    <!-- 热门问题模块 -->
    <CCard variant="elevated" class="sidebar-card">
      <view class="card-header">
        <Icon name="trending-up" :size="18" class="header-icon" />
        <text class="header-title">热门问题</text>
      </view>
      <view class="hot-questions">
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
    </CCard>

    <!-- 活跃答主模块 -->
    <CCard variant="elevated" class="sidebar-card">
      <view class="card-header">
        <Icon name="users" :size="18" class="header-icon" />
        <text class="header-title">活跃答主</text>
      </view>
      <view class="active-users">
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
    </CCard>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Icon from '@/components/icons/index.vue'
import { CCard } from '@/components/ui'

// 模拟数据（实际应该从 API 获取）
const hotTags = ref([
  { name: '数据结构', count: 128 },
  { name: '算法', count: 95 },
  { name: '前端开发', count: 87 },
  { name: 'Vue', count: 76 },
  { name: '宿舍生活', count: 65 },
  { name: '图书馆', count: 54 },
  { name: 'Java', count: 48 },
  { name: '选课攻略', count: 42 },
])

const hotQuestions = ref([
  {
    qid: 1,
    title: '如何高效准备期末考试？',
    views: 2345,
    answerCount: 23,
  },
  {
    qid: 2,
    title: '学校附近有哪些好吃的餐厅？',
    views: 1876,
    answerCount: 18,
  },
  {
    qid: 3,
    title: '计算机专业有哪些值得学习的技能？',
    views: 1654,
    answerCount: 15,
  },
  {
    qid: 4,
    title: '如何申请奖学金？',
    views: 1432,
    answerCount: 12,
  },
  {
    qid: 5,
    title: '宿舍断网了怎么办？',
    views: 1287,
    answerCount: 9,
  },
])

const activeUsers = ref([
  {
    userId: 1,
    nickname: '编程小能手',
    avatar: 'https://via.placeholder.com/80',
    answerCount: 145,
    badge: '学霸',
  },
  {
    userId: 2,
    nickname: '校园达人',
    avatar: 'https://via.placeholder.com/80',
    answerCount: 98,
    badge: '热心',
  },
  {
    userId: 3,
    nickname: '技术大牛',
    avatar: 'https://via.placeholder.com/80',
    answerCount: 76,
    badge: null,
  },
  {
    userId: 4,
    nickname: '学习委员',
    avatar: 'https://via.placeholder.com/80',
    answerCount: 54,
    badge: null,
  },
])

const getRankClass = (index: number) => {
  if (index === 0) return 'rank-1'
  if (index === 1) return 'rank-2'
  if (index === 2) return 'rank-3'
  return ''
}

const handleTagClick = (tag: string) => {
  uni.showToast({ title: `点击标签: ${tag}`, icon: 'none' })
  // TODO: 触发父组件的标签筛选
}

const handleQuestionClick = (qid: number) => {
  uni.navigateTo({ url: `/pages/question/detail?id=${qid}` })
}

const handleUserClick = (userId: number) => {
  uni.navigateTo({ url: `/pages/user/profile?id=${userId}` })
}

// 实际应用中，这里应该调用 API 获取真实数据
onMounted(() => {
  // loadHotTags()
  // loadHotQuestions()
  // loadActiveUsers()
})
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.recommend-sidebar {
  display: flex;
  flex-direction: column;
  gap: $sp-6;
  position: sticky;
  top: $sp-6;
}

// ===================================
// 卡片通用样式
// ===================================
.sidebar-card {
  padding: $sp-6;
}

.card-header {
  display: flex;
  align-items: center;
  gap: $sp-3;
  margin-bottom: $sp-6;
  padding-bottom: $sp-4;
  border-bottom: 2rpx solid $gray-100;
}

.header-icon {
  color: $primary;
}

.header-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $gray-900;
  letter-spacing: -0.01em;
}

// ===================================
// 热门标签
// ===================================
.tags-grid {
  display: flex;
  flex-wrap: wrap;
  gap: $sp-3;
}

.tag-pill {
  display: inline-flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-2 $sp-4;
  background: $gray-50;
  border-radius: $radius-pill;
  border: 1rpx solid $gray-200;
  cursor: pointer;
  transition: all $duration-base;

  &:hover {
    background: $primary-50;
    border-color: $primary-200;
    transform: translateY(-1rpx);
  }

  &:active {
    transform: translateY(0);
  }
}

.tag-text {
  font-size: $font-size-sm;
  color: $gray-700;
  font-weight: $font-weight-medium;
}

.tag-count {
  font-size: $font-size-xs;
  color: $gray-500;
}

// ===================================
// 热门问题
// ===================================
.hot-questions {
  display: flex;
  flex-direction: column;
  gap: $sp-5;
}

.hot-question-item {
  display: flex;
  gap: $sp-3;
  cursor: pointer;
  transition: transform $duration-base;

  &:hover {
    transform: translateX(4rpx);

    .question-title {
      color: $primary;
    }
  }
}

.rank-badge {
  flex-shrink: 0;
  width: 40rpx;
  height: 40rpx;
  border-radius: $radius-sm;
  background: $gray-100;
  color: $gray-600;
  font-size: $font-size-sm;
  font-weight: $font-weight-bold;
  @include flex-center;

  &.rank-1 {
    background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
    color: $white;
    box-shadow: 0 2rpx 8rpx rgba(255, 165, 0, 0.3);
  }

  &.rank-2 {
    background: linear-gradient(135deg, #C0C0C0 0%, #A9A9A9 100%);
    color: $white;
    box-shadow: 0 2rpx 8rpx rgba(192, 192, 192, 0.3);
  }

  &.rank-3 {
    background: linear-gradient(135deg, #CD7F32 0%, #B8860B 100%);
    color: $white;
    box-shadow: 0 2rpx 8rpx rgba(205, 127, 50, 0.3);
  }
}

.question-content {
  flex: 1;
  min-width: 0;
}

.question-title {
  @include text-ellipsis(2);
  font-size: $font-size-base;
  color: $gray-800;
  line-height: 1.4;
  margin-bottom: $sp-2;
  transition: color $duration-base;
}

.question-meta {
  display: flex;
  align-items: center;
  gap: $sp-2;
}

.meta-icon {
  color: $gray-400;
}

.meta-text {
  font-size: $font-size-xs;
  color: $gray-500;
  margin-right: $sp-3;
}

// ===================================
// 活跃答主
// ===================================
.active-users {
  display: flex;
  flex-direction: column;
  gap: $sp-4;
}

.user-item {
  display: flex;
  align-items: center;
  gap: $sp-3;
  cursor: pointer;
  transition: transform $duration-base;

  &:hover {
    transform: translateX(4rpx);

    .user-name {
      color: $primary;
    }
  }
}

.user-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  flex-shrink: 0;
  border: 2rpx solid $gray-100;
}

.user-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: $sp-1;
}

.user-name {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $gray-800;
  @include text-ellipsis(1);
  transition: color $duration-base;
}

.user-answers {
  font-size: $font-size-xs;
  color: $gray-500;
}

.user-badge {
  flex-shrink: 0;
  padding: $sp-1 $sp-3;
  background: linear-gradient(135deg, $accent-50 0%, $accent-100 100%);
  border-radius: $radius-pill;
}

.badge-text {
  font-size: $font-size-xs;
  color: $accent;
  font-weight: $font-weight-semibold;
}
</style>
