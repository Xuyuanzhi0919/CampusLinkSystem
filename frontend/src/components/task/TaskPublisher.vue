<template>
  <view class="task-publisher">
    <view class="publisher-header">
      <view class="header-icon">👤</view>
      <text class="header-title">发布者信息</text>
    </view>

    <view class="publisher-content">
      <!-- 发布者基本信息 -->
      <view class="publisher-basic">
        <image
          class="publisher-avatar"
          :src="avatar || '/static/default-avatar.png'"
          mode="aspectFill"
        />
        <view class="publisher-info">
          <view class="name-row">
            <text class="publisher-name">{{ name }}</text>
            <view class="verified-badge" v-if="verified">
              <text class="verified-icon">✓</text>
              <text class="verified-text">已认证</text>
            </view>
          </view>
          <text class="publisher-username">@{{ username || 'user' + publisherId }}</text>
        </view>
      </view>

      <!-- 发布者统计数据 -->
      <view class="publisher-stats">
        <view class="stat-item">
          <text class="stat-value">{{ publishCount }}</text>
          <text class="stat-label">发布任务</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-value">{{ rating }}%</text>
          <text class="stat-label">好评率</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-value">{{ responseTime }}</text>
          <text class="stat-label">响应时长</text>
        </view>
      </view>

      <!-- 联系方式（需权限） -->
      <view class="contact-section" v-if="showContact">
        <view class="contact-item" v-if="phone">
          <view class="contact-label">
            <text class="contact-icon">📱</text>
            <text class="contact-text">手机号码</text>
          </view>
          <view class="contact-actions">
            <text class="contact-value">{{ phone }}</text>
            <view class="contact-btn" @click="handleCopyPhone">
              <text class="btn-icon">📋</text>
            </view>
          </view>
        </view>

        <view class="contact-item" v-if="wechat">
          <view class="contact-label">
            <text class="contact-icon">💬</text>
            <text class="contact-text">微信号</text>
          </view>
          <view class="contact-actions">
            <text class="contact-value">{{ wechat }}</text>
            <view class="contact-btn" @click="handleCopyWechat">
              <text class="btn-icon">📋</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="action-buttons">
        <view class="action-btn primary" @click="handleSendMessage">
          <text class="btn-icon">💬</text>
          <text class="btn-text">发送私信</text>
        </view>
        <view class="action-btn secondary" @click="handleContact">
          <text class="btn-icon">📞</text>
          <text class="btn-text">联系TA</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
interface Props {
  publisherId: number
  name: string
  username?: string
  avatar?: string
  verified?: boolean
  publishCount?: number
  rating?: number
  responseTime?: string
  phone?: string
  wechat?: string
  showContact?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  verified: false,
  publishCount: 0,
  rating: 100,
  responseTime: '<1小时',
  showContact: false
})

const emit = defineEmits<{
  contact: []
  sendMessage: []
  copyPhone: []
  copyWechat: []
}>()

// 复制手机号
const handleCopyPhone = () => {
  if (!props.phone) return

  uni.setClipboardData({
    data: props.phone,
    success: () => {
      uni.showToast({
        title: '手机号已复制',
        icon: 'success'
      })
      emit('copyPhone')
    }
  })
}

// 复制微信号
const handleCopyWechat = () => {
  if (!props.wechat) return

  uni.setClipboardData({
    data: props.wechat,
    success: () => {
      uni.showToast({
        title: '微信号已复制',
        icon: 'success'
      })
      emit('copyWechat')
    }
  })
}

// 发送私信
const handleSendMessage = () => {
  emit('sendMessage')
}

// 联系发布者
const handleContact = () => {
  emit('contact')
}
</script>

<style lang="scss" scoped>
.task-publisher {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(23, 63, 128, 0.06);
  border-left: 8rpx solid #3B82F6;
}

.publisher-header {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.header-icon {
  width: 56rpx;
  height: 56rpx;
  background: #3B82F6;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
}

.header-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2937;
}

.publisher-content {
  padding-left: 72rpx;
}

/* 基本信息 */
.publisher-basic {
  display: flex;
  align-items: center;
  gap: 24rpx;
  margin-bottom: 32rpx;
}

.publisher-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: #F3F4F6;
  border: 4rpx solid #E5E7EB;
}

.publisher-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.publisher-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #1F2937;
}

.verified-badge {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 6rpx 12rpx;
  background: #DBEAFE;
  border-radius: 8rpx;
}

.verified-icon {
  font-size: 20rpx;
  color: #3B82F6;
}

.verified-text {
  font-size: 20rpx;
  color: #3B82F6;
  font-weight: 500;
}

.publisher-username {
  font-size: 24rpx;
  color: #9CA3AF;
}

/* 统计数据 */
.publisher-stats {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 24rpx;
  background: #F9FAFB;
  border-radius: 16rpx;
  margin-bottom: 32rpx;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.stat-value {
  font-size: 36rpx;
  font-weight: 700;
  color: #3B82F6;
}

.stat-label {
  font-size: 24rpx;
  color: #6B7280;
}

.stat-divider {
  width: 2rpx;
  height: 48rpx;
  background: #E5E7EB;
}

/* 联系方式 */
.contact-section {
  padding: 24rpx;
  background: #FFFBEB;
  border-radius: 16rpx;
  border: 2rpx solid #FCD34D;
  margin-bottom: 24rpx;
}

.contact-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 0;

  &:not(:last-child) {
    border-bottom: 2rpx solid #FDE68A;
  }
}

.contact-label {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.contact-icon {
  font-size: 32rpx;
}

.contact-text {
  font-size: 26rpx;
  color: #92400E;
  font-weight: 500;
}

.contact-actions {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.contact-value {
  font-size: 28rpx;
  color: #1F2937;
  font-weight: 600;
  font-family: 'Courier New', monospace;
}

.contact-btn {
  width: 56rpx;
  height: 56rpx;
  background: #FFFFFF;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;

  &:active {
    transform: scale(0.9);
    background: #F3F4F6;
  }
}

.btn-icon {
  font-size: 28rpx;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 16rpx;
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 24rpx;
  border-radius: 16rpx;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;

  &.primary {
    background: #3B82F6;
    color: #FFFFFF;

    &:active {
      background: #2563EB;
      transform: scale(0.98);
    }
  }

  &.secondary {
    background: #F3F4F6;
    color: #4B5563;

    &:active {
      background: #E5E7EB;
      transform: scale(0.98);
    }
  }
}

.btn-text {
  font-size: 28rpx;
}

/* PC端适配 */
@media screen and (min-width: 768px) {
  .task-publisher {
    padding: 48rpx;
  }

  .publisher-content {
    padding-left: 88rpx;
  }

  .action-buttons {
    gap: 24rpx;
  }
}
</style>
