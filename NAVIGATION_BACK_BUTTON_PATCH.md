# 导航返回按钮补丁说明

## 📝 修改说明

为资源页和问答页添加智能返回按钮，仅在通过 `navigateTo` 进入时显示。

---

## 🎯 修改 1：资源页 (`frontend/src/pages/resource/index.vue`)

### 1.1 模板部分 - 添加返回按钮

**位置**：第 5-10 行左右

**原代码**：
```vue
    <view class="top-nav-fixed" :class="{ collapsed: isHeaderCollapsed }">
      <view class="top-nav-container">
        <!-- Logo -->
        <view class="brand-logo">
          <Icon name="file-text" :size="20" class="logo-icon" />
          <text class="logo-text">资源广场</text>
        </view>
```

**修改为**：
```vue
    <view class="top-nav-fixed" :class="{ collapsed: isHeaderCollapsed }">
      <view class="top-nav-container">
        <!-- 🎯 返回按钮（仅在通过navigateTo进入时显示） -->
        <view v-if="showBackButton" class="back-button" @click="handleBack">
          <Icon name="arrow-left" :size="20" class="back-icon" />
        </view>

        <!-- Logo -->
        <view class="brand-logo">
          <Icon name="file-text" :size="20" class="logo-icon" />
          <text class="logo-text">资源广场</text>
        </view>
```

---

### 1.2 Script 部分 - 添加响应式变量和逻辑

**位置**：在 `onMounted` 之前添加（大约第 420 行附近，`const resources = ref<ResourceItem[]>([])` 下方）

**添加代码**：
```typescript
// 🎯 返回按钮相关
const showBackButton = ref(false)

// 🎯 检测页面来源，决定是否显示返回按钮
onMounted(() => {
  const pages = getCurrentPages()
  // 如果页面栈深度 > 1，说明是通过 navigateTo 进入的，显示返回按钮
  showBackButton.value = pages.length > 1

  // 原有的 onMounted 逻辑继续保留...
})

// 🎯 返回按钮点击处理
const handleBack = () => {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    // 可以返回上一页
    uni.navigateBack()
  } else {
    // 无法返回，切换到首页
    uni.switchTab({ url: '/pages/home/index' })
  }
}
```

**注意**：如果文件中已经有 `onMounted`，则将检测逻辑添加到现有 `onMounted` 函数的开头。

---

### 1.3 样式部分 - 添加返回按钮样式

**位置**：在 `<style scoped lang="scss">` 的顶部导航栏样式附近添加

**添加代码**：
```scss
/* 🎯 返回按钮样式 */
.back-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  margin-right: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0; /* 防止按钮被压缩 */

  &:hover {
    background-color: rgba(0, 0, 0, 0.05);
    border-radius: 8px;
  }

  &:active {
    transform: scale(0.96);
  }
}

.back-icon {
  color: #333;
}

/* 调整 brand-logo 样式，适配返回按钮 */
.brand-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  /* 如果有 flex: 1，移除它，改用 margin-right: auto */
}
```

---

## 🎯 修改 2：问答页 (`frontend/src/pages/question/index.vue`)

### 2.1 模板部分 - 添加返回按钮

**位置**：第 5-10 行左右

**原代码**：
```vue
    <view class="top-nav-fixed" :class="{ collapsed: isHeaderCollapsed }">
      <view class="top-nav-container">
        <!-- Logo -->
        <view class="brand-logo">
          <Icon name="message-circle" :size="isHeaderCollapsed ? 18 : 20" class="logo-icon" />
          <text class="logo-text">问答社区</text>
        </view>
```

**修改为**：
```vue
    <view class="top-nav-fixed" :class="{ collapsed: isHeaderCollapsed }">
      <view class="top-nav-container">
        <!-- 🎯 返回按钮（仅在通过navigateTo进入时显示） -->
        <view v-if="showBackButton" class="back-button" @click="handleBack">
          <Icon name="arrow-left" :size="isHeaderCollapsed ? 18 : 20" class="back-icon" />
        </view>

        <!-- Logo -->
        <view class="brand-logo">
          <Icon name="message-circle" :size="isHeaderCollapsed ? 18 : 20" class="logo-icon" />
          <text class="logo-text">问答社区</text>
        </view>
```

---

### 2.2 Script 部分 - 添加响应式变量和逻辑

**添加代码**（与资源页相同）：
```typescript
// 🎯 返回按钮相关
const showBackButton = ref(false)

// 🎯 检测页面来源
onMounted(() => {
  const pages = getCurrentPages()
  showBackButton.value = pages.length > 1

  // 原有的 onMounted 逻辑继续保留...
})

// 🎯 返回按钮点击处理
const handleBack = () => {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uni.switchTab({ url: '/pages/home/index' })
  }
}
```

---

### 2.3 样式部分 - 添加返回按钮样式

**添加代码**（与资源页相同）：
```scss
/* 🎯 返回按钮样式 */
.back-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  margin-right: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;

  &:hover {
    background-color: rgba(0, 0, 0, 0.05);
    border-radius: 8px;
  }

  &:active {
    transform: scale(0.96);
  }
}

.back-icon {
  color: #333;
}
```

---

## ✅ 验证步骤

修改完成后，测试以下场景：

### 场景 1：通过 TabBar 进入（不应该显示返回按钮）
1. 打开首页
2. 点击底部 TabBar 的"资源"或"问答"
3. **预期**：顶部导航栏 **不显示返回按钮**

### 场景 2：通过 navigateTo 进入（应该显示返回按钮）
1. 打开首页
2. 在某个地方通过 `uni.navigateTo({ url: '/pages/resource/index' })` 跳转
3. **预期**：顶部导航栏 **显示返回按钮**
4. 点击返回按钮，应该返回上一页

### 场景 3：返回按钮无法返回时（自动跳转首页）
1. 清空页面栈，直接打开资源页
2. 点击返回按钮
3. **预期**：跳转到首页

---

## 📌 注意事项

1. **导入 `getCurrentPages`**：
   - `getCurrentPages` 是 uni-app 的全局 API，无需导入

2. **检查现有 `onMounted`**：
   - 如果文件中已有 `onMounted`，将检测逻辑添加到现有函数的开头
   - 不要创建重复的 `onMounted`

3. **样式冲突检查**：
   - 如果已有 `.back-button` 或 `.back-icon` 样式，请合并或重命名

4. **响应式适配**：
   - 返回按钮会自动适配移动端和桌面端
   - 在小屏幕下，按钮宽度固定为 40px

---

## 🚀 下一步优化（可选）

如果你想进一步优化，可以考虑：

### 优化 1：平滑过渡动画
```scss
.back-button {
  opacity: 0;
  animation: fadeInLeft 0.3s ease forwards;
}

@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}
```

### 优化 2：提示文字（桌面端）
```vue
<view v-if="showBackButton" class="back-button" @click="handleBack" title="返回上一页">
  <Icon name="arrow-left" :size="20" class="back-icon" />
  <!-- #ifdef H5 -->
  <text class="back-text">返回</text>
  <!-- #endif -->
</view>
```

### 优化 3：统一返回逻辑到工具函数
创建 `frontend/src/utils/navigation.ts`：
```typescript
export const smartBack = () => {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uni.switchTab({ url: '/pages/home/index' })
  }
}

export const shouldShowBackButton = () => {
  const pages = getCurrentPages()
  return pages.length > 1
}
```

然后在页面中使用：
```typescript
import { smartBack, shouldShowBackButton } from '@/utils/navigation'

const showBackButton = ref(false)

onMounted(() => {
  showBackButton.value = shouldShowBackButton()
})

const handleBack = () => {
  smartBack()
}
```

---

**修改完成时间**：2025-12-29
**预计修改时间**：10-15 分钟
**难度**：⭐⭐ (简单)
