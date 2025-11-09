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
      <!-- 第一行：品牌标识区 + 用户头像 -->
      <view class="top-row">
        <!-- 左侧：品牌标识 -->
        <view class="brand-section">
          <!-- 校园标识 + 校徽 -->
          <view class="school-identity">
            <image
              v-if="schoolInfo?.logoUrl"
              :src="schoolInfo.logoUrl"
              class="school-logo-img"
              mode="aspectFit"
            />
            <view v-else class="school-badge">🏛️</view>
            <text class="school-name">{{ schoolInfo?.schoolName || '未设置学校' }}</text>
          </view>
          <!-- CampusLink Logo + Slogan -->
          <view class="brand-logo-wrapper">
            <view class="logo-row">
              <text class="brand-logo">CampusLink</text>
              <text class="campus-muule">校园 muule</text>
            </view>
            <text class="brand-slogan">{{ userCount }}万大学生的互助学习圈</text>
          </view>
        </view>

        <!-- 右侧：仅保留用户头像 -->
        <view class="user-avatar-section" @click="handleUserClick">
          <image
            v-if="userInfo?.avatar"
            :src="userInfo.avatar"
            class="user-avatar"
            mode="aspectFill"
          />
          <view v-else class="user-avatar-placeholder">
            <text class="avatar-text">{{ userInfo?.nickname?.charAt(0) || '?' }}</text>
          </view>
        </view>
      </view>

      <!-- 第二行：搜索栏（独占一行） -->
      <view class="search-section">
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
          <!-- 搜索按钮 -->
          <view class="search-btn-blue" @click="handleSearch">
            <text class="search-btn-text">搜索</text>
          </view>
          <!-- 语音搜索按钮 -->
          <view
            class="voice-search-btn"
            :class="{ 'voice-active': isVoiceActive }"
            @click="handleVoiceSearch"
            @touchstart="handleVoiceTouchStart"
            @touchend="handleVoiceTouchEnd"
          >
            <svg
              class="voice-icon"
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <!-- 麦克风图标 -->
              <path
                d="M12 14C13.66 14 15 12.66 15 11V5C15 3.34 13.66 2 12 2C10.34 2 9 3.34 9 5V11C9 12.66 10.34 14 12 14Z"
                fill="white"
              />
              <path
                d="M17 11C17 13.76 14.76 16 12 16C9.24 16 7 13.76 7 11H5C5 14.53 7.61 17.43 11 17.92V21H13V17.92C16.39 17.43 19 14.53 19 11H17Z"
                fill="white"
              />
            </svg>
            <!-- 语音波纹效果 -->
            <view v-if="isVoiceActive" class="voice-ripple"></view>
            <view v-if="isVoiceActive" class="voice-ripple voice-ripple-2"></view>
          </view>
        </view>
      </view>
    </view>

    <!-- 插画元素层 -->
    <view class="illustration-layer">
      <!-- 左侧校园建筑图标 -->
      <view class="campus-building">
        <CampusBuilding :width="120" :height="160" color="#FF7D00" />
      </view>

      <!-- 右侧主体插画 - 手持手机的学生 -->
      <view class="student-illustration">
        <StudentWithPhone :width="300" :height="300" />
      </view>

      <!-- 装饰元素 -->
      <view class="decoration-element decoration-book">
        <DecorativeElements type="book" color="#409EFF" :width="100" :height="100" />
      </view>
      <view class="decoration-element decoration-bulb">
        <DecorativeElements type="bulb" color="#FF7D00" :width="100" :height="100" />
      </view>
      <view class="decoration-element decoration-pencil">
        <DecorativeElements type="pencil" color="#52C41A" :width="80" :height="80" />
      </view>
      <view class="decoration-element decoration-cap">
        <DecorativeElements type="graduation-cap" color="#409EFF" :width="90" :height="90" />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import CampusBuilding from '@/components/illustrations/CampusBuilding.vue'
import StudentWithPhone from '@/components/illustrations/StudentWithPhone.vue'
import DecorativeElements from '@/components/illustrations/DecorativeElements.vue'

// Props & Emits
const emit = defineEmits<{
  search: [keyword: string]
  upload: []
  aiAnswer: []
}>()

// Stores
const userStore = useUserStore()
const appStore = useAppStore()

// 数据
const searchKeyword = ref('')
const showHotTags = ref(false)
const hotTags = ref(['高数课件', '四六级真题', '数据结构', '考研资料'])
const userCount = ref(100) // 默认100万，可从后端获取
const isVoiceActive = ref(false) // 语音搜索激活状态

// 计算属性 - 学校信息
const schoolInfo = computed(() => {
  // 优先从用户信息获取学校名称
  if (userStore.userInfo?.schoolName) {
    return {
      schoolName: userStore.userInfo.schoolName,
      logoUrl: '', // 可以从后端获取学校 logo
    }
  }
  // 其次从应用状态获取
  const currentSchool = appStore.getCurrentSchool()
  if (currentSchool) {
    return {
      schoolName: currentSchool.schoolName,
      logoUrl: '', // 可以从后端获取学校 logo
    }
  }
  // 默认值
  return {
    schoolName: '未设置学校',
    logoUrl: '',
  }
})

/**
 * 初始化
 */
onMounted(() => {
  // 初始化用户信息
  userStore.init()
  // 可以在这里调用 API 获取用户数量
  // getUserCount().then(count => userCount.value = count)
})

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
 * 上传资料
 */
const handleUpload = () => {
  emit('upload')
}

/**
 * 点击个人信息
 */
const handleUserClick = () => {
  if (!userStore.isLoggedIn) {
    // 未登录，跳转到登录页
    uni.navigateTo({
      url: '/pages/auth/login',
    })
  } else {
    // 已登录，跳转到个人中心
    uni.navigateTo({
      url: '/pages/user/profile',
    })
  }
}

// 组件挂载时初始化用户信息
onMounted(() => {
  userStore.init()
})
</script>

<style scoped lang="scss">
/* ========== 一、顶部背景容器 ========== */
.top-focus-bar {
  position: relative;
  width: 100%;
  height: 440rpx; /* PC端 220px */
  background: linear-gradient(180deg, #1E5FFF 0%, #5A7FFF 100%);
  overflow: hidden;
  z-index: 1;
}

/* ========== 二、背景装饰层 ========== */
.bg-decoration-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  pointer-events: none;
}

/* 装饰圆形 1 - 左上角 */
.bg-circle-1 {
  position: absolute;
  top: -100rpx;
  left: 10%;
  width: 400rpx;
  height: 400rpx;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.15) 0%, transparent 70%);
  border-radius: 50%;
}

/* 装饰圆形 2 - 右下角 */
.bg-circle-2 {
  position: absolute;
  bottom: -80rpx;
  right: 15%;
  width: 300rpx;
  height: 300rpx;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.15) 0%, transparent 70%);
  border-radius: 50%;
}

/* 波浪形装饰 */
.bg-wave {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 80rpx;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50% 50% 0 0 / 100% 100% 0 0;
}

/* ========== 三、内容容器 ========== */
.focus-container {
  position: relative;
  max-width: 2400rpx; /* 1200px */
  height: 100%;
  margin: 0 auto;
  padding: 0 60rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 40rpx;
  z-index: 2;
}

/* ========== 四、品牌标识区 ========== */
.brand-section {
  display: flex;
  align-items: center;
  gap: 32rpx;
  flex-shrink: 0;
}

/* 校园身份标识 */
.school-identity {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 12rpx 24rpx;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 24rpx;
  backdrop-filter: blur(8rpx);
}

.school-logo-img {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  background: white;
}

.school-badge {
  font-size: 32rpx;
  line-height: 1;
}

.school-name {
  font-size: 28rpx; /* 14px */
  font-weight: 600;
  color: white;
  line-height: 1;
  max-width: 200rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* CampusLink Logo + Slogan */
.brand-logo-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.logo-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.brand-logo {
  font-size: 64rpx; /* 32px */
  font-weight: 700;
  color: white;
  line-height: 1;
  letter-spacing: 1rpx;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.15);
}

.campus-muule {
  font-size: 24rpx; /* 12px */
  font-weight: 600;
  color: rgba(255, 255, 255, 0.85);
  padding: 6rpx 12rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12rpx;
  line-height: 1;
}

.brand-slogan {
  font-size: 26rpx; /* 13px */
  font-weight: 500;
  color: rgba(255, 255, 255, 0.95);
  line-height: 1;
  letter-spacing: 0.5rpx;
}

/* ========== 五、搜索栏 ========== */
.search-section {
  flex: 1;
  display: flex;
  justify-content: center;
  max-width: 1400rpx; /* 70% 宽度占比 */
}

.search-box {
  position: relative;
  width: 100%;
  height: 80rpx; /* 40px */
  background: white;
  border-radius: 40rpx; /* 20px */
  display: flex;
  align-items: center;
  padding: 0 24rpx;
  gap: 16rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.1);
  transition: all 0.2s ease;
  border: 2rpx solid transparent;
}

.search-box:hover {
  box-shadow: 0 12rpx 32rpx rgba(30, 95, 255, 0.15);
  transform: translateY(-2rpx);
  border-color: rgba(30, 95, 255, 0.2);
}

.search-box:focus-within {
  box-shadow: 0 12rpx 32rpx rgba(30, 95, 255, 0.2);
  border-color: #1E5FFF;
  transform: translateY(-2rpx);
}

/* 搜索图标 */
.search-icon {
  font-size: 32rpx;
  line-height: 1;
  color: #86909C;
  flex-shrink: 0;
}

/* 搜索输入框 */
.search-input {
  flex: 1;
  height: 100%;
  font-size: 28rpx; /* 14px */
  color: #1D2129;
  border: none;
  outline: none;
  background: transparent;
}

.search-input::placeholder {
  color: #C9CDD4; /* 浅灰色 */
  font-size: 28rpx;
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

/* 蓝色搜索按钮 */
.search-btn-blue {
  padding: 14rpx 32rpx; /* 调整为14rpx，使高度与语音按钮一致（14*2 + 28 = 56rpx） */
  background: #1E5FFF;
  border-radius: 40rpx; /* 20px */
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
  box-shadow: 0 4rpx 12rpx rgba(30, 95, 255, 0.25);
}

.search-btn-blue:hover {
  background: #1650E6;
  transform: scale(1.03);
  box-shadow: 0 6rpx 16rpx rgba(30, 95, 255, 0.35);
}

.search-btn-blue:active {
  transform: scale(0.98);
}

.search-btn-text {
  font-size: 28rpx;
  font-weight: 600;
  color: white;
  line-height: 1;
}

/* 橙色语音搜索按钮 */
.voice-search-btn {
  position: relative;
  width: 56rpx;
  height: 56rpx;
  background: linear-gradient(135deg, #FF7D00 0%, #FFA940 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
  box-shadow: 0 2rpx 8rpx rgba(255, 125, 0, 0.3);
  overflow: visible;
}

.voice-search-btn:hover {
  background: linear-gradient(135deg, #E67000 0%, #FF9020 100%);
  transform: scale(1.08);
  box-shadow: 0 4rpx 16rpx rgba(255, 125, 0, 0.4);
}

.voice-search-btn:active {
  transform: scale(0.92);
}

/* 语音激活状态 */
.voice-search-btn.voice-active {
  background: linear-gradient(135deg, #FF4D4F 0%, #FF7875 100%);
  animation: voice-pulse 1.5s ease-in-out infinite;
}

.voice-icon {
  width: 28rpx;
  height: 28rpx;
  position: relative;
  z-index: 2;
}

/* 语音波纹效果 */
.voice-ripple {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  border: 2rpx solid #FF7D00;
  opacity: 0.6;
  animation: ripple 1.5s ease-out infinite;
}

.voice-ripple-2 {
  animation-delay: 0.75s;
}

/* 语音脉冲动画 */
@keyframes voice-pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 2rpx 8rpx rgba(255, 77, 79, 0.3);
  }
  50% {
    transform: scale(1.1);
    box-shadow: 0 4rpx 20rpx rgba(255, 77, 79, 0.5);
  }
}

/* 波纹扩散动画 */
@keyframes ripple {
  0% {
    width: 56rpx;
    height: 56rpx;
    opacity: 0.6;
  }
  100% {
    width: 120rpx;
    height: 120rpx;
    opacity: 0;
  }
}

/* ========== 六、右侧区域（个人信息 + CTA） ========== */
.right-section {
  display: flex;
  align-items: center;
  gap: 24rpx;
  flex-shrink: 0;
}

/* 个人信息按钮 - 圆形卡片悬浮样式 */
.user-info-btn {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 8rpx 20rpx;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10rpx);
  border-radius: 40rpx;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1rpx solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.user-info-btn:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-4rpx);
  box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.user-info-btn:active {
  transform: translateY(-2rpx);
}

/* 用户头像 */
.user-avatar {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  border: 2rpx solid rgba(255, 255, 255, 0.3);
  flex-shrink: 0;
}

.user-avatar-placeholder {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #409EFF 0%, #5DAFFF 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2rpx solid rgba(255, 255, 255, 0.3);
  flex-shrink: 0;
}

.avatar-text {
  font-size: 28rpx;
  font-weight: 600;
  color: white;
  line-height: 1;
}

/* 用户文字信息 */
.user-text {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.user-nickname {
  font-size: 28rpx;
  font-weight: 600;
  color: white;
  line-height: 1.2;
  max-width: 120rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-points {
  font-size: 22rpx;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.85);
  line-height: 1;
}

.cta-btn-primary {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  width: 240rpx; /* 120px */
  height: 88rpx; /* 44px */
  background: linear-gradient(90deg, #FFB64B 0%, #FF8C00 100%);
  border-radius: 48rpx; /* 24px */
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 6rpx 20rpx rgba(255, 140, 0, 0.35);
}

.cta-btn-primary:hover {
  background: linear-gradient(90deg, #FFA940 0%, #E67000 100%);
  transform: scale(1.03);
  box-shadow: 0 8rpx 28rpx rgba(255, 140, 0, 0.45);
}

.cta-btn-primary:active {
  transform: scale(0.98);
}

.cta-btn-text {
  font-size: 32rpx; /* 16px */
  font-weight: 700;
  color: white;
  line-height: 1;
}

.cta-arrow {
  font-size: 32rpx;
  font-weight: 700;
  color: white;
  line-height: 1;
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

/* 左侧校园建筑图标 */
.campus-building {
  position: absolute;
  left: 5%;
  bottom: 12%;
  width: 120rpx;
  height: 160rpx;
  opacity: 0.85;
  animation: float-building 6s ease-in-out infinite;
}

/* 右侧主体插画 - 手持手机的学生 */
.student-illustration {
  position: absolute;
  right: 6%;
  bottom: 8%;
  width: 300rpx;
  height: 300rpx;
  opacity: 0.9;
  animation: float-student 4s ease-in-out infinite;
}

/* 装饰元素 */
.decoration-element {
  position: absolute;
  opacity: 0.7;
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

/* ========== 九、H5 端适配 - 极简两行布局 + 底部导航栏 ========== */
@media (max-width: 750px) {
  /* 顶部容器高度调整 - 进一步减少 */
  .top-focus-bar {
    height: 240rpx; /* H5端 120px - 极简两行布局 */
  }

  /* 内容容器调整 - 两行布局 */
  .focus-container {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: stretch;
    gap: 10rpx; /* 两行之间的间距 */
    padding: 16rpx 24rpx; /* 减少上下内边距 */
  }

  /* 第一行：品牌 + 用户头像 */
  .top-row {
    display: flex;
    flex-direction: row;
    justify-content: space-between; /* 左右分布 */
    align-items: center;
    gap: 16rpx;
    height: 56rpx; /* 固定高度 */
  }

  /* 品牌区域 - 左对齐 */
  .brand-section {
    display: flex;
    flex-direction: row;
    align-items: center;
    gap: 12rpx;
    flex: 1; /* 占据剩余空间 */
    min-width: 0; /* 允许收缩 */
  }

  /* 隐藏校徽和学校名 */
  .school-identity {
    display: none; /* 完全隐藏校园标识 */
  }

  /* 品牌Logo区域优化 */
  .brand-logo-wrapper {
    display: flex;
    flex-direction: row; /* 横向排列 */
    align-items: center;
    gap: 12rpx;
  }

  .logo-row {
    display: flex;
    align-items: center;
    gap: 12rpx;
  }

  /* 品牌名字号 */
  .brand-logo {
    font-size: 36rpx; /* 18px */
    font-weight: 700;
    line-height: 1.2;
  }

  /* "校园muule"标签 */
  .campus-muule {
    font-size: 20rpx; /* 10px */
    padding: 4rpx 8rpx;
    line-height: 1;
  }

  /* 隐藏副标题 */
  .brand-slogan {
    display: none; /* 隐藏"100万大学生的互助学习圈" */
  }

  /* 用户头像区域 - 右对齐 */
  .user-avatar-section {
    flex-shrink: 0; /* 不收缩 */
    cursor: pointer;
  }

  .user-avatar,
  .user-avatar-placeholder {
    width: 56rpx; /* 28px */
    height: 56rpx;
    border-radius: 50%;
    border: 2rpx solid rgba(255, 255, 255, 0.3);
    transition: all 0.3s ease;
  }

  .user-avatar-section:active .user-avatar,
  .user-avatar-section:active .user-avatar-placeholder {
    transform: scale(0.9);
  }

  .avatar-text {
    font-size: 24rpx;
    color: #ffffff;
    font-weight: 600;
  }

  /* 第二行：搜索框独占 */
  .search-section {
    display: flex;
    align-items: center;
    width: 100%; /* 占满整行 */
  }

  /* 搜索框样式 */
  .search-box {
    height: 60rpx; /* 30px - 搜索框高度 */
    padding: 0 16rpx;
    gap: 10rpx;
    width: 100%; /* 占满整行 */
  }

  .search-icon {
    font-size: 26rpx;
  }

  .search-input {
    font-size: 24rpx;
    flex: 1; /* 占据剩余空间 */
  }

  .search-btn-blue {
    padding: 10rpx 18rpx;
  }

  .search-btn-text {
    font-size: 22rpx;
  }

  .voice-search-btn {
    width: 44rpx;
    height: 44rpx;
  }

  .voice-icon {
    width: 22rpx;
    height: 22rpx;
  }

  .voice-ripple {
    width: 44rpx;
    height: 44rpx;
  }

  /* 插画元素 - H5 端适配 240rpx 高度 */
  .illustration-layer {
    height: 240rpx; /* 与容器高度一致 */
  }

  .campus-building {
    width: 60rpx; /* 30px */
    height: 75rpx; /* 37.5px */
    left: 2%;
    bottom: 12%;
  }

  .student-illustration {
    width: 130rpx; /* 65px */
    height: 130rpx;
    right: 3%;
    bottom: 10%;
  }

  .decoration-element {
    opacity: 0.4; /* 更淡化 */
    transform: scale(0.75); /* 进一步缩小装饰元素 */
  }

  .decoration-book {
    top: 18%;
    left: 18%;
  }

  .decoration-bulb {
    top: 22%;
    right: 22%;
  }

  .decoration-pencil {
    bottom: 28%;
    left: 28%;
  }

  .decoration-cap {
    bottom: 24%;
    right: 18%;
  }

  .decoration-book {
    width: 70rpx;
    height: 70rpx;
    left: 12%;
    top: 10%;
  }

  .decoration-bulb {
    width: 70rpx;
    height: 70rpx;
    right: 18%;
    top: 8%;
  }

  .decoration-pencil {
    width: 60rpx;
    height: 60rpx;
    left: 30%;
    bottom: 15%;
  }

  .decoration-cap {
    width: 65rpx;
    height: 65rpx;
    right: 30%;
    bottom: 18%;
  }

  /* 学校 logo */
  .school-logo-img {
    width: 40rpx;
    height: 40rpx;
  }

  .school-name {
    max-width: 150rpx;
  }

  /* 背景装饰缩小 */
  .bg-circle-1 {
    width: 250rpx;
    height: 250rpx;
  }

  .bg-circle-2 {
    width: 200rpx;
    height: 200rpx;
  }
}
</style>

