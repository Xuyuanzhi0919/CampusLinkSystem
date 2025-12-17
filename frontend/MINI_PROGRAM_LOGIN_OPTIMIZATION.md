# 小程序登录流程优化方案

## 问题分析

### 当前问题
用户在首页点击"未登录"卡片 → 跳转到"我的"页面 → 立即跳回首页，形成死循环。

### 问题根因

1. **首页登录入口逻辑** (`pages/home/index.vue:326-330`)
   ```typescript
   const handleMiniLogin = () => {
     uni.switchTab({ url: '/pages/user/index' })
   }
   ```
   - 未登录时直接跳转到"我的"Tab 页

2. **"我的"页面加载逻辑** (`pages/user/index.vue:258-287`)
   ```typescript
   const loadUserData = async () => {
     const [profileRes, statsRes, ...] = await Promise.all([
       getUserProfile(),  // ← 未登录时返回 401
       getUserStats(),
       // ...
     ])
   }
   ```
   - `onShow()` 和 `onMounted()` 都会调用 `loadUserData()`
   - 未登录时 API 返回 401

3. **请求拦截器跳转逻辑** (`utils/request.ts:190-201`)
   ```typescript
   // Token刷新失败，清除登录信息并跳转首页
   this.clearToken()
   setTimeout(() => {
     uni.reLaunch({ url: '/pages/home/index' })
   }, 1500)
   ```
   - 401 响应后自动跳转回首页

### 死循环流程
```
点击"未登录"
  → switchTab('/pages/user/index')
  → onShow() 触发 loadUserData()
  → getUserProfile() 返回 401
  → reLaunch('/pages/home/index')
  → 回到首页
```

---

## 优化方案（推荐方案 A）

### 核心思路
**小程序端应该有独立的登录页面，而不是直接跳转到"我的"页面**

### 方案优势
1. ✅ 符合小程序用户体验规范
2. ✅ 避免 Tab 页面之间的频繁跳转
3. ✅ 可以展示清晰的登录引导和权益说明
4. ✅ 支持微信授权登录流程

### 实施步骤

#### 1. 创建小程序登录页面
**文件路径**: `frontend/src/pages/auth/mp-login.vue`

```vue
<template>
  <view class="mp-login-page">
    <!-- Logo 和标题 -->
    <view class="header">
      <image class="logo" src="/static/logo.png" mode="aspectFit" />
      <text class="title">欢迎来到 CampusLink</text>
      <text class="subtitle">高校资源互助与问答平台</text>
    </view>

    <!-- 权益说明 -->
    <view class="benefits">
      <view class="benefit-item">
        <text class="benefit-icon">📚</text>
        <text class="benefit-text">海量学习资源免费下载</text>
      </view>
      <view class="benefit-item">
        <text class="benefit-icon">💬</text>
        <text class="benefit-text">快速获取问题解答</text>
      </view>
      <view class="benefit-item">
        <text class="benefit-icon">🎯</text>
        <text class="benefit-text">发布任务赚取积分</text>
      </view>
      <view class="benefit-item">
        <text class="benefit-icon">🎉</text>
        <text class="benefit-text">参与社团活动</text>
      </view>
    </view>

    <!-- 登录按钮 -->
    <view class="login-actions">
      <button
        class="wechat-login-btn"
        type="primary"
        open-type="getUserInfo"
        @getuserinfo="handleWechatLogin"
        :loading="loginLoading"
      >
        <text class="btn-icon">👤</text>
        <text class="btn-text">微信一键登录</text>
      </button>

      <view class="agreement">
        <text class="agreement-text">登录即表示同意</text>
        <text class="agreement-link" @click="handleViewAgreement('user')">《用户协议》</text>
        <text class="agreement-text">和</text>
        <text class="agreement-link" @click="handleViewAgreement('privacy')">《隐私政策》</text>
      </view>
    </view>

    <!-- 跳过登录 -->
    <view class="skip-login" @click="handleSkipLogin">
      <text class="skip-text">暂不登录，先逛逛</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { wechatLogin } from '@/services/auth'

const userStore = useUserStore()
const loginLoading = ref(false)

// 微信登录
const handleWechatLogin = async (e: any) => {
  try {
    loginLoading.value = true

    // 1. 获取微信授权信息
    const userInfo = e.detail.userInfo
    if (!userInfo) {
      uni.showToast({ title: '授权已取消', icon: 'none' })
      return
    }

    // 2. 获取微信登录 code
    const loginRes = await uni.login({ provider: 'weixin' }) as any
    if (!loginRes.code) {
      throw new Error('获取微信登录凭证失败')
    }

    // 3. 调用后端登录接口
    const response = await wechatLogin({
      code: loginRes.code,
      userInfo: userInfo
    })

    // 4. 保存登录信息
    userStore.login(response)

    // 5. 跳转到"我的"页面
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages/user/index' })
    }, 1000)

  } catch (error: any) {
    console.error('微信登录失败:', error)
    uni.showToast({
      title: error.message || '登录失败，请重试',
      icon: 'none'
    })
  } finally {
    loginLoading.value = false
  }
}

// 查看协议
const handleViewAgreement = (type: 'user' | 'privacy') => {
  const url = type === 'user'
    ? '/pages/about/user-agreement'
    : '/pages/about/privacy-policy'
  uni.navigateTo({ url })
}

// 跳过登录
const handleSkipLogin = () => {
  uni.switchTab({ url: '/pages/home/index' })
}
</script>

<style scoped lang="scss">
.mp-login-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #EFF6FF 0%, #FFFFFF 50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 64rpx;
}

.header {
  text-align: center;
  margin-bottom: 80rpx;

  .logo {
    width: 160rpx;
    height: 160rpx;
    margin-bottom: 32rpx;
  }

  .title {
    display: block;
    font-size: 44rpx;
    font-weight: 700;
    color: #0F172A;
    margin-bottom: 16rpx;
  }

  .subtitle {
    display: block;
    font-size: 28rpx;
    color: #64748B;
  }
}

.benefits {
  width: 100%;
  margin-bottom: 80rpx;

  .benefit-item {
    display: flex;
    align-items: center;
    padding: 24rpx 0;
  }

  .benefit-icon {
    font-size: 40rpx;
    margin-right: 24rpx;
  }

  .benefit-text {
    font-size: 30rpx;
    color: #334155;
  }
}

.login-actions {
  width: 100%;
  margin-bottom: 40rpx;

  .wechat-login-btn {
    width: 100%;
    height: 96rpx;
    background: linear-gradient(135deg, #10B981 0%, #059669 100%);
    border-radius: 48rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 8rpx 24rpx rgba(16, 185, 129, 0.3);
    border: none;
    margin-bottom: 32rpx;

    &::after {
      border: none;
    }

    .btn-icon {
      font-size: 36rpx;
      margin-right: 12rpx;
    }

    .btn-text {
      font-size: 32rpx;
      font-weight: 600;
      color: #FFFFFF;
    }
  }

  .agreement {
    text-align: center;
    line-height: 1.6;

    .agreement-text {
      font-size: 24rpx;
      color: #94A3B8;
    }

    .agreement-link {
      font-size: 24rpx;
      color: #2563EB;
      text-decoration: underline;
    }
  }
}

.skip-login {
  .skip-text {
    font-size: 28rpx;
    color: #64748B;
    text-decoration: underline;
  }
}
</style>
```

#### 2. 修改首页登录入口
**文件**: `pages/home/index.vue`

```typescript
// #ifdef MP-WEIXIN
const handleMiniLogin = () => {
  // 跳转到小程序登录页面
  uni.navigateTo({
    url: '/pages/auth/mp-login'
  })
}
// #endif
```

#### 3. 修改"我的"页面逻辑
**文件**: `pages/user/index.vue`

在 `loadUserData()` 前添加登录检查：

```typescript
const loadUserData = async () => {
  // #ifdef MP-WEIXIN
  // 小程序端：未登录时显示登录引导，不加载数据
  if (!userStore.isLoggedIn) {
    loading.value = false
    return
  }
  // #endif

  try {
    loading.value = true
    // ... 原有逻辑
  } catch (error: any) {
    console.error('加载用户数据失败:', error)

    // #ifdef MP-WEIXIN
    // 小程序端：401 错误时不自动跳转，显示登录引导
    if (error.message?.includes('401') || error.message?.includes('未登录')) {
      userStore.clearLoginInfo()
      loading.value = false
      return
    }
    // #endif

    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}
```

在模板中添加未登录状态：

```vue
<template>
  <view class="user-profile-page">
    <!-- 未登录状态（仅小程序） -->
    <!-- #ifdef MP-WEIXIN -->
    <view v-if="!userStore.isLoggedIn && !loading" class="not-logged-in">
      <view class="empty-icon">👤</view>
      <text class="empty-text">还未登录</text>
      <text class="empty-hint">登录后可查看个人信息和数据</text>
      <button class="login-btn" @click="handleGoToLogin">
        去登录
      </button>
    </view>
    <!-- #endif -->

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 已登录状态 -->
    <view v-else-if="userStore.isLoggedIn" class="content-container">
      <!-- 原有内容 -->
    </view>
  </view>
</template>

<script setup lang="ts">
// ... 原有代码

// #ifdef MP-WEIXIN
const handleGoToLogin = () => {
  uni.navigateTo({
    url: '/pages/auth/mp-login'
  })
}
// #endif
</script>

<style lang="scss" scoped>
/* #ifdef MP-WEIXIN */
.not-logged-in {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64rpx;

  .empty-icon {
    font-size: 120rpx;
    margin-bottom: 32rpx;
    opacity: 0.3;
  }

  .empty-text {
    font-size: 36rpx;
    font-weight: 600;
    color: #0F172A;
    margin-bottom: 16rpx;
  }

  .empty-hint {
    font-size: 28rpx;
    color: #64748B;
    margin-bottom: 48rpx;
  }

  .login-btn {
    width: 400rpx;
    height: 88rpx;
    background: linear-gradient(135deg, #2563EB 0%, #3B82F6 100%);
    color: #FFFFFF;
    font-size: 32rpx;
    font-weight: 600;
    border-radius: 44rpx;
    border: none;

    &::after {
      border: none;
    }
  }
}
/* #endif */
</style>
```

#### 4. 优化请求拦截器（可选）
**文件**: `utils/request.ts`

为小程序添加特殊处理：

```typescript
// 401 处理
if (res.code === 401) {
  // ... 原有刷新逻辑

  if (!refreshed) {
    // Token刷新失败
    this.clearToken()
    const friendlyMessage = getFriendlyErrorMessage({ code: 401 })
    uni.showToast({ title: friendlyMessage, icon: 'none' })

    // #ifdef MP-WEIXIN
    // 小程序端：不自动跳转，由页面自己处理
    // 只在非 Tab 页面时才跳转
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    const isTabPage = [
      'pages/home/index',
      'pages/question/index',
      'pages/resource/index',
      'pages/task/index',
      'pages/club/list',
      'pages/message/index',
      'pages/user/index'
    ].some(path => currentPage.route?.includes(path))

    if (!isTabPage) {
      setTimeout(() => {
        uni.reLaunch({ url: '/pages/home/index' })
      }, 1500)
    }
    // #endif

    // #ifdef H5
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/home/index' })
    }, 1500)
    // #endif

    return Promise.reject(new Error(res.message))
  }
}
```

#### 5. 配置路由（pages.json）
添加登录页面路由：

```json
{
  "path": "pages/auth/mp-login",
  "style": {
    "navigationBarTitleText": "登录",
    "navigationBarBackgroundColor": "#EFF6FF",
    "navigationBarTextStyle": "black",
    "enablePullDownRefresh": false
  }
}
```

---

## 备选方案 B（快速修复）

如果不想创建新页面，可以快速修复：

### 修改首页登录入口
```typescript
// #ifdef MP-WEIXIN
const handleMiniLogin = () => {
  uni.showModal({
    title: '提示',
    content: '小程序登录功能开发中，敬请期待',
    showCancel: false
  })
}
// #endif
```

### 修改"我的"页面
在 `loadUserData()` 开头添加：

```typescript
const loadUserData = async () => {
  // #ifdef MP-WEIXIN
  if (!userStore.isLoggedIn) {
    loading.value = false
    return
  }
  // #endif

  // ... 原有逻辑
}
```

---

## 测试清单

完成修改后需要测试：

- [ ] 首页点击"未登录"卡片 → 跳转到登录页面
- [ ] 登录页面点击"微信一键登录" → 授权成功 → 跳转到"我的"页面
- [ ] 登录页面点击"暂不登录" → 返回首页
- [ ] 未登录时切换到"我的" Tab → 显示登录引导，不跳转
- [ ] 登录后切换到"我的" Tab → 正常加载用户数据
- [ ] 登出后切换到"我的" Tab → 显示登录引导

---

## 推荐实施顺序

1. ✅ **先实施快速修复（方案 B）** - 防止死循环
2. ✅ **测试基本流程** - 确认不再跳转
3. ✅ **实施完整方案（方案 A）** - 创建登录页面
4. ✅ **集成微信登录** - 对接后端 API
5. ✅ **完整测试** - 覆盖所有场景

---

## 相关文件清单

需要修改的文件：
1. `frontend/src/pages/auth/mp-login.vue` - 新建
2. `frontend/src/pages/home/index.vue` - 修改登录入口
3. `frontend/src/pages/user/index.vue` - 添加未登录状态
4. `frontend/src/utils/request.ts` - 优化 401 处理（可选）
5. `frontend/src/pages.json` - 添加路由配置

---

## 总结

**核心改进**：
- ✅ 独立的登录页面，符合小程序 UX 规范
- ✅ 避免 Tab 页面之间的死循环跳转
- ✅ 未登录状态有清晰的引导
- ✅ 401 错误不会强制跳转 Tab 页面

**用户体验提升**：
- 登录流程清晰明了
- 未登录时可以先浏览内容
- 避免频繁的页面跳转
- 符合微信小程序最佳实践
