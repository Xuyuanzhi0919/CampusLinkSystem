import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue'),
      meta: { guest: true }
    },
    {
      path: '/',
      component: () => import('@/views/layout/AdminLayout.vue'),
      redirect: '/dashboard',
      meta: { requiresAuth: true },
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/dashboard/DashboardView.vue'),
          meta: { title: '仪表板', icon: 'DataLine' }
        },
        {
          path: 'users',
          name: 'Users',
          component: () => import('@/views/users/UsersView.vue'),
          meta: { title: '用户管理', icon: 'User' }
        },
        {
          path: 'content',
          name: 'Content',
          component: () => import('@/views/content/ContentView.vue'),
          meta: { title: '内容管理', icon: 'Document' }
        },
        {
          path: 'reports',
          name: 'Reports',
          component: () => import('@/views/reports/ReportsView.vue'),
          meta: { title: '举报管理', icon: 'Warning' }
        },
        {
          path: 'notices',
          name: 'Notices',
          component: () => import('@/views/notices/NoticesView.vue'),
          meta: { title: '公告管理', icon: 'Bell' }
        },
        {
          path: 'config',
          name: 'Config',
          component: () => import('@/views/config/ConfigView.vue'),
          meta: { title: '系统配置', icon: 'Setting' }
        }
      ]
    },
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
  ]
})

// 路由守卫
router.beforeEach(to => {
  const token = localStorage.getItem('admin_token')
  if (to.meta.requiresAuth && !token) {
    return { name: 'Login' }
  }
  if (to.meta.guest && token) {
    return { name: 'Dashboard' }
  }
})

export default router
