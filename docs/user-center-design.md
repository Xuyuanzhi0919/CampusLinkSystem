# 用户中心设计文档

## 📋 文档信息

| 项目 | 内容 |
|------|------|
| **文档名称** | 用户中心设计文档 |
| **版本** | v1.0.0 |
| **创建日期** | 2025-01-19 |
| **最后更新** | 2025-01-19 |
| **设计者** | CampusLink Team |
| **状态** | 设计中 ✏️ |

---

## 🎯 设计目标

### 产品定位
CampusLink 定位为**清爽学习社区 + 校园工具**，而非游戏积分商城。用户中心的设计应体现：
- **理性、可信赖**：学习相关产品的基础调性
- **年轻、有元气**：校园氛围的活力感
- **安静、有秩序**：信息优先，避免过度娱乐化

### 核心目标
1. **信息展示**: 清晰展示用户基本信息、积分、等级、统计数据
2. **功能入口**: 提供各种用户相关功能的快捷入口
3. **个性化体验**: 支持用户自定义资料、头像、昵称等
4. **数据可视化**: 直观展示用户成长轨迹和贡献统计
5. **多端适配**: PC/H5/小程序一致体验

### 设计原则
- **简洁明了**: 信息层次清晰，避免视觉混乱
- **易用性**: 常用功能易于访问，操作流程简单
- **一致性**: 与现有资源中心、问答中心风格统一
- **克制配色**: 避免"彩虹超市风"，统一品牌主色
- **轻量阴影**: 避免厚重感，用灰底拉开层级
- **响应式**: 适配不同屏幕尺寸
- **高性能**: 快速加载，流畅交互

### 配色优化思路
❌ **之前的问题**：
- 紫蓝渐变 header（偏电竞/游戏风）
- 每个功能入口独立高饱和渐变（彩虹超市）
- 等级徽章过于华丽（闪瞎眼的金色）
- 品牌色不统一（蓝/紫/绿/黄/红全开）

✅ **优化后的方案**：
- **统一品牌主色**：#2563EB（校园科技蓝）
- **按功能分组配色**：学习（蓝）/ 资产（黄）/ 系统（灰）
- **轻量化阴影**：用灰底 + 轻阴影代替厚重感
- **哑光金属色**：等级徽章降低华丽度

---

## 📐 整体布局设计

### 页面结构

```
┌──────────────────────────────────────┐
│          顶部信息卡片区              │
│  ┌────────────────────────────────┐  │
│  │ 背景装饰层（渐变+几何图形）    │  │
│  │  ┌──────┐                      │  │
│  │  │ 头像  │  昵称 / 学号         │  │
│  │  └──────┘  等级徽章             │  │
│  │                                 │  │
│  │  ┌────┬────┬────┬────┐        │  │
│  │  │积分│等级│资源│问答│        │  │
│  │  └────┴────┴────┴────┘        │  │
│  └────────────────────────────────┘  │
└──────────────────────────────────────┘
┌──────────────────────────────────────┐
│          功能入口网格区              │
│  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐  │
│  │我的 │ │我的 │ │我的 │ │我的 │  │
│  │资源 │ │问答 │ │任务 │ │收藏 │  │
│  └─────┘ └─────┘ └─────┘ └─────┘  │
│  ┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐  │
│  │积分 │ │设置 │ │关于 │ │帮助 │  │
│  │记录 │ │     │ │     │ │中心 │  │
│  └─────┘ └─────┘ └─────┘ └─────┘  │
└──────────────────────────────────────┘
┌──────────────────────────────────────┐
│          账号管理区                  │
│  修改资料 / 修改密码 / 退出登录      │
└──────────────────────────────────────┘
```

---

## 🎨 视觉设计规范

### 色彩方案

基于 CampusLink "清爽学习社区"的定位，采用克制、理性的色彩体系：

#### 核心设计理念
- **主基调**: 理性、可信赖（学习相关产品的基础）
- **点缀**: 年轻、有一点元气（校园氛围）
- **整体**: 安静、有秩序（信息优先，避免视觉混乱）

#### 品牌主色（统一全站）
```scss
// 品牌主色：校园科技蓝（统一识别）
$primary: #2563EB;                // 主按钮、链接、品牌标识
$primary-hover: #1D4ED8;          // hover状态
$primary-soft: #EFF6FF;           // 主色浅背景（卡片底色）
$primary-gradient: linear-gradient(135deg, #2563EB 0%, #1D4ED8 100%);

// 辅助色：突出"学习 & 校园"
$accent-teal: #14B8A6;            // 学习成就、关键标签
$accent-yellow: #FACC15;          // 积分、等级点缀（少量使用）

// 功能语义色
$success-green: #10B981;          // 成功状态
$warning-orange: #F59E0B;         // 警告、提示
$danger-red: #EF4444;             // 危险操作、错误
$info-blue: #3B82F6;              // 信息提示
```

#### 中性色（阅读舒适）
```scss
// 背景色（柔和灰阶）
$bg-page: #F3F4F6;                // 页面底色
$bg-card: #FFFFFF;                // 卡片背景
$bg-secondary: #F9FAFB;           // 次级区域背景
$bg-tertiary: #F3F4F6;            // 功能入口底色

// 文字色（清晰层次）
$text-primary: #111827;           // 主标题（深灰，非纯黑）
$text-secondary: #6B7280;         // 次要文字
$text-tertiary: #9CA3AF;          // 辅助文字
$text-disabled: #D1D5DB;          // 禁用状态

// 边框色（轻量）
$border-light: #F3F4F6;           // 浅边框
$border-normal: #E5E7EB;          // 普通边框
$border-dark: #D1D5DB;            // 深边框
```

#### 功能入口分组配色（按类型统一）
```scss
// 学习相关（资源、问答、任务）- 蓝系
$function-study-bg: #EFF6FF;      // 柔和蓝底
$function-study-icon: #2563EB;    // 品牌蓝图标

// 个人资产（积分、收藏）- 黄橙系
$function-asset-bg: #FFFBEB;      // 柔和黄底
$function-asset-icon: #F59E0B;    // 暖黄图标

// 系统类（设置、关于、帮助）- 中性灰
$function-system-bg: #F3F4F6;     // 中性灰底
$function-system-icon: #6B7280;   // 灰色图标
```

#### 顶部卡片背景（两种方案）
```scss
// 方案A：清爽浅蓝块（推荐，更工具型）
$header-bg: linear-gradient(135deg, #EFF6FF 0%, #DBEAFE 100%);
$header-border: 1px solid rgba(37, 99, 235, 0.10);
// 配合深色文字，头像边框用品牌蓝

// 方案B：品牌蓝渐变（保留渐变但统一品牌色）
$header-gradient: linear-gradient(135deg, #2563EB 0%, #1D4ED8 100%);
// 配合白色文字，装饰图形降低透明度
```

#### 等级徽章（哑光金属色，降低游戏感）
```scss
$level-bronze:  linear-gradient(135deg, #B45309 0%, #F97316 100%);   // 青铜（橙棕色）
$level-silver:  linear-gradient(135deg, #9CA3AF 0%, #E5E7EB 100%);   // 白银（浅灰）
$level-gold:    linear-gradient(135deg, #D97706 0%, #FBBF24 100%);   // 黄金（暖金色）
$level-diamond: linear-gradient(135deg, #0EA5E9 0%, #E0F2FE 100%);   // 钻石（天蓝色）
$level-master:  linear-gradient(135deg, #8B5CF6 0%, #DDD6FE 100%);   // 大师（浅紫色）
```

### 字体规范

```scss
// 标题字体
$font-title-large: 48rpx;         // 大标题（昵称）
$font-title-medium: 36rpx;        // 中标题（功能区标题）
$font-title-small: 32rpx;         // 小标题（卡片标题）

// 正文字体
$font-body-large: 30rpx;          // 大正文
$font-body-normal: 28rpx;         // 普通正文
$font-body-small: 26rpx;          // 小正文
$font-caption: 24rpx;             // 辅助文字

// 字重
$font-weight-bold: 600;           // 粗体（标题）
$font-weight-medium: 500;         // 中等（强调）
$font-weight-normal: 400;         // 普通（正文）
```

### 间距规范

```scss
// 外边距（margin）
$spacing-xs: 8rpx;
$spacing-sm: 16rpx;
$spacing-md: 24rpx;
$spacing-lg: 32rpx;
$spacing-xl: 48rpx;
$spacing-xxl: 64rpx;

// 内边距（padding）
$padding-xs: 12rpx;
$padding-sm: 16rpx;
$padding-md: 24rpx;
$padding-lg: 32rpx;
$padding-xl: 48rpx;
```

### 圆角规范

```scss
$radius-xs: 4rpx;                 // 小圆角（标签）
$radius-sm: 8rpx;                 // 普通圆角（按钮）
$radius-md: 16rpx;                // 中圆角（卡片）
$radius-lg: 24rpx;                // 大圆角（大卡片）
$radius-full: 50%;                // 圆形（头像）
```

### 阴影规范（轻量化，避免厚重感）

```scss
// 卡片阴影（整体扁平化）
$shadow-xs: 0 1rpx 3rpx rgba(0, 0, 0, 0.05);    // 微阴影（功能卡片）
$shadow-sm: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);    // 轻阴影（普通卡片）
$shadow-md: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);   // 中阴影（重要卡片）

// 交互阴影（hover 状态）
$shadow-hover: 0 4rpx 16rpx rgba(37, 99, 235, 0.15);  // 品牌蓝色光晕

// 使用策略：
// - 页面底色用灰色 (#F3F4F6) 拉开层级
// - 功能入口卡片：用柔和底色 + 1px 边框，不加阴影
// - 顶部信息卡片：用浅阴影 $shadow-sm 即可
// - 交互按钮：hover 时才显示 $shadow-hover
```

---

## 📦 核心组件设计

### 1. 顶部信息卡片 (UserProfileHeader)

#### 功能描述
展示用户核心信息和统计数据，是用户中心的视觉焦点。

#### 视觉效果
```
┌───────────────────────────────────────┐
│  [背景：紫蓝渐变 + 几何装饰图形]      │
│                                       │
│     ┌────────┐                        │
│     │        │   李明 LV.5 🔥        │
│     │  头像   │   学号：2021001       │
│     │        │   计算机科学与技术     │
│     └────────┘                        │
│                                       │
│  ┌─────────┬─────────┬─────────┬────┐│
│  │  1250   │  LV.5   │   42    │ 18 ││
│  │  积分    │  等级    │  资源   │问答││
│  └─────────┴─────────┴─────────┴────┘│
│                                       │
│          [编辑资料]  [签到]           │
└───────────────────────────────────────┘
```

#### 数据结构
```typescript
interface UserProfileData {
  // 基本信息
  userId: number
  username: string
  nickname: string
  avatar: string | null
  studentId: string | null

  // 学业信息
  major: string | null
  grade: number | null
  schoolName: string | null

  // 积分等级
  points: number
  level: number
  levelName: string           // '青铜', '白银', '黄金', '钻石'
  nextLevelPoints: number     // 距离下一等级所需积分

  // 统计数据
  resourceCount: number       // 上传资源数
  questionCount: number       // 提问数
  answerCount: number         // 回答数
  taskCount: number           // 发布/完成任务数

  // 签到信息
  isCheckedInToday: boolean
  continuousCheckInDays: number
}
```

#### 样式规范
```scss
.profile-header {
  position: relative;
  padding: 48rpx 32rpx;
  background: $header-gradient;
  border-radius: 0 0 32rpx 32rpx;
  overflow: hidden;

  // 背景装饰
  .bg-decoration {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    opacity: 0.1;

    .circle {
      position: absolute;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.2);

      &.circle-1 {
        width: 200rpx;
        height: 200rpx;
        top: -50rpx;
        right: -50rpx;
      }

      &.circle-2 {
        width: 150rpx;
        height: 150rpx;
        bottom: -30rpx;
        left: -30rpx;
      }
    }
  }

  // 用户信息区
  .user-info {
    display: flex;
    align-items: center;
    margin-bottom: 32rpx;

    .avatar {
      width: 120rpx;
      height: 120rpx;
      border-radius: 50%;
      border: 4rpx solid rgba(255, 255, 255, 0.3);
      box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.2);
    }

    .info-text {
      margin-left: 24rpx;
      color: #FFFFFF;

      .nickname {
        font-size: 48rpx;
        font-weight: 600;
        display: flex;
        align-items: center;
        gap: 12rpx;
      }

      .student-id,
      .major {
        font-size: 26rpx;
        opacity: 0.9;
        margin-top: 8rpx;
      }
    }
  }

  // 统计数据网格
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16rpx;
    margin-bottom: 24rpx;

    .stat-item {
      text-align: center;
      padding: 16rpx;
      background: rgba(255, 255, 255, 0.15);
      border-radius: 16rpx;
      backdrop-filter: blur(10rpx);

      .stat-value {
        font-size: 36rpx;
        font-weight: 600;
        color: #FFFFFF;
      }

      .stat-label {
        font-size: 24rpx;
        color: rgba(255, 255, 255, 0.9);
        margin-top: 4rpx;
      }
    }
  }

  // 操作按钮
  .action-buttons {
    display: flex;
    gap: 16rpx;
    justify-content: center;

    .action-btn {
      flex: 1;
      padding: 16rpx 24rpx;
      background: rgba(255, 255, 255, 0.2);
      border: 1rpx solid rgba(255, 255, 255, 0.3);
      border-radius: 48rpx;
      color: #FFFFFF;
      font-size: 28rpx;
      text-align: center;
      backdrop-filter: blur(10rpx);
      transition: all 0.3s;

      &:active {
        transform: scale(0.95);
        background: rgba(255, 255, 255, 0.3);
      }
    }
  }
}
```

### 2. 功能入口网格 (FunctionGrid)

#### 功能描述
提供用户各种功能的快捷入口，采用网格布局。

#### 视觉效果
```
┌─────────────────────────────┐
│   功能中心                  │
│                             │
│  ┌────┐ ┌────┐ ┌────┐ ┌────┐│
│  │📚  │ │💬  │ │📋  │ │⭐  ││
│  │我的│ │我的│ │我的│ │我的││
│  │资源│ │问答│ │任务│ │收藏││
│  └────┘ └────┘ └────┘ └────┘│
│  ┌────┐ ┌────┐ ┌────┐ ┌────┐│
│  │🎁  │ │⚙️  │ │📖  │ │💡  ││
│  │积分│ │设置│ │关于│ │帮助││
│  │记录│ │    │ │    │ │中心││
│  └────┘ └────┘ └────┘ └────┘│
└─────────────────────────────┘
```

#### 功能项配置（按类型分组）
```typescript
interface FunctionItem {
  id: string
  icon: string              // emoji图标
  label: string             // 功能名称
  path: string              // 跳转路径
  type: 'study' | 'asset' | 'system'  // 功能类型（决定配色）
  description?: string      // 功能描述
  badge?: number            // 角标数字（如未读消息）
  requiredAuth?: boolean    // 是否需要登录
}

const functionItems: FunctionItem[] = [
  // 学习相关（蓝系）
  {
    id: 'my-resources',
    icon: '📚',
    label: '我的资源',
    path: '/pages/user/my-resources',
    type: 'study',
    requiredAuth: true
  },
  {
    id: 'my-questions',
    icon: '💬',
    label: '我的问答',
    path: '/pages/question/my',
    type: 'study',
    requiredAuth: true
  },
  {
    id: 'my-tasks',
    icon: '📋',
    label: '我的任务',
    path: '/pages/user/my-tasks',
    type: 'study',
    requiredAuth: true
  },

  // 个人资产（黄橙系）
  {
    id: 'my-favorites',
    icon: '⭐',
    label: '我的收藏',
    path: '/pages/user/favorites',
    type: 'asset',
    requiredAuth: true
  },
  {
    id: 'points-history',
    icon: '🎁',
    label: '积分记录',
    path: '/pages/user/points',
    type: 'asset',
    requiredAuth: true
  },

  // 系统类（中性灰）
  {
    id: 'settings',
    icon: '⚙️',
    label: '设置',
    path: '/pages/user/settings',
    type: 'system',
    requiredAuth: false
  },
  {
    id: 'about',
    icon: '📖',
    label: '关于',
    path: '/pages/user/about',
    type: 'system',
    requiredAuth: false
  },
  {
    id: 'help',
    icon: '💡',
    label: '帮助中心',
    path: '/pages/user/help',
    type: 'system',
    requiredAuth: false
  }
]

// 类型配色映射
const typeColorMap = {
  study: {
    bg: '#EFF6FF',      // 柔和蓝底
    icon: '#2563EB',    // 品牌蓝
    border: '#DBEAFE'
  },
  asset: {
    bg: '#FFFBEB',      // 柔和黄底
    icon: '#F59E0B',    // 暖黄
    border: '#FEF3C7'
  },
  system: {
    bg: '#F3F4F6',      // 中性灰底
    icon: '#6B7280',    // 灰色
    border: '#E5E7EB'
  }
}
```

#### 样式规范（轻量化设计）
```scss
.function-grid {
  padding: 32rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  margin: 24rpx 32rpx;
  box-shadow: $shadow-sm;

  .grid-title {
    font-size: 36rpx;
    font-weight: 600;
    color: $text-primary;
    margin-bottom: 24rpx;
  }

  .grid-container {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20rpx;

    // 移动端改为2列
    @media (max-width: 750rpx) {
      grid-template-columns: repeat(2, 1fr);
    }
  }

  .function-item {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 24rpx 16rpx;
    border-radius: 16rpx;
    border: 1px solid transparent;
    transition: all 0.2s;
    cursor: pointer;

    // 按类型应用背景色
    &.type-study {
      background: #EFF6FF;
      border-color: #DBEAFE;

      .icon { color: #2563EB; }
    }

    &.type-asset {
      background: #FFFBEB;
      border-color: #FEF3C7;

      .icon { color: #F59E0B; }
    }

    &.type-system {
      background: #F3F4F6;
      border-color: #E5E7EB;

      .icon { color: #6B7280; }
    }

    &:hover {
      transform: translateY(-2rpx);  // 降低移动距离
      box-shadow: $shadow-hover;
    }

    &:active {
      transform: scale(0.98);  // 轻微缩放
    }

    .icon-wrapper {
      position: relative;
      width: 80rpx;  // 缩小图标容器
      height: 80rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 12rpx;

      .icon {
        font-size: 44rpx;  // emoji 大小
        filter: none;  // 不添加额外滤镜
      }

      // 角标
      .badge {
        position: absolute;
        top: -8rpx;
        right: -8rpx;
        min-width: 32rpx;
        height: 32rpx;
        padding: 0 8rpx;
        background: $danger-red;
        color: #FFFFFF;
        font-size: 20rpx;
        border-radius: 16rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 2rpx 8rpx rgba(239, 68, 68, 0.3);
      }
    }

    .label {
      font-size: 26rpx;
      color: $text-secondary;
      text-align: center;
      font-weight: 500;
    }
  }
}
```

### 3. 账号管理区 (AccountActions)

#### 功能描述
提供修改资料、修改密码、退出登录等账号相关操作。

#### 视觉效果
```
┌─────────────────────────────┐
│   账号管理                  │
│                             │
│  ┌─────────────────────────┐│
│  │ 👤  修改资料            >││
│  └─────────────────────────┘│
│  ┌─────────────────────────┐│
│  │ 🔒  修改密码            >││
│  └─────────────────────────┘│
│  ┌─────────────────────────┐│
│  │ 🚪  退出登录            >││
│  └─────────────────────────┘│
└─────────────────────────────┘
```

#### 样式规范
```scss
.account-actions {
  padding: 32rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  margin: 24rpx 32rpx;
  box-shadow: $shadow-sm;

  .actions-title {
    font-size: 36rpx;
    font-weight: 600;
    color: $text-primary;
    margin-bottom: 24rpx;
  }

  .action-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 24rpx;
    background: $bg-tertiary;
    border-radius: 12rpx;
    margin-bottom: 16rpx;
    transition: all 0.2s;
    cursor: pointer;

    &:last-child {
      margin-bottom: 0;
    }

    &:hover {
      background: darken($bg-tertiary, 3%);
    }

    &:active {
      transform: scale(0.98);
    }

    &.danger {
      .action-text {
        color: $danger-red;
      }
    }

    .action-left {
      display: flex;
      align-items: center;
      gap: 16rpx;

      .action-icon {
        font-size: 40rpx;
      }

      .action-text {
        font-size: 30rpx;
        color: $text-primary;
      }
    }

    .action-arrow {
      font-size: 32rpx;
      color: $text-tertiary;
    }
  }
}
```

---

## 🔌 API 接口设计

### 用户信息相关

#### 1. 获取当前用户完整信息
```
GET /api/v1/user/profile
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "uId": 1,
    "username": "student001",
    "nickname": "李明",
    "email": "liming@example.com",
    "phone": "138****1234",
    "avatarUrl": "https://oss.example.com/avatar/1.jpg",
    "role": "student",
    "studentId": "2021001",
    "schoolId": 1,
    "schoolName": "清华大学",
    "major": "计算机科学与技术",
    "grade": 2021,
    "points": 1250,
    "level": 5,
    "createdAt": "2021-09-01T08:00:00"
  }
}
```

#### 2. 更新用户资料
```
PUT /api/v1/user/profile
Authorization: Bearer {token}
Content-Type: application/json

Request:
{
  "nickname": "新昵称",
  "email": "newemail@example.com",
  "phone": "13800138000",
  "avatarUrl": "https://oss.example.com/avatar/new.jpg",
  "major": "软件工程",
  "grade": 2021
}

Response:
{
  "code": 200,
  "message": "资料更新成功",
  "data": { ...updatedProfile }
}
```

#### 3. 获取用户统计数据
```
GET /api/v1/user/stats
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "resourceCount": 42,        // 上传资源数
    "downloadCount": 128,       // 下载次数
    "questionCount": 18,        // 提问数
    "answerCount": 35,          // 回答数
    "acceptedAnswerCount": 12,  // 被采纳回答数
    "taskPublishCount": 5,      // 发布任务数
    "taskCompleteCount": 8,     // 完成任务数
    "favoriteCount": 56,        // 收藏数
    "likeCount": 89,            // 获赞数
    "checkInDays": 15           // 连续签到天数
  }
}
```

#### 4. 获取积分记录
```
GET /api/v1/user/points/log?page=1&pageSize=20
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 1,
        "points": 10,
        "type": "upload",          // upload/download/answer/accept/task/checkin
        "description": "上传资源《算法导论》",
        "createdAt": "2025-01-19T10:30:00"
      },
      ...
    ],
    "total": 156,
    "page": 1,
    "pageSize": 20
  }
}
```

#### 5. 每日签到
```
POST /api/v1/user/check-in
Authorization: Bearer {token}

Response:
{
  "code": 200,
  "message": "签到成功",
  "data": {
    "points": 10,
    "continuousDays": 5,
    "totalCheckInDays": 25,
    "isCheckedInToday": true
  }
}
```

---

## 📱 页面交互设计

### 进入用户中心

**触发方式**:
1. 点击首页顶部头像
2. 点击底部 TabBar "我的"标签
3. 从其他页面点击"个人中心"链接

**进入动画**:
- 从右侧滑入（uni.navigateTo）
- 或 TabBar 切换无动画（uni.switchTab）

### 数据加载

**加载时机**:
1. 页面 onLoad: 加载基本用户信息
2. 页面 onShow: 刷新统计数据
3. 下拉刷新: 刷新所有数据

**加载状态**:
```
初始加载: 显示骨架屏
下拉刷新: 顶部loading动画
加载失败: 显示错误提示 + 重试按钮
```

### 编辑资料

**交互流程**:
```
1. 点击"编辑资料"按钮
2. 跳转到编辑资料页（/pages/user/edit-profile）
3. 修改信息
4. 点击"保存"
5. 调用API保存
6. 返回用户中心，显示最新数据
```

### 签到功能

**交互流程**:
```
1. 点击"签到"按钮
2. 调用签到API
3. 显示签到成功动画 + 获得积分数
4. 更新签到状态（按钮变为"已签到"）
5. 更新积分数字（带动画）
```

**签到动画**:
- 按钮震动
- 积分数字跳动
- 显示"+10积分"浮动文字

### 退出登录

**交互流程**:
```
1. 点击"退出登录"
2. 弹出确认对话框
   "确定要退出登录吗？"
   [取消] [确定]
3. 点击"确定"
4. 清除本地token和用户信息
5. 跳转到首页
6. 显示登录按钮
```

---

## 🎯 用户等级体系设计

### 等级划分

| 等级 | 等级名称 | 所需积分 | 徽章颜色 | 徽章图标 |
|------|---------|---------|---------|---------|
| 1-2  | 青铜    | 0-99    | 🥉 棕色  | Bronze  |
| 3-4  | 白银    | 100-499 | 🥈 银色  | Silver  |
| 5-6  | 黄金    | 500-1499| 🥇 金色  | Gold    |
| 7-8  | 钻石    | 1500-3499| 💎 蓝色 | Diamond |
| 9-10 | 大师    | 3500+   | 👑 彩色  | Master  |

### 等级权益

```typescript
interface LevelPrivilege {
  level: number
  levelName: string
  privileges: string[]
}

const levelPrivileges: LevelPrivilege[] = [
  {
    level: 1,
    levelName: '青铜 I',
    privileges: [
      '每日签到+5积分',
      '资源下载限制10次/天',
      '提问限制5次/天'
    ]
  },
  {
    level: 3,
    levelName: '白银 I',
    privileges: [
      '每日签到+10积分',
      '资源下载限制20次/天',
      '提问限制10次/天',
      '专属白银徽章'
    ]
  },
  {
    level: 5,
    levelName: '黄金 I',
    privileges: [
      '每日签到+15积分',
      '资源下载无限制',
      '提问无限制',
      '优先展示问答',
      '专属黄金徽章'
    ]
  },
  {
    level: 7,
    levelName: '钻石 I',
    privileges: [
      '每日签到+20积分',
      '所有黄金权益',
      '发布任务免手续费',
      '回答优先展示',
      '专属钻石徽章'
    ]
  },
  {
    level: 9,
    levelName: '大师 I',
    privileges: [
      '每日签到+30积分',
      '所有钻石权益',
      '专属客服支持',
      '管理员推荐',
      '专属大师徽章'
    ]
  }
]
```

---

## 📐 响应式设计

### 断点定义

```scss
// 移动端（手机）
$breakpoint-mobile: 750rpx;       // < 750rpx

// 平板端
$breakpoint-tablet: 1200rpx;      // 750rpx - 1200rpx

// 桌面端
$breakpoint-desktop: 1200rpx;     // > 1200rpx
```

### 布局适配

#### 移动端 (< 750rpx)
- 功能网格: 2列
- 顶部卡片: 全宽，圆角底部
- 统计数据: 4列紧凑布局
- 间距: 正常

#### 平板端 (750rpx - 1200rpx)
- 功能网格: 4列
- 顶部卡片: 全宽，圆角底部
- 统计数据: 4列
- 间距: 增大

#### 桌面端 (> 1200rpx)
- 功能网格: 4列
- 顶部卡片: 最大宽度1200rpx，居中
- 统计数据: 4列，图标更大
- 间距: 最大

---

## 🔒 权限控制

### 未登录状态

显示内容:
- 默认头像和昵称"未登录"
- 统计数据全部显示为0
- 功能按钮点击后跳转到登录页

### 已登录状态

显示内容:
- 真实用户信息
- 真实统计数据
- 功能按钮正常跳转

---

## 🚀 性能优化

### 数据缓存

```typescript
// 缓存策略
const cacheConfig = {
  userProfile: {
    key: 'campuslink_user_profile',
    expire: 5 * 60 * 1000      // 5分钟
  },
  userStats: {
    key: 'campuslink_user_stats',
    expire: 10 * 60 * 1000     // 10分钟
  }
}
```

### 图片优化

- 头像压缩: 使用OSS缩略图 `?x-oss-process=image/resize,w_200`
- 懒加载: 非首屏图片懒加载
- 占位图: 加载时显示渐变占位图

### 动画优化

- 使用 CSS transform 而非 position
- 使用 will-change 提示浏览器
- 避免频繁操作DOM

---

## ✅ 设计检查清单

### 视觉设计
- [x] 色彩方案定义完整
- [x] 字体规范明确
- [x] 间距规范统一
- [x] 圆角规范一致
- [x] 阴影效果适度

### 组件设计
- [x] 顶部信息卡片设计
- [x] 功能入口网格设计
- [x] 账号管理区设计
- [x] 组件可复用性高

### 交互设计
- [x] 进入动画流畅
- [x] 加载状态清晰
- [x] 错误提示友好
- [x] 操作反馈及时

### 响应式设计
- [x] 移动端适配
- [x] 平板端适配
- [x] 桌面端适配
- [x] 断点定义合理

### API设计
- [x] 接口定义完整
- [x] 参数命名规范
- [x] 响应格式统一
- [x] 错误处理完善

---

## 📝 附录

### 参考页面
- 首页顶部栏: `/pages/home/components/TopFocusBar.vue`
- 问答详情页: `/pages/question/detail.vue`
- 资源详情页: `/pages/resource/detail.vue`

### 设计工具
- Figma: UI设计
- Sketch: 原型设计
- IconPark: 图标库

### 相关文档
- [API设计文档](./api-design.md)
- [数据库设计文档](./database-design.md)
- [前端开发规范](./frontend-guideline.md)

---

**文档结束**

*下一步: 编写《用户中心开发文档》*
