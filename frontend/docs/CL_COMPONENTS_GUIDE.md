# CampusLink 企业级组件库使用指南

## 📦 组件概览

CampusLink 企业级组件库基于企业级内容平台标准打造，提供统一的设计系统和组件规范。

### 组件清单

**基础组件**：
- `ClTag` - 统一标签组件
- `ClMetaRow` - 统一元数据行
- `ClActionBar` - 统一操作按钮区
- `ClAvatar` - 统一头像组件

**卡片组件**：
- `ClFeaturedQAItem` - 精选推荐问答卡片
- `ClFeedQAItem` - 最新问答流卡片
- `ClResourceCard` - 精选资料卡片
- `ClEventCard` - 社团活动卡片

---

## 🎨 Design Tokens（设计变量）

所有组件基于统一的 Design Tokens 构建：

```scss
// 导入 Design Tokens
@import '@/styles/design-tokens.scss';

// 使用变量
.my-component {
  color: $campus-blue;           // 品牌色 #377DFF
  padding: $spacing-card-padding; // 卡片内边距
  border-radius: $radius-card;    // 卡片圆角
  box-shadow: $shadow-card;       // 卡片阴影
}
```

**核心变量**：
- **品牌色**: `$campus-blue` (#377DFF)
- **功能色**: `$color-success`, `$color-warning`, `$color-danger`
- **间距**: `$spacing-*` (基于 8px 栅格)
- **圆角**: `$radius-*`
- **阴影**: `$shadow-*`

---

## 📚 组件使用示例

### 1. ClTag - 标签组件

```vue
<script setup>
import { ClTag } from '@/components/cl'
</script>

<template>
  <!-- 基础用法 -->
  <ClTag text="已解决" type="success" />

  <!-- 不同类型 -->
  <ClTag text="待回答" type="primary" />
  <ClTag text="热门" type="danger" />
  <ClTag text="警告" type="warning" />

  <!-- 不同尺寸 -->
  <ClTag text="小标签" size="small" />
  <ClTag text="中等标签" size="medium" />
  <ClTag text="大标签" size="large" />

  <!-- 可点击 -->
  <ClTag text="点我" clickable @click="handleClick" />

  <!-- 可关闭 -->
  <ClTag text="可关闭" closable @close="handleClose" />
</template>
```

**Props**：
| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| text | string | - | 标签文本（必填） |
| type | string | 'default' | 类型：default/primary/success/warning/danger/info |
| size | string | 'medium' | 尺寸：small/medium/large |
| clickable | boolean | false | 是否可点击 |
| closable | boolean | false | 是否可关闭 |

---

### 2. ClMetaRow - 元数据行组件

```vue
<script setup>
import { ClMetaRow, type MetaItem } from '@/components/cl'
import { ref } from 'vue'

const metaItems = ref<MetaItem[]>([
  { icon: 'icon-eye', text: '1.2k' },
  { icon: 'icon-message', text: '32', clickable: true },
  { icon: 'icon-heart', text: '256', clickable: true }
])

const handleMetaClick = (item: MetaItem, index: number) => {
  console.log('点击了:', item, index)
}
</script>

<template>
  <ClMetaRow :items="metaItems" @click="handleMetaClick" />

  <!-- 右对齐 -->
  <ClMetaRow :items="metaItems" align-right />
</template>
```

**Props**：
| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| items | MetaItem[] | - | 元数据项列表（必填） |
| alignRight | boolean | false | 是否右对齐 |

**MetaItem 类型**：
```typescript
interface MetaItem {
  icon?: string       // 图标类名
  text: string | number  // 显示文本
  clickable?: boolean    // 是否可点击
  data?: any            // 自定义数据
}
```

---

### 3. ClActionBar - 操作按钮区组件

```vue
<script setup>
import { ClActionBar, type Action } from '@/components/cl'
import { ref } from 'vue'

const actions = ref<Action[]>([
  { text: '回答问题', type: 'primary', icon: 'icon-edit' },
  { text: '查看详情', type: 'secondary' }
])

const handleActionClick = (action: Action, index: number) => {
  console.log('点击了:', action, index)
}
</script>

<template>
  <ClActionBar :actions="actions" @click="handleActionClick" />

  <!-- 左对齐 -->
  <ClActionBar :actions="actions" align="left" />

  <!-- 居中 -->
  <ClActionBar :actions="actions" align="center" />
</template>
```

**Props**：
| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| actions | Action[] | - | 操作按钮列表（必填） |
| align | string | 'right' | 对齐方式：left/center/right |

**Action 类型**：
```typescript
interface Action {
  text?: string       // 按钮文本
  type?: string       // 类型：primary/secondary/ghost/text/default
  icon?: string       // 图标类名
  disabled?: boolean  // 是否禁用
  data?: any          // 自定义数据
}
```

---

### 4. ClAvatar - 头像组件

```vue
<script setup>
import { ClAvatar } from '@/components/cl'
</script>

<template>
  <!-- 基础用法 -->
  <ClAvatar src="https://..." />

  <!-- 不同尺寸 -->
  <ClAvatar src="https://..." size="small" />
  <ClAvatar src="https://..." size="medium" />
  <ClAvatar src="https://..." size="large" />
  <ClAvatar src="https://..." size="xlarge" />

  <!-- 方形 -->
  <ClAvatar src="https://..." shape="square" />

  <!-- 显示认证徽章 -->
  <ClAvatar src="https://..." :verified="true" />

  <!-- 显示在线状态 -->
  <ClAvatar src="https://..." show-online :online="true" />

  <!-- 占位符（无图片时） -->
  <ClAvatar name="张三" />

  <!-- 可点击 -->
  <ClAvatar src="https://..." clickable @click="handleClick" />
</template>
```

**Props**：
| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| src | string | - | 头像图片地址 |
| name | string | - | 用户名称（用于生成占位符） |
| size | string | 'medium' | 尺寸：small/medium/large/xlarge |
| shape | string | 'circle' | 形状：circle/square |
| verified | boolean | false | 是否显示认证徽章 |
| showOnline | boolean | false | 是否显示在线状态 |
| online | boolean | false | 是否在线 |
| clickable | boolean | false | 是否可点击 |

---

## 🎴 卡片组件使用示例

### 1. ClFeaturedQAItem - 精选推荐问答卡片

```vue
<script setup>
import { ClFeaturedQAItem } from '@/components/cl'
import { ref } from 'vue'

const question = ref({
  id: 1,
  title: '如何高效学习前端开发？',
  description: '我是一名大二学生，对前端开发很感兴趣...',
  user: {
    id: 1,
    username: '张三',
    avatar: 'https://...',
    role: 'student',
    verified: true
  },
  tags: ['前端', '学习方法', 'Vue'],
  views: 1234,
  comments: 32,
  likes: 256,
  createdAt: '2025-12-07T10:00:00Z',
  isHot: true,
  reason: '根据你的浏览记录推荐',
  rewardPoints: 50
})

const handleAnswer = (q) => {
  console.log('回答问题:', q)
}
</script>

<template>
  <ClFeaturedQAItem
    :question="question"
    @click="handleQuestionClick"
    @answer="handleAnswer"
    @user-click="handleUserClick"
  />
</template>
```

---

### 2. ClFeedQAItem - 最新问答流卡片

```vue
<script setup>
import { ClFeedQAItem } from '@/components/cl'

const question = ref({
  id: 2,
  title: 'Vue 3 Composition API 的最佳实践？',
  user: {
    id: 2,
    username: '李四',
    avatar: 'https://...'
  },
  tags: ['Vue3', 'Composition API'],
  views: 567,
  comments: 12,
  likes: 45,
  createdAt: '2025-12-07T12:00:00Z',
  isSolved: false,
  rewardPoints: 30
})
</script>

<template>
  <ClFeedQAItem
    :question="question"
    @click="handleQuestionClick"
  />
</template>
```

---

### 3. ClResourceCard - 精选资料卡片

```vue
<script setup>
import { ClResourceCard } from '@/components/cl'

const resource = ref({
  id: 1,
  title: '前端开发完整教程 2025版',
  description: '包含 HTML、CSS、JavaScript、Vue 3 等完整知识体系',
  fileType: 'pdf',
  tags: ['前端', '教程', 'Vue'],
  downloads: 1234,
  rating: 4.8,
  createdAt: '2025-11-15T10:00:00Z',
  points: 10
})

const handleDownload = (res) => {
  console.log('下载资源:', res)
}
</script>

<template>
  <ClResourceCard
    :resource="resource"
    @click="handleResourceClick"
    @download="handleDownload"
  />
</template>
```

---

### 4. ClEventCard - 社团活动卡片

```vue
<script setup>
import { ClEventCard } from '@/components/cl'

const event = ref({
  id: 1,
  title: '前端技术分享会',
  organizer: '计算机协会',
  type: 'lecture',
  startTime: '2025-12-15T14:00:00Z',
  location: '图书馆报告厅',
  participants: 128,
  views: 567,
  isEnded: false,
  isRegistering: true
})

const handleRegister = (evt) => {
  console.log('报名活动:', evt)
}
</script>

<template>
  <ClEventCard
    :event="event"
    @click="handleEventClick"
    @register="handleRegister"
  />
</template>
```

---

## 🔧 统一导入方式

```typescript
// 方式 1：按需导入
import { ClTag, ClMetaRow, ClAvatar } from '@/components/cl'

// 方式 2：导入类型
import type { MetaItem, Action } from '@/components/cl'

// 方式 3：导入所有组件
import * as ClComponents from '@/components/cl'
```

---

## 📐 布局规范

所有卡片遵循统一的三段式结构：

```
┌─────────────────────────────┐
│ Header（用户/图标/状态）     │
├─────────────────────────────┤
│ Body（标题/描述/标签）       │
├─────────────────────────────┤
│ Meta（浏览/点赞/评论/时间）  │
├─────────────────────────────┤
│ Action（操作按钮）           │
└─────────────────────────────┘
```

**间距规范**：
- 左侧主内容对齐线：24px
- 右侧操作按钮边距：20px
- 模块间距：16px
- 卡片内边距：12px

---

## 🎯 最佳实践

### 1. 颜色使用
```scss
// ✅ 正确：使用 Design Tokens
color: $campus-blue;

// ❌ 错误：硬编码颜色
color: #377DFF;
```

### 2. 间距使用
```scss
// ✅ 正确：使用间距变量
padding: $spacing-card-padding;

// ❌ 错误：硬编码像素值
padding: 12px;
```

### 3. 组件组合
```vue
<!-- ✅ 正确：组合基础组件构建复杂UI -->
<ClMetaRow :items="metaItems" />
<ClActionBar :actions="actions" />

<!-- ❌ 错误：重复实现相同功能 -->
<view class="custom-meta-row">...</view>
```

---

## 📝 注意事项

1. **图标系统**：项目使用 Iconify，图标类名格式为 `icon-*`
2. **响应式**：所有组件支持 H5/小程序/App 多端
3. **类型安全**：建议使用 TypeScript，导入类型定义
4. **性能优化**：大列表使用虚拟滚动，避免一次性渲染过多卡片

---

## 🆕 版本更新

### v1.0.0 (2025-12-07)
- ✅ 创建 Design Tokens 系统
- ✅ 实现 4 个基础组件
- ✅ 实现 4 个卡片组件
- ✅ 统一品牌色为 #377DFF

---

## 🤝 贡献指南

添加新组件时请遵循以下规范：
1. 组件命名使用 `Cl` 前缀（CampusLink）
2. 使用 Design Tokens 而非硬编码
3. 提供完整的 TypeScript 类型定义
4. 添加使用示例到本文档

---

**CampusLink Design Team**
*企业级组件库 v1.0.0*
