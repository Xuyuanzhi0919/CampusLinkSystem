<template>
  <view class="manage-page">
    <CNavBar
      :title="club?.clubName ? `管理 · ${club.clubName}` : '管理社团'"
      :show-back="true"
      :border="true"
    />

    <!-- 骨架屏 -->
    <view v-if="loading" class="skeleton-wrapper">
      <view class="skeleton-tabs">
        <view v-for="i in 2" :key="i" class="skeleton-tab" />
      </view>
      <view class="skeleton-card">
        <view v-for="i in 4" :key="i" class="skeleton-row" />
      </view>
    </view>

    <!-- 主内容 -->
    <view v-else-if="club">

      <!-- ═══════════════ PC 双栏布局 ═══════════════ -->
      <view v-if="isPC" class="pc-layout">

        <!-- 侧边栏 -->
        <view class="pc-sidebar">
          <view class="sidebar-club">
            <view class="sidebar-avatar">
              <image
                v-if="club.logoUrl"
                :src="club.logoUrl"
                class="sidebar-avatar-img"
                mode="aspectFill"
              />
              <text v-else class="sidebar-avatar-text">{{ club.clubName?.charAt(0) || '社' }}</text>
            </view>
            <text class="sidebar-club-name">{{ club.clubName }}</text>
          </view>

          <view class="sidebar-divider" />

          <view class="sidebar-nav">
            <view
              v-for="tab in tabs"
              :key="tab.value"
              class="sidebar-nav-item"
              :class="{ 'sidebar-nav-item--active': activeTab === tab.value }"
              @click="activeTab = tab.value"
            >
              <ClIcon
                :name="tab.icon"
                size="sm"
                :color="activeTab === tab.value ? '#7C9082' : '#8A8A8A'"
              />
              <text
                class="sidebar-nav-label"
                :class="{ 'sidebar-nav-label--active': activeTab === tab.value }"
              >{{ tab.label }}</text>
            </view>
          </view>
        </view>

        <!-- 主内容区 -->
        <view class="pc-content">

          <!-- PC: 基本设置 -->
          <view v-if="activeTab === 'info'" class="pc-panel">
            <view class="panel-header">
              <view class="panel-title-group">
                <text class="panel-title">基本设置</text>
                <text class="panel-subtitle">编辑社团基础信息</text>
              </view>
            </view>
            <view class="panel-body">
              <view class="logo-section" @click="handlePickLogo">
                <view class="logo-preview" :class="{ 'logo-preview--uploading': uploading }">
                  <image v-if="form.logoUrl" :src="form.logoUrl" class="logo-img" mode="aspectFill" />
                  <view v-else class="logo-placeholder">
                    <text class="logo-placeholder-text">{{ club.clubName?.charAt(0) || '社' }}</text>
                  </view>
                  <view class="logo-overlay">
                    <text class="logo-overlay-text">{{ uploading ? '上传中...' : '点击更换' }}</text>
                  </view>
                </view>
                <text class="logo-hint-text">点击更换 Logo</text>
              </view>
              <view class="form-section">
                <view class="form-item">
                  <text class="form-label">社团名称 <text class="required">*</text></text>
                  <input v-model="form.clubName" class="form-input" placeholder="请输入社团名称" maxlength="100" />
                </view>
                <view class="form-item">
                  <text class="form-label">分类</text>
                  <view class="category-grid">
                    <view
                      v-for="cat in CATEGORIES"
                      :key="cat"
                      class="category-chip"
                      :class="{ 'category-chip--active': form.category === cat }"
                      @click="form.category = cat"
                    >{{ cat }}</view>
                  </view>
                </view>
                <view class="form-item">
                  <text class="form-label">社团简介</text>
                  <textarea
                    v-model="form.description"
                    class="form-textarea"
                    placeholder="介绍一下你的社团..."
                    maxlength="2000"
                    auto-height
                  />
                  <text class="char-count">{{ form.description?.length ?? 0 }}/2000</text>
                </view>
                <CButton type="primary" size="lg" block :loading="saving" @click="handleSaveInfo">
                  保存修改
                </CButton>
              </view>
            </view>
          </view>

          <!-- PC: 社团动态 -->
          <view v-else-if="activeTab === 'posts'" class="pc-panel">
            <view class="panel-header">
              <text class="panel-title">社团动态</text>
            </view>
            <view class="panel-body">
              <view class="post-compose">
                <textarea
                  v-model="newPostContent"
                  class="post-textarea"
                  placeholder="发布一条社团动态，让大家了解社团最新动向..."
                  maxlength="1000"
                  auto-height
                />
                <view class="post-compose-footer">
                  <text class="char-count">{{ newPostContent.length }}/1000</text>
                  <CButton type="primary" size="sm" :loading="postSubmitting" @click="handlePublishPost">发布</CButton>
                </view>
              </view>
              <view v-if="postsLoading && posts.length === 0" class="center-tip">
                <uni-load-more status="loading" />
              </view>
              <view v-else class="posts-list">
                <view v-for="post in posts" :key="post.id" class="post-card">
                  <view class="post-header">
                    <ClAvatar :src="post.userAvatar || undefined" :name="post.userName" size="medium" />
                    <view class="post-meta">
                      <text class="post-username">{{ post.userName }}</text>
                      <text class="post-time">{{ formatTime(post.createdAt) }}</text>
                    </view>
                    <CButton
                      v-if="isAdmin || post.userId === currentUserId"
                      type="text"
                      size="sm"
                      class="danger-btn"
                      @click="handleDeletePost(post)"
                    >删除</CButton>
                  </view>
                  <text class="post-content">{{ post.content }}</text>
                  <view class="post-stats">
                    <text class="post-stat">❤️ {{ post.likes }}</text>
                    <text class="post-stat">💬 {{ post.comments }}</text>
                  </view>
                </view>
                <view v-if="!postsLoading && posts.length === 0" class="empty-tip">
                  <text>暂无动态</text>
                  <text class="empty-hint">发布第一条动态，让大家了解社团！</text>
                </view>
                <view v-if="postsHasMore" class="load-more" @click="loadPosts(false)">
                  <text class="load-more-text">加载更多</text>
                </view>
              </view>
            </view>
          </view>

          <!-- PC: 资料库 -->
          <view v-else-if="activeTab === 'resources'" class="pc-panel">
            <view class="panel-header">
              <view class="panel-title-group">
                <text class="panel-title">资料库</text>
                <text class="panel-subtitle">共 {{ resources.length }} 个文件，展示社团成员上传并通过审核的资源</text>
              </view>
              <CButton v-if="isAdmin" type="primary" size="sm" @click="handleUploadResource">
                上传资源
              </CButton>
            </view>
            <view class="panel-body">
              <view v-if="resourcesLoading && resources.length === 0" class="center-tip">
                <uni-load-more status="loading" />
              </view>
              <view v-else>
                <view class="pc-table">
                  <view class="pc-table-header">
                    <view class="pc-table-cell cell-name">文件名</view>
                    <view class="pc-table-cell cell-uploader">上传者</view>
                    <view class="pc-table-cell cell-size">大小</view>
                    <view class="pc-table-cell cell-time">上传时间</view>
                    <view class="pc-table-cell cell-action">操作</view>
                  </view>
                  <view
                    v-for="res in resources"
                    :key="res.id"
                    class="pc-table-row"
                    @click="navigateToResource(res.id)"
                  >
                    <view class="pc-table-cell cell-name">
                      <view class="file-type-badge" :style="{ background: getFileTypeColor(res.fileType) }">
                        <text class="file-type-text">{{ getFileTypeLabel(res.fileType) }}</text>
                      </view>
                      <text class="pc-resource-title">{{ res.title }}</text>
                    </view>
                    <view class="pc-table-cell cell-uploader">
                      <text class="pc-cell-text">{{ res.uploaderName || '未知' }}</text>
                    </view>
                    <view class="pc-table-cell cell-size">
                      <text class="pc-cell-text">{{ res.fileSize }}</text>
                    </view>
                    <view class="pc-table-cell cell-time">
                      <text class="pc-cell-text">{{ formatTime(res.uploadTime) }}</text>
                    </view>
                    <view class="pc-table-cell cell-action">
                      <text class="pc-action-link" @click.stop="navigateToResource(res.id)">查看</text>
                    </view>
                  </view>
                </view>
                <view v-if="!resourcesLoading && resources.length === 0" class="empty-tip">
                  <text>暂无资料</text>
                  <text class="empty-hint">成员上传并审核通过的资源将在此展示</text>
                </view>
                <view v-if="resourcesHasMore" class="load-more" @click="loadResources(false)">
                  <text class="load-more-text">加载更多</text>
                </view>
              </view>
            </view>
          </view>

          <!-- PC: 活动管理 -->
          <view v-else-if="activeTab === 'activities'" class="pc-panel">
            <view class="panel-header">
              <view class="panel-title-group">
                <text class="panel-title">活动管理</text>
              </view>
              <CButton v-if="isAdmin" type="primary" size="sm" @click="navigateToPublishActivity">
                + 发布新活动
              </CButton>
            </view>
            <view class="panel-body">
              <view v-if="activitiesLoading && activities.length === 0" class="center-tip">
                <uni-load-more status="loading" />
              </view>
              <view v-else class="activities-list">
                <view
                  v-for="act in activities"
                  :key="act.activityId"
                  class="activity-card"
                  @click="navigateToActivity(act.activityId)"
                >
                  <view class="activity-cover" v-if="act.coverImage">
                    <image :src="act.coverImage" class="activity-cover-img" mode="aspectFill" />
                  </view>
                  <view class="activity-info">
                    <view class="activity-title-row">
                      <text class="activity-title">{{ act.title }}</text>
                      <view :class="['activity-status-badge', ACTIVITY_STATUS_COLORS[act.status]]">
                        {{ ACTIVITY_STATUS_LABELS[act.status] }}
                      </view>
                    </view>
                    <text class="activity-time">🕐 {{ formatDateTime(act.startTime) }}</text>
                    <text class="activity-location">📍 {{ act.location }}</text>
                    <view class="activity-participants-row">
                      <text class="activity-participants">
                        {{ act.currentParticipants }}/{{ act.maxParticipants }} 人报名
                      </text>
                    </view>
                  </view>
                </view>
                <view v-if="!activitiesLoading && activities.length === 0" class="empty-tip">
                  <text>暂无活动</text>
                  <text v-if="isAdmin" class="empty-hint">点击上方按钮发布第一个活动吧！</text>
                </view>
                <view v-if="activitiesHasMore" class="load-more" @click="loadActivities(false)">
                  <text class="load-more-text">加载更多</text>
                </view>
              </view>
            </view>
          </view>

          <!-- PC: 成员管理 -->
          <view v-else-if="activeTab === 'members'" class="pc-panel">
            <view class="panel-header">
              <view class="panel-title-group">
                <text class="panel-title">成员管理</text>
                <text class="panel-subtitle">共 {{ members.length }} 名成员，管理社团成员角色与权限</text>
              </view>
            </view>
            <view class="panel-body">
              <view v-if="membersLoading" class="center-tip">
                <uni-load-more status="loading" />
              </view>
              <view v-else>
                <view class="pc-table">
                  <view class="pc-table-header">
                    <view class="pc-table-cell cell-member">成员</view>
                    <view class="pc-table-cell cell-role">角色</view>
                    <view class="pc-table-cell cell-join">加入时间</view>
                    <view class="pc-table-cell cell-action">操作</view>
                  </view>
                  <view
                    v-for="member in members"
                    :key="member.userId"
                    class="pc-table-row"
                  >
                    <view class="pc-table-cell cell-member">
                      <ClAvatar :src="member.avatarUrl" :name="member.nickname" size="medium" />
                      <text class="pc-member-name">{{ member.nickname }}</text>
                    </view>
                    <view class="pc-table-cell cell-role">
                      <view class="role-badge" :class="`role-badge--${member.role}`">
                        {{ ROLE_LABELS[member.role] || member.role }}
                      </view>
                    </view>
                    <view class="pc-table-cell cell-join">
                      <text class="pc-cell-text">{{ formatJoinTime(member.joinedAt) }}</text>
                    </view>
                    <view class="pc-table-cell cell-action">
                      <template v-if="member.role !== 'founder'">
                        <template v-if="isFounder">
                          <text
                            v-if="member.role === 'member'"
                            class="pc-action-link"
                            @click="handlePromote(member)"
                          >设为管理员</text>
                          <text
                            v-else-if="member.role === 'admin'"
                            class="pc-action-link"
                            @click="handleDemote(member)"
                          >降为成员</text>
                        </template>
                        <text
                          v-if="isFounder || (isAdmin && member.role === 'member')"
                          class="pc-action-link pc-action-danger"
                          @click="handleRemove(member)"
                        >移除</text>
                      </template>
                      <text v-else class="pc-cell-text pc-cell-muted">—</text>
                    </view>
                  </view>
                </view>
                <view v-if="!membersLoading && members.length === 0" class="empty-tip">
                  <text>暂无成员</text>
                </view>
                <view v-if="membersHasMore" class="load-more" @click="loadMoreMembers">
                  <text class="load-more-text">加载更多</text>
                </view>
              </view>
            </view>
          </view>

        </view>
      </view>

      <!-- ═══════════════ 移动端布局 ═══════════════ -->
      <view v-else class="mobile-layout">

        <!-- 社团信息横幅 -->
        <view class="mobile-banner">
          <view class="banner-avatar">
            <image
              v-if="club.logoUrl"
              :src="club.logoUrl"
              class="banner-avatar-img"
              mode="aspectFill"
            />
            <text v-else class="banner-avatar-text">{{ club.clubName?.charAt(0) || '社' }}</text>
          </view>
          <view class="banner-info">
            <text class="banner-name">{{ club.clubName }}</text>
            <text class="banner-meta">{{ club.category || '未分类' }} · {{ club.memberCount || 0 }} 名成员</text>
          </view>
        </view>

        <!-- Tab 栏 -->
        <scroll-view scroll-x class="tab-scroll-wrap" :show-scrollbar="false">
          <view class="tab-bar">
            <view
              v-for="tab in tabs"
              :key="tab.value"
              class="tab-item"
              :class="{ 'tab-item--active': activeTab === tab.value }"
              @click="activeTab = tab.value"
            >
              {{ tab.label }}
            </view>
          </view>
        </scroll-view>

        <!-- ── 基本设置 ── -->
        <view v-if="activeTab === 'info'" class="tab-panel">
          <view class="logo-section" @click="handlePickLogo">
            <view class="logo-preview" :class="{ 'logo-preview--uploading': uploading }">
              <image v-if="form.logoUrl" :src="form.logoUrl" class="logo-img" mode="aspectFill" />
              <view v-else class="logo-placeholder">
                <text class="logo-placeholder-text">{{ club.clubName?.charAt(0) || '社' }}</text>
              </view>
              <view class="logo-overlay">
                <text class="logo-overlay-text">{{ uploading ? '上传中...' : '点击更换' }}</text>
              </view>
            </view>
            <text class="logo-hint-text">点击更换 Logo</text>
          </view>
          <view class="form-section">
            <view class="form-item">
              <text class="form-label">社团名称 <text class="required">*</text></text>
              <input v-model="form.clubName" class="form-input" placeholder="请输入社团名称" maxlength="100" />
            </view>
            <view class="form-item">
              <text class="form-label">分类</text>
              <view class="category-grid">
                <view
                  v-for="cat in CATEGORIES"
                  :key="cat"
                  class="category-chip"
                  :class="{ 'category-chip--active': form.category === cat }"
                  @click="form.category = cat"
                >{{ cat }}</view>
              </view>
            </view>
            <view class="form-item">
              <text class="form-label">社团简介</text>
              <textarea
                v-model="form.description"
                class="form-textarea"
                placeholder="介绍一下你的社团..."
                maxlength="2000"
                auto-height
              />
              <text class="char-count">{{ form.description?.length ?? 0 }}/2000</text>
            </view>
            <CButton type="primary" size="lg" block :loading="saving" @click="handleSaveInfo">
              保存修改
            </CButton>
          </view>
        </view>

        <!-- ── 社团动态 ── -->
        <view v-else-if="activeTab === 'posts'" class="tab-panel">
          <view class="post-compose">
            <textarea
              v-model="newPostContent"
              class="post-textarea"
              placeholder="发布一条社团动态，让大家了解社团最新动向..."
              maxlength="1000"
              auto-height
            />
            <view class="post-compose-footer">
              <text class="char-count">{{ newPostContent.length }}/1000</text>
              <CButton type="primary" size="sm" :loading="postSubmitting" @click="handlePublishPost">发布</CButton>
            </view>
          </view>
          <view v-if="postsLoading && posts.length === 0" class="center-tip">
            <uni-load-more status="loading" />
          </view>
          <view v-else class="posts-list">
            <view v-for="post in posts" :key="post.id" class="post-card">
              <view class="post-header">
                <ClAvatar :src="post.userAvatar || undefined" :name="post.userName" size="medium" />
                <view class="post-meta">
                  <text class="post-username">{{ post.userName }}</text>
                  <text class="post-time">{{ formatTime(post.createdAt) }}</text>
                </view>
                <CButton
                  v-if="isAdmin || post.userId === currentUserId"
                  type="text"
                  size="sm"
                  class="danger-btn"
                  @click="handleDeletePost(post)"
                >删除</CButton>
              </view>
              <text class="post-content">{{ post.content }}</text>
              <view class="post-stats">
                <text class="post-stat">❤️ {{ post.likes }}</text>
                <text class="post-stat">💬 {{ post.comments }}</text>
              </view>
            </view>
            <view v-if="!postsLoading && posts.length === 0" class="empty-tip">
              <text>暂无动态</text>
              <text class="empty-hint">发布第一条动态，让大家了解社团！</text>
            </view>
            <view v-if="postsHasMore" class="load-more" @click="loadPosts(false)">
              <text class="load-more-text">加载更多</text>
            </view>
          </view>
        </view>

        <!-- ── 资料库 ── -->
        <view v-else-if="activeTab === 'resources'" class="tab-panel">
          <view class="resources-toolbar">
            <text class="resources-count">共 {{ resources.length }} 个文件</text>
            <CButton v-if="isAdmin" type="primary" size="sm" @click="handleUploadResource">
              上传资源
            </CButton>
          </view>
          <view v-if="resourcesLoading && resources.length === 0" class="center-tip">
            <uni-load-more status="loading" />
          </view>
          <view v-else class="resources-list">
            <view
              v-for="res in resources"
              :key="res.id"
              class="resource-card"
              @click="navigateToResource(res.id)"
            >
              <view class="file-type-badge" :style="{ background: getFileTypeColor(res.fileType) }">
                <text class="file-type-text">{{ getFileTypeLabel(res.fileType) }}</text>
              </view>
              <view class="resource-info">
                <text class="resource-title">{{ res.title }}</text>
                <text class="resource-meta">
                  {{ res.uploaderName || '未知' }} · {{ res.fileSize }} · {{ formatTime(res.uploadTime) }}
                </text>
              </view>
              <ClIcon name="icon-chevron-right" size="sm" color="#ccc" />
            </view>
            <view v-if="!resourcesLoading && resources.length === 0" class="empty-tip">
              <text>暂无资料</text>
              <text class="empty-hint">成员上传并审核通过的资源将在此展示</text>
            </view>
            <view v-if="resourcesHasMore" class="load-more" @click="loadResources(false)">
              <text class="load-more-text">加载更多</text>
            </view>
          </view>
        </view>

        <!-- ── 活动管理 ── -->
        <view v-else-if="activeTab === 'activities'" class="tab-panel">
          <view v-if="isAdmin" class="activity-action-bar">
            <CButton type="primary" size="sm" @click="navigateToPublishActivity">
              + 发布新活动
            </CButton>
          </view>
          <view v-if="activitiesLoading && activities.length === 0" class="center-tip">
            <uni-load-more status="loading" />
          </view>
          <view v-else class="activities-list">
            <view
              v-for="act in activities"
              :key="act.activityId"
              class="activity-card"
              @click="navigateToActivity(act.activityId)"
            >
              <view class="activity-cover" v-if="act.coverImage">
                <image :src="act.coverImage" class="activity-cover-img" mode="aspectFill" />
              </view>
              <view class="activity-info">
                <view class="activity-title-row">
                  <text class="activity-title">{{ act.title }}</text>
                  <view :class="['activity-status-badge', ACTIVITY_STATUS_COLORS[act.status]]">
                    {{ ACTIVITY_STATUS_LABELS[act.status] }}
                  </view>
                </view>
                <text class="activity-time">🕐 {{ formatDateTime(act.startTime) }}</text>
                <text class="activity-location">📍 {{ act.location }}</text>
                <view class="activity-participants-row">
                  <text class="activity-participants">
                    {{ act.currentParticipants }}/{{ act.maxParticipants }} 人报名
                  </text>
                </view>
              </view>
            </view>
            <view v-if="!activitiesLoading && activities.length === 0" class="empty-tip">
              <text>暂无活动</text>
              <text v-if="isAdmin" class="empty-hint">点击上方按钮发布第一个活动吧！</text>
            </view>
            <view v-if="activitiesHasMore" class="load-more" @click="loadActivities(false)">
              <text class="load-more-text">加载更多</text>
            </view>
          </view>
        </view>

        <!-- ── 成员管理 ── -->
        <view v-else-if="activeTab === 'members'" class="tab-panel">
          <view v-if="membersLoading" class="center-tip">
            <uni-load-more status="loading" />
          </view>
          <view v-else class="members-list">
            <view
              v-for="member in members"
              :key="member.userId"
              class="member-card"
            >
              <view class="member-left">
                <ClAvatar :src="member.avatarUrl" :name="member.nickname" size="large" />
                <view class="member-info">
                  <text class="member-name">{{ member.nickname }}</text>
                  <view class="member-meta">
                    <view class="role-badge" :class="`role-badge--${member.role}`">
                      {{ ROLE_LABELS[member.role] || member.role }}
                    </view>
                    <text class="join-time">{{ formatJoinTime(member.joinedAt) }} 加入</text>
                  </view>
                </view>
              </view>
              <view v-if="member.role !== 'founder'" class="member-actions">
                <template v-if="isFounder">
                  <CButton
                    v-if="member.role === 'member'"
                    type="text"
                    size="sm"
                    @click="handlePromote(member)"
                  >设为管理员</CButton>
                  <CButton
                    v-else-if="member.role === 'admin'"
                    type="text"
                    size="sm"
                    @click="handleDemote(member)"
                  >取消管理员</CButton>
                </template>
                <CButton
                  v-if="isFounder || (isAdmin && member.role === 'member')"
                  type="text"
                  size="sm"
                  class="danger-btn"
                  @click="handleRemove(member)"
                >移除</CButton>
              </view>
              <view v-else class="founder-badge">
                <text>创始人</text>
              </view>
            </view>
            <view v-if="members.length === 0" class="empty-tip">
              <text>暂无成员</text>
            </view>
            <view v-if="membersHasMore" class="load-more" @click="loadMoreMembers">
              <text class="load-more-text">加载更多</text>
            </view>
          </view>
        </view>

      </view>
    </view>

    <!-- 错误状态 -->
    <view v-else class="error-state">
      <text class="error-text">加载失败，请返回重试</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import CNavBar from '@/components/layout/CNavBar.vue'
import CButton from '@/components/ui/CButton.vue'
import ClAvatar from '@/components/cl/ClAvatar.vue'
import ClIcon from '@/components/cl/ClIcon.vue'
import { useUserStore } from '@/stores/user'
import {
  getClubDetail,
  getClubMembers,
  updateClub,
  removeMember,
  updateMemberRole,
  getClubPosts,
  createClubPost,
  deleteClubPost,
  getClubResources,
  type ClubPost,
  type ClubResource,
} from '@/services/club'
import { getActivityList } from '@/services/club'
import { getOSSSignature, uploadToOSS } from '@/utils/upload'
import type { ClubDetail, ClubMember, ActivityItem } from '@/types/club'
import dayjs from 'dayjs'

// ─── 常量 ───────────────────────────────────────────
const CATEGORIES = ['学术科技', '文化艺术', '体育竞技', '志愿公益', '创新创业', '兴趣爱好']
const ROLE_LABELS: Record<string, string> = {
  founder: '创始人',
  admin: '管理员',
  member: '成员',
}
const ACTIVITY_STATUS_LABELS: Record<number, string> = {
  0: '未开始',
  1: '报名中',
  2: '已结束',
  3: '已取消',
}
const ACTIVITY_STATUS_COLORS: Record<number, string> = {
  0: 'status-pending',
  1: 'status-active',
  2: 'status-ended',
  3: 'status-cancelled',
}
const FILE_TYPE_MAP: Record<string, string> = {
  pdf: 'PDF', doc: 'DOC', docx: 'DOC', xls: 'XLS', xlsx: 'XLS',
  ppt: 'PPT', pptx: 'PPT', txt: 'TXT', zip: 'ZIP', rar: 'ZIP',
  jpg: 'IMG', jpeg: 'IMG', png: 'IMG', gif: 'IMG', mp4: 'MP4',
}
const tabs = [
  { value: 'info',       label: '基本设置', icon: 'icon-settings'   },
  { value: 'posts',      label: '社团动态', icon: 'icon-activity'  },
  { value: 'resources',  label: '资料库',   icon: 'icon-book'       },
  { value: 'activities', label: '活动管理', icon: 'icon-calendar'   },
  { value: 'members',    label: '成员管理', icon: 'icon-user-group' },
]

// ─── 响应式布局 ──────────────────────────────────────
const windowWidth = ref(uni.getSystemInfoSync().windowWidth)
const isPC = computed(() => windowWidth.value > 768)

// ─── 状态 ───────────────────────────────────────────
const userStore = useUserStore()
const clubId = ref<number>(0)
const club = ref<ClubDetail | null>(null)
const loading = ref(true)
const saving = ref(false)
const uploading = ref(false)
const activeTab = ref('info')

const members = ref<ClubMember[]>([])
const membersLoading = ref(false)
const membersPage = ref(1)
const membersHasMore = ref(true)

const posts = ref<ClubPost[]>([])
const postsLoading = ref(false)
const postsPage = ref(1)
const postsHasMore = ref(true)
const newPostContent = ref('')
const postSubmitting = ref(false)

const resources = ref<ClubResource[]>([])
const resourcesLoading = ref(false)
const resourcesPage = ref(1)
const resourcesHasMore = ref(true)

const activities = ref<ActivityItem[]>([])
const activitiesLoading = ref(false)
const activitiesPage = ref(1)
const activitiesHasMore = ref(true)

const form = ref({
  clubName: '',
  description: '',
  logoUrl: '',
  category: '',
})

// ─── 计算属性 ────────────────────────────────────────
const isFounder = computed(() => club.value?.userRole === 'founder')
const isAdmin = computed(() =>
  club.value?.userRole === 'founder' || club.value?.userRole === 'admin'
)
const currentUserId = computed(() =>
  (userStore.userInfo as any)?.uid || (userStore.userInfo as any)?.userId || 0
)

// ─── 生命周期 ─────────────────────────────────────────
onLoad((options: any) => {
  const id = parseInt(options?.id, 10)
  if (!id) {
    uni.showToast({ title: '参数错误', icon: 'error' })
    setTimeout(() => uni.navigateBack(), 1500)
    return
  }
  clubId.value = id
  loadClub()
  loadMembers(true)
})

// ─── 数据加载 ─────────────────────────────────────────
async function loadClub() {
  loading.value = true
  try {
    const res = await getClubDetail(clubId.value)
    club.value = res
    form.value = {
      clubName: res.clubName,
      description: res.description ?? '',
      logoUrl: res.logoUrl ?? '',
      category: res.category ?? '',
    }
  } catch {
    uni.showToast({ title: '加载失败', icon: 'error' })
  } finally {
    loading.value = false
  }
}

async function loadMembers(reset = false) {
  if (membersLoading.value) return
  if (!reset && !membersHasMore.value) return
  if (reset) { membersPage.value = 1; membersHasMore.value = true }
  membersLoading.value = true
  try {
    const res = await getClubMembers(clubId.value, { page: membersPage.value, pageSize: 20 })
    const list = res.list ?? []
    if (reset) members.value = list; else members.value.push(...list)
    membersHasMore.value = members.value.length < res.total
    membersPage.value++
  } catch {
    uni.showToast({ title: '加载成员失败', icon: 'none' })
  } finally {
    membersLoading.value = false
  }
}

function loadMoreMembers() { loadMembers(false) }

// ─── 动态 ─────────────────────────────────────────────
async function loadPosts(reset = false) {
  if (postsLoading.value) return
  if (!reset && !postsHasMore.value) return
  if (reset) { postsPage.value = 1; postsHasMore.value = true }
  postsLoading.value = true
  try {
    const res = await getClubPosts(clubId.value, { page: postsPage.value, pageSize: 15 })
    const list = res.list ?? []
    if (reset) posts.value = list; else posts.value.push(...list)
    postsHasMore.value = posts.value.length < res.total
    postsPage.value++
  } catch {
    uni.showToast({ title: '加载动态失败', icon: 'none' })
  } finally {
    postsLoading.value = false
  }
}

async function handlePublishPost() {
  const content = newPostContent.value.trim()
  if (!content) { uni.showToast({ title: '请输入动态内容', icon: 'none' }); return }
  postSubmitting.value = true
  try {
    await createClubPost(clubId.value, content)
    newPostContent.value = ''
    uni.showToast({ title: '发布成功', icon: 'success' })
    loadPosts(true)
  } catch (e: any) {
    uni.showToast({ title: e?.message || '发布失败', icon: 'error' })
  } finally {
    postSubmitting.value = false
  }
}

function handleDeletePost(post: ClubPost) {
  uni.showModal({
    title: '删除动态',
    content: '确定删除这条动态吗？',
    confirmColor: '#ef4444',
    success: async ({ confirm }) => {
      if (!confirm) return
      try {
        await deleteClubPost(clubId.value, post.id)
        posts.value = posts.value.filter((p) => p.id !== post.id)
        uni.showToast({ title: '已删除', icon: 'success' })
      } catch (e: any) {
        uni.showToast({ title: e?.message || '删除失败', icon: 'error' })
      }
    },
  })
}

// ─── 资料库 ───────────────────────────────────────────
async function loadResources(reset = false) {
  if (resourcesLoading.value) return
  if (!reset && !resourcesHasMore.value) return
  if (reset) { resourcesPage.value = 1; resourcesHasMore.value = true }
  resourcesLoading.value = true
  try {
    const res = await getClubResources(clubId.value, { page: resourcesPage.value, pageSize: 15 })
    const list = res.list ?? []
    if (reset) resources.value = list; else resources.value.push(...list)
    resourcesHasMore.value = resources.value.length < res.total
    resourcesPage.value++
  } catch {
    uni.showToast({ title: '加载资料失败', icon: 'none' })
  } finally {
    resourcesLoading.value = false
  }
}

function navigateToResource(resourceId: number) {
  uni.navigateTo({ url: `/pages/resource/detail?id=${resourceId}` })
}

function handleUploadResource() {
  uni.navigateTo({ url: `/pages/resource/upload?clubId=${clubId.value}` })
}

function getFileTypeLabel(fileType: string | null): string {
  if (!fileType) return 'FILE'
  const ext = fileType.toLowerCase().split('/').pop() || fileType.toLowerCase()
  return FILE_TYPE_MAP[ext] || ext.toUpperCase().slice(0, 4)
}

function getFileTypeColor(fileType: string | null): string {
  const label = getFileTypeLabel(fileType)
  const colors: Record<string, string> = {
    PDF: '#ef4444', DOC: '#3b82f6', XLS: '#22c55e',
    PPT: '#f97316', IMG: '#a855f7', MP4: '#ec4899',
    ZIP: '#f59e0b', TXT: '#6b7280',
  }
  return colors[label] || '#6b7280'
}

// ─── 活动管理 ─────────────────────────────────────────
async function loadActivities(reset = false) {
  if (activitiesLoading.value) return
  if (!reset && !activitiesHasMore.value) return
  if (reset) { activitiesPage.value = 1; activitiesHasMore.value = true }
  activitiesLoading.value = true
  try {
    const res = await getActivityList({ clubId: clubId.value, page: activitiesPage.value, pageSize: 10 })
    const list = res.list ?? []
    if (reset) activities.value = list; else activities.value.push(...list)
    activitiesHasMore.value = activities.value.length < res.total
    activitiesPage.value++
  } catch {
    uni.showToast({ title: '加载活动失败', icon: 'none' })
  } finally {
    activitiesLoading.value = false
  }
}

function navigateToPublishActivity() {
  uni.navigateTo({ url: `/pages/club/publish-activity?clubId=${clubId.value}` })
}

function navigateToActivity(activityId: number) {
  uni.navigateTo({ url: `/pages/club/activity-detail?id=${activityId}` })
}

// ─── Tab 懒加载 ───────────────────────────────────────
watch(activeTab, (tab) => {
  if (tab === 'posts' && posts.value.length === 0) loadPosts(true)
  if (tab === 'resources' && resources.value.length === 0) loadResources(true)
  if (tab === 'activities' && activities.value.length === 0) loadActivities(true)
})

// ─── 操作 ─────────────────────────────────────────────
function handlePickLogo() {
  if (uploading.value) return
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const tempPath = res.tempFilePaths[0]
      uploading.value = true
      try {
        const fileName = tempPath.split('/').pop() || 'logo.jpg'
        const signature = await getOSSSignature(fileName)
        const url = await uploadToOSS(tempPath, signature)
        form.value.logoUrl = url
      } catch {
        uni.showToast({ title: 'Logo 上传失败', icon: 'none' })
      } finally {
        uploading.value = false
      }
    },
  })
}

async function handleSaveInfo() {
  if (!form.value.clubName.trim()) {
    uni.showToast({ title: '社团名称不能为空', icon: 'none' })
    return
  }
  saving.value = true
  try {
    await updateClub(clubId.value, {
      clubName: form.value.clubName.trim(),
      description: form.value.description,
      logoUrl: form.value.logoUrl || undefined,
      category: form.value.category || undefined,
    })
    uni.showToast({ title: '保存成功', icon: 'success' })
    if (club.value) {
      club.value.clubName = form.value.clubName.trim()
      club.value.description = form.value.description
      club.value.logoUrl = form.value.logoUrl
      club.value.category = form.value.category
    }
  } catch (e: any) {
    uni.showToast({ title: e?.message || '保存失败', icon: 'error' })
  } finally {
    saving.value = false
  }
}

function handlePromote(member: ClubMember) {
  uni.showModal({
    title: '设为管理员',
    content: `确定将「${member.nickname}」设为管理员吗？`,
    success: async ({ confirm }) => {
      if (!confirm) return
      try {
        await updateMemberRole(clubId.value, member.userId, 'admin')
        member.role = 'admin'
        uni.showToast({ title: '已设为管理员', icon: 'success' })
      } catch (e: any) {
        uni.showToast({ title: e?.message || '操作失败', icon: 'error' })
      }
    },
  })
}

function handleDemote(member: ClubMember) {
  uni.showModal({
    title: '取消管理员',
    content: `确定取消「${member.nickname}」的管理员权限吗？`,
    success: async ({ confirm }) => {
      if (!confirm) return
      try {
        await updateMemberRole(clubId.value, member.userId, 'member')
        member.role = 'member'
        uni.showToast({ title: '已取消管理员', icon: 'success' })
      } catch (e: any) {
        uni.showToast({ title: e?.message || '操作失败', icon: 'error' })
      }
    },
  })
}

function handleRemove(member: ClubMember) {
  uni.showModal({
    title: '移除成员',
    content: `确定将「${member.nickname}」移出社团吗？`,
    confirmColor: '#ef4444',
    success: async ({ confirm }) => {
      if (!confirm) return
      try {
        await removeMember(clubId.value, member.userId)
        members.value = members.value.filter((m) => m.userId !== member.userId)
        if (club.value) club.value.memberCount = Math.max(0, (club.value.memberCount ?? 1) - 1)
        uni.showToast({ title: '已移除', icon: 'success' })
      } catch (e: any) {
        uni.showToast({ title: e?.message || '操作失败', icon: 'error' })
      }
    },
  })
}

// ─── 工具函数 ─────────────────────────────────────────
function formatJoinTime(time: string) {
  return dayjs(time).format('YYYY-MM-DD')
}

function formatTime(time: string) {
  const d = dayjs(time)
  const now = dayjs()
  if (now.diff(d, 'hour') < 24) return d.format('HH:mm')
  if (now.diff(d, 'day') < 7) return d.format('MM-DD HH:mm')
  return d.format('YYYY-MM-DD')
}

function formatDateTime(time: string) {
  return dayjs(time).format('MM月DD日 HH:mm')
}
</script>

<style lang="scss" scoped>
// ─── 设计令牌 ─────────────────────────────────────────
$sage: #7C9082;
$sage-light: rgba(124, 144, 130, 0.08);
$bg: #FAF8F5;
$border: #E8E4DF;
$text-primary: #2D2D2D;
$text-secondary: #5C5C5C;
$text-muted: #8A8A8A;
$white: #FFFFFF;

.manage-page {
  min-height: 100vh;
  background: $bg;
}

// ── 骨架屏 ─────────────────────────────────────────────
.skeleton-wrapper {
  padding: 16px;

  .skeleton-tabs {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
  }

  .skeleton-tab {
    height: 36px;
    width: 100px;
    border-radius: 8px;
    background: $border;
    animation: shimmer 1.5s infinite;
  }

  .skeleton-card {
    background: $white;
    border-radius: 12px;
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .skeleton-row {
    height: 20px;
    border-radius: 4px;
    background: $border;
    animation: shimmer 1.5s infinite;
  }
}

@keyframes shimmer {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

// ══════════════════════════════════════════════════════
//   PC 双栏布局
// ══════════════════════════════════════════════════════
.pc-layout {
  display: flex;
  min-height: calc(100vh - 44px);
}

// ── PC 侧边栏 ──────────────────────────────────────────
.pc-sidebar {
  width: 220px;
  flex-shrink: 0;
  background: $white;
  border-right: 1px solid $border;
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: calc(100vh - 44px);
  overflow-y: auto;
}

.sidebar-club {
  padding: 28px 20px 20px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
}

.sidebar-avatar {
  width: 72px;
  height: 72px;
  border-radius: 16px;
  background: $sage;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;

  .sidebar-avatar-img {
    width: 100%;
    height: 100%;
  }

  .sidebar-avatar-text {
    color: $white;
    font-size: 28px;
    font-weight: 700;
    font-family: 'Fraunces', serif;
  }
}

.sidebar-club-name {
  font-size: 16px;
  font-weight: 600;
  color: $text-primary;
  font-family: 'Inter', sans-serif;
}

.sidebar-divider {
  height: 1px;
  background: $border;
  margin: 0 20px;
}

.sidebar-nav {
  padding: 12px 8px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.sidebar-nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.15s;

  &--active {
    background: $sage-light;
  }

  &:not(&--active):hover {
    background: rgba(0, 0, 0, 0.03);
  }
}

.sidebar-nav-label {
  font-size: 14px;
  font-family: 'Inter', sans-serif;
  color: $text-muted;

  &--active {
    color: $sage;
    font-weight: 500;
  }
}

// ── PC 主内容 ──────────────────────────────────────────
.pc-content {
  flex: 1;
  min-width: 0;
  padding: 32px;
  overflow-y: auto;
}

.pc-panel {
  max-width: 900px;
}

.panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
}

.panel-title-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.panel-title {
  font-size: 22px;
  font-weight: 700;
  color: $text-primary;
  font-family: 'Fraunces', serif;
}

.panel-subtitle {
  font-size: 13px;
  color: $text-muted;
  font-family: 'Inter', sans-serif;
}

.panel-body {
  // 内容区留白
}

// ── PC 表格 ────────────────────────────────────────────
.pc-table-wrap {
  background: $white;
  border-radius: 12px;
  border: 1px solid $border;
  overflow: hidden;
}

.pc-table {
  background: $white;
  border-radius: 12px;
  border: 1px solid $border;
  overflow: hidden;
}

.pc-table-header {
  display: flex;
  align-items: center;
  padding: 0 20px;
  height: 44px;
  background: #F5F2EE;
  border-bottom: 1px solid $border;
}

.pc-table-row {
  display: flex;
  align-items: center;
  padding: 0 20px;
  min-height: 56px;
  border-bottom: 1px solid $border;
  cursor: pointer;
  transition: background 0.15s;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: rgba(0, 0, 0, 0.02);
  }
}

.pc-table-cell {
  font-size: 13px;
  color: $text-secondary;
  font-family: 'Inter', sans-serif;
  display: flex;
  align-items: center;
  gap: 10px;

  // 表头特殊样式
  .pc-table-header & {
    font-size: 12px;
    font-weight: 500;
    color: $text-muted;
  }
}

// 列宽分配
.cell-name     { flex: 2; min-width: 0; }
.cell-uploader { flex: 1; }
.cell-size     { width: 80px; flex-shrink: 0; }
.cell-time     { width: 110px; flex-shrink: 0; }
.cell-member   { flex: 2; min-width: 0; gap: 12px; }
.cell-role     { width: 90px; flex-shrink: 0; }
.cell-join     { width: 110px; flex-shrink: 0; }
.cell-action   { width: 120px; flex-shrink: 0; display: flex; gap: 12px; }

.pc-resource-title {
  font-size: 14px;
  font-weight: 500;
  color: $text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pc-member-name {
  font-size: 14px;
  font-weight: 500;
  color: $text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pc-cell-text {
  font-size: 13px;
  color: $text-secondary;
}

.pc-cell-muted {
  color: $text-muted;
}

.pc-action-link {
  font-size: 13px;
  color: $sage;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

.pc-action-danger {
  color: #ef4444;
}

// ══════════════════════════════════════════════════════
//   移动端布局
// ══════════════════════════════════════════════════════
.mobile-layout {
  display: flex;
  flex-direction: column;
}

// ── 社团横幅 ──────────────────────────────────────────
.mobile-banner {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: $white;
  border-bottom: 1px solid $border;
}

.banner-avatar {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: $sage;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;

  .banner-avatar-img {
    width: 100%;
    height: 100%;
  }

  .banner-avatar-text {
    color: $white;
    font-size: 16px;
    font-weight: 700;
    font-family: 'Fraunces', serif;
  }
}

.banner-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.banner-name {
  font-size: 14px;
  font-weight: 500;
  color: $text-primary;
  font-family: 'Inter', sans-serif;
}

.banner-meta {
  font-size: 12px;
  color: $text-muted;
  font-family: 'Inter', sans-serif;
}

// ── Tab 栏 ─────────────────────────────────────────────
.tab-scroll-wrap {
  background: $white;
  border-bottom: 1px solid $border;
  white-space: nowrap;
}

.tab-bar {
  display: flex;
  padding: 0 4px;
}

.tab-item {
  flex-shrink: 0;
  padding: 12px 14px;
  font-size: 13px;
  font-family: 'Inter', sans-serif;
  color: $text-muted;
  position: relative;
  cursor: pointer;

  &--active {
    color: $sage;
    font-weight: 500;

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 14px;
      right: 14px;
      height: 2px;
      background: $sage;
      border-radius: 1px;
    }
  }
}

// ── 面板 ──────────────────────────────────────────────
.tab-panel {
  padding: 16px;
}

// ── 资料库工具栏 ──────────────────────────────────────
.resources-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.resources-count {
  font-size: 13px;
  color: $text-muted;
  font-family: 'Inter', sans-serif;
}

// ══════════════════════════════════════════════════════
//   共享样式（PC + Mobile）
// ══════════════════════════════════════════════════════

// ── 基本设置 ──────────────────────────────────────────
.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
  cursor: pointer;
}

.logo-preview {
  width: 80px;
  height: 80px;
  border-radius: 16px;
  overflow: hidden;
  background: $sage;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  position: relative;

  .logo-img { width: 100%; height: 100%; }

  .logo-placeholder-text {
    color: $white;
    font-size: 28px;
    font-weight: 700;
    font-family: 'Fraunces', serif;
  }

  .logo-overlay {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.45);
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 16px;
    opacity: 0;
    transition: opacity 0.2s;

    .logo-overlay-text {
      color: $white;
      font-size: 12px;
      font-weight: 500;
    }
  }

  &:hover .logo-overlay,
  &--uploading .logo-overlay { opacity: 1; }
}

.logo-hint-text {
  font-size: 12px;
  color: $sage;
}

.form-section {
  background: $white;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: $text-primary;
  font-family: 'Inter', sans-serif;
}

.required { color: #ef4444; }

.form-input {
  height: 44px;
  padding: 0 12px;
  border: 1px solid $border;
  border-radius: 8px;
  font-size: 14px;
  background: $white;
  color: $text-primary;

  &:focus {
    border-color: $sage;
    outline: none;
  }
}

.form-textarea {
  min-height: 100px;
  padding: 12px;
  border: 1px solid $border;
  border-radius: 8px;
  font-size: 14px;
  background: $white;
  color: $text-primary;
  width: 100%;
  box-sizing: border-box;
  line-height: 1.6;
}

.char-count {
  font-size: 12px;
  color: $text-muted;
  text-align: right;
}

.category-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.category-chip {
  padding: 6px 14px;
  border-radius: 20px;
  border: 1px solid $border;
  font-size: 13px;
  color: $text-secondary;
  cursor: pointer;
  transition: all 0.15s;

  &--active {
    border-color: $sage;
    background: $sage-light;
    color: $sage;
    font-weight: 500;
  }
}

// ── 成员管理 ──────────────────────────────────────────
.members-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.member-card {
  background: $white;
  border-radius: 12px;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.member-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.member-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.member-name {
  font-size: 15px;
  font-weight: 500;
  color: $text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.member-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.role-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;

  &--founder { background: #fef3c7; color: #d97706; }
  &--admin   { background: #dcfce7; color: #16a34a; }
  &--member  { background: #f3f4f6; color: #6b7280; }
}

.join-time {
  font-size: 12px;
  color: $text-muted;
}

.member-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.founder-badge {
  font-size: 12px;
  color: #d97706;
  background: #fef3c7;
  padding: 3px 8px;
  border-radius: 4px;
  flex-shrink: 0;
}

.danger-btn {
  color: #ef4444 !important;
}

// ── 动态 ──────────────────────────────────────────────
.post-compose {
  background: $white;
  border-radius: 12px;
  padding: 14px 16px;
  margin-bottom: 14px;
}

.post-textarea {
  width: 100%;
  min-height: 80px;
  font-size: 14px;
  color: $text-primary;
  line-height: 1.6;
  box-sizing: border-box;
}

.post-compose-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
}

.posts-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.post-card {
  background: $white;
  border-radius: 12px;
  padding: 14px 16px;
}

.post-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.post-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.post-username {
  font-size: 14px;
  font-weight: 500;
  color: $text-primary;
}

.post-time {
  font-size: 12px;
  color: $text-muted;
}

.post-content {
  display: block;
  font-size: 14px;
  color: $text-secondary;
  line-height: 1.6;
  margin-bottom: 10px;
  white-space: pre-wrap;
}

.post-stats {
  display: flex;
  gap: 14px;

  .post-stat {
    font-size: 13px;
    color: $text-muted;
  }
}

// ── 资料库 ────────────────────────────────────────────
.resources-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.resource-card {
  background: $white;
  border-radius: 12px;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  active-opacity: 0.7;
}

.file-type-badge {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.file-type-text {
  font-size: 11px;
  font-weight: 700;
  color: $white;
  letter-spacing: 0.5px;
}

.resource-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.resource-title {
  font-size: 14px;
  font-weight: 500;
  color: $text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.resource-meta {
  font-size: 12px;
  color: $text-muted;
}

// ── 活动管理 ──────────────────────────────────────────
.activity-action-bar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 14px;
}

.activities-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.activity-card {
  background: $white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  active-opacity: 0.7;
}

.activity-cover {
  width: 100%;
  height: 120px;
  overflow: hidden;

  .activity-cover-img {
    width: 100%;
    height: 100%;
  }
}

.activity-info {
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.activity-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.activity-title {
  font-size: 15px;
  font-weight: 600;
  color: $text-primary;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-status-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  flex-shrink: 0;

  &.status-pending   { background: #fef3c7; color: #d97706; }
  &.status-active    { background: #dcfce7; color: #16a34a; }
  &.status-ended     { background: #f3f4f6; color: #6b7280; }
  &.status-cancelled { background: #fee2e2; color: #ef4444; }
}

.activity-time,
.activity-location {
  font-size: 13px;
  color: $text-secondary;
}

.activity-participants-row { margin-top: 2px; }

.activity-participants {
  font-size: 13px;
  color: $sage;
  font-weight: 500;
}

// ── 通用状态 ──────────────────────────────────────────
.center-tip {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.empty-tip {
  text-align: center;
  padding: 40px 0;
  color: $text-muted;
  font-size: 14px;
}

.empty-hint {
  display: block;
  font-size: 12px;
  color: #bbb;
  margin-top: 6px;
}

.load-more {
  text-align: center;
  padding: 16px;
  cursor: pointer;

  .load-more-text {
    font-size: 13px;
    color: $sage;
  }
}

.error-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;

  .error-text {
    color: $text-muted;
    font-size: 14px;
  }
}
</style>
