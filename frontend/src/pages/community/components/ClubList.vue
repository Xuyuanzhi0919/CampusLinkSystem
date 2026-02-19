<template>
  <view class="club-list">

    <!-- 骨架屏 -->
    <view v-if="loading && list.length === 0" class="skeleton-container">
      <view class="club-skeleton" v-for="i in 4" :key="i">
        <view class="sk-icon"></view>
        <view class="sk-body">
          <view class="sk-line sk-title"></view>
          <view class="sk-line sk-desc"></view>
          <view class="sk-line sk-meta"></view>
        </view>
      </view>
    </view>

    <!-- 社团列表 -->
    <view v-else-if="list.length > 0" class="club-items">
      <view
        v-for="item in list"
        :key="item.clubId"
        class="club-card"
        @click="handleClubClick(item.clubId)"
      >
        <!-- 顶部品牌色条 -->
        <view class="club-card__type-bar"></view>

        <!-- Header: 社团图标 + 分类标签 -->
        <view class="club-card__header">
          <view class="club-card__icon">
            <image
              v-if="item.cover"
              class="club-cover-img"
              :src="item.cover"
              mode="aspectFill"
            />
            <view v-else class="club-icon-placeholder">
              <Icon name="users" :size="22" color="#377DFF" />
            </view>
          </view>
          <view class="club-card__capsule">
            <text>{{ item.category || '综合' }}</text>
          </view>
        </view>

        <!-- Body: 名称 + 简介 -->
        <view class="club-card__body">
          <text class="club-card__title">{{ item.name }}</text>
          <text v-if="item.description" class="club-card__desc">{{ item.description }}</text>
        </view>

        <!-- Meta: 成员数 + 活动数 -->
        <view class="club-card__meta">
          <view class="club-card__meta-item">
            <Icon name="users" :size="14" class="meta-icon" />
            <text>{{ item.memberCount || 0 }} 人</text>
          </view>
          <view class="club-card__meta-item">
            <Icon name="calendar" :size="14" class="meta-icon" />
            <text>{{ item.activityCount || 0 }} 场活动</text>
          </view>
        </view>

        <!-- Actions: 加入/退出按钮 -->
        <view class="club-card__actions">
          <view
            class="club-card__btn"
            :class="{
              'club-card__btn--joined': item.isJoined,
              'club-card__btn--loading': joiningIds.has(item.clubId)
            }"
            @click.stop="handleJoinClub(item)"
          >
            <Icon
              :name="joiningIds.has(item.clubId) ? 'loader' : (item.isJoined ? 'check' : 'plus')"
              :size="13"
              class="btn-icon"
            />
            <text class="btn-text">
              {{ joiningIds.has(item.clubId) ? '处理中' : (item.isJoined ? '已加入' : '加入社团') }}
            </text>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else class="empty-container">
      <view class="empty-icon-wrap">
        <Icon name="users" :size="40" color="#D1D5DB" />
      </view>
      <text class="empty-title">暂无社团</text>
      <text class="empty-hint">快来创建第一个社团吧</text>
    </view>

    <!-- 加载更多 -->
    <view v-if="loading && list.length > 0" class="loading-more">
      <view class="loading-dots">
        <view class="dot"></view>
        <view class="dot"></view>
        <view class="dot"></view>
      </view>
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
    emit('refresh')
  } catch (error: any) {
    uni.showToast({ title: error?.message || '操作失败，请重试', icon: 'none', duration: 2000 })
  } finally {
    joiningIds.value.delete(club.clubId)
  }
}
</script>

<style scoped lang="scss">
@import '@/styles/design-tokens.scss';

.club-list {
  padding: 12px 16px 80px;
}

/* ========== 骨架屏 ========== */
.club-skeleton {
  display: flex;
  gap: 12px;
  background: $color-bg-card;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 12px;
  box-shadow: $shadow-card;
}

.sk-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  flex-shrink: 0;
  background: linear-gradient(90deg, #F3F4F6 25%, #E5E7EB 50%, #F3F4F6 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.sk-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.sk-line {
  border-radius: 4px;
  background: linear-gradient(90deg, #F3F4F6 25%, #E5E7EB 50%, #F3F4F6 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}
.sk-title { height: 18px; width: 55%; }
.sk-desc  { height: 14px; width: 90%; }
.sk-meta  { height: 14px; width: 40%; }

@keyframes shimmer {
  0%   { background-position: -200% 0; }
  100% { background-position:  200% 0; }
}

/* ========== 社团卡片 ========== */
.club-card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: $card-bg;
  backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid $card-border;
  border-radius: $card-radius;
  box-shadow: $card-shadow;
  padding: 20px;
  margin-bottom: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: $transition-all;

  &:hover {
    box-shadow: $card-shadow-hover;
    transform: translateY(-2px);
  }

  &:active {
    transform: scale(0.98);
  }
}

/* 顶部品牌色条 - 蓝色（社团） */
.club-card__type-bar {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, $campus-blue 0%, $campus-blue-light 100%);
  border-radius: $card-radius $card-radius 0 0;
}

/* Header */
.club-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 4px;
}

.club-card__icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
}

.club-cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.club-icon-placeholder {
  width: 100%;
  height: 100%;
  background: $campus-blue-lighter;
  display: flex;
  align-items: center;
  justify-content: center;
}

.club-card__capsule {
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  color: $campus-blue;
  background: rgba($campus-blue, 0.1);
}

/* Body */
.club-card__body {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.club-card__title {
  font-size: 16px;
  font-weight: 700;
  color: $color-text-primary;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.club-card__desc {
  font-size: 13px;
  color: $color-text-tertiary;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Meta */
.club-card__meta {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-top: 8px;
  border-top: 1px solid $color-divider;
}

.club-card__meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: $color-text-tertiary;

  .meta-icon {
    color: $color-text-quaternary;
    flex-shrink: 0;
  }
}

/* Actions */
.club-card__actions {
  display: flex;
  justify-content: flex-end;
}

.club-card__btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 7px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  transition: $transition-all;
  background: $campus-blue;
  color: #FFFFFF;
  border: 1.5px solid $campus-blue;

  .btn-icon { color: #FFFFFF; }

  &:active { transform: scale(0.95); }

  &--joined {
    background: transparent;
    color: $color-text-tertiary;
    border-color: $color-border;
    .btn-icon { color: $color-text-tertiary; }
  }

  &--loading {
    opacity: 0.65;
    pointer-events: none;
  }
}

/* ========== 空状态 ========== */
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 72px 20px;
  gap: 8px;
}

.empty-icon-wrap {
  width: 80px;
  height: 80px;
  background: $color-bg-hover;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
}

.empty-title {
  font-size: 15px;
  font-weight: 600;
  color: $color-text-secondary;
}

.empty-hint {
  font-size: 13px;
  color: $color-text-quaternary;
}

/* ========== 加载更多 ========== */
.loading-more {
  padding: 20px;
  display: flex;
  justify-content: center;
}

.loading-dots {
  display: flex;
  gap: 6px;
  align-items: center;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: $campus-blue;
  opacity: 0.4;
  animation: dotPulse 1.2s ease-in-out infinite;

  &:nth-child(2) { animation-delay: 0.2s; }
  &:nth-child(3) { animation-delay: 0.4s; }
}

@keyframes dotPulse {
  0%, 80%, 100% { transform: scale(0.8); opacity: 0.4; }
  40% { transform: scale(1.1); opacity: 1; }
}

/* ========== PC 端双列布局 ========== */
@media (min-width: 1024px) {
  .club-list {
    padding: 20px 80px 80px;
  }

  .club-items {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  .club-card {
    margin-bottom: 0;
  }
}
</style>
