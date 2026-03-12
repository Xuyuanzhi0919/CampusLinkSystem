<template>
  <view class="checkin-card" :class="{ 'is-done': isCheckedIn }">

    <!-- 积分飘字动画 -->
    <view v-if="showPointsAnim" class="points-float">+10</view>

    <!-- ── 主体行 ── -->
    <view class="card-main">

      <!-- 左：图标 + 天数 + 进度点 -->
      <view class="card-left">
        <view class="flame-badge" :class="{ 'flame-badge--done': isCheckedIn }">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2C12 2 10 6 7 8C4 10 3 13 5 16C6.5 18.5 9 20 12 20C15 20 17.5 18.5 19 16C21 13 20 10 17 8C16 7 15 5.5 15 4C14 5.5 13.5 7 14 9C13 8 12.5 6 12 2Z"
              stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M12 14C12 14 10.5 13 10 11.5C9.5 13 10 14.5 11 15.5C11.5 16 12 16 12 16C12 16 12.5 16 13 15.5C14 14.5 14.5 13 14 11.5C13.5 13 12 14 12 14Z"
              stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </view>

        <view class="card-info">
          <!-- 天数 -->
          <view class="streak-row">
            <text class="streak-num">{{ consecutiveDays }}</text>
            <text class="streak-label">天连续签到</text>
          </view>
          <!-- 7 天周期进度点 -->
          <view class="progress-dots">
            <view
              v-for="i in 7"
              :key="i"
              class="dot"
              :class="{
                'dot--filled': i <= cycleDay,
                'dot--today': i === cycleDay && !isCheckedIn,
                'dot--done': i <= cycleDay && isCheckedIn
              }"
            />
          </view>
        </view>
      </view>

      <!-- 右：签到按钮 -->
      <view class="btn-wrap" @click="handleClick">
        <!-- 未签到 -->
        <view v-if="!isCheckedIn" class="sign-btn" :class="{ 'sign-btn--loading': checking }">
          <view v-if="checking" class="btn-spinner" />
          <svg v-else class="btn-icon" viewBox="0 0 16 16" fill="none">
            <path d="M2 8.5L6 12.5L14 4" stroke="currentColor" stroke-width="2.2"
              stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <text class="btn-text">{{ checking ? '签到中…' : '立即签到' }}</text>
        </view>
        <!-- 已签到 -->
        <view v-else class="done-btn">
          <svg class="done-icon" viewBox="0 0 16 16" fill="none">
            <path d="M2 8.5L6 12.5L14 4" stroke="currentColor" stroke-width="2.2"
              stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <text class="done-text">已签到</text>
        </view>
      </view>
    </view>

    <!-- ── 底部提示行 ── -->
    <view class="card-hint">
      <svg class="hint-icon" viewBox="0 0 12 12" fill="none">
        <circle cx="6" cy="6" r="5" stroke="currentColor" stroke-width="1.2"/>
        <path d="M6 5v3M6 3.5v.5" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
      </svg>
      <text class="hint-text">{{ hintText }}</text>
      <!-- 成长明细入口 -->
      <view class="hint-link" @click.stop="goGrowth">
        <text class="hint-link-text">成长明细</text>
        <svg viewBox="0 0 10 10" fill="none" class="hint-link-arrow">
          <path d="M3.5 7.5L6.5 5L3.5 2.5" stroke="currentColor" stroke-width="1.4" stroke-linecap="round"/>
        </svg>
      </view>
    </view>

  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { checkIn } from '@/services/user'

const props = defineProps<{
  isCheckedIn: boolean
  consecutiveDays: number
}>()

const emit = defineEmits<{
  checkInSuccess: [{ consecutiveDays: number }]
}>()

const checking = ref(false)
const showPointsAnim = ref(false)

/** 当前 7 天周期内的第几天（1~7） */
const cycleDay = computed(() => {
  const d = props.consecutiveDays % 7
  return d === 0 && props.consecutiveDays > 0 ? 7 : d
})

/** 底部提示文案 */
const hintText = computed(() => {
  if (props.isCheckedIn) {
    const left = 7 - cycleDay.value
    if (left === 0) return '🎉 已完成本周期！明天开始新一轮'
    return `再签 ${left} 天解锁「签到达人」徽章 · 明日 +10 积分`
  }
  const left = 7 - cycleDay.value
  if (left <= 0) return '🎯 完成今日签到，即可解锁本周期徽章！'
  return `再签 ${left} 天解锁「签到达人」徽章`
})

const handleClick = async () => {
  if (props.isCheckedIn || checking.value) return
  checking.value = true
  try {
    await checkIn()
    // 飘字动画
    showPointsAnim.value = true
    setTimeout(() => { showPointsAnim.value = false }, 1200)
    emit('checkInSuccess', { consecutiveDays: props.consecutiveDays + 1 })
    uni.showToast({ title: '签到成功 +10 积分 🎉', icon: 'success' })
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
  margin: 0 14px 10px;
  padding: 14px 14px 11px;
  border-radius: 18px;
  background: linear-gradient(135deg, #EEF4FF 0%, #F0FAFA 100%);
  border: 1px solid rgba(37, 99, 235, 0.14);
  box-shadow: 0 2px 12px rgba(37, 99, 235, 0.07);
  overflow: hidden;

  // 已签到：淡出为中性色
  &.is-done {
    background: #F9FAFB;
    border-color: #E5E7EB;
    box-shadow: none;
  }

  // 右上角装饰光晕
  &::before {
    content: '';
    position: absolute;
    top: -20px;
    right: -20px;
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(37, 99, 235, 0.08) 0%, transparent 70%);
    pointer-events: none;
  }
  &.is-done::before { display: none; }
}

// ── 积分飘字动画
.points-float {
  position: absolute;
  top: 10px;
  right: 70px;
  font-size: 20px;
  font-weight: 800;
  color: #16A34A;
  pointer-events: none;
  animation: floatUp 1.2s ease-out forwards;
  z-index: 10;
}
@keyframes floatUp {
  0%   { opacity: 1; transform: translateY(0) scale(1); }
  60%  { opacity: 1; transform: translateY(-28px) scale(1.2); }
  100% { opacity: 0; transform: translateY(-48px) scale(0.9); }
}

// ── 主体行
.card-main {
  display: flex;
  align-items: center;
  gap: 10px;
}

// ── 左侧
.card-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.flame-badge {
  width: 38px;
  height: 38px;
  border-radius: 11px;
  background: linear-gradient(135deg, #FEF3C7, #FDE68A);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(217, 119, 6, 0.2);

  svg {
    width: 20px;
    height: 20px;
    color: #D97706;
  }

  &--done {
    background: #F3F4F6;
    box-shadow: none;
    svg { color: #9CA3AF; }
  }
}

.card-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.streak-row {
  display: flex;
  align-items: baseline;
  gap: 3px;
}

.streak-num {
  font-size: 22px;
  font-weight: 800;
  color: #1D4ED8;
  line-height: 1;

  .is-done & { color: #6B7280; }
}

.streak-label {
  font-size: 12px;
  color: #6B7280;
  font-weight: 500;
}

// ── 7 天进度点
.progress-dots {
  display: flex;
  align-items: center;
  gap: 5px;
}

.dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #D1D5DB;
  transition: background 0.2s, transform 0.2s;

  &--filled {
    background: #93C5FD;
  }

  &--today {
    background: #2563EB;
    transform: scale(1.3);
    box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.2);
  }

  &--done {
    background: #6B7280;
  }
}

// ── 按钮区
.btn-wrap {
  flex-shrink: 0;
}

.sign-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 16px;
  background: linear-gradient(135deg, #2563EB 0%, #4F46E5 100%);
  border-radius: 22px;
  box-shadow: 0 3px 10px rgba(37, 99, 235, 0.35);
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;

  &:active { transform: scale(0.94); box-shadow: 0 1px 4px rgba(37, 99, 235, 0.2); }
  &--loading { opacity: 0.75; }
}

.btn-spinner {
  width: 13px;
  height: 13px;
  border: 2px solid rgba(255,255,255,0.35);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.btn-icon {
  width: 13px;
  height: 13px;
  color: #fff;
}

.btn-text {
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
}

.done-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 14px;
  background: #F3F4F6;
  border-radius: 22px;
}

.done-icon {
  width: 13px;
  height: 13px;
  color: #9CA3AF;
}

.done-text {
  font-size: 13px;
  font-weight: 500;
  color: #9CA3AF;
  white-space: nowrap;
}

// ── 底部提示行
.card-hint {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 10px;
  padding-top: 9px;
  border-top: 1px solid rgba(37, 99, 235, 0.08);

  .is-done & {
    border-top-color: #E5E7EB;
  }
}

.hint-icon {
  width: 11px;
  height: 11px;
  color: #93C5FD;
  flex-shrink: 0;

  .is-done & { color: #D1D5DB; }
}

.hint-text {
  flex: 1;
  font-size: 11px;
  color: #6B7280;
  line-height: 1.4;
  min-width: 0;
}

.hint-link {
  display: flex;
  align-items: center;
  gap: 1px;
  flex-shrink: 0;
  cursor: pointer;
}

.hint-link-text {
  font-size: 11px;
  color: #93C5FD;
  font-weight: 500;

  .is-done & { color: #9CA3AF; }
}

.hint-link-arrow {
  width: 10px;
  height: 10px;
  color: #93C5FD;

  .is-done & { color: #9CA3AF; }
}
</style>
