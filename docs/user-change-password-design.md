# 修改密码页面设计文档

## 一、页面概述

**页面名称**：修改密码（Change Password）
**访问路径**：`/pages/user/change-password`
**功能描述**：允许用户修改登录密码，需要验证旧密码

## 二、UI 设计

### 1. 页面布局

简洁的表单布局，包含三个密码输入框和一个提交按钮。

```
┌────────────────────────────────┐
│  🔒 修改密码                    │
│                                │
│  ┌──────────────────────────┐ │
│  │ 🔑 旧密码                 │ │
│  │ [••••••••]     [👁]       │ │
│  └──────────────────────────┘ │
│                                │
│  ┌──────────────────────────┐ │
│  │ 🆕 新密码                 │ │
│  │ [••••••••]     [👁]       │ │
│  └──────────────────────────┘ │
│                                │
│  ┌──────────────────────────┐ │
│  │ ✅ 确认新密码             │ │
│  │ [••••••••]     [👁]       │ │
│  └──────────────────────────┘ │
│                                │
│  ┌──────────────────────────┐ │
│  │      确认修改             │ │
│  └──────────────────────────┘ │
│                                │
│  💡 密码要求：                 │
│  • 长度为 6-20 个字符          │
│  • 必须包含大小写字母和数字     │
│                                │
└────────────────────────────────┘
```

### 2. 输入框设计

**旧密码**：
- 标签：`🔑 旧密码`
- 占位符："请输入旧密码"
- 类型：`password`
- 显示/隐藏密码切换按钮：`👁` 图标

**新密码**：
- 标签：`🆕 新密码`
- 占位符："请输入新密码 (6-20位，含大小写字母和数字)"
- 类型：`password`
- 实时验证提示
- 显示/隐藏密码切换按钮

**确认新密码**：
- 标签：`✅ 确认新密码`
- 占位符："请再次输入新密码"
- 类型：`password`
- 显示/隐藏密码切换按钮

### 3. 提交按钮

- **默认状态**：蓝色背景 (`#2563EB`)，白色文字
- **禁用状态**：灰色背景 (`#D1D5DB`)，表单未填写完整或验证未通过时禁用
- **加载状态**：显示 loading 图标和"提交中..."文字
- **圆角**：`12rpx`
- **尺寸**：全宽，高度 `88rpx`

### 4. 密码强度指示器

根据新密码内容实时显示强度：
- **弱**：红色，只包含数字或字母
- **中**：橙色，包含大小写字母或数字的组合
- **强**：绿色，包含大小写字母和数字

### 5. 提示文案

**密码要求说明**：
```
💡 密码要求：
• 长度为 6-20 个字符
• 必须包含大小写字母和数字
• 新密码不能与旧密码相同
```

**错误提示**：
- "请输入旧密码"
- "请输入新密码"
- "请确认新密码"
- "新密码格式不正确"
- "两次输入的新密码不一致"
- "新密码不能与旧密码相同"
- "旧密码不正确"（后端返回）

**成功提示**：
- "密码修改成功，请重新登录"

## 三、交互设计

### 1. 实时验证

**新密码输入时**：
- 检查长度（6-20字符）
- 检查是否包含大小写字母和数字
- 显示密码强度指示器
- 显示错误提示（如果不符合要求）

**确认密码输入时**：
- 检查是否与新密码一致
- 显示错误提示（如果不一致）

**提交按钮状态**：
- 所有字段填写完整且验证通过才启用

### 2. 显示/隐藏密码

- 每个密码框右侧有眼睛图标
- 点击切换密码明文/密文显示
- 图标状态：👁（显示）/ 👁‍🗨（隐藏）

### 3. 提交流程

1. 点击"确认修改"按钮
2. 再次验证所有输入（前端验证）
3. 调用 API 提交数据
4. 显示 loading 状态
5. 成功：
   - 显示成功提示
   - 延迟 1.5 秒后自动跳转到登录页
   - 清除本地 Token 和用户信息
6. 失败：
   - 显示错误提示
   - 保持在当前页面

### 4. 返回操作

- 导航栏左侧有返回按钮
- 如果用户输入了内容：显示确认对话框"确定要放弃修改吗？"
- 如果用户未输入：直接返回

## 四、数据接口

### API 端点

```
PUT /user/password
```

### 请求数据

```json
{
  "oldPassword": "OldPass123",
  "newPassword": "NewPass456"
}
```

### 响应数据

**成功**：
```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null,
  "timestamp": 1704067200000
}
```

**失败**：
```json
{
  "code": 400,
  "message": "旧密码不正确",
  "data": null,
  "timestamp": 1704067200000
}
```

## 五、技术实现要点

### 1. 密码验证正则

```typescript
// 密码格式验证：6-20字符，包含大小写字母和数字
const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{6,20}$/

const validatePassword = (password: string): boolean => {
  return passwordRegex.test(password)
}
```

### 2. 密码强度计算

```typescript
const calculatePasswordStrength = (password: string): 'weak' | 'medium' | 'strong' => {
  if (password.length < 6) return 'weak'

  const hasLower = /[a-z]/.test(password)
  const hasUpper = /[A-Z]/.test(password)
  const hasNumber = /\d/.test(password)

  const strength = [hasLower, hasUpper, hasNumber].filter(Boolean).length

  if (strength >= 3) return 'strong'
  if (strength >= 2) return 'medium'
  return 'weak'
}
```

### 3. 表单验证状态

```typescript
interface FormErrors {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

const errors = ref<FormErrors>({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const isFormValid = computed(() => {
  return (
    formData.oldPassword &&
    formData.newPassword &&
    formData.confirmPassword &&
    !errors.value.oldPassword &&
    !errors.value.newPassword &&
    !errors.value.confirmPassword
  )
})
```

### 4. 密码显示切换

```typescript
const showOldPassword = ref(false)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)

const togglePasswordVisibility = (field: 'old' | 'new' | 'confirm') => {
  switch (field) {
    case 'old':
      showOldPassword.value = !showOldPassword.value
      break
    case 'new':
      showNewPassword.value = !showNewPassword.value
      break
    case 'confirm':
      showConfirmPassword.value = !showConfirmPassword.value
      break
  }
}
```

### 5. 修改成功后处理

```typescript
const handleSubmit = async () => {
  try {
    submitting.value = true

    await changePassword(formData.oldPassword, formData.newPassword)

    uni.showToast({
      title: '密码修改成功',
      icon: 'success'
    })

    // 延迟跳转并清除认证信息
    setTimeout(() => {
      // 清除 Token 和用户信息
      uni.removeStorageSync('token')
      uni.removeStorageSync('refreshToken')
      userStore.clearUser()

      // 跳转到登录页
      uni.reLaunch({
        url: '/pages/auth/login'
      })
    }, 1500)
  } catch (error: any) {
    uni.showToast({
      title: error.message || '修改失败',
      icon: 'none'
    })
  } finally {
    submitting.value = false
  }
}
```

## 六、样式规范

### 颜色方案

- **背景色**：`#F9FAFB`
- **卡片背景**：`#FFFFFF`
- **主色调（蓝色）**：`#2563EB`
- **错误提示（红色）**：`#EF4444`
- **成功提示（绿色）**：`#10B981`
- **文本主色**：`#1F2937`
- **文本次色**：`#6B7280`
- **边框颜色**：`#E5E7EB`
- **禁用状态**：`#D1D5DB`

### 密码强度颜色

- **弱**：`#EF4444` (红色)
- **中**：`#F59E0B` (橙色)
- **强**：`#10B981` (绿色)

### 字体大小

- **标题**：`36rpx`，加粗
- **标签**：`28rpx`
- **输入框**：`32rpx`
- **提示文字**：`24rpx`
- **按钮文字**：`32rpx`

### 间距规范

- **页面内边距**：`32rpx`
- **表单组间距**：`32rpx`
- **标签与输入框间距**：`16rpx`
- **按钮上边距**：`48rpx`
- **提示区域上边距**：`24rpx`

## 七、页面配置

### pages.json 配置

```json
{
  "path": "pages/user/change-password",
  "style": {
    "navigationBarTitleText": "修改密码",
    "navigationBarBackgroundColor": "#FFFFFF"
  }
}
```

## 八、安全注意事项

### 1. 密码安全

- ❌ 不在前端明文存储密码
- ❌ 不在日志中打印密码
- ✅ 使用 HTTPS 传输（生产环境）
- ✅ 密码输入框使用 `password` 类型

### 2. 表单验证

- ✅ 前端验证：格式、长度、一致性
- ✅ 后端验证：旧密码正确性、格式复杂度
- ✅ 防止重复提交（提交时禁用按钮）

### 3. 用户体验

- ✅ 修改成功后强制重新登录
- ✅ 清除所有本地认证信息
- ✅ 提供清晰的错误提示

## 九、测试用例

### 功能测试

- [ ] 旧密码正确，新密码符合要求，修改成功
- [ ] 旧密码错误，显示"旧密码不正确"
- [ ] 新密码格式不正确，显示格式错误提示
- [ ] 两次新密码不一致，显示不一致提示
- [ ] 新旧密码相同，显示不能相同提示
- [ ] 修改成功后自动跳转登录页
- [ ] 修改成功后 Token 已清除

### 边界测试

- [ ] 密码长度 < 6，验证失败
- [ ] 密码长度 > 20，验证失败
- [ ] 只有小写字母，验证失败
- [ ] 只有大写字母，验证失败
- [ ] 只有数字，验证失败
- [ ] 包含特殊字符，验证失败（当前规则）
- [ ] 快速点击提交按钮，不重复请求

## 十、开发检查清单

- [ ] 创建 `frontend/src/pages/user/change-password.vue` 页面组件
- [ ] 更新 `frontend/src/pages.json` 添加页面配置
- [ ] 实现三个密码输入框
- [ ] 实现密码显示/隐藏切换
- [ ] 实现实时表单验证
- [ ] 实现密码强度指示器
- [ ] 实现提交逻辑
- [ ] 实现修改成功后跳转和清除认证
- [ ] 实现返回确认提示
- [ ] 测试所有验证场景
- [ ] 提交代码并更新文档
