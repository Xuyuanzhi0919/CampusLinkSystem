# HotQuestions 组件使用文档

可复用的热门问题列表组件,支持排名徽章、数据展示、点击交互等功能。

## 功能特性

- ✅ **视觉层级优化**:Top1 头条感强化(字重+间距+徽章尺寸)
- ✅ **热度可视化**:浏览量下方显示渐变热度条(基于百分比)
- ✅ **排名徽章**:前三名渐变高亮,Top4-5 弱化处理
- ✅ **明确交互状态**:Hover 浅灰背景+左移,Active 主色左边框
- ✅ **数据展示**:浏览量、回答数、悬赏积分等元信息
- ✅ **数字格式化**:自动格式化大数字(k/w)
- ✅ **点击交互**:支持问题点击和查看更多
- ✅ **空状态**:无数据时显示友好提示
- ✅ **类型安全**:完整的 TypeScript 类型定义

## 基础用法

```vue
<script setup lang="ts">
import HotQuestions from '@/components/HotQuestions.vue'
import type { HotQuestionItem } from '@/components/HotQuestions.vue'

const questions = ref<HotQuestionItem[]>([
  { qid: 1, title: 'Java多线程如何实现?', views: 1245, answerCount: 23, bounty: 50 },
  { qid: 2, title: '数据结构常见算法总结', views: 1089, answerCount: 18 },
  { qid: 3, title: 'MySQL性能优化技巧', views: 987, answerCount: 15 }
])

const handleQuestionClick = (question: HotQuestionItem) => {
  console.log('点击问题:', question.title)
  uni.navigateTo({ url: `/pages/question/detail?id=${question.qid}` })
}
</script>

<template>
  <HotQuestions
    :questions="questions"
    title="热门问题"
    @question-click="handleQuestionClick"
  />
</template>
```

## Props 参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `questions` | `HotQuestionItem[]` | `[]` | **必需**。问题数据数组 |
| `title` | `string` | `'热门问题'` | 标题文字 |
| `headerIcon` | `string` | `'trending-up'` | 标题图标名称(Icon组件) |
| `showHeader` | `boolean` | `true` | 是否显示标题栏 |
| `showBadge` | `boolean` | `false` | 是否显示问题总数徽章 |
| `showViewMore` | `boolean` | `false` | 是否显示"查看更多"按钮 |
| `showBounty` | `boolean` | `true` | 是否显示悬赏积分 |
| `maxDisplay` | `number` | `5` | 最大显示数量 |
| `emptyText` | `string` | `'暂无热门问题'` | 空状态提示文字 |

## 事件

| 事件名 | 参数 | 说明 |
|--------|------|------|
| `question-click` | `question: HotQuestionItem` | 点击问题时触发,参数为完整的问题对象 |
| `view-more` | - | 点击"查看更多"按钮时触发 |

## 类型定义

```typescript
export interface HotQuestionItem {
  qid: number          // 问题ID
  title: string        // 问题标题
  views: number        // 浏览量
  answerCount: number  // 回答数
  bounty?: number      // 悬赏积分(可选)
}
```

## 使用场景示例

### 场景1:问答社区右侧栏(默认配置)

适用于展示热门问题,简洁干净,不显示悬赏信息。

```vue
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
  @question-click="handleQuestionClick"
/>
```

**效果**:
- 前3名显示金/银/铜渐变徽章
- 显示浏览量和回答数
- 不显示悬赏积分
- 最多显示5个问题

### 场景2:首页推荐模块(显示悬赏+查看更多)

适用于首页展示,显示悬赏信息吸引用户,支持查看更多。

```vue
<HotQuestions
  :questions="recommendedQuestions"
  title="推荐问题"
  header-icon="star"
  :show-header="true"
  :show-badge="true"
  :show-view-more="true"
  :show-bounty="true"
  :max-display="3"
  empty-text="暂无推荐问题"
  @question-click="handleQuestionClick"
  @view-more="handleViewMore"
/>
```

**效果**:
- 显示标题栏+问题总数徽章
- 显示悬赏积分(橙色高亮)
- 最多显示3个,超出显示"查看更多"按钮
- 支持查看更多操作

### 场景3:用户个人中心(无标题栏)

适用于用户个人中心的"我的提问",简洁展示。

```vue
<HotQuestions
  :questions="myQuestions"
  :show-header="false"
  :show-bounty="false"
  :max-display="10"
  empty-text="您还没有提问哦"
  @question-click="handleQuestionClick"
/>
```

**效果**:
- 不显示标题栏
- 不显示悬赏信息
- 最多显示10个问题

## 数据来源方式

### 方式1:从 API 获取(推荐)

```typescript
import { getQuestionList } from '@/services/question'

const questions = ref<HotQuestionItem[]>([])

const loadHotQuestions = async () => {
  try {
    const res = await getQuestionList({
      page: 1,
      pageSize: 5,
      sortBy: 'views',
      sortOrder: 'desc'
    })
    questions.value = res.list.map(q => ({
      qid: q.qid,
      title: q.title,
      views: q.views,
      answerCount: q.answerCount,
      bounty: q.bounty  // 如果后端返回悬赏字段
    }))
  } catch (error) {
    console.error('加载热门问题失败:', error)
    questions.value = []
  }
}

onMounted(() => {
  loadHotQuestions()
})
```

### 方式2:从现有数据统计

```typescript
import type { QuestionItem } from '@/types/question'

const allQuestions = ref<QuestionItem[]>([])
const hotQuestions = ref<HotQuestionItem[]>([])

// 从问题列表中提取热门问题(按浏览量排序)
const calculateHotQuestions = () => {
  hotQuestions.value = [...allQuestions.value]
    .sort((a, b) => b.views - a.views)
    .slice(0, 5)
    .map(q => ({
      qid: q.qid,
      title: q.title,
      views: q.views,
      answerCount: q.answerCount,
      bounty: q.bounty
    }))
}

// 每次问题列表更新时重新计算
watch(allQuestions, () => {
  calculateHotQuestions()
})
```

### 方式3:静态数据(测试用)

```typescript
const questions = ref<HotQuestionItem[]>([
  { qid: 1, title: 'Java多线程如何实现?', views: 1245, answerCount: 23, bounty: 50 },
  { qid: 2, title: '数据结构常见算法总结', views: 1089, answerCount: 18 },
  { qid: 3, title: 'MySQL性能优化技巧', views: 987, answerCount: 15 }
])
```

## 样式自定义

组件使用了全局样式变量(`@/styles/variables.scss`),可通过修改以下变量自定义样式:

```scss
$primary        // 主色(图标、悬赏信息)
$accent         // 强调色(悬赏积分)
$white          // 白色(背景、文字)
$gray-50        // 浅灰(问题项hover背景)
$gray-200       // 灰色(默认徽章背景)
$gray-400       // 图标颜色
$gray-500       // 次要文字
$gray-600       // 默认徽章文字
$gray-900       // 标题文字
```

如需深度自定义,可以使用 `:deep()` 选择器覆盖组件内部样式:

```vue
<style scoped lang="scss">
:deep(.question-item) {
  padding: 16rpx;  // 修改问题项内边距
  border-radius: 12rpx;  // 修改圆角
}

:deep(.rank-badge.rank-1) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);  // 自定义第1名渐变色
}

:deep(.question-title) {
  font-size: 28rpx;  // 修改标题字号
  color: #333;  // 修改标题颜色
}
</style>
```

## 视觉层级设计

### 排名徽章
- **Top1**: 金色渐变 `#FFD700 → #FF8C00` (44×44rpx, 字重800, 更强阴影)
- **Top2**: 银色渐变 `#C0C0C0 → #A8A8A8` (40×40rpx)
- **Top3**: 铜色渐变 `#CD7F32 → #B8722B` (40×40rpx)
- **Top4-5**: 浅灰弱化 `$gray-100` (40×40rpx, 字重600, 浅色文字)

### 标题层级
- **Top1**: 字重 600, 行高 1.5(呼吸感更强)
- **Top2-3**: 字重 500, 行高 1.4
- **Top4-5**: 字重 400, 颜色 `$gray-700`(弱化)

### 热度可视化
- 浏览量下方显示 3rpx 高度渐变条
- 宽度基于浏览量百分比动态计算(最小 5%)
- Hover 时高度增至 4rpx,颜色更饱满

### 交互状态
- **Hover**: 浅灰背景 + 向左移动 2rpx + 轻微阴影
- **Active**: 左侧 4rpx 主色边框 + 主色半透明背景

## 注意事项

1. **性能优化**:
   - 问题数量较多时(>10),建议设置 `maxDisplay` 限制显示数量
   - 使用 `showViewMore` 提供"查看更多"功能

2. **类型安全**:
   - 务必导入 `HotQuestionItem` 类型:`import type { HotQuestionItem } from '@/components/HotQuestions.vue'`
   - 确保传入的 `questions` 数组符合 `HotQuestionItem[]` 接口

3. **数字格式化**:
   - 浏览量/回答数 >= 10000 时自动格式化为 `x.xw`
   - >= 1000 时格式化为 `x.xk`
   - 确保传入的数字为 `number` 类型

4. **空状态处理**:
   - 当 `questions` 为空数组时,会自动显示空状态
   - 可通过 `emptyText` 自定义空状态文案

5. **事件参数**:
   - `question-click` 事件传递完整的 `HotQuestionItem` 对象,可直接访问 `qid`、`title` 等属性
   - 建议在点击处理函数中使用 `question.qid` 进行路由跳转

## 完整示例

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import HotQuestions from '@/components/HotQuestions.vue'
import type { HotQuestionItem } from '@/components/HotQuestions.vue'
import { getQuestionList } from '@/services/question'

const hotQuestions = ref<HotQuestionItem[]>([])

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
      answerCount: q.answerCount,
      bounty: q.bounty
    }))
  } catch (error) {
    console.error('[HotQuestions] 加载失败:', error)
    hotQuestions.value = []
  }
}

// 点击问题
const handleQuestionClick = (question: HotQuestionItem) => {
  console.log('点击问题:', question.title)
  uni.navigateTo({ url: `/pages/question/detail?id=${question.qid}` })
}

// 查看更多
const handleViewMore = () => {
  console.log('查看更多热门问题')
  uni.navigateTo({ url: '/pages/question/index?tab=hot' })
}

onMounted(() => {
  loadHotQuestions()
})
</script>

<template>
  <view class="sidebar">
    <view class="sidebar-card">
      <HotQuestions
        :questions="hotQuestions"
        title="热门问题"
        header-icon="trending-up"
        :show-header="true"
        :show-badge="false"
        :show-view-more="true"
        :show-bounty="true"
        :max-display="5"
        empty-text="暂无热门问题"
        @question-click="handleQuestionClick"
        @view-more="handleViewMore"
      />
    </view>
  </view>
</template>

<style scoped lang="scss">
.sidebar {
  width: 320px;
}

.sidebar-card {
  padding: 20rpx;
  background: white;
  border-radius: 12rpx;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
</style>
```

## 更新日志

### v1.1.0 (2025-01-XX) - 视觉层级优化版
- 🎨 **新增**:热度可视化条(浏览量下方渐变进度条)
- 🎨 **优化**:Top1 头条感强化(字重+间距+徽章尺寸)
- 🎨 **优化**:Top4-5 弱化处理(浅灰背景+减轻字重)
- 🎨 **优化**:明确交互状态(Hover 左移 + Active 左边框)
- 🎨 **优化**:列表节奏增强(Top1 额外呼吸感)
- ⚡ **性能**:热度条宽度动态计算(基于浏览量百分比)

### v1.0.0 (2025-01-XX)
- ✨ 初始版本发布
- ✅ 支持排名徽章、数据展示、点击交互
- ✅ 完整的 TypeScript 类型定义
- ✅ 问答社区右侧栏已接入使用

---

**设计理念**:
> 基于专业 UX 评审反馈优化,在保持"辅助内容"定位的同时,强化热度感知和视觉节奏,从 80 分提升到 85-90 分产品完成度。
