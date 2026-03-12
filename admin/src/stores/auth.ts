import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { AuthInfo } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const userInfo = ref<AuthInfo | null>(
    JSON.parse(localStorage.getItem('admin_user') || 'null')
  )

  const isLoggedIn = computed(() => !!userInfo.value)
  const isAdmin = computed(() => userInfo.value?.role === 'admin')

  function setAuth(info: AuthInfo) {
    userInfo.value = info
    localStorage.setItem('admin_token', info.token)
    localStorage.setItem('admin_user', JSON.stringify(info))
  }

  function logout() {
    userInfo.value = null
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_user')
  }

  return { userInfo, isLoggedIn, isAdmin, setAuth, logout }
})
