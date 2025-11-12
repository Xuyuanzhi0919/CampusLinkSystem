# CampusLink 设计系统规范文档

> **版本**：v1.0.0
> **更新日期**：2025-11-11
> **适用范围**：CampusLink 前端所有页面和组件开发

---

## 📋 目录

1. [设计理念](#设计理念)
2. [色彩系统](#色彩系统)
3. [字体系统](#字体系统)
4. [间距系统](#间距系统)
5. [圆角系统](#圆角系统)
6. [阴影系统](#阴影系统)
7. [动画系统](#动画系统)
8. [布局规范](#布局规范)
9. [组件规范](#组件规范)
10. [响应式设计](#响应式设计)

---

## 🎯 设计理念

### 核心价值观

**简洁 · 高效 · 温暖**

- **简洁**：纯白背景，去除干扰，让内容成为主角
- **高效**：清晰的信息层级，直观的交互反馈
- **温暖**：柔和的色彩，友好的视觉语言

### 品牌定位

**100万大学生互助学习圈**

- 面向大学生群体的轻量级互助平台
- 强调**资源共享**、**知识问答**、**校园互助**
- 视觉风格：**现代、清爽、专业**

---

## 🎨 色彩系统

### 主色调（Primary Colors）

#### 品牌蓝（CampusLink Blue）

```scss
// 主色蓝 - 用于按钮、链接、标签等核心交互元素
--cl-primary: #2563EB;        // 标准蓝（Blue 600）
--cl-primary-hover: #1D4ED8;  // 深蓝（Blue 700）- hover状态
--cl-primary-light: #3B82F6;  // 浅蓝（Blue 500）- 辅助色
--cl-primary-lightest: #DBEAFE; // 极浅蓝（Blue 100）- 背景色
```

**使用场景**：
- 主要操作按钮（"搜索"、"发布"、"查看更多"）
- 链接文字
- 高亮标签
- 热门榜单标题背景

**示例**：
```vue
<!-- 主按钮 -->
<button class="btn-primary">搜索</button>

<style>
.btn-primary {
  background: var(--cl-primary, #2563EB);
  color: #FFFFFF;
}

.btn-primary:hover {
  background: var(--cl-primary-hover, #1D4ED8);
}
</style>
```

---

#### 强调橙（Accent Orange）

```scss
// 橙色 - 用于吸引注意力的次要元素
--cl-accent-orange: #F59E0B;  // 标准橙（Amber 500）
--cl-accent-orange-dark: #D97706; // 深橙（Amber 600）
--cl-accent-orange-light: #FCD34D; // 浅橙（Amber 300）
--cl-accent-orange-bg: rgba(255, 169, 64, 0.1); // 橙色背景
```

**使用场景**：
- 智能问答卡片渐变背景（`#FFF4D7 → #FFF9E7`）
- "查看全部"按钮
- 社团动态徽章
- 热门标签

**示例**：
```vue
<!-- 智能问答卡片 -->
<view class="card-ai-qa">
  <text class="card-title">智能问答</text>
</view>

<style>
.card-ai-qa {
  background: linear-gradient(135deg, #FFF4D7 0%, #FFF9E7 100%);
}

.card-ai-qa:hover {
  box-shadow: 0 16rpx 48rpx rgba(251, 191, 36, 0.25); /* 黄色光晕 */
}
</style>
```

---

### 中性色（Neutral Colors）

#### 灰度色阶（Slate Gray）

```scss
// 文本色阶
--cl-gray-900: #1E293B;  // 标题文字（Slate 800）
--cl-gray-800: #334155;  // 副标题（Slate 700）
--cl-gray-700: #475569;  // 正文文字（Slate 600）
--cl-gray-600: #64748B;  // 次要文字（Slate 500）
--cl-gray-500: #94A3B8;  // 辅助文字（Slate 400）
--cl-gray-400: #CBD5E1;  // 禁用文字（Slate 300）

// 背景色阶
--cl-gray-50: #F8FAFC;   // 页面背景（Slate 50）
--cl-gray-100: #F1F5F9;  // 卡片hover背景（Slate 100）
--cl-gray-200: #E2E8F0;  // 分割线（Slate 200）
```

**使用规则**：

| 元素类型 | 颜色变量 | Hex 值 | 使用场景 |
|---------|---------|--------|---------|
| **大标题** | `--cl-gray-900` | `#1E293B` | 页面主标题、卡片标题 |
| **副标题** | `--cl-gray-800` | `#334155` | 区块副标题 |
| **正文** | `--cl-gray-700` | `#475569` | 卡片描述文字 |
| **次要信息** | `--cl-gray-600` | `#64748B` | 时间、作者、标签 |
| **辅助信息** | `--cl-gray-500` | `#94A3B8` | 占位符、提示文字 |
| **禁用** | `--cl-gray-400` | `#CBD5E1` | 禁用按钮、不可点击文字 |

**示例**：
```scss
// 标题
.card-title {
  font-size: 32rpx;
  font-weight: 700;
  color: var(--cl-gray-900, #1E293B);
}

// 描述
.card-desc {
  font-size: 28rpx;
  font-weight: 400;
  color: var(--cl-gray-700, #475569);
}

// 时间
.card-time {
  font-size: 24rpx;
  color: var(--cl-gray-500, #94A3B8);
}
```

---

### 功能色（Functional Colors）

```scss
// 成功（绿色）
--cl-success: #22C55E;        // Green 500
--cl-success-light: #86EFAC;  // Green 300
--cl-success-bg: #F0FDF4;     // Green 50

// 警告（橙色）
--cl-warning: #F59E0B;        // Amber 500
--cl-warning-light: #FCD34D;  // Amber 300
--cl-warning-bg: #FFFBEB;     // Amber 50

// 错误（红色）
--cl-error: #EF4444;          // Red 500
--cl-error-light: #FCA5A5;    // Red 300
--cl-error-bg: #FEF2F2;       // Red 50

// 信息（蓝色）
--cl-info: #3B82F6;           // Blue 500
--cl-info-light: #93C5FD;     // Blue 300
--cl-info-bg: #EFF6FF;        // Blue 50
```

**使用场景**：
- **成功**：任务完成、上传成功
- **警告**：即将到期、库存不足
- **错误**：表单错误、操作失败
- **信息**：系统通知、提示消息

---

### 背景色（Background Colors）

```scss
// 页面背景
--cl-bg-page: #FFFFFF;        // 纯白背景
--cl-bg-section: #F8FAFC;     // 辅助区块背景（Slate 50）

// 卡片背景
--cl-bg-card: #FFFFFF;        // 白色卡片
--cl-bg-card-hover: #F9FAFB;  // 卡片hover状态

// 输入框背景
--cl-bg-input: #F8FAFC;       // 输入框底色
--cl-bg-input-focus: #FFFFFF; // 输入框聚焦底色

// 榜单背景（特殊）
--cl-bg-ranking: #F9FBFF;     // 淡灰蓝背景（用于右侧榜单）
```

**布局应用**：
```scss
// 首页整体背景
.home-new {
  background: var(--cl-bg-page, #FFFFFF); // 纯白
}

// 辅助信息区（积分中心、校园公告等）
.auxiliary-section {
  background: var(--cl-bg-section, #F8FAFC); // 浅灰
}

// 热门榜单卡片
.ranking-area {
  background: var(--cl-bg-ranking, #F9FBFF); // 淡灰蓝
}
```

---

### 渐变色（Gradient Colors）

#### 卡片渐变

```scss
// 资料共享卡片（蓝色渐变）
.card-resource {
  background: linear-gradient(135deg, #EAF3FF 0%, #F2F8FF 100%);
}

// 智能问答卡片（黄色渐变）
.card-ai-qa {
  background: linear-gradient(135deg, #FFF4D7 0%, #FFF9E7 100%);
}

// 社团活动按钮（橙色渐变）
.btn-club-activity {
  background: linear-gradient(135deg, #FFA940 0%, #FFB64B 100%);
}
```

#### 背景装饰渐变

```scss
// 顶部聚焦区渐变（#E3EFFF → #F8FBFF）
.top-focus-bar-bg {
  background: linear-gradient(180deg, #E3EFFF 0%, #F8FBFF 100%);
}
```

---

## 🔤 字体系统

### 字体族

```scss
// 主字体（优先使用系统字体）
--cl-font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI',
                  'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei',
                  'Helvetica Neue', Helvetica, Arial, sans-serif;
```

### 字号规范

| 用途 | 变量名 | rpx | px | 备注 |
|------|--------|-----|----|----|
| **特大标题** | `--cl-font-xxxl` | 72rpx | 36px | 页面主标题 |
| **大标题** | `--cl-font-xxl` | 48rpx | 24px | 区块标题 |
| **中标题** | `--cl-font-xl` | 40rpx | 20px | 卡片标题 |
| **小标题** | `--cl-font-lg` | 32rpx | 16px | 列表标题 |
| **正文** | `--cl-font-md` | 28rpx | 14px | 正文、描述 |
| **辅助文字** | `--cl-font-sm` | 24rpx | 12px | 时间、标签 |
| **极小文字** | `--cl-font-xs` | 20rpx | 10px | 角标、备注 |

### 字重规范

```scss
--cl-font-light: 300;    // 纤细（极少使用）
--cl-font-normal: 400;   // 常规（正文）
--cl-font-medium: 500;   // 中等（副标题）
--cl-font-semibold: 600; // 半粗（小标题）
--cl-font-bold: 700;     // 粗体（主标题）
--cl-font-black: 900;    // 特粗（强调）
```

### 行高规范

```scss
--cl-line-height-tight: 1.2;   // 紧凑（大标题）
--cl-line-height-normal: 1.5;  // 常规（正文）
--cl-line-height-loose: 1.8;   // 宽松（长文本）
```

### 应用示例

```scss
// 卡片标题
.card-title {
  font-size: var(--cl-font-xl, 40rpx);
  font-weight: var(--cl-font-bold, 700);
  line-height: var(--cl-line-height-tight, 1.2);
  color: var(--cl-gray-900, #1E293B);
}

// 正文描述
.card-desc {
  font-size: var(--cl-font-md, 28rpx);
  font-weight: var(--cl-font-normal, 400);
  line-height: var(--cl-line-height-normal, 1.5);
  color: var(--cl-gray-700, #475569);
}

// 辅助信息
.card-meta {
  font-size: var(--cl-font-sm, 24rpx);
  font-weight: var(--cl-font-normal, 400);
  color: var(--cl-gray-500, #94A3B8);
}
```

---

## 📏 间距系统

### 间距变量

```scss
// 基础间距（8px 网格系统）
--cl-spacing-xs: 8rpx;    // 4px - 极小间距
--cl-spacing-sm: 16rpx;   // 8px - 小间距
--cl-spacing-md: 24rpx;   // 12px - 中等间距
--cl-spacing-lg: 32rpx;   // 16px - 大间距
--cl-spacing-xl: 48rpx;   // 24px - 超大间距
--cl-spacing-xxl: 64rpx;  // 32px - 特大间距
--cl-spacing-xxxl: 96rpx; // 48px - 极大间距

// 页面级间距
--cl-spacing-page-h: 160rpx; // 80px - 页面水平边距（PC端）
--cl-spacing-page-v: 128rpx; // 64px - 页面垂直间距
```

### 内边距（Padding）规范

```scss
// 卡片内边距
.card {
  padding: 32rpx; // 16px - 标准卡片内边距
}

// 小卡片内边距
.card-small {
  padding: 24rpx; // 12px
}

// 大卡片内边距
.card-large {
  padding: 48rpx; // 24px
}

// 按钮内边距
.btn {
  padding: 16rpx 32rpx; // 8px 16px - 上下 左右
}
```

### 外边距（Margin）规范

```scss
// 区块间距
.section {
  margin-bottom: 48rpx; // 24px - 区块之间
}

// 卡片间距
.card + .card {
  margin-top: 24rpx; // 12px - 卡片之间
}

// 元素间距
.title {
  margin-bottom: 16rpx; // 8px - 标题与内容之间
}
```

### 布局间距示例

```scss
// 首页主内容区
.content-container {
  max-width: 2560rpx; // 1280px
  margin: 0 auto;
  padding: 0 160rpx; // 80px - 左右边距
  display: flex;
  gap: 48rpx; // 24px - 左右栏间距
}

// 响应式间距
@media (max-width: 1440px) {
  .content-container {
    padding: 0 96rpx; // 48px
  }
}

@media (max-width: 1024px) {
  .content-container {
    padding: 0 48rpx; // 24px
  }
}
```

---

## 🔘 圆角系统

### 圆角变量

```scss
--cl-radius-none: 0;      // 直角
--cl-radius-xs: 8rpx;     // 4px - 极小圆角
--cl-radius-sm: 12rpx;    // 6px - 小圆角
--cl-radius-md: 16rpx;    // 8px - 中圆角
--cl-radius-lg: 24rpx;    // 12px - 大圆角
--cl-radius-xl: 32rpx;    // 16px - 超大圆角
--cl-radius-xxl: 48rpx;   // 24px - 特大圆角
--cl-radius-full: 9999px; // 完全圆角
```

### 应用规范

```scss
// 卡片圆角
.card {
  border-radius: var(--cl-radius-lg, 24rpx); // 12px
}

// 按钮圆角
.btn {
  border-radius: var(--cl-radius-md, 16rpx); // 8px
}

// 圆形按钮
.btn-circle {
  border-radius: var(--cl-radius-full, 9999px);
}

// 标签圆角
.tag {
  border-radius: var(--cl-radius-sm, 12rpx); // 6px
}

// 搜索框圆角
.search-box {
  border-radius: var(--cl-radius-xl, 32rpx); // 16px
}
```

---

## 🌟 阴影系统

### 阴影变量

```scss
// 轻阴影（hover前）
--cl-shadow-sm: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);

// 标准阴影（常规卡片）
--cl-shadow-md: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);

// 中等阴影（hover状态）
--cl-shadow-lg: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);

// 深阴影（强调元素）
--cl-shadow-xl: 0 12rpx 32rpx rgba(0, 0, 0, 0.12);

// 品牌色阴影（蓝色光晕）
--cl-shadow-primary: 0 8rpx 24rpx rgba(37, 99, 235, 0.12);
--cl-shadow-primary-hover: 0 12rpx 32rpx rgba(37, 99, 235, 0.25);

// 强调色阴影（橙色光晕）
--cl-shadow-accent: 0 8rpx 24rpx rgba(245, 158, 11, 0.12);
--cl-shadow-accent-hover: 0 12rpx 32rpx rgba(251, 191, 36, 0.25);
```

### 阴影应用规则

```scss
// 白色卡片默认阴影
.card {
  background: #FFFFFF;
  box-shadow: var(--cl-shadow-md, 0 4rpx 12rpx rgba(0, 0, 0, 0.04));
}

// hover 时增强阴影
.card:hover {
  box-shadow: var(--cl-shadow-lg, 0 8rpx 24rpx rgba(0, 0, 0, 0.08));
}

// 榜单卡片（蓝色阴影）
.ranking-card:hover {
  box-shadow: var(--cl-shadow-primary-hover, 0 12rpx 32rpx rgba(37, 99, 235, 0.25));
}

// 社团活动卡片（橙色阴影）
.club-card:hover {
  box-shadow: var(--cl-shadow-accent-hover, 0 12rpx 32rpx rgba(245, 158, 11, 0.12));
}
```

### 特殊阴影效果

```scss
// 玻璃拟态（功能卡片）
.function-card {
  box-shadow:
    0 12rpx 24rpx rgba(0, 0, 0, 0.04), /* 外阴影 */
    inset 0 -2rpx 8rpx rgba(255, 255, 255, 0.5); /* 内渐变白 */
}

// 浮动阴影（悬浮按钮）
.floating-btn {
  box-shadow:
    0 8rpx 16rpx rgba(0, 0, 0, 0.1),
    0 4rpx 8rpx rgba(0, 0, 0, 0.06);
}
```

---

## 🎬 动画系统

### 过渡时间（Transition Duration）

```scss
--cl-duration-fast: 150ms;     // 快速（按钮hover）
--cl-duration-normal: 200ms;   // 常规（卡片hover）
--cl-duration-slow: 300ms;     // 慢速（弹窗展开）
--cl-duration-slower: 500ms;   // 更慢（页面切换）
```

### 缓动函数（Easing Function）

```scss
--cl-easing-linear: linear;
--cl-easing-ease: ease;
--cl-easing-ease-in: ease-in;
--cl-easing-ease-out: ease-out;
--cl-easing-ease-in-out: ease-in-out;

// 自定义缓动（更自然）
--cl-easing-smooth: cubic-bezier(0.4, 0, 0.2, 1);
```

### 常用动画

#### 淡入淡出（Fade）

```scss
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.fade-in {
  animation: fadeIn var(--cl-duration-normal, 200ms) var(--cl-easing-ease-out, ease-out);
}
```

#### 向上淡入（Fade Up）

```scss
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-in-up {
  animation: fadeInUp 240ms var(--cl-easing-ease-out, ease-out) both;
}

// 延迟入场（多个元素依次出现）
.fade-in-up:nth-child(1) { animation-delay: 0.1s; }
.fade-in-up:nth-child(2) { animation-delay: 0.2s; }
.fade-in-up:nth-child(3) { animation-delay: 0.3s; }
```

#### Hover 过渡

```scss
.card {
  transition: all var(--cl-duration-normal, 200ms) var(--cl-easing-ease-out, ease-out);
}

.card:hover {
  transform: translateY(-4rpx); // 微微上浮
  box-shadow: var(--cl-shadow-lg, 0 8rpx 24rpx rgba(0, 0, 0, 0.08));
}
```

#### 脉冲动画（语音按钮）

```scss
@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.05);
    opacity: 0.8;
  }
}

.voice-btn-active {
  animation: pulse 1.5s var(--cl-easing-ease-in-out, ease-in-out) infinite;
}
```

---

## 📐 布局规范

### 最大宽度限制

```scss
// 内容区最大宽度（超宽屏时居中）
--cl-container-max-width: 2560rpx; // 1280px
```

### 响应式断点

```scss
// 断点定义
--cl-breakpoint-mobile: 750px;    // 移动端
--cl-breakpoint-tablet: 960px;    // 平板
--cl-breakpoint-desktop: 1280px;  // 桌面端
--cl-breakpoint-wide: 1440px;     // 宽屏
```

### 栅格系统

```scss
// 首页左右栏比例
.content-container {
  display: flex;
  gap: 48rpx; // 2% 间距

  // ≥1280px：左 70% / 右 30%
  @media (min-width: 1280px) {
    .recommend-area {
      flex: 70;
    }
    .ranking-area {
      flex: 30;
    }
  }

  // <960px：垂直堆叠
  @media (max-width: 960px) {
    flex-direction: column;
  }
}
```

---

## 🧩 组件规范

### 按钮（Button）

#### 主按钮（Primary）

```vue
<button class="btn btn-primary">搜索</button>

<style>
.btn-primary {
  padding: 16rpx 32rpx;
  background: var(--cl-primary, #2563EB);
  color: #FFFFFF;
  border-radius: 16rpx;
  font-size: 28rpx;
  font-weight: 600;
  transition: all 150ms ease;
}

.btn-primary:hover {
  background: var(--cl-primary-hover, #1D4ED8);
  box-shadow: 0 4rpx 12rpx rgba(37, 99, 235, 0.25);
}

.btn-primary:active {
  transform: scale(0.98);
}
</style>
```

#### 次要按钮（Secondary）

```scss
.btn-secondary {
  padding: 16rpx 32rpx;
  background: var(--cl-gray-100, #F1F5F9);
  color: var(--cl-gray-700, #475569);
  border-radius: 16rpx;
}

.btn-secondary:hover {
  background: var(--cl-gray-200, #E2E8F0);
}
```

### 卡片（Card）

```scss
.card {
  padding: 32rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
  transition: all 200ms ease-out;
}

.card:hover {
  transform: translateY(-4rpx);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
}
```

### 标签（Tag）

```scss
.tag {
  display: inline-block;
  padding: 8rpx 16rpx;
  background: var(--cl-primary-lightest, #DBEAFE);
  color: var(--cl-primary, #2563EB);
  font-size: 24rpx;
  border-radius: 12rpx;
  transition: all 150ms ease;
}

.tag:hover {
  background: var(--cl-primary, #2563EB);
  color: #FFFFFF;
}
```

---

## 📱 响应式设计

### H5 端适配

```scss
@media (max-width: 750px) {
  // 减小边距
  .content-container {
    padding: 0 24rpx;
  }

  // 单列布局
  .content-container {
    flex-direction: column;
  }

  // 字体缩小
  .page-title {
    font-size: 48rpx; // 从 72rpx 缩小
  }
}
```

### PC 端适配

```scss
/* #ifdef H5 */
@media (min-width: 768px) {
  // 最大宽度限制
  .container {
    max-width: 1200rpx;
    margin: 0 auto;
  }
}
/* #endif */
```

---

## 📚 使用指南

### 快速开始

```vue
<template>
  <view class="page">
    <view class="container">
      <view class="card">
        <text class="card-title">标题</text>
        <text class="card-desc">描述文字</text>
        <button class="btn btn-primary">按钮</button>
      </view>
    </view>
  </view>
</template>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: var(--cl-bg-page, #FFFFFF);
}

.container {
  max-width: var(--cl-container-max-width, 2560rpx);
  margin: 0 auto;
  padding: 0 var(--cl-spacing-page-h, 160rpx);
}

.card {
  padding: var(--cl-spacing-lg, 32rpx);
  background: var(--cl-bg-card, #FFFFFF);
  border-radius: var(--cl-radius-lg, 24rpx);
  box-shadow: var(--cl-shadow-md, 0 4rpx 12rpx rgba(0, 0, 0, 0.04));
}

.card-title {
  font-size: var(--cl-font-xl, 40rpx);
  font-weight: var(--cl-font-bold, 700);
  color: var(--cl-gray-900, #1E293B);
}

.card-desc {
  font-size: var(--cl-font-md, 28rpx);
  color: var(--cl-gray-700, #475569);
}

.btn-primary {
  padding: 16rpx 32rpx;
  background: var(--cl-primary, #2563EB);
  color: #FFFFFF;
  border-radius: var(--cl-radius-md, 16rpx);
}
</style>
```

---

## 📝 变更日志

| 日期 | 版本 | 变更内容 |
|------|------|---------|
| 2025-11-11 | v1.0.0 | 初始版本，基于首页设计提取规范 |

---

## 🎉 总结

本设计系统文档基于 CampusLink 首页的实际实现提取而成，包含：

✅ **完整的色彩系统**：主色、中性色、功能色、背景色、渐变色
✅ **规范的字体系统**：字号、字重、行高
✅ **统一的间距系统**：8px 网格，响应式间距
✅ **一致的圆角系统**：从 4px 到完全圆角
✅ **专业的阴影系统**：轻阴影到品牌色光晕
✅ **流畅的动画系统**：过渡、淡入、hover 效果
✅ **清晰的布局规范**：栅格系统、响应式断点
✅ **可复用的组件规范**：按钮、卡片、标签

**使用建议**：
1. 新页面开发时优先使用本文档定义的 CSS 变量
2. 保持视觉一致性，避免随意定义新颜色
3. 遵循间距和圆角规范，构建统一的视觉节奏
4. 复用组件样式，降低维护成本

**下一步**：
- 补充更多组件规范（输入框、下拉框、模态框等）
- 添加暗黑模式支持
- 制作 Figma 设计系统文件
