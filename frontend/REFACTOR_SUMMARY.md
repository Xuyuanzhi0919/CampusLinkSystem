# 前端首页重构总结（第二版 - 基于 UI 设计文档）

## 📅 重构日期
2025-11-08

## 🎯 重构目标
根据 `docs/uiDesign/index.md` UI 设计文档，对前端首页进行全面重构，实现符合设计规范的高校资源互助与问答平台首页。

**设计理念**：清晰层级、高效触达、大屏适配，以「青春活力+理性学习」为核心调性。

## ✅ 已完成的工作

### 0. 更新色彩系统配置 (`frontend/src/config.ts`)
- ✅ 主色调：青春蓝 `#409EFF`（导航、按钮、标题、图标）
- ✅ 辅助色：活力橙 `#FF7D00`（积分、紧急任务、报名按钮）
- ✅ 功能色：成功色 `#00B42A`、警告色 `#FF4D4F`、信息色 `#1890FF`
- ✅ 中性色：纯白、浅灰、中灰、深灰、纯黑
- ✅ 字体大小配置（PC端和H5端）
- ✅ 间距和圆角配置

### 1. 创建类型定义文件 (`frontend/src/types/`)
- ✅ `types/api.ts` - API 通用类型定义（统一响应格式、分页参数等）
- ✅ `types/resource.ts` - 资源模块类型（资源列表、详情、上传参数等）
- ✅ `types/question.ts` - 问答模块类型（问题、回答、列表参数等）
- ✅ `types/task.ts` - 任务模块类型（任务列表、详情、状态枚举等）
- ✅ `types/club.ts` - 社团模块类型（社团、活动、列表参数等）

### 2. 创建 API 服务层 (`frontend/src/services/`)
- ✅ `services/resource.ts` - 资源相关 API（列表、详情、上传、下载、点赞等）
- ✅ `services/question.ts` - 问答相关 API（列表、详情、提问、回答、采纳等）
- ✅ `services/task.ts` - 任务相关 API（列表、详情、发布、接单、完成等）
- ✅ `services/club.ts` - 社团相关 API（列表、详情、创建、加入、活动等）

**设计特点**：
- 统一使用 `@/utils/request` 封装的请求方法
- 所有 API 方法都有完整的 TypeScript 类型定义
- 遵循 RESTful API 设计规范
- 支持分页、搜索、排序等查询参数

### 3. 创建 Pinia 状态管理 (`frontend/src/stores/`)
- ✅ `stores/user.ts` - 用户状态管理
  - 用户信息（userId, username, nickname, avatar, points 等）
  - Token 管理（token, refreshToken）
  - 登录/登出功能
  - 积分更新
  - 权限判断（isAdmin, isModerator）

- ✅ `stores/app.ts` - 应用全局状态管理
  - 当前学校信息
  - 系统配置
  - 网络状态
  - 全局加载状态
  - 工具方法（showLoading, showToast, showConfirm）

- ✅ `stores/index.ts` - 统一导出

**设计特点**：
- 使用 Composition API 风格
- 数据持久化到本地存储
- 提供便捷的工具方法

### 4. 创建新的首页组件 (`frontend/src/components/`)

#### 核心组件（根据 UI 设计文档）
- ✅ `BannerSwiper.vue` - Banner 轮播组件
  - 自动轮播（4秒/张）
  - 指示器（激活态主题色）
  - 支持点击跳转
  - 渐变遮罩文案展示

- ✅ `FunctionGrid.vue` - 功能入口组件
  - 8个核心功能入口（资料共享、智能问答、互助任务、社团活动、积分中心、附近同学、校园公告、我的收藏）
  - 横向滚动布局
  - 图标 + 文字 + 简短描述
  - 渐变背景图标

- ✅ `HotRanking.vue` - 热门榜单组件
  - 三栏切换（热门问答、精选资料、紧急任务）
  - 排序序号（前三名金银铜渐变）
  - 列表展示（标题 + 元数据）
  - 热门标识

- ✅ `RecommendCards.vue` - 个性化推荐组件
  - 双列卡片布局
  - 混合推荐（资料/问答/任务）
  - 类型标识（不同颜色渐变）
  - 换一批功能

- ✅ `ActivityScroll.vue` - 社团活动组件
  - 横向滚动活动卡片
  - 活动海报 + 时间 + 报名人数
  - 社团标签

- ✅ `PointsPanel.vue` - 积分动态组件
  - 我的积分展示（渐变卡片）
  - 今日可赚积分任务列表
  - 任务快捷入口

- ✅ `NoticeList.vue` - 校园公告组件
  - 公告列表（带"新"标签）
  - 时间显示
  - 更多按钮

#### 保留的基础组件
- ✅ `ResourceCard.vue` - 资源卡片组件
- ✅ `QuestionCard.vue` - 问题卡片组件
- ✅ `TaskCard.vue` - 任务卡片组件
- ✅ `ClubCard.vue` - 社团卡片组件
- ✅ `EmptyState.vue` - 空状态组件
- ✅ `LoadingMore.vue` - 加载更多组件

**设计特点**：
- 遵循 UI 设计文档的色彩系统
- 统一的圆角、间距、字体大小
- 渐变背景和阴影效果
- 流畅的交互动画

### 5. 重构首页页面 (`frontend/src/pages/home/index.vue`)

#### 新的首页布局（基于 UI 设计文档）
✅ **顶部搜索栏**
- Logo（图标 + 文字"CampusLink"）
- 搜索框（圆角48rpx，占位文字"搜课件/问题/任务"）
- 消息图标（带未读红点）
- 吸顶效果（position: sticky）

✅ **主内容区域（垂直滚动）**
1. **Banner 轮播区**
   - 高度 440rpx
   - 自动轮播，4秒/张
   - 渐变遮罩 + 文案

2. **功能入口区**
   - 横向滚动
   - 8个功能入口
   - 图标 + 名称 + 描述

3. **个性化推荐**
   - 标题"为你推荐" + "换一批"按钮
   - 双列卡片布局
   - 混合推荐（资料/问答/任务）

4. **热门榜单**
   - 三栏切换（热门问答、精选资料、紧急任务）
   - 排序序号 + 列表

5. **社团活动**
   - 横向滚动活动卡片
   - 活动海报 + 信息

6. **积分动态**
   - 我的积分卡片
   - 今日可赚积分任务

7. **校园公告**
   - 公告列表
   - 带"新"标签

✅ **下拉刷新**
- 支持下拉刷新所有数据
- 刷新成功提示

✅ **响应式设计**
- 使用 rpx 单位适配不同屏幕
- 符合 uni-app 多端规范

#### 技术实现
- 使用 Composition API
- 使用 Pinia 管理用户状态
- 组件化设计，模块清晰
- 遵循 UI 设计文档的色彩系统
- 所有交互都有友好提示

### 6. 配置 Pinia
- ✅ 安装 pinia 依赖（使用 --legacy-peer-deps）
- ✅ 在 `main.ts` 中配置 Pinia

## 📁 文件结构

```
frontend/src/
├── types/                    # 类型定义
│   ├── api.ts               # API 通用类型
│   ├── resource.ts          # 资源类型
│   ├── question.ts          # 问答类型
│   ├── task.ts              # 任务类型
│   └── club.ts              # 社团类型
├── services/                 # API 服务层
│   ├── resource.ts          # 资源 API
│   ├── question.ts          # 问答 API
│   ├── task.ts              # 任务 API
│   └── club.ts              # 社团 API
├── stores/                   # 状态管理
│   ├── user.ts              # 用户状态
│   ├── app.ts               # 应用状态
│   └── index.ts             # 统一导出
├── components/               # 可复用组件
│   ├── ResourceCard.vue     # 资源卡片
│   ├── QuestionCard.vue     # 问题卡片
│   ├── TaskCard.vue         # 任务卡片
│   ├── ClubCard.vue         # 社团卡片
│   ├── EmptyState.vue       # 空状态
│   └── LoadingMore.vue      # 加载更多
└── pages/
    └── home/
        └── index.vue         # 首页（已重构）
```

## 🎨 设计规范遵循

### 1. API 调用规范 ✅
- ✅ 统一使用 `services/` 目录下的 API 方法
- ✅ 不直接使用 `uni.request`
- ✅ 使用 `utils/request.ts` 封装的请求方法
- ✅ 所有 API 调用使用 `try-catch` 捕获错误
- ✅ 使用 `uni.showToast` 显示错误提示

### 2. 状态管理规范 ✅
- ✅ 使用 Pinia 进行状态管理
- ✅ 用户信息存储在 `userStore`
- ✅ 应用配置存储在 `appStore`
- ✅ 数据持久化到本地存储

### 3. 组件化规范 ✅
- ✅ 提取可复用组件到 `components/` 目录
- ✅ 组件使用 TypeScript 类型定义
- ✅ 组件支持 Props 和 Emits
- ✅ 统一的样式风格

### 4. 多端适配规范 ✅
- ✅ 使用 uni-app 提供的组件和 API
- ✅ 使用 rpx 单位适配不同屏幕
- ✅ 避免使用平台特定 API

### 5. 代码质量规范 ✅
- ✅ 添加必要的中文注释
- ✅ 使用 TypeScript 类型定义
- ✅ 统一的命名规范（驼峰命名）
- ✅ 代码结构清晰，易于维护

## 🚀 下一步工作

### 1. 详情页开发
- [ ] 资源详情页 (`pages/resource/detail.vue`)
- [ ] 问题详情页 (`pages/question/detail.vue`)
- [ ] 任务详情页 (`pages/task/detail.vue`)
- [ ] 社团详情页 (`pages/club/detail.vue`)

### 2. 搜索功能
- [ ] 搜索页面 (`pages/search/index.vue`)
- [ ] 搜索 API 集成
- [ ] 搜索历史记录

### 3. 筛选功能
- [ ] 资源分类筛选
- [ ] 问题状态筛选
- [ ] 任务类型筛选

### 4. 用户认证
- [ ] 登录页 (`pages/auth/login.vue`)
- [ ] 注册页 (`pages/auth/register.vue`)
- [ ] 微信小程序登录集成

### 5. 测试
- [ ] 单元测试
- [ ] 集成测试
- [ ] 多端测试（微信小程序、H5、App）

## 📝 注意事项

1. **后端接口**：当前代码调用的后端接口需要后端实现，否则会报错
2. **图片资源**：用户头像等图片需要有默认占位图
3. **权限控制**：部分功能需要登录后才能访问
4. **错误处理**：已添加基础错误处理，可根据实际情况优化
5. **性能优化**：大量数据时可考虑虚拟列表优化

## 🔧 如何运行

### 开发环境
```bash
# 进入前端目录
cd frontend

# 安装依赖（如果还没安装）
npm install

# 运行 H5
npm run dev:h5

# 运行微信小程序
npm run dev:mp-weixin
```

### 生产构建
```bash
# 构建 H5
npm run build:h5

# 构建微信小程序
npm run build:mp-weixin
```

## 📚 参考文档
- [API 接口设计文档](../docs/api-design.md)
- [数据库设计文档](../docs/database-design.md)
- [CLAUDE.md](../CLAUDE.md)
- [uni-app 官方文档](https://uniapp.dcloud.net.cn/)
- [Pinia 官方文档](https://pinia.vuejs.org/)

