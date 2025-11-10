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
        <!-- 左侧：品牌标识 - 优化：更新品牌副标题 -->
        <view class="brand-section">
          <text class="brand-logo">CampusLink</text>
          <!-- 优化：品牌副标题，强化品牌气质与信任感 -->
          <text class="brand-slogan">100万大学生互助学习圈</text>
        </view>

        <!-- 右侧：操作按钮 - 优化：合并登录/注册，升级发布按钮 -->
        <view class="action-buttons">
          <!-- 登录/注册（次操作 - Secondary）-->
          <view class="action-btn action-btn-secondary" @click="handleLoginRegister">
            <text class="action-btn-text">登录 / 注册</text>
          </view>
          <!-- 发布（主操作 - Primary）-->
          <view class="action-btn action-btn-primary" @click="handlePublish">
            <text class="action-btn-icon">✨</text>
            <text class="action-btn-text">发布</text>
          </view>
        </view>
      </view>

      <!-- 第二行：主内容区（搜索框居中）-->
      <view class="main-content-area">
        <!-- 搜索栏 -->
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
const isVoiceActive = ref(false) // 语音搜索激活状态

/**
 * 登录/注册（优化：合并入口，打开统一弹窗）
 */
const handleLoginRegister = () => {
  // TODO: 打开登录/注册弹窗（Tab切换式设计）
  uni.navigateTo({ url: '/pages/auth/login' })
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


</script>

<style scoped lang="scss">
/* 企业级重构：第一层 - 品牌渐变区（顶部背景）- 优化：径向渐变增强纵深 */
.top-focus-bar {
  position: relative;
  width: 100%;
  height: 800rpx; /* 400px - 延伸至导航下方，覆盖更多区域 */
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
  z-index: 1;

  /* 增加轻微底部阴影，增强纵深感 */
  box-shadow: 0 8rpx 32rpx rgba(37, 99, 235, 0.06);
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
  font-size: 40rpx; /* 20px - 减小字号 */
  font-weight: 700;
  color: var(--cl-primary, #2563EB);
  line-height: 1;
  letter-spacing: 1rpx;
}

.brand-slogan {
  font-size: 22rpx; /* 11px - 减小字号 */
  font-weight: 500;
  color: var(--cl-gray-600, #64748B);
  line-height: 1;
}

/* 操作按钮组 */
.action-buttons {
  display: flex;
  align-items: center;
  gap: 16rpx;
  /* 优化：右上角半透明圆角背景 */
  padding: 12rpx 20rpx;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(12px);
  border-radius: 32rpx; /* 16px */
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
}

.action-btn {
  padding: 10rpx 20rpx; /* 减小内边距 */
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

/* 优化：次操作按钮（登录/注册）- 透明背景 + 蓝色描边 */
.action-btn-secondary {
  background: rgba(255, 255, 255, 0.3); /* 透明背景 */
  border: 1px solid var(--cl-primary, #2563EB); /* 蓝色描边 */
}

.action-btn-secondary .action-btn-text {
  color: var(--cl-primary, #2563EB); /* 蓝色文字 */
  font-size: 28rpx; /* 14px */
}

.action-btn-secondary:hover {
  background: rgba(37, 99, 235, 0.1); /* 轻微蓝色背景 */
  border-color: var(--cl-primary, #2563EB);
  transform: translateY(-2rpx);
  box-shadow: 0 4rpx 12rpx rgba(37, 99, 235, 0.15);
}

/* 优化：主操作按钮（发布）- 主色实心按钮 + 微光渐变 */
.action-btn-primary {
  background: var(--cl-primary, #2563EB); /* 主色实心 */
  border: none;
  /* 优化：增加阴影 */
  box-shadow: 0 8rpx 24rpx rgba(37, 99, 235, 0.3);
}

.action-btn-primary .action-btn-text {
  color: #FFFFFF; /* 白色文字 */
  font-size: 30rpx; /* 15px - 稍大 */
  font-weight: 600;
}

.action-btn-primary .action-btn-icon {
  color: #FFFFFF;
}

.action-btn-primary:hover {
  /* 优化：微光渐变 */
  background: linear-gradient(90deg, #3B82F6 0%, #60A5FA 100%);
  transform: translateY(-2rpx);
  box-shadow: 0 12rpx 32rpx rgba(37, 99, 235, 0.4);
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

/* ========== 五、主内容区（第二行：搜索框居中）========== */
.main-content-area {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1; /* 占据剩余空间 */
  margin-top: 0; /* 移除上边距 */
}

/* 搜索栏（居中）*/
.search-section {
  display: flex;
  align-items: center;
  max-width: 1400rpx; /* 最大宽度 700px */
  width: 70%; /* 占据 70% 宽度 */
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
  /* 优化：柔和阴影（模糊 12px，透明度 10%）*/
  box-shadow: 0 6rpx 24rpx rgba(0, 0, 0, 0.10);
  transition: all var(--transition-hover, 150ms ease);
  /* 描边（文档规范）*/
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.search-box:hover {
  background: rgba(255, 255, 255, 0.85);
  /* 优化：hover 时阴影加深 */
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.12);
  transform: translateY(-2rpx);
  border-color: rgba(37, 99, 235, 0.2);
}

.search-box:focus-within {
  background: rgba(255, 255, 255, 0.95);
  /* 优化：聚焦时放大 1.03、发光蓝边 */
  box-shadow: 0 8rpx 40rpx rgba(37, 99, 235, 0.20), 0 0 0 4rpx rgba(37, 99, 235, 0.10);
  border-color: var(--cl-primary, #2563EB);
  transform: translateY(-2rpx) scale(1.03);
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

  /* 搜索框居中 */
  .search-section {
    width: 90%; /* 移动端占据 90% 宽度 */
  }
}
</style>

