<template>
  <view class="best-answer-badge">
    <view class="badge-chip">
      <ClIcon name="icon-crown" size="sm" class="badge-chip__icon" />
      <text class="badge-chip__label">最佳答案</text>
    </view>
    <text class="badge-subtitle">提问者已采纳</text>
  </view>
</template>

<script setup lang="ts">
import { ClIcon } from '@/components/cl'
</script>

<style lang="scss" scoped>
.best-answer-badge {
  display: inline-flex;
  align-items: center;
  gap: $sp-3;
  padding: $sp-2 $sp-3;
  background: #fef9c3;
  border: 1rpx solid #facc15;
  border-radius: $radius-lg;
  color: #ca8a04;
  box-shadow: 0 2rpx 6rpx rgba(0, 0, 0, 0.04);
  max-width: 100%;
}

.badge-chip {
  display: inline-flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-1 $sp-3;
  border-radius: 999px;
  background: #fffdf5;
  border: 1rpx solid rgba(#facc15, 0.6);
}

.badge-chip__icon {
  color: #eab308;
}

.badge-chip__label {
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
  color: #ca8a04;
  letter-spacing: 1rpx;
}

.badge-subtitle {
  font-size: $font-size-xs;
  color: #a16207;
}

@include mobile {
  .best-answer-badge {
    padding: $sp-2 $sp-3;
    gap: $sp-2;
  }

  .badge-chip__label {
    font-size: $font-size-xs;
  }
}
</style>
