<template>
  <!-- ========== Hero Section - 沉浸式个人门面 ========== -->
  <view class="hero-section">
    <!-- 多层背景 -->
    <view class="hero-bg">
      <view class="bg-gradient" />
      <view class="bg-noise" />
      <view class="bg-orb bg-orb--1" />
      <view class="bg-orb bg-orb--2" />
      <view class="bg-orb bg-orb--3" />
      <!-- 网格线装饰 -->
      <view class="bg-grid" />
    </view>

    <!-- 顶部操作栏 -->
    <view class="hero-topbar">
      <view class="topbar-left">
        <text class="topbar-greeting">{{ greeting }}</text>
      </view>
      <view class="topbar-right">
        <view class="edit-btn" @click="$emit('editProfile')">
          <Icon name="edit-2" :size="15" />
          <text class="edit-btn-text">编辑</text>
        </view>
      </view>
    </view>

    <!-- 主内容区 -->
    <view class="hero-body">
      <!-- 左：头像区 -->
      <view class="avatar-zone" @click="$emit('editProfile')">
        <view class="avatar-ring">
          <image class="avatar-img" :src="profile?.avatarUrl || defaultAvatar" mode="aspectFill" />
          <!-- 等级徽章 -->
          <view class="lv-chip">
            <text class="lv-text">Lv.{{ profile?.level || 1 }}</text>
          </view>
        </view>
        <!-- 在线状态 -->
        <view class="online-dot" />
      </view>

      <!-- 右：信息区 -->
      <view class="info-zone">
        <text class="user-name">{{ profile?.nickname || '未设置昵称' }}</text>
        <view class="user-tags">
          <view class="tag tag--school">
            <Icon name="map-pin" :size="11" />
            <text class="tag-text">{{ profile?.schoolName || '未设置学校' }}</text>
          </view>
          <view v-if="profile?.major" class="tag tag--slogan">
            <text class="tag-text">{{ profile.major }}</text>
          </view>
        </view>

        <!-- 校园币 -->
        <view class="points-row" @click="$emit('pointsClick')">
          <view class="points-coin">
            <text class="coin-icon">⬡</text>
            <text class="coin-value">{{ profile?.points || 0 }}</text>
            <text class="coin-label">校园币</text>
          </view>
          <Icon name="chevron-right" :size="14" class="points-arrow" />
        </view>
      </view>
    </view>

    <!-- 底部装饰波浪（与下方内容衔接） -->
    <view class="hero-wave" />
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Icon from '@/components/icons/index.vue'
import type { UserProfileData } from '@/types/user'

interface Props {
  profile: UserProfileData | null
}

defineProps<Props>()

defineEmits<{
  editProfile: []
  pointsClick: []
}>()

const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=campus'

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了，注意休息 🌙'
  if (hour < 12) return '早上好 ☀️'
  if (hour < 14) return '午安 🍱'
  if (hour < 18) return '下午好 ☕'
  return '晚上好 🌆'
})
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

/* ========== Hero Section ========== */
.hero-section {
  position: relative;
  padding-bottom: 40rpx;
  overflow: hidden;
  animation: heroFadeIn 0.7s cubic-bezier(0.22, 1, 0.36, 1) both;
}

@keyframes heroFadeIn {
  from { opacity: 0; transform: translateY(-20rpx); }
  to   { opacity: 1; transform: translateY(0); }
}

/* ========== 多层背景 ========== */
.hero-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.bg-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    145deg,
    #0F2027 0%,
    #1a3a5c 40%,
    #203a43 70%,
    #2c5364 100%
  );
}

.bg-noise {
  position: absolute;
  inset: 0;
  opacity: 0.04;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)' opacity='1'/%3E%3C/svg%3E");
  background-size: 200px;
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255,255,255,0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.03) 1px, transparent 1px);
  background-size: 40px 40px;
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  pointer-events: none;
}

.bg-orb--1 {
  width: 300rpx;
  height: 300rpx;
  top: -80rpx;
  right: -60rpx;
  background: rgba(55, 125, 255, 0.25);
  animation: orbFloat 8s ease-in-out infinite;
}

.bg-orb--2 {
  width: 200rpx;
  height: 200rpx;
  bottom: 0;
  left: -40rpx;
  background: rgba(99, 202, 183, 0.2);
  animation: orbFloat 10s ease-in-out infinite reverse;
}

.bg-orb--3 {
  width: 160rpx;
  height: 160rpx;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(249, 115, 22, 0.1);
  animation: orbFloat 12s ease-in-out infinite 2s;
}

@keyframes orbFloat {
  0%, 100% { transform: translateY(0) scale(1); }
  50%       { transform: translateY(-20rpx) scale(1.05); }
}

/* ========== 顶部操作栏 ========== */
.hero-topbar {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 60rpx 36rpx 24rpx;
}

.topbar-greeting {
  font-size: 24rpx;
  color: rgba(255,255,255,0.55);
  font-weight: 400;
  letter-spacing: 0.02em;
}

.edit-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 10rpx 20rpx;
  background: rgba(255,255,255,0.1);
  border: 1px solid rgba(255,255,255,0.15);
  border-radius: 9999rpx;
  color: rgba(255,255,255,0.75);
  backdrop-filter: blur(8px);
  cursor: pointer;
  transition: all 0.2s ease;

  &:active {
    background: rgba(255,255,255,0.18);
    transform: scale(0.96);
  }
}

.edit-btn-text {
  font-size: 22rpx;
  font-weight: 500;
}

/* ========== 主体内容 ========== */
.hero-body {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 28rpx;
  padding: 8rpx 36rpx 40rpx;
}

/* 头像区 */
.avatar-zone {
  position: relative;
  flex-shrink: 0;
  cursor: pointer;
}

.avatar-ring {
  position: relative;
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  padding: 4rpx;
  background: linear-gradient(135deg, rgba(55,125,255,0.8) 0%, rgba(99,202,183,0.8) 50%, rgba(249,115,22,0.6) 100%);
  box-shadow: 0 0 0 3rpx rgba(255,255,255,0.1), 0 8rpx 32rpx rgba(0,0,0,0.3);
  transition: transform 0.3s cubic-bezier(0.34,1.56,0.64,1);

  &:active {
    transform: scale(0.94);
  }
}

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 4rpx solid #1a3a5c;
  display: block;
}

.lv-chip {
  position: absolute;
  bottom: -8rpx;
  left: 50%;
  transform: translateX(-50%);
  background: linear-gradient(135deg, #F97316, #FBBF24);
  border: 3rpx solid #1a3a5c;
  border-radius: 9999rpx;
  padding: 4rpx 14rpx;
  box-shadow: 0 4rpx 12rpx rgba(249,115,22,0.4);
}

.lv-text {
  font-size: 18rpx;
  font-weight: 700;
  color: #fff;
  letter-spacing: 0.03em;
}

.online-dot {
  position: absolute;
  top: 8rpx;
  right: 2rpx;
  width: 18rpx;
  height: 18rpx;
  background: #3CCB7F;
  border-radius: 50%;
  border: 3rpx solid #1a3a5c;
  box-shadow: 0 0 8rpx rgba(60,203,127,0.6);
  animation: pulse 2.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { box-shadow: 0 0 8rpx rgba(60,203,127,0.6); }
  50%       { box-shadow: 0 0 16rpx rgba(60,203,127,0.9); }
}

/* 信息区 */
.info-zone {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 14rpx;
}

.user-name {
  font-size: 38rpx;
  font-weight: 700;
  color: #fff;
  letter-spacing: -0.01em;
  line-height: 1.1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: 5rpx;
  padding: 5rpx 14rpx;
  border-radius: 9999rpx;
  backdrop-filter: blur(8px);
}

.tag--school {
  background: rgba(55,125,255,0.2);
  border: 1px solid rgba(55,125,255,0.3);
  color: rgba(120,180,255,0.95);
}

.tag--slogan {
  background: rgba(255,255,255,0.08);
  border: 1px solid rgba(255,255,255,0.12);
  color: rgba(255,255,255,0.6);
}

.tag-text {
  font-size: 20rpx;
  font-weight: 400;
  line-height: 1;
}

/* 校园币行 */
.points-row {
  display: inline-flex;
  align-items: center;
  gap: 10rpx;
  padding: 12rpx 20rpx;
  background: rgba(255,255,255,0.08);
  border: 1px solid rgba(255,255,255,0.12);
  border-radius: 16rpx;
  backdrop-filter: blur(8px);
  cursor: pointer;
  transition: all 0.2s ease;
  align-self: flex-start;

  &:active {
    background: rgba(255,255,255,0.14);
    transform: scale(0.97);
  }
}

.points-coin {
  display: flex;
  align-items: center;
  gap: 7rpx;
}

.coin-icon {
  font-size: 20rpx;
  color: #FBBF24;
  line-height: 1;
}

.coin-value {
  font-size: 28rpx;
  font-weight: 700;
  color: #FBBF24;
  line-height: 1;
}

.coin-label {
  font-size: 20rpx;
  color: rgba(255,255,255,0.5);
  font-weight: 400;
}

.points-arrow {
  color: rgba(255,255,255,0.3);
  flex-shrink: 0;
}

/* ========== 底部波浪衔接 ========== */
.hero-wave {
  position: relative;
  height: 40rpx;
  background: $color-bg-page;
  border-radius: 40rpx 40rpx 0 0;
  margin-top: -1rpx;
  z-index: 3;
}

/* ========== PC 端适配 ========== */
@media (min-width: 1024px) {
  // PC 端 topbar：内容区居中对齐（860px 与 pc-body 宽度一致），去掉移动端顶部状态栏 padding
  .hero-topbar {
    padding: 32px 0 20px;
    max-width: 860px;
    margin: 0 auto;
  }

  .hero-body {
    padding: 8px 0 40px;
    max-width: 860px;
    margin: 0 auto;
    gap: 32px;
  }

  .avatar-ring {
    width: 140px;
    height: 140px;
  }

  .user-name {
    font-size: 32px;
  }

  .hero-wave {
    height: 32px;
    border-radius: 32px 32px 0 0;
  }
}
</style>
