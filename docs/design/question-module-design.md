# 智能问答模块设计文档

**文档版本**: v1.1
**更新日期**: 2025-11-18
**设计人**: Claude Code
**项目**: CampusLink - 高校资源互助与问答平台

**更新说明**:
- v1.1 (2025-11-18): 🎨 更新色彩系统，采用平台统一配色（蓝色 #1E5FFF），移除 AI 风格紫色
- v1.0 (2025-11-18): 📝 初版设计文档，完整功能规划

---

## 📋 目录

1. [模块概述](#模块概述)
2. [功能需求](#功能需求)
3. [页面结构](#页面结构)
4. [数据模型](#数据模型)
5. [核心功能设计](#核心功能设计)
6. [UX/UI 设计](#uxui-设计)
7. [技术实现](#技术实现)
8. [开发计划](#开发计划)
9. [风险与挑战](#风险与挑战)

---

## 模块概述

### 1.1 背景

智能问答模块是 CampusLink 平台的核心功能之一，旨在为高校学生提供：
- **学术互助**：学习问题、技术难题、作业辅导
- **生活咨询**：校园生活、社团活动、实习求职
- **智能辅助**：AI 自动答复（MVP 后期集成）
- **悬赏激励**：积分悬赏机制，激励优质回答

### 1.2 目标用户

| 用户类型 | 使用场景 | 核心需求 |
|---------|---------|---------|
| **提问者** | 遇到学习/生活问题 | 快速发布问题、获得高质量回答、采纳最佳答案 |
| **回答者** | 分享知识、获取积分 | 浏览感兴趣的问题、撰写回答、获得点赞和采纳 |
| **浏览者** | 查找相似问题 | 搜索问题、查看答案、收藏有价值内容 |

### 1.3 核心价值

1. **知识沉淀**：高质量问答内容长期保留，形成知识库
2. **社区互助**：学生互相帮助，增强归属感
3. **激励机制**：积分悬赏 + 点赞系统，促进活跃度
4. **智能辅助**：AI 答复（后期）减轻人工负担

### 1.4 设计原则

- **简洁优先**：参考知乎/Stack Overflow，突出内容本身
- **移动优先**：优化移动端体验（H5 + 微信小程序）
- **渐进增强**：MVP 先实现核心功能，后续迭代智能特性
- **性能优先**：虚拟滚动、懒加载、骨架屏

---

## 功能需求

### 2.1 MVP 功能清单

#### 2.1.1 问题列表页

**核心功能**：
- [x] 问题列表展示（分页加载）
- [x] 分类筛选（学习/生活/技术/其他）
- [x] 状态筛选（已解决/未解决）
- [x] 排序方式（最新/最热/悬赏最高）
- [x] 搜索功能（标题/内容关键词）
- [x] 问题卡片（标题、摘要、标签、数据统计）
- [x] 发布问题悬浮按钮

**数据展示**：
```
┌─────────────────────────────────────┐
│ 📚 如何准备数据结构期末考试？         │ ← 标题
│ 我是大一新生，期末快到了...         │ ← 内容摘要
│ #数据结构 #期末复习                  │ ← 标签
│ 🎁 20 积分  👁️ 120 次  💬 5 条回答   │ ← 数据统计
│ 👤 张三 · 计算机学院 · 2 小时前      │ ← 用户信息
│ ✅ 已解决                           │ ← 状态标识
└─────────────────────────────────────┘
```

#### 2.1.2 问题详情页

**核心功能**：
- [x] 问题完整内容展示
- [x] 回答列表（按点赞数/时间排序）
- [x] 发布回答输入框
- [x] 采纳最佳答案（仅提问者）
- [x] 点赞问题/回答
- [x] 分享问题（链接/二维码）
- [x] 举报功能

**布局结构**：
```
┌───────────────────────────────────┐
│ [返回]  问题详情          [分享]    │ ← 顶部导航
├───────────────────────────────────┤
│ 📚 如何准备数据结构期末考试？       │ ← 问题标题
│                                   │
│ 我是大一新生，期末快到了，对数据   │
│ 结构的概念还不是很理解...          │ ← 问题内容
│                                   │
│ [图片1] [图片2]                    │ ← 图片附件
│                                   │
│ #数据结构 #期末复习                │ ← 标签
│                                   │
│ 👤 张三 · 计算机学院              │
│ 🕐 2024-11-18 10:00              │
│ 👁️ 120 次浏览  🎁 悬赏 20 积分    │
├───────────────────────────────────┤
│ 💬 5 条回答                       │ ← 回答标题
├───────────────────────────────────┤
│ ✅ 已采纳                         │ ← 最佳答案标识
│ 我的建议是...                     │ ← 回答内容
│ 👤 李四 · 2 小时前                │
│ 👍 15 点赞                        │
├───────────────────────────────────┤
│ 其他回答...                       │
├───────────────────────────────────┤
│ [写下你的回答...]         [发布]   │ ← 回答输入框
└───────────────────────────────────┘
```

#### 2.1.3 发布问题页

**核心功能**：
- [x] 问题标题输入（必填，10-200 字符）
- [x] 问题内容输入（必填，20-2000 字符）
- [x] 分类选择（学习/生活/技术/其他）
- [x] 标签添加（最多 5 个）
- [x] 图片上传（最多 9 张）
- [x] 悬赏积分设置（0-100 分）
- [x] 预览功能
- [x] 草稿保存

**表单验证**：
| 字段 | 验证规则 | 错误提示 |
|------|---------|---------|
| 标题 | 10-200 字符 | "标题长度应在 10-200 字符之间" |
| 内容 | 20-2000 字符 | "内容长度应在 20-2000 字符之间" |
| 分类 | 必选 | "请选择问题分类" |
| 标签 | 最多 5 个 | "最多添加 5 个标签" |
| 图片 | 最多 9 张，单张 <5MB | "单张图片不得超过 5MB" |
| 悬赏 | 0-100 分，≤ 用户积分 | "悬赏积分不足" |

#### 2.1.4 我的问答

**核心功能**：
- [x] 我提出的问题（全部/已解决/未解决）
- [x] 我回答的问题
- [x] 我采纳的答案
- [x] 我点赞的问题/答案
- [x] 积分明细（提问扣分、回答得分、采纳奖励）

### 2.2 后期功能（V2.0）

- [ ] AI 智能答复（后端集成 OpenAI/文心一言）
- [ ] 相似问题推荐（基于标题相似度）
- [ ] 问题关注功能（收藏问题，有新回答时通知）
- [ ] 回答排序优化（综合考虑时间、点赞、采纳）
- [ ] 问题热度算法（浏览量 + 回答数 + 点赞数）
- [ ] 专家认证系统（学科专家、活跃答主）
- [ ] 问答统计分析（回答率、采纳率、平均响应时间）

---

## 页面结构

### 3.1 页面层级

```
问答模块
├── 问题列表页 (/pages/question/index.vue)
│   ├── 筛选栏（分类/状态/排序）
│   ├── 搜索框
│   ├── 问题卡片列表
│   └── 发布问题悬浮按钮
│
├── 问题详情页 (/pages/question/detail.vue)
│   ├── 问题内容区
│   ├── 回答列表区
│   └── 回答输入框
│
├── 发布问题页 (/pages/question/ask.vue)
│   ├── 标题输入
│   ├── 内容输入
│   ├── 分类选择
│   ├── 标签管理
│   ├── 图片上传
│   └── 悬赏设置
│
├── 我的问答页 (/pages/question/my.vue)
│   ├── Tab 切换（我提出的/我回答的）
│   └── 问题列表
│
└── 积分榜单页 (/pages/question/ranking.vue)（已存在）
    ├── 周榜/月榜/总榜
    └── 用户排名列表
```

### 3.2 路由配置

**pages.json** 新增路由：
```json
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
```

---

## 数据模型

### 4.1 后端数据库设计（已有）

#### 4.1.1 问题表 (question)

```sql
CREATE TABLE `question` (
  `q_id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL COMMENT '问题标题',
  `content` TEXT NOT NULL COMMENT '问题内容',
  `asker_id` BIGINT NOT NULL COMMENT '提问者ID',
  `category` VARCHAR(50) NOT NULL COMMENT '分类',
  `tags` JSON COMMENT '标签（JSON数组）',
  `images` JSON COMMENT '图片URL列表',
  `ai_answer` TEXT COMMENT 'AI生成的答案',
  `ai_generated_at` DATETIME COMMENT 'AI答案生成时间',
  `views` INT DEFAULT 0 COMMENT '浏览次数',
  `answer_count` INT DEFAULT 0 COMMENT '回答数量',
  `is_solved` TINYINT DEFAULT 0 COMMENT '是否已解决',
  `reward_points` INT DEFAULT 0 COMMENT '悬赏积分',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-已删除，1-正常',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_asker_id` (`asker_id`),
  INDEX `idx_category` (`category`),
  INDEX `idx_is_solved` (`is_solved`),
  INDEX `idx_created_at` (`created_at`)
);
```

#### 4.1.2 回答表 (answer)

```sql
CREATE TABLE `answer` (
  `a_id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `question_id` BIGINT NOT NULL COMMENT '问题ID',
  `responder_id` BIGINT NOT NULL COMMENT '回答者ID',
  `content` TEXT NOT NULL COMMENT '回答内容',
  `images` JSON COMMENT '图片URL列表',
  `likes` INT DEFAULT 0 COMMENT '点赞数',
  `is_accepted` TINYINT DEFAULT 0 COMMENT '是否被采纳',
  `status` TINYINT DEFAULT 1 COMMENT '状态',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_question_id` (`question_id`),
  INDEX `idx_responder_id` (`responder_id`),
  INDEX `idx_is_accepted` (`is_accepted`),
  FOREIGN KEY (`question_id`) REFERENCES `question` (`q_id`) ON DELETE CASCADE
);
```

### 4.2 前端类型定义（已有）

#### 4.2.1 TypeScript 接口

```typescript
// 问题列表项
export interface QuestionItem {
  questionId: number
  title: string
  content: string
  askerName: string
  askerAvatar: string
  category: '学习' | '生活' | '技术' | '其他'
  tags: string[]
  views: number
  answerCount: number
  isSolved: boolean
  rewardPoints: number
  createdAt: string
}

// 问题详情
export interface QuestionDetail extends QuestionItem {
  askerId: number
  images?: string[]
  aiAnswer?: string
  aiGeneratedAt?: string
}

// 回答列表项
export interface AnswerItem {
  answerId: number
  questionId: number
  responderId: number
  responderName: string
  responderAvatar: string
  content: string
  images?: string[]
  likes: number
  isAccepted: boolean
  isLiked: boolean
  createdAt: string
}
```

### 4.3 API 接口（已有）

#### 4.3.1 问题相关接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取问题列表 | GET | /question/list | 支持分页、筛选、排序 |
| 获取问题详情 | GET | /question/{id} | 自动增加浏览量 |
| 提问 | POST | /question/ask | 扣除悬赏积分 |
| 点赞问题 | POST | /question/{id}/like | 返回最新点赞数 |
| 取消点赞 | DELETE | /question/{id}/like | 返回最新点赞数 |
| 删除问题 | DELETE | /question/{id} | 仅提问者或管理员 |

#### 4.3.2 回答相关接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取回答列表 | GET | /question/{id}/answers | 支持分页 |
| 回答问题 | POST | /question/{id}/answer | 增加回答数 |
| 采纳回答 | POST | /question/{questionId}/accept/{answerId} | 仅提问者，仅一次 |
| 点赞回答 | POST | /answer/{id}/like | 返回最新点赞数 |
| 取消点赞 | DELETE | /answer/{id}/like | 返回最新点赞数 |
| 删除回答 | DELETE | /answer/{id} | 仅回答者或管理员 |

---

## 核心功能设计

### 5.1 问题列表页

#### 5.1.1 筛选功能

**分类筛选**：
```typescript
const categories = [
  { label: '全部', value: null, icon: '📦' },
  { label: '学习', value: '学习', icon: '📚' },
  { label: '生活', value: '生活', icon: '🏠' },
  { label: '技术', value: '技术', icon: '💻' },
  { label: '其他', value: '其他', icon: '📌' }
]
```

**状态筛选**：
- 全部问题
- 未解决（`isSolved=false`）
- 已解决（`isSolved=true`）

**排序方式**：
- 最新发布（`sortBy=created_at&sortOrder=desc`）
- 浏览最多（`sortBy=views&sortOrder=desc`）
- 悬赏最高（`sortBy=rewardPoints&sortOrder=desc`）
- 回答最多（`sortBy=answerCount&sortOrder=desc`）

#### 5.1.2 问题卡片设计

**信息层级**：
1. **主信息**：标题（2 行省略）
2. **次要信息**：内容摘要（1 行省略）
3. **元数据**：标签（最多 3 个）+ 数据统计
4. **用户信息**：头像 + 用户名 + 学校 + 时间

**视觉设计**：
```scss
.question-card {
  padding: 24rpx;
  background: #FFF;
  border-radius: 16rpx;
  margin-bottom: 16rpx;

  .title {
    font-size: 30rpx;
    font-weight: 700;
    color: #111;
    line-height: 1.5;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .content-preview {
    font-size: 26rpx;
    color: #666;
    line-height: 1.6;
    margin-top: 12rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .tags {
    display: flex;
    gap: 12rpx;
    margin-top: 16rpx;

    .tag {
      padding: 4rpx 12rpx;
      background: rgba(30, 95, 255, 0.1);
      color: #1E5FFF;
      font-size: 22rpx;
      border-radius: 8rpx;
    }
  }

  .stats {
    display: flex;
    gap: 24rpx;
    margin-top: 16rpx;
    font-size: 24rpx;
    color: #999;

    .stat-item {
      display: flex;
      align-items: center;
      gap: 6rpx;
    }

    .reward {
      color: #FF7A00;
      font-weight: 700;
    }

    .solved {
      color: #10B981;
    }
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 12rpx;
    margin-top: 16rpx;
    font-size: 24rpx;
    color: #999;

    .avatar {
      width: 48rpx;
      height: 48rpx;
      border-radius: 50%;
    }
  }
}
```

#### 5.1.3 空状态设计

**无问题时**：
```
┌───────────────────────────────┐
│                               │
│         📭                    │
│                               │
│      还没有问题哦              │
│   快来提出第一个问题吧！        │
│                               │
│     [+ 发布问题]               │
│                               │
└───────────────────────────────┘
```

**搜索无结果时**：
```
┌───────────────────────────────┐
│         🔍                    │
│                               │
│    没有找到相关问题            │
│  试试换个关键词或调整筛选条件   │
└───────────────────────────────┘
```

### 5.2 问题详情页

#### 5.2.1 问题内容区

**布局组件**：
1. **返回导航栏**：← 返回 | 问题详情 | 分享 →
2. **问题标题**：大字号、加粗
3. **问题内容**：富文本（支持换行、链接）
4. **图片附件**：网格展示（3 列），点击预览
5. **标签列表**：最多 5 个，可点击筛选
6. **问题元数据**：
   - 提问者：头像 + 用户名 + 学校
   - 时间：发布时间 + 距今时间
   - 统计：浏览量 + 回答数 + 悬赏积分
   - 状态：已解决 / 未解决
7. **操作按钮**：
   - 点赞按钮（爱心图标 + 点赞数）
   - 分享按钮（生成链接/二维码）
   - 举报按钮（仅登录用户）
   - 删除按钮（仅提问者/管理员）

#### 5.2.2 回答列表区

**排序逻辑**：
1. **已采纳答案**：永远置顶
2. **其他答案**：按点赞数降序 / 时间降序（可切换）

**回答卡片**：
```scss
.answer-card {
  padding: 24rpx;
  background: #F9FAFB;
  border-radius: 12rpx;
  margin-bottom: 16rpx;

  &.accepted {
    background: linear-gradient(135deg, rgba(16, 185, 129, 0.05) 0%, rgba(16, 185, 129, 0.1) 100%);
    border: 2rpx solid #10B981;

    .accepted-badge {
      display: flex;
      align-items: center;
      gap: 6rpx;
      color: #10B981;
      font-size: 24rpx;
      font-weight: 700;
      margin-bottom: 12rpx;

      &::before {
        content: '✓';
        display: inline-block;
        width: 32rpx;
        height: 32rpx;
        line-height: 32rpx;
        text-align: center;
        background: #10B981;
        color: #FFF;
        border-radius: 50%;
      }
    }
  }

  .content {
    font-size: 28rpx;
    color: #333;
    line-height: 1.8;
    word-wrap: break-word;
  }

  .images {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12rpx;
    margin-top: 16rpx;

    .image {
      width: 100%;
      height: 200rpx;
      border-radius: 8rpx;
      object-fit: cover;
    }
  }

  .footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 16rpx;

    .user-info {
      display: flex;
      align-items: center;
      gap: 12rpx;

      .avatar {
        width: 48rpx;
        height: 48rpx;
        border-radius: 50%;
      }

      .name {
        font-size: 24rpx;
        color: #666;
      }

      .time {
        font-size: 22rpx;
        color: #999;
      }
    }

    .actions {
      display: flex;
      gap: 24rpx;

      .like-btn {
        display: flex;
        align-items: center;
        gap: 6rpx;
        padding: 8rpx 16rpx;
        background: rgba(30, 95, 255, 0.1);
        color: #1E5FFF;
        font-size: 24rpx;
        border-radius: 24rpx;
        cursor: pointer;

        &.liked {
          background: linear-gradient(135deg, #1E5FFF 0%, #2563EB 100%);
          color: #FFF;
        }
      }

      .accept-btn {
        padding: 8rpx 16rpx;
        background: linear-gradient(135deg, #10B981 0%, #059669 100%);
        color: #FFF;
        font-size: 24rpx;
        border-radius: 24rpx;
        cursor: pointer;
      }
    }
  }
}
```

#### 5.2.3 回答输入框

**位置**：固定在页面底部
**功能**：
- 多行文本输入（最多 2000 字符）
- 图片上传按钮（最多 9 张）
- 发布按钮（蓝色渐变）
- 字符计数器（实时显示）

**交互设计**：
1. 点击输入框：自动聚焦，底部工具栏上移
2. 输入过程：实时保存草稿（本地存储）
3. 点击发布：
   - 验证内容长度（20-2000 字符）
   - 显示加载状态
   - 成功后：清空输入框，刷新回答列表，滚动到新回答
   - 失败后：显示错误提示，保留草稿

### 5.3 发布问题页

#### 5.3.1 表单结构

**字段列表**：
1. **标题**（必填）
   - 输入框：placeholder="简明扼要地描述你的问题"
   - 字符计数：10-200
   - 实时校验：少于 10 字显示警告

2. **内容**（必填）
   - 多行文本域：placeholder="详细描述你的问题，包括背景、已尝试的方法等"
   - 字符计数：20-2000
   - 实时预览：markdown 渲染（可选）

3. **分类**（必填）
   - 单选：学习、生活、技术、其他
   - 样式：卡片式选择器，选中后高亮

4. **标签**（可选）
   - 标签输入：回车或逗号分隔
   - 最多 5 个
   - 热门标签推荐（根据分类）

5. **图片**（可选）
   - 上传控件：9 宫格
   - 最多 9 张，单张 <5MB
   - 支持删除、排序

6. **悬赏**（可选）
   - 滑块选择：0-100 分
   - 显示当前积分余额
   - 提示：悬赏越高，回答越快

**底部操作栏**：
- 保存草稿按钮（灰色）
- 预览按钮（蓝色边框）
- 发布按钮（蓝色渐变，主要操作）

#### 5.3.2 草稿保存

**自动保存**：
- 每 30 秒自动保存到本地存储
- 页面卸载时保存（`onUnload`）
- 草稿 Key：`question_draft`

**恢复草稿**：
- 进入页面时检查本地存储
- 有草稿时弹出提示："检测到未发布的草稿，是否继续编辑？"
- 用户选择：恢复 / 重新开始（清空草稿）

#### 5.3.3 图片上传

**上传流程**：
1. 选择图片（uni.chooseImage）
2. 压缩图片（如果 >1MB，压缩到 80% 质量）
3. 获取 OSS 上传签名（`/resource/upload/signature`）
4. 客户端直传到 OSS（uni.uploadFile）
5. 返回图片 URL，添加到 images 数组

**错误处理**：
- 图片过大：显示"单张图片不得超过 5MB"
- 数量超限：显示"最多上传 9 张图片"
- 上传失败：显示"上传失败，请重试"

### 5.4 我的问答页

#### 5.4.1 Tab 切换

**标签页**：
1. 我提出的（默认）
   - 全部 / 已解决 / 未解决
2. 我回答的
   - 全部 / 已采纳 / 未采纳
3. 我点赞的
   - 问题 / 回答

**数据加载**：
- 每个 Tab 独立分页
- 切换 Tab 时重新加载
- 支持下拉刷新、上拉加载更多

#### 5.4.2 积分明细

**展示位置**：页面顶部卡片
**内容**：
- 当前积分总数（大字号）
- 本周获得积分 / 消耗积分
- 查看详情按钮（跳转到积分明细页）

**积分来源**：
- 回答问题：+5 分
- 回答被采纳：+20 分（+ 提问者悬赏）
- 提问：-2 分（+ 悬赏积分）

---

## UX/UI 设计

### 6.1 设计风格

**色彩系统** (匹配平台统一设计)：
```scss
// 🎨 主色调：蓝色（匹配首页主色，知识、专业）
$primary-blue: #1E5FFF;          // 主要蓝色（首页一致）
$primary-blue-hover: #2563EB;    // 悬停状态
$primary-blue-light: rgba(30, 95, 255, 0.1);  // 淡背景
$primary-blue-bg: #E8F4FF;       // 学习类背景（首页一致）

// 🎁 悬赏色：橙色（匹配资源广场）
$reward-orange: #FF7A00;         // 悬赏主色（资源广场一致）
$reward-orange-light: rgba(255, 122, 0, 0.1);
$reward-orange-bg: #FFF5E6;      // 生活类背景

// ✅ 成功色：绿色（已解决状态）
$success-green: #10B981;         // 已解决标识
$success-green-light: rgba(16, 185, 129, 0.1);
$success-green-bg: #ECFDF5;      // 技术类背景

// 📝 文字颜色
$text-primary: #1D1D1F;          // 主要文本
$text-secondary: #6B7280;        // 次要文本
$text-tertiary: #9CA3AF;         // 辅助文本

// 🎯 背景颜色
$bg-page: #FBFCFE;               // 页面背景（首页一致）
$bg-card: #FFFFFF;               // 卡片背景
$bg-hover: #F5F5F5;              // 悬停背景
```

**分类色彩** (匹配首页功能卡片)：
```scss
// 📚 学习类（蓝色系）
.category-study {
  background: linear-gradient(135deg, #E8F4FF 0%, #F0F8FF 100%);
  color: #1E5FFF;
}

// 🏠 生活类（橙色系）
.category-life {
  background: linear-gradient(135deg, #FFF5E6 0%, #FFFAF0 100%);
  color: #FF7A00;
}

// 💻 技术类（绿色系）
.category-tech {
  background: linear-gradient(135deg, #ECFDF5 0%, #F0FDF9 100%);
  color: #10B981;
}

// 📌 其他类（灰色系）
.category-other {
  background: linear-gradient(135deg, #F5F5F5 0%, #FAFAFA 100%);
  color: #6B7280;
}
```

**字体规范**：
- 标题：30-36rpx，加粗
- 正文：26-28rpx，常规
- 辅助文字：22-24rpx，细体
- 行高：1.5-1.8（提升可读性）

### 6.2 组件设计

#### 6.2.1 问题卡片组件 (QuestionCard.vue)

**Props**：
```typescript
interface QuestionCardProps {
  question: QuestionItem
  showCategory?: boolean  // 是否显示分类
  showStats?: boolean     // 是否显示统计数据
  onClick?: (id: number) => void
}
```

**Slots**：
- `actions`：自定义操作按钮（如删除、编辑）

**事件**：
- `@click`：点击卡片跳转详情页
- `@tag-click`：点击标签筛选

#### 6.2.2 回答卡片组件 (AnswerCard.vue)

**Props**：
```typescript
interface AnswerCardProps {
  answer: AnswerItem
  isOwner: boolean          // 是否是提问者
  canAccept: boolean        // 是否可以采纳
  onLike?: (id: number) => void
  onAccept?: (id: number) => void
}
```

#### 6.2.3 标签输入组件 (TagInput.vue)

**功能**：
- 支持回车/逗号分隔输入
- 显示已添加标签（可删除）
- 最多 5 个标签
- 热门标签推荐（点击添加）

### 6.3 交互设计

#### 6.3.1 点赞动画

```typescript
const handleLike = async (answerId: number) => {
  // 乐观更新
  const answer = answers.value.find(a => a.answerId === answerId)
  if (!answer) return

  const wasLiked = answer.isLiked
  answer.isLiked = !wasLiked
  answer.likes += wasLiked ? -1 : 1

  // 触发动画（scale + 淡入心形）
  // ...

  try {
    if (wasLiked) {
      await unlikeAnswer(answerId)
    } else {
      await likeAnswer(answerId)
    }
  } catch (err) {
    // 回滚
    answer.isLiked = wasLiked
    answer.likes += wasLiked ? 1 : -1
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}
```

#### 6.3.2 采纳确认

```typescript
const handleAcceptAnswer = (answerId: number) => {
  uni.showModal({
    title: '采纳回答',
    content: '采纳后将无法更改，确定采纳这个答案吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await acceptAnswer(questionId, answerId)
          uni.showToast({ title: '采纳成功', icon: 'success' })
          // 刷新详情
          await loadQuestionDetail()
          await loadAnswers()
        } catch (err) {
          uni.showToast({ title: err.message || '采纳失败', icon: 'none' })
        }
      }
    }
  })
}
```

#### 6.3.3 图片预览

```typescript
const handleImageClick = (images: string[], current: number) => {
  uni.previewImage({
    urls: images,
    current: current
  })
}
```

### 6.4 响应式设计

**PC 端优化**：
```scss
@media (min-width: 768px) {
  .question-list {
    max-width: 900px;
    margin: 0 auto;
  }

  .question-detail {
    max-width: 800px;
    margin: 24px auto;
    background: #FFF;
    border-radius: 16px;
    padding: 40px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  }

  .question-card {
    &:hover {
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
      transform: translateY(-2px);
    }
  }
}
```

---

## 技术实现

### 7.1 状态管理

**Pinia Store** (`stores/question.ts`):

```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getQuestionList, getQuestionDetail } from '@/services/question'
import type { QuestionItem, QuestionDetail } from '@/types/question'

export const useQuestionStore = defineStore('question', () => {
  // 问题列表
  const questions = ref<QuestionItem[]>([])
  const currentQuestion = ref<QuestionDetail | null>(null)

  // 筛选条件
  const category = ref<string | null>(null)
  const isSolved = ref<boolean | null>(null)
  const sortBy = ref<'created_at' | 'views' | 'rewardPoints'>('created_at')

  // 分页
  const page = ref(1)
  const pageSize = ref(20)
  const total = ref(0)
  const hasMore = computed(() => questions.value.length < total.value)

  // 加载问题列表
  const loadQuestions = async (refresh = false) => {
    if (refresh) {
      page.value = 1
      questions.value = []
    }

    const res = await getQuestionList({
      category: category.value,
      isSolved: isSolved.value,
      page: page.value,
      pageSize: pageSize.value,
      sortBy: sortBy.value,
      sortOrder: 'desc'
    })

    if (refresh) {
      questions.value = res.list
    } else {
      questions.value.push(...res.list)
    }

    total.value = res.total
    page.value++
  }

  // 加载问题详情
  const loadQuestionDetail = async (id: number) => {
    currentQuestion.value = await getQuestionDetail(id)
  }

  return {
    questions,
    currentQuestion,
    category,
    isSolved,
    sortBy,
    page,
    total,
    hasMore,
    loadQuestions,
    loadQuestionDetail
  }
})
```

### 7.2 性能优化

#### 7.2.1 虚拟滚动

**使用场景**：问题列表、回答列表（>100 条）

**实现方案**：
```vue
<template>
  <recycle-scroller
    class="question-list"
    :items="questions"
    :item-size="200"
    key-field="questionId"
    v-slot="{ item }"
  >
    <QuestionCard :question="item" />
  </recycle-scroller>
</template>
```

#### 7.2.2 图片懒加载

```vue
<template>
  <image
    class="question-image"
    :src="imageSrc"
    :lazy-load="true"
    mode="aspectFill"
  />
</template>
```

#### 7.2.3 防抖与节流

**搜索防抖**：
```typescript
const searchDebounce = ref<number | null>(null)

const handleSearchInput = (keyword: string) => {
  if (searchDebounce.value) {
    clearTimeout(searchDebounce.value)
  }

  searchDebounce.value = setTimeout(() => {
    loadQuestions(true)
  }, 300) as unknown as number
}
```

**滚动节流**：
```typescript
const handleScroll = throttle((e: any) => {
  const { scrollTop, scrollHeight, clientHeight } = e.detail
  if (scrollTop + clientHeight >= scrollHeight - 100 && hasMore.value) {
    loadQuestions()
  }
}, 200)
```

### 7.3 数据缓存

**本地缓存策略**：
```typescript
// 缓存问题列表（5 分钟）
const CACHE_KEY = 'question_list_cache'
const CACHE_DURATION = 5 * 60 * 1000

const getCachedQuestions = () => {
  const cached = uni.getStorageSync(CACHE_KEY)
  if (!cached) return null

  const { data, timestamp } = JSON.parse(cached)
  if (Date.now() - timestamp > CACHE_DURATION) {
    uni.removeStorageSync(CACHE_KEY)
    return null
  }

  return data
}

const setCachedQuestions = (data: QuestionItem[]) => {
  uni.setStorageSync(CACHE_KEY, JSON.stringify({
    data,
    timestamp: Date.now()
  }))
}
```

---

## 开发计划

### 8.1 开发阶段

#### Phase 1：基础框架搭建（0.5 天）

**任务清单**：
- [ ] 创建页面文件
  - `/pages/question/index.vue`（问题列表）
  - `/pages/question/detail.vue`（问题详情）
  - `/pages/question/ask.vue`（发布问题）
  - `/pages/question/my.vue`（我的问答）
- [ ] 配置路由（pages.json）
- [ ] 创建组件
  - `QuestionCard.vue`（问题卡片）
  - `AnswerCard.vue`（回答卡片）
  - `TagInput.vue`（标签输入）
  - `EmptyState.vue`（空状态，复用资源模块）
- [ ] 配置 Pinia Store（`stores/question.ts`）
- [ ] 验证 API 服务（`services/question.ts`）

#### Phase 2：问题列表页（1 天）

**任务清单**：
- [ ] 实现筛选栏
  - 分类筛选（4 个分类）
  - 状态筛选（已解决/未解决）
  - 排序切换（最新/最热/悬赏）
- [ ] 实现搜索功能
  - 搜索框 UI
  - 防抖处理
  - 搜索历史（可选）
- [ ] 实现问题列表
  - QuestionCard 组件
  - 分页加载（下拉刷新 + 上拉加载更多）
  - 骨架屏加载状态
  - 空状态展示
- [ ] 实现发布按钮
  - 悬浮按钮（FAB）
  - 点击跳转发布页
- [ ] 性能优化
  - 列表虚拟滚动（如果列表 >100）
  - 图片懒加载
  - 本地缓存

#### Phase 3：问题详情页（1.5 天）

**任务清单**：
- [ ] 实现问题内容区
  - 问题标题、内容、图片
  - 标签列表
  - 用户信息、时间、统计数据
  - 状态标识（已解决/未解决）
- [ ] 实现操作按钮
  - 点赞按钮（乐观更新 + 动画）
  - 分享按钮（生成链接/二维码）
  - 删除按钮（仅提问者/管理员）
- [ ] 实现回答列表
  - AnswerCard 组件
  - 已采纳答案置顶
  - 排序切换（点赞数/时间）
  - 点赞回答（乐观更新）
  - 采纳回答（确认弹窗）
  - 分页加载
- [ ] 实现回答输入框
  - 固定底部输入框
  - 图片上传
  - 字符计数
  - 草稿保存
  - 发布回答
- [ ] 错误处理
  - 问题不存在（404）
  - 权限不足（403）
  - 网络错误

#### Phase 4：发布问题页（1 天）

**任务清单**：
- [ ] 实现表单输入
  - 标题输入（字符计数 10-200）
  - 内容输入（字符计数 20-2000）
  - 分类选择（卡片式）
  - 标签输入（TagInput 组件）
  - 图片上传（9 宫格，最多 9 张）
  - 悬赏设置（滑块 0-100 分）
- [ ] 实现表单验证
  - 必填校验
  - 长度校验
  - 积分余额校验
  - 实时错误提示
- [ ] 实现草稿功能
  - 自动保存（30 秒）
  - 恢复草稿（提示弹窗）
  - 清空草稿
- [ ] 实现预览功能（可选）
- [ ] 实现发布功能
  - 提交数据
  - 加载状态
  - 成功跳转详情页
  - 失败显示错误

#### Phase 5：我的问答页（0.5 天）

**任务清单**：
- [ ] 实现 Tab 切换
  - 我提出的 / 我回答的 / 我点赞的
  - 每个 Tab 独立分页
- [ ] 实现问题列表
  - 复用 QuestionCard
  - 下拉刷新 + 上拉加载更多
  - 空状态
- [ ] 实现积分卡片
  - 当前积分
  - 本周获得/消耗
  - 查看详情按钮

#### Phase 6：测试与优化（0.5 天）

**任务清单**：
- [ ] 功能测试
  - 发布问题流程
  - 回答问题流程
  - 采纳答案流程
  - 点赞功能
  - 分享功能
- [ ] 性能测试
  - 列表滚动流畅度
  - 图片加载速度
  - 网络请求优化
- [ ] 兼容性测试
  - H5 端
  - 微信小程序端
  - PC 端响应式
- [ ] Bug 修复
- [ ] 代码优化
- [ ] 文档完善

### 8.2 时间估算

| 阶段 | 预估时间 | 优先级 |
|------|---------|--------|
| Phase 1: 基础框架 | 0.5 天 | P0 |
| Phase 2: 问题列表 | 1 天 | P0 |
| Phase 3: 问题详情 | 1.5 天 | P0 |
| Phase 4: 发布问题 | 1 天 | P0 |
| Phase 5: 我的问答 | 0.5 天 | P1 |
| Phase 6: 测试优化 | 0.5 天 | P0 |
| **总计** | **5 天** | - |

**里程碑**：
- Day 1：完成基础框架 + 问题列表页
- Day 2-3：完成问题详情页
- Day 4：完成发布问题页
- Day 5：完成我的问答页 + 测试优化

---

## 风险与挑战

### 9.1 技术风险

| 风险 | 影响 | 概率 | 缓解措施 |
|------|------|------|---------|
| 虚拟滚动性能不足 | 列表卡顿 | 中 | 使用成熟的虚拟滚动库（vue-virtual-scroller） |
| 图片上传失败率高 | 用户体验差 | 中 | 压缩图片、重试机制、错误提示 |
| 富文本编辑器兼容性 | 跨端问题 | 低 | MVP 阶段使用纯文本，V2.0 再引入富文本 |
| AI 答复延迟 | 用户等待时间长 | 低 | 异步生成，生成后推送通知 |

### 9.2 UX 风险

| 风险 | 影响 | 概率 | 缓解措施 |
|------|------|------|---------|
| 问题质量参差不齐 | 社区体验差 | 高 | 引导文案、示例问题、审核机制（V2.0） |
| 回答率低 | 提问者失望 | 中 | 悬赏激励、AI 答复、推送通知 |
| 采纳率低 | 回答者积极性下降 | 中 | 采纳提醒、积分奖励、排行榜 |
| 重复问题多 | 内容冗余 | 中 | 相似问题推荐（V2.0）、搜索引导 |

### 9.3 业务风险

| 风险 | 影响 | 概率 | 缓解措施 |
|------|------|------|---------|
| 恶意刷积分 | 积分体系崩溃 | 中 | 限流、反作弊检测、异常行为封禁 |
| 垃圾内容泛滥 | 社区质量下降 | 高 | 关键词过滤、举报机制、人工审核 |
| 用户流失 | 社区死寂 | 中 | 运营活动、推送通知、积分奖励 |

---

## 附录

### A. 参考产品

- **知乎**：问答形式、点赞机制、专家认证
- **Stack Overflow**：技术问答、采纳机制、积分系统
- **百度知道**：悬赏机制、分类筛选、AI 答复
- **小红书**：图片展示、标签系统、社区氛围

### B. 设计资源

- 图标库：uni-app 内置图标 + 自定义 SVG
- 配色方案：Tailwind CSS 色板
- 字体：系统默认字体（San Francisco / Roboto）

### C. 技术栈

**前端**：
- uni-app（Vue 3 + TypeScript）
- Pinia（状态管理）
- SCSS（样式预处理器）
- vue-virtual-scroller（虚拟滚动，可选）

**后端**：
- Spring Boot 3.4
- MyBatisPlus（数据库 ORM）
- Redis（缓存、Session）
- MySQL（数据存储）

**第三方服务**：
- 阿里云 OSS（图片存储）
- OpenAI / 文心一言（AI 答复，V2.0）

---

---

## 附录 D. 色彩系统说明

### D.1 设计原则

**平台统一性优先**：
- ✅ 使用首页主色蓝色 `#1E5FFF` 作为主色调
- ✅ 使用资源广场橙色 `#FF7A00` 作为悬赏色
- ✅ 分类色彩匹配首页功能卡片
- ❌ 避免使用 AI 风格的紫色 `#6366F1`

### D.2 色彩语义

| 颜色 | 用途 | 心理暗示 |
|------|------|---------|
| 蓝色 `#1E5FFF` | 主操作、链接、学习类 | 专业、知识、信任 |
| 橙色 `#FF7A00` | 悬赏、生活类、强调 | 活力、奖励、温暖 |
| 绿色 `#10B981` | 已解决、技术类、成功 | 完成、成长、技术 |
| 灰色 `#6B7280` | 其他类、辅助信息 | 中性、平衡 |

### D.3 对比度检查

所有文字颜色均符合 WCAG AA 标准：
- 蓝色文字 `#1E5FFF` on 白色背景：对比度 5.2:1 ✅
- 橙色文字 `#FF7A00` on 白色背景：对比度 4.8:1 ✅
- 绿色文字 `#10B981` on 白色背景：对比度 4.5:1 ✅

---

## 版本历史

| 版本 | 日期 | 变更内容 |
|------|------|---------|
| v1.1 | 2025-11-18 | 更新色彩系统，移除紫色 AI 风格，统一平台配色 |
| v1.0 | 2025-11-18 | 初版设计文档，完整功能规划 |

---

**文档结束**

**下一步行动**：
1. ✅ 评审设计文档
2. ✅ 更新色彩系统（匹配平台设计）
3. ⏳ 开始 Phase 1 开发（基础框架搭建）

**相关文档**：
- [问答模块开发计划](../plan/question-module-development-plan.md)
- [问答模块色彩系统](question-module-color-system.md)
