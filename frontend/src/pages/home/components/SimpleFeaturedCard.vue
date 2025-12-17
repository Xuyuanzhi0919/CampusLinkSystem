<template>
  <view class="simple-card" @click="handleClick">
    <!-- 问答类型 -->
    <view v-if="type === 'question'" class="card-content">
      <view class="card-header">
        <text class="card-type-tag">问答</text>
        <view v-if="data.bounty > 0" class="bounty-tag">
          <uni-icons type="wallet-filled" size="12" color="#F97316"></uni-icons>
          <text class="bounty-text">{{ data.bounty }}积分</text>
        </view>
      </view>
      <view class="card-title">{{ data.title }}</view>
      <view class="card-meta">
        <view class="meta-item">
          <uni-icons type="eye" size="14" color="#64748B"></uni-icons>
          <text>{{ data.views || 0 }}</text>
        </view>
        <view class="meta-item">
          <uni-icons type="chat" size="14" color="#64748B"></uni-icons>
          <text>{{ data.answerCount || 0 }}回答</text>
        </view>
        <text v-if="data.askerNickname" class="meta-author">by {{ data.askerNickname }}</text>
      </view>
    </view>

    <!-- 资源类型 -->
    <view v-else-if="type === 'resource'" class="card-content">
      <view class="card-header">
        <text class="card-type-tag resource">资源</text>
        <text class="resource-type">{{ getResourceTypeText(data.resourceType) }}</text>
      </view>
      <view class="card-title">{{ data.title }}</view>
      <view class="card-meta">
        <view class="meta-item">
          <uni-icons type="download" size="14" color="#64748B"></uni-icons>
          <text>{{ data.downloadCount || 0 }}下载</text>
        </view>
        <view class="meta-item">
          <uni-icons type="star" size="14" color="#F59E0B"></uni-icons>
          <text>{{ data.rating || 0 }}</text>
        </view>
        <text v-if="data.uploaderNickname" class="meta-author">by {{ data.uploaderNickname }}</text>
      </view>
    </view>

    <!-- 活动类型 -->
    <view v-else-if="type === 'activity'" class="card-content">
      <view class="card-header">
        <text class="card-type-tag activity">活动</text>
        <view v-if="data.maxParticipants" class="participants">
          <uni-icons type="person" size="12" color="#64748B"></uni-icons>
          <text>{{ data.currentParticipants || 0 }}/{{ data.maxParticipants }}</text>
        </view>
      </view>
      <view class="card-title">{{ data.title }}</view>
      <view class="card-meta">
        <view class="meta-item">
          <uni-icons type="calendar" size="14" color="#64748B"></uni-icons>
          <text>{{ formatDate(data.activityTime) }}</text>
        </view>
        <view class="meta-item">
          <uni-icons type="location" size="14" color="#64748B"></uni-icons>
          <text>{{ data.location || '待定' }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
interface Props {
  type: 'question' | 'resource' | 'activity'
  data: any
}

const props = defineProps<Props>()

const emit = defineEmits<{
  click: [data: any]
}>()

const handleClick = () => {
  emit('click', props.data)
}

const getResourceTypeText = (type: number) => {
  const typeMap: Record<number, string> = {
    1: '课件',
    2: '试题',
    3: '笔记',
    4: '其他'
  }
  return typeMap[type] || '资源'
}

const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}
</script>

<style scoped lang="scss">
.simple-card {
  background: #FFFFFF;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);
  transition: all 0.2s;

  &:active {
    transform: scale(0.98);
    box-shadow: 0 1rpx 4rpx rgba(0, 0, 0, 0.1);
  }
}

.card-content {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-type-tag {
  display: inline-block;
  padding: 4rpx 16rpx;
  font-size: 22rpx;
  font-weight: 500;
  color: #2563EB;
  background: rgba(37, 99, 235, 0.1);
  border-radius: 8rpx;

  &.resource {
    color: #16A34A;
    background: rgba(22, 163, 74, 0.1);
  }

  &.activity {
    color: #F59E0B;
    background: rgba(245, 158, 11, 0.1);
  }
}

.bounty-tag {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: 4rpx 12rpx;
  font-size: 22rpx;
  font-weight: 500;
  color: #F97316;
  background: rgba(249, 115, 22, 0.1);
  border-radius: 8rpx;

  .bounty-text {
    line-height: 1;
  }
}

.resource-type {
  font-size: 24rpx;
  color: #6B7280;
}

.participants {
  display: flex;
  align-items: center;
  gap: 4rpx;
  font-size: 24rpx;
  color: #6B7280;
}

.card-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #0F172A;
  line-height: 1.5;

  /* 最多显示2行 */
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 24rpx;
  font-size: 24rpx;
  color: #94A3B8;

  .meta-item {
    display: flex;
    align-items: center;
    gap: 6rpx;
  }

  .meta-author {
    color: #94A3B8;
  }
}
</style>
