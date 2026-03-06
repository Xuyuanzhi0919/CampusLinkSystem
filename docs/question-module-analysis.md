# 问答中心模块全面分析报告

**分析时间**: 2025-11-19
**分析范围**: 所有问答相关页面和组件
**目标**: 找出不足、错误和需要优化的地方

---

## 一、问题列表页 (index.vue)

### ✅ 功能完整性
- [x] 搜索功能（带历史记录）
- [x] 分类筛选
- [x] 排序功能
- [x] 状态筛选（已解决/未解决）
- [x] 下拉刷新
- [x] 上拉加载更多
- [x] 骨架屏
- [x] 空状态

### ❌ 发现的问题

#### 1. **排序选项与后端字段不匹配**
```typescript
// Line 181-186
const sortOptions = [
  { label: '最新', value: 'created_at' },
  { label: '最热', value: 'views' },
  { label: '悬赏', value: 'rewardPoints' },  // ❌ 错误：后端字段是 bounty
  { label: '回答', value: 'answerCount' }
]
```
**问题**: `rewardPoints` 应该改为 `bounty`

#### 2. **状态筛选逻辑混乱**
```typescript
// Line 161
const isSolved = ref<boolean | null>(null)
```
**问题**: 后端使用的是 `status: number (0/1)`，而不是 `isSolved: boolean`

#### 3. **重复的页面状态管理**
```typescript
// Line 165-168
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
```
**问题**: 这些状态在 store 中已有，造成冗余

#### 4. **缺少加载防抖**
在快速切换分类或排序时，会发起多次请求

#### 5. **搜索防抖时间过短**
```typescript
// Line 254
searchDebounce = setTimeout(() => { ... }, 300)
```
**问题**: 300ms 对于中文输入法不够友好，建议 500-800ms

---

## 二、问题详情页 (detail.vue)

### ✅ 功能完整性
- [x] 问题详情展示
- [x] 回答列表
- [x] 点赞功能
- [x] 采纳功能
- [x] 删除功能
- [x] 发布回答
- [x] 图片预览
- [x] AI答案展示

### ❌ 发现的问题

#### 1. **回答列表数据兼容处理不优雅**
```typescript
// Line 318-328
const list = Array.isArray(res) ? res : (res.list || [])
const total = Array.isArray(res) ? res.length : (res.total || 0)
```
**问题**: 临时性兼容代码，应该统一后端返回格式

#### 2. **缺少错误边界处理**
当问题不存在或被删除时，没有友好的错误提示页面

#### 3. **回答排序逻辑可能有问题**
```typescript
// Line 145-156
const sortedAnswers = computed(() => {
  const sorted = [...answers.value]

  // 已采纳的回答总是排在最前
  sorted.sort((a, b) => {
    if (a.isAccepted && !b.isAccepted) return -1
    if (!a.isAccepted && b.isAccepted) return 1
    // 其他排序...
  })
})
```
**问题**: 排序选项切换时，已采纳回答始终在前，但可能用户想按时间或点赞数查看

#### 4. **图片预览没有错误处理**
```typescript
// Line 581
const handlePreviewImage = (index: number) => {
  uni.previewImage({
    urls: question.value?.images || [],
    current: index
  })
}
```
**问题**: 如果图片加载失败，没有fallback

#### 5. **回复输入框固定在底部可能遮挡内容**
在某些设备上，固定底栏可能遮挡最后一条回答

#### 6. **乐观更新没有回滚机制**
```typescript
// Line 442 点赞逻辑
answer.likes += answer.isLiked ? -1 : 1
answer.isLiked = !answer.isLiked
```
**问题**: 如果API调用失败，状态没有回滚

---

## 三、发布问题页 (ask.vue)

### ✅ 功能完整性
- [x] 标题输入（字数统计）
- [x] 内容输入（字数统计）
- [x] 分类选择
- [x] 标签输入（推荐标签）
- [x] 图片上传（占位）
- [x] 悬赏设置
- [x] 表单校验
- [x] 取消确认

### ❌ 发现的问题

#### 1. **图片上传功能未实现**
```typescript
// Line 319-327
handleChooseImage = () => {
  // TODO: 上传到OSS
  // 这里先用本地路径模拟
  formData.value.images.push(...res.tempFilePaths)

  uni.showToast({
    title: '图片上传功能待实现',
    icon: 'none'
  })
}
```
**问题**: 核心功能缺失

#### 2. **悬赏积分验证时机不对**
```typescript
// Line 341-349
const handleRewardSelect = (points: number) => {
  if (points > 0 && points > userPoints.value) {
    uni.showToast({ title: '积分不足', icon: 'none' })
    return
  }
  formData.value.rewardPoints = points
}
```
**问题**: 应该在选择时就禁用，而不是点击后才提示

#### 3. **标签输入没有过滤非法字符**
允许用户输入特殊字符，可能导致显示问题

#### 4. **表单校验不够严格**
```typescript
// Line 239-246
const canSubmit = computed(() => {
  return (
    formData.value.title.trim().length >= 5 &&
    formData.value.content.trim().length >= 10 &&
    formData.value.category !== '' &&
    !submitting.value
  )
})
```
**问题**:
- 标题可以是5个空格
- 没有检查标题和内容是否包含敏感词
- 没有限制标题的最大长度（只在textarea有maxlength）

#### 5. **推荐标签是硬编码的**
```typescript
// Line 224-231
const recommendTags = computed(() => {
  const tagMap: Record<string, string[]> = {
    '学习': ['考研', '课程', '复习', '笔记', '资料'],
    // ...
  }
})
```
**问题**: 应该从后端获取热门标签

#### 6. **没有草稿保存功能**
用户输入一半退出后，内容丢失

---

## 四、我的问答页 (my.vue)

### ✅ 功能完整性
- [x] Tab切换（提问/回答）
- [x] 状态筛选（仅提问）
- [x] 列表展示
- [x] 空状态
- [x] 下拉刷新
- [x] 上拉加载

### ❌ 发现的问题

#### 1. **API接口可能不存在**
```typescript
// Line 183-195
const res = await getMyQuestions({
  page: page.value,
  pageSize: pageSize.value,
  status: statusFilter.value === null ? undefined : statusFilter.value
})
```
**问题**: `getMyQuestions` 和 `getMyAnswers` 是新增的API，后端可能没有实现

#### 2. **Tab切换时没有缓存数据**
每次切换都重新加载，体验不好

#### 3. **状态筛选又是用的 status**
```typescript
// Line 148-152
const statusFilters = [
  { label: '全部', value: null },
  { label: '未解决', value: 0 },
  { label: '已解决', value: 1 }
]
```
**问题**: 与列表页的 `isSolved` 不一致

#### 4. **回答卡片的 question-icon 显示问题**
```typescript
// Line 459-471 SCSS
.question-icon {
  width: 36rpx;
  height: 36rpx;
  display: flex;  // ❌ text 元素不能用 flex
}
```

---

## 五、榜单页 (ranking.vue)

### ✅ 功能完整性
- [x] 热门榜单
- [x] 排名显示
- [x] 点击跳转

### ❌ 发现的问题

#### 1. **排序参数已修复，但缺少时间范围**
没有日榜/周榜/月榜的选择

#### 2. **热度值是假的**
```typescript
// Line 92
hot: Math.max(100 - index * 5, 10)
```
**问题**: 应该从后端获取真实热度

#### 3. **缺少骨架屏组件**
```typescript
// Line 18
<SkeletonCard v-for="i in 10" :key="i" type="question" />
```
**问题**: `SkeletonCard` 组件可能不存在

---

## 六、QuestionCard 组件

### ✅ 功能完整性
- [x] 问题信息展示
- [x] 分类标签
- [x] 状态标签
- [x] 统计数据
- [x] 时间格式化

### ❌ 发现的问题

#### 1. **数字格式化函数重复**
`formatNumber` 和 `formatTime` 在多个页面重复定义，应该提取到 utils

#### 2. **时间格式化可能有Bug**
```typescript
// Line 127-149
const formatTime = (timeStr: string): string => {
  const time = new Date(timeStr).getTime()
  const now = Date.now()
  const diff = now - time
  // ...
  if (diff < month) {
    return Math.floor(diff / day) + '天前'
  } else {
    const date = new Date(time)
    return `${date.getMonth() + 1}月${date.getDate()}日`
  }
}
```
**问题**:
- 超过1个月后，没有显示年份（跨年问题）
- 负数时间差没有处理（未来时间）

#### 3. **标签过多时截断逻辑可能不合理**
```typescript
// Line 88-91
const displayTags = computed(() => {
  if (!props.question.tags) return []
  return props.question.tags.slice(0, 3)
})
```
**问题**: 截断了但没有"..."提示

---

## 七、类型定义问题

### ❌ question.ts 类型定义不统一

```typescript
export interface QuestionItem {
  qid: number              // 后端返回 qid
  questionId?: number      // 兼容字段
  status: number           // 后端返回 status (0/1)
  isSolved?: boolean       // 兼容字段
  bounty: number           // 后端返回 bounty
  rewardPoints?: number    // 兼容字段
}
```

**问题**: 大量兼容字段说明前后端字段名不统一，应该彻底统一

---

## 八、Services API 问题

### ❌ 发现的问题

#### 1. **新增的API可能不存在**
- `getMyQuestions`
- `getMyAnswers`

需要确认后端是否实现

#### 2. **排序参数类型定义错误**
```typescript
// QuestionListParams
sortBy?: 'views' | 'created_at' | 'rewardPoints'
```
**问题**: `rewardPoints` 应该是 `bounty`

---

## 九、Store 问题

### ❌ stores/question.ts

#### 1. **字段名不统一**
```typescript
// Line 62-63
const unsolvedCount = computed(() => {
  return questions.value.filter((q) => !q.isSolved).length
})
```
**问题**: 使用 `isSolved` 但实际字段是 `status`

#### 2. **缓存策略可能不合理**
5分钟缓存对于问答场景可能太长，新问题/新回答无法及时显示

---

## 十、全局性问题

### ❌ 代码质量问题

#### 1. **代码重复严重**
- `formatTime` 函数在 4 个文件中重复
- `formatNumber` 函数在 3 个文件中重复
- 骨架屏样式在多个地方重复

#### 2. **缺少错误边界**
大部分页面的 catch 只是 toast 提示，没有降级方案

#### 3. **缺少加载状态管理**
某些操作没有 loading 状态，用户不知道是否在处理

#### 4. **缺少权限控制**
- 删除按钮只检查 `isMyQuestion`，没有二次确认
- 采纳答案没有检查问题是否已解决

#### 5. **图片相关功能不完整**
- 图片上传未实现
- 图片压缩未实现
- 图片预览错误处理缺失

#### 6. **无障碍性问题**
- 没有 aria-label
- 图片没有 alt 属性

---

## 问题优先级分类

### 🔴 P0 - 严重问题（必须修复）
1. ✅ **字段名不匹配**: `rewardPoints` → `bounty`
2. ✅ **状态字段不统一**: `isSolved` → `status`
3. ❌ **图片上传功能缺失**
4. ❌ **新增API可能不存在**
5. ❌ **回答列表数据格式兼容问题**

### 🟡 P1 - 重要问题（应该修复）
1. ❌ **乐观更新缺少回滚**
2. ❌ **表单校验不够严格**
3. ❌ **缺少错误边界处理**
4. ❌ **时间格式化跨年问题**
5. ❌ **代码重复（工具函数）**

### 🟢 P2 - 优化建议（可选）
1. ⚠️  Tab切换数据缓存
2. ⚠️  草稿保存功能
3. ⚠️  推荐标签动态化
4. ⚠️  榜单时间范围筛选
5. ⚠️  搜索防抖时间调整

---

## 建议的优化方案

### 阶段1：修复严重问题（1-2小时）
1. 统一字段名：`rewardPoints` → `bounty`，`isSolved` → `status`
2. 修复类型定义和API参数
3. 实现图片上传到OSS
4. 统一回答列表数据格式

### 阶段2：代码重构（2-3小时）
1. 提取公共工具函数到 `utils/` 目录
2. 创建公共骨架屏组件
3. 统一错误处理机制
4. 添加乐观更新回滚逻辑

### 阶段3：体验优化（2-3小时）
1. 完善表单校验
2. 添加草稿保存
3. 优化搜索防抖
4. 添加Tab切换缓存
5. 优化时间格式化

---

## 总结

**总计发现问题**: 35+
**P0 严重问题**: 5个
**P1 重要问题**: 5个
**P2 优化建议**: 10个
**代码质量问题**: 15+

主要问题集中在：
1. 前后端字段名不统一
2. 图片上传功能未实现
3. 代码重复严重
4. 错误处理不完善
5. 表单校验不严格
