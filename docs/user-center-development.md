# 用户中心开发文档

## 📋 文档信息

| 项目 | 内容 |
|------|------|
| **文档名称** | 用户中心开发文档 |
| **版本** | v1.0.0 |
| **创建日期** | 2025-01-19 |
| **开发者** | CampusLink Team |
| **状态** | 开发中 🚧 |

---

## 🎯 开发目标

### 本次开发范围

**Phase 1: 核心功能** (当前阶段)
- [x] 用户中心主页 (`/pages/user/index.vue`)
- [ ] 编辑资料页 (`/pages/user/edit-profile.vue`)
- [ ] 积分记录页 (`/pages/user/points.vue`)
- [ ] 设置页面 (`/pages/user/settings.vue`)

**Phase 2: 扩展功能** (后续)
- [ ] 我的资源 (`/pages/user/my-resources.vue`)
- [ ] 我的任务 (`/pages/user/my-tasks.vue`)
- [ ] 我的收藏 (`/pages/user/favorites.vue`)
- [ ] 关于页面 (`/pages/user/about.vue`)
- [ ] 帮助中心 (`/pages/user/help.vue`)

---

## 📁 目录结构

```
frontend/src/pages/user/
├── index.vue                    # 用户中心主页
├── edit-profile.vue             # 编辑资料页
├── points.vue                   # 积分记录页
├── settings.vue                 # 设置页面
├── my-resources.vue             # 我的资源
├── my-tasks.vue                 # 我的任务
├── favorites.vue                # 我的收藏
├── about.vue                    # 关于页面
├── help.vue                     # 帮助中心
└── components/
    ├── UserProfileHeader.vue    # 顶部信息卡片组件
    ├── FunctionGrid.vue         # 功能网格组件
    ├── AccountActions.vue       # 账号管理组件
    ├── LevelBadge.vue           # 等级徽章组件
    ├── StatCard.vue             # 统计卡片组件
    └── PointsLogItem.vue        # 积分记录项组件
```

---

## 🔧 技术栈

### 前端框架
- **uni-app**: 3.0+
- **Vue 3**: Composition API
- **TypeScript**: 5.0+
- **Pinia**: 状态管理

### UI 组件
- 自定义组件（复用现有设计风格）
- uni-ui 补充组件

### 工具库
- **dayjs**: 日期处理
- **@/utils/formatters**: 数字格式化工具

---

## 🎨 核心组件开发

### 1. 用户中心主页 (index.vue)

#### 文件路径
```
frontend/src/pages/user/index.vue
```

#### 组件结构
```vue
<template>
  <view class="user-center-page">
    <!-- 顶部信息卡片 -->
    <UserProfileHeader
      :user-data="userData"
      :stats="userStats"
      :is-checked-in="isCheckedIn"
      @edit-profile="handleEditProfile"
      @check-in="handleCheckIn"
    />

    <!-- 功能入口网格 -->
    <FunctionGrid
      :items="functionItems"
      @item-click="handleFunctionClick"
    />

    <!-- 账号管理区 -->
    <AccountActions
      @edit-profile="handleEditProfile"
      @change-password="handleChangePassword"
      @logout="handleLogout"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import UserProfileHeader from './components/UserProfileHeader.vue'
import FunctionGrid from './components/FunctionGrid.vue'
import AccountActions from './components/AccountActions.vue'
import type { UserProfileData, UserStatsData, FunctionItem } from '@/types/user'

// Stores
const userStore = useUserStore()

// 页面状态
const loading = ref(true)
const userData = ref<UserProfileData | null>(null)
const userStats = ref<UserStatsData | null>(null)
const isCheckedIn = ref(false)

// 功能项配置
const functionItems: FunctionItem[] = [
  {
    id: 'my-resources',
    icon: '📚',
    label: '我的资源',
    path: '/pages/user/my-resources',
    gradient: 'linear-gradient(135deg, #E53935 0%, #FF6B6B 100%)',
    requiredAuth: true
  },
  {
    id: 'my-questions',
    icon: '💬',
    label: '我的问答',
    path: '/pages/question/my',
    gradient: 'linear-gradient(135deg, #1E5FFF 0%, #60A5FA 100%)',
    requiredAuth: true
  },
  {
    id: 'my-tasks',
    icon: '📋',
    label: '我的任务',
    path: '/pages/user/my-tasks',
    gradient: 'linear-gradient(135deg, #48BB78 0%, #81E6D9 100%)',
    requiredAuth: true
  },
  {
    id: 'my-favorites',
    icon: '⭐',
    label: '我的收藏',
    path: '/pages/user/favorites',
    gradient: 'linear-gradient(135deg, #FFD700 0%, #FFF4A3 100%)',
    requiredAuth: true
  },
  {
    id: 'points-history',
    icon: '🎁',
    label: '积分记录',
    path: '/pages/user/points',
    gradient: 'linear-gradient(135deg, #FF6B35 0%, #FF8C42 100%)',
    requiredAuth: true
  },
  {
    id: 'settings',
    icon: '⚙️',
    label: '设置',
    path: '/pages/user/settings',
    gradient: 'linear-gradient(135deg, #64748B 0%, #94A3B8 100%)',
    requiredAuth: false
  },
  {
    id: 'about',
    icon: '📖',
    label: '关于',
    path: '/pages/user/about',
    gradient: 'linear-gradient(135deg, #7C3AED 0%, #A78BFA 100%)',
    requiredAuth: false
  },
  {
    id: 'help',
    icon: '💡',
    label: '帮助中心',
    path: '/pages/user/help',
    gradient: 'linear-gradient(135deg, #0EA5E9 0%, #7DD3FC 100%)',
    requiredAuth: false
  }
]

// 生命周期
onLoad(() => {
  loadUserData()
})

onShow(() => {
  refreshData()
})

onPullDownRefresh(() => {
  refreshData()
  uni.stopPullDownRefresh()
})

// 加载用户数据
const loadUserData = async () => {
  try {
    loading.value = true

    // 并行请求
    const [profile, stats, checkInStatus] = await Promise.all([
      userStore.getUserProfile(),
      userStore.getUserStats(),
      userStore.getCheckInStatus()
    ])

    userData.value = profile
    userStats.value = stats
    isCheckedIn.value = checkInStatus
  } catch (error: any) {
    console.error('加载用户数据失败:', error)
    uni.showToast({
      title: error.message || '加载失败',
      icon: 'none'
    })
  } finally {
    loading.value = false
  }
}

// 刷新数据
const refreshData = async () => {
  try {
    const [stats, checkInStatus] = await Promise.all([
      userStore.getUserStats(),
      userStore.getCheckInStatus()
    ])

    userStats.value = stats
    isCheckedIn.value = checkInStatus
  } catch (error: any) {
    console.error('刷新数据失败:', error)
  }
}

// 编辑资料
const handleEditProfile = () => {
  uni.navigateTo({
    url: '/pages/user/edit-profile'
  })
}

// 签到
const handleCheckIn = async () => {
  try {
    const result = await userStore.checkIn()

    // 显示签到成功动画
    uni.showToast({
      title: `签到成功！+${result.points}积分`,
      icon: 'success'
    })

    // 更新签到状态
    isCheckedIn.value = true

    // 刷新用户数据
    refreshData()
  } catch (error: any) {
    uni.showToast({
      title: error.message || '签到失败',
      icon: 'none'
    })
  }
}

// 功能项点击
const handleFunctionClick = (item: FunctionItem) => {
  // 检查是否需要登录
  if (item.requiredAuth && !userStore.isLoggedIn) {
    uni.showModal({
      title: '提示',
      content: '请先登录',
      success: (res) => {
        if (res.confirm) {
          // 触发登录
          userStore.showLoginModal()
        }
      }
    })
    return
  }

  // 跳转页面
  uni.navigateTo({
    url: item.path
  })
}

// 修改密码
const handleChangePassword = () => {
  uni.navigateTo({
    url: '/pages/user/change-password'
  })
}

// 退出登录
const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
        uni.showToast({
          title: '已退出登录',
          icon: 'success'
        })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.user-center-page {
  min-height: 100vh;
  background: #F9FBFF;
  padding-bottom: 32rpx;
}
</style>
```

### 2. 顶部信息卡片组件 (UserProfileHeader.vue)

#### 组件功能
- 展示用户基本信息（头像、昵称、学号、专业）
- 展示等级徽章
- 展示统计数据（积分、等级、资源数、问答数）
- 提供编辑资料和签到按钮

#### 代码实现

```vue
<template>
  <view class="profile-header">
    <!-- 背景装饰层 -->
    <view class="bg-decoration">
      <view class="bg-circle bg-circle-1"></view>
      <view class="bg-circle bg-circle-2"></view>
    </view>

    <!-- 用户信息区 -->
    <view class="user-info">
      <!-- 头像 -->
      <image
        v-if="userData?.avatarUrl"
        class="avatar"
        :src="userData.avatarUrl"
        mode="aspectFill"
      />
      <view v-else class="avatar avatar-default">
        <text class="avatar-text">{{ displayName }}</text>
      </view>

      <!-- 基本信息 -->
      <view class="info-text">
        <view class="nickname">
          <text>{{ userData?.nickname || '未登录' }}</text>
          <LevelBadge :level="userData?.level || 1" />
        </view>
        <text v-if="userData?.studentId" class="student-id">
          学号：{{ userData.studentId }}
        </text>
        <text v-if="userData?.major" class="major">
          {{ userData.major }}
        </text>
      </view>
    </view>

    <!-- 统计数据网格 -->
    <view class="stats-grid">
      <view class="stat-item">
        <text class="stat-value">{{ userData?.points || 0 }}</text>
        <text class="stat-label">积分</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">LV.{{ userData?.level || 1 }}</text>
        <text class="stat-label">等级</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ stats?.resourceCount || 0 }}</text>
        <text class="stat-label">资源</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ stats?.questionCount || 0 }}</text>
        <text class="stat-label">问答</text>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons">
      <view class="action-btn" @click="handleEditProfile">
        <text class="btn-icon">✏️</text>
        <text class="btn-text">编辑资料</text>
      </view>
      <view
        class="action-btn"
        :class="{ 'checked-in': isCheckedIn }"
        @click="handleCheckIn"
      >
        <text class="btn-icon">{{ isCheckedIn ? '✅' : '📅' }}</text>
        <text class="btn-text">{{ isCheckedIn ? '已签到' : '签到' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import LevelBadge from './LevelBadge.vue'
import type { UserProfileData, UserStatsData } from '@/types/user'

interface Props {
  userData: UserProfileData | null
  stats: UserStatsData | null
  isCheckedIn: boolean
}

const props = defineProps<Props>()
const emit = defineEmits(['edit-profile', 'check-in'])

// 显示名称首字母
const displayName = computed(() => {
  if (props.userData?.nickname) {
    return props.userData.nickname.charAt(0).toUpperCase()
  }
  return 'U'
})

const handleEditProfile = () => {
  emit('edit-profile')
}

const handleCheckIn = () => {
  if (!props.isCheckedIn) {
    emit('check-in')
  }
}
</script>

<style lang="scss" scoped>
.profile-header {
  position: relative;
  padding: 48rpx 32rpx;
  background: linear-gradient(135deg, #667EEA 0%, #764BA2 100%);
  border-radius: 0 0 32rpx 32rpx;
  overflow: hidden;

  // 背景装饰
  .bg-decoration {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    opacity: 0.1;

    .bg-circle {
      position: absolute;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.2);

      &.bg-circle-1 {
        width: 200rpx;
        height: 200rpx;
        top: -50rpx;
        right: -50rpx;
      }

      &.bg-circle-2 {
        width: 150rpx;
        height: 150rpx;
        bottom: -30rpx;
        left: -30rpx;
      }
    }
  }

  // 用户信息区
  .user-info {
    position: relative;
    z-index: 1;
    display: flex;
    align-items: center;
    margin-bottom: 32rpx;

    .avatar {
      width: 120rpx;
      height: 120rpx;
      border-radius: 50%;
      border: 4rpx solid rgba(255, 255, 255, 0.3);
      box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.2);

      &.avatar-default {
        display: flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(135deg, #3B82F6 0%, #60A5FA 100%);

        .avatar-text {
          font-size: 48rpx;
          font-weight: 600;
          color: #FFFFFF;
        }
      }
    }

    .info-text {
      margin-left: 24rpx;
      color: #FFFFFF;

      .nickname {
        font-size: 48rpx;
        font-weight: 600;
        display: flex;
        align-items: center;
        gap: 12rpx;
        margin-bottom: 8rpx;
      }

      .student-id,
      .major {
        font-size: 26rpx;
        opacity: 0.9;
        margin-top: 4rpx;
      }
    }
  }

  // 统计数据网格
  .stats-grid {
    position: relative;
    z-index: 1;
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16rpx;
    margin-bottom: 24rpx;

    .stat-item {
      text-align: center;
      padding: 16rpx;
      background: rgba(255, 255, 255, 0.15);
      border-radius: 16rpx;
      backdrop-filter: blur(10rpx);

      .stat-value {
        display: block;
        font-size: 36rpx;
        font-weight: 600;
        color: #FFFFFF;
      }

      .stat-label {
        display: block;
        font-size: 24rpx;
        color: rgba(255, 255, 255, 0.9);
        margin-top: 4rpx;
      }
    }
  }

  // 操作按钮
  .action-buttons {
    position: relative;
    z-index: 1;
    display: flex;
    gap: 16rpx;
    justify-content: center;

    .action-btn {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8rpx;
      padding: 16rpx 24rpx;
      background: rgba(255, 255, 255, 0.2);
      border: 1rpx solid rgba(255, 255, 255, 0.3);
      border-radius: 48rpx;
      backdrop-filter: blur(10rpx);
      transition: all 0.3s;

      &:active {
        transform: scale(0.95);
        background: rgba(255, 255, 255, 0.3);
      }

      &.checked-in {
        opacity: 0.6;
        cursor: not-allowed;
      }

      .btn-icon {
        font-size: 32rpx;
      }

      .btn-text {
        font-size: 28rpx;
        color: #FFFFFF;
      }
    }
  }
}
</style>
```

### 3. 功能网格组件 (FunctionGrid.vue)

#### 组件代码

```vue
<template>
  <view class="function-grid">
    <view class="grid-title">功能中心</view>

    <view class="grid-container">
      <view
        v-for="item in items"
        :key="item.id"
        class="function-item"
        @click="handleClick(item)"
      >
        <view class="icon-wrapper" :style="{ background: item.gradient }">
          <text class="icon">{{ item.icon }}</text>

          <!-- 角标 -->
          <view v-if="item.badge && item.badge > 0" class="badge">
            {{ item.badge > 99 ? '99+' : item.badge }}
          </view>
        </view>

        <text class="label">{{ item.label }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import type { FunctionItem } from '@/types/user'

interface Props {
  items: FunctionItem[]
}

const props = defineProps<Props>()
const emit = defineEmits(['item-click'])

const handleClick = (item: FunctionItem) => {
  emit('item-click', item)
}
</script>

<style lang="scss" scoped>
.function-grid {
  padding: 32rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  margin: 24rpx 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);

  .grid-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #333333;
    margin-bottom: 24rpx;
  }

  .grid-container {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 24rpx;

    // 移动端改为2列
    @media (max-width: 750rpx) {
      grid-template-columns: repeat(2, 1fr);
    }
  }

  .function-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 24rpx 16rpx;
    background: #F8FAFC;
    border-radius: 16rpx;
    transition: all 0.3s;

    &:active {
      transform: scale(0.95);
    }

    .icon-wrapper {
      position: relative;
      width: 96rpx;
      height: 96rpx;
      border-radius: 24rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 16rpx;

      .icon {
        font-size: 48rpx;
      }

      .badge {
        position: absolute;
        top: -8rpx;
        right: -8rpx;
        min-width: 32rpx;
        height: 32rpx;
        padding: 0 8rpx;
        background: #FF4D4F;
        color: #FFFFFF;
        font-size: 20rpx;
        border-radius: 16rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 2rpx 8rpx rgba(255, 77, 79, 0.4);
      }
    }

    .label {
      font-size: 26rpx;
      color: #666666;
      text-align: center;
    }
  }
}
</style>
```

### 4. 等级徽章组件 (LevelBadge.vue)

```vue
<template>
  <view class="level-badge" :class="`level-${levelTier}`">
    <text class="badge-text">{{ levelIcon }} LV.{{ level }}</text>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  level: number
}

const props = defineProps<Props>()

// 等级分层
const levelTier = computed(() => {
  if (props.level <= 2) return 'bronze'
  if (props.level <= 4) return 'silver'
  if (props.level <= 6) return 'gold'
  if (props.level <= 8) return 'diamond'
  return 'master'
})

// 等级图标
const levelIcon = computed(() => {
  const icons: Record<string, string> = {
    bronze: '🥉',
    silver: '🥈',
    gold: '🥇',
    diamond: '💎',
    master: '👑'
  }
  return icons[levelTier.value] || '🥉'
})
</script>

<style lang="scss" scoped>
.level-badge {
  display: inline-flex;
  align-items: center;
  padding: 4rpx 12rpx;
  border-radius: 16rpx;
  font-size: 24rpx;
  font-weight: 600;

  &.level-bronze {
    background: linear-gradient(135deg, #CD7F32 0%, #E8A87C 100%);
    color: #FFFFFF;
  }

  &.level-silver {
    background: linear-gradient(135deg, #C0C0C0 0%, #E8E8E8 100%);
    color: #666666;
  }

  &.level-gold {
    background: linear-gradient(135deg, #FFD700 0%, #FFF4A3 100%);
    color: #B8860B;
  }

  &.level-diamond {
    background: linear-gradient(135deg, #4FC3F7 0%, #B3E5FC 100%);
    color: #0277BD;
  }

  &.level-master {
    background: linear-gradient(135deg, #9C27B0 0%, #E1BEE7 100%);
    color: #FFFFFF;
  }
}
</style>
```

---

## 📡 API 服务封装

### 用户服务 (services/user.ts)

```typescript
import request from '@/utils/request'
import type {
  UserProfileData,
  UserStatsData,
  UpdateProfileRequest,
  CheckInResponse,
  PointsLogItem,
  PageResult
} from '@/types/user'

/**
 * 获取当前用户资料
 */
export const getUserProfile = () => {
  return request<UserProfileData>({
    url: '/user/profile',
    method: 'GET'
  })
}

/**
 * 更新用户资料
 */
export const updateUserProfile = (data: UpdateProfileRequest) => {
  return request<UserProfileData>({
    url: '/user/profile',
    method: 'PUT',
    data
  })
}

/**
 * 获取用户统计数据
 */
export const getUserStats = () => {
  return request<UserStatsData>({
    url: '/user/stats',
    method: 'GET'
  })
}

/**
 * 获取积分记录
 */
export const getPointsLog = (page: number = 1, pageSize: number = 20) => {
  return request<PageResult<PointsLogItem>>({
    url: '/user/points/log',
    method: 'GET',
    params: { page, pageSize }
  })
}

/**
 * 每日签到
 */
export const checkIn = () => {
  return request<CheckInResponse>({
    url: '/user/check-in',
    method: 'POST'
  })
}

/**
 * 获取签到状态
 */
export const getCheckInStatus = () => {
  return request<boolean>({
    url: '/user/check-in/status',
    method: 'GET'
  })
}

/**
 * 修改密码
 */
export const changePassword = (oldPassword: string, newPassword: string) => {
  return request({
    url: '/user/password',
    method: 'PUT',
    data: { oldPassword, newPassword }
  })
}
```

---

## 🗂️ 类型定义

### types/user.ts

```typescript
/**
 * 用户资料数据
 */
export interface UserProfileData {
  uId: number
  username: string
  nickname: string
  email: string | null
  phone: string | null
  avatarUrl: string | null
  role: string
  studentId: string | null
  schoolId: number | null
  schoolName: string | null
  major: string | null
  grade: number | null
  points: number
  level: number
  createdAt: string
}

/**
 * 用户统计数据
 */
export interface UserStatsData {
  resourceCount: number
  downloadCount: number
  questionCount: number
  answerCount: number
  acceptedAnswerCount: number
  taskPublishCount: number
  taskCompleteCount: number
  favoriteCount: number
  likeCount: number
  checkInDays: number
}

/**
 * 更新资料请求
 */
export interface UpdateProfileRequest {
  nickname?: string
  email?: string
  phone?: string
  avatarUrl?: string
  major?: string
  grade?: number
}

/**
 * 签到响应
 */
export interface CheckInResponse {
  points: number
  continuousDays: number
  totalCheckInDays: number
  isCheckedInToday: boolean
}

/**
 * 积分记录项
 */
export interface PointsLogItem {
  id: number
  userId: number
  points: number
  type: string
  description: string
  createdAt: string
}

/**
 * 功能项配置
 */
export interface FunctionItem {
  id: string
  icon: string
  label: string
  path: string
  description?: string
  badge?: number
  iconColor?: string
  gradient?: string
  requiredAuth?: boolean
}

/**
 * 分页结果
 */
export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
}
```

---

## 📝 pages.json 配置

```json
{
  "pages": [
    // ... 其他页面

    // 用户中心相关页面
    {
      "path": "pages/user/index",
      "style": {
        "navigationBarTitleText": "个人中心",
        "navigationBarBackgroundColor": "#667EEA",
        "navigationBarTextStyle": "white",
        "enablePullDownRefresh": true,
        "backgroundTextStyle": "light"
      }
    },
    {
      "path": "pages/user/edit-profile",
      "style": {
        "navigationBarTitleText": "编辑资料",
        "navigationStyle": "custom"
      }
    },
    {
      "path": "pages/user/points",
      "style": {
        "navigationBarTitleText": "积分记录",
        "navigationBarBackgroundColor": "#FFFFFF",
        "enablePullDownRefresh": true
      }
    },
    {
      "path": "pages/user/settings",
      "style": {
        "navigationBarTitleText": "设置",
        "navigationBarBackgroundColor": "#FFFFFF"
      }
    }
  ]
}
```

---

## 🚀 开发流程

### Phase 1: 基础页面开发

**步骤 1: 创建目录结构**
```bash
mkdir -p frontend/src/pages/user/components
touch frontend/src/pages/user/index.vue
touch frontend/src/pages/user/components/UserProfileHeader.vue
touch frontend/src/pages/user/components/FunctionGrid.vue
touch frontend/src/pages/user/components/LevelBadge.vue
```

**步骤 2: 创建类型定义**
```bash
touch frontend/src/types/user.ts
```

**步骤 3: 封装API服务**
```bash
# 在 frontend/src/services/user.ts 中添加用户相关API
```

**步骤 4: 实现主页面**
```bash
# 实现 frontend/src/pages/user/index.vue
```

**步骤 5: 实现子组件**
```bash
# 实现 UserProfileHeader.vue
# 实现 FunctionGrid.vue
# 实现 LevelBadge.vue
```

**步骤 6: 配置路由**
```bash
# 在 pages.json 中添加路由配置
```

**步骤 7: 测试验证**
```bash
cd frontend
npm run dev:h5
```

### Phase 2: 扩展功能开发

- 编辑资料页
- 积分记录页
- 设置页面
- 其他子页面

---

## ✅ 开发检查清单

### 代码质量
- [ ] TypeScript 类型完整
- [ ] ESLint 无警告
- [ ] 代码注释清晰
- [ ] 组件可复用

### 功能完整性
- [ ] 用户信息正确显示
- [ ] 统计数据准确
- [ ] 签到功能正常
- [ ] 功能跳转正确
- [ ] 退出登录正常

### 用户体验
- [ ] 加载状态友好
- [ ] 错误提示明确
- [ ] 操作反馈及时
- [ ] 动画流畅自然

### 响应式设计
- [ ] 移动端布局正常
- [ ] 平板端布局正常
- [ ] 桌面端布局正常
- [ ] 字体大小合适

### 性能优化
- [ ] 图片懒加载
- [ ] 数据缓存
- [ ] 防抖节流
- [ ] 列表虚拟滚动

---

## 🐛 常见问题

### Q1: 头像上传失败？
**解决方案**: 检查 OSS 配置是否正确，确认有上传权限。

### Q2: 签到重复提示？
**解决方案**: 检查签到状态缓存，确保状态同步。

### Q3: 统计数据不一致？
**解决方案**: 后端添加事务保证数据一致性。

### Q4: 页面加载慢？
**解决方案**:
- 使用骨架屏
- 数据预加载
- 图片压缩

---

## 📚 参考资料

- [uni-app 官方文档](https://uniapp.dcloud.net.cn/)
- [Vue 3 文档](https://cn.vuejs.org/)
- [TypeScript 文档](https://www.typescriptlang.org/)
- [Pinia 文档](https://pinia.vuejs.org/)

---

**文档结束**

*下一步: 开始实现用户中心主页面*
