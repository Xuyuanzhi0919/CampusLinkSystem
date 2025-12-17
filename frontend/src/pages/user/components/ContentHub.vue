<template>
  <!-- ========== 内容中心(Content Hub) ========== -->
  <view class="content-hub">
    <!-- 标题区域 -->
    <view class="hub-header">
      <text class="hub-title">我的动态</text>
    </view>

    <!-- Tab切换栏 -->
    <view class="tab-bar">
      <view
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-item"
        :class="{ active: currentTab === tab.key }"
        @click="handleTabChange(tab.key)"
      >
        <text class="tab-label">{{ tab.label }}</text>
        <view v-if="currentTab === tab.key" class="tab-indicator"></view>
      </view>
    </view>

    <!-- 内容区域 -->
    <view class="tab-content">
      <!-- 我的资源 -->
      <view v-if="currentTab === 'resources'" class="content-list">
        <view v-if="resourceList.length === 0" class="empty-state">
          <Icon name="file-text" :size="64" :stroke-width="1.5" />
          <text class="empty-text">还没有上传资源</text>
          <text class="empty-hint">分享你的学习资料,帮助更多同学</text>
        </view>
        <view v-else class="list-items">
          <view
            v-for="item in resourceList"
            :key="item.id"
            class="list-item"
            @click="handleItemClick('resource', item.id)"
          >
            <view class="item-header">
              <text class="item-title">{{ item.title }}</text>
              <text class="item-category">{{ item.category }}</text>
            </view>
            <view class="item-meta">
              <text class="meta-item">
                <Icon name="download" :size="14" />
                {{ item.downloads }}
              </text>
              <text class="meta-item">
                <Icon name="eye" :size="14" />
                {{ item.views }}
              </text>
              <text class="meta-time">{{ item.createdAt }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 我的回答 -->
      <view v-if="currentTab === 'answers'" class="content-list">
        <view v-if="answerList.length === 0" class="empty-state">
          <Icon name="message-circle" :size="64" :stroke-width="1.5" />
          <text class="empty-text">还没有回答问题</text>
          <text class="empty-hint">帮助他人解决疑惑,获得积分奖励</text>
        </view>
        <view v-else class="list-items">
          <view
            v-for="item in answerList"
            :key="item.id"
            class="list-item"
            @click="handleItemClick('answer', item.id)"
          >
            <view class="item-header">
              <text class="item-title">{{ item.questionTitle }}</text>
              <text v-if="item.adopted" class="adopted-badge">已采纳</text>
            </view>
            <text class="answer-preview">{{ item.content }}</text>
            <view class="item-meta">
              <text class="meta-item">
                <Icon name="thumbs-up" :size="14" />
                {{ item.likes }}
              </text>
              <text class="meta-time">{{ item.createdAt }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 我的互动 -->
      <view v-if="currentTab === 'interactions'" class="content-list">
        <view v-if="interactionList.length === 0" class="empty-state">
          <Icon name="heart" :size="64" :stroke-width="1.5" />
          <text class="empty-text">还没有互动记录</text>
          <text class="empty-hint">点赞、收藏、评论,与他人交流</text>
        </view>
        <view v-else class="list-items">
          <view
            v-for="item in interactionList"
            :key="item.id"
            class="list-item interaction-item"
            @click="handleItemClick(item.type, item.targetId)"
          >
            <view class="interaction-header">
              <Icon :name="getInteractionIcon(item.action)" :size="20" />
              <text class="interaction-action">{{ getInteractionText(item.action) }}</text>
            </view>
            <text class="item-title">{{ item.title }}</text>
            <text class="meta-time">{{ item.createdAt }}</text>
          </view>
        </view>
      </view>

      <!-- 我的成长 -->
      <view v-if="currentTab === 'growth'" class="content-list">
        <view class="growth-timeline">
          <view
            v-for="event in growthEvents"
            :key="event.id"
            class="timeline-item"
          >
            <view class="timeline-dot" :class="event.type"></view>
            <view class="timeline-content">
              <view class="event-header">
                <text class="event-title">{{ event.title }}</text>
                <text class="event-points" v-if="event.points">
                  +{{ event.points }}
                </text>
              </view>
              <text class="event-desc">{{ event.description }}</text>
              <text class="event-time">{{ event.createdAt }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import Icon from '@/components/icons/index.vue'

interface Tab {
  key: string
  label: string
}

interface ResourceItem {
  id: number
  title: string
  category: string
  downloads: number
  views: number
  createdAt: string
}

interface AnswerItem {
  id: number
  questionTitle: string
  content: string
  likes: number
  adopted: boolean
  createdAt: string
}

interface InteractionItem {
  id: number
  action: 'like' | 'collect' | 'comment'
  type: string
  targetId: number
  title: string
  createdAt: string
}

interface GrowthEvent {
  id: number
  type: 'level' | 'badge' | 'achievement'
  title: string
  description: string
  points?: number
  createdAt: string
}

interface Props {
  resourceList?: ResourceItem[]
  answerList?: AnswerItem[]
  interactionList?: InteractionItem[]
  growthEvents?: GrowthEvent[]
}

const props = withDefaults(defineProps<Props>(), {
  resourceList: () => [],
  answerList: () => [],
  interactionList: () => [],
  growthEvents: () => []
})

const emit = defineEmits<{
  itemClick: [type: string, id: number]
}>()

const tabs: Tab[] = [
  { key: 'resources', label: '我的资源' },
  { key: 'answers', label: '我的回答' },
  { key: 'interactions', label: '我的互动' },
  { key: 'growth', label: '我的成长' }
]

const currentTab = ref<string>('resources')

const handleTabChange = (key: string) => {
  currentTab.value = key
}

const handleItemClick = (type: string, id: number) => {
  emit('itemClick', type, id)
}

const getInteractionIcon = (action: string) => {
  const iconMap: Record<string, string> = {
    like: 'thumbs-up',
    collect: 'bookmark',
    comment: 'message-circle'
  }
  return iconMap[action] || 'heart'
}

const getInteractionText = (action: string) => {
  const textMap: Record<string, string> = {
    like: '赞了',
    collect: '收藏了',
    comment: '评论了'
  }
  return textMap[action] || '互动了'
}
</script>

<style lang="scss" scoped>
// 变量已通过 uni.scss 全局注入

/* ========== Content Hub ========== */
.content-hub {
  padding: 0 32rpx;
  margin-bottom: 32rpx;
}

/* 标题区域 */
.hub-header {
  margin-bottom: 24rpx;

  .hub-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #0F172A; // text-primary
  }
}

/* 🎯 Tab切换栏 */
.tab-bar {
  display: flex;
  align-items: center;
  gap: 32rpx;
  margin-bottom: 32rpx;
  border-bottom: 2rpx solid #E5E7EB;
  padding-bottom: 0;
}

.tab-item {
  position: relative;
  padding: 16rpx 0;
  cursor: pointer;
  transition: all 0.2s ease;

  .tab-label {
    font-size: 28rpx;
    font-weight: 500;
    color: #6B7280; // text-secondary
    transition: color 0.2s ease;
  }

  &.active {
    .tab-label {
      color: #2563EB; // primary
      font-weight: 600;
    }
  }

  .tab-indicator {
    position: absolute;
    bottom: -2rpx;
    left: 0;
    right: 0;
    height: 4rpx;
    background: #2563EB;
    border-radius: 4rpx 4rpx 0 0;
  }
}

/* 🎯 内容区域 */
.tab-content {
  min-height: 400rpx;
}

.content-list {
  width: 100%;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 32rpx;
  color: #9CA3AF;

  .empty-text {
    font-size: 28rpx;
    font-weight: 500;
    color: #6B7280;
    margin-top: 24rpx;
  }

  .empty-hint {
    font-size: 24rpx;
    color: #9CA3AF;
    margin-top: 12rpx;
  }
}

/* 🎯 列表项 */
.list-items {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.list-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
  cursor: pointer;

  &:active {
    transform: scale(0.98);
    box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
  }

  .item-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12rpx;
  }

  .item-title {
    flex: 1;
    font-size: 28rpx;
    font-weight: 600;
    color: #0F172A;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .item-category {
    flex-shrink: 0;
    font-size: 22rpx;
    color: #2563EB;
    background: #EFF6FF;
    padding: 4rpx 12rpx;
    border-radius: 8rpx;
    margin-left: 12rpx;
  }

  .adopted-badge {
    flex-shrink: 0;
    font-size: 22rpx;
    color: #16A34A; // success
    background: #F0FDF4;
    padding: 4rpx 12rpx;
    border-radius: 8rpx;
    margin-left: 12rpx;
  }

  .answer-preview {
    font-size: 26rpx;
    color: #6B7280;
    line-height: 1.6;
    margin-bottom: 12rpx;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .item-meta {
    display: flex;
    align-items: center;
    gap: 24rpx;
    font-size: 22rpx;
    color: #9CA3AF;

    .meta-item {
      display: flex;
      align-items: center;
      gap: 6rpx;
    }

    .meta-time {
      margin-left: auto;
    }
  }
}

/* 🎯 互动项特殊样式 */
.interaction-item {
  .interaction-header {
    display: flex;
    align-items: center;
    gap: 12rpx;
    margin-bottom: 12rpx;

    .interaction-action {
      font-size: 24rpx;
      color: #6B7280;
    }
  }
}

/* 🎯 成长时间轴 */
.growth-timeline {
  position: relative;
  padding-left: 40rpx;

  &::before {
    content: '';
    position: absolute;
    left: 16rpx;
    top: 20rpx;
    bottom: 20rpx;
    width: 2rpx;
    background: #E5E7EB;
  }
}

.timeline-item {
  position: relative;
  margin-bottom: 40rpx;

  &:last-child {
    margin-bottom: 0;
  }

  .timeline-dot {
    position: absolute;
    left: -28rpx;
    top: 4rpx;
    width: 24rpx;
    height: 24rpx;
    border-radius: 50%;
    border: 4rpx solid #fff;
    background: #2563EB;
    box-shadow: 0 0 0 2rpx #E5E7EB;

    &.level {
      background: #F97316; // accent
    }

    &.badge {
      background: #16A34A; // success
    }

    &.achievement {
      background: #8B5CF6; // purple
    }
  }

  .timeline-content {
    background: #fff;
    border-radius: 12rpx;
    padding: 20rpx;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);

    .event-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 8rpx;

      .event-title {
        font-size: 26rpx;
        font-weight: 600;
        color: #0F172A;
      }

      .event-points {
        font-size: 24rpx;
        font-weight: 600;
        color: #F97316; // accent
      }
    }

    .event-desc {
      font-size: 24rpx;
      color: #6B7280;
      line-height: 1.5;
      display: block;
      margin-bottom: 8rpx;
    }

    .event-time {
      font-size: 22rpx;
      color: #9CA3AF;
    }
  }
}

// 🎯 响应式适配
@media (min-width: 768px) {
  .content-hub {
    max-width: 1200rpx;
    margin: 0 auto 48rpx;
  }

  .tab-bar {
    gap: 48rpx;
  }

  .list-item {
    padding: 32rpx;
  }
}
</style>
