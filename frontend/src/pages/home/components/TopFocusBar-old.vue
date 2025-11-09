<template>
  <view class="top-focus-bar">
    <!-- 背景装饰层 -->
    <view class="bg-decoration-layer">
      <!-- 装饰圆形 -->
      <view class="bg-circle bg-circle-1"></view>
      <view class="bg-circle bg-circle-2"></view>
      <view class="bg-wave"></view>
    </view>

    <!-- 内容容器 - 单行横向布局 -->
    <view class="focus-container">
      <!-- Logo -->
      <view class="logo-section">
        <view class="logo-icon">🎓</view>
        <text class="logo-text">校园Link</text>
      </view>

      <!-- 搜索框 -->
      <view class="search-section">
        <view class="search-box">
          <text class="search-icon">🔍</text>
          <input
            class="search-input"
            type="text"
            v-model="searchKeyword"
            placeholder="请输入搜索栏"
            @confirm="handleSearch"
          />
        </view>
      </view>

      <!-- 聚焦按钮 -->
      <view class="focus-btn" @click="handleUpload">
        <text class="focus-btn-text">聚焦</text>
      </view>

      <!-- 用户头像 -->
      <view class="user-avatar-btn" @click="handleUserClick">
        <image
          v-if="userInfo?.avatar"
          :src="userInfo.avatar"
          class="avatar-img"
          mode="aspectFill"
        />
        <view v-else class="avatar-placeholder">
          <text class="avatar-initial">{{ userInfo?.nickname?.charAt(0) || 'U' }}</text>
        </view>
      </view>

      <!-- 右侧图标组 -->
      <view class="icon-group">
        <view class="icon-btn">
          <text class="icon">🔄</text>
        </view>
        <view class="icon-btn">
          <text class="icon">🔔</text>
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
 * 上传资料/聚焦按钮
 */
const handleUpload = () => {
  emit('upload')
}

/**
 * 点击用户头像
 */
const handleUserClick = () => {
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/auth/login' })
  } else {
    uni.navigateTo({ url: '/pages/user/profile' })
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
  background: linear-gradient(90deg, #2F80ED 0%, #409EFF 50%, #5DAFFF 100%);
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

/* ========== 三、内容容器（单行横向布局） ========== */
.focus-container {
  position: relative;
  max-width: 2400rpx;
  height: 100%;
  margin: 0 auto;
  padding: 0 60rpx;
  display: flex;
  align-items: center;
  gap: 32rpx;
  z-index: 2;
}

/* ========== 四、Logo区域 ========== */
.logo-section {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-shrink: 0;
}

.logo-icon {
  font-size: 48rpx;
  line-height: 1;
}

.logo-text {
  font-size: 36rpx;
  font-weight: 700;
  color: white;
  line-height: 1;
  white-space: nowrap;
}

/* ========== 五、搜索区域 ========== */
.search-section {
  flex: 1;
  display: flex;
  max-width: 800rpx;
}

.search-box {
  position: relative;
  width: 100%;
  height: 80rpx;
  background: white;
  border-radius: 40rpx;
  display: flex;
  align-items: center;
  padding: 0 24rpx;
  gap: 16rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
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
  color: #409EFF;
  padding: 8rpx 16rpx;
  background: #E6F4FF;
  border-radius: 16rpx;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
}

.hot-tag:hover {
  background: #409EFF;
  color: white;
  transform: scale(1.05);
}

.hot-tag:active {
  transform: scale(0.95);
}

/* 蓝色搜索按钮 */
.search-btn-blue {
  padding: 14rpx 32rpx; /* 调整为14rpx，使高度与语音按钮一致（14*2 + 28 = 56rpx） */
  background: #409EFF;
  border-radius: 40rpx; /* 20px */
  cursor: pointer;
  transition: all 0.3s;
  flex-shrink: 0;
}

.search-btn-blue:hover {
  background: #2F80ED;
  transform: scale(1.02);
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

/* ========== 六、核心CTA按钮（放大尺寸，活力橙） ========== */
.cta-btn-primary {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  padding: 20rpx 48rpx;
  min-width: 280rpx; /* 140px - 放大尺寸 */
  height: 96rpx; /* 48px - 与搜索框同高 */
  background: linear-gradient(135deg, #FF7D00 0%, #FFA940 100%);
  border-radius: 48rpx; /* 24px */
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 6rpx 20rpx rgba(255, 125, 0, 0.4);
  flex-shrink: 0;
}

.cta-btn-primary:hover {
  background: linear-gradient(135deg, #E67000 0%, #FF9020 100%);
  transform: translateY(-4rpx) scale(1.05);
  box-shadow: 0 10rpx 28rpx rgba(255, 125, 0, 0.5);
}

.cta-btn-primary:active {
  transform: translateY(-2rpx) scale(1.02);
}

.cta-btn-text {
  font-size: 36rpx; /* 18px - 放大字体 */
  font-weight: 700;
  color: white;
  line-height: 1;
  letter-spacing: 1rpx;
}

.cta-arrow {
  font-size: 36rpx;
  font-weight: 700;
  color: white;
  line-height: 1;
  transition: transform 0.3s;
}

.cta-btn-primary:hover .cta-arrow {
  transform: translateX(4rpx);
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

/* ========== 九、H5 端适配 ========== */
@media (max-width: 750px) {
  /* 顶部容器高度调整 */
  .top-focus-bar {
    height: 360rpx; /* H5端 180px */
  }

  /* 内容容器调整 */
  .focus-container {
    padding: 24rpx 32rpx;
    gap: 24rpx;
  }

  /* 第一行：品牌 + CTA */
  .top-row {
    flex-direction: column;
    align-items: stretch;
    gap: 20rpx;
  }

  /* 品牌区域 - H5 端简化显示 */
  .brand-section {
    flex-direction: row;
    justify-content: center;
    gap: 20rpx;
  }

  .school-logo {
    display: none; /* H5 端隐藏校园标识 */
  }

  .brand-logo {
    font-size: 48rpx; /* 24px */
  }

  .brand-subtitle {
    display: none; /* H5 端隐藏副标题 */
  }

  /* 搜索区域 */
  .search-section {
    max-width: 100%;
  }

  .search-box {
    height: 72rpx; /* 36px */
    padding: 0 20rpx;
    gap: 12rpx;
  }

  .search-icon {
    font-size: 28rpx;
  }

  .search-input {
    font-size: 26rpx;
  }

  .search-btn-blue {
    padding: 11rpx 24rpx; /* 调整为11rpx，使高度与语音按钮一致（11*2 + 26 = 48rpx） */
  }

  .search-btn-text {
    font-size: 26rpx;
  }

  .voice-search-btn {
    width: 48rpx;
    height: 48rpx;
  }

  .voice-icon {
    width: 24rpx;
    height: 24rpx;
  }

  .voice-ripple {
    width: 48rpx;
    height: 48rpx;
  }

  /* CTA 按钮 - H5 端调整 */
  .cta-btn-primary {
    width: 100%;
    min-width: auto;
    height: 80rpx; /* 40px */
    padding: 16rpx 32rpx;
  }

  .cta-btn-text,
  .cta-arrow {
    font-size: 30rpx; /* 15px */
  }

  /* 插画元素 - H5 端缩小 */
  .campus-building {
    width: 80rpx;
    height: 100rpx;
    left: 3%;
    bottom: 10%;
  }

  .student-illustration {
    width: 180rpx;
    height: 180rpx;
    right: 4%;
    bottom: 8%;
  }

  .decoration-element {
    opacity: 0.5;
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

