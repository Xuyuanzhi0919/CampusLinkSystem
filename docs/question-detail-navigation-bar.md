# 问答详情页导航栏优化方案

## 📋 优化背景

问答详情页顶部缺少返回按钮，用户无法直观地返回问答中心或上一页，需要借鉴资源详情页的设计，添加顶部导航栏。

## 🎯 优化目标

1. 添加顶部导航栏，提供清晰的返回入口
2. 实现智能返回逻辑（区分 tabBar 页面和普通页面）
3. 添加更多菜单功能（编辑、举报、分享）
4. 保持与资源详情页的视觉一致性

## 🔨 实现方案

### 1. 导航栏结构

参考 `frontend/src/pages/resource/detail.vue`，添加三段式导航栏：

```vue
<view class="nav-bar">
  <!-- 左侧：返回按钮 -->
  <view class="nav-left" @click="goBack">
    <text class="nav-icon">‹</text>
    <text class="nav-text">返回</text>
  </view>

  <!-- 中间：页面标题 -->
  <view class="nav-center">
    <text class="nav-title">问题详情</text>
  </view>

  <!-- 右侧：更多菜单 -->
  <view class="nav-right" @click="showMoreMenu">
    <text class="nav-icon">•••</text>
  </view>
</view>
```

**位置**: 放在 `.detail-page` 容器的最顶部，在加载状态之前

### 2. 智能返回逻辑

```typescript
const goBack = () => {
  const pages = getCurrentPages()

  // 情况1: 页面栈只有一页（直接访问详情页）
  if (pages.length === 1) {
    uni.switchTab({ url: '/pages/question/index' })
  }
  // 情况2: 上一页是问答中心（tabBar页面）
  else if (pages.length >= 2) {
    const prevPage = pages[pages.length - 2]
    const prevRoute = prevPage.route || ''

    if (prevRoute === 'pages/question/index') {
      uni.switchTab({ url: '/pages/question/index' })
    } else {
      uni.navigateBack()
    }
  }
  // 情况3: 默认返回
  else {
    uni.navigateBack()
  }
}
```

**返回逻辑说明**:
- **直接访问详情页**（如从外部链接跳转）→ 使用 `switchTab` 跳转到问答中心
- **从问答中心进入**（tabBar页面）→ 使用 `switchTab` 返回问答中心
- **从其他普通页面进入**（如我的问题列表）→ 使用 `navigateBack` 返回上一页

> **为什么要区分 `switchTab` 和 `navigateBack`？**
>
> uni-app 规范要求：跳转到 tabBar 页面必须使用 `uni.switchTab`，否则会报错。因此需要检测上一页是否为 tabBar 页面。

### 3. 更多菜单功能

#### 弹出层结构

```vue
<view v-if="showMorePopup" class="more-menu-overlay" @click="closeMoreMenu">
  <view class="more-menu-content" @click.stop>
    <!-- 编辑问题（仅问题所有者可见） -->
    <view v-if="isMyQuestion" class="menu-item" @click="handleEditQuestion">
      <text class="menu-icon">✏️</text>
      <text class="menu-label">编辑问题</text>
    </view>

    <!-- 举报 -->
    <view class="menu-item" @click="handleReportQuestion">
      <text class="menu-icon">🚨</text>
      <text class="menu-label">举报</text>
    </view>

    <!-- 分享 -->
    <view class="menu-item" @click="handleShareQuestion">
      <text class="menu-icon">📤</text>
      <text class="menu-label">分享</text>
    </view>

    <!-- 取消 -->
    <view class="menu-item menu-item--cancel" @click="closeMoreMenu">
      <text class="menu-label">取消</text>
    </view>
  </view>
</view>
```

#### 菜单功能实现

```typescript
// 菜单状态
const showMorePopup = ref(false)

// 显示菜单
const showMoreMenu = () => {
  showMorePopup.value = true
}

// 关闭菜单
const closeMoreMenu = () => {
  showMorePopup.value = false
}

// 编辑问题（跳转到发布页，携带问题ID）
const handleEditQuestion = () => {
  closeMoreMenu()
  uni.navigateTo({
    url: `/pages/question/ask?id=${questionId.value}`
  })
}

// 举报问题
const handleReportQuestion = () => {
  closeMoreMenu()
  uni.showModal({
    title: '举报',
    content: '确定要举报这个问题吗？',
    success: (res) => {
      if (res.confirm) {
        uni.showToast({
          title: '举报成功，我们会尽快处理',
          icon: 'success'
        })
      }
    }
  })
}

// 分享问题（复用现有分享功能）
const handleShareQuestion = () => {
  closeMoreMenu()
  handleShare()
}
```

### 4. 样式设计

#### 导航栏样式

```scss
.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 32rpx;
  background: #FFFFFF;
  box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.05);  // 轻微底部阴影
  position: sticky;  // 固定在顶部
  top: 0;
  z-index: 100;
}

.nav-left,
.nav-right {
  display: flex;
  align-items: center;
  padding: 8rpx;
  cursor: pointer;
  transition: opacity 0.2s;

  &:active {
    opacity: 0.6;  // 点击反馈
  }
}

.nav-icon {
  font-size: 40rpx;
  color: #333333;
}

.nav-text {
  font-size: 28rpx;
  color: #333333;
  margin-left: 4rpx;
}

.nav-center {
  flex: 1;
  text-align: center;
}

.nav-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #333333;
}
```

#### 更多菜单样式

```scss
.more-menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);  // 半透明遮罩
  z-index: 999;
  display: flex;
  align-items: flex-end;
  animation: fadeIn 0.3s ease;  // 淡入动画
}

.more-menu-content {
  width: 100%;
  background: #FFFFFF;
  border-radius: 24rpx 24rpx 0 0;  // 顶部圆角
  padding: 24rpx 0;
  animation: slideUp 0.3s ease;  // 从底部滑入
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 32rpx 48rpx;
  transition: background 0.2s;

  &:active {
    background: #F5F7FA;  // 点击反馈
  }

  &--cancel {
    justify-content: center;
    border-top: 1rpx solid #F0F0F0;
    margin-top: 16rpx;
    padding-top: 32rpx;

    .menu-label {
      color: #999999;
    }
  }
}

// 动画
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}
```

## ✅ 优化效果

### 用户体验提升

1. **导航清晰**: 顶部固定导航栏，返回入口明显
2. **智能返回**: 自动识别来源页面，选择最佳返回方式
3. **功能丰富**: 更多菜单提供编辑、举报、分享等操作
4. **视觉一致**: 与资源详情页保持相同的设计风格

### 交互优化

1. **固定导航**: `position: sticky` 保证滚动时导航栏始终可见
2. **点击反馈**: 按钮点击时有透明度变化，提升交互感
3. **流畅动画**: 菜单弹出有淡入和滑入动画，体验流畅
4. **权限控制**: 编辑按钮仅对问题所有者可见

### 兼容性

- ✅ 支持 H5、微信小程序、App 多端
- ✅ 兼容直接访问详情页的场景
- ✅ 兼容从 tabBar 和普通页面进入的场景
- ✅ 复用现有的 `handleShare` 分享功能

## 📊 对比分析

### 优化前

```
┌─────────────────────┐
│  [问题内容直接显示]  │
│                     │
│  用户需要使用系统返回键 │
│  或手势返回           │
└─────────────────────┘
```

**问题**:
- ❌ 无明显返回入口
- ❌ 不符合常规APP设计规范
- ❌ 新用户不知道如何返回

### 优化后

```
┌─────────────────────┐
│ ‹ 返回  问题详情  ••• │ ← 固定导航栏
├─────────────────────┤
│  [问题内容]          │
│                     │
│  清晰的返回按钮      │
│  丰富的操作菜单      │
└─────────────────────┘
```

**改进**:
- ✅ 导航栏固定在顶部
- ✅ 返回按钮明显
- ✅ 提供更多操作入口
- ✅ 符合用户习惯

## 🔗 参考设计

本次优化参考了以下设计：

1. **资源详情页**: `frontend/src/pages/resource/detail.vue`
   - 导航栏结构
   - 智能返回逻辑
   - 更多菜单设计

2. **uni-app 官方规范**:
   - tabBar 页面跳转规范
   - 页面栈管理最佳实践

## 🚀 后续扩展

### 可选功能

1. **收藏功能**: 在导航栏右侧添加收藏按钮
2. **下拉刷新**: 已实现，导航栏不影响下拉刷新
3. **面包屑导航**: Web 端可显示 "首页 > 问答中心 > 问题详情"
4. **快捷操作**: 长按导航栏显示更多快捷操作

### 性能优化

1. **懒加载**: 菜单内容按需渲染
2. **事件节流**: 防止快速点击触发多次操作
3. **动画优化**: 使用 GPU 加速的 transform 动画

## 📝 注意事项

1. **tabBar 跳转**: 必须使用 `uni.switchTab`，不能使用 `uni.navigateTo`
2. **页面栈检测**: 使用 `getCurrentPages()` 获取页面栈信息
3. **遮罩点击**: 点击遮罩层关闭菜单时，需要在内容区使用 `@click.stop` 阻止事件冒泡
4. **兼容性**: `.route` 属性在某些平台可能为空，需要做空值判断

## 📚 相关文档

- [uni-app 页面路由文档](https://uniapp.dcloud.net.cn/api/router)
- [uni-app 页面栈管理](https://uniapp.dcloud.net.cn/tutorial/page.html)
- [问答模块完整文档](./question-module-analysis.md)
