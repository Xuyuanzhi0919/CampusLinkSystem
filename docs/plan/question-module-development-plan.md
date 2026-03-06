# 智能问答模块开发计划

**文档版本**: v1.0
**创建日期**: 2025-11-18
**项目**: CampusLink - 问答模块 MVP
**开发周期**: 5 个工作日

---

## 📊 项目概览

### 目标

开发完整的智能问答模块，包括问题列表、问题详情、发布问题、回答问题、我的问答等核心功能。

### 交付物

- ✅ 问题列表页（/pages/question/index.vue）
- ✅ 问题详情页（/pages/question/detail.vue）
- ✅ 发布问题页（/pages/question/ask.vue）
- ✅ 我的问答页（/pages/question/my.vue）
- ✅ 可复用组件（QuestionCard、AnswerCard、TagInput）
- ✅ 完整的单元测试
- ✅ 技术文档

### 成功标准

| 指标 | 目标 | 验收标准 |
|------|------|---------|
| 功能完成度 | 100% | 所有 MVP 功能可用 |
| 代码质量 | A | 无严重 Bug，符合编码规范 |
| 性能 | 优秀 | 列表滚动流畅（60fps），首屏加载 <2s |
| 兼容性 | 100% | H5 + 微信小程序 + PC 端完美运行 |
| 文档完整性 | 100% | API 文档、组件文档、使用说明齐全 |

---

## 📅 阶段划分

### Phase 1: 基础框架搭建（Day 1 上午，4 小时）

**目标**：完成项目基础结构，确保 API 和类型定义就绪

#### 1.1 创建页面文件（30 分钟）

**任务清单**：
- [ ] 创建 `/pages/question/index.vue`（问题列表页）
  - 基础模板（header + content + tabbar）
  - 引入 PCFloatingNav 和 CustomTabBar
  - 配置页面样式（灰色背景）

- [ ] 创建 `/pages/question/detail.vue`（问题详情页）
  - 基础模板（header + content）
  - 路由参数接收（questionId）
  - 配置页面样式

- [ ] 创建 `/pages/question/ask.vue`（发布问题页）
  - 基础模板（header + form）
  - 表单结构（标题、内容、分类等）
  - 配置页面样式

- [ ] 创建 `/pages/question/my.vue`（我的问答页）
  - 基础模板（header + tabs + list）
  - Tab 切换逻辑
  - 配置页面样式

**验收标准**：
- ✅ 所有页面可以正常访问
- ✅ 路由跳转正常
- ✅ 页面布局符合设计稿

#### 1.2 配置路由（15 分钟）

**pages.json 配置**：
```json
{
  "pages": [
    {
      "path": "pages/question/detail",
      "style": {
        "navigationBarTitleText": "问题详情",
        "enablePullDownRefresh": false
      }
    },
    {
      "path": "pages/question/ask",
      "style": {
        "navigationBarTitleText": "提问",
        "enablePullDownRefresh": false
      }
    },
    {
      "path": "pages/question/my",
      "style": {
        "navigationBarTitleText": "我的问答",
        "enablePullDownRefresh": true
      }
    }
  ]
}
```

**验收标准**：
- ✅ 所有路由配置正确
- ✅ 标题显示正确
- ✅ 下拉刷新配置正确

#### 1.3 创建组件（1 小时）

**任务清单**：

**A. QuestionCard.vue**（问题卡片组件）
```vue
<template>
  <view class="question-card" @click="handleClick">
    <!-- 标题 -->
    <view class="title">{{ question.title }}</view>

    <!-- 内容摘要 -->
    <view class="content-preview">{{ question.content }}</view>

    <!-- 标签 -->
    <view class="tags">
      <view v-for="tag in question.tags.slice(0, 3)" :key="tag" class="tag">
        #{{ tag }}
      </view>
    </view>

    <!-- 统计数据 -->
    <view class="stats">
      <view v-if="question.rewardPoints > 0" class="stat-item reward">
        <text class="icon">🎁</text>
        <text>{{ question.rewardPoints }} 积分</text>
      </view>
      <view class="stat-item">
        <text class="icon">👁️</text>
        <text>{{ question.views }} 次</text>
      </view>
      <view class="stat-item">
        <text class="icon">💬</text>
        <text>{{ question.answerCount }} 条回答</text>
      </view>
      <view v-if="question.isSolved" class="stat-item solved">
        <text class="icon">✅</text>
        <text>已解决</text>
      </view>
    </view>

    <!-- 用户信息 -->
    <view class="user-info">
      <image class="avatar" :src="question.askerAvatar" />
      <text class="name">{{ question.askerName }}</text>
      <text class="time">· {{ formatTime(question.createdAt) }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { QuestionItem } from '@/types/question'
import { formatRelativeTime } from '@/utils/time'

interface Props {
  question: QuestionItem
}

const props = defineProps<Props>()
const emit = defineEmits<{
  click: [id: number]
}>()

const handleClick = () => {
  emit('click', props.question.questionId)
}

const formatTime = (time: string) => {
  return formatRelativeTime(new Date(time))
}
</script>
```

**B. AnswerCard.vue**（回答卡片组件）
```vue
<template>
  <view class="answer-card" :class="{ accepted: answer.isAccepted }">
    <!-- 已采纳标识 -->
    <view v-if="answer.isAccepted" class="accepted-badge">
      最佳答案
    </view>

    <!-- 回答内容 -->
    <view class="content">{{ answer.content }}</view>

    <!-- 图片 -->
    <view v-if="answer.images && answer.images.length > 0" class="images">
      <image
        v-for="(img, index) in answer.images"
        :key="index"
        class="image"
        :src="img"
        mode="aspectFill"
        @click="previewImage(answer.images, index)"
      />
    </view>

    <!-- 底部信息 -->
    <view class="footer">
      <view class="user-info">
        <image class="avatar" :src="answer.responderAvatar" />
        <text class="name">{{ answer.responderName }}</text>
        <text class="time">· {{ formatTime(answer.createdAt) }}</text>
      </view>

      <view class="actions">
        <!-- 点赞按钮 -->
        <view
          class="like-btn"
          :class="{ liked: answer.isLiked }"
          @click="handleLike"
        >
          <text class="icon">{{ answer.isLiked ? '❤️' : '🤍' }}</text>
          <text>{{ answer.likes }}</text>
        </view>

        <!-- 采纳按钮（仅提问者且问题未解决） -->
        <view
          v-if="canAccept"
          class="accept-btn"
          @click="handleAccept"
        >
          采纳
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { AnswerItem } from '@/types/question'
import { formatRelativeTime } from '@/utils/time'

interface Props {
  answer: AnswerItem
  canAccept?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  canAccept: false
})

const emit = defineEmits<{
  like: [id: number]
  accept: [id: number]
}>()

const handleLike = () => {
  emit('like', props.answer.answerId)
}

const handleAccept = () => {
  emit('accept', props.answer.answerId)
}

const previewImage = (images: string[], current: number) => {
  uni.previewImage({
    urls: images,
    current
  })
}

const formatTime = (time: string) => {
  return formatRelativeTime(new Date(time))
}
</script>
```

**C. TagInput.vue**（标签输入组件）
```vue
<template>
  <view class="tag-input-wrapper">
    <!-- 已添加的标签 -->
    <view v-if="modelValue.length > 0" class="tags">
      <view
        v-for="(tag, index) in modelValue"
        :key="index"
        class="tag"
      >
        <text class="tag-text">#{{ tag }}</text>
        <text class="tag-delete" @click="removeTag(index)">✕</text>
      </view>
    </view>

    <!-- 输入框 -->
    <input
      v-model="inputValue"
      class="tag-input"
      type="text"
      placeholder="输入标签，回车添加（最多5个）"
      @confirm="addTag"
    />

    <!-- 热门标签推荐 -->
    <view v-if="hotTags.length > 0" class="hot-tags">
      <text class="hot-tags-title">热门标签：</text>
      <view
        v-for="tag in hotTags"
        :key="tag"
        class="hot-tag"
        @click="addHotTag(tag)"
      >
        #{{ tag }}
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface Props {
  modelValue: string[]
  maxTags?: number
  hotTags?: string[]
}

const props = withDefaults(defineProps<Props>(), {
  maxTags: 5,
  hotTags: () => []
})

const emit = defineEmits<{
  'update:modelValue': [value: string[]]
}>()

const inputValue = ref('')

const addTag = () => {
  const tag = inputValue.value.trim()
  if (!tag) return

  if (props.modelValue.length >= props.maxTags) {
    uni.showToast({ title: `最多添加 ${props.maxTags} 个标签`, icon: 'none' })
    return
  }

  if (props.modelValue.includes(tag)) {
    uni.showToast({ title: '标签已存在', icon: 'none' })
    return
  }

  emit('update:modelValue', [...props.modelValue, tag])
  inputValue.value = ''
}

const removeTag = (index: number) => {
  const newTags = [...props.modelValue]
  newTags.splice(index, 1)
  emit('update:modelValue', newTags)
}

const addHotTag = (tag: string) => {
  if (props.modelValue.includes(tag)) {
    uni.showToast({ title: '标签已存在', icon: 'none' })
    return
  }

  if (props.modelValue.length >= props.maxTags) {
    uni.showToast({ title: `最多添加 ${props.maxTags} 个标签`, icon: 'none' })
    return
  }

  emit('update:modelValue', [...props.modelValue, tag])
}
</script>
```

**验收标准**：
- ✅ QuestionCard 组件显示正确
- ✅ AnswerCard 组件显示正确
- ✅ TagInput 组件功能完整
- ✅ 所有组件响应式适配

#### 1.4 配置 Pinia Store（30 分钟）

**创建 `stores/question.ts`**：
```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  getQuestionList,
  getQuestionDetail,
  getAnswerList,
  createQuestion,
  answerQuestion,
  likeAnswer,
  unlikeAnswer,
  acceptAnswer
} from '@/services/question'
import type {
  QuestionItem,
  QuestionDetail,
  QuestionListParams,
  AnswerItem
} from '@/types/question'

export const useQuestionStore = defineStore('question', () => {
  // 问题列表
  const questions = ref<QuestionItem[]>([])
  const loading = ref(false)
  const refreshing = ref(false)
  const page = ref(1)
  const pageSize = ref(20)
  const total = ref(0)
  const hasMore = computed(() => questions.value.length < total.value)

  // 当前问题详情
  const currentQuestion = ref<QuestionDetail | null>(null)
  const answers = ref<AnswerItem[]>([])
  const answerPage = ref(1)
  const answerTotal = ref(0)
  const hasMoreAnswers = computed(() => answers.value.length < answerTotal.value)

  // 筛选条件
  const filters = ref<QuestionListParams>({
    category: null,
    isSolved: null,
    sortBy: 'created_at',
    sortOrder: 'desc'
  })

  // 加载问题列表
  const loadQuestions = async (refresh = false) => {
    if (refresh) {
      page.value = 1
      questions.value = []
      refreshing.value = true
    }

    if (loading.value) return

    loading.value = true

    try {
      const res = await getQuestionList({
        ...filters.value,
        page: page.value,
        pageSize: pageSize.value
      })

      if (refresh) {
        questions.value = res.list
      } else {
        questions.value.push(...res.list)
      }

      total.value = res.total
      page.value++
    } catch (error) {
      console.error('[QuestionStore] 加载失败:', error)
      uni.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      loading.value = false
      refreshing.value = false
    }
  }

  // 加载问题详情
  const loadQuestionDetail = async (id: number) => {
    try {
      currentQuestion.value = await getQuestionDetail(id)
    } catch (error) {
      console.error('[QuestionStore] 加载详情失败:', error)
      throw error
    }
  }

  // 加载回答列表
  const loadAnswers = async (questionId: number, refresh = false) => {
    if (refresh) {
      answerPage.value = 1
      answers.value = []
    }

    try {
      const res = await getAnswerList(questionId, {
        page: answerPage.value,
        pageSize: 20,
        sortBy: 'likes'
      })

      if (refresh) {
        answers.value = res.list
      } else {
        answers.value.push(...res.list)
      }

      answerTotal.value = res.total
      answerPage.value++
    } catch (error) {
      console.error('[QuestionStore] 加载回答失败:', error)
      throw error
    }
  }

  // 重置状态
  const reset = () => {
    questions.value = []
    page.value = 1
    total.value = 0
    currentQuestion.value = null
    answers.value = []
    answerPage.value = 1
    answerTotal.value = 0
  }

  return {
    questions,
    loading,
    refreshing,
    page,
    total,
    hasMore,
    currentQuestion,
    answers,
    answerPage,
    answerTotal,
    hasMoreAnswers,
    filters,
    loadQuestions,
    loadQuestionDetail,
    loadAnswers,
    reset
  }
})
```

**验收标准**：
- ✅ Store 正常工作
- ✅ 状态管理正确
- ✅ API 调用成功

#### 1.5 验证 API 服务（30 分钟）

**任务清单**：
- [ ] 测试 `getQuestionList` 接口
- [ ] 测试 `getQuestionDetail` 接口
- [ ] 测试 `createQuestion` 接口
- [ ] 测试 `answerQuestion` 接口
- [ ] 测试点赞/采纳接口

**验收标准**：
- ✅ 所有 API 接口返回数据正确
- ✅ 错误处理完善
- ✅ Token 自动刷新正常

---

### Phase 2: 问题列表页（Day 1 下午 + Day 2 上午，6 小时）

**目标**：完成问题列表页的所有功能

#### 2.1 实现筛选栏（1.5 小时）

**任务清单**：

**A. 分类筛选**（30 分钟）
```vue
<view class="filter-tabs">
  <view
    v-for="category in categories"
    :key="category.value"
    class="filter-tab"
    :class="{ active: filters.category === category.value }"
    @click="handleCategoryChange(category.value)"
  >
    <text class="tab-icon">{{ category.icon }}</text>
    <text class="tab-label">{{ category.label }}</text>
  </view>
</view>
```

**B. 状态筛选**（30 分钟）
```vue
<view class="status-filters">
  <view
    class="status-tab"
    :class="{ active: filters.isSolved === null }"
    @click="filters.isSolved = null; loadQuestions(true)"
  >
    全部
  </view>
  <view
    class="status-tab"
    :class="{ active: filters.isSolved === false }"
    @click="filters.isSolved = false; loadQuestions(true)"
  >
    未解决
  </view>
  <view
    class="status-tab"
    :class="{ active: filters.isSolved === true }"
    @click="filters.isSolved = true; loadQuestions(true)"
  >
    已解决
  </view>
</view>
```

**C. 排序切换**（30 分钟）
```vue
<view class="sort-tabs">
  <view
    class="sort-tab"
    :class="{ active: filters.sortBy === 'created_at' }"
    @click="handleSortChange('created_at')"
  >
    最新
  </view>
  <view
    class="sort-tab"
    :class="{ active: filters.sortBy === 'views' }"
    @click="handleSortChange('views')"
  >
    最热
  </view>
  <view
    class="sort-tab"
    :class="{ active: filters.sortBy === 'rewardPoints' }"
    @click="handleSortChange('rewardPoints')"
  >
    悬赏
  </view>
</view>
```

**验收标准**：
- ✅ 分类筛选正常工作
- ✅ 状态筛选正常工作
- ✅ 排序切换正常工作
- ✅ 筛选后自动刷新列表

#### 2.2 实现搜索功能（1 小时）

**任务清单**：

**A. 搜索框 UI**（20 分钟）
```vue
<view class="search-box">
  <view class="search-icon">🔍</view>
  <input
    v-model="searchKeyword"
    class="search-input"
    placeholder="搜索问题标题或内容..."
    confirm-type="search"
    @confirm="handleSearch"
    @input="handleSearchInput"
  />
  <view v-if="searchKeyword" class="clear-icon" @click="clearSearch">
    ✕
  </view>
</view>
```

**B. 防抖处理**（20 分钟）
```typescript
const searchDebounce = ref<number | null>(null)

const handleSearchInput = () => {
  if (searchDebounce.value) {
    clearTimeout(searchDebounce.value)
  }

  searchDebounce.value = setTimeout(() => {
    filters.value.keyword = searchKeyword.value
    loadQuestions(true)
  }, 300) as unknown as number
}
```

**C. 搜索历史（可选）**（20 分钟）

**验收标准**：
- ✅ 搜索框显示正确
- ✅ 防抖生效
- ✅ 搜索结果正确

#### 2.3 实现问题列表（2.5 小时）

**任务清单**：

**A. QuestionCard 集成**（30 分钟）
```vue
<view class="question-list">
  <QuestionCard
    v-for="item in questions"
    :key="item.questionId"
    :question="item"
    @click="handleQuestionClick"
  />
</view>
```

**B. 分页加载**（1 小时）
- 下拉刷新（onPullDownRefresh）
- 上拉加载更多（onReachBottom）
- 加载状态管理

**C. 骨架屏**（30 分钟）
```vue
<view v-if="loading && questions.length === 0" class="skeleton-list">
  <SkeletonQuestionCard v-for="n in 5" :key="n" />
</view>
```

**D. 空状态**（30 分钟）
```vue
<EmptyState
  v-else-if="questions.length === 0"
  icon="📭"
  title="还没有问题哦"
  description="快来提出第一个问题吧！"
>
  <button class="ask-btn" @click="goToAsk">+ 发布问题</button>
</EmptyState>
```

**验收标准**：
- ✅ 问题列表显示正确
- ✅ 下拉刷新正常
- ✅ 上拉加载更多正常
- ✅ 骨架屏显示正确
- ✅ 空状态显示正确

#### 2.4 实现发布按钮（30 分钟）

**任务清单**：

**A. 悬浮按钮（FAB）**
```vue
<view class="ask-fab" @click="goToAsk">
  <view class="fab-icon">✏️</view>
</view>
```

**B. 样式设计**
```scss
.ask-fab {
  position: fixed;
  right: 32rpx;
  bottom: 140rpx;
  width: 88rpx;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #6366F1 0%, #4F46E5 100%);
  border-radius: 50%;
  box-shadow: 0 8rpx 24rpx rgba(99, 102, 241, 0.3);
  cursor: pointer;
  z-index: 999;
  animation: slideInUp 0.3s ease-out;

  &:active {
    transform: scale(0.95);
  }

  .fab-icon {
    font-size: 40rpx;
  }
}

@keyframes slideInUp {
  from {
    transform: translateY(100rpx);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
```

**验收标准**：
- ✅ FAB 显示正确
- ✅ 点击跳转发布页
- ✅ 动画效果流畅

#### 2.5 性能优化（30 分钟）

**任务清单**：
- [ ] 列表虚拟滚动（如果列表 >100）
- [ ] 图片懒加载
- [ ] 本地缓存（5 分钟）
- [ ] 防抖节流

**验收标准**：
- ✅ 滚动流畅（60fps）
- ✅ 图片按需加载
- ✅ 缓存命中率 >80%

---

### Phase 3: 问题详情页（Day 2 下午 + Day 3，8 小时）

**目标**：完成问题详情页的所有功能

#### 3.1 实现问题内容区（2 小时）

**任务清单**：

**A. 问题标题、内容、图片**（1 小时）
**B. 标签列表**（20 分钟）
**C. 用户信息、统计数据**（20 分钟）
**D. 状态标识**（20 分钟）

**验收标准**：
- ✅ 问题内容显示完整
- ✅ 图片预览正常
- ✅ 标签可点击筛选
- ✅ 统计数据准确

#### 3.2 实现操作按钮（1.5 小时）

**任务清单**：

**A. 点赞按钮**（30 分钟）
- 乐观更新
- 点赞动画（scale + 心形淡入）
- 错误回滚

**B. 分享按钮**（30 分钟）
- 生成链接
- 二维码（uni.previewImage）
- 复制链接

**C. 删除按钮**（30 分钟）
- 权限判断（仅提问者/管理员）
- 确认弹窗
- 删除成功跳转列表页

**验收标准**：
- ✅ 点赞功能正常
- ✅ 分享功能正常
- ✅ 删除功能正常

#### 3.3 实现回答列表（2.5 小时）

**任务清单**：

**A. AnswerCard 集成**（30 分钟）
**B. 已采纳答案置顶**（20 分钟）
**C. 排序切换**（点赞数/时间）（20 分钟）
**D. 点赞回答**（乐观更新）（30 分钟）
**E. 采纳回答**（确认弹窗）（30 分钟）
**F. 分页加载**（30 分钟）

**验收标准**：
- ✅ 回答列表显示正确
- ✅ 已采纳答案置顶
- ✅ 排序切换正常
- ✅ 点赞功能正常
- ✅ 采纳功能正常

#### 3.4 实现回答输入框（1.5 小时）

**任务清单**：

**A. 固定底部输入框**（30 分钟）
```vue
<view class="answer-input-bar">
  <textarea
    v-model="answerContent"
    class="answer-input"
    placeholder="写下你的回答..."
    maxlength="2000"
    auto-height
    @focus="handleInputFocus"
    @blur="handleInputBlur"
  />
  <view class="input-actions">
    <view class="image-upload-btn" @click="uploadImage">
      <text class="icon">📷</text>
    </view>
    <button
      class="submit-btn"
      :disabled="!canSubmit"
      @click="handleSubmitAnswer"
    >
      发布
    </button>
  </view>
  <view class="char-count">{{ answerContent.length }}/2000</view>
</view>
```

**B. 图片上传**（30 分钟）
**C. 字符计数**（10 分钟）
**D. 草稿保存**（20 分钟）
**E. 发布回答**（20 分钟）

**验收标准**：
- ✅ 输入框固定底部
- ✅ 图片上传正常
- ✅ 字符计数正确
- ✅ 草稿保存正常
- ✅ 发布成功刷新列表

#### 3.5 错误处理（30 分钟）

**任务清单**：
- [ ] 问题不存在（404）
- [ ] 权限不足（403）
- [ ] 网络错误（超时、断网）

**验收标准**：
- ✅ 所有错误有友好提示
- ✅ 网络错误可重试

---

### Phase 4: 发布问题页（Day 4，6 小时）

**目标**：完成发布问题页的所有功能

#### 4.1 实现表单输入（2.5 小时）

**任务清单**：

**A. 标题输入**（20 分钟）
**B. 内容输入**（20 分钟）
**C. 分类选择**（卡片式）（30 分钟）
**D. 标签输入**（TagInput 组件）（30 分钟）
**E. 图片上传**（9 宫格）（30 分钟）
**F. 悬赏设置**（滑块 0-100 分）（30 分钟）

**验收标准**：
- ✅ 所有表单字段正常工作
- ✅ 图片上传正常
- ✅ 悬赏设置正确

#### 4.2 实现表单验证（1 小时）

**任务清单**：

**A. 必填校验**（20 分钟）
**B. 长度校验**（20 分钟）
**C. 积分余额校验**（10 分钟）
**D. 实时错误提示**（10 分钟）

**验收标准**：
- ✅ 所有校验规则正确
- ✅ 错误提示友好

#### 4.3 实现草稿功能（1 小时）

**任务清单**：

**A. 自动保存**（30 秒）（20 分钟）
**B. 恢复草稿**（提示弹窗）（20 分钟）
**C. 清空草稿**（20 分钟）

**验收标准**：
- ✅ 自动保存正常
- ✅ 恢复草稿正常
- ✅ 清空草稿正常

#### 4.4 实现发布功能（1 小时）

**任务清单**：

**A. 提交数据**（20 分钟）
**B. 加载状态**（10 分钟）
**C. 成功跳转详情页**（10 分钟）
**D. 失败显示错误**（20 分钟）

**验收标准**：
- ✅ 发布成功跳转详情页
- ✅ 发布失败显示错误
- ✅ 加载状态显示正确

---

### Phase 5: 我的问答页（Day 5 上午，3 小时）

**目标**：完成我的问答页的所有功能

#### 5.1 实现 Tab 切换（1 小时）

**任务清单**：

**A. Tab 切换 UI**（30 分钟）
**B. 每个 Tab 独立分页**（30 分钟）

**验收标准**：
- ✅ Tab 切换正常
- ✅ 每个 Tab 独立加载数据

#### 5.2 实现问题列表（1 小时）

**任务清单**：

**A. 复用 QuestionCard**（20 分钟）
**B. 下拉刷新 + 上拉加载更多**（20 分钟）
**C. 空状态**（20 分钟）

**验收标准**：
- ✅ 问题列表显示正确
- ✅ 刷新加载正常
- ✅ 空状态显示正确

#### 5.3 实现积分卡片（1 小时）

**任务清单**：

**A. 当前积分**（20 分钟）
**B. 本周获得/消耗**（20 分钟）
**C. 查看详情按钮**（20 分钟）

**验收标准**：
- ✅ 积分卡片显示正确
- ✅ 查看详情正常跳转

---

### Phase 6: 测试与优化（Day 5 下午，3 小时）

**目标**：完成所有测试和优化

#### 6.1 功能测试（1 小时）

**测试清单**：
- [ ] 发布问题流程
- [ ] 回答问题流程
- [ ] 采纳答案流程
- [ ] 点赞功能
- [ ] 分享功能
- [ ] 搜索功能
- [ ] 筛选功能

**验收标准**：
- ✅ 所有功能正常工作
- ✅ 无严重 Bug

#### 6.2 性能测试（30 分钟）

**测试清单**：
- [ ] 列表滚动流畅度
- [ ] 图片加载速度
- [ ] 网络请求优化
- [ ] 首屏加载时间

**验收标准**：
- ✅ 滚动流畅（60fps）
- ✅ 首屏加载 <2s
- ✅ 图片懒加载正常

#### 6.3 兼容性测试（30 分钟）

**测试清单**：
- [ ] H5 端测试
- [ ] 微信小程序端测试
- [ ] PC 端响应式测试

**验收标准**：
- ✅ H5 端正常
- ✅ 微信小程序正常
- ✅ PC 端响应式正常

#### 6.4 Bug 修复（30 分钟）

**验收标准**：
- ✅ 所有 Bug 修复

#### 6.5 代码优化（30 分钟）

**优化清单**：
- [ ] 代码重构
- [ ] 性能优化
- [ ] 文档完善

**验收标准**：
- ✅ 代码质量 A
- ✅ 文档完整

---

## 📈 进度跟踪

### 每日检查点

**Day 1**：
- ✅ 完成基础框架搭建
- ✅ 完成问题列表页 50%

**Day 2**：
- ✅ 完成问题列表页 100%
- ✅ 完成问题详情页 50%

**Day 3**：
- ✅ 完成问题详情页 100%

**Day 4**：
- ✅ 完成发布问题页 100%

**Day 5**：
- ✅ 完成我的问答页 100%
- ✅ 完成测试与优化

### 风险管理

| 风险 | 概率 | 影响 | 缓解措施 |
|------|------|------|---------|
| API 接口延迟 | 低 | 高 | 提前测试 API，确保后端就绪 |
| 性能问题 | 中 | 中 | 使用虚拟滚动、图片懒加载 |
| 兼容性问题 | 低 | 中 | 早期多端测试 |
| 需求变更 | 中 | 高 | 严格控制需求，MVP 阶段锁定功能 |

---

## 📚 资源清单

### 开发工具

- VS Code
- 微信开发者工具
- Chrome DevTools
- Postman（API 测试）

### 技术文档

- [uni-app 官方文档](https://uniapp.dcloud.net.cn/)
- [Vue 3 文档](https://vuejs.org/)
- [Pinia 文档](https://pinia.vuejs.org/)
- [TypeScript 文档](https://www.typescriptlang.org/)

### 设计资源

- Figma 设计稿
- 图标库（uni-app 内置图标）
- 配色方案（Tailwind CSS 色板）

---

**文档结束**

**下一步行动**：
1. ✅ 评审开发计划
2. ⏳ 开始 Phase 1 开发（基础框架搭建）
