<template>
  <view class="club-list">
    <view v-if="loading && list.length === 0" class="loading-container">
      <view class="loading-skeleton" v-for="i in 5" :key="i">
        <view class="skeleton-avatar"></view>
        <view class="skeleton-content">
          <view class="skeleton-line skeleton-title"></view>
          <view class="skeleton-line skeleton-desc"></view>
        </view>
      </view>
    </view>

    <view v-else-if="list.length > 0" class="club-items">
      <view
        v-for="item in list"
        :key="item.clubId"
        class="club-card"
        @click="handleClubClick(item.clubId)"
      >
        <!-- 社团封面 -->
        <image class="club-cover" :src="item.cover || '/static/images/default-club.png'" mode="aspectFill" />

        <view class="club-info">
          <!-- 社团名称 -->
          <text class="club-name">{{ item.name }}</text>

          <!-- 社团简介 -->
          <text class="club-description">{{ item.description }}</text>

          <!-- 统计信息 -->
          <view class="stats">
            <view class="stat-item">
              <Icon name="users" :size="13" class="stat-icon" />
              <text class="stat-label">{{ item.memberCount || 0 }} 人</text>
            </view>
            <view class="stat-item">
              <Icon name="calendar" :size="13" class="stat-icon" />
              <text class="stat-label">{{ item.activityCount || 0 }} 场活动</text>
            </view>
          </view>

          <!-- 加入/退出按钮 -->
          <view
            class="action-btn"
            :class="{ 'action-btn--joined': item.isJoined, 'action-btn--loading': joiningIds.has(item.clubId) }"
            @click.stop="handleJoinClub(item)"
          >
            <text class="btn-text">{{ joiningIds.has(item.clubId) ? '处理中...' : (item.isJoined ? '已加入' : '加入社团') }}</text>
          </view>
        </view>
      </view>
    </view>

    <view v-else class="empty-container">
      <image class="empty-image" src="/static/images/empty-club.png" mode="aspectFit" />
      <text class="empty-text">暂无社团信息</text>
    </view>

    <view v-if="loading && list.length > 0" class="loading-more">
      <text class="loading-text">加载中...</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useNavigation } from '@/composables/useNavigation'
import Icon from '@/components/icons/index.vue'
import { joinClub, quitClub } from '@/services/club'
import { requireLogin } from '@/utils/auth'

interface Props {
  list: any[]
  loading: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{ (e: 'refresh'): void }>()

const { toClubDetail } = useNavigation()

// 防止重复点击
const joiningIds = ref<Set<number>>(new Set())

const handleClubClick = (clubId: number) => {
  toClubDetail(clubId)
}

const handleJoinClub = async (club: any) => {
  if (!requireLogin('join')) return
  if (joiningIds.value.has(club.clubId)) return

  joiningIds.value.add(club.clubId)
  try {
    if (club.isJoined) {
      await quitClub(club.clubId)
      uni.showToast({ title: '已退出社团', icon: 'success', duration: 1500 })
    } else {
      await joinClub(club.clubId)
      uni.showToast({ title: '加入成功', icon: 'success', duration: 1500 })
    }
    // 通知父组件刷新列表以更新 isJoined 状态
    emit('refresh')
  } catch (error: any) {
    uni.showToast({ title: error?.message || '操作失败，请重试', icon: 'none', duration: 2000 })
  } finally {
    joiningIds.value.delete(club.clubId)
  }
}
</script>

<style scoped lang="scss">
.club-list {
  padding: 12px 16px;
}

/* ========== 骨架屏 ========== */
.loading-skeleton {
  display: flex;
  background: #FFFFFF;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
}

.skeleton-avatar {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  background: linear-gradient(90deg, #F3F4F6 25%, #E5E7EB 50%, #F3F4F6 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  flex-shrink: 0;
  margin-right: 12px;
}

.skeleton-content {
  flex: 1;
}

.skeleton-line {
  background: linear-gradient(90deg, #F3F4F6 25%, #E5E7EB 50%, #F3F4F6 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
  margin-bottom: 12px;
}

.skeleton-title {
  height: 20px;
  width: 60%;
}

.skeleton-desc {
  height: 16px;
  width: 100%;
}

@keyframes shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

/* ========== 社团卡片 ========== */
.club-card {
  display: flex;
  background: #FFFFFF;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;

  &:active {
    transform: scale(0.98);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  }
}

.club-cover {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  flex-shrink: 0;
  margin-right: 12px;
  object-fit: cover;
}

.club-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.club-name {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.club-description {
  font-size: 13px;
  color: #6B7280;
  line-height: 1.5;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.stats {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;

  .icon {
    font-size: 14px;
  }

  .count {
    font-size: 12px;
    color: #9CA3AF;
  }
}

.action-btn {
  align-self: flex-start;
  padding: 6px 16px;
  background: linear-gradient(135deg, #2563EB 0%, #3B82F6 100%);
  border-radius: 6px;
  transition: all 0.25s ease;

  &:active {
    transform: scale(0.95);
  }

  &--joined {
    background: #F3F4F6;
    .btn-text { color: #6B7280; }
  }

  &--loading {
    opacity: 0.7;
    pointer-events: none;
  }

  .btn-text {
    font-size: 13px;
    font-weight: 600;
    color: #FFFFFF;
  }
}

.stat-icon {
  color: #9CA3AF;
  flex-shrink: 0;
}

.stat-label {
  font-size: 12px;
  color: #9CA3AF;
}

/* ========== 空状态 ========== */
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.empty-image {
  width: 200px;
  height: 200px;
  margin-bottom: 16px;
  opacity: 0.6;
}

.empty-text {
  font-size: 14px;
  color: #9CA3AF;
}

/* ========== 加载更多 ========== */
.loading-more {
  padding: 20px;
  text-align: center;
}

.loading-text {
  font-size: 13px;
  color: #9CA3AF;
}
</style>
