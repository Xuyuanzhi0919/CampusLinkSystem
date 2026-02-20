<template>
  <view class="hero">
    <!-- 背景 -->
    <view class="hero__bg" />
    <!-- 微妙的光晕，左上角一个 -->
    <view class="hero__glow" />

    <!-- 主内容 -->
    <view class="hero__inner">

      <!-- ① 顶栏：问候 + 操作 -->
      <view class="hero__topbar">
        <view class="hero__greeting-wrap">
          <text class="hero__time">{{ timeLabel }}</text>
          <text class="hero__greeting">{{ greeting }}</text>
        </view>
        <view class="hero__actions">
          <view class="hero__icon-btn" @click="$emit('editProfile')">
            <Icon name="edit-2" :size="16" />
          </view>
        </view>
      </view>

      <!-- ② 核心身份区 -->
      <view class="hero__identity">
        <!-- 头像 -->
        <view class="hero__avatar-area" @click="$emit('editProfile')">
          <view class="hero__avatar-ring">
            <image
              class="hero__avatar-img"
              :src="profile?.avatarUrl || defaultAvatar"
              mode="aspectFill"
            />
          </view>
          <!-- 在线状态 -->
          <view class="hero__online" />
          <!-- 等级 -->
          <view class="hero__lv">
            <text class="hero__lv-text">Lv.{{ profile?.level || 1 }}</text>
          </view>
        </view>

        <!-- 右侧信息 -->
        <view class="hero__info">
          <!-- 姓名 -->
          <text class="hero__name">{{ profile?.nickname || '未设置昵称' }}</text>

          <!-- 标签行 -->
          <view class="hero__tags">
            <view v-if="profile?.schoolName" class="hero__tag">
              <Icon name="map-pin" :size="10" class="hero__tag-icon" />
              <text class="hero__tag-text">{{ profile.schoolName }}</text>
            </view>
            <view v-if="profile?.major" class="hero__tag hero__tag--dim">
              <text class="hero__tag-text">{{ profile.major }}</text>
            </view>
          </view>

          <!-- 数据横排 -->
          <view class="hero__stats">
            <view
              v-for="s in quickStats"
              :key="s.key"
              class="hero__stat"
              @click="$emit('statClick', s.key)"
            >
              <text class="hero__stat-num">{{ s.val }}</text>
              <text class="hero__stat-lbl">{{ s.label }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- ③ 积分 + 编辑资料 行 -->
      <view class="hero__footer-row">
        <view class="hero__points" @click="$emit('pointsClick')">
          <text class="hero__points-icon">✦</text>
          <text class="hero__points-num">{{ profile?.points?.toLocaleString() || '0' }}</text>
          <text class="hero__points-unit">积分</text>
          <Icon name="chevron-right" :size="12" class="hero__points-chevron" />
        </view>
        <view class="hero__edit-btn" @click="$emit('editProfile')">
          <Icon name="edit-2" :size="13" />
          <text class="hero__edit-text">编辑资料</text>
        </view>
      </view>

    </view>

    <!-- 底部白色圆弧过渡 -->
    <view class="hero__wave" />
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Icon from '@/components/icons/index.vue'
import type { UserProfileData } from '@/types/user'

interface Props {
  profile: UserProfileData | null
  stats?: { resourceCount?: number; answerCount?: number; likeCount?: number }
}

const props = defineProps<Props>()

defineEmits<{
  editProfile: []
  pointsClick: []
  statClick: [key: string]
}>()

const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=campus'

const timeLabel = computed(() => {
  const h = new Date().getHours()
  if (h < 6)  return 'MIDNIGHT'
  if (h < 12) return 'MORNING'
  if (h < 14) return 'NOON'
  if (h < 18) return 'AFTERNOON'
  return 'EVENING'
})

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6)  return '夜深了，注意休息 👋'
  if (h < 12) return '早上好，开始新的一天！'
  if (h < 14) return '午安，记得吃饭哦~'
  if (h < 18) return '下午好，继续加油！'
  return '晚上好，今天辛苦了'
})

const fmt = (n: number) => n >= 1000 ? (n / 1000).toFixed(1) + 'k' : String(n)

const quickStats = computed(() => [
  { key: 'resources', label: '资源', val: fmt(props.stats?.resourceCount ?? 0) },
  { key: 'answers',   label: '回答', val: fmt(props.stats?.answerCount   ?? 0) },
  { key: 'likes',     label: '获赞', val: fmt(props.stats?.likeCount     ?? 0) },
])
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

/* ════════════════════════════════
   Hero 容器
════════════════════════════════ */
.hero {
  position: relative;
  overflow: hidden;
}

/* ── 背景 ── */
.hero__bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(145deg, #152d5e 0%, #1e4a8a 50%, #2c6dbf 100%);
}

/* 左上角柔光 */
.hero__glow {
  position: absolute;
  top: -60rpx;
  left: -40rpx;
  width: 400rpx;
  height: 400rpx;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(100,160,255,0.18) 0%, transparent 70%);
  pointer-events: none;

  @media (min-width: 1024px) {
    width: 300px;
    height: 300px;
    top: -40px;
    left: -20px;
  }
}

/* ── 内容包裹 ── */
.hero__inner {
  position: relative;
  z-index: 2;
  padding: 0 36rpx;

  @media (min-width: 1024px) {
    max-width: 860px;
    margin: 0 auto;
    padding: 0;
  }
}

/* ════════════════════════════════
   ① 顶栏
════════════════════════════════ */
.hero__topbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding-top: 60rpx;
  padding-bottom: 28rpx;

  @media (min-width: 1024px) {
    padding-top: 36px;
    padding-bottom: 20px;
  }
}

.hero__greeting-wrap {
  display: flex;
  flex-direction: column;
  gap: 4rpx;

  @media (min-width: 1024px) {
    gap: 3px;
  }
}

.hero__time {
  font-size: 18rpx;
  font-weight: 600;
  color: rgba(255,255,255,0.35);
  letter-spacing: 0.12em;
  line-height: 1;

  @media (min-width: 1024px) {
    font-size: 11px;
  }
}

.hero__greeting {
  font-size: 24rpx;
  color: rgba(255,255,255,0.7);
  font-weight: 400;
  letter-spacing: 0.01em;

  @media (min-width: 1024px) {
    font-size: 14px;
  }
}

.hero__actions {
  display: flex;
  gap: 12rpx;
  align-items: center;

  @media (min-width: 1024px) {
    gap: 8px;
  }
}

.hero__icon-btn {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: rgba(255,255,255,0.1);
  border: 1px solid rgba(255,255,255,0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255,255,255,0.8);
  cursor: pointer;
  transition: background 0.18s;

  &:active { background: rgba(255,255,255,0.2); }

  // #ifdef H5
  &:hover { background: rgba(255,255,255,0.18); }
  // #endif

  /* 在 PC 端隐藏，底部 footer-row 已有编辑按钮 */
  @media (min-width: 1024px) {
    display: none;
  }
}

/* ════════════════════════════════
   ② 身份区
════════════════════════════════ */
.hero__identity {
  display: flex;
  align-items: flex-end;
  gap: 28rpx;
  padding-bottom: 28rpx;

  @media (min-width: 1024px) {
    gap: 24px;
    padding-bottom: 20px;
    align-items: flex-end;
  }
}

/* 头像区域 */
.hero__avatar-area {
  position: relative;
  flex-shrink: 0;
  cursor: pointer;
}

.hero__avatar-ring {
  width: 168rpx;
  height: 168rpx;
  border-radius: 50%;
  padding: 4rpx;
  background: linear-gradient(
    135deg,
    rgba(255,255,255,0.5) 0%,
    rgba(255,255,255,0.15) 50%,
    rgba(255,255,255,0.05) 100%
  );
  box-shadow:
    0 0 0 1px rgba(255,255,255,0.12),
    0 8rpx 32rpx rgba(0,0,0,0.3);
  transition: transform 0.25s cubic-bezier(0.34,1.56,0.64,1);

  &:active { transform: scale(0.95); }

  @media (min-width: 1024px) {
    width: 112px;
    height: 112px;
    padding: 3px;
  }
}

.hero__avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  display: block;
  background: #1e4a8a;
}

/* 在线绿点 */
.hero__online {
  position: absolute;
  top: 10rpx;
  right: 4rpx;
  width: 20rpx;
  height: 20rpx;
  background: #4ade80;
  border-radius: 50%;
  border: 3px solid #1e4a8a;
  box-shadow: 0 0 0 2px rgba(74,222,128,0.3);

  @media (min-width: 1024px) {
    width: 13px;
    height: 13px;
    top: 7px;
    right: 3px;
  }
}

/* 等级标签 */
.hero__lv {
  position: absolute;
  bottom: -2rpx;
  left: 50%;
  transform: translateX(-50%);
  height: 38rpx;
  padding: 0 16rpx;
  background: linear-gradient(90deg, #f59e0b 0%, #f97316 100%);
  border-radius: 100rpx;
  border: 2.5px solid rgba(255,255,255,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
  box-shadow: 0 3rpx 12rpx rgba(249,115,22,0.5);

  @media (min-width: 1024px) {
    height: 24px;
    padding: 0 10px;
    bottom: -2px;
  }
}

.hero__lv-text {
  font-size: 18rpx;
  font-weight: 700;
  color: #fff;
  line-height: 1;
  letter-spacing: 0.02em;

  @media (min-width: 1024px) {
    font-size: 11px;
  }
}

/* 右侧信息 */
.hero__info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 14rpx;
  // 与头像底部对齐
  padding-bottom: 12rpx;

  @media (min-width: 1024px) {
    gap: 10px;
    padding-bottom: 8px;
  }
}

.hero__name {
  font-size: 46rpx;
  font-weight: 700;
  color: #fff;
  line-height: 1.1;
  letter-spacing: -0.02em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  @media (min-width: 1024px) {
    font-size: 30px;
  }
}

/* 标签 */
.hero__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8rpx;

  @media (min-width: 1024px) {
    gap: 6px;
  }
}

.hero__tag {
  display: inline-flex;
  align-items: center;
  gap: 5rpx;
  padding: 5rpx 14rpx;
  background: rgba(255,255,255,0.13);
  border: 1px solid rgba(255,255,255,0.2);
  border-radius: 100rpx;

  &--dim {
    background: rgba(255,255,255,0.07);
    border-color: rgba(255,255,255,0.1);
  }

  @media (min-width: 1024px) {
    padding: 3px 10px;
    gap: 4px;
    border-radius: 100px;
  }
}

.hero__tag-icon {
  color: rgba(255,255,255,0.6);
  flex-shrink: 0;
}

.hero__tag-text {
  font-size: 20rpx;
  color: rgba(255,255,255,0.78);
  line-height: 1;

  @media (min-width: 1024px) {
    font-size: 12px;
  }
}

/* 快速数据横排 */
.hero__stats {
  display: flex;
  gap: 28rpx;

  @media (min-width: 1024px) {
    gap: 20px;
  }
}

.hero__stat {
  display: flex;
  flex-direction: column;
  gap: 2rpx;
  cursor: pointer;

  @media (min-width: 1024px) {
    gap: 2px;
  }
}

.hero__stat-num {
  font-size: 32rpx;
  font-weight: 700;
  color: #fff;
  line-height: 1;
  letter-spacing: -0.01em;

  @media (min-width: 1024px) {
    font-size: 20px;
  }
}

.hero__stat-lbl {
  font-size: 19rpx;
  color: rgba(255,255,255,0.5);
  font-weight: 400;

  @media (min-width: 1024px) {
    font-size: 11px;
  }
}

/* ════════════════════════════════
   ③ 底部信息行
════════════════════════════════ */
.hero__footer-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18rpx 0 52rpx;
  border-top: 1px solid rgba(255,255,255,0.1);

  @media (min-width: 1024px) {
    padding: 14px 0 44px;
  }
}

/* 积分胶囊 */
.hero__points {
  display: inline-flex;
  align-items: center;
  gap: 7rpx;
  cursor: pointer;
  transition: opacity 0.18s;

  &:active { opacity: 0.75; }

  // #ifdef H5
  &:hover { opacity: 0.85; }
  // #endif

  @media (min-width: 1024px) {
    gap: 5px;
  }
}

.hero__points-icon {
  font-size: 18rpx;
  color: #fbbf24;
  line-height: 1;

  @media (min-width: 1024px) {
    font-size: 12px;
  }
}

.hero__points-num {
  font-size: 34rpx;
  font-weight: 700;
  color: #fbbf24;
  line-height: 1;
  letter-spacing: -0.01em;

  @media (min-width: 1024px) {
    font-size: 22px;
  }
}

.hero__points-unit {
  font-size: 20rpx;
  color: rgba(255,255,255,0.45);
  font-weight: 400;
  margin-left: 2rpx;

  @media (min-width: 1024px) {
    font-size: 12px;
  }
}

.hero__points-chevron {
  color: rgba(255,255,255,0.25);
  flex-shrink: 0;
}

/* 编辑资料按钮（仅 PC） */
.hero__edit-btn {
  display: none;

  @media (min-width: 1024px) {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 18px;
    background: rgba(255,255,255,0.1);
    border: 1px solid rgba(255,255,255,0.18);
    border-radius: 100px;
    color: rgba(255,255,255,0.8);
    cursor: pointer;
    transition: background 0.18s;
    font-size: 13px;

    &:hover { background: rgba(255,255,255,0.18); }
  }
}

.hero__edit-text {
  font-size: 22rpx;
  font-weight: 500;
  color: inherit;

  @media (min-width: 1024px) {
    font-size: 13px;
  }
}

/* ════════════════════════════════
   波浪过渡
════════════════════════════════ */
.hero__wave {
  position: relative;
  z-index: 3;
  height: 44rpx;
  background: $color-bg-page;
  border-radius: 44rpx 44rpx 0 0;

  @media (min-width: 1024px) {
    height: 32px;
    border-radius: 32px 32px 0 0;
  }
}
</style>
