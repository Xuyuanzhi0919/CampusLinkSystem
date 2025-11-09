# 登录注册弹窗组件使用文档

## 组件概述

根据 `login-register.md` UI设计文档和 `API前端对接文档.md` API接口设计,已完成登录注册弹窗组件的开发。

## 文件清单

### 1. 核心组件
- **`frontend/src/components/AuthModal.vue`** - 登录/注册弹窗组件
- **`frontend/src/services/auth.ts`** - 认证相关API服务
- **`frontend/src/stores/user.ts`** - 用户状态管理(已存在,已适配)
- **`frontend/src/utils/request.ts`** - HTTP请求封装(已存在)

### 2. 集成页面
- **`frontend/src/pages/home/index.vue`** - 首页(已集成AuthModal)
- **`frontend/src/components/PCFloatingNav.vue`** - PC端悬浮导航(已添加登录/注册入口)

## 功能特性

### ✅ UI设计实现

符合 `login-register.md` 设计规范:

1. **视觉风格**
   - 中心模态弹窗(480px宽,圆角16px)
   - 半透明遮罩(rgba(10,15,30,.38) + 模糊6px)
   - 主色调 #2563EB,符合CampusLink品牌规范

2. **交互动效**
   - Modal入场:Scale 0.98 → 1 + 淡入(200ms)
   - Tab切换:滑块动画(200ms)
   - 错误态:输入框抖动 + 描边变红
   - 成功态:按钮填充动画 + 勾号显示

3. **表单校验**
   - 实时校验:邮箱格式、手机号格式、密码强度
   - 错误提示:红色描边 + 下方错误文字
   - 密码强度:弱/中/强 + 进度条

4. **无障碍支持**
   - 键盘访问(Tab/Shift+Tab/Enter/Esc)
   - 焦点环:2px主色35%透明
   - CapsLock提示(TODO:可扩展)

### ✅ API对接实现

符合 `API前端对接文档.md` 接口规范:

#### 登录接口
```typescript
POST /auth/login
请求: { username: string, password: string }
响应: { userId, username, nickname, role, token, refreshToken, expiresIn }
```

#### 注册接口
```typescript
POST /auth/register
请求: {
  username: string
  password: string
  email: string
  phone: string
  nickname: string
  schoolId: number
  studentId: string
}
响应: { userId, username, token, refreshToken, expiresIn }
```

#### 学校列表
```typescript
GET /school/list
参数: { province?, city?, keyword?, page?, pageSize? }
响应: { records: School[], total, current, size, pages }
```

### ✅ 设计调整说明

针对UI设计与API的差异,进行了以下调整:

| 设计文档 | API接口 | 调整方案 |
|---------|--------|---------|
| 登录账户:邮箱/手机号 | username字段 | 保留"邮箱/手机号"提示,后端需支持多字段登录 |
| 注册无username字段 | 必需username | 添加"用户名"输入框(唯一登录ID) |
| 注册无学校选择 | 必需schoolId | 添加学校选择器(下拉选择) |
| 注册无学号字段 | 必需studentId | 添加学号输入框 |
| 验证码(可选) | API未提供 | 暂未实现,预留扩展点 |
| 三方登录 | API未提供 | 暂未实现,预留扩展点 |

## 使用方法

### 1. 在页面中使用

```vue
<template>
  <view>
    <!-- 触发按钮 -->
    <button @click="showLogin">登录</button>
    <button @click="showRegister">注册</button>

    <!-- 登录注册弹窗 -->
    <AuthModal
      :visible="authModalVisible"
      :default-mode="authMode"
      @update:visible="authModalVisible = $event"
      @success="handleAuthSuccess"
    />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import AuthModal from '@/components/AuthModal.vue'

const authModalVisible = ref(false)
const authMode = ref<'login' | 'register'>('login')

const showLogin = () => {
  authMode.value = 'login'
  authModalVisible.value = true
}

const showRegister = () => {
  authMode.value = 'register'
  authModalVisible.value = true
}

const handleAuthSuccess = () => {
  console.log('登录/注册成功')
  // 刷新页面数据或跳转
}
</script>
```

### 2. PC悬浮导航集成

已在 `PCFloatingNav.vue` 中集成:

```vue
<!-- 未登录时显示登录/注册按钮 -->
<PCFloatingNav @show-auth="showAuthModal" />

<script>
const showAuthModal = (mode: 'login' | 'register') => {
  authMode.value = mode
  authModalVisible.value = true
}
</script>
```

### 3. 用户状态管理

使用 `useUserStore()` 管理登录状态:

```typescript
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 检查登录状态
if (userStore.isLoggedIn) {
  console.log('已登录', userStore.userInfo)
} else {
  console.log('未登录')
}

// 退出登录
userStore.logout()
```

## 组件Props

### AuthModal

| 属性 | 类型 | 默认值 | 说明 |
|------|------|-------|------|
| visible | Boolean | false | 是否显示弹窗 |
| defaultMode | 'login' \| 'register' | 'login' | 默认显示模式 |

### AuthModal Events

| 事件 | 参数 | 说明 |
|------|------|------|
| update:visible | (value: boolean) | 弹窗显示状态变化 |
| success | - | 登录/注册成功 |

## 表单验证规则

### 登录表单
- **用户名**:必填
- **密码**:必填

### 注册表单
- **昵称**:2-16字符,不含特殊符号
- **用户名**:必填
- **邮箱**:标准邮箱格式(正则:/^[^\s@]+@[^\s@]+\.[^\s@]+$/)
- **手机号**:11位,1开头(正则:/^1[3-9]\d{9}$/)
- **学校**:必选
- **学号**:必填
- **密码**:≥8位
- **确认密码**:需与密码一致
- **协议**:必须勾选

## 密码强度规则

```typescript
弱(weak):   长度<8 或 仅含2种字符类型
中(medium): 含3种字符类型(大写/小写/数字/符号)
强(strong): 含4种字符类型
```

## 样式变量

遵循 `config.ts` 设计token:

```scss
// 颜色
--primary: #2563EB        // 主色
--danger: #DC2626         // 错误色
--text: #0F172A           // 主文字
--subtext: #64748B        // 副文字

// 圆角
--modal-radius: 16px      // 弹窗圆角
--input-radius: 20px      // 输入框圆角
--btn-radius: 24px        // 按钮圆角

// 尺寸
--input-height: 88rpx     // 输入框高度
--btn-height: 88rpx       // 按钮高度

// 阴影
--shadow-modal: 0 24px 64px rgba(0,0,0,0.18)
--focus-ring: 0 0 0 6rpx rgba(37,99,235,0.12)
```

## 已知限制与TODO

### 功能限制
- ❌ 图形验证码(API未提供)
- ❌ 短信验证码(API未提供)
- ❌ 三方登录(API未提供)
- ❌ 找回密码(API未提供)
- ❌ CapsLock提示
- ❌ 异地登录提醒

### 扩展计划
1. **Token自动刷新**:在request.ts中实现
2. **记住我功能**:7天免登录
3. **深色模式**:适配系统主题
4. **移动端适配**:底部弹出层(高度70%,圆角20)
5. **表单状态持久化**:刷新页面保留输入

## 测试建议

### 单元测试
```typescript
// 1. 表单验证测试
测试邮箱格式校验
测试手机号格式校验
测试密码强度计算
测试两次密码一致性

// 2. UI交互测试
测试Tab切换动画
测试关闭弹窗(Esc键/遮罩点击/关闭按钮)
测试密码显隐切换
测试Loading状态

// 3. API集成测试
测试登录成功流程
测试登录失败(错误提示)
测试注册成功流程
测试注册失败(用户名重复等)
```

### 手工测试
1. **登录流程**
   - 输入正确账号密码 → 点击登录 → 成功跳转
   - 输入错误密码 → 显示错误提示 → 聚焦密码框
   - 空字段提交 → 显示必填提示

2. **注册流程**
   - 填写完整信息 → 勾选协议 → 点击注册 → 成功并自动登录
   - 邮箱格式错误 → 显示格式提示
   - 两次密码不一致 → 显示不一致提示
   - 未勾选协议 → 提示需同意协议

3. **视觉效果**
   - 检查弹窗居中
   - 检查动画流畅性
   - 检查响应式布局(PC/H5/小程序)
   - 检查深浅色主题

## 常见问题

### Q1: 弹窗不显示?
```typescript
// 检查visible绑定
<AuthModal :visible="authModalVisible" />

// 确保设置为true
authModalVisible.value = true
```

### Q2: 登录后页面未刷新?
```typescript
// 在success回调中刷新数据
const handleAuthSuccess = () => {
  // 刷新用户信息
  loadUserData()
  // 或跳转页面
  uni.reLaunch({ url: '/pages/home/index' })
}
```

### Q3: 学校列表为空?
```typescript
// 确保后端/school/list接口正常
// 检查网络请求
// 查看控制台错误日志
```

### Q4: Token未自动添加到请求头?
```typescript
// 检查request.ts的beforeRequest拦截器
// 确保Token已存储到uni.storage
// 检查tokenKey配置是否正确
```

## 更新日志

### v1.0.0 (2025-11-09)
- ✅ 完成登录/注册弹窗UI
- ✅ 对接后端登录/注册/学校列表API
- ✅ 集成到首页和PC悬浮导航
- ✅ 实现表单验证和错误提示
- ✅ 实现密码强度检测
- ✅ 实现动画效果(Modal入场/Tab切换/错误抖动)
- ✅ 用户状态管理(Pinia store)
- ✅ Token存储和自动添加请求头

---

**维护者**: CampusLink 前端团队
**文档更新时间**: 2025-11-09
