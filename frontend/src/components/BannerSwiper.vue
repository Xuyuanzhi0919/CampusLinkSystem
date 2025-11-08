<template>
  <view class="banner-swiper">
    <swiper
      class="swiper"
      :indicator-dots="true"
      :autoplay="true"
      :interval="4000"
      :duration="500"
      :circular="true"
      indicator-color="rgba(255, 255, 255, 0.5)"
      indicator-active-color="#409EFF"
    >
      <swiper-item v-for="(banner, index) in banners" :key="index">
        <view class="banner-item" @click="handleBannerClick(banner)">
          <image class="banner-image" :src="banner.image" mode="aspectFill" />
          <view class="banner-content">
            <text class="banner-title">{{ banner.title }}</text>
            <text class="banner-desc">{{ banner.description }}</text>
            <!-- CTA 按钮（仅在第3个 Banner 显示"立即提问"） -->
            <view v-if="index === 2" class="cta-btn" @click.stop="handleCTAClick">
              <text class="cta-text">立即提问</text>
              <text class="cta-icon">→</text>
            </view>
          </view>
        </view>
      </swiper-item>
    </swiper>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { BANNER_IMAGES } from '@/config/images'

// Banner 数据类型
interface Banner {
  id: number
  image: string
  title: string
  description: string
  link?: string
}

// Props
interface Props {
  banners?: Banner[]
}

const props = withDefaults(defineProps<Props>(), {
  banners: () => [
    {
      id: 1,
      image: BANNER_IMAGES.campusStudy,
      title: '100万+大学生的学习互助圈',
      description: '海量课件、智能问答、互助任务，一站式解决学习难题',
    },
    {
      id: 2,
      image: BANNER_IMAGES.library,
      title: '海量学习资源免费下载',
      description: '课件、试题、笔记应有尽有，助力学业进步',
    },
    {
      id: 3,
      image: BANNER_IMAGES.discussion,
      title: 'AI智能答疑，秒速解惑',
      description: '遇到难题？AI助手24小时在线为你解答',
    },
  ],
})

// Emits
const emit = defineEmits<{
  click: [banner: Banner]
}>()

/**
 * 点击 Banner
 */
const handleBannerClick = (banner: Banner) => {
  emit('click', banner)

  if (banner.link) {
    uni.navigateTo({
      url: banner.link,
    })
  }
}

/**
 * 点击 CTA 按钮
 */
const handleCTAClick = () => {
  uni.navigateTo({
    url: '/pages/question/create',
    fail: () => {
      uni.showToast({
        title: '功能开发中',
        icon: 'none',
      })
    },
  })
}
</script>

<style scoped>
.banner-swiper {
  width: 100%;
  height: 440rpx;
  background: #f5f5f5;
  margin-bottom: 40rpx; /* 增加与功能图标栏的间距至 20px (40rpx) */
}

.swiper {
  width: 100%;
  height: 100%;
}

.banner-item {
  width: 100%;
  height: 100%;
  position: relative;
}

.banner-image {
  width: 100%;
  height: 100%;
}

.banner-content {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 40rpx 30rpx;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.6), transparent);
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.banner-title {
  font-size: 36rpx;
  font-weight: 600;
  color: #ffffff;
  line-height: 1.4;
}

.banner-desc {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.5;
}

/* CTA 按钮 */
.cta-btn {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 16rpx 32rpx;
  background: linear-gradient(135deg, #FF7D00 0%, #FFA940 100%); /* 活力橙渐变 */
  border-radius: 48rpx; /* 圆角 */
  margin-top: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(255, 125, 0, 0.4); /* 橙色阴影 */
  transition: all 0.3s ease;
}

.cta-btn:active {
  transform: scale(0.95);
  box-shadow: 0 2rpx 8rpx rgba(255, 125, 0, 0.3);
}

.cta-text {
  font-size: 28rpx;
  font-weight: 600;
  color: white;
  line-height: 1;
}

.cta-icon {
  font-size: 24rpx;
  color: white;
  line-height: 1;
}
</style>

