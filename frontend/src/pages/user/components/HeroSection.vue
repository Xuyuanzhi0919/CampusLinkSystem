<template>
  <!-- ========== Hero Section ========== -->
  <view class="hero-section">
    <!-- 背景 -->
    <view class="hero-bg" />

    <!-- 顶部栏 -->
    <view class="hero-topbar">
      <text class="greeting">{{ greeting }}</text>
      <view class="edit-btn" @click="$emit('editProfile')">
        <Icon name="edit-2" :size="14" />
        <text class="edit-btn-text">编辑资料</text>
      </view>
    </view>

    <!-- 主内容 -->
    <view class="hero-body">
      <!-- 头像 -->
      <view class="avatar-wrap" @click="$emit('editProfile')">
        <image class="avatar-img" :src="profile?.avatarUrl || defaultAvatar" mode="aspectFill" />
        <view class="lv-badge">
          <text class="lv-text">{{ profile?.level || 1 }}</text>
        </view>
      </view>

      <!-- 用户信息 -->
      <view class="user-info">
        <text class="user-name">{{ profile?.nickname || '未设置昵称' }}</text>

        <view class="meta-row">
          <view v-if="profile?.schoolName" class="meta-tag">
            <Icon name="map-pin" :size="10" class="meta-icon" />
            <text class="meta-text">{{ profile.schoolName }}</text>
          </view>
          <view v-if="profile?.major" class="meta-tag meta-tag--plain">
            <text class="meta-text">{{ profile.major }}</text>
          </view>
        </view>

        <!-- 积分 -->
        <view class="points-chip" @click="$emit('pointsClick')">
          <text class="points-icon">✦</text>
          <text class="points-val">{{ profile?.points || 0 }}</text>
          <text class="points-unit">积分</text>
          <Icon name="chevron-right" :size="12" class="points-chevron" />
        </view>
      </view>
    </view>

    <!-- 底部白色圆角过渡 -->
    <view class="hero-foot" />
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
  const h = new Date().getHours()
  if (h < 6)  return '夜深了，注意休息'
  if (h < 12) return '早上好'
  if (h < 14) return '午安'
  if (h < 18) return '下午好'
  return '晚上好'
})
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

/* ─── 整体容器 ─── */
.hero-section {
  position: relative;
  overflow: hidden;
}

/* ─── 背景层 ─── */
.hero-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(150deg, #1d3a72 0%, #2e5fa3 55%, #3d82c4 100%);
}

/* ─── 顶部栏 ─── */
.hero-topbar {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 56rpx 36rpx 20rpx;

  @media (min-width: 1024px) {
    padding: 28px 0 16px;
    max-width: 860px;
    margin: 0 auto;
  }
}

.greeting {
  font-size: 24rpx;
  color: rgba(255,255,255,0.6);
  font-weight: 400;
  letter-spacing: 0.01em;

  @media (min-width: 1024px) {
    font-size: 14px;
  }
}

.edit-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 10rpx 22rpx;
  background: rgba(255,255,255,0.12);
  border: 1px solid rgba(255,255,255,0.2);
  border-radius: 100rpx;
  color: rgba(255,255,255,0.85);
  cursor: pointer;
  transition: background 0.18s;

  &:active { background: rgba(255,255,255,0.2); }

  // #ifdef H5
  &:hover { background: rgba(255,255,255,0.18); }
  // #endif

  @media (min-width: 1024px) {
    padding: 7px 16px;
    gap: 5px;
  }
}

.edit-btn-text {
  font-size: 22rpx;
  font-weight: 500;

  @media (min-width: 1024px) {
    font-size: 13px;
  }
}

/* ─── 主体 ─── */
.hero-body {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 30rpx;
  padding: 12rpx 36rpx 56rpx;

  @media (min-width: 1024px) {
    padding: 8px 0 48px;
    max-width: 860px;
    margin: 0 auto;
    gap: 28px;
  }
}

/* ─── 头像 ─── */
.avatar-wrap {
  position: relative;
  flex-shrink: 0;
  cursor: pointer;
}

.avatar-img {
  width: 136rpx;
  height: 136rpx;
  border-radius: 50%;
  border: 3px solid rgba(255,255,255,0.3);
  display: block;
  background: rgba(255,255,255,0.1);
  transition: opacity 0.2s;

  &:active { opacity: 0.85; }

  @media (min-width: 1024px) {
    width: 96px;
    height: 96px;
  }
}

.lv-badge {
  position: absolute;
  bottom: -4rpx;
  right: -4rpx;
  min-width: 36rpx;
  height: 36rpx;
  padding: 0 10rpx;
  background: linear-gradient(135deg, #f59e0b, #f97316);
  border-radius: 100rpx;
  border: 2px solid rgba(255,255,255,0.35);
  display: flex;
  align-items: center;
  justify-content: center;

  @media (min-width: 1024px) {
    min-width: 24px;
    height: 24px;
    padding: 0 6px;
  }
}

.lv-text {
  font-size: 18rpx;
  font-weight: 700;
  color: #fff;
  line-height: 1;

  @media (min-width: 1024px) {
    font-size: 11px;
  }
}

/* ─── 用户信息 ─── */
.user-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 12rpx;

  @media (min-width: 1024px) {
    gap: 8px;
  }
}

.user-name {
  font-size: 40rpx;
  font-weight: 700;
  color: #fff;
  line-height: 1.15;
  letter-spacing: -0.01em;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  @media (min-width: 1024px) {
    font-size: 26px;
  }
}

.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;

  @media (min-width: 1024px) {
    gap: 6px;
  }
}

.meta-tag {
  display: inline-flex;
  align-items: center;
  gap: 5rpx;
  padding: 5rpx 14rpx;
  background: rgba(255,255,255,0.12);
  border: 1px solid rgba(255,255,255,0.18);
  border-radius: 100rpx;

  &--plain {
    background: rgba(255,255,255,0.07);
    border-color: rgba(255,255,255,0.1);
  }

  @media (min-width: 1024px) {
    padding: 3px 10px;
    gap: 4px;
    border-radius: 100px;
  }
}

.meta-icon {
  color: rgba(255,255,255,0.65);
  flex-shrink: 0;
}

.meta-text {
  font-size: 20rpx;
  color: rgba(255,255,255,0.75);
  font-weight: 400;
  line-height: 1;

  @media (min-width: 1024px) {
    font-size: 12px;
  }
}

/* ─── 积分胶囊 ─── */
.points-chip {
  display: inline-flex;
  align-items: center;
  gap: 7rpx;
  padding: 10rpx 18rpx;
  background: rgba(255,255,255,0.1);
  border: 1px solid rgba(255,255,255,0.15);
  border-radius: 14rpx;
  cursor: pointer;
  transition: background 0.18s;
  align-self: flex-start;

  &:active { background: rgba(255,255,255,0.18); }

  // #ifdef H5
  &:hover { background: rgba(255,255,255,0.17); }
  // #endif

  @media (min-width: 1024px) {
    padding: 7px 14px;
    gap: 5px;
    border-radius: 10px;
  }
}

.points-icon {
  font-size: 16rpx;
  color: #fbbf24;
  line-height: 1;

  @media (min-width: 1024px) {
    font-size: 11px;
  }
}

.points-val {
  font-size: 28rpx;
  font-weight: 700;
  color: #fbbf24;
  line-height: 1;

  @media (min-width: 1024px) {
    font-size: 18px;
  }
}

.points-unit {
  font-size: 20rpx;
  color: rgba(255,255,255,0.5);
  font-weight: 400;

  @media (min-width: 1024px) {
    font-size: 12px;
  }
}

.points-chevron {
  color: rgba(255,255,255,0.3);
  flex-shrink: 0;
}

/* ─── 底部圆角过渡到白色内容区 ─── */
.hero-foot {
  position: relative;
  z-index: 3;
  height: 36rpx;
  background: $color-bg-page;
  border-radius: 36rpx 36rpx 0 0;

  @media (min-width: 1024px) {
    height: 28px;
    border-radius: 28px 28px 0 0;
  }
}
</style>
