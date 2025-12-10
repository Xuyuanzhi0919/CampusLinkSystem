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
// 本校热议卡片特殊样式（淡化处理）
// ===================================
.school-card {
  background: rgba($primary, 0.02);  // 大幅淡化背景
  border: 1px solid $gray-200;  // 使用统一边框

  .card-header {
    border-bottom-color: $gray-100;
  }

  .school-icon {
    color: $accent;
  }
}

.school-badge {
  padding: 3px 8px;  // 统一使用px
  background: rgba($accent, 0.08);  // 淡化背景
  border-radius: 12px;

  .badge-text {
    font-size: 11px;
    color: $accent;
    font-weight: $font-weight-medium;  // 从semibold降为medium
  }
}

.school-rank {
  background: $accent;  // 去除渐变，使用纯色
  color: $white;
  box-shadow: none;  // 去除阴影
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
  gap: 10px;  // 统一使用px
}

.user-item {
  display: flex;
  align-items: center;
  gap: 10px;  // 统一使用px
  cursor: pointer;
  transition: transform 0.2s ease-out;

  &:hover {
    transform: translateX(2px);  // 统一使用px

    .user-name {
      color: $primary;
    }
  }
}

.user-avatar {
  width: 36px;  // 统一使用px，缩小尺寸
  height: 36px;
  border-radius: 50%;
  flex-shrink: 0;
  border: 1px solid $gray-200;
}

.user-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;  // 统一使用px
}

.user-name {
  font-size: 13px;  // 统一使用px
  font-weight: $font-weight-medium;
  color: $gray-800;
  @include text-ellipsis(1);
  transition: color 0.2s ease-out;
}

.user-answers {
  font-size: 11px;  // 统一使用px
  color: $gray-500;
}

.user-badge {
  flex-shrink: 0;
  padding: 3px 8px;  // 统一使用px
  background: rgba($accent, 0.08);  // 淡化背景
  border-radius: 12px;
}

.badge-text {
  font-size: 11px;  // 统一使用px
  color: $accent;
  font-weight: $font-weight-medium;  // 从semibold降为medium
}
</style>
