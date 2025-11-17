# 资源详情页设计文档

## 1. 页面概述

### 1.1 页面定位
资源详情页是资源广场的核心页面，用户可以查看资源完整信息、下载资源、点赞、发表评论等。

### 1.2 访问路径与权限
- 从资源列表页点击卡片进入：`/pages/resource/detail?id={resourceId}`
- 从个人中心"我的上传"进入
- 从下载历史进入
- 通过分享链接直接访问

**分享链接访问权限规则**：
- ✅ 未登录用户可浏览资源详情（标题、描述、统计数据）
- ❌ 未登录用户不能下载、点赞、评论、举报（按钮显示但点击后弹出登录提示）
- ✅ 私有/仅校内可见资源通过分享链接访问时，检查用户权限，不符合则显示"权限不足"页面

### 1.3 核心功能（MVP 范围）
- ✅ 资源信息展示（标题、描述、上传者、时间、分类等）
- ✅ 文件信息展示（类型、大小、下载次数、点赞数）
- ✅ **下载功能**（积分扣除、下载确认、已下载标识）- 主操作
- ✅ 点赞功能（点赞/取消点赞、实时更新计数）
- ✅ 评论功能（发表评论、查看评论、回复、删除）
- ✅ 上传者信息卡片（头像、昵称、积分、跳转主页）
- ✅ 相关推荐（同分类/课程资源）
- ✅ 举报功能（不当内容举报）
- ✅ 分享功能（生成分享链接）

**注意**：
- 评分功能（⭐⭐⭐⭐⭐）暂不在 MVP 范围，移至"后续优化方向"
- 当前只保留点赞（♥ likes）作为资源热度指标

---

## 2. UI 布局设计

### 2.1 整体结构（移动端）

```
┌─────────────────────────────────────┐
│      导航栏 (< 返回 + 分享 •••)      │
├─────────────────────────────────────┤
│      资源头图 / 文件类型图标         │
│         (280rpx 高度)                │
├─────────────────────────────────────┤
│   📋 信息头区（整合）                │
│   ┌─ 资源标题 (加粗)                 │
│   ├─ 标签: [课件] [计算机] [🔥热门] │
│   └─ 统计: 👁123 · 💾45 · ♥67 · 💬8│
├─────────────────────────────────────┤
│   👤 上传者信息卡片                  │
│   ┌─────┬─────────────────────┐    │
│   │头像 │  张三 · 积分 535      │    │
│   │     │  2天前上传            │→  │
│   └─────┴─────────────────────┘    │
├─────────────────────────────────────┤
│   📄 资源详情卡片                    │
│   ┌─ 课程: 数据结构                 │
│   ├─ 类型: PDF · 大小: 2.5 MB      │
│   └─ 描述: [展开/收起]              │
├─────────────────────────────────────┤
│   💬 评论区                          │
│   └─ <CommentList />                │
├─────────────────────────────────────┤
│   🔗 相关推荐 (同课程)               │
│   └─ [资源卡片 × 4]                 │
├─────────────────────────────────────┤
│                                     │
│   ⬇️ 固定底部操作栏                  │
│   ┌──────────────┬─────┬─────┐    │
│   │   下载 (-5分) │ ♥67 │ ••• │    │
│   │  (橙色渐变主按钮) │     │     │    │
│   └──────────────┴─────┴─────┘    │
└─────────────────────────────────────┘

说明：
• 头图高度从 400rpx 降至 280rpx，首屏可见核心信息
• 信息头整合标题+标签+统计，视觉连贯
• 底部操作栏简化为：下载(主操作) + 点赞 + 更多(举报/评论跳转)
• 图标优先使用统一 icon 字体，少量 Emoji 仅用于强调
```

### 2.2 组件分解

#### 2.2.1 顶部导航栏
- **左侧**：返回按钮（< 返回）
- **中间**：资源详情（标题）
- **右侧**：更多菜单（•••）- 包含分享、举报等次要操作
- **样式**：白色背景，下边框阴影

#### 2.2.2 资源头图区
- **PDF/DOC**：显示文档图标 + 渐变背景
- **PPT**：显示幻灯片图标 + 渐变背景
- **图片**：直接显示缩略图
- **视频**：显示视频封面 + 播放按钮
- **其他**：显示通用文件图标 + 渐变背景
- **尺寸**：100% 宽度，高度 **280rpx**（优化后）
- **背景渐变色**：
  - PDF: `linear-gradient(135deg, #667eea 0%, #764ba2 100%)`
  - PPT: `linear-gradient(135deg, #f093fb 0%, #f5576c 100%)`
  - DOC: `linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)`
  - ZIP: `linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)`
  - 默认: `linear-gradient(135deg, #fa709a 0%, #fee140 100%)`

#### 2.2.3 标题与标签区
```vue
<view class="title-section">
  <text class="resource-title">数据结构课程笔记 - 第三章树与二叉树</text>
  <view class="tag-group">
    <view class="tag tag-category">课件</view>
    <view class="tag tag-course">计算机科学</view>
    <view class="tag tag-hot">🔥 热门</view>
  </view>
</view>
```

**样式要点**：
- 标题：32rpx，加粗，行高1.4，最多2行省略
- 标签：圆角胶囊样式，不同类型不同颜色

#### 2.2.4 统计数据栏（整合到信息头）
```vue
<!-- 紧凑单行显示，与标题/标签在同一卡片内 -->
<view class="info-header">
  <text class="resource-title">数据结构课程笔记 - 第三章</text>
  <view class="tag-group">
    <view class="tag tag-category">课件</view>
    <view class="tag tag-course">计算机</view>
    <view class="tag tag-hot">🔥热门</view>
  </view>
  <view class="stats-compact">
    <text class="stat-item">👁️ 123</text>
    <text class="stat-divider">·</text>
    <text class="stat-item">💾 45</text>
    <text class="stat-divider">·</text>
    <text class="stat-item" @click="scrollToComment">
      ♥ <text :class="{ 'text-primary': isLiked }">67</text>
    </text>
    <text class="stat-divider">·</text>
    <text class="stat-item" @click="scrollToComment">💬 8</text>
  </view>
</view>
```

**优化点**：
- 统计数据从独立卡片改为紧凑单行，与标题/标签在同一区块
- 使用 `·` 分隔符，视觉更轻量
- 点赞/评论数字可点击跳转到对应区域

#### 2.2.5 上传者信息卡片
```vue
<view class="uploader-card" @click="navigateToUserProfile">
  <image :src="uploaderAvatar" class="uploader-avatar" />
  <view class="uploader-info">
    <text class="uploader-name">{{ uploaderName }}</text>
    <text class="uploader-points">💰 积分: {{ uploaderPoints }}</text>
    <text class="upload-time">📅 {{ uploadTime }}</text>
  </view>
  <text class="arrow-icon">›</text>
</view>
```

**交互**：
- 点击卡片跳转到上传者个人主页
- 显示上传者头像、昵称、积分、上传时间
- 右侧箭头提示可点击

#### 2.2.6 资源详情卡片
```vue
<view class="detail-card">
  <view class="detail-row">
    <text class="detail-label">课程名称</text>
    <text class="detail-value">{{ courseName || '未分类' }}</text>
  </view>
  <view class="detail-row">
    <text class="detail-label">文件类型</text>
    <view class="file-type-badge">
      <text class="file-type-text">{{ fileType.toUpperCase() }}</text>
    </view>
  </view>
  <view class="detail-row">
    <text class="detail-label">文件大小</text>
    <text class="detail-value">{{ formatFileSize(fileSize) }}</text>
  </view>
  <view class="detail-row description-row">
    <text class="detail-label">资源描述</text>
    <text
      class="description-text"
      :class="{ 'expanded': descriptionExpanded }"
    >
      {{ description }}
    </text>
    <text
      v-if="description.length > 100"
      class="expand-btn"
      @click="descriptionExpanded = !descriptionExpanded"
    >
      {{ descriptionExpanded ? '收起' : '展开' }}
    </text>
  </view>
</view>
```

**样式**：
- 白色卡片，圆角阴影
- 每行左右布局：标签（灰色） + 值（黑色）
- 描述区域：默认最多3行，超过显示"展开"按钮
- **已移除评分功能**（移至后续优化）

#### 2.2.7 操作按钮组（固定底部 - 优化版）
```vue
<view class="action-bar">
  <!-- 主操作：下载按钮（占 60% 宽度） -->
  <view
    class="primary-action-btn"
    :class="{ 'downloaded': isDownloaded }"
    @click="handleDownload"
  >
    <text class="btn-icon">⬇</text>
    <text class="btn-text">
      {{ isDownloaded ? '已下载' : '下载 (-5积分)' }}
    </text>
  </view>

  <!-- 次要操作：点赞按钮 -->
  <view
    class="secondary-action-btn like-btn"
    :class="{ 'is-liked': isLiked }"
    @click="handleLike"
  >
    <text class="like-icon">{{ isLiked ? '♥' : '♡' }}</text>
    <text class="like-count">{{ likes }}</text>
  </view>

  <!-- 次要操作：更多菜单 -->
  <view class="secondary-action-btn more-btn" @click="showMoreMenu">
    <text class="more-icon">•••</text>
  </view>
</view>

<!-- 更多菜单弹窗 -->
<uni-popup ref="morePopup" type="bottom">
  <view class="more-menu">
    <view class="menu-item" @click="scrollToComment">
      <text class="menu-icon">💬</text>
      <text class="menu-text">评论 ({{ commentCount }})</text>
    </view>
    <view class="menu-item" @click="handleShare">
      <text class="menu-icon">📤</text>
      <text class="menu-text">分享</text>
    </view>
    <view class="menu-item" @click="handleReport">
      <text class="menu-icon">⚠️</text>
      <text class="menu-text">举报</text>
    </view>
    <view class="menu-item cancel" @click="closeMoreMenu">
      <text class="menu-text">取消</text>
    </view>
  </view>
</uni-popup>
```

**优化要点**：
- **主次分明**：下载按钮占 60% 宽度，橙色渐变，视觉主导
- **简化布局**：底部只保留 3 个按钮（下载 + 点赞 + 更多）
- **图标统一**：使用 Unicode 字符 `⬇ ♥ ♡ •••` 代替 Emoji，跨平台一致
- **更多菜单**：评论、分享、举报收入底部弹窗，避免操作栏拥挤
- **固定底部**：iOS 需要适配 `safe-area-inset-bottom`

**交互逻辑**：
1. **下载按钮（主操作）**：
   - 未下载：`⬇ 下载 (-5积分)` - 橙色渐变
   - 已下载：`⬇ 已下载` - 灰色禁用
   - 点击触发下载确认弹窗

2. **点赞按钮**：
   - 未点赞：`♡ 67` - 灰色
   - 已点赞：`♥ 67` - 红色 + 放大动画
   - 点击切换状态（乐观更新）

3. **更多按钮（•••）**：
   - 点击弹出底部菜单
   - 菜单包含：评论跳转、分享、举报

#### 2.2.8 评论区
直接使用已实现的 `CommentList` 组件：

```vue
<view class="comment-section">
  <view class="section-header">
    <text class="section-title">评论区</text>
    <text class="comment-count">({{ commentCount }})</text>
  </view>
  <CommentList :resourceId="resourceId" />
</view>
```

#### 2.2.9 相关推荐区
```vue
<view class="recommend-section">
  <view class="section-header">
    <text class="section-title">相关推荐</text>
    <text class="more-link" @click="viewMoreResources">更多 ›</text>
  </view>
  <view class="recommend-list">
    <ResourceCard
      v-for="item in relatedResources"
      :key="item.resourceId"
      :resource="item"
      @click="navigateToDetail(item.resourceId)"
    />
  </view>
</view>
```

**推荐逻辑**（完整规则）：
1. **候选集筛选**：
   - 第一优先级：同课程资源（`courseName` 相同）
   - 第二优先级：同分类资源（`category` 相同）
   - 必须排除当前资源（`resourceId != current.resourceId`）
   - 必须是已审核通过的资源（`status = 1`）

2. **排序规则**（在候选集内）：
   - 优先按 **下载量**（`downloads`）降序
   - 下载量相同时按 **点赞数**（`likes`）降序
   - 仍相同时按 **创建时间**（`createdAt`）降序（最新优先）

3. **数量限制**：
   - 最多返回 **4 条**
   - 如果同课程不足 4 条，用同分类的补齐

**SQL 示例**：
```sql
-- 同课程推荐
SELECT * FROM resource
WHERE course_name = '数据结构'
  AND resource_id != 16
  AND status = 1
ORDER BY downloads DESC, likes DESC, created_at DESC
LIMIT 4;

-- 如果不足，用同分类补齐
SELECT * FROM resource
WHERE category = '课件'
  AND resource_id != 16
  AND resource_id NOT IN (已选的同课程资源)
  AND status = 1
ORDER BY downloads DESC, likes DESC, created_at DESC
LIMIT (4 - 已选数量);
```

---

## 3. 数据结构

### 3.1 页面状态
```typescript
interface ResourceDetailState {
  // 资源基本信息
  resourceId: number
  title: string
  description: string
  category: string
  courseName: string
  fileType: string
  fileSize: number
  fileUrl: string
  score: number

  // 统计数据
  views: number
  downloads: number
  likes: number
  commentCount: number

  // 上传者信息
  uploaderId: number
  uploaderName: string
  uploaderAvatar: string
  uploaderPoints: number
  uploadedAt: string

  // 用户交互状态
  isDownloaded: boolean  // 是否已下载
  isLiked: boolean       // 是否已点赞
  isFavorited: boolean   // 是否已收藏

  // UI 状态
  loading: boolean
  descriptionExpanded: boolean  // 描述是否展开

  // 相关推荐
  relatedResources: ResourceItem[]
}
```

### 3.2 API 响应结构
```typescript
interface ResourceDetailResponse {
  resourceId: number
  title: string
  description: string
  uploaderName: string
  uploaderAvatar: string
  uploaderPoints: number
  uploaderId: number
  fileType: string
  fileSize: number
  fileUrl: string
  category: string
  courseName: string
  score: number
  views: number
  downloads: number
  likes: number
  status: number
  isDownloaded: boolean
  isLiked: boolean
  isFavorited: boolean
  createdAt: string
  updatedAt: string
}
```

---

## 4. 交互流程

### 4.1 页面加载流程
```
用户进入页面
    ↓
从 URL 获取 resourceId
    ↓
调用 getResourceDetail(resourceId)
    ↓
┌─────────────────────────────┐
│ 成功                         │
├─────────────────────────────┤
│ 1. 渲染资源信息              │
│ 2. 加载评论组件              │
│ 3. 加载相关推荐              │
│ 4. 记录浏览历史              │
└─────────────────────────────┘
    ↓
┌─────────────────────────────┐
│ 失败（404/权限不足）         │
├─────────────────────────────┤
│ 显示错误提示                 │
│ 3秒后返回资源列表            │
└─────────────────────────────┘
```

### 4.2 下载流程
```
点击"下载"按钮
    ↓
检查登录状态
    ↓
    ├─ 未登录 → 跳转登录页
    └─ 已登录
        ↓
    检查是否已下载
        ↓
        ├─ 已下载 → 提示"已下载"，询问是否重新下载
        └─ 未下载
            ↓
        显示下载确认弹窗
        ┌────────────────────────┐
        │ 确认下载该资源？        │
        │ • 消耗积分：5          │
        │ • 当前积分：535        │
        │ • 剩余积分：530        │
        │                        │
        │  [取消]    [确认下载]  │
        └────────────────────────┘
            ↓
        用户确认
            ↓
        调用 downloadResource(resourceId)
            ↓
        ┌─ 成功
        │  ├─ 扣除积分
        │  ├─ 增加下载次数
        │  ├─ 更新 isDownloaded = true
        │  ├─ 显示"下载成功"
        │  └─ 触发文件下载/打开
        │
        └─ 失败
           ├─ 积分不足 → 提示"积分不足，去赚积分？"
           ├─ 文件不存在 → 提示"文件已失效"
           └─ 其他错误 → 显示错误信息
```

**积分扣除细则**（重要边界情况）：
| 场景 | 积分扣除 | 说明 |
|------|---------|------|
| 首次下载 | **扣除 5 分** | 正常扣分，记录下载日志 |
| 重复下载同一资源 | **不扣分** | `isDownloaded = true`，可重新下载但不重复扣分 |
| 下载自己上传的资源 | **不扣分** | 作者可免费下载自己的资源 |
| 下载过程失败 | **不扣分或回滚** | OSS 404/网络超时时不扣分；已扣分则自动回滚 |
| 积分不足 | **拒绝下载** | 弹出"去赚积分"引导，跳转任务/问答页 |
| 资源被删除/下架 | **不扣分** | 提示"资源已下架"，不允许下载 |

**实现要点**：
- 使用事务保证"扣积分 + 记录下载日志"的原子性
- 下载日志表 `download_log` 记录 (user_id, resource_id, timestamp)
- 通过 `download_log` 判断是否已下载，而非依赖前端缓存
```

### 4.3 点赞流程
```
点击"点赞"按钮
    ↓
检查登录状态
    ↓
    ├─ 未登录 → 跳转登录页
    └─ 已登录
        ↓
    检查当前状态
        ↓
        ├─ 未点赞
        │   ↓
        │  乐观更新UI (立即显示红心 + 数量+1)
        │   ↓
        │  调用 likeResource(resourceId)
        │   ↓
        │  ├─ 成功 → 保持UI状态
        │  └─ 失败 → 回滚UI + 提示错误
        │
        └─ 已点赞
            ↓
           乐观更新UI (显示空心 + 数量-1)
            ↓
           调用 unlikeResource(resourceId)
            ↓
           ├─ 成功 → 保持UI状态
           └─ 失败 → 回滚UI + 提示错误
```

### 4.4 评论交互流程
评论功能已由 `CommentList` 组件完整实现，包括：
- 发表评论
- 查看评论列表
- 回复评论
- 删除评论

详见 [COMMENT_INTEGRATION.md](../../frontend/COMMENT_INTEGRATION.md)

### 4.5 分享流程
```
点击"分享"按钮
    ↓
显示分享弹窗
┌────────────────────────────┐
│   分享到                    │
├────────────────────────────┤
│  [📱 微信]  [👥 朋友圈]     │
│  [📋 复制链接]  [💾 保存]   │
└────────────────────────────┘
    ↓
用户选择分享方式
    ↓
    ├─ 微信/朋友圈 → 调用 uni.share()
    ├─ 复制链接 → 复制到剪贴板 + 提示
    └─ 保存图片 → 生成资源卡片图片 + 保存相册
```

### 4.6 举报流程
```
点击"举报"按钮
    ↓
检查登录状态
    ↓
显示举报弹窗
┌────────────────────────────┐
│   举报原因                  │
├────────────────────────────┤
│  ○ 内容不当               │
│  ○ 虚假资源               │
│  ○ 侵权内容               │
│  ○ 垃圾广告               │
│  ○ 其他                   │
│                            │
│  补充说明：                │
│  [文本框]                  │
│                            │
│  [取消]    [提交举报]      │
└────────────────────────────┘
    ↓
用户提交
    ↓
调用 reportResource(resourceId, type, reason)
    ↓
    ├─ 成功 → 提示"举报成功，我们会尽快处理"
    └─ 失败 → 提示错误信息
```

---

## 5. API 接口清单

### 5.1 获取资源详情
```
GET /resource/{id}
```

**响应示例**：
```json
{
  "code": 200,
  "data": {
    "resourceId": 16,
    "title": "数据结构课程笔记",
    "description": "完整的数据结构课程笔记...",
    "uploaderId": 2,
    "uploaderName": "张三",
    "uploaderAvatar": "https://...",
    "uploaderPoints": 535,
    "fileType": "pdf",
    "fileSize": 2048000,
    "fileUrl": "https://...",
    "category": "课件",
    "courseName": "数据结构",
    "score": 5,
    "views": 123,
    "downloads": 45,
    "likes": 67,
    "isDownloaded": false,
    "isLiked": false,
    "createdAt": "2025-11-09 13:13:17"
  }
}
```

### 5.2 下载资源
```
POST /resource/{id}/download
```

**响应**：
```json
{
  "code": 200,
  "message": "下载成功",
  "data": {
    "downloadUrl": "https://..."
  }
}
```

### 5.3 点赞/取消点赞
```
POST /resource/{id}/like      # 点赞
DELETE /resource/{id}/like    # 取消点赞
```

### 5.4 评论相关
```
GET /resource/{id}/comments           # 获取评论列表
POST /resource/{id}/comments          # 添加评论
DELETE /resource/comments/{commentId} # 删除评论
```

### 5.5 相关推荐
```
GET /resource/{id}/related?limit=4
```

### 5.6 举报资源
```
POST /resource/{id}/report
Body: { "type": "inappropriate", "reason": "..." }
```

---

## 6. 样式规范

### 6.1 颜色方案
```scss
// 主题色
$primary-color: #FF6B35;           // 主橙色
$primary-gradient: linear-gradient(135deg, #FF6B35 0%, #FF8C42 100%);

// 功能色
$success-color: #4CAF50;           // 成功绿
$error-color: #F44336;             // 错误红
$warning-color: #FF9800;           // 警告橙
$info-color: #2196F3;              // 信息蓝

// 文本色
$text-primary: #333333;            // 主文本
$text-secondary: #666666;          // 次要文本
$text-tertiary: #999999;           // 辅助文本
$text-disabled: #CCCCCC;           // 禁用文本

// 背景色
$bg-page: #F5F5F5;                 // 页面背景
$bg-card: #FFFFFF;                 // 卡片背景
$bg-hover: #F8F8F8;                // 悬停背景
$bg-disabled: #E0E0E0;             // 禁用背景

// 边框色
$border-color: #E0E0E0;            // 默认边框
$border-light: #F0F0F0;            // 浅色边框

// 点赞相关
$like-active: #F87171;             // 点赞红色
$like-inactive: #999999;           // 未点赞灰色
```

### 6.2 间距规范
```scss
$spacing-xs: 8rpx;
$spacing-sm: 16rpx;
$spacing-md: 24rpx;
$spacing-lg: 32rpx;
$spacing-xl: 48rpx;
```

### 6.3 圆角规范
```scss
$radius-sm: 8rpx;   // 小圆角（按钮、标签）
$radius-md: 12rpx;  // 中圆角（卡片）
$radius-lg: 16rpx;  // 大圆角（弹窗）
$radius-round: 999rpx;  // 圆形（头像、胶囊按钮）
```

### 6.4 字体规范
```scss
$font-xs: 22rpx;    // 辅助信息
$font-sm: 24rpx;    // 次要文本
$font-md: 28rpx;    // 正文
$font-lg: 32rpx;    // 标题
$font-xl: 36rpx;    // 大标题
```

---

## 7. 响应式适配

### 7.1 屏幕尺寸适配
- **小屏手机**（< 375px）：单列布局，缩小图标和字号
- **普通手机**（375-414px）：标准布局
- **大屏手机/平板**（> 414px）：增加内边距，限制最大宽度

### 7.2 H5 vs 小程序差异
| 功能 | H5 | 小程序 |
|------|-----|--------|
| 下载方式 | 浏览器下载 | uni.downloadFile() |
| 分享 | Web Share API | uni.share() |
| 预览文件 | window.open() | uni.openDocument() |
| 返回按钮 | 浏览器返回 | 左上角返回 |

### 7.3 PC Web 端布局差异（重要）

**整体布局**：
```
┌────────────────────────────────────────────────┐
│            顶部导航栏（全宽）                    │
├────────────────────────────────────────────────┤
│                                                │
│  ┌─────────────────┬───────────────────────┐  │
│  │                 │                       │  │
│  │  左侧主内容区    │  右侧侧边栏           │  │
│  │  (max-width:    │  (固定 300px)         │  │
│  │   800px)        │                       │  │
│  │                 │  ┌─ 上传者信息        │  │
│  │  ┌─ 资源头图     │  │                   │  │
│  │  ├─ 标题标签     │  ├─ 资源详情卡片    │  │
│  │  ├─ 统计数据     │  │                   │  │
│  │  ├─ 操作按钮     │  └─ 相关推荐        │  │
│  │  ├─ 描述         │     (吸顶滚动)      │  │
│  │  └─ 评论区       │                       │  │
│  │                 │                       │  │
│  └─────────────────┴───────────────────────┘  │
│         (max-width: 1200px, 居中显示)          │
└────────────────────────────────────────────────┘
```

**核心差异**：

1. **布局结构**
   - 移动端：纵向堆叠（单列）
   - PC 端：**左右两栏**（主内容 + 侧边栏）
   - 整体最大宽度：`max-width: 1200px; margin: 0 auto;`

2. **操作按钮位置**
   - 移动端：固定底部悬浮栏
   - PC 端：
     - **下载按钮**：放在资源头图下方，作为醒目的 CTA 按钮
     - **点赞/分享/举报**：放在标题右侧或详情卡片内的工具条
     - 不使用底部悬浮（PC 端悬浮栏体验差）

3. **侧边栏内容**（PC 独有）
   - **上传者卡片**：移到右侧侧边栏顶部
   - **资源详情卡片**：跟随在上传者卡片下方
   - **相关推荐**：侧边栏底部，**支持吸顶滚动**
   - 侧边栏宽度：固定 `300px`

4. **滚动行为**
   - 移动端：完整页面纵向滚动
   - PC 端：
     - 主内容区正常滚动
     - 侧边栏吸顶（滚动到一定位置后固定）
     - 使用 `position: sticky` 实现

5. **文件预览**
   - 移动端：点击下载后用系统应用打开
   - PC 端：
     - **PDF**：内嵌 `<iframe>` 或 PDF.js 预览
     - **图片**：Lightbox 大图预览
     - **视频**：内嵌播放器
     - **其他**：仍使用下载

6. **交互优化**
   - PC 端支持 **hover 效果**（按钮、卡片）
   - 评论区输入框更大（支持多行）
   - 快捷键支持：
     - `Ctrl/Cmd + D`：下载
     - `L`：点赞
     - `C`：跳转评论

**响应式 CSS 示例**：
```scss
// 移动端优先
.resource-detail-page {
  // 默认单列布局
}

// 平板及以上（768px+）
@media (min-width: 768px) {
  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 24px;
  }
}

// PC 端（1024px+）
@media (min-width: 1024px) {
  .content-wrapper {
    display: flex;
    gap: 32px;
  }

  .main-content {
    flex: 1;
    max-width: 800px;
  }

  .sidebar {
    width: 300px;
    position: sticky;
    top: 80px; // 距离顶部
    align-self: flex-start;
  }

  // 移除底部固定操作栏
  .action-bar {
    position: static;
    display: flex;
    justify-content: flex-start;
    margin-top: 24px;
  }
}
```

**总结**：PC 端布局遵循 **"主内容 + 侧边栏"** 的经典桌面应用模式，充分利用横向空间，避免过度纵向滚动。

---

## 8. 性能优化

### 8.1 图片优化
- 头像、封面使用缩略图（宽度限制 750px）
- 懒加载相关推荐的图片
- 支持 WebP 格式（优先）

### 8.2 数据加载优化
- 首屏加载：资源详情 + 统计数据
- 延迟加载：评论区（滚动到可见区域再加载）
- 延迟加载：相关推荐（延迟 1 秒）

### 8.3 缓存策略
- 资源详情缓存 5 分钟（localStorage）
- 点赞状态本地缓存（避免重复请求）
- 下载状态本地缓存

---

## 9. 错误处理

### 9.1 资源不存在
```
显示占位页面：
┌────────────────────┐
│   📦               │
│                    │
│  资源不存在或已下架 │
│                    │
│  [返回资源广场]    │
└────────────────────┘
```

### 9.2 权限不足
```
显示提示：
┌────────────────────┐
│   🔒               │
│                    │
│  该资源需要权限访问 │
│  请联系管理员      │
│                    │
│  [返回]            │
└────────────────────┘
```

### 9.3 积分不足
```
显示弹窗：
┌────────────────────┐
│  积分不足           │
│                    │
│  下载需要 5 积分   │
│  当前积分：2       │
│                    │
│  [去赚积分]  [取消]│
└────────────────────┘
```

---

## 10. 开发清单

### 10.1 组件依赖
- ✅ `ResourceCard` - 相关推荐使用
- ✅ `CommentList` - 评论区
- ✅ `DownloadConfirmDialog` - 下载确认弹窗
- ⏳ `ReportDialog` - 举报弹窗（待开发）
- ⏳ `SharePopup` - 分享弹窗（待开发）

### 10.2 实现步骤
1. **Phase 1 - 基础展示** （30%）
   - [ ] 创建页面文件 `resource/detail.vue`
   - [ ] 实现顶部导航栏
   - [ ] 实现资源信息展示区
   - [ ] 实现上传者信息卡片
   - [ ] 调用 API 加载数据

2. **Phase 2 - 核心交互** （40%）
   - [ ] 实现下载功能（含确认弹窗）
   - [ ] 实现点赞功能（含动画）
   - [ ] 集成评论组件
   - [ ] 实现固定底部操作栏

3. **Phase 3 - 扩展功能** （20%）
   - [ ] 实现相关推荐
   - [ ] 实现举报功能
   - [ ] 实现分享功能
   - [ ] 添加浏览历史记录

4. **Phase 4 - 优化完善** （10%）
   - [ ] 响应式适配
   - [ ] 性能优化
   - [ ] 错误处理
   - [ ] 骨架屏加载

---

## 11. 测试用例

### 11.1 功能测试
- [ ] 正常加载资源详情
- [ ] 资源不存在时的错误处理
- [ ] 未登录用户访问（可查看，不可下载/点赞/评论）
- [ ] 已登录用户完整流程
- [ ] 下载功能（积分充足/不足）
- [ ] 点赞/取消点赞
- [ ] 评论功能（发表/回复/删除）
- [ ] 举报功能
- [ ] 分享功能
- [ ] 相关推荐展示

### 11.2 边界测试
- [ ] 超长标题/描述的显示
- [ ] 上传者未设置头像
- [ ] 0 点赞/0 评论的显示
- [ ] 网络错误时的重试
- [ ] 快速连续点击点赞按钮
- [ ] 已下载资源的重复下载

---

## 12. 后续优化方向

**说明**：
- ✅ = 已在正文详细描述，本期可实现
- 📝 = 在正文中提及，需后期完善
- ⏳ = 仅规划，不在本期开发范围

| 优先级 | 功能 | 状态 | 说明 |
|-------|------|------|------|
| P0 | **评分功能** | ⏳ | 用户对资源打1-5星评分，计算平均分<br>需要：rating 表、平均分计算、一次评分/可修改 |
| P0 | **收藏功能** | 📝 | 加入收藏夹，快速访问<br>在正文 1.3 提及但未详细设计 |
| P1 | **文件预览** | 📝 | PDF/图片/视频在线预览<br>PC端已在 7.3 提及，移动端待设计 |
| P1 | **富文本描述** | ⏳ | 支持 Markdown 或 HTML 格式描述<br>需要：编辑器、XSS 防护、样式渲染 |
| P2 | **版本历史** | ⏳ | 资源更新时保留历史版本<br>需要：version 表、文件版本管理 |
| P2 | **下载二维码** | ⏳ | 生成资源下载二维码，方便分享<br>需要：二维码生成库、短链接服务 |
| P3 | **打印友好** | ⏳ | 优化打印样式（去除导航、侧边栏）<br>需要：@media print CSS |
| P3 | **离线缓存** | ⏳ | PWA 离线访问已下载资源<br>需要：Service Worker、IndexedDB |

**近期优先实现** (P0)：
- 评分功能：完善资源质量反馈机制
- 收藏功能：提升用户留存和资源利用率

---

## 附录：参考设计

### A. 类似产品参考
- **百度网盘** - 文件详情页布局
- **GitHub** - 仓库详情页（README + 统计）
- **B站视频详情页** - 信息展示 + 评论区
- **淘宝商品详情** - 固定底部操作栏

### B. 设计规范参考
- **Ant Design Mobile** - 移动端组件库
- **Vant** - 有赞移动端组件库
- **Material Design** - 谷歌设计规范
