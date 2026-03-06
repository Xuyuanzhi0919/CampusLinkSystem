<template>
  <!-- ====== PC 端：紧凑横向 Profile Header ====== -->
  <view v-if="isDesktop" class="hero hero--pc">
    <view class="hero-bg" />
    <!-- 装饰：右上角高光球（与移动端一致的视觉语言） -->
    <view class="hero-pc-deco" />
    <!-- 装饰：左下角柔光晕 -->
    <view class="hero-pc-deco hero-pc-deco--lt" />
    <view class="hero-pc-inner">
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

      <!-- 文字信息 -->
      <view class="hero-pc-info">
        <text class="hero-pc-greeting">{{ timeLabel }}</text>
        <text class="hero-pc-name">{{ profile?.nickname || '未设置昵称' }}</text>
        <view class="hero-tags">
          <view v-if="profile?.schoolName" class="hero-tag">
            <Icon name="map-pin" :size="9" class="hero-tag-icon" />
            <text class="hero-tag-text">{{ profile.schoolName }}</text>
          </view>
          <view v-if="profile?.major" class="hero-tag hero-tag--dim">
            <text class="hero-tag-text">{{ profile.major }}</text>
          </view>
        </view>
      </view>

      <!-- 竖向分隔线 -->
      <view class="hero-pc-sep" />

      <!-- 统计数据 -->
      <view class="hero-pc-stats">
        <view
          v-for="s in quickStats"
          :key="s.key"
          class="hero-pc-stat"
          @click="$emit('statClick', s.key)"
        >
          <text class="hero-pc-stat-num">{{ s.val }}</text>
          <text class="hero-pc-stat-lbl">{{ s.label }}</text>
        </view>
      </view>

      <!-- 竖向分隔线 -->
      <view class="hero-pc-sep" />

      <!-- 积分 + 进度条 -->
      <view class="hero-pc-pts">
        <view class="hero-pc-pts-top">
          <view class="hero-pc-pts-left" @click="$emit('pointsClick')">
            <text class="hero-pc-pts-star">✦</text>
            <text class="hero-pc-pts-num">{{ profile?.points?.toLocaleString() || '0' }}</text>
            <text class="hero-pc-pts-unit">积分</text>
          </view>
          <text class="hero-pc-pts-meta">{{ levelName }}</text>
        </view>
        <view class="hero-pc-bar-row">
          <view class="hero-pc-bar-track">
            <view class="hero-pc-bar-fill" :style="{ width: progressPct + '%' }" />
          </view>
          <text class="hero-pc-pct">{{ progressPct }}%</text>
        </view>
      </view>

      <!-- 编辑按钮 -->
      <view class="hero-pc-edit-btn" @click="$emit('editProfile')">
        <Icon name="edit-2" :size="14" />
      </view>
    </view>
  </view>

  <!-- ====== 移动端：全宽沉浸式 Banner ====== -->
  <view v-else class="hero">
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
  isDesktop?: boolean
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

/* ================================================================
   移动端：全宽沉浸式 Banner
   ================================================================ */

/* ─── 容器 ─── */
.hero {
  display: block;
  position: relative;
  overflow: hidden;
  width: 100%;
}

/* ─── 背景 ─── */
.hero-bg {
  display: block;
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #2d3fa0 0%, #3a5bbf 40%, #5579d4 75%, #7a9ae0 100%);
}

/* 右上角高光球（仅移动端） */
.hero-bg-shine {
  position: absolute;
  top: -80rpx;
  right: -60rpx;
  width: 340rpx;
  height: 340rpx;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,255,255,0.13) 0%, transparent 65%);
  pointer-events: none;
}

/* ─── 内容包裹 ─── */
.hero-inner {
  display: block;
  position: relative;
  z-index: 2;
  padding: 0 32rpx;
}

/* ─── 顶栏 ─── */
.hero-topbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding-top: 56px;
  padding-bottom: 22rpx;

  @media (max-width: 375px) {
    padding-top: 48px;
  }
}

.hero-greeting {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.hero-hi {
  font-size: 22rpx;
  font-weight: 600;
  color: rgba(255,255,255,0.9);
  letter-spacing: 0.01em;
  line-height: 1;
}

.hero-hello {
  font-size: 20rpx;
  color: rgba(255,255,255,0.5);
  font-weight: 400;
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
}

/* ─── 身份区 ─── */
.hero-identity {
  display: flex;
  align-items: flex-end;
  gap: 22rpx;
  padding-bottom: 26rpx;
}

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
}

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
}

.hero-lv {
  position: absolute;
  bottom: -4rpx;
  right: -4rpx;
  padding: 4rpx 12rpx;
  background: linear-gradient(90deg, #f59e0b, #f97316);
  border-radius: 100rpx;
  border: 2px solid rgba(255,255,255,0.4);
  box-shadow: 0 2rpx 10rpx rgba(249,115,22,0.4);
}

.hero-lv-text {
  font-size: 18rpx;
  font-weight: 700;
  color: #fff;
  line-height: 1;
  letter-spacing: 0.02em;
}

.hero-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 14rpx;
  padding-bottom: 8rpx;
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
}

/* 标签行 */
.hero-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 7rpx;
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
}

.hero-tag-icon { color: rgba(255,255,255,0.6); flex-shrink: 0; }

.hero-tag-text {
  font-size: 19rpx;
  color: rgba(255,255,255,0.78);
  line-height: 1;
}

/* inline 数据 */
.hero-stats {
  display: flex;
  gap: 24rpx;
}

.hero-stat {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
  cursor: pointer;
}

.hero-stat-num {
  font-size: 30rpx;
  font-weight: 700;
  color: #fff;
  letter-spacing: -0.02em;
  line-height: 1;
}

.hero-stat-lbl {
  font-size: 19rpx;
  color: rgba(255,255,255,0.48);
  font-weight: 400;
}

/* ─── 积分 footer ─── */
.hero-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 0 10rpx;
  border-top: 1px solid rgba(255,255,255,0.12);
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
}

.hero-pts-star {
  font-size: 14rpx;
  color: #fbbf24;
  line-height: 1;
}

.hero-pts-num {
  font-size: 32rpx;
  font-weight: 700;
  color: #fbbf24;
  letter-spacing: -0.01em;
  line-height: 1;
}

.hero-pts-unit {
  font-size: 19rpx;
  color: rgba(255,255,255,0.38);
  font-weight: 400;
}

.hero-pts-meta {
  font-size: 19rpx;
  color: rgba(255,255,255,0.38);
  font-weight: 400;
}

/* ─── 积分进度条 ─── */
.hero-bar-track {
  width: 100%;
  height: 5rpx;
  background: rgba(255,255,255,0.14);
  border-radius: 100rpx;
  overflow: hidden;
  margin-bottom: 50rpx;
}

.hero-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #fbbf24, #f97316);
  border-radius: 100rpx;
  transition: width 0.8s cubic-bezier(0.4,0,0.2,1);
}

/* ─── 底部白色圆弧过渡 ─── */
.hero-wave {
  display: block;
  position: relative;
  z-index: 3;
  height: 52rpx;
  background: $color-bg-page;
  border-radius: 52rpx 52rpx 0 0;
  margin-top: -16rpx;
}


/* ================================================================
   PC 端：紧凑横向 Profile Header
   ================================================================ */

.hero--pc {
  display: block;
  position: relative;
  overflow: hidden;
  width: 100%;
  box-shadow: 0 4px 24px rgba(30, 58, 138, 0.18);
}

/* PC 装饰光晕：右上角主球 + 左下角副晕 */
.hero-pc-deco {
  position: absolute;
  top: -60px;
  right: 10%;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.13) 0%, transparent 65%);
  pointer-events: none;
  z-index: 1;

  &--lt {
    top: auto;
    bottom: -80px;
    right: auto;
    left: 8%;
    width: 200px;
    height: 200px;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.06) 0%, transparent 65%);
  }
}

/* 内容行：max-width 居中，水平 flex
   padding-top: 80px = 76px WebHeader 高度 + 4px 喘息空间
   渐变背景从 y=0 填充（覆盖 header 底层区域），内容从 header 下方开始 */
.hero-pc-inner {
  position: relative;
  z-index: 2;
  max-width: 1080px;
  margin: 0 auto;
  padding: 80px 24px 18px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  gap: 20px;
}

/* PC 头像 —— 覆盖移动端 rpx 尺寸 */
.hero--pc .hero-avatar-wrap {
  flex-shrink: 0;
}

.hero--pc .hero-avatar {
  width: 72px;
  height: 72px;
  border-width: 2.5px;
  box-shadow: 0 4px 14px rgba(0,0,0,0.2);
}

.hero--pc .hero-online {
  width: 12px;
  height: 12px;
  top: 4px;
  right: 2px;
  border-width: 2px;
}

.hero--pc .hero-lv {
  padding: 2px 8px;
  bottom: -3px;
  right: -3px;
  border-radius: 100px;
}

.hero--pc .hero-lv-text {
  font-size: 11px;
}

/* PC 文字信息列 */
.hero-pc-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.hero-pc-greeting {
  font-size: 12px;
  color: rgba(255,255,255,0.52);
  font-weight: 400;
  line-height: 1;
}

.hero-pc-name {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  letter-spacing: -0.02em;
  line-height: 1.15;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* PC 标签继承移动端样式，仅覆盖尺寸 */
.hero--pc .hero-tags {
  gap: 5px;
  flex-wrap: nowrap;
  overflow: hidden;
}

.hero--pc .hero-tag {
  padding: 3px 9px;
  gap: 3px;
  border-radius: 100px;
  flex-shrink: 0;
}

.hero--pc .hero-tag-text {
  font-size: 11px;
}

/* 竖向分隔线 */
.hero-pc-sep {
  width: 1px;
  height: 44px;
  background: rgba(255,255,255,0.15);
  flex-shrink: 0;
}

/* PC 统计数据 */
.hero-pc-stats {
  display: flex;
  align-items: stretch;
  flex-shrink: 0;
}

.hero-pc-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  cursor: pointer;
  padding: 6px 22px;
  border-radius: 8px;
  transition: background 0.18s;

  &:not(:last-child) {
    border-right: 1px solid rgba(255,255,255,0.1);
  }

  &:active { background: rgba(255,255,255,0.1); }

  // #ifdef H5
  &:hover { background: rgba(255,255,255,0.08); }
  // #endif
}

.hero-pc-stat-num {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  letter-spacing: -0.02em;
  line-height: 1;
}

.hero-pc-stat-lbl {
  font-size: 11px;
  color: rgba(255,255,255,0.48);
  font-weight: 400;
}

/* PC 积分区 */
.hero-pc-pts {
  display: flex;
  flex-direction: column;
  gap: 7px;
  flex-shrink: 0;
  min-width: 168px;
}

.hero-pc-pts-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.hero-pc-pts-left {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  transition: opacity 0.18s;

  &:active { opacity: 0.75; }

  // #ifdef H5
  &:hover { opacity: 0.82; }
  // #endif
}

.hero-pc-pts-star {
  font-size: 11px;
  color: #fbbf24;
  line-height: 1;
}

.hero-pc-pts-num {
  font-size: 19px;
  font-weight: 700;
  color: #fbbf24;
  letter-spacing: -0.01em;
  line-height: 1;
}

.hero-pc-pts-unit {
  font-size: 12px;
  color: rgba(255,255,255,0.38);
  font-weight: 400;
}

.hero-pc-pts-meta {
  font-size: 11px;
  color: rgba(255,255,255,0.42);
  font-weight: 400;
  white-space: nowrap;
}

.hero-pc-bar-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.hero-pc-bar-track {
  flex: 1;
  height: 4px;
  background: rgba(255,255,255,0.14);
  border-radius: 100px;
  overflow: hidden;
}

.hero-pc-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #fbbf24, #f97316);
  border-radius: 100px;
  transition: width 0.8s cubic-bezier(0.4,0,0.2,1);
}

.hero-pc-pct {
  font-size: 11px;
  color: rgba(255,255,255,0.42);
  font-weight: 400;
  white-space: nowrap;
}

/* PC 编辑按钮 */
.hero-pc-edit-btn {
  width: 34px;
  height: 34px;
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
}
</style>
