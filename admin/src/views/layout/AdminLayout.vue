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
.admin-layout {
  height: 100vh;
  overflow: hidden;
}

.aside {
  background: #1a1a2e;
  transition: width 0.3s;
  overflow: hidden;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #409eff, #0070f3);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
}

.brand-text {
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
}

.header {
  background: #fff;
  border-bottom: 1px solid #e8eaed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 56px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
  color: #374151;
}

.main {
  background: #f4f6fa;
  overflow-y: auto;
  padding: 24px;
}

.badge {
  margin-left: 4px;
}

:deep(.el-menu) {
  border-right: none;
}

:deep(.el-menu-item.is-active) {
  background: rgba(64, 158, 255, 0.12) !important;
  border-right: 3px solid #409eff;
}

:deep(.el-menu--collapse) {
  width: 64px;
}
</style>
