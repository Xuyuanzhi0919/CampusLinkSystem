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
          <text class="brand-slogan">校园互助学习圈</text>
        </view>

        <!-- 右侧：操作按钮 -->
        <view class="action-buttons">
          <view class="action-btn" @click="handleLogin">
            <text class="action-btn-text">登录</text>
          </view>
          <view class="action-btn action-btn-primary" @click="handleRegister">
            <text class="action-btn-text">注册</text>
          </view>
          <view class="action-btn action-btn-publish" @click="handlePublish">
            <text class="action-btn-icon">✨</text>
            <text class="action-btn-text">发布</text>
          </view>
        </view>
      </view>

      <!-- 第二行：主内容区（左右分栏）-->
      <view class="main-content-area">
        <!-- 左侧：搜索栏 -->
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

        <!-- 右侧：快捷入口卡片 -->
        <view class="quick-entry-cards">
          <view
            v-for="item in quickEntries"
            :key="item.id"
            class="quick-card"
            :class="'quick-card-' + item.theme"
            @click="handleQuickEntry(item)"
          >
            <text class="quick-icon">{{ item.icon }}</text>
            <view class="quick-info">
              <text class="quick-title">{{ item.title }}</text>
              <text class="quick-count">{{ item.count }}</text>
            </view>
            <text class="quick-arrow">→</text>
          </view>
        </view>
      </view>

      <!-- 第三行：底部数据栏 -->
      <view class="bottom-stats-bar">
        <view class="stat-item">
          <text class="stat-icon">👥</text>
          <text class="stat-text">{{ userCount }}万用户</text>
        </view>
        <text class="stat-divider">·</text>
        <view class="stat-item">
          <text class="stat-icon">📚</text>
          <text class="stat-text">{{ resourceCount }}万资源</text>
        </view>
        <text class="stat-divider">·</text>
        <view class="stat-item">
          <text class="stat-icon">⏰</text>
          <text class="stat-text">24小时在线答疑</text>
        </view>
      </view>
    </view>

    <!-- 插画元素层（保留但降低透明度）-->
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
import { ref } from 'vue'
import CampusBuilding from '@/components/illustrations/CampusBuilding.vue'
import StudentWithPhone from '@/components/illustrations/StudentWithPhone.vue'
import DecorativeElements from '@/components/illustrations/DecorativeElements.vue'

// Props & Emits
const emit = defineEmits<{
  search: [keyword: string]
  upload: []
  aiAnswer: []
}>()

// 数据
const searchKeyword = ref('')
const showHotTags = ref(false)
const hotTags = ref(['高数课件', '四六级真题', '数据结构', '考研资料'])
const userCount = ref(100) // 默认100万，可从后端获取
const resourceCount = ref(500) // 默认500万资源
const isVoiceActive = ref(false) // 语音搜索激活状态

// 快捷入口数据
interface QuickEntry {
  id: number
  icon: string
  title: string
  count: string
  theme: string
  path: string
}

const quickEntries = ref<QuickEntry[]>([
  { id: 1, icon: '📚', title: '今日推荐', count: '128 条新内容', theme: 'blue', path: '/pages/resource/index' },
  { id: 2, icon: '💡', title: '热门问答', count: '56 个待解答', theme: 'orange', path: '/pages/question/index' },
  { id: 3, icon: '🎯', title: '紧急任务', count: '23 个高悬赏', theme: 'green', path: '/pages/task/index' }
])

/**
 * 登录
 */
const handleLogin = () => {
  uni.navigateTo({ url: '/pages/auth/login' })
}

/**
 * 注册
 */
const handleRegister = () => {
  uni.navigateTo({ url: '/pages/auth/register' })
}

/**
 * 发布
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
 * 快捷入口点击
 */
const handleQuickEntry = (item: QuickEntry) => {
  uni.navigateTo({ url: item.path })
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


</script>

<style scoped lang="scss">
/* ========== 一、顶部背景容器（方案 B：左右分栏式）========== */
.top-focus-bar {
  position: relative;
  width: 100%;
  height: 480rpx; /* 240px - 方案 B 标准高度 */
  /* 浅蓝渐变（非饱和亮蓝）*/
  background: linear-gradient(180deg, #EFF6FF 0%, #DBEAFE 100%);
  overflow: hidden;
  z-index: 1;

  /* 下边界加 1px 分隔线 */
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 1px;
    background: rgba(0, 0, 0, 0.06);
  }
}

/* ========== 二、背景装饰层（文档优化：插画弱化透明到 12-16%）========== */
.bg-decoration-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  pointer-events: none;
}

/* 装饰圆形 1 - 左上角（透明度降低到 12%）*/
.bg-circle-1 {
  position: absolute;
  top: -100rpx;
  left: 10%;
  width: 400rpx;
  height: 400rpx;
  background: radial-gradient(circle, rgba(37, 99, 235, 0.12) 0%, transparent 70%);
  border-radius: 50%;
}

/* 装饰圆形 2 - 右下角（透明度降低到 14%）*/
.bg-circle-2 {
  position: absolute;
  bottom: -80rpx;
  right: 15%;
  width: 300rpx;
  height: 300rpx;
  background: radial-gradient(circle, rgba(37, 99, 235, 0.14) 0%, transparent 70%);
  border-radius: 50%;
}

/* 波浪形装饰（透明度降低到 10%）*/
.bg-wave {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 80rpx;
  background: rgba(37, 99, 235, 0.10);
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
  height: 120rpx; /* 60px */
  padding-top: 24rpx;
}

/* 品牌标识 */
.brand-section {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.brand-logo {
  font-size: 48rpx; /* 24px */
  font-weight: 700;
  color: var(--cl-primary, #2563EB);
  line-height: 1;
  letter-spacing: 1rpx;
}

.brand-slogan {
  font-size: 24rpx; /* 12px */
  font-weight: 500;
  color: var(--cl-gray-600, #64748B);
  line-height: 1;
}

/* 操作按钮组 */
.action-buttons {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.action-btn {
  padding: 12rpx 24rpx;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(8px);
  border-radius: 20rpx; /* 10px */
  border: 1px solid rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all var(--transition-hover, 150ms ease);
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.95);
  transform: translateY(-2rpx);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.action-btn-primary {
  background: var(--cl-primary, #2563EB);
  border-color: var(--cl-primary, #2563EB);
}

.action-btn-primary .action-btn-text {
  color: white;
}

.action-btn-primary:hover {
  background: var(--cl-primary-700, #1D4ED8);
}

.action-btn-publish {
  background: linear-gradient(135deg, var(--cl-accent-purple, #6C5CE7) 0%, var(--cl-primary, #2563EB) 100%);
  border: none;
  box-shadow: 0 2px 8px rgba(108, 92, 231, 0.25);
}

.action-btn-publish .action-btn-text {
  color: white;
}

.action-btn-publish:hover {
  box-shadow: 0 4px 12px rgba(108, 92, 231, 0.35);
}

.action-btn-text {
  font-size: 28rpx; /* 14px */
  font-weight: 500;
  color: var(--cl-gray-700, #475569);
  line-height: 1;
}

.action-btn-icon {
  font-size: 28rpx;
  line-height: 1;
}

/* ========== 五、主内容区（第二行：左右分栏）========== */
.main-content-area {
  display: flex;
  align-items: center;
  gap: 32rpx;
  height: 240rpx; /* 120px */
  margin-top: 16rpx;
}

/* 左侧：搜索栏（60%）*/
.search-section {
  flex: 6;
  display: flex;
  align-items: center;
}

.search-box {
  position: relative;
  width: 100%;
  height: 80rpx; /* 40px */
  /* 半透明玻璃效果（文档规范）*/
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border-radius: 32rpx; /* 16px - 文档规范 */
  display: flex;
  align-items: center;
  padding: 0 24rpx;
  gap: 16rpx;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all var(--transition-hover, 150ms ease);
  /* 描边（文档规范）*/
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.search-box:hover {
  background: rgba(255, 255, 255, 0.85);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2rpx);
  border-color: rgba(37, 99, 235, 0.2);
}

.search-box:focus-within {
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.15);
  border-color: var(--cl-primary, #2563EB);
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

/* 蓝色搜索按钮（文档规范：按钮蓝 #2563EB）*/
.search-btn-blue {
  padding: 14rpx 32rpx; /* 调整为14rpx，使高度与语音按钮一致（14*2 + 28 = 56rpx） */
  background: var(--cl-primary, #2563EB);
  border-radius: 40rpx; /* 20px */
  cursor: pointer;
  transition: all var(--transition-hover, 150ms ease);
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.25);
}

.search-btn-blue:hover {
  background: var(--cl-primary-700, #1D4ED8);
  transform: scale(1.03);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.35);
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

/* 橙色语音搜索按钮（文档规范：语音橙降饱和）*/
.voice-search-btn {
  position: relative;
  width: 56rpx;
  height: 56rpx;
  /* 降饱和的橙色 */
  background: linear-gradient(135deg, #F59E0B 0%, #FB923C 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-hover, 150ms ease);
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.25);
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

/* ========== 六、右侧快捷入口卡片（35%）========== */
.quick-entry-cards {
  flex: 3.5;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.quick-card {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 16rpx 20rpx;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
  border-radius: var(--radius-card, 12px);
  border: 1px solid rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all var(--transition-hover, 150ms ease);
}

.quick-card:hover {
  background: rgba(255, 255, 255, 0.95);
  transform: translateX(4rpx);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.quick-icon {
  font-size: 40rpx; /* 20px */
  line-height: 1;
  flex-shrink: 0;
}

.quick-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.quick-title {
  font-size: 28rpx; /* 14px */
  font-weight: 600;
  color: var(--cl-gray-900, #0F172A);
  line-height: 1;
}

.quick-count {
  font-size: 22rpx; /* 11px */
  font-weight: 400;
  color: var(--cl-gray-500, #94A3B8);
  line-height: 1;
}

.quick-arrow {
  font-size: 28rpx;
  color: var(--cl-gray-400, #CBD5E1);
  line-height: 1;
  flex-shrink: 0;
  transition: all var(--transition-hover, 150ms ease);
}

.quick-card:hover .quick-arrow {
  color: var(--cl-primary, #2563EB);
  transform: translateX(4rpx);
}

/* 不同主题的卡片 */
.quick-card-blue:hover {
  border-color: rgba(37, 99, 235, 0.2);
}

.quick-card-orange:hover {
  border-color: rgba(245, 158, 11, 0.2);
}

.quick-card-green:hover {
  border-color: rgba(22, 163, 74, 0.2);
}

/* ========== 七、底部数据栏（第三行）========== */
.bottom-stats-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24rpx;
  height: 80rpx; /* 40px */
  padding-bottom: 16rpx;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.stat-icon {
  font-size: 28rpx;
  line-height: 1;
}

.stat-text {
  font-size: 24rpx; /* 12px */
  font-weight: 500;
  color: var(--cl-gray-600, #64748B);
  line-height: 1;
}

.stat-divider {
  font-size: 24rpx;
  color: var(--cl-gray-400, #CBD5E1);
  line-height: 1;
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



  /* 背景装饰缩小 */
  .bg-circle-1 {
    width: 250rpx;
    height: 250rpx;
  }

  .bg-circle-2 {
    width: 200rpx;
    height: 200rpx;
  }

  /* 方案 B 响应式：快捷入口卡片 */
  .quick-entry-cards {
    width: 100%;
    flex-direction: row;
    gap: 8rpx;
  }

  .quick-card {
    flex: 1;
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 12rpx;
  }

  .quick-icon {
    font-size: 32rpx;
  }

  .quick-title {
    font-size: 24rpx;
  }

  .quick-count {
    font-size: 20rpx;
  }

  .quick-arrow {
    display: none;
  }

  /* 底部数据栏 */
  .bottom-stats-bar {
    height: 60rpx;
    gap: 16rpx;
    padding-bottom: 12rpx;
  }

  .stat-text {
    font-size: 20rpx;
  }

  .stat-icon {
    font-size: 24rpx;
  }
}
</style>

