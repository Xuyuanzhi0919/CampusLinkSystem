<template>
  <view class="recommend-sidebar">
    <!-- ===== 全站趋势分组 ===== -->
    <view class="sidebar-section">
      <view class="section-header">
        <Icon name="trending-up" :size="16" class="section-icon" />
        <text class="section-title">全站趋势</text>
      </view>

      <!-- 热门标签模块 -->
      <CCard variant="default" class="sidebar-card">
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
      <CCard variant="default" class="sidebar-card">
        <view class="card-header">
          <Icon name="flame" :size="18" class="header-icon" />
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
    </view>

    <!-- ===== 校园动态分组 ===== -->
    <view class="sidebar-section">
      <view class="section-header">
        <Icon name="school" :size="16" class="section-icon" />
        <text class="section-title">校园动态</text>
      </view>

      <!-- 本校热议模块 -->
      <CCard variant="default" class="sidebar-card school-card">
        <view class="card-header">
          <Icon name="message-square" :size="18" class="header-icon school-icon" />
          <text class="header-title">本校热议</text>
          <view class="school-badge">
            <text class="badge-text">{{ schoolName }}</text>
          </view>
        </view>
        <view class="hot-questions">
          <view
            v-for="(question, index) in schoolQuestions"
            :key="question.qid"
            class="hot-question-item"
            @click="handleQuestionClick(question.qid)"
          >
            <view class="rank-badge school-rank">
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
      <CCard variant="default" class="sidebar-card">
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
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useUserStore } from '@/stores/user'
import Icon from '@/components/icons/index.vue'
import { CCard } from '@/components/ui'

// Store
const userStore = useUserStore()
const { userInfo } = storeToRefs(userStore)

// 学校名称（从用户信息获取）
const schoolName = computed(() => {
  return userInfo.value?.schoolName || '校园'
})

// 本校热议问题（模拟数据，实际应基于用户 schoolId 从 API 获取）
const schoolQuestions = ref([
  {
    qid: 101,
    title: '图书馆周末开放时间有变化吗？',
    views: 1234,
    answerCount: 15,
  },
  {
    qid: 102,
    title: '校园卡充值在哪里比较方便？',
    views: 987,
    answerCount: 12,
  },
  {
    qid: 103,
    title: '新生宿舍楼有空调吗？',
    views: 856,
    answerCount: 10,
  },
  {
    qid: 104,
    title: '学校食堂哪个窗口最好吃？',
    views: 745,
    answerCount: 8,
  },
  {
    qid: 105,
    title: '校内打印店在哪里？',
    views: 623,
    answerCount: 6,
  },
])

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
  gap: $sp-8;  // 分组之间的间距（32px）
  padding-bottom: $sp-6;  // 底部留白，避免内容贴底
}

// ===================================
// 分组样式
// ===================================
.sidebar-section {
  display: flex;
  flex-direction: column;
  gap: $sp-6;  // 卡片之间的间距（24px）
}

.section-header {
  display: flex;
  align-items: center;
  gap: $sp-3;
  padding: $sp-4 $sp-5;
  margin-bottom: $sp-4;
  background: linear-gradient(135deg, rgba($primary, 0.04) 0%, rgba($accent, 0.02) 100%);
  border-left: 4rpx solid $primary;
  border-radius: $radius-md;
}

.section-icon {
  color: $primary;
  flex-shrink: 0;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-bold;
  color: $gray-900;
  letter-spacing: -0.01em;
  flex: 1;
}

// ===================================
// 卡片通用样式（统一规范）
// ===================================
.sidebar-card {
  padding: $sp-6;  // 统一内边距 24rpx (12px)
}

.card-header {
  display: flex;
  align-items: center;
  gap: $sp-3;
  margin-bottom: $sp-6;
  padding-bottom: $sp-4;
  border-bottom: 2rpx solid $gray-100;
  height: 48rpx;  // 统一标题区高度 48rpx (24px)
}

.header-icon {
  color: $primary;
}

.header-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $gray-900;
  letter-spacing: -0.01em;
  flex: 1;
}

// ===================================
// 本校热议卡片特殊样式
// ===================================
.school-card {
  background: linear-gradient(135deg, rgba($primary, 0.03) 0%, rgba($accent, 0.02) 100%);
  border: 1rpx solid rgba($primary, 0.1);

  .card-header {
    border-bottom-color: rgba($primary, 0.1);
  }

  .school-icon {
    color: $accent;
  }
}

.school-badge {
  padding: $sp-1 $sp-3;
  background: linear-gradient(135deg, rgba($primary, 0.1) 0%, rgba($accent, 0.1) 100%);
  border-radius: $radius-2xl;

  .badge-text {
    font-size: $font-size-xs;
    color: $accent;
    font-weight: $font-weight-semibold;
  }
}

.school-rank {
  background: linear-gradient(135deg, $accent 0%, $accent-600 100%);
  color: $white;
  box-shadow: 0 2rpx 8rpx rgba($accent, 0.25);
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
  border-radius: $radius-2xl;
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
    background: linear-gradient(135deg, $rank-gold 0%, $rank-gold-dark 100%);
    color: $white;
    box-shadow: 0 2rpx 8rpx rgba($rank-gold-dark, 0.3);
  }

  &.rank-2 {
    background: linear-gradient(135deg, $rank-silver 0%, $rank-silver-dark 100%);
    color: $white;
    box-shadow: 0 2rpx 8rpx rgba($rank-silver, 0.3);
  }

  &.rank-3 {
    background: linear-gradient(135deg, $rank-bronze 0%, $rank-bronze-dark 100%);
    color: $white;
    box-shadow: 0 2rpx 8rpx rgba($rank-bronze, 0.3);
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
  border-radius: $radius-2xl;
}

.badge-text {
  font-size: $font-size-xs;
  color: $accent;
  font-weight: $font-weight-semibold;
}
</style>
