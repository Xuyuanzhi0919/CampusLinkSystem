<template>
  <view class="top-focus-bar">
    <!-- 背景装饰层 -->
    <view class="bg-decoration-layer">
      <!-- 装饰圆形 -->
      <view class="bg-circle bg-circle-1"></view>
      <view class="bg-circle bg-circle-2"></view>
      <view class="bg-wave"></view>
    </view>

    <!-- 内容容器 -->
    <view class="focus-container">
      <!-- 第一行：顶部导航栏 -->
      <view class="top-nav-bar">
        <!-- 左侧：品牌标识 -->
        <view class="brand-section">
          <text class="brand-logo">CampusLink</text>
          <!-- 桌面端显示完整副标题，平板端简化，移动端隐藏 -->
          <text class="brand-slogan brand-slogan-full">100万大学生互助学习圈</text>
          <text class="brand-slogan brand-slogan-short">大学生互助圈</text>
        </view>

        <!-- 右侧：操作按钮 -->
        <view class="action-buttons">
          <!-- 登录/注册按钮（未登录时在桌面端显示）-->
          <view
            v-if="showLoginButton"
            class="action-btn action-btn-secondary desktop-only"
            @click="handleLoginRegister"
          >
            <text class="action-btn-text">登录 / 注册</text>
          </view>

          <!-- 用户头像按钮（登录后显示）-->
          <view
            v-if="showAvatarButton"
            class="avatar-wrapper"
          >
            <UserAvatar
              :avatar-url="userInfo.avatar"
              :nickname="userInfo.nickname"
              :is-active="showUserMenu"
              @click="handleAvatarClick"
            />
          </view>

          <!-- 发布按钮（主操作）-->
          <view class="action-btn action-btn-primary" @click="handlePublish">
            <text class="action-btn-icon">✨</text>
            <text class="action-btn-text desktop-only">发布</text>
          </view>
        </view>

        <!-- 移动端：汉堡菜单图标 (已废弃) -->
        <!-- <view class="mobile-menu-icon mobile-only" @click="toggleMobileMenu"> ... </view> -->

        <!-- 用户下拉菜单 -->
        <UserDropdownMenu
          :visible="showUserMenu"
          :user-info="userInfo"
          :is-checked-in="isCheckedIn"
          @update:visible="showUserMenu = $event"
          @menu-click="handleMenuClick"
          @check-in="handleCheckIn"
        />
      </view>

      <!-- 第二行：主内容区（搜索框居中）-->
      <view class="main-content-area">
        <!-- 搜索栏 -->
        <view class="search-section">
          <!-- 搜索框容器 -->
          <view class="search-box-wrapper">
            <view class="search-box">
              <!-- 搜索图标 -->
              <text class="search-icon">🔍</text>
              <!-- 搜索输入框 -->
              <input
                class="search-input"
                type="text"
                v-model="searchKeyword"
                placeholder="搜索课程资料 / 问题 / 活动"
                @confirm="handleSearch"
                @focus="showHotTags = true"
                @blur="handleBlur"
              />
              <!-- 热门搜索标签 -->
              <view v-if="showHotTags && !searchKeyword" class="hot-tags">
                <text
                  v-for="tag in hotTags"
                  :key="tag"
                  class="hot-tag"
                  @click="handleTagClick(tag)"
                >
                  {{ tag }}
                </text>
              </view>
              <!-- 语音搜索按钮 -->
              <view
                class="voice-search-btn-inline"
                :class="{ 'voice-active': isVoiceActive }"
                @click="handleVoiceSearch"
                @touchstart="handleVoiceTouchStart"
                @touchend="handleVoiceTouchEnd"
              >
                <svg
                  class="voice-icon-inline"
                  viewBox="0 0 24 24"
                  fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <!-- 麦克风图标 -->
                  <path
                    d="M12 14C13.66 14 15 12.66 15 11V5C15 3.34 13.66 2 12 2C10.34 2 9 3.34 9 5V11C9 12.66 10.34 14 12 14Z"
                    fill="currentColor"
                  />
                  <path
                    d="M17 11C17 13.76 14.76 16 12 16C9.24 16 7 13.76 7 11H5C5 14.53 7.61 17.43 11 17.92V21H13V17.92C16.39 17.43 19 14.53 19 11H17Z"
                    fill="currentColor"
                  />
                </svg>
                <!-- 语音波纹效果 -->
                <view v-if="isVoiceActive" class="voice-ripple-inline"></view>
                <view v-if="isVoiceActive" class="voice-ripple-inline voice-ripple-2"></view>
              </view>
              <!-- 搜索按钮 -->
              <view class="search-btn-primary" @click="handleSearch">
                <text class="search-btn-text">搜索</text>
              </view>
            </view>
            <!-- 搜索框下方提示文字（桌面端和平板端显示）-->
            <view class="search-tips desktop-tablet-only">
              <text class="search-tip-item">超500万大学资源</text>
              <text class="search-tip-divider">·</text>
              <text class="search-tip-item">24小时AI问答</text>
              <text class="search-tip-divider">·</text>
              <text class="search-tip-item">校迅捷答即时配</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 插画元素层（保留但降低透明度）-->
    <view class="illustration-layer">
      <!-- 左侧校园建筑图标（降低饱和度 10-15%）-->
      <view class="campus-building">
        <CampusBuilding :width="120" :height="160" color="#FF9933" />
      </view>

      <!-- 右侧主体插画 - 手持手机的学生 -->
      <view class="student-illustration">
        <StudentWithPhone :width="300" :height="300" />
      </view>

      <!-- 装饰元素（降低饱和度 10-15%）-->
      <view class="decoration-element decoration-book">
        <DecorativeElements type="book" color="#5CADFF" :width="100" :height="100" />
      </view>
      <view class="decoration-element decoration-bulb">
        <DecorativeElements type="bulb" color="#FF9933" :width="100" :height="100" />
      </view>
      <view class="decoration-element decoration-pencil">
        <DecorativeElements type="pencil" color="#6FD147" :width="80" :height="80" />
      </view>
      <view class="decoration-element decoration-cap">
        <DecorativeElements type="graduation-cap" color="#5CADFF" :width="90" :height="90" />
      </view>
    </view>

    <!-- 积分动画 -->
    <PointsAnimation
      :points="pointsValue"
      :visible="showPointsAnimation"
      :position="pointsPosition"
      @complete="showPointsAnimation = false"
    />

    <!-- 移动端折叠菜单 -->
    <view v-if="showMobileMenu" class="mobile-menu-overlay" @click="closeMobileMenu">
      <view class="mobile-menu-panel" @click.stop>
        <!-- 菜单头部 - 优化: 品牌化设计 -->
        <view class="mobile-menu-header">
          <view class="mobile-menu-brand">
            <text class="mobile-menu-logo">CampusLink</text>
            <text class="mobile-menu-subtitle">个人中心</text>
          </view>
          <view class="mobile-menu-close" @click="closeMobileMenu">
            <text class="close-icon">×</text>
          </view>
        </view>

        <!-- 菜单内容 -->
        <view class="mobile-menu-content">
          <!-- 未登录：显示登录按钮 -->
          <view v-if="!isLoggedIn" class="mobile-menu-item" @click="handleMobileLogin">
            <text class="mobile-menu-item-icon">👤</text>
            <text class="mobile-menu-item-text">登录 / 注册</text>
          </view>

          <!-- 已登录：显示用户信息和功能 -->
          <template v-else>
            <!-- 用户信息卡片 -->
            <view class="mobile-user-card" @click="handleMobileProfile">
              <image
                v-if="userInfo.avatar"
                class="mobile-user-avatar"
                :src="userInfo.avatar"
                mode="aspectFill"
              />
              <view v-else class="mobile-user-avatar mobile-user-avatar-default">
                <text class="avatar-text">{{ userInfo.nickname ? userInfo.nickname[0] : '用' }}</text>
              </view>
              <view class="mobile-user-info">
                <text class="mobile-user-name">{{ userInfo.nickname || '用户' }}</text>
                <text class="mobile-user-email">{{ userInfo.email || '未设置邮箱' }}</text>
              </view>
            </view>

            <!-- 分组标题: 常用功能 -->
            <view class="mobile-menu-section-title">常用功能</view>

            <!-- 签到 -->
            <view class="mobile-menu-item menu-item-checkin" @click="handleCheckIn">
              <text class="mobile-menu-item-icon icon-checkin">{{ isCheckedIn ? '✨' : '📅' }}</text>
              <text class="mobile-menu-item-text">{{ isCheckedIn ? '今日已签到' : '每日签到' }}</text>
              <text v-if="!isCheckedIn" class="mobile-menu-item-badge">+10积分</text>
              <text v-if="isCheckedIn" class="mobile-menu-item-badge checked">已完成</text>
            </view>

            <!-- 发布 -->
            <view class="mobile-menu-item menu-item-publish" @click="handleMobilePublish">
              <text class="mobile-menu-item-icon icon-publish">✨</text>
              <text class="mobile-menu-item-text">我的发布</text>
            </view>

            <!-- 我的收藏 -->
            <view class="mobile-menu-item menu-item-favorites" @click="handleMobileFavorites">
              <text class="mobile-menu-item-icon icon-favorites">⭐</text>
              <text class="mobile-menu-item-text">我的收藏</text>
            </view>

            <!-- 分组分割线 -->
            <view class="mobile-menu-divider"></view>

            <!-- 分组标题: 系统管理 -->
            <view class="mobile-menu-section-title">系统管理</view>

            <!-- 设置 -->
            <view class="mobile-menu-item menu-item-settings" @click="handleMobileSettings">
              <text class="mobile-menu-item-icon icon-settings">⚙️</text>
              <text class="mobile-menu-item-text">设置</text>
            </view>

            <!-- 退出登录 -->
            <view class="mobile-menu-item mobile-menu-item-danger" @click="handleMobileLogout">
              <text class="mobile-menu-item-icon icon-logout">🚪</text>
              <text class="mobile-menu-item-text">退出登录</text>
            </view>
          </template>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import CampusBuilding from '@/components/illustrations/CampusBuilding.vue'
import StudentWithPhone from '@/components/illustrations/StudentWithPhone.vue'
import DecorativeElements from '@/components/illustrations/DecorativeElements.vue'
import UserAvatar from '@/components/UserAvatar.vue'
import UserDropdownMenu from '@/components/UserDropdownMenu.vue'
import PointsAnimation from '@/components/PointsAnimation.vue'
import config from '@/config'
import { checkIn, getCheckInStatus } from '@/services/user'

// Props & Emits
const emit = defineEmits<{
  search: [keyword: string]
  upload: []
  aiAnswer: []
  login: []
}>()

// 数据
const searchKeyword = ref('')
const showHotTags = ref(false)
const hotTags = ref(['高数课件', '四六级真题', '数据结构', '考研资料'])
const isVoiceActive = ref(false) // 语音搜索激活状态

// 用户登录状态
const isLoggedIn = ref(false)
const showUserMenu = ref(false)
const userInfo = ref({
  userId: null as number | null,
  nickname: '',
  email: '',
  phone: '',
  avatar: ''
})

// 按钮淡入淡出状态
const showLoginButton = ref(true)
const showAvatarButton = ref(false)

// 签到状态
const isCheckedIn = ref(false)

// 积分动画状态
const showPointsAnimation = ref(false)
const pointsValue = ref(0)
const pointsPosition = ref({ x: 0, y: 0 })

// 移动端菜单状态
const showMobileMenu = ref(false)

// 检查登录状态
const checkLoginStatus = () => {
  const token = uni.getStorageSync(config.tokenKey)
  const userInfoStr = uni.getStorageSync(config.userInfoKey)

  if (token && userInfoStr) {
    try {
      const parsedUserInfo = JSON.parse(userInfoStr)
      userInfo.value = {
        userId: parsedUserInfo.uId || parsedUserInfo.userId || parsedUserInfo.id || null,
        nickname: parsedUserInfo.nickname || '用户',
        email: parsedUserInfo.email || '',
        phone: parsedUserInfo.phone || '',
        avatar: parsedUserInfo.avatar || ''
      }
      isLoggedIn.value = true

      // 从后端同步签到状态
      syncCheckInStatus()

      // 动画：登录按钮淡出，头像按钮淡入
      showLoginButton.value = false
      setTimeout(() => {
        showAvatarButton.value = true
      }, 200)

      // 🎯 企业级事件总线：触发全局登录事件，通知所有监听组件刷新数据
      uni.$emit('user-login', { userInfo: userInfo.value })
    } catch (error) {
      console.error('解析用户信息失败:', error)
      isLoggedIn.value = false
    }
  } else {
    isLoggedIn.value = false
    showLoginButton.value = true
    showAvatarButton.value = false
  }
}

// 从后端同步签到状态
const syncCheckInStatus = async () => {
  try {
    const isCheckedInToday = await getCheckInStatus()
    isCheckedIn.value = isCheckedInToday

    // 同时更新本地存储，保持一致性
    const userId = userInfo.value.userId
    if (userId) {
      const storageKey = `lastCheckInDate_${userId}`
      const today = new Date().toDateString()

      if (isCheckedInToday) {
        uni.setStorageSync(storageKey, today)
      } else {
        uni.removeStorageSync(storageKey)
      }
    }
  } catch (error) {
    console.error('同步签到状态失败:', error)
    // 如果同步失败，回退到本地检查
    checkTodayCheckIn()
  }
}

// 检查今日是否已签到（本地检查，作为备用方案）
const checkTodayCheckIn = () => {
  // 使用用户专属的存储键
  const userId = userInfo.value.userId
  if (!userId) {
    isCheckedIn.value = false
    return
  }

  const storageKey = `lastCheckInDate_${userId}`
  const lastCheckInDate = uni.getStorageSync(storageKey)
  const today = new Date().toDateString()
  isCheckedIn.value = lastCheckInDate === today
}

// 签到处理
const handleCheckIn = async (event: any) => {
  if (isCheckedIn.value) {
    uni.showToast({ title: '今日已签到', icon: 'none' })
    return
  }

  try {
    // 调用后端签到API
    const response = await checkIn()

    if (response.success) {
      // 签到成功 - 使用用户专属的存储键
      const userId = userInfo.value.userId
      if (userId) {
        const storageKey = `lastCheckInDate_${userId}`
        const today = new Date().toDateString()
        uni.setStorageSync(storageKey, today)
      }
      isCheckedIn.value = true

      // 显示签到成功提示（精美动画，已包含积分信息）
      showCheckInSuccess(response.pointsEarned || 10, response.consecutiveDays || 1)

      console.log('签到成功:', response)
    } else {
      // 今日已签到
      uni.showToast({ title: response.message || '今日已签到', icon: 'none' })
    }
  } catch (error) {
    console.error('签到失败:', error)
    uni.showToast({ title: '签到失败，请稍后重试', icon: 'none' })
  }
}

// 显示签到成功提示
const showCheckInSuccess = (points: number = 10, consecutiveDays: number = 1) => {
  // #ifdef H5
  const toastDiv = document.createElement('div')

  // 连续签到天数提示
  const streakText = consecutiveDays > 1
    ? `<span style="
        font-size: 13px;
        font-weight: 500;
        color: #F59E0B;
        line-height: 1.4;
        margin-top: 4px;
      ">🔥 已连续签到 ${consecutiveDays} 天</span>`
    : ''

  toastDiv.innerHTML = `
    <div style="display: flex; flex-direction: column; align-items: center; gap: 12px;">
      <div class="check-in-icon-big" style="
        font-size: 64px;
        line-height: 1;
        animation: bounceIn 0.6s ease-out;
        filter: drop-shadow(0 4px 12px rgba(16, 185, 129, 0.3));
      ">🎉</div>
      <div style="display: flex; flex-direction: column; align-items: center; gap: 4px;">
        <span style="
          font-size: 18px;
          font-weight: 600;
          color: #10B981;
          line-height: 1.4;
        ">签到成功！</span>
        <span style="
          font-size: 14px;
          font-weight: 500;
          color: #6B7280;
          line-height: 1.4;
        ">获得 ${points} 积分</span>
        ${streakText}
      </div>
    </div>
  `

  toastDiv.style.cssText = `
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%) scale(0.9);
    background: #FFFFFF;
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    padding: 32px 48px;
    border-radius: 20px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
    z-index: 10000;
    opacity: 0;
    transition: all 0.3s ease-out;
    pointer-events: none;
  `

  const style = document.createElement('style')
  style.textContent = `
    @keyframes bounceIn {
      0% { transform: scale(0); opacity: 0; }
      50% { transform: scale(1.2); opacity: 1; }
      100% { transform: scale(1); opacity: 1; }
    }
  `
  document.head.appendChild(style)

  document.body.appendChild(toastDiv)

  requestAnimationFrame(() => {
    requestAnimationFrame(() => {
      toastDiv.style.opacity = '1'
      toastDiv.style.transform = 'translate(-50%, -50%) scale(1)'
    })
  })

  setTimeout(() => {
    toastDiv.style.opacity = '0'
    toastDiv.style.transform = 'translate(-50%, -50%) scale(0.9)'

    setTimeout(() => {
      if (document.body.contains(toastDiv)) {
        document.body.removeChild(toastDiv)
      }
      if (document.head.contains(style)) {
        document.head.removeChild(style)
      }
    }, 300)
  }, 2000)
  // #endif

  // #ifndef H5
  const streakMsg = consecutiveDays > 1 ? `，连续${consecutiveDays}天` : ''
  uni.showToast({
    title: `签到成功！+${points}积分${streakMsg}`,
    icon: 'success',
    duration: 2000
  })
  // #endif
}

// 组件挂载时检查登录状态和签到状态
onMounted(() => {
  // 清理旧的全局存储键(兼容旧版本)
  const oldKey = 'lastCheckInDate'
  if (uni.getStorageSync(oldKey)) {
    uni.removeStorageSync(oldKey)
  }

  checkLoginStatus()
  checkTodayCheckIn()
})

// 点击头像
const handleAvatarClick = () => {
  showUserMenu.value = !showUserMenu.value
}

// 菜单项点击
const handleMenuClick = (menuId: string) => {
  switch (menuId) {
    case 'profile':
      uni.navigateTo({
        url: '/pages/user/profile',
        fail: () => {
          uni.showToast({ title: '功能开发中', icon: 'none' })
        }
      })
      break
    case 'favorites':
      uni.navigateTo({
        url: '/pages/user/favorites',
        fail: () => {
          uni.showToast({ title: '功能开发中', icon: 'none' })
        }
      })
      break
    case 'settings':
      uni.navigateTo({
        url: '/pages/user/settings',
        fail: () => {
          uni.showToast({ title: '功能开发中', icon: 'none' })
        }
      })
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 退出登录
const handleLogout = () => {
  // 二次确认
  uni.showModal({
    title: '退出登录',
    content: '确定要退出登录吗？',
    confirmText: '退出',
    cancelText: '取消',
    success: (res) => {
      if (res.confirm) {
        // 用户确认退出
        console.log('用户确认退出登录')

        // 清除用户专属的签到状态
        const userId = userInfo.value.userId
        if (userId) {
          const storageKey = `lastCheckInDate_${userId}`
          uni.removeStorageSync(storageKey)
        }

        // 清除本地存储
        uni.removeStorageSync(config.tokenKey)
        uni.removeStorageSync(config.refreshTokenKey)
        uni.removeStorageSync(config.userInfoKey)

        // 🎯 企业级事件总线：触发全局退出登录事件，通知所有监听组件清空数据
        uni.$emit('user-logout')

        // 显示提示
        showWelcomeToast('已安全退出,期待下次再见!', 'info')

        // 动画：头像按钮淡出，登录按钮淡入
        showAvatarButton.value = false
        setTimeout(() => {
          showLoginButton.value = true
          isLoggedIn.value = false
          isCheckedIn.value = false
          userInfo.value = {
            userId: null,
            nickname: '',
            email: '',
            phone: '',
            avatar: ''
          }
        }, 300)
      } else {
        // 用户取消退出
        console.log('用户取消退出登录')
      }
    }
  })
}

// 显示欢迎提示（产品级轻量气泡 - CampusLink品牌调性）
const showWelcomeToast = (message: string, type: 'success' | 'info' = 'success') => {
  // #ifdef H5
  const toastDiv = document.createElement('div')

  // 精细化配色：更轻盈的背景 + 品牌蓝灰调和 + 精美图标
  const typeConfig = {
    success: {
      bg: 'rgba(209, 250, 229, 0.55)', // 优化：降低不透明度到55%
      textColor: '#14532d', // 优化：更深的绿色文字
      icon: '🎉', // 优化：改用庆祝图标，更欢迎
      glowColor: 'rgba(34, 197, 94, 0.25)' // 柔和绿光
    },
    info: {
      bg: 'rgba(239, 246, 255, 0.55)', // 优化：降低不透明度到55%
      textColor: '#1E3A8A', // 深蓝文字
      icon: '👋', // 优化：改用挥手图标，更友好
      glowColor: 'rgba(59, 130, 246, 0.25)' // 柔和蓝光
    }
  }

  const cfg = typeConfig[type]

  // 轻量化结构：单个图标 + 文字（移除重复图标）
  toastDiv.innerHTML = `
    <div style="display: flex; align-items: center; gap: 8px;">
      <span class="toast-icon" style="
        font-size: 16px;
        line-height: 1;
        flex-shrink: 0;
        animation: iconPulse 1.5s ease-in-out infinite;
        filter: drop-shadow(0 0 4px ${cfg.glowColor});
      ">${cfg.icon}</span>
      <span style="
        font-size: 14px;
        font-weight: 500;
        color: ${cfg.textColor};
        line-height: 1.4;
        letter-spacing: 0.3px;
      ">${message}</span>
    </div>
  `

  // 优化：更轻盈的样式 - 减小padding，降低阴影，提高位置（远离弹窗）
  toastDiv.style.cssText = `
    position: fixed;
    top: 60px;
    left: 50%;
    transform: translate(-50%, -10px) scale(0.98);
    background: ${cfg.bg};
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    padding: 8px 20px;
    border-radius: 10px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    z-index: 10000;
    opacity: 0;
    transition: all 0.35s ease-out;
    pointer-events: none;
  `

  // 添加图标脉动动画样式
  const style = document.createElement('style')
  style.textContent = `
    @keyframes iconPulse {
      0%, 100% { transform: scale(1); opacity: 1; }
      50% { transform: scale(1.1); opacity: 0.85; }
    }
  `
  document.head.appendChild(style)

  document.body.appendChild(toastDiv)

  // 入场动画：柔和淡入 + 微缩放 + 光晕显现
  requestAnimationFrame(() => {
    requestAnimationFrame(() => {
      toastDiv.style.opacity = '1'
      toastDiv.style.transform = 'translate(-50%, 0) scale(1)'
      toastDiv.style.boxShadow = `0 4px 12px rgba(0, 0, 0, 0.08), 0 0 12px ${cfg.glowColor}`
    })
  })

  // 停留2.5秒后上滑淡出
  setTimeout(() => {
    toastDiv.style.opacity = '0'
    toastDiv.style.transform = 'translate(-50%, -8px) scale(0.98)'
    toastDiv.style.boxShadow = '0 2px 8px rgba(0, 0, 0, 0.05)'

    setTimeout(() => {
      if (document.body.contains(toastDiv)) {
        document.body.removeChild(toastDiv)
      }
      if (document.head.contains(style)) {
        document.head.removeChild(style)
      }
    }, 300)
  }, 2500)
  // #endif

  // #ifndef H5
  uni.showToast({
    title: message,
    icon: 'none',
    duration: 2500
  })
  // #endif
}

/**
 * 登录/注册（优化：合并入口，打开统一弹窗）
 */
const handleLoginRegister = () => {
  // 触发父组件的登录事件，打开登录弹窗
  emit('login')
}

/**
 * 发布（优化：主操作按钮）
 */
const handlePublish = () => {
  uni.showActionSheet({
    itemList: ['发布资源', '提出问题', '发布任务'],
    success: (res) => {
      if (res.tapIndex === 0) {
        uni.navigateTo({ url: '/pages/resource/publish' })
      } else if (res.tapIndex === 1) {
        uni.navigateTo({ url: '/pages/question/publish' })
      } else if (res.tapIndex === 2) {
        uni.navigateTo({ url: '/pages/task/publish' })
      }
    }
  })
}


/**
 * 搜索处理
 */
const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    uni.showToast({ title: '请输入搜索内容', icon: 'none' })
    return
  }
  showHotTags.value = false
  emit('search', searchKeyword.value.trim())
}

/**
 * 热门标签点击
 */
const handleTagClick = (tag: string) => {
  searchKeyword.value = tag
  showHotTags.value = false
  handleSearch()
}

/**
 * 输入框失焦
 */
const handleBlur = () => {
  // 延迟隐藏，确保点击标签事件能触发
  setTimeout(() => {
    showHotTags.value = false
  }, 200)
}

/**
 * 语音搜索
 */
const handleVoiceSearch = () => {
  if (isVoiceActive.value) {
    // 停止语音识别
    stopVoiceRecognition()
  } else {
    // 开始语音识别
    startVoiceRecognition()
  }
}

/**
 * 语音按钮按下
 */
const handleVoiceTouchStart = () => {
  isVoiceActive.value = true
}

/**
 * 语音按钮松开
 */
const handleVoiceTouchEnd = () => {
  setTimeout(() => {
    isVoiceActive.value = false
  }, 200)
}

/**
 * 开始语音识别
 */
const startVoiceRecognition = () => {
  isVoiceActive.value = true

  // #ifdef H5
  // H5 端使用 Web Speech API
  if ('webkitSpeechRecognition' in window || 'SpeechRecognition' in window) {
    const SpeechRecognition = (window as any).webkitSpeechRecognition || (window as any).SpeechRecognition
    const recognition = new SpeechRecognition()
    recognition.lang = 'zh-CN'
    recognition.continuous = false
    recognition.interimResults = false

    recognition.onresult = (event: any) => {
      const transcript = event.results[0][0].transcript
      searchKeyword.value = transcript
      isVoiceActive.value = false
      handleSearch()
    }

    recognition.onerror = () => {
      isVoiceActive.value = false
      uni.showToast({ title: '语音识别失败，请重试', icon: 'none' })
    }

    recognition.onend = () => {
      isVoiceActive.value = false
    }

    recognition.start()
  } else {
    isVoiceActive.value = false
    uni.showToast({ title: '您的浏览器不支持语音识别', icon: 'none' })
  }
  // #endif

  // #ifdef MP-WEIXIN
  // 微信小程序使用录音管理器
  const recorderManager = uni.getRecorderManager()

  recorderManager.onStop((res: any) => {
    isVoiceActive.value = false
    // 这里需要调用后端 API 进行语音识别
    uni.showToast({ title: '语音识别功能开发中', icon: 'none' })
  })

  recorderManager.onError(() => {
    isVoiceActive.value = false
    uni.showToast({ title: '录音失败，请重试', icon: 'none' })
  })

  recorderManager.start({
    duration: 60000,
    format: 'mp3',
  })
  // #endif

  // #ifndef H5 || MP-WEIXIN
  isVoiceActive.value = false
  uni.showToast({ title: '当前平台暂不支持语音搜索', icon: 'none' })
  // #endif
}

/**
 * 停止语音识别
 */
const stopVoiceRecognition = () => {
  isVoiceActive.value = false

  // #ifdef MP-WEIXIN
  const recorderManager = uni.getRecorderManager()
  recorderManager.stop()
  // #endif
}

/**
 * 移动端菜单控制
 */
const toggleMobileMenu = () => {
  showMobileMenu.value = !showMobileMenu.value

  // 关键修复：菜单打开时阻止页面滚动
  if (showMobileMenu.value) {
    // #ifdef H5
    document.body.style.overflow = 'hidden'
    document.body.style.position = 'fixed'
    document.body.style.width = '100%'
    // #endif
  } else {
    // #ifdef H5
    document.body.style.overflow = ''
    document.body.style.position = ''
    document.body.style.width = ''
    // #endif
  }
}

const closeMobileMenu = () => {
  showMobileMenu.value = false

  // 关键修复：菜单关闭时恢复页面滚动
  // #ifdef H5
  document.body.style.overflow = ''
  document.body.style.position = ''
  document.body.style.width = ''
  // #endif
}

/**
 * 移动端菜单操作
 */
const handleMobileLogin = () => {
  closeMobileMenu()
  handleLoginRegister()
}

const handleMobileProfile = () => {
  closeMobileMenu()
  uni.navigateTo({
    url: '/pages/user/profile',
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    }
  })
}

const handleMobilePublish = () => {
  closeMobileMenu()
  handlePublish()
}

const handleMobileFavorites = () => {
  closeMobileMenu()
  uni.navigateTo({
    url: '/pages/user/favorites',
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    }
  })
}

const handleMobileSettings = () => {
  closeMobileMenu()
  uni.navigateTo({
    url: '/pages/user/settings',
    fail: () => {
      uni.showToast({ title: '功能开发中', icon: 'none' })
    }
  })
}

const handleMobileLogout = () => {
  // 优化：添加确认弹窗，防止误触
  uni.showModal({
    title: '退出登录',
    content: '确定要退出登录吗？',
    confirmText: '退出',
    cancelText: '取消',
    confirmColor: '#FF4D4F',
    success: (res) => {
      if (res.confirm) {
        closeMobileMenu()
        handleLogout()
      }
    }
  })
}

// 暴露方法给父组件
defineExpose({
  checkLoginStatus
})
</script>

<style scoped lang="scss">
// 变量已通过 uni.scss 全局注入

// Web 端重构样式
// 核心目标：压缩高度、强化导航、统一组件样式

.top-focus-bar {
  position: relative;
  width: 100%;
  background: linear-gradient(180deg, #F8FAFC 0%, #FFFFFF 100%); // 使用更专业的淡色渐变
  overflow: visible;
  z-index: 10; // 确保在页面内容之上
  border-bottom: 1px solid #E2E8F0; // 轻微分割线
}

.focus-container {
  max-width: 1280px; // 适配主流屏幕
  margin: 0 auto;
  padding: 16px 32px; // 统一内边距
  display: flex;
  flex-direction: column;
  gap: 24px; // 品牌区与搜索区的间距
}

/* 第一行：品牌区 + 用户操作区 */
.top-nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 72px; // 建议高度
}

.brand-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-logo {
  font-size: 24px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.5px;
}

.brand-slogan-full {
  font-size: 14px;
  font-weight: 400; // 按建议调整
  color: #64748B; // 按建议调整
  padding-left: 12px;
  border-left: 1px solid #CBD5E1;
  line-height: 1.2;
}

.brand-slogan-short {
  display: none; // 移动端专用, Web端隐藏
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 16px; // 按建议调整
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 44px; // 统一按钮高度
  padding: 0 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.action-btn-secondary { // 登录/注册按钮
  color: #334155;
  background-color: #FFFFFF;
  border-color: #CBD5E1;

  &:hover {
    background-color: #F8FAFC;
    border-color: #94A3B8;
  }
}

.action-btn-primary { // 发布按钮
  color: #FFFFFF;
  background-color: #2563EB; // 统一主色
  gap: 8px;

  .action-btn-icon {
    font-size: 16px;
  }

  &:hover {
    background-color: #1D4ED8; // Hover深一阶
  }
}

.avatar-wrapper {
  cursor: pointer;
}

/* 第二行：搜索区 */
.main-content-area {
  display: flex;
  justify-content: center;
}

.search-section {
  width: 80%; // 占容器80%宽度
  max-width: 960px;
}

.search-box-wrapper {
  width: 100%;
}

.search-box {
  display: flex;
  align-items: center;
  height: 56px; // 建议高度
  width: 100%;
  background: #FFFFFF;
  border-radius: 999px; // 胶囊形状
  padding: 0 8px 0 20px; // 内边距调整
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04); // 建议阴影
  border: 1px solid #E2E8F0;
  transition: all 0.2s ease;

  &:hover {
    border-color: #A7C5F9;
    box-shadow: 0 8px 32px rgba(15, 23, 42, 0.08);
  }

  &:focus-within {
    border-color: #2563EB;
    box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
  }
}

.search-icon {
  font-size: 20px;
  color: #94A3B8;
  margin-right: 12px;
}

.search-input {
  flex-grow: 1;
  height: 100%;
  border: none;
  outline: none;
  background: transparent;
  font-size: 16px;
  color: #0F172A;

  &::placeholder {
    color: #94A3B8;
  }
}

.voice-search-btn-inline {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  color: #64748B;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: #F1F5F9;
  }
}

.voice-icon-inline {
  width: 20px;
  height: 20px;
}

.search-btn-primary {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 48px;
  padding: 0 28px;
  border-radius: 999px; // 胶囊形状
  background-color: #2563EB;
  color: #FFFFFF;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s ease;
  margin-left: 8px;

  &:hover {
    background-color: #1D4ED8;
  }
}

.search-tips { // 搜索框下方提示
  margin-top: 12px;
  display: flex;
  justify-content: center;
  gap: 16px;
  font-size: 12px;
  color: #94A3B8;
}

.hot-tags {
  display: none; // 在新设计中，暂时隐藏输入框内的热门标签以简化
}


/* 移除不再需要的元素 */
.bg-decoration-layer, .illustration-layer {
  display: none;
}

/* 响应式设计保留，但主要针对移动端，Web样式已在上面重构 */
.mobile-only {
  display: none !important;
}

@media (max-width: 1024px) {
  .focus-container {
    padding: 16px;
  }
  .brand-slogan-full {
    display: none;
  }
  .search-section {
    width: 100%;
  }
}


@media (max-width: 768px) {
  .desktop-only { display: none !important; }

  .top-focus-bar {
    height: 128px; // 整体高度
    background: #FFFFFF;
    border-bottom: 1px solid #F1F5F9;
  }

  .focus-container {
    padding: 0 16px;
    gap: 8px;
    height: 100%;
    justify-content: center;
  }

  .top-nav-bar {
    height: 56px; // 第一行高度
  }
  
  .brand-logo { font-size: 20px; }
  .brand-slogan-full { display: none; }

  .action-buttons {
    gap: 0; // 移动端按钮更紧凑
  }
  
  // 修正对UserAvatar子组件的选择器
  .avatar-wrapper :deep(.user-avatar-comp) {
    width: 36px;
    height: 36px;
  }

  .action-btn.action-btn-primary {
    height: 36px;
    width: 36px;
    padding: 0; // 移除内边距，使其为纯图标按钮
    .action-btn-icon { font-size: 18px; }
  }

  .main-content-area, .search-section {
    width: 100%;
  }

  .search-box {
    height: 44px; // 搜索框高度
    border-radius: 999px; // 胶囊圆角
    padding: 0 4px 0 16px;
    box-shadow: none;
    background: #F1F5F9;
    border-color: transparent;
    
    &:hover, &:focus-within {
      background: #E2E8F0;
    }
  }
  
  .search-icon { font-size: 18px; margin-right: 8px; }
  .search-input { font-size: 15px; }

  .search-btn-primary {
    height: 36px;
    padding: 0 16px;
    font-size: 14px;
    margin-left: 4px;
  }
  
  .search-tips { display: none; }
}

// ... 此处省略移动端菜单等未改动样式 ...
// 保持原有的 .mobile-menu-overlay, .mobile-menu-panel 等样式不变
/* ========== 移动端折叠菜单样式 - 锦上添花优化 ========== */
.mobile-menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba($gray-900, 0.6);
  /* 锦上添花: 增强背景模糊效果 backdrop-filter: blur(12px) */
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  z-index: $z-modal;
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  animation: fadeIn 0.3s ease-out;
  pointer-events: auto;
  touch-action: none;
}

.mobile-menu-panel {
  width: 560rpx; /* 280px - 70% 屏幕宽度 */
  max-width: 85%;
  height: 100vh;
  /* 优化: 添加淡蓝到白渐变背景 */
  background: linear-gradient(180deg, $gray-50 0%, $bg-surface 100%);
  box-shadow: -16rpx 0 48rpx rgba($gray-900, 0.12); /* 优化: 增强阴影 */
  display: flex;
  flex-direction: column;
  animation: slideInRight 0.3s cubic-bezier(0.4, 0, 0.2, 1); /* 优化: 更流畅的缓动 */
  position: relative;
  z-index: $z-modal + 1;
  transform: translateZ(0);
  will-change: transform;
  /* 优化: 统一圆角 - 左上和左下圆角 */
  border-radius: $radius-xl 0 0 $radius-xl;
}

@keyframes slideInRight {
  from {
    transform: translateX(100%) translateZ(0);
    opacity: 0.8;
  }
  to {
    transform: translateX(0) translateZ(0);
    opacity: 1;
  }
}

/* ==========  菜单头部 - 优化品牌化设计 ========== */
.mobile-menu-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $sp-10 $sp-8;
  border-bottom: 1px solid rgba($gray-500, 0.1); /* 优化: 更柔和的分割线 */
  background: linear-gradient(180deg, rgba($primary-100, 0.3) 0%, transparent 100%); /* 优化: 轻微蓝色渐变 */
}

/* 优化: 品牌区域 */
.mobile-menu-brand {
  display: flex;
  flex-direction: column;
  gap: $sp-1;
  position: relative;
  padding-left: $sp-4;

  /* 优化: 品牌色分割线 */
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 6rpx; /* 3px */
    background: linear-gradient(180deg, $primary 0%, rgba($primary, 0.3) 100%);
    border-radius: $radius-sm;
  }
}

.mobile-menu-logo {
  font-size: $font-size-2xl; /* 18px */
  font-weight: $font-weight-bold;
  color: $primary;
  line-height: $line-height-tight;
  letter-spacing: 0.5rpx;
}

.mobile-menu-subtitle {
  font-size: $font-size-xs; /* 12px */
  font-weight: $font-weight-medium;
  color: $gray-500;
  line-height: $line-height-tight;
  opacity: 0.9;
}

.mobile-menu-close {
  width: 88rpx; /* 44px */
  height: 88rpx;
  @include flex-center;
  cursor: pointer;
  border-radius: $radius-full;
  background: rgba($gray-500, 0.08); /* 优化: 浅灰底色 */
  border: 2rpx solid rgba($gray-500, 0.1); /* 优化: 线框风格 */
  transition: $transition-fast;
}

.mobile-menu-close:active {
  background: rgba($gray-900, 0.12); /* 优化：增强按下反馈 */
  transform: scale(0.92);
}

.close-icon {
  font-size: $font-size-4xl; /* 28px - 优化：增大图标尺寸 */
  font-weight: $font-weight-light;
  color: $gray-600;
  line-height: 1;
}

/* 菜单内容 */
.mobile-menu-content {
  flex: 1;
  padding: $sp-8 0;
  overflow-y: auto;
}

/* ==========  用户信息卡片 - 锦上添花优化 ========== */
.mobile-user-card {
  display: flex;
  align-items: center;
  gap: $sp-7;
  padding: $sp-9;
  margin: 0 $sp-6 $sp-8;
  /* 锦上添花: 更柔和的180°渐变 */
  background: linear-gradient(180deg, $primary-50 0%, $bg-surface 100%);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: $radius-xl;
  border: 2rpx solid rgba($bg-surface, 0.6);
  cursor: pointer;
  transition: $transition-base;
  box-shadow: $shadow-sm;
  position: relative;
  overflow: hidden;

  /* 微光效果 */
  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba($bg-surface, 0.2) 0%, transparent 70%);
    opacity: 0;
    transition: $transition-base;
  }

  &:active::before {
    opacity: 1;
  }
}

.mobile-user-card:active {
  transform: scale(0.97);
  box-shadow: $shadow-sm;
}

.mobile-user-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: $radius-full;
  flex-shrink: 0;
  border: 4rpx solid rgba($bg-surface, 0.8);
  /* 锦上添花: 增强头像阴影,提升空间感 */
  box-shadow: $shadow-sm;
}

.mobile-user-avatar-default {
  background: $primary;
  @include flex-center;
}

.avatar-text {
  font-size: $font-size-2xl; /* 20px */
  font-weight: $font-weight-bold;
  color: $text-inverse;
}

.mobile-user-info {
  display: flex;
  flex-direction: column;
  gap: $sp-3; /* 优化：增加昵称与邮箱的间距 */
  flex: 1;
  min-width: 0;
}

.mobile-user-name {
  font-size: $font-size-lg; /* 17px - 优化：略微增大字号 */
  font-weight: $font-weight-semibold;
  color: $text-primary;
  @include text-ellipsis;
  line-height: $line-height-tight; /* 优化：增加行高 */
}

.mobile-user-email {
  font-size: $font-size-sm;
  color: $gray-400; /* 锦上添花: 更浅的灰色,增强层次区分 */
  @include text-ellipsis;
  line-height: $line-height-normal;
  opacity: 0.85; /* 锦上添花: 降低不透明度 */
}

/* ==========  分组标题 - 锦上添花优化 ========== */
.mobile-menu-section-title {
  position: relative;
  font-size: $font-size-xs;
  font-weight: $font-weight-semibold;
  color: $gray-500; /* 锦上添花: 更浅的灰色,增强轻量感 */
  padding: $sp-6 $sp-12 $sp-3 56rpx; /* 锦上添花: 左侧增加空间给竖条 */
  letter-spacing: 1rpx;
  text-transform: uppercase;

  /* 锦上添花: 添加左侧品牌蓝竖条 */
  &::before {
    content: '';
    position: absolute;
    left: $sp-10;
    top: 50%;
    transform: translateY(-50%);
    width: 4rpx; /* 2px */
    height: 28rpx; /* 14px */
    background: linear-gradient(180deg, $primary 0%, rgba($primary, 0.3) 100%);
    border-radius: $radius-xs;
  }
}

/* ==========  分组分割线 ========== */
.mobile-menu-divider {
  height: 2rpx;
  background: linear-gradient(90deg, transparent 0%, rgba($gray-500, 0.12) 50%, transparent 100%);
  margin: $sp-5 $sp-6;
}

/* ==========  菜单项 - 锦上添花交互优化 ========== */
.mobile-menu-item {
  display: flex;
  align-items: center;
  gap: $sp-5;
  min-height: 96rpx;
  padding: $sp-6 $sp-12;
  margin: 0 $sp-4;
  border-radius: $radius-xl;
  cursor: pointer;
  transition: $transition-fast;
  position: relative;

  /* 锦上添花: hover时添加浅蓝背景 */
  &:hover {
    background: rgba($primary-50, 0.6);
  }
}

.mobile-menu-item:active {
  background: rgba($primary-100, 0.8); /* 更深的按压反馈 */
  transform: scale(0.98);
}

.mobile-menu-item-icon {
  font-size: $font-size-2xl;
  line-height: 1;
  flex-shrink: 0;
  width: 40rpx;
  text-align: center;
  transition: $transition-fast;
  /* 锦上添花: 默认轻微上浮 */
  transform: translateY(-1rpx);
}

/* 锦上添花: hover时图标回落 */
.mobile-menu-item:hover .mobile-menu-item-icon {
  transform: translateY(0);
}

.mobile-menu-item:active .mobile-menu-item-icon {
  transform: scale(1.1); /* 按压时图标放大 */
}

.mobile-menu-item-text {
  font-size: $font-size-md;
  font-weight: $font-weight-medium;
  color: $gray-600; /* 锦上添花: 稍深的灰色,增强可读性 */
  flex: 1;
  line-height: $line-height-relaxed;
}

.mobile-menu-item-badge {
  font-size: $font-size-xs; /* 11px */
  font-weight: $font-weight-semibold;
  color: $success;
  padding: $sp-1 $sp-4;
  background: rgba($success, 0.15);
  border-radius: $radius-md; /* 优化: 统一圆角 */
  white-space: nowrap;
}

.mobile-menu-item-badge.checked {
  color: $success;
  background: rgba($success, 0.15);
  /* 锦上添花: 微光晕动画效果 */
  box-shadow: 0 0 12rpx rgba($success, 0.4);
  animation: badge-glow 2s ease-in-out infinite;
}

/* 锦上添花: 徽章光晕脉动动画 */
@keyframes badge-glow {
  0%, 100% {
    box-shadow: 0 0 12rpx rgba($success, 0.4);
  }
  50% {
    box-shadow: 0 0 20rpx rgba($success, 0.6);
  }
}

/* ==========  功能图标个性色 ========== */
/* 签到 - 绿色 */
.icon-checkin {
  filter: grayscale(0.2);
}

.menu-item-checkin:active .icon-checkin {
  filter: grayscale(0) brightness(1.2) hue-rotate(10deg);
}

/* 发布 - 蓝色 */
.icon-publish {
  filter: grayscale(0.3);
}

.menu-item-publish:active .icon-publish {
  filter: grayscale(0) brightness(1.2);
}

/* 收藏 - 金黄色 */
.icon-favorites {
  filter: grayscale(0.2);
}

.menu-item-favorites:active .icon-favorites {
  filter: grayscale(0) brightness(1.3) hue-rotate(15deg);
}

/* 设置 - 灰蓝色 */
.icon-settings {
  filter: grayscale(0.4);
}

.menu-item-settings:active .icon-settings {
  filter: grayscale(0) brightness(1.1);
}

/* 退出 - 灰色 → 红色 */
.icon-logout {
  filter: grayscale(1) brightness(0.8);
}

.mobile-menu-item-danger:active .icon-logout {
  filter: grayscale(0) brightness(1) hue-rotate(-10deg);
}

/* ==========  退出登录 - 锦上添花透明红描边风格 ========== */
.mobile-menu-item-danger {
  /* 锦上添花: 透明红渐变描边风格 */
  background: rgba($error, 0.05);
  margin: $sp-4 $sp-6 $sp-6;
  border-radius: $radius-xl;
  border: 2rpx solid rgba($error, 0.3);
  transition: $transition-fast;

  /* 锦上添花: hover效果 */
  &:hover {
    background: rgba($error, 0.08);
    border-color: rgba($error, 0.4);
  }
}

.mobile-menu-item-danger:active {
  background: rgba($error, 0.12);
  border-color: rgba($error, 0.5);
  transform: scale(0.98);
}

.mobile-menu-item-danger .mobile-menu-item-text {
  color: $error; /* 锦上添花: 更鲜艳的红色 */
  font-weight: $font-weight-semibold; /* 锦上添花: 加粗文字 */
}

.mobile-menu-item-danger .mobile-menu-item-icon {
  opacity: 0.9;
  /* 锦上添花: 灰度处理,hover时恢复彩色 */
  filter: grayscale(0.3);
}

.mobile-menu-item-danger:hover .mobile-menu-item-icon {
  filter: grayscale(0);
}
</style>

