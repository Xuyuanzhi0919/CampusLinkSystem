<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="aside">
      <div class="sidebar-header">
        <div class="brand" :class="{ collapsed: isCollapsed }">
          <span class="brand-icon">CL</span>
          <span v-show="!isCollapsed" class="brand-text">CampusLink</span>
        </div>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        router
        background-color="#1a1a2e"
        text-color="#a8b3c8"
        active-text-color="#409eff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon>
          <template #title>仪表板</template>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        <el-menu-item index="/content">
          <el-icon><Document /></el-icon>
          <template #title>内容管理</template>
        </el-menu-item>
        <el-menu-item index="/reports">
          <el-icon><Warning /></el-icon>
          <template #title>
            举报管理
            <el-badge v-if="pendingReports > 0" :value="pendingReports" class="badge" />
          </template>
        </el-menu-item>
        <el-menu-item index="/tasks">
          <el-icon><List /></el-icon>
          <template #title>任务管理</template>
        </el-menu-item>
        <el-menu-item index="/clubs">
          <el-icon><Flag /></el-icon>
          <template #title>社团管理</template>
        </el-menu-item>
        <el-menu-item index="/activities">
          <el-icon><Calendar /></el-icon>
          <template #title>活动管理</template>
        </el-menu-item>
        <el-menu-item index="/notices">
          <el-icon><Bell /></el-icon>
          <template #title>公告管理</template>
        </el-menu-item>
        <el-menu-item index="/config">
          <el-icon><Setting /></el-icon>
          <template #title>系统配置</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container class="main-container">
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-button
            text
            :icon="isCollapsed ? 'Expand' : 'Fold'"
            @click="isCollapsed = !isCollapsed"
          />
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="authStore.userInfo?.avatarUrl">
                {{ authStore.userInfo?.nickname?.charAt(0) }}
              </el-avatar>
              <span>{{ authStore.userInfo?.nickname }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 页面内容 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { getPendingCount } from '@/api/report'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const isCollapsed = ref(false)
const pendingReports = ref(0)

const activeMenu = computed(() => '/' + (route.path.split('/')[1] || 'dashboard'))
const currentTitle = computed(() => {
  const map: Record<string, string> = {
    dashboard: '仪表板',
    users: '用户管理',
    content: '内容管理',
    reports: '举报管理',
    tasks: '任务管理',
    clubs: '社团管理',
    activities: '活动管理',
    notices: '公告管理',
    config: '系统配置'
  }
  return map[route.path.split('/')[1]] || ''
})

async function handleCommand(cmd: string) {
  if (cmd === 'logout') {
    await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
    authStore.logout()
    router.push('/login')
  }
}

onMounted(async () => {
  try {
    pendingReports.value = await getPendingCount()
  } catch {
    // 忽略
  }
})
</script>

<style scoped>
/* ─── 布局 ──────────────────────────────────────────────────── */
.admin-layout { height: 100vh; overflow: hidden; }

/* ─── 侧边栏 ────────────────────────────────────────────────── */
.aside {
  background: var(--cp-sidebar, #0c0920);
  background-image: radial-gradient(ellipse 140% 60% at 50% -10%,
    rgba(124,58,237,.22) 0%, transparent 70%);
  transition: width 0.28s cubic-bezier(.4,0,.2,1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 18px 14px 14px;
  border-bottom: 1px solid rgba(255,255,255,.06);
  flex-shrink: 0;
}

.brand { display: flex; align-items: center; gap: 11px; }

.brand-icon {
  width: 38px; height: 38px;
  background: linear-gradient(135deg, #7c3aed 0%, #ec4899 100%);
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; font-weight: 800; color: #fff;
  flex-shrink: 0;
  font-family: 'Outfit', sans-serif;
  letter-spacing: -0.5px;
  box-shadow: 0 0 18px rgba(124,58,237,.55), 0 0 6px rgba(236,72,153,.35);
  transition: box-shadow 0.3s;
}

.brand-text {
  color: #fff; font-size: 16px; font-weight: 700;
  white-space: nowrap; font-family: 'Outfit', sans-serif;
  letter-spacing: -0.3px;
}

/* ─── 菜单 ──────────────────────────────────────────────────── */
:deep(.el-menu) {
  border-right: none;
  background: transparent !important;
  padding: 10px 8px;
}

:deep(.el-menu-item) {
  height: 44px; line-height: 44px;
  border-radius: 10px; margin-bottom: 2px;
  padding: 0 14px !important;
  color: rgba(255,255,255,.55) !important;
  font-family: var(--cp-font-body); font-weight: 600; font-size: 14px;
  transition: all 0.2s ease;
}

:deep(.el-menu-item .el-icon) { font-size: 18px !important; transition: color 0.2s; }

:deep(.el-menu-item:hover) {
  background: rgba(255,255,255,.07) !important;
  color: rgba(255,255,255,.9) !important;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg,rgba(124,58,237,.82),rgba(168,85,247,.65)) !important;
  color: #fff !important;
  border-right: none !important;
  box-shadow: 0 4px 14px rgba(124,58,237,.38), inset 0 1px 0 rgba(255,255,255,.12);
}

:deep(.el-menu--collapse) { width: 64px; }
:deep(.el-menu--collapse .el-menu-item) { padding: 0 !important; justify-content: center; }

.badge { margin-left: 6px; }

/* ─── 顶栏 ──────────────────────────────────────────────────── */
.header {
  background: #fff;
  border-bottom: none;
  box-shadow: 0 1px 0 #ede9fe, 0 2px 16px rgba(109,40,217,.05);
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 24px; height: 60px; flex-shrink: 0;
}

.header-left { display: flex; align-items: center; gap: 14px; }

:deep(.el-breadcrumb__inner) {
  color: #6b7280 !important;
  font-family: var(--cp-font-body) !important; font-weight: 600 !important;
}
:deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #7c3aed !important; font-weight: 700 !important;
}

.header-right { display: flex; align-items: center; gap: 12px; }

.user-info {
  display: flex; align-items: center; gap: 9px;
  cursor: pointer; font-size: 14px; font-weight: 600; color: #1e1b4b;
  padding: 5px 12px 5px 5px; border-radius: 24px;
  transition: background 0.2s;
}
.user-info:hover { background: #ede9fe; }
:deep(.user-info .el-avatar) {
  box-shadow: 0 0 0 2px #fff, 0 0 0 4px rgba(124,58,237,.3);
}

/* ─── 主内容 ────────────────────────────────────────────────── */
.main {
  background: var(--cp-bg, #f5f3ff);
  overflow-y: auto; padding: 24px;
}
</style>
