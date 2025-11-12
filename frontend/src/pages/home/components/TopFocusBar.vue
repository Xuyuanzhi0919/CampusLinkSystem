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

        <!-- 右侧：操作按钮（桌面端和平板端显示）-->
        <view class="action-buttons desktop-tablet-only">
          <!-- 登录/注册按钮（未登录时显示）-->
          <view
            v-if="showLoginButton"
            class="action-btn action-btn-secondary login-btn-fade"
            :class="{ 'btn-fade-out': !showLoginButton }"
            @click="handleLoginRegister"
          >
            <text class="action-btn-text">登录 / 注册</text>
          </view>

          <!-- 用户头像按钮（登录后显示）-->
          <view
            v-if="showAvatarButton"
            class="avatar-wrapper avatar-fade"
            :class="{ 'btn-fade-in': showAvatarButton }"
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
            <text class="action-btn-text">发布</text>
          </view>
        </view>

        <!-- 移动端：汉堡菜单图标 -->
        <view class="mobile-menu-icon mobile-only" @click="toggleMobileMenu">
          <view class="hamburger-line"></view>
          <view class="hamburger-line"></view>
          <view class="hamburger-line"></view>
        </view>

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
                placeholder="搜课件/问问题/找活动"
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
        <!-- 菜单头部 -->
        <view class="mobile-menu-header">
          <text class="mobile-menu-title">菜单</text>
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

            <!-- 签到 -->
            <view class="mobile-menu-item" @click="handleCheckIn">
              <text class="mobile-menu-item-icon">{{ isCheckedIn ? '✓' : '📅' }}</text>
              <text class="mobile-menu-item-text">{{ isCheckedIn ? '今日已签到' : '每日签到' }}</text>
              <text v-if="!isCheckedIn" class="mobile-menu-item-badge">+10积分</text>
            </view>

            <!-- 发布 -->
            <view class="mobile-menu-item" @click="handleMobilePublish">
              <text class="mobile-menu-item-icon">✨</text>
              <text class="mobile-menu-item-text">发布内容</text>
            </view>

            <!-- 我的收藏 -->
            <view class="mobile-menu-item" @click="handleMobileFavorites">
              <text class="mobile-menu-item-icon">⭐</text>
              <text class="mobile-menu-item-text">我的收藏</text>
            </view>

            <!-- 设置 -->
            <view class="mobile-menu-item" @click="handleMobileSettings">
              <text class="mobile-menu-item-icon">⚙️</text>
              <text class="mobile-menu-item-text">设置</text>
            </view>

            <!-- 退出登录 -->
            <view class="mobile-menu-item mobile-menu-item-danger" @click="handleMobileLogout">
              <text class="mobile-menu-item-icon">🚪</text>
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
        nickname: parsedUserInfo.nickname || '用户',
        email: parsedUserInfo.email || '',
        phone: parsedUserInfo.phone || '',
        avatar: parsedUserInfo.avatar || ''
      }
      isLoggedIn.value = true

      // 动画：登录按钮淡出，头像按钮淡入
      showLoginButton.value = false
      setTimeout(() => {
        showAvatarButton.value = true
      }, 200)
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

// 检查今日是否已签到
const checkTodayCheckIn = () => {
  const lastCheckInDate = uni.getStorageSync('lastCheckInDate')
  const today = new Date().toDateString()
  isCheckedIn.value = lastCheckInDate === today
}

// 签到处理
const handleCheckIn = (event: any) => {
  if (isCheckedIn.value) {
    uni.showToast({ title: '今日已签到', icon: 'none' })
    return
  }

  // 执行签到
  const today = new Date().toDateString()
  uni.setStorageSync('lastCheckInDate', today)
  isCheckedIn.value = true

  // 显示签到成功提示（精美动画）
  showCheckInSuccess()

  // 显示积分动画
  // #ifdef H5
  const rect = event.target.getBoundingClientRect()
  pointsPosition.value = {
    x: rect.left + rect.width / 2,
    y: rect.top + rect.height / 2
  }
  // #endif

  // #ifndef H5
  pointsPosition.value = {
    x: uni.getSystemInfoSync().windowWidth / 2,
    y: 200
  }
  // #endif

  pointsValue.value = 10
  showPointsAnimation.value = true
}

// 显示签到成功提示
const showCheckInSuccess = () => {
  // #ifdef H5
  const toastDiv = document.createElement('div')

  toastDiv.innerHTML = `
    <div style="display: flex; flex-direction: column; align-items: center; gap: 12px;">
      <div class="check-in-icon-big" style="
        font-size: 48px;
        line-height: 1;
        animation: bounceIn 0.6s ease-out;
      ">✓</div>
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
        ">获得 10 积分</span>
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
  uni.showToast({
    title: '签到成功！+10积分',
    icon: 'success',
    duration: 2000
  })
  // #endif
}

// 组件挂载时检查登录状态和签到状态
onMounted(() => {
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
  // 清除本地存储
  uni.removeStorageSync(config.tokenKey)
  uni.removeStorageSync(config.refreshTokenKey)
  uni.removeStorageSync(config.userInfoKey)

  // 显示提示
  showWelcomeToast('已安全退出,期待下次再见!', 'info')

  // 动画：头像按钮淡出，登录按钮淡入
  showAvatarButton.value = false
  setTimeout(() => {
    showLoginButton.value = true
    isLoggedIn.value = false
    userInfo.value = {
      nickname: '',
      email: '',
      phone: '',
      avatar: ''
    }
  }, 300)
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
/* 企业级重构：第一层 - 品牌渐变区（顶部背景）- 优化：缩减高度增强紧凑感 */
.top-focus-bar {
  position: relative;
  width: 100%;
  height: 640rpx; /* 320px - 优化：从800rpx减小到640rpx，减少20% */
  /* 优化：径向渐变，从中心向外扩散，增强纵深感 */
  background:
    radial-gradient(ellipse at 50% 0%, #E8F1FF 0%, transparent 70%),
    linear-gradient(180deg,
      #E8F0FF 0%,      /* 顶部更亮的蓝 */
      #EEF4FF 25%,     /* 中上 */
      #F5F8FF 50%,     /* 中部 */
      #F9FAFB 75%,     /* 中下略深 */
      #FFFFFF 100%     /* 底部纯白 */
    );
  overflow: visible; /* 允许内容溢出，实现自然过渡 */
  z-index: auto; /* 关键修复：移除固定z-index，避免限制内部移动端菜单的层级 */

  /* 优化：增强阴影，提升浮层感 */
  box-shadow: 0 12rpx 40rpx rgba(37, 99, 235, 0.08);
}

/* 企业级重构：几何线条、模糊圆点、光晕装饰（10% 不透明度）*/
.bg-decoration-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  pointer-events: none;

  /* 添加整体光晕效果 + 白光透明渐层（增强视觉纵深）*/
  background:
    /* 顶部发光晕圈（白光透明渐层）*/
    radial-gradient(circle at 50% 0%, rgba(255, 255, 255, 0.4) 0%, transparent 40%),
    /* 右上光晕（45° 光线方向）*/
    radial-gradient(circle at 85% 15%, rgba(37, 99, 235, 0.12) 0%, transparent 50%),
    /* 左下光晕 */
    radial-gradient(circle at 15% 85%, rgba(220, 235, 255, 0.10) 0%, transparent 50%),
    /* 中央柔光 */
    radial-gradient(circle at 50% 40%, rgba(232, 240, 255, 0.08) 0%, transparent 60%);
}

/* 几何线条装饰 1 - 右上角（10% 不透明度）*/
.bg-circle-1 {
  position: absolute;
  top: -50rpx;
  right: 10%;
  width: 500rpx;
  height: 500rpx;
  background:
    /* 模糊圆点 */
    radial-gradient(circle, rgba(37, 99, 235, 0.10) 0%, transparent 60%);
  border-radius: 50%;
  filter: blur(40rpx); /* 20px 模糊 */
}

/* 几何线条装饰 2 - 左下角（10% 不透明度）*/
.bg-circle-2 {
  position: absolute;
  bottom: -100rpx;
  left: 5%;
  width: 400rpx;
  height: 400rpx;
  background:
    /* 模糊圆点 */
    radial-gradient(circle, rgba(220, 235, 255, 0.10) 0%, transparent 60%);
  border-radius: 50%;
  filter: blur(30rpx); /* 15px 模糊 */
}

/* 波浪形装饰 - 底部柔光带 */
.bg-wave {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 120rpx; /* 60px */
  background: linear-gradient(180deg, transparent 0%, rgba(249, 251, 255, 0.6) 100%);
  border-radius: 50% 50% 0 0 / 100% 100% 0 0;
}

/* ========== 三、内容容器（方案 B：三层结构）========== */
.focus-container {
  position: relative;
  max-width: 2400rpx; /* 1200px */
  height: 100%;
  margin: 0 auto;
  padding: 0 60rpx;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  z-index: 2;
}

/* ========== 四、顶部导航栏（第一行）========== */
.top-nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 80rpx; /* 40px - 减小高度 */
  padding-top: 16rpx;
}

/* 品牌标识 */
.brand-section {
  display: flex;
  flex-direction: column;
  gap: 2rpx;
  position: relative;
  padding-left: 16rpx; /* 为分割线留出空间 */

  /* 优化：增加品牌色分割线，强化品牌识别 */
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 6rpx; /* 3px */
    background: linear-gradient(180deg, var(--cl-primary, #2563EB) 0%, rgba(37, 99, 235, 0.3) 100%);
    border-radius: 3rpx;
  }
}

.brand-logo {
  font-size: 48rpx; /* 24px - 增大字号，增强品牌识别 */
  font-weight: 700;
  color: var(--cl-primary, #2563EB);
  line-height: 1.2;
  letter-spacing: 1rpx;
  /* 优化：增加文字阴影，增强立体感 */
  text-shadow: 0 2rpx 8rpx rgba(37, 99, 235, 0.15);
}

.brand-slogan {
  font-size: 24rpx; /* 12px - 增大字号 */
  font-weight: 500;
  color: var(--cl-gray-600, #64748B);
  line-height: 1.2;
  /* 优化：增加轻微透明度 */
  opacity: 0.9;
}

/* 操作按钮组 - 优化：增强视觉层次 */
.action-buttons {
  display: flex;
  align-items: center;
  gap: 20rpx; /* 增大间距 */
  /* 优化：移除背景容器，让按钮更突出 */
  padding: 0;
}

.action-btn {
  padding: 16rpx 32rpx; /* 增大内边距 */
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  border-radius: 28rpx; /* 14px - 更圆润 */
  border: 2px solid transparent;
  cursor: pointer;
  transition: all var(--transition-hover, 150ms ease);
  display: flex;
  align-items: center;
  gap: 8rpx;
  /* 优化：增加阴影 */
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
}

/* 优化：次操作按钮（登录/注册）- 白色背景 + 蓝色描边 */
.action-btn-secondary {
  background: rgba(255, 255, 255, 0.95); /* 白色背景 */
  border: 2px solid var(--cl-primary, #2563EB); /* 蓝色描边 */
}

.action-btn-secondary .action-btn-text {
  color: var(--cl-primary, #2563EB); /* 蓝色文字 */
  font-size: 30rpx; /* 15px - 增大 */
  font-weight: 600;
}

.action-btn-secondary:hover {
  background: rgba(37, 99, 235, 0.08); /* 轻微蓝色背景 */
  border-color: var(--cl-primary, #2563EB);
  transform: translateY(-2rpx);
  box-shadow: 0 6rpx 20rpx rgba(37, 99, 235, 0.2);
}

/* 优化：主操作按钮（发布）- 蓝色实心按钮 + 渐变 */
.action-btn-primary {
  background: linear-gradient(135deg, var(--cl-primary, #2563EB) 0%, #3B82F6 100%); /* 蓝色渐变 */
  border: none;
  /* 优化：增强阴影 */
  box-shadow: 0 8rpx 24rpx rgba(37, 99, 235, 0.35);
}

.action-btn-primary .action-btn-text {
  color: #FFFFFF; /* 白色文字 */
  font-size: 30rpx; /* 15px */
  font-weight: 600;
}

.action-btn-primary .action-btn-icon {
  color: #FFFFFF;
  font-size: 28rpx;
}

.action-btn-primary:hover {
  /* 优化：hover 时渐变更亮 */
  background: linear-gradient(135deg, #3B82F6 0%, #60A5FA 100%);
  transform: translateY(-2rpx) scale(1.03);
  box-shadow: 0 12rpx 32rpx rgba(37, 99, 235, 0.45);
}

.action-btn-text {
  font-size: 26rpx; /* 13px - 减小字号 */
  font-weight: 500;
  color: var(--cl-gray-700, #475569);
  line-height: 1;
}

.action-btn-icon {
  font-size: 26rpx; /* 减小字号 */
  line-height: 1;
}

/* 淡入淡出动画 */
.login-btn-fade {
  animation: fadeIn 0.3s ease-in-out;
}

.btn-fade-out {
  animation: fadeOut 0.3s ease-in-out;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
}

.avatar-fade {
  animation: fadeIn 0.3s ease-in-out;
}

.btn-fade-in {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10rpx);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeOut {
  from {
    opacity: 1;
    transform: translateY(0);
  }
  to {
    opacity: 0;
    transform: translateY(-10rpx);
  }
}


/* ========== 五、主内容区（第二行：搜索框居中）========== */
.main-content-area {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1; /* 占据剩余空间 */
  margin-top: 0; /* 移除上边距 */
}

/* 搜索栏（居中）- 优化：垂直布局 */
.search-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  max-width: 1400rpx; /* 最大宽度 700px */
  width: 70%; /* 占据 70% 宽度 */
  gap: 20rpx; /* 搜索框与提示文字的间距 */
}

/* 搜索框包装器 - 优化：增大间距，增强呼吸感 */
.search-box-wrapper {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 100rpx; /* 50px - 从 20rpx 增加到 100rpx，增强呼吸感 */
}

/* 搜索框 - 优化：适中尺寸 + 增强阴影 */
.search-box {
  position: relative;
  width: 100%;
  height: 92rpx; /* 46px - 优化：从100rpx减小到92rpx */
  /* 半透明玻璃效果（文档规范）*/
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-radius: 46rpx; /* 23px - 匹配高度的一半 */
  display: flex;
  align-items: center;
  padding: 0 28rpx;
  gap: 14rpx;
  /* 优化：柔和阴影，增强浮层感 */
  box-shadow: 0 10rpx 36rpx rgba(0, 0, 0, 0.10);
  transition: all var(--transition-hover, 150ms ease);
  /* 描边（文档规范）*/
  border: 2px solid rgba(255, 255, 255, 0.9);
}

.search-box:hover {
  background: rgba(255, 255, 255, 0.95);
  /* 优化：hover 时阴影更深 */
  box-shadow: 0 14rpx 44rpx rgba(0, 0, 0, 0.14);
  transform: translateY(-3rpx);
  border-color: rgba(37, 99, 235, 0.4);
}

.search-box:focus-within {
  background: rgba(255, 255, 255, 0.98);
  /* 优化：聚焦时发光蓝边 */
  box-shadow: 0 14rpx 48rpx rgba(37, 99, 235, 0.28), 0 0 0 4rpx rgba(37, 99, 235, 0.15);
  border-color: var(--cl-primary, #2563EB);
  transform: translateY(-3rpx) scale(1.01);
}

/* 搜索图标 - 优化：增大尺寸 */
.search-icon {
  font-size: 40rpx; /* 20px - 增大 */
  line-height: 1;
  color: var(--cl-primary, #2563EB); /* 改为品牌蓝 */
  flex-shrink: 0;
}

/* 搜索输入框 - 优化：增大字号 */
.search-input {
  flex: 1;
  height: 100%;
  font-size: 32rpx; /* 16px - 增大 */
  color: #1D2129;
  border: none;
  outline: none;
  background: transparent;
}

.search-input::placeholder {
  color: #C9CDD4; /* 浅灰色 */
  font-size: 32rpx; /* 16px - 增大 */
}

/* 热门搜索标签 */
.hot-tags {
  position: absolute;
  left: 60rpx;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  gap: 12rpx;
  z-index: 10;
}

.hot-tag {
  font-size: 24rpx; /* 12px */
  font-weight: 500;
  color: #1E5FFF;
  padding: 8rpx 16rpx;
  background: #E6F0FF;
  border-radius: 16rpx;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.hot-tag:hover {
  background: #1E5FFF;
  color: white;
  transform: scale(1.05);
}

.hot-tag:active {
  transform: scale(0.95);
}

/* 优化：搜索按钮 - 橙色圆角矩形按钮 */
.search-btn-primary {
  position: relative;
  height: 68rpx; /* 34px */
  padding: 0 40rpx; /* 左右内边距 */
  background: linear-gradient(135deg, #F59E0B 0%, #FB923C 100%); /* 橙色渐变 */
  border-radius: 34rpx; /* 17px - 圆角 */
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-hover, 150ms ease);
  flex-shrink: 0;
  box-shadow: 0 6rpx 20rpx rgba(245, 158, 11, 0.3);
  border: none;
}

.search-btn-primary:hover {
  background: linear-gradient(135deg, #E67000 0%, #FF9020 100%);
  transform: translateY(-2rpx) scale(1.05);
  box-shadow: 0 8rpx 28rpx rgba(245, 158, 11, 0.4);
}

.search-btn-primary:active {
  transform: translateY(0) scale(0.98);
}

.search-btn-text {
  font-size: 32rpx; /* 16px */
  font-weight: 600;
  color: #FFFFFF;
  line-height: 1;
  letter-spacing: 1rpx;
}

/* 优化：语音搜索按钮 - 内联样式（在搜索框内部）*/
.voice-search-btn-inline {
  position: relative;
  width: 48rpx; /* 24px - 较小尺寸 */
  height: 48rpx;
  background: transparent;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-hover, 150ms ease);
  flex-shrink: 0;
  overflow: visible;
}

.voice-search-btn-inline:hover {
  background: rgba(245, 158, 11, 0.1);
  transform: scale(1.1);
}

.voice-search-btn-inline:active {
  transform: scale(0.95);
}

/* 语音激活状态 */
.voice-search-btn-inline.voice-active {
  background: rgba(245, 158, 11, 0.2);
  animation: voice-pulse-inline 1.5s ease-in-out infinite;
}

/* 优化：语音图标 - 内联样式 */
.voice-icon-inline {
  width: 28rpx; /* 14px */
  height: 28rpx;
  color: var(--cl-warning, #F59E0B); /* 橙色 */
  position: relative;
  z-index: 2;
}

.voice-search-btn-inline.voice-active .voice-icon-inline {
  color: #FF4D4F; /* 激活时变红 */
}

/* 语音波纹效果 - 内联样式 */
.voice-ripple-inline {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  border: 2rpx solid #F59E0B;
  opacity: 0.6;
  animation: ripple-inline 1.5s ease-out infinite;
}

.voice-ripple-inline.voice-ripple-2 {
  animation-delay: 0.75s;
}

/* 语音脉冲动画 - 内联样式 */
@keyframes voice-pulse-inline {
  0%, 100% {
    transform: scale(1);
    background: rgba(245, 158, 11, 0.2);
  }
  50% {
    transform: scale(1.1);
    background: rgba(245, 158, 11, 0.3);
  }
}

/* 波纹扩散动画 - 内联样式 */
@keyframes ripple-inline {
  0% {
    width: 40rpx;
    height: 40rpx;
    opacity: 0.6;
  }
  100% {
    width: 80rpx;
    height: 80rpx;
    opacity: 0;
  }
}

/* 优化：搜索框下方提示文字 - 更精简轻盈 */
.search-tips {
  display: flex;
  align-items: center;
  gap: 12rpx; /* 6px - 优化：减小间距，更紧凑 */
  font-size: 24rpx; /* 12px - 优化：从28rpx减小到24rpx */
  color: var(--cl-gray-500, #94A3B8); /* 降低灰度，减弱视觉权重 */
  line-height: 40rpx; /* 20px - 优化：减小行高 */
  /* 优化：添加淡入动画 */
  opacity: 0;
  animation: fadeInUp 0.6s ease-out 0.3s forwards; /* 延迟 0.3s 后淡入 */
}

.search-tip-item {
  font-weight: 400;
  color: var(--cl-gray-500, #94A3B8);
  /* 优化：添加轻微透明度 */
  opacity: 0.85;
}

.search-tip-divider {
  color: var(--cl-gray-400, #CBD5E1);
  font-weight: 300;
  opacity: 0.6; /* 优化：进一步降低透明度 */
}

/* 淡入向上动画 */
@keyframes fadeInUp {
  0% {
    opacity: 0;
    transform: translateY(20rpx); /* 从下方 10px 处开始 */
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}



/* ========== 七、插画元素层 ========== */
.illustration-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  pointer-events: none;
}

/* 企业级优化：左侧校园建筑图标 - 透明度降至 8-12% */
.campus-building {
  position: absolute;
  left: 5%;
  bottom: 12%;
  width: 120rpx;
  height: 160rpx;
  opacity: 0.10; /* 降至 10%，增强融合度 */
  animation: float-building 6s ease-in-out infinite;
  filter: blur(1rpx); /* 添加轻微模糊，使之不显突兀 */
}

/* 企业级优化：右侧主体插画 - 手持手机的学生 - 透明度降至 8-12% */
.student-illustration {
  position: absolute;
  right: 6%;
  bottom: 8%;
  width: 300rpx;
  height: 300rpx;
  opacity: 0.12; /* 降至 12%，增强融合度 */
  animation: float-student 4s ease-in-out infinite;
  filter: blur(1rpx); /* 添加轻微模糊，使之不显突兀 */
}

/* 企业级优化：装饰元素 - 透明度降至 6-8% */
.decoration-element {
  position: absolute;
  opacity: 0.08; /* 降至 8%，增强融合度 */
  animation: float-decoration 5s ease-in-out infinite;
}

.decoration-book {
  left: 15%;
  top: 12%;
  width: 100rpx;
  height: 100rpx;
  animation-delay: 0s;
}

.decoration-bulb {
  right: 20%;
  top: 8%;
  width: 100rpx;
  height: 100rpx;
  animation-delay: 1s;
}

.decoration-pencil {
  left: 35%;
  bottom: 18%;
  width: 80rpx;
  height: 80rpx;
  animation-delay: 2s;
}

.decoration-cap {
  right: 35%;
  bottom: 22%;
  width: 90rpx;
  height: 90rpx;
  animation-delay: 1.5s;
}

/* ========== 八、动画效果 ========== */
/* 建筑浮动动画 */
@keyframes float-building {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-12rpx);
  }
}

/* 学生插画浮动动画 */
@keyframes float-student {
  0%, 100% {
    transform: translateY(0) scale(1);
  }
  50% {
    transform: translateY(-16rpx) scale(1.02);
  }
}

/* 装饰元素浮动动画 */
@keyframes float-decoration {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-10rpx) rotate(5deg);
  }
}

/* ========== 响应式设计 - 三级断点 ========== */

/* 通用工具类：显示/隐藏控制 */
.desktop-tablet-only {
  display: flex !important;
}

.mobile-only {
  display: none !important;
}

/* ========== 1. 桌面端（≥1024px）- 默认样式 ========== */
/* 主要样式已在上方定义，这里是桌面端的默认状态 */

.brand-slogan-full {
  display: inline; /* 显示完整副标题 */
}

.brand-slogan-short {
  display: none; /* 隐藏简化副标题 */
}

/* ========== 2. 平板端（768px - 1024px）========== */
@media (min-width: 768px) and (max-width: 1024px) {
  /* 顶部容器高度保持不变 */
  .top-focus-bar {
    height: 640rpx; /* 320px */
  }

  /* 品牌区域：简化副标题 */
  .brand-slogan-full {
    display: none !important; /* 隐藏完整副标题 */
  }

  .brand-slogan-short {
    display: inline !important; /* 显示简化副标题 */
  }

  .brand-logo {
    font-size: 44rpx; /* 22px - 略微缩小 */
  }

  .brand-slogan {
    font-size: 22rpx; /* 11px */
  }

  /* 操作按钮：缩小尺寸 */
  .action-btn {
    padding: 14rpx 28rpx; /* 缩小内边距 */
  }

  .action-btn-text {
    font-size: 28rpx; /* 14px */
  }

  .check-in-btn {
    height: 60rpx; /* 30px */
    padding: 0 22rpx;
  }

  .check-in-icon,
  .check-in-text {
    font-size: 26rpx; /* 13px */
  }

  /* 搜索栏：增加宽度占比 */
  .search-section {
    width: 85%; /* 平板端占据 85% 宽度 */
  }

  .search-box {
    height: 88rpx; /* 44px - 略微缩小 */
  }

  .search-input {
    font-size: 30rpx; /* 15px */
  }

  .search-btn-primary {
    height: 64rpx; /* 32px */
    padding: 0 36rpx;
  }

  .search-btn-text {
    font-size: 30rpx; /* 15px */
  }
}

/* ========== 3. 移动端（<768px）========== */
@media (max-width: 768px) {
  /* 隐藏/显示控制反转 */
  .desktop-tablet-only {
    display: none !important;
  }

  .mobile-only {
    display: flex !important;
  }

  /* 顶部容器高度调整 */
  .top-focus-bar {
    height: 180rpx; /* 90px - 优化：进一步缩减 */
  }

  /* 内容容器调整 */
  .focus-container {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: stretch;
    gap: 16rpx; /* 两行之间的间距 */
    padding: 20rpx 32rpx; /* 增加左右内边距 */
  }

  /* 第一行：品牌 + 汉堡菜单 */
  .top-nav-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: auto;
    padding-top: 0;
  }

  /* 品牌区域 */
  .brand-section {
    display: flex;
    flex-direction: row;
    align-items: center;
    gap: 0;
    padding-left: 12rpx; /* 保留左侧分割线空间 */
  }

  .brand-logo {
    font-size: 36rpx; /* 18px */
  }

  /* 隐藏所有副标题 */
  .brand-slogan {
    display: none !important;
  }

  /* 汉堡菜单图标 */
  .mobile-menu-icon {
    width: 88rpx; /* 44px - 触摸规范 */
    height: 88rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 10rpx;
    cursor: pointer;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.9);
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;
  }

  .mobile-menu-icon:active {
    transform: scale(0.95);
    background: rgba(255, 255, 255, 1);
  }

  .hamburger-line {
    width: 40rpx; /* 20px */
    height: 4rpx; /* 2px */
    background: var(--cl-primary, #2563EB);
    border-radius: 2rpx;
    transition: all 0.3s ease;
  }

  /* 搜索区域 */
  .main-content-area {
    flex: 1;
    margin-top: 0;
  }

  .search-section {
    width: 100%; /* 移动端占满宽度 */
  }

  .search-box-wrapper {
    gap: 0; /* 移除提示文字间距 */
  }

  .search-box {
    height: 80rpx; /* 40px */
    padding: 0 20rpx;
    gap: 12rpx;
  }

  .search-icon {
    font-size: 32rpx; /* 16px */
  }

  .search-input {
    font-size: 28rpx; /* 14px */
  }

  .search-input::placeholder {
    font-size: 28rpx;
  }

  .voice-search-btn-inline {
    width: 52rpx; /* 26px */
    height: 52rpx;
  }

  .voice-icon-inline {
    width: 32rpx; /* 16px */
    height: 32rpx;
  }

  .search-btn-primary {
    height: 60rpx; /* 30px */
    padding: 0 32rpx;
  }

  .search-btn-text {
    font-size: 28rpx; /* 14px */
  }

  /* 插画元素 - 移动端淡化 */
  .illustration-layer {
    height: 180rpx;
    opacity: 0.5; /* 整体降低透明度 */
  }

  .campus-building {
    width: 80rpx; /* 40px */
    height: 100rpx; /* 50px */
    left: 3%;
    bottom: 15%;
  }

  .student-illustration {
    width: 150rpx; /* 75px */
    height: 150rpx;
    right: 3%;
    bottom: 10%;
  }

  .decoration-element {
    display: none; /* 移动端完全隐藏装饰元素 */
  }

  /* 背景装饰缩小 */
  .bg-circle-1 {
    width: 300rpx;
    height: 300rpx;
  }

  .bg-circle-2 {
    width: 250rpx;
    height: 250rpx;
  }
}

/* ========== 移动端折叠菜单样式 ========== */
.mobile-menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6); /* 优化：提高遮罩不透明度，增强菜单与主页面区分 */
  backdrop-filter: blur(8px); /* 优化：增加模糊度 */
  -webkit-backdrop-filter: blur(8px);
  z-index: 99999; /* 关键修复：大幅提高层级到99999，确保覆盖所有页面内容（包括"为你推荐"卡片）*/
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  animation: fadeIn 0.3s ease-out;
  /* 关键修复：阻止触摸穿透 */
  pointer-events: auto;
  touch-action: none;
}

.mobile-menu-panel {
  width: 560rpx; /* 280px - 70% 屏幕宽度 */
  max-width: 85%;
  height: 100vh;
  background: #FFFFFF;
  box-shadow: -8rpx 0 32rpx rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  animation: slideInRight 0.3s ease-out;
  /* 关键修复：确保菜单独立定位，不影响页面布局 */
  position: relative;
  z-index: 100000; /* 关键修复：菜单面板层级设为100000，高于遮罩层 */
  /* 关键修复：启用硬件加速，优化滑动性能 */
  transform: translateZ(0);
  will-change: transform;
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

/* 菜单头部 */
.mobile-menu-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 40rpx 32rpx;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
}

.mobile-menu-title {
  font-size: 36rpx; /* 18px */
  font-weight: 700;
  color: #1D2129;
}

.mobile-menu-close {
  width: 88rpx; /* 44px - 优化：扩大触摸区域，符合触摸规范 */
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.05);
  transition: all 0.2s ease;
}

.mobile-menu-close:active {
  background: rgba(0, 0, 0, 0.12); /* 优化：增强按下反馈 */
  transform: scale(0.92);
}

.close-icon {
  font-size: 56rpx; /* 28px - 优化：增大图标尺寸 */
  font-weight: 300;
  color: #4E5969;
  line-height: 1;
}

/* 菜单内容 */
.mobile-menu-content {
  flex: 1;
  padding: 32rpx 0;
  overflow-y: auto;
}

/* 用户信息卡片 */
.mobile-user-card {
  display: flex;
  align-items: center;
  gap: 28rpx; /* 优化：增加头像与信息的间距 */
  padding: 36rpx; /* 优化：增加内边距，增强呼吸感 */
  margin: 0 24rpx 32rpx;
  background: linear-gradient(135deg, #E8F0FF 0%, #F5F8FF 100%);
  border-radius: 24rpx;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2rpx 12rpx rgba(37, 99, 235, 0.08); /* 优化：添加轻微阴影 */
}

.mobile-user-card:active {
  transform: scale(0.98);
  box-shadow: 0 2rpx 8rpx rgba(37, 99, 235, 0.06); /* 优化：按下时减弱阴影 */
}

.mobile-user-avatar {
  width: 96rpx; /* 48px */
  height: 96rpx;
  border-radius: 50%;
  flex-shrink: 0;
  border: 4rpx solid rgba(255, 255, 255, 0.8); /* 优化：添加白色边框 */
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08); /* 优化：添加阴影，增强层次感 */
}

.mobile-user-avatar-default {
  background: var(--cl-primary, #2563EB);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-text {
  font-size: 40rpx; /* 20px */
  font-weight: 700;
  color: #FFFFFF;
}

.mobile-user-info {
  display: flex;
  flex-direction: column;
  gap: 12rpx; /* 优化：增加昵称与邮箱的间距，从 8rpx 调整为 12rpx */
  flex: 1;
  min-width: 0;
}

.mobile-user-name {
  font-size: 34rpx; /* 17px - 优化：略微增大字号 */
  font-weight: 600;
  color: #1D2129;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.3; /* 优化：增加行高 */
}

.mobile-user-email {
  font-size: 26rpx; /* 13px - 优化：略微增大字号 */
  color: #86909C;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4; /* 优化：增加行高 */
}

/* 菜单项 */
.mobile-menu-item {
  display: flex;
  align-items: center;
  gap: 24rpx;
  min-height: 96rpx; /* 48px - 优化：固定最小高度，确保触摸区域充足 */
  padding: 24rpx 48rpx; /* 优化：调整内边距，使高度为 48px */
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
}

.mobile-menu-item:active {
  background: rgba(0, 0, 0, 0.06); /* 优化：增强按下反馈 */
}

.mobile-menu-item-icon {
  font-size: 40rpx; /* 20px */
  line-height: 1;
  flex-shrink: 0;
  width: 40rpx; /* 优化：固定图标宽度，保证对齐 */
  text-align: center;
}

.mobile-menu-item-text {
  font-size: 30rpx; /* 15px */
  font-weight: 500;
  color: #1D2129;
  flex: 1;
  line-height: 1.5; /* 优化：增加行高，垂直居中对齐 */
}

.mobile-menu-item-badge {
  font-size: 24rpx; /* 12px */
  font-weight: 600;
  color: #10B981;
  padding: 6rpx 16rpx; /* 优化：增加内边距，更易识别 */
  background: rgba(16, 185, 129, 0.12); /* 优化：增加背景不透明度 */
  border-radius: 14rpx;
}

/* 优化："退出登录"样式 - 降低冲击感 */
.mobile-menu-item-danger {
  background: rgba(255, 77, 79, 0.04); /* 添加浅红色背景 */
  margin: 16rpx 24rpx 0; /* 增加外边距，与其他选项区分 */
  border-radius: 16rpx; /* 添加圆角 */
}

.mobile-menu-item-danger:active {
  background: rgba(255, 77, 79, 0.08); /* 优化：按下时加深背景 */
}

.mobile-menu-item-danger .mobile-menu-item-text {
  color: #FF4D4F;
}

.mobile-menu-item-danger .mobile-menu-item-icon {
  opacity: 0.85; /* 优化：略微提高不透明度 */
}
</style>

