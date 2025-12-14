# HotQuestions 组件优化说明

## 优化背景

基于专业 UX 评审反馈,原版本存在以下问题:
1. ❌ 热度"只有排序,没有层级感"
2. ❌ Hover/Active 反馈过弱,状态语义不明确
3. ❌ 标题行略显"平",Top1 不够抓眼
4. ❌ 列表项节奏单一,有"文字墙"感

**目标**:从 80 分(可上线)提升到 85-90 分(产品作品集水准)

---

## 优化方案总览

### 1️⃣ 强化 Top1 层级感(不平均用力)

**优化前**:
```scss
// 所有排名徽章尺寸一致
.rank-badge { width: 40rpx; height: 40rpx; }

// 所有标题字重一致
.question-title { font-weight: 500; }
```

**优化后**:
```scss
// Top1 更大更饱满
.rank-badge.rank-1 {
  width: 44rpx;  // +4rpx
  height: 44rpx;
  font-size: 22rpx;  // +2rpx
  font-weight: 800;  // 更粗
  background: linear-gradient(135deg, #FFD700 0%, #FF8C00 100%);  // 更鲜艳
  box-shadow: 0 3rpx 12rpx rgba(#FFD700, 0.4);  // 更强阴影
}

// Top1 标题加粗
.question-title.title-rank-1 {
  font-weight: 600;  // 500→600
  line-height: 1.5;  // 1.4→1.5,增强呼吸感
}

// Top4-5 弱化
.rank-badge.rank-4,
.rank-badge.rank-5 {
  background: $gray-100;  // 更浅
  color: $gray-500;  // 更浅
  font-weight: 600;  // 减轻字重
}

.question-title.title-rank-4,
.question-title.title-rank-5 {
  font-weight: 400;  // 减轻字重
  color: $gray-700;  // 文字颜色变浅
}
```

**效果**:形成清晰的阅读节奏 - Top1(头条) → Top2-3(重点) → Top4-5(扫读)

---

### 2️⃣ 热度可视化(关键改进)

**优化前**:
- 只有数字(868、799、792)
- 用户"读到热",但"感受不到热"

**优化后**:
```vue
<!-- 模板:浏览量下方添加热度条 -->
<view class="meta-item views-item">
  <Icon name="eye" :size="12" />
  <text class="meta-text">{{ formatNumber(question.views) }}</text>
  <!-- 热度可视化条 -->
  <view class="heat-bar" :style="{ width: getHeatWidth(question.views) }"></view>
</view>
```

```typescript
// 计算热度条宽度(基于浏览量百分比)
const getHeatWidth = (views: number): string => {
  const maxViews = Math.max(...props.questions.map(q => q.views))
  const percentage = (views / maxViews) * 100
  return `${Math.max(percentage, 5)}%` // 最小5%保证可见性
}
```

```scss
// 热度条样式
.heat-bar {
  position: absolute;
  left: 0;
  bottom: -2rpx;
  height: 3rpx;
  background: linear-gradient(90deg, rgba($primary, 0.3) 0%, rgba($primary, 0.6) 100%);
  border-radius: 2rpx;
  transition: all 0.3s ease-out;

  // Hover 时更明显
  .question-item:hover & {
    height: 4rpx;
    background: linear-gradient(90deg, $primary 0%, lighten($primary, 10%) 100%);
    box-shadow: 0 1rpx 4rpx rgba($primary, 0.3);
  }
}
```

**效果**:
- Top1 热度条接近 100% 宽度
- Top2-3 明显短于 Top1
- Top4-5 更短
- 用户可以"看到热度差距",而不是"计算数字差距"

---

### 3️⃣ 明确交互状态(必须改进)

**优化前**:
```scss
.question-item:hover {
  background: $gray-50;
  transform: translateX(4rpx);  // 向右移动(不够严谨)
}
```

**优化后**:
```scss
.question-item {
  border-left: 4rpx solid transparent;  // 预留左边框空间
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);

  // Hover:浅灰背景+向左移动(更符合列表交互习惯)
  &:hover {
    background: $gray-50;
    transform: translateX(-2rpx);  // 向左移动
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
  }

  // Active:左侧主色边框(明确"当前查看"状态)
  &:active,
  &.active {
    border-left-color: $primary;
    background: rgba($primary, 0.02);
  }
}
```

**效果**:
- Hover = 浅灰背景 + 轻微左移 + 阴影
- Active = 左侧主色边框 + 主色半透明背景
- 两种状态语义清晰,不会混淆

---

### 4️⃣ 优化列表节奏(呼吸感)

**优化前**:
```scss
.questions-list {
  gap: 12rpx;  // 统一间距
}

.question-item {
  padding: 12rpx;  // 统一内边距
}
```

**优化后**:
```scss
.questions-list {
  gap: 10rpx;  // 默认间距
}

.question-item {
  padding: 12rpx;

  // Top1 特殊间距(呼吸感)
  &:first-child {
    padding: 16rpx 12rpx;  // 上下+4rpx
    margin-bottom: 4rpx;  // 额外间距
  }
}
```

**效果**:
- Top1 占据更多空间,有"头条"的视觉权重
- 整体列表不再"文字墙",而是"内容推荐"

---

## 对比总结

| 维度 | 优化前(v1.0) | 优化后(v1.1) |
|------|------------|------------|
| **热度感知** | ❌ 只有数字,需要计算 | ✅ 热度条直观显示差距 |
| **视觉层级** | ❌ 1-5 名差异不明显 | ✅ Top1 头条 → Top2-3 重点 → Top4-5 弱化 |
| **交互状态** | ❌ Hover 向右移(不严谨) | ✅ Hover 左移 + Active 左边框(明确) |
| **列表节奏** | ❌ 统一间距,文字墙感 | ✅ Top1 呼吸感更强 |
| **产品完成度** | 80 分(可上线) | 85-90 分(作品集水准) |

---

## 技术实现细节

### 1. 动态计算热度条宽度
```typescript
const getHeatWidth = (views: number): string => {
  if (props.questions.length === 0) return '0%'
  const maxViews = Math.max(...props.questions.map(q => q.views))
  if (maxViews === 0) return '0%'
  const percentage = (views / maxViews) * 100
  return `${Math.max(percentage, 5)}%` // 最小5%保证可见性
}
```

**关键点**:
- 使用 `Math.max()` 找到最大浏览量
- 计算百分比:`(当前浏览量 / 最大浏览量) * 100`
- 最小宽度 5%,避免热度条完全消失

### 2. 层级样式应用
```vue
<text class="question-title" :class="`title-rank-${index + 1}`">
  {{ question.title }}
</text>
```

**关键点**:
- 动态绑定 class:`title-rank-1` ~ `title-rank-5`
- 使用 SCSS 嵌套选择器控制不同排名的样式
- Top1/Top2-3/Top4-5 三档差异化

### 3. Hover 状态层叠
```scss
// Hover 时热度条变化
.question-item:hover .heat-bar {
  height: 4rpx;
  background: linear-gradient(90deg, $primary 0%, lighten($primary, 10%) 100%);
}

// Hover 时标题变色
.question-item:hover .title-rank-1 {
  color: $primary;
}
```

**关键点**:
- 使用父选择器 `.question-item:hover` 控制子元素
- 热度条、标题、徽章同步响应 Hover
- 动画使用 `cubic-bezier(0.4, 0, 0.2, 1)` 缓动函数

---

## 使用建议

### 适用场景
✅ **问答社区右侧栏**(已集成,推荐配置)
✅ **首页推荐模块**(显示悬赏+查看更多)
✅ **用户个人中心**(我的提问)
✅ **任何需要展示热门问题的地方**

### 不适用场景
❌ 主列表(会抢主内容的视觉权重)
❌ 平铺展示(适合垂直右侧栏,不适合横向平铺)

### 配置建议
```vue
<!-- 问答社区右侧栏(推荐) -->
<HotQuestions
  :questions="hotQuestions"
  title="热门问题"
  :show-bounty="false"  // 不显示悬赏,简洁
  :max-display="5"  // 最多5个,避免过长
/>

<!-- 首页推荐模块 -->
<HotQuestions
  :questions="recommendedQuestions"
  title="推荐问题"
  :show-bounty="true"  // 显示悬赏,吸引点击
  :show-view-more="true"  // 支持查看更多
  :max-display="3"  // 最多3个,留白充足
/>
```

---

## 后续优化方向

### 可选增强(根据实际需求)
1. **微动画**:Top1 徽章轻微脉冲动画(参考知乎热榜)
2. **新标签**:24小时内新增的问题显示"NEW"标签
3. **趋势箭头**:排名变化显示上升/下降箭头
4. **骨架屏**:数据加载时显示骨架屏(参考 `SkeletonScreen.vue`)

### 不建议的方向
❌ 添加问题封面图(会抢视觉焦点,与"辅助内容"定位冲突)
❌ 过度动画(轮播、旋转等,干扰阅读)
❌ 复杂图表(饼图、柱状图等,信息过载)

---

## v1.2 克制打磨(专业评审后微调)

### 基于第二轮评审的3个精准改进

#### 改进1:色彩逻辑统一(解决"Top2灰+Top3橙"跳跃感)
**问题**:用户潜意识疑问"为什么第2是灰,第3是橙?"

**方案A(已采用)**:Top1-3 金色系不同明度
```scss
Top1: #FFD700 → #FFA500 (高饱和金,100%明度)
Top2: #E6C200 → #D4A017 (中等金,70%明度)
Top3: #C9A003 → #B8860B (暗金,50%明度)
```

**效果**:清晰的"金银铜"视觉层级,色彩逻辑一目了然。

---

#### 改进2:Top1头条感微调(字号+行高)
**问题**:虽然已加粗,但在右侧热榜里Top1还不够"像头条"

**优化**:
```scss
.title-rank-1 {
  font-size: 28rpx;  // 26rpx→28rpx,微调+2rpx
  font-weight: 600;  // 已有
  line-height: 1.5;  // 已有
}
```

**效果**:Top1 更像"头条",而非"列表第一项"。

---

#### 改进3:热度条起点内缩(克制设计)
**问题**:热度条从最左开始,每条下方"横线很多",略显突兀

**优化**:
```scss
.heat-bar {
  left: 28rpx;  // 从图标右侧开始(icon+gap+text)
  height: 2.5rpx;  // 3rpx→2.5rpx,更轻量
  opacity: 0.6 (Top4-5);  // 进一步弱化
}
```

**效果**:
- 热度条起点自然(从数字右侧开始)
- 整体更干净,避免"横线扎堆"感
- Top4-5 热度条降低透明度,减少视觉噪音

---

## 总结

### v1.0 → v1.1 → v1.2 演进路径

| 版本 | 核心改进 | 完成度 |
|------|---------|-------|
| v1.0 | 基础功能实现 | 80分(可上线) |
| v1.1 | 热度可视化+视觉层级 | 85分(产品水准) |
| v1.2 | 色彩统一+克制打磨 | **成熟社区产品可上线水准** |

### 优化原则
- ✅ 没有推翻原有结构
- ✅ 没有引入复杂依赖
- ✅ 代码改动 < 150 行
- ✅ 每次迭代都是**精准微调**,而非大改

**核心价值**:
> 从"读到热度"到"感受到热度",从"看到排名"到"感受到层级"。

**最终定位**:
> 一个**非常成熟、但刻意保持低调的模块**——这在首页右侧,其实是优点。
