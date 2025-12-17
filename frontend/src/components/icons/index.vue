<template>
  <svg
    :width="size"
    :height="size"
    :viewBox="viewBox"
    :fill="fill"
    :stroke="stroke"
    :stroke-width="strokeWidth"
    :class="['icon', `icon-${name}`]"
  >
    <!-- User 图标 -->
    <template v-if="name === 'user'">
      <circle cx="12" cy="8" r="4" stroke="currentColor" fill="none"/>
      <path d="M6 21V19C6 16.7909 7.79086 15 10 15H14C16.2091 15 18 16.7909 18 19V21" stroke="currentColor" fill="none"/>
    </template>

    <!-- Heart 图标 -->
    <template v-else-if="name === 'heart'">
      <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" stroke="currentColor" fill="none"/>
    </template>

    <!-- Star 图标 -->
    <template v-else-if="name === 'star'">
      <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" stroke="currentColor" fill="none"/>
    </template>

    <!-- Eye 图标 -->
    <template v-else-if="name === 'eye'">
      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" stroke="currentColor" fill="none"/>
      <circle cx="12" cy="12" r="3" stroke="currentColor" fill="none"/>
    </template>

    <!-- Message Circle 图标 -->
    <template v-else-if="name === 'message-circle'">
      <path d="M21 11.5C21 16.75 16.97 21 12 21C10.39 21 8.87 20.59 7.54 19.88L3 21L4.12 16.46C3.41 15.13 3 13.61 3 12C3 6.75 7.03 2.5 12 2.5C16.97 2.5 21 6.75 21 11.5Z" stroke="currentColor" fill="none"/>
    </template>

    <!-- Bookmark 图标 -->
    <template v-else-if="name === 'bookmark'">
      <path d="M19 21L12 16L5 21V5C5 3.89543 5.89543 3 7 3H17C18.1046 3 19 3.89543 19 5V21Z" stroke="currentColor" fill="none"/>
    </template>

    <!-- Share 图标 -->
    <template v-else-if="name === 'share'">
      <circle cx="18" cy="5" r="3" stroke="currentColor" fill="none"/>
      <circle cx="6" cy="12" r="3" stroke="currentColor" fill="none"/>
      <circle cx="18" cy="19" r="3" stroke="currentColor" fill="none"/>
      <line x1="8.59" y1="13.51" x2="15.42" y2="17.49" stroke="currentColor"/>
      <line x1="15.41" y1="6.51" x2="8.59" y2="10.49" stroke="currentColor"/>
    </template>

    <!-- Flag 图标（举报） -->
    <template v-else-if="name === 'flag'">
      <path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z" stroke="currentColor" fill="none"/>
      <line x1="4" y1="22" x2="4" y2="15" stroke="currentColor"/>
    </template>

    <!-- Trash 图标 -->
    <template v-else-if="name === 'trash'">
      <polyline points="3 6 5 6 21 6" stroke="currentColor" fill="none"/>
      <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" stroke="currentColor" fill="none"/>
    </template>

    <!-- Thumbs Up 图标 -->
    <template v-else-if="name === 'thumbs-up'">
      <path d="M7 22V11M2 13V20C2 21.1046 2.89543 22 4 22H17.4C18.3604 22 19.2127 21.4447 19.5528 20.5528L21.5528 15.5528C21.8343 14.8343 21.8343 14.0351 21.5528 13.3166L19.5528 8.31662C19.2127 7.42473 18.3604 6.86957 17.4 6.86957H14C13.4477 6.86957 13 6.42185 13 5.86957V4C13 2.89543 12.1046 2 11 2C10.4477 2 10 2.44772 10 3L9 11H4C2.89543 11 2 11.8954 2 13Z" stroke="currentColor" fill="none"/>
    </template>

    <!-- Gift 图标（积分） -->
    <template v-else-if="name === 'gift'">
      <polyline points="20 12 20 22 4 22 4 12" stroke="currentColor" fill="none"/>
      <rect x="2" y="7" width="20" height="5" stroke="currentColor" fill="none"/>
      <line x1="12" y1="22" x2="12" y2="7" stroke="currentColor"/>
      <path d="M12 7H7.5a2.5 2.5 0 0 1 0-5C11 2 12 7 12 7z" stroke="currentColor" fill="none"/>
      <path d="M12 7h4.5a2.5 2.5 0 0 0 0-5C13 2 12 7 12 7z" stroke="currentColor" fill="none"/>
    </template>

    <!-- Check Circle 图标（已解决） -->
    <template v-else-if="name === 'check-circle'">
      <circle cx="12" cy="12" r="10" stroke="currentColor" fill="none"/>
      <polyline points="9 12 11 14 15 10" stroke="currentColor" fill="none"/>
    </template>

    <!-- Check 图标（勾选） -->
    <template v-else-if="name === 'check'">
      <polyline points="20 6 9 17 4 12" stroke="currentColor" fill="none"/>
    </template>

    <!-- Bar Chart 图标（统计） -->
    <template v-else-if="name === 'bar-chart'">
      <line x1="12" y1="20" x2="12" y2="10" stroke="currentColor"/>
      <line x1="18" y1="20" x2="18" y2="4" stroke="currentColor"/>
      <line x1="6" y1="20" x2="6" y2="16" stroke="currentColor"/>
    </template>

    <!-- Link 图标（相关推荐） -->
    <template v-else-if="name === 'link'">
      <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71" stroke="currentColor" fill="none"/>
      <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71" stroke="currentColor" fill="none"/>
    </template>

    <!-- File Text 图标（文档/资源） -->
    <template v-else-if="name === 'file-text'">
      <path d="M14 2H6C4.89543 2 4 2.89543 4 4V20C4 21.1046 4.89543 22 6 22H18C19.1046 22 20 21.1046 20 20V8L14 2Z" stroke="currentColor" fill="none"/>
      <polyline points="14 2 14 8 20 8" stroke="currentColor" fill="none"/>
      <line x1="16" y1="13" x2="8" y2="13" stroke="currentColor"/>
      <line x1="16" y1="17" x2="8" y2="17" stroke="currentColor"/>
      <line x1="10" y1="9" x2="8" y2="9" stroke="currentColor"/>
    </template>

    <!-- Lightning 图标（快捷操作） -->
    <template v-else-if="name === 'lightning'">
      <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" stroke="currentColor" fill="none"/>
    </template>

    <!-- Clock 图标 -->
    <template v-else-if="name === 'clock'">
      <circle cx="12" cy="12" r="10" stroke="currentColor" fill="none"/>
      <polyline points="12 6 12 12 16 14" stroke="currentColor" fill="none"/>
    </template>

    <!-- Calendar 图标（日历/活动） -->
    <template v-else-if="name === 'calendar'">
      <rect x="3" y="4" width="18" height="18" rx="2" ry="2" stroke="currentColor" fill="none"/>
      <line x1="16" y1="2" x2="16" y2="6" stroke="currentColor"/>
      <line x1="8" y1="2" x2="8" y2="6" stroke="currentColor"/>
      <line x1="3" y1="10" x2="21" y2="10" stroke="currentColor"/>
    </template>

    <!-- X 图标（关闭/清除） -->
    <template v-else-if="name === 'x'">
      <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" stroke-linecap="round"/>
      <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" stroke-linecap="round"/>
    </template>

    <!-- Tag 图标 -->
    <template v-else-if="name === 'tag'">
      <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z" stroke="currentColor" fill="none"/>
      <line x1="7" y1="7" x2="7.01" y2="7" stroke="currentColor"/>
    </template>

    <!-- Map Pin 图标（地点/位置） -->
    <template v-else-if="name === 'map-pin'">
      <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" stroke="currentColor" fill="none"/>
      <circle cx="12" cy="10" r="3" stroke="currentColor" fill="none"/>
    </template>

    <!-- Ticket 图标（门票/名额） -->
    <template v-else-if="name === 'ticket'">
      <path d="M2 9a3 3 0 0 1 0 6v2a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-2a3 3 0 0 1 0-6V7a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2v2z" stroke="currentColor" fill="none"/>
      <line x1="13" y1="5" x2="13" y2="19" stroke="currentColor" stroke-dasharray="2 2"/>
    </template>

    <!-- Award 图标（奖励/悬赏） -->
    <template v-else-if="name === 'award'">
      <circle cx="12" cy="8" r="7" stroke="currentColor" fill="none"/>
      <polyline points="8.21 13.89 7 23 12 20 17 23 15.79 13.88" stroke="currentColor" fill="none"/>
    </template>

    <!-- Help Circle 图标（帮助/问题） -->
    <template v-else-if="name === 'help-circle'">
      <circle cx="12" cy="12" r="10" stroke="currentColor" fill="none"/>
      <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3" stroke="currentColor" fill="none"/>
      <line x1="12" y1="17" x2="12.01" y2="17" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
    </template>

    <!-- Image 图标 -->
    <template v-else-if="name === 'image'">
      <rect x="3" y="3" width="18" height="18" rx="2" ry="2" stroke="currentColor" fill="none"/>
      <circle cx="8.5" cy="8.5" r="1.5" stroke="currentColor" fill="none"/>
      <polyline points="21 15 16 10 5 21" stroke="currentColor" fill="none"/>
    </template>

    <!-- Send 图标（发送） -->
    <template v-else-if="name === 'send'">
      <line x1="22" y1="2" x2="11" y2="13" stroke="currentColor" fill="none"/>
      <polygon points="22 2 15 22 11 13 2 9 22 2" stroke="currentColor" fill="none"/>
    </template>

    <!-- More Vertical 图标 -->
    <template v-else-if="name === 'more-vertical'">
      <circle cx="12" cy="5" r="1" fill="currentColor"/>
      <circle cx="12" cy="12" r="1" fill="currentColor"/>
      <circle cx="12" cy="19" r="1" fill="currentColor"/>
    </template>

    <!-- Edit 图标（编辑） -->
    <template v-else-if="name === 'edit'">
      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" stroke="currentColor" fill="none"/>
      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" fill="none"/>
    </template>

    <!-- Edit 3 图标（编辑笔） -->
    <template v-else-if="name === 'edit-3'">
      <path d="M12 20h9" stroke="currentColor" fill="none"/>
      <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" stroke="currentColor" fill="none"/>
    </template>

    <!-- Home 图标 -->
    <template v-else-if="name === 'home'">
      <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" stroke="currentColor" fill="none"/>
      <polyline points="9 22 9 12 15 12 15 22" stroke="currentColor" fill="none"/>
    </template>

    <!-- Book 图标 -->
    <template v-else-if="name === 'book'">
      <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" stroke="currentColor" fill="none"/>
      <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" stroke="currentColor" fill="none"/>
    </template>

    <!-- Book Open 图标（学习） -->
    <template v-else-if="name === 'book-open'">
      <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z" stroke="currentColor" fill="none"/>
      <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z" stroke="currentColor" fill="none"/>
    </template>

    <!-- Life Buoy 图标（生活） -->
    <template v-else-if="name === 'life-buoy'">
      <circle cx="12" cy="12" r="10" stroke="currentColor" fill="none"/>
      <circle cx="12" cy="12" r="4" stroke="currentColor" fill="none"/>
      <line x1="4.93" y1="4.93" x2="9.17" y2="9.17" stroke="currentColor"/>
      <line x1="14.83" y1="14.83" x2="19.07" y2="19.07" stroke="currentColor"/>
      <line x1="14.83" y1="9.17" x2="19.07" y2="4.93" stroke="currentColor"/>
      <line x1="4.93" y1="19.07" x2="9.17" y2="14.83" stroke="currentColor"/>
    </template>

    <!-- Code 图标（技术） -->
    <template v-else-if="name === 'code'">
      <polyline points="16 18 22 12 16 6" stroke="currentColor" fill="none"/>
      <polyline points="8 6 2 12 8 18" stroke="currentColor" fill="none"/>
    </template>

    <!-- Grid 图标（全部） -->
    <template v-else-if="name === 'grid'">
      <rect x="3" y="3" width="7" height="7" stroke="currentColor" fill="none"/>
      <rect x="14" y="3" width="7" height="7" stroke="currentColor" fill="none"/>
      <rect x="14" y="14" width="7" height="7" stroke="currentColor" fill="none"/>
      <rect x="3" y="14" width="7" height="7" stroke="currentColor" fill="none"/>
    </template>

    <!-- More Horizontal 图标（其他） -->
    <template v-else-if="name === 'more-horizontal'">
      <circle cx="12" cy="12" r="1" stroke="currentColor" fill="currentColor"/>
      <circle cx="19" cy="12" r="1" stroke="currentColor" fill="currentColor"/>
      <circle cx="5" cy="12" r="1" stroke="currentColor" fill="currentColor"/>
    </template>

    <!-- Search 图标（搜索） -->
    <template v-else-if="name === 'search'">
      <circle cx="11" cy="11" r="8" stroke="currentColor" fill="none"/>
      <line x1="21" y1="21" x2="16.65" y2="16.65" stroke="currentColor"/>
    </template>

    <!-- WiFi Off 图标（网络断开） -->
    <template v-else-if="name === 'wifi-off'">
      <line x1="1" y1="1" x2="23" y2="23" stroke="currentColor"/>
      <path d="M16.72 11.06A10.94 10.94 0 0 1 19 12.55" stroke="currentColor" fill="none"/>
      <path d="M5 12.55a10.94 10.94 0 0 1 5.17-2.39" stroke="currentColor" fill="none"/>
      <path d="M10.71 5.05A16 16 0 0 1 22.58 9" stroke="currentColor" fill="none"/>
      <path d="M1.42 9a15.91 15.91 0 0 1 4.7-2.88" stroke="currentColor" fill="none"/>
      <path d="M8.53 16.11a6 6 0 0 1 6.95 0" stroke="currentColor" fill="none"/>
      <line x1="12" y1="20" x2="12.01" y2="20" stroke="currentColor"/>
    </template>

    <!-- Alert Triangle 图标（警告） -->
    <template v-else-if="name === 'alert-triangle'">
      <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" stroke="currentColor" fill="none"/>
      <line x1="12" y1="9" x2="12" y2="13" stroke="currentColor"/>
      <line x1="12" y1="17" x2="12.01" y2="17" stroke="currentColor"/>
    </template>

    <!-- Arrow Left 图标（返回） -->
    <template v-else-if="name === 'arrow-left'">
      <line x1="19" y1="12" x2="5" y2="12" stroke="currentColor"/>
      <polyline points="12 19 5 12 12 5" stroke="currentColor" fill="none"/>
    </template>

    <!-- Chevron Down 图标（下拉） -->
    <template v-else-if="name === 'chevron-down'">
      <polyline points="6 9 12 15 18 9" stroke="currentColor" fill="none"/>
    </template>

    <!-- Chevron Up 图标（上滑/返回顶部） -->
    <template v-else-if="name === 'chevron-up'">
      <polyline points="18 15 12 9 6 15" stroke="currentColor" fill="none"/>
    </template>

    <!-- Arrow Down Up 图标（排序） -->
    <template v-else-if="name === 'arrow-down-up'">
      <path d="M11 5v14M18 13l-7 7-7-7" stroke="currentColor" fill="none"/>
      <path d="M7 11l7-7 7 7" stroke="currentColor" fill="none"/>
    </template>

    <!-- Sliders 图标（筛选/设置） -->
    <template v-else-if="name === 'sliders'">
      <line x1="4" y1="21" x2="4" y2="14" stroke="currentColor"/>
      <line x1="4" y1="10" x2="4" y2="3" stroke="currentColor"/>
      <line x1="12" y1="21" x2="12" y2="12" stroke="currentColor"/>
      <line x1="12" y1="8" x2="12" y2="3" stroke="currentColor"/>
      <line x1="20" y1="21" x2="20" y2="16" stroke="currentColor"/>
      <line x1="20" y1="12" x2="20" y2="3" stroke="currentColor"/>
      <line x1="1" y1="14" x2="7" y2="14" stroke="currentColor"/>
      <line x1="9" y1="8" x2="15" y2="8" stroke="currentColor"/>
      <line x1="17" y1="16" x2="23" y2="16" stroke="currentColor"/>
    </template>

    <!-- Image Plus 图标（上传图片） -->
    <template v-else-if="name === 'image-plus'">
      <rect x="3" y="3" width="18" height="18" rx="2" ry="2" stroke="currentColor" fill="none"/>
      <circle cx="8.5" cy="8.5" r="1.5" fill="currentColor"/>
      <polyline points="21 15 16 10 5 21" stroke="currentColor" fill="none"/>
      <line x1="12" y1="8" x2="12" y2="14" stroke="currentColor"/>
      <line x1="9" y1="11" x2="15" y2="11" stroke="currentColor"/>
    </template>

    <!-- Info 图标（信息提示） -->
    <template v-else-if="name === 'info'">
      <circle cx="12" cy="12" r="10" stroke="currentColor" fill="none"/>
      <line x1="12" y1="16" x2="12" y2="12" stroke="currentColor"/>
      <line x1="12" y1="8" x2="12.01" y2="8" stroke="currentColor"/>
    </template>

    <!-- Lightbulb 图标（提示/建议） -->
    <template v-else-if="name === 'lightbulb'">
      <path d="M9 18h6" stroke="currentColor" fill="none"/>
      <path d="M10 22h4" stroke="currentColor" fill="none"/>
      <path d="M15.09 14c.18-.98.65-1.74 1.41-2.5A4.97 4.97 0 0 0 18 8a6 6 0 1 0-12 0c0 1.5.7 2.73 1.5 3.5.76.76 1.23 1.52 1.41 2.5" stroke="currentColor" fill="none"/>
    </template>

    <!-- Trending Up 图标（热门/趋势） -->
    <template v-else-if="name === 'trending-up'">
      <polyline points="23 6 13.5 15.5 8.5 10.5 1 18" stroke="currentColor" fill="none"/>
      <polyline points="17 6 23 6 23 12" stroke="currentColor" fill="none"/>
    </template>

    <!-- Users 图标（多用户/社区） -->
    <template v-else-if="name === 'users'">
      <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" stroke="currentColor" fill="none"/>
      <circle cx="9" cy="7" r="4" stroke="currentColor" fill="none"/>
      <path d="M23 21v-2a4 4 0 0 0-3-3.87" stroke="currentColor" fill="none"/>
      <path d="M16 3.13a4 4 0 0 1 0 7.75" stroke="currentColor" fill="none"/>
    </template>

    <!-- School 图标（学校/教育） -->
    <template v-else-if="name === 'school'">
      <path d="M22 10v6M2 10l10-5 10 5-10 5z" stroke="currentColor" fill="none"/>
      <path d="M6 12v5c0 1.66 2.69 3 6 3s6-1.34 6-3v-5" stroke="currentColor" fill="none"/>
    </template>

    <!-- Download 图标（下载） -->
    <template v-else-if="name === 'download'">
      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" stroke="currentColor" fill="none"/>
      <polyline points="7 10 12 15 17 10" stroke="currentColor" fill="none"/>
      <line x1="12" y1="15" x2="12" y2="3" stroke="currentColor"/>
    </template>

    <!-- Trash 2 图标（删除-更精致） -->
    <template v-else-if="name === 'trash-2'">
      <polyline points="3 6 5 6 21 6" stroke="currentColor" fill="none"/>
      <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" stroke="currentColor" fill="none"/>
      <line x1="10" y1="11" x2="10" y2="17" stroke="currentColor"/>
      <line x1="14" y1="11" x2="14" y2="17" stroke="currentColor"/>
    </template>

    <!-- Corner Down Left 图标（回复） -->
    <template v-else-if="name === 'corner-down-left'">
      <polyline points="9 10 4 15 9 20" stroke="currentColor" fill="none"/>
      <path d="M20 4v7a4 4 0 0 1-4 4H4" stroke="currentColor" fill="none"/>
    </template>

    <!-- Star Off 图标（未收藏） -->
    <template v-else-if="name === 'star-off'">
      <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" stroke="currentColor" fill="none"/>
    </template>

    <!-- Share 2 图标（分享-箭头样式） -->
    <template v-else-if="name === 'share-2'">
      <circle cx="18" cy="5" r="3" stroke="currentColor" fill="none"/>
      <circle cx="6" cy="12" r="3" stroke="currentColor" fill="none"/>
      <circle cx="18" cy="19" r="3" stroke="currentColor" fill="none"/>
      <line x1="8.59" y1="13.51" x2="15.42" y2="17.49" stroke="currentColor"/>
      <line x1="15.41" y1="6.51" x2="8.59" y2="10.49" stroke="currentColor"/>
    </template>

    <!-- CPU 图标（技术） -->
    <template v-else-if="name === 'cpu'">
      <rect x="4" y="4" width="16" height="16" rx="2" stroke="currentColor" fill="none"/>
      <rect x="9" y="9" width="6" height="6" stroke="currentColor" fill="none"/>
      <line x1="9" y1="1" x2="9" y2="4" stroke="currentColor"/>
      <line x1="15" y1="1" x2="15" y2="4" stroke="currentColor"/>
      <line x1="9" y1="20" x2="9" y2="23" stroke="currentColor"/>
      <line x1="15" y1="20" x2="15" y2="23" stroke="currentColor"/>
      <line x1="20" y1="9" x2="23" y2="9" stroke="currentColor"/>
      <line x1="20" y1="15" x2="23" y2="15" stroke="currentColor"/>
      <line x1="1" y1="9" x2="4" y2="9" stroke="currentColor"/>
      <line x1="1" y1="15" x2="4" y2="15" stroke="currentColor"/>
    </template>

    <!-- Heart Pulse 图标（体育） -->
    <template v-else-if="name === 'heart-pulse'">
      <path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z" stroke="currentColor" fill="none"/>
      <path d="M3.22 12h3.08L7.5 9.5l3 7 1.5-3.5h3.72" stroke="currentColor" fill="none"/>
    </template>

    <!-- Palette 图标（艺术） -->
    <template v-else-if="name === 'palette'">
      <circle cx="13.5" cy="6.5" r=".5" fill="currentColor" stroke="none"/>
      <circle cx="17.5" cy="10.5" r=".5" fill="currentColor" stroke="none"/>
      <circle cx="8.5" cy="7.5" r=".5" fill="currentColor" stroke="none"/>
      <circle cx="6.5" cy="12.5" r=".5" fill="currentColor" stroke="none"/>
      <path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c.926 0 1.648-.746 1.648-1.688 0-.437-.18-.835-.437-1.125-.29-.289-.438-.652-.438-1.125a1.64 1.64 0 0 1 1.668-1.668h1.996c3.051 0 5.555-2.503 5.555-5.554C21.965 6.012 17.461 2 12 2Z" stroke="currentColor" fill="none"/>
    </template>

    <!-- Sparkles 图标（兴趣） -->
    <template v-else-if="name === 'sparkles'">
      <path d="M9.937 15.5A2 2 0 0 0 8.5 14.063l-6.135-1.582a.5.5 0 0 1 0-.962L8.5 9.936A2 2 0 0 0 9.937 8.5l1.582-6.135a.5.5 0 0 1 .963 0L14.063 8.5A2 2 0 0 0 15.5 9.937l6.135 1.581a.5.5 0 0 1 0 .964L15.5 14.063a2 2 0 0 0-1.437 1.437l-1.582 6.135a.5.5 0 0 1-.963 0z" stroke="currentColor" fill="none"/>
      <path d="M20 3v4" stroke="currentColor"/>
      <path d="M22 5h-4" stroke="currentColor"/>
      <path d="M4 17v2" stroke="currentColor"/>
      <path d="M5 18H3" stroke="currentColor"/>
    </template>

    <!-- Plus Circle 图标（添加/创建） -->
    <template v-else-if="name === 'plus-circle'">
      <circle cx="12" cy="12" r="10" stroke="currentColor" fill="none"/>
      <line x1="12" y1="8" x2="12" y2="16" stroke="currentColor"/>
      <line x1="8" y1="12" x2="16" y2="12" stroke="currentColor"/>
    </template>

    <!-- External Link 图标（外链/新窗口） -->
    <template v-else-if="name === 'external-link'">
      <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" stroke="currentColor" fill="none"/>
      <polyline points="15 3 21 3 21 9" stroke="currentColor" fill="none"/>
      <line x1="10" y1="14" x2="21" y2="3" stroke="currentColor"/>
    </template>
  </svg>
</template>

<script setup lang="ts">
interface Props {
  name: string
  size?: number | string
  color?: string
  strokeWidth?: number | string
}

const props = withDefaults(defineProps<Props>(), {
  size: 24,
  strokeWidth: 2
})

const viewBox = '0 0 24 24'
const fill = 'none'
const stroke = props.color || 'currentColor'
</script>

<style lang="scss" scoped>
.icon {
  display: inline-block;
  vertical-align: middle;
  flex-shrink: 0;
  stroke-linecap: round;
  stroke-linejoin: round;
}
</style>
