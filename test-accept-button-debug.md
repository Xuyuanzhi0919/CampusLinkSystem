# 采纳按钮调试指南

请在浏览器控制台执行以下操作来帮我定位问题:

## 步骤1: 检查用户信息

打开问题详情页,在浏览器控制台(F12)执行:

```javascript
// 检查当前登录用户
console.log('当前用户ID:', localStorage.getItem('campuslink_user_info'))

// 或者直接看userStore
console.log('UserStore:', uni.getStorageSync('campuslink_user_info'))
```

## 步骤2: 检查问题数据

在控制台的Elements标签中,找到Vue DevTools,查看:

```
- question.askerId 的值
- question.status 的值
- userStore.userInfo.userId 的值
```

## 步骤3: 手动计算条件

```javascript
// 在控制台执行
const question = { askerId: 你看到的askerId值, status: 你看到的status值 }
const userId = 你看到的userId值

console.log('问题提问者ID:', question.askerId)
console.log('当前用户ID:', userId)
console.log('是我的问题:', userId === question.askerId)
console.log('问题状态:', question.status)
console.log('问题未解决:', question.status !== 1)
console.log('应该显示采纳按钮:', userId === question.askerId && question.status !== 1)
```

## 预期结果

如果你是提问者且问题未解决,应该看到:
- ✅ 是我的问题: true
- ✅ 问题未解决: true
- ✅ 应该显示采纳按钮: true

## 可能的问题

1. **类型不匹配**: userId是Number,askerId是String (或相反)
2. **数据未加载**: question对象为null或undefined
3. **状态错误**: status已经是1(已解决)

请把控制台的输出结果告诉我!
