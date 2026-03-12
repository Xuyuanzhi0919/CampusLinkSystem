<template>
  <view class="checkin-banner" :class="{ 'is-done': isCheckedIn }" @click="handleClick">
    <!-- 左侧：连续天数 -->
    <view class="banner-left">
      <view class="flame-wrap">
        <svg class="flame-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 2C12 2 10 6 7 8C4 10 3 13 5 16C6.5 18.5 9 20 12 20C15 20 17.5 18.5 19 16C21 13 20 10 17 8C16 7 15 5.5 15 4C14 5.5 13.5 7 14 9C13 8 12.5 6 12 2Z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M12 14C12 14 10.5 13 10 11.5C9.5 13 10 14.5 11 15.5C11.5 16 12 16 12 16C12 16 12.5 16 13 15.5C14 14.5 14.5 13 14 11.5C13.5 13 12 14 12 14Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </view>
      <view class="banner-text">
        <text class="days-label">连续签到</text>
        <text class="days-num">{{ consecutiveDays }}</text>
        <text class="days-unit">天</text>
      </view>
    </view>

    <!-- 右侧：按钮 -->
    <view class="banner-right">
      <view v-if="!isCheckedIn" class="checkin-btn" :class="{ loading: checking }">
        <svg v-if="!checking" class="btn-icon" viewBox="0 0 16 16" fill="none">
          <path d="M3 8L6.5 11.5L13 4.5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <view v-else class="btn-spinner" />
        <text class="btn-text">{{ checking ? '签到中' : '签到 +10' }}</text>
      </view>
      <view v-else class="done-badge">
        <svg class="done-icon" viewBox="0 0 16 16" fill="none">
          <path d="M3 8L6.5 11.5L13 4.5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <text class="done-text">已签到</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { checkIn } from '@/services/user'

const props = defineProps<{
  isCheckedIn: boolean
  consecutiveDays: number
}>()

const emit = defineEmits<{
  checkInSuccess: [{ consecutiveDays: number }]
}>()

const checking = ref(false)

const handleClick = async () => {
  if (props.isCheckedIn || checking.value) return
  checking.value = true
  try {
    await checkIn()
    emit('checkInSuccess', { consecutiveDays: props.consecutiveDays + 1 })
    uni.showToast({ title: '签到成功 +10 积分', icon: 'success' })
  } catch (err: any) {
    uni.showToast({ title: err?.message || '签到失败', icon: 'none' })
  } finally {
    checking.value = false
  }
}
</script>

<style scoped lang="scss">
.checkin-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 0 14px 10px;
  padding: 10px 14px;
  background: linear-gradient(135deg, #EFF6FF 0%, #F0FDF4 100%);
  border: 1px solid #DBEAFE;
  border-radius: 14px;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;

  &:active:not(.is-done) {
    transform: scale(0.98);
    box-shadow: 0 2px 8px rgba(37, 99, 235, 0.12);
  }

  &.is-done {
    background: #F9FAFB;
    border-color: #E5E7EB;
    cursor: default;
  }
}

// ── 左侧
.banner-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.flame-wrap {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #FEF3C7, #FDE68A);
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .is-done & {
    background: #F3F4F6;
  }
}

.flame-icon {
  width: 17px;
  height: 17px;
  color: #D97706;

  .is-done & { color: #9CA3AF; }
}

.banner-text {
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.days-label {
  font-size: 12px;
  color: #6B7280;

  .is-done & { color: #9CA3AF; }
}

.days-num {
  font-size: 18px;
  font-weight: 700;
  color: #1D4ED8;
  line-height: 1;
  margin: 0 1px;

  .is-done & { color: #9CA3AF; }
}

.days-unit {
  font-size: 12px;
  color: #6B7280;

  .is-done & { color: #9CA3AF; }
}

// ── 右侧按钮
.checkin-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  background: linear-gradient(135deg, #2563EB 0%, #4F46E5 100%);
  border-radius: 20px;
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.3);

  &.loading {
    opacity: 0.75;
  }
}

.btn-icon {
  width: 13px;
  height: 13px;
  color: #fff;
}

.btn-spinner {
  width: 13px;
  height: 13px;
  border: 2px solid rgba(255,255,255,0.35);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.btn-text {
  font-size: 13px;
  font-weight: 600;
  color: #fff;
}

.done-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: #F3F4F6;
  border-radius: 20px;
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
}
</style>
