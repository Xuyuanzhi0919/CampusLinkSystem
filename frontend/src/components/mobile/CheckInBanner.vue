<template>
  <view class="checkin-card" :class="{ 'is-done': isCheckedIn }">

    <!-- 积分飘字 -->
    <view v-if="showPointsAnim" class="pts-pop">+{{ earnedPoints }}</view>

    <!-- 右上角装饰光晕（未签到时） -->
    <view v-if="!isCheckedIn" class="glow-orb" />

    <!-- ── 主体一行 ── -->
    <view class="card-row">

      <!-- 火焰图标 -->
      <view class="flame-wrap" :class="{ 'flame-wrap--done': isCheckedIn }">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 2C12 2 10 6 7 8C4 10 3 13 5 16C6.5 18.5 9 20 12 20C15 20 17.5 18.5 19 16C21 13 20 10 17 8C16 7 15 5.5 15 4C14 5.5 13.5 7 14 9C13 8 12.5 6 12 2Z"
            stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M12 14C12 14 10.5 13 10 11.5C9.5 13 10 14.5 11 15.5C11.5 16 12 16 12 16C12 16 12.5 16 13 15.5C14 14.5 14.5 13 14 11.5C13.5 13 12 14 12 14Z"
            stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </view>

      <!-- 文字 + 进度条 -->
      <view class="card-info">
        <!-- 天数行 -->
        <view class="streak-row">
          <text class="streak-num">{{ consecutiveDays }}</text>
          <text class="streak-unit">天</text>
          <text class="streak-desc">连续签到</text>
        </view>

        <!-- 进度条 -->
        <view class="progress-row">
          <view class="bar-track">
            <view class="bar-fill" :style="{ width: progressPct + '%' }" />
          </view>
          <text class="bar-label">{{ cycleDay }}/7天</text>
        </view>

        <!-- 激励文案 -->
        <text class="reward-hint">{{ rewardHint }}</text>
      </view>

      <!-- 签到按钮 -->
      <view class="btn-area" @click="handleClick">
        <view v-if="!isCheckedIn" class="sign-btn" :class="{ 'sign-btn--busy': checking }">
          <view v-if="checking" class="spinner" />
          <svg v-else class="sign-icon" viewBox="0 0 16 16" fill="none">
            <path d="M3 8.5L6.5 12L13 5" stroke="currentColor" stroke-width="2.2"
              stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <text class="sign-text">{{ checking ? '签到中' : `签到\n+${signinPoints}` }}</text>
        </view>
        <view v-else class="done-btn">
          <svg class="done-icon" viewBox="0 0 16 16" fill="none">
            <path d="M3 8.5L6.5 12L13 5" stroke="currentColor" stroke-width="2.2"
              stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <text class="done-text">已签到</text>
        </view>
      </view>

    </view>

    <!-- ── 底部提示行 ── -->
    <view class="card-footer">
      <text class="footer-text">{{ footerText }}</text>
      <view class="footer-link" @click.stop="goGrowth">
        <text class="footer-link-text">成长明细</text>
        <svg viewBox="0 0 10 10" fill="none" class="footer-arrow">
          <path d="M3.5 7.5L6.5 5L3.5 2.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
        </svg>
      </view>
    </view>

  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { checkIn } from '@/services/user'
import { getPublicConfig } from '@/services/config'

const props = defineProps<{
  isCheckedIn: boolean
  consecutiveDays: number
  signinPoints?: number
}>()

const emit = defineEmits<{
  checkInSuccess: [{ consecutiveDays: number; pointsEarned: number }]
}>()

// 签到积分：优先用 prop，其次从公开配置读取，最后降级到 10
const configSigninPoints = ref(10)
const signinPoints = computed(() => props.signinPoints ?? configSigninPoints.value)
const checking = ref(false)
const showPointsAnim = ref(false)
const earnedPoints = ref(signinPoints.value)

onMounted(async () => {
  if (props.signinPoints != null) return // prop 已提供，无需请求
  try {
    const cfg = await getPublicConfig()
    if (cfg?.['points.daily_signin']) {
      configSigninPoints.value = Math.abs(parseInt(cfg['points.daily_signin']))
      earnedPoints.value = configSigninPoints.value
    }
  } catch {
    // 降级使用默认值 10
  }
})

/** 当前 7 天周期内第几天（1~7） */
const cycleDay = computed(() => {
  const d = props.consecutiveDays % 7
  return d === 0 && props.consecutiveDays > 0 ? 7 : d
})

/** 进度百分比 */
const progressPct = computed(() => {
  const pct = (cycleDay.value / 7) * 100
  return props.isCheckedIn ? pct : Math.max(pct - 2, 0) // 未签到时略留余量感
})

/** 主体激励文案 */
const rewardHint = computed(() => {
  if (props.isCheckedIn) {
    const left = 7 - cycleDay.value
    if (left === 0) return '🎉 本周期全勤达成！'
    return `再坚持 ${left} 天解锁专属徽章`
  }
  const left = 7 - cycleDay.value
  if (left <= 0) return '🏆 今日签到即可解锁徽章！'
  return `再签 ${left} 天可解锁签到达人`
})

/** 底部小字 */
const footerText = computed(() => {
  if (props.isCheckedIn) return `今日已签到 · 明日可领 +${signinPoints} 积分`
  return `每日签到 +${signinPoints} 积分 · 连签 7 天得徽章`
})

const handleClick = async () => {
  if (props.isCheckedIn || checking.value) return
  checking.value = true
  try {
    const res = await checkIn()
    earnedPoints.value = res?.pointsEarned ?? signinPoints.value
    showPointsAnim.value = true
    setTimeout(() => { showPointsAnim.value = false }, 1400)
    emit('checkInSuccess', { consecutiveDays: props.consecutiveDays + 1, pointsEarned: earnedPoints.value })
    uni.showToast({ title: `签到成功 +${earnedPoints.value} 积分 🎉`, icon: 'success' })
  } catch (err: any) {
    uni.showToast({ title: err?.message || '签到失败，请稍后再试', icon: 'none' })
  } finally {
    checking.value = false
  }
}

const goGrowth = () => {
  uni.navigateTo({ url: '/pages/user/points-history' })
}
</script>

<style scoped lang="scss">
// ── 卡片容器
.checkin-card {
  position: relative;
  margin: 0 14px 4px;
  padding: 13px 14px 10px;
  border-radius: 18px;
  background: linear-gradient(145deg, #FFFAF5 0%, #FFF7ED 100%);
  border: 1px solid rgba(249, 115, 22, 0.22);
  box-shadow: 0 2px 14px rgba(249, 115, 22, 0.1);
  overflow: hidden;

  // 已签到：切换为成功绿色调
  &.is-done {
    background: linear-gradient(145deg, #F0FDF4 0%, #DCFCE7 100%);
    border-color: rgba(22, 163, 74, 0.2);
    box-shadow: 0 2px 10px rgba(22, 163, 74, 0.08);
  }
}

// 右上角暖色光晕
.glow-orb {
  position: absolute;
  top: -18px;
  right: -18px;
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(249, 115, 22, 0.14) 0%, transparent 70%);
  pointer-events: none;
}

// ── 积分飘字
.pts-pop {
  position: absolute;
  top: 8px;
  right: 68px;
  font-size: 18px;
  font-weight: 900;
  color: #16A34A;
  pointer-events: none;
  animation: popUp 1.4s ease-out forwards;
  z-index: 10;
}
@keyframes popUp {
  0%   { opacity: 0; transform: translateY(6px) scale(0.8); }
  20%  { opacity: 1; transform: translateY(0) scale(1.25); }
  60%  { opacity: 1; transform: translateY(-24px) scale(1.1); }
  100% { opacity: 0; transform: translateY(-44px) scale(0.9); }
}

// ── 主体一行
.card-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

// ── 火焰图标
.flame-wrap {
  width: 42px;
  height: 42px;
  border-radius: 13px;
  background: linear-gradient(135deg, #FEF3C7 0%, #FDE68A 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(217, 119, 6, 0.22);

  svg {
    width: 22px;
    height: 22px;
    color: #D97706;
  }

  &--done {
    background: #D1FAE5;
    box-shadow: 0 2px 8px rgba(22, 163, 74, 0.15);
    svg { color: #16A34A; }
  }
}

// ── 文字信息区
.card-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

// 天数行
.streak-row {
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.streak-num {
  font-size: 26px;
  font-weight: 900;
  color: #EA580C;
  line-height: 1;
  letter-spacing: -0.5px;

  .is-done & { color: #16A34A; }
}

.streak-unit {
  font-size: 14px;
  font-weight: 700;
  color: #EA580C;
  margin-right: 2px;

  .is-done & { color: #16A34A; }
}

.streak-desc {
  font-size: 12px;
  color: #9CA3AF;
  font-weight: 500;
}

// 进度条
.progress-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.bar-track {
  flex: 1;
  height: 5px;
  background: rgba(0, 0, 0, 0.07);
  border-radius: 3px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 3px;
  background: linear-gradient(to right, #F97316, #FBBF24);
  transition: width 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);

  .is-done & {
    background: linear-gradient(to right, #16A34A, #4ADE80);
  }
}

.bar-label {
  font-size: 10.5px;
  color: #9CA3AF;
  font-weight: 600;
  white-space: nowrap;
  flex-shrink: 0;
}

// 激励文案
.reward-hint {
  font-size: 11.5px;
  color: #6B7280;
  line-height: 1.35;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

// ── 签到按钮区
.btn-area {
  flex-shrink: 0;
}

.sign-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  width: 52px;
  height: 52px;
  background: linear-gradient(145deg, #F97316 0%, #EA580C 100%);
  border-radius: 16px;
  box-shadow: 0 4px 14px rgba(249, 115, 22, 0.42),
              inset 0 1px 0 rgba(255, 255, 255, 0.2);
  cursor: pointer;
  transition: transform 0.14s ease, box-shadow 0.14s ease;

  &:active {
    transform: scale(0.91);
    box-shadow: 0 2px 6px rgba(249, 115, 22, 0.28);
  }

  &--busy { opacity: 0.72; }
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.35);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.sign-icon {
  width: 16px;
  height: 16px;
  color: #fff;
}

.sign-text {
  font-size: 10px;
  font-weight: 700;
  color: #fff;
  text-align: center;
  line-height: 1.2;
  white-space: pre;  // 保留 \n 换行
}

.done-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  width: 52px;
  height: 52px;
  background: #DCFCE7;
  border-radius: 16px;
}

.done-icon {
  width: 16px;
  height: 16px;
  color: #16A34A;
}

.done-text {
  font-size: 10px;
  font-weight: 600;
  color: #16A34A;
  white-space: nowrap;
}

// ── 底部提示行
.card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 9px;
  padding-top: 8px;
  border-top: 1px solid rgba(249, 115, 22, 0.1);

  .is-done & {
    border-top-color: rgba(22, 163, 74, 0.12);
  }
}

.footer-text {
  font-size: 11px;
  color: #9CA3AF;
  line-height: 1.4;
}

.footer-link {
  display: flex;
  align-items: center;
  gap: 1px;
  cursor: pointer;
  flex-shrink: 0;
}

.footer-link-text {
  font-size: 11px;
  font-weight: 500;
  color: #F97316;

  .is-done & { color: #16A34A; }
}

.footer-arrow {
  width: 10px;
  height: 10px;
  color: #F97316;

  .is-done & { color: #16A34A; }
}
</style>
