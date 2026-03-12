<template>
  <view class="task-hall-page">

    <!-- ╔══════════════════════════════════════╗ -->
    <!-- ║         移动端独立布局                ║ -->
    <!-- ╚══════════════════════════════════════╝ -->
    <template v-if="!isDesktop">
      <view class="mobile-page">

        <!-- NavBar：title slot 放搜索框，right slot 放发布 -->
        <CNavBar :auto-back="true" :border="true">
          <template #title>
            <view class="m-nav-search">
              <Icon name="search" :size="15" class="m-nav-search-icon" />
              <input
                v-model="searchKeyword"
                class="m-nav-search-input"
                placeholder="搜索任务..."
                confirm-type="search"
                @confirm="handleSearch"
                @focus="handleSearchFocus"
                @blur="handleSearchBlur"
              />
              <view v-if="searchKeyword" class="m-nav-clear" @click.stop="clearSearch">
                <Icon name="x" :size="13" />
              </view>
            </view>
          </template>
          <template #right>
            <view class="m-publish-btn" @click="handlePublish">
              <Icon name="plus" :size="20" class="m-publish-icon" />
            </view>
          </template>
        </CNavBar>

        <!-- 搜索历史下拉（叠在 NavBar 正下方）-->
        <view v-if="showSearchHistory" class="m-search-dropdown">
          <template v-if="searchHistory.length > 0">
            <view class="m-sh-header">
              <text class="m-sh-title">搜索历史</text>
              <text class="m-sh-clear" @click="clearSearchHistory">清空</text>
            </view>
            <view class="m-sh-list">
              <view
                v-for="(item, index) in searchHistory"
                :key="index"
                class="m-sh-item"
                @click="handleSearchHistoryClick(item)"
              >
                <Icon name="clock" :size="13" class="m-sh-icon" />
                <text class="m-sh-text">{{ item }}</text>
                <view class="m-sh-remove" @click.stop="deleteSearchHistoryItem(item)">
                  <Icon name="x" :size="12" />
                </view>
              </view>
            </view>
          </template>
          <view v-else class="m-sh-empty">
            <Icon name="search" :size="28" class="m-sh-empty-icon" />
            <text class="m-sh-empty-text">暂无搜索历史</text>
          </view>
        </view>

        <!-- 筛选栏（类型滚动 + 状态 + 侧边栏入口）-->
        <view class="m-filter-bar">
          <scroll-view class="m-type-scroll" scroll-x :show-scrollbar="false">
            <view class="m-type-tabs">
              <view
                v-for="type in taskTypes"
                :key="type.value"
                class="m-type-tab"
                :class="{ active: currentType === type.value }"
                @click="handleTypeChange(type.value)"
              >
                <Icon :name="type.iconName" :size="13" class="m-tab-icon" />
                <text class="m-tab-label">{{ type.label }}</text>
              </view>
            </view>
          </scroll-view>

          <!-- 右侧：状态下拉 + 数据面板入口 -->
          <view class="m-filter-right">
            <view
              class="m-status-btn"
              :class="{ active: currentStatus !== '' }"
              @click.stop="toggleStatusDropdown"
            >
              <text class="m-status-label">{{ currentStatusLabel }}</text>
              <Icon
                name="chevron-down"
                :size="11"
                class="m-dropdown-arrow"
                :class="{ rotated: statusDropdownOpen }"
              />
            </view>
            <view class="m-info-btn" @click="openMobileSidebar">
              <Icon name="sliders-horizontal" :size="16" class="m-info-icon" />
              <view v-if="mobileActiveCount > 0" class="m-info-dot" />
            </view>
          </view>

          <!-- 状态下拉菜单 -->
          <view v-if="statusDropdownOpen" class="m-status-menu">
            <view
              v-for="opt in statusOptions"
              :key="opt.value"
              class="m-menu-item"
              :class="{ active: currentStatus === opt.value }"
              @click.stop="selectStatus(opt.value)"
            >
              <text class="m-menu-label">{{ opt.label }}</text>
              <Icon v-if="currentStatus === opt.value" name="check" :size="13" class="m-menu-check" />
            </view>
          </view>
        </view>

        <!-- 状态遮罩 -->
        <view v-if="statusDropdownOpen" class="dropdown-mask" @click="statusDropdownOpen = false" />

        <!-- 我的任务快捷条（已登录时显示）-->
        <view v-if="userStore.isLoggedIn" class="m-my-strip" @click="openMobileSidebar">
          <view class="m-strip-left">
            <Icon name="clipboard-list" :size="14" class="m-strip-icon" />
            <text class="m-strip-title">我的任务</text>
          </view>
          <view class="m-strip-stats">
            <view class="m-strip-cell">
              <text class="m-strip-num m-strip-num--pending">{{ myStats.pending }}</text>
              <text class="m-strip-name">待接单</text>
            </view>
            <view class="m-strip-div" />
            <view class="m-strip-cell">
              <text class="m-strip-num m-strip-num--ongoing">{{ myStats.ongoing }}</text>
              <text class="m-strip-name">进行中</text>
            </view>
            <view class="m-strip-div" />
            <view class="m-strip-cell">
              <text class="m-strip-num m-strip-num--done">{{ myStats.done }}</text>
              <text class="m-strip-name">已完成</text>
            </view>
          </view>
          <Icon name="chevron-right" :size="14" class="m-strip-arrow" />
        </view>

        <!-- ───── 主列表（scroll-view 接管滚动）───── -->
        <scroll-view
          class="m-scroll"
          scroll-y
          refresher-enabled
          :refresher-triggered="mobileRefreshing"
          @refresherrefresh="handleMobileRefresh"
          @scrolltolower="handleScrollToLower"
        >
          <!-- 骨架屏 -->
          <SkeletonScreen v-if="loading && taskList.length === 0" type="card" :count="4" />

          <!-- 任务卡片列表 -->
          <view v-if="!loading || taskList.length > 0" class="m-task-list">
            <view
              v-for="task in taskList"
              :key="task.tid"
              class="m-task-card"
              @click="handleTaskClick(task)"
            >
              <!-- 行1：类型标签 + 状态 -->
              <view class="m-card-top">
                <view class="type-badge" :class="`type-${task.taskType}`">
                  <Icon :name="getTypeIconName(task.taskType)" :size="11" class="type-badge-icon" />
                  <text class="type-badge-label">{{ getTypeLabel(task.taskType) }}</text>
                </view>
                <view
                  class="status-tag"
                  :class="[`status-${task.status}`, { 'status-expired': task.status === 0 && isTaskExpired(task) }]"
                >
                  <text class="status-text">{{ getStatusLabel(task.status, task) }}</text>
                </view>
              </view>

              <!-- 行2：标题 -->
              <text class="m-card-title">{{ task.title }}</text>

              <!-- 行3：内容摘要 -->
              <text v-if="task.content" class="m-card-excerpt">{{ task.content }}</text>

              <!-- 行4：发布者 + 时间 + 地点/截止 -->
              <view class="m-card-meta">
                <view class="avatar-placeholder" :style="getAvatarBg(task.publisherNickname)">
                  <text class="avatar-char">{{ task.publisherNickname?.charAt(0)?.toUpperCase() || '?' }}</text>
                </view>
                <text class="meta-name">{{ task.publisherNickname }}</text>
                <text class="meta-dot">·</text>
                <text class="meta-time">{{ formatTime(task.createdAt) }}</text>
                <view v-if="task.location && !parseCoord(task.location)" class="meta-loc">
                  <Icon name="map-pin" :size="10" class="meta-loc-icon" />
                  <text class="meta-loc-text">{{ task.location }}</text>
                </view>
                <view v-else-if="task.deadline" class="meta-loc">
                  <Icon name="clock" :size="10" class="meta-loc-icon" />
                  <text class="meta-loc-text">{{ formatDeadline(task.deadline) }}</text>
                </view>
              </view>

              <!-- 行5：积分 + 快捷接单 -->
              <view class="m-card-footer">
                <view class="reward-pill">
                  <Icon name="zap" :size="12" class="reward-icon" />
                  <text class="reward-pts">{{ task.rewardPoints }}</text>
                  <text class="reward-unit">积分</text>
                </view>
                <view
                  v-if="task.status === 0 && !isTaskExpired(task) && userStore.isLoggedIn && task.publisherNickname !== userStore.userInfo?.nickname"
                  class="quick-accept-btn"
                  @click.stop="handleQuickAccept(task)"
                >
                  <text>立即接单</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 空状态 -->
          <view v-if="!loading && taskList.length === 0" class="empty-state">
            <Icon name="clipboard-list" :size="56" class="empty-icon" />
            <text class="empty-text">暂无任务</text>
            <text class="empty-tip">
              {{ (currentType || currentStatus) ? '未找到符合条件的任务，试试调整筛选条件' : '快去发布一个新任务吧~' }}
            </text>
            <view v-if="currentType || currentStatus" class="empty-action-btn" @click="resetFilters">
              <Icon name="rotate-ccw" :size="14" />
              <text class="empty-action-text">重置筛选</text>
            </view>
            <view v-else class="empty-action-btn primary" @click="handlePublish">
              <Icon name="plus" :size="14" />
              <text class="empty-action-text">发布任务</text>
            </view>
          </view>

          <!-- 加载更多 -->
          <view v-if="taskList.length > 0" class="load-more">
            <text v-if="loadingMore" class="load-more-text">加载中...</text>
            <text v-else-if="!hasMore" class="load-more-text">没有更多了</text>
          </view>

          <view class="m-safe-bottom" />
        </scroll-view>

        <!-- ───── Bottom Sheet：数据面板 ───── -->
        <view
          v-if="showMobileSidebar"
          class="bs-overlay"
          :class="{ 'bs-overlay--dim': bsUp }"
          @click="closeMobileSidebar"
        >
          <view class="bs-sheet" :class="{ 'bs-sheet--up': bsUp }" @click.stop>
            <view class="bs-bar" />

            <!-- Sheet 头部 -->
            <view class="bs-header">
              <text class="bs-title">任务中心</text>
              <view class="bs-close" @click="closeMobileSidebar">
                <Icon name="x" :size="18" class="bs-close-icon" />
              </view>
            </view>

            <scroll-view class="bs-scroll" scroll-y>

              <!-- 我的任务（登录后） -->
              <view v-if="userStore.isLoggedIn" class="bs-card">
                <view class="bs-card-header">
                  <Icon name="clipboard-list" :size="14" class="bs-card-icon" />
                  <text class="bs-card-title">我的任务</text>
                </view>
                <view class="bs-stats-row">
                  <view class="bs-stat">
                    <text class="bs-stat-num bs-stat-num--pending">{{ myStats.pending }}</text>
                    <text class="bs-stat-label">待接单</text>
                  </view>
                  <view class="bs-stat-div" />
                  <view class="bs-stat">
                    <text class="bs-stat-num bs-stat-num--ongoing">{{ myStats.ongoing }}</text>
                    <text class="bs-stat-label">进行中</text>
                  </view>
                  <view class="bs-stat-div" />
                  <view class="bs-stat">
                    <text class="bs-stat-num bs-stat-num--done">{{ myStats.done }}</text>
                    <text class="bs-stat-label">已完成</text>
                  </view>
                </view>
                <view v-if="totalTaskDone > 0 || (userStore.userInfo?.points ?? 0) > 0" class="bs-achievement">
                  <Icon name="award" :size="13" class="bs-award-icon" />
                  <text class="bs-achievement-text">
                    累计完成 {{ totalTaskDone }} 个任务，持有 {{ userStore.userInfo?.points ?? 0 }} 积分
                  </text>
                </view>
                <view class="bs-quick-row">
                  <view class="bs-quick-btn" @click="goMyPublished">
                    <Icon name="send" :size="13" class="bs-quick-icon" />
                    <text class="bs-quick-label">我的发布</text>
                  </view>
                  <view class="bs-quick-btn" @click="goMyAccepted">
                    <Icon name="package-check" :size="13" class="bs-quick-icon" />
                    <text class="bs-quick-label">我的接单</text>
                  </view>
                </view>
              </view>

              <!-- 高积分任务 -->
              <view v-if="hotTasks.length > 0" class="bs-card">
                <view class="bs-card-header">
                  <Icon name="flame" :size="14" class="bs-card-icon bs-card-icon--hot" />
                  <text class="bs-card-title">高积分任务</text>
                </view>
                <view class="bs-hot-list">
                  <view
                    v-for="(task, index) in hotTasks"
                    :key="task.tid"
                    class="bs-hot-item"
                    @click="() => { closeMobileSidebar(); setTimeout(() => handleTaskClick(task), 320) }"
                  >
                    <text class="bs-hot-rank" :class="`rank-${index + 1}`">{{ index + 1 }}</text>
                    <text class="bs-hot-title">{{ task.rewardPoints >= 50 ? '🔥 ' + task.title : task.title }}</text>
                    <view class="bs-hot-reward">
                      <Icon name="zap" :size="11" class="bs-hot-icon" />
                      <text class="bs-hot-pts">{{ task.rewardPoints }}</text>
                    </view>
                  </view>
                </view>
              </view>

              <!-- 今日动态 -->
              <view v-if="todayPublished > 0" class="bs-card bs-card--compact">
                <view class="bs-platform-row">
                  <view class="bs-platform-item">
                    <text class="bs-platform-num">{{ todayPublished }}</text>
                    <text class="bs-platform-label">今日发布</text>
                  </view>
                  <view class="bs-platform-div" />
                  <view class="bs-platform-item">
                    <text class="bs-platform-num">{{ taskList.length }}</text>
                    <text class="bs-platform-label">当前在线</text>
                  </view>
                </view>
              </view>

              <!-- 附近任务（已获取定位）-->
              <view v-if="nearbyTasks.length > 0" class="bs-card">
                <view class="bs-card-header">
                  <Icon name="map-pin" :size="14" class="bs-card-icon bs-card-icon--nearby" />
                  <text class="bs-card-title">附近任务</text>
                  <view class="bs-nearby-refresh" @click="loadNearbyTasks">
                    <Icon name="rotate-cw" :size="13" :class="{ spinning: locationLoading }" />
                  </view>
                </view>
                <view class="bs-nearby-list">
                  <view
                    v-for="task in nearbyTasks"
                    :key="task.tid"
                    class="bs-nearby-item"
                    @click="() => { closeMobileSidebar(); setTimeout(() => handleTaskClick(task), 320) }"
                  >
                    <view class="bs-nearby-info">
                      <text class="bs-nearby-title">{{ task.title }}</text>
                      <view class="bs-nearby-meta">
                        <Icon name="map-pin" :size="10" class="bs-nearby-icon" />
                        <text class="bs-nearby-dist">{{ formatDist(task.distance) }}</text>
                        <text class="bs-nearby-dot">·</text>
                        <Icon name="zap" :size="10" class="bs-nearby-icon" />
                        <text class="bs-nearby-pts">{{ task.rewardPoints }}</text>
                      </view>
                    </view>
                    <Icon name="chevron-right" :size="13" class="bs-nearby-arrow" />
                  </view>
                </view>
              </view>

              <!-- 定位引导（未获取定位时，仅 H5）-->
              <!-- #ifdef H5 -->
              <view
                v-if="nearbyTasks.length === 0 && !locationLoading && !userLocation"
                class="bs-location-prompt"
                @click="loadNearbyTasks"
              >
                <Icon name="map-pin" :size="15" class="bs-location-icon" />
                <text class="bs-location-text">开启定位，发现附近任务</text>
                <Icon name="chevron-right" :size="13" class="bs-location-arrow" />
              </view>
              <!-- #endif -->

              <view class="bs-safe-bottom" />
            </scroll-view>
          </view>
        </view>

        <!-- 移动端底部导航 -->
        <CustomTabBar />

      </view>
    </template>

    <!-- ╔══════════════════════════════════════╗ -->
    <!-- ║         桌面端布局（完全保持原有）     ║ -->
    <!-- ╚══════════════════════════════════════╝ -->
    <template v-else>

      <!-- 固定顶部导航区 -->
      <view class="top-nav-fixed" :class="{ collapsed: isHeaderCollapsed }">
        <view class="top-nav-container">
          <view class="brand-logo">
            <Icon name="clipboard-list" :size="20" class="logo-icon" />
            <text class="logo-text">任务大厅</text>
          </view>
          <view class="search-wrapper">
            <view class="compact-search-bar">
              <Icon name="search" :size="16" class="search-icon" />
              <input
                v-model="searchKeyword"
                class="search-input"
                placeholder="搜索任务..."
                confirm-type="search"
                @confirm="handleSearch"
                @focus="handleSearchFocus"
                @blur="handleSearchBlur"
              />
              <view v-if="searchKeyword" class="clear-icon" @click="clearSearch">
                <Icon name="x" :size="14" />
              </view>
            </view>
            <view v-if="showSearchHistory" class="search-history-dropdown">
              <template v-if="searchHistory.length > 0">
                <view class="history-header">
                  <text class="history-title">搜索历史</text>
                  <text class="history-clear" @click="clearSearchHistory">清空</text>
                </view>
                <view class="history-list">
                  <view
                    v-for="(item, index) in searchHistory"
                    :key="index"
                    class="history-item"
                    @click="handleSearchHistoryClick(item)"
                  >
                    <Icon name="clock" :size="14" class="history-icon" />
                    <text class="history-text">{{ item }}</text>
                    <Icon name="x" :size="14" class="history-remove" @click.stop="deleteSearchHistoryItem(item)" />
                  </view>
                </view>
              </template>
              <view v-else class="history-empty">
                <Icon name="search" :size="32" class="history-empty-icon" />
                <text class="empty-text">暂无搜索历史</text>
                <text class="empty-hint">搜索后会自动记录</text>
              </view>
            </view>
          </view>
          <view class="publish-btn" @click="handlePublish">
            <Icon name="plus" :size="15" class="publish-icon" />
            <text class="publish-text">发布</text>
          </view>
        </view>
      </view>

      <view class="nav-spacer" />

      <!-- Sticky 筛选区 -->
      <view class="sticky-nav" :class="{ 'header-collapsed': isHeaderCollapsed }">
        <view class="filter-nav-container">
          <view class="type-tabs-wrap">
            <scroll-view class="type-scroll" scroll-x :show-scrollbar="false">
              <view class="type-tabs">
                <view
                  v-for="type in taskTypes"
                  :key="type.value"
                  class="type-tab"
                  :class="{ active: currentType === type.value }"
                  @click="handleTypeChange(type.value)"
                >
                  <Icon :name="type.iconName" :size="14" class="type-tab-icon" />
                  <text class="type-tab-label">{{ type.label }}</text>
                </view>
              </view>
            </scroll-view>
          </view>
          <view class="status-dropdown-wrap">
            <view class="status-dropdown-btn" :class="{ active: currentStatus !== '' }" @click.stop="toggleStatusDropdown">
              <text class="status-dropdown-label">{{ currentStatusLabel }}</text>
              <Icon name="chevron-down" :size="12" class="dropdown-arrow" :class="{ rotated: statusDropdownOpen }" />
            </view>
            <view v-if="statusDropdownOpen" class="status-dropdown-menu">
              <view
                v-for="opt in statusOptions"
                :key="opt.value"
                class="dropdown-item"
                :class="{ active: currentStatus === opt.value }"
                @click.stop="selectStatus(opt.value)"
              >
                <text class="dropdown-item-label">{{ opt.label }}</text>
                <Icon v-if="currentStatus === opt.value" name="check" :size="13" class="dropdown-item-check" />
              </view>
            </view>
          </view>
        </view>
      </view>

      <view v-if="statusDropdownOpen" class="dropdown-mask" @click="statusDropdownOpen = false" />

      <!-- 主内容区 -->
      <view class="main-content">
        <view class="content-container">

          <!-- 主列表 -->
          <view class="task-main">
            <SkeletonScreen v-if="loading && taskList.length === 0" type="card" :count="4" />
            <view v-if="!loading || taskList.length > 0" class="task-list">
              <view
                v-for="task in taskList"
                :key="task.tid"
                class="task-card"
                @click="handleTaskClick(task)"
              >
                <view class="card-row1">
                  <view class="card-row1-left">
                    <view class="type-badge" :class="`type-${task.taskType}`">
                      <Icon :name="getTypeIconName(task.taskType)" :size="12" class="type-badge-icon" />
                      <text class="type-badge-label">{{ getTypeLabel(task.taskType) }}</text>
                    </view>
                    <text class="card-title">{{ task.title }}</text>
                  </view>
                  <view
                    class="status-tag"
                    :class="[`status-${task.status}`, { 'status-expired': task.status === 0 && isTaskExpired(task) }]"
                  >
                    <text class="status-text">{{ getStatusLabel(task.status, task) }}</text>
                  </view>
                </view>
                <text v-if="task.content" class="card-excerpt">{{ task.content }}</text>
                <view class="card-row2">
                  <view class="meta-avatar">
                    <view class="avatar-placeholder" :style="getAvatarBg(task.publisherNickname)">
                      <text class="avatar-char">{{ task.publisherNickname?.charAt(0)?.toUpperCase() || '?' }}</text>
                    </view>
                  </view>
                  <text class="meta-name">{{ task.publisherNickname }}</text>
                  <text class="meta-dot">·</text>
                  <text class="meta-time">{{ formatTime(task.createdAt) }}</text>
                  <view v-if="task.location && !parseCoord(task.location)" class="meta-loc">
                    <Icon name="map-pin" :size="11" class="meta-loc-icon" />
                    <text class="meta-loc-text">{{ task.location }}</text>
                  </view>
                  <view v-else-if="task.deadline" class="meta-loc">
                    <Icon name="clock" :size="11" class="meta-loc-icon" />
                    <text class="meta-loc-text">{{ formatDeadline(task.deadline) }}</text>
                  </view>
                </view>
                <view class="card-row3">
                  <view class="reward-pill">
                    <Icon name="zap" :size="12" class="reward-icon" />
                    <text class="reward-pts">{{ task.rewardPoints }}</text>
                    <text class="reward-unit">积分</text>
                  </view>
                  <view
                    v-if="task.status === 0 && !isTaskExpired(task) && userStore.isLoggedIn && task.publisherNickname !== userStore.userInfo?.nickname"
                    class="quick-accept-btn"
                    @click.stop="handleQuickAccept(task)"
                  >
                    <text>立即接单</text>
                  </view>
                </view>
              </view>
            </view>

            <view v-if="!loading && taskList.length === 0" class="empty-state">
              <Icon name="clipboard-list" :size="56" class="empty-icon" />
              <text class="empty-text">暂无任务</text>
              <text class="empty-tip">{{ (currentType || currentStatus) ? '未找到符合条件的任务，试试调整筛选条件' : '快去发布一个新任务吧~' }}</text>
              <view v-if="currentType || currentStatus" class="empty-action-btn" @click="resetFilters">
                <Icon name="rotate-ccw" :size="14" />
                <text class="empty-action-text">重置筛选</text>
              </view>
              <view v-else class="empty-action-btn primary" @click="handlePublish">
                <Icon name="plus" :size="14" />
                <text class="empty-action-text">发布任务</text>
              </view>
            </view>

            <view v-if="taskList.length > 0" class="load-more">
              <text v-if="loadingMore" class="load-more-text">加载中...</text>
              <text v-else-if="!hasMore" class="load-more-text">没有更多了</text>
            </view>
            <view class="safe-area-bottom" />
          </view>

          <!-- 侧边栏（桌面端） -->
          <view class="task-sidebar" :class="{ 'mobile-open': showMobileSidebar }">
            <view class="mobile-sidebar-close" @click="showMobileSidebar = false">
              <Icon name="x" :size="18" />
            </view>
            <view class="sidebar-card" v-if="userStore.isLoggedIn">
              <view class="sidebar-card-header">
                <Icon name="clipboard-list" :size="15" class="sidebar-header-icon" />
                <text class="sidebar-card-title">我的任务</text>
              </view>
              <view class="my-stats-grid">
                <view class="stat-cell">
                  <text class="stat-num stat-pending">{{ myStats.pending }}</text>
                  <text class="stat-label">待接单</text>
                </view>
                <view class="stat-cell">
                  <text class="stat-num stat-ongoing">{{ myStats.ongoing }}</text>
                  <text class="stat-label">进行中</text>
                </view>
                <view class="stat-cell">
                  <text class="stat-num stat-done">{{ myStats.done }}</text>
                  <text class="stat-label">已完成</text>
                </view>
              </view>
              <view v-if="totalTaskDone > 0 || (userStore.userInfo?.points ?? 0) > 0" class="achievement-banner">
                <Icon name="award" :size="14" class="achievement-icon" />
                <view class="achievement-content">
                  <text class="achievement-text">
                    累计完成 <text class="achievement-highlight">{{ totalTaskDone }}</text> 个任务，持有 <text class="achievement-highlight">{{ userStore.userInfo?.points ?? 0 }}</text> 积分
                  </text>
                  <view v-if="weekTrend" class="trend-row">
                    <Icon
                      :name="weekTrend.diff >= 0 ? 'trending-up' : 'trending-down'"
                      :size="12"
                      class="trend-icon"
                      :class="weekTrend.diff >= 0 ? 'trend-up' : 'trend-down'"
                    />
                    <text class="trend-text" :class="weekTrend.diff >= 0 ? 'trend-up' : 'trend-down'">
                      本周 +{{ weekTrend.thisWeekPts }} 积分{{ weekTrend.diff !== 0 ? `，较上周 ${weekTrend.diff > 0 ? '+' : ''}${weekTrend.diff}` : '' }}
                    </text>
                  </view>
                </view>
              </view>
              <view class="sidebar-link-row">
                <view class="sidebar-quick-btn" @click="goMyPublished">
                  <Icon name="send" :size="13" class="quick-btn-icon" />
                  <text class="quick-btn-label">我的发布</text>
                </view>
                <view class="sidebar-quick-btn" @click="goMyAccepted">
                  <Icon name="package-check" :size="13" class="quick-btn-icon" />
                  <text class="quick-btn-label">我的接单</text>
                </view>
              </view>
            </view>
            <view class="sidebar-card" v-if="todayPublished > 0">
              <view class="sidebar-card-header">
                <Icon name="bar-chart-2" :size="15" class="sidebar-header-icon" />
                <text class="sidebar-card-title">今日动态</text>
              </view>
              <view class="platform-stats">
                <view class="platform-stat-item">
                  <text class="platform-stat-num">{{ todayPublished }}</text>
                  <text class="platform-stat-label">今日发布</text>
                </view>
                <view class="platform-stat-divider" />
                <view class="platform-stat-item">
                  <text class="platform-stat-num">{{ taskList.length }}</text>
                  <text class="platform-stat-label">当前在线</text>
                </view>
              </view>
            </view>
            <view class="sidebar-card" v-if="hotTasks.length > 0">
              <view class="sidebar-card-header">
                <Icon name="flame" :size="15" class="sidebar-header-icon" />
                <text class="sidebar-card-title">高积分任务</text>
              </view>
              <view class="hot-task-list">
                <view
                  v-for="(task, index) in hotTasks"
                  :key="task.tid"
                  class="hot-task-item"
                  @click="handleTaskClick(task)"
                >
                  <text class="hot-rank" :class="`rank-${index + 1}`">{{ index + 1 }}</text>
                  <text class="hot-title">{{ task.rewardPoints >= 50 ? '🔥 ' + task.title : task.title }}</text>
                  <view class="hot-reward">
                    <Icon name="zap" :size="11" />
                    <text class="hot-pts">{{ task.rewardPoints }}</text>
                  </view>
                </view>
              </view>
            </view>
            <view class="sidebar-card" v-if="nearbyTasks.length > 0">
              <view class="sidebar-card-header">
                <Icon name="map-pin" :size="15" class="sidebar-header-icon nearby-icon" />
                <text class="sidebar-card-title">附近任务</text>
                <view class="nearby-refresh" @click="loadNearbyTasks">
                  <Icon name="rotate-cw" :size="13" class="nearby-refresh-icon" :class="{ spinning: locationLoading }" />
                </view>
              </view>
              <view class="nearby-task-list">
                <view
                  v-for="task in nearbyTasks"
                  :key="task.tid"
                  class="nearby-task-item"
                  @click="handleTaskClick(task)"
                >
                  <view class="nearby-task-info">
                    <text class="nearby-task-title">{{ task.title }}</text>
                    <view class="nearby-task-meta">
                      <Icon name="map-pin" :size="11" class="nearby-meta-icon" />
                      <text class="nearby-dist">{{ formatDist(task.distance) }}</text>
                      <text class="nearby-sep">·</text>
                      <Icon name="zap" :size="11" class="nearby-pts-icon" />
                      <text class="nearby-pts">{{ task.rewardPoints }}</text>
                    </view>
                  </view>
                  <Icon name="chevron-right" :size="14" class="nearby-arrow" />
                </view>
              </view>
            </view>
            <!-- #ifdef H5 -->
            <view v-if="nearbyTasks.length === 0 && !locationLoading && !userLocation" class="location-prompt" @click="loadNearbyTasks">
              <Icon name="map-pin" :size="15" class="location-prompt-icon" />
              <text class="location-prompt-text">开启定位，发现附近任务</text>
              <Icon name="chevron-right" :size="13" class="location-prompt-arrow" />
            </view>
            <!-- #endif -->
          </view>

        </view>
      </view>

      <view v-if="showMobileSidebar" class="mobile-sidebar-backdrop" @click="showMobileSidebar = false" />

      <!-- PC端悬浮导航（仅 H5） -->
      <!-- #ifdef H5 -->
      <PCFloatingNav />
      <!-- #endif -->

    </template>

  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { onPageScroll } from '@dcloudio/uni-app'
import { getTaskList, getMyPublishedTasks, getMyAcceptedTasks, acceptTask } from '@/services/task'
import { getTodayStats } from '@/services/stats'
import { getUserStats, getPointsLog } from '@/services/user'
import { useUserStore } from '@/stores/user'
import type { TaskStatus, TaskListItem, TaskType } from '@/types/task'
import SkeletonScreen from '@/components/SkeletonScreen.vue'
import Icon from '@/components/icons/index.vue'
import CNavBar from '@/components/layout/CNavBar.vue'
import { taskSearchHistory } from '@/utils/searchHistory'

import { CustomTabBar } from '@/components/mobile'

// #ifdef H5
import { PCFloatingNav } from '@/components/desktop'
// #endif

const userStore = useUserStore()

const statusOptions = [
  { value: '', label: '全部' },
  { value: '0', label: '待接单' },
  { value: '1', label: '进行中' },
  { value: '2', label: '已完成' },
  { value: '3', label: '已取消' },
  { value: '6', label: '已超时' }
]

const taskTypes = [
  { value: '', label: '全部', iconName: 'layout-grid' },
  { value: 'errand', label: '跑腿', iconName: 'footprints' },
  { value: 'borrow', label: '借用', iconName: 'handshake' },
  { value: 'tutor', label: '答疑互助', iconName: 'book-open' },
  { value: 'other', label: '其他', iconName: 'package' }
]

const AVATAR_COLORS = ['#1677FF', '#52C41A', '#FF6B35', '#722ED1', '#EB2F96', '#13C2C2', '#FA8C16']
const getAvatarBg = (name: string) => {
  const idx = name ? name.charCodeAt(0) % AVATAR_COLORS.length : 0
  return { background: AVATAR_COLORS[idx] }
}

const FILTER_PREF_KEY = 'task_hall_filter'
const savedPref = (() => { try { return JSON.parse(uni.getStorageSync(FILTER_PREF_KEY) || '{}') } catch { return {} } })()
const currentStatus = ref<string>(savedPref.status ?? '')
const currentType = ref<string>(savedPref.type ?? '')
const taskList = ref<TaskListItem[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(1)
const pageSize = 20
const searchKeyword = ref('')
const showSearchHistory = ref(false)
const searchHistory = ref<string[]>([])
const isHeaderCollapsed = ref(false)
const statusDropdownOpen = ref(false)
const showMobileSidebar = ref(false)

// 移动端新增状态
const mobileRefreshing = ref(false)
const bsUp = ref(false)

const currentStatusLabel = computed(() => {
  const opt = statusOptions.find(o => o.value === currentStatus.value)
  return opt ? opt.label : '全部状态'
})

const toggleStatusDropdown = () => { statusDropdownOpen.value = !statusDropdownOpen.value }

const selectStatus = (status: string) => {
  statusDropdownOpen.value = false
  handleStatusChange(status)
}

const resetFilters = () => {
  currentType.value = ''
  currentStatus.value = ''
  taskList.value = []
  loadTasks(true)
}

const loadTasks = async (isRefresh = false) => {
  if (isRefresh) { page.value = 1; hasMore.value = true }
  if (!hasMore.value && !isRefresh) return
  try {
    if (page.value === 1) loading.value = true
    else loadingMore.value = true
    const params: any = { page: page.value, pageSize, sortBy: 'created_at', sortOrder: 'desc' }
    if (currentStatus.value !== '') params.status = currentStatus.value
    if (currentType.value) params.taskType = currentType.value
    if (searchKeyword.value) params.keyword = searchKeyword.value
    const result = await getTaskList(params)
    if (isRefresh || page.value === 1) taskList.value = result.list
    else taskList.value = [...taskList.value, ...result.list]
    hasMore.value = page.value < result.totalPages
  } catch (error: any) {
    console.error('加载任务列表失败:', error)
    uni.showToast({ title: error.message || '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const saveFilterPref = () => {
  try { uni.setStorageSync(FILTER_PREF_KEY, JSON.stringify({ status: currentStatus.value, type: currentType.value })) } catch {}
}

const handleStatusChange = (status: string) => {
  if (currentStatus.value === status) return
  currentStatus.value = status
  saveFilterPref()
  taskList.value = []
  loadTasks(true)
}

const handleTypeChange = (type: string) => {
  if (currentType.value === type) return
  currentType.value = type
  saveFilterPref()
  taskList.value = []
  loadTasks(true)
}

const loadSearchHistory = () => { searchHistory.value = taskSearchHistory.getHistory() }

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    taskSearchHistory.add(searchKeyword.value.trim())
    loadSearchHistory()
  }
  showSearchHistory.value = false
  taskList.value = []
  loadTasks(true)
}

const clearSearch = () => {
  searchKeyword.value = ''
  taskList.value = []
  loadTasks(true)
}

const clearSearchHistory = () => {
  uni.showModal({
    title: '提示',
    content: '确定清空所有搜索历史吗？',
    success: (res) => { if (res.confirm) { taskSearchHistory.clear(); loadSearchHistory() } }
  })
}

const deleteSearchHistoryItem = (keyword: string) => { taskSearchHistory.remove(keyword); loadSearchHistory() }
const handleSearchHistoryClick = (keyword: string) => { showSearchHistory.value = false; searchKeyword.value = keyword; handleSearch() }
const handleSearchFocus = () => { setTimeout(() => { showSearchHistory.value = true }, 100) }
const handleSearchBlur = () => { setTimeout(() => { showSearchHistory.value = false }, 200) }

const handleTaskClick = (task: TaskListItem) => {
  uni.navigateTo({ url: `/pages/task/detail?id=${task.tid}` })
}

const handleQuickAccept = (task: TaskListItem) => {
  uni.showModal({
    title: '确认接单',
    content: `接受任务「${task.title}」？`,
    confirmText: '接单',
    confirmColor: '#1677FF',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await acceptTask(task.tid)
        uni.showToast({ title: '接单成功', icon: 'success' })
        taskList.value = []
        loadTasks(true)
      } catch (e: any) {
        uni.showToast({ title: e.message || '接单失败', icon: 'none' })
      }
    }
  })
}

const windowWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 0)
const isDesktop = computed(() => windowWidth.value >= 1024)

const handlePublish = () => { uni.navigateTo({ url: '/pages/task/publish' }) }
const goMyPublished = () => { uni.navigateTo({ url: '/pages/task/my?tab=published' }) }
const goMyAccepted  = () => { uni.navigateTo({ url: '/pages/task/my?tab=accepted' }) }

const getTypeIconName = (type: TaskType): string => {
  const iconMap: Record<string, string> = { errand: 'footprints', borrow: 'handshake', tutor: 'book-open', other: 'package' }
  return iconMap[type] || 'package'
}

const getTypeLabel = (type: TaskType): string => {
  const labelMap: Record<string, string> = { errand: '跑腿', borrow: '借用', tutor: '答疑互助', other: '其他' }
  return labelMap[type] || '其他'
}

const isTaskExpired = (task: TaskListItem): boolean => {
  if (!task.deadline) return false
  return new Date() > new Date(task.deadline)
}

const getStatusLabel = (status: TaskStatus | number, task?: TaskListItem): string => {
  const statusNum = typeof status === 'string' ? parseInt(status) : status
  if (statusNum === 0 && task && isTaskExpired(task)) return '已截止'
  const labelMap: Record<number, string> = {
    0: '待接单', 1: '已接取', 2: '进行中',
    3: '待确认', 4: '已完成', 5: '已取消', 6: '已超时'
  }
  return labelMap[statusNum] !== undefined ? labelMap[statusNum] : '未知'
}

const formatTime = (dateStr: string): string => {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 172800000) return '昨天'
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `${month}-${day}`
}

const formatDeadline = (dateStr: string): string => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = date.getTime() - now.getTime()
  if (diff < 0) return '已截止'
  if (diff < 3600000) return `剩余 ${Math.floor(diff / 60000)} 分钟`
  if (diff < 86400000) return `剩余 ${Math.floor(diff / 3600000)} 小时`
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `截止 ${month}-${day}`
}

// 侧边栏数据
interface MyTaskStats { pending: number; ongoing: number; done: number }
const myStats = ref<MyTaskStats>({ pending: 0, ongoing: 0, done: 0 })
const mobileActiveCount = computed(() => userStore.isLoggedIn ? myStats.value.pending + myStats.value.ongoing : 0)
const hotTasks = ref<TaskListItem[]>([])
const todayPublished = ref<number>(0)
const totalTaskDone = ref<number>(0)

interface WeekTrend { thisWeekPts: number; diff: number }
const weekTrend = ref<WeekTrend | null>(null)

// 附近任务
interface NearbyTask extends TaskListItem { distance: number }
const nearbyTasks = ref<NearbyTask[]>([])
const userLocation = ref<{ lat: number; lng: number } | null>(null)
const locationLoading = ref(false)

const parseCoord = (loc: string): { lat: number; lng: number } | null => {
  if (!loc) return null
  const m = loc.match(/^(-?\d+\.?\d*)\s*[,，]\s*(-?\d+\.?\d*)$/)
  if (!m) return null
  const a = parseFloat(m[1]), b = parseFloat(m[2])
  return Math.abs(a) > 90 ? { lat: b, lng: a } : { lat: a, lng: b }
}

const haversine = (lat1: number, lng1: number, lat2: number, lng2: number): number => {
  const R = 6371, toRad = (d: number) => d * Math.PI / 180
  const dLat = toRad(lat2 - lat1), dLng = toRad(lng2 - lng1)
  const a = Math.sin(dLat / 2) ** 2 + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng / 2) ** 2
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
}

const formatDist = (km: number): string => km < 1 ? `${Math.round(km * 1000)}m` : `${km.toFixed(1)}km`

const loadNearbyTasks = async () => {
  /* #ifdef H5 */
  if (typeof navigator === 'undefined' || !navigator.geolocation || locationLoading.value) return
  locationLoading.value = true
  try {
    const pos = await new Promise<GeolocationPosition>((resolve, reject) =>
      navigator.geolocation.getCurrentPosition(resolve, reject, { timeout: 6000, maximumAge: 120000 })
    )
    userLocation.value = { lat: pos.coords.latitude, lng: pos.coords.longitude }
    const result = await getTaskList({ pageSize: 50, status: 0 })
    const { lat, lng } = userLocation.value
    nearbyTasks.value = (result.list || [])
      .map(t => ({ ...t, _coord: parseCoord(t.location) }))
      .filter((t): t is typeof t & { _coord: NonNullable<ReturnType<typeof parseCoord>> } => t._coord !== null)
      .map(t => ({ ...t, distance: haversine(lat, lng, t._coord.lat, t._coord.lng) }))
      .sort((a, b) => a.distance - b.distance)
      .slice(0, 3) as NearbyTask[]
  } catch { /* 用户拒绝或超时，静默失败 */ }
  finally { locationLoading.value = false }
  /* #endif */
}

const loadSidebarData = async () => {
  try {
    const [pubResult, accResult, statsResult, hotResult, userStatsResult, pointsResult] = await Promise.allSettled([
      userStore.isLoggedIn ? getMyPublishedTasks({ pageSize: 100 }) : Promise.resolve(null),
      userStore.isLoggedIn ? getMyAcceptedTasks({ pageSize: 100 }) : Promise.resolve(null),
      getTodayStats(),
      getTaskList({ sortBy: 'reward_points', sortOrder: 'desc', pageSize: 3, status: 0 }),
      userStore.isLoggedIn ? getUserStats() : Promise.resolve(null),
      userStore.isLoggedIn ? getPointsLog(1, 100) : Promise.resolve(null)
    ])
    if (pubResult.status === 'fulfilled' && pubResult.value) {
      const list = pubResult.value.list || []
      myStats.value.pending = list.filter((t: TaskListItem) => t.status === 0).length
      myStats.value.done = list.filter((t: TaskListItem) => t.status === 4 || t.status === 2).length
    }
    if (accResult.status === 'fulfilled' && accResult.value) {
      const list = accResult.value.list || []
      myStats.value.ongoing = list.filter((t: TaskListItem) => t.status === 1 || t.status === 2).length
    }
    if (statsResult.status === 'fulfilled') todayPublished.value = statsResult.value.newTasks || 0
    if (hotResult.status === 'fulfilled') hotTasks.value = hotResult.value.list?.slice(0, 3) || []
    if (userStatsResult.status === 'fulfilled' && userStatsResult.value) {
      totalTaskDone.value = userStatsResult.value.taskCompleteCount || 0
    }
    if (pointsResult.status === 'fulfilled' && pointsResult.value) {
      const now = Date.now(), week = 7 * 86400000
      const logs = pointsResult.value.list || []
      const sum = (from: number, to: number) =>
        logs.filter(l => { const t = new Date(l.createdAt).getTime(); return t >= from && t < to && l.pointsChange > 0 })
            .reduce((s, l) => s + l.pointsChange, 0)
      const thisWeek = sum(now - week, now), lastWeek = sum(now - 2 * week, now - week)
      if (thisWeek > 0 || lastWeek > 0) weekTrend.value = { thisWeekPts: thisWeek, diff: thisWeek - lastWeek }
    }
  } catch { /* 侧边栏数据加载失败不影响主列表 */ }
}

// 桌面端：页面滚动监听（折叠顶部导航）
onPageScroll((e) => { isHeaderCollapsed.value = e.scrollTop > 40 })

// 移动端：scroll-view 滚动到底部加载更多
const handleScrollToLower = () => {
  if (!loadingMore.value && hasMore.value) {
    page.value++
    loadTasks()
  }
}

// 移动端：下拉刷新
const handleMobileRefresh = async () => {
  mobileRefreshing.value = true
  taskList.value = []
  await loadTasks(true)
  mobileRefreshing.value = false
}

// 移动端：bottom sheet 开关（带动画）
const openMobileSidebar = () => {
  showMobileSidebar.value = true
  nextTick(() => { bsUp.value = true })
}

const closeMobileSidebar = () => {
  bsUp.value = false
  setTimeout(() => { showMobileSidebar.value = false }, 300)
}

onMounted(() => {
  loadTasks()
  loadSidebarData()
  loadSearchHistory()
  // #ifdef H5
  window.addEventListener('resize', () => { windowWidth.value = window.innerWidth })
  // #endif
})

onUnmounted(() => {
  // #ifdef H5
  window.removeEventListener('resize', () => { windowWidth.value = window.innerWidth })
  // #endif
})

// 桌面端页面生命周期（document-level scroll）
defineExpose({
  onReachBottom: () => {
    if (!loadingMore.value && hasMore.value) { page.value++; loadTasks() }
  },
  onPullDownRefresh: () => {
    taskList.value = []
    loadTasks(true)
    setTimeout(() => { uni.stopPullDownRefresh() }, 1000)
  }
})
</script>

<style lang="scss" scoped>

// ═══════════════════════════════════════════════
// 页面容器（同时承载移动端和桌面端）
// ═══════════════════════════════════════════════
.task-hall-page {
  min-height: 100vh;
  background: $bg-page;
}

// ╔══════════════════════════════════════╗
// ║         移动端样式                   ║
// ╚══════════════════════════════════════╝

.mobile-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background: $bg-page;
}

// ── NavBar 搜索框 ──
.m-nav-search {
  display: flex;
  align-items: center;
  background: $bg-page;
  border-radius: 36rpx;
  padding: 0 20rpx;
  height: 64rpx;
  gap: 10rpx;
  width: 100%;
  max-width: 560rpx;
}

.m-nav-search-icon {
  color: $gray-400;
  flex-shrink: 0;
}

.m-nav-search-input {
  flex: 1;
  height: 100%;
  font-size: 26rpx;
  color: $gray-800;
  background: transparent;
  border: none;
  outline: none;
  min-width: 0;

  &::placeholder { color: $text-placeholder; }
}

.m-nav-clear {
  display: flex;
  align-items: center;
  justify-content: center;
  color: $gray-400;
  padding: 6rpx;
  border-radius: 50%;
  cursor: pointer;
  flex-shrink: 0;

  &:active { background: $gray-200; }
}

.m-publish-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64rpx;
  height: 64rpx;
  background: $primary;
  border-radius: 50%;
  cursor: pointer;
  flex-shrink: 0;
  transition: opacity 0.15s;

  &:active { opacity: 0.8; }
}

.m-publish-icon { color: $white; }

// ── 搜索历史下拉 ──
.m-search-dropdown {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  background: $white;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
  z-index: 200;
  max-height: 320rpx;
  overflow: hidden;
  // CNavBar 本身 sticky top:0，dropdown 叠在其下方
  margin-top: 100rpx; // CNavBar height
}

.m-sh-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 32rpx;
  border-bottom: 1rpx solid $gray-100;
  background: $gray-50;
}

.m-sh-title {
  font-size: 24rpx;
  font-weight: 600;
  color: $gray-700;
}

.m-sh-clear {
  font-size: 24rpx;
  color: $primary;
  cursor: pointer;
  padding: 4rpx 8rpx;
}

.m-sh-list { padding: 8rpx 0; }

.m-sh-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 18rpx 32rpx;
  cursor: pointer;

  &:active { background: $gray-50; }
}

.m-sh-icon { color: $gray-400; flex-shrink: 0; }

.m-sh-text {
  flex: 1;
  font-size: 26rpx;
  color: $gray-800;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.m-sh-remove {
  color: $gray-400;
  padding: 4rpx;
  cursor: pointer;
}

.m-sh-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48rpx 32rpx;
  gap: 12rpx;
}

.m-sh-empty-icon { color: $gray-300; }
.m-sh-empty-text { font-size: 26rpx; color: $gray-400; }

// ── 筛选栏 ──
.m-filter-bar {
  background: $white;
  border-bottom: 1rpx solid $gray-100;
  display: flex;
  align-items: center;
  height: 92rpx;
  padding: 0 28rpx 0 24rpx;
  position: relative;
  flex-shrink: 0;
  gap: 0;
}

.m-type-scroll {
  flex: 1;
  min-width: 0;
  white-space: nowrap;

  /* #ifdef H5 */
  &::-webkit-scrollbar { display: none; }
  /* #endif */
}

.m-type-tabs {
  display: inline-flex;
  gap: 8rpx;
  padding-right: 16rpx;
}

.m-type-tab {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 10rpx 20rpx;
  border-radius: 100rpx;
  background: $gray-100;
  flex-shrink: 0;
  transition: all 0.18s ease;

  &.active {
    background: $primary;
    .m-tab-icon, .m-tab-label { color: $white; }
  }

  &:active { transform: scale(0.95); }
}

.m-tab-icon { color: $gray-500; flex-shrink: 0; }
.m-tab-label { font-size: 24rpx; color: $gray-600; font-weight: 500; }

// 右侧：状态 + 信息按钮
.m-filter-right {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-shrink: 0;
  padding-left: 16rpx;
  border-left: 1rpx solid $gray-100;
}

.m-status-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 10rpx 16rpx;
  border-radius: 100rpx;
  background: $gray-100;
  cursor: pointer;
  transition: all 0.15s ease;

  &.active {
    background: rgba($primary, 0.1);
    .m-status-label, .m-dropdown-arrow { color: $primary; }
  }

  &:active { opacity: 0.7; }
}

.m-status-label { font-size: 24rpx; color: $gray-600; font-weight: 500; white-space: nowrap; }
.m-dropdown-arrow {
  color: $gray-400;
  transition: transform 0.2s ease;
  &.rotated { transform: rotate(180deg); }
}

.m-info-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: $gray-100;
  position: relative;
  cursor: pointer;
  flex-shrink: 0;
  transition: background 0.15s;

  &:active { background: $gray-200; }
}

.m-info-icon { color: $gray-600; }

.m-info-dot {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: $error;
  border: 2rpx solid $white;
}

// 状态下拉菜单（移动端）
.m-status-menu {
  position: absolute;
  top: calc(100% + 4rpx);
  right: 28rpx;
  background: $white;
  border-radius: 16rpx;
  box-shadow: 0 8rpx 32rpx rgba($gray-900, 0.14);
  border: 1rpx solid $gray-200;
  min-width: 200rpx;
  z-index: 150;
  overflow: hidden;
}

.m-menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22rpx 28rpx;
  gap: $sp-4;
  transition: background 0.15s;

  &:not(:last-child) { border-bottom: 1rpx solid $gray-100; }
  &.active .m-menu-label { color: $primary; font-weight: 600; }
  &:active { background: $gray-50; }
}

.m-menu-label { font-size: 26rpx; color: $gray-700; }
.m-menu-check { color: $primary; flex-shrink: 0; }

// ── 我的任务快捷条 ──
.m-my-strip {
  display: flex;
  align-items: center;
  background: $white;
  border-bottom: 1rpx solid $gray-100;
  padding: 18rpx 28rpx;
  flex-shrink: 0;
  cursor: pointer;
  gap: 16rpx;
  transition: background 0.15s;

  &:active { background: $gray-50; }
}

.m-strip-left {
  display: flex;
  align-items: center;
  gap: 10rpx;
  flex-shrink: 0;
}

.m-strip-icon { color: $primary; }

.m-strip-title {
  font-size: 26rpx;
  font-weight: 600;
  color: $gray-800;
}

.m-strip-stats {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 20rpx;
}

.m-strip-cell {
  display: flex;
  align-items: baseline;
  gap: 6rpx;
}

.m-strip-num {
  font-size: 28rpx;
  font-weight: 700;

  &--pending { color: #1677FF; }
  &--ongoing { color: #13C2C2; }
  &--done    { color: #52C41A; }
}

.m-strip-name { font-size: 20rpx; color: $gray-400; }

.m-strip-div {
  width: 1rpx;
  height: 28rpx;
  background: $gray-200;
  flex-shrink: 0;
}

.m-strip-arrow { color: $gray-400; flex-shrink: 0; }

// ── 主列表 scroll-view ──
.m-scroll {
  flex: 1;
  min-height: 0;
}

.m-task-list {
  padding: 20rpx 24rpx 0;
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

// ── 移动端任务卡片 ──
.m-task-card {
  background: $white;
  border-radius: 20rpx;
  padding: 28rpx 28rpx 22rpx;
  box-shadow: 0 2rpx 10rpx rgba($gray-900, 0.06);
  cursor: pointer;
  transition: all 0.18s ease;

  &:active { opacity: 0.88; transform: scale(0.99); }
}

.m-card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14rpx;
}

.m-card-title {
  display: block;
  font-size: 30rpx;
  font-weight: 600;
  color: $gray-900;
  line-height: 1.4;
  margin-bottom: 10rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.m-card-excerpt {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-size: 26rpx;
  color: $gray-500;
  line-height: 1.5;
  margin-bottom: 16rpx;
}

.m-card-meta {
  display: flex;
  align-items: center;
  gap: $sp-2;
  margin-bottom: 18rpx;
  flex-wrap: nowrap;
  overflow: hidden;
}

.m-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.m-safe-bottom { height: 160rpx; }

// ── Bottom Sheet ──
.bs-overlay {
  position: fixed;
  inset: 0;
  z-index: $z-modal;
  background: rgba(0, 0, 0, 0);
  transition: background 0.3s ease;
  pointer-events: none;
  display: flex;
  align-items: flex-end;

  &--dim {
    background: rgba(0, 0, 0, 0.45);
    pointer-events: auto;
  }
}

.bs-sheet {
  width: 100%;
  background: $white;
  border-radius: 32rpx 32rpx 0 0;
  box-shadow: 0 -8px 32px rgba(0, 0, 0, 0.12);
  transform: translateY(100%);
  transition: transform 0.3s cubic-bezier(0.32, 0.72, 0, 1);
  pointer-events: auto;
  max-height: 85vh;
  display: flex;
  flex-direction: column;

  &--up { transform: translateY(0); }
}

.bs-bar {
  width: 60rpx;
  height: 6rpx;
  background: $gray-300;
  border-radius: 100rpx;
  margin: 18rpx auto 0;
  flex-shrink: 0;
}

.bs-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 32rpx 16rpx;
  flex-shrink: 0;
  border-bottom: 1rpx solid $gray-100;
}

.bs-title {
  font-size: 32rpx;
  font-weight: 700;
  color: $gray-900;
}

.bs-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: $gray-100;
  cursor: pointer;
  transition: background 0.15s;

  &:active { background: $gray-200; }
}

.bs-close-icon { color: $gray-500; }

.bs-scroll {
  flex: 1;
  min-height: 0;
}

// BS 卡片
.bs-card {
  margin: 20rpx 28rpx 0;
  background: $gray-50;
  border-radius: 20rpx;
  padding: 24rpx;

  &--compact { padding: 20rpx 24rpx; }
}

.bs-card-header {
  display: flex;
  align-items: center;
  gap: 10rpx;
  margin-bottom: 18rpx;
}

.bs-card-icon {
  color: $primary;

  &--hot    { color: #FF6B35; }
  &--nearby { color: #52C41A; }
}

.bs-card-title {
  font-size: 26rpx;
  font-weight: 600;
  color: $gray-800;
  flex: 1;
}

// BS 我的任务统计
.bs-stats-row {
  display: flex;
  align-items: center;
  justify-content: space-around;
  margin-bottom: 18rpx;
}

.bs-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
}

.bs-stat-div {
  width: 1rpx;
  height: 48rpx;
  background: $gray-200;
}

.bs-stat-num {
  font-size: 40rpx;
  font-weight: 800;
  line-height: 1;

  &--pending { color: #1677FF; }
  &--ongoing { color: #13C2C2; }
  &--done    { color: #52C41A; }
}

.bs-stat-label { font-size: 22rpx; color: $gray-500; }

// BS 成就横幅
.bs-achievement {
  display: flex;
  align-items: center;
  gap: 8rpx;
  background: rgba($primary, 0.06);
  border-radius: 12rpx;
  padding: 14rpx 16rpx;
  margin-bottom: 18rpx;
}

.bs-award-icon { color: #F59E0B; flex-shrink: 0; }

.bs-achievement-text { font-size: 22rpx; color: $gray-600; line-height: 1.4; }

// BS 快捷按钮
.bs-quick-row {
  display: flex;
  gap: 12rpx;
}

.bs-quick-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  height: 72rpx;
  border-radius: 12rpx;
  background: $white;
  border: 1rpx solid $gray-200;
  cursor: pointer;
  transition: all 0.15s;

  &:active { background: $gray-50; }
}

.bs-quick-icon { color: $primary; }
.bs-quick-label { font-size: 24rpx; color: $gray-700; font-weight: 500; }

// BS 高积分任务
.bs-hot-list { display: flex; flex-direction: column; gap: 0; }

.bs-hot-item {
  display: flex;
  align-items: center;
  gap: 14rpx;
  padding: 16rpx 0;
  border-bottom: 1rpx solid $gray-100;
  cursor: pointer;

  &:last-child { border-bottom: none; }
  &:active { opacity: 0.7; }
}

.bs-hot-rank {
  font-size: 26rpx;
  font-weight: 800;
  width: 32rpx;
  text-align: center;
  flex-shrink: 0;

  &.rank-1 { color: #FF4D4F; }
  &.rank-2 { color: #FA8C16; }
  &.rank-3 { color: #FAAD14; }
}

.bs-hot-title {
  flex: 1;
  font-size: 26rpx;
  color: $gray-700;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bs-hot-reward {
  display: flex;
  align-items: center;
  gap: 4rpx;
  flex-shrink: 0;
}

.bs-hot-icon { color: #FF6B35; }
.bs-hot-pts { font-size: 24rpx; font-weight: 700; color: #FF6B35; }

// BS 今日动态
.bs-platform-row {
  display: flex;
  align-items: center;
  justify-content: space-around;
}

.bs-platform-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}

.bs-platform-div {
  width: 1rpx;
  height: 48rpx;
  background: $gray-200;
}

.bs-platform-num { font-size: 36rpx; font-weight: 700; color: $primary; }
.bs-platform-label { font-size: 22rpx; color: $gray-400; }

// BS 附近任务
.bs-nearby-refresh { margin-left: auto; color: $gray-400; cursor: pointer; }
.bs-nearby-list { display: flex; flex-direction: column; gap: 0; }

.bs-nearby-item {
  display: flex;
  align-items: center;
  padding: 16rpx 0;
  border-bottom: 1rpx solid $gray-100;
  cursor: pointer;

  &:last-child { border-bottom: none; }
  &:active { opacity: 0.7; }
}

.bs-nearby-info { flex: 1; min-width: 0; }
.bs-nearby-title { font-size: 26rpx; color: $gray-700; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.bs-nearby-meta {
  display: flex;
  align-items: center;
  gap: 6rpx;
  margin-top: 6rpx;
}

.bs-nearby-icon { color: $gray-400; }
.bs-nearby-dist { font-size: 22rpx; color: $gray-400; }
.bs-nearby-dot { font-size: 22rpx; color: $gray-300; }
.bs-nearby-pts { font-size: 22rpx; color: #FF6B35; font-weight: 600; }
.bs-nearby-arrow { color: $gray-400; flex-shrink: 0; margin-left: 8rpx; }

// BS 定位引导
.bs-location-prompt {
  display: flex;
  align-items: center;
  gap: 14rpx;
  margin: 20rpx 28rpx 0;
  padding: 20rpx 24rpx;
  background: rgba($primary, 0.05);
  border-radius: 16rpx;
  border: 1rpx dashed rgba($primary, 0.3);
  cursor: pointer;

  &:active { background: rgba($primary, 0.1); }
}

.bs-location-icon { color: $primary; flex-shrink: 0; }
.bs-location-text { flex: 1; font-size: 26rpx; color: $primary; }
.bs-location-arrow { color: $primary; flex-shrink: 0; }

.bs-safe-bottom { height: 48rpx; }


// ╔══════════════════════════════════════╗
// ║         桌面端样式（原有，完整保留）  ║
// ╚══════════════════════════════════════╝

.top-nav-fixed {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: $z-dropdown + 10;
  background: $white;
  border-bottom: 1rpx solid $gray-200;
  box-shadow: 0 2rpx 6rpx rgba($gray-900, 0.08);
  transition: all 0.18s cubic-bezier(0.25, 0.1, 0.25, 1);

  &.collapsed {
    box-shadow: 0 4rpx 16rpx rgba($gray-900, 0.12);
    .top-nav-container { height: 96rpx; }
    .brand-logo { min-width: 200rpx; .logo-text { font-size: 30rpx; } }
    .compact-search-bar { height: 64rpx; }
    .publish-btn { height: 64rpx; padding: 0 28rpx; }
  }
}

.top-nav-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 80rpx;
  height: 120rpx;
  display: flex;
  align-items: center;
  gap: 32rpx;
  transition: height 0.18s cubic-bezier(0.25, 0.1, 0.25, 1);

  @media (max-width: 1200px) { padding: 0 64rpx; }
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex-shrink: 0;
  min-width: 240rpx;
  transition: min-width 0.18s cubic-bezier(0.25, 0.1, 0.25, 1);
}

.logo-icon { color: $primary; }

.logo-text {
  font-size: 32rpx;
  font-weight: $font-weight-bold;
  color: $gray-900;
  white-space: nowrap;
  transition: font-size 0.18s;
}

.search-wrapper {
  position: relative;
  flex: 1;
  max-width: 960rpx;
  margin: 0 auto;
  min-width: 0;
}

.compact-search-bar {
  width: 100%;
  height: 72rpx;
  display: flex;
  align-items: center;
  background: $bg-page;
  border-radius: 36rpx;
  padding: 0 28rpx;
  gap: 16rpx;
  transition: all 0.18s cubic-bezier(0.25, 0.1, 0.25, 1);

  &:focus-within {
    background: $gray-100;
    box-shadow: 0 0 0 4rpx rgba($primary, 0.1);
  }
}

.search-icon { color: $gray-600; flex-shrink: 0; }

.search-input {
  flex: 1;
  height: 100%;
  font-size: 28rpx;
  color: $gray-800;
  background: transparent;
  border: none;
  outline: none;
  min-width: 0;

  &::placeholder { color: $text-placeholder; }
}

.clear-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  color: $gray-600;
  cursor: pointer;
  padding: 8rpx;
  border-radius: 50%;
  flex-shrink: 0;
  transition: all 0.2s;

  &:hover { background: $gray-200; color: $gray-900; }
  &:active { transform: scale(0.9); }
}

.search-history-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 8px;
  background: $white;
  border-radius: 12px;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.12), 0 0 0 1px rgba(0, 0, 0, 0.05);
  max-height: 360px;
  overflow: hidden;
  z-index: 200;
  animation: searchDropdownIn 0.18s ease-out;
}

@keyframes searchDropdownIn {
  from { opacity: 0; transform: translateY(-8px); }
  to { opacity: 1; transform: translateY(0); }
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid $gray-100;
  background: $gray-50;
}

.history-title {
  font-size: 14px;
  font-weight: 600;
  color: $gray-800;
  display: flex;
  align-items: center;
  gap: 6px;

  &::before {
    content: '';
    display: block;
    width: 3px;
    height: 14px;
    background: $primary;
    border-radius: 2px;
  }
}

.history-clear {
  font-size: 13px;
  color: $primary;
  font-weight: 500;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: all 0.2s;

  &:hover { background: rgba($primary, 0.1); }
  &:active { opacity: 0.7; }
}

.history-list {
  padding: 8px;
  max-height: 300px;
  overflow-y: auto;

  /* #ifdef H5 */
  &::-webkit-scrollbar { width: 6px; }
  &::-webkit-scrollbar-track { background: transparent; }
  &::-webkit-scrollbar-thumb { background: $gray-300; border-radius: 3px; }
  /* #endif */
}

.history-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: $gray-50;
    transform: translateX(2px);
    .history-remove { opacity: 1; }
  }

  &:active { background: $gray-100; transform: translateX(0); }
}

.history-icon {
  color: $gray-400;
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $gray-100;
  border-radius: 50%;
  padding: 3px;
}

.history-text {
  flex: 1;
  font-size: 14px;
  color: $gray-800;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-remove {
  color: $gray-400;
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  flex-shrink: 0;
  opacity: 0;
  transition: all 0.2s;

  &:hover { background: rgba($error, 0.1); color: $error; }
  &:active { transform: scale(0.9); }
}

.history-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 20px;
  text-align: center;
}

.history-empty-icon { color: $gray-300; margin-bottom: 12px; }

.empty-text {
  font-size: 15px;
  color: $gray-600;
  font-weight: 500;
  margin-bottom: 6px;
}

.empty-hint {
  font-size: 13px;
  color: $gray-400;
}

.publish-btn {
  display: flex;
  align-items: center;
  gap: 12rpx;
  height: 72rpx;
  background: $primary;
  padding: 0 36rpx;
  border-radius: 36rpx;
  flex-shrink: 0;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: $primary-light;
    transform: translateY(-2rpx);
    box-shadow: 0 4rpx 12rpx rgba($primary, 0.3);
  }

  &:active { opacity: 0.85; transform: scale(0.96); }
}

.publish-icon { color: $white; }

.publish-text {
  font-size: $font-size-sm;
  font-weight: $font-weight-semibold;
  color: $white;
}

.nav-spacer {
  height: 120rpx;
}

.sticky-nav {
  position: sticky;
  top: 120rpx;
  z-index: $z-dropdown;
  background: $white;
  border-bottom: 1rpx solid $gray-100;
  box-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.02);
  transition: all 0.18s cubic-bezier(0.25, 0.1, 0.25, 1);
  overflow: hidden;

  &.header-collapsed {
    max-height: 0;
    opacity: 0;
    border-bottom: none;
    box-shadow: none;
  }
}

.filter-nav-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  gap: 32rpx;

  @media (max-width: 1200px) { padding: 0 64rpx; }
}

.type-tabs-wrap {
  position: relative;
  flex: 1;
  min-width: 0;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    right: 0; top: 0; bottom: 0;
    width: 32px;
    background: linear-gradient(to right, transparent, $white);
    pointer-events: none;
    z-index: 1;
  }
}

.type-scroll {
  white-space: nowrap;

  /* #ifdef H5 */
  &::-webkit-scrollbar { display: none; }
  /* #endif */
}

.type-tabs {
  display: inline-flex;
  padding-right: 32px;
  gap: $sp-3;
}

.type-tab {
  display: inline-flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-2 $sp-5;
  border-radius: $radius-2xl;
  background: $gray-100;
  transition: $transition-slow;
  flex-shrink: 0;

  &.active {
    background: $primary;
    .type-tab-icon, .type-tab-label { color: $white; }
  }

  &:active { transform: scale(0.95); }
}

.type-tab-icon { color: $gray-500; flex-shrink: 0; }
.type-tab-label { font-size: $font-size-sm; color: $gray-600; font-weight: $font-weight-medium; }

.status-dropdown-wrap {
  position: relative;
  flex-shrink: 0;
  padding-left: $sp-5;
  border-left: 1rpx solid $gray-200;
}

.status-dropdown-btn {
  display: flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-2 $sp-4;
  border-radius: $radius-2xl;
  background: $gray-100;
  cursor: pointer;
  transition: $transition-slow;

  &.active {
    background: rgba($primary, 0.1);
    .status-dropdown-label, .dropdown-arrow { color: $primary; }
  }

  &:active { transform: scale(0.96); }
}

.status-dropdown-label {
  font-size: $font-size-sm;
  color: $gray-600;
  font-weight: $font-weight-medium;
  white-space: nowrap;
}

.dropdown-arrow {
  color: $gray-400;
  transition: transform 0.2s ease;
  &.rotated { transform: rotate(180deg); }
}

.status-dropdown-menu {
  position: absolute;
  top: calc(100% + 8rpx);
  right: 0;
  background: $white;
  border-radius: $radius-lg;
  box-shadow: 0 8rpx 32rpx rgba($gray-900, 0.14);
  border: 1rpx solid $gray-200;
  min-width: 200rpx;
  z-index: $z-dropdown + 5;
  overflow: hidden;
}

.dropdown-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $sp-5 $sp-6;
  gap: $sp-4;
  transition: background 0.15s;

  &:not(:last-child) { border-bottom: 1rpx solid $gray-100; }
  &.active .dropdown-item-label { color: $primary; font-weight: $font-weight-semibold; }
  &:active { background: $gray-50; }
}

.dropdown-item-label { font-size: $font-size-sm; color: $gray-700; }
.dropdown-item-check { color: $primary; flex-shrink: 0; }

.dropdown-mask {
  position: fixed;
  inset: 0;
  z-index: $z-dropdown + 4;
}

.main-content {
  padding-bottom: 80rpx;
  background: $bg-page;
}

.content-container {
  max-width: 1160px;
  margin: 0 auto;
  padding: $sp-6 $sp-8 0;
  display: flex;
  align-items: flex-start;
  gap: 48rpx;
}

.task-main {
  flex: 1;
  min-width: 0;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: $sp-5;
}

.task-card {
  background: $white;
  border-radius: $radius-card;
  padding: $sp-6 $sp-7;
  box-shadow: 0 2rpx 12rpx rgba($gray-900, 0.06);
  transition: all 0.2s ease;
  cursor: pointer;

  &:active { opacity: 0.88; }

  /* #ifdef H5 */
  &:hover {
    transform: translateY(-4rpx);
    box-shadow: 0 8rpx 24rpx rgba($gray-900, 0.12);
  }
  /* #endif */
}

.card-row1 {
  display: flex;
  align-items: flex-start;
  gap: $sp-3;
  margin-bottom: $sp-4;
}

.card-row1-left {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: $sp-3;
}

.type-badge {
  display: inline-flex;
  align-items: center;
  gap: $sp-2;
  padding: 4rpx $sp-4;
  border-radius: $radius-2xl;
  flex-shrink: 0;

  &.type-errand {
    background: rgba(#F59E0B, 0.12);
    .type-badge-icon, .type-badge-label { color: #D97706; }
  }
  &.type-borrow {
    background: $primary-100;
    .type-badge-icon, .type-badge-label { color: $primary; }
  }
  &.type-tutor {
    background: rgba(#8B5CF6, 0.1);
    .type-badge-icon, .type-badge-label { color: #7C3AED; }
  }
  &.type-other {
    background: $gray-100;
    .type-badge-icon, .type-badge-label { color: $gray-500; }
  }
}

.type-badge-icon { flex-shrink: 0; }
.type-badge-label { font-size: 22rpx; font-weight: $font-weight-medium; }

.card-title {
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: $gray-800;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}

.card-excerpt {
  display: block;
  font-size: $font-size-sm;
  color: $gray-500;
  line-height: 1.5;
  margin-bottom: $sp-4;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.status-tag {
  padding: 4rpx $sp-4;
  border-radius: $radius-sm;
  font-size: 22rpx;
  font-weight: $font-weight-medium;
  flex-shrink: 0;
  white-space: nowrap;

  &.status-0 { background: rgba(#1677FF, 0.1); color: #1677FF; }
  &.status-1 { background: rgba(#13C2C2, 0.1); color: #13C2C2; }
  &.status-2 { background: rgba(#1677FF, 0.08); color: #1677FF; }
  &.status-3 { background: rgba(#722ED1, 0.1); color: #722ED1; }
  &.status-4 { background: rgba(#52C41A, 0.1); color: #52C41A; }
  &.status-5 { background: $gray-100; color: $gray-400; }
  &.status-6 { background: rgba(#FAAD14, 0.1); color: #FAAD14; }
  &.status-expired { background: $gray-100; color: $gray-400; }
}

.status-text { display: block; }

.card-row2 {
  display: flex;
  align-items: center;
  gap: $sp-2;
  margin-bottom: $sp-4;
}

.meta-avatar { flex-shrink: 0; }

.avatar-placeholder {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-char {
  font-size: 18rpx;
  font-weight: $font-weight-bold;
  color: $white;
}

.meta-name { font-size: $font-size-xs; color: $gray-600; font-weight: $font-weight-medium; flex-shrink: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 160rpx; }
.meta-dot { font-size: $font-size-sm; color: $gray-400; flex-shrink: 0; }
.meta-time { font-size: $font-size-xs; color: $gray-400; flex-shrink: 0; }

.meta-loc {
  display: flex;
  align-items: center;
  gap: $sp-1;
  flex-shrink: 1;
  overflow: hidden;
  margin-left: $sp-2;
}

.meta-loc-icon { color: $gray-400; flex-shrink: 0; }
.meta-loc-text {
  font-size: $font-size-xs;
  color: $gray-400;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-row3 {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.quick-accept-btn {
  padding: 10rpx 28rpx;
  background: $primary;
  color: $white;
  border-radius: 40rpx;
  font-size: $font-size-xs;
  font-weight: $font-weight-medium;
  cursor: pointer;
  transition: opacity 0.15s;

  &:active { opacity: 0.8; }

  /* #ifdef H5 */
  &:hover { opacity: 0.88; }
  /* #endif */
}

.reward-pill {
  display: inline-flex;
  align-items: center;
  gap: $sp-2;
  background: rgba(#FF6B35, 0.1);
  color: #FF6B35;
  padding: $sp-2 $sp-5;
  border-radius: $radius-2xl;
}

.reward-icon { flex-shrink: 0; }
.reward-pts { font-size: $font-size-base; font-weight: $font-weight-bold; }
.reward-unit { font-size: 22rpx; }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 160rpx $sp-8;
}

.empty-icon { color: $gray-300; margin-bottom: $sp-8; }
.empty-tip { font-size: $font-size-sm; color: $gray-400; text-align: center; margin-bottom: $sp-8; }

.empty-action-btn {
  display: inline-flex;
  align-items: center;
  gap: $sp-2;
  padding: $sp-4 $sp-8;
  border-radius: $radius-2xl;
  border: 1rpx solid $gray-300;
  background: transparent;
  color: $gray-600;
  transition: $transition-slow;

  &.primary {
    background: $primary;
    border-color: $primary;
    color: $white;
  }

  &:active { opacity: 0.85; transform: scale(0.97); }
}

.empty-action-text { font-size: $font-size-sm; font-weight: $font-weight-medium; }

.load-more {
  @include flex-center;
  padding: $sp-8;
}

.load-more-text { font-size: $font-size-sm; color: $gray-400; }

.safe-area-bottom { height: 120rpx; }

.task-sidebar {
  width: 560rpx;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: $sp-6;
  position: sticky;
  top: 240rpx;

  @include mobile {
    display: none;

    &.mobile-open {
      display: flex;
      position: fixed;
      right: 0;
      top: 0;
      bottom: 0;
      z-index: $z-modal;
      width: 80vw;
      max-width: 340px;
      background: $white;
      overflow-y: auto;
      padding: $sp-6;
      box-shadow: -4rpx 0 32rpx rgba($gray-900, 0.15);
      animation: slideInRight 0.25s cubic-bezier(0.25, 0.46, 0.45, 0.94);
    }
  }
}

@keyframes slideInRight {
  from { transform: translateX(100%); opacity: 0; }
  to   { transform: translateX(0);    opacity: 1; }
}

.mobile-sidebar-close {
  display: none;

  @include mobile {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    margin-bottom: $sp-4;
    color: $gray-500;
    cursor: pointer;
  }
}

.mobile-sidebar-backdrop {
  display: none;

  @include mobile {
    display: block;
    position: fixed;
    inset: 0;
    z-index: $z-modal - 1;
    background: rgba($gray-900, 0.4);
    backdrop-filter: blur(2px);
    animation: fadeIn 0.2s ease;
  }
}

.sidebar-card {
  background: $white;
  border-radius: $radius-card;
  padding: $sp-7;
  box-shadow: 0 2rpx 12rpx rgba($gray-900, 0.06);
}

.sidebar-card-header {
  display: flex;
  align-items: center;
  gap: $sp-3;
  margin-bottom: $sp-6;
  padding-bottom: $sp-5;
  border-bottom: 1rpx solid $gray-100;
}

.sidebar-header-icon { color: $primary; }

.sidebar-card-title {
  font-size: $font-size-base;
  font-weight: $font-weight-semibold;
  color: $gray-800;
  flex: 1;
}

.my-stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: $sp-4;
  margin-bottom: $sp-5;
}

.stat-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $sp-2;
  padding: $sp-5;
  background: $gray-50;
  border-radius: $radius-lg;
}

.stat-num {
  font-size: 40rpx;
  font-weight: $font-weight-bold;
  line-height: 1;

  &.stat-pending { color: #1677FF; }
  &.stat-ongoing { color: #13C2C2; }
  &.stat-done    { color: #52C41A; }
}

.stat-label { font-size: $font-size-xs; color: $gray-400; }

.achievement-banner {
  display: flex;
  align-items: flex-start;
  gap: $sp-3;
  background: rgba($primary, 0.05);
  border-radius: $radius-lg;
  padding: $sp-5;
  margin-bottom: $sp-5;
}

.achievement-icon { color: #F59E0B; flex-shrink: 0; margin-top: 2rpx; }
.achievement-content { display: flex; flex-direction: column; gap: $sp-2; }

.achievement-text {
  font-size: $font-size-xs;
  color: $gray-600;
  line-height: 1.5;
}

.achievement-highlight { color: $primary; font-weight: $font-weight-semibold; }

.trend-row {
  display: flex;
  align-items: center;
  gap: $sp-2;
}

.trend-icon { flex-shrink: 0; }

.trend-text {
  font-size: 22rpx;
  font-weight: $font-weight-medium;

  &.trend-up   { color: #52C41A; }
  &.trend-down { color: #FF4D4F; }
}

.sidebar-link-row {
  display: flex;
  gap: $sp-4;
}

.sidebar-quick-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $sp-2;
  padding: $sp-4;
  background: $gray-50;
  border-radius: $radius-lg;
  cursor: pointer;
  transition: $transition-slow;

  &:hover { background: $primary-100; }
  &:active { transform: scale(0.96); }
}

.quick-btn-icon { color: $primary; }
.quick-btn-label { font-size: $font-size-xs; color: $gray-700; font-weight: $font-weight-medium; }

.platform-stats {
  display: flex;
  align-items: center;
  justify-content: space-around;
}

.platform-stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $sp-2;
}

.platform-stat-divider {
  width: 1rpx;
  height: 48rpx;
  background: $gray-100;
}

.platform-stat-num { font-size: $font-size-xl; font-weight: $font-weight-bold; color: $primary; }
.platform-stat-label { font-size: $font-size-xs; color: $gray-400; }

.hot-task-list { display: flex; flex-direction: column; gap: $sp-1; }

.hot-task-item {
  display: flex;
  align-items: center;
  gap: $sp-3;
  padding: $sp-4 0;
  border-bottom: 1rpx solid $gray-100;
  cursor: pointer;

  &:last-child { border-bottom: none; }
  &:active { opacity: 0.7; }

  /* #ifdef H5 */
  &:hover { .hot-title { color: $primary; } }
  /* #endif */
}

.hot-rank {
  font-size: 26rpx;
  font-weight: $font-weight-bold;
  width: 24rpx;
  text-align: center;
  flex-shrink: 0;

  &.rank-1 { color: #FF4D4F; }
  &.rank-2 { color: #FA8C16; }
  &.rank-3 { color: #FAAD14; }
  color: $gray-400;
}

.hot-title {
  flex: 1;
  font-size: $font-size-xs;
  color: $gray-700;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s;
}

.hot-reward {
  display: flex;
  align-items: center;
  gap: $sp-1;
  flex-shrink: 0;
  color: #FF6B35;
}

.hot-pts { font-size: $font-size-xs; font-weight: $font-weight-semibold; }

.nearby-task-list { display: flex; flex-direction: column; gap: $sp-1; }

.nearby-task-item {
  display: flex;
  align-items: center;
  padding: $sp-4 0;
  border-bottom: 1rpx solid $gray-100;
  cursor: pointer;

  &:last-child { border-bottom: none; }
  &:active { opacity: 0.7; }
}

.nearby-task-info { flex: 1; min-width: 0; }
.nearby-task-title { font-size: $font-size-xs; color: $gray-700; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.nearby-task-meta {
  display: flex;
  align-items: center;
  gap: $sp-2;
  margin-top: $sp-1;
}

.nearby-meta-icon { color: $gray-400; }
.nearby-dist { font-size: 22rpx; color: $gray-400; }
.nearby-sep { font-size: 22rpx; color: $gray-300; }
.nearby-pts-icon { color: #FF6B35; }
.nearby-pts { font-size: 22rpx; color: #FF6B35; font-weight: $font-weight-semibold; }
.nearby-arrow { color: $gray-400; flex-shrink: 0; margin-left: auto; }

.nearby-icon { color: #52C41A !important; }
.nearby-refresh { margin-left: auto; color: $gray-400; cursor: pointer; }
.nearby-refresh-icon { transition: transform 0.6s linear; &.spinning { animation: spin 1s linear infinite; } }

.location-prompt {
  display: flex;
  align-items: center;
  gap: $sp-3;
  padding: $sp-5 $sp-6;
  background: rgba($primary, 0.04);
  border-radius: $radius-lg;
  border: 1rpx dashed rgba($primary, 0.25);
  cursor: pointer;
  transition: $transition-slow;

  &:hover { background: rgba($primary, 0.08); }
  &:active { opacity: 0.75; }
}

.location-prompt-icon { color: $primary; flex-shrink: 0; }
.location-prompt-text { flex: 1; font-size: $font-size-xs; color: $primary; }
.location-prompt-arrow { color: $primary; flex-shrink: 0; }

@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

</style>
