<template>
  <view class="recommend-sidebar">
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
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Icon from '@/components/icons/index.vue'
import { CCard } from '@/components/ui'

// 热门标签（模拟数据，实际应该从 API 获取）
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
</style>
