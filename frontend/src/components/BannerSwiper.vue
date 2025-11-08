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
          </view>
        </view>
      </swiper-item>
    </swiper>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

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
      image: 'https://via.placeholder.com/750x440/409EFF/FFFFFF?text=CampusLink',
      title: '100万+大学生的学习互助圈',
      description: '海量课件、智能问答、互助任务，一站式解决学习难题',
    },
    {
      id: 2,
      image: 'https://via.placeholder.com/750x440/FF7D00/FFFFFF?text=资源共享',
      title: '海量学习资源免费下载',
      description: '课件、试题、笔记应有尽有，助力学业进步',
    },
    {
      id: 3,
      image: 'https://via.placeholder.com/750x440/00B42A/FFFFFF?text=智能问答',
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
</script>

<style scoped>
.banner-swiper {
  width: 100%;
  height: 440rpx;
  background: #f5f5f5;
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
</style>

