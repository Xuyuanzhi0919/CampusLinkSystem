# 通知中心 API 集成文档

## 📋 文档概述

**更新时间**: 2025-01-19
**模块**: 通知中心 (Notification Center)
**状态**: ✅ 已完成 API 服务层，待集成到组件

---

## 🎯 集成目标

将通知中心从 Mock 数据切换到真实后端 API，实现以下功能:

1. ✅ 实时显示未读通知数量
2. ✅ 下拉弹窗展示最新通知（最多显示 10 条）
3. ✅ 点击通知标记已读并跳转
4. ✅ 一键标记所有通知为已读
5. ✅ 查看全部通知（跳转到通知列表页）

---

## 📚 后端 API 接口清单

### 1. 获取未读通知列表

**接口**: `GET /notification/unread`
**用途**: 弹窗显示未读通知（前 10 条）

**请求参数**:
```typescript
{
  page?: number      // 页码，默认 1
  pageSize?: number  // 每页数量，建议 10
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "notificationId": 1,
        "title": "系统通知",
        "content": "您的资源"算法导论笔记"已通过审核",
        "notifyType": "SYSTEM",
        "notifyTypeName": "系统通知",
        "relatedType": "RESOURCE",
        "relatedId": 123,
        "isRead": false,
        "createdAt": "2025-01-19T10:30:00"
      }
    ],
    "total": 25,
    "page": 1,
    "pageSize": 10,
    "totalPages": 3
  }
}
```

---

### 2. 获取未读通知数

**接口**: `GET /notification/unread-count`
**用途**: 显示导航栏未读数量红点

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": 5
}
```

---

### 3. 标记单个通知已读

**接口**: `PUT /notification/{notificationId}/read`
**用途**: 点击通知项时标记已读

**URL 参数**:
- `notificationId`: 通知 ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 4. 标记所有通知已读

**接口**: `PUT /notification/read-all`
**用途**: 点击"全部已读"按钮

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 5. 获取所有通知列表（全部通知页）

**接口**: `GET /notification/my`
**用途**: 通知列表页分页查询

**请求参数**:
```typescript
{
  page?: number      // 页码，默认 1
  pageSize?: number  // 每页数量，默认 20
  type?: string      // 通知类型筛选（可选）
}
```

**响应示例**: 同接口 1，但包含已读和未读通知

---

## 🔧 前端服务层实现

### 文件: `src/services/notification.ts`

已完成的 API 封装:

```typescript
/**
 * 通知响应类型
 */
export interface NotificationResponse {
  notificationId: number
  title: string
  content: string
  notifyType: string
  notifyTypeName: string
  relatedType?: string
  relatedId?: number
  isRead: boolean
  createdAt: string
}

/**
 * 获取未读通知列表（弹窗用）
 */
export const getUnreadNotifications = (params: {
  page?: number
  pageSize?: number
} = {}) => {
  return request.get<PageResult<NotificationResponse>>('/notification/unread', params)
}

/**
 * 获取未读通知数（红点用）
 */
export const getUnreadCount = () => {
  return request.get<number>('/notification/unread-count')
}

/**
 * 标记单个通知已读
 */
export const markNotificationRead = (id: number) => {
  return request.put(`/notification/${id}/read`)
}

/**
 * 标记所有通知已读
 */
export const markAllNotificationsRead = () => {
  return request.put('/notification/read-all')
}

/**
 * 获取所有通知列表（列表页用）
 */
export const getMyNotifications = (params: {
  type?: string
  page?: number
  pageSize?: number
} = {}) => {
  return request.get<PageResult<NotificationResponse>>('/notification/my', params)
}
```

---

## 🛠️ 工具函数

### 1. 图标映射

```typescript
export const NOTIFICATION_ICONS: Record<string, string> = {
  'SYSTEM': '🔔',
  'COMMENT': '💬',
  'FAVORITE': '⭐',
  'TASK': '📋',
  'RESOURCE': '📄',
  'QUESTION': '❓',
  'ACTIVITY': '🎉',
  'DEFAULT': '📢'
}

export function getNotificationIcon(notifyType: string): string {
  return NOTIFICATION_ICONS[notifyType] || NOTIFICATION_ICONS.DEFAULT
}
```

### 2. 相对时间格式化

```typescript
export function formatRelativeTime(dateStr: string): string {
  const now = new Date()
  const date = new Date(dateStr)
  const diffMs = now.getTime() - date.getTime()
  const diffMinutes = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMinutes < 1) return '刚刚'
  if (diffMinutes < 60) return `${diffMinutes} 分钟前`
  if (diffHours < 24) return `${diffHours} 小时前`
  if (diffDays === 1) return '昨天'
  if (diffDays < 7) return `${diffDays} 天前`

  // 超过 7 天显示具体日期
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = date.getHours()
  const minute = date.getMinutes()
  return `${month}月${day}日 ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`
}
```

### 3. 跳转链接构建

```typescript
export function buildNotificationLink(notification: NotificationResponse): string | null {
  if (!notification.relatedType || !notification.relatedId) return null

  const linkMap: Record<string, string> = {
    'RESOURCE': `/pages/resource/detail?id=${notification.relatedId}`,
    'QUESTION': `/pages/question/detail?id=${notification.relatedId}`,
    'ANSWER': `/pages/question/detail?id=${notification.relatedId}`,
    'TASK': `/pages/task/detail?id=${notification.relatedId}`,
    'ACTIVITY': `/pages/club/activity-detail?id=${notification.relatedId}`,
    'COMMENT': `/pages/resource/detail?id=${notification.relatedId}`,
  }

  return linkMap[notification.relatedType] || null
}
```

---

## 📦 组件集成指南

### TopFocusBar.vue 修改

#### 1. 导入服务

```typescript
import {
  getUnreadNotifications,
  getUnreadCount,
  markNotificationRead,
  markAllNotificationsRead,
  getNotificationIcon,
  formatRelativeTime,
  buildNotificationLink,
  type NotificationResponse
} from '@/services/notification'
```

#### 2. 替换 Mock 数据

**修改前**:
```typescript
// Mock 通知数据
const mockNotifications = ref([
  {
    id: 1,
    type: '系统通知',
    icon: '🔔',
    message: '您的资源"算法导论笔记"已通过审核',
    time: '2 分钟前',
    isRead: false,
    linkUrl: '/pages/resource/detail?id=123'
  }
])

const unreadNotificationCount = computed(() =>
  mockNotifications.value.filter(n => !n.isRead).length
)
```

**修改后**:
```typescript
// 真实通知数据
const notifications = ref<NotificationResponse[]>([])
const unreadCount = ref(0)

// 转换通知数据为弹窗需要的格式
const displayNotifications = computed(() => {
  return notifications.value.map(n => ({
    id: n.notificationId,
    type: n.notifyTypeName,
    icon: getNotificationIcon(n.notifyType),
    message: n.content,
    time: formatRelativeTime(n.createdAt),
    isRead: n.isRead,
    linkUrl: buildNotificationLink(n)
  }))
})
```

#### 3. 加载通知数据

```typescript
/**
 * 加载未读通知数量
 */
const loadUnreadCount = async () => {
  try {
    const count = await getUnreadCount()
    unreadCount.value = count
  } catch (error) {
    console.error('加载未读数量失败:', error)
  }
}

/**
 * 加载未读通知列表
 */
const loadNotifications = async () => {
  try {
    const result = await getUnreadNotifications({ page: 1, pageSize: 10 })
    notifications.value = result.list
  } catch (error) {
    console.error('加载通知列表失败:', error)
    uni.showToast({ title: '加载通知失败', icon: 'none' })
  }
}

/**
 * 打开通知弹窗时加载数据
 */
const handleMessagesClick = () => {
  if (!showNotificationMenu.value) {
    // 计算位置
    if (notificationContainer.value) {
      const el = notificationContainer.value
      const rect = el.getBoundingClientRect()
      notificationPosition.value = {
        top: 76,
        left: rect.left + rect.width / 2,
      }
    }

    // 加载通知数据
    loadNotifications()

    // 显示弹窗
    requestAnimationFrame(() => {
      showNotificationMenu.value = true
    })
  } else {
    showNotificationMenu.value = false
  }
}
```

#### 4. 处理用户操作

```typescript
/**
 * 标记所有通知为已读
 */
const handleMarkAllRead = async () => {
  try {
    await markAllNotificationsRead()
    notifications.value.forEach(n => n.isRead = true)
    unreadCount.value = 0
    uni.showToast({ title: '已全部标记为已读', icon: 'success' })
  } catch (error) {
    console.error('标记已读失败:', error)
    uni.showToast({ title: '操作失败', icon: 'error' })
  }
}

/**
 * 点击通知项
 */
const handleNotificationClick = async (notification: any) => {
  try {
    // 标记为已读
    const item = notifications.value.find(n => n.notificationId === notification.id)
    if (item && !item.isRead) {
      await markNotificationRead(item.notificationId)
      item.isRead = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }

    // 跳转到对应页面
    if (notification.linkUrl) {
      uni.navigateTo({ url: notification.linkUrl })
    }
  } catch (error) {
    console.error('操作失败:', error)
  }
}
```

#### 5. 页面挂载时加载未读数

```typescript
onMounted(() => {
  checkLoginStatus()
  if (isLoggedIn.value) {
    loadUnreadCount()  // 加载未读数量
  }
})
```

---

## 🎨 NotificationDropdown.vue 数据格式对齐

弹窗组件期望的数据格式:

```typescript
interface Notification {
  id: number          // 通知 ID
  type: string        // 通知类型名称（如"系统通知"）
  icon: string        // 图标 emoji
  message: string     // 通知内容
  time: string        // 相对时间（如"2 分钟前"）
  isRead: boolean     // 是否已读
  linkUrl?: string    // 跳转链接
}
```

**数据转换示例**:
```typescript
const displayNotifications = computed(() => {
  return notifications.value.map(n => ({
    id: n.notificationId,
    type: n.notifyTypeName,
    icon: getNotificationIcon(n.notifyType),
    message: n.content,
    time: formatRelativeTime(n.createdAt),
    isRead: n.isRead,
    linkUrl: buildNotificationLink(n)
  }))
})
```

---

## 🚀 测试清单

### 功能测试

- [ ] 登录后导航栏显示正确的未读数量
- [ ] 点击通知图标加载未读通知列表
- [ ] 未读通知显示蓝色小圆点
- [ ] 点击通知项跳转到正确页面
- [ ] 点击通知后未读数量减 1
- [ ] 点击"全部已读"清空所有未读标记
- [ ] 点击"查看全部通知"跳转到通知列表页
- [ ] 空状态正确显示"暂无新通知"

### 边界测试

- [ ] 未登录时不显示未读数量红点
- [ ] 未读数量超过 99 显示"99+"
- [ ] 通知列表为空时正确显示空状态
- [ ] API 请求失败时显示友好提示
- [ ] 快速点击多个通知不会出现错误

### 性能测试

- [ ] 首次加载通知列表 < 1 秒
- [ ] 标记已读操作响应 < 500ms
- [ ] 未读数量更新及时准确

---

## 📝 后续优化建议

### 1. WebSocket 实时推送

当前实现基于轮询或手动刷新，后续可集成 WebSocket 实现实时通知推送:

```typescript
// WebSocket 连接
const ws = new WebSocket(`wss://api.campuslink.com/ws/notification?token=${token}`)

ws.onmessage = (event) => {
  const newNotification = JSON.parse(event.data)
  notifications.value.unshift(newNotification)
  unreadCount.value++

  // 显示系统通知
  uni.showToast({ title: '您有新通知', icon: 'none' })
}
```

### 2. 本地缓存优化

使用 `uni.storage` 缓存未读数量，减少 API 请求:

```typescript
// 缓存未读数量（5 分钟）
const cacheKey = 'notification_unread_count'
const cachedCount = uni.getStorageSync(cacheKey)
if (cachedCount && Date.now() - cachedCount.timestamp < 300000) {
  unreadCount.value = cachedCount.count
} else {
  loadUnreadCount()
}
```

### 3. 通知分组显示

按时间分组显示通知（今天、昨天、更早）:

```typescript
const groupedNotifications = computed(() => {
  const groups = {
    today: [],
    yesterday: [],
    earlier: []
  }

  notifications.value.forEach(n => {
    const diffDays = Math.floor((Date.now() - new Date(n.createdAt).getTime()) / 86400000)
    if (diffDays === 0) groups.today.push(n)
    else if (diffDays === 1) groups.yesterday.push(n)
    else groups.earlier.push(n)
  })

  return groups
})
```

---

## 🔗 相关文件

- API 服务层: [src/services/notification.ts](src/services/notification.ts)
- 通知弹窗组件: [src/components/NotificationDropdown.vue](src/components/NotificationDropdown.vue)
- 导航栏组件: [src/pages/home/components/TopFocusBar.vue](src/pages/home/components/TopFocusBar.vue)
- 后端接口文档: [docs/API前端对接文档.md](../docs/API前端对接文档.md)

---

**版本**: v1.0.0
**创建时间**: 2025-01-19
**最后更新**: 2025-01-19
