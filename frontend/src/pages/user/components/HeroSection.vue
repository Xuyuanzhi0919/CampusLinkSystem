<template>
  <view class="hero">
    <!-- 背景层 -->
    <view class="hero-bg" />
    <view class="hero-bg-shine" />

    <!-- 内容 -->
    <view class="hero-inner">

      <!-- 顶栏 -->
      <view class="hero-topbar">
        <view class="hero-greeting">
          <text class="hero-hi">{{ timeLabel }}</text>
          <text class="hero-hello">{{ greeting }}</text>
        </view>
        <view class="hero-edit-icon" @click="$emit('editProfile')">
          <Icon name="edit-2" :size="15" />
        </view>
      </view>

      <!-- 身份区：头像左 + 信息右 -->
      <view class="hero-identity">
        <!-- 头像 -->
        <view class="hero-avatar-wrap" @click="$emit('editProfile')">
          <image
            class="hero-avatar"
            :src="profile?.avatarUrl || defaultAvatar"
            mode="aspectFill"
          />
          <view class="hero-online" />
          <view class="hero-lv">
            <text class="hero-lv-text">Lv.{{ profile?.level || 1 }}</text>
          </view>
        </view>

        <!-- 右侧信息 -->
        <view class="hero-info">
          <text class="hero-name">{{ profile?.nickname || '未设置昵称' }}</text>

          <!-- 标签 -->
          <view class="hero-tags">
            <view v-if="profile?.schoolName" class="hero-tag">
              <Icon name="map-pin" :size="9" class="hero-tag-icon" />
              <text class="hero-tag-text">{{ profile.schoolName }}</text>
            </view>
            <view v-if="profile?.major" class="hero-tag hero-tag--dim">
              <text class="hero-tag-text">{{ profile.major }}</text>
            </view>
          </view>

          <!-- inline 数据胶囊 -->
          <view class="hero-stats">
            <view
              v-for="s in quickStats"
              :key="s.key"
              class="hero-stat"
              @click="$emit('statClick', s.key)"
            >
              <text class="hero-stat-num">{{ s.val }}</text>
              <text class="hero-stat-lbl">{{ s.label }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 积分进度行 -->
      <view class="hero-footer">
        <view class="hero-pts" @click="$emit('pointsClick')">
          <text class="hero-pts-star">✦</text>
          <text class="hero-pts-num">{{ profile?.points?.toLocaleString() || '0' }}</text>
          <text class="hero-pts-unit">积分</text>
        </view>
        <text class="hero-pts-meta">{{ levelName }} · {{ progressPct }}%</text>
      </view>
      <!-- 积分条 -->
      <view class="hero-bar-track">
        <view class="hero-bar-fill" :style="{ width: progressPct + '%' }" />
      </view>

    </view>

    <!-- 底部白色圆弧过渡 -->
    <view class="hero-wave" />
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
  if (h < 6)  return '深夜 · 注意休息'
  if (h < 12) return '早上好'
  if (h < 14) return '午安'
  if (h < 18) return '下午好'
  return '晚上好'
})

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6)  return '夜深了，早点休息 👋'
  if (h < 12) return '元气满满，开始新的一天！'
  if (h < 14) return '记得吃饭补充能量~'
  if (h < 18) return '继续加油，你很棒！'
  return '今天辛苦了，好好休息'
})

const levelName = computed(() => {
  const lv = props.profile?.level || 1
  if (lv < 5)  return '校园新星'
  if (lv < 10) return '活跃学子'
  if (lv < 20) return '知识达人'
  if (lv < 30) return '互助先锋'
  return '校园传奇'
})

const nextLevelExp = computed(() => {
  const lv = props.profile?.level || 1
  return lv <= 10 ? lv * 200 : 2000 + (lv - 10) * 500
})

const progressPct = computed(() =>
  Math.min(Math.round(((props.profile?.points || 0) / nextLevelExp.value) * 100), 100)
)

const fmt = (n: number) => n >= 1000 ? (n / 1000).toFixed(1) + 'k' : String(n)

const quickStats = computed(() => [
  { key: 'resources', label: '资源', val: fmt(props.stats?.resourceCount ?? 0) },
  { key: 'answers',   label: '回答', val: fmt(props.stats?.answerCount   ?? 0) },
  { key: 'likes',     label: '获赞', val: fmt(props.stats?.likeCount     ?? 0) },
])
</script>

<style lang="scss" scoped>
@import '@/styles/design-tokens.scss';

/* ─── 容器 ─── */
.hero {
  position: relative;
  overflow: hidden;
}

/* ─── 背景 ─── */
.hero-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #2d3fa0 0%, #3a5bbf 40%, #5579d4 75%, #7a9ae0 100%);
}

/* 右上角高光球 */
.hero-bg-shine {
  position: absolute;
  top: -80rpx;
  right: -60rpx;
  width: 340rpx;
  height: 340rpx;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,255,255,0.13) 0%, transparent 65%);
  pointer-events: none;

  @media (min-width: 1024px) {
    width: 240px;
    height: 240px;
    top: -60px;
    right: -40px;
  }
}

/* ─── 内容包裹 ─── */
.hero-inner {
  position: relative;
  z-index: 2;
  padding: 0 32rpx;

  @media (min-width: 1024px) {
    max-width: 860px;
    margin: 0 auto;
    padding: 0;
  }
}

/* ─── 顶栏 ─── */
.hero-topbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding-top: 60rpx;
  padding-bottom: 22rpx;

  @media (min-width: 1024px) {
    padding-top: 36px;
    padding-bottom: 16px;
  }
}

.hero-greeting {
  display: flex;
  flex-direction: column;
  gap: 4rpx;

  @media (min-width: 1024px) { gap: 3px; }
}

.hero-hi {
  font-size: 22rpx;
  font-weight: 600;
  color: rgba(255,255,255,0.9);
  letter-spacing: 0.01em;
  line-height: 1;

  @media (min-width: 1024px) { font-size: 15px; }
}

.hero-hello {
  font-size: 20rpx;
  color: rgba(255,255,255,0.5);
  font-weight: 400;

  @media (min-width: 1024px) { font-size: 13px; }
}

.hero-edit-icon {
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: rgba(255,255,255,0.12);
  border: 1px solid rgba(255,255,255,0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255,255,255,0.8);
  cursor: pointer;
  transition: background 0.18s;
  flex-shrink: 0;

  &:active { background: rgba(255,255,255,0.22); }

  // #ifdef H5
  &:hover { background: rgba(255,255,255,0.2); }
  // #endif

  @media (min-width: 1024px) {
    width: 36px;
    height: 36px;
  }
}

/* ─── 身份区 ─── */
.hero-identity {
  display: flex;
  align-items: flex-end;
  gap: 20rpx;
  padding-bottom: 22rpx;

  @media (min-width: 1024px) {
    gap: 18px;
    padding-bottom: 16px;
  }
}

/* 头像 */
.hero-avatar-wrap {
  position: relative;
  flex-shrink: 0;
  cursor: pointer;
}

.hero-avatar {
  width: 152rpx;
  height: 152rpx;
  border-radius: 50%;
  border: 3px solid rgba(255,255,255,0.38);
  display: block;
  background: rgba(255,255,255,0.15);
  box-shadow: 0 6rpx 24rpx rgba(0,0,0,0.22);
  transition: opacity 0.2s;

  &:active { opacity: 0.85; }

  @media (min-width: 1024px) {
    width: 96px;
    height: 96px;
    border-width: 2.5px;
  }
}

/* 在线绿点 */
.hero-online {
  position: absolute;
  top: 8rpx;
  right: 4rpx;
  width: 18rpx;
  height: 18rpx;
  background: #4ade80;
  border-radius: 50%;
  border: 2.5px solid #3a5bbf;
  box-shadow: 0 0 0 2px rgba(74,222,128,0.28);

  @media (min-width: 1024px) {
    width: 12px;
    height: 12px;
    top: 5px;
    right: 3px;
  }
}

/* 等级 */
.hero-lv {
  position: absolute;
  bottom: -4rpx;
  right: -4rpx;
  padding: 4rpx 12rpx;
  background: linear-gradient(90deg, #f59e0b, #f97316);
  border-radius: 100rpx;
  border: 2px solid rgba(255,255,255,0.4);
  box-shadow: 0 2rpx 10rpx rgba(249,115,22,0.4);

  @media (min-width: 1024px) {
    padding: 2px 8px;
    bottom: -2px;
    right: -2px;
  }
}

.hero-lv-text {
  font-size: 18rpx;
  font-weight: 700;
  color: #fff;
  line-height: 1;
  letter-spacing: 0.02em;

  @media (min-width: 1024px) { font-size: 11px; }
}

/* 右侧信息 */
.hero-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  padding-bottom: 8rpx;

  @media (min-width: 1024px) {
    gap: 9px;
    padding-bottom: 4px;
  }
}

.hero-name {
  font-size: 44rpx;
  font-weight: 700;
  color: #fff;
  letter-spacing: -0.02em;
  line-height: 1.1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  @media (min-width: 1024px) { font-size: 28px; }
}

/* 标签行 */
.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 7rpx;

  @media (min-width: 1024px) { gap: 5px; }
}

.hero-tag {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  padding: 5rpx 12rpx;
  background: rgba(255,255,255,0.14);
  border: 1px solid rgba(255,255,255,0.2);
  border-radius: 100rpx;

  &--dim {
    background: rgba(255,255,255,0.07);
    border-color: rgba(255,255,255,0.1);
  }

  @media (min-width: 1024px) {
    padding: 3px 9px;
    gap: 3px;
    border-radius: 100px;
  }
}

.hero-tag-icon { color: rgba(255,255,255,0.6); flex-shrink: 0; }

.hero-tag-text {
  font-size: 19rpx;
  color: rgba(255,255,255,0.78);
  line-height: 1;

  @media (min-width: 1024px) { font-size: 11px; }
}

/* inline 数据 */
.hero-stats {
  display: flex;
  gap: 24rpx;

  @media (min-width: 1024px) { gap: 18px; }
}

.hero-stat {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
  cursor: pointer;

  @media (min-width: 1024px) { gap: 3px; }
}

.hero-stat-num {
  font-size: 30rpx;
  font-weight: 700;
  color: #fff;
  letter-spacing: -0.02em;
  line-height: 1;

  @media (min-width: 1024px) { font-size: 19px; }
}

.hero-stat-lbl {
  font-size: 19rpx;
  color: rgba(255,255,255,0.48);
  font-weight: 400;

  @media (min-width: 1024px) { font-size: 11px; }
}

/* ─── 积分 footer ─── */
.hero-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 0 10rpx;
  border-top: 1px solid rgba(255,255,255,0.12);

  @media (min-width: 1024px) {
    padding: 12px 0 8px;
  }
}

.hero-pts {
  display: flex;
  align-items: center;
  gap: 6rpx;
  cursor: pointer;
  transition: opacity 0.18s;

  &:active { opacity: 0.75; }

  // #ifdef H5
  &:hover { opacity: 0.82; }
  // #endif

  @media (min-width: 1024px) { gap: 5px; }
}

.hero-pts-star {
  font-size: 14rpx;
  color: #fbbf24;
  line-height: 1;

  @media (min-width: 1024px) { font-size: 11px; }
}

.hero-pts-num {
  font-size: 32rpx;
  font-weight: 700;
  color: #fbbf24;
  letter-spacing: -0.01em;
  line-height: 1;

  @media (min-width: 1024px) { font-size: 20px; }
}

.hero-pts-unit {
  font-size: 19rpx;
  color: rgba(255,255,255,0.38);
  font-weight: 400;

  @media (min-width: 1024px) { font-size: 12px; }
}

.hero-pts-meta {
  font-size: 19rpx;
  color: rgba(255,255,255,0.38);
  font-weight: 400;

  @media (min-width: 1024px) { font-size: 12px; }
}

/* ─── 积分进度条 ─── */
.hero-bar-track {
  width: 100%;
  height: 4rpx;
  background: rgba(255,255,255,0.12);
  border-radius: 100rpx;
  overflow: hidden;
  margin-bottom: 44rpx;

  @media (min-width: 1024px) {
    height: 3px;
    border-radius: 100px;
    margin-bottom: 36px;
  }
}

.hero-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #fbbf24, #f97316);
  border-radius: 100rpx;
  transition: width 0.8s cubic-bezier(0.4,0,0.2,1);
}

/* ─── 底部白色圆弧过渡 ─── */
.hero-wave {
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
